package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.recipes.ShredderRecipes;
import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShredderRecipeHandler implements IRecipeCategory<ShredderRecipeHandler.ShredderRecipeWrapper> {
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei_shredder.png");

	private final IDrawable background;
	private final IDrawableAnimated powerBar;
	private final IDrawableAnimated progressBar;

	public ShredderRecipeHandler(IGuiHelper helper) {
		this.background = helper.createDrawable(GUI_TEXTURE, 5, 11, 166, 65);

		IDrawableStatic powerDrawable = helper.createDrawable(GUI_TEXTURE, 36, 86, 16, 52);
		this.powerBar = helper.createAnimatedDrawable(powerDrawable, 480, IDrawableAnimated.StartDirection.TOP, true);

		IDrawableStatic progressDrawable = helper.createDrawable(GUI_TEXTURE, 100, 118, 24, 16);
		this.progressBar = helper.createAnimatedDrawable(progressDrawable, 48, IDrawableAnimated.StartDirection.LEFT, false);
	}

	@Override
	public String getUid() {
		return JEIConfig.SHREDDER;
	}

	@Override
	public String getTitle() {
		return I18nUtil.resolveKey("tile.machine_shredder.name");
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
	public void setRecipe(IRecipeLayout recipeLayout, ShredderRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup stacks = recipeLayout.getItemStacks();

		stacks.init(0, true, 38, 23);
		stacks.init(1, false, 128, 23);

		stacks.set(ingredients);

		int bladeTopIndex = 2;
		int bladeBottomIndex = 3;

		stacks.init(bladeTopIndex, true, 83, 5);
		stacks.init(bladeBottomIndex, true, 83, 41);

		List<ItemStack> blades = recipeWrapper.getFuels();
		if (!blades.isEmpty()) {
			stacks.set(bladeTopIndex, blades);
			stacks.set(bladeBottomIndex, blades);
		}
	}

	@Override
	public void drawExtras(Minecraft minecraft) {
		powerBar.draw(minecraft, 3, 6);     // 83 - (18 * 4) - 9 + 1, 6
		progressBar.draw(minecraft, 80, 23); // 83 - 3, 5 + 18
	}

	public static List<ShredderRecipeWrapper> getRecipes() {
		List<ShredderRecipeWrapper> list = new ArrayList<>();

		List<ItemStack> blades = new ArrayList<>();
		for (ItemStack blade : JeiRecipes.getBlades()) {
			if (blade != null && !blade.isEmpty()) {
				blades.add(blade.copy());
			}
		}

		Map<Object, Object> recipes = ShredderRecipes.getShredderRecipes();
		for (Map.Entry<Object, Object> e : recipes.entrySet()) {
			Object key = e.getKey();
			Object val = e.getValue();

			if (!(key instanceof RecipesCommon.ComparableStack) || !(val instanceof ItemStack)) continue;

			ItemStack input = ((RecipesCommon.ComparableStack) key).toStack();
			ItemStack output = ((ItemStack) val).copy();

			if (input == null || input.isEmpty() || output.isEmpty()) continue;

			list.add(new ShredderRecipeWrapper(input, output, blades));
		}

		return list;
	}

	public static class ShredderRecipeWrapper implements IRecipeWrapper {
		private final ItemStack input;
		private final ItemStack output;
		private final List<ItemStack> fuels;

		public ShredderRecipeWrapper(ItemStack input, ItemStack output, List<ItemStack> fuels) {
			this.input = input.copy();
			this.input.setCount(1);
			this.output = output.copy();
			this.fuels = new ArrayList<>();
			for (ItemStack f : fuels) {
				if (f != null && !f.isEmpty()) {
					this.fuels.add(f.copy());
				}
			}
		}

		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInput(VanillaTypes.ITEM, input);
			ingredients.setOutput(VanillaTypes.ITEM, output);
		}

		public List<ItemStack> getFuels() {
			return fuels;
		}

		@Override
		public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		}
	}
}
