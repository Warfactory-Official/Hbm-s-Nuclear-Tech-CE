package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.ArcWelderRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArcWelderRecipeHandler extends JEIUniversalHandler {

    public ArcWelderRecipeHandler(IGuiHelper helper) {
        super(helper, JEIConfig.ARC_WELDER, ModBlocks.machine_arc_welder.getTranslationKey(), new ItemStack[]{new ItemStack(ModBlocks.machine_arc_welder)}, ArcWelderRecipes.getRecipes());
    }

    @Override
    protected void buildRecipes(HashMap<Object, Object> recipeMap, ItemStack[] machines) {
        for (Map.Entry<Object, Object> entry : recipeMap.entrySet()) {
            List<List<ItemStack>> inputs = extractInputLists(entry.getKey());
            ItemStack[] outputs = extractOutput(entry.getValue());
            if (inputs.isEmpty() || outputs.length == 0) {
                continue;
            }

            ArcWelderRecipes.ArcWelderRecipe arc = findMatchingRecipe(entry.getKey(), outputs[0]);
            int duration = arc != null ? arc.duration : 0;
            int consumption = arc != null ? (int) arc.consumption : 0;

            recipes.add(new JeiRecipes.ArcWelderRecipe(inputs, outputs, machines, duration, consumption, arc != null));
        }
    }

    private ArcWelderRecipes.ArcWelderRecipe findMatchingRecipe(Object originalInput, ItemStack output) {
        if (output == null || output.isEmpty()) {
            return null;
        }

        Object[] originalArray = originalInput instanceof Object[] ? (Object[]) originalInput : null;

        for (ArcWelderRecipes.ArcWelderRecipe recipe : ArcWelderRecipes.recipes) {
            if (!ItemStack.areItemStacksEqual(recipe.output, output)) {
                continue;
            }

            if (originalArray == null) {
                return recipe;
            }

            int fluidOffset = recipe.fluid == null ? 0 : 1;
            if (originalArray.length - fluidOffset != recipe.ingredients.length) {
                continue;
            }

            boolean matches = true;
            for (int i = 0; i < recipe.ingredients.length; i++) {
                if (recipe.ingredients[i] != originalArray[i]) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                return recipe;
            }
        }
        return null;
    }
}
