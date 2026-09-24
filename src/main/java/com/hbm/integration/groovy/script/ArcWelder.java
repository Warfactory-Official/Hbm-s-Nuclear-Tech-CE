package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.integration.groovy.util.IngredientUtils;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.recipes.ArcWelderRecipes;

import java.util.Collections;

/**
 * Dedicated GroovyScript integration for the Arc Welder (com.hbm.inventory.recipes.ArcWelderRecipes).
 * Exposed as mods.hbm.arcWelder - explicit name to avoid the recipeOverrides RecipeFileBinding alias
 * "arcwelder" (see AssemblyMachine's javadoc for the full rationale).
 * <p>
 * ArcWelderRecipes is a bespoke SerializableRecipe (not GenericRecipes&lt;GenericRecipe&gt; like
 * Assembly Machine/Chemical Plant) - its recipe list has no name/uniqueness concept at all, just a
 * plain List&lt;ArcWelderRecipe&gt;, so removal here is by object reference rather than by name.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class ArcWelder extends VirtualizedRegistry<ArcWelderRecipes.ArcWelderRecipe> {

    public ArcWelder() {
        super(Collections.singletonList("arcWelder"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(ArcWelderRecipes.ArcWelderRecipe recipe) {
        ArcWelderRecipes.recipes.add(recipe);
        this.addScripted(recipe);
    }

    private void removeRecipe(ArcWelderRecipes.ArcWelderRecipe recipe) {
        ArcWelderRecipes.recipes.remove(recipe);
        this.addBackup(recipe);
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public int size() {
        return ArcWelderRecipes.recipes.size();
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<ArcWelderRecipes.ArcWelderRecipe> {

        private FluidStack fluid;
        private int duration = 100;
        private long power = 100;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_arcwelder_";
        }

        /**
         * The Arc Welder uses hbm's own FluidStack type, not Forge's - construct with e.g.
         * new FluidStack(Fluids.WATER, 2000).
         */
        public RecipeBuilder inputFluid(FluidStack fluid) {
            this.fluid = fluid;
            return this;
        }

        public RecipeBuilder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public RecipeBuilder power(long power) {
            this.power = power;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Arc Welder recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            this.validateItems(msg, 1, 12, 1, 1);
        }

        @Override
        public ArcWelderRecipes.ArcWelderRecipe register() {
            if (!this.validate()) return null;

            RecipesCommon.AStack[] items = new RecipesCommon.AStack[this.input.size()];
            for (int i = 0; i < items.length; i++) items[i] = IngredientUtils.convertIngredient2Astack(this.input.get(i));

            ArcWelderRecipes.ArcWelderRecipe recipe = new ArcWelderRecipes.ArcWelderRecipe(this.output.get(0), this.duration, this.power, this.fluid, items);
            HbmGroovyPropertyContainer.ARCWELDER.addRecipe(recipe);
            return recipe;
        }
    }
}
