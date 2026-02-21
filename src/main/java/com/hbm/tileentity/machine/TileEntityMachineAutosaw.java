package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockTallPlant.EnumTallFlower;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.packet.toclient.BufPacket;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.MutableVec3d;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@AutoRegister
public class TileEntityMachineAutosaw extends TileEntityLoadedBase implements IBufPacketReceiver, IFluidStandardReceiver, IFluidCopiable, ITickable {

    public static final HashSet<FluidType> acceptedFuels = new HashSet<>();

    static {
        acceptedFuels.add(Fluids.WOODOIL);
        acceptedFuels.add(Fluids.ETHANOL);
        acceptedFuels.add(Fluids.FISHOIL);
        acceptedFuels.add(Fluids.HEAVYOIL);
    }

    public FluidTankNTM tank;

    public boolean isOn;
    public boolean isSuspended;
    private int forceSkip;
    public float syncYaw;
    public float rotationYaw;
    public float prevRotationYaw;
    public float syncPitch;
    public float rotationPitch;
    public float prevRotationPitch;

    // 0: searching, 1: extending, 2: retracting
    private int state = 0;

    private int turnProgress;

    public float spin;
    public float lastSpin;

    public TileEntityMachineAutosaw() {
        this.tank = new FluidTankNTM(Fluids.WOODOIL, 100);
    }

