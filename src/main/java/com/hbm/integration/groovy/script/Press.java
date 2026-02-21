package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.integration.groovy.util.IngredientUtils;
import com.hbm.inventory.RecipesCommon;
import com.hbm.items.machine.ItemStamp;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

import java.util.Collections;

import static com.hbm.inventory.recipes.AssemblerRecipes.recipeList;
import static com.hbm.inventory.recipes.PressRecipes.recipes;

@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class Press extends VirtualizedRegistry<Tuple.Pair<Tuple.Pair<RecipesCommon.AStack, ItemStamp.StampType>, ItemStack>> {
    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
        Collections.sort(recipeList);
    }

    private void addRecipe(Tuple.Pair<Tuple.Pair<RecipesCommon.AStack, ItemStamp.StampType>, ItemStack> pairItemStackPair) {
        recipes.put(pairItemStackPair.getKey(), pairItemStackPair.getValue());
        this.addScripted(pairItemStackPair);
    }

    private void removeRecipe(Tuple.Pair<Tuple.Pair<RecipesCommon.AStack, ItemStamp.StampType>, ItemStack> pair){
        recipes.remove(pair.getKey());
        this.addBackup(pair);
    }

    private void removeRecipebyOutput(ItemStack out){
        for(Tuple.Pair<RecipesCommon.AStack, ItemStamp.StampType> key: recipes.keySet()){
            ItemStack recipeout = recipes.get(key);
            if(recipeout == out){
                recipes.remove(key);
                this.addBackup(new Tuple.Pair<>(key, recipeout));
            }
        }
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Pair<Tuple.Pair<RecipesCommon.AStack, ItemStamp.StampType>, ItemStack>> {

        private ItemStamp.StampType type = ItemStamp.StampType.FLAT;

        public RecipeBuilder Flat(){
            this.type = ItemStamp.StampType.FLAT;
            return this;
        }

        public RecipeBuilder Plate(){
            this.type = ItemStamp.StampType.PLATE;
            return this;
        }

        public RecipeBuilder Wire(){
            this.type = ItemStamp.StampType.WIRE;
            return this;
        }

        public RecipeBuilder Circuit(){
            this.type = ItemStamp.StampType.CIRCUIT;
            return this;
        }

        public RecipeBuilder ThreeFiveSeven(){
            this.type = ItemStamp.StampType.C357;
            return this;
        }

        public RecipeBuilder Fourfour(){
            this.type = ItemStamp.StampType.C44;
            return this;
        }

        public RecipeBuilder Nine(){
            this.type = ItemStamp.StampType.C9;
            return this;
        }

        public RecipeBuilder Fivezero(){
            this.type = ItemStamp.StampType.C50;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding recipes for NTM Press";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            this.validateItems(msg, 1, 1, 1, 1);
        }

        @Override
        public  Tuple.Pair<Tuple.Pair<RecipesCommon.AStack, ItemStamp.StampType>, ItemStack> register() {
            if (!this.validate()) {
                return null;
            }
            Tuple.Pair<Tuple.Pair<RecipesCommon.AStack, ItemStamp.StampType>, ItemStack> recipe = new Tuple.Pair<>(new Tuple.Pair<>(IngredientUtils.convertIngredient2Astack(this.input.get(0)), this.type), this.output.get(0));
            HbmGroovyPropertyContainer.PRESS.addRecipe(recipe);
            return recipe;
        }
    }
}
