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
import com.hbm.inventory.recipes.AmmoPressRecipes;
import net.minecraft.item.ItemStack;

import java.util.Collections;

import static com.hbm.inventory.recipes.AmmoPressRecipes.recipes;

/**
 * Dedicated GroovyScript integration for the Ammo Press (com.hbm.inventory.recipes.AmmoPressRecipes).
 * Exposed as mods.hbm.ammoPress. Unlike most machines here, this is a fixed 9-slot shaped grid
 * recipe (left-to-right, top-to-bottom, like a crafting grid) with no duration/power at all - build
 * with .grid(...), exactly 9 entries, null for an empty slot.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class AmmoPress extends VirtualizedRegistry<AmmoPressRecipes.AmmoPressRecipe> {

    public AmmoPress() {
        super(Collections.singletonList("ammoPress"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(AmmoPressRecipes.AmmoPressRecipe recipe) {
        recipes.add(recipe);
        this.addScripted(recipe);
    }

    private void removeRecipe(AmmoPressRecipes.AmmoPressRecipe recipe) {
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

    public static class RecipeBuilder extends AbstractRecipeBuilder<AmmoPressRecipes.AmmoPressRecipe> {

        private IIngredient[] grid;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_ammopress_";
        }

        /** Exactly 9 entries, left-to-right top-to-bottom, like a crafting grid. Use null for an empty slot. */
        public RecipeBuilder grid(IIngredient... grid) {
            this.grid = grid;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Ammo Press recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.output.isEmpty()) {
                msg.add("Ammo Press recipe needs an output");
            }
            if (this.grid == null || this.grid.length != 9) {
                msg.add("Ammo Press recipe's grid(...) needs exactly 9 entries (use null for an empty slot), found {}", this.grid == null ? 0 : this.grid.length);
            }
        }

        @Override
        public AmmoPressRecipes.AmmoPressRecipe register() {
            if (!this.validate()) return null;

            RecipesCommon.AStack[] input = new RecipesCommon.AStack[9];
            for (int i = 0; i < 9; i++) {
                input[i] = this.grid[i] == null ? null : IngredientUtils.convertIngredient2Astack(this.grid[i]);
            }

            AmmoPressRecipes.AmmoPressRecipe recipe = new AmmoPressRecipes.AmmoPressRecipe(this.output.get(0), input);
            HbmGroovyPropertyContainer.AMMOPRESS.addRecipe(recipe);
            return recipe;
        }
    }
}
