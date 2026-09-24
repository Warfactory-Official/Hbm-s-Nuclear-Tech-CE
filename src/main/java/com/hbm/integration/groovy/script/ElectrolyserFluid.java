package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.recipes.ElectrolyserFluidRecipes;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

import java.util.Collections;

import static com.hbm.inventory.recipes.ElectrolyserFluidRecipes.recipes;

/**
 * Dedicated GroovyScript integration for the Fluid Electrolyser (com.hbm.inventory.recipes.ElectrolyserFluidRecipes).
 * Exposed as mods.hbm.electrolyserFluid. Keyed by a single input FluidType and mB amount consumed,
 * produces two output fluids plus optional item byproducts, over a duration (defaults to 20 ticks).
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class ElectrolyserFluid extends VirtualizedRegistry<Tuple.Pair<FluidType, ElectrolyserFluidRecipes.ElectrolysisRecipe>> {

    public ElectrolyserFluid() {
        super(Collections.singletonList("electrolyserFluid"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(Tuple.Pair<FluidType, ElectrolyserFluidRecipes.ElectrolysisRecipe> entry) {
        recipes.put(entry.getKey(), entry.getValue());
        this.addScripted(entry);
    }

    private void removeRecipe(Tuple.Pair<FluidType, ElectrolyserFluidRecipes.ElectrolysisRecipe> entry) {
        recipes.remove(entry.getKey());
        this.addBackup(entry);
    }

    public void removeByInput(FluidType type) {
        ElectrolyserFluidRecipes.ElectrolysisRecipe removed = recipes.get(type);
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

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Pair<FluidType, ElectrolyserFluidRecipes.ElectrolysisRecipe>> {

        private FluidStack input;
        private FluidStack output1;
        private FluidStack output2;
        private int duration = 20;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_electrolyserfluid_";
        }

        /** The fluid type this recipe keys on and the mB consumed per operation. */
        public RecipeBuilder input(FluidStack input) {
            this.input = input;
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

        public RecipeBuilder duration(int duration) {
            this.duration = duration;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Fluid Electrolyser recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.input == null) {
                msg.add("Fluid Electrolyser recipe needs an input(...) fluid");
            }
            if (this.output1 == null || this.output2 == null) {
                msg.add("Fluid Electrolyser recipe needs both outputFluid1(...) and outputFluid2(...)");
            }
        }

        @Override
        public Tuple.Pair<FluidType, ElectrolyserFluidRecipes.ElectrolysisRecipe> register() {
            if (!this.validate()) return null;

            ItemStack[] byproducts = this.output.toArray(new ItemStack[0]);
            ElectrolyserFluidRecipes.ElectrolysisRecipe recipe =
                    new ElectrolyserFluidRecipes.ElectrolysisRecipe(this.input.fill, this.output1, this.output2, this.duration, byproducts);
            Tuple.Pair<FluidType, ElectrolyserFluidRecipes.ElectrolysisRecipe> entry = new Tuple.Pair<>(this.input.type, recipe);
            HbmGroovyPropertyContainer.ELECTROLYSERFLUID.addRecipe(entry);
            return entry;
        }
    }
}
