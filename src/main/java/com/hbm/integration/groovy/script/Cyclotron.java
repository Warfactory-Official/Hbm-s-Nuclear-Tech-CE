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
import com.hbm.inventory.recipes.CyclotronRecipes;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

import java.util.Collections;

import static com.hbm.inventory.recipes.CyclotronRecipes.recipes;

/**
 * Dedicated GroovyScript integration for the Cyclotron (com.hbm.inventory.recipes.CyclotronRecipes).
 * Exposed as mods.hbm.cyclotron. Keyed by a particle item + a regular item/ore-dict input, produces
 * an item output plus an antimatter yield.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class Cyclotron extends VirtualizedRegistry<Tuple.Pair<Tuple.Pair<RecipesCommon.ComparableStack, RecipesCommon.AStack>, Tuple.Pair<ItemStack, Integer>>> {

    public Cyclotron() {
        super(Collections.singletonList("cyclotron"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(Tuple.Pair<Tuple.Pair<RecipesCommon.ComparableStack, RecipesCommon.AStack>, Tuple.Pair<ItemStack, Integer>> entry) {
        recipes.put(entry.getKey(), entry.getValue());
        this.addScripted(entry);
    }

    private void removeRecipe(Tuple.Pair<Tuple.Pair<RecipesCommon.ComparableStack, RecipesCommon.AStack>, Tuple.Pair<ItemStack, Integer>> entry) {
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

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Pair<Tuple.Pair<RecipesCommon.ComparableStack, RecipesCommon.AStack>, Tuple.Pair<ItemStack, Integer>>> {

        private ItemStack particle;
        private IIngredient input;
        private int antimatter = 0;
        private ItemStack output;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_cyclotron_";
        }

        public RecipeBuilder particle(ItemStack particle) {
            this.particle = particle;
            return this;
        }

        public RecipeBuilder input(IIngredient input) {
            this.input = input;
            return this;
        }

        public RecipeBuilder antimatter(int antimatter) {
            this.antimatter = antimatter;
            return this;
        }

        public RecipeBuilder output(ItemStack output) {
            this.output = output;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Cyclotron recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.particle == null) {
                msg.add("Cyclotron recipe needs a particle(...)");
            }
            if (this.input == null) {
                msg.add("Cyclotron recipe needs an input(...)");
            }
            if (this.output == null) {
                msg.add("Cyclotron recipe needs an output(...)");
            }
        }

        @Override
        public Tuple.Pair<Tuple.Pair<RecipesCommon.ComparableStack, RecipesCommon.AStack>, Tuple.Pair<ItemStack, Integer>> register() {
            if (!this.validate()) return null;

            Tuple.Pair<RecipesCommon.ComparableStack, RecipesCommon.AStack> key =
                    new Tuple.Pair<>(new RecipesCommon.ComparableStack(this.particle), IngredientUtils.convertIngredient2Astack(this.input));
            Tuple.Pair<ItemStack, Integer> value = new Tuple.Pair<>(this.output, this.antimatter);
            Tuple.Pair<Tuple.Pair<RecipesCommon.ComparableStack, RecipesCommon.AStack>, Tuple.Pair<ItemStack, Integer>> entry = new Tuple.Pair<>(key, value);
            HbmGroovyPropertyContainer.CYCLOTRON.addRecipe(entry);
            return entry;
        }
    }
}
