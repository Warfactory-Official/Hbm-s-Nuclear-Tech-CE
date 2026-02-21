package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.items.special.ItemBedrockOreNew;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OreSlopperHandler extends JEIUniversalHandler{

    public OreSlopperHandler(IGuiHelper helper) {
        super(helper, JEIConfig.ORE_SLOPPER, ModBlocks.machine_ore_slopper.getTranslationKey(), new ItemStack[] {new ItemStack(ModBlocks.machine_ore_slopper)}, getSlopperRecipes());
    }

    private static HashMap<Object, Object> getSlopperRecipes() {
        HashMap<Object, Object> recipes = new HashMap<>();
        List<ItemStack> outputs = new ArrayList<>();
        for(ItemBedrockOreNew.BedrockOreType type : ItemBedrockOreNew.BedrockOreType.VALUES) outputs.add(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.BASE, type));
        outputs.add(ItemFluidIcon.make(Fluids.SLOP, 1000));
        recipes.put(new ItemStack[] {ItemFluidIcon.make(Fluids.WATER, 1000), new ItemStack(ModItems.bedrock_ore_base)}, outputs.toArray(new ItemStack[0]));
        return recipes;
    }
}