    @Override
    public void update() {

        if (!world.isRemote) {

            if (!isSuspended && world.getTotalWorldTime() % 20 == 0) {
                if (tank.getFill() > 0) {
                    tank.setFill(tank.getFill() - 1);
                    this.isOn = true;
                } else {
                    this.isOn = false;
                }

                this.subscribeToAllAround(tank.getTankType(), this);
            }

            if (isOn && !isSuspended) {
                Vec3d pivot = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.75, pos.getZ() + 0.5);
                MutableVec3d upperArm = new MutableVec3d(0, 0, -4);
                upperArm.rotatePitchSelf((float) Math.toRadians(80 - rotationPitch));
                upperArm.rotateYawSelf(-(float) Math.toRadians(rotationYaw));
                MutableVec3d lowerArm = new MutableVec3d(0, 0, -4);
                lowerArm.rotatePitchSelf((float) -Math.toRadians(80 - rotationPitch));
                lowerArm.rotateYawSelf(-(float) Math.toRadians(rotationYaw));
                MutableVec3d armTip = new MutableVec3d(0, 0, -2);
                armTip.rotateYawSelf(-(float) Math.toRadians(rotationYaw));

                double cX = pivot.x + upperArm.x + lowerArm.x + armTip.x;
                double cY = pivot.y;
                double cZ = pivot.z + upperArm.z + lowerArm.z + armTip.z;

                List<EntityLivingBase> affected = world.getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(cX - 1, cY - 0.25, cZ - 1, cX + 1, cY + 0.25, cZ + 1));

                for (EntityLivingBase e : affected) {
                    if (e.isEntityAlive() && e.attackEntityFrom(ModDamageSource.turbofan, 100)) {
                        world.playSound(null, e.posX, e.posY, e.posZ, SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, SoundCategory.HOSTILE, 2.0F,
                                0.95F + world.rand.nextFloat() * 0.2F);
                        int count = Math.min((int) Math.ceil(e.getMaxHealth() / 4), 250);
                        NBTTagCompound data = new NBTTagCompound();
                        data.setString("type", "vanillaburst");
                        data.setInteger("count", count * 4);
                        data.setDouble("motion", 0.1D);
                        data.setString("mode", "blockdust");
                        data.setInteger("block", Block.getIdFromBlock(Blocks.REDSTONE_BLOCK));
                        PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, e.posX, e.posY + e.height * 0.5, e.posZ),
                                new TargetPoint(e.dimension, e.posX, e.posY, e.posZ, 50));
                    }
                }

                if (state == 0) {

                    this.rotationYaw += 1;

                    if (this.rotationYaw >= 360) {
                        this.rotationYaw -= 360;
                    }

                    if (forceSkip > 0) {
                        forceSkip--;
                    } else {
                        final double CUT_ANGLE = Math.toRadians(5);
                        double rotationYawRads = Math.toRadians((rotationYaw + 270) % 360);

                        BlockPos.MutableBlockPos scanPos = new BlockPos.MutableBlockPos();

                        outer:
                        for (int dx = -9; dx <= 9; dx++) {
                            for (int dz = -9; dz <= 9; dz++) {
                                int sqrDst = dx * dx + dz * dz;

                                if (sqrDst <= 4 || sqrDst > 81) continue;

                                double angle = Math.atan2(dz, dx);
                                double relAngle = Math.abs(angle - rotationYawRads);
                                relAngle = Math.abs((relAngle + Math.PI) % (2 * Math.PI) - Math.PI);

                                if (relAngle > CUT_ANGLE) continue;

                                int x = pos.getX() + dx;
                                int y = pos.getY() + 1;
                                int z = pos.getZ() + dz;

                                scanPos.setPos(x, y, z);
                                IBlockState blockState = world.getBlockState(scanPos);
                                Material mat = blockState.getMaterial();
                                if (!(mat == Material.WOOD || mat == Material.LEAVES || mat == Material.PLANTS)) continue;

                                Block block = blockState.getBlock();
                                int meta = block.getMetaFromState(blockState);
                                if (shouldIgnore(world, scanPos, blockState, block, meta)) continue;

                                state = 1;
                                break outer;
                            }
                        }
                    }
                }

                int hitY = (int) Math.floor(cY);
                int hitX0 = (int) Math.floor(cX - 0.5);
                int hitZ0 = (int) Math.floor(cZ - 0.5);
                int hitX1 = (int) Math.floor(cX + 0.5);
                int hitZ1 = (int) Math.floor(cZ + 0.5);
                BlockPos.MutableBlockPos hitPos = new BlockPos.MutableBlockPos();
                this.tryInteract(hitPos.setPos(hitX0, hitY, hitZ0));
                this.tryInteract(hitPos.setPos(hitX1, hitY, hitZ0));
                this.tryInteract(hitPos.setPos(hitX0, hitY, hitZ1));
                this.tryInteract(hitPos.setPos(hitX1, hitY, hitZ1));

                if (state == 1) {
                    this.rotationPitch += 2;

                    if (this.rotationPitch > 80) {
                        this.rotationPitch = 80;
                        state = 2;
                    }
                }

                if (state == 2) {
                    this.rotationPitch -= 2;

                    if (this.rotationPitch <= 0) {
                        this.rotationPitch = 0;
                        state = 0;
                    }
                }
            }

            PacketThreading.createAllAroundThreadedPacket(new BufPacket(pos.getX(), pos.getY(), pos.getZ(), this),
                    new TargetPoint(this.world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 100));
        } else {

            this.lastSpin = this.spin;

            if (isOn && !isSuspended) {
                this.spin += 15F;

                Vec3d vec = new Vec3d(0.625, 0, 1.625);
                vec = vec.rotateYaw(-(float) Math.toRadians(rotationYaw));

                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + 0.5 + vec.x, pos.getY() + 2.0625, pos.getZ() + 0.5 + vec.z, 0, 0, 0);
            }

            if (this.spin >= 360F) {
                this.spin -= 360F;
                this.lastSpin -= 360F;
            }

            this.prevRotationYaw = this.rotationYaw;
            this.prevRotationPitch = this.rotationPitch;

            if (this.turnProgress > 0) {
                double d0 = MathHelper.wrapDegrees(this.syncYaw - (double) this.rotationYaw);
                double d1 = MathHelper.wrapDegrees(this.syncPitch - (double) this.rotationPitch);
                this.rotationYaw = (float) ((double) this.rotationYaw + d0 / (double) this.turnProgress);
                this.rotationPitch = (float) ((double) this.rotationPitch + d1 / (double) this.turnProgress);
                --this.turnProgress;
            } else {
                this.rotationYaw = this.syncYaw;
                this.rotationPitch = this.syncPitch;
            }
        }
    }

    /**
     * Anything additionally that the detector nor the blades should pick up on, like non-mature
     * willows
     */
    private static boolean shouldIgnore(World world, BlockPos pos, IBlockState state, Block block, int meta) {
        if (block == ModBlocks.plant_tall) {
            return meta == EnumTallFlower.CD2.ordinal() + 8 || meta == EnumTallFlower.CD3.ordinal() + 8;
        }

        if (block instanceof IGrowable growable) {
            return growable.canGrow(world, pos, state, world.isRemote);
        }

        return false;
    }

    protected void tryInteract(BlockPos pos) {

        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        int meta = block.getMetaFromState(blockState);

        if (!shouldIgnore(world, pos, blockState, block, meta)) {
            Material mat = blockState.getMaterial();
            if (mat == Material.LEAVES || mat == Material.PLANTS) {
                cutCrop(pos);
            } else if (mat == Material.WOOD) {
                fellTree(pos);
                if (state == 1) {
                    state = 2;
                }
            }
        }

        // Return when hitting a wall
        if (state == 1) {
            if (world.getBlockState(pos).isNormalCube()) {
                state = 2;
                forceSkip = 5;
            }
        }
    }

    protected void cutCrop(BlockPos pos) {

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        BlockPos soilPos = pos.down();

        IBlockState soilState = world.getBlockState(soilPos);
        Block soil = soilState.getBlock();

        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        world.playEvent(2001, pos, Block.getStateId(blockState));

        IBlockState replacementState = Blocks.AIR.getDefaultState();

        if (!world.isRemote && !world.restoringBlockSnapshots) {
            NonNullList<ItemStack> drops = NonNullList.create();

            block.getDrops(drops, world, pos, blockState, 0);
            boolean replanted = false;

            for (ItemStack drop : drops) {
                if (!replanted && !drop.isEmpty() && drop.getItem() instanceof IPlantable seed) {
                    if (soil.canSustainPlant(soilState, world, soilPos, EnumFacing.UP, seed)) {
                        replacementState = seed.getPlant(world, pos);
                        replanted = true;
                        drop.shrink(1);
                    }
                }

                if (drop.isEmpty()) continue;

                float delta = 0.7F;
                double dx = (double) (world.rand.nextFloat() * delta) + (double) (1.0F - delta) * 0.5D;
                double dy = (double) (world.rand.nextFloat() * delta) + (double) (1.0F - delta) * 0.5D;
                double dz = (double) (world.rand.nextFloat() * delta) + (double) (1.0F - delta) * 0.5D;

                EntityItem entityItem = new EntityItem(world, x + dx, y + dy, z + dz, drop);
                entityItem.setPickupDelay(10);
                world.spawnEntity(entityItem);
            }

            if (block == Blocks.WHEAT && !replanted) {
                replacementState = Blocks.WHEAT.getDefaultState();
            }
        }

        world.setBlockState(pos, replacementState, 3);
    }

    private static final int[][] DIR = new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    // mlbv: made safe for leaves from other mods like BOP
    protected void fellTree(BlockPos targetPos) {
        int x = targetPos.getX();
        int y = targetPos.getY();
        int z = targetPos.getZ();

        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();

        p.setPos(x, y - 1, z);
        if (world.getBlockState(p).getMaterial() == Material.WOOD) {
            y--;
            p.setPos(x, y - 2, z);
            if (world.getBlockState(p).getMaterial() == Material.WOOD) {
                y--;
            }
        }

        IBlockState saplingState = null;

        for (int i = y; i < y + 10; i++) {

            for (int[] d : DIR) {
                p.setPos(x + d[0], i, z + d[1]);
                IBlockState state = world.getBlockState(p);

                if (state.getMaterial() == Material.WOOD) {
                    world.destroyBlock(p, true);
                    continue;
                }

                Block b = state.getBlock();
                if (b instanceof BlockLeaves) {
                    if (saplingState == null) {
                        Item saplingItem = b.getItemDropped(state, world.rand, 0);
                        if (saplingItem != Items.AIR) {
                            Block saplingBlock = Block.getBlockFromItem(saplingItem);
                            if (saplingBlock != Blocks.AIR) {
                                int sapMeta = b.damageDropped(state);
                                saplingState = saplingBlock.getStateFromMeta(sapMeta);
                            }
                        }
                    }
                    world.destroyBlock(p, true);
                }
            }
        }

        if (saplingState != null) {
            BlockPos saplingPos = new BlockPos(x, y, z);
            Block saplingBlock = saplingState.getBlock();
            if (saplingBlock != Blocks.AIR && saplingBlock.canPlaceBlockAt(world, saplingPos)) {
                world.setBlockState(saplingPos, saplingState, 3);
            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(this.isOn);
        buf.writeBoolean(this.isSuspended);
        buf.writeFloat(this.rotationYaw);
        buf.writeFloat(this.rotationPitch);
        this.tank.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.isOn = buf.readBoolean();
        this.isSuspended = buf.readBoolean();
        this.syncYaw = buf.readFloat();
        this.syncPitch = buf.readFloat();
        this.turnProgress = 3; // use 3-ply for extra smoothness
        this.tank.deserialize(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.isOn = nbt.getBoolean("isOn");
        this.isSuspended = nbt.getBoolean("isSuspended");
        this.forceSkip = nbt.getInteger("skip");
        this.rotationYaw = nbt.getFloat("yaw");
        this.rotationPitch = nbt.getFloat("pitch");
        this.state = nbt.getInteger("state");
        this.tank.readFromNBT(nbt, "t");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setBoolean("isOn", this.isOn);
        nbt.setBoolean("isSuspended", this.isSuspended);
        nbt.setInteger("skip", this.forceSkip);
        nbt.setFloat("yaw", this.rotationYaw);
        nbt.setFloat("pitch", this.rotationPitch);
        nbt.setInteger("state", this.state);
        tank.writeToNBT(nbt, "t");
        return nbt;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{tank};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tank};
    }

    AxisAlignedBB bb = null;

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb = new AxisAlignedBB(pos.getX() - 12, pos.getY(), pos.getZ() - 12, pos.getX() + 13, pos.getY() + 10, pos.getZ() + 13);
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public FluidTankNTM getTankToPaste() {
        return tank;
    }
}
