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
import com.hbm.inventory.recipes.PedestalRecipes;

import java.util.Collections;

import static com.hbm.inventory.recipes.PedestalRecipes.recipeSets;
import static com.hbm.inventory.recipes.PedestalRecipes.recipes;

/**
 * Dedicated GroovyScript integration for the Pedestal (com.hbm.inventory.recipes.PedestalRecipes).
 * Exposed as mods.hbm.pedestal. Like Ammo Press, this is a fixed 9-slot shaped grid recipe - build
 * with .grid(...), exactly 9 entries, null for an empty slot. Also supports an optional extra()
 * ritual condition and set() bucket (PedestalRecipes buckets recipes by set % 2 for lookup grouping -
 * see PedestalRecipes.register()).
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class Pedestal extends VirtualizedRegistry<PedestalRecipes.PedestalRecipe> {

    public Pedestal() {
        super(Collections.singletonList("pedestal"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(PedestalRecipes.PedestalRecipe recipe) {
        PedestalRecipes.register(recipe);
        this.addScripted(recipe);
    }

    private void removeRecipe(PedestalRecipes.PedestalRecipe recipe) {
        recipes.remove(recipe);
        int set = Math.abs(recipe.recipeSet) % recipeSets.length;
        recipeSets[set].remove(recipe);
        this.addBackup(recipe);
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public int size() {
        return recipes.size();
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<PedestalRecipes.PedestalRecipe> {

        private IIngredient[] grid;
        private PedestalRecipes.PedestalExtraCondition extra = PedestalRecipes.PedestalExtraCondition.NONE;
        private int set = 0;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_pedestal_";
        }

        /** Exactly 9 entries, left-to-right top-to-bottom, like a crafting grid. Use null for an empty slot. */
        public RecipeBuilder grid(IIngredient... grid) {
            this.grid = grid;
            return this;
        }

        /** A ritual condition, e.g. PedestalRecipes.PedestalExtraCondition.FULL_MOON. Defaults to NONE. */
        public RecipeBuilder extra(PedestalRecipes.PedestalExtraCondition extra) {
            this.extra = extra;
            return this;
        }

        /** Grouping bucket (bucketed by set % 2 internally) - matches the pack's own convention if it uses this. */
        public RecipeBuilder set(int set) {
            this.set = set;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Pedestal recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.output.isEmpty()) {
                msg.add("Pedestal recipe needs an output");
            }
            if (this.grid == null || this.grid.length != 9) {
                msg.add("Pedestal recipe's grid(...) needs exactly 9 entries (use null for an empty slot), found {}", this.grid == null ? 0 : this.grid.length);
            }
        }

        @Override
        public PedestalRecipes.PedestalRecipe register() {
            if (!this.validate()) return null;

            RecipesCommon.AStack[] input = new RecipesCommon.AStack[9];
            for (int i = 0; i < 9; i++) {
                input[i] = this.grid[i] == null ? null : IngredientUtils.convertIngredient2Astack(this.grid[i]);
            }

            PedestalRecipes.PedestalRecipe recipe = new PedestalRecipes.PedestalRecipe(this.output.get(0), input)
                    .extra(this.extra)
                    .set(this.set);
            HbmGroovyPropertyContainer.PEDESTAL.addRecipe(recipe);
            return recipe;
        }
    }
}
