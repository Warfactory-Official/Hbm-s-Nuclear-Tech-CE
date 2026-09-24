package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingStep;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingType;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IConnectionAnchors;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoRegister
public class TileEntityMachineHephaestus extends TileEntityLoadedBase implements ITickable, IBufPacketReceiver, IFluidStandardTransceiver,
        IFluidCopiable, IConnectionAnchors {

    private final int[] heat = new int[10];
    public FluidTankNTM input;
    public FluidTankNTM output;
    public int bufferedHeat;
    public float rot;
    public float prevRot;
    private FluidTankNTM inputSync;
    private FluidTankNTM outputSync;
    private long fissureScanTime;
    private AudioWrapper audio;
    private AxisAlignedBB bb = null;

    public TileEntityMachineHephaestus() {
        this.input = new FluidTankNTM(Fluids.OIL, 24_000).withOwner(this);
        this.output = new FluidTankNTM(Fluids.HOTOIL, 24_000).withOwner(this);
    }

    @Override
    public void update() {

        if (!world.isRemote) {

            setupTanks();

            if (world.getTotalWorldTime() % 20 == 0) {
                this.updateConnections();
            }

            int height = (int) (world.getTotalWorldTime() % 10);
            int range = 7;
            int fromY = pos.getY() - 1 - height;

            heat[height] = 0;

            if (fromY >= 0) {
                for (int offsetX = -range; offsetX <= range; offsetX++) {
                    for (int offsetZ = -range; offsetZ <= range; offsetZ++) {
                        heat[height] += heatFromBlock(pos.getX() + offsetX, fromY, pos.getZ() + offsetZ);
                    }
                }
            }

            inputSync = input.clone();

            heatFluid();

            outputSync = output.clone();

            if (output.getFill() > 0) {
                for (DirPos dirPos : getConPos()) {
                    BlockPos pos = dirPos.getPos();
                    this.sendFluid(output, world, pos.getX(), pos.getY(), pos.getZ(), dirPos.getDir());
                }
            }
            networkPackNT(150);
        } else {

            this.prevRot = this.rot;

            if (this.bufferedHeat > 0) {
                this.rot += 0.5F;

                if (world.rand.nextInt(7) == 0) {
                    double x = world.rand.nextGaussian() * 2;
                    double y = world.rand.nextGaussian() * 3;
                    double z = world.rand.nextGaussian() * 2;
                    world.spawnParticle(EnumParticleTypes.CLOUD, pos.getX() + 0.5 + x, pos.getY() + 6 + y, pos.getZ() + 0.5 + z, 0, 0, 0);
                }

                if (audio == null) {
                    audio = MainRegistry.proxy.getLoopedSound(HBMSoundHandler.hephaestusRunning, SoundCategory.BLOCKS, pos.getX(), pos.getY() + 5F,
                            pos.getZ(), 10F, 1.0F);
                    audio.startSound();
                }
            } else {
                if (audio != null) {
                    audio.stopSound();
                    audio = null;
                }
            }

            if (this.rot >= 360F) {
                this.prevRot -= 360F;
                this.rot -= 360F;
            }
        }
    }

    protected void heatFluid() {

        FluidType type = input.getTankType();

        if (type.hasTrait(FT_Heatable.class)) {
            FT_Heatable trait = type.getTrait(FT_Heatable.class);
            int heat = this.getTotalHeat();
            HeatingStep step = trait.getFirstStep();

            int inputOps = input.getFill() / step.amountReq;
            int outputOps = (output.getMaxFill() - output.getFill()) / step.amountProduced;
            int heatOps = heat / step.heatReq;
            int ops = Math.min(Math.min(inputOps, outputOps), heatOps);

            input.setFill(input.getFill() - step.amountReq * ops);
            output.setFill(output.getFill() + step.amountProduced * ops);
            markDirty();
        }
    }

    protected void setupTanks() {

        FluidType type = input.getTankType();

        if (type.hasTrait(FT_Heatable.class)) {
            FT_Heatable trait = type.getTrait(FT_Heatable.class);

            if (trait.getEfficiency(HeatingType.HEATEXCHANGER) > 0) {
                FluidType outType = trait.getFirstStep().typeProduced;
                output.setTankType(outType);
                return;
            }
        }

        input.setTankType(Fluids.NONE);
        output.setTankType(Fluids.NONE);
    }

    protected int heatFromBlock(int x, int y, int z) {
        Block b = world.getBlockState(new BlockPos(x, y, z)).getBlock();

        if (b == Blocks.LAVA || b == Blocks.FLOWING_LAVA) return 5;
        if (b == ModBlocks.volcanic_lava_block) return 150;

        if (b == ModBlocks.ore_volcano) {
            this.fissureScanTime = world.getTotalWorldTime();
            return 300;
        }

        return 0;
    }

    public int getTotalHeat() {
        boolean fissure = world.getTotalWorldTime() - this.fissureScanTime < 20;
        int heat = 0;

        for (int h : this.heat) {
            heat += h;
        }

        if (fissure) {
            heat *= 3;
        }

        return heat;
    }

    @Override
    public void serializeInitial(ByteBuf buf) {
        this.input.serialize(buf);
        this.output.serialize(buf);
        buf.writeInt(this.getTotalHeat());
    }

    @Override
    public void serialize(ByteBuf buf) {
        inputSync.serialize(buf);
        outputSync.serialize(buf);
        buf.writeInt(this.getTotalHeat());
    }

    @Override
    public void deserialize(ByteBuf buf) {
        input.deserialize(buf);
        output.deserialize(buf);

        this.bufferedHeat = buf.readInt();
    }

    private void updateConnections() {

        if (input.getTankType() == Fluids.NONE) return;

        for (DirPos dirPos : getConPos()) {
            BlockPos pos = dirPos.getPos();
            this.trySubscribe(input.getTankType(), world, pos.getX(), pos.getY(), pos.getZ(), dirPos.getDir());
        }
    }

    public DirPos[] getConPos() {

        int xCoord = pos.getX(), yCoord = pos.getY(), zCoord = pos.getZ();
        return new DirPos[]{new DirPos(xCoord + 2, yCoord, zCoord, Library.POS_X), new DirPos(xCoord - 2, yCoord, zCoord, Library.NEG_X),
                new DirPos(xCoord, yCoord, zCoord + 2, Library.POS_Z), new DirPos(xCoord, yCoord, zCoord - 2, Library.NEG_Z), new DirPos(xCoord + 2
                , yCoord + 11, zCoord, Library.POS_X), new DirPos(xCoord - 2, yCoord + 11, zCoord, Library.NEG_X), new DirPos(xCoord, yCoord + 11,
                zCoord + 2, Library.POS_Z), new DirPos(xCoord, yCoord + 11, zCoord - 2, Library.NEG_Z)};
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.input.readFromNBT(nbt, "0");
        this.output.readFromNBT(nbt, "1");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        this.input.writeToNBT(nbt, "0");
        this.output.writeToNBT(nbt, "1");
        return nbt;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{input, output};
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{output};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{input};
    }

    @Override
    public boolean canConnect(FluidType type, ForgeDirection dir) {
        return dir != ForgeDirection.UNKNOWN && dir != ForgeDirection.UP && dir != ForgeDirection.DOWN;
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();

        if (audio != null) {
            audio.stopSound();
            audio = null;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if (audio != null) {
            audio.stopSound();
            audio = null;
        }
    }

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            int xCoord = pos.getX(), yCoord = pos.getY(), zCoord = pos.getZ();
            bb = new AxisAlignedBB(xCoord - 3, yCoord, zCoord - 3, xCoord + 4, yCoord + 12, zCoord + 4);
        }

        return bb;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new NTMFluidHandlerWrapper(this));
        }
        return super.getCapability(capability, facing);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
