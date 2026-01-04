package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidIcon;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.hbm.inventory.OreDictManager.*;

public class LiquefactionRecipes extends SerializableRecipe {

    private static HashMap<Object, FluidStack> recipes = new HashMap();

    @Override
    public void registerDefaults() {

        //oil processing
        recipes.put(COAL.gem(),											new FluidStack(100, Fluids.COALOIL));
        recipes.put(COAL.dust(),										new FluidStack(100, Fluids.COALOIL));
        recipes.put(LIGNITE.gem(),										new FluidStack(50, Fluids.COALOIL));
        recipes.put(LIGNITE.dust(),										new FluidStack(50, Fluids.COALOIL));
        recipes.put(KEY_OIL_TAR,										new FluidStack(75, Fluids.BITUMEN));
        recipes.put(KEY_CRACK_TAR,										new FluidStack(100, Fluids.BITUMEN));
        recipes.put(KEY_COAL_TAR,										new FluidStack(50, Fluids.BITUMEN));
        recipes.put(KEY_LOG,											new FluidStack(100, Fluids.MUG));
        recipes.put(NA.dust(),											new FluidStack(100, Fluids.SODIUM));
        recipes.put(PB.ingot(),											new FluidStack(100, Fluids.LEAD));
        recipes.put(PB.dust(),											new FluidStack(100, Fluids.LEAD));
        //general utility recipes because why not
        recipes.put(new RecipesCommon.ComparableStack(Blocks.NETHERRACK),			new FluidStack(250, Fluids.LAVA));
        recipes.put(new RecipesCommon.ComparableStack(Blocks.COBBLESTONE),		new FluidStack(250, Fluids.LAVA));
        recipes.put(new RecipesCommon.ComparableStack(Blocks.STONE),				new FluidStack(250, Fluids.LAVA));
        recipes.put(new RecipesCommon.ComparableStack(Blocks.OBSIDIAN),			new FluidStack(500, Fluids.LAVA));
        recipes.put(new RecipesCommon.ComparableStack(Items.SNOWBALL),			new FluidStack(125, Fluids.WATER));
        recipes.put(new RecipesCommon.ComparableStack(Blocks.SNOW),				new FluidStack(500, Fluids.WATER));
        recipes.put(new RecipesCommon.ComparableStack(Blocks.ICE),				new FluidStack(1000, Fluids.WATER));
        recipes.put(new RecipesCommon.ComparableStack(Blocks.PACKED_ICE),			new FluidStack(1000, Fluids.WATER));
        recipes.put(new RecipesCommon.ComparableStack(Items.ENDER_PEARL),			new FluidStack(100, Fluids.ENDERJUICE));
        recipes.put(new RecipesCommon.ComparableStack(ModItems.pellet_charged),	new FluidStack(4000, Fluids.HELIUM4));
        recipes.put(new RecipesCommon.ComparableStack(ModBlocks.ore_oil_sand),	new FluidStack(100, Fluids.BITUMEN));

        recipes.put(new RecipesCommon.ComparableStack(Items.SUGAR),				new FluidStack(100, Fluids.ETHANOL));
        recipes.put(new RecipesCommon.ComparableStack(ModBlocks.plant_flower, 1, 3),	new FluidStack(150, Fluids.ETHANOL));
        recipes.put(new RecipesCommon.ComparableStack(ModBlocks.plant_flower, 1, 4),	new FluidStack(50, Fluids.ETHANOL));
        recipes.put(new RecipesCommon.ComparableStack(ModItems.biomass),			new FluidStack(125, Fluids.BIOGAS));
        recipes.put(new RecipesCommon.ComparableStack(ModItems.glyphid_gland_empty),	new FluidStack(2000, Fluids.BIOGAS));
        recipes.put(new RecipesCommon.ComparableStack(Items.FISH, 1, OreDictionary.WILDCARD_VALUE), new FluidStack(100, Fluids.FISHOIL));
        recipes.put(new RecipesCommon.ComparableStack(Blocks.DOUBLE_PLANT, 1, 0),	new FluidStack(100, Fluids.SUNFLOWEROIL));

        recipes.put(new RecipesCommon.ComparableStack(Items.WHEAT_SEEDS),			new FluidStack(50, Fluids.SEEDSLURRY));
        recipes.put(new RecipesCommon.ComparableStack(Blocks.TALLGRASS, 1, 1),	new FluidStack(100, Fluids.SEEDSLURRY));
        recipes.put(new RecipesCommon.ComparableStack(Blocks.TALLGRASS, 1, 2),	new FluidStack(100, Fluids.SEEDSLURRY));
        recipes.put(new RecipesCommon.ComparableStack(Blocks.VINE),				new FluidStack(100, Fluids.SEEDSLURRY));
    }

    public static FluidStack getOutput(ItemStack stack) {

        if(stack.isEmpty() || stack.getItem() == null)
            return null;

        RecipesCommon.ComparableStack comp = new RecipesCommon.ComparableStack(stack.getItem(), 1, stack.getItemDamage());

        if(recipes.containsKey(comp))
            return recipes.get(comp);

        String[] dictKeys = comp.getDictKeys();
        comp = new RecipesCommon.ComparableStack(stack.getItem(), 1, OreDictionary.WILDCARD_VALUE);

        if(recipes.containsKey(comp))
            return recipes.get(comp);

        for(String key : dictKeys) {

            if(recipes.containsKey(key))
                return recipes.get(key);
        }

        return null;
    }

    public static HashMap<Object, ItemStack> getRecipes() {

        HashMap<Object, ItemStack> recipes = new HashMap<Object, ItemStack>();

        for(Map.Entry<Object, FluidStack> entry : LiquefactionRecipes.recipes.entrySet()) {

            FluidStack out = entry.getValue();

            if(entry.getKey() instanceof String) {
                recipes.put(new RecipesCommon.OreDictStack((String)entry.getKey()), ItemFluidIcon.make(out.type, out.fill));
            } else {
                recipes.put(((RecipesCommon.ComparableStack)entry.getKey()).toStack(), ItemFluidIcon.make(out.type, out.fill));
            }
        }

        return recipes;
    }

    @Override
    public String getFileName() {
        return "hbmLiquefactor.json";
    }

    @Override
    public String getComment() {
        return "As with most handlers, stacksizes for the inputs are ignored and default to 1.";
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
        RecipesCommon.AStack in = this.readAStack(obj.get("input").getAsJsonArray());
        FluidStack out = this.readFluidStack(obj.get("output").getAsJsonArray());

        if(in instanceof RecipesCommon.ComparableStack) {
            recipes.put(((RecipesCommon.ComparableStack) in).makeSingular(), out);
        } else if(in instanceof RecipesCommon.OreDictStack) {
            recipes.put(((RecipesCommon.OreDictStack) in).name, out);
        }
    }

    @Override
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
        Map.Entry<Object, FluidStack> rec = (Map.Entry<Object, FluidStack>) recipe;
        Object key = rec.getKey();

        writer.name("input");
        if(key instanceof String) {
            this.writeAStack(new RecipesCommon.OreDictStack((String) key), writer);
        } else if(key instanceof RecipesCommon.ComparableStack) {
            this.writeAStack((RecipesCommon.ComparableStack) key, writer);
        }

        writer.name("output");
        this.writeFluidStack(rec.getValue(), writer);
    }
}
