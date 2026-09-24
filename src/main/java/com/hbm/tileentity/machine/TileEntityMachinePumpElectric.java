package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.DirPos;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

@AutoRegister
public class TileEntityMachinePumpElectric extends TileEntityMachinePumpBase implements IEnergyReceiverMK2 {

    public long power;
    public static final long maxPower = 10_000;

    public TileEntityMachinePumpElectric() {
        super();
        water = new FluidTankNTM(Fluids.WATER, electricSpeed * 100).withOwner(this);
    }

    @Override
    public void update() {
        if(!world.isRemote) {
            if(world.getTotalWorldTime() % 20 == 0) for(DirPos pos : getConPos()) {
                this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }
        }
        super.update();
    }
    
    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(power);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
    }

    @Override
    protected boolean canOperate() {
        return power >= 1_000 && water.getFill() < water.getMaxFill();
    }

    @Override
    protected void operate() {
        this.power -= 1_000;
        int pumpSpeed = water.getTankType() == Fluids.WATER ? electricSpeed : electricSpeed / nonWaterDebuff;
        water.setFill(Math.min(water.getFill() + pumpSpeed, water.getMaxFill()));
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(
                    new NTMEnergyCapabilityWrapper(this)
            );
        }
        return super.getCapability(capability, facing);
    }
}
