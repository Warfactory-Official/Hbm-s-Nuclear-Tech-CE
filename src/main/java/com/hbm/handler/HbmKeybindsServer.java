package com.hbm.handler;

import com.hbm.capability.HbmCapability;
import com.hbm.items.IKeybindReceiver;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class HbmKeybindsServer {

    /** Can't put this in HbmKeybinds because it's littered with clientonly stuff */
    public static void onPressedServer(EntityPlayer player, HbmKeybinds.EnumKeybind key, boolean state) {

        // EXTPROP HANDLING
        HbmCapability.IHBMData props = HbmCapability.getData(player);

        boolean wasJetpackOn = props.getEnableBackpack();
        boolean wasHudOn = props.getEnableHUD();
        boolean wasMagnetOn = props.getEnableMagnet();

        props.setKeyPressed(key, state);

        if(player instanceof EntityPlayerMP) {
            EntityPlayerMP mp = (EntityPlayerMP) player;
            if(key == HbmKeybinds.EnumKeybind.TOGGLE_JETPACK && props.getEnableBackpack() != wasJetpackOn) {
                String msg = props.getEnableBackpack()
                        ? TextFormatting.GREEN + "Jetpack ON"
                        : TextFormatting.RED + "Jetpack OFF";
                PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(msg), mp);
            }
            if(key == HbmKeybinds.EnumKeybind.TOGGLE_HEAD && props.getEnableHUD() != wasHudOn) {
                String msg = props.getEnableHUD()
                        ? TextFormatting.GREEN + "HUD ON"
                        : TextFormatting.RED + "HUD OFF";
                PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(msg), mp);
            }
            if(key == HbmKeybinds.EnumKeybind.TOGGLE_MAGNET && props.getEnableMagnet() != wasMagnetOn) {
                String msg = props.getEnableMagnet()
                        ? TextFormatting.GREEN + "Magnet ON"
                        : TextFormatting.RED + "Magnet OFF";
                PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(msg), mp);
            }
        }

        // ITEM HANDLING
        ItemStack held = player.getHeldItemMainhand();
        if(!held.isEmpty() && held.getItem() instanceof IKeybindReceiver) {
            IKeybindReceiver rec = (IKeybindReceiver) held.getItem();
            if(rec.canHandleKeybind(player, held, key)) rec.handleKeybind(player, held, key, state);
        }
    }
}

