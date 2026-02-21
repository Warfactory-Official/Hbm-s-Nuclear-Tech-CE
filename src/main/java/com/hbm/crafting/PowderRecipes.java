package com.hbm.crafting;

import com.hbm.config.GeneralConfig;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemChemicalDye.EnumChemDye;
import com.hbm.items.machine.ItemScraps;
import com.hbm.main.CraftingManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static com.hbm.inventory.OreDictManager.*;

/**
 * For recipes mostly involving or resulting in powder
 * @author hbm
 */
public class PowderRecipes {
    public static void register() {

        //Explosives
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.ballistite, 3), Items.GUNPOWDER, KNO.dust(), Items.SUGAR );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.ball_dynamite, 2), KNO.dust(), Items.SUGAR, Blocks.SAND, new CraftingManager.ComplexOreIngredient(KEY_TOOL_CHEMISTRYSET));
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.ball_tnt, 4), Fluids.AROMATICS.getDict(1000), KNO.dust(), new CraftingManager.ComplexOreIngredient(KEY_TOOL_CHEMISTRYSET));
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.ingot_c4, 4), Fluids.UNSATURATEDS.getDict(1000), KNO.dust(), new CraftingManager.ComplexOreIngredient(KEY_TOOL_CHEMISTRYSET));
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_semtex_mix, 3), ModItems.solid_fuel, ModItems.cordite, KNO.dust() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_semtex_mix, 1), ModItems.solid_fuel, ModItems.ballistite, KNO.dust() );
        CraftingManager.addShapelessAuto(new ItemStack(Items.CLAY_BALL, 4), KEY_SAND, ModItems.dust, ModItems.dust, Fluids.WATER.getDict(1_000) );
        CraftingManager.addShapelessAuto(new ItemStack(Items.CLAY_BALL, 4), Blocks.CLAY ); //clay uncrafting because placing and breaking it isn't worth anyone's time
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_cement, 4), LIMESTONE.dust(), Items.CLAY_BALL, Items.CLAY_BALL, Items.CLAY_BALL );

        //Other
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.ingot_steel_dusted, 1), STEEL.ingot(), COAL.dust() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_bakelite, 2), Fluids.AROMATICS.getDict(1000), Fluids.PETROLEUM.getDict(1000), new CraftingManager.ComplexOreIngredient(KEY_TOOL_CHEMISTRYSET));

        //Gunpowder
        CraftingManager.addShapelessAuto(new ItemStack(Items.GUNPOWDER, 3), S.dust(), KNO.dust(), COAL.gem() );
        CraftingManager.addShapelessAuto(new ItemStack(Items.GUNPOWDER, 3), S.dust(), KNO.dust(), new ItemStack(Items.COAL, 1, 1) );
        CraftingManager.addShapelessAuto(new ItemStack(Items.GUNPOWDER, 3), S.dust(), KNO.dust(), COAL.gem() );
        CraftingManager.addShapelessAuto(new ItemStack(Items.GUNPOWDER, 3), S.dust(), KNO.dust(), new ItemStack(Items.COAL, 1, 1) );

        //Blends
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_power, 3), "dustGlowstone", DIAMOND.dust(), MAGTUNG.dust() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_nitan_mix, 6), NP237.dust(), I.dust(), TH232.dust(), AT.dust(), ND.dust(), CS.dust() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_nitan_mix, 6), SR.dust(), CO.dust(), BR.dust(), TS.dust(), NB.dust(), CE.dust() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_spark_mix, 3), DESH.dust(), EUPH.dust(), ModItems.powder_power );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_meteorite, 4), IRON.dust(), CU.dust(), LI.dust(), NETHERQUARTZ.dust() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_thermite, 4), IRON.dust(), IRON.dust(), IRON.dust(), AL.dust() );

        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_desh_mix, 1), B.dustTiny(), B.dustTiny(), LA.dustTiny(), LA.dustTiny(), CE.dustTiny(), CO.dustTiny(), LI.dustTiny(), ND.dustTiny(), NB.dustTiny() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_desh_mix, 9), B.dust(), B.dust(), LA.dust(), LA.dust(), CE.dust(), CO.dust(), LI.dust(), ND.dust(), NB.dust() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_desh_ready, 1), ModItems.powder_desh_mix, ModItems.ingot_mercury, ModItems.ingot_mercury, COAL.dust() );

        //Metal powders
        CraftingManager.addShapelessAuto(ItemScraps.create(new Mats.MaterialStack(Mats.MAT_MINGRADE, MaterialShapes.INGOT.q(2))), CU.dust(), REDSTONE.dust() );
        CraftingManager.addShapelessAuto(ItemScraps.create(new Mats.MaterialStack(Mats.MAT_MAGTUNG, MaterialShapes.INGOT.q(1))), W.dust(), SA326.nugget() );
        CraftingManager.addShapelessAuto(ItemScraps.create(new Mats.MaterialStack(Mats.MAT_TCALLOY, MaterialShapes.INGOT.q(1))), STEEL.dust(), TC99.nugget() );
        CraftingManager.addShapelessAuto(ItemScraps.create(new Mats.MaterialStack(Mats.MAT_STEEL, MaterialShapes.INGOT.q(1))), IRON.dust(), COAL.dust() );
        CraftingManager.addShapelessAuto(ItemScraps.create(new Mats.MaterialStack(Mats.MAT_STEEL, MaterialShapes.INGOT.q(4))), IRON.dust(), IRON.dust(), IRON.dust(), IRON.dust(), COAL.dust(), COAL.dust(), COAL.dust(), COAL.dust() );

        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_flux, 1), new ItemStack(Items.COAL, 1, 1), KEY_SAND );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_flux, 2), COAL.dust(), KEY_SAND );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_flux, 4), F.dust(), KEY_SAND );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_flux, 8), PB.dust(), S.dust(), KEY_SAND );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_flux, 12), LIMESTONE.dust(), KEY_SAND );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_flux, 12), CA.dust(), KEY_SAND );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_flux, 16), BORAX.dust(), KEY_SAND );

        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_fertilizer, 4), CA.dust(), P_RED.dust(), KNO.dust(), S.dust() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_fertilizer, 4), ANY_ASH.any(), P_RED.dust(), KNO.dust(), S.dust() );

        if(GeneralConfig.enableLBSM && GeneralConfig.enableLBSMSimpleCrafting) {
            CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_advanced_alloy, 4), REDSTONE.dust(), IRON.dust(), COAL.dust(), CU.dust() );
            CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_advanced_alloy, 4), IRON.dust(), COAL.dust(), MINGRADE.dust(), MINGRADE.dust() );
            CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_advanced_alloy, 4), REDSTONE.dust(), CU.dust(), STEEL.dust(), STEEL.dust() );
            CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_advanced_alloy, 2), MINGRADE.dust(), STEEL.dust() );
            CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_red_copper, 2), REDSTONE.dust(), CU.dust() );
            CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_dura_steel, 2), STEEL.dust(), W.dust() );
            CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_dura_steel, 2), STEEL.dust(), CO.dust() );
            CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_dura_steel, 4), IRON.dust(), COAL.dust(), W.dust(), W.dust() );
            CraftingManager.addShapelessAuto(new ItemStack(ModItems.powder_dura_steel, 4), IRON.dust(), COAL.dust(), CO.dust(), CO.dust() );
            CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_firebrick, 4), "BN", "NB", 'B', Items.BRICK, 'N', Items.NETHERBRICK );
        }

        //Unleash the colores
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.GRAY, 2), DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.BLACK),		DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.WHITE) );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.SILVER, 2), DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.GRAY),		DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.WHITE) );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.ORANGE, 2), DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.RED),		DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.YELLOW) );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.LIME, 2), DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.GREEN),		DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.WHITE) );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.CYAN, 2), DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.BLUE),		DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.GREEN) );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.PURPLE, 2), DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.RED),		DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.BLUE) );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.BROWN, 2), DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.ORANGE),	DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.BLACK) );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.MAGENTA, 2), DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.PINK),		DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.PURPLE) );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.LIGHTBLUE, 2), DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.BLUE),		DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.WHITE) );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.PINK, 2), DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.RED),		DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.WHITE) );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.GREEN, 2), DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.BLUE),		DictFrame.fromOne(ModItems.chemical_dye, EnumChemDye.YELLOW) );

        for(int i = 0; i < 15; i++) CraftingManager.addShapelessAuto(new ItemStack(ModItems.crayon, 4, i), new ItemStack(ModItems.chemical_dye, 1, i), ANY_TAR.any(), Items.PAPER );

    }
}
