package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.machine.ItemRTGPellet;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class RTGRecipeHandler extends JEIUniversalHandler {

    public RTGRecipeHandler(IGuiHelper helper) {
        super(helper, JEIConfig.RTG, "jei.rtg",
                new ItemStack[]{new ItemStack(ModBlocks.machine_rtg_grey), new ItemStack(ModBlocks.machine_difurnace_rtg_off)}, wrap(ItemRTGPellet.getRecipeMap()));
    }

    public static HashMap<Object, Object> wrap(HashMap<ItemStack, ItemStack> map) {
        return new HashMap<>(map);
    }
}
