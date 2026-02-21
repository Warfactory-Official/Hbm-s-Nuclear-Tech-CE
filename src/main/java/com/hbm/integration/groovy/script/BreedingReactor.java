package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.recipes.BreederRecipes;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class BreedingReactor extends VirtualizedRegistry<Tuple.Pair<RecipesCommon.ComparableStack, BreederRecipes.BreederRecipe>> {
    @Override
    public void onReload() {
        this.removeScripted().forEach(this::removeRecipe);
        this.restoreFromBackup().forEach(this::addRecipe);
    }

    public void removeRecipe(Tuple.Pair<RecipesCommon.ComparableStack, BreederRecipes.BreederRecipe> recipe){
        BreederRecipes.removeRecipe(recipe.getKey());
        this.addBackup(recipe);
    }

    public void removeRecipe(ItemStack input){
        BreederRecipes.BreederRecipe recipe = BreederRecipes.getOutput(input);
        if(recipe == null){
            return;
        }
        Tuple.Pair<RecipesCommon.ComparableStack, BreederRecipes.BreederRecipe> pair = new Tuple.Pair<>(new RecipesCommon.ComparableStack(input), recipe);
        this.addRecipe(pair);
        this.addBackup(pair);
    }

    public void addRecipe(Tuple.Pair<RecipesCommon.ComparableStack, BreederRecipes.BreederRecipe> recipe){
        BreederRecipes.addRecipe(recipe.getKey(), recipe.getValue().output, recipe.getValue().flux);
        this.addScripted(recipe);
    }

    public void addRecipe(ItemStack output, ItemStack ingredient, int heatlevel){
        addRecipe(new Tuple.Pair<>(new RecipesCommon.ComparableStack(output), new BreederRecipes.BreederRecipe(ingredient, heatlevel)));
    }

    public void addRecipe(ItemStack output, IIngredient ingredient, int heatlevel){
        for(ItemStack stack:ingredient.getMatchingStacks()){
            addRecipe(output, stack, heatlevel);
        }
    }

    public RecipeBuilder recipeBuilder(){
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Pair<RecipesCommon.ComparableStack, BreederRecipes.BreederRecipe>>{

        private int heatLvl = 1;

        public void heatLevel(int heatLvl) {
            this.heatLvl = heatLvl;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Breeder Reactor Recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            this.validateItems(msg, 1, 1, 1, 1);
            msg.add(this.heatLvl < 1 || this.heatLvl > 4, "Breeding heat needs to be between 1-4, but it was {}.", this.heatLvl);
        }

        @Override
        public Tuple.Pair<RecipesCommon.ComparableStack, BreederRecipes.BreederRecipe> register() {
            Tuple.Pair<RecipesCommon.ComparableStack, BreederRecipes.BreederRecipe> recipe = new Tuple.Pair<>(new RecipesCommon.ComparableStack(this.input.get(0).getMatchingStacks()[0]), new BreederRecipes.BreederRecipe(this.output.get(0),this.heatLvl));
            HbmGroovyPropertyContainer.BREEDINGREACTOR.addRecipe(recipe);
            return recipe;
        }
    }

}
