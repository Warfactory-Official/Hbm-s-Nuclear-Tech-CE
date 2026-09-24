package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemStamp;
import com.hbm.util.Tuple.Pair;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.hbm.inventory.OreDictManager.*;

public class PressRecipes extends SerializableRecipe {

	public static HashMap<Pair<AStack, ItemStamp.StampType>, ItemStack> recipes = new HashMap();

	@NotNull
	public static ItemStack getOutput(ItemStack ingredient, ItemStack stamp) {

		if(ingredient == null || stamp == null)
			return ItemStack.EMPTY;

		if(!(stamp.getItem() instanceof ItemStamp))
			return ItemStack.EMPTY;

		ItemStamp.StampType type = ((ItemStamp) stamp.getItem()).getStampType(stamp.getItem(), stamp.getItemDamage());

		for(Map.Entry<Pair<AStack, ItemStamp.StampType>, ItemStack> recipe : recipes.entrySet()) {

			if(recipe.getKey().getValue() == type && recipe.getKey().getKey().matchesRecipe(ingredient, true))
				return recipe.getValue();
		}

		return ItemStack.EMPTY;
	}

	@Override
	public void registerDefaults() {

		makeRecipe(ItemStamp.StampType.FLAT, new OreDictStack(NETHERQUARTZ.dust()),					Items.QUARTZ);
		makeRecipe(ItemStamp.StampType.FLAT, new OreDictStack(LAPIS.dust()),							new ItemStack(Items.DYE, 1, 4));
		makeRecipe(ItemStamp.StampType.FLAT, new OreDictStack(DIAMOND.dust()),						Items.DIAMOND);
		makeRecipe(ItemStamp.StampType.FLAT, new OreDictStack(EMERALD.dust()),						Items.EMERALD);
		makeRecipe(ItemStamp.StampType.FLAT, new ComparableStack(ModItems.biomass),					ModItems.biomass_compressed);
		makeRecipe(ItemStamp.StampType.FLAT, new OreDictStack(ANY_COKE.gem()),						ModItems.ingot_graphite);
		makeRecipe(ItemStamp.StampType.FLAT, new ComparableStack(ModItems.meteorite_sword_reforged),	ModItems.meteorite_sword_hardened);
		makeRecipe(ItemStamp.StampType.FLAT, new ComparableStack(Blocks.LOG, 1, 3),					ModItems.ball_resin);

		makeRecipe(ItemStamp.StampType.FLAT, new OreDictStack(COAL.dust()),							DictFrame.fromOne(ModItems.briquette, ItemEnums.EnumBriquetteType.COAL));
		makeRecipe(ItemStamp.StampType.FLAT, new OreDictStack(LIGNITE.dust()),						DictFrame.fromOne(ModItems.briquette, ItemEnums.EnumBriquetteType.LIGNITE));
		makeRecipe(ItemStamp.StampType.FLAT, new ComparableStack(ModItems.powder_sawdust),			DictFrame.fromOne(ModItems.briquette, ItemEnums.EnumBriquetteType.WOOD));

		makeRecipe(ItemStamp.StampType.PLATE, new OreDictStack(IRON.ingot()),			ModItems.plate_iron);
		makeRecipe(ItemStamp.StampType.PLATE, new OreDictStack(GOLD.ingot()),			ModItems.plate_gold);
		makeRecipe(ItemStamp.StampType.PLATE, new OreDictStack(TI.ingot()),			ModItems.plate_titanium);
		makeRecipe(ItemStamp.StampType.PLATE, new OreDictStack(AL.ingot()),			ModItems.plate_aluminium);
		makeRecipe(ItemStamp.StampType.PLATE, new OreDictStack(STEEL.ingot()),		ModItems.plate_steel);
		makeRecipe(ItemStamp.StampType.PLATE, new OreDictStack(PB.ingot()),			ModItems.plate_lead);
		makeRecipe(ItemStamp.StampType.PLATE, new OreDictStack(CU.ingot()),			ModItems.plate_copper);
		makeRecipe(ItemStamp.StampType.PLATE, new OreDictStack(SA326.ingot()),		ModItems.plate_schrabidium);
		makeRecipe(ItemStamp.StampType.PLATE, new OreDictStack(CMB.ingot()),			ModItems.plate_combine_steel);
		makeRecipe(ItemStamp.StampType.PLATE, new OreDictStack(GUNMETAL.ingot()),		ModItems.plate_gunmetal);
		makeRecipe(ItemStamp.StampType.PLATE, new OreDictStack(WEAPONSTEEL.ingot()),	ModItems.plate_weaponsteel);
		makeRecipe(ItemStamp.StampType.PLATE, new OreDictStack(BIGMT.ingot()),		ModItems.plate_saturnite);
		makeRecipe(ItemStamp.StampType.PLATE, new OreDictStack(DURA.ingot()),			ModItems.plate_dura_steel);

		makeRecipe(ItemStamp.StampType.C9, 	new OreDictStack(GUNMETAL.plate()),		DictFrame.fromOne(ModItems.casing, ItemEnums.EnumCasingType.SMALL, 4));
		makeRecipe(ItemStamp.StampType.C50, 	new OreDictStack(GUNMETAL.plate()),		DictFrame.fromOne(ModItems.casing, ItemEnums.EnumCasingType.LARGE, 2));
		makeRecipe(ItemStamp.StampType.C9, 	new OreDictStack(WEAPONSTEEL.plate()),	DictFrame.fromOne(ModItems.casing, ItemEnums.EnumCasingType.SMALL_STEEL, 4));
		makeRecipe(ItemStamp.StampType.C50, 	new OreDictStack(WEAPONSTEEL.plate()),	DictFrame.fromOne(ModItems.casing, ItemEnums.EnumCasingType.LARGE_STEEL, 2));

		for(NTMMaterial mat : Mats.orderedList) {
			if(mat.autogen.contains(MaterialShapes.WIRE) && OreDictionary.doesOreNameExist(MaterialShapes.INGOT.make(mat))) {
				makeRecipe(ItemStamp.StampType.WIRE, new OreDictStack(MaterialShapes.INGOT.make(mat)), new ItemStack(ModItems.wire_fine, 8, mat.id));
			}
		}

		makeRecipe(ItemStamp.StampType.CIRCUIT, new OreDictStack(SI.billet()),						DictFrame.fromOne(ModItems.circuit, ItemEnums.EnumCircuitType.SILICON));

		makeRecipe(ItemStamp.StampType.PRINTING1, new ComparableStack(Items.PAPER), DictFrame.fromOne(ModItems.page_of_, ItemEnums.EnumPages.PAGE1));
		makeRecipe(ItemStamp.StampType.PRINTING2, new ComparableStack(Items.PAPER), DictFrame.fromOne(ModItems.page_of_, ItemEnums.EnumPages.PAGE2));
		makeRecipe(ItemStamp.StampType.PRINTING3, new ComparableStack(Items.PAPER), DictFrame.fromOne(ModItems.page_of_, ItemEnums.EnumPages.PAGE3));
		makeRecipe(ItemStamp.StampType.PRINTING4, new ComparableStack(Items.PAPER), DictFrame.fromOne(ModItems.page_of_, ItemEnums.EnumPages.PAGE4));
		makeRecipe(ItemStamp.StampType.PRINTING5, new ComparableStack(Items.PAPER), DictFrame.fromOne(ModItems.page_of_, ItemEnums.EnumPages.PAGE5));
		makeRecipe(ItemStamp.StampType.PRINTING6, new ComparableStack(Items.PAPER), DictFrame.fromOne(ModItems.page_of_, ItemEnums.EnumPages.PAGE6));
		makeRecipe(ItemStamp.StampType.PRINTING7, new ComparableStack(Items.PAPER), DictFrame.fromOne(ModItems.page_of_, ItemEnums.EnumPages.PAGE7));
		makeRecipe(ItemStamp.StampType.PRINTING8, new ComparableStack(Items.PAPER), DictFrame.fromOne(ModItems.page_of_, ItemEnums.EnumPages.PAGE8));
	}

