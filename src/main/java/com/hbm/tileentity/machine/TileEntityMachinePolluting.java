package com.hbm.tileentity.machine;

import com.hbm.api.fluidmk2.IFluidStandardSenderMK2;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Polluting;
import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityMachineBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class TileEntityMachinePolluting extends TileEntityMachineBase implements IFluidStandardSenderMK2 {

    public FluidTankNTM smoke;
    public FluidTankNTM smoke_leaded;
    public FluidTankNTM smoke_poison;

    public TileEntityMachinePolluting(int scount, int buffer, boolean enableFluidWrapper, boolean enableEnergyWrapper){
        super(scount, enableFluidWrapper, enableEnergyWrapper);
        smoke = new FluidTankNTM(Fluids.SMOKE, buffer).withOwner(this);
        smoke_leaded = new FluidTankNTM(Fluids.SMOKE_LEADED, buffer).withOwner(this);
        smoke_poison = new FluidTankNTM(Fluids.SMOKE_POISON, buffer).withOwner(this);
    }

    @Deprecated
    public TileEntityMachinePolluting(int scount, int buffer) {
        this(scount, buffer, false, false);
    }

    public void pollute(PollutionHandler.PollutionType type, float amount) {
        FluidTankNTM tank = type == PollutionHandler.PollutionType.SOOT ? smoke : type == PollutionHandler.PollutionType.HEAVYMETAL ? smoke_leaded : smoke_poison;

        int fluidAmount = (int) Math.ceil(amount * 100);
        tank.setFill(tank.getFill() + fluidAmount);

        if(tank.getFill() > tank.getMaxFill()) {
            int overflow = tank.getFill() - tank.getMaxFill();
            tank.setFill(tank.getMaxFill());
            PollutionHandler.incrementPollution(world, pos, type, overflow / 100F);

            if(world.rand.nextInt(3) == 0) world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.1F, 1.5F);
        }
    }
    public void pollute(FluidType type, FluidTrait.FluidReleaseType release, float amount) {
        FluidTankNTM tank;
        FT_Polluting trait = type.getTrait(FT_Polluting.class);
        if(trait == null) return;
        if(release == FluidTrait.FluidReleaseType.VOID) return;

        HashMap<PollutionHandler.PollutionType, Float> map = release == FluidTrait.FluidReleaseType.BURN ? trait.burnMap : trait.releaseMap;

        for(Map.Entry<PollutionHandler.PollutionType, Float> entry : map.entrySet()) {

            tank = entry.getKey() == PollutionHandler.PollutionType.SOOT ? smoke : entry.getKey() == PollutionHandler.PollutionType.HEAVYMETAL ? smoke_leaded : smoke_poison;
            int fluidAmount = (int) Math.ceil(entry.getValue() * amount * 100);
            tank.setFill(tank.getFill() + fluidAmount);

            if (tank.getFill() > tank.getMaxFill()) {
                int overflow = tank.getFill() - tank.getMaxFill();
                tank.setFill(tank.getMaxFill());
                PollutionHandler.incrementPollution(world, pos, entry.getKey(), overflow / 100F);

                if (world.rand.nextInt(3) == 0)
                    world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.1F, 1.5F);
            }
        }
    }

    public void sendSmoke(BlockPos pos, ForgeDirection dir) {sendSmoke(pos.getX(), pos.getY(), pos.getZ(), dir);}
    public void sendSmoke(int x, int y, int z, ForgeDirection dir) {
        if(this.smoke.getFill() > 0) this.tryProvide(smoke, world, x, y, z, dir);
        if(this.smoke_leaded.getFill() > 0) this.tryProvide(smoke_leaded, world, x, y, z, dir);
        if(this.smoke_poison.getFill() > 0) this.tryProvide(smoke_poison, world, x, y, z, dir);
    }

    public FluidTankNTM[] getSmokeTanks() {
        return new FluidTankNTM[] {smoke, smoke_leaded, smoke_poison};
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        smoke.readFromNBT(nbt, "smoke0");
        smoke_leaded.readFromNBT(nbt, "smoke1");
        smoke_poison.readFromNBT(nbt, "smoke2");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        smoke.writeToNBT(nbt, "smoke0");
        smoke_leaded.writeToNBT(nbt, "smoke1");
        smoke_poison.writeToNBT(nbt, "smoke2");
        return super.writeToNBT(nbt);
    }
}
