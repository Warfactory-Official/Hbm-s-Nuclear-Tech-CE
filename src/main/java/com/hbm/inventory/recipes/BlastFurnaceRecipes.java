package com.hbm.inventory.recipes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.imc.IMCBlastFurnace;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.Tuple;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

import static com.hbm.inventory.OreDictManager.*;

public class BlastFurnaceRecipes extends SerializableRecipe {

    public static final ArrayList<Tuple.Triplet<Object, Object, ItemStack>> blastFurnaceRecipes = new ArrayList<>();
    public static final ArrayList<RecipesCommon.ComparableStack> hiddenRecipes = new ArrayList<>();
    public static final LinkedHashMap<RecipesCommon.AStack, Integer> diFuels = new LinkedHashMap<>();

    private static void registerFuels() {
        addFuel(new RecipesCommon.OreDictStack(COAL.gem()), 200);
        addFuel(new RecipesCommon.OreDictStack(COAL.dust()), 220);
        addFuel(new RecipesCommon.OreDictStack(COAL.block()), 2000);
        addFuel(new RecipesCommon.OreDictStack(LIGNITE.gem()), 150);
        addFuel(new RecipesCommon.OreDictStack(LIGNITE.dust()), 150);
        addFuel(new RecipesCommon.OreDictStack(LIGNITE.block()), 1500);
        addFuel(new RecipesCommon.ComparableStack(ModItems.briquette), 200);
        addFuel(new RecipesCommon.OreDictStack("gemCharcoal"), 150);
        addFuel(new RecipesCommon.OreDictStack("blockCharcoal"), 1500);
        addFuel(new RecipesCommon.OreDictStack("fuelCoke"), 400);
        addFuel(new RecipesCommon.OreDictStack(ANY_COKE.gem()), 400);
        addFuel(new RecipesCommon.OreDictStack(ANY_COKE.block()), 4000);
        addFuel(new RecipesCommon.ComparableStack(Items.LAVA_BUCKET), 12800);
        addFuel(new RecipesCommon.ComparableStack(Items.BLAZE_ROD), 1000);
        addFuel(new RecipesCommon.ComparableStack(Items.BLAZE_POWDER), 300);
        addFuel(new RecipesCommon.ComparableStack(Items.COAL, 1, 1), 200);
        addFuel(new RecipesCommon.OreDictStack(INFERNAL.gem()), 300);
        addFuel(new RecipesCommon.OreDictStack(INFERNAL.block()), 3000);
        addFuel(new RecipesCommon.ComparableStack(ModItems.solid_fuel), 400);
        addFuel(new RecipesCommon.ComparableStack(ModItems.solid_fuel_presto), 800);
        addFuel(new RecipesCommon.ComparableStack(ModItems.solid_fuel_presto_triplet), 2400);
    }

    public static void addFuel(RecipesCommon.AStack fuel, int power) {
        diFuels.put(fuel, power);
    }

    public static void removeFuel(RecipesCommon.AStack fuel) {
        diFuels.remove(fuel);
    }

    public static int getItemPower(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return 0;
        ItemStack item = stack.copy();
        item.setCount(1);
        int power;
        if (item.hasTagCompound()) {
            power = toInt(diFuels.get(new RecipesCommon.NbtComparableStack(item)));
        } else {
            power = toInt(diFuels.get(new RecipesCommon.ComparableStack(item)));
        }
        if (power > 0) {
            return power;
        }
        for (int id : OreDictionary.getOreIDs(new ItemStack(item.getItem(), 1, item.getItemDamage()))) {
            power = toInt(diFuels.get(new RecipesCommon.OreDictStack(OreDictionary.getOreName(id))));
            if (power > 0) {
                return power;
            }
        }
        return 0;
    }

    private static int toInt(Integer i) {
        if (i == null) return 0;
        return i;
    }

    public static List<ItemStack> getAlloyFuels() {
        ArrayList<ItemStack> fuels = new ArrayList<>();
        for (RecipesCommon.AStack entry : diFuels.keySet()) {
            fuels.addAll(entry.getStackList());
        }
        return fuels;
    }

    public static void addRecipe(Object in1, Object in2, ItemStack out) {

        if (in1 instanceof Item) in1 = new RecipesCommon.ComparableStack((Item) in1);
        if (in1 instanceof Block) in1 = new RecipesCommon.ComparableStack((Block) in1);
        if (in2 instanceof Item) in2 = new RecipesCommon.ComparableStack((Item) in2);
        if (in2 instanceof Block) in2 = new RecipesCommon.ComparableStack((Block) in2);

        blastFurnaceRecipes.add(new Tuple.Triplet<>(in1, in2, out));
    }

