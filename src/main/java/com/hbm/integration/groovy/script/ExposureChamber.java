package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.integration.groovy.util.IngredientUtils;
import com.hbm.inventory.recipes.ExposureChamberRecipes;

import java.util.Collections;

import static com.hbm.inventory.recipes.ExposureChamberRecipes.recipes;

/**
 * Dedicated GroovyScript integration for the Exposure Chamber (com.hbm.inventory.recipes.ExposureChamberRecipes).
 * Exposed as mods.hbm.exposureChamber. Two single-item ingredients (particle + regular item) and one
 * item output, no duration/power.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class ExposureChamber extends VirtualizedRegistry<ExposureChamberRecipes.ExposureChamberRecipe> {

    public ExposureChamber() {
        super(Collections.singletonList("exposureChamber"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(ExposureChamberRecipes.ExposureChamberRecipe recipe) {
        recipes.add(recipe);
        this.addScripted(recipe);
    }

    private void removeRecipe(ExposureChamberRecipes.ExposureChamberRecipe recipe) {
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

    public static class RecipeBuilder extends AbstractRecipeBuilder<ExposureChamberRecipes.ExposureChamberRecipe> {

        private IIngredient particle;
        private IIngredient ingredient;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_exposurechamber_";
        }

        public RecipeBuilder particle(IIngredient particle) {
            this.particle = particle;
            return this;
        }

        public RecipeBuilder ingredient(IIngredient ingredient) {
            this.ingredient = ingredient;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Exposure Chamber recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.output.isEmpty()) {
                msg.add("Exposure Chamber recipe needs an output");
            }
            if (this.particle == null) {
                msg.add("Exposure Chamber recipe needs a particle(...)");
            }
            if (this.ingredient == null) {
                msg.add("Exposure Chamber recipe needs an ingredient(...)");
            }
        }

        @Override
        public ExposureChamberRecipes.ExposureChamberRecipe register() {
            if (!this.validate()) return null;

            ExposureChamberRecipes.ExposureChamberRecipe recipe = new ExposureChamberRecipes.ExposureChamberRecipe(
                    IngredientUtils.convertIngredient2Astack(this.particle),
                    IngredientUtils.convertIngredient2Astack(this.ingredient),
                    this.output.get(0));
            HbmGroovyPropertyContainer.EXPOSURECHAMBER.addRecipe(recipe);
            return recipe;
        }
    }
}
