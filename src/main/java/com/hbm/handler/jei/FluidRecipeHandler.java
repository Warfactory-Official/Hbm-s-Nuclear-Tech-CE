package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.handler.jei.JeiRecipes.FluidRecipe;
import com.hbm.handler.jei.JeiRecipes.FluidRecipeInverse;
import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.util.ResourceLocation;

public class FluidRecipeHandler implements IRecipeCategory<FluidRecipe> {

	public static final ResourceLocation gui_rl = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei_fluid.png");
	
	protected final IDrawable background;
	
	public FluidRecipeHandler(IGuiHelper help) {
		background = help.createDrawable(gui_rl, 38, 29, 99, 27);
	}
	
	@Override
	public String getUid() {
		return JEIConfig.FLUIDS;
	}

	@Override
	public String getTitle() {
		return I18nUtil.resolveKey("jei.fluids");
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
	public void setRecipe(IRecipeLayout recipeLayout, FluidRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		boolean inverse = recipeWrapper instanceof FluidRecipeInverse;
		
		guiItemStacks.init(0, inverse, 5, 5);
		guiItemStacks.init(1, !inverse, 78, 6);
		guiItemStacks.set(ingredients);
	}

}
