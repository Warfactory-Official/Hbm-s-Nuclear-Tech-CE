package com.hbm.api.fluidmk2;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FluidNetTest {

    private static final FluidTankNTM[] NO_TANKS = new FluidTankNTM[0];

    @Test
    void updateRotatesRemainderAcrossReceivers() {
        FluidNetMK2 net = new FluidNetMK2(null);
        CountingFluidProvider provider = new CountingFluidProvider(1, 1L, 1L);
        CountingFluidReceiver receiverA = new CountingFluidReceiver(2, 1L);
        CountingFluidReceiver receiverB = new CountingFluidReceiver(3, 1L);

        net.addProvider(provider);
        net.addReceiver(receiverA);
        net.addReceiver(receiverB);

        net.update();
        net.update();

        assertEquals(1L, receiverA.received);
        assertEquals(1L, receiverB.received);
        assertEquals(2L, provider.used);
    }

    @Test
    void updateRotatesRemainderAcrossProviders() {
        FluidNetMK2 net = new FluidNetMK2(null);
        CountingFluidProvider providerA = new CountingFluidProvider(11, 1L, 1L);
        CountingFluidProvider providerB = new CountingFluidProvider(22, 1L, 1L);
        CountingFluidReceiver receiver = new CountingFluidReceiver(33, 1L);

        net.addProvider(providerA);
        net.addProvider(providerB);
        net.addReceiver(receiver);

        net.update();
        net.update();

        assertEquals(2L, receiver.received);
        assertEquals(1L, providerA.used);
        assertEquals(1L, providerB.used);
    }

    @Test
    void updateAccountsAllTransferredFluidWithManyProvidersAndLowDemand() {
        FluidNetMK2 net = new FluidNetMK2(null);
        int providerCount = 1000;
        CountingFluidProvider[] providers = new CountingFluidProvider[providerCount];

        for (int i = 0; i < providerCount; i++) {
            CountingFluidProvider provider = new CountingFluidProvider(1000 + i, 1L, 1L);
            providers[i] = provider;
            net.addProvider(provider);
        }

        CountingFluidReceiver receiver = new CountingFluidReceiver(2000, 500L);
        net.addReceiver(receiver);

        net.update();

        long totalUsed = 0L;
        for (CountingFluidProvider provider : providers) totalUsed += provider.used;

        assertEquals(500L, receiver.received);
        assertEquals(receiver.received, totalUsed);
    }

    private static final class CountingFluidReceiver implements IFluidReceiverMK2 {
        private final int hash;
        private final long demandPerTick;
        private long received;

        private CountingFluidReceiver(int hash, long demandPerTick) {
            this.hash = hash;
            this.demandPerTick = demandPerTick;
        }

        @Override
        public long transferFluid(FluidType type, int pressure, long amount) {
            if (pressure == 0 && amount > 0) received += amount;
            return 0L;
        }

        @Override
        public long getReceiverSpeed(FluidType type, int pressure) {
            return pressure == 0 ? demandPerTick : 0L;
        }

        @Override
        public long getDemand(FluidType type, int pressure) {
            return pressure == 0 ? demandPerTick : 0L;
        }

        @Override
        public FluidTankNTM[] getAllTanks() {
            return NO_TANKS;
        }

        @Override
        public boolean isLoaded() {
            return true;
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    private static final class CountingFluidProvider implements IFluidProviderMK2 {
        private final int hash;
        private final long availablePerTick;
        private final long speedPerTick;
        private long used;

        private CountingFluidProvider(int hash, long availablePerTick, long speedPerTick) {
            this.hash = hash;
            this.availablePerTick = availablePerTick;
            this.speedPerTick = speedPerTick;
        }

        @Override
        public void useUpFluid(FluidType type, int pressure, long amount) {
            if (pressure == 0 && amount > 0) used += amount;
        }

        @Override
        public long getProviderSpeed(FluidType type, int pressure) {
            return pressure == 0 ? speedPerTick : 0L;
        }

        @Override
        public long getFluidAvailable(FluidType type, int pressure) {
            return pressure == 0 ? availablePerTick : 0L;
        }

        @Override
        public FluidTankNTM[] getAllTanks() {
            return NO_TANKS;
        }

        @Override
        public boolean isLoaded() {
            return true;
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
