package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.handler.jei.JeiRecipes.RBMKFuelRecipe;
import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.util.ResourceLocation;

public class RBMKFuelRecipeHandler implements IRecipeCategory<RBMKFuelRecipe> {

	public static ResourceLocation gui_rl = new ResourceLocation(Tags.MODID + ":textures/gui/jei/crafting_background.png");
	
	protected final IDrawable background;
	
	public RBMKFuelRecipeHandler(IGuiHelper help) {
		background = help.createDrawable(gui_rl, 12, 26, 116, 26);
	}
	
	@Override
	public String getUid() {
		return JEIConfig.RBMKFUEL;
	}

	@Override
	public String getTitle() {
		return I18nUtil.resolveKey("jei.rbmkfueluncrafting");
	}

	@Override
	public String getModName() {
		return Tags.MODID;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, RBMKFuelRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		guiItemStacks.init(0, true, 18, 4);
		
		guiItemStacks.init(1, false, 94, 4);
		
		guiItemStacks.set(ingredients);
	}
}
