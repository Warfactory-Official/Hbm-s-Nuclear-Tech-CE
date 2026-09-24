package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.CustomMachineConfigJSON;
import com.hbm.config.CustomMachineConfigJSON.MachineConfiguration;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.recipes.CustomMachineRecipes;
import com.hbm.inventory.recipes.CustomMachineRecipes.CustomMachineRecipe;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.main.MainRegistry;
import com.hbm.util.ItemStackUtil;
import com.hbm.util.Tuple.Pair;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomMachineRecipeHandler implements IRecipeCategory<CustomMachineRecipeHandler.JeiCustomMachineRecipe> {

	protected static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei_custom.png");

	protected final IDrawable background;
	public final MachineConfiguration conf;
	protected final List<JeiCustomMachineRecipe> recipes = new ArrayList<>();

	public CustomMachineRecipeHandler(IGuiHelper helper, MachineConfiguration conf) {
		this.conf = conf;
		this.background = helper.createDrawable(GUI_TEXTURE, 5, 11, 166, 84);

		List<CustomMachineRecipe> list = CustomMachineRecipes.recipes.get(conf.recipeKey);
		if(list != null) for(CustomMachineRecipe recipe : list) {
			recipes.add(new JeiCustomMachineRecipe(recipe));
		}
	}

	public List<JeiCustomMachineRecipe> getRecipes() {
		return recipes;
	}

	@Override
	public String getUid() {
		return getUid(conf);
	}

	public static String getUid(MachineConfiguration conf) {
		return "hbm.custom_machine." + conf.unlocalizedName;
	}

	@Override
	public String getTitle() {
		String localized = conf.localization.get(MainRegistry.proxy.getLanguageCode());
		return localized != null ? localized : conf.localizedName;
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
	public void setRecipe(IRecipeLayout recipeLayout, JeiCustomMachineRecipe wrapper, IIngredients ingredients) {
		IGuiItemStackGroup stacks = recipeLayout.getItemStacks();

		int slot = 0;
		for(int i = 0; i < wrapper.recipe.inputFluids.length && i < 3; i++) {
			EmiCompat.initSlot(recipeLayout, slot, true, 12 + i * 18 - 1, 6 - 1, Collections.singletonList(ItemFluidIcon.make(wrapper.recipe.inputFluids[i])));
			slot++;
		}
		for(int i = 0; i < wrapper.recipe.inputItems.length && i < 6; i++) {
			int sx = i < 3 ? 12 + i * 18 : 12 + (i - 3) * 18;
			int sy = i < 3 ? 24 : 42;
			stacks.init(slot, true, sx - 1, sy - 1);
			stacks.set(slot, wrapper.recipe.inputItems[i].extractForJEI());
			slot++;
		}
		for(int i = 0; i < wrapper.recipe.outputFluids.length && i < 3; i++) {
			EmiCompat.initSlot(recipeLayout, slot, false, 102 + i * 18 - 1, 6 - 1, Collections.singletonList(ItemFluidIcon.make(wrapper.recipe.outputFluids[i])));
			slot++;
		}
		for(int i = 0; i < wrapper.recipe.outputItems.length && i < 6; i++) {
			int sx = i < 3 ? 102 + i * 18 : 102 + (i - 3) * 18;
			int sy = i < 3 ? 24 : 42;
			stacks.init(slot, false, sx - 1, sy - 1);
			stacks.set(slot, wrapper.getOutputStack(i));
			slot++;
		}

		EmiCompat.initDisplaySlot(recipeLayout, slot, false, 75 - 1, 42 - 1, Collections.singletonList(new ItemStack(ModBlocks.custom_machine, 1, 100 + CustomMachineConfigJSON.niceList.indexOf(conf))));
	}

	public class JeiCustomMachineRecipe implements IRecipeWrapper {

		protected final CustomMachineRecipe recipe;

		public JeiCustomMachineRecipe(CustomMachineRecipe recipe) {
			this.recipe = recipe;
		}

		public ItemStack getOutputStack(int i) {
			Pair<ItemStack, Float> pair = recipe.outputItems[i];
			ItemStack out = pair.getKey().copy();
			if(pair.getValue() != 1) {
				ItemStackUtil.addTooltipToStack(out, TextFormatting.RED + "" + (((int) (pair.getValue() * 1000)) / 10D) + "%");
			}
			return out;
		}

		@Override
		public void getIngredients(IIngredients ingredients) {

			List<List<ItemStack>> inputs = new ArrayList<>();
			for(FluidStack fluid : recipe.inputFluids) inputs.add(Collections.singletonList(ItemFluidIcon.make(fluid)));
			for(AStack stack : recipe.inputItems) inputs.add(stack.extractForJEI());

			List<List<ItemStack>> outputs = new ArrayList<>();
			for(FluidStack fluid : recipe.outputFluids) outputs.add(Collections.singletonList(ItemFluidIcon.make(fluid)));
			for(int i = 0; i < recipe.outputItems.length; i++) outputs.add(Collections.singletonList(getOutputStack(i)));

			ingredients.setInputLists(VanillaTypes.ITEM, inputs);
			ingredients.setOutputLists(VanillaTypes.ITEM, outputs);
		}

		@Override
		public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
			int side = 83;
			if(recipe.radiationAmount != 0) {
				String radiation = "Radiation:" + recipe.radiationAmount;
				minecraft.fontRenderer.drawString(radiation, 160 - minecraft.fontRenderer.getStringWidth(radiation), 63, 0x08FF00);
			}
			if(recipe.pollutionAmount != 0) {
				String pollution = recipe.pollutionType + ":" + recipe.pollutionAmount;
				minecraft.fontRenderer.drawString(pollution, 160 - minecraft.fontRenderer.getStringWidth(pollution), 75, 0x404040);
			}
			if(conf.fluxMode) {
				String flux = "Flux:" + recipe.flux;
				minecraft.fontRenderer.drawString(flux, side - minecraft.fontRenderer.getStringWidth(flux) / 2, 16, 0x08FF00);
			}
			if(conf.maxHeat > 0 && recipe.heat > 0) {
				String heat = "Heat:" + recipe.heat;
				minecraft.fontRenderer.drawString(heat, side - minecraft.fontRenderer.getStringWidth(heat) / 2, 8, 0xFF0000);
			}
		}
	}
}
