package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.PlasmaForgeRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class PlasmaForgeRecipeHandler extends JEIGenericRecipeHandler {

    public PlasmaForgeRecipeHandler(IGuiHelper helper) {
        super(helper, JEIConfig.PLASMA_FORGE, ModBlocks.fusion_plasma_forge.getTranslationKey(), PlasmaForgeRecipes.INSTANCE, new ItemStack(ModBlocks.fusion_plasma_forge));
    }
}
