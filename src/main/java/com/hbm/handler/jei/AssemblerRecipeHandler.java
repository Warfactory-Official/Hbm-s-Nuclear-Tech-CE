package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.handler.jei.JeiRecipes.AssemblerRecipeWrapper;
import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class AssemblerRecipeHandler implements IRecipeCategory<AssemblerRecipeWrapper> {

	public static final ResourceLocation gui_rl = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei_assembler.png");
	
	public static final int[] inputSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
	public static final int outputSlot = 13;
	public static final int templateSlot = 12;
	
	protected final IDrawable background;
	protected final IDrawableStatic staticArrow;
	protected final IDrawableAnimated animatedArrow;
	protected final IDrawableStatic staticPower;
	protected final IDrawableAnimated animatedPower;
	
	public AssemblerRecipeHandler(IGuiHelper help) {
		background = help.createDrawable(gui_rl, 6, 15, 156, 56);
		staticArrow = help.createDrawable(gui_rl, 16, 86, 36, 18);
		animatedArrow = help.createAnimatedDrawable(staticArrow, 48, StartDirection.LEFT, false);
		staticPower = help.createDrawable(gui_rl, 0, 86, 16, 52);
		animatedPower = help.createAnimatedDrawable(staticPower, 480, StartDirection.TOP, true);
	}
	
	@Override
	public String getUid() {
		return JEIConfig.ASSEMBLY;
	}

	@Override
	public String getTitle() {
		return I18nUtil.resolveKey("tile.machine_assembler.name");
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
	public IDrawable getIcon() {
		return IRecipeCategory.super.getIcon();
	}
	
	@Override
	public void drawExtras(Minecraft minecraft) {
		animatedArrow.draw(minecraft, 100, 19);
		animatedPower.draw(minecraft, 2, 2);
	}
	
	
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, AssemblerRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		guiItemStacks.init(inputSlots[0], true, 28, 1);
		guiItemStacks.init(inputSlots[1], true, 46, 1);
		guiItemStacks.init(inputSlots[2], true, 64, 1);
		guiItemStacks.init(inputSlots[3], true, 82, 1);
		guiItemStacks.init(inputSlots[4], true, 28, 19);
		guiItemStacks.init(inputSlots[5], true, 46, 19);
		guiItemStacks.init(inputSlots[6], true, 64, 19);
		guiItemStacks.init(inputSlots[7], true, 82, 19);
		guiItemStacks.init(inputSlots[8], true, 28, 37);
		guiItemStacks.init(inputSlots[9], true, 46, 37);
		guiItemStacks.init(inputSlots[10], true, 64, 37);
		guiItemStacks.init(inputSlots[11], true, 82, 37);
		
		guiItemStacks.init(templateSlot, true, 109, 1);

		guiItemStacks.init(outputSlot, false, 136, 19);
		
		guiItemStacks.set(ingredients);
	}

}
