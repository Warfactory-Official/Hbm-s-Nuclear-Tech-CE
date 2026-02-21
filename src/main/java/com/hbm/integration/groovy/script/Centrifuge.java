package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.recipes.CentrifugeRecipes;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class Centrifuge extends VirtualizedRegistry<Tuple.Pair<RecipesCommon.AStack, ItemStack[]>> {
    @Override
    public void onReload() {
        this.removeScripted().forEach(this::remove);
        this.restoreFromBackup().forEach(this::addRecipe);
    }

    public void removeAll(){

        for(RecipesCommon.AStack key:CentrifugeRecipes.recipes.keySet()) {
            ItemStack[] stackarray = CentrifugeRecipes.recipes.get(key);
            Tuple.Pair<RecipesCommon.AStack, ItemStack[]> pair = new Tuple.Pair<>(key, stackarray);
            addBackup(pair);
            remove(pair);
        }
    }

    public void remove(Tuple.Pair<RecipesCommon.AStack, ItemStack[]> pair){
        CentrifugeRecipes.removeRecipe(pair.getKey());
        this.addBackup(pair);
    }

    public void remove(RecipesCommon.AStack ingredient){
        ItemStack[] out = CentrifugeRecipes.getOutput(ingredient.getStack());
        this.remove(new Tuple.Pair<>(ingredient, out));
    }

    public void addRecipe(Tuple.Pair<RecipesCommon.AStack, ItemStack[]> pair){
        CentrifugeRecipes.addRecipe(pair.getKey(), pair.getValue());
        this.addScripted(pair);
    }

    public void addRecipe(IIngredient input, ItemStack... stacks){
        for(ItemStack stack: input.getMatchingStacks()){
            Tuple.Pair<RecipesCommon.AStack, ItemStack[]> pair = new Tuple.Pair<>(new RecipesCommon.ComparableStack(stack), stacks);
            addScripted(pair);
        }
    }

}
