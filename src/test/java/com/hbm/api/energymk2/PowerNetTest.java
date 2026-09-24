package com.hbm.api.energymk2;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PowerNetTest {

    @Test
    void weightedShareHandlesOverflowAndMatchesBigInteger() {
        long total = Long.MAX_VALUE - 5L;
        long part = Long.MAX_VALUE - 11L;
        long whole = Long.MAX_VALUE - 3L;

        BigInteger expected = BigInteger.valueOf(total).multiply(BigInteger.valueOf(part))
                                        .divide(BigInteger.valueOf(whole));

        long actual = PowerNetMK2.weightedShare(total, part, whole, Long.MAX_VALUE);
        assertEquals(expected.longValue(), actual);

        long cap = 123_456_789L;
        long capped = PowerNetMK2.weightedShare(total, part, whole, cap);
        assertEquals(cap, capped);
    }

    @Test
    void updateRotatesRemainderAcrossReceivers() {
        PowerNetMK2 net = new PowerNetMK2();
        CountingProvider provider = new CountingProvider(10, 1L, 1L);
        CountingReceiver receiverA = new CountingReceiver(20);
        CountingReceiver receiverB = new CountingReceiver(30);

        net.addProvider(provider);
        net.addReceiver(receiverA);
        net.addReceiver(receiverB);

        net.update();
        net.update();

        assertEquals(1L, receiverA.accepted);
        assertEquals(1L, receiverB.accepted);
        assertEquals(2L, provider.used);
    }

    @Test
    void updateRotatesRemainderAcrossProviders() {
        PowerNetMK2 net = new PowerNetMK2();
        CountingProvider providerA = new CountingProvider(11, 1L, 1L);
        CountingProvider providerB = new CountingProvider(22, 1L, 1L);
        CountingReceiver receiver = new CountingReceiver(33);

        net.addProvider(providerA);
        net.addProvider(providerB);
        net.addReceiver(receiver);

        net.update();
        net.update();

        assertEquals(2L, receiver.accepted);
        assertEquals(1L, providerA.used);
        assertEquals(1L, providerB.used);
    }

    @Test
    void updateAccountsAllTransferredPowerWithManyProvidersAndLowDemand() {
        PowerNetMK2 net = new PowerNetMK2();
        int providerCount = 1000;
        CountingProvider[] providers = new CountingProvider[providerCount];

        for (int i = 0; i < providerCount; i++) {
            CountingProvider provider = new CountingProvider(1000 + i, 1L, 1L);
            providers[i] = provider;
            net.addProvider(provider);
        }

        CountingReceiver receiver = new CountingReceiver(2000, 500L);
        net.addReceiver(receiver);

        net.update();

        long totalUsed = 0L;
        for (CountingProvider provider : providers) totalUsed += provider.used;

        assertEquals(500L, receiver.accepted);
        assertEquals(receiver.accepted, totalUsed);
    }

    @Test
    void sendPowerDiodeSimulationDoesNotAdvanceRemainderCursor() {
        PowerNetMK2 simulatedFirst = new PowerNetMK2();
        CountingReceiver simA = new CountingReceiver(101);
        CountingReceiver simB = new CountingReceiver(202);
        simulatedFirst.addReceiver(simA);
        simulatedFirst.addReceiver(simB);

        long leftoverSimulated = simulatedFirst.sendPowerDiode(1L, true);
        long leftoverRealAfterSim = simulatedFirst.sendPowerDiode(1L, false);

        PowerNetMK2 baseline = new PowerNetMK2();
        CountingReceiver baseA = new CountingReceiver(101);
        CountingReceiver baseB = new CountingReceiver(202);
        baseline.addReceiver(baseA);
        baseline.addReceiver(baseB);

        long leftoverBaseline = baseline.sendPowerDiode(1L, false);

        assertEquals(0L, leftoverSimulated);
        assertEquals(0L, leftoverRealAfterSim);
        assertEquals(0L, leftoverBaseline);
        assertEquals(baseA.accepted, simA.accepted);
        assertEquals(baseB.accepted, simB.accepted);
    }

    @Test
    void extractPowerDiodeRotatesRemainderAcrossProviders() {
        PowerNetMK2 net = new PowerNetMK2();
        CountingProvider providerA = new CountingProvider(7, 1L, 1L);
        CountingProvider providerB = new CountingProvider(8, 1L, 1L);
        net.addProvider(providerA);
        net.addProvider(providerB);

        long first = net.extractPowerDiode(1L, false);
        long second = net.extractPowerDiode(1L, false);

        assertEquals(1L, first);
        assertEquals(1L, second);
        assertEquals(1L, providerA.used);
        assertEquals(1L, providerB.used);
    }

    @Test
    void dummyReddBackToBackBufferModeConservesEnergy() {
        PowerNetMK2 net = new PowerNetMK2();
        DummyRedd left = new DummyRedd(1, 1_000_000L);
        DummyRedd right = new DummyRedd(2, 0L);

        net.addProvider(left);
        net.addReceiver(left);
        net.addProvider(right);
        net.addReceiver(right);

        BigInteger initialTotal = left.getActualPower().add(right.getActualPower());

        for (int tick = 0; tick < 200; tick++) {
            net.update();
            BigInteger total = left.getActualPower().add(right.getActualPower());

            assertEquals(initialTotal, total, "Energy was not conserved at tick " + tick);
            assertTrue(left.getActualPower().signum() >= 0, "Left battery went negative at tick " + tick);
            assertTrue(right.getActualPower().signum() >= 0, "Right battery went negative at tick " + tick);
        }
    }

    @Test
    void dummyNormalBatteryBackToBackBufferModeConservesEnergy() {
        PowerNetMK2 net = new PowerNetMK2();
        DummyBattery left = new DummyBattery(101, 1_000_000L, 1_000_000L);
        DummyBattery right = new DummyBattery(202, 0L, 1_000_000L);

        // buffer mode
        net.addProvider(left);
        net.addReceiver(left);
        net.addProvider(right);
        net.addReceiver(right);

        long initialTotal = left.getPower() + right.getPower();

        for (int tick = 0; tick < 200; tick++) {
            net.update();
            long total = left.getPower() + right.getPower();

            assertEquals(initialTotal, total, "Energy was not conserved at tick " + tick);
            assertTrue(left.getPower() >= 0L, "Left battery went negative at tick " + tick);
            assertTrue(right.getPower() >= 0L, "Right battery went negative at tick " + tick);
            assertTrue(left.getPower() <= left.getMaxPower(), "Left battery exceeded capacity at tick " + tick);
            assertTrue(right.getPower() <= right.getMaxPower(), "Right battery exceeded capacity at tick " + tick);
        }
    }

    private static final class CountingReceiver implements IEnergyReceiverMK2 {
        private final int hash;
        private final long demandPerTick;
        private long accepted;

        private CountingReceiver(int hash) {
            this(hash, 1L);
        }

        private CountingReceiver(int hash, long demandPerTick) {
            this.hash = hash;
            this.demandPerTick = demandPerTick;
        }

        @Override
        public long transferPower(long power, boolean simulate) {
            if (!simulate && power > 0) accepted += power;
            return 0L;
        }

        @Override
        public long getReceiverSpeed() {
            return demandPerTick;
        }

        @Override
        public long getPower() {
            return 0L;
        }

        @Override
        public void setPower(long power) {
        }

        @Override
        public long getMaxPower() {
            return Long.MAX_VALUE;
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

    private static final class CountingProvider implements IEnergyProviderMK2 {
        private final int hash;
        private final long availablePerTick;
        private final long speedPerTick;
        private long used;

        private CountingProvider(int hash, long availablePerTick, long speedPerTick) {
            this.hash = hash;
            this.availablePerTick = availablePerTick;
            this.speedPerTick = speedPerTick;
        }

        @Override
        public void usePower(long power) {
            if (power > 0) used += power;
        }

        @Override
        public long getProviderSpeed() {
            return speedPerTick;
        }

        @Override
        public long getPower() {
            return availablePerTick;
        }

        @Override
        public void setPower(long power) {
        }

        @Override
        public long getMaxPower() {
            return Long.MAX_VALUE;
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

    private static final class DummyRedd implements IEnergyProviderMK2, IEnergyReceiverMK2 {
        private final int hash;
        private BigInteger power;

        private DummyRedd(int hash, long initialPower) {
            this.hash = hash;
            power = BigInteger.valueOf(initialPower);
        }

        private BigInteger getActualPower() {
            return power;
        }

        @Override
        public void usePower(long power) {
            if (power > 0) {
                this.power = this.power.subtract(BigInteger.valueOf(power));
            }
        }

        @Override
        public long transferPower(long power, boolean simulate) {
            if (!simulate && power > 0) {
                this.power = this.power.add(BigInteger.valueOf(power));
            }
            return 0L;
        }

        @Override
        public long getPower() {
            return power.min(BigInteger.valueOf(getMaxPower() / 2L)).longValue();
        }

        @Override
        public void setPower(long power) {
            this.power = BigInteger.valueOf(power);
        }

        @Override
        public long getMaxPower() {
            return Long.MAX_VALUE / 100L;
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

    private static final class DummyBattery implements IEnergyProviderMK2, IEnergyReceiverMK2 {
        private final int hash;
        private final long maxPower;
        private long power;

        private DummyBattery(int hash, long initialPower, long maxPower) {
            this.hash = hash;
            this.maxPower = maxPower;
            power = Math.max(0L, Math.min(initialPower, maxPower));
        }

        @Override
        public long getProviderSpeed() {
            return maxPower / 20L;
        }

        @Override
        public long getReceiverSpeed() {
            return maxPower / 20L;
        }

        @Override
        public long getPower() {
            return power;
        }

        @Override
        public void setPower(long power) {
            this.power = Math.max(0L, Math.min(power, maxPower));
        }

        @Override
        public long getMaxPower() {
            return maxPower;
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
