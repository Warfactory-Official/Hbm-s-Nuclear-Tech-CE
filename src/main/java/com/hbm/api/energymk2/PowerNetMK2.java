package com.hbm.api.energymk2;

import com.google.common.math.LongMath;
import com.hbm.uninos.NodeNet;
import com.hbm.util.Tuple;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Technically MK3 since it's now UNINOS compatible, although UNINOS was build out of 95% nodespace code
 * Slightly modified to prevent precision problems in upstream
 *
 * @author hbm, mlbv
 */
public class PowerNetMK2 extends NodeNet<IEnergyReceiverMK2, IEnergyProviderMK2, Nodespace.PowerNode, PowerNetMK2> {

    public long energyTracker = 0L;

    private final ReentrantLock lock = new ReentrantLock();
    private final int[] updateReceiverRemainderCursor = new int[IEnergyReceiverMK2.ConnectionPriority.VALUES.length];
    private final int[] diodeReceiverRemainderCursor = new int[IEnergyReceiverMK2.ConnectionPriority.VALUES.length];
    private int updateProviderRemainderCursor = 0;
    private int diodeProviderRemainderCursor = 0;

    protected static int timeout = 3_000;

    @Override public void resetTrackers() { this.energyTracker = 0; }

    public static long weightedShare(long total, long part, long whole, long cap) {
        if (total <= 0 || part <= 0 || whole <= 0 || cap <= 0) return 0;
        if (part >= whole) return Math.min(total, cap);
        if (total <= Long.MAX_VALUE / part) {
            // this should be 99% of the case
            long share = (total * part) / whole;
            if (share <= 0) return 0;
            return Math.min(share, cap);
        }
        long reducedTotal = total;
        long reducedPart = part;
        long reducedWhole = whole;

        long gcdTW = LongMath.gcd(reducedTotal, reducedWhole);
        reducedTotal /= gcdTW;
        reducedWhole /= gcdTW;

        long gcdPW = LongMath.gcd(reducedPart, reducedWhole);
        reducedPart /= gcdPW;
        reducedWhole /= gcdPW;

        long share;
        if (reducedTotal <= Long.MAX_VALUE / reducedPart) {
            share = (reducedTotal * reducedPart) / reducedWhole;
        } else {
            BigInteger numerator = BigInteger.valueOf(reducedTotal).multiply(BigInteger.valueOf(reducedPart));
            BigInteger exactShare = numerator.divide(BigInteger.valueOf(reducedWhole));
            if (exactShare.signum() <= 0) return 0;
            BigInteger capLimit = BigInteger.valueOf(cap);
            if (exactShare.compareTo(capLimit) >= 0) return cap;
            share = exactShare.longValue();
        }

        if (share <= 0) return 0;
        return Math.min(share, cap);
    }

    private static int normalizedCursor(int cursor, int size) {
        return size <= 0 ? 0 : Math.floorMod(cursor, size);
    }

