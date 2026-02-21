package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.items.special.ItemBedrockOreNew;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.hbm.inventory.OreDictManager.*;
import static com.hbm.inventory.fluid.Fluids.*;
import static com.hbm.inventory.fluid.Fluids.GAS_COKER;

public class PyroOvenRecipes extends SerializableRecipe {

    public static List<PyroOvenRecipe> recipes = new ArrayList<>();

    @Override
    public void registerDefaults() {

        //solid fuel
        registerSFAuto(SMEAR);
        registerSFAuto(HEATINGOIL);
        registerSFAuto(HEATINGOIL_VACUUM);
        registerSFAuto(RECLAIMED);
        registerSFAuto(PETROIL);
        registerSFAuto(NAPHTHA);
        registerSFAuto(NAPHTHA_CRACK);
        registerSFAuto(DIESEL);
        registerSFAuto(DIESEL_REFORM);
        registerSFAuto(DIESEL_CRACK);
        registerSFAuto(DIESEL_CRACK_REFORM);
        registerSFAuto(LIGHTOIL);
        registerSFAuto(LIGHTOIL_CRACK);
        registerSFAuto(LIGHTOIL_VACUUM);
        registerSFAuto(KEROSENE);
        registerSFAuto(KEROSENE_REFORM);
        registerSFAuto(SOURGAS);
        registerSFAuto(REFORMGAS);
        registerSFAuto(SYNGAS);
        registerSFAuto(PETROLEUM);
        registerSFAuto(LPG);
        registerSFAuto(BIOFUEL);
        registerSFAuto(AROMATICS);
        registerSFAuto(UNSATURATEDS);
        registerSFAuto(REFORMATE);
        registerSFAuto(XYLENE);
        registerSFAuto(BALEFIRE, 24_000_000L, ModItems.solid_fuel_bf);

        //bedrock ores

        for(ItemBedrockOreNew.BedrockOreType type : ItemBedrockOreNew.BedrockOreType.VALUES) {
            recipes.add(new PyroOvenRecipe(10).in(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.BASE, type))).out(new FluidStack(Fluids.VITRIOL, 50)).out(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.BASE_ROASTED, type)));
            recipes.add(new PyroOvenRecipe(10).in(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY, type))).out(new FluidStack(Fluids.VITRIOL, 50)).out(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_ROASTED, type)));
            recipes.add(new PyroOvenRecipe(10).in(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SULFURIC_BYPRODUCT, type))).out(new FluidStack(Fluids.VITRIOL, 50)).out(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SULFURIC_ROASTED, type)));
            recipes.add(new PyroOvenRecipe(10).in(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SOLVENT_BYPRODUCT, type))).out(new FluidStack(Fluids.VITRIOL, 50)).out(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SOLVENT_ROASTED, type)));
            recipes.add(new PyroOvenRecipe(10).in(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.RAD_BYPRODUCT, type))).out(new FluidStack(Fluids.VITRIOL, 50)).out(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.RAD_ROASTED, type)));
        }

        //syngas from coal
        recipes.add(new PyroOvenRecipe(100)
                .in(new FluidStack(Fluids.STEAM, 500)).in(new RecipesCommon.OreDictStack(COAL.gem()))
                .out(new FluidStack(Fluids.SYNGAS, 1_000)));
        recipes.add(new PyroOvenRecipe(100)
                .in(new FluidStack(Fluids.STEAM, 500)).in(new RecipesCommon.OreDictStack(COAL.dust()))
                .out(new FluidStack(Fluids.SYNGAS, 1_000)));
        recipes.add(new PyroOvenRecipe(100)
                .in(new FluidStack(Fluids.STEAM, 250)).in(new RecipesCommon.OreDictStack(ANY_COKE.gem()))
                .out(new FluidStack(Fluids.SYNGAS, 1_000)));
        //syngas from biomass
        recipes.add(new PyroOvenRecipe(100)
                .in(new RecipesCommon.ComparableStack(ModItems.biomass, 4))
                .out(new FluidStack(Fluids.SYNGAS, 1_000)).out(new ItemStack(Items.COAL, 1, 1)));
        //soot from tar
        recipes.add(new PyroOvenRecipe(40)
                .out(new FluidStack(Fluids.HYDROGEN, 250)).in(new RecipesCommon.OreDictStack(ANY_TAR.any(), 4))
                .out(new FluidStack(Fluids.CARBONDIOXIDE, 1_000)).out(DictFrame.fromOne(ModItems.powder_ash, ItemEnums.EnumAshType.SOOT)));
        //heavyoil from coal
        recipes.add(new PyroOvenRecipe(100)
                .in(new FluidStack(Fluids.HYDROGEN, 500)).in(new RecipesCommon.OreDictStack(COAL.gem()))
                .out(new FluidStack(Fluids.HEAVYOIL, 1_000)));
        recipes.add(new PyroOvenRecipe(100)
                .in(new FluidStack(Fluids.HYDROGEN, 500)).in(new RecipesCommon.OreDictStack(COAL.dust()))
                .out(new FluidStack(Fluids.HEAVYOIL, 1_000)));
        recipes.add(new PyroOvenRecipe(100)
                .in(new FluidStack(Fluids.HYDROGEN, 250)).in(new RecipesCommon.OreDictStack(ANY_COKE.gem()))
                .out(new FluidStack(Fluids.HEAVYOIL, 1_000)));
        //coalgas from coal
        recipes.add(new PyroOvenRecipe(50)
                .in(new FluidStack(Fluids.HEAVYOIL, 500)).in(new RecipesCommon.OreDictStack(COAL.gem()))
                .out(new FluidStack(Fluids.COALGAS, 1_000)));
        recipes.add(new PyroOvenRecipe(50)
                .in(new FluidStack(Fluids.HEAVYOIL, 500)).in(new RecipesCommon.OreDictStack(COAL.dust()))
                .out(new FluidStack(Fluids.COALGAS, 1_000)));
        recipes.add(new PyroOvenRecipe(50)
                .in(new FluidStack(Fluids.HEAVYOIL, 500)).in(new RecipesCommon.OreDictStack(ANY_COKE.gem()))
                .out(new FluidStack(Fluids.COALGAS, 1_000)));
        //refgas from coker gas
        recipes.add(new PyroOvenRecipe(60)
                .in(new FluidStack(GAS_COKER, 4_000))
                .out(new FluidStack(Fluids.REFORMGAS, 100)));
        //hydrogen and carbon from natgas
        recipes.add(new PyroOvenRecipe(60)
                .in(new FluidStack(Fluids.GAS, 12_000))
                .out(new FluidStack(Fluids.HYDROGEN, 8_000)).out(new ItemStack(ModItems.ingot_graphite, 1)));
    }

    public static void registerSFAuto(FluidType fluid) {
        registerSFAuto(fluid, 1_440_000L, ModItems.solid_fuel); //3200 burntime * 1.5 burntime bonus * 300 TU/t
    }
    public static void registerSFAuto(FluidType fluid, long tuPerSF, Item fuel) {
        long tuPerBucket = fluid.getTrait(FT_Flammable.class).getHeatEnergy();
        double bonus = 0.5D; //double efficiency!!

        int mB = (int) (tuPerSF * 1000L * bonus / tuPerBucket);

        if(mB > 10_000) mB -= (mB % 1000);
        else if(mB > 1_000) mB -= (mB % 100);
        else if(mB > 100) mB -= (mB % 10);

        mB = Math.max(mB, 1);

        registerRecipe(fluid, mB, fuel);
    }

    public static void registerRecipe(FluidType type, int quantity, Item output) { registerRecipe(type, quantity, new ItemStack(output)); }
    public static void registerRecipe(FluidType type, int quantity, ItemStack output) { recipes.add(new PyroOvenRecipe(60).in(new FluidStack(type, quantity)).out(output)); }

    public static HashMap<Object[], Object[]> getRecipes() {
        HashMap<Object[], Object[]> map = new HashMap<>();

        for(PyroOvenRecipe rec : recipes) {

            Object[] in = null;
            Object[] out = null;

            if(rec.inputFluid != null && rec.inputItem != null) in = new Object[] {ItemFluidIcon.make(rec.inputFluid), rec.inputItem};
            if(rec.inputFluid != null && rec.inputItem == null) in = new Object[] {ItemFluidIcon.make(rec.inputFluid)};
            if(rec.inputFluid == null && rec.inputItem != null) in = new Object[] {rec.inputItem};

            if(rec.outputFluid != null && rec.outputItem != null) out = new Object[] {rec.outputItem, ItemFluidIcon.make(rec.outputFluid)};
            if(rec.outputFluid != null && rec.outputItem == null) out = new Object[] {ItemFluidIcon.make(rec.outputFluid)};
            if(rec.outputFluid == null && rec.outputItem != null) out = new Object[] {rec.outputItem};

            if(in != null && out != null) {
                map.put(in, out);
            }
        }

        return map;
    }

    @Override
    public String getFileName() {
        return "hbmPyrolysis.json";
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
        JsonObject obj = (JsonObject) recipe;

        RecipesCommon.AStack inputItem = obj.has("inputItem") ? readAStack(obj.get("inputItem").getAsJsonArray()) : null;
        FluidStack inputFluid = obj.has("inputFluid") ? readFluidStack(obj.get("inputFluid").getAsJsonArray()) : null;
        ItemStack outputItem = obj.has("outputItem") ? readItemStack(obj.get("outputItem").getAsJsonArray()) : null;
        FluidStack outputFluid = obj.has("outputFluid") ? readFluidStack(obj.get("outputFluid").getAsJsonArray()) : null;
        int duration = obj.get("duration").getAsInt();

        recipes.add(new PyroOvenRecipe(duration).in(inputFluid).in(inputItem).out(outputFluid).out(outputItem));
    }

    @Override
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

        PyroOvenRecipe rec = (PyroOvenRecipe) recipe;

        if(rec.inputFluid != null) { writer.name("inputFluid"); writeFluidStack(rec.inputFluid, writer); }
        if(rec.inputItem != null) { writer.name("inputItem"); writeAStack(rec.inputItem, writer); }
        if(rec.outputFluid != null) { writer.name("outputFluid"); writeFluidStack(rec.outputFluid, writer); }
        if(rec.outputItem != null) { writer.name("outputItem"); writeItemStack(rec.outputItem, writer); }
        writer.name("duration").value(rec.duration);
    }

    public static class PyroOvenRecipe {
        public FluidStack inputFluid;
        public RecipesCommon.AStack inputItem;
        public FluidStack outputFluid;
        public ItemStack outputItem;
        public int duration;

        public PyroOvenRecipe(int duration) {
            this.duration = duration;
        }

        public PyroOvenRecipe in(FluidStack stack) { this.inputFluid = stack; return this; }
        public PyroOvenRecipe in(RecipesCommon.AStack stack) { this.inputItem = stack; return this; }
        public PyroOvenRecipe out(FluidStack stack) { this.outputFluid = stack; return this; }
        public PyroOvenRecipe out(ItemStack stack) { this.outputItem = stack; return this; }
    }
}
