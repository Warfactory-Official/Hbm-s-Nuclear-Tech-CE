package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.recipes.CokerRecipes;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

import java.util.Collections;

import static com.hbm.inventory.recipes.CokerRecipes.recipes;

/**
 * Dedicated GroovyScript integration for the Coker (com.hbm.inventory.recipes.CokerRecipes).
 * Exposed as mods.hbm.coker. Keyed entirely by a single input FluidType - one input fluid "recipe"
 * per FluidType, no items in, an mB amount consumed per operation, one item output and one optional
 * fluid byproduct.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class Coker extends VirtualizedRegistry<Tuple.Pair<FluidType, Tuple.Triplet<Integer, ItemStack, FluidStack>>> {

    public Coker() {
        super(Collections.singletonList("coker"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(Tuple.Pair<FluidType, Tuple.Triplet<Integer, ItemStack, FluidStack>> entry) {
        recipes.put(entry.getKey(), entry.getValue());
        this.addScripted(entry);
    }

    private void removeRecipe(Tuple.Pair<FluidType, Tuple.Triplet<Integer, ItemStack, FluidStack>> entry) {
        recipes.remove(entry.getKey());
        this.addBackup(entry);
    }

    public void removeByInput(FluidType type) {
        Tuple.Triplet<Integer, ItemStack, FluidStack> removed = recipes.get(type);
        if (removed == null) return;
        this.removeRecipe(new Tuple.Pair<>(type, removed));
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public int size() {
        return recipes.size();
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Pair<FluidType, Tuple.Triplet<Integer, ItemStack, FluidStack>>> {

        private FluidType inputType;
        private int inputAmount;
        private ItemStack outputItem;
        private FluidStack byproduct;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_coker_";
        }

        /** The fluid this recipe keys on, and the mB consumed from it per operation. */
        public RecipeBuilder input(FluidStack input) {
            this.inputType = input.type;
            this.inputAmount = input.fill;
            return this;
        }

        public RecipeBuilder outputItem(ItemStack item) {
            this.outputItem = item;
            return this;
        }

        public RecipeBuilder byproduct(FluidStack fluid) {
            this.byproduct = fluid;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Coker recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.inputType == null) {
                msg.add("Coker recipe needs an input(...) fluid");
            }
            if (this.outputItem == null && this.byproduct == null) {
                msg.add("Coker recipe needs an outputItem(...) and/or a byproduct(...)");
            }
        }

        @Override
        public Tuple.Pair<FluidType, Tuple.Triplet<Integer, ItemStack, FluidStack>> register() {
            if (!this.validate()) return null;

            Tuple.Pair<FluidType, Tuple.Triplet<Integer, ItemStack, FluidStack>> entry =
                    new Tuple.Pair<>(this.inputType, new Tuple.Triplet<>(this.inputAmount, this.outputItem, this.byproduct));
            HbmGroovyPropertyContainer.COKER.addRecipe(entry);
            return entry;
        }
    }
}
