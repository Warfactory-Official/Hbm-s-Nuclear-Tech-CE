package com.hbm.api.fluidmk2;

import com.hbm.api.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import com.hbm.api.energymk2.PowerNetMK2;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.uninos.NodeNet;
import com.hbm.util.Tuple;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.ArrayList;
import java.util.List;

/**
 * Slightly modified to prevent precision problems in upstream
 *
 * @author hbm, mlbv
 */
public class FluidNetMK2 extends NodeNet<IFluidReceiverMK2, IFluidProviderMK2, FluidNode, FluidNetMK2> {

    public long fluidTracker = 0L;

    protected static int timeout = 3_000;
    protected static long currentTime = 0;
    protected FluidType type;

    public FluidNetMK2(FluidType type) {
        this.type = type;
        for(int i = 0; i < IFluidUserMK2.HIGHEST_VALID_PRESSURE + 1; i++) providers[i] = new ArrayList<>();
        for(int i = 0; i < IFluidUserMK2.HIGHEST_VALID_PRESSURE + 1; i++) for(int j = 0; j < ConnectionPriority.VALUES.length; j++) receivers[i][j] = new ArrayList<>();
    }

    @Override public void resetTrackers() { this.fluidTracker = 0; }

    @Override
    public void update() {

        if(providerEntries.isEmpty()) return;
        if(receiverEntries.isEmpty()) return;
        currentTime = System.currentTimeMillis();

        setupFluidProviders();
        setupFluidReceivers();
        transferFluid();

        cleanUp();
    }

    //this sucks ass, but it makes the code just a smidge more structured
    public long[] fluidAvailable = new long[IFluidUserMK2.HIGHEST_VALID_PRESSURE + 1];
    public List<Tuple.ObjectLongPair<IFluidProviderMK2>>[] providers = new ArrayList[IFluidUserMK2.HIGHEST_VALID_PRESSURE + 1];
    public long[][] fluidDemand = new long[IFluidUserMK2.HIGHEST_VALID_PRESSURE + 1][ConnectionPriority.VALUES.length];
    public List<Tuple.ObjectLongPair<IFluidReceiverMK2>>[][] receivers = new ArrayList[IFluidUserMK2.HIGHEST_VALID_PRESSURE + 1][ConnectionPriority.VALUES.length];
    public long[] transfered = new long[IFluidUserMK2.HIGHEST_VALID_PRESSURE + 1];
    private final int[][] receiverRemainderCursor = new int[IFluidUserMK2.HIGHEST_VALID_PRESSURE + 1][ConnectionPriority.VALUES.length];
    private final int[] providerRemainderCursor = new int[IFluidUserMK2.HIGHEST_VALID_PRESSURE + 1];

    private static int normalizedCursor(int cursor, int size) {
        return size <= 0 ? 0 : Math.floorMod(cursor, size);
    }

    public void setupFluidProviders() {
        ObjectIterator<Object2LongMap.Entry<IFluidProviderMK2>> iterator = providerEntries.object2LongEntrySet().fastIterator();

        while(iterator.hasNext()) {
            Object2LongMap.Entry<IFluidProviderMK2> entry = iterator.next();
            if(currentTime - entry.getLongValue() > timeout || isBadLink(entry.getKey())) { iterator.remove(); continue; }
            IFluidProviderMK2 provider = entry.getKey();
            int[] pressureRange = provider.getProvidingPressureRange(type);
            for(int p = pressureRange[0]; p <= pressureRange[1]; p++) {
                long available = Math.min(provider.getFluidAvailable(type, p), provider.getProviderSpeed(type, p));
                providers[p].add(new Tuple.ObjectLongPair<>(provider, available));
                fluidAvailable[p] += available;
            }
        }
    }

    public void setupFluidReceivers() {
        ObjectIterator<Object2LongMap.Entry<IFluidReceiverMK2>> iterator = receiverEntries.object2LongEntrySet().fastIterator();

        while(iterator.hasNext()) {
            Object2LongMap.Entry<IFluidReceiverMK2> entry = iterator.next();
            if(currentTime - entry.getLongValue() > timeout || isBadLink(entry.getKey())) { iterator.remove(); continue; }
            IFluidReceiverMK2 receiver = entry.getKey();
            int[] pressureRange = receiver.getReceivingPressureRange(type);
            for(int p = pressureRange[0]; p <= pressureRange[1]; p++) {
                long required = Math.min(receiver.getDemand(type, p), receiver.getReceiverSpeed(type, p));
                int priority = receiver.getFluidPriority().ordinal();
                receivers[p][priority].add(new Tuple.ObjectLongPair(receiver, required));
                fluidDemand[p][priority] += required;
            }
        }
    }

