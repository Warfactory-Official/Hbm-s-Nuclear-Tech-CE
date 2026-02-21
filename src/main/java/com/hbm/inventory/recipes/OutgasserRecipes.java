package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.util.Tuple;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static com.hbm.inventory.OreDictManager.*;

public class OutgasserRecipes extends SerializableRecipe {

	public static Map<RecipesCommon.AStack, OutgasserRecipe> recipes = new HashMap();

	@Override
	public void registerDefaults() {

		/* lithium to tritium */
		recipes.put(new RecipesCommon.OreDictStack(LI.block()),		new OutgasserRecipe(null, new FluidStack(Fluids.TRITIUM, 10_000)));
		recipes.put(new RecipesCommon.OreDictStack(LI.ingot()),		new OutgasserRecipe(null, new FluidStack(Fluids.TRITIUM, 1_000)));
		recipes.put(new RecipesCommon.OreDictStack(LI.dust()),		new OutgasserRecipe(null, new FluidStack(Fluids.TRITIUM, 1_000)));
		recipes.put(new RecipesCommon.OreDictStack(LI.dustTiny()),	new OutgasserRecipe(null, new FluidStack(Fluids.TRITIUM, 100)));

		/* gold to gold-198 */
		recipes.put(new RecipesCommon.OreDictStack(GOLD.ingot()),		new OutgasserRecipe(new ItemStack(ModItems.ingot_au198), null));
		recipes.put(new RecipesCommon.OreDictStack(GOLD.nugget()),	new OutgasserRecipe(new ItemStack(ModItems.nugget_au198), null));
		recipes.put(new RecipesCommon.OreDictStack(GOLD.dust()),		new OutgasserRecipe(new ItemStack(ModItems.powder_au198), null));

		/* thorium to thorium fuel */
		recipes.put(new RecipesCommon.OreDictStack(TH232.ingot()),	new OutgasserRecipe(new ItemStack(ModItems.ingot_thorium_fuel), null));
		recipes.put(new RecipesCommon.OreDictStack(TH232.nugget()),	new OutgasserRecipe(new ItemStack(ModItems.nugget_thorium_fuel), null));
		recipes.put(new RecipesCommon.OreDictStack(TH232.billet()),	new OutgasserRecipe(new ItemStack(ModItems.billet_thorium_fuel), null));

		/* mushrooms to glowing mushrooms */
		recipes.put(new ComparableStack(Blocks.BROWN_MUSHROOM),	new OutgasserRecipe(new ItemStack(ModBlocks.mush), null));
		recipes.put(new ComparableStack(Blocks.RED_MUSHROOM),	new OutgasserRecipe(new ItemStack(ModBlocks.mush), null));
		recipes.put(new ComparableStack(Items.MUSHROOM_STEW),	new OutgasserRecipe(new ItemStack(ModItems.glowing_stew), null));

		recipes.put(new RecipesCommon.OreDictStack(COAL.gem()),		new OutgasserRecipe(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.COAL, 1), new FluidStack(Fluids.SYNGAS, 50)));
		recipes.put(new RecipesCommon.OreDictStack(COAL.dust()),		new OutgasserRecipe(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.COAL, 1), new FluidStack(Fluids.SYNGAS, 50)));
		recipes.put(new RecipesCommon.OreDictStack(COAL.block()),		new OutgasserRecipe(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.COAL, 9), new FluidStack(Fluids.SYNGAS, 500)));

		recipes.put(new ComparableStack(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.COAL)),	new OutgasserRecipe(null, new FluidStack(Fluids.COALOIL, 100)));
		recipes.put(new ComparableStack(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.WAX)),	new OutgasserRecipe(null, new FluidStack(Fluids.RADIOSOLVENT, 100)));
	}

	public static OutgasserRecipe getRecipe(ItemStack input) {

		ComparableStack comp = new ComparableStack(input).makeSingular();

		if(recipes.containsKey(comp)) {
			return recipes.get(comp);
		}

		String[] dictKeys = comp.getDictKeys();

		for(String key : dictKeys) {
			RecipesCommon.OreDictStack dict = new RecipesCommon.OreDictStack(key);
			if(recipes.containsKey(dict)) {
				return recipes.get(dict);
			}
		}

		return null;
	}

	public static Tuple.Pair<ItemStack, FluidStack> getOutput(ItemStack input) {
		OutgasserRecipe recipe = getRecipe(input);
		if(recipe == null) return null;
		return new Tuple.Pair<>(recipe.solidOutput, recipe.fluidOutput);
	}

	public static HashMap<Object, Object[]> getRecipes() {

		HashMap<Object, Object[]> recipes = new HashMap<>();

		for(Entry<RecipesCommon.AStack, OutgasserRecipe> entry : OutgasserRecipes.recipes.entrySet()) {

			RecipesCommon.AStack input = entry.getKey();
			ItemStack solidOutput = entry.getValue().solidOutput;
			FluidStack fluidOutput = entry.getValue().fluidOutput;

			if(solidOutput != null && fluidOutput != null) recipes.put(input, new Object[] {solidOutput, ItemFluidIcon.make(fluidOutput)});
			if(solidOutput != null && fluidOutput == null) recipes.put(input, new Object[] {solidOutput});
			if(solidOutput == null && fluidOutput != null) recipes.put(input, new Object[] {ItemFluidIcon.make(fluidOutput)});
		}

		return recipes;
	}

	@Override
	public String getFileName() {
		return "hbmIrradiation.json";
	}

	@Override
	public Object getRecipeObject() {
		return recipes;
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject obj = (JsonObject) recipe;

		RecipesCommon.AStack input = readAStack(obj.get("input").getAsJsonArray());
		ItemStack solidOutput = null;
		FluidStack fluidOutput = null;

		if(obj.has("solidOutput")) {
			solidOutput = readItemStack(obj.get("solidOutput").getAsJsonArray());
		}

		if(obj.has("fluidOutput")) {
			fluidOutput = readFluidStack(obj.get("fluidOutput").getAsJsonArray());
		}

		OutgasserRecipe outgasserRecipe = new OutgasserRecipe(solidOutput, fluidOutput);
		if(obj.has("fusionOnly") && obj.get("fusionOnly").getAsBoolean()) {
			outgasserRecipe.fusionOnly();
		}

		if(solidOutput != null || fluidOutput != null) {
			recipes.put(input, outgasserRecipe);
		}
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		Entry<RecipesCommon.AStack, OutgasserRecipe> rec = (Entry<RecipesCommon.AStack, OutgasserRecipe>) recipe;

		writer.name("input");
		writeAStack(rec.getKey(), writer);

		if(rec.getValue().solidOutput != null) {
			writer.name("solidOutput");
			writeItemStack(rec.getValue().solidOutput, writer);
		}

		if(rec.getValue().fluidOutput != null) {
			writer.name("fluidOutput");
			writeFluidStack(rec.getValue().fluidOutput, writer);
		}

		writer.name("fusionOnly").value(rec.getValue().fusionOnly);
	}

	@Override
	public void deleteRecipes() {
		recipes.clear();
	}

	public static class OutgasserRecipe {
		public final ItemStack solidOutput;
		public final FluidStack fluidOutput;
		public boolean fusionOnly = false;

		public OutgasserRecipe(ItemStack solidOutput, FluidStack fluidOutput) {
			this.solidOutput = solidOutput;
			this.fluidOutput = fluidOutput;
		}

		public OutgasserRecipe fusionOnly() {
			this.fusionOnly = true;
			return this;
		}
	}
}
