package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.recipes.FractionRecipes;
import com.hbm.util.Tuple;

import java.util.Collections;

import static com.hbm.inventory.recipes.FractionRecipes.fractions;

/**
 * Dedicated GroovyScript integration for the Fractionator (com.hbm.inventory.recipes.FractionRecipes).
 * Exposed as mods.hbm.fraction. Keyed by an input FluidType (always assumed to be a 100mB input),
 * produces two output fluids.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class Fraction extends VirtualizedRegistry<Tuple.Pair<FluidType, Tuple.Pair<FluidStack, FluidStack>>> {

    public Fraction() {
        super(Collections.singletonList("fraction"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(Tuple.Pair<FluidType, Tuple.Pair<FluidStack, FluidStack>> entry) {
        fractions.put(entry.getKey(), entry.getValue());
        this.addScripted(entry);
    }

    private void removeRecipe(Tuple.Pair<FluidType, Tuple.Pair<FluidStack, FluidStack>> entry) {
        fractions.remove(entry.getKey());
        this.addBackup(entry);
    }

    public void removeByInput(FluidType type) {
        Tuple.Pair<FluidStack, FluidStack> removed = fractions.get(type);
        if (removed == null) return;
        this.removeRecipe(new Tuple.Pair<>(type, removed));
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public int size() {
        return fractions.size();
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Pair<FluidType, Tuple.Pair<FluidStack, FluidStack>>> {

        private FluidType inputType;
        private FluidStack output1;
        private FluidStack output2;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_fraction_";
        }

        /** The fluid this recipe keys on - inputs are always assumed to be 100mB. */
        public RecipeBuilder input(FluidType input) {
            this.inputType = input;
            return this;
        }

        public RecipeBuilder outputFluid1(FluidStack output) {
            this.output1 = output;
            return this;
        }

        public RecipeBuilder outputFluid2(FluidStack output) {
            this.output2 = output;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Fractionator recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.inputType == null) {
                msg.add("Fractionator recipe needs an input(...) fluid type");
            }
            if (this.output1 == null || this.output2 == null) {
                msg.add("Fractionator recipe needs both outputFluid1(...) and outputFluid2(...)");
            }
        }

        @Override
        public Tuple.Pair<FluidType, Tuple.Pair<FluidStack, FluidStack>> register() {
            if (!this.validate()) return null;

            Tuple.Pair<FluidType, Tuple.Pair<FluidStack, FluidStack>> entry =
                    new Tuple.Pair<>(this.inputType, new Tuple.Pair<>(this.output1, this.output2));
            HbmGroovyPropertyContainer.FRACTION.addRecipe(entry);
            return entry;
        }
    }
}
