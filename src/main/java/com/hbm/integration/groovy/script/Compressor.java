package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.recipes.CompressorRecipes;
import com.hbm.util.Tuple;

import java.util.Collections;

import static com.hbm.inventory.recipes.CompressorRecipes.recipes;

/**
 * Dedicated GroovyScript integration for the Compressor (com.hbm.inventory.recipes.CompressorRecipes).
 * Exposed as mods.hbm.compressor. Keyed by an input fluid type + pressure tier, produces an output
 * fluid, over a duration (defaults to 100 ticks).
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class Compressor extends VirtualizedRegistry<Tuple.Pair<Tuple.Pair<FluidType, Integer>, CompressorRecipes.CompressorRecipe>> {

    public Compressor() {
        super(Collections.singletonList("compressor"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(Tuple.Pair<Tuple.Pair<FluidType, Integer>, CompressorRecipes.CompressorRecipe> entry) {
        recipes.put(entry.getKey(), entry.getValue());
        this.addScripted(entry);
    }

    private void removeRecipe(Tuple.Pair<Tuple.Pair<FluidType, Integer>, CompressorRecipes.CompressorRecipe> entry) {
        recipes.remove(entry.getKey());
        this.addBackup(entry);
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public int size() {
        return recipes.size();
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Pair<Tuple.Pair<FluidType, Integer>, CompressorRecipes.CompressorRecipe>> {

        private FluidStack input;
        private FluidStack output;
        private int duration = 100;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_compressor_";
        }

        /** Keys on the fluid's type and pressure; the fill amount is the mB consumed per operation. */
        public RecipeBuilder input(FluidStack input) {
            this.input = input;
            return this;
        }

        public RecipeBuilder output(FluidStack output) {
            this.output = output;
            return this;
        }

        public RecipeBuilder duration(int duration) {
            this.duration = duration;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Compressor recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.input == null) {
                msg.add("Compressor recipe needs an input(...) fluid");
            }
            if (this.output == null) {
                msg.add("Compressor recipe needs an output(...) fluid");
            }
        }

        @Override
        public Tuple.Pair<Tuple.Pair<FluidType, Integer>, CompressorRecipes.CompressorRecipe> register() {
            if (!this.validate()) return null;

            Tuple.Pair<FluidType, Integer> key = new Tuple.Pair<>(this.input.type, this.input.pressure);
            CompressorRecipes.CompressorRecipe recipe = new CompressorRecipes.CompressorRecipe(this.input.fill, this.output, this.duration);
            Tuple.Pair<Tuple.Pair<FluidType, Integer>, CompressorRecipes.CompressorRecipe> entry = new Tuple.Pair<>(key, recipe);
            HbmGroovyPropertyContainer.COMPRESSOR.addRecipe(entry);
            return entry;
        }
    }
}
