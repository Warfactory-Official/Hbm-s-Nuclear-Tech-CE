package com.hbm.api.fluidmk2;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.Compat;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * IFluidProviderMK2 with standard implementation for fluid provision and fluid removal.
 * @author hbm
 */
public interface IFluidStandardSenderMK2 extends IFluidProviderMK2 {

    default void tryProvide(FluidTankNTM tank, World world, DirPos pos) { tryProvide(tank.getTankType(), tank.getPressure(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir()); }
    default void tryProvide(FluidType type, World world, DirPos pos) { tryProvide(type, 0, world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir()); }
    default void tryProvide(FluidType type, int pressure, World world, DirPos pos) { tryProvide(type, pressure, world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir()); }

    default void tryProvide(FluidTankNTM tank, World world, int x, int y, int z, ForgeDirection dir) { tryProvide(tank.getTankType(), tank.getPressure(), world, x, y, z, dir); }
    default void tryProvide(FluidTankNTM tank, World world, BlockPos pos, ForgeDirection dir) { tryProvide(tank.getTankType(), tank.getPressure(), world, pos.getX(), pos.getY(), pos.getZ(), dir); }
    default void tryProvide(FluidType type, World world, int x, int y, int z, ForgeDirection dir) { tryProvide(type, 0, world, x, y, z, dir); }

    default void tryProvide(FluidType type, int pressure, World world, int x, int y, int z, ForgeDirection dir) {

        TileEntity te = Compat.getTileStandard(world, x, y, z);
        boolean red = false;

        if(te instanceof IFluidConnectorMK2) {
            IFluidConnectorMK2 con = (IFluidConnectorMK2) te;
            if(con.canConnect(type, dir.getOpposite())) {

                GenNode<FluidNetMK2> node = UniNodespace.getNode(world, new BlockPos(x, y, z), type.getNetworkProvider());

                if(node != null && node.net != null) {
                    node.net.addProvider(this);
                    red = true;
                }
            }
        }

        if(te != this && te instanceof IFluidReceiverMK2) {
            IFluidReceiverMK2 rec = (IFluidReceiverMK2) te;
            if(rec.canConnect(type, dir.getOpposite())) {
                long provides = Math.min(this.getFluidAvailable(type, pressure), this.getProviderSpeed(type, pressure));
                long receives = Math.min(rec.getDemand(type, pressure), rec.getReceiverSpeed(type, pressure));
                long toTransfer = Math.min(provides, receives);
                toTransfer -= rec.transferFluid(type, pressure, toTransfer);
                this.useUpFluid(type, pressure, toTransfer);
            }
        }

        if(particleDebug) {
            NBTTagCompound data = new NBTTagCompound();
            data.setString("mode", "fluid");
            data.setInteger("color", type.getColor());
            double posX = x + 0.5 - dir.offsetX * 0.5 + world.rand.nextDouble() * 0.5 - 0.25;
            double posY = y + 0.5 - dir.offsetY * 0.5 + world.rand.nextDouble() * 0.5 - 0.25;
            double posZ = z + 0.5 - dir.offsetZ * 0.5 + world.rand.nextDouble() * 0.5 - 0.25;
            data.setDouble("mX", dir.offsetX * (red ? 0.025 : 0.1));
            data.setDouble("mY", dir.offsetY * (red ? 0.025 : 0.1));
            data.setDouble("mZ", dir.offsetZ * (red ? 0.025 : 0.1));
            PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.Network, data, posX, posY, posZ), new NetworkRegistry.TargetPoint(world.provider.getDimension(), posX, posY, posZ, 25));
        }
    }

    @NotNull FluidTankNTM[] getSendingTanks();

    @Override
    default long getFluidAvailable(FluidType type, int pressure) {
        long amount = 0;
        for(FluidTankNTM tank : getSendingTanks()) {
            if(tank.getTankType() == type && tank.getPressure() == pressure) amount += tank.getFill();
        }
        return amount;
    }

    @Override
    default void useUpFluid(FluidType type, int pressure, long amount) {
        int tanks = 0;
        for(FluidTankNTM tank : getSendingTanks()) {
            if(tank.getTankType() == type && tank.getPressure() == pressure) tanks++;
        }
        if(tanks > 1) {
            int firstRound = (int) Math.floor((double) amount / (double) tanks);
            for(FluidTankNTM tank : getSendingTanks()) {
                if(tank.getTankType() == type && tank.getPressure() == pressure) {
                    int toRem = Math.min(firstRound, tank.getFill());
                    tank.setFill(tank.getFill() - toRem);
                    amount -= toRem;
                }
            }
        }
        if(amount > 0) for(FluidTankNTM tank : getSendingTanks()) {
            if(tank.getTankType() == type && tank.getPressure() == pressure) {
                int toRem = (int) Math.min(amount, tank.getFill());
                tank.setFill(tank.getFill() - toRem);
                amount -= toRem;
            }
        }
    }

    @Override
    default int[] getProvidingPressureRange(FluidType type) {
        int lowest = HIGHEST_VALID_PRESSURE;
        int highest = 0;

        for(FluidTankNTM tank : getSendingTanks()) {
            if(tank.getTankType() == type) {
                if(tank.getPressure() < lowest) lowest = tank.getPressure();
                if(tank.getPressure() > highest) highest = tank.getPressure();
            }
        }

        return lowest <= highest ? new int[] {lowest, highest} : DEFAULT_PRESSURE_RANGE;
    }

    @Override
    default long getProviderSpeed(FluidType type, int pressure) {
        return 1_000_000_000;
    }
}
