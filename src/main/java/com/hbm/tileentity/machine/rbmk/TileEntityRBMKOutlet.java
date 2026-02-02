package com.hbm.tileentity.machine.rbmk;

import com.hbm.api.fluid.IFluidStandardSender;
import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityRBMKOutlet extends TileEntityLoadedBase implements ITickable, IFluidStandardSender, IBufPacketReceiver {

    public FluidTankNTM steam;

    public TileEntityRBMKOutlet() {
        steam = new FluidTankNTM(Fluids.SUPERHOTSTEAM, 32000);
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            if(RBMKDials.getReasimBoilers(world)) for (int i = 2; i < 6; i++) {
                ForgeDirection dir = ForgeDirection.getOrientation(i);
                Block b = world.getBlockState(new BlockPos(pos.getX() + dir.offsetX, pos.getY(), pos.getZ() + dir.offsetZ)).getBlock();

                if (b instanceof RBMKBase) {
                    int[] pos = ((RBMKBase) b).findCore(world, this.pos.getX() + dir.offsetX, this.pos.getY(), this.pos.getZ() + dir.offsetZ);

                    if (pos != null) {
                        TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));

                        if (te instanceof TileEntityRBMKBase rbmk) {

                            int prov = Math.min(steam.getMaxFill() - steam.getFill(), rbmk.reasimSteam);
                            rbmk.reasimSteam -= prov;
                            steam.setFill(steam.getFill() + prov);
                        }
                    }
                }
            }

            fillFluidInit();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.steam.readFromNBT(nbt, "tank");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        this.steam.writeToNBT(nbt, "tank");
        return nbt;
    }


    public void fillFluidInit() {
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            this.sendFluid(steam, world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new NTMFluidHandlerWrapper(this));
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{steam};
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{steam};

    }
}
