package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.ingredient.OreDictIngredient;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.integration.groovy.util.IngredientUtils;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.recipes.LiquefactionRecipes;
import com.hbm.util.Tuple;

import java.util.Collections;

import static com.hbm.inventory.recipes.LiquefactionRecipes.recipes;

/**
 * Dedicated GroovyScript integration for the Liquefactor (com.hbm.inventory.recipes.LiquefactionRecipes).
 * Exposed as mods.hbm.liquefaction. Keyed by a single input item/ore-dict entry (stacksize is always
 * 1, per hbm's own note on this handler), produces one output fluid.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class Liquefaction extends VirtualizedRegistry<Tuple.Pair<Object, FluidStack>> {

    public Liquefaction() {
        super(Collections.singletonList("liquefaction"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(Tuple.Pair<Object, FluidStack> entry) {
        recipes.put(entry.getKey(), entry.getValue());
        this.addScripted(entry);
    }

    private void removeRecipe(Tuple.Pair<Object, FluidStack> entry) {
        recipes.remove(entry.getKey());
        this.addBackup(entry);
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public int size() {
        return recipes.size();
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Pair<Object, FluidStack>> {

        private IIngredient input;
        private FluidStack output;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_liquefaction_";
        }

        public RecipeBuilder input(IIngredient input) {
            this.input = input;
            return this;
        }

        public RecipeBuilder output(FluidStack output) {
            this.output = output;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Liquefactor recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.input == null) {
                msg.add("Liquefactor recipe needs an input(...)");
            }
            if (this.output == null) {
                msg.add("Liquefactor recipe needs an output(...) fluid");
            }
        }

        @Override
        public Tuple.Pair<Object, FluidStack> register() {
            if (!this.validate()) return null;

            Object key;
            if (this.input instanceof OreDictIngredient) {
                key = ((OreDictIngredient) this.input).getOreDict();
            } else {
                RecipesCommon.AStack stack = IngredientUtils.convertIngredient2Astack(this.input);
                key = stack instanceof RecipesCommon.ComparableStack ? ((RecipesCommon.ComparableStack) stack).makeSingular() : stack;
            }

            Tuple.Pair<Object, FluidStack> entry = new Tuple.Pair<>(key, this.output);
            HbmGroovyPropertyContainer.LIQUEFACTION.addRecipe(entry);
            return entry;
        }
    }
}
