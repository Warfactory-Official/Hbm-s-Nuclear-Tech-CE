package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.handler.jei.JeiRecipes.RefineryRecipe;
import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class RefineryRecipeHandler implements IRecipeCategory<RefineryRecipe> {

	public static final ResourceLocation gui_rl = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei_refinery.png");
	private static final int[][] OUTPUT_POS = {{109, 1}, {127, 10}, {109, 19}, {127, 28}, {109, 37}};
	
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
		List<List<ItemStack>> inputs = ingredients.getInputs(VanillaTypes.ITEM);
		List<List<ItemStack>> outputs = ingredients.getOutputs(VanillaTypes.ITEM);

		if(!inputs.isEmpty()) EmiCompat.initSlot(recipeLayout, 0, true, 46, 19, inputs.get(0));

		for(int i = 0; i < outputs.size() && i < OUTPUT_POS.length; i++) {
			EmiCompat.initSlot(recipeLayout, i + 1, false, OUTPUT_POS[i][0], OUTPUT_POS[i][1], outputs.get(i));
		}
	}

}
