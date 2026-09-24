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
import com.hbm.inventory.recipes.ArcFurnaceRecipes;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

import java.util.Collections;

import static com.hbm.inventory.recipes.ArcFurnaceRecipes.occupiedSolid;
import static com.hbm.inventory.recipes.ArcFurnaceRecipes.recipeList;

/**
 * Dedicated GroovyScript integration for the Arc Furnace (com.hbm.inventory.recipes.ArcFurnaceRecipes).
 * Exposed as mods.hbm.arcFurnace.
 * <p>
 * NOTE: only the solid (item) output is supported here - the fluid/material output side of this
 * machine uses hbm's Mats.MaterialStack system (material + amount, not a plain ItemStack/FluidStack),
 * which needs its own dedicated DSL design. Same limitation as CrucibleRecipes, RotaryFurnaceRecipes
 * and ElectrolyserMetalRecipes, all skipped for the same reason.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class ArcFurnace extends VirtualizedRegistry<Tuple.Pair<RecipesCommon.AStack, ArcFurnaceRecipes.ArcFurnaceRecipe>> {

    public ArcFurnace() {
        super(Collections.singletonList("arcFurnace"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(Tuple.Pair<RecipesCommon.AStack, ArcFurnaceRecipes.ArcFurnaceRecipe> entry) {
        ArcFurnaceRecipes.register(entry.getKey(), entry.getValue());
        this.addScripted(entry);
    }

    private void removeRecipe(Tuple.Pair<RecipesCommon.AStack, ArcFurnaceRecipes.ArcFurnaceRecipe> entry) {
        recipeList.remove(entry);
        for (ItemStack stack : entry.getKey().extractForJEI()) {
            occupiedSolid.remove(new RecipesCommon.ComparableStack(stack));
        }
        this.addBackup(entry);
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public int size() {
        return recipeList.size();
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Pair<RecipesCommon.AStack, ArcFurnaceRecipes.ArcFurnaceRecipe>> {

        private IIngredient input;
        private ItemStack solidOutput;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_arcfurnace_";
        }

        public RecipeBuilder input(IIngredient input) {
            this.input = input;
            return this;
        }

        public RecipeBuilder solidOutput(ItemStack output) {
            this.solidOutput = output;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Arc Furnace recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.input == null) {
                msg.add("Arc Furnace recipe needs an input(...)");
            }
            if (this.solidOutput == null) {
                msg.add("Arc Furnace recipe needs a solidOutput(...)");
            }
        }

        @Override
        public Tuple.Pair<RecipesCommon.AStack, ArcFurnaceRecipes.ArcFurnaceRecipe> register() {
            if (!this.validate()) return null;

            ArcFurnaceRecipes.ArcFurnaceRecipe recipe = new ArcFurnaceRecipes.ArcFurnaceRecipe().solid(this.solidOutput);
            Tuple.Pair<RecipesCommon.AStack, ArcFurnaceRecipes.ArcFurnaceRecipe> entry =
                    new Tuple.Pair<>(IngredientUtils.convertIngredient2Astack(this.input), recipe);
            HbmGroovyPropertyContainer.ARCFURNACE.addRecipe(entry);
            return entry;
        }
    }
}
