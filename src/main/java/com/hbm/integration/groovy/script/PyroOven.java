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
import com.hbm.inventory.recipes.PyroOvenRecipes;
import net.minecraft.item.ItemStack;

import java.util.Collections;

import static com.hbm.inventory.recipes.PyroOvenRecipes.recipes;

/**
 * Dedicated GroovyScript integration for Pyrolysis (com.hbm.inventory.recipes.PyroOvenRecipes).
 * Exposed as mods.hbm.pyroOven. One optional item input, one optional fluid input, one optional
 * item output, one optional fluid output (mix and match, at least one in and one out) plus a
 * duration - no power field for this machine.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class PyroOven extends VirtualizedRegistry<PyroOvenRecipes.PyroOvenRecipe> {

    public PyroOven() {
        super(Collections.singletonList("pyroOven"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(PyroOvenRecipes.PyroOvenRecipe recipe) {
        recipes.add(recipe);
        this.addScripted(recipe);
    }

    private void removeRecipe(PyroOvenRecipes.PyroOvenRecipe recipe) {
        recipes.remove(recipe);
        this.addBackup(recipe);
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public int size() {
        return recipes.size();
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<PyroOvenRecipes.PyroOvenRecipe> {

        private IIngredient inputItem;
        private FluidStack inputFluid;
        private ItemStack outputItem;
        private FluidStack outputFluid;
        private int duration = 100;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_pyrooven_";
        }

        public RecipeBuilder inputItem(IIngredient item) {
            this.inputItem = item;
            return this;
        }

        /** Uses hbm's own FluidStack type, not Forge's. */
        public RecipeBuilder inputFluid(FluidStack fluid) {
            this.inputFluid = fluid;
            return this;
        }

        public RecipeBuilder outputItem(ItemStack item) {
            this.outputItem = item;
            return this;
        }

        public RecipeBuilder outputFluid(FluidStack fluid) {
            this.outputFluid = fluid;
            return this;
        }

        public RecipeBuilder duration(int duration) {
            this.duration = duration;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Pyrolysis recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.inputItem == null && this.inputFluid == null) {
                msg.add("Pyrolysis recipe needs at least one item or fluid input");
            }
            if (this.outputItem == null && this.outputFluid == null) {
                msg.add("Pyrolysis recipe needs at least one item or fluid output");
            }
        }

        @Override
        public PyroOvenRecipes.PyroOvenRecipe register() {
            if (!this.validate()) return null;

            PyroOvenRecipes.PyroOvenRecipe recipe = new PyroOvenRecipes.PyroOvenRecipe(this.duration)
                    .in(this.inputFluid)
                    .in(this.inputItem == null ? null : IngredientUtils.convertIngredient2Astack(this.inputItem))
                    .out(this.outputFluid)
                    .out(this.outputItem);
            HbmGroovyPropertyContainer.PYROOVEN.addRecipe(recipe);
            return recipe;
        }
    }
}
