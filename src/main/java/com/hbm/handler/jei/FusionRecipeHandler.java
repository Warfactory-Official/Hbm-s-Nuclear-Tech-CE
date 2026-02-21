package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.jei.JeiRecipes.FusionRecipe;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.FusionRecipes;
import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class FusionRecipeHandler extends JEIGenericRecipeHandler {

    public FusionRecipeHandler(IGuiHelper helper) {
        super(helper, JEIConfig.FUSION_BYPRODUCT, ModBlocks.fusion_torus.getTranslationKey(), FusionRecipes.INSTANCE, new ItemStack(ModBlocks.fusion_torus));
    }

}
