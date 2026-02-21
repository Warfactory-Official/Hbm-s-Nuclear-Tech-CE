package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.handler.jei.JeiRecipes.CMBFurnaceRecipe;
import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class CMBFurnaceRecipeHandler implements IRecipeCategory<CMBFurnaceRecipe> {

	public static final ResourceLocation gui_rl = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei_cmb.png");
	
	protected final IDrawable background;
	protected final IDrawableStatic progressStatic;
	protected final IDrawableAnimated progressAnimated;
	protected final IDrawableStatic mudStatic;
	protected final IDrawableAnimated mudAnimated;
	protected final IDrawableStatic powerStatic;
	protected final IDrawableAnimated powerAnimated;
	
	public CMBFurnaceRecipeHandler(IGuiHelper help) {
		background = help.createDrawable(gui_rl, 6, 15, 145, 55);
		
		progressStatic = help.createDrawable(gui_rl, 100, 119, 23, 16);
		progressAnimated = help.createAnimatedDrawable(progressStatic, 48, StartDirection.LEFT, false);
		
		mudStatic = help.createDrawable(gui_rl, 84, 86, 16, 52);
		mudAnimated = help.createAnimatedDrawable(mudStatic, 480, StartDirection.TOP, true);
		
		powerStatic = help.createDrawable(gui_rl, 36, 86, 16, 52);
		powerAnimated = help.createAnimatedDrawable(powerStatic, 480, StartDirection.TOP, true);
	}
	
	@Override
	public String getUid() {
		return JEIConfig.CMB;
	}

	@Override
	public String getTitle() {
		return I18nUtil.resolveKey("tile.machine_combine_factory.name");
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
	public void drawExtras(Minecraft minecraft) {
		progressAnimated.draw(minecraft, 95, 20);
		mudAnimated.draw(minecraft, 20, 2);
		powerAnimated.draw(minecraft, 2, 2);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CMBFurnaceRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		guiItemStacks.init(0, true, 64, 1);
		guiItemStacks.init(1, true, 64, 37);
		guiItemStacks.init(2, false, 127, 19);
		
		guiItemStacks.set(ingredients);
	}

}
