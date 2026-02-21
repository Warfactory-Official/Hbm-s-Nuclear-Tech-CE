package com.hbm.crafting;

import com.hbm.blocks.BlockEnums;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.items.ItemEnums;
import com.hbm.items.ItemEnums.EnumDepletedRTGMaterial;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemWasteLong;
import com.hbm.items.special.ItemWasteShort;
import com.hbm.main.CraftingManager;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import static com.hbm.inventory.OreDictManager.*;

/**
 * Anything that deals exclusively with nuggets, ingots, blocks or compression in general
 * @author hbm
 */
public class MineralRecipes {

    public static void register() {

        add1To9Pair(ModItems.dust, ModItems.dust_tiny);
        add1To9Pair(ModItems.powder_coal, ModItems.powder_coal_tiny);
        add1To9Pair(ModItems.ingot_mercury, ModItems.nugget_mercury);

        add1To9Pair(ModBlocks.block_aluminium, ModItems.ingot_aluminium);
        add1To9Pair(ModBlocks.block_graphite, ModItems.ingot_graphite);
        add1To9Pair(ModBlocks.block_boron, ModItems.ingot_boron);
        add1To9Pair(ModBlocks.block_schraranium, ModItems.ingot_schraranium);
        add1To9Pair(ModBlocks.block_lanthanium, ModItems.ingot_lanthanium);
        add1To9Pair(ModBlocks.block_ra226, ModItems.ingot_ra226);
        add1To9Pair(ModBlocks.block_actinium, ModItems.ingot_actinium);
        add1To9Pair(ModBlocks.block_schrabidate, ModItems.ingot_schrabidate);
        add1To9Pair(ModBlocks.block_coltan, ModItems.fragment_coltan);
        add1To9Pair(ModBlocks.block_smore, ModItems.ingot_smore);
        add1To9Pair(ModBlocks.block_semtex, ModItems.ingot_semtex);
        add1To9Pair(ModBlocks.block_c4, ModItems.ingot_c4);
        add1To9Pair(ModBlocks.block_polymer, ModItems.ingot_polymer);
        add1To9Pair(ModBlocks.block_bakelite, ModItems.ingot_bakelite);
        add1To9Pair(ModBlocks.block_rubber, ModItems.ingot_rubber);
        add1To9Pair(ModBlocks.block_cadmium, ModItems.ingot_cadmium);
        add1To9Pair(ModBlocks.block_tcalloy, ModItems.ingot_tcalloy);
        add1To9Pair(ModBlocks.block_cdalloy, ModItems.ingot_cdalloy);

        for(int i = 0; i < ItemEnums.EnumCokeType.values().length; i++) {
            add1To9PairSameMeta(Item.getItemFromBlock(ModBlocks.block_coke), ModItems.coke, i);
        }

        addMineralSet(ModItems.nugget_niobium, ModItems.ingot_niobium, ModBlocks.block_niobium);
        addMineralSet(ModItems.nugget_bismuth, ModItems.ingot_bismuth, ModBlocks.block_bismuth);
        addMineralSet(ModItems.nugget_tantalium, ModItems.ingot_tantalium, ModBlocks.block_tantalium);
        addMineralSet(ModItems.nugget_zirconium, ModItems.ingot_zirconium, ModBlocks.block_zirconium);
        addMineralSet(ModItems.nugget_dineutronium, ModItems.ingot_dineutronium, ModBlocks.block_dineutronium);
        addMineralSet(ModItems.nuclear_waste_vitrified_tiny, ModItems.nuclear_waste_vitrified, ModBlocks.block_waste_vitrified);

        add1To9Pair(ModItems.ingot_silicon, ModItems.nugget_silicon);

        add1To9Pair(ModItems.powder_boron, ModItems.powder_boron_tiny);
        add1To9Pair(ModItems.powder_sr90, ModItems.powder_sr90_tiny);
        add1To9Pair(ModItems.powder_xe135, ModItems.powder_xe135_tiny);
        add1To9Pair(ModItems.powder_cs137, ModItems.powder_cs137_tiny);
        add1To9Pair(ModItems.powder_i131, ModItems.powder_i131_tiny);

        add1To9Pair(ModItems.ingot_technetium, ModItems.nugget_technetium);
        add1To9Pair(ModItems.ingot_co60, ModItems.nugget_co60);
        add1To9Pair(ModItems.ingot_sr90, ModItems.nugget_sr90);
        add1To9Pair(ModItems.ingot_au198, ModItems.nugget_au198);
        add1To9Pair(ModItems.ingot_pb209, ModItems.nugget_pb209);
        add1To9Pair(ModItems.ingot_ra226, ModItems.nugget_ra226);
        add1To9Pair(ModItems.ingot_actinium, ModItems.nugget_actinium);
        add1To9Pair(ModItems.ingot_arsenic, ModItems.nugget_arsenic);

        add1To9Pair(ModItems.ingot_pu241, ModItems.nugget_pu241);
        add1To9Pair(ModItems.ingot_am241, ModItems.nugget_am241);
        add1To9Pair(ModItems.ingot_am242, ModItems.nugget_am242);
        add1To9Pair(ModItems.ingot_am_mix, ModItems.nugget_am_mix);
        add1To9Pair(ModItems.ingot_americium_fuel, ModItems.nugget_americium_fuel);

        add1To9Pair(ModItems.ingot_gh336, ModItems.nugget_gh336);

        for(int i = 0; i < ItemWasteLong.WasteClass.VALUES.length; i++) {
            add1To9PairSameMeta(ModItems.nuclear_waste_long, ModItems.nuclear_waste_long_tiny, i);
            add1To9PairSameMeta(ModItems.nuclear_waste_long_depleted, ModItems.nuclear_waste_long_depleted_tiny, i);
        }

        for(int i = 0; i < ItemWasteShort.WasteClass.VALUES.length; i++) {
            add1To9PairSameMeta(ModItems.nuclear_waste_short, ModItems.nuclear_waste_short_tiny, i);
            add1To9PairSameMeta(ModItems.nuclear_waste_short_depleted, ModItems.nuclear_waste_short_depleted_tiny, i);
        }

        add1To9Pair(ModBlocks.block_fallout, ModItems.fallout);
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.fallout, 2), "##", '#', ModItems.fallout );

        addMineralSet(ModItems.nugget_pu_mix, ModItems.ingot_pu_mix, ModBlocks.block_pu_mix);
        add1To9Pair(ModItems.ingot_neptunium_fuel, ModItems.nugget_neptunium_fuel);

        addBillet(ModItems.billet_cobalt,				ModItems.ingot_cobalt,				ModItems.nugget_cobalt);
        addBillet(ModItems.billet_co60,					ModItems.ingot_co60,				ModItems.nugget_co60);
        addBillet(ModItems.billet_sr90,					ModItems.ingot_sr90,				ModItems.nugget_sr90, SR90.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_uranium,				ModItems.ingot_uranium,				ModItems.nugget_uranium, U.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_u233,					ModItems.ingot_u233,				ModItems.nugget_u233, U233.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_u235,					ModItems.ingot_u235,				ModItems.nugget_u235, U235.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_u238,					ModItems.ingot_u238,				ModItems.nugget_u238, U238.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_th232,				ModItems.ingot_th232,				ModItems.nugget_th232, TH232.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_plutonium,			ModItems.ingot_plutonium,			ModItems.nugget_plutonium, PU.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_pu238,				ModItems.ingot_pu238,				ModItems.nugget_pu238, PU238.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_pu239,				ModItems.ingot_pu239,				ModItems.nugget_pu239, PU239.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_pu240,				ModItems.ingot_pu240,				ModItems.nugget_pu240, PU240.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_pu241,				ModItems.ingot_pu241,				ModItems.nugget_pu241, PU241.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_pu_mix,				ModItems.ingot_pu_mix,				ModItems.nugget_pu_mix);
        addBillet(ModItems.billet_am241,				ModItems.ingot_am241,				ModItems.nugget_am241, AM241.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_am242,				ModItems.ingot_am242,				ModItems.nugget_am242, AM242.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_am_mix,				ModItems.ingot_am_mix,				ModItems.nugget_am_mix);
        addBillet(ModItems.billet_neptunium,			ModItems.ingot_neptunium,			ModItems.nugget_neptunium, NP237.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_polonium,				ModItems.ingot_polonium,			ModItems.nugget_polonium, PO210.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_technetium,			ModItems.ingot_technetium,			ModItems.nugget_technetium, TC99.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_au198,				ModItems.ingot_au198,				ModItems.nugget_au198, AU198.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_pb209,				ModItems.ingot_pb209,				ModItems.nugget_pb209, PB209.all(MaterialShapes.NUGGET)); //and so forth
        addBillet(ModItems.billet_ra226,				ModItems.ingot_ra226,				ModItems.nugget_ra226, RA226.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_actinium,				ModItems.ingot_actinium,			ModItems.nugget_actinium, AC227.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_schrabidium,			ModItems.ingot_schrabidium,			ModItems.nugget_schrabidium, SA326.nugget());
        addBillet(ModItems.billet_solinium,				ModItems.ingot_solinium,			ModItems.nugget_solinium, SA327.nugget());
        addBillet(ModItems.billet_gh336,				ModItems.ingot_gh336,				ModItems.nugget_gh336, GH336.all(MaterialShapes.NUGGET));
        addBillet(ModItems.billet_uranium_fuel,			ModItems.ingot_uranium_fuel,		ModItems.nugget_uranium_fuel);
        addBillet(ModItems.billet_thorium_fuel,			ModItems.ingot_thorium_fuel,		ModItems.nugget_thorium_fuel);
        addBillet(ModItems.billet_plutonium_fuel,		ModItems.ingot_plutonium_fuel,		ModItems.nugget_plutonium_fuel);
        addBillet(ModItems.billet_neptunium_fuel,		ModItems.ingot_neptunium_fuel,		ModItems.nugget_neptunium_fuel);
        addBillet(ModItems.billet_mox_fuel,				ModItems.ingot_mox_fuel,			ModItems.nugget_mox_fuel);
        addBillet(ModItems.billet_les,					ModItems.ingot_les,					ModItems.nugget_les);
        addBillet(ModItems.billet_schrabidium_fuel,		ModItems.ingot_schrabidium_fuel,	ModItems.nugget_schrabidium_fuel);
        addBillet(ModItems.billet_hes,					ModItems.ingot_hes,					ModItems.nugget_hes);
        addBillet(ModItems.billet_australium,			ModItems.ingot_australium,			ModItems.nugget_australium, "nuggetAustralium");
        addBillet(ModItems.billet_australium_greater,										ModItems.nugget_australium_greater);
        addBillet(ModItems.billet_australium_lesser,										ModItems.nugget_australium_lesser);
        addBillet(ModItems.billet_nuclear_waste,		ModItems.nuclear_waste,				ModItems.nuclear_waste_tiny);
        addBillet(ModItems.billet_beryllium,			ModItems.ingot_beryllium,			ModItems.nugget_beryllium, BE.nugget());
        addBillet(ModItems.billet_zirconium,			ModItems.ingot_zirconium,			ModItems.nugget_zirconium, ZR.nugget());
        addBillet(ModItems.billet_bismuth,				ModItems.ingot_bismuth,				ModItems.nugget_bismuth);
        addBillet(ModItems.billet_silicon,				ModItems.ingot_silicon,				ModItems.nugget_silicon, SI.nugget());

        CraftingManager.addShapelessAuto(new ItemStack(ModItems.billet_thorium_fuel, 6), ModItems.billet_th232, ModItems.billet_th232, ModItems.billet_th232, ModItems.billet_th232, ModItems.billet_th232, ModItems.billet_u233 );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_thorium_fuel, 1), "nuggetThorium232", "nuggetThorium232", "nuggetThorium232", "nuggetThorium232", "nuggetThorium232", "nuggetUranium233" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_thorium_fuel, 1), "tinyTh232", "tinyTh232", "tinyTh232", "tinyTh232", "tinyTh232", "tinyU233" );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.billet_uranium_fuel, 6), ModItems.billet_u238, ModItems.billet_u238, ModItems.billet_u238, ModItems.billet_u238, ModItems.billet_u238, ModItems.billet_u235 );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_uranium_fuel, 1), "nuggetUranium238", "nuggetUranium238", "nuggetUranium238", "nuggetUranium238", "nuggetUranium238", "nuggetUranium235" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_uranium_fuel, 1), "tinyU238", "tinyU238", "tinyU238", "tinyU238", "tinyU238", "tinyU235" );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.billet_plutonium_fuel, 3), ModItems.billet_u238, ModItems.billet_u238, ModItems.billet_pu_mix );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_plutonium_fuel, 1), ModItems.nugget_pu_mix, ModItems.nugget_pu_mix, "nuggetUranium238", "nuggetUranium238", "nuggetUranium238", "nuggetUranium238" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_plutonium_fuel, 1), ModItems.nugget_pu_mix, ModItems.nugget_pu_mix, "tinyU238", "tinyU238", "tinyU238", "tinyU238" );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.billet_pu_mix, 3), ModItems.billet_pu239, ModItems.billet_pu239, ModItems.billet_pu240 );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_pu_mix, 1), "nuggetPlutonium239", "nuggetPlutonium239", "nuggetPlutonium239", "nuggetPlutonium239", "nuggetPlutonium240", "nuggetPlutonium240" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_pu_mix, 1), "tinyPu239", "tinyPu239", "tinyPu239", "tinyPu239", "tinyPu240", "tinyPu240" );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.billet_americium_fuel, 3), ModItems.billet_u238, ModItems.billet_u238, ModItems.billet_am_mix );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_americium_fuel, 1), ModItems.nugget_am_mix, ModItems.nugget_am_mix, "nuggetUranium238", "nuggetUranium238", "nuggetUranium238", "nuggetUranium238" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_americium_fuel, 1), ModItems.nugget_am_mix, ModItems.nugget_am_mix, "tinyU238", "tinyU238", "tinyU238", "tinyU238" );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.billet_am_mix, 3), ModItems.billet_am241, ModItems.billet_am242, ModItems.billet_am242 );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_am_mix, 1), "nuggetAmericium241", "nuggetAmericium241", "nuggetAmericium242", "nuggetAmericium242", "nuggetAmericium242", "nuggetAmericium242" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_am_mix, 1), "tinyAm241", "tinyAm241", "tinyAm242", "tinyAm242", "tinyAm242", "tinyAm242" );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.billet_neptunium_fuel, 3), ModItems.billet_u238, ModItems.billet_u238, ModItems.billet_neptunium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_neptunium_fuel, 1), "nuggetNeptunium237", "nuggetNeptunium237", "nuggetUranium238", "nuggetUranium238", "nuggetUranium238", "nuggetUranium238" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_neptunium_fuel, 1), "tinyNp237", "tinyNp237", "tinyU238", "tinyU238", "tinyU238", "tinyU238" );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.billet_mox_fuel, 3), ModItems.billet_uranium_fuel, ModItems.billet_uranium_fuel, ModItems.billet_pu_mix );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_mox_fuel, 1), ModItems.nugget_pu_mix, ModItems.nugget_pu_mix, ModItems.nugget_uranium_fuel, ModItems.nugget_uranium_fuel, ModItems.nugget_uranium_fuel, ModItems.nugget_uranium_fuel );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.billet_schrabidium_fuel, 3), ModItems.billet_schrabidium, ModItems.billet_neptunium, ModItems.billet_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_schrabidium_fuel, 1), ModItems.nugget_schrabidium, ModItems.nugget_schrabidium, "nuggetNeptunium237", "nuggetNeptunium237", ModItems.nugget_beryllium, ModItems.nugget_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_schrabidium_fuel, 1), ModItems.nugget_schrabidium, ModItems.nugget_schrabidium, "tinyNp237", "tinyNp237", ModItems.nugget_beryllium, ModItems.nugget_beryllium );

        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_po210be, 1), "nuggetPolonium210", "nuggetPolonium210", "nuggetPolonium210", ModItems.nugget_beryllium, ModItems.nugget_beryllium, ModItems.nugget_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_pu238be, 1), "nuggetPlutonium238", "nuggetPlutonium238", "nuggetPlutonium238", ModItems.nugget_beryllium, ModItems.nugget_beryllium, ModItems.nugget_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_ra226be, 1), "nuggetRadium226", "nuggetRadium226", "nuggetRadium226", ModItems.nugget_beryllium, ModItems.nugget_beryllium, ModItems.nugget_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_po210be, 2), ModItems.billet_polonium, ModItems.billet_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_pu238be, 2), ModItems.billet_pu238, ModItems.billet_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_ra226be, 2), ModItems.billet_ra226, ModItems.billet_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_po210be, 6), ModItems.billet_polonium, ModItems.billet_polonium, ModItems.billet_polonium,  ModItems.billet_beryllium, ModItems.billet_beryllium, ModItems.billet_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_pu238be, 6), ModItems.billet_pu238, ModItems.billet_pu238, ModItems.billet_pu238,  ModItems.billet_beryllium, ModItems.billet_beryllium, ModItems.billet_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_ra226be, 6), ModItems.billet_ra226, ModItems.billet_ra226, ModItems.billet_ra226,  ModItems.billet_beryllium, ModItems.billet_beryllium, ModItems.billet_beryllium );

        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_zfb_bismuth, 1), ZR.nugget(), ZR.nugget(), ZR.nugget(), U.nugget(), PU241.nugget(), BI.nugget() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_zfb_pu241, 1), ZR.nugget(), ZR.nugget(), ZR.nugget(), U235.nugget(), PU240.nugget(), PU241.nugget() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_zfb_am_mix, 1), ZR.nugget(), ZR.nugget(), ZR.nugget(), PU241.nugget(), PU241.nugget(), AMRG.nugget() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_zfb_bismuth, 6), ZR.billet(), ZR.billet(), ZR.billet(), U.billet(), PU241.billet(), BI.billet() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_zfb_pu241, 6), ZR.billet(), ZR.billet(), ZR.billet(), U235.billet(), PU240.billet(), PU241.billet() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_zfb_am_mix, 6), ZR.billet(), ZR.billet(), ZR.billet(), PU241.billet(), PU241.billet(), AMRG.billet() );


        CraftingManager.addShapelessAuto(new ItemStack(ModItems.billet_uranium, 2), ModItems.billet_uranium_fuel, ModItems.billet_u238 );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_uranium, 2), ModItems.billet_u238, "nuggetUranium238", "nuggetUranium238", "nuggetUranium238", "nuggetUranium238", "nuggetUranium238", "nuggetUranium235" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.billet_uranium, 2), ModItems.billet_u238, "tinyU238", "tinyU238", "tinyU238", "tinyU238", "tinyU238", "tinyU235" );

        CraftingManager.addShapelessAuto(new ItemStack(ModItems.billet_balefire_gold, 1), ModItems.billet_au198, new ItemStack(ModItems.cell, 1, Fluids.AMAT.getID()), ModItems.pellet_charged );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.billet_flashlead, 2), ModItems.billet_balefire_gold, ModItems.billet_pb209, new ItemStack(ModItems.cell, 1, Fluids.AMAT.getID()) );

        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg), ModItems.billet_pu238, ModItems.billet_pu238, ModItems.billet_pu238, IRON.plate() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg_radium), ModItems.billet_ra226, ModItems.billet_ra226, ModItems.billet_ra226, IRON.plate() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg_weak), ModItems.billet_u238, ModItems.billet_u238, ModItems.billet_pu238, IRON.plate() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg_strontium), ModItems.billet_sr90, ModItems.billet_sr90, ModItems.billet_sr90, IRON.plate() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg_cobalt), ModItems.billet_co60, ModItems.billet_co60, ModItems.billet_co60, IRON.plate() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg_actinium), ModItems.billet_actinium, ModItems.billet_actinium, ModItems.billet_actinium, IRON.plate() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg_polonium), ModItems.billet_polonium, ModItems.billet_polonium, ModItems.billet_polonium, IRON.plate() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg_lead), ModItems.billet_pb209, ModItems.billet_pb209, ModItems.billet_pb209, IRON.plate() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg_gold), ModItems.billet_au198, ModItems.billet_au198, ModItems.billet_au198, IRON.plate() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg_americium), ModItems.billet_am241, ModItems.billet_am241, ModItems.billet_am241, IRON.plate() );

        //There's no need for anvil recycling recipes if you simply set the container item
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.billet_bismuth, 3), new ItemStack(ModItems.pellet_rtg_depleted, 1, EnumDepletedRTGMaterial.BISMUTH.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.ingot_lead, 2), new ItemStack(ModItems.pellet_rtg_depleted, 1, EnumDepletedRTGMaterial.LEAD.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.ingot_mercury, 2), new ItemStack(ModItems.pellet_rtg_depleted, 1, EnumDepletedRTGMaterial.MERCURY.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.billet_neptunium, 3), new ItemStack(ModItems.pellet_rtg_depleted, 1, EnumDepletedRTGMaterial.NEPTUNIUM.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.billet_zirconium, 3), new ItemStack(ModItems.pellet_rtg_depleted, 1, EnumDepletedRTGMaterial.ZIRCONIUM.ordinal()) );
        if (OreDictionary.doesOreNameExist("ingotNickel")) {
            NonNullList<ItemStack> ores = OreDictionary.getOres("ingotNickel");
            if (!ores.isEmpty()) {
                ItemStack out = ores.get(0).copy();
                out.setCount(2);
                CraftingManager.addShapelessAuto(out, new ItemStack(ModItems.pellet_rtg_depleted, 1, EnumDepletedRTGMaterial.NICKEL.ordinal()));
            }
        }

        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_copper), 1), "###", "###", "###", '#', ModItems.ingot_copper );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_fluorite), 1), "###", "###", "###", '#', ModItems.fluorite );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_niter), 1), "###", "###", "###", '#', ModItems.niter );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_red_copper), 1), "###", "###", "###", '#', ModItems.ingot_red_copper );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_steel), 1), "###", "###", "###", '#', ModItems.ingot_steel );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_sulfur), 1), "###", "###", "###", '#', ModItems.sulfur );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_titanium), 1), "###", "###", "###", '#', ModItems.ingot_titanium );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_tungsten), 1), "###", "###", "###", '#', ModItems.ingot_tungsten );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_uranium), 1), "###", "###", "###", '#', ModItems.ingot_uranium );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_thorium), 1), "###", "###", "###", '#', ModItems.ingot_th232 );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_lead), 1), "###", "###", "###", '#', ModItems.ingot_lead );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_trinitite), 1), "###", "###", "###", '#', ModItems.trinitite );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_waste), 1), "###", "###", "###", '#', ModItems.nuclear_waste );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_scrap), 1), "##", "##", '#', ModItems.scrap );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_scrap), 1), "###", "###", "###", '#', ModItems.dust );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_beryllium), 1), "###", "###", "###", '#', ModItems.ingot_beryllium );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_schrabidium), 1), "###", "###", "###", '#', ModItems.ingot_schrabidium );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_schrabidium_cluster, 1), "#S#", "SXS", "#S#", '#', ModItems.ingot_schrabidium, 'S', ModItems.ingot_starmetal, 'X', ModItems.ingot_schrabidate );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_euphemium), 1), "###", "###", "###", '#', ModItems.ingot_euphemium );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_advanced_alloy), 1), "###", "###", "###", '#', ModItems.ingot_advanced_alloy );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_magnetized_tungsten), 1), "###", "###", "###", '#', ModItems.ingot_magnetized_tungsten );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_combine_steel), 1), "###", "###", "###", '#', ModItems.ingot_combine_steel );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_australium), 1), "###", "###", "###", '#', ModItems.ingot_australium );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_desh), 1), "###", "###", "###", '#', ModItems.ingot_desh );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_dura_steel), 1), "###", "###", "###", '#', ModItems.ingot_dura_steel );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_meteor_cobble), 1), "##", "##", '#', ModItems.fragment_meteorite );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_meteor_broken), 1), "###", "###", "###", '#', ModItems.fragment_meteorite );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_yellowcake), 1), "###", "###", "###", '#', ModItems.powder_yellowcake );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_starmetal, 1), "###", "###", "###", '#', ModItems.ingot_starmetal );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_u233, 1), "###", "###", "###", '#', ModItems.ingot_u233 );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_u235, 1), "###", "###", "###", '#', ModItems.ingot_u235 );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_u238, 1), "###", "###", "###", '#', ModItems.ingot_u238 );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_uranium_fuel, 1), "###", "###", "###", '#', ModItems.ingot_uranium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_neptunium, 1), "###", "###", "###", '#', ModItems.ingot_neptunium );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_polonium, 1), "###", "###", "###", '#', ModItems.ingot_polonium );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_plutonium, 1), "###", "###", "###", '#', ModItems.ingot_plutonium );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_pu238, 1), "###", "###", "###", '#', ModItems.ingot_pu238 );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_pu239, 1), "###", "###", "###", '#', ModItems.ingot_pu239 );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_pu240, 1), "###", "###", "###", '#', ModItems.ingot_pu240 );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_mox_fuel, 1), "###", "###", "###", '#', ModItems.ingot_mox_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_plutonium_fuel, 1), "###", "###", "###", '#', ModItems.ingot_plutonium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_thorium_fuel, 1), "###", "###", "###", '#', ModItems.ingot_thorium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_solinium, 1), "###", "###", "###", '#', ModItems.ingot_solinium );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_schrabidium_fuel, 1), "###", "###", "###", '#', ModItems.ingot_schrabidium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_lithium, 1), "###", "###", "###", '#', ModItems.lithium );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_white_phosphorus, 1), "###", "###", "###", '#', ModItems.ingot_phosphorus );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_red_phosphorus, 1), "###", "###", "###", '#', ModItems.powder_fire );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_insulator, 1), "###", "###", "###", '#', ModItems.plate_polymer );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_asbestos, 1), "###", "###", "###", '#', ModItems.ingot_asbestos );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_fiberglass, 1), "###", "###", "###", '#', ModItems.ingot_fiberglass );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_cobalt, 1), "###", "###", "###", '#', ModItems.ingot_cobalt );

        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_copper, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_copper) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.fluorite, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_fluorite) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.niter, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_niter) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_red_copper, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_red_copper) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_steel, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_steel) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.sulfur, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_sulfur) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_titanium, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_titanium) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_tungsten, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_tungsten) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_uranium, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_uranium) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_th232, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_thorium) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_lead, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_lead) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.trinitite, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_trinitite) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nuclear_waste, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_waste) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nuclear_waste, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_waste_painted) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_beryllium, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_beryllium) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_schrabidium, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_schrabidium) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_euphemium, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_euphemium) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_advanced_alloy, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_advanced_alloy) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_magnetized_tungsten, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_magnetized_tungsten) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_combine_steel, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_combine_steel) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_australium, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_australium) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_desh, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_desh) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_dura_steel, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_dura_steel) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_yellowcake, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_yellowcake) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_starmetal, 9), "#", '#', ModBlocks.block_starmetal );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_u233, 9), "#", '#', ModBlocks.block_u233 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_u235, 9), "#", '#', ModBlocks.block_u235 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_u238, 9), "#", '#', ModBlocks.block_u238 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_uranium_fuel, 9), "#", '#', ModBlocks.block_uranium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_neptunium, 9), "#", '#', ModBlocks.block_neptunium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_polonium, 9), "#", '#', ModBlocks.block_polonium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_plutonium, 9), "#", '#', ModBlocks.block_plutonium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_pu238, 9), "#", '#', ModBlocks.block_pu238 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_pu239, 9), "#", '#', ModBlocks.block_pu239 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_pu240, 9), "#", '#', ModBlocks.block_pu240 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_mox_fuel, 9), "#", '#', ModBlocks.block_mox_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_plutonium_fuel, 9), "#", '#', ModBlocks.block_plutonium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_thorium_fuel, 9), "#", '#', ModBlocks.block_thorium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_solinium, 9), "#", '#', ModBlocks.block_solinium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_schrabidium_fuel, 9), "#", '#', ModBlocks.block_schrabidium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.lithium, 9), "#", '#', ModBlocks.block_lithium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_phosphorus, 9), "#", '#', ModBlocks.block_white_phosphorus );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_fire, 9), "#", '#', ModBlocks.block_red_phosphorus );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.plate_polymer, 9), "#", '#', ModBlocks.block_insulator );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_asbestos, 9), "#", '#', ModBlocks.block_asbestos );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_fiberglass, 9), "#", '#', ModBlocks.block_fiberglass );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_cobalt, 9), "#", '#', ModBlocks.block_cobalt );

        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_plutonium, 1), "###", "###", "###", '#', ModItems.nugget_plutonium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_plutonium, 9), "#", '#', ModItems.ingot_plutonium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_pu238, 1), "###", "###", "###", '#', ModItems.nugget_pu238 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_pu238, 9), "#", '#', ModItems.ingot_pu238 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_pu239, 1), "###", "###", "###", '#', ModItems.nugget_pu239 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_pu239, 9), "#", '#', ModItems.ingot_pu239 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_pu240, 1), "###", "###", "###", '#', ModItems.nugget_pu240 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_pu240, 9), "#", '#', ModItems.ingot_pu240 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_th232, 1), "###", "###", "###", '#', ModItems.nugget_th232 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_th232, 9), "#", '#', ModItems.ingot_th232 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_uranium, 1), "###", "###", "###", '#', ModItems.nugget_uranium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_uranium, 9), "#", '#', ModItems.ingot_uranium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_u233, 1), "###", "###", "###", '#', ModItems.nugget_u233 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_u233, 9), "#", '#', ModItems.ingot_u233 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_u235, 1), "###", "###", "###", '#', ModItems.nugget_u235 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_u235, 9), "#", '#', ModItems.ingot_u235 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_u238, 1), "###", "###", "###", '#', ModItems.nugget_u238 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_u238, 9), "#", '#', ModItems.ingot_u238 );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_neptunium, 1), "###", "###", "###", '#', ModItems.nugget_neptunium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_neptunium, 9), "#", '#', ModItems.ingot_neptunium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_polonium, 1), "###", "###", "###", '#', ModItems.nugget_polonium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_polonium, 9), "#", '#', ModItems.ingot_polonium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_lead, 1), "###", "###", "###", '#', ModItems.nugget_lead );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_lead, 9), "#", '#', ModItems.ingot_lead );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_beryllium, 1), "###", "###", "###", '#', ModItems.nugget_beryllium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_beryllium, 9), "#", '#', ModItems.ingot_beryllium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_schrabidium, 1), "###", "###", "###", '#', ModItems.nugget_schrabidium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_schrabidium, 9), "#", '#', ModItems.ingot_schrabidium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_uranium_fuel, 1), "###", "###", "###", '#', ModItems.nugget_uranium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_uranium_fuel, 9), "#", '#', ModItems.ingot_uranium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_thorium_fuel, 1), "###", "###", "###", '#', ModItems.nugget_thorium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_thorium_fuel, 9), "#", '#', ModItems.ingot_thorium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_plutonium_fuel, 1), "###", "###", "###", '#', ModItems.nugget_plutonium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_plutonium_fuel, 9), "#", '#', ModItems.ingot_plutonium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_mox_fuel, 1), "###", "###", "###", '#', ModItems.nugget_mox_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_mox_fuel, 9), "#", '#', ModItems.ingot_mox_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_schrabidium_fuel, 1), "###", "###", "###", '#', ModItems.nugget_schrabidium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_schrabidium_fuel, 9), "#", '#', ModItems.ingot_schrabidium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_hes, 1), "###", "###", "###", '#', ModItems.nugget_hes );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_hes, 9), "#", '#', ModItems.ingot_hes );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_les, 1), "###", "###", "###", '#', ModItems.nugget_les );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_les, 9), "#", '#', ModItems.ingot_les );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_australium, 1), "###", "###", "###", '#', ModItems.nugget_australium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_australium, 9), "#", '#', ModItems.ingot_australium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_steel, 1), "###", "###", "###", '#', ModItems.powder_steel_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_steel_tiny, 9), "#", '#', ModItems.powder_steel );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_lithium, 1), "###", "###", "###", '#', ModItems.powder_lithium_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_lithium_tiny, 9), "#", '#', ModItems.powder_lithium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_cobalt, 1), "###", "###", "###", '#', ModItems.powder_cobalt_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_cobalt_tiny, 9), "#", '#', ModItems.powder_cobalt );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_neodymium, 1), "###", "###", "###", '#', ModItems.powder_neodymium_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_neodymium_tiny, 9), "#", '#', ModItems.powder_neodymium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_niobium, 1), "###", "###", "###", '#', ModItems.powder_niobium_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_niobium_tiny, 9), "#", '#', ModItems.powder_niobium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_cerium, 1), "###", "###", "###", '#', ModItems.powder_cerium_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_cerium_tiny, 9), "#", '#', ModItems.powder_cerium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_lanthanium, 1), "###", "###", "###", '#', ModItems.powder_lanthanium_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_lanthanium_tiny, 9), "#", '#', ModItems.powder_lanthanium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_actinium, 1), "###", "###", "###", '#', ModItems.powder_actinium_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_actinium_tiny, 9), "#", '#', ModItems.powder_actinium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_meteorite, 1), "###", "###", "###", '#', ModItems.powder_meteorite_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.powder_meteorite_tiny, 9), "#", '#', ModItems.powder_meteorite );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_solinium, 1), "###", "###", "###", '#', ModItems.nugget_solinium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_solinium, 9), "#", '#', ModItems.ingot_solinium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nuclear_waste, 1), "###", "###", "###", '#', ModItems.nuclear_waste_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nuclear_waste_tiny, 9), "#", '#', ModItems.nuclear_waste );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.bottle_mercury, 1), "###", "#B#", "###", '#', ModItems.ingot_mercury, 'B', Items.GLASS_BOTTLE );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_mercury, 8), "#", '#', ModItems.bottle_mercury );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.egg_balefire, 1), "###", "###", "###", '#', ModItems.egg_balefire_shard );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.egg_balefire_shard, 9), "#", '#', ModItems.egg_balefire );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nitra, 1), "##", "##", '#', ModItems.nitra_small );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nitra_small, 4), "#", '#', ModItems.nitra );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.glass_polarized, 4), "##", "##", '#', DictFrame.fromOne(ModItems.part_generic, ItemEnums.EnumPartType.GLASS_POLARIZED) );
        add1To9Pair(ModItems.powder_paleogenite, ModItems.powder_paleogenite_tiny);
        add1To9Pair(ModItems.ingot_osmiridium, ModItems.nugget_osmiridium);

        CraftingManager.addRecipeAuto(new ItemStack(ModItems.egg_balefire_shard, 1), "##", "##", '#', ModItems.powder_balefire );
        add9To1(ModItems.cell_balefire, ModItems.egg_balefire_shard);

        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ingot_euphemium, 1), "###", "###", "###", '#', ModItems.nugget_euphemium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nugget_euphemium, 9), "#", '#', ModItems.ingot_euphemium );

        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.ingot_schrabidium_fuel, 1), "nuggetSchrabidium", "nuggetSchrabidium", "nuggetSchrabidium", "nuggetNeptunium237", "nuggetNeptunium237", "nuggetNeptunium237", ModItems.nugget_beryllium, ModItems.nugget_beryllium, ModItems.nugget_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.ingot_hes, 1), "nuggetSchrabidium", "nuggetSchrabidium", "nuggetSchrabidium", "nuggetSchrabidium", "nuggetSchrabidium", "nuggetNeptunium237", "nuggetNeptunium237", ModItems.nugget_beryllium, ModItems.nugget_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.ingot_les, 1), "nuggetSchrabidium", "nuggetNeptunium237", "nuggetNeptunium237", "nuggetNeptunium237", "nuggetNeptunium237", ModItems.nugget_beryllium, ModItems.nugget_beryllium, ModItems.nugget_beryllium, ModItems.nugget_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.ingot_pu_mix, 1), "nuggetPlutonium239", "nuggetPlutonium239", "nuggetPlutonium239", "nuggetPlutonium239", "nuggetPlutonium239", "nuggetPlutonium239", "nuggetPluonium240", "nuggetPluonium240", "nuggetPluonium240" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.ingot_pu_mix, 1), "tinyPu239", "tinyPu239", "tinyPu239", "tinyPu239", "tinyPu239", "tinyPu239", "tinyPu240", "tinyPu240", "tinyPu240" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.ingot_am_mix, 1), "nuggetAmericium241", "nuggetAmericium241", "nuggetAmericium241", "nuggetAmericium242", "nuggetAmericium242", "nuggetAmericium242", "nuggetAmericium242", "nuggetAmericium242", "nuggetAmericium242" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.ingot_am_mix, 1), "tinyAm241", "tinyAm241", "tinyAm241", "tinyAm242", "tinyAm242", "tinyAm242", "tinyAm242", "tinyAm242", "tinyAm242" );

        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.ball_fireclay, 4), Items.CLAY_BALL, Items.CLAY_BALL, Items.CLAY_BALL, AL.dust() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.ball_fireclay, 4), Items.CLAY_BALL, Items.CLAY_BALL, Items.CLAY_BALL, AL.ore() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.ball_fireclay, 4), Items.CLAY_BALL, Items.CLAY_BALL, DictFrame.fromOne(ModBlocks.stone_resource, BlockEnums.EnumStoneType.LIMESTONE), KEY_SAND );
    }

    //Bundled 1/9 recipes
    public static void add1To9Pair(Item one, Item nine) {
        add1To9(new ItemStack(one), new ItemStack(nine, 9));
        add9To1(new ItemStack(nine), new ItemStack(one));
    }

    public static void add1To9Pair(Block one, Item nine) {
        add1To9(new ItemStack(one), new ItemStack(nine, 9));
        add9To1(new ItemStack(nine), new ItemStack(one));
    }

    public static void add1To9PairSameMeta(Item one, Item nine, int meta) {
        add1To9SameMeta(one, nine, meta);
        add9To1SameMeta(nine, one, meta);
    }

    //Full set of nugget, ingot and block
    public static void addMineralSet(Item nugget, Item ingot, Block block) {
        add1To9(new ItemStack(ingot), new ItemStack(nugget, 9));
        add9To1(new ItemStack(nugget), new ItemStack(ingot));
        add1To9(new ItemStack(block), new ItemStack(ingot, 9));
        add9To1(new ItemStack(ingot), new ItemStack(block));
    }

    //Decompress one item into nine
    public static void add1To9(Block one, Item nine) {
        add1To9(new ItemStack(one), new ItemStack(nine, 9));
    }

    public static void add1To9(Item one, Item nine) {
        add1To9(new ItemStack(one), new ItemStack(nine, 9));
    }

    public static void add1To9SameMeta(Item one, Item nine, int meta) {
        add1To9(new ItemStack(one, 1, meta), new ItemStack(nine, 9, meta));
    }

    public static void add1To9(ItemStack one, ItemStack nine){
        CraftingManager.addShapelessAuto(nine, one );
    }

    //Compress nine items into one
    public static void add9To1(Item nine, Block one) {
        add9To1(new ItemStack(nine), new ItemStack(one));
    }

    public static void add9To1(Item nine, Item one) {
        add9To1(new ItemStack(nine), new ItemStack(one));
    }

    public static void add9To1SameMeta(Item nine, Item one, int meta) {
        add9To1(new ItemStack(nine, 1, meta), new ItemStack(one, 1, meta));
    }

    public static void add9To1(ItemStack nine, ItemStack one){
        CraftingManager.addRecipeAuto(one, "###", "###", "###", '#', nine );
    }

    public static void addBillet(Item billet, Item nugget, String... ore){
        for(String o : ore) CraftingManager.addRecipeAuto(new ItemStack(billet), "###", "###", '#', o );
        addBillet(billet, nugget);
    }

    public static void addBillet(Item billet, Item ingot, Item nugget, String... ore) {
        for(String o : ore) CraftingManager.addRecipeAuto(new ItemStack(billet), "###", "###", '#', o );
        addBillet(billet, ingot, nugget);
    }

    public static void addBilletFragment(ItemStack billet, ItemStack nugget) {
        CraftingManager.addRecipeAuto(billet.copy(), "###", "###", '#', nugget );
    }

    public static void addBillet(Item billet, Item nugget) {
        CraftingManager.addRecipeAuto(new ItemStack(billet), "###", "###", '#', nugget );
        CraftingManager.addShapelessAuto(new ItemStack(nugget, 6), billet );
    }

    public static void addBillet(Item billet, Item ingot, Item nugget) {
        CraftingManager.addRecipeAuto(new ItemStack(billet), "###", "###", '#', nugget );
        CraftingManager.addShapelessAuto(new ItemStack(nugget, 6), billet );
        addBilletToIngot(billet, ingot);
    }

    public static void addBilletToIngot(Item billet, Item ingot) {
        CraftingManager.addShapelessAuto(new ItemStack(ingot, 2), billet, billet, billet );
        CraftingManager.addRecipeAuto(new ItemStack(billet, 3), "##", '#', ingot );
    }
}
