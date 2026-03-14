package com.hbm.handler.jei;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.jei.JeiRecipes.JeiUniversalRecipe;
import com.hbm.inventory.recipes.AnnihilatorRecipes;
import com.hbm.items.ModItems;

import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class AnnihilatorHandler extends JEIUniversalHandler {

    public AnnihilatorHandler(IGuiHelper helper) {
        super(helper, JEIConfig.ANNIHILATING, ModBlocks.machine_annihilator.getTranslationKey(), new ItemStack[]{new ItemStack(ModBlocks.machine_annihilator)}, AnnihilatorRecipes.getRecipes());
    }

    @Override
    protected void buildRecipes(HashMap<Object, Object> recipeMap, ItemStack[] machines) {
        for (Map.Entry<Object, Object> entry : recipeMap.entrySet()) {
            List<List<ItemStack>> inputs = extractInputLists(entry.getKey());
            ItemStack[] outputs = extractOutput(entry.getValue());

            boolean hasSecret = false;
            for (List<ItemStack> list : inputs) {
                for (ItemStack stack : list) {
                    if (!stack.isEmpty() && stack.getItem() == ModItems.item_secret) {
                        hasSecret = true;
                        break;
                    }
                }
                if (hasSecret) break;
            }

            if (!hasSecret) {
                for (ItemStack stack : outputs) {
                    if (!stack.isEmpty() && stack.getItem() == ModItems.item_secret) {
                        hasSecret = true;
                        break;
                    }
                }
            }

            if (!hasSecret && !inputs.isEmpty() && outputs.length > 0) {
                recipes.add(new JeiUniversalRecipe(inputs, outputs, machines));
            }
        }
    }
}