package com.hbm.interfaces;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Override Container#onContainerClosed(net.minecraft.entity.player.EntityPlayer) to listen close events.
 */
public interface IContainerOpenEventListener {
    void onContainerOpened(EntityPlayer player);
}
