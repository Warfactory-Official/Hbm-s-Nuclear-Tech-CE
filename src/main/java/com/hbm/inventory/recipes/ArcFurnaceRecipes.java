package com.hbm.inventory.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemScraps;
import com.hbm.items.special.ItemBedrockOreNew;
import com.hbm.util.Tuple;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.oredict.OreDictionary;

import java.io.IOException;
import java.util.*;

import static com.hbm.inventory.OreDictManager.*;

public class ArcFurnaceRecipes extends SerializableRecipe {

    public static List<Tuple.Pair<RecipesCommon.AStack, ArcFurnaceRecipe>> recipeList = new ArrayList<>();
    /* quick lookup for translating input stacks into the output, created lazily whenever a recipe is checked for the first time */
    public static HashMap<RecipesCommon.ComparableStack, ArcFurnaceRecipe> fastCacheSolid = new HashMap<>();
    public static HashMap<RecipesCommon.ComparableStack, ArcFurnaceRecipe> fastCacheLiquid = new HashMap<>();
    /* used for the recipe creation process to cache which inputs are already in use to prevent input collisions */
    public static HashSet<RecipesCommon.ComparableStack> occupiedSolid = new HashSet<>();
    public static HashSet<RecipesCommon.ComparableStack> occupiedLiquid = new HashSet<>();

