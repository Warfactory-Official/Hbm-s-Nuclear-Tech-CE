package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.RockMillRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class RockMillRecipeHandler extends JEIGenericRecipeHandler {

    public RockMillRecipeHandler(IGuiHelper helper) {
        super(helper, JEIConfig.ROCKMILL, ModBlocks.machine_rockmill.getTranslationKey(), RockMillRecipes.INSTANCE, new ItemStack(ModBlocks.machine_rockmill));
    }

}
