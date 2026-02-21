package com.hbm.api.energymk2;

import com.hbm.config.GeneralConfig;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.lib.ForgeDirection;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 * If it sends energy, use this
 */
public interface IEnergyProviderMK2 extends IEnergyHandlerMK2 {

    /**
     * Uses up available power, default implementation has no sanity checking, make sure that the requested power is lequal to the current power
     *
     * @param power The amount of power to use. Ensure this value is less than or equal to the current power.
     */
    default void usePower(long power) {
        // Subtract the specified power from the current power and update the power level
        this.setPower(this.getPower() - power);
    }

    /**
     * Retrieves the maximum speed at which the energy provider can send energy.
     * By default, this method returns the maximum power capacity of the provider.
     *
     * @return The maximum energy transfer speed, represented by the provider's maximum power capacity.
     */
    default long getProviderSpeed() {
        // Return the maximum power capacity as the default provider speed
        return this.getMaxPower();
    }

    /**
     * Attempts to provide energy to a target tile entity at specific coordinates.
     * It checks for HBM's native energy interfaces first, and then checks for Forge Energy capability
     *
     * @param world The game world.
     * @param x     The x-coordinate of the <b>target tile entity</b> (the potential receiver).
     * @param y     The y-coordinate of the <b>target tile entity</b>.
     * @param z     The z-coordinate of the <b>target tile entity</b>.
     * @param dir   The {@link ForgeDirection} from this provider to the target tile entity.
     */
    default void tryProvide(World world, int x, int y, int z, ForgeDirection dir) {
        BlockPos targetPos = new BlockPos(x, y, z);
        TileEntity targetTE = world.getTileEntity(targetPos);

        if (targetTE == null) return;

        boolean connectedToNetwork = false;
        boolean powerTransferred = false;

        if (targetTE instanceof IEnergyConductorMK2 con) {
            if (con.canConnect(dir.getOpposite())) {
                Nodespace.PowerNode node = Nodespace.getNode(world, targetPos);
                if (node != null && node.net != null) {
                    node.net.addProvider(this);
                    connectedToNetwork = true;
                }
            }
        }

        if (targetTE instanceof IEnergyReceiverMK2 rec && targetTE != this) {
            if (rec.canConnect(dir.getOpposite()) && rec.allowDirectProvision()) {
                long canProvide = Math.min(this.getPower(), this.getProviderSpeed());
                long canReceive = Math.min(rec.getMaxPower() - rec.getPower(), rec.getReceiverSpeed());
                long toTransfer = Math.min(canProvide, canReceive);

                if (toTransfer > 0) {
                    long rejected = rec.transferPower(toTransfer, false);
                    long accepted = toTransfer - rejected;
                    if (accepted > 0) {
                        this.usePower(accepted);
                        powerTransferred = true;
                    }
                }
            }
        } else if (targetTE != this) {
            EnumFacing targetFace = dir.getOpposite().toEnumFacing();
            if (targetTE.hasCapability(CapabilityEnergy.ENERGY, targetFace)) {
                IEnergyStorage cap = targetTE.getCapability(CapabilityEnergy.ENERGY, targetFace);
                boolean ready = cap != null && cap.canReceive() && GeneralConfig.conversionRateHeToRF > 0 && this.getPower() > 0 && this.getProviderSpeed() > 0;
                if (ready) {
                    long heBudget = Math.min(this.getPower(), this.getProviderSpeed());
                    long feBudget = (long) Math.floor(heBudget * GeneralConfig.conversionRateHeToRF);
                    if (feBudget > 0) {
                        int feToSend = (int) Math.min(feBudget, Integer.MAX_VALUE);
                        int feAccepted = cap.receiveEnergy(feToSend, false);
                        if (feAccepted > 0) {
                            long heDrained = (long) Math.ceil(feAccepted / GeneralConfig.conversionRateHeToRF);
                            this.usePower(heDrained);
                            powerTransferred = true;
                        }
                    }
                }
            }
        }

        if (particleDebug && (connectedToNetwork || powerTransferred)) {
            NBTTagCompound data = new NBTTagCompound();
            data.setString("type", "network");
            data.setString("mode", "power");
            double posX = x + 0.5 - dir.offsetX * 0.5 + world.rand.nextDouble() * 0.5 - 0.25;
            double posY = y + 0.5 - dir.offsetY * 0.5 + world.rand.nextDouble() * 0.5 - 0.25;
            double posZ = z + 0.5 - dir.offsetZ * 0.5 + world.rand.nextDouble() * 0.5 - 0.25;
            data.setDouble("mX", dir.offsetX * (connectedToNetwork ? 0.025 : 0.1));
            data.setDouble("mY", dir.offsetY * (connectedToNetwork ? 0.025 : 0.1));
            data.setDouble("mZ", dir.offsetZ * (connectedToNetwork ? 0.025 : 0.1));
            PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, posX, posY, posZ), new NetworkRegistry.TargetPoint(world.provider.getDimension(), posX, posY, posZ, 25));
        }
    }

    default void tryProvide(World world, BlockPos pos, ForgeDirection dir) {
        tryProvide(world, pos.getX(), pos.getY(), pos.getZ(), dir);
    }
}
