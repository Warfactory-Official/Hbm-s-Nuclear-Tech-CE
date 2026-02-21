package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.handler.jei.JeiRecipes.TransmutationRecipe;
import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.util.ResourceLocation;

public class TransmutationRecipeHandler implements IRecipeCategory<TransmutationRecipe> {

	public static final ResourceLocation gui_rl = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei_transmutation.png");
	
	protected final IDrawable background;
	
	public TransmutationRecipeHandler(IGuiHelper help) {
		background = help.createDrawable(gui_rl, 38, 29, 99, 27);
	}
	
	@Override
	public String getUid() {
		return JEIConfig.TRANSMUTATION;
	}

	@Override
	public String getTitle() {
		return I18nUtil.resolveKey("tile.machine_schrabidium_transmutator.name");
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
	public void setRecipe(IRecipeLayout recipeLayout, TransmutationRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		guiItemStacks.init(0, true, 5, 5);
		guiItemStacks.init(1, false, 78, 6);

		guiItemStacks.set(ingredients);
	}

}
