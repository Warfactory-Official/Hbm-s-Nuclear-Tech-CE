package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.DirPos;
import io.netty.buffer.ByteBuf;

@AutoRegister
public class TileEntityMachinePumpSteam extends TileEntityMachinePumpBase {

    public FluidTankNTM steam;
    public FluidTankNTM lps;

    public TileEntityMachinePumpSteam() {
        super();
        water = new FluidTankNTM(Fluids.WATER, steamSpeed * 100).withOwner(this);
        steam = new FluidTankNTM(Fluids.STEAM, 1_000).withOwner(this);
        lps = new FluidTankNTM(Fluids.SPENTSTEAM, 10).withOwner(this);
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            for (DirPos pos : getConPos()) {
                this.trySubscribe(steam.getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                if (lps.getFill() > 0) {
                    this.sendFluid(lps, world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                }
            }
        }
        super.update();
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{water, steam, lps};
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{water, lps};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{steam};
    }

    
    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        steam.serialize(buf);
        lps.serialize(buf);
    }
    
    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        steam.deserialize(buf);
        lps.deserialize(buf);
    }

    @Override
    protected boolean canOperate() {
        return steam.getFill() >= 100 && lps.getMaxFill() - lps.getFill() > 0 && water.getFill() < water.getMaxFill();
    }

    @Override
    protected void operate() {
        steam.setFill(steam.getFill() - 100);
        lps.setFill(lps.getFill() + 1);
        int pumpSpeed = water.getTankType() == Fluids.WATER ? steamSpeed : steamSpeed / nonWaterDebuff;
        water.setFill(Math.min(water.getFill() + pumpSpeed, water.getMaxFill()));
    }
}
