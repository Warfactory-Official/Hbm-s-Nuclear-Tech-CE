package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.SuperComputerRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class SuperComputerRecipeHandler extends JEIGenericRecipeHandler {

    public SuperComputerRecipeHandler(IGuiHelper helper) {
        super(helper, JEIConfig.SUPERCOMPUTER, ModBlocks.machine_supercomputer.getTranslationKey(), SuperComputerRecipes.INSTANCE, new ItemStack(ModBlocks.machine_supercomputer));
    }

}
