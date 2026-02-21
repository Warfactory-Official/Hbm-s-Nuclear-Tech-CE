package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.ArcWelderRecipes;
import com.hbm.inventory.recipes.FluidBreederRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class FusionBreederRecipeHandler extends JEIUniversalHandler {
    public FusionBreederRecipeHandler(IGuiHelper helper) {
        super(helper, JEIConfig.FUSION_BREEDER, ModBlocks.fusion_breeder.getTranslationKey(), new ItemStack[] {new ItemStack(ModBlocks.fusion_breeder)}, FluidBreederRecipes.getRecipes());
    }
}