    public void transferFluid() {

        long[] received = new long[IFluidUserMK2.HIGHEST_VALID_PRESSURE + 1];
        long[] notAccountedFor = new long[IFluidUserMK2.HIGHEST_VALID_PRESSURE + 1];

        for(int p = 0; p <= IFluidUserMK2.HIGHEST_VALID_PRESSURE; p++) { // if the pressure range were ever to increase, we might have to rethink this

            long totalAvailable = fluidAvailable[p];

            for(int i = ConnectionPriority.VALUES.length - 1; i >= 0; i--) {

                long priorityDemand = fluidDemand[p][i];
                List<Tuple.ObjectLongPair<IFluidReceiverMK2>> list = receivers[p][i];
                if(priorityDemand <= 0 || list.isEmpty() || totalAvailable <= 0) continue;
                long toTransfer = Math.min(priorityDemand, totalAvailable);
                long sentThisPriority = 0L;
                long remainingDemand = priorityDemand;
                int receiverCount = list.size();
                int receiverStart = normalizedCursor(receiverRemainderCursor[p][i], receiverCount);
                for(int step = 0; step < receiverCount && sentThisPriority < toTransfer; step++) {
                    Tuple.ObjectLongPair<IFluidReceiverMK2> entry = list.get((receiverStart + step) % receiverCount);
                    long receiverDemand = entry.getValue();
                    long remainingBudget = toTransfer - sentThisPriority;
                    long maxForReceiver = Math.min(receiverDemand, remainingBudget);
                    if(maxForReceiver <= 0) {
                        remainingDemand -= receiverDemand;
                        continue;
                    }

                    long toSend;
                    toSend = step == receiverCount - 1 ? maxForReceiver : PowerNetMK2.weightedShare(remainingBudget,
                            receiverDemand, remainingDemand, maxForReceiver);
                    if(toSend <= 0) {
                        remainingDemand -= receiverDemand;
                        continue;
                    }

                    long accepted = toSend - entry.getKey().transferFluid(type, p, toSend);
                    if(accepted > 0) {
                        long accounted = Math.min(accepted, toSend);
                        sentThisPriority += accounted;
                        received[p] += accounted;
                        fluidTracker += accounted;
                    }
                    remainingDemand -= receiverDemand;
                }
                receiverRemainderCursor[p][i] = (receiverStart + 1) % receiverCount;

                totalAvailable -= sentThisPriority;
            }

            notAccountedFor[p] = received[p];
        }

        for(int p = 0; p <= IFluidUserMK2.HIGHEST_VALID_PRESSURE; p++) {

            if(fluidAvailable[p] <= 0 || received[p] <= 0 || providers[p].isEmpty()) continue;
            long remainingToDebit = notAccountedFor[p];
            long remainingSupply = fluidAvailable[p];

            int providerCount = providers[p].size();
            int providerStart = normalizedCursor(providerRemainderCursor[p], providerCount);
            for(int step = 0; step < providerCount && remainingToDebit > 0; step++) {
                Tuple.ObjectLongPair<IFluidProviderMK2> entry = providers[p].get((providerStart + step) % providerCount);
                long providerSupply = entry.getValue();
                long maxForProvider = Math.min(providerSupply, remainingToDebit);
                if(maxForProvider <= 0) {
                    remainingSupply -= providerSupply;
                    continue;
                }

                long toUse;
                if (step == providerCount - 1) {
                    toUse = maxForProvider;
                } else {
                    toUse = PowerNetMK2.weightedShare(remainingToDebit, providerSupply, remainingSupply, maxForProvider);
                }
                if(toUse <= 0) {
                    remainingSupply -= providerSupply;
                    continue;
                }

                entry.getKey().useUpFluid(type, p, toUse);
                remainingToDebit -= toUse;
                remainingSupply -= providerSupply;
            }
            notAccountedFor[p] = remainingToDebit;
            if(providerCount > 0) providerRemainderCursor[p] = (providerStart + 1) % providerCount;
        }
    }

    public void cleanUp() {
        for(int i = 0; i < IFluidUserMK2.HIGHEST_VALID_PRESSURE + 1; i++) {
            fluidAvailable[i] = 0;
            providers[i].clear();
            transfered[i] = 0;

            for(int j = 0; j < ConnectionPriority.VALUES.length; j++) {
                fluidDemand[i][j] = 0;
                receivers[i][j].clear();
            }
        }
    }
}
