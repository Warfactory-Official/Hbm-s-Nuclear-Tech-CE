package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.handler.jei.JeiRecipes.BookRecipe;
import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.util.ResourceLocation;

public class BookRecipeHandler implements IRecipeCategory<BookRecipe> {

	public static ResourceLocation gui_rl = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_book.png");
	
	protected final IDrawable background;
	
	public BookRecipeHandler(IGuiHelper help) {
		background = help.createDrawable(gui_rl, 28, 15, 132, 55);
	}
	
	@Override
	public String getUid() {
		return JEIConfig.BOOK;
	}

	@Override
	public String getTitle() {
		return I18nUtil.resolveKey("jei.blackbook");
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
	public void setRecipe(IRecipeLayout recipeLayout, BookRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		guiItemStacks.init(0, true, 1, 1);
		guiItemStacks.init(1, true, 37, 1);
		guiItemStacks.init(2, true, 1, 37);
		guiItemStacks.init(3, true, 37, 37);
		
		guiItemStacks.init(4, false, 95, 19);
		
		guiItemStacks.set(ingredients);
	}

}
