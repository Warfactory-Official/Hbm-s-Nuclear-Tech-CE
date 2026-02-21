package com.hbm.tileentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.container.ContainerMachineRotaryFurnace;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineRotaryFurnace;
import com.hbm.inventory.gui.GuiInfoContainer;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.recipes.RotaryFurnaceRecipes;
import com.hbm.inventory.recipes.RotaryFurnaceRecipes.RotaryFurnaceRecipe;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.modules.ModuleBurnTime;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.CrucibleUtil;
import com.hbm.util.MutableVec3d;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Random;

@AutoRegister
public class TileEntityMachineRotaryFurnace extends TileEntityMachinePolluting implements IFluidStandardTransceiver, IGUIProvider, IFluidCopiable, IConfigurableMachine, ITickable {

    public FluidTankNTM[] tanks;
    public boolean isProgressing;
    public float progress;
    public int burnTime;
    public double burnHeat = 1D;
    public int maxBurnTime;
    public int steamUsed = 0;
    public boolean isVenting;
    public MaterialStack output;
    public static final int maxOutput = MaterialShapes.BLOCK.q(16);

    public int anim;
    public int lastAnim;

    /**
     * Given this has no heat, the heat mod instead affects the progress per fuel *
     */
    public static ModuleBurnTime burnModule = new ModuleBurnTime()
            .setCokeTimeMod(1.25)
            .setRocketTimeMod(1.5)
            .setSolidTimeMod(1.5)
            .setBalefireTimeMod(1.5)
            .setSolidHeatMod(1.5)
            .setRocketHeatMod(3)
            .setBalefireHeatMod(10);

    public TileEntityMachineRotaryFurnace() {
        super(5, 50, true, false);
        tanks = new FluidTankNTM[3];
        tanks[0] = new FluidTankNTM(Fluids.NONE, 16_000);
        tanks[1] = new FluidTankNTM(Fluids.STEAM, 12_000);
        tanks[2] = new FluidTankNTM(Fluids.SPENTSTEAM, 120);
    }

    @Override
    public @NotNull String getDefaultName() {
        return "container.machineRotaryFurnace";
    }

