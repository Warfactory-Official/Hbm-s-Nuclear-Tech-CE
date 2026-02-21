package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.handler.jei.JeiRecipes.RefineryRecipe;
import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class RefineryRecipeHandler implements IRecipeCategory<RefineryRecipe> {

	public static final ResourceLocation gui_rl = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei_refinery.png");
	
	protected final IDrawable background;
	protected final IDrawableStatic progressStatic;
	protected final IDrawableAnimated progressAnimated;
	protected final IDrawableStatic powerStatic;
	protected final IDrawableAnimated powerAnimated;
	
	public RefineryRecipeHandler(IGuiHelper help) {
		background = help.createDrawable(gui_rl, 6, 15, 145, 55);
		
		powerStatic = help.createDrawable(gui_rl, 0, 86, 16, 52);
		powerAnimated = help.createAnimatedDrawable(powerStatic, 480, StartDirection.TOP, true);
		
		progressStatic = help.createDrawable(gui_rl, 16, 86, 24, 17);
		progressAnimated = help.createAnimatedDrawable(progressStatic, 48, StartDirection.LEFT, false);
	}
	
	@Override
	public String getUid() {
		return JEIConfig.REFINERY;
	}

	@Override
	public String getTitle() {
		return I18nUtil.resolveKey("tile.machine_refinery.name");
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
		progressAnimated.draw(minecraft, 77, 20);
		powerAnimated.draw(minecraft, 2, 2);
	}
	
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, RefineryRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		guiItemStacks.init(0, true, 46, 19);
		
		guiItemStacks.init(1, false, 109, 1);
		guiItemStacks.init(2, false, 127, 10);
		guiItemStacks.init(3, false, 109, 19);
		guiItemStacks.init(4, false, 127, 28);
		guiItemStacks.init(5, false, 109, 37);
		
		guiItemStacks.set(ingredients);
	}

}
