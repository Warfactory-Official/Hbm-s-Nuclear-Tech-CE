package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.handler.jei.JeiRecipes.HadronRecipe;
import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class HadronRecipeHandler implements IRecipeCategory<HadronRecipe> {

public static final ResourceLocation gui_rl = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei_hadron.png");
	
	protected final IDrawable background;
	protected final IDrawableStatic progressStatic;
	protected final IDrawableAnimated progressAnimated;
	public static IDrawableStatic analysis;
	
	public HadronRecipeHandler(IGuiHelper help) {
		background = help.createDrawable(gui_rl, 16, 17, 135, 52);
		
		progressStatic = help.createDrawable(gui_rl, 18, 86, 22, 16);
		progressAnimated = help.createAnimatedDrawable(progressStatic, 48, StartDirection.LEFT, false);
		
		analysis = help.createDrawable(gui_rl, 0, 86, 18, 18);
	}
	
	@Override
	public String getUid() {
		return JEIConfig.HADRON;
	}

	@Override
	public String getTitle() {
		return I18nUtil.resolveKey("container.hadron");
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
		progressAnimated.draw(minecraft, 43, 18);
	}
	
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, HadronRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 0, 17);
		guiItemStacks.init(1, true, 18, 17);
		
		guiItemStacks.init(2, false, 72, 17);
		guiItemStacks.init(3, false, 90, 17);
		
		guiItemStacks.set(ingredients);
	}

}
