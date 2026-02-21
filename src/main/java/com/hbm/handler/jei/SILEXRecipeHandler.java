package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.handler.jei.JeiRecipes.SILEXRecipe;
import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.util.ResourceLocation;

public class SILEXRecipeHandler implements IRecipeCategory<SILEXRecipe> {

	public static final ResourceLocation gui_rl = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei_silex.png");
	
	protected final IDrawable background;
	
	private SILEXRecipe currentDrawHack;
	
	public SILEXRecipeHandler(IGuiHelper help){
		background = help.createDrawable(gui_rl, 3, 3, 170, 80);
	}
	
	@Override
	public String getUid(){
		return JEIConfig.SILEX;
	}

	@Override
	public String getTitle(){
		return I18nUtil.resolveKey("container.machineSILEX");
	}

	@Override
	public String getModName(){
		return Tags.MODID;
	}

	@Override
	public IDrawable getBackground(){
		return background;
	}
	
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, SILEXRecipe recipeWrapper, IIngredients ingredients){
		currentDrawHack = recipeWrapper;
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		guiItemStacks.init(0, true, 13, 31);
		
		int rec_size = recipeWrapper.outputs.size();
		int sep = rec_size > 6 ? 4 : rec_size > 4 ? 3 : 2;
		for(int i = 0; i < rec_size; i ++){
			if(i < sep) {
				guiItemStacks.init(i+1, false, 72, 28 + i * 18 - 9 * ((Math.min(rec_size, sep) + 1) / 2));
			} else {
				guiItemStacks.init(i+1, false, 120, 28 + (i - sep) * 18 - 9 * ((Math.min(rec_size - sep, sep) + 1)/2));
			}
		}
		
		guiItemStacks.set(ingredients);
	}
}