    @NotNull
    public static ItemStack getOutput(ItemStack in1, ItemStack in2) {
        for (Tuple.Triplet<Object, Object, ItemStack> recipe : blastFurnaceRecipes) {
            RecipesCommon.AStack[] recipeItem1 = getRecipeStacks(recipe.getX());
            RecipesCommon.AStack[] recipeItem2 = getRecipeStacks(recipe.getY());

            if ((doStacksMatch(recipeItem1, in1) && doStacksMatch(recipeItem2, in2)) || (doStacksMatch(recipeItem2, in1) && doStacksMatch(recipeItem1, in2))) {
                return recipe.getZ().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    public static Tuple.Triplet<Integer, Integer, ItemStack> getRequiredCounts(ItemStack in1, ItemStack in2) {
        if (in1 == null || in1.isEmpty() || in2 == null || in2.isEmpty()) return null;
        for (Tuple.Triplet<Object, Object, ItemStack> recipe : blastFurnaceRecipes) {
            RecipesCommon.AStack[] a1 = getRecipeStacks(recipe.getX());
            RecipesCommon.AStack[] a2 = getRecipeStacks(recipe.getY());

            RecipesCommon.AStack m1 = findMatching(a1, in1);
            RecipesCommon.AStack m2 = findMatching(a2, in2);
            if (m1 != null && m2 != null) {
                int c1 = requiredCountFor(m1, in1);
                int c2 = requiredCountFor(m2, in2);
                return new Tuple.Triplet<>(c1, c2, recipe.getZ().copy());
            }

            // swap inputs
            m1 = findMatching(a1, in2);
            m2 = findMatching(a2, in1);
            if (m1 != null && m2 != null) {
                int c1 = requiredCountFor(m2, in1);
                int c2 = requiredCountFor(m1, in2);
                return new Tuple.Triplet<>(c1, c2, recipe.getZ().copy());
            }
        }
        return null;
    }

    private static RecipesCommon.AStack findMatching(RecipesCommon.AStack[] recipe, ItemStack in) {
        if (in == null || in.isEmpty()) return null;
        for (RecipesCommon.AStack a : recipe) {
            if (a.matchesRecipe(in, true)) return a;
        }
        return null;
    }

    private static int requiredCountFor(RecipesCommon.AStack def, ItemStack in) {
        if (def == null) return 0;
        java.util.List<ItemStack> candidates = def.extractForJEI();
        if (candidates.isEmpty()) return 1;
        if (in != null && !in.isEmpty()) {
            for (ItemStack cand : candidates) {
                if (net.minecraftforge.oredict.OreDictionary.itemMatches(cand, in, false)) {
                    return Math.max(1, cand.getCount());
                }
            }
        }
        return Math.max(1, candidates.get(0).getCount());
    }

    private static boolean doStacksMatch(RecipesCommon.AStack[] recipe, ItemStack in) {
        boolean flag = false;
        byte i = 0;
        while (!flag && i < recipe.length) {
            flag = recipe[i].matchesRecipe(in, true);
            i++;
        }
        return flag;
    }

    private static RecipesCommon.AStack[] getRecipeStacks(Object in) {

        RecipesCommon.AStack[] recipeItem1 = new RecipesCommon.AStack[0];

        if (in instanceof DictFrame recipeItem) {
            recipeItem1 = new RecipesCommon.AStack[]{new RecipesCommon.OreDictStack(recipeItem.ingot()),
                    new RecipesCommon.OreDictStack(recipeItem.plate()), new RecipesCommon.OreDictStack(recipeItem.gem()),
                    new RecipesCommon.OreDictStack(recipeItem.dust())};

        } else if (in instanceof RecipesCommon.AStack) {
            recipeItem1 = new RecipesCommon.AStack[]{(RecipesCommon.AStack) in};

        } else if (in instanceof String) {
            recipeItem1 = new RecipesCommon.AStack[]{new RecipesCommon.OreDictStack((String) in)};

        }

        return recipeItem1;
    }

    public static Map<List<ItemStack>[], ItemStack> getRecepiesforJEI() {
        HashMap<List<ItemStack>[], ItemStack> recipes = new HashMap<>();

        for (Tuple.Triplet<Object, Object, ItemStack> recipe : blastFurnaceRecipes) {
            if (!hiddenRecipes.contains(new RecipesCommon.ComparableStack(recipe.getZ()))) {
                ItemStack nothing =
                        new ItemStack(ModItems.nothing).setStackDisplayName("If you're reading this, an error has occured! Check the " + "console.");
                List<ItemStack> in1 = new ArrayList<>();
                List<ItemStack> in2 = new ArrayList<>();
                in1.add(nothing);
                in2.add(nothing);

                for (RecipesCommon.AStack stack : getRecipeStacks(recipe.getX())) {
                    if (!stack.extractForJEI().isEmpty()) {
                        in1.remove(nothing);
                        in1.addAll(stack.extractForJEI());
                        break;
                    }
                }
                if (in1.contains(nothing)) {
                    MainRegistry.logger.error("Blast furnace cannot compile recipes for NEI: apparent nonexistent item #1 in recipe for item: {}",
                            recipe.getZ().getDisplayName());
                }
                for (RecipesCommon.AStack stack : getRecipeStacks(recipe.getY())) {
                    if (!stack.extractForJEI().isEmpty()) {
                        in2.remove(nothing);
                        in2.addAll(stack.extractForJEI());
                        break;
                    }
                }
                if (in2.contains(nothing)) {
                    MainRegistry.logger.error("Blast furnace cannot compile recipes for NEI: apparent nonexistent item #2 in recipe for item: {}",
                            recipe.getZ().getDisplayName());
                }

                List<ItemStack>[] inputs = new List[2];
                inputs[0] = in1;
                inputs[1] = in2;
                recipes.put(inputs, recipe.getZ());
            }
        }
        return ImmutableMap.copyOf(recipes);
    }

    public static List<Tuple.Triplet<RecipesCommon.AStack[], RecipesCommon.AStack[], ItemStack>> getRecipes() {
        List<Tuple.Triplet<RecipesCommon.AStack[], RecipesCommon.AStack[], ItemStack>> subRecipes = new ArrayList<>();
        for (Tuple.Triplet<Object, Object, ItemStack> recipe : blastFurnaceRecipes) {
            subRecipes.add(new Tuple.Triplet<>(getRecipeStacks(recipe.getX()), getRecipeStacks(recipe.getY()), recipe.getZ()));
        }
        return ImmutableList.copyOf(subRecipes);
    }

    private static void writeDictFrame(DictFrame frame, JsonWriter writer) throws IOException {
        writer.beginArray();
        writer.setIndent("");
        writer.value("dictframe");
        writer.value(frame.mats[0]);
        writer.endArray();
        writer.setIndent("  ");
    }

    private static DictFrame readDictFrame(JsonArray array) {
        return new DictFrame(array.get(1).getAsString());
    }

    @Override
    public void registerDefaults() {
        registerFuels();
        /* STEEL */
        addRecipe(IRON, COAL, new ItemStack(ModItems.ingot_steel, 1));
        addRecipe(IRON, ANY_COKE, new ItemStack(ModItems.ingot_steel, 1));
        addRecipe(IRON.ore(), COAL, new ItemStack(ModItems.ingot_steel, 2));
        addRecipe(IRON.ore(), ANY_COKE, new ItemStack(ModItems.ingot_steel, 3));
        addRecipe(IRON.ore(), new RecipesCommon.ComparableStack(ModItems.powder_flux), new ItemStack(ModItems.ingot_steel, 3));

        addRecipe(CU, REDSTONE, new ItemStack(ModItems.ingot_red_copper, 2));
        addRecipe(STEEL, MINGRADE, new ItemStack(ModItems.ingot_advanced_alloy, 2));
        addRecipe(W, COAL, new ItemStack(ModItems.neutron_reflector, 2));
        addRecipe(W, ANY_COKE, new ItemStack(ModItems.neutron_reflector, 2));
        addRecipe(new RecipesCommon.ComparableStack(ModItems.canister_full, 1, Fluids.GASOLINE.getID()), "slimeball", new ItemStack(ModItems.canister_napalm));
        addRecipe(W, SA326.nugget(), new ItemStack(ModItems.ingot_magnetized_tungsten));
        addRecipe(STEEL, TC99.nugget(), new ItemStack(ModItems.ingot_tcalloy));
        addRecipe(GOLD.plate(), ModItems.plate_mixed, new ItemStack(ModItems.plate_paa, 2));
        addRecipe(BIGMT, ModItems.ingot_meteorite, new ItemStack(ModItems.ingot_starmetal, 2));
        addRecipe(CO, ModItems.powder_meteorite, new ItemStack(ModItems.ingot_meteorite));
        addRecipe(ModItems.meteorite_sword_hardened, CO, new ItemStack(ModItems.meteorite_sword_alloyed));


        if (GeneralConfig.enableLBSM && GeneralConfig.enableLBSMSimpleChemsitry) {
            addRecipe(ModItems.canister_empty, COAL, new ItemStack(ModItems.canister_full, 1, Fluids.OIL.getID()));
        }

        if (!IMCBlastFurnace.buffer.isEmpty()) {
            blastFurnaceRecipes.addAll(IMCBlastFurnace.buffer);
            MainRegistry.logger.info("Fetched {} IMC blast furnace recipes!", IMCBlastFurnace.buffer.size());
            IMCBlastFurnace.buffer.clear();
        }

        hiddenRecipes.add(new RecipesCommon.ComparableStack(ModItems.meteorite_sword_alloyed));
    }

    @Override
    public String getFileName() {
        return "hbmBlastFurnace.json";
    }

    @Override
    public String getComment() {
        return "Inputs can use the unique 'dictframe' type which is an ore dictionary material suffix. The recipes will accept most ore dictionary "
                + "entries equivalent to one ingot (gems, dust, plates, etc).\n" + "This file also contains fuel definitions for the blast furnace.";
    }

    @Override
    public Object getRecipeObject() {
        List<Object> allEntries = new ArrayList<>();
        allEntries.addAll(blastFurnaceRecipes);
        allEntries.addAll(diFuels.entrySet());
        return allEntries;
    }

    @Override
    public void readRecipe(JsonElement recipe) {
        JsonObject rec = (JsonObject) recipe;

        if (rec.has("fuel") && rec.has("power")) {
            RecipesCommon.AStack fuelStack = readAStack(rec.get("fuel").getAsJsonArray());
            int power = rec.get("power").getAsInt();
            if (fuelStack != null) {
                addFuel(fuelStack, power);
            }
        } else {
            ItemStack output = readItemStack(rec.get("output").getAsJsonArray());

            Object input1 = null;
            Object input2 = null;

            JsonArray array1 = rec.get("input1").getAsJsonArray();
            if (array1.get(0).getAsString().equals("item")) input1 = readAStack(array1);
            if (array1.get(0).getAsString().equals("dict"))
                input1 = ((RecipesCommon.OreDictStack) readAStack(array1)).name;
            if (array1.get(0).getAsString().equals("dictframe")) input1 = readDictFrame(array1);

            JsonArray array2 = rec.get("input2").getAsJsonArray();
            if (array2.get(0).getAsString().equals("item")) input2 = readAStack(array2);
            if (array2.get(0).getAsString().equals("dict"))
                input2 = ((RecipesCommon.OreDictStack) readAStack(array2)).name;
            if (array2.get(0).getAsString().equals("dictframe")) input2 = readDictFrame(array2);

            if (input1 != null && input2 != null) {
                addRecipe(input1, input2, output);

                if (rec.has("hidden") && rec.get("hidden").getAsBoolean()) {
                    hiddenRecipes.add(new RecipesCommon.ComparableStack(output));
                }
            }
        }
    }

    @Override
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
        if (recipe instanceof Map.Entry) {
            @SuppressWarnings("unchecked") Map.Entry<RecipesCommon.AStack, Integer> fuelEntry = (Map.Entry<RecipesCommon.AStack, Integer>) recipe;
            writer.name("fuel");
            writeAStack(fuelEntry.getKey(), writer);
            writer.name("power").value(fuelEntry.getValue());
        } else if (recipe instanceof Tuple.Triplet) {
            Tuple.Triplet<Object, Object, ItemStack> rec = (Tuple.Triplet<Object, Object, ItemStack>) recipe;
            writer.name("output");
            writeItemStack(rec.getZ(), writer);

            writer.name("input1");
            if (rec.getX() instanceof RecipesCommon.ComparableStack)
                writeAStack((RecipesCommon.ComparableStack) rec.getX(), writer);
            if (rec.getX() instanceof String) writeAStack(new RecipesCommon.OreDictStack((String) rec.getX()), writer);
            if (rec.getX() instanceof DictFrame) writeDictFrame((DictFrame) rec.getX(), writer);

            writer.name("input2");
            if (rec.getY() instanceof RecipesCommon.ComparableStack)
                writeAStack((RecipesCommon.ComparableStack) rec.getY(), writer);
            if (rec.getY() instanceof String) writeAStack(new RecipesCommon.OreDictStack((String) rec.getY()), writer);
            if (rec.getY() instanceof DictFrame) writeDictFrame((DictFrame) rec.getY(), writer);

            if (hiddenRecipes.contains(new RecipesCommon.ComparableStack(rec.getZ()))) {
                writer.name("hidden").value(true);
            }
        }
    }

    @Override
    public void deleteRecipes() {
        blastFurnaceRecipes.clear();
        hiddenRecipes.clear();
        diFuels.clear();
    }
}
