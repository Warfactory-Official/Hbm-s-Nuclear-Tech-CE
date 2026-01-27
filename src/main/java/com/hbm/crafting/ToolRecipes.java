package com.hbm.crafting;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.items.ItemEnums;
import com.hbm.items.ItemEnums.EnumCircuitType;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBatteryPack;
import com.hbm.items.tool.ItemBlowtorch;
import com.hbm.items.tool.ItemToolAbilityFueled;
import com.hbm.main.CraftingManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static com.hbm.inventory.OreDictManager.*;

/**
 * For mining and utility tools
 * @author hbm
 */
public class ToolRecipes {

    public static void register() {

        //Regular tools
        addSword(	STEEL.ingot(), ModItems.steel_sword);
        addPickaxe(	STEEL.ingot(), ModItems.steel_pickaxe);
        addAxe(		STEEL.ingot(), ModItems.steel_axe);
        addShovel(	STEEL.ingot(), ModItems.steel_shovel);
        addHoe(		STEEL.ingot(), ModItems.steel_hoe);
        addSword(	TI.ingot(), ModItems.titanium_sword);
        addPickaxe(	TI.ingot(), ModItems.titanium_pickaxe);
        addAxe(		TI.ingot(), ModItems.titanium_axe);
        addShovel(	TI.ingot(), ModItems.titanium_shovel);
        addHoe(		TI.ingot(), ModItems.titanium_hoe);
        addSword(	CO.ingot(), ModItems.cobalt_sword);
        addPickaxe(	CO.ingot(), ModItems.cobalt_pickaxe);
        addAxe(		CO.ingot(), ModItems.cobalt_axe);
        addShovel(	CO.ingot(), ModItems.cobalt_shovel);
        addHoe(		CO.ingot(), ModItems.cobalt_hoe);
        addSword(	ALLOY.ingot(), ModItems.alloy_sword);
        addPickaxe(	ALLOY.ingot(), ModItems.alloy_pickaxe);
        addAxe(		ALLOY.ingot(), ModItems.alloy_axe);
        addShovel(	ALLOY.ingot(), ModItems.alloy_shovel);
        addHoe(		ALLOY.ingot(), ModItems.alloy_hoe);
        addSword(	CMB.ingot(), ModItems.cmb_sword);
        addPickaxe(	CMB.ingot(), ModItems.cmb_pickaxe);
        addAxe(		CMB.ingot(), ModItems.cmb_axe);
        addShovel(	CMB.ingot(), ModItems.cmb_shovel);
        addHoe(		CMB.ingot(), ModItems.cmb_hoe);
        addSword(	DESH.ingot(), ModItems.desh_sword);
        addPickaxe(	DESH.ingot(), ModItems.desh_pickaxe);
        addAxe(		DESH.ingot(), ModItems.desh_axe);
        addShovel(	DESH.ingot(), ModItems.desh_shovel);
        addHoe(		DESH.ingot(), ModItems.desh_hoe);

        CraftingManager.addRecipeAuto(new ItemStack(ModItems.elec_sword, 1), "RPR", "RPR", " B ", 'P', ANY_PLASTIC.ingot(), 'R', DURA.bolt(), 'B', ItemBatteryPack.EnumBatteryPack.BATTERY_LEAD.stack() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.elec_pickaxe, 1), "RDM", " PB", " P ", 'P', ANY_PLASTIC.ingot(), 'D', DURA.ingot(), 'R', DURA.bolt(), 'M', ModItems.motor, 'B', ItemBatteryPack.EnumBatteryPack.BATTERY_LEAD.stack() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.elec_axe, 1), " DP", "RRM", " PB", 'P', ANY_PLASTIC.ingot(), 'D', DURA.ingot(), 'R', DURA.bolt(), 'M', ModItems.motor, 'B', ItemBatteryPack.EnumBatteryPack.BATTERY_LEAD.stack() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.elec_shovel, 1), "  P", "RRM", "  B", 'P', ANY_PLASTIC.ingot(), 'R', DURA.bolt(), 'M', ModItems.motor, 'B', ItemBatteryPack.EnumBatteryPack.BATTERY_LEAD.stack() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.centri_stick, 1), ModItems.centrifuge_element, ModItems.energy_core, KEY_STICK );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.smashing_hammer, 1), "STS", "SPS", " P ", 'S', STEEL.block(), 'T', W.block(), 'P', ANY_PLASTIC.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.meteorite_sword, 1), "  B", "GB ", "SG ", 'B', ModItems.blade_meteorite, 'G', GOLD.plate(), 'S', KEY_STICK );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.dwarven_pickaxe, 1), "CIC", " S ", " S ", 'C', CU.ingot(), 'I', IRON.ingot(), 'S', KEY_STICK );

        //Super pickaxes
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.bismuth_pickaxe, 1), " BM", "BPB", "TB ", 'B', ModItems.ingot_bismuth, 'M', ModItems.ingot_meteorite, 'P', ModItems.starmetal_pickaxe, 'T', W.bolt() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.volcanic_pickaxe, 1), " BM", "BPB", "TB ", 'B', ModItems.gem_volcanic, 'M', ModItems.ingot_meteorite, 'P', ModItems.starmetal_pickaxe, 'T', W.bolt() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.chlorophyte_pickaxe, 1), " SD", "APS", "FA ", 'S', ModItems.blades_steel, 'D', ModItems.powder_chlorophyte, 'A', FIBER.ingot(), 'P', ModItems.bismuth_pickaxe, 'F', DURA.bolt() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.chlorophyte_pickaxe, 1), " SD", "APS", "FA ", 'S', ModItems.blades_steel, 'D', ModItems.powder_chlorophyte, 'A', FIBER.ingot(), 'P', ModItems.volcanic_pickaxe, 'F', DURA.bolt() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mese_pickaxe, 1), " SD", "APS", "FA ", 'S', ModItems.blades_desh, 'D', ModItems.powder_dineutronium, 'A', ModItems.plate_paa, 'P', ModItems.chlorophyte_pickaxe, 'F', ModItems.shimmer_handle );

        //Super Axes
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.bismuth_axe, 1), " BM", "BPB", "TB ", 'B', ModItems.ingot_bismuth, 'M', ModItems.ingot_meteorite, 'P', ModItems.starmetal_axe, 'T', W.bolt() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.volcanic_axe, 1), " BM", "BPB", "TB ", 'B', ModItems.gem_volcanic, 'M', ModItems.ingot_meteorite, 'P', ModItems.starmetal_axe, 'T', W.bolt() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.chlorophyte_axe, 1), " SD", "APS", "FA ", 'S', ModItems.blades_steel, 'D', ModItems.powder_chlorophyte, 'A', FIBER.ingot(), 'P', ModItems.bismuth_axe, 'F', DURA.bolt() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.chlorophyte_axe, 1), " SD", "APS", "FA ", 'S', ModItems.blades_steel, 'D', ModItems.powder_chlorophyte, 'A', FIBER.ingot(), 'P', ModItems.volcanic_axe, 'F', DURA.bolt() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mese_axe, 1), " SD", "APS", "FA ", 'S', ModItems.blades_desh, 'D', ModItems.powder_dineutronium, 'A', ModItems.plate_paa, 'P', ModItems.chlorophyte_axe, 'F', ModItems.shimmer_handle );

        //Chainsaws
        CraftingManager.addRecipeAuto(ItemToolAbilityFueled.getEmptyTool(ModItems.chainsaw), "CCH", "BBP", "CCE", 'H', STEEL.shell(), 'B', ModItems.blades_steel, 'P', ModItems.piston_selenium, 'C', ModBlocks.chain, 'E', ModItems.canister_empty );

        //Misc
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.crowbar, 1), "II", " I", " I", 'I', STEEL.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.bottle_opener, 1), "S", "P", 'S', STEEL.plate(), 'P', KEY_PLANKS );
        CraftingManager.addRecipeAuto(new ItemStack(Items.SADDLE, 1), "LLL", "LRL", " S ", 'S', STEEL.ingot(), 'L', Items.LEATHER, 'R', DictFrame.fromOne(ModItems.plant_item, ItemEnums.EnumPlantType.ROPE) );

        //Matches
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.matchstick, 16), "I", "S", 'I', S.dust(), 'S', KEY_STICK );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.matchstick, 24), "I", "S", 'I', P_RED.dust(), 'S', KEY_STICK );

        //Gavels
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.wood_gavel, 1), "SWS", " R ", " R ", 'S', KEY_SLAB, 'W', KEY_LOG, 'R', KEY_STICK );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.lead_gavel, 1), "PIP", "IGI", "PIP", 'P', ModItems.pellet_buckshot, 'I', PB.ingot(), 'G', ModItems.wood_gavel );

        //Misc weapons
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.pipe_lead, 1), "II", " I", " I", 'I', PB.pipe() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ullapool_caber, 1), "ITI", " S ", " S ", 'I', IRON.plate(), 'T', Blocks.TNT, 'S', KEY_STICK );

        //Utility
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.rangefinder, 1), "GRC", "  S", 'G', KEY_ANYPANE, 'R', REDSTONE.dust(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), 'S' ,STEEL.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.designator, 1), "  A", "#B#", "#B#", '#', ANY_PLASTIC.ingot(), 'A', STEEL.plate(), 'B', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.designator_range, 1), ModItems.rangefinder, ModItems.designator, ANY_PLASTIC.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.designator_manual, 1), "  A", "#C#", "#B#", '#', ANY_PLASTIC.ingot(), 'A', PB.plate(), 'B', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'C', ModItems.designator );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.designator_arty_range, 1), ModItems.rangefinder, DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), ANY_PLASTIC.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.linker, 1), "I I", "ICI", "GGG", 'I', IRON.plate(), 'G', GOLD.plate(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.oil_detector, 1), "W I", "WCI", "PPP", 'W', GOLD.wireFine(), 'I', CU.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ANALOG), 'P', STEEL.plate528() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.turret_chip, 1), "WWW", "CPC", "WWW", 'W', GOLD.wireFine(), 'P', ANY_PLASTIC.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED));
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.survey_scanner, 1), "SWS", " G ", "PCP", 'W', GOLD.wireFine(), 'P', ANY_PLASTIC.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'S', STEEL.plate528(), 'G', GOLD.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.geiger_counter, 1), "GPP", "WCS", "WBB", 'W', GOLD.wireFine(), 'P', ANY_RUBBER.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), 'G', GOLD.ingot(), 'S', STEEL.plate528(), 'B', ModItems.ingot_beryllium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.dosimeter, 1), "WGW", "WCW", "WBW", 'W', KEY_PLANKS, 'G', KEY_ANYPANE, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE), 'B', BE.ingot() );
        CraftingManager.addShapelessAuto(new ItemStack(ModBlocks.geiger), ModItems.geiger_counter );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.digamma_diagnostic), ModItems.geiger_counter, PO210.billet(), ASBESTOS.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.pollution_detector, 1), "SFS", "SCS", " S ", 'S', STEEL.plate(), 'F', ModItems.filter_coal, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ore_density_scanner, 1), "VVV", "CSC", "GGG", 'V', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CAPACITOR), 'S', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CONTROLLER_CHASSIS), 'G', GOLD.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.defuser, 1), " PS", "P P", " P ", 'P', ANY_PLASTIC.ingot(), 'S', STEEL.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.coltan_tool, 1), "ACA", "CXC", "ACA", 'A', ALLOY.ingot(), 'C', CINNABAR.crystal(), 'X', Items.COMPASS );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.reacher, 1), "BIB", "P P", "B B", 'B', W.bolt(), 'I', W.ingot(), 'P', ANY_RUBBER.ingot() );
        // TODO
        //CraftingManager.addRecipeAuto(new ItemStack(ModItems.sat_designator, 1), "RRD", "PIC", "  P", 'P', GOLD.plate(), 'R', Items.REDSTONE, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'D', ModItems.sat_chip, 'I', GOLD.ingot() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.sat_relay), ModItems.sat_chip, ModItems.ducttape, ModItems.radar_linker );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.settings_tool), " P ", "PCP", "III", 'P', IRON.plate(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ANALOG), 'I', ModItems.plate_polymer );

        CraftingManager.addRecipeAuto(new ItemStack(ModItems.pipette, 1), "  L", " G ", "G  ", 'L', ANY_RUBBER.ingot(), 'G', KEY_CLEARGLASS);
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.pipette_boron, 1), "  P", " B ", "B  ", 'P', RUBBER.ingot(), 'B', ModBlocks.glass_boron);
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.pipette_laboratory, 1), "  C", " R ", "P  ", 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP), 'R', RUBBER.ingot(), 'P', ModItems.pipette_boron );

        CraftingManager.addRecipeAuto(new ItemStack(ModItems.siphon, 1), " GR", " GR", " G ", 'G', KEY_CLEARGLASS, 'R', ANY_RUBBER.ingot());

        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mirror_tool), " A ", " IA", "I  ", 'A', AL.ingot(), 'I', IRON.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.rbmk_tool), " A ", " IA", "I  ", 'A', PB.ingot(), 'I', IRON.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.power_net_tool), "WRW", " I ", " B ", 'W', MINGRADE.wireFine(), 'R', REDSTONE.dust(), 'I', IRON.ingot(), 'B', ItemBatteryPack.EnumBatteryPack.BATTERY_LEAD.stack() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.analysis_tool), "  G", " S ", "S  ", 'G', KEY_ANYPANE, 'S', STEEL.ingot() );

        CraftingManager.addRecipeAuto(new ItemStack(ModItems.toolbox), "CCC", "CIC", 'C', CU.plate(), 'I', IRON.ingot() );

        CraftingManager.addRecipeAuto(new ItemStack(ModItems.screwdriver, 1), "  I", " I ", "S  ", 'S', STEEL.ingot(), 'I', IRON.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.screwdriver_desh, 1), "  I", " I ", "S  ", 'S', ANY_PLASTIC.ingot(), 'I', DESH.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.hand_drill), " D", "S ", " S", 'D', DURA.ingot(), 'S', KEY_STICK );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.hand_drill_desh), " D", "S ", " S", 'D', DESH.ingot(), 'S', ANY_PLASTIC.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.chemistry_set), "GIG", "GCG", 'G', KEY_ANYGLASS, 'I', IRON.ingot(), 'C', CU.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.chemistry_set_boron), "GIG", "GCG", 'G', ModBlocks.glass_boron, 'I', STEEL.ingot(), 'C', CO.ingot() );
        CraftingManager.addRecipeAuto(ItemBlowtorch.getEmptyTool(ModItems.blowtorch), "CC ", " I ", "CCC", 'C', CU.plate528(), 'I', IRON.ingot() );
        CraftingManager.addRecipeAuto(ItemBlowtorch.getEmptyTool(ModItems.acetylene_torch), "SS ", " PS", " T ", 'S', STEEL.plate528(), 'P', ANY_PLASTIC.ingot(), 'T', ModItems.tank_steel );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.boltgun), "DPS", " RD", " D ", 'D', DURA.ingot(), 'P', DictFrame.fromOne(ModItems.part_generic, ItemEnums.EnumPartType.PISTON_PNEUMATIC), 'R', RUBBER.ingot(), 'S', STEEL.shell() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.rebar_placer), "RDR", "DWD", "RDR", 'R', ModBlocks.rebar, 'D', ModItems.ducttape, 'W', ModItems.wrench );

        //Bobmazon
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.bobmazon), Items.BOOK, Items.GOLD_NUGGET, Items.STRING, KEY_BLUE);

        //Carts
        // TODO: ItemModMinecart
        /*CraftingManager.addRecipeAuto(ItemModMinecart.createCartItem(EnumCartBase.WOOD, EnumMinecart.EMPTY), "P P", "WPW", 'P',KEY_SLAB, 'W', KEY_PLANKS );
        CraftingManager.addRecipeAuto(ItemModMinecart.createCartItem(EnumCartBase.STEEL, EnumMinecart.EMPTY), "P P", "IPI", 'P', STEEL.plate(), 'I', STEEL.ingot() );
        CraftingManager.addShapelessAuto(ItemModMinecart.createCartItem(EnumCartBase.PAINTED, EnumMinecart.EMPTY), ItemModMinecart.createCartItem(EnumCartBase.STEEL, EnumMinecart.EMPTY), KEY_RED );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.boat_rubber), "L L", "LLL", 'L', ANY_RUBBER.ingot() );

        for(EnumCartBase base : EnumCartBase.values()) {

            if(EnumMinecart.DESTROYER.supportsBase(base))	CraftingManager.addRecipeAuto(ItemModMinecart.createCartItem(base, EnumMinecart.DESTROYER), "S S", "BLB", "SCS", 'S', STEEL.ingot(), 'B', ModItems.blades_steel, 'L', Fluids.LAVA.getDict(1000), 'C', ItemModMinecart.createCartItem(base, EnumMinecart.EMPTY) );
            if(EnumMinecart.POWDER.supportsBase(base))		CraftingManager.addRecipeAuto(ItemModMinecart.createCartItem(base, EnumMinecart.POWDER), "PPP", "PCP", "PPP", 'P', Items.GUNPOWDER, 'C', ItemModMinecart.createCartItem(base, EnumMinecart.EMPTY) );
            if(EnumMinecart.SEMTEX.supportsBase(base))		CraftingManager.addRecipeAuto(ItemModMinecart.createCartItem(base, EnumMinecart.SEMTEX), "S", "C", 'S', ModBlocks.semtex, 'C', ItemModMinecart.createCartItem(base, EnumMinecart.EMPTY) );
        }
        net.minecraft.item.crafting.CraftingManager.getInstance().addRecipe(DictFrame.fromOne(ModItems.cart, EnumMinecart.CRATE), "C", "S", 'C', ModBlocks.crate_steel, 'S', Items.MINECART ).func_92100_c();*/

        //Configged
        if(GeneralConfig.enableLBSM && GeneralConfig.enableLBSMSimpleToolRecipes) {
            addSword(	CO.block(), ModItems.cobalt_decorated_sword);
            addPickaxe(	CO.block(), ModItems.cobalt_decorated_pickaxe);
            addAxe(		CO.block(), ModItems.cobalt_decorated_axe);
            addShovel(	CO.block(), ModItems.cobalt_decorated_shovel);
            addHoe(		CO.block(), ModItems.cobalt_decorated_hoe);
            addSword(	STAR.ingot(), ModItems.starmetal_sword);
            addPickaxe(	STAR.ingot(), ModItems.starmetal_pickaxe);
            addAxe(		STAR.ingot(), ModItems.starmetal_axe);
            addShovel(	STAR.ingot(), ModItems.starmetal_shovel);
            addHoe(		STAR.ingot(), ModItems.starmetal_hoe);
            addSword(	SA326.ingot(), ModItems.schrabidium_sword);
            addPickaxe(	SA326.ingot(), ModItems.schrabidium_pickaxe);
            addAxe(		SA326.ingot(), ModItems.schrabidium_axe);
            addShovel(	SA326.ingot(), ModItems.schrabidium_shovel);
            addHoe(		SA326.ingot(), ModItems.schrabidium_hoe);
        } else {
            CraftingManager.addRecipeAuto(new ItemStack(ModItems.starmetal_sword, 1), " I ", " B ", "ISI", 'I', STAR.ingot(), 'S', ModItems.ring_starmetal, 'B', ModItems.cobalt_decorated_sword );
            CraftingManager.addRecipeAuto(new ItemStack(ModItems.starmetal_pickaxe, 1), "ISI", " B ", " I ", 'I', STAR.ingot(), 'S', ModItems.ring_starmetal, 'B', ModItems.cobalt_decorated_pickaxe );
            CraftingManager.addRecipeAuto(new ItemStack(ModItems.starmetal_axe, 1), "IS", "IB", " I", 'I', STAR.ingot(), 'S', ModItems.ring_starmetal, 'B', ModItems.cobalt_decorated_axe );
            CraftingManager.addRecipeAuto(new ItemStack(ModItems.starmetal_shovel, 1), "I", "B", "I", 'I', STAR.ingot(), 'B', ModItems.cobalt_decorated_shovel );
            CraftingManager.addRecipeAuto(new ItemStack(ModItems.starmetal_hoe, 1), "IS", " B", " I", 'I', STAR.ingot(), 'S', ModItems.ring_starmetal, 'B', ModItems.cobalt_decorated_hoe );
            CraftingManager.addRecipeAuto(new ItemStack(ModItems.schrabidium_sword, 1), "I", "W", "S", 'I', SA326.block(), 'W', ModItems.desh_sword, 'S', ANY_PLASTIC.ingot() );
            CraftingManager.addRecipeAuto(new ItemStack(ModItems.schrabidium_pickaxe, 1), "BSB", " W ", " P ", 'B', ModItems.blades_desh, 'S', SA326.block(), 'W', ModItems.desh_pickaxe, 'P', ANY_PLASTIC.ingot() );
            CraftingManager.addRecipeAuto(new ItemStack(ModItems.schrabidium_axe, 1), "BS", "BW", " P", 'B', ModItems.blades_desh, 'S', SA326.block(), 'W', ModItems.desh_axe, 'P', ANY_PLASTIC.ingot() );
            CraftingManager.addRecipeAuto(new ItemStack(ModItems.schrabidium_shovel, 1), "B", "W", "P", 'B', SA326.block(), 'W', ModItems.desh_shovel, 'P', ANY_PLASTIC.ingot() );
            CraftingManager.addRecipeAuto(new ItemStack(ModItems.schrabidium_hoe, 1), "IW", " S", " S", 'I', SA326.ingot(), 'W', ModItems.desh_hoe, 'S', ANY_PLASTIC.ingot() );
        }
    }

    //Common wrappers
    public static void addSword(Object ingot, Item sword) {
        addTool(ingot, sword, patternSword);
    }
    public static void addPickaxe(Object ingot, Item pick) {
        addTool(ingot, pick, patternPick);
    }
    public static void addAxe(Object ingot, Item axe) {
        addTool(ingot, axe, patternAxe);
    }
    public static void addShovel(Object ingot, Item shovel) {
        addTool(ingot, shovel, patternShovel);
    }
    public static void addHoe(Object ingot, Item hoe) {
        addTool(ingot, hoe, patternHoe);
    }

    public static void addTool(Object ingot, Item tool, String[] pattern) {
        CraftingManager.addRecipeAuto(new ItemStack(tool), pattern, 'X', ingot, '#', KEY_STICK );
    }

    public static final String[] patternSword = new String[] {"X", "X", "#"};
    public static final String[] patternPick = new String[] {"XXX", " # ", " # "};
    public static final String[] patternAxe = new String[] {"XX", "X#", " #"};
    public static final String[] patternShovel = new String[] {"X", "#", "#"};
    public static final String[] patternHoe = new String[] {"XX", " #", " #"};
}
