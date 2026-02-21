package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.recipes.SILEXRecipes;
import com.hbm.items.machine.ItemFELCrystal;
import com.hbm.util.Tuple;
import com.hbm.util.WeightedRandomObject;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.hbm.inventory.recipes.SILEXRecipes.recipes;

@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class SILEX extends VirtualizedRegistry<Tuple.Pair<Object, SILEXRecipes.SILEXRecipe>> {
    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    public void removeAll(){
        for(Object key:recipes.keySet()){
            this.removeRecipe(new Tuple.Pair<>(key, recipes.get(key)));
        }
    }

    public void removeRecipe(IIngredient ingredient){
        for(ItemStack stack:ingredient.getMatchingStacks()){
            removeRecipe(stack);
        }
    }

    public void removeRecipe(ItemStack stack){
        RecipesCommon.ComparableStack stack1 = new RecipesCommon.ComparableStack(stack);
        SILEXRecipes.SILEXRecipe recipe = recipes.get(stack1);
        Tuple.Pair<Object, SILEXRecipes.SILEXRecipe> recipesPair = new Tuple.Pair<>(stack1, recipe);
        removeRecipe(recipesPair);
    }

    public void removeRecipe(Tuple.Pair<Object, SILEXRecipes.SILEXRecipe> pair){
        recipes.remove(pair.getKey());
        this.addBackup(pair);
    }

    public void addRecipe(Tuple.Pair<Object, SILEXRecipes.SILEXRecipe> recipe){
        recipes.put(recipe.getKey(), recipe.getValue());
        this.addScripted(recipe);
    }

    public void addRecipe(IIngredient ingredient, SILEXRecipes.SILEXRecipe recipe){
        for(ItemStack stack:ingredient.getMatchingStacks()){
            RecipesCommon.ComparableStack input = new RecipesCommon.ComparableStack(stack);
            recipes.put(input, recipe);
        }
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Pair<Object, SILEXRecipes.SILEXRecipe>>{

        public int fluidProduced = 0;
        public int fluidConsumed = 0;
        public ItemFELCrystal.EnumWavelengths laserStrength = ItemFELCrystal.EnumWavelengths.NULL;
        public List<WeightedRandomObject> outputs = new ArrayList<>();

        public RecipeBuilder fluidProduced(int value){
            this.fluidProduced = value;
            return this;
        }

        public RecipeBuilder fluidConsumed(int value){
            this.fluidConsumed = value;
            return this;
        }

        public RecipeBuilder WaveLengthIR(){
            this.laserStrength = ItemFELCrystal.EnumWavelengths.IR;
            return this;
        }

        public RecipeBuilder WaveLengthVisible(){
            this.laserStrength = ItemFELCrystal.EnumWavelengths.VISIBLE;
            return this;
        }

        public RecipeBuilder WaveLengthUV(){
            this.laserStrength = ItemFELCrystal.EnumWavelengths.UV;
            return this;
        }

        public RecipeBuilder WaveLengthGamma(){
            this.laserStrength = ItemFELCrystal.EnumWavelengths.GAMMA;
            return this;
        }

        public RecipeBuilder WaveLengthDRX(){
            this.laserStrength = ItemFELCrystal.EnumWavelengths.DRX;
            return this;
        }

        public RecipeBuilder addOutput(ItemStack stack, int weight){
            this.outputs.add(new WeightedRandomObject(stack, weight));
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding SILEX Recipes";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            this.validateItems(msg, 1, 1, 1, 1);
            msg.add(this.outputs.isEmpty(), "outputs can't be empty!");
            msg.add(this.fluidProduced < 0 || this.fluidProduced > 16000, "SILEX Recipe's output solution must be 0-16k but got {}mb.", this.fluidProduced);
            msg.add(this.fluidConsumed < 0 || this.fluidConsumed > 16000, "SILEX Recipe's input solution must be 0-16k but got {}mb.", this.fluidConsumed);
        }

        @Override
        public Tuple.Pair<Object, SILEXRecipes.SILEXRecipe> register() {
            if (!this.validate()) {
                return null;
            }

            Tuple.Pair<Object, SILEXRecipes.SILEXRecipe> recipe = new Tuple.Pair<>(this.input.get(0), new SILEXRecipes.SILEXRecipe(this.fluidProduced, this.fluidConsumed, this.laserStrength).addOutAll(this.outputs));
            HbmGroovyPropertyContainer.SILEX.addRecipe(recipe);
            return recipe;
        }
    }

}