    @Override
    public void registerDefaults() {

        register(new RecipesCommon.OreDictStack(KEY_SAND),			new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon))		.fluid(new Mats.MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(1))));
        register(new RecipesCommon.ComparableStack(Items.FLINT),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon, 4))		.fluid(new Mats.MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2))));
        register(new RecipesCommon.OreDictStack(QUARTZ.gem()),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon, 3))		.fluid(new Mats.MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(3))));
        register(new RecipesCommon.OreDictStack(QUARTZ.dust()),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon, 3))		.fluid(new Mats.MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(3))));
        register(new RecipesCommon.OreDictStack(QUARTZ.block()),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon, 12))	.fluid(new Mats.MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(12))));
        register(new RecipesCommon.OreDictStack(FIBER.ingot()),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon, 4))		.fluid(new Mats.MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2))));
        register(new RecipesCommon.OreDictStack(FIBER.block()),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon, 40))	.fluid(new Mats.MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(9, 2))));
        register(new RecipesCommon.OreDictStack(ASBESTOS.ingot()),	new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon, 4))		.fluid(new Mats.MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2))));
        register(new RecipesCommon.OreDictStack(ASBESTOS.dust()),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon, 4))		.fluid(new Mats.MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2))));
        register(new RecipesCommon.OreDictStack(ASBESTOS.block()),	new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon, 40))	.fluid(new Mats.MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(9, 2))));

        register(new RecipesCommon.ComparableStack(ModBlocks.sand_quartz), new ArcFurnaceRecipe().solid(new ItemStack(ModBlocks.glass_quartz)));
        register(new RecipesCommon.OreDictStack(BORAX.dust()), new ArcFurnaceRecipe().solid(new ItemStack(ModItems.powder_boron_tiny, 3)).fluid(new Mats.MaterialStack(Mats.MAT_BORON, MaterialShapes.NUGGET.q(3))));

        for(ItemBedrockOreNew.BedrockOreType type : ItemBedrockOreNew.BedrockOreType.VALUES) {
            register(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SULFURIC_BYPRODUCT, type)), new ArcFurnaceRecipe().solid(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SULFURIC_ARC, type, 2)));
            register(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SULFURIC_ROASTED, type)), new ArcFurnaceRecipe().solid(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SULFURIC_ARC, type, 4)));
            register(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SOLVENT_BYPRODUCT, type)), new ArcFurnaceRecipe().solid(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SOLVENT_ARC, type, 2)));
            register(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SOLVENT_ROASTED, type)), new ArcFurnaceRecipe().solid(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SOLVENT_ARC, type, 4)));
            register(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.RAD_BYPRODUCT, type)), new ArcFurnaceRecipe().solid(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.RAD_ARC, type, 2)));
            register(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.RAD_ROASTED, type)), new ArcFurnaceRecipe().solid(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.RAD_ARC, type, 4)));

            register(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_FIRST, type)), new ArcFurnaceRecipe().fluidNull(ItemBedrockOreNew.toFluid(type.primary1, 5), ItemBedrockOreNew.toFluid(type.primary2, 2)));
            register(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SECOND, type)), new ArcFurnaceRecipe().fluidNull(ItemBedrockOreNew.toFluid(type.primary1, 2), ItemBedrockOreNew.toFluid(type.primary2, 5)));
            register(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.CRUMBS, type)), new ArcFurnaceRecipe().fluidNull(ItemBedrockOreNew.toFluid(type.primary1, 1), ItemBedrockOreNew.toFluid(type.primary2, 1)));

            int i3 = 3;
            register(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SULFURIC_WASHED, type)), new ArcFurnaceRecipe().fluidNull(ItemBedrockOreNew.toFluid(type.byproductAcid1, i3), ItemBedrockOreNew.toFluid(type.byproductAcid2, i3), ItemBedrockOreNew.toFluid(type.byproductAcid3, i3)));
            register(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SOLVENT_WASHED, type)), new ArcFurnaceRecipe().fluidNull(ItemBedrockOreNew.toFluid(type.byproductSolvent1, i3), ItemBedrockOreNew.toFluid(type.byproductSolvent2, i3), ItemBedrockOreNew.toFluid(type.byproductSolvent3, i3)));
            register(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.RAD_WASHED, type)), new ArcFurnaceRecipe().fluidNull(ItemBedrockOreNew.toFluid(type.byproductRad1, i3), ItemBedrockOreNew.toFluid(type.byproductRad2, i3), ItemBedrockOreNew.toFluid(type.byproductRad3, i3)));
        }

        // Autogen for simple single type items
        for(NTMMaterial material : Mats.orderedList) {
            int in = material.convIn;
            int out = material.convOut;
            NTMMaterial convert = material.smeltsInto;
            if(convert.smeltable == NTMMaterial.SmeltingBehavior.SMELTABLE) {
                for(MaterialShapes shape : MaterialShapes.allShapes) {
                    if(!shape.noAutogen) {
                        String name = shape.name() + material.names[0];
                        if(!OreDictionary.getOres(name).isEmpty()) {
                            RecipesCommon.OreDictStack dict = new RecipesCommon.OreDictStack(name);
                            ArcFurnaceRecipe recipe = new ArcFurnaceRecipe();
                            recipe.fluid(new Mats.MaterialStack(convert, shape.q(1) * out / in));
                            register(dict, recipe);
                        }
                    }
                }
            }
        }

        // Autogen for custom smeltables
        for(Map.Entry<String, List<Mats.MaterialStack>> entry : Mats.materialOreEntries.entrySet()) {
            RecipesCommon.OreDictStack dict = new RecipesCommon.OreDictStack(entry.getKey());
            addCustomSmeltable(dict, entry.getValue());
        }
        for(Map.Entry<RecipesCommon.ComparableStack, List<Mats.MaterialStack>> entry : Mats.materialEntries.entrySet()) {
            addCustomSmeltable(entry.getKey(), entry.getValue());
        }

        // Autogen for furnace recipes
        for(Map.Entry entry : FurnaceRecipes.instance().getSmeltingList().entrySet()) {
            ItemStack input = (ItemStack) entry.getKey();
            ItemStack output = (ItemStack) entry.getValue();

            if(input != null && output != null && !input.isEmpty() && !output.isEmpty()) {
                RecipesCommon.ComparableStack comp = new RecipesCommon.ComparableStack(input);
                if(OreDictManager.arcSmeltable.contains(comp) || OreDictManager.arcSmeltable.contains(new RecipesCommon.ComparableStack(output))) {
                    ArcFurnaceRecipe recipe = new ArcFurnaceRecipe();
                    recipe.solid(output.copy());
                    register(comp, recipe);
                }
            }
        }
    }

    public static void register(RecipesCommon.AStack input, ArcFurnaceRecipe output) {
        List<ItemStack> inputs = input.extractForJEI();
        for(ItemStack stack : inputs) {
            RecipesCommon.ComparableStack compStack = new RecipesCommon.ComparableStack(stack);
            if(compStack.meta == OreDictionary.WILDCARD_VALUE) compStack.meta = 0;
            if(output.solidOutput != null) if(occupiedSolid.contains(compStack)) return;
            if(output.fluidOutput != null) if(occupiedLiquid.contains(compStack)) return;
        }
        recipeList.add(new Tuple.Pair<>(input, output));
        for(ItemStack stack : inputs) {
            RecipesCommon.ComparableStack compStack = new RecipesCommon.ComparableStack(stack);
            if(compStack.meta == OreDictionary.WILDCARD_VALUE) compStack.meta = 0;
            if(output.solidOutput != null) occupiedSolid.add(compStack);
            if(output.fluidOutput != null) occupiedLiquid.add(compStack);
        }
    }

    private static void addCustomSmeltable(RecipesCommon.AStack astack, List<Mats.MaterialStack> mats) {
        List<Mats.MaterialStack> smeltables = new ArrayList<>();
        for(Mats.MaterialStack mat : mats) {
            if(mat.material.smeltable == NTMMaterial.SmeltingBehavior.SMELTABLE) {
                smeltables.add(mat);
            }
        }
        if(smeltables.isEmpty()) return;
        ArcFurnaceRecipe recipe = new ArcFurnaceRecipe();
        recipe.fluid(smeltables.toArray(new Mats.MaterialStack[0]));
        register(astack, recipe);
    }

    public static ArcFurnaceRecipe getOutput(ItemStack stack, boolean liquid) {

        if(stack == null) return null;

        if(stack.getItem() == ModItems.scraps && liquid) {
            NTMMaterial mat = Mats.matById.get(stack.getItemDamage());
            if(mat == null) return null;
            Mats.MaterialStack mats = ItemScraps.getMats(stack);
            if(mats.material.smeltable == NTMMaterial.SmeltingBehavior.SMELTABLE) {
                return new ArcFurnaceRecipe().fluid(mats);
            }
        }

        RecipesCommon.ComparableStack cacheKey = new RecipesCommon.ComparableStack(stack).makeSingular();
        if(!liquid && fastCacheSolid.containsKey(cacheKey)) return fastCacheSolid.get(cacheKey);
        if(liquid && fastCacheLiquid.containsKey(cacheKey)) return fastCacheLiquid.get(cacheKey);

        for(Tuple.Pair<RecipesCommon.AStack, ArcFurnaceRecipe> entry : recipeList) {
            if(entry.getKey().matchesRecipe(stack, true)) {
                ArcFurnaceRecipe rec = entry.getValue();
                if((liquid && rec.fluidOutput != null) || (!liquid && rec.solidOutput != null)) {
                    if(!liquid) fastCacheSolid.put(cacheKey, rec);
                    if(liquid) fastCacheLiquid.put(cacheKey, rec);
                    return rec;
                }
            }
        }

        if(!liquid) fastCacheSolid.put(cacheKey, null);
        if(liquid) fastCacheLiquid.put(cacheKey, null);

        return null;
    }

    public static HashMap<Object, Object> getSolidRecipes() {
        HashMap<Object, Object> recipes = new HashMap<>();
        for(Tuple.Pair<RecipesCommon.AStack, ArcFurnaceRecipe> recipe : ArcFurnaceRecipes.recipeList) {
            if(recipe.getValue().solidOutput != null) recipes.put(recipe.getKey().copy(), recipe.getValue().solidOutput.copy());
        }
        return recipes;
    }

    public static HashMap<Object, Object> getFluidRecipes() {
        HashMap<Object, Object> recipes = new HashMap<>();
        for(Tuple.Pair<RecipesCommon.AStack, ArcFurnaceRecipe> recipe : ArcFurnaceRecipes.recipeList) {
            if(recipe.getValue().fluidOutput != null && recipe.getValue().fluidOutput.length > 0) {
                Object[] out = new Object[recipe.getValue().fluidOutput.length];
                for(int i = 0; i < out.length; i++) out[i] = ItemScraps.create(recipe.getValue().fluidOutput[i], true);
                recipes.put(recipe.getKey().copy(), out);
            }
        }
        for(NTMMaterial mat : Mats.orderedList) {
            if(mat.smeltable == NTMMaterial.SmeltingBehavior.SMELTABLE) {
                recipes.put(new ItemStack(ModItems.scraps, 1, mat.id), ItemScraps.create(new Mats.MaterialStack(mat, MaterialShapes.INGOT.q(1)), true));
            }
        }
        return recipes;
    }

    @Override
    public String getFileName() {
        return "hbmArcFurnace.json";
    }

    @Override
    public Object getRecipeObject() {
        return recipeList;
    }

    @Override
    public void deleteRecipes() {
        occupiedSolid.clear();
        occupiedLiquid.clear();
        recipeList.clear();
        fastCacheSolid.clear();
        fastCacheLiquid.clear();
    }

    @Override
    public void readRecipe(JsonElement recipe) {
        JsonObject rec = (JsonObject) recipe;
        ArcFurnaceRecipe arc = new ArcFurnaceRecipe();

        RecipesCommon.AStack input = this.readAStack(rec.get("input").getAsJsonArray());

        if(rec.has("solid")) {
            arc.solid(this.readItemStack(rec.get("solid").getAsJsonArray()));
        }

        if(rec.has("fluid")) {
            JsonArray fluids = rec.get("fluid").getAsJsonArray();
            List<Mats.MaterialStack> mats = new ArrayList<>();
            for(JsonElement fluid : fluids) {
                JsonArray matStack = fluid.getAsJsonArray();
                Mats.MaterialStack stack = new Mats.MaterialStack(Mats.matByName.get(matStack.get(0).getAsString()), matStack.get(1).getAsInt());
                if(stack.material.smeltable == NTMMaterial.SmeltingBehavior.SMELTABLE) {
                    mats.add(stack);
                }
            }
            if(!mats.isEmpty()) {
                arc.fluid(mats.toArray(new Mats.MaterialStack[0]));
            }
        }

        register(input, arc);
    }

    @Override
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
        Tuple.Pair<RecipesCommon.AStack, ArcFurnaceRecipe> rec = (Tuple.Pair<RecipesCommon.AStack, ArcFurnaceRecipe>) recipe;

        writer.name("input");
        this.writeAStack(rec.getKey(), writer);

        if(rec.getValue().solidOutput != null) {
            writer.name("solid");
            this.writeItemStack(rec.getValue().solidOutput, writer);
        }

        if(rec.getValue().fluidOutput != null) {
            writer.name("fluid").beginArray();
            writer.setIndent("");
            for(Mats.MaterialStack stack : rec.getValue().fluidOutput) {
                writer.beginArray();
                writer.value(stack.material.names[0]).value(stack.amount);
                writer.endArray();
            }
            writer.endArray();
            writer.setIndent("  ");
        }
    }

    public static class ArcFurnaceRecipe {

        public Mats.MaterialStack[] fluidOutput;
        public ItemStack solidOutput;

        public ArcFurnaceRecipe fluid(Mats.MaterialStack... outputs) {
            this.fluidOutput = outputs;
            return this;
        }

        public ArcFurnaceRecipe fluidNull(Mats.MaterialStack... outputs) {
            List<Mats.MaterialStack> mat = new ArrayList<>();
            for(Mats.MaterialStack stack : outputs) if(stack != null) mat.add(stack);
            if(!mat.isEmpty()) this.fluidOutput = mat.toArray(new Mats.MaterialStack[0]);
            return this;
        }

        public ArcFurnaceRecipe solid(ItemStack output) {
            this.solidOutput = output;
            return this;
        }
    }
}
