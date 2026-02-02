package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.recipes.OutgasserRecipes;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

import java.util.Iterator;

import static com.hbm.inventory.recipes.OutgasserRecipes.recipes;

@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class IrradiationChannel extends VirtualizedRegistry<Tuple.Pair<RecipesCommon.AStack, OutgasserRecipes.OutgasserRecipe>> {
    @Override
    public void onReload() {
        this.removeScripted().forEach(this::removeRecipe);
        this.restoreFromBackup().forEach(this::addRecipe);
    }

    public void removeRecipe(Tuple.Pair<RecipesCommon.AStack, OutgasserRecipes.OutgasserRecipe> pair){
        recipes.remove(pair.getKey());
        this.addBackup(pair);
    }

    public void removeAll(){
        for (Iterator<RecipesCommon.AStack> it = recipes.keySet().iterator(); it.hasNext(); ) {
            RecipesCommon.AStack stack = it.next();
            OutgasserRecipes.OutgasserRecipe param = recipes.get(stack);
            this.addBackup(new Tuple.Pair<>(stack, param));
            it.remove();
        }
    }

    public void removeRecipe(IIngredient ingredient){
        for(ItemStack stack:ingredient.getMatchingStacks()){
            removeRecipe(stack);
        }
    }

    public void removeRecipe(ItemStack stack){
        RecipesCommon.AStack comparableStack = new RecipesCommon.ComparableStack(stack);
        OutgasserRecipes.OutgasserRecipe param = recipes.get(comparableStack);
        recipes.remove(comparableStack);
        this.addBackup(new Tuple.Pair<>(comparableStack, param));
    }

    public void addRecipe(ItemStack input, ItemStack outItem, FluidStack outFluid){
        addRecipe(input, outItem, outFluid, false);
    }

    public void addRecipe(ItemStack input, ItemStack outItem, FluidStack outFluid, boolean fusionOnly){
        RecipesCommon.ComparableStack stack = new RecipesCommon.ComparableStack(input);
        FluidStack output = outFluid == null ? null : new FluidStack(outFluid.type, outFluid.fill);
        OutgasserRecipes.OutgasserRecipe recipe = new OutgasserRecipes.OutgasserRecipe(outItem, output);
        if (fusionOnly) {
            recipe.fusionOnly();
        }
        addRecipe(new Tuple.Pair<>(stack, recipe));
    }

    public void addRecipe(ItemStack input, FluidStack out){
        addRecipe(input, null, out);
    }

    public void addRecipe(ItemStack input, ItemStack out){
        addRecipe(input, out, null);
    }

    public void addRecipe(Tuple.Pair<RecipesCommon.AStack, OutgasserRecipes.OutgasserRecipe> pair){
        recipes.put(pair.getKey(), pair.getValue());
        this.addScripted(pair);
    }
}
