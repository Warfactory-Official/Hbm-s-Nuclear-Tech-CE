package com.hbm.api.ntl;

import net.minecraft.item.ItemStack;

public interface ISlotMonitorProvider {

    SlotMonitor[] getMonitors();

    ItemStack getSlotAt(int index);

    boolean isAvailableToTerminal(int termX, int termY, int termZ);
}
