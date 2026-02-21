package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.recipes.WasteDrumRecipes;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

import static com.hbm.inventory.recipes.StorageDrumRecipes.outputs;
import static com.hbm.inventory.recipes.WasteDrumRecipes.recipes;

@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class WasteDrum extends VirtualizedRegistry<Tuple.Pair<RecipesCommon.ComparableStack, ItemStack>> {

    @Override
    public void onReload() {
        this.removeScripted().forEach(this::removeRecipe);
        this.restoreFromBackup().forEach(this::addRecipe);
    }

    public void removeAll(){
        recipes.clear();
        outputs.clear();
    }

    public void removeRecipe(RecipesCommon.ComparableStack input, ItemStack output){
        WasteDrumRecipes.removeRecipe(input, output);
    }

    public void removeRecipe(Tuple.Pair<RecipesCommon.ComparableStack, ItemStack> pair){
        WasteDrumRecipes.removeRecipe(pair.getKey(), pair.getValue());
        this.addBackup(pair);
    }

    public void addRecipe(Tuple.Pair<RecipesCommon.ComparableStack, ItemStack> pair){
        WasteDrumRecipes.addRecipe(pair.getKey(), pair.getValue());
        this.addScripted(pair);
    }

    public void addRecipe(IIngredient ingredient, ItemStack output){
        for(ItemStack stack:ingredient.getMatchingStacks()) {
            HbmGroovyPropertyContainer.WASTEDRUM.addRecipe(new Tuple.Pair<>(new RecipesCommon.ComparableStack(stack), output));
        }
    }

}
