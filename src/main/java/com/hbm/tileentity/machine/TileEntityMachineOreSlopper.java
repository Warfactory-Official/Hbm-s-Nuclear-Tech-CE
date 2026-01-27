package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerOreSlopper;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIOreSlopper;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.special.ItemBedrockOreBase;
import com.hbm.items.special.ItemBedrockOreNew;
import com.hbm.items.special.ItemBedrockOreNew.BedrockOreGrade;
import com.hbm.items.special.ItemBedrockOreNew.BedrockOreType;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AutoRegister
public class TileEntityMachineOreSlopper extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiver, IGUIProvider, IFluidCopiable, ITickable {

    public static final long maxPower = 100_000;
    public static final int waterUsedBase = 1_000;
    public static final long consumptionBase = 200;
    private static final int[] slot_access = new int[]{2, 3, 4, 5, 6, 7, 8};
    private final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);
    public long power;
    public int waterUsed = waterUsedBase;
    public long consumption = consumptionBase;
    public float progress;
    public boolean processing;
    public SlopperAnimation animation = SlopperAnimation.LOWERING;
    public float slider;
    public float prevSlider;
    public float bucket;
    public float prevBucket;
    public float blades;
    public float prevBlades;
    public float fan;
    public float prevFan;
    public int delay;
    public FluidTankNTM[] tanks;
    public double[] ores = new double[BedrockOreType.VALUES.length];
    AxisAlignedBB bb = null;

    public TileEntityMachineOreSlopper() {
        super(11, true, true);
        tanks = new FluidTankNTM[2];
        tanks[0] = new FluidTankNTM(Fluids.WATER, 16_000);
        tanks[1] = new FluidTankNTM(Fluids.SLOP, 16_000);
    }

    @Override
    public String getDefaultName() {
        return "container.machineOreSlopper";
    }

    @Override
    public void update() {

        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);

        if (!world.isRemote) {

            this.power = Library.chargeTEFromItems(inventory, 0, power, maxPower);

            tanks[0].setType(1, inventory);
            FluidType conversion = this.getFluidOutput(tanks[0].getTankType());
            if (conversion != null) tanks[1].setTankType(conversion);

            for (DirPos pos : getConPos()) {
                this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                this.trySubscribe(tanks[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                if (tanks[1].getFill() > 0)
                    this.sendFluid(tanks[1], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }

            this.processing = false;

            upgradeManager.checkSlots(inventory, 9, 10);
            int speed = Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED), 3);
            int efficiency = Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.EFFECT), 3);

            this.consumption = this.consumptionBase + (this.consumptionBase * speed) / 2 + (this.consumptionBase * efficiency);

            if (canSlop()) {
                this.power -= this.consumption;
                this.progress += 1F / (600 - speed * 150);
                this.processing = true;
                boolean markDirty = false;

                while (progress >= 1F && canSlop()) {
                    progress -= 1F;

                    for (BedrockOreType type : BedrockOreType.VALUES) {
                        ores[type.ordinal()] += (ItemBedrockOreBase.getOreAmount(inventory.getStackInSlot(2), type) * (1D + efficiency * 0.1));
                    }

                    this.inventory.getStackInSlot(2).shrink(1);
                    this.tanks[0].setFill(this.tanks[0].getFill() - waterUsed);
                    this.tanks[1].setFill(this.tanks[1].getFill() + waterUsed);
                    markDirty = true;
                }

                if (markDirty) this.markDirty();

                List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.getX() - 0.5, pos.getY() + 1, pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 3, pos.getZ() + 1.5).offset(dir.offsetX, 0, dir.offsetZ));

                for (Entity e : entities) {
                    e.attackEntityFrom(ModDamageSource.turbofan, 1000F);

                    if (!e.isEntityAlive() && e instanceof EntityLivingBase) {
                        NBTTagCompound vdat = new NBTTagCompound();
                        vdat.setString("type", "giblets");
                        vdat.setInteger("ent", e.getEntityId());
                        vdat.setInteger("cDiv", 5);
                        PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(vdat, e.posX, e.posY + e.height * 0.5, e.posZ), new NetworkRegistry.TargetPoint(e.dimension, e.posX, e.posY + e.height * 0.5, e.posZ, 150));

                        world.playSound(null, e.posX, e.posY, e.posZ, SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, SoundCategory.BLOCKS, 2.0F, 0.95F + world.rand.nextFloat() * 0.2F);
                    }
                }

            } else {
                this.progress = 0;
            }

            for (BedrockOreType type : BedrockOreType.VALUES) {
                ItemStack output = ItemBedrockOreNew.make(BedrockOreGrade.BASE, type);
                outer:
                while (ores[type.ordinal()] >= 1) {
                    for (int i = 3; i <= 8; i++)
                        if (!inventory.getStackInSlot(i).isEmpty() && inventory.getStackInSlot(i).getItem() == output.getItem() && inventory.getStackInSlot(i).getItemDamage() == output.getItemDamage() && inventory.getStackInSlot(i).getCount() < output.getMaxStackSize()) {
                            inventory.getStackInSlot(i).grow(1);
                            ores[type.ordinal()] -= 1F;
                            continue outer;
                        }
                    for (int i = 3; i <= 8; i++)
                        if (inventory.getStackInSlot(i).isEmpty()) {
                            inventory.setStackInSlot(i, output);
                            ores[type.ordinal()] -= 1F;
                            continue outer;
                        }
                    break outer;
                }
            }

            this.networkPackNT(150);

        } else {

            this.prevSlider = this.slider;
            this.prevBucket = this.bucket;
            this.prevBlades = this.blades;
            this.prevFan = this.fan;

            if (this.processing) {

                this.blades += 15F;
                this.fan += 35F;

                if (blades >= 360) {
                    blades -= 360;
                    prevBlades -= 360;
                }

                if (fan >= 360) {
                    fan -= 360;
                    prevFan -= 360;
                }

                if (animation == animation.DUMPING && MainRegistry.proxy.me().getDistance(pos.getX() + 0.5, pos.getY() + 4, pos.getZ() + 0.5) <= 50) {
                    NBTTagCompound data = new NBTTagCompound();
                    data.setString("type", "vanillaExt");
                    data.setString("mode", "blockdust");
                    data.setInteger("block", Block.getIdFromBlock(Blocks.IRON_BLOCK));
                    data.setDouble("posX", pos.getX() + 0.5 + dir.offsetX + world.rand.nextGaussian() * 0.25);
                    data.setDouble("posY", pos.getY() + 4.25);
                    data.setDouble("posZ", pos.getZ() + 0.5 + dir.offsetZ + world.rand.nextGaussian() * 0.25);
                    data.setDouble("mY", -0.2D);
                    MainRegistry.proxy.effectNT(data);
                }

                if (delay > 0) {
                    delay--;
                    return;
                }

                switch (animation) {
                    case LOWERING:
                        this.bucket += 1F / 40F;
                        if (bucket >= 1F) {
                            bucket = 1F;
                            animation = SlopperAnimation.LIFTING;
                            delay = 20;
                        }
                        break;
                    case LIFTING:
                        this.bucket -= 1F / 40F;
                        if (bucket <= 0) {
                            bucket = 0F;
                            animation = SlopperAnimation.MOVE_SHREDDER;
                            delay = 10;
                        }
                        break;
                    case MOVE_SHREDDER:
                        this.slider += 1 / 50F;
                        if (slider >= 1F) {
                            slider = 1F;
                            animation = SlopperAnimation.DUMPING;
                            delay = 60;
                        }
                        break;
                    case DUMPING:
                        animation = SlopperAnimation.MOVE_BUCKET;
                        break;
                    case MOVE_BUCKET:
                        this.slider -= 1 / 50F;
                        if (slider <= 0F) {
                            animation = SlopperAnimation.LOWERING;
                            delay = 10;
                        }
                        break;
                }
            }
        }
    }

    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        return new DirPos[]{
                new DirPos(pos.getX() + dir.offsetX * 4, pos.getY(), pos.getZ() + dir.offsetZ * 4, dir),
                new DirPos(pos.getX() - dir.offsetX * 4, pos.getY(), pos.getZ() - dir.offsetZ * 4, dir.getOpposite()),
                new DirPos(pos.getX() + rot.offsetX * 2, pos.getY(), pos.getZ() + rot.offsetZ * 2, rot),
                new DirPos(pos.getX() - rot.offsetX * 2, pos.getY(), pos.getZ() - rot.offsetZ * 2, rot.getOpposite()),
                new DirPos(pos.getX() + dir.offsetX * 2 + rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * 2, rot),
                new DirPos(pos.getX() + dir.offsetX * 2 - rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 2 - rot.offsetZ * 2, rot.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX * 2 + rot.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 2 + rot.offsetZ * 2, rot),
                new DirPos(pos.getX() - dir.offsetX * 2 - rot.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 2 - rot.offsetZ * 2, rot.getOpposite())
        };
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == 2 && stack.getItem() == ModItems.bedrock_ore_base;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack stack, int j) {
        return i >= 3 && i <= 8;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return slot_access;
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(power);
        buf.writeLong(consumption);
        buf.writeFloat(progress);
        buf.writeBoolean(processing);
        tanks[0].serialize(buf);
        tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.consumption = buf.readLong();
        this.progress = buf.readFloat();
        this.processing = buf.readBoolean();
        tanks[0].deserialize(buf);
        tanks[1].deserialize(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.power = nbt.getLong("power");
        this.progress = nbt.getFloat("progress");
        tanks[0].readFromNBT(nbt, "water");
        tanks[1].readFromNBT(nbt, "slop");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setLong("power", power);
        nbt.setFloat("progress", progress);
        tanks[0].writeToNBT(nbt, "water");
        tanks[1].writeToNBT(nbt, "slop");
        return super.writeToNBT(nbt);
    }

    public boolean canSlop() {
        if (this.getFluidOutput(tanks[0].getTankType()) == null) return false;
        if (tanks[0].getFill() < waterUsed) return false;
        if (tanks[1].getFill() + waterUsed > tanks[1].getMaxFill()) return false;
        if (power < consumption) return false;

        return !inventory.getStackInSlot(2).isEmpty() && inventory.getStackInSlot(2).getItem() == ModItems.bedrock_ore_base;
    }

    public FluidType getFluidOutput(FluidType input) {
        if (input == Fluids.WATER) return Fluids.SLOP;
        return null;
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{tanks[1]};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tanks[0]};
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 3,
                    pos.getY(),
                    pos.getZ() - 3,
                    pos.getX() + 4,
                    pos.getY() + 7,
                    pos.getZ() + 4
            );
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerOreSlopper(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIOreSlopper(player.inventory, this);
    }

    public static enum SlopperAnimation {
        LOWERING, LIFTING, MOVE_SHREDDER, DUMPING, MOVE_BUCKET
    }
}
