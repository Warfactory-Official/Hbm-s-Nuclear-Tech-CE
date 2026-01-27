package com.hbm.crafting;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import net.minecraft.item.ItemStack;

import static com.hbm.inventory.OreDictManager.*;
import static com.hbm.main.CraftingManager.*;

/**
 * 1.12.2 Exclusive Recipes
 *
 * @author mlbv
 */
public class ExclusiveRecipes {
    public static void register() {
        registerShielding();
    }

    // additional rad-resistant blocks
    private static void registerShielding() {
        addRecipeAuto(new ItemStack(ModBlocks.hazmat, 8), "###", "# #", "###", '#', ModItems.hazmat_cloth);
        addRecipeAuto(new ItemStack(ModItems.hazmat_cloth, 1), "#", '#', ModBlocks.hazmat);
        addRecipeAuto(new ItemStack(ModBlocks.block_niter_reinforced, 1), "TCT", "CNC", "TCT", 'T', TCALLOY.ingot(), 'C', ModBlocks.concrete, 'N', KNO.block());
        addShapelessAuto(new ItemStack(ModBlocks.red_wire_sealed, 1), ModBlocks.red_wire_coated, ModBlocks.brick_compound);
        addRecipeAuto(new ItemStack(ModBlocks.fluid_duct_solid, 8), "SAS", "ADA", "SAS", 'S', STEEL.ingot(), 'A', AL.plate(), 'D', ModItems.ducttape);
        addShapelessAuto(new ItemStack(ModBlocks.fluid_duct_solid_sealed, 1), ModBlocks.fluid_duct_solid, ModBlocks.brick_compound);
    }
}
