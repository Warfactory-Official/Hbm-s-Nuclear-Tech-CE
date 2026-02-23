package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.inventory.fluid.trait.FT_Coolable.CoolingType;
import com.hbm.lib.DirPos;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityLoadedBase;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public abstract class TileEntityTurbineBase extends TileEntityLoadedBase implements ITickable, IEnergyProviderMK2, IFluidStandardTransceiverMK2, IBufPacketReceiver, IFluidCopiable {

    protected ByteBuf buf;
    public long powerBuffer;

    public FluidTankNTM[] tanks;
    protected double[] info = new double[3];
    public boolean operational = false;

    public abstract double getEfficiency();
    public abstract DirPos[] getConPos();
    public abstract DirPos[] getPowerPos();
    public abstract double consumptionPercent();

    public void generatePower(long power, int steamConsumed) {
        this.powerBuffer += power;
    }

    @Override
    public void update() {

        if(!world.isRemote) {

            this.powerBuffer = 0;
            this.info = new double[3];

            if(this.buf != null) this.buf.release();
            this.buf = Unpooled.buffer();

            this.tanks[0].serialize(buf);

            operational = false;
            FluidType in = tanks[0].getTankType();
            boolean valid = false;
            if(in.hasTrait(FT_Coolable.class)) {
                FT_Coolable trait = in.getTrait(FT_Coolable.class);
                double eff = trait.getEfficiency(CoolingType.TURBINE) * getEfficiency();
                if(eff > 0) {
                    tanks[1].setTankType(trait.coolsTo);
                    int inputOps = (int) Math.ceil((tanks[0].getFill() * consumptionPercent()) / trait.amountReq);
                    int outputOps = (tanks[1].getMaxFill() - tanks[1].getFill()) / trait.amountProduced;
                    int ops = Math.min(inputOps, outputOps);
                    if(ops > 0) {
                        tanks[0].setFill(tanks[0].getFill() - ops * trait.amountReq);
                        tanks[1].setFill(tanks[1].getFill() + ops * trait.amountProduced);
                        this.generatePower((long) (ops * trait.heatEnergy * eff), ops * trait.amountReq);
                    }
                    info[0] = ops * trait.amountReq;
                    info[1] = ops * trait.amountProduced;
                    info[2] = ops * trait.heatEnergy * eff;
                    valid = true;
                    operational = ops > 0;
                }
            }

            onServerTick();

            this.tanks[1].serialize(buf);
            this.buf.writeLong(this.powerBuffer);

            if(!valid) tanks[1].setTankType(Fluids.NONE);

            for(DirPos pos : this.getPowerPos()) {
                this.tryProvide(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }

            for(DirPos pos : this.getConPos()) {
                this.tryProvide(tanks[1], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                this.trySubscribe(tanks[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }
            networkPackNT(150);

        } else {
            onClientTick();
        }
    }

    public void onServerTick() { }
    public void onClientTick() { }

    public void onLeverPull() {

        FluidType type = tanks[0].getTankType();
        boolean resize = this.doesResizeCompressor();

        if(type == Fluids.STEAM) {
            tanks[0].setTankType(Fluids.HOTSTEAM); tanks[1].setTankType(Fluids.STEAM);
            if(resize) { tanks[0].changeTankSize(tanks[0].getMaxFill() / 10); tanks[1].changeTankSize(tanks[1].getMaxFill() / 10); }
        } else if(type == Fluids.HOTSTEAM) {
            tanks[0].setTankType(Fluids.SUPERHOTSTEAM); tanks[1].setTankType(Fluids.HOTSTEAM);
            if(resize) { tanks[0].changeTankSize(tanks[0].getMaxFill() / 10); tanks[1].changeTankSize(tanks[1].getMaxFill() / 10); }
        } else if(type == Fluids.SUPERHOTSTEAM) {
            tanks[0].setTankType(Fluids.ULTRAHOTSTEAM); tanks[1].setTankType(Fluids.SUPERHOTSTEAM);
            if(resize) { tanks[0].changeTankSize(tanks[0].getMaxFill() / 10); tanks[1].changeTankSize(tanks[1].getMaxFill() / 10); }
        } else if(type == Fluids.ULTRAHOTSTEAM) {
            tanks[0].setTankType(Fluids.STEAM); tanks[1].setTankType(Fluids.SPENTSTEAM);
            if(resize) { tanks[0].changeTankSize(tanks[0].getMaxFill() * 1000); tanks[1].changeTankSize(tanks[1].getMaxFill() * 1000); }
        } else {
            tanks[0].setTankType(Fluids.STEAM); tanks[1].setTankType(Fluids.SPENTSTEAM);
        }

        markDirty();
    }

    public boolean doesResizeCompressor() {
        return false;
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeBytes(this.buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.tanks[0].deserialize(buf);
        this.tanks[1].deserialize(buf);
        this.powerBuffer = buf.readLong();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tanks[0].readFromNBT(nbt, "water");
        tanks[1].readFromNBT(nbt, "steam");
        powerBuffer = nbt.getLong("power");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        tanks[0].writeToNBT(nbt, "water");
        tanks[1].writeToNBT(nbt, "steam");
        nbt.setLong("power", powerBuffer);
        return super.writeToNBT(nbt);
    }

    @Override public long getPower() { return powerBuffer; }
    @Override public long getMaxPower() { return powerBuffer; }
    @Override public void setPower(long power) { this.powerBuffer = power; }

    @Override public FluidTankNTM[] getSendingTanks() { return new FluidTankNTM[] {tanks[1]}; }
    @Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] {tanks[0]}; }
    @Override public FluidTankNTM[] getAllTanks() { return tanks; }

    @Override public FluidTankNTM getTankToPaste() { return null; }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
