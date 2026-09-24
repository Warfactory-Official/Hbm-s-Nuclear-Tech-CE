package com.hbm.api.ntl;

import com.hbm.uninos.networkproviders.PneumaticNetwork;
import net.minecraft.item.ItemStack;

/**
 * Interface for storage tile entities which provides the access terminals with slot monitors,
 * and slot monitors with ways of accessing the underlying stacks
 *
 * @author hbm
 */
public interface ISlotMonitorProvider {

    /** Returns an array of available slot monitors, which should ideally mirror the available slots of that container */
    SlotMonitor[] getMonitors();

    /** Returns the slot contents of that index, so that the monitors can detect changes */
    ItemStack getSlotAt(int index);

    /** Returns the amount of that slot at that index. Some storages may use int64 datatypes so we have to account for those too somehow, since ItemStacks cannot handle that. */
    long getAmountAt(int index);

    /** Removes the given number of items from that slot, returns the amount left to remove if the stack was smaller than the supplied amount */
    long useUpItem(int index, long amount);

    /** Adds the given number of items to that slot, returns the amount that couldn't be added due to stack limits */
    default long addItem(int index, long amount) {
        return amount;
    }

    /** Sets the slot contents, returns the number of items that couldn't be added */
    default long setupType(int index, ItemStack zeroStack, long amount) {
        return amount;
    }

    /** Whether this container allows types to be set via the access terminal */
    default boolean allowTypeSetting() {
        return false;
    }

    /** Whether this storage unit is reachable by the access point */
    boolean isAvailableToCache(StackCache cache);

    /** This allows slot monitors to find the network, and by extension all cached slots */
    PneumaticNetwork getRelevantNetwork();

    /** Runs whenever a new stack cache user (i.e. an access point) joins the network in order to grab all the stack monitors */
    default void onNewCacheHasJoined(StackCache stackCache, PneumaticNetwork network) {
        for(SlotMonitor monitor : getMonitors()) {
            if(!stackCache.hasExpired && isAvailableToCache(stackCache)) {
                stackCache.addToCache(monitor);
            }
        }
    }

    default void updateMonitors() {
        for(SlotMonitor monitor : getMonitors()) monitor.checkUpdate();
    }
}
