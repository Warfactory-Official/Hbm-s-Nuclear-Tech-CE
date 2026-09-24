package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.BlastFurnaceRecipesNT;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class BlastFurnaceHandler extends JEIGenericRecipeHandler {

    public BlastFurnaceHandler(IGuiHelper helper) {
        super(helper, JEIConfig.BLAST_FURNACE, ModBlocks.machine_blast_furnace.getTranslationKey(), BlastFurnaceRecipesNT.INSTANCE, new ItemStack(ModBlocks.machine_blast_furnace));
    }
}
