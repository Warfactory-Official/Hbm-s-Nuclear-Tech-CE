package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.integration.groovy.util.IngredientUtils;
import com.hbm.inventory.RecipesCommon;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

import static com.hbm.inventory.recipes.ShredderRecipes.jeiShredderRecipes;
import static com.hbm.inventory.recipes.ShredderRecipes.shredderRecipes;

@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class Shredder extends VirtualizedRegistry<Tuple.Pair<RecipesCommon.ComparableStack, ItemStack>> {
    @Override
    public void onReload() {
        jeiShredderRecipes = null;
        removeScripted().forEach(this::addRecipe);
        restoreFromBackup().forEach(this::removeRecipe);
    }

    private void removeRecipe(Tuple.Pair<RecipesCommon.ComparableStack, ItemStack> pair) {
        shredderRecipes.remove(pair.getKey());
        this.addBackup(pair);
    }

    private void removeRecipe(ItemStack input) {
        RecipesCommon.ComparableStack in = new RecipesCommon.ComparableStack(input);
        ItemStack out = shredderRecipes.get(in);
        this.removeRecipe(new Tuple.Pair<>(in, out));
    }

    private void removeAll() {
        for(RecipesCommon.ComparableStack stack:shredderRecipes.keySet()){
            ItemStack out = shredderRecipes.get(stack);
            this.removeRecipe(new Tuple.Pair<>(stack, out));
        }
    }

    private void addRecipe(Tuple.Pair<RecipesCommon.ComparableStack, ItemStack> pair) {
        shredderRecipes.put(pair.getKey(), pair.getValue());
        this.addScripted(pair);
    }

    public RecipeBuilder recipeBuilder(){
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Pair<RecipesCommon.ComparableStack, ItemStack>> {

        @Override
        public String getErrorMsg() {
            return "Error adding NTM Shredder Recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            this.validateItems(msg, 1, 1, 1, 1);
        }

        @Override
        public Tuple.Pair<RecipesCommon.ComparableStack, ItemStack> register() {
            if (!this.validate()) {
                return null;
            }
            Tuple.Pair<RecipesCommon.ComparableStack, ItemStack> recipe = new Tuple.Pair<>(IngredientUtils.convertIngredient2ComparableStack(this.input.get(0)), this.output.get(0));
            HbmGroovyPropertyContainer.SHREDDER.addRecipe(recipe);
            return recipe;
        }
    }

}
