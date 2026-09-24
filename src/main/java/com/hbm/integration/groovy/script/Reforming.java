package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.recipes.ReformingRecipes;
import com.hbm.util.Tuple;

import java.util.Collections;

import static com.hbm.inventory.recipes.ReformingRecipes.recipes;

/**
 * Dedicated GroovyScript integration for Reforming (com.hbm.inventory.recipes.ReformingRecipes).
 * Exposed as mods.hbm.reforming. Keyed by a single input FluidType, produces three output fluids.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class Reforming extends VirtualizedRegistry<Tuple.Pair<FluidType, Tuple.Triplet<FluidStack, FluidStack, FluidStack>>> {

    public Reforming() {
        super(Collections.singletonList("reforming"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(Tuple.Pair<FluidType, Tuple.Triplet<FluidStack, FluidStack, FluidStack>> entry) {
        recipes.put(entry.getKey(), entry.getValue());
        this.addScripted(entry);
    }

    private void removeRecipe(Tuple.Pair<FluidType, Tuple.Triplet<FluidStack, FluidStack, FluidStack>> entry) {
        recipes.remove(entry.getKey());
        this.addBackup(entry);
    }

    public void removeByInput(FluidType type) {
        Tuple.Triplet<FluidStack, FluidStack, FluidStack> removed = recipes.get(type);
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

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Pair<FluidType, Tuple.Triplet<FluidStack, FluidStack, FluidStack>>> {

        private FluidType inputType;
        private FluidStack output1;
        private FluidStack output2;
        private FluidStack output3;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_reforming_";
        }

        /** The fluid this recipe keys on. */
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

        public RecipeBuilder outputFluid3(FluidStack output) {
            this.output3 = output;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Reforming recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.inputType == null) {
                msg.add("Reforming recipe needs an input(...) fluid type");
            }
            if (this.output1 == null || this.output2 == null || this.output3 == null) {
                msg.add("Reforming recipe needs outputFluid1(...), outputFluid2(...) and outputFluid3(...)");
            }
        }

        @Override
        public Tuple.Pair<FluidType, Tuple.Triplet<FluidStack, FluidStack, FluidStack>> register() {
            if (!this.validate()) return null;

            Tuple.Pair<FluidType, Tuple.Triplet<FluidStack, FluidStack, FluidStack>> entry =
                    new Tuple.Pair<>(this.inputType, new Tuple.Triplet<>(this.output1, this.output2, this.output3));
            HbmGroovyPropertyContainer.REFORMING.addRecipe(entry);
            return entry;
        }
    }
}
