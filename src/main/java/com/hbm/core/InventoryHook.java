package com.hbm.core;

import com.hbm.config.GeneralConfig;
import com.hbm.events.InventoryChangedEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import static com.hbm.core.HbmCorePlugin.coreLogger;

@SuppressWarnings("unused")
public class InventoryHook {

    public static void onClientSlotChange(InventoryPlayer inventory, int slotIndex, ItemStack newStack) {
        EntityPlayer player = inventory.player;
        if (player == null || !player.world.isRemote) return;
        ItemStack oldStack = inventory.getStackInSlot(slotIndex);
        if (ItemStack.areItemStacksEqual(oldStack, newStack)) return;
        if (GeneralConfig.enableExtendedLogging) coreLogger.debug("Client slot change detected with slotIndex {}", slotIndex);
        MinecraftForge.EVENT_BUS.post(new InventoryChangedEvent(player, slotIndex, oldStack, newStack, false));
    }

    public static void onContainerChange(Container container, int slotIndex, ItemStack oldStack, ItemStack newStack) {
        if (!(container instanceof ContainerPlayer containerPlayer)) return;
        for (IContainerListener listener : container.listeners) {
            if (listener instanceof EntityPlayerMP player) {
                Slot slot = container.getSlot(slotIndex);
                if (slot.inventory == player.inventory) {
                    if (GeneralConfig.enableExtendedLogging) {
                        coreLogger.debug("Container change detected for player {} in slot {}. Old: {}, New: {}", player.getName(), slotIndex, oldStack, newStack);
                    }
                    MinecraftForge.EVENT_BUS.post(new InventoryChangedEvent(player, slotIndex, oldStack, newStack, true));
                }
            }
        }
    }

//    public static void onServerFullSync(EntityPlayer player) {
//        if (player == null || player.world.isRemote) return;
//        if (GeneralConfig.enableExtendedLogging) coreLogger.debug("Server full sync detected");
//        MinecraftForge.EVENT_BUS.post(new InventoryChangedEvent(player, true));
//    }
}