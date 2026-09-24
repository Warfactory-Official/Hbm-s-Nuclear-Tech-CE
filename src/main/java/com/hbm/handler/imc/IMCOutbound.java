package com.hbm.handler.imc;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockChargeBase;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class IMCOutbound {

    private static final String CARRY_ON = "carryon";

    public static void send() {

        if(Loader.isModLoaded(CARRY_ON)) {
            for(Block block : ModBlocks.ALL_BLOCKS) {
                if(block instanceof BlockChargeBase && block.getRegistryName() != null) {
                    FMLInterModComms.sendMessage(CARRY_ON, "blacklistBlock", block.getRegistryName().toString());
                }
            }
        }
    }
}
