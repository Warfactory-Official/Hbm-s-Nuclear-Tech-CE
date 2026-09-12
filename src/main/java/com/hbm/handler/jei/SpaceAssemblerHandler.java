package com.hbm.handler.jei;

import com.hbm.inventory.recipes.SpaceAssemblerRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemSatellite;
import com.hbm.items.machine.ItemSatellite.EnumSatType;
import mezz.jei.api.IGuiHelper;

public class SpaceAssemblerHandler extends JEIGenericRecipeHandler {

    public SpaceAssemblerHandler(IGuiHelper helper) {
        super(helper, JEIConfig.SPACE_ASSEMBLER, ModItems.satellite.getTranslationKey(ItemSatellite.make(EnumSatType.SCIENCE_ASSEMBLER)),
                SpaceAssemblerRecipes.INSTANCE, ItemSatellite.make(EnumSatType.SCIENCE_ASSEMBLER));
    }
}
