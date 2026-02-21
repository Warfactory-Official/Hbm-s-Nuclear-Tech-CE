package com.hbm.capability;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.config.GeneralConfig;
import com.hbm.lib.CapabilityContextProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class is a wrapper for {@link IEnergyHandlerMK2}, exposing it as {@link IEnergyStorage} for Forge Energy compatibility.
 *
 * @author mlbv
 */
public class NTMEnergyCapabilityWrapper implements IEnergyStorage {

    @NotNull
    private final IEnergyHandlerMK2 handler;
    @Nullable
    private final IEnergyReceiverMK2 receiver;
    @Nullable
    private final IEnergyProviderMK2 provider;
    @Nullable
    private final BlockPos accessor;

    /**
     * @param pos The position of the accessor. Null -> Internal access.
     */
    public NTMEnergyCapabilityWrapper(@NotNull TileEntity handler, @Nullable BlockPos pos) {
        if (handler instanceof IEnergyHandlerMK2 energyHandlerMK2) this.handler = energyHandlerMK2;
        else throw new IllegalArgumentException("Handler must implement EnergyHandlerMK2");
        this.receiver = handler instanceof IEnergyReceiverMK2 ? (IEnergyReceiverMK2) handler : null;
        this.provider = handler instanceof IEnergyProviderMK2 ? (IEnergyProviderMK2) handler : null;
        this.accessor = pos;
    }

    public NTMEnergyCapabilityWrapper(@NotNull TileEntity handler) {
        this(handler, null);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive() || maxReceive <= 0 || GeneralConfig.conversionRateHeToRF <= 0) return 0;
        if (accessor != null) {
            var prev = CapabilityContextProvider.pushPos(accessor);
            try {
                return receiveEnergyInternal(maxReceive, simulate);
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return receiveEnergyInternal(maxReceive, simulate);
    }

    private int receiveEnergyInternal(int maxReceive, boolean simulate) {
        long heToOffer = (long) Math.floor(maxReceive / GeneralConfig.conversionRateHeToRF);
        if (heToOffer <= 0) return simulate ? 1 : 0;
        long leftoverHE = receiver.transferPower(heToOffer, simulate);
        long acceptedHE = heToOffer - leftoverHE;
        long acceptedFE = Math.round(acceptedHE * GeneralConfig.conversionRateHeToRF);
        return (int) Math.min(maxReceive, Math.min(Integer.MAX_VALUE, acceptedFE));
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract() || maxExtract <= 0 || GeneralConfig.conversionRateHeToRF <= 0) return 0;
        if (accessor != null) {
            var prev = CapabilityContextProvider.pushPos(accessor);
            try {
                return extractEnergyInternal(maxExtract, simulate);
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return extractEnergyInternal(maxExtract, simulate);
    }

    private int extractEnergyInternal(int maxExtract, boolean simulate) {
        long heBudget = (long) Math.floor(maxExtract / GeneralConfig.conversionRateHeToRF);
        if (heBudget <= 0) return simulate ? 1 : 0;
        long availableHE = Math.min(provider.getPower(), provider.getProviderSpeed());
        long heToExtract = Math.min(heBudget, availableHE);
        if (heToExtract <= 0) return 0;
        if (!simulate) provider.usePower(heToExtract);
        long feExtracted = Math.round(heToExtract * GeneralConfig.conversionRateHeToRF);
        return (int) Math.min(maxExtract, Math.min(Integer.MAX_VALUE, feExtracted));
    }

    @Override
    public int getEnergyStored() {
        if (accessor != null) {
            var prev = CapabilityContextProvider.pushPos(accessor);
            try {
                return (int) Math.min(Integer.MAX_VALUE, Math.round(handler.getPower() * GeneralConfig.conversionRateHeToRF));
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return (int) Math.min(Integer.MAX_VALUE, Math.round(handler.getPower() * GeneralConfig.conversionRateHeToRF));
    }

    @Override
    public int getMaxEnergyStored() {
        if (accessor != null) {
            var prev = CapabilityContextProvider.pushPos(accessor);
            try {
                return (int) Math.min(Integer.MAX_VALUE, Math.round(handler.getMaxPower() * GeneralConfig.conversionRateHeToRF));
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return (int) Math.min(Integer.MAX_VALUE, Math.round(handler.getMaxPower() * GeneralConfig.conversionRateHeToRF));
    }

    @Override
    public boolean canExtract() {
        if (provider == null) return false;
        if (accessor != null) {
            var prev = CapabilityContextProvider.pushPos(accessor);
            try {
                return provider.getPower() > 0;
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return provider.getPower() > 0;
    }

    @Override
    public boolean canReceive() {
        if (receiver == null) return false;
        if (accessor != null) {
            var prev = CapabilityContextProvider.pushPos(accessor);
            try {
                return receiver.getPower() < receiver.getMaxPower();
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return receiver.getPower() < receiver.getMaxPower();
    }
}
