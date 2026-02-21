package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.recipes.PressRecipes;
import com.hbm.items.machine.ItemStamp;
import com.hbm.util.I18nUtil;
import com.hbm.util.Tuple;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PressRecipeHandler implements IRecipeCategory<PressRecipeHandler.Wrapper> {
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei_press.png");

	private final IDrawable background;

	public PressRecipeHandler(IGuiHelper guiHelper) {
		this.background = guiHelper.createDrawable(GUI_TEXTURE, 5, 11, 166, 65);
	}

	@Override
	public @NotNull String getUid() {
		return JEIConfig.PRESS;
	}

	@Override
	public @NotNull String getTitle() {
		return I18nUtil.resolveKey("desc.machine_press");
	}

	@Override
	public @NotNull String getModName() {
		return Tags.MODID;
	}

	@Override
	public @NotNull IDrawable getBackground() {
		return background;
	}

	@Override
	public void setRecipe(@NotNull IRecipeLayout recipeLayout, @NotNull Wrapper wrapper, @NotNull IIngredients ingredients) {
		IGuiItemStackGroup stacks = recipeLayout.getItemStacks();

		stacks.init(0, true, 47, 41); // input
		stacks.init(1, true, 47, 5);  // stamp

		List<List<ItemStack>> outs = ingredients.getOutputs(VanillaTypes.ITEM);
		for (int i = 0; i < outs.size(); i++) {
			stacks.init(2 + i, false, 110 + (i % 3) * 18, 23 + (i / 3) * 18);
		}

		stacks.set(ingredients);
	}

	public static class Wrapper implements IRecipeWrapper {
		final List<ItemStack> inputAlts;
		final List<ItemStack> stampAlts;
		final List<ItemStack> results;

		public Wrapper(RecipesCommon.AStack input, ItemStamp.StampType stampType, ItemStack result) {
			this.inputAlts = new ArrayList<>();
			for (ItemStack s : input.extractForJEI()) this.inputAlts.add(s.copy());

			this.stampAlts = new ArrayList<>();
			List<ItemStack> stampsOfType = ItemStamp.stamps.get(stampType);
			if (stampsOfType != null) {
				for (ItemStack s : stampsOfType) this.stampAlts.add(s.copy());
			}

			this.results = new ArrayList<>(1);
			this.results.add(result.copy());
		}

		public Wrapper(RecipesCommon.AStack input, List<ItemStack> stampStacks, ItemStack result) {
			this.inputAlts = new ArrayList<>();
			for (ItemStack s : input.extractForJEI()) this.inputAlts.add(s.copy());

			this.stampAlts = new ArrayList<>(stampStacks.size());
			for (ItemStack s : stampStacks) this.stampAlts.add(s.copy());

			this.results = new ArrayList<>(1);
			this.results.add(result.copy());
		}

		@Override
		public void getIngredients(IIngredients ingredients) {
			List<List<ItemStack>> ins = new ArrayList<>(2);
			ins.add(inputAlts);
			ins.add(stampAlts);
			ingredients.setInputLists(VanillaTypes.ITEM, ins);
			ingredients.setOutputs(VanillaTypes.ITEM, results);
		}

		@Override
		public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
			GlStateManager.color(1.0F, 1.0F, 1.0F);
			minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
			Gui.drawModalRectWithCustomSizedTexture(47, 24, 0, 86, 18, 18, 256, 256);
		}
	}

	public static List<Wrapper> getRecipes() {
		List<Wrapper> list = new ArrayList<>();
		HashMap<Tuple.Pair<RecipesCommon.AStack, ItemStamp.StampType>, ItemStack> map = PressRecipes.recipes;
		for (Map.Entry<Tuple.Pair<RecipesCommon.AStack, ItemStamp.StampType>, ItemStack> e : map.entrySet()) {
			list.add(new Wrapper(e.getKey().getKey(), e.getKey().getValue(), e.getValue()));
		}
		return list;
	}
}
