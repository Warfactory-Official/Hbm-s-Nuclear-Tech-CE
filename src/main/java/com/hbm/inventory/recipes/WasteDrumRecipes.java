package com.hbm.inventory.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemPWRFuel;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.hbm.inventory.RecipesCommon.ComparableStack;

public class WasteDrumRecipes extends SerializableRecipe {

	public static final HashMap<ComparableStack, ItemStack> recipes = new HashMap<>();
	public static final WasteDrumRecipes instance = new WasteDrumRecipes();

    public static void addRecipe(ComparableStack key, ItemStack value) {
        recipes.put(key, value);
    }

    public static void removeRecipe(ComparableStack input, ItemStack output) {
        recipes.remove(input, output);
    }

    @Override
	public void registerDefaults() {
		addRecipe(new ComparableStack(ModItems.waste_natural_uranium, 1, 1), new ItemStack(ModItems.waste_natural_uranium));
		addRecipe(new ComparableStack(ModItems.waste_uranium, 1, 1), new ItemStack(ModItems.waste_uranium));
		addRecipe(new ComparableStack(ModItems.waste_thorium, 1, 1), new ItemStack(ModItems.waste_thorium));
		addRecipe(new ComparableStack(ModItems.waste_mox, 1, 1), new ItemStack(ModItems.waste_mox));
		addRecipe(new ComparableStack(ModItems.waste_plutonium, 1, 1), new ItemStack(ModItems.waste_plutonium));
		addRecipe(new ComparableStack(ModItems.waste_u233, 1, 1), new ItemStack(ModItems.waste_u233));
		addRecipe(new ComparableStack(ModItems.waste_u235, 1, 1), new ItemStack(ModItems.waste_u235));
		addRecipe(new ComparableStack(ModItems.waste_schrabidium, 1, 1), new ItemStack(ModItems.waste_schrabidium));
		addRecipe(new ComparableStack(ModItems.waste_zfb_mox, 1, 1), new ItemStack(ModItems.waste_zfb_mox));
		addRecipe(new ComparableStack(ModItems.waste_plate_u233, 1, 1), new ItemStack(ModItems.waste_plate_u233));
		addRecipe(new ComparableStack(ModItems.waste_plate_u235, 1, 1), new ItemStack(ModItems.waste_plate_u235));
		addRecipe(new ComparableStack(ModItems.waste_plate_mox, 1, 1), new ItemStack(ModItems.waste_plate_mox));
		addRecipe(new ComparableStack(ModItems.waste_plate_pu239, 1, 1), new ItemStack(ModItems.waste_plate_pu239));
		addRecipe(new ComparableStack(ModItems.waste_plate_sa326, 1, 1), new ItemStack(ModItems.waste_plate_sa326));
		addRecipe(new ComparableStack(ModItems.waste_plate_ra226be, 1, 1), new ItemStack(ModItems.waste_plate_ra226be));
		addRecipe(new ComparableStack(ModItems.waste_plate_pu238be, 1, 1), new ItemStack(ModItems.waste_plate_pu238be));

		for(ItemPWRFuel.EnumPWRFuel pwr : ItemPWRFuel.EnumPWRFuel.values()) addRecipe(new ComparableStack(ModItems.pwr_fuel_hot, 1, pwr.ordinal()), new ItemStack(ModItems.pwr_fuel_depleted, 1, pwr.ordinal()));
	}

	@Override
	public String getFileName() {
		return "hbmFuelpool.json";
	}

	@Override
	public Object getRecipeObject() {
		return recipes;
	}

	@Override
	public void deleteRecipes() {
		recipes.clear();
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonElement input = ((JsonObject)recipe).get("input");
		JsonElement output = ((JsonObject)recipe).get("output");
		ItemStack in = readItemStack((JsonArray) input);
		ItemStack out = readItemStack((JsonArray) output);
		addRecipe(new ComparableStack(in), out);
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		Map.Entry<ComparableStack, ItemStack> entry = (Map.Entry<ComparableStack, ItemStack>) recipe;
		ItemStack in = entry.getKey().toStack();
		ItemStack out = entry.getValue();

		writer.name("input");
		writeItemStack(in, writer);
		writer.name("output");
		writeItemStack(out, writer);
	}
}
