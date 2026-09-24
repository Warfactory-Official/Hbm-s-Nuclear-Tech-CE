package com.hbm.integration.groovy.util;

import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.helper.ingredient.OreDictIngredient;
import com.hbm.inventory.RecipesCommon;
import net.minecraft.item.ItemStack;

public class IngredientUtils {

    // NOTE: ComparableStack(Item, int stacksize) hardcodes meta=0 (see RecipesCommon.ComparableStack) -
    // using it here silently dropped any metadata the groovy script set via item('modid:name', META),
    // e.g. item('hbm:plate_cast', 30) would register as plate_cast meta 0 instead of 30. Read the meta
    // off the actual matching ItemStack instead of just its Item, so it round-trips correctly.

    public static RecipesCommon.AStack convertIngredient2Astack(IIngredient ingredient){
        if(ingredient instanceof OreDictIngredient){
            return new RecipesCommon.OreDictStack(((OreDictIngredient) ingredient).getOreDict(), ingredient.getAmount());
        }
        ItemStack stack = ingredient.getMatchingStacks()[0];
        return new RecipesCommon.ComparableStack(stack.getItem(), ingredient.getAmount(), stack.getItemDamage());
    }

    public static RecipesCommon.ComparableStack convertIngredient2ComparableStack(IIngredient ingredient){
        ItemStack stack = ingredient.getMatchingStacks()[0];
        return new RecipesCommon.ComparableStack(stack.getItem(), ingredient.getAmount(), stack.getItemDamage());
    }
}
