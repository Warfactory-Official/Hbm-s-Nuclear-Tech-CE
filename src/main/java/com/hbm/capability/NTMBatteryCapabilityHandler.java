package com.hbm.capability;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.config.GeneralConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Attaches {@link CapabilityEnergy#ENERGY} capability to any item that implements {@link IBatteryItem}.
 *
 * @author mlbv
 */
public class NTMBatteryCapabilityHandler {

    public static final ResourceLocation HBM_BATTERY_CAPABILITY = new ResourceLocation("hbm", "battery_wrapper");

    public static void initialize() {
        MinecraftForge.EVENT_BUS.register(new NTMBatteryCapabilityHandler());
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.isEmpty() || !(stack.getItem() instanceof IBatteryItem)) return;
        event.addCapability(HBM_BATTERY_CAPABILITY, new Wrapper(stack));
    }

    private static class Wrapper implements ICapabilityProvider, IEnergyStorage {

        @NotNull
        private final ItemStack container;
        private final IBatteryItem batteryItem;

        public Wrapper(@NotNull ItemStack container) {
            this.container = container;
            this.batteryItem = (IBatteryItem) container.getItem();
        }

        @Override
        public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CapabilityEnergy.ENERGY;
        }

        @Nullable
        @Override
        public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == CapabilityEnergy.ENERGY ? CapabilityEnergy.ENERGY.cast(this) : null;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!canReceive() || maxReceive <= 0 || GeneralConfig.conversionRateHeToRF <= 0) return 0;
            long heBudget = (long) Math.floor(maxReceive / GeneralConfig.conversionRateHeToRF);
            if (heBudget <= 0) return simulate ? 1 : 0;
            long spaceHE = batteryItem.getMaxCharge(container) - batteryItem.getCharge(container);
            long heCanAccept = Math.min(spaceHE, batteryItem.getChargeRate(container));
            long heAccepted = Math.min(heBudget, heCanAccept);
            if (heAccepted > 0 && !simulate) batteryItem.chargeBattery(container, heAccepted);
            long feAccepted = Math.round(heAccepted * GeneralConfig.conversionRateHeToRF);
            return (int) Math.min(maxReceive, Math.min(Integer.MAX_VALUE, feAccepted));
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (!canExtract() || maxExtract <= 0 || GeneralConfig.conversionRateHeToRF <= 0) return 0;
            long heBudget = (long) Math.floor(maxExtract / GeneralConfig.conversionRateHeToRF);
            if (heBudget <= 0) return simulate ? 1 : 0;
            long heAvailable = Math.min(batteryItem.getCharge(container), batteryItem.getDischargeRate(container));
            long heExtracted = Math.min(heBudget, heAvailable);
            if (heExtracted > 0 && !simulate) batteryItem.dischargeBattery(container, heExtracted);
            long feExtracted = Math.round(heExtracted * GeneralConfig.conversionRateHeToRF);
            return (int) Math.min(maxExtract, Math.min(Integer.MAX_VALUE, feExtracted));
        }

        @Override
        public int getEnergyStored() {
            return GeneralConfig.conversionRateHeToRF <= 0 ? 0 : (int) Math.min(Integer.MAX_VALUE,
                    Math.round(batteryItem.getCharge(container) * GeneralConfig.conversionRateHeToRF));
        }

        @Override
        public int getMaxEnergyStored() {
            return GeneralConfig.conversionRateHeToRF <= 0 ? 0 : (int) Math.min(Integer.MAX_VALUE,
                    Math.round(batteryItem.getMaxCharge(container) * GeneralConfig.conversionRateHeToRF));
        }

        @Override
        public boolean canExtract() {
            return batteryItem.getDischargeRate(container) > 0 && batteryItem.getCharge(container) > 0;
        }

        @Override
        public boolean canReceive() {
            return batteryItem.getChargeRate(container) > 0 && batteryItem.getCharge(container) < batteryItem.getMaxCharge(container);
        }
    }
}
