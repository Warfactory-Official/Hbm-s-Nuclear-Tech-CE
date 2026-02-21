package com.hbm.inventory.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.hbm.inventory.OreDictManager.*;

public class CyclotronRecipes extends SerializableRecipe {

	public static HashMap<Tuple.Pair<ComparableStack, RecipesCommon.AStack>, Tuple.Pair<ItemStack, Integer>> recipes = new HashMap<>();

	@Override
	public void registerDefaults() {

		/// LITHIUM START ///
		int liA = 50;

		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustLithium"), new ItemStack(ModItems.powder_beryllium), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustBeryllium"), new ItemStack(ModItems.powder_boron), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustBoron"), new ItemStack(ModItems.powder_coal), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustNetherQuartz"), new ItemStack(ModItems.powder_fire), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustPhosphorus"), new ItemStack(ModItems.sulfur), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustIron"), new ItemStack(ModItems.powder_cobalt), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new ComparableStack(ModItems.powder_strontium), new ItemStack(ModItems.powder_zirconium), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustGold"), new ItemStack(ModItems.ingot_mercury), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustPolonium"), new ItemStack(ModItems.powder_astatine), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustLanthanium"), new ItemStack(ModItems.powder_cerium), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustActinium"), new ItemStack(ModItems.powder_thorium), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack(U.dust()), new ItemStack(ModItems.powder_neptunium), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack(NP237.dust()), new ItemStack(ModItems.powder_plutonium), liA);
		/// LITHIUM END ///

		/// BERYLLIUM START ///
		int beA = 25;

		makeRecipe(new ComparableStack(ModItems.part_beryllium), new RecipesCommon.OreDictStack("dustLithium"), new ItemStack(ModItems.powder_boron), beA);
		makeRecipe(new ComparableStack(ModItems.part_beryllium), new RecipesCommon.OreDictStack("dustNetherQuartz"), new ItemStack(ModItems.sulfur), beA);
		makeRecipe(new ComparableStack(ModItems.part_beryllium), new RecipesCommon.OreDictStack("dustTitanium"), new ItemStack(ModItems.powder_iron), beA);
		makeRecipe(new ComparableStack(ModItems.part_beryllium), new RecipesCommon.OreDictStack("dustCobalt"), new ItemStack(ModItems.powder_copper), beA);
		makeRecipe(new ComparableStack(ModItems.part_beryllium), new ComparableStack(ModItems.powder_strontium), new ItemStack(ModItems.powder_niobium), beA);
		makeRecipe(new ComparableStack(ModItems.part_beryllium), new ComparableStack(ModItems.powder_cerium), new ItemStack(ModItems.powder_neodymium), beA);
		makeRecipe(new ComparableStack(ModItems.part_beryllium), new RecipesCommon.OreDictStack("dustThorium"), new ItemStack(ModItems.powder_uranium), beA);
		/// BERYLLIUM END ///

		/// CARBON START ///
		int caA = 10;

		makeRecipe(new ComparableStack(ModItems.part_carbon), new RecipesCommon.OreDictStack("dustBoron"), new ItemStack(ModItems.powder_aluminium), caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon), new RecipesCommon.OreDictStack("dustSulfur"), new ItemStack(ModItems.powder_titanium), caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon), new RecipesCommon.OreDictStack("dustTitanium"), new ItemStack(ModItems.powder_cobalt), caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon), new ComparableStack(ModItems.powder_caesium), new ItemStack(ModItems.powder_lanthanium), caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon), new ComparableStack(ModItems.powder_neodymium), new ItemStack(ModItems.powder_gold), caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon), new ComparableStack(ModItems.ingot_mercury), new ItemStack(ModItems.powder_polonium), caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon), new RecipesCommon.OreDictStack(PB.dust()), new ItemStack(ModItems.powder_ra226),caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon), new ComparableStack(ModItems.powder_astatine), new ItemStack(ModItems.powder_actinium), caA);
		/// CARBON END ///

		/// COPPER START ///
		int coA = 15;

		makeRecipe(new ComparableStack(ModItems.part_copper), new RecipesCommon.OreDictStack("dustBeryllium"), new ItemStack(ModItems.powder_quartz), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper), new RecipesCommon.OreDictStack("dustCoal"), new ItemStack(ModItems.powder_bromine), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper), new RecipesCommon.OreDictStack("dustTitanium"), new ItemStack(ModItems.powder_strontium), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper), new RecipesCommon.OreDictStack("dustIron"), new ItemStack(ModItems.powder_niobium), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper), new ComparableStack(ModItems.powder_bromine), new ItemStack(ModItems.powder_iodine), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper), new ComparableStack(ModItems.powder_strontium), new ItemStack(ModItems.powder_neodymium), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper), new ComparableStack(ModItems.powder_niobium), new ItemStack(ModItems.powder_caesium), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper), new ComparableStack(ModItems.powder_iodine), new ItemStack(ModItems.powder_polonium), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper), new ComparableStack(ModItems.powder_caesium), new ItemStack(ModItems.powder_actinium), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper), new RecipesCommon.OreDictStack("dustGold"), new ItemStack(ModItems.powder_uranium), coA);
		/// COPPER END ///

		/// PLUTONIUM START ///
		int plA = 100;

		makeRecipe(new ComparableStack(ModItems.part_plutonium), new RecipesCommon.OreDictStack("dustPhosphorus"), new ItemStack(ModItems.powder_tennessine), plA);
		makeRecipe(new ComparableStack(ModItems.part_plutonium), new RecipesCommon.OreDictStack(PU.dust()), new ItemStack(ModItems.powder_tennessine), plA);
		makeRecipe(new ComparableStack(ModItems.part_plutonium), new ComparableStack(ModItems.powder_tennessine), new ItemStack(ModItems.powder_australium), plA);
		makeRecipe(new ComparableStack(ModItems.part_plutonium), new ComparableStack(ModItems.pellet_charged), new ItemStack(ModItems.nugget_schrabidium), 1000);
		makeRecipe(new ComparableStack(ModItems.part_plutonium), new ComparableStack(ModItems.cell, 1, Fluids.AMAT.getID()), new ItemStack(ModItems.cell, 1, Fluids.ASCHRAB.getID()), 0);
		/// PLUTONIUM END ///
	}

	public static void makeRecipe(ComparableStack part, RecipesCommon.AStack in, ItemStack out, int amat) {
		recipes.put(new Tuple.Pair<>(part, in), new Tuple.Pair<>(out, amat));
	}

	public static Object[] getOutput(ItemStack stack, ItemStack box) {

		if(stack == null || stack.isEmpty() || box == null)
			return null;

		ComparableStack boxStack = new ComparableStack(box).makeSingular();
		ComparableStack comp = new ComparableStack(stack).makeSingular();

		//boo hoo we iterate over a hash map, cry me a river
		for(Entry<Tuple.Pair<ComparableStack, RecipesCommon.AStack>, Tuple.Pair<ItemStack, Integer>> entry : recipes.entrySet()) {

			if(entry.getKey().getKey().isApplicable(boxStack) && entry.getKey().getValue().isApplicable(comp)) {
				return new Object[] { entry.getValue().getKey().copy(), entry.getValue().getValue() };
			}
		}

		return null;
	}

	public static Map<Object[], Object> getRecipes() {

		Map<Object[], Object> map = new HashMap<>();

		for(Entry<Tuple.Pair<ComparableStack, RecipesCommon.AStack>, Tuple.Pair<ItemStack, Integer>> entry : recipes.entrySet()) {
			List<ItemStack> stack = entry.getKey().getValue().extractForJEI();

			for(ItemStack ingredient : stack) {
				map.put(new ItemStack[] { entry.getKey().getKey().toStack(), ingredient }, entry.getValue().getKey());
			}
		}

		return map;
	}

	@Override
	public String getFileName() {
		return "hbmCyclotron.json";
	}

	@Override
	public Object getRecipeObject() {
		return recipes;
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonArray particle = ((JsonObject)recipe).get("particle").getAsJsonArray();
		JsonArray input = ((JsonObject)recipe).get("input").getAsJsonArray();
		JsonArray output = ((JsonObject)recipe).get("output").getAsJsonArray();
		int antimatter = ((JsonObject)recipe).get("antimatter").getAsInt();
		ItemStack partStack = readItemStack(particle);
		RecipesCommon.AStack inStack = readAStack(input);
		ItemStack outStack = readItemStack(output);

		recipes.put(new Tuple.Pair<>(new ComparableStack(partStack), inStack),  new Tuple.Pair<>(outStack, antimatter));
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		try{
			Entry<Tuple.Pair<ComparableStack, RecipesCommon.AStack>, Tuple.Pair<ItemStack, Integer>> rec = (Entry<Tuple.Pair<ComparableStack, RecipesCommon.AStack>, Tuple.Pair<ItemStack, Integer>>) recipe;

			writer.name("particle");
			writeItemStack(rec.getKey().getKey().toStack(), writer);
			writer.name("input");
			writeAStack(rec.getKey().getValue(), writer);
			writer.name("output");
			writeItemStack(rec.getValue().getKey(), writer);
			writer.name("antimatter").value(rec.getValue().getValue());

		} catch(Exception ex) {
			MainRegistry.logger.error(ex);
			ex.printStackTrace();
		}
	}

	@Override
	public void deleteRecipes() {
		recipes.clear();
	}

	@Override
	public String getComment() {
		return "The particle item, while being an input, has to be defined as an item stack without ore dictionary support.";
	}
}