    @Override
    public void update() {

        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

        if (!world.isRemote) {

            tanks[0].setType(3, inventory);

            for (DirPos dirPos : getSteamPos()) {
                this.trySubscribe(tanks[1].getTankType(), world, dirPos.getPos().getX(), dirPos.getPos().getY(), dirPos.getPos().getZ(),
                        dirPos.getDir());
                if (tanks[2].getFill() > 0)
                    this.sendFluid(tanks[2], world, dirPos.getPos().getX(), dirPos.getPos().getY(), dirPos.getPos().getZ(), dirPos.getDir());
            }
            if (tanks[0].getTankType() != Fluids.NONE)
                for (DirPos dirPos : getFluidPos()) {
                    this.trySubscribe(tanks[0].getTankType(), world, dirPos.getPos().getX(), dirPos.getPos().getY(), dirPos.getPos().getZ(),
                            dirPos.getDir());
                }

            if (smoke.getFill() > 0)
                this.sendFluid(smoke, world, pos.getX() + rot.offsetX, pos.getY() + 5, pos.getZ() + rot.offsetZ, Library.POS_Y);

            if (this.output != null) {

                int prev = this.output.amount;
                MutableVec3d impact = new MutableVec3d(0, 0, 0);
                MaterialStack leftover = CrucibleUtil.pourSingleStack(world, pos.getX() + 0.5D + rot.offsetX * 2.875D, pos.getY() + 1.25D,
                        pos.getZ() + 0.5D + rot.offsetZ * 2.875D, 6, true, this.output, MaterialShapes.INGOT.q(1), impact);
                this.output = leftover;

                if (prev != this.output.amount) {
                    NBTTagCompound data = new NBTTagCompound();
                    data.setString("type", "foundry");
                    data.setInteger("color", leftover.material.moltenColor);
                    data.setByte("dir", (byte) rot.ordinal());
                    data.setFloat("off", 0.625F);
                    data.setFloat("base", 0.625F);
                    data.setFloat("len", Math.max(1F, pos.getY() + 1 - (float) (Math.ceil(impact.y) - 1.125)));
                    PacketThreading.createAllAroundThreadedPacket(
                            new AuxParticlePacketNT(data, pos.getX() + 0.5D + rot.offsetX * 2.875D, pos.getY() + 0.75,
                                    pos.getZ() + 0.5D + rot.offsetZ * 2.875D),
                            new TargetPoint(world.provider.getDimension(), pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 50));
                }

                if (output.amount <= 0) this.output = null;
            }

            RotaryFurnaceRecipe recipe = RotaryFurnaceRecipes.getRecipe(inventory.getStackInSlot(0), inventory.getStackInSlot(1),
                    inventory.getStackInSlot(2));
            this.isProgressing = false;

            if (recipe != null) {

                if (this.burnTime <= 0 && !inventory.getStackInSlot(4).isEmpty() && TileEntityFurnace.isItemFuel(inventory.getStackInSlot(4))) {
                    this.burnHeat = burnModule.getMod(inventory.getStackInSlot(4), burnModule.getModHeat());
                    this.maxBurnTime = this.burnTime = burnModule.getBurnTime(inventory.getStackInSlot(4)) / 2;
                    inventory.getStackInSlot(4).shrink(1);
                    markDirty();
                }

                if (this.canProcess(recipe)) {
                    float speed = Math.max((float) burnHeat, 1);
                    this.progress += speed / recipe.duration;

                    speed = (float) (13 * Math.log10(speed) + 1);
                    tanks[1].setFill((int) (tanks[1].getFill() - recipe.steam * speed));
                    steamUsed += (int) (recipe.steam * speed);
                    this.isProgressing = true;

                    if (this.progress >= 1F) {
                        this.progress -= 1F;
                        this.consumeItems(recipe);

                        if (this.output == null) {
                            this.output = recipe.output.copy();
                        } else {
                            this.output.amount += recipe.output.amount;
                        }
                        this.markDirty();
                    }

                    if (this.burnTime > 0) {
                        this.pollute(PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND / 10F);
                        this.burnTime--;
                    }

                } else {
                    this.progress = 0;
                }

                if (this.steamUsed >= 100) {
                    int steamReturn = this.steamUsed / 100;
                    int canReturn = tanks[2].getMaxFill() - tanks[2].getFill();
                    int doesReturn = Math.min(steamReturn, canReturn);
                    this.steamUsed -= doesReturn * 100;
                    tanks[2].setFill(tanks[2].getFill() + doesReturn);
                }

            } else {
                this.progress = 0;
            }

            this.isVenting = false;

            networkPackNT(50);

        } else {

            if (this.burnTime > 0 && MainRegistry.proxy.me().getDistance(pos.getX(), pos.getY(), pos.getZ()) < 25) {
                Random rand = world.rand;
                world.spawnParticle(EnumParticleTypes.FLAME, pos.getX() + 0.5 + dir.offsetX * 0.5 + rot.offsetX + rand.nextGaussian() * 0.25,
                        pos.getY() + 0.375, pos.getZ() + 0.5 + dir.offsetZ * 0.5 + rot.offsetZ + rand.nextGaussian() * 0.25, 0, 0, 0);
            }

            if (isVenting && world.getTotalWorldTime() % 2 == 0) {

                NBTTagCompound fx = new NBTTagCompound();
                fx.setString("type", "tower");
                fx.setFloat("lift", 10F);
                fx.setFloat("base", 0.25F);
                fx.setFloat("max", 2.5F);
                fx.setInteger("life", 100 + world.rand.nextInt(20));
                fx.setInteger("color", 0x202020);
                fx.setDouble("posX", pos.getX() + 0.5 + rot.offsetX);
                fx.setDouble("posY", pos.getY() + 5);
                fx.setDouble("posZ", pos.getZ() + 0.5 + rot.offsetZ);
                MainRegistry.proxy.effectNT(fx);
            }
            this.lastAnim = this.anim;
            if (this.isProgressing) {
                this.anim += (int) Math.max(burnModule.getMod(inventory.getStackInSlot(4), burnModule.getModHeat()), 1);
            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        tanks[0].serialize(buf);
        tanks[1].serialize(buf);
        tanks[2].serialize(buf);
        buf.writeBoolean(isVenting);
        buf.writeBoolean(isProgressing);
        buf.writeFloat(progress);
        buf.writeInt(burnTime);
        buf.writeInt(maxBurnTime);

        if (this.output != null) {
            buf.writeBoolean(true);
            buf.writeInt(this.output.material.id);
            buf.writeInt(this.output.amount);
        } else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        tanks[0].deserialize(buf);
        tanks[1].deserialize(buf);
        tanks[2].deserialize(buf);
        isVenting = buf.readBoolean();
        isProgressing = buf.readBoolean();
        progress = buf.readFloat();
        burnTime = buf.readInt();
        maxBurnTime = buf.readInt();

        if (buf.readBoolean()) {
            this.output = new MaterialStack(Mats.matById.get(buf.readInt()), buf.readInt());
        } else {
            this.output = null;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.tanks[0].readFromNBT(nbt, "t0");
        this.tanks[1].readFromNBT(nbt, "t1");
        this.tanks[2].readFromNBT(nbt, "t2");
        this.progress = nbt.getFloat("prog");
        this.burnTime = nbt.getInteger("burn");
        this.burnHeat = nbt.getDouble("heat");
        this.maxBurnTime = nbt.getInteger("maxBurn");
        if (nbt.hasKey("outType")) {
            NTMMaterial mat = Mats.matById.get(nbt.getInteger("outType"));
            this.output = new MaterialStack(mat, nbt.getInteger("outAmount"));
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        this.tanks[0].writeToNBT(nbt, "t0");
        this.tanks[1].writeToNBT(nbt, "t1");
        this.tanks[2].writeToNBT(nbt, "t2");
        nbt.setFloat("prog", progress);
        nbt.setInteger("burn", burnTime);
        nbt.setDouble("heat", burnHeat);
        nbt.setInteger("maxBurn", maxBurnTime);
        if (this.output != null) {
            nbt.setInteger("outType", this.output.material.id);
            nbt.setInteger("outAmount", this.output.amount);
        }
        return nbt;
    }

    public DirPos[] getSteamPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

        return new DirPos[]{
                new DirPos(pos.getX() - dir.offsetX * 2 - rot.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 2 - rot.offsetZ * 2,
                        dir.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX * 2 - rot.offsetX, pos.getY(), pos.getZ() - dir.offsetZ * 2 - rot.offsetZ, dir.getOpposite())
        };
    }

    public DirPos[] getFluidPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

        return new DirPos[]{
                new DirPos(pos.getX() + dir.offsetX + rot.offsetX * 3, pos.getY(), pos.getZ() + dir.offsetZ + rot.offsetZ * 3, rot),
                new DirPos(pos.getX() - dir.offsetX + rot.offsetX * 3, pos.getY(), pos.getZ() - dir.offsetZ + rot.offsetZ * 3, rot)
        };
    }

    public boolean canProcess(RotaryFurnaceRecipe recipe) {

        if (this.burnTime <= 0) return false;

        if (recipe.fluid != null) {
            if (this.tanks[0].getTankType() != recipe.fluid.type) return false;
            if (this.tanks[0].getFill() < recipe.fluid.fill) return false;
        }

        float speed = Math.max((float) burnHeat, 1);

        if (tanks[1].getFill() < recipe.steam * speed) return false;
        if (tanks[2].getMaxFill() - tanks[2].getFill() < recipe.steam * speed / 100) return false;
        if (this.steamUsed > 100) return false;

        if (this.output != null) {
            if (this.output.material != recipe.output.material) return false;
            if (this.output.amount + recipe.output.amount > maxOutput) return false;
        }

        return true;
    }

    public void consumeItems(RotaryFurnaceRecipe recipe) {

        for (AStack aStack : recipe.ingredients) {

            for (int i = 0; i < 3; i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (aStack.matchesRecipe(stack, true) && stack.getCount() >= aStack.stacksize) {
                    inventory.getStackInSlot(i).shrink(aStack.stacksize);
                    break;
                }
            }
        }

        if (recipe.fluid != null) {
            this.tanks[0].setFill(tanks[0].getFill() - recipe.fluid.fill);
        }
    }

    @Override
    public void pollute(PollutionType type, float amount) {
        FluidTankNTM tank =
                type == PollutionType.SOOT ? smoke : type == PollutionType.HEAVYMETAL ? smoke_leaded : smoke_poison;

        int fluidAmount = (int) Math.ceil(amount * 100);
        tank.setFill(tank.getFill() + fluidAmount);

        if (tank.getFill() > tank.getMaxFill()) {
            int overflow = tank.getFill() - tank.getMaxFill();
            tank.setFill(tank.getMaxFill());
            PollutionHandler.incrementPollution(world, pos, type, overflow / 100F);
            this.isVenting = true;
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, @NotNull ItemStack stack) {
        return slot < 3 || slot == 4;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return false;
    }

    AxisAlignedBB bb = null;

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb =
                    new AxisAlignedBB(
                            pos.getX() - 2,
                            pos.getY(),
                            pos.getZ() - 2,
                            pos.getX() + 3,
                            pos.getY() + 5,
                            pos.getZ() + 3);
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side, BlockPos pos) {
        EnumFacing dir = EnumFacing.byIndex(this.getBlockMetadata() - 10);
        EnumFacing rot = dir.rotateY();
        BlockPos core = this.pos;

        //Red
        if (side == dir.getOpposite() && pos.equals(core.offset(dir, -1).offset(rot, -2))) return new int[]{0};
        //Yellow
        if (side == dir.getOpposite() && pos.equals(core.offset(dir, -1).offset(rot, -1))) return new int[]{1};
        //Green
        if (side == dir.getOpposite() && pos.equals(core.offset(dir, -1))) return new int[]{2};
        //Fuel
        if (side == dir && pos.equals(core.offset(dir, 1).offset(rot, -1))) return new int[]{4};

        return new int[]{};
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{tanks[0], tanks[1], tanks[2], smoke};
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{tanks[2], smoke};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tanks[0], tanks[1]};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerMachineRotaryFurnace(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiInfoContainer provideGUI(
            int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineRotaryFurnace(player.inventory, this);
    }

    @Override
    public String getConfigName() {
        return "rotaryfurnace";
    }

    @Override
    public void readIfPresent(JsonObject obj) {
        if (obj.has("M:burnModule")) {
            burnModule.readIfPresent(obj.get("M:burnModule").getAsJsonObject());
        }
    }

    @Override
    public void writeConfig(JsonWriter writer) throws IOException {
        writer.name("M:burnModule").beginObject();
        burnModule.writeConfig(writer);
        writer.endObject();
    }
}
