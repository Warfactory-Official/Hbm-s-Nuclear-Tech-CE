package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.inventory.RecipesCommon;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.recipes.AnnihilatorRecipes;
import com.hbm.util.Tuple;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hbm.inventory.recipes.AnnihilatorRecipes.recipes;

/**
 * Dedicated GroovyScript integration for the Annihilator (com.hbm.inventory.recipes.AnnihilatorRecipes).
 * Exposed as mods.hbm.annihilator. Keyed by one of: an Item, a specific (item, meta) combo, a
 * FluidType, or an ore-dict name - use exactly one of key(...)'s overloads. Value is a list of
 * milestones (a matter-amount threshold and its ItemStack payout) added via milestone(...), in
 * ascending order of amount.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class Annihilator extends VirtualizedRegistry<Tuple.Pair<Object, AnnihilatorRecipes.AnnihilatorRecipe>> {

    public Annihilator() {
        super(Collections.singletonList("annihilator"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(Tuple.Pair<Object, AnnihilatorRecipes.AnnihilatorRecipe> entry) {
        recipes.put(entry.getKey(), entry.getValue());
        this.addScripted(entry);
    }

    private void removeRecipe(Tuple.Pair<Object, AnnihilatorRecipes.AnnihilatorRecipe> entry) {
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

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Pair<Object, AnnihilatorRecipes.AnnihilatorRecipe>> {

        private Object key;
        private final List<Tuple.Pair<BigInteger, ItemStack>> milestones = new ArrayList<>();

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_annihilator_";
        }

        public RecipeBuilder key(Item item) {
            this.key = item;
            return this;
        }

        public RecipeBuilder key(ItemStack stack) {
            this.key = new RecipesCommon.ComparableStack(stack.getItem(), 1, stack.getMetadata());
            return this;
        }

        public RecipeBuilder key(FluidType fluid) {
            this.key = fluid;
            return this;
        }

        /** An ore-dict name, e.g. "ingotSteel". */
        public RecipeBuilder key(String oreDict) {
            this.key = oreDict;
            return this;
        }

        public RecipeBuilder milestone(long amount, ItemStack payout) {
            return milestone(BigInteger.valueOf(amount), payout);
        }

        public RecipeBuilder milestone(BigInteger amount, ItemStack payout) {
            this.milestones.add(new Tuple.Pair<>(amount, payout));
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Annihilator recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.key == null) {
                msg.add("Annihilator recipe needs a key(...) - an Item, an (item, meta) stack, a FluidType, or an ore-dict name");
            }
            if (this.milestones.isEmpty()) {
                msg.add("Annihilator recipe needs at least one milestone(amount, payout)");
            }
        }

        @Override
        public Tuple.Pair<Object, AnnihilatorRecipes.AnnihilatorRecipe> register() {
            if (!this.validate()) return null;

            AnnihilatorRecipes.AnnihilatorRecipe recipe = new AnnihilatorRecipes.AnnihilatorRecipe(
                    this.milestones.toArray(new Tuple.Pair[0]));
            Tuple.Pair<Object, AnnihilatorRecipes.AnnihilatorRecipe> entry = new Tuple.Pair<>(this.key, recipe);
            HbmGroovyPropertyContainer.ANNIHILATOR.addRecipe(entry);
            return entry;
        }
    }
}