    @Override
    public void update() {

        if(providerEntries.isEmpty()) return;
        if(receiverEntries.isEmpty()) return;

        long timestamp = System.currentTimeMillis();

        List<Tuple.ObjectLongPair<IEnergyProviderMK2>> providers = new ArrayList<>();
        long powerAvailable = 0;

        // sum up total demand, categorized by priority
        List<Tuple.ObjectLongPair<IEnergyReceiverMK2>>[] receivers = new ArrayList[IEnergyReceiverMK2.ConnectionPriority.VALUES.length];
        for(int i = 0; i < receivers.length; i++) receivers[i] = new ArrayList<>();
        long[] demand = new long[IEnergyReceiverMK2.ConnectionPriority.VALUES.length];
        long totalDemand = 0;

        // sum up available power
        lock.lock();
        try {
            var provIt = providerEntries.object2LongEntrySet().fastIterator();
            while(provIt.hasNext()) {
                var entry = provIt.next();
                IEnergyProviderMK2 p = entry.getKey();
                if(timestamp - entry.getLongValue() > timeout || isBadLink(p)) { provIt.remove(); continue; }
                long src = Math.min(p.getPower(), p.getProviderSpeed());
                if(src > 0) {
                    providers.add(new Tuple.ObjectLongPair<>(p, src));
                    powerAvailable += src;
                }
            }

            var recIt = receiverEntries.object2LongEntrySet().fastIterator();

            while(recIt.hasNext()) {
                var entry = recIt.next();
                IEnergyReceiverMK2 r = entry.getKey();
                if(timestamp - entry.getLongValue() > timeout || isBadLink(r)) { recIt.remove(); continue; }
                long rec = Math.min(r.getMaxPower() - r.getPower(), r.getReceiverSpeed());
                if(rec > 0) {
                    int p = r.getPriority().ordinal();
                    receivers[p].add(new Tuple.ObjectLongPair<>(r, rec));
                    demand[p] += rec;
                    totalDemand += rec;
                }
            }
        } finally {
            lock.unlock();
        }

        long toTransfer = Math.min(powerAvailable, totalDemand);
        long energyUsed = 0;

        // add power to receivers, ordered by priority
        for(int i = IEnergyReceiverMK2.ConnectionPriority.VALUES.length - 1; i >= 0; i--) {
            var list = receivers[i];
            long priorityDemand = demand[i];
            if (priorityDemand <= 0 || list.isEmpty() || toTransfer <= 0) continue;

            long priorityTransfer = Math.min(toTransfer, priorityDemand);
            long priorityUsed = 0;
            long remainingDemand = priorityDemand;
            int receiverCount = list.size();
            int receiverStart = normalizedCursor(updateReceiverRemainderCursor[i], receiverCount);

            for (int step = 0; step < receiverCount && priorityUsed < priorityTransfer; step++) {
                var entry = list.get((receiverStart + step) % receiverCount);
                long receiverDemand = entry.getValue();
                long remainingBudget = priorityTransfer - priorityUsed;
                long maxForReceiver = Math.min(receiverDemand, remainingBudget);
                if (maxForReceiver <= 0) {
                    remainingDemand -= receiverDemand;
                    continue;
                }

                long toSend = step == receiverCount - 1
                        ? maxForReceiver
                        : weightedShare(remainingBudget, receiverDemand, remainingDemand, maxForReceiver);

                if (toSend <= 0) {
                    remainingDemand -= receiverDemand;
                    continue;
                }

                long accepted = toSend - entry.getKey().transferPower(toSend, false);
                if (accepted > 0) {
                    priorityUsed += Math.min(accepted, toSend);
                }
                remainingDemand -= receiverDemand;
            }
            updateReceiverRemainderCursor[i] = (receiverStart + 1) % receiverCount;

            // subtract only this priority bucket's accepted total
            energyUsed += priorityUsed;
            toTransfer -= priorityUsed;
        }

        this.energyTracker += energyUsed;
        long remainingToDebit = energyUsed;
        long remainingSupply = powerAvailable;

        // remove power from providers
        int providerCount = providers.size();
        int providerStart = normalizedCursor(updateProviderRemainderCursor, providerCount);
        for (int step = 0; step < providerCount && remainingToDebit > 0; step++) {
            var entry = providers.get((providerStart + step) % providerCount);
            long providerSupply = entry.getValue();
            long maxForProvider = Math.min(providerSupply, remainingToDebit);
            if (maxForProvider <= 0) {
                remainingSupply -= providerSupply;
                continue;
            }

            long toUse = step == providerCount - 1
                    ? maxForProvider
                    : weightedShare(remainingToDebit, providerSupply, remainingSupply, maxForProvider);
            if (toUse <= 0) {
                remainingSupply -= providerSupply;
                continue;
            }

            entry.getKey().usePower(toUse);
            remainingToDebit -= toUse;
            remainingSupply -= providerSupply;
        }
        if (providerCount > 0) updateProviderRemainderCursor = (providerStart + 1) % providerCount;
    }

