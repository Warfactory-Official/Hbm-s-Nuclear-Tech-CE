package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.NTMAnvil;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AnvilRecipeHandler implements IRecipeCategory<AnvilRecipeHandler.AnvilRecipe> {
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei_anvil.png");

	private final IDrawable background;
	private final IDrawable slotDrawable;
	private final String title;

	public AnvilRecipeHandler(IGuiHelper guiHelper) {
		this.background = guiHelper.createDrawable(GUI_TEXTURE, 5, 11, 166, 65);
		this.slotDrawable = guiHelper.getSlotDrawable();
		this.title = "Anvil";
	}

	@NotNull
	@Override
	public String getUid() {
		return JEIConfig.ANVIL_CON;
	}

	@NotNull
	@Override
	public String getTitle() {
		return this.title;
	}

	@NotNull
	@Override
	public String getModName() {
		return Tags.MODID;
	}

	@NotNull
	@Override
	public IDrawable getBackground() {
		return this.background;
	}

	@Override
	public void setRecipe(@NotNull IRecipeLayout recipeLayout, @NotNull AnvilRecipe recipeWrapper, @NotNull IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		List<List<ItemStack>> inputs = recipeWrapper.getInputs();
		List<ItemStack> outputs = recipeWrapper.getOutputs();
		List<ItemStack> anvils = recipeWrapper.getAnvils();

		int inCount = inputs.size();
		int outCount = outputs.size();

		int inLine = 1;
		int outLine = 1;
		int inOX = 0;
		int inOY = 0;
		int outOX = 0;
		int outOY = 0;
		int anvX = 0;
		int anvY = 31;

		AnvilRecipes.OverlayType overlay = recipeWrapper.getOverlay();

		switch (overlay) {
			case SMITHING -> {
				inOX = 48;
				inOY = 24;
				outOX = 102;
				outOY = 24;
				anvX = 75;
			}
			case RECYCLING -> {
				outLine = 6;
				inOX = 12;
				inOY = 24;
				outOX = 48;
				outOY = 6;
				anvX = 30;
			}
			case CONSTRUCTION -> {
				inLine = 6;
				inOX = 12;
				inOY = 6;
				outOX = 138;
				outOY = 24;
				anvX = 120;
			}
			case NONE -> {}
			default -> {
				inLine = 4;
				outLine = 4;
				inOX = 3;
				inOY = 6;
				outOX = 93;
				outOY = 6;
				anvX = 75;
			}
		}

		// Initialize input slots
		int slotIndex = 0;
		for (int i = 0; i < inCount; i++) {
			int x = inOX + 18 * (i % inLine);
			int y = inOY + 18 * (i / inLine);
			guiItemStacks.init(slotIndex, true, x - 1, y - 1);
			guiItemStacks.setBackground(slotIndex, slotDrawable);
			slotIndex++;
		}

		// Anvil slot (as an extra input)
		int anvilSlotIndex = slotIndex;
		guiItemStacks.init(anvilSlotIndex, true, anvX - 1, anvY - 1);
		slotIndex++;

		// Output slots
		int outputsStartIndex = slotIndex;
		for (int i = 0; i < outCount; i++) {
			int x = outOX + 18 * (i % outLine);
			int y = outOY + 18 * (i / outLine);
			guiItemStacks.init(outputsStartIndex + i, false, x - 1, y - 1);
			guiItemStacks.setBackground(outputsStartIndex + i, slotDrawable);
		}

		// Provide ingredients to JEI in the same order as slots created
		List<List<ItemStack>> inputListsForJei = new ArrayList<>(inputs.size() + 1);
		inputListsForJei.addAll(inputs);
		inputListsForJei.add(anvils);
		ingredients.setInputLists(VanillaTypes.ITEM, inputListsForJei);
		ingredients.setOutputs(VanillaTypes.ITEM, outputs);

		guiItemStacks.set(ingredients);

		// Tooltip for output chances
		List<Double> chances = recipeWrapper.getOutputChances();
		guiItemStacks.addTooltipCallback((slot, input, stack, tooltip) -> {
			if (!input) {
				int idx = slot - outputsStartIndex;
				if (idx >= 0 && idx < chances.size()) {
					double chance = chances.get(idx);
					if (chance < 1.0D) {
						tooltip.add(net.minecraft.util.text.TextFormatting.RED.toString() + ((int) (chance * 1000)) / 10D + "%");
					}
				}
			}
		});
	}

	public static class AnvilRecipe implements IRecipeWrapper {
		private final List<List<ItemStack>> inputs;
		private final List<ItemStack> outputs;
		private final List<Double> outputChances;
		private final List<ItemStack> anvils;
		private final AnvilRecipes.OverlayType overlay;

		public AnvilRecipe(List<List<ItemStack>> inputs, List<ItemStack> outputs, List<Double> outputChances, int tier, AnvilRecipes.OverlayType overlay) {
			this.inputs = inputs;
			this.outputs = outputs;
			this.outputChances = outputChances;
			this.anvils = NTMAnvil.getAnvilsFromTier(tier);
			this.overlay = overlay;
		}

		@Override
		public void getIngredients(@NotNull IIngredients ingredients) {
			List<List<ItemStack>> inputLists = new ArrayList<>(this.inputs.size() + 1);
			inputLists.addAll(this.inputs);
			inputLists.add(this.anvils);
			ingredients.setInputLists(VanillaTypes.ITEM, inputLists);
			ingredients.setOutputs(VanillaTypes.ITEM, this.outputs);
		}

		@Override
		public void drawInfo(@NotNull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
			minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
			// Big area highlight (u=5,v=87,w=72,h=54)
			// Small slot highlight (u=113,v=105,w=18,h=18)
			// Operation overlays by type: NONE(131,96), SMITHING(149,96), CONSTRUCTION(167,96), RECYCLING(185,96), all w=18,h=36
			switch (overlay) {
				case NONE -> {
					Gui.drawModalRectWithCustomSizedTexture(2, 5, 5, 87, 72, 54, 256, 256);   // in area
					Gui.drawModalRectWithCustomSizedTexture(92, 5, 5, 87, 72, 54, 256, 256);  // out area
					Gui.drawModalRectWithCustomSizedTexture(74, 14, 131, 96, 18, 36, 256, 256); // op
				}
				case SMITHING -> {
					Gui.drawModalRectWithCustomSizedTexture(47, 23, 113, 105, 18, 18, 256, 256); // in single
					Gui.drawModalRectWithCustomSizedTexture(101, 23, 113, 105, 18, 18, 256, 256); // out single
					Gui.drawModalRectWithCustomSizedTexture(74, 14, 149, 96, 18, 36, 256, 256); // op
				}
				case CONSTRUCTION -> {
					Gui.drawModalRectWithCustomSizedTexture(11, 5, 5, 87, 108, 54, 256, 256);   // in area wide
					Gui.drawModalRectWithCustomSizedTexture(137, 23, 113, 105, 18, 18, 256, 256); // out single
					Gui.drawModalRectWithCustomSizedTexture(119, 14, 167, 96, 18, 36, 256, 256); // op
				}
				case RECYCLING -> {
					Gui.drawModalRectWithCustomSizedTexture(11, 23, 113, 105, 18, 18, 256, 256); // in single
					Gui.drawModalRectWithCustomSizedTexture(47, 5, 5, 87, 108, 54, 256, 256);    // out area wide
					Gui.drawModalRectWithCustomSizedTexture(29, 14, 185, 96, 18, 36, 256, 256);  // op
				}
			}
		}

		public List<List<ItemStack>> getInputs() {
			return inputs;
		}

		public List<ItemStack> getOutputs() {
			return outputs;
		}

		public List<Double> getOutputChances() {
			return outputChances;
		}

		public List<ItemStack> getAnvils() {
			return anvils;
		}

		public AnvilRecipes.OverlayType getOverlay() {
			return overlay;
		}
	}

	public static List<AnvilRecipe> getRecipes() {
		List<AnvilRecipe> list = new ArrayList<>();
		for (AnvilRecipes.AnvilConstructionRecipe recipe : AnvilRecipes.getConstruction()) {
			List<List<ItemStack>> inputs = new ArrayList<>();
			for (RecipesCommon.AStack a : recipe.input) {
				inputs.add(a.extractForJEI());
			}
			List<ItemStack> outputs = new ArrayList<>();
			List<Double> chances = new ArrayList<>();
			for (AnvilRecipes.AnvilOutput ao : recipe.output) {
				outputs.add(ao.stack.copy());
				chances.add((double) ao.chance);
			}
			int tier = recipe.tierLower;
			list.add(new AnvilRecipe(inputs, outputs, chances, tier, recipe.getOverlay()));
		}
		return list;
	}

	public static void addAnvilCatalysts(IModRegistry reg, String cat) {
		reg.addRecipeCatalyst(new ItemStack(ModBlocks.anvil_iron), cat);
		reg.addRecipeCatalyst(new ItemStack(ModBlocks.anvil_lead), cat);
		reg.addRecipeCatalyst(new ItemStack(ModBlocks.anvil_steel), cat);
		reg.addRecipeCatalyst(new ItemStack(ModBlocks.anvil_desh), cat);
		reg.addRecipeCatalyst(new ItemStack(ModBlocks.anvil_saturnite), cat);
		reg.addRecipeCatalyst(new ItemStack(ModBlocks.anvil_ferrouranium), cat);
		reg.addRecipeCatalyst(new ItemStack(ModBlocks.anvil_bismuth_bronze), cat);
		reg.addRecipeCatalyst(new ItemStack(ModBlocks.anvil_arsenic_bronze), cat);
		reg.addRecipeCatalyst(new ItemStack(ModBlocks.anvil_schrabidate), cat);
		reg.addRecipeCatalyst(new ItemStack(ModBlocks.anvil_dnt), cat);
		reg.addRecipeCatalyst(new ItemStack(ModBlocks.anvil_osmiridium), cat);
		reg.addRecipeCatalyst(new ItemStack(ModBlocks.anvil_murky), cat);
	}
}
