package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardSenderMK2;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@AutoRegister
public class TileEntityMachineIntake extends TileEntityLoadedBase implements ITickable, IEnergyReceiverMK2, IFluidStandardSenderMK2 {

    public FluidTankNTM compair;
    public long power;
    public float fan = 0;
    public float prevFan = 0;
    private AxisAlignedBB bb = null;
    private AudioWrapper audio;

    public TileEntityMachineIntake() {
        this.compair = new FluidTankNTM(Fluids.AIR, 1_000);
    }

    @Override
    public void update() {

        if (!world.isRemote) {

            if (this.power >= this.getMaxPower() / 20) {
                this.compair.setFill(this.compair.getMaxFill());
                this.power -= this.getMaxPower() / 20;
            }

            for (DirPos pos : getConPos()) {
                if (this.compair.getFill() > 0) this.tryProvide(compair, world, pos);
                this.trySubscribe(world, pos.getPos(), pos.getDir());
            }

            this.networkPackNT(50);

        } else {

            this.prevFan = this.fan;

            if (this.power >= this.getMaxPower() / 20) {
                this.fan += 45;

                if (this.fan >= 360) {
                    this.fan -= 360;
                    this.prevFan -= 360;
                }

                if (audio == null) {
                    audio = createAudioLoop();
                    audio.startSound();
                } else if (!audio.isPlaying()) {
                    audio = rebootAudio(audio);
                }

                audio.keepAlive();
                audio.updateVolume(this.getVolume(0.25F));

            } else {

                if (audio != null) {
                    audio.stopSound();
                    audio = null;
                }
            }
        }
    }

    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        int xCoord = pos.getX(), yCoord = pos.getY(), zCoord = pos.getZ();
        return new DirPos[]{new DirPos(xCoord + dir.offsetX, yCoord, zCoord + dir.offsetZ, dir),
                new DirPos(xCoord + dir.offsetX + rot.offsetX, yCoord, zCoord + dir.offsetZ + rot.offsetZ, dir),

                new DirPos(xCoord - dir.offsetX * 2, yCoord, zCoord - dir.offsetZ * 2, dir.getOpposite()),
                new DirPos(xCoord - dir.offsetX * 2 + rot.offsetX, yCoord, zCoord - dir.offsetZ * 2 + rot.offsetZ, dir.getOpposite()),

                new DirPos(xCoord + rot.offsetX * 2, yCoord, zCoord + rot.offsetZ * 2, rot),
                new DirPos(xCoord + rot.offsetX * 2 - dir.offsetX, yCoord, zCoord + rot.offsetZ * 2 - dir.offsetZ, rot),

                new DirPos(xCoord - rot.offsetX, yCoord, zCoord - rot.offsetZ, rot.getOpposite()),
                new DirPos(xCoord - rot.offsetX - dir.offsetX, yCoord, zCoord - rot.offsetZ - dir.offsetZ, rot.getOpposite())};
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.motor, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 0.25F, 10F, 1.0F, 20);
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
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(power);
        compair.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        compair.deserialize(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.power = nbt.getLong("power");
        compair.readFromNBT(nbt, "compair");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setLong("power", power);
        compair.writeToNBT(nbt, "compair");
        return nbt;
    }

    @Override
    public boolean canConnect(ForgeDirection dir) {
        return dir != ForgeDirection.UP && dir != ForgeDirection.DOWN;
    }

    @Override
    public boolean canConnect(FluidType type, ForgeDirection dir) {
        return type == Fluids.AIR && dir != ForgeDirection.UP && dir != ForgeDirection.DOWN;
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long i) {
        power = i;
    }

    @Override
    public long getMaxPower() {
        return 2_000;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{compair};
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{compair};
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            int xCoord = pos.getX(), yCoord = pos.getY(), zCoord = pos.getZ();
            bb = new AxisAlignedBB(xCoord - 1, yCoord, zCoord - 1, xCoord + 2, yCoord + 1, zCoord + 2);
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(
                    new NTMEnergyCapabilityWrapper(this)
            );
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
                    new NTMFluidHandlerWrapper(this)
            );
        }
        return super.getCapability(capability, facing);
    }
}
