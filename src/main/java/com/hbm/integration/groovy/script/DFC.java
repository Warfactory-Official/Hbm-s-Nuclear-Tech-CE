package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.inventory.RecipesCommon;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

import static com.hbm.inventory.recipes.DFCRecipes.dfcRecipes;

@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class DFC extends VirtualizedRegistry<Tuple.Pair<RecipesCommon.ComparableStack, Object[]>> {
    @Override
    public void onReload() {
        this.removeScripted().forEach(this::removeRecipe);
        this.restoreFromBackup().forEach(this::addRecipe);
    }

    public void removeRecipe(Tuple.Pair<RecipesCommon.ComparableStack, Object[]> pair){
        dfcRecipes.remove(pair.getKey());
        this.addBackup(pair);
    }

    public void removeRecipe(ItemStack stack){
        RecipesCommon.ComparableStack comparableStack = new RecipesCommon.ComparableStack(stack);
        Object[] values = dfcRecipes.get(comparableStack);
        removeRecipe(new Tuple.Pair<>(comparableStack, values));
    }

    public void addRecipe(IIngredient ingredient, ItemStack output, long flux) {
        for (ItemStack stack : ingredient.getMatchingStacks()) {
            addRecipe(stack, output, flux);
        }
    }

    public void addRecipe(ItemStack stack, ItemStack output, long flux) {
        RecipesCommon.ComparableStack comparableStack = new RecipesCommon.ComparableStack(stack);
        Object[] params = new Object[] {flux, output};
        addRecipe(new Tuple.Pair<>(comparableStack, params));
    }

    public void addRecipe(Tuple.Pair<RecipesCommon.ComparableStack, Object[]> pair) {
        dfcRecipes.put(pair.getKey(), pair.getValue());
        this.addScripted(pair);
    }




}