    public long sendPowerDiode(long power, boolean simulate) {
        if (receiverEntries.isEmpty()) return power;

        long timestamp = System.currentTimeMillis();

        List<Tuple.ObjectLongPair<IEnergyReceiverMK2>>[] receivers = new ArrayList[IEnergyReceiverMK2.ConnectionPriority.VALUES.length];
        for(int i = 0; i < receivers.length; i++) receivers[i] = new ArrayList<>();
        long[] demand = new long[IEnergyReceiverMK2.ConnectionPriority.VALUES.length];
        long totalDemand = 0;
        lock.lock();
        try {
            var recIt = receiverEntries.object2LongEntrySet().fastIterator();
            while(recIt.hasNext()) {
                var entry = recIt.next();
                if(timestamp - entry.getLongValue() > timeout) { recIt.remove(); continue; }
                long rec = Math.min(entry.getKey().getMaxPower() - entry.getKey().getPower(), entry.getKey().getReceiverSpeed());
                int p = entry.getKey().getPriority().ordinal();
                receivers[p].add(new Tuple.ObjectLongPair<>(entry.getKey(), rec));
                demand[p] += rec;
                totalDemand += rec;
            }
        } finally {
            lock.unlock();
        }

        long toTransfer = Math.min(power, totalDemand);
        long energyUsed = 0;

        for(int i = IEnergyReceiverMK2.ConnectionPriority.VALUES.length - 1; i >= 0; i--) {
            var list = receivers[i];
            long priorityDemand = demand[i];
            if (priorityDemand <= 0 || list.isEmpty() || toTransfer <= 0) continue;

            long priorityTransfer = Math.min(toTransfer, priorityDemand);
            long priorityUsed = 0;
            long remainingDemand = priorityDemand;
            int receiverCount = list.size();
            int receiverStart = normalizedCursor(diodeReceiverRemainderCursor[i], receiverCount);

            for (int step = 0; step < receiverCount && priorityUsed < priorityTransfer; step++) {
                var entry = list.get((receiverStart + step) % receiverCount);
                long receiverDemand = entry.getValue();
                long remainingBudget = priorityTransfer - priorityUsed;
                long maxForReceiver = Math.min(receiverDemand, remainingBudget);
                if (maxForReceiver <= 0) {
                    remainingDemand -= receiverDemand;
                    continue;
                }

                long toSend = step == receiverCount - 1
                        ? maxForReceiver
                        : weightedShare(remainingBudget, receiverDemand, remainingDemand, maxForReceiver);
                if (toSend <= 0) {
                    remainingDemand -= receiverDemand;
                    continue;
                }

                long accepted = toSend - entry.getKey().transferPower(toSend, simulate);
                if (accepted > 0) {
                    priorityUsed += Math.min(accepted, toSend);
                }
                remainingDemand -= receiverDemand;
            }
            if (!simulate) diodeReceiverRemainderCursor[i] = (receiverStart + 1) % receiverCount;

            energyUsed += priorityUsed;
            toTransfer -= priorityUsed;
        }

        if (!simulate) this.energyTracker += energyUsed;

        return power - energyUsed;
    }

    public long extractPowerDiode(long power, boolean simulate) {
        if (providerEntries.isEmpty() || power <= 0) return 0;

        long timestamp = System.currentTimeMillis();

        List<Tuple.ObjectLongPair<IEnergyProviderMK2>> providers = new ArrayList<>();
        long supply = 0;
        lock.lock();
        try {
            var provIt = providerEntries.object2LongEntrySet().fastIterator();
            while (provIt.hasNext()) {
                var entry = provIt.next();
                if (timestamp - entry.getLongValue() > timeout) {
                    if (!simulate) provIt.remove();
                    continue;
                }
                long prov = Math.min(entry.getKey().getPower(), entry.getKey().getProviderSpeed());
                if (prov > 0) {
                    providers.add(new Tuple.ObjectLongPair<>(entry.getKey(), prov));
                    supply += prov;
                }
            }
        } finally {
            lock.unlock();
        }

        if (supply <= 0) return 0;

        long powerToExtract = Math.min(power, supply);
        long totalExtracted = 0;

        long remainingToExtract = powerToExtract;
        long remainingSupply = supply;

        int providerCount = providers.size();
        int providerStart = normalizedCursor(diodeProviderRemainderCursor, providerCount);
        for (int step = 0; step < providerCount && remainingToExtract > 0; step++) {
            var entry = providers.get((providerStart + step) % providerCount);
            long providerSupply = entry.getValue();
            long maxForProvider = Math.min(providerSupply, remainingToExtract);
            if (maxForProvider <= 0) {
                remainingSupply -= providerSupply;
                continue;
            }

            long toExtract = step == providerCount - 1
                    ? maxForProvider
                    : weightedShare(remainingToExtract, providerSupply, remainingSupply, maxForProvider);
            if (toExtract <= 0) {
                remainingSupply -= providerSupply;
                continue;
            }

            long actualExtract = Math.min(toExtract, entry.getKey().getPower());
            if (!simulate) entry.getKey().usePower(actualExtract);
            totalExtracted += actualExtract;
            remainingToExtract -= actualExtract;
            remainingSupply -= providerSupply;
        }
        if (!simulate && providerCount > 0) diodeProviderRemainderCursor = (providerStart + 1) % providerCount;

        if (!simulate) {
            this.energyTracker += totalExtracted;
        }

        return totalExtracted;
    }
}
