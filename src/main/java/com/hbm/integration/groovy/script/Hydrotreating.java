package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.recipes.HydrotreatingRecipes;
import com.hbm.util.Tuple;

import java.util.Collections;

import static com.hbm.inventory.recipes.HydrotreatingRecipes.recipes;

/**
 * Dedicated GroovyScript integration for Hydrotreating (com.hbm.inventory.recipes.HydrotreatingRecipes).
 * Exposed as mods.hbm.hydrotreating. Keyed by a single input FluidType, consumes a hydrogen fluid
 * alongside it, produces two output fluids.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class Hydrotreating extends VirtualizedRegistry<Tuple.Pair<FluidType, Tuple.Triplet<FluidStack, FluidStack, FluidStack>>> {

    public Hydrotreating() {
        super(Collections.singletonList("hydrotreating"));
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
        private FluidStack hydrogen;
        private FluidStack output1;
        private FluidStack output2;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_hydrotreating_";
        }

        /** The fluid this recipe keys on. */
        public RecipeBuilder input(FluidType input) {
            this.inputType = input;
            return this;
        }

        public RecipeBuilder hydrogen(FluidStack hydrogen) {
            this.hydrogen = hydrogen;
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
            return "Error adding Hydrotreating recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.inputType == null) {
                msg.add("Hydrotreating recipe needs an input(...) fluid type");
            }
            if (this.hydrogen == null) {
                msg.add("Hydrotreating recipe needs hydrogen(...)");
            }
            if (this.output1 == null || this.output2 == null) {
                msg.add("Hydrotreating recipe needs both outputFluid1(...) and outputFluid2(...)");
            }
        }

        @Override
        public Tuple.Pair<FluidType, Tuple.Triplet<FluidStack, FluidStack, FluidStack>> register() {
            if (!this.validate()) return null;

            Tuple.Pair<FluidType, Tuple.Triplet<FluidStack, FluidStack, FluidStack>> entry =
                    new Tuple.Pair<>(this.inputType, new Tuple.Triplet<>(this.hydrogen, this.output1, this.output2));
            HbmGroovyPropertyContainer.HYDROTREATING.addRecipe(entry);
            return entry;
        }
    }
}
