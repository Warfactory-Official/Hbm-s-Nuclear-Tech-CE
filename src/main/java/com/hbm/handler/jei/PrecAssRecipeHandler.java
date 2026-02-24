package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.PrecAssRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class PrecAssRecipeHandler extends JEIGenericRecipeHandler {

    public PrecAssRecipeHandler(IGuiHelper helper) {
        super(helper, JEIConfig.PREC_ASS, ModBlocks.machine_precass.getTranslationKey(), PrecAssRecipes.INSTANCE, new ItemStack(ModBlocks.machine_precass));
    }
}
