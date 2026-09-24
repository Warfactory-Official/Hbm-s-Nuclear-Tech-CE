package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.ingredient.OreDictIngredient;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

import java.util.Collections;

import static com.hbm.inventory.recipes.CrystallizerRecipes.recipes;

/**
 * Dedicated GroovyScript integration for the Crystallizer (com.hbm.inventory.recipes.CrystallizerRecipes).
 * Exposed as mods.hbm.crystallizer. Keyed by an input item/ore-dict entry plus an acid FluidType,
 * consumes a given item count and acid mB amount, produces one item output over a duration.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class Crystallizer extends VirtualizedRegistry<Tuple.Pair<Tuple.Pair<Object, FluidType>, CrystallizerRecipes.CrystallizerRecipe>> {

    public Crystallizer() {
        super(Collections.singletonList("crystallizer"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(Tuple.Pair<Tuple.Pair<Object, FluidType>, CrystallizerRecipes.CrystallizerRecipe> entry) {
        recipes.put(entry.getKey(), entry.getValue());
        this.addScripted(entry);
    }

    private void removeRecipe(Tuple.Pair<Tuple.Pair<Object, FluidType>, CrystallizerRecipes.CrystallizerRecipe> entry) {
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

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Pair<Tuple.Pair<Object, FluidType>, CrystallizerRecipes.CrystallizerRecipe>> {

        private IIngredient input;
        private int itemAmount = 1;
        private FluidStack acid;
        private ItemStack output;
        private int duration = 100;
        private float productivity = 0F;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_crystallizer_";
        }

        /** The item/ore-dict this recipe keys on and the count consumed per operation. */
        public RecipeBuilder input(IIngredient input, int count) {
            this.input = input;
            this.itemAmount = count;
            return this;
        }

        /** Keys on the fluid's type; the fill amount is the acid mB consumed per operation. */
        public RecipeBuilder acid(FluidStack acid) {
            this.acid = acid;
            return this;
        }

        public RecipeBuilder output(ItemStack output) {
            this.output = output;
            return this;
        }

        public RecipeBuilder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public RecipeBuilder productivity(float productivity) {
            this.productivity = productivity;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Crystallizer recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.input == null) {
                msg.add("Crystallizer recipe needs an input(...)");
            }
            if (this.acid == null) {
                msg.add("Crystallizer recipe needs an acid(...) fluid");
            }
            if (this.output == null) {
                msg.add("Crystallizer recipe needs an output(...)");
            }
        }

        @Override
        public Tuple.Pair<Tuple.Pair<Object, FluidType>, CrystallizerRecipes.CrystallizerRecipe> register() {
            if (!this.validate()) return null;

            // the lookup key's own stacksize is always forced to 1 (matching the JSON-loaded behavior) -
            // the actual amount consumed is tracked separately via itemAmount/setReq(...) below.
            Object key;
            if (this.input instanceof OreDictIngredient) {
                key = ((OreDictIngredient) this.input).getOreDict();
            } else {
                RecipesCommon.AStack stack = com.hbm.integration.groovy.util.IngredientUtils.convertIngredient2Astack(this.input);
                stack.stacksize = 1;
                key = stack;
            }

            CrystallizerRecipes.CrystallizerRecipe recipe = new CrystallizerRecipes.CrystallizerRecipe(this.output, this.duration)
                    .setReq(this.itemAmount)
                    .prod(this.productivity);
            recipe.acidAmount = this.acid.fill;

            Tuple.Pair<Object, FluidType> mapKey = new Tuple.Pair<>(key, this.acid.type);
            Tuple.Pair<Tuple.Pair<Object, FluidType>, CrystallizerRecipes.CrystallizerRecipe> entry = new Tuple.Pair<>(mapKey, recipe);
            HbmGroovyPropertyContainer.CRYSTALLIZER.addRecipe(entry);
            return entry;
        }
    }
}
