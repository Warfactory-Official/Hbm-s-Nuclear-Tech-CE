package com.hbm.api.fluidmk2;

import com.hbm.api.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.uninos.NodeNet;
import com.hbm.util.Tuple;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.ArrayList;
import java.util.List;

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

                long toTransfer = Math.min(fluidDemand[p][i], totalAvailable);
                if(toTransfer <= 0) continue;

                long priorityDemand = fluidDemand[p][i];
                long sentThisPriority = 0L;

                for(Tuple.ObjectLongPair<IFluidReceiverMK2> entry : receivers[p][i]) {
                    double weight = (double) entry.getValue() / (double) (priorityDemand);
                    long toSend = (long) Math.max(toTransfer * weight, 0D);
                    toSend -= entry.getKey().transferFluid(type, p, toSend);
                    sentThisPriority += toSend;
                    received[p] += toSend;
                    fluidTracker += toSend;
                }

                totalAvailable -= sentThisPriority;
            }

            notAccountedFor[p] = received[p];
        }

        for(int p = 0; p <= IFluidUserMK2.HIGHEST_VALID_PRESSURE; p++) {

            for(Tuple.ObjectLongPair<IFluidProviderMK2> entry : providers[p]) {
                double weight = (double) entry.getValue() / (double) fluidAvailable[p];
                long toUse = (long) Math.max(received[p] * weight, 0D);
                entry.getKey().useUpFluid(type, p, toUse);
                notAccountedFor[p] -= toUse;
            }
        }

        for(int p = 0; p <= IFluidUserMK2.HIGHEST_VALID_PRESSURE; p++) {

            int iterationsLeft = 100;
            while(iterationsLeft > 0 && notAccountedFor[p] > 0 && !providers[p].isEmpty()) {
                iterationsLeft--;

                Tuple.ObjectLongPair<IFluidProviderMK2> selected = providers[p].get(rand.nextInt(providers[p].size()));
                IFluidProviderMK2 scapegoat = selected.getKey();

                long toUse = Math.min(notAccountedFor[p], scapegoat.getFluidAvailable(type, p));
                scapegoat.useUpFluid(type, p, toUse);
                notAccountedFor[p] -= toUse;
            }
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