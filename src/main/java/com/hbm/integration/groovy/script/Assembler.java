package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.integration.groovy.util.IngredientUtils;
import com.hbm.inventory.RecipesCommon;
import com.hbm.util.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hbm.inventory.recipes.AssemblerRecipes.*;

@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class Assembler extends VirtualizedRegistry<Tuple.Triplet<RecipesCommon.ComparableStack, RecipesCommon.AStack[], Integer>>  {
    @Override
    public void onReload() {
        removeScripted().forEach(recipe->{
            this.removeRecipebyOutput(recipe.getX());
        });
        restoreFromBackup().forEach(this::addRecipe);
        Collections.sort(recipeList);
    }

    public void addRecipe(Tuple.Triplet<RecipesCommon.ComparableStack, RecipesCommon.AStack[], Integer> recipe){
        makeRecipe(recipe.getX(), recipe.getY(), recipe.getZ());
        this.addScripted(recipe);
    }

    public void removeRecipebyOutput(RecipesCommon.ComparableStack output){
        AssemblerRecipe recipe = recipes.get(output);
        this.addBackup(new Tuple.Triplet<>(output,recipe.ingredients,recipe.time));
        recipes.remove(output);
        recipeList.remove(output);
    }

    public void removeAll(){
        for(RecipesCommon.ComparableStack stack:recipeList){
            AssemblerRecipe recipe = recipes.get(stack);
            this.addBackup(new Tuple.Triplet<>(stack,recipe.ingredients,recipe.time));
        }
        recipeList.clear();
        recipes.clear();
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Triplet<RecipesCommon.ComparableStack, RecipesCommon.AStack[], Integer>>{

        int time = 200;

        public RecipeBuilder time(int time){
            this.time = time;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding NTM Assembler Recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            this.validateItems(msg, 1, Integer.MAX_VALUE, 1, 1);
        }

        @Override
        public Tuple.Triplet<RecipesCommon.ComparableStack, RecipesCommon.AStack[], Integer> register() {
            if (!this.validate()) {
                return null;
            }
            List<RecipesCommon.AStack> list = new ArrayList<>();
            for(IIngredient ingredient:this.input){
                list.add(IngredientUtils.convertIngredient2Astack(ingredient));
            }
            Tuple.Triplet<RecipesCommon.ComparableStack, RecipesCommon.AStack[], Integer> recipe = new Tuple.Triplet<>(new RecipesCommon.ComparableStack(this.output.get(0)),  list.toArray(new RecipesCommon.AStack[0]), this.time);
            HbmGroovyPropertyContainer.ASSEMBLER.addRecipe(recipe);
            return recipe;
        }
    }

}