	public static void makeRecipe(ItemStamp.StampType type, AStack in, Item out) {
		recipes.put(new Pair<AStack, ItemStamp.StampType>(in, type),  new ItemStack(out));
	}
	public static void makeRecipe(ItemStamp.StampType type, AStack in, ItemStack out) {
		recipes.put(new Pair<AStack, ItemStamp.StampType>(in, type),  out);
	}

	@Override
	public String getFileName() {
		return "hbmPress.json";
	}

	@Override
	public Object getRecipeObject() {
		return recipes;
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject obj = (JsonObject) recipe;

		AStack input = this.readAStack(obj.get("input").getAsJsonArray());
		ItemStamp.StampType stamp = ItemStamp.StampType.valueOf(obj.get("stamp").getAsString().toUpperCase());
		ItemStack output = this.readItemStack(obj.get("output").getAsJsonArray());

		if(stamp != null) {
			makeRecipe(stamp, input, output);
		}
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		Map.Entry<Pair<AStack, ItemStamp.StampType>, ItemStack> entry = (Map.Entry<Pair<AStack, ItemStamp.StampType>, ItemStack>) recipe;

		writer.name("input");
		this.writeAStack(entry.getKey().getKey(), writer);
		writer.name("stamp").value(entry.getKey().getValue().name().toLowerCase(Locale.US));
		writer.name("output");
		this.writeItemStack(entry.getValue(), writer);
	}

	@Override
	public void deleteRecipes() {
		recipes.clear();
	}
}
