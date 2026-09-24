package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.recipes.FluidBreederRecipes;
import com.hbm.util.Tuple;

import java.util.Collections;

import static com.hbm.inventory.recipes.FluidBreederRecipes.recipes;

/**
 * Dedicated GroovyScript integration for the Fluid Breeder (com.hbm.inventory.recipes.FluidBreederRecipes).
 * Exposed as mods.hbm.fluidBreeder. Keyed by a single input FluidType and mB amount consumed, produces
 * one output fluid.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class FluidBreeder extends VirtualizedRegistry<Tuple.Pair<FluidType, Tuple.Pair<Integer, FluidStack>>> {

    public FluidBreeder() {
        super(Collections.singletonList("fluidBreeder"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(Tuple.Pair<FluidType, Tuple.Pair<Integer, FluidStack>> entry) {
        recipes.put(entry.getKey(), entry.getValue());
        this.addScripted(entry);
    }

    private void removeRecipe(Tuple.Pair<FluidType, Tuple.Pair<Integer, FluidStack>> entry) {
        recipes.remove(entry.getKey());
        this.addBackup(entry);
    }

    public void removeByInput(FluidType type) {
        Tuple.Pair<Integer, FluidStack> removed = recipes.get(type);
        if (removed == null) return;
        this.removeRecipe(new Tuple.Pair<>(type, removed));
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public int size() {
        return recipes.size();
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Pair<FluidType, Tuple.Pair<Integer, FluidStack>>> {

        private FluidStack input;
        private FluidStack output;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_fluidbreeder_";
        }

        /** The fluid type this recipe keys on and the mB consumed per operation. */
        public RecipeBuilder input(FluidStack input) {
            this.input = input;
            return this;
        }

        public RecipeBuilder output(FluidStack output) {
            this.output = output;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Fluid Breeder recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.input == null) {
                msg.add("Fluid Breeder recipe needs an input(...) fluid");
            }
            if (this.output == null) {
                msg.add("Fluid Breeder recipe needs an output(...) fluid");
            }
        }

        @Override
        public Tuple.Pair<FluidType, Tuple.Pair<Integer, FluidStack>> register() {
            if (!this.validate()) return null;

            Tuple.Pair<FluidType, Tuple.Pair<Integer, FluidStack>> entry =
                    new Tuple.Pair<>(this.input.type, new Tuple.Pair<>(this.input.fill, this.output));
            HbmGroovyPropertyContainer.FLUIDBREEDER.addRecipe(entry);
            return entry;
        }
    }
}
