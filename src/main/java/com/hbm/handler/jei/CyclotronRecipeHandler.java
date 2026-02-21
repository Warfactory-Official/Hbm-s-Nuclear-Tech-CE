package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.handler.jei.JeiRecipes.CyclotronRecipe;
import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class CyclotronRecipeHandler implements IRecipeCategory<CyclotronRecipe> {

	//TODO remake this to fit new cyclotron.
	public static final ResourceLocation gui_rl = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei_cyclotron.png");
	
	protected final IDrawable background;
	protected final IDrawableStatic arrowStatic;
	protected final IDrawableAnimated arrowAnimated;
	
	public CyclotronRecipeHandler(IGuiHelper help) {
		background = help.createDrawable(gui_rl, 24, 16, 127, 53);
		arrowStatic = help.createDrawable(gui_rl, 100, 118, 24, 18);
		arrowAnimated = help.createAnimatedDrawable(arrowStatic, 48, StartDirection.LEFT, false);
	}
	
	@Override
	public String getUid() {
		return JEIConfig.CYCLOTRON;
	}

	@Override
	public String getTitle() {
		return I18nUtil.resolveKey("tile.machine_cyclotron.name");
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
		arrowAnimated.draw(minecraft, 25, 18);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CyclotronRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		guiItemStacks.init(0, true, 1, 18);
		guiItemStacks.init(1, true, 55, 18);
		guiItemStacks.init(2, false, 109, 18);
		
		guiItemStacks.set(ingredients);
	}

}
