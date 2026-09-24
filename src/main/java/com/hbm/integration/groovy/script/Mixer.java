package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.integration.groovy.util.IngredientUtils;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.recipes.MixerRecipes;
import com.hbm.util.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.hbm.inventory.recipes.MixerRecipes.recipes;

/**
 * Dedicated GroovyScript integration for the Mixer (com.hbm.inventory.recipes.MixerRecipes).
 * Exposed as mods.hbm.mixer. Keyed by its OUTPUT fluid type - multiple recipes can share the same
 * output type, each with up to 2 fluid inputs and/or one solid item input.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class Mixer extends VirtualizedRegistry<Tuple.Pair<FluidType, MixerRecipes.MixerRecipe>> {

    public Mixer() {
        super(Collections.singletonList("mixer"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(Tuple.Pair<FluidType, MixerRecipes.MixerRecipe> entry) {
        MixerRecipes.MixerRecipe[] existing = recipes.get(entry.getKey());
        MixerRecipes.MixerRecipe[] updated;
        if (existing == null) {
            updated = new MixerRecipes.MixerRecipe[] {entry.getValue()};
        } else {
            updated = Arrays.copyOf(existing, existing.length + 1);
            updated[existing.length] = entry.getValue();
        }
        recipes.put(entry.getKey(), updated);
        this.addScripted(entry);
    }

    private void removeRecipe(Tuple.Pair<FluidType, MixerRecipes.MixerRecipe> entry) {
        MixerRecipes.MixerRecipe[] existing = recipes.get(entry.getKey());
        if (existing != null) {
            List<MixerRecipes.MixerRecipe> list = new ArrayList<>(Arrays.asList(existing));
            list.remove(entry.getValue());
            if (list.isEmpty()) {
                recipes.remove(entry.getKey());
            } else {
                recipes.put(entry.getKey(), list.toArray(new MixerRecipes.MixerRecipe[0]));
            }
        }
        this.addBackup(entry);
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public int size() {
        int total = 0;
        for (MixerRecipes.MixerRecipe[] arr : recipes.values()) total += arr.length;
        return total;
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Pair<FluidType, MixerRecipes.MixerRecipe>> {

        private FluidType outputType;
        private int outputAmount;
        private int duration = 100;
        private FluidStack inputFluid1;
        private FluidStack inputFluid2;
        private IIngredient inputSolid;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_mixer_";
        }

        public RecipeBuilder outputFluid(FluidType type, int amount) {
            this.outputType = type;
            this.outputAmount = amount;
            return this;
        }

        public RecipeBuilder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public RecipeBuilder inputFluid1(FluidStack input) {
            this.inputFluid1 = input;
            return this;
        }

        public RecipeBuilder inputFluid2(FluidStack input) {
            this.inputFluid2 = input;
            return this;
        }

        public RecipeBuilder inputSolid(IIngredient input) {
            this.inputSolid = input;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Mixer recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.outputType == null) {
                msg.add("Mixer recipe needs an outputFluid(type, amount)");
            }
            if (this.inputFluid1 == null && this.inputFluid2 == null && this.inputSolid == null) {
                msg.add("Mixer recipe needs at least one of inputFluid1(...), inputFluid2(...) or inputSolid(...)");
            }
        }

        @Override
        public Tuple.Pair<FluidType, MixerRecipes.MixerRecipe> register() {
            if (!this.validate()) return null;

            MixerRecipes.MixerRecipe recipe = new MixerRecipes.MixerRecipe(this.outputAmount, this.duration);
            if (this.inputFluid1 != null) recipe.setStack1(this.inputFluid1);
            if (this.inputFluid2 != null) recipe.setStack2(this.inputFluid2);
            if (this.inputSolid != null) recipe.setSolid(IngredientUtils.convertIngredient2Astack(this.inputSolid));

            Tuple.Pair<FluidType, MixerRecipes.MixerRecipe> entry = new Tuple.Pair<>(this.outputType, recipe);
            HbmGroovyPropertyContainer.MIXER.addRecipe(entry);
            return entry;
        }
    }
}
