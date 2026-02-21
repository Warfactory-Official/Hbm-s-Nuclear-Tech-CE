package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.handler.jei.JeiRecipes.AlloyFurnaceRecipe;
import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class AlloyFurnaceRecipeHandler implements IRecipeCategory<AlloyFurnaceRecipe> {

	public static final ResourceLocation gui_rl = new ResourceLocation(Tags.MODID, "textures/gui/guidifurnace.png");
	
	protected final IDrawable background;
	protected final IDrawableStatic powerStatic;
	protected final IDrawableAnimated powerAnimated;
	protected final IDrawableStatic progressStatic;
	protected final IDrawableAnimated progressAnimated;
	protected final IDrawableStatic fuelStatic;
	protected final IDrawableAnimated fuelAnimated;
	
	protected final List<ItemStack> alloyFuels;
	
	public AlloyFurnaceRecipeHandler(IGuiHelper help) {
		background = help.createDrawable(gui_rl, 6, 16, 150, 56);
		
		powerStatic = help.createDrawable(gui_rl, 176, 0, 14, 14);
		powerAnimated = help.createAnimatedDrawable(powerStatic, 48, StartDirection.TOP, true);
		
		progressStatic = help.createDrawable(gui_rl, 176, 14, 24, 17);
		progressAnimated = help.createAnimatedDrawable(progressStatic, 48, StartDirection.LEFT, false);
		
		fuelStatic = help.createDrawable(gui_rl, 201, 0, 17, 54);
		fuelAnimated = help.createAnimatedDrawable(fuelStatic, 480, StartDirection.TOP, true);
		
		alloyFuels = JeiRecipes.getAlloyFuels();
	}
	
	@Override
	public String getUid() {
		return JEIConfig.ALLOY;
	}

	@Override
	public String getTitle() {
		return I18nUtil.resolveKey("tile.machine_difurnace_off.name");
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
		powerAnimated.draw(minecraft, 56, 21);
		progressAnimated.draw(minecraft, 95, 19);
		fuelAnimated.draw(minecraft, 38, 1);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, AlloyFurnaceRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		guiItemStacks.init(0, true, 73, 1);
		guiItemStacks.init(1, true, 73, 37);
		guiItemStacks.init(2, false, 127, 19);
		guiItemStacks.init(3, true, 1, 19);
		
		guiItemStacks.set(ingredients);
		guiItemStacks.set(3, alloyFuels);
	}

}
