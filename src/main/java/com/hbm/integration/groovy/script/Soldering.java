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
import com.hbm.inventory.recipes.SolderingRecipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dedicated GroovyScript integration for Soldering (com.hbm.inventory.recipes.SolderingRecipes).
 * Exposed as mods.hbm.soldering.
 * <p>
 * Bespoke SerializableRecipe, not GenericRecipes - plain List&lt;SolderingRecipe&gt;, no
 * name/uniqueness concept, so removal is by object reference. Unlike Assembly Machine/Chemical
 * Plant, this machine has three separate ingredient categories (toppings/pcb/solder) instead of one
 * flat input list, so the base builder's .input(...) is unused here in favor of three dedicated
 * methods.
 * <p>
 * NOTE: SolderingRecipe's constructor has a side effect of adding its ingredients to static
 * toppings/pcb/solder HashSets (used elsewhere for slot-filtering) - removing a recipe here does NOT
 * retract that, same limitation a plain JSON-file removal would have without a full recipe reload.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class Soldering extends VirtualizedRegistry<SolderingRecipes.SolderingRecipe> {

    public Soldering() {
        super(Collections.singletonList("soldering"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(SolderingRecipes.SolderingRecipe recipe) {
        SolderingRecipes.recipes.add(recipe);
        this.addScripted(recipe);
    }

    private void removeRecipe(SolderingRecipes.SolderingRecipe recipe) {
        SolderingRecipes.recipes.remove(recipe);
        this.addBackup(recipe);
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public int size() {
        return SolderingRecipes.recipes.size();
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<SolderingRecipes.SolderingRecipe> {

        private final List<IIngredient> toppings = new ArrayList<>();
        private final List<IIngredient> pcb = new ArrayList<>();
        private final List<IIngredient> solder = new ArrayList<>();
        private FluidStack fluid;
        private int duration = 100;
        private long power = 100;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_soldering_";
        }

        public RecipeBuilder toppings(IIngredient... items) {
            Collections.addAll(this.toppings, items);
            return this;
        }

        public RecipeBuilder pcb(IIngredient... items) {
            Collections.addAll(this.pcb, items);
            return this;
        }

        public RecipeBuilder solder(IIngredient... items) {
            Collections.addAll(this.solder, items);
            return this;
        }

        /**
         * Soldering uses hbm's own FluidStack type, not Forge's - construct with e.g.
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
            return "Error adding Soldering recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.output.isEmpty()) {
                msg.add("Soldering recipe needs an output");
            }
            if (this.toppings.isEmpty() && this.pcb.isEmpty() && this.solder.isEmpty()) {
                msg.add("Soldering recipe needs at least one topping, pcb, or solder ingredient");
            }
        }

        private static RecipesCommon.AStack[] toAStacks(List<IIngredient> list) {
            RecipesCommon.AStack[] result = new RecipesCommon.AStack[list.size()];
            for (int i = 0; i < result.length; i++) result[i] = IngredientUtils.convertIngredient2Astack(list.get(i));
            return result;
        }

        @Override
        public SolderingRecipes.SolderingRecipe register() {
            if (!this.validate()) return null;

            SolderingRecipes.SolderingRecipe recipe = new SolderingRecipes.SolderingRecipe(
                    this.output.get(0), this.duration, this.power, this.fluid,
                    toAStacks(this.toppings), toAStacks(this.pcb), toAStacks(this.solder));
            HbmGroovyPropertyContainer.SOLDERING.addRecipe(recipe);
            return recipe;
        }
    }
}
