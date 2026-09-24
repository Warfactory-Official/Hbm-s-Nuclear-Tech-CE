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
import com.hbm.inventory.recipes.CombinationRecipes;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

import java.util.Collections;

import static com.hbm.inventory.recipes.CombinationRecipes.recipes;

/**
 * Dedicated GroovyScript integration for the Combination Furnace (com.hbm.inventory.recipes.CombinationRecipes).
 * Exposed as mods.hbm.combination. Keyed by a single input item or ore-dict entry, produces an
 * optional item output and/or an optional fluid output (at least one).
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class Combination extends VirtualizedRegistry<Tuple.Pair<Object, Tuple.Pair<ItemStack, FluidStack>>> {

    public Combination() {
        super(Collections.singletonList("combination"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(Tuple.Pair<Object, Tuple.Pair<ItemStack, FluidStack>> entry) {
        recipes.put(entry.getKey(), entry.getValue());
        this.addScripted(entry);
    }

    private void removeRecipe(Tuple.Pair<Object, Tuple.Pair<ItemStack, FluidStack>> entry) {
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

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Pair<Object, Tuple.Pair<ItemStack, FluidStack>>> {

        private IIngredient input;
        private ItemStack outputItem;
        private FluidStack outputFluid;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_combination_";
        }

        public RecipeBuilder input(IIngredient input) {
            this.input = input;
            return this;
        }

        public RecipeBuilder outputItem(ItemStack output) {
            this.outputItem = output;
            return this;
        }

        public RecipeBuilder outputFluid(FluidStack output) {
            this.outputFluid = output;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Combination Furnace recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.input == null) {
                msg.add("Combination Furnace recipe needs an input(...)");
            }
            if (this.outputItem == null && this.outputFluid == null) {
                msg.add("Combination Furnace recipe needs an outputItem(...) and/or outputFluid(...)");
            }
        }

        @Override
        public Tuple.Pair<Object, Tuple.Pair<ItemStack, FluidStack>> register() {
            if (!this.validate()) return null;

            Object key;
            if (this.input instanceof OreDictIngredient) {
                key = ((OreDictIngredient) this.input).getOreDict();
            } else {
                RecipesCommon.AStack stack = IngredientUtils.convertIngredient2Astack(this.input);
                key = stack instanceof RecipesCommon.ComparableStack ? ((RecipesCommon.ComparableStack) stack).makeSingular() : stack;
            }

            Tuple.Pair<Object, Tuple.Pair<ItemStack, FluidStack>> entry =
                    new Tuple.Pair<>(key, new Tuple.Pair<>(this.outputItem, this.outputFluid));
            HbmGroovyPropertyContainer.COMBINATION.addRecipe(entry);
            return entry;
        }
    }
}
