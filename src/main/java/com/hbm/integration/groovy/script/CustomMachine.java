package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.integration.groovy.util.IngredientUtils;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.recipes.CustomMachineRecipes;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hbm.inventory.recipes.CustomMachineRecipes.recipes;

/**
 * Dedicated GroovyScript integration for user-defined custom machines (com.hbm.inventory.recipes.
 * CustomMachineRecipes) - lets a pack define recipes for an entirely custom machine keyed by an
 * arbitrary machine ID string (e.g. "paperPress" in hbm's own stock example), rather than adding to
 * an existing machine. Exposed as mods.hbm.customMachine.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class CustomMachine extends VirtualizedRegistry<Tuple.Pair<String, CustomMachineRecipes.CustomMachineRecipe>> {

    public CustomMachine() {
        super(Collections.singletonList("customMachine"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(Tuple.Pair<String, CustomMachineRecipes.CustomMachineRecipe> entry) {
        recipes.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
        this.addScripted(entry);
    }

    private void removeRecipe(Tuple.Pair<String, CustomMachineRecipes.CustomMachineRecipe> entry) {
        List<CustomMachineRecipes.CustomMachineRecipe> list = recipes.get(entry.getKey());
        if (list != null) {
            list.remove(entry.getValue());
            if (list.isEmpty()) recipes.remove(entry.getKey());
        }
        this.addBackup(entry);
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public int size() {
        int total = 0;
        for (List<CustomMachineRecipes.CustomMachineRecipe> list : recipes.values()) total += list.size();
        return total;
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Pair<String, CustomMachineRecipes.CustomMachineRecipe>> {

        private String machineKey;
        private FluidStack[] inputFluids = new FluidStack[0];
        private IIngredient[] inputItems = new IIngredient[0];
        private FluidStack[] outputFluids = new FluidStack[0];
        private final List<Tuple.Pair<ItemStack, Float>> outputItems = new ArrayList<>();
        private int duration = 100;
        private int power = 100;
        private String pollutionType = "";
        private float pollutionAmount = 0F;
        private float radiationAmount = 0F;
        private int flux = 0;
        private int heat = 0;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_custommachine_";
        }

        /** The custom machine's own ID string, e.g. "paperPress" - matches whatever the machine itself looks up recipes by. */
        public RecipeBuilder machine(String key) {
            this.machineKey = key;
            return this;
        }

        public RecipeBuilder inputFluids(FluidStack... fluids) {
            this.inputFluids = fluids;
            return this;
        }

        public RecipeBuilder inputItems(IIngredient... items) {
            this.inputItems = items;
            return this;
        }

        public RecipeBuilder outputFluids(FluidStack... fluids) {
            this.outputFluids = fluids;
            return this;
        }

        /** Adds one chance-weighted item output (chance 0-1, 1 = always). Call multiple times for multiple outputs. */
        public RecipeBuilder outputItem(ItemStack item, float chance) {
            this.outputItems.add(new Tuple.Pair<>(item, chance));
            return this;
        }

        public RecipeBuilder outputItem(ItemStack item) {
            return outputItem(item, 1F);
        }

        public RecipeBuilder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public RecipeBuilder power(int power) {
            this.power = power;
            return this;
        }

        public RecipeBuilder pollution(String type, float amount) {
            this.pollutionType = type;
            this.pollutionAmount = amount;
            return this;
        }

        public RecipeBuilder radiation(float amount) {
            this.radiationAmount = amount;
            return this;
        }

        public RecipeBuilder flux(int flux) {
            this.flux = flux;
            return this;
        }

        public RecipeBuilder heat(int heat) {
            this.heat = heat;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Custom Machine recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.machineKey == null || this.machineKey.isEmpty()) {
                msg.add("Custom Machine recipe needs a machine(...) key string");
            }
            if (this.inputFluids.length == 0 && this.inputItems.length == 0) {
                msg.add("Custom Machine recipe needs at least one input");
            }
            if (this.outputFluids.length == 0 && this.outputItems.isEmpty()) {
                msg.add("Custom Machine recipe needs at least one output");
            }
        }

        @Override
        public Tuple.Pair<String, CustomMachineRecipes.CustomMachineRecipe> register() {
            if (!this.validate()) return null;

            RecipesCommon.AStack[] items = new RecipesCommon.AStack[this.inputItems.length];
            for (int i = 0; i < items.length; i++) items[i] = IngredientUtils.convertIngredient2Astack(this.inputItems[i]);

            CustomMachineRecipes.CustomMachineRecipe recipe = new CustomMachineRecipes.CustomMachineRecipe();
            recipe.inputFluids = this.inputFluids;
            recipe.inputItems = items;
            recipe.outputFluids = this.outputFluids;
            recipe.outputItems = this.outputItems.toArray(new Tuple.Pair[0]);
            recipe.duration = this.duration;
            recipe.consumptionPerTick = this.power;
            recipe.pollutionType = this.pollutionType;
            recipe.pollutionAmount = this.pollutionAmount;
            recipe.radiationAmount = this.radiationAmount;
            recipe.flux = this.flux;
            recipe.heat = this.heat;

            Tuple.Pair<String, CustomMachineRecipes.CustomMachineRecipe> entry = new Tuple.Pair<>(this.machineKey, recipe);
            HbmGroovyPropertyContainer.CUSTOMMACHINE.addRecipe(entry);
            return entry;
        }
    }
}
