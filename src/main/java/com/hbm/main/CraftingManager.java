package com.hbm.main;

import com.hbm.Tags;
import com.hbm.blocks.BlockEnums;
import com.hbm.blocks.BlockEnums.LightstoneType;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.PlantEnums;
import com.hbm.blocks.generic.BlockAbsorber;
import com.hbm.blocks.generic.BlockConcreteColoredExt.EnumConcreteType;
import com.hbm.config.GeneralConfig;
import com.hbm.crafting.*;
import com.hbm.crafting.handlers.*;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.ItemEnums;
import com.hbm.items.ItemEnums.CircuitComponentType;
import com.hbm.items.ItemEnums.EnumCircuitType;
import com.hbm.items.ItemEnums.EnumPartType;
import com.hbm.items.ItemEnums.ScrapType;
import com.hbm.items.ModItems;
import com.hbm.items.machine.*;
import com.hbm.items.machine.ItemZirnoxRod.EnumZirnoxType;
import com.hbm.items.tool.ItemConveyorWand;
import com.hbm.items.tool.ItemDrone;
import com.hbm.items.tool.ItemGuideBook;
import com.hbm.lib.Library;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Objects;

import static com.hbm.inventory.OreDictManager.*;

// Th3_Sl1ze: don't shit in your pants when you'll see lots of TODOs
// I'll make sure every block/item in needing of port will be signed as TODO
public class CraftingManager {

	public static RegistryEvent.Register<IRecipe> hack;

	public static void init(){
		if(!GeneralConfig.recipes) {
			return;
		}
		addCrafting();
		SmeltingRecipes.AddSmeltingRec();

		MineralRecipes.register();
		RodRecipes.register();
		ToolRecipes.register();
		ArmorRecipes.register();
		WeaponRecipes.register();
		ConsumableRecipes.register();
		PowderRecipes.register();
		ExclusiveRecipes.register();

		hack.getRegistry().register(new RBMKFuelCraftingHandler().setRegistryName(new ResourceLocation(Tags.MODID, "rbmk_fuel_crafting_handler")));
		hack.getRegistry().register(new MKUCraftingHandler().setRegistryName(new ResourceLocation(Tags.MODID, "mku_crafting_handler")));
		hack.getRegistry().register(new CargoShellCraftingHandler().setRegistryName(new ResourceLocation(Tags.MODID, "cargo_shell_crafting_handler")));
		hack.getRegistry().register(new ScrapsCraftingHandler().setRegistryName(new ResourceLocation(Tags.MODID, "scraps_crafting_handler")));
		addUpgradeContainers(hack.getRegistry());
	}

	public static void addCrafting(){
		// TODO: rework that addslabstair shit
		addSlabStair(ModBlocks.reinforced_brick_slab, ModBlocks.reinforced_brick_stairs, ModBlocks.reinforced_brick);
		addSlabStair(ModBlocks.reinforced_sand_slab, ModBlocks.reinforced_sand_stairs, ModBlocks.reinforced_sand);
		addSlabStair(ModBlocks.reinforced_stone_slab, ModBlocks.reinforced_stone_stairs, ModBlocks.reinforced_stone);
		addSlabStair(ModBlocks.brick_concrete_slab, ModBlocks.brick_concrete_stairs, ModBlocks.brick_concrete);
		addSlabStair(ModBlocks.brick_concrete_mossy_slab, ModBlocks.brick_concrete_mossy_stairs, ModBlocks.brick_concrete_mossy);
		addSlabStair(ModBlocks.brick_concrete_cracked_slab, ModBlocks.brick_concrete_cracked_stairs, ModBlocks.brick_concrete_cracked);
		addSlabStair(ModBlocks.brick_concrete_broken_slab, ModBlocks.brick_concrete_broken_stairs, ModBlocks.brick_concrete_broken);
		addSlabStair(ModBlocks.brick_compound_slab, ModBlocks.brick_compound_stairs, ModBlocks.brick_compound);
		addSlabStair(ModBlocks.brick_asbestos_slab, ModBlocks.brick_asbestos_stairs, ModBlocks.brick_asbestos);
		addSlabStair(ModBlocks.brick_obsidian_slab, ModBlocks.brick_obsidian_stairs, ModBlocks.brick_obsidian);
		addSlabStair(ModBlocks.cmb_brick_reinforced_slab, ModBlocks.cmb_brick_reinforced_stairs, ModBlocks.cmb_brick_reinforced);
		addSlabStair(ModBlocks.concrete_slab, ModBlocks.concrete_stairs, ModBlocks.concrete);
		addSlabStair(ModBlocks.concrete_smooth_slab, ModBlocks.concrete_smooth_stairs, ModBlocks.concrete_smooth);
		addSlabStairColConcrete(ModBlocks.concrete_colored_stairs, ModBlocks.concrete_colored);
		addStairColorExt(ModBlocks.concrete_colored_ext_stairs, ModBlocks.concrete_colored_ext);
		addSlabStair(ModBlocks.concrete_asbestos_slab, ModBlocks.concrete_asbestos_stairs, ModBlocks.concrete_asbestos);
		addSlabStair(ModBlocks.ducrete_smooth_slab, ModBlocks.ducrete_smooth_stairs, ModBlocks.ducrete_smooth);
		addSlabStair(ModBlocks.ducrete_slab, ModBlocks.ducrete_stairs, ModBlocks.ducrete);
		addSlabStair(ModBlocks.ducrete_brick_slab, ModBlocks.ducrete_brick_stairs, ModBlocks.brick_ducrete);
		addSlabStair(ModBlocks.ducrete_reinforced_slab, ModBlocks.ducrete_reinforced_stairs, ModBlocks.reinforced_ducrete);
		addSlabStair(ModBlocks.tile_lab_slab, ModBlocks.tile_lab_stairs, ModBlocks.tile_lab);
		addSlabStair(ModBlocks.tile_lab_cracked_slab, ModBlocks.tile_lab_cracked_stairs, ModBlocks.tile_lab_cracked);
		addSlabStair(ModBlocks.tile_lab_broken_slab, ModBlocks.tile_lab_broken_stairs, ModBlocks.tile_lab_broken);

		addSlabStair(ModBlocks.pink_slab, ModBlocks.pink_stairs, ModBlocks.pink_planks);

		addRecipeAuto(new ItemStack(ModItems.redstone_sword, 1), "R", "R", "S", 'R', REDSTONE.block(), 'S', KEY_STICK );
		addRecipeAuto(new ItemStack(ModItems.big_sword, 1), "QIQ", "QIQ", "GSG", 'G', Items.GOLD_INGOT, 'S', KEY_STICK, 'I', Items.IRON_INGOT, 'Q', Items.QUARTZ);

		addShapelessAuto(new ItemStack(ModBlocks.machine_assembly_machine), new ItemStack(ModBlocks.machine_assembler));
		addShapelessAuto(new ItemStack(ModBlocks.machine_assembly_factory), new ItemStack(ModBlocks.machine_assemfac));
		addShapelessAuto(new ItemStack(ModBlocks.machine_chemical_plant), new ItemStack(ModBlocks.machine_chemplant));
		addShapelessAuto(new ItemStack(ModBlocks.machine_chemical_factory), new ItemStack(ModBlocks.machine_chemfac));

		addRecipeAuto(Mats.MAT_IRON.make(ModItems.plate_cast), "BPB", "BPB", "BPB", 'B', STEEL.bolt(), 'P', IRON.plate() );
		addRecipeAuto(new ItemStack(ModItems.hazmat_cloth_red, 1), "C", "R", "C", 'C', ModItems.hazmat_cloth, 'R', REDSTONE.dust() );
		addRecipeAuto(new ItemStack(ModItems.hazmat_cloth_grey, 1), " P ", "ICI", " L ", 'C', ModItems.hazmat_cloth_red, 'P', IRON.plate(), 'L', PB.plate(), 'I', ANY_RUBBER.ingot() );
		addRecipeAuto(new ItemStack(ModItems.asbestos_cloth, 8), "SCS", "CPC", "SCS", 'S', Items.STRING, 'P', BR.dust(), 'C', Blocks.WOOL );
		addRecipeAuto(new ItemStack(ModItems.bolt_spike, 2), "BB", "B ", "B ", 'B', STEEL.bolt());
		addRecipeAuto(new ItemStack(ModItems.pipes_steel, 1), "B", "B", "B", 'B', STEEL.block() );
		addRecipeAuto(new ItemStack(ModItems.plate_polymer, 8), "DD", 'D', ANY_PLASTIC.ingot() );
		addRecipeAuto(new ItemStack(ModItems.plate_polymer, 8), "DD", 'D', ANY_RUBBER.ingot() );
		addRecipeAuto(new ItemStack(ModItems.plate_polymer, 16), "DD", 'D', FIBER.ingot());
		addRecipeAuto(new ItemStack(ModItems.plate_polymer, 16), "DD", 'D', ASBESTOS.ingot());
		addRecipeAuto(new ItemStack(ModItems.plate_polymer, 4), "SWS", 'S', Items.STRING, 'W', Blocks.WOOL );
		addRecipeAuto(new ItemStack(ModItems.plate_polymer, 4), "BB", 'B', "ingotBrick" );
		addRecipeAuto(new ItemStack(ModItems.plate_polymer, 4), "BB", 'B', "ingotNetherBrick" );

		addRecipeAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE), "G", "W", "I", 'G', KEY_ANYPANE, 'W', W.wireFine(), 'I', ModItems.plate_polymer );
		addRecipeAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE), "G", "W", "I", 'G', KEY_ANYPANE, 'W', CARBON.wireFine(), 'I', ModItems.plate_polymer );
		addRecipeAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CAPACITOR), "I", "N", "W", 'I', ModItems.plate_polymer, 'N', NB.nugget(), 'W', AL.wireFine() );
		addRecipeAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CAPACITOR), "I", "N", "W", 'I', ModItems.plate_polymer, 'N', NB.nugget(), 'W', CU.wireFine() );
		addRecipeAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CAPACITOR, 2), "IAI", "W W", 'I', ModItems.plate_polymer, 'A', AL.dust(), 'W', AL.wireFine() );
		addRecipeAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CAPACITOR, 2), "IAI", "W W", 'I', ModItems.plate_polymer, 'A', AL.dust(), 'W', CU.wireFine() );
		addRecipeAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CAPACITOR_TANTALIUM), "I", "N", "W", 'I', ModItems.plate_polymer, 'N', TA.nugget(), 'W', AL.wireFine() );
		addRecipeAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CAPACITOR_TANTALIUM), "I", "N", "W", 'I', ModItems.plate_polymer, 'N', TA.nugget(), 'W', CU.wireFine() );
		addRecipeAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.PCB), "I", "P", 'I', ModItems.plate_polymer, 'P', CU.plate() );
		addRecipeAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.PCB, 4), "I", "P", 'I', ModItems.plate_polymer, 'P', GOLD.plate() );
		addRecipeAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP), "I", "S", "W", 'I', ModItems.plate_polymer, 'S', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.SILICON), 'W', CU.wireFine() );
		addRecipeAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP), "I", "S", "W", 'I', ModItems.plate_polymer, 'S', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.SILICON), 'W', GOLD.wireFine() );
		addRecipeAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP_BISMOID), "III", "SNS", "WWW", 'I', ModItems.plate_polymer, 'S', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.SILICON), 'N', ANY_BISMOID.nugget(), 'W', CU.wireFine() );
		addRecipeAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP_BISMOID), "III", "SNS", "WWW", 'I', ModItems.plate_polymer, 'S', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.SILICON), 'N', ANY_BISMOID.nugget(), 'W', GOLD.wireFine() );
		addRecipeAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP_QUANTUM), "HHH", "SIS", "WWW", 'H', ANY_HARDPLASTIC.ingot(), 'S', BSCCO.wireDense(), 'I', ModItems.pellet_charged, 'W', CU.wireFine() );
		addRecipeAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP_QUANTUM), "HHH", "SIS", "WWW", 'H', ANY_HARDPLASTIC.ingot(), 'S', BSCCO.wireDense(), 'I', ModItems.pellet_charged, 'W', GOLD.wireFine() );
		addRecipeAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CONTROLLER_CHASSIS), "PPP", "CBB", "PPP", 'P', ANY_PLASTIC.ingot(), 'C', ModItems.crt_display, 'B', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.PCB) );
		addRecipeAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ATOMIC_CLOCK), "ICI", "CSC", "ICI", 'I', ModItems.plate_polymer, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP), 'S', SR.dust() );

		addRecipeAuto(new ItemStack(ModItems.crt_display, 4), " A ", "SGS", " T ", 'A', AL.dust(), 'S', STEEL.plate(), 'G', KEY_ANYPANE, 'T', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE) );

		addRecipeAuto(new ItemStack(ModItems.cell, 6), " S ", "G G", " S ", 'S', STEEL.plate(), 'G', KEY_ANYPANE );
		addRecipeAuto(new ItemStack(ModItems.cell, 8, Fluids.DEUTERIUM.getID()), "DDD", "DTD", "DDD", 'D', new ItemStack(ModItems.cell), 'T', ModItems.mike_deut );
		addRecipeAuto(new ItemStack(ModItems.particle_empty, 2), "STS", "G G", "STS", 'S', PB.plateCast(), 'T', ModItems.coil_gold, 'G', KEY_ANYPANE );
		addShapelessAuto(new ItemStack(ModItems.particle_copper, 1), ModItems.particle_empty, CU.dust(), ModItems.pellet_charged );
		addShapelessAuto(new ItemStack(ModItems.particle_lead, 1), ModItems.particle_empty, PB.dust(), ModItems.pellet_charged );
		addShapelessAuto(new ItemStack(ModItems.cell, 1, Fluids.AMAT.getID()), ModItems.particle_aproton, ModItems.particle_aelectron, new ItemStack(ModItems.cell) );
		addShapelessAuto(new ItemStack(ModItems.particle_amat, 1), ModItems.particle_aproton, ModItems.particle_aelectron, ModItems.particle_empty );

		addRecipeAuto(new ItemStack(ModItems.canister_empty, 2), "S ", "AA", "AA", 'S', STEEL.plate(), 'A', AL.plate() );
		addRecipeAuto(new ItemStack(ModItems.gas_empty, 2), "S ", "AA", "AA", 'A', STEEL.plate(), 'S', CU.plate() );
		addShapelessAuto(new ItemStack(ModBlocks.block_waste_painted, 1), KEY_YELLOW, ModBlocks.block_waste );


		addRecipeAuto(new ItemStack(ModItems.ingot_aluminium, 1), "###", "###", "###", '#', AL.wireFine() );
		addRecipeAuto(new ItemStack(ModItems.ingot_copper, 1), "###", "###", "###", '#', CU.wireFine() );
		addRecipeAuto(new ItemStack(ModItems.ingot_tungsten, 1), "###", "###", "###", '#', W.wireFine() );
		addRecipeAuto(new ItemStack(ModItems.ingot_red_copper, 1), "###", "###", "###", '#', MINGRADE.wireFine() );
		addRecipeAuto(new ItemStack(ModItems.ingot_advanced_alloy, 1), "###", "###", "###", '#', ALLOY.wireFine() );
		addRecipeAuto(new ItemStack(Items.GOLD_INGOT, 1), "###", "###", "###", '#', GOLD.wireFine() );
		addRecipeAuto(new ItemStack(ModItems.ingot_schrabidium, 1), "###", "###", "###", '#', SA326.wireFine() );
		addRecipeAuto(new ItemStack(ModItems.ingot_magnetized_tungsten, 1), "###", "###", "###", '#', MAGTUNG.wireFine() );

		addShapelessAuto(new ItemStack(ModItems.biomass, 4), Items.SUGAR, ModItems.powder_sawdust, ModItems.powder_sawdust, ModItems.powder_sawdust, ModItems.powder_sawdust, ModItems.powder_sawdust );
		addShapelessAuto(new ItemStack(ModItems.biomass, 4), Items.SUGAR, ModItems.powder_sawdust, ModItems.powder_sawdust, Items.APPLE, Items.APPLE, Items.APPLE );
		addShapelessAuto(new ItemStack(ModItems.biomass, 4), Items.SUGAR, ModItems.powder_sawdust, ModItems.powder_sawdust, Items.REEDS, Items.REEDS, Items.REEDS );
		addShapelessAuto(new ItemStack(ModItems.biomass, 4), Items.SUGAR, ModItems.powder_sawdust, ModItems.powder_sawdust, Items.ROTTEN_FLESH, Items.ROTTEN_FLESH, Items.ROTTEN_FLESH );
		addShapelessAuto(new ItemStack(ModItems.biomass, 4), Items.SUGAR, ModItems.powder_sawdust, ModItems.powder_sawdust, Items.CARROT, Items.CARROT, Items.CARROT, Items.CARROT, Items.CARROT, Items.CARROT );
		addShapelessAuto(new ItemStack(ModItems.biomass, 4), Items.SUGAR, ModItems.powder_sawdust, ModItems.powder_sawdust, Items.POTATO, Items.POTATO, Items.POTATO, Items.POTATO, Items.POTATO, Items.POTATO );
		addShapelessAuto(new ItemStack(ModItems.biomass, 4), Items.SUGAR, ModItems.powder_sawdust, ModItems.powder_sawdust, KEY_SAPLING, KEY_SAPLING, KEY_SAPLING, KEY_SAPLING, KEY_SAPLING, KEY_SAPLING );
		addShapelessAuto(new ItemStack(ModItems.biomass, 4), Items.SUGAR, ModItems.powder_sawdust, ModItems.powder_sawdust, KEY_LEAVES, KEY_LEAVES, KEY_LEAVES, KEY_LEAVES, KEY_LEAVES, KEY_LEAVES );
		addShapelessAuto(new ItemStack(ModItems.biomass, 4), Items.SUGAR, ModItems.powder_sawdust, ModItems.powder_sawdust, Blocks.PUMPKIN );
		addShapelessAuto(new ItemStack(ModItems.biomass, 4), Items.SUGAR, ModItems.powder_sawdust, ModItems.powder_sawdust, Blocks.MELON_BLOCK );
		addShapelessAuto(new ItemStack(ModItems.biomass, 4), Items.SUGAR, ModItems.powder_sawdust, ModItems.powder_sawdust, Blocks.CACTUS, Blocks.CACTUS, Blocks.CACTUS );
		addShapelessAuto(new ItemStack(ModItems.biomass, 4), Items.SUGAR, ModItems.powder_sawdust, ModItems.powder_sawdust, Items.WHEAT, Items.WHEAT, Items.WHEAT, Items.WHEAT, Items.WHEAT, Items.WHEAT );
		addShapelessAuto(new ItemStack(ModItems.biomass, 4), DictFrame.fromOne(ModBlocks.plant_flower, PlantEnums.EnumFlowerPlantType.HEMP), DictFrame.fromOne(ModBlocks.plant_flower, PlantEnums.EnumFlowerPlantType.HEMP), DictFrame.fromOne(ModBlocks.plant_flower, PlantEnums.EnumFlowerPlantType.HEMP), DictFrame.fromOne(ModBlocks.plant_flower, PlantEnums.EnumFlowerPlantType.HEMP), DictFrame.fromOne(ModBlocks.plant_flower, PlantEnums.EnumFlowerPlantType.HEMP), DictFrame.fromOne(ModBlocks.plant_flower, PlantEnums.EnumFlowerPlantType.HEMP) );

		addRecipeAuto(new ItemStack(ModItems.coil_copper, 1), "WWW", "WIW", "WWW", 'W', MINGRADE.wireFine(), 'I', IRON.ingot() );
		addRecipeAuto(new ItemStack(ModItems.coil_advanced_alloy, 1), "WWW", "WIW", "WWW", 'W', ALLOY.wireFine(), 'I', IRON.ingot() );
		addRecipeAuto(new ItemStack(ModItems.coil_gold, 1), "WWW", "WIW", "WWW", 'W', GOLD.wireFine(), 'I', IRON.ingot() );
		addRecipeAuto(new ItemStack(ModItems.coil_copper_torus, 2), " C ", "CPC", " C ", 'P', IRON.plate(), 'C', ModItems.coil_copper );
		addRecipeAuto(new ItemStack(ModItems.coil_advanced_torus, 2), " C ", "CPC", " C ", 'P', IRON.plate(), 'C', ModItems.coil_advanced_alloy );
		addRecipeAuto(new ItemStack(ModItems.coil_gold_torus, 2), " C ", "CPC", " C ", 'P', IRON.plate(), 'C', ModItems.coil_gold );
		addRecipeAuto(new ItemStack(ModItems.coil_copper_torus, 2), " C ", "CPC", " C ", 'P', STEEL.plate(), 'C', ModItems.coil_copper );
		addRecipeAuto(new ItemStack(ModItems.coil_advanced_torus, 2), " C ", "CPC", " C ", 'P', STEEL.plate(), 'C', ModItems.coil_advanced_alloy );
		addRecipeAuto(new ItemStack(ModItems.coil_gold_torus, 2), " C ", "CPC", " C ", 'P', STEEL.plate(), 'C', ModItems.coil_gold );
		addRecipeAuto(new ItemStack(ModItems.coil_tungsten, 1), "WWW", "WIW", "WWW", 'W', W.wireFine(), 'I', IRON.ingot() );
		addRecipeAuto(new ItemStack(ModItems.coil_magnetized_tungsten, 1), "WWW", "WIW", "WWW", 'W', MAGTUNG.wireFine(), 'I', IRON.ingot() );
		addRecipeAuto(new ItemStack(ModItems.tank_steel, 2), "STS", "S S", "STS", 'S', STEEL.plate(), 'T', TI.plate() );
		addRecipeAuto(new ItemStack(ModItems.motor, 2), " R ", "ICI", "ITI", 'R', MINGRADE.wireFine(), 'T', ModItems.coil_copper_torus, 'I', IRON.plate(), 'C', ModItems.coil_copper );
		addRecipeAuto(new ItemStack(ModItems.motor, 2), " R ", "ICI", " T ", 'R', MINGRADE.wireFine(), 'T', ModItems.coil_copper_torus, 'I', STEEL.plate(), 'C', ModItems.coil_copper );
		addRecipeAuto(new ItemStack(ModItems.motor_desh, 1), "PCP", "DMD", "PCP", 'P', ANY_PLASTIC.ingot(), 'C', ModItems.coil_gold_torus, 'D', DESH.ingot(), 'M', ModItems.motor );
		addRecipeAuto(new ItemStack(ModItems.motor_bismuth, 1), "BCB", "SDS", "BCB", 'B', BI.nugget(), 'C', ModBlocks.hadron_coil_alloy, 'S', STEEL.plateCast(), 'D', DURA.ingot() );
		addRecipeAuto(new ItemStack(ModItems.deuterium_filter, 1), "TST", "SCS", "TST", 'T', ANY_RESISTANTALLOY.ingot(), 'S', S.dust(), 'C', ModItems.catalyst_clay );

		addRecipeAuto(new ItemStack(ModItems.fins_flat, 1), "IP", "PP", "IP", 'P', STEEL.plate(), 'I', STEEL.ingot() );
		addRecipeAuto(new ItemStack(ModItems.fins_small_steel, 1), " PP", "PII", " PP", 'P', STEEL.plate(), 'I', STEEL.ingot() );
		addRecipeAuto(new ItemStack(ModItems.fins_big_steel, 1), " PI", "III", " PI", 'P', STEEL.plate(), 'I', STEEL.ingot() );
		addRecipeAuto(new ItemStack(ModItems.fins_tri_steel, 1), " PI", "IIB", " PI", 'P', STEEL.plate(), 'I', STEEL.ingot(), 'B', STEEL.block() );
		addRecipeAuto(new ItemStack(ModItems.fins_quad_titanium, 1), " PP", "III", " PP", 'P', TI.plate(), 'I', TI.ingot() );
		addRecipeAuto(new ItemStack(ModItems.sphere_steel, 1), "PIP", "I I", "PIP", 'P', STEEL.plate(), 'I', STEEL.ingot() );
		addRecipeAuto(new ItemStack(ModItems.pedestal_steel, 1), "P P", "P P", "III", 'P', STEEL.plate(), 'I', STEEL.ingot() );
		addRecipeAuto(new ItemStack(ModItems.lemon, 1), " D ", "DSD", " D ", 'D', KEY_YELLOW, 'S', "stone" );
		addRecipeAuto(new ItemStack(ModItems.blade_titanium, 2), "TP", "TP", "TT", 'P', TI.plate(), 'T', TI.ingot() );
		addRecipeAuto(new ItemStack(ModItems.turbine_titanium, 1), "BBB", "BSB", "BBB", 'B', ModItems.blade_titanium, 'S', STEEL.ingot() );
		addRecipeAuto(new ItemStack(ModItems.shimmer_head, 1), "SSS", "DTD", "SSS", 'S', STEEL.ingot(), 'D', DESH.block(), 'T', W.block() );
		addRecipeAuto(new ItemStack(ModItems.shimmer_axe_head, 1), "PII", "PBB", "PII", 'P', STEEL.plate(), 'B', DESH.block(), 'I', W.ingot() );
		addRecipeAuto(new ItemStack(ModItems.shimmer_handle, 1), "GP", "GP", "GP", 'G', GOLD.plate(), 'P', ANY_PLASTIC.ingot() );
		addRecipeAuto(new ItemStack(ModItems.shimmer_sledge, 1), "H", "G", "G", 'G', ModItems.shimmer_handle, 'H', ModItems.shimmer_head );
		addRecipeAuto(new ItemStack(ModItems.shimmer_axe, 1), "H", "G", "G", 'G', ModItems.shimmer_handle, 'H', ModItems.shimmer_axe_head );
		addShapelessAuto(new ItemStack(ModItems.definitelyfood, 4), ANY_RUBBER.ingot(), Items.WHEAT, Items.ROTTEN_FLESH, "treeSapling" );
		addShapelessAuto(new ItemStack(ModItems.definitelyfood, 4), ANY_RUBBER.ingot(), Items.WHEAT, Items.ROTTEN_FLESH, Items.WHEAT_SEEDS, Items.WHEAT_SEEDS, Items.WHEAT_SEEDS );
		addRecipeAuto(new ItemStack(ModItems.turbine_tungsten, 1), "BBB", "BSB", "BBB", 'B', ModItems.blade_tungsten, 'S', DURA.ingot() );
		addRecipeAuto(new ItemStack(ModItems.ring_starmetal, 1), " S ", "S S", " S ", 'S', STAR.ingot() );
		addRecipeAuto(new ItemStack(ModItems.flywheel_beryllium, 1), "IBI", "BTB", "IBI", 'B', BE.block(), 'I', IRON.plateCast(), 'T', DURA.pipe() );

		addShapelessAuto(new ItemStack(ModItems.powder_poison), DictFrame.fromOne(ModBlocks.plant_flower, PlantEnums.EnumFlowerPlantType.NIGHTSHADE) );
		addShapelessAuto(new ItemStack(ModItems.syringe_metal_stimpak), ModItems.syringe_metal_empty, Items.CARROT, DictFrame.fromOne(ModBlocks.plant_flower, PlantEnums.EnumFlowerPlantType.FOXGLOVE) ); //xander root and broc flower
		addShapelessAuto(new ItemStack(ModItems.pill_herbal), COAL.dust(), Items.POISONOUS_POTATO, Items.NETHER_WART, DictFrame.fromOne(ModBlocks.plant_flower, PlantEnums.EnumFlowerPlantType.FOXGLOVE) );
		addShapelessAuto(DictFrame.fromOne(ModItems.plant_item, ItemEnums.EnumPlantType.ROPE, 1), Items.STRING, Items.STRING, Items.STRING );
		addRecipeAuto(DictFrame.fromOne(ModItems.plant_item, ItemEnums.EnumPlantType.ROPE, 4), "W", "W", "W", 'W', DictFrame.fromOne(ModBlocks.plant_flower, PlantEnums.EnumFlowerPlantType.HEMP) );
		addShapelessAuto(new ItemStack(Items.STRING, 3), DictFrame.fromOne(ModBlocks.plant_flower, PlantEnums.EnumFlowerPlantType.HEMP) );
		addRecipeAuto(new ItemStack(Items.PAPER, 3), "SSS", 'S', ModItems.powder_sawdust );

		addRecipeAuto(new ItemStack(ModItems.wrench, 1), " S ", " IS", "I  ", 'S', STEEL.ingot(), 'I', IRON.ingot() );
		addRecipeAuto(new ItemStack(ModItems.wrench_flipped, 1), "S", "D", "W", 'S', Items.IRON_SWORD, 'D', ModItems.ducttape, 'W', ModItems.wrench );
		addRecipeAuto(new ItemStack(ModItems.memespoon, 1), "CGC", "PSP", "IAI", 'C', ModItems.powder_yellowcake, 'G', TH232.block(), 'P', ModItems.photo_panel, 'S', ModItems.steel_shovel, 'I', ModItems.plate_polymer, 'A', "ingotAustralium" );
		addShapelessAuto(new ItemStack(ModItems.cbt_device, 1), STEEL.bolt(), ModItems.wrench );

		addShapelessAuto(new ItemStack(ModItems.toothpicks, 3), KEY_STICK, KEY_STICK, KEY_STICK );
		addRecipeAuto(new ItemStack(ModItems.ducttape, 4), "F", "P", "S", 'F', Items.STRING, 'S', KEY_SLIME, 'P', Items.PAPER );

		addRecipeAuto(new ItemStack(ModBlocks.radio_torch_sender, 4), "G", "R", "I", 'G', "dustGlowstone", 'R', Blocks.REDSTONE_TORCH, 'I', NETHERQUARTZ.gem() );
		addRecipeAuto(new ItemStack(ModBlocks.radio_torch_receiver, 4), "G", "R", "I", 'G', "dustGlowstone", 'R', Blocks.REDSTONE_TORCH, 'I', IRON.ingot() );
		// TODO: finish with radio torches
		/*addRecipeAuto(new ItemStack(ModBlocks.radio_torch_logic, 4), "G", "R", "I", 'G', "dustGlowstone", 'R', Blocks.REDSTONE_TORCH, 'I', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP) );
		addRecipeAuto(new ItemStack(ModBlocks.radio_torch_counter, 4), "G", "R", "I", 'G', "dustGlowstone", 'R', Blocks.REDSTONE_TORCH, 'I', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE) );
		addRecipeAuto(new ItemStack(ModBlocks.radio_torch_reader, 4), " G ", "IRI", 'G', "dustGlowstone", 'R', Blocks.REDSTONE_TORCH, 'I', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE) );
		addRecipeAuto(new ItemStack(ModBlocks.radio_torch_controller, 4), " G ", "IRI", 'G', "dustGlowstone", 'R', Blocks.REDSTONE_TORCH, 'I', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP) );*/
		addRecipeAuto(new ItemStack(ModBlocks.radio_telex, 2), "SCR", "W#W", "WWW", 'S', ModBlocks.radio_torch_sender, 'C', ModItems.crt_display, 'R', ModBlocks.radio_torch_receiver, 'W', KEY_PLANKS, '#', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ANALOG) );

		addRecipeAuto(DictFrame.fromOne(ModItems.conveyor_wand, ItemConveyorWand.ConveyorType.REGULAR, 16), "LLL", "I I", "LLL", 'L', Items.LEATHER, 'I', IRON.ingot() );
		addRecipeAuto(DictFrame.fromOne(ModItems.conveyor_wand, ItemConveyorWand.ConveyorType.REGULAR, 16), "RSR", "I I", "RSR", 'I', IRON.ingot(), 'R', DictFrame.fromOne(ModItems.plant_item, ItemEnums.EnumPlantType.ROPE), 'S', IRON.plate() );
		addRecipeAuto(DictFrame.fromOne(ModItems.conveyor_wand, ItemConveyorWand.ConveyorType.REGULAR, 64), "LLL", "I I", "LLL", 'L', ANY_RUBBER.ingot(), 'I', IRON.ingot() );
		addRecipeAuto(DictFrame.fromOne(ModItems.conveyor_wand, ItemConveyorWand.ConveyorType.EXPRESS, 8), "CCC", "CLC", "CCC", 'C', DictFrame.fromOne(ModItems.conveyor_wand, ItemConveyorWand.ConveyorType.REGULAR), 'L', Fluids.LUBRICANT.getDict(1_000) );
		addRecipeAuto(DictFrame.fromOne(ModItems.conveyor_wand, ItemConveyorWand.ConveyorType.DOUBLE), "CPC", 'C', DictFrame.fromOne(ModItems.conveyor_wand, ItemConveyorWand.ConveyorType.REGULAR), 'P', IRON.plate() );
		addRecipeAuto(DictFrame.fromOne(ModItems.conveyor_wand, ItemConveyorWand.ConveyorType.TRIPLE), "DPC", 'C', DictFrame.fromOne(ModItems.conveyor_wand, ItemConveyorWand.ConveyorType.REGULAR), 'D', DictFrame.fromOne(ModItems.conveyor_wand, ItemConveyorWand.ConveyorType.DOUBLE), 'P', STEEL.plate() );

		addRecipeAuto(new ItemStack(ModBlocks.machine_difurnace_ext, 1), " C ", "BGB", "BGB", 'C', CU.plate(), 'B', ModItems.ingot_firebrick, 'G', ModBlocks.steel_grate );
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.machine_electric_furnace_off), 1), "BBB", "WFW", "RRR", 'B', BE.ingot(), 'R', ModItems.coil_tungsten, 'W', CU.plateCast(), 'F', Item.getItemFromBlock(Blocks.FURNACE) );
		addRecipeAuto(new ItemStack(ModBlocks.red_wire_coated, 16), "WRW", "RIR", "WRW", 'W', ModItems.plate_polymer, 'I', MINGRADE.ingot(), 'R', MINGRADE.wireFine() );
	    // TODO: paintable cables
		addRecipeAuto(new ItemStack(ModBlocks.red_cable_paintable, 16), "WRW", "RIR", "WRW", 'W', STEEL.plate(), 'I', MINGRADE.ingot(), 'R', MINGRADE.wireFine() );
		/*if (ModBlocks.oc_cable_paintable != null)
			addRecipeAuto(new ItemStack(ModBlocks.oc_cable_paintable, 16), "WRW", "RIR", "WRW", 'W', STEEL.plate(), 'I', REDSTONE.dust(), 'R', MINGRADE.wireFine() );*/
		addRecipeAuto(new ItemStack(ModBlocks.cable_switch, 1), "S", "W", 'S', Blocks.LEVER, 'W', ModBlocks.red_wire_coated );
		addRecipeAuto(new ItemStack(ModBlocks.cable_detector, 1), "S", "W", 'S', REDSTONE.dust(), 'W', ModBlocks.red_wire_coated );
		addRecipeAuto(new ItemStack(ModBlocks.cable_diode, 1), " Q ", "CAC", " Q ", 'Q', SI.nugget(), 'C', ModBlocks.red_cable, 'A', AL.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.machine_detector, 1), "IRI", "CTC", "IRI", 'I', ModItems.plate_polymer, 'R', REDSTONE.dust(), 'C', MINGRADE.wireFine(), 'T', ModItems.coil_tungsten );
		addRecipeAuto(new ItemStack(ModBlocks.red_cable, 16), " W ", "RRR", " W ", 'W', ModItems.plate_polymer, 'R', MINGRADE.wireFine() );
		// TODO: I don't know what the fuck red_cable_classic are btw
		/*addShapelessAuto(new ItemStack(ModBlocks.red_cable_classic, 1), ModBlocks.red_cable );
		addShapelessAuto(new ItemStack(ModBlocks.red_cable, 1), ModBlocks.red_cable_classic );*/
		addRecipeAuto(new ItemStack(ModBlocks.red_connector, 4), "C", "I", "S", 'C', ModItems.coil_copper, 'I', ModItems.plate_polymer, 'S', STEEL.ingot() );
		addShapelessAuto(new ItemStack(ModBlocks.red_cable_gauge), ModBlocks.red_wire_coated, STEEL.ingot(), DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC) );
		addRecipeAuto(new ItemStack(ModBlocks.red_pylon, 4), "CWC", "PWP", " T ", 'C', ModItems.coil_copper, 'W', KEY_PLANKS, 'P', ModItems.plate_polymer, 'T', ModBlocks.red_wire_coated );
		addRecipeAuto(new ItemStack(ModBlocks.red_pylon_medium_wood, 2), "CCW", "IIW", "  S", 'C', ModItems.coil_copper, 'W', KEY_PLANKS, 'I', ModItems.plate_polymer, 'S', KEY_COBBLESTONE );
		addShapelessAuto(new ItemStack(ModBlocks.red_pylon_medium_wood_transformer, 1), ModBlocks.red_pylon_medium_wood, ModItems.plate_polymer, ModItems.coil_copper );
		addRecipeAuto(new ItemStack(ModBlocks.red_pylon_medium_steel, 2), "CCW", "IIW", "  S", 'C', ModItems.coil_copper, 'W', STEEL.pipe(), 'I', ModItems.plate_polymer, 'S', KEY_COBBLESTONE );
		addShapelessAuto(new ItemStack(ModBlocks.red_pylon_medium_steel_transformer, 1), ModBlocks.red_pylon_medium_steel, ModItems.plate_polymer, ModItems.coil_copper );
		addRecipeAuto(new ItemStack(ModBlocks.machine_battery_potato, 1), "PCP", "WRW", "PCP", 'P', ItemBattery.getEmptyBattery(ModItems.battery_potato), 'C', CU.ingot(), 'R', REDSTONE.block(), 'W', KEY_PLANKS );
		addRecipeAuto(new ItemStack(ModBlocks.capacitor_bus, 1), "PIP", "PIP", "PIP", 'P', ModItems.plate_polymer, 'I', MINGRADE.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.capacitor_copper, 1), "PPP", "PCP", "WWW", 'P', STEEL.plate(), 'C', CU.block(), 'W', KEY_PLANKS );
		addRecipeAuto(new ItemStack(ModBlocks.capacitor_gold, 1), "PPP", "ICI", "WWW", 'P', STEEL.plate(), 'I', ANY_PLASTIC.ingot(), 'C', GOLD.block(), 'W', STEEL.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.capacitor_niobium, 1), "PPP", "ICI", "WWW", 'P', STEEL.plate(), 'I', RUBBER.ingot(), 'C', NB.block(), 'W', STEEL.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.capacitor_tantalium, 1), "PPP", "ICI", "WWW", 'P', STEEL.plate(), 'I', ANY_RESISTANTALLOY.ingot(), 'C', TA.block(), 'W', STEEL.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.capacitor_schrabidate, 1), "PPP", "ICI", "WWW", 'P', STEEL.plate(), 'I', ANY_RESISTANTALLOY.ingot(), 'C', SBD.block(), 'W', STEEL.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.machine_wood_burner, 1), "PPP", "CFC", "I I" , 'P', STEEL.plate528(), 'C', ModItems.coil_copper, 'I', IRON.ingot(), 'F', Blocks.FURNACE);
		addRecipeAuto(new ItemStack(ModBlocks.machine_turbine, 1), "SMS", "PTP", "SMS", 'S', STEEL.ingot(), 'T', ModItems.turbine_titanium, 'M', ModItems.coil_copper, 'P', ANY_PLASTIC.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.crate_template, 1), "IPI", "P P", "IPI", 'I', IRON.ingot(), 'P', Items.PAPER );
		addRecipeAuto(new ItemStack(ModBlocks.crate_iron, 1), "PPP", "I I", "III", 'P', IRON.plate(), 'I', IRON.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.crate_steel, 1), "PPP", "I I", "III", 'P', STEEL.plate(), 'I', STEEL.ingot() );

		addRecipeAuto(new ItemStack(ModBlocks.machine_battery_socket), "I I", "I I", "IRI", 'I', ModItems.plate_polymer, 'R', ModItems.coil_copper);
		addRecipeAuto(new ItemStack(ModBlocks.machine_battery_socket), "PRP", 'P', STEEL.plate(), 'R', MINGRADE.ingot());
		addRecipeAuto(new ItemStack(ModItems.battery_pack, 1, ItemBatteryPack.EnumBatteryPack.BATTERY_REDSTONE.ordinal()), "IRI", "PRP", "IRI", 'I', IRON.plate(), 'R', REDSTONE.block(), 'P', ModItems.plate_polymer);
		addRecipeAuto(new ItemStack(ModItems.battery_pack, 1, ItemBatteryPack.EnumBatteryPack.CAPACITOR_COPPER.ordinal()), "IRI", "PRP", "IRI", 'I', STEEL.plate(), 'R', CU.block(), 'P', ModItems.plate_polymer);

		addRecipeAuto(new ItemStack(ModItems.battery_sc, 1, ItemBatterySC.EnumBatterySC.EMPTY.ordinal()), "PGP", "L L", "PGP", 'P', ANY_PLASTIC.ingot(), 'G', GOLD.wireFine(), 'L', PB.plate());
		addShapelessAuto(new ItemStack(ModItems.battery_sc, 1, ItemBatterySC.EnumBatterySC.WASTE.ordinal()), DictFrame.fromOne(ModItems.battery_sc, ItemBatterySC.EnumBatterySC.EMPTY), ModItems.billet_nuclear_waste, ModItems.billet_nuclear_waste);
		addShapelessAuto(new ItemStack(ModItems.battery_sc, 1, ItemBatterySC.EnumBatterySC.RA226.ordinal()), DictFrame.fromOne(ModItems.battery_sc, ItemBatterySC.EnumBatterySC.EMPTY), RA226.billet(), RA226.billet());
		addShapelessAuto(new ItemStack(ModItems.battery_sc, 1, ItemBatterySC.EnumBatterySC.TC99.ordinal()), DictFrame.fromOne(ModItems.battery_sc, ItemBatterySC.EnumBatterySC.EMPTY), TC99.billet(), TC99.billet());
		addShapelessAuto(new ItemStack(ModItems.battery_sc, 1, ItemBatterySC.EnumBatterySC.CO60.ordinal()), DictFrame.fromOne(ModItems.battery_sc, ItemBatterySC.EnumBatterySC.EMPTY), CO60.billet(), CO60.billet());
		addShapelessAuto(new ItemStack(ModItems.battery_sc, 1, ItemBatterySC.EnumBatterySC.PU238.ordinal()), DictFrame.fromOne(ModItems.battery_sc, ItemBatterySC.EnumBatterySC.EMPTY), PU238.billet(), PU238.billet());
		addShapelessAuto(new ItemStack(ModItems.battery_sc, 1, ItemBatterySC.EnumBatterySC.PO210.ordinal()), DictFrame.fromOne(ModItems.battery_sc, ItemBatterySC.EnumBatterySC.EMPTY), PO210.billet(), PO210.billet());
		addShapelessAuto(new ItemStack(ModItems.battery_sc, 1, ItemBatterySC.EnumBatterySC.AU198.ordinal()), DictFrame.fromOne(ModItems.battery_sc, ItemBatterySC.EnumBatterySC.EMPTY), AU198.billet(), AU198.billet());
		addShapelessAuto(new ItemStack(ModItems.battery_sc, 1, ItemBatterySC.EnumBatterySC.PB209.ordinal()), DictFrame.fromOne(ModItems.battery_sc, ItemBatterySC.EnumBatterySC.EMPTY), PB209.billet(), PB209.billet());
		addShapelessAuto(new ItemStack(ModItems.battery_sc, 1, ItemBatterySC.EnumBatterySC.AM241.ordinal()), DictFrame.fromOne(ModItems.battery_sc, ItemBatterySC.EnumBatterySC.EMPTY), AM241.billet(), AM241.billet());

		// Note: doesn't preserve storage because a crate's contents are different items, but a mass storage's is just one
		addRecipeAuto(new ItemStack(ModBlocks.mass_storage_iron), " L ", "ICI", " I ", 'I', TI.ingot(), 'C', ModBlocks.crate_steel, 'L', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE) );
		addRecipeAuto(new ItemStack(ModBlocks.mass_storage_wood), "PPP", "PIP", "PPP", 'P', KEY_PLANKS, 'I', IRON.plate() );

		addRecipeAuto(new ItemStack(ModBlocks.machine_autocrafter, 1), "SCS", "MWM", "SCS", 'S', STEEL.plate(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE), 'M', ModItems.motor, 'W', Blocks.CRAFTING_TABLE );
		// TODO
		//addRecipeAuto(new ItemStack(ModBlocks.machine_funnel, 1), "S S", "SRS", " S ", 'S', STEEL.ingot(), 'R', REDSTONE.dust() );
		addRecipeAuto(new ItemStack(ModBlocks.machine_waste_drum, 1), "LRL", "BRB", "LRL", 'L', PB.ingot(), 'B', Blocks.IRON_BARS, 'R', ModItems.rod_quad_empty );
		addRecipeAuto(new ItemStack(ModBlocks.machine_press, 1), "IRI", "IPI", "IBI", 'I', IRON.ingot(), 'R', Blocks.FURNACE, 'B', IRON.block(), 'P', Blocks.PISTON );
		addRecipeAuto(new ItemStack(ModBlocks.machine_ammo_press, 1), "IPI", "C C", "SSS", 'I', IRON.ingot(), 'P', Blocks.PISTON, 'C', CU.ingot(), 'S', Blocks.STONE );
		addRecipeAuto(new ItemStack(ModBlocks.machine_siren, 1), "SIS", "ICI", "SRS", 'S', STEEL.plate(), 'I', ANY_RUBBER.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE), 'R', REDSTONE.dust() );
		addRecipeAuto(new ItemStack(ModBlocks.machine_microwave, 1), "III", "SGM", "IDI", 'I', ModItems.plate_polymer, 'S', STEEL.plate(), 'G', KEY_ANYPANE, 'M', ModItems.magnetron, 'D', ModItems.motor );
		addRecipeAuto(new ItemStack(ModBlocks.machine_solar_boiler), "SHS", "DHD", "SHS", 'S', STEEL.ingot(), 'H', STEEL.shell(), 'D', KEY_BLACK );
		addRecipeAuto(new ItemStack(ModBlocks.solar_mirror, 3), "AAA", " B ", "SSS", 'A', AL.plate(), 'B', ModBlocks.steel_beam, 'S', STEEL.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.anvil_iron, 1), "III", " B ", "III", 'I', IRON.ingot(), 'B', IRON.block() );
		addRecipeAuto(new ItemStack(ModBlocks.anvil_lead, 1), "III", " B ", "III", 'I', PB.ingot(), 'B', PB.block() );
		addRecipeAuto(new ItemStack(ModBlocks.anvil_murky, 1), "UUU", "UAU", "UUU", 'U', ModItems.undefined, 'A', ModBlocks.anvil_steel );
		addRecipeAuto(new ItemStack(ModBlocks.machine_fraction_tower), "H", "G", "H", 'H', STEEL.plateWelded(), 'G', ModBlocks.steel_grate );
		addRecipeAuto(new ItemStack(ModBlocks.fraction_spacer), "BHB", 'H', STEEL.shell(), 'B', Blocks.IRON_BARS );
		addRecipeAuto(new ItemStack(ModBlocks.machine_furnace_brick_off), "III", "I I", "BBB", 'I', Items.BRICK, 'B', Blocks.STONE );
		addRecipeAuto(new ItemStack(ModBlocks.furnace_iron), "III", "IFI", "BBB", 'I', IRON.ingot(), 'F', Blocks.FURNACE, 'B', Blocks.STONEBRICK );
		addRecipeAuto(new ItemStack(ModBlocks.machine_mixer), "PIP", "GCG", "PMP", 'P', STEEL.plate(), 'I', DURA.ingot(), 'G', KEY_ANYPANE, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE), 'M', ModItems.motor );
		addRecipeAuto(new ItemStack(ModBlocks.fan), "BPB", "PRP", "BPB", 'B', STEEL.bolt(), 'P', IRON.plate(), 'R', REDSTONE.dust() );
		// TODO
		//addRecipeAuto(new ItemStack(ModBlocks.piston_inserter), "ITI", "TPT", "ITI", 'P', DictFrame.fromOne(ModItems.part_generic, EnumPartType.PISTON_PNEUMATIC), 'I', IRON.plate(), 'T', STEEL.bolt() );

		addRecipeAuto(new ItemStack(ModItems.upgrade_muffler, 16), "III", "IWI", "III", 'I', ANY_RUBBER.ingot(), 'W', Blocks.WOOL );
		addRecipeAuto(new ItemStack(ModItems.upgrade_template, 1), "WIW", "PCP", "WIW", 'W', CU.wireFine(), 'I', IRON.plate(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ANALOG), 'P', ModItems.plate_polymer );
		addRecipeAuto(new ItemStack(ModItems.upgrade_template, 1), "WIW", "PCP", "WIW", 'W', CU.wireFine(), 'I', ANY_PLASTIC.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), 'P', ModItems.plate_polymer );

		addRecipeAuto(DictFrame.fromOne(ModItems.arc_electrode, ItemArcElectrode.EnumElectrodeType.GRAPHITE), "C", "T", "C", 'C', GRAPHITE.ingot(), 'T', STEEL.bolt() );
		addRecipeAuto(DictFrame.fromOne(ModItems.arc_electrode, ItemArcElectrode.EnumElectrodeType.GRAPHITE), "C", "T", "C", 'C', PETCOKE.gem(), 'T', ANY_TAR.any() );
		addRecipeAuto(DictFrame.fromOne(ModItems.arc_electrode, ItemArcElectrode.EnumElectrodeType.LANTHANIUM), "C", "T", "C", 'C', LA.ingot(), 'T', KEY_BRICK );
		addRecipeAuto(DictFrame.fromOne(ModItems.arc_electrode, ItemArcElectrode.EnumElectrodeType.DESH), "C", "T", "C", 'C', DESH.ingot(), 'T', TI.ingot() );
		addRecipeAuto(DictFrame.fromOne(ModItems.arc_electrode, ItemArcElectrode.EnumElectrodeType.DESH), "C", "T", "C", 'C', DESH.ingot(), 'T', W.ingot() );
		addRecipeAuto(DictFrame.fromOne(ModItems.arc_electrode, ItemArcElectrode.EnumElectrodeType.SATURNITE), "C", "T", "C", 'C', BIGMT.ingot(), 'T', NB.ingot() );

		addRecipeAuto(new ItemStack(ModItems.detonator, 1), "C", "S", 'S', STEEL.plate(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC));
		addShapelessAuto(new ItemStack(ModItems.detonator_multi, 1), ModItems.detonator, DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED) );
		addShapelessAuto(new ItemStack(ModItems.detonator_laser, 1), ModItems.rangefinder, DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), RUBBER.ingot(), GOLD.wireDense() );
		addShapelessAuto(new ItemStack(ModItems.detonator_deadman, 1), ModItems.detonator, ModItems.defuser, ModItems.ducttape );
		addRecipeAuto(new ItemStack(ModItems.detonator_de, 1), "T", "D", "T", 'T', Blocks.TNT, 'D', ModItems.detonator_deadman );

		// These are disabled because we have SAFE
		// nope it was killed in 3454ffdc86b639b861d86016a5885d93ffd0d542
		addRecipeAuto(new ItemStack(ModItems.singularity, 1), "ESE", "SBS", "ESE", 'E', EUPH.nugget(), 'S', new ItemStack(ModItems.cell, 1, Fluids.AMAT.getID()), 'B', SA326.block() );
		addRecipeAuto(new ItemStack(ModItems.singularity_counter_resonant, 1), "CTC", "TST", "CTC", 'C', CMB.plate(), 'T', MAGTUNG.ingot(), 'S', ModItems.singularity );
		addRecipeAuto(new ItemStack(ModItems.singularity_super_heated, 1), "CTC", "TST", "CTC", 'C', ALLOY.plate(), 'T', ModItems.powder_power, 'S', ModItems.singularity );
		addRecipeAuto(new ItemStack(ModItems.black_hole, 1), "SSS", "SCS", "SSS", 'C', ModItems.singularity, 'S', ModItems.crystal_xen );
		addRecipeAuto(new ItemStack(ModItems.crystal_xen, 1), "EEE", "EIE", "EEE", 'E', ModItems.powder_power, 'I', EUPH.ingot() );

		addShapelessAuto(new ItemStack(ModItems.fuse, 1), STEEL.plate(), ModItems.plate_polymer, W.wireFine() );
		addShapelessAuto(new ItemStack(ModItems.overfuse, 1), STEEL.bolt(), NP237.dust(), I.dust(), TH232.dust(), AT.dust(), ND.dust(), CU.plateCast(), ModItems.black_hole, CS.dust() );
		addShapelessAuto(new ItemStack(ModItems.overfuse, 1), STEEL.bolt(), SR.dust(), BR.dust(), CO.dust(), TS.dust(), NB.dust(), CU.plateCast(), ModItems.black_hole, CE.dust() );

		addRecipeAuto(new ItemStack(ModItems.blades_steel, 1), " P ", "PIP", " P ", 'P', STEEL.plate(), 'I', STEEL.ingot() );
		addRecipeAuto(new ItemStack(ModItems.blades_titanium, 1), " P ", "PIP", " P ", 'P', TI.plate(), 'I', TI.ingot() );
		addRecipeAuto(new ItemStack(ModItems.blades_advanced_alloy, 1), " P ", "PIP", " P ", 'P', ALLOY.plate(), 'I', ALLOY.ingot() );
		addRecipeAuto(new ItemStack(ModItems.blades_desh, 1), " P ", "PBP", " P ", 'P', ModItems.plate_desh, 'B', ModItems.blades_advanced_alloy ); //4 desh ingots still needed to do anything

		addRecipeAuto(new ItemStack(ModItems.blades_steel, 1), "PIP", 'P', STEEL.plate(), 'I', new ItemStack(ModItems.blades_steel, 1, OreDictionary.WILDCARD_VALUE) );
		addRecipeAuto(new ItemStack(ModItems.blades_titanium, 1), "PIP", 'P', TI.plate(), 'I', new ItemStack(ModItems.blades_titanium, 1, OreDictionary.WILDCARD_VALUE) );
		addRecipeAuto(new ItemStack(ModItems.blades_advanced_alloy, 1), "PIP", 'P', ALLOY.plate(), 'I', new ItemStack(ModItems.blades_advanced_alloy, 1, OreDictionary.WILDCARD_VALUE) );
		addRecipeAuto(new ItemStack(ModItems.laser_crystal_co2, 1), "QDQ", "NCN", "QDQ", 'Q', ModBlocks.glass_quartz, 'D', DESH.ingot(), 'N', NB.ingot(), 'C', new ItemStack(ModItems.fluid_tank_full, 1, Fluids.CARBONDIOXIDE.getID()) );
		addRecipeAuto(new ItemStack(ModItems.laser_crystal_bismuth, 1), "QUQ", "BCB", "QTQ", 'Q', ModBlocks.glass_quartz, 'U', U.ingot(), 'T', TH232.ingot(), 'B', ModItems.nugget_bismuth, 'C', ModItems.crystal_rare );
		addRecipeAuto(new ItemStack(ModItems.laser_crystal_cmb, 1), "QBQ", "CSC", "QBQ", 'Q', ModBlocks.glass_quartz, 'B', CMB.ingot(), 'C', SBD.ingot(), 'S', new ItemStack(ModItems.cell, 1, Fluids.AMAT.getID()) );
		addRecipeAuto(new ItemStack(ModItems.laser_crystal_bale, 1), "QDQ", "SBS", "QDQ", 'Q', ModBlocks.glass_quartz, 'D', DNT.ingot(), 'B', ModItems.egg_balefire, 'S', ModItems.powder_spark_mix );
		addRecipeAuto(new ItemStack(ModItems.laser_crystal_digamma, 1), "QUQ", "UEU", "QUQ", 'Q', ModBlocks.glass_quartz, 'U', ModItems.undefined, 'E', ModItems.ingot_electronium  );

		Item[] bricks = new Item[] {Items.BRICK, Items.NETHERBRICK};

		for(Item brick : bricks) {
			addRecipeAuto(new ItemStack(ModItems.stamp_stone_flat, 1), "III", "SSS", 'I', brick, 'S', "stone" );
			addRecipeAuto(new ItemStack(ModItems.stamp_iron_flat, 1), "III", "SSS", 'I', brick, 'S', IRON.ingot() );
			addRecipeAuto(new ItemStack(ModItems.stamp_steel_flat, 1), "III", "SSS", 'I', brick, 'S', STEEL.ingot() );
			addRecipeAuto(new ItemStack(ModItems.stamp_titanium_flat, 1), "III", "SSS", 'I', brick, 'S', TI.ingot() );
			addRecipeAuto(new ItemStack(ModItems.stamp_obsidian_flat, 1), "III", "SSS", 'I', brick, 'S', Blocks.OBSIDIAN );
			addRecipeAuto(new ItemStack(ModItems.stamp_desh_flat, 1), "BDB", "DSD", "BDB", 'B', brick, 'D', DESH.ingot(), 'S', FERRO.ingot() );
		}

		addRecipeAuto(new ItemStack(ModBlocks.watz_pump, 1), "MPM", "PCP", "PSP", 'M', ModItems.motor_desh, 'P', ANY_RESISTANTALLOY.plateCast(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BISMOID), 'S', ModItems.pipes_steel );

		addRecipeAuto(new ItemStack(ModBlocks.reinforced_stone, 4), "FBF", "BFB", "FBF", 'F', Blocks.COBBLESTONE, 'B', Blocks.STONE );
		addRecipeAuto(new ItemStack(ModBlocks.brick_light, 4), "FBF", "BFB", "FBF", 'F', "fenceWood", 'B', Blocks.BRICK_BLOCK );
		addRecipeAuto(new ItemStack(ModBlocks.brick_asbestos, 2), " A ", "ABA", " A ", 'B', ModBlocks.brick_light, 'A', ASBESTOS.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.concrete, 4), "CC", "CC", 'C', ModBlocks.concrete_smooth );
		addRecipeAuto(new ItemStack(ModBlocks.concrete_pillar, 6), "CBC", "CBC", "CBC", 'C', ModBlocks.concrete_smooth, 'B', Blocks.IRON_BARS );
		addRecipeAuto(new ItemStack(ModBlocks.brick_concrete, 4), " C ", "CBC", " C ", 'C', ModBlocks.concrete_smooth, 'B', Items.CLAY_BALL );
		addRecipeAuto(new ItemStack(ModBlocks.brick_concrete, 4), " C ", "CBC", " C ", 'C', ModBlocks.concrete, 'B', Items.CLAY_BALL );
		addRecipeAuto(new ItemStack(ModBlocks.brick_concrete_mossy, 8), "CCC", "CVC", "CCC", 'C', ModBlocks.brick_concrete, 'V', Blocks.VINE );
		addRecipeAuto(new ItemStack(ModBlocks.brick_concrete_cracked, 6), " C " , "C C", " C ", 'C', ModBlocks.brick_concrete );
		addRecipeAuto(new ItemStack(ModBlocks.brick_concrete_broken, 6), " C " , "C C", " C ", 'C', ModBlocks.brick_concrete_cracked );
		addRecipeAuto(new ItemStack(ModBlocks.ducrete, 4), "DD", "DD", 'D', ModBlocks.ducrete_smooth );
		addRecipeAuto(new ItemStack(ModBlocks.brick_ducrete, 4), "CDC", "DLD", "CDC", 'D', ModBlocks.ducrete_smooth, 'C', Items.CLAY_BALL, 'L', ModItems.plate_lead );
		addRecipeAuto(new ItemStack(ModBlocks.brick_ducrete, 4), "CDC", "DLD", "CDC", 'D', ModBlocks.ducrete, 'C', Items.CLAY_BALL, 'L', ModItems.plate_lead );
		addRecipeAuto(new ItemStack(ModBlocks.reinforced_ducrete, 4), "DSD", "SUS", "DSD", 'D', ModBlocks.brick_ducrete, 'S', ModItems.plate_steel, 'U', U238.billet() );
		addRecipeAuto(new ItemStack(ModBlocks.brick_obsidian, 4), "FBF", "BFB", "FBF", 'F', Blocks.IRON_BARS, 'B', Blocks.OBSIDIAN );
		addRecipeAuto(new ItemStack(ModBlocks.meteor_polished, 4), "CC", "CC", 'C', ModBlocks.block_meteor_broken );
		addRecipeAuto(new ItemStack(ModBlocks.meteor_pillar, 2), "C", "C", 'C', ModBlocks.meteor_polished );
		addRecipeAuto(new ItemStack(ModBlocks.meteor_brick, 4), "CC", "CC", 'C', ModBlocks.meteor_polished );
		addRecipeAuto(new ItemStack(ModBlocks.meteor_brick_mossy, 8), "CCC", "CVC", "CCC", 'C', ModBlocks.meteor_brick, 'V', Blocks.VINE );
		addRecipeAuto(new ItemStack(ModBlocks.meteor_brick_cracked, 6), " C " , "C C", " C ", 'C', ModBlocks.meteor_brick );
		addRecipeAuto(new ItemStack(ModBlocks.meteor_battery, 1), "MSM", "MWM", "MSM", 'M', ModBlocks.meteor_polished, 'S', STAR.block(), 'W', SA326.wireFine() );
		addRecipeAuto(new ItemStack(ModBlocks.tile_lab, 4), "CBC", "CBC", "CBC", 'C', Items.BRICK, 'B', ASBESTOS.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.tile_lab_cracked, 6), " C " , "C C", " C ", 'C', ModBlocks.tile_lab );
		addRecipeAuto(new ItemStack(ModBlocks.tile_lab_broken, 6), " C " , "C C", " C ", 'C', ModBlocks.tile_lab_cracked );
		addShapelessAuto(new ItemStack(ModBlocks.asphalt_light, 1), ModBlocks.asphalt, Items.GLOWSTONE_DUST );
		addShapelessAuto(new ItemStack(ModBlocks.asphalt, 1), ModBlocks.asphalt_light );

		String[] dyes = { "Black", "Red", "Green", "Brown", "Blue", "Purple", "Cyan", "LightGray", "Gray", "Pink", "Lime", "Yellow", "LightBlue", "Magenta", "Orange", "White" };

		for(int i = 0; i < 16; i++) {
			String dyeName = "dye" + dyes[15 - i];
			addRecipeAuto(new ItemStack(ModBlocks.concrete_colored, 8, i), "CCC", "CDC", "CCC", 'C', ModBlocks.concrete_smooth, 'D', dyeName );
		}
		addShapelessAuto(new ItemStack(ModBlocks.concrete_smooth, 1), "unknownConcrete");

		addRecipeAuto(new ItemStack(ModBlocks.concrete_colored_ext, 6, EnumConcreteType.MACHINE.ordinal()), "CCC", "1 2", "CCC", 'C', ModBlocks.concrete_smooth, '1', KEY_BROWN, '2', KEY_GRAY );
		addRecipeAuto(new ItemStack(ModBlocks.concrete_colored_ext, 6, EnumConcreteType.MACHINE_STRIPE.ordinal()), "CCC", "1 2", "CCC", 'C', ModBlocks.concrete_smooth, '1', KEY_BROWN, '2', KEY_BLACK );
		addRecipeAuto(new ItemStack(ModBlocks.concrete_colored_ext, 6, EnumConcreteType.INDIGO.ordinal()), "CCC", "1 2", "CCC", 'C', ModBlocks.concrete_smooth, '1', KEY_BLUE, '2', KEY_PURPLE );
		addRecipeAuto(new ItemStack(ModBlocks.concrete_colored_ext, 6, EnumConcreteType.PURPLE.ordinal()), "CCC", "1 2", "CCC", 'C', ModBlocks.concrete_smooth, '1', KEY_PURPLE, '2', KEY_PURPLE );
		addRecipeAuto(new ItemStack(ModBlocks.concrete_colored_ext, 6, EnumConcreteType.PINK.ordinal()), "CCC", "1 2", "CCC", 'C', ModBlocks.concrete_smooth, '1', KEY_PINK, '2', KEY_RED );
		addRecipeAuto(new ItemStack(ModBlocks.concrete_colored_ext, 6, EnumConcreteType.HAZARD.ordinal()), "CCC", "1 2", "CCC", 'C', ModBlocks.concrete_smooth, '1', KEY_YELLOW, '2', KEY_BLACK );
		addRecipeAuto(new ItemStack(ModBlocks.concrete_colored_ext, 6, EnumConcreteType.SAND.ordinal()), "CCC", "1 2", "CCC", 'C', ModBlocks.concrete_smooth, '1', KEY_YELLOW, '2', KEY_GRAY );
		addRecipeAuto(new ItemStack(ModBlocks.concrete_colored_ext, 6, EnumConcreteType.BRONZE.ordinal()), "CCC", "1 2", "CCC", 'C', ModBlocks.concrete_smooth, '1', KEY_ORANGE, '2', KEY_BROWN );

		addRecipeAuto(new ItemStack(ModBlocks.gneiss_tile, 4), "CC", "CC", 'C', ModBlocks.stone_gneiss );
		addRecipeAuto(new ItemStack(ModBlocks.gneiss_brick, 4), "CC", "CC", 'C', ModBlocks.gneiss_tile );
		addShapelessAuto(new ItemStack(ModBlocks.gneiss_chiseled, 1), ModBlocks.gneiss_tile );
		addRecipeAuto(new ItemStack(ModBlocks.depth_brick, 4), "CC", "CC", 'C', ModBlocks.stone_depth );
		addRecipeAuto(new ItemStack(ModBlocks.depth_tiles, 4), "CC", "CC", 'C', ModBlocks.depth_brick );
		addRecipeAuto(new ItemStack(ModBlocks.depth_nether_brick, 4), "CC", "CC", 'C', ModBlocks.stone_depth_nether );
		addRecipeAuto(new ItemStack(ModBlocks.depth_nether_tiles, 4), "CC", "CC", 'C', ModBlocks.depth_nether_brick );
		addRecipeAuto(new ItemStack(ModBlocks.basalt_polished, 4), "CC", "CC", 'C', ModBlocks.basalt_smooth );
		addRecipeAuto(new ItemStack(ModBlocks.basalt_brick, 4), "CC", "CC", 'C', ModBlocks.basalt_polished );
		addRecipeAuto(new ItemStack(ModBlocks.basalt_tiles, 4), "CC", "CC", 'C', ModBlocks.basalt_brick );
		addShapelessAuto(new ItemStack(ModBlocks.lightstone, 4), Blocks.STONE, Blocks.STONE, Blocks.STONE, ModItems.powder_limestone );
		addRecipeAuto(new ItemStack(ModBlocks.lightstone, 4, LightstoneType.TILE.ordinal()), "CC", "CC", 'C', new ItemStack(ModBlocks.lightstone, 1, 0) );
		addRecipeAuto(new ItemStack(ModBlocks.lightstone, 4, LightstoneType.BRICKS.ordinal()), "CC", "CC", 'C', new ItemStack(ModBlocks.lightstone, 1, LightstoneType.TILE.ordinal()) );
		addShapelessAuto(new ItemStack(ModBlocks.lightstone, 1, LightstoneType.BRICKS_CHISELED.ordinal()), new ItemStack(ModBlocks.lightstone, 1, LightstoneType.BRICKS.ordinal()) );
		addShapelessAuto(new ItemStack(ModBlocks.lightstone, 1, LightstoneType.CHISELED.ordinal()), new ItemStack(ModBlocks.lightstone, 1, LightstoneType.TILE.ordinal()) );

		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.reinforced_brick), 4), "FBF", "BFB", "FBF", 'F', Blocks.IRON_BARS, 'B', ModBlocks.brick_concrete );
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.brick_compound), 4), "FBF", "BTB", "FBF", 'F', STEEL.bolt(), 'B', ModBlocks.reinforced_brick, 'T', ANY_TAR.any() );
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.reinforced_glass), 4), "FBF", "BFB", "FBF", 'F', Blocks.IRON_BARS, 'B', Blocks.GLASS );
		// TODO: wtf laminate?
		/*addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.reinforced_glass_pane), 16), "   ", "GGG", "GGG", 'G', ModBlocks.reinforced_glass);
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.reinforced_laminate_pane), 16), "   ", "LLL", "LLL", 'L', ModBlocks.reinforced_laminate);*/
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.reinforced_light), 1), "FFF", "FBF", "FFF", 'F', Blocks.IRON_BARS, 'B', Blocks.GLOWSTONE );
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.reinforced_lamp_off), 1), "FFF", "FBF", "FFF", 'F', Blocks.IRON_BARS, 'B', Blocks.REDSTONE_LAMP );
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.reinforced_sand), 4), "FBF", "BFB", "FBF", 'F', Blocks.IRON_BARS, 'B', Blocks.SANDSTONE );
		// TODO
		/*addShapelessAuto(new ItemStack(ModBlocks.lamp_tritium_green_off, 1), KEY_ANYGLASS, P_RED.dust(), Fluids.TRITIUM.getDict(1_000), S.dust() );
		addShapelessAuto(new ItemStack(ModBlocks.lamp_tritium_blue_off, 1), KEY_ANYGLASS, P_RED.dust(), Fluids.TRITIUM.getDict(1_000), AL.dust() );*/
		addRecipeAuto(new ItemStack(ModBlocks.lantern, 1), "PGP", " S ", " S ", 'P', KEY_ANYPANE, 'G', Items.GLOWSTONE_DUST, 'S', ModBlocks.steel_beam );
		/*addRecipeAuto(new ItemStack(ModBlocks.spotlight_incandescent, 8), "G", "T", "I", 'G', KEY_ANYPANE, 'T', W.wireFine(), 'I', IRON.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.spotlight_fluoro, 8), "G", "M", "A", 'G', KEY_ANYPANE, 'M', ModItems.ingot_mercury, 'A', ModItems.plate_aluminium );
		addRecipeAuto(new ItemStack(ModBlocks.spotlight_halogen, 8), "G", "B", "S", 'G', KEY_ANYPANE, 'B', ModItems.powder_bromine, 'S', STEEL.plate() );
		addRecipeAuto(new ItemStack(ModBlocks.floodlight, 2), "CSC", "TST", "G G", 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CAPACITOR), 'S', STEEL.plate(), 'T', ModItems.coil_tungsten, 'G', KEY_ANYPANE );*/

		addRecipeAuto(new ItemStack(ModBlocks.barbed_wire, 16), "AIA", "I I", "AIA", 'A', STEEL.wireFine(), 'I', IRON.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.barbed_wire_fire, 8), "BBB", "BIB", "BBB", 'B', ModBlocks.barbed_wire, 'I', P_RED.dust() );
		addRecipeAuto(new ItemStack(ModBlocks.barbed_wire_poison, 8), "BBB", "BIB", "BBB", 'B', ModBlocks.barbed_wire, 'I', ModItems.powder_poison );
		addRecipeAuto(new ItemStack(ModBlocks.barbed_wire_acid, 8), "BBB", "BIB", "BBB", 'B', ModBlocks.barbed_wire, 'I', new ItemStack(ModItems.fluid_tank_full, 1, Fluids.PEROXIDE.getID()) );
		addRecipeAuto(new ItemStack(ModBlocks.barbed_wire_wither, 8), "BBB", "BIB", "BBB", 'B', ModBlocks.barbed_wire, 'I', new ItemStack(Items.SKULL, 1, 1) );
		addRecipeAuto(new ItemStack(ModBlocks.barbed_wire_ultradeath, 4), "BCB", "CIC", "BCB", 'B', ModBlocks.barbed_wire, 'C', ModItems.powder_yellowcake, 'I', ModItems.nuclear_waste );
		addShapelessAuto(new ItemStack(ModBlocks.sandbags, 4), ModItems.plate_polymer, KEY_SAND, KEY_SAND, KEY_SAND );

		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.tape_recorder), 4), "TST", "SSS", 'T', W.ingot(), 'S', STEEL.ingot() );
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.steel_poles), 16), "S S", "SSS", "S S", 'S', STEEL.ingot() );
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.pole_top), 1), "T T", "TRT", "BBB", 'T', W.ingot(), 'B', BE.ingot(), 'R', MINGRADE.ingot() );
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.pole_satellite_receiver), 1), "SS ", "SCR", "SS ", 'S', STEEL.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE), 'R', MINGRADE.wireFine() );
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.steel_beam), 8), "S", "S", "S", 'S', STEEL.ingot() );
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.steel_wall), 4), "SSS", "SSS", 'S', STEEL.ingot() );
		addShapelessAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.steel_corner)), ModBlocks.steel_wall, ModBlocks.steel_wall );
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.steel_roof), 2), "SSS", 'S', STEEL.ingot() );
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.steel_scaffold), 8), "SSS", " S ", "SSS", 'S', STEEL.ingot() );
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.steel_beam), 8), "S", "S", "S", 'S', ModBlocks.steel_scaffold );
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.chain), 8), "S", "S", "S", 'S', ModBlocks.steel_beam );
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.steel_grate), 4), "SS", "SS", 'S', ModBlocks.steel_beam );
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.steel_grate_wide), 4), "SS", 'S', ModBlocks.steel_grate );
		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.steel_grate), 1), "SS", 'S', ModBlocks.steel_grate_wide );
		addRecipeAuto(new ItemStack(ModBlocks.rebar, 8), "BB", "BB", 'B', STEEL.bolt() );

		addRecipeAuto(new ItemStack(ModBlocks.steel_scaffold, 8, 0), "SSS", "SDS", "SSS", 'S', ModBlocks.steel_scaffold, 'D', "dyeGray" );
		addRecipeAuto(new ItemStack(ModBlocks.steel_scaffold, 8, 1), "SSS", "SDS", "SSS", 'S', ModBlocks.steel_scaffold, 'D', "dyeRed" );
		addRecipeAuto(new ItemStack(ModBlocks.steel_scaffold, 8, 2), "SSS", "SDS", "SSS", 'S', ModBlocks.steel_scaffold, 'D', "dyeWhite" );
		addRecipeAuto(new ItemStack(ModBlocks.steel_scaffold, 8, 3), "SSS", "SDS", "SSS", 'S', ModBlocks.steel_scaffold, 'D', "dyeYellow" );
		// TODO: wood structures
		addRecipeAuto(new ItemStack(ModBlocks.wood_barrier, 8), "SFS", "SFS", 'S', KEY_SLAB, 'F', "fenceWood" );
//		addRecipeAuto(DictFrame.fromOne(ModBlocks.wood_structure, EnumWoodStructure.ROOF, 16), "SSS", "F F", 'S', KEY_SLAB, 'F', "fenceWood" );
//		addRecipeAuto(DictFrame.fromOne(ModBlocks.wood_structure, EnumWoodStructure.CEILING, 16), "F F", "SSS", 'S', KEY_SLAB, 'F', "fenceWood" );
//		addRecipeAuto(DictFrame.fromOne(ModBlocks.wood_structure, EnumWoodStructure.SCAFFOLD, 4), "SSS", "F F", "F F", 'S', KEY_SLAB, 'F', "fenceWood" );

		reg2();

	}

	public static void reg2(){

		addRecipeAuto(new ItemStack(ModBlocks.sat_dock, 1), "SSS", "PCP", 'S', STEEL.ingot(), 'P', ANY_PLASTIC.ingot(), 'C', ModBlocks.crate_iron );
		addRecipeAuto(new ItemStack(ModBlocks.book_guide, 1), "IBI", "LBL", "IBI", 'B', Items.BOOK, 'I', KEY_BLACK, 'L', KEY_BLUE );
		// TODO: rails?..
		/*addRecipeAuto(new ItemStack(ModBlocks.rail_wood, 16), "S S", "SRS", "S S", 'S', Items.STICK, 'R', DictFrame.fromOne(ModItems.plant_item, ItemEnums.EnumPlantType.ROPE) );
		addRecipeAuto(new ItemStack(ModBlocks.rail_narrow, 64), "S S", "S S", "S S", 'S', ModBlocks.steel_beam );*/
		addRecipeAuto(new ItemStack(ModBlocks.rail_highspeed, 16), "S S", "SIS", "S S", 'S', STEEL.ingot(), 'I', IRON.plate() );
		addRecipeAuto(new ItemStack(ModBlocks.rail_booster, 6), "S S", "CIC", "SRS", 'S', STEEL.ingot(), 'I', IRON.plate(), 'R', MINGRADE.ingot(), 'C', ModItems.coil_copper );

		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.bomb_multi), 1), "AAD", "CHF", "AAD", 'A', AL.wireFine(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), 'H', AL.shell(), 'F', ModItems.fins_quad_titanium, 'D', KEY_WHITE );
		addShapelessAuto(new ItemStack(ModItems.powder_ice, 4), Items.SNOWBALL, KNO.dust(), REDSTONE.dust() );
		addShapelessAuto(new ItemStack(ModItems.powder_poison, 4), Items.SPIDER_EYE, REDSTONE.dust(), NETHERQUARTZ.gem() );
		addShapelessAuto(new ItemStack(ModItems.pellet_gas, 2), Items.WATER_BUCKET, "dustGlowstone", STEEL.plate() );

		addRecipeAuto(new ItemStack(ModItems.flame_pony, 1), " O ", "DPD", " O ", 'D', "dyePink", 'O', KEY_YELLOW, 'P', Items.PAPER );
		addRecipeAuto(new ItemStack(ModItems.flame_conspiracy, 1), " S ", "STS", " S ", 'S', Fluids.KEROSENE.getDict(1000), 'T', STEEL.ingot() );
		addRecipeAuto(new ItemStack(ModItems.flame_politics, 1), " I ", "IPI", " I ", 'P', Items.PAPER, 'I', KEY_BLACK );
		addRecipeAuto(new ItemStack(ModItems.flame_opinion, 1), " R ", "RPR", " R ", 'P', Items.PAPER, 'R', KEY_RED );

		addRecipeAuto(new ItemStack(ModItems.solid_fuel_presto, 1), " P ", "SRS", " P ", 'P', Items.PAPER, 'S', ModItems.solid_fuel, 'R', REDSTONE.dust() );
		addShapelessAuto(new ItemStack(ModItems.solid_fuel_presto_triplet, 1), ModItems.solid_fuel_presto, ModItems.solid_fuel_presto, ModItems.solid_fuel_presto, ModItems.ball_dynamite );
		addRecipeAuto(new ItemStack(ModItems.solid_fuel_presto_bf, 1), " P ", "SRS", " P ", 'P', Items.PAPER, 'S', ModItems.solid_fuel_bf, 'R', REDSTONE.dust() );
		addShapelessAuto(new ItemStack(ModItems.solid_fuel_presto_triplet_bf, 1), ModItems.solid_fuel_presto_bf, ModItems.solid_fuel_presto_bf, ModItems.solid_fuel_presto_bf, ModItems.ingot_c4 );

		addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.flame_war), 1), "WHW", "CTP", "WOW", 'W', Item.getItemFromBlock(Blocks.PLANKS), 'T', Item.getItemFromBlock(Blocks.TNT), 'H', ModItems.flame_pony, 'C', ModItems.flame_conspiracy, 'P', ModItems.flame_politics, 'O', ModItems.flame_opinion );
		addRecipeAuto(new ItemStack(ModBlocks.det_cord, 4), " P ", "PGP", " P ", 'P', Items.PAPER, 'G', Items.GUNPOWDER );
		addRecipeAuto(new ItemStack(ModBlocks.det_charge, 1), "PDP", "DTD", "PDP", 'P', STEEL.plate(), 'D', ModBlocks.det_cord, 'T', ANY_PLASTICEXPLOSIVE.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.det_nuke, 1), "PFP", "DCD", "PFP", 'P', DESH.plateCast(), 'D', ModBlocks.det_charge, 'C', ModItems.man_core, 'F', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CONTROLLER) );
		addRecipeAuto(new ItemStack(ModBlocks.det_miner, 4), "FFF", "ITI", "ITI", 'F', Items.FLINT, 'I', IRON.plate(), 'T', ModItems.ball_dynamite );
		addRecipeAuto(new ItemStack(ModBlocks.det_miner, 12), "FFF", "ITI", "ITI", 'F', Items.FLINT, 'I', STEEL.plate(), 'T', ANY_PLASTICEXPLOSIVE.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.emp_bomb, 1), "LML", "LCL", "LML", 'L', PB.plate(), 'M', ModItems.magnetron, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED) );
		addShapelessAuto(new ItemStack(ModBlocks.charge_dynamite, 1), ModItems.stick_dynamite, ModItems.stick_dynamite, ModItems.stick_dynamite, ModItems.ducttape );
		addRecipeAuto(new ItemStack(ModBlocks.charge_miner, 1), " F ", "FCF", " F ", 'F', Items.FLINT, 'C', ModBlocks.charge_dynamite );
		addShapelessAuto(new ItemStack(ModBlocks.charge_semtex, 1), ModItems.stick_semtex, ModItems.stick_semtex, ModItems.stick_semtex, ModItems.ducttape );
		addShapelessAuto(new ItemStack(ModBlocks.charge_c4, 1), ModItems.stick_c4, ModItems.stick_c4, ModItems.stick_c4, ModItems.ducttape );

		addRecipeAuto(ItemBattery.getFullBattery(ModItems.energy_core), "PCW", "TRD", "PCW", 'P', ALLOY.plate(), 'C', ModItems.coil_advanced_alloy, 'W', ALLOY.wireFine(), 'R', new ItemStack(ModItems.cell, 1, Fluids.TRITIUM.getID()), 'D', new ItemStack(ModItems.cell, 1, Fluids.DEUTERIUM.getID()), 'T', W.ingot() );
		addRecipeAuto(ItemBattery.getFullBattery(ModItems.energy_core), "PCW", "TDR", "PCW", 'P', ALLOY.plate(), 'C', ModItems.coil_advanced_alloy, 'W', ALLOY.wireFine(), 'R', new ItemStack(ModItems.cell, 1, Fluids.TRITIUM.getID()), 'D', new ItemStack(ModItems.cell, 1, Fluids.DEUTERIUM.getID()), 'T', W.ingot() );
		addRecipeAuto(new ItemStack(ModItems.hev_battery, 4), " W ", "IEI", "ICI", 'W', GOLD.wireFine(), 'I', ModItems.plate_polymer, 'E', REDSTONE.dust(), 'C', CO.dust() );
		addRecipeAuto(new ItemStack(ModItems.hev_battery, 4), " W ", "ICI", "IEI", 'W', GOLD.wireFine(), 'I', ModItems.plate_polymer, 'E', REDSTONE.dust(), 'C', CO.dust() );
		addShapelessAuto(new ItemStack(ModItems.hev_battery, 1), ModBlocks.hev_battery );
		addShapelessAuto(new ItemStack(ModBlocks.hev_battery, 1), ModItems.hev_battery );

		addShapelessAuto(ItemBattery.getFullBattery(ModItems.battery_potato), Items.POTATO, AL.wireFine(), CU.wireFine() );
		addShapelessAuto(ItemBattery.getFullBattery(ModItems.battery_potatos), ItemBattery.getFullBattery(ModItems.battery_potato), ModItems.turret_chip, REDSTONE.dust() );

		addRecipeAuto(new ItemStack(ModItems.wiring_red_copper, 1), "PPP", "PIP", "PPP", 'P', STEEL.plate(), 'I', STEEL.ingot() );

		addRecipeAuto(new ItemStack(ModItems.jetpack_tank, 1), " S ", "BKB", " S ", 'S', STEEL.plate(), 'B', STEEL.bolt(), 'K', Fluids.KEROSENE.getDict(1000) );
		addShapelessAuto(new ItemStack(ModItems.gun_kit_1, 1), ANY_RUBBER.ingot(), Fluids.WOODOIL.getDict(1_000), IRON.ingot() );
		addShapelessAuto(new ItemStack(ModItems.gun_kit_2, 1), ModItems.gun_kit_1, ModItems.wrench, ModItems.ducttape, Fluids.LUBRICANT.getDict(1_000) );

		addRecipeAuto(new ItemStack(ModItems.igniter, 1), " W", "SC", "CE", 'S', STEEL.plate(), 'W', SA326.wireFine(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'E', EUPH.ingot() );
		addRecipeAuto(new ItemStack(ModItems.watch, 1), "LYL", "EWE", "LYL", 'E', EUPH.ingot(), 'L', KEY_BLUE, 'W', Items.CLOCK, 'Y', ModItems.billet_yharonite );

		addRecipeAuto(new ItemStack(ModItems.key, 1), "  B", " B ", "P  ", 'P', STEEL.plate(), 'B', STEEL.bolt() );
		addRecipeAuto(new ItemStack(ModItems.key_kit, 1), "PKP", "DTD", "PKP", 'P', GOLD.plate(), 'K', ModItems.key, 'D', DESH.dust(), 'T', KEY_TOOL_SCREWDRIVER );
		addRecipeAuto(new ItemStack(ModItems.key_red, 1), "RCA", "CIC", "KCR", 'R', KEY_RED, 'C', STAR.wireDense(), 'A', ModItems.gem_alexandrite, 'I', ModItems.ingot_chainsteel, 'K', ModItems.key );
		addRecipeAuto(new ItemStack(ModItems.pin, 1), "W ", " W", " W", 'W', CU.wireFine() );
		addRecipeAuto(new ItemStack(ModItems.padlock_rusty, 1), "I", "B", "I", 'I', IRON.ingot(), 'B', STEEL.bolt() );
		addRecipeAuto(new ItemStack(ModItems.padlock, 1), " P ", "PBP", "PPP", 'P', STEEL.plate(), 'B', STEEL.bolt() );
		addRecipeAuto(new ItemStack(ModItems.padlock_reinforced, 1), " P ", "PBP", "PDP", 'P', ALLOY.plate(), 'D', ModItems.plate_desh, 'B', DURA.bolt() );
		addRecipeAuto(new ItemStack(ModItems.padlock_unbreakable, 1), " P ", "PBP", "PDP", 'P', BIGMT.plate(), 'D', DIAMOND.gem(), 'B', DURA.bolt() );

		addRecipeAuto(new ItemStack(ModItems.record_lc, 1), " S ", "SDS", " S ", 'S', ANY_PLASTIC.ingot(), 'D', LAPIS.dust() );
		addRecipeAuto(new ItemStack(ModItems.record_ss, 1), " S ", "SDS", " S ", 'S', ANY_PLASTIC.ingot(), 'D', ALLOY.dust() );
		addRecipeAuto(new ItemStack(ModItems.record_vc, 1), " S ", "SDS", " S ", 'S', ANY_PLASTIC.ingot(), 'D', CMB.dust() );

		addRecipeAuto(new ItemStack(ModItems.polaroid, 1), " C ", "RPY", " B ", 'B', LAPIS.dust(), 'C', COAL.dust(), 'R', ALLOY.dust(), 'Y', GOLD.dust(), 'P', Items.PAPER );

		addShapelessAuto(new ItemStack(ModItems.crystal_horn, 1), NP237.dust(), I.dust(), TH232.dust(), AT.dust(), ND.dust(), CS.dust(), ModBlocks.block_meteor, ModBlocks.gravel_obsidian, Items.WATER_BUCKET );
		addShapelessAuto(new ItemStack(ModItems.crystal_charred, 1), SR.dust(), CO.dust(), BR.dust(), NB.dust(), TS.dust(), CE.dust(), ModBlocks.block_meteor, AL.block(), Items.WATER_BUCKET );
		addRecipeAuto(new ItemStack(ModBlocks.crystal_virus, 1), "STS", "THT", "STS", 'S', ModItems.particle_strange, 'T', W.dust(), 'H', ModItems.crystal_horn );
		addRecipeAuto(new ItemStack(ModBlocks.crystal_pulsar, 32), "STS", "THT", "STS", 'S', new ItemStack(ModItems.cell, 1, Fluids.UF6.getID()), 'T', AL.dust(), 'H', ModItems.crystal_charred );
		addRecipeAuto(new ItemStack(ModBlocks.fluid_duct_neo, 8, 0), "SAS", "   ", "SAS", 'S', STEEL.plate(), 'A', AL.plate() );
		addRecipeAuto(new ItemStack(ModBlocks.fluid_duct_neo, 8, 1), "IAI", "   ", "IAI", 'I', IRON.plate(), 'A', AL.plate() );
		addRecipeAuto(new ItemStack(ModBlocks.fluid_duct_neo, 8, 2), "ASA", "   ", "ASA", 'S', STEEL.plate(), 'A', AL.plate() );
		addRecipeAuto(new ItemStack(ModBlocks.fluid_duct_paintable, 8), "SAS", "A A", "SAS", 'S', STEEL.ingot(), 'A', AL.plate() );
		addRecipeAuto(new ItemStack(ModBlocks.fluid_duct_paintable_block_exhaust, 8), "SAS", "A A", "SAS", 'S', IRON.ingot(), 'A', ModItems.plate_polymer);
		addShapelessAuto(new ItemStack(ModBlocks.fluid_duct_gauge), ModBlocks.fluid_duct_paintable, STEEL.ingot(), DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC) );
		addRecipeAuto(new ItemStack(ModBlocks.fluid_valve, 1), "S", "W", 'S', Blocks.LEVER, 'W', ModBlocks.fluid_duct_paintable );
		addRecipeAuto(new ItemStack(ModBlocks.fluid_switch, 1), "S", "W", 'S', REDSTONE.dust(), 'W', ModBlocks.fluid_duct_paintable );
		addRecipeAuto(new ItemStack(ModBlocks.fluid_counter_valve, 1), "S", "W", 'S', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP), 'W', ModBlocks.fluid_switch);
		addRecipeAuto(new ItemStack(ModBlocks.fluid_pump, 1), " S ", "PGP", "IMI", 'S', STEEL.shell(), 'P', STEEL.pipe(), 'G', GRAPHITE.ingot(), 'I', STEEL.ingot(), 'M', ModItems.motor );
		addRecipeAuto(new ItemStack(ModBlocks.pneumatic_tube, 8), "CRC", 'C', CU.plateCast(), 'R', ANY_RUBBER.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.pneumatic_tube, 24), "CRC", 'C', CU.plateWelded(), 'R', ANY_RUBBER.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.pneumatic_tube_paintable, 4), "SAS", "A A", "SAS", 'S', STEEL.plate(), 'A', ModBlocks.pneumatic_tube);
        addRecipeAuto(new ItemStack(ModBlocks.pipe_anchor, 2), "P", "P", "S", 'P', STEEL.pipe(), 'S', STEEL.ingot());

		addRecipeAuto(new ItemStack(ModItems.template_folder, 1), "LPL", "BPB", "LPL", 'P', Items.PAPER, 'L', "dye", 'B', "dye" );
		addRecipeAuto(new ItemStack(ModItems.pellet_antimatter, 1), "###", "###", "###", '#', new ItemStack(ModItems.cell, 1, Fluids.AMAT.getID()) );
		addRecipeAuto(new ItemStack(ModItems.fluid_tank_empty, 8), "121", "1G1", "121", '1', AL.plate(), '2', IRON.plate(), 'G', KEY_ANYPANE );
		addRecipeAuto(new ItemStack(ModItems.fluid_tank_lead_empty, 4), "LUL", "LTL", "LUL", 'L', PB.plate(), 'U', U238.billet(), 'T', ModItems.fluid_tank_empty );
		addRecipeAuto(new ItemStack(ModItems.fluid_barrel_empty, 2), "121", "1G1", "121", '1', STEEL.plate(), '2', AL.plate(), 'G', KEY_ANYPANE );

		if(!GeneralConfig.enable528) {
			addRecipeAuto(new ItemStack(ModItems.inf_water, 1), "222", "131", "222", '1', Items.WATER_BUCKET, '2', AL.plate(), '3', DIAMOND.gem() );
			addRecipeAuto(new ItemStack(ModItems.inf_water_mk2, 1), "BPB", "PTP", "BPB", 'B', ModItems.inf_water, 'P', ModBlocks.fluid_duct_neo, 'T', ModItems.tank_steel );
		}

		//not so Temporary Crappy Recipes
		addRecipeAuto(new ItemStack(ModItems.piston_selenium, 1), "SSS", "STS", " D ", 'S', STEEL.plate(), 'T', W.ingot(), 'D', DURA.bolt() );
		addShapelessAuto(new ItemStack(ModItems.catalyst_clay), IRON.dust(), Items.CLAY_BALL );
		addRecipeAuto(new ItemStack(ModItems.singularity_spark, 1), "XAX", "BCB", "XAX", 'X', ModItems.plate_dineutronium, 'A', ModItems.singularity_counter_resonant, 'B', ModItems.singularity_super_heated, 'C', ModItems.black_hole );
		addRecipeAuto(new ItemStack(ModItems.singularity_spark, 1), "XBX", "ACA", "XBX", 'X', ModItems.plate_dineutronium, 'A', ModItems.singularity_counter_resonant, 'B', ModItems.singularity_super_heated, 'C', ModItems.black_hole );
		addRecipeAuto(new ItemStack(ModItems.ams_core_sing, 1), "EAE", "ASA", "EAE", 'E', ModItems.plate_euphemium, 'A', new ItemStack(ModItems.cell, 1, Fluids.AMAT.getID()), 'S', ModItems.singularity );
		addRecipeAuto(new ItemStack(ModItems.ams_core_wormhole, 1), "DPD", "PSP", "DPD", 'D', ModItems.plate_dineutronium, 'P', ModItems.powder_spark_mix, 'S', ModItems.singularity );
		addRecipeAuto(new ItemStack(ModItems.ams_core_eyeofharmony, 1), "ALA", "LSL", "ALA", 'A', ModItems.plate_dalekanium, 'L', new ItemStack(ModItems.fluid_barrel_full, 1, Fluids.LAVA.getID()), 'S', ModItems.black_hole );
		addRecipeAuto(new ItemStack(ModItems.ams_core_thingy), "NSN", "NGN", "G G", 'N', GOLD.nugget(), 'G', GOLD.ingot(), 'S', new ItemStack(ModItems.battery_pack, 1, ItemBatteryPack.EnumBatteryPack.BATTERY_QUANTUM.ordinal()) );
		addRecipeAuto(new ItemStack(ModItems.photo_panel), " G ", "IPI", " C ", 'G', KEY_ANYPANE, 'I', ModItems.plate_polymer, 'P', NETHERQUARTZ.dust(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.PCB) );
		addRecipeAuto(new ItemStack(ModBlocks.machine_satlinker), "PSP", "SCS", "PSP", 'P', STEEL.plate(), 'S', STAR.ingot(), 'C', ModItems.sat_chip );
		addRecipeAuto(new ItemStack(ModBlocks.machine_keyforge), "PCP", "WSW", "WSW", 'P', STEEL.plate(), 'S', W.ingot(), 'C', ModItems.padlock, 'W', KEY_PLANKS );
		addRecipeAuto(new ItemStack(ModItems.sat_chip), "WWW", "CIC", "WWW", 'W', MINGRADE.wireFine(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'I', ANY_PLASTIC.ingot() );
		addShapelessAuto(new ItemStack(ModItems.sat_mapper), ModBlocks.sat_mapper );
		addShapelessAuto(new ItemStack(ModItems.sat_scanner), ModBlocks.sat_scanner );
		addShapelessAuto(new ItemStack(ModItems.sat_radar), ModBlocks.sat_radar );
		addShapelessAuto(new ItemStack(ModItems.sat_laser), ModBlocks.sat_laser );
		addShapelessAuto(new ItemStack(ModItems.sat_resonator), ModBlocks.sat_resonator );
		addShapelessAuto(new ItemStack(ModItems.sat_foeq), ModBlocks.sat_foeq );
		addShapelessAuto(new ItemStack(ModItems.geiger_counter), ModBlocks.geiger );
		addRecipeAuto(new ItemStack(ModItems.sat_interface), "ISI", "PCP", "PAP", 'I', STEEL.ingot(), 'S', STAR.ingot(), 'P', ModItems.plate_polymer, 'C', ModItems.sat_chip, 'A', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED) );
		addRecipeAuto(new ItemStack(ModItems.sat_coord), "SII", "SCA", "SPP", 'I', STEEL.ingot(), 'S', STAR.ingot(), 'P', ModItems.plate_polymer, 'C', ModItems.sat_chip, 'A', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED) );
		addRecipeAuto(new ItemStack(ModBlocks.machine_transformer), "SCS", "MDM", "SCS", 'S', IRON.ingot(), 'D', MINGRADE.ingot(), 'M',ModItems.coil_advanced_alloy, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CAPACITOR) );
		addRecipeAuto(new ItemStack(ModBlocks.machine_transformer_dnt), "SDS", "MCM", "MCM", 'S', STAR.ingot(), 'D', DESH.ingot(), 'M', MAGTUNG.wireDense(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BISMOID) );
		addRecipeAuto(new ItemStack(ModBlocks.radiobox), "PLP", "PSP", "PLP", 'P', STEEL.plate(), 'S', ModItems.ring_starmetal, 'L', getReflector() );
		addRecipeAuto(new ItemStack(ModBlocks.radiorec), "  W", "PCP", "PIP", 'W', CU.wireFine(), 'P', STEEL.plate(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE), 'I', ANY_PLASTIC.ingot() );
		addRecipeAuto(new ItemStack(ModItems.jackt), "S S", "LIL", "LIL", 'S', STEEL.plate(), 'L', Items.LEATHER, 'I', ANY_RUBBER.ingot() );
		addRecipeAuto(new ItemStack(ModItems.jackt2), "S S", "LIL", "III", 'S', STEEL.plate(), 'L', Items.LEATHER, 'I', ANY_RUBBER.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.vent_chlorine), "IGI", "ICI", "IDI", 'I', IRON.plate(), 'G', Blocks.IRON_BARS, 'C', ModItems.pellet_gas, 'D', Blocks.DISPENSER );
		addRecipeAuto(new ItemStack(ModBlocks.vent_chlorine_seal), "ISI", "SCS", "ISI", 'I', BIGMT.ingot(), 'S', STAR.ingot(), 'C', ModItems.chlorine_pinwheel );
		addRecipeAuto(new ItemStack(ModBlocks.vent_cloud), "IGI", "ICI", "IDI", 'I', IRON.plate(), 'G', Blocks.IRON_BARS, 'C', ModItems.grenade_cloud, 'D', Blocks.DISPENSER );
		addRecipeAuto(new ItemStack(ModBlocks.vent_pink_cloud), "IGI", "ICI", "IDI", 'I', IRON.plate(), 'G', Blocks.IRON_BARS, 'C', ModItems.grenade_pink_cloud, 'D', Blocks.DISPENSER );
		addRecipeAuto(new ItemStack(ModBlocks.spikes, 4), "BBB", "BBB", "TTT", 'B', STEEL.bolt(), 'T', STEEL.ingot() );
		addRecipeAuto(new ItemStack(ModItems.custom_fall, 1), "IIP", "CHW", "IIP", 'I', ANY_RUBBER.ingot(), 'P', BIGMT.plate(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'H', STEEL.shell(), 'W', ModItems.coil_copper );
		addRecipeAuto(new ItemStack(ModBlocks.machine_controller, 1), "TDT", "DCD", "TDT", 'T', ANY_RESISTANTALLOY.ingot(), 'D', ModItems.crt_display, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED) );
		addRecipeAuto(new ItemStack(ModItems.containment_box, 1), "LUL", "UCU", "LUL", 'L', PB.plate(), 'U', U238.billet(), 'C', ModBlocks.crate_steel );
		addRecipeAuto(new ItemStack(ModItems.casing_bag, 1), " L ", "LGL", " L ", 'L', Items.LEATHER, 'G', GUNMETAL.plate() );
		addRecipeAuto(new ItemStack(ModItems.casing_bag, 1), " L ", "LGL", " L ", 'L', ANY_RUBBER.ingot(), 'G', GUNMETAL.plate() );
		addRecipeAuto(new ItemStack(ModItems.ammo_bag, 1), "LLL", "MGM", "LLL", 'L', Items.LEATHER, 'G', WEAPONSTEEL.plate(), 'M', WEAPONSTEEL.mechanism() );
		addRecipeAuto(new ItemStack(ModItems.ammo_bag, 1), "LLL", "MGM", "LLL", 'L', ANY_RUBBER.ingot(), 'G', WEAPONSTEEL.plate(), 'M', WEAPONSTEEL.mechanism() );

		addRecipeAuto(new ItemStack(ModBlocks.rad_absorber, 1, BlockAbsorber.EnumAbsorberTier.BASE.ordinal()), "ICI", "CPC", "ICI", 'I', CU.ingot(), 'C', COAL.dust(), 'P', PB.dust() );
		addRecipeAuto(new ItemStack(ModBlocks.rad_absorber, 1, BlockAbsorber.EnumAbsorberTier.RED.ordinal()), "ICI", "CPC", "ICI", 'I', TI.ingot(), 'C', COAL.dust(), 'P', new ItemStack(ModBlocks.rad_absorber, 1, BlockAbsorber.EnumAbsorberTier.BASE.ordinal()) );
		addRecipeAuto(new ItemStack(ModBlocks.rad_absorber, 1, BlockAbsorber.EnumAbsorberTier.GREEN.ordinal()), "ICI", "CPC", "ICI", 'I', ANY_PLASTIC.ingot(), 'C', ModItems.powder_desh_mix, 'P', new ItemStack(ModBlocks.rad_absorber, 1, BlockAbsorber.EnumAbsorberTier.RED.ordinal()) );
		addRecipeAuto(new ItemStack(ModBlocks.rad_absorber, 1, BlockAbsorber.EnumAbsorberTier.PINK.ordinal()), "ICI", "CPC", "ICI", 'I', BIGMT.ingot(), 'C', ModItems.powder_nitan_mix, 'P', new ItemStack(ModBlocks.rad_absorber, 1, BlockAbsorber.EnumAbsorberTier.GREEN.ordinal()) );
		addRecipeAuto(new ItemStack(ModBlocks.decon, 1), "BGB", "SAS", "BSB", 'B', BE.ingot(), 'G', Blocks.IRON_BARS, 'S', STEEL.ingot(), 'A', new ItemStack(ModBlocks.rad_absorber, 1, BlockAbsorber.EnumAbsorberTier.BASE.ordinal()) );
		addRecipeAuto(new ItemStack(ModBlocks.machine_minirtg, 1), "LLL", "PPP", "TRT", 'L', PB.plate(), 'P', PU238.billet(), 'T', ModItems.thermo_element, 'R', ModItems.rtg_unit );
		addRecipeAuto(new ItemStack(ModBlocks.machine_powerrtg, 1), "SRS", "PTP", "SRS", 'S', STAR.ingot(), 'R', ModItems.rtg_unit, 'P', PO210.billet(), 'T', TS.dust() );

		addRecipeAuto(new ItemStack(ModBlocks.pink_planks, 4), "W", 'W', ModBlocks.pink_log );
		addRecipeAuto(new ItemStack(ModBlocks.pink_slab, 6), "WWW", 'W', ModBlocks.pink_planks );
		addRecipeAuto(new ItemStack(ModBlocks.pink_stairs, 6), "W  ", "WW ", "WWW", 'W', ModBlocks.pink_planks );

		addRecipeAuto(new ItemStack(ModItems.door_metal, 1), "II", "SS", "II", 'I', IRON.plate(), 'S', STEEL.plate() );
		addRecipeAuto(new ItemStack(ModItems.door_office, 1), "II", "SS", "II", 'I', KEY_PLANKS, 'S', IRON.plate() );
		addRecipeAuto(new ItemStack(ModItems.door_bunker, 1), "II", "SS", "II", 'I', STEEL.plate(), 'S', PB.plate() );

		addShapelessAuto(new ItemStack(Items.PAPER, 1), new ItemStack(ModItems.assembly_template, 1, OreDictionary.WILDCARD_VALUE) );
		addShapelessAuto(new ItemStack(Items.PAPER, 1), new ItemStack(ModItems.chemistry_template, 1, OreDictionary.WILDCARD_VALUE) );
		addShapelessAuto(new ItemStack(Items.PAPER, 1), new ItemStack(ModItems.crucible_template, 1, OreDictionary.WILDCARD_VALUE) );
		addShapelessAuto(new ItemStack(Items.SLIME_BALL, 16), new ItemStack(Items.DYE, 1, 15), new ItemStack(Items.DYE, 1, 15), new ItemStack(Items.DYE, 1, 15), new ItemStack(Items.DYE, 1, 15), Fluids.SULFURIC_ACID.getDict(1000) );

//		for(int i = 1; i < Fluids.getAll().length; ++i) {
//			ItemStack id = new ItemStack(ModItems.fluid_identifier_multi, 1, i);
//			ItemFluidIDMulti.setType(id, Fluids.fromID(i), true);
//
//			addShapelessAuto(new ItemStack(ModItems.fluid_duct, 1, i), new ItemStack(ModBlocks.fluid_duct_neo, 1), id);
//
//			addShapelessAuto(new ItemStack(ModItems.fluid_duct, 8, i),
//					new ItemStack(ModBlocks.fluid_duct_neo, 1), new ItemStack(ModBlocks.fluid_duct_neo, 1), new ItemStack(ModBlocks.fluid_duct_neo, 1),
//					new ItemStack(ModBlocks.fluid_duct_neo, 1), new ItemStack(ModBlocks.fluid_duct_neo, 1), new ItemStack(ModBlocks.fluid_duct_neo, 1),
//					new ItemStack(ModBlocks.fluid_duct_neo, 1), new ItemStack(ModBlocks.fluid_duct_neo, 1), id);
//
//			addShapelessAuto(new ItemStack(ModItems.fluid_duct, 1, i),
//					new ItemStack(ModItems.fluid_duct, 1, OreDictionary.WILDCARD_VALUE), id);
//
//			addShapelessAuto(new ItemStack(ModItems.fluid_duct, 8, i),
//					new ItemStack(ModItems.fluid_duct, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(ModItems.fluid_duct, 1, OreDictionary.WILDCARD_VALUE),
//					new ItemStack(ModItems.fluid_duct, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(ModItems.fluid_duct, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(ModItems.fluid_duct, 1, OreDictionary.WILDCARD_VALUE),
//					new ItemStack(ModItems.fluid_duct, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(ModItems.fluid_duct, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(ModItems.fluid_duct, 1, OreDictionary.WILDCARD_VALUE), id);
//		}
        // mlbv: used a dynamic recipe to replace the boilerplate above
        hack.getRegistry().register(new FluidDuctRetypeHandler().setRegistryName(Tags.MODID, "duct_retype"));
		addShapelessAuto(new ItemStack(ModBlocks.fluid_duct_neo, 1), new ItemStack(ModItems.fluid_duct, 1, OreDictionary.WILDCARD_VALUE) );

		addRecipeAuto(new ItemStack(Blocks.TORCH, 3), "L", "S", 'L', LIGNITE.gem(), 'S', KEY_STICK );
		addRecipeAuto(new ItemStack(Blocks.TORCH, 8), "L", "S", 'L', ANY_COKE.gem(), 'S', KEY_STICK );

		addRecipeAuto(new ItemStack(ModBlocks.machine_missile_assembly, 1), "PWP", "SSS", "CCC", 'P', ModItems.pedestal_steel, 'W', ModItems.wrench, 'S', STEEL.plate(), 'C', ModBlocks.steel_scaffold );
		addRecipeAuto(new ItemStack(ModBlocks.struct_launcher, 8), "PPP", "SDS", "CCC", 'P', STEEL.plate(), 'S', ModBlocks.steel_scaffold, 'D', STEEL.pipe(), 'C', ANY_CONCRETE.any() );
		addRecipeAuto(new ItemStack(ModBlocks.struct_scaffold, 8), "SSS", "DCD", "SSS", 'S', ModBlocks.steel_scaffold, 'D', new ItemStack(ModBlocks.fluid_duct_neo, 1, OreDictionary.WILDCARD_VALUE), 'C', ModBlocks.red_cable );

		addRecipeAuto(new ItemStack(ModItems.seg_10, 1), "P", "S", "B", 'P', AL.plate(), 'S', ModBlocks.steel_scaffold, 'B', ModBlocks.steel_beam );
		addRecipeAuto(new ItemStack(ModItems.seg_15, 1), "PP", "SS", "BB", 'P', TI.plate(), 'S', ModBlocks.steel_scaffold, 'B', ModBlocks.steel_beam );
		addRecipeAuto(new ItemStack(ModItems.seg_20, 1), "PGP", "SSS", "BBB", 'P', STEEL.plate(), 'G', GOLD.plate(), 'S', ModBlocks.steel_scaffold, 'B', ModBlocks.steel_beam );

		addRecipeAuto(new ItemStack(ModBlocks.fence_metal, 6), "BIB", "BIB", 'B', Blocks.IRON_BARS, 'I', Items.IRON_INGOT );
		addShapelessAuto(new ItemStack(ModBlocks.fence_metal, 1, 1), new ItemStack(ModBlocks.fence_metal, 1, 0) );
		addShapelessAuto(new ItemStack(ModBlocks.fence_metal, 1, 0), new ItemStack(ModBlocks.fence_metal, 1, 1) );

		addShapelessAuto(new ItemStack(ModBlocks.waste_trinitite), new ItemStack(Blocks.SAND, 1, 0), ModItems.trinitite );
		addShapelessAuto(new ItemStack(ModBlocks.waste_trinitite_red), new ItemStack(Blocks.SAND, 1, 1), ModItems.trinitite );
		addShapelessAuto(new ItemStack(ModBlocks.sand_uranium, 8), "sand", "sand", "sand", "sand", "sand", "sand", "sand", "sand", U.dust() );
		addShapelessAuto(new ItemStack(ModBlocks.sand_polonium, 8), "sand", "sand", "sand", "sand", "sand", "sand", "sand", "sand", PO210.dust() );
		addShapelessAuto(new ItemStack(ModBlocks.sand_boron, 8), "sand", "sand", "sand", "sand", "sand", "sand", "sand", "sand", B.dust() );
		addShapelessAuto(new ItemStack(ModBlocks.sand_lead, 8), "sand", "sand", "sand", "sand", "sand", "sand", "sand", "sand", PB.dust() );
		addShapelessAuto(new ItemStack(ModBlocks.sand_quartz, 1), "sand", "sand", NETHERQUARTZ.dust(), NETHERQUARTZ.dust() );

		addRecipeAuto(new ItemStack(ModItems.rune_blank, 1), "PSP", "SDS", "PSP", 'P', ModItems.powder_magic, 'S', STAR.ingot(), 'D', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BISMOID) );
		addShapelessAuto(new ItemStack(ModItems.rune_isa, 1), ModItems.rune_blank, ModItems.powder_spark_mix, ModItems.singularity_counter_resonant );
		addShapelessAuto(new ItemStack(ModItems.rune_dagaz, 1), ModItems.rune_blank, ModItems.powder_spark_mix, ModItems.singularity );
		addShapelessAuto(new ItemStack(ModItems.rune_hagalaz, 1), ModItems.rune_blank, ModItems.powder_spark_mix, ModItems.singularity_super_heated );
		addShapelessAuto(new ItemStack(ModItems.rune_jera, 1), ModItems.rune_blank, ModItems.powder_spark_mix, ModItems.singularity_spark );
		addShapelessAuto(new ItemStack(ModItems.rune_thurisaz, 1), ModItems.rune_blank, ModItems.powder_spark_mix, ModItems.black_hole );
		addRecipeAuto(new ItemStack(ModItems.ams_lens, 1), "PDP", "GDG", "PDP", 'P', ModItems.plate_dineutronium, 'G', ModBlocks.reinforced_glass, 'D', Blocks.DIAMOND_BLOCK );
		addRecipeAuto(new ItemStack(ModItems.ams_catalyst_blank, 1), "TET", "ETE", "TET", 'T', TS.dust(), 'E', EUPH.ingot());
		addShapelessAuto(new ItemStack(ModItems.ams_catalyst_lithium, 1), ModItems.ams_catalyst_blank, ModItems.rune_isa, ModItems.rune_isa, ModItems.rune_jera, ModItems.rune_jera, LI.dust(), LI.dust(), LI.dust(), LI.dust() );
		addShapelessAuto(new ItemStack(ModItems.ams_catalyst_beryllium, 1), ModItems.ams_catalyst_blank, ModItems.rune_isa, ModItems.rune_dagaz, ModItems.rune_jera, ModItems.rune_jera, BE.dust(), BE.dust(), BE.dust(), BE.dust() );
		addShapelessAuto(new ItemStack(ModItems.ams_catalyst_copper, 1), ModItems.ams_catalyst_blank, ModItems.rune_dagaz, ModItems.rune_dagaz, ModItems.rune_jera, ModItems.rune_jera, CU.dust(), CU.dust(), CU.dust(), CU.dust() );
		addShapelessAuto(new ItemStack(ModItems.ams_catalyst_cobalt, 1), ModItems.ams_catalyst_blank, ModItems.rune_dagaz, ModItems.rune_hagalaz, ModItems.rune_jera, ModItems.rune_jera, CO.dust(), CO.dust(), CO.dust(), CO.dust() );
		addShapelessAuto(new ItemStack(ModItems.ams_catalyst_tungsten, 1), ModItems.ams_catalyst_blank, ModItems.rune_hagalaz, ModItems.rune_hagalaz, ModItems.rune_jera, ModItems.rune_jera, W.dust(), W.dust(), W.dust(), W.dust() );
		addShapelessAuto(new ItemStack(ModItems.ams_catalyst_aluminium, 1), ModItems.ams_catalyst_blank, ModItems.rune_isa, ModItems.rune_isa, ModItems.rune_jera, ModItems.rune_thurisaz, AL.dust(), AL.dust(), AL.dust(), AL.dust() );
		addShapelessAuto(new ItemStack(ModItems.ams_catalyst_iron, 1), ModItems.ams_catalyst_blank, ModItems.rune_isa, ModItems.rune_dagaz, ModItems.rune_jera, ModItems.rune_thurisaz, IRON.dust(), IRON.dust(), IRON.dust(), IRON.dust() );
		addShapelessAuto(new ItemStack(ModItems.ams_catalyst_strontium, 1), ModItems.ams_catalyst_blank, ModItems.rune_dagaz, ModItems.rune_dagaz, ModItems.rune_jera, ModItems.rune_thurisaz, SR.dust(), SR.dust(), SR.dust(), SR.dust() );
		addShapelessAuto(new ItemStack(ModItems.ams_catalyst_niobium, 1), ModItems.ams_catalyst_blank, ModItems.rune_dagaz, ModItems.rune_hagalaz, ModItems.rune_jera, ModItems.rune_thurisaz, NB.dust(), NB.dust(), NB.dust(), NB.dust() );
		addShapelessAuto(new ItemStack(ModItems.ams_catalyst_cerium, 1), ModItems.ams_catalyst_blank, ModItems.rune_hagalaz, ModItems.rune_hagalaz, ModItems.rune_jera, ModItems.rune_thurisaz, CE.dust(), CE.dust(), CE.dust(), CE.dust() );
		addShapelessAuto(new ItemStack(ModItems.ams_catalyst_caesium, 1), ModItems.ams_catalyst_blank, ModItems.rune_isa, ModItems.rune_isa, ModItems.rune_thurisaz, ModItems.rune_thurisaz, CS.dust(), CS.dust(), CS.dust(), CS.dust() );
		addShapelessAuto(new ItemStack(ModItems.ams_catalyst_thorium, 1), ModItems.ams_catalyst_blank, ModItems.rune_isa, ModItems.rune_dagaz, ModItems.rune_thurisaz, ModItems.rune_thurisaz, TH232.dust(), TH232.dust(), TH232.dust(), TH232.dust() );
		addShapelessAuto(new ItemStack(ModItems.ams_catalyst_euphemium, 1), ModItems.ams_catalyst_blank, ModItems.rune_dagaz, ModItems.rune_dagaz, ModItems.rune_thurisaz, ModItems.rune_thurisaz, EUPH.dust(), EUPH.dust(), EUPH.dust(), EUPH.dust() );
		addShapelessAuto(new ItemStack(ModItems.ams_catalyst_schrabidium, 1), ModItems.ams_catalyst_blank, ModItems.rune_dagaz, ModItems.rune_hagalaz, ModItems.rune_thurisaz, ModItems.rune_thurisaz, SA326.dust(), SA326.dust(), SA326.dust(), SA326.dust() );
		addShapelessAuto(new ItemStack(ModItems.ams_catalyst_dineutronium, 1), ModItems.ams_catalyst_blank, ModItems.rune_hagalaz, ModItems.rune_hagalaz, ModItems.rune_thurisaz, ModItems.rune_thurisaz, DNT.dust(), DNT.dust(), DNT.dust(), DNT.dust() );
		addRecipeAuto(new ItemStack(ModBlocks.dfc_core, 1), "DLD", "LML", "DLD", 'D', ModItems.ingot_bismuth, 'L', DNT.block(), 'M', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BISMOID) );
		addRecipeAuto(new ItemStack(ModBlocks.dfc_emitter, 1), "SDS", "TXL", "SDS", 'S', OSMIRIDIUM.plateWelded(), 'D', ModItems.plate_desh, 'T', ModBlocks.machine_transformer_dnt, 'X', ModItems.crystal_xen, 'L', ModItems.sat_head_laser );
		addRecipeAuto(new ItemStack(ModBlocks.dfc_receiver, 1), "SDS", "TXL", "SDS", 'S', OSMIRIDIUM.plateWelded(), 'D', ModItems.plate_desh, 'T', ModBlocks.machine_transformer_dnt, 'X', ModBlocks.block_dineutronium, 'L', STEEL.shell() );
		addRecipeAuto(new ItemStack(ModBlocks.dfc_injector, 1), "SDS", "TXL", "SDS", 'S', OSMIRIDIUM.plateWelded(), 'D', CMB.plate(), 'T', ModBlocks.machine_fluidtank, 'X', ModItems.motor, 'L', ModItems.pipes_steel );
		addRecipeAuto(new ItemStack(ModBlocks.dfc_stabilizer, 1), "SDS", "TXL", "SDS", 'S', OSMIRIDIUM.plateWelded(), 'D', ModItems.plate_desh, 'T', ModItems.singularity_spark, 'X', new ItemStack(ModBlocks.hadron_coil_alloy, 1, 0), 'L', ModItems.crystal_xen );
		addRecipeAuto(new ItemStack(ModBlocks.barrel_plastic, 1), "IPI", "I I", "IPI", 'I', ModItems.plate_polymer, 'P', AL.plate() );
		addRecipeAuto(new ItemStack(ModBlocks.barrel_iron, 1), "IPI", "I I", "IPI", 'I', IRON.plate(), 'P', IRON.ingot() );
		addShapelessAuto(new ItemStack(ModBlocks.barrel_iron, 1), ModBlocks.barrel_corroded, ANY_TAR.any() );
		addRecipeAuto(new ItemStack(ModBlocks.barrel_steel, 1), "IPI", "ITI", "IPI", 'I', STEEL.plate(), 'P', STEEL.ingot(), 'T', ANY_TAR.any() );
		addRecipeAuto(new ItemStack(ModBlocks.barrel_tcalloy, 1), "IPI", "I I", "IPI", 'I', "ingotTcAlloy", 'P', TI.plate() );
		addRecipeAuto(new ItemStack(ModBlocks.barrel_antimatter, 1), "IPI", "I I", "IPI", 'I', BIGMT.plate(), 'P', ModItems.coil_advanced_torus);
		addRecipeAuto(new ItemStack(ModBlocks.tesla, 1), "CCC", "PIP", "WTW", 'C', ModItems.coil_copper, 'I', IRON.ingot(), 'P', ANY_PLASTIC.ingot(), 'T', ModBlocks.machine_transformer, 'W', KEY_PLANKS );
		addRecipeAuto(new ItemStack(ModBlocks.struct_plasma_core, 1), "CBC", "BHB", "CBC", 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'B', ModBlocks.machine_lithium_battery, 'H', ModBlocks.fusion_heater );
		addRecipeAuto(new ItemStack(ModBlocks.struct_watz_core, 1), "CBC", "BHB", "CBC", 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'B', ANY_RESISTANTALLOY.plateCast(), 'H', ModBlocks.watz_cooler );
		addShapelessAuto(new ItemStack(ModBlocks.fusion_heater), ModBlocks.fusion_hatch );
		addShapelessAuto(new ItemStack(ModItems.energy_core), ModItems.fusion_core, ModItems.fuse );
		addRecipeAuto(new ItemStack(ModItems.catalytic_converter, 1), "PCP", "PBP", "PCP", 'P', ANY_HARDPLASTIC.ingot(), 'C', CO.dust(), 'B', ANY_BISMOID.ingot() );

		addRecipeAuto(new ItemStack(ModItems.upgrade_nullifier, 1), "SPS", "PUP", "SPS", 'S', STEEL.plate(), 'P', ModItems.powder_fire, 'U', ModItems.upgrade_template );
		addRecipeAuto(new ItemStack(ModItems.upgrade_smelter, 1), "PHP", "CUC", "DTD", 'P', CU.plate(), 'H', Blocks.HOPPER, 'C', ModItems.coil_tungsten, 'U', ModItems.upgrade_template, 'D', ModItems.coil_copper, 'T', ModBlocks.machine_transformer );
		addRecipeAuto(new ItemStack(ModItems.upgrade_shredder, 1), "PHP", "CUC", "DTD", 'P', ModItems.motor, 'H', Blocks.HOPPER, 'C', ModItems.blades_advanced_alloy, 'U', ModItems.upgrade_smelter, 'D', TI.plate(), 'T', ModBlocks.machine_transformer );
		addRecipeAuto(new ItemStack(ModItems.upgrade_centrifuge, 1), "PHP", "PUP", "DTD", 'P', ModItems.centrifuge_element, 'H', Blocks.HOPPER, 'U', ModItems.upgrade_shredder, 'D', ANY_PLASTIC.ingot(), 'T', ModBlocks.machine_transformer );
		addRecipeAuto(new ItemStack(ModItems.upgrade_crystallizer, 1), "PHP", "CUC", "DTD", 'P', new ItemStack(ModItems.fluid_barrel_full, 1, Fluids.PEROXIDE.getID()), 'H', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'C', ModBlocks.barrel_steel, 'U', ModItems.upgrade_centrifuge, 'D', ModItems.motor, 'T', ModBlocks.machine_transformer );
		addRecipeAuto(new ItemStack(ModItems.upgrade_screm, 1), "SUS", "SCS", "SUS", 'S', STEEL.plate(), 'U', ModItems.upgrade_template, 'C', ModItems.crystal_xen );
		addRecipeAuto(new ItemStack(ModItems.upgrade_gc_speed, 1), "GNG", "RUR", "GMG", 'R', RUBBER.ingot(), 'M', ModItems.motor, 'G', ModItems.coil_gold, 'N', NB.ingot(), 'U', ModItems.upgrade_template);

		addRecipeAuto(new ItemStack(ModItems.upgrade_stack_1, 1), " C ", "PUP", " C ", 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE), 'P', DictFrame.fromOne(ModItems.part_generic, EnumPartType.PISTON_PNEUMATIC), 'U', ModItems.upgrade_template );
		addRecipeAuto(new ItemStack(ModItems.upgrade_stack_2, 1), " C ", "PUP", " C ", 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CAPACITOR), 'P', DictFrame.fromOne(ModItems.part_generic, EnumPartType.PISTON_HYDRAULIC), 'U', ModItems.upgrade_stack_1 );
		addRecipeAuto(new ItemStack(ModItems.upgrade_stack_3, 1), " C ", "PUP", " C ", 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP), 'P', DictFrame.fromOne(ModItems.part_generic, EnumPartType.PISTON_ELECTRIC), 'U', ModItems.upgrade_stack_2 );
		addRecipeAuto(new ItemStack(ModItems.upgrade_ejector_1, 1), " C ", "PUP", " C ", 'C', ModItems.plate_copper, 'P', ModItems.motor, 'U', ModItems.upgrade_template );
		addRecipeAuto(new ItemStack(ModItems.upgrade_ejector_2, 1), " C ", "PUP", " C ", 'C', ModItems.plate_gold, 'P', ModItems.motor, 'U', ModItems.upgrade_ejector_1 );
		addRecipeAuto(new ItemStack(ModItems.upgrade_ejector_3, 1), " C ", "PUP", " C ", 'C', ModItems.plate_saturnite, 'P', ModItems.motor, 'U', ModItems.upgrade_ejector_2 );

		addRecipeAuto(new ItemStack(ModItems.mech_key, 1), "MCM", "MKM", "MMM", 'M', ModItems.ingot_meteorite_forged, 'C', ModItems.coin_maskman, 'K', ModItems.key );
		addRecipeAuto(new ItemStack(ModItems.spawn_ufo, 1), "MMM", "DCD", "MMM", 'M', ModItems.ingot_meteorite, 'D', DNT.ingot(), 'C', ModItems.coin_worm );

		addShapelessAuto(new ItemStack(ModItems.wire_dense, 4, Mats.MAT_GOLD.id), ModBlocks.hadron_coil_gold );
		addShapelessAuto(new ItemStack(ModItems.wire_dense, 4, Mats.MAT_NEODYMIUM.id), ModBlocks.hadron_coil_neodymium );
		addShapelessAuto(new ItemStack(ModItems.wire_dense, 4, Mats.MAT_MAGTUNG.id), ModBlocks.hadron_coil_magtung );
		addShapelessAuto(new ItemStack(ModItems.wire_dense, 2, Mats.MAT_SCHRABIDIUM.id), ModBlocks.hadron_coil_schrabidium );
		addShapelessAuto(new ItemStack(ModItems.wire_dense, 2, Mats.MAT_SCHRABIDATE.id), ModBlocks.hadron_coil_schrabidate );
		addShapelessAuto(new ItemStack(ModItems.wire_dense, 2, Mats.MAT_STAR.id), ModBlocks.hadron_coil_starmetal );
		addShapelessAuto(new ItemStack(ModItems.powder_chlorophyte, 2), ModBlocks.hadron_coil_chlorophyte );
		addShapelessAuto(new ItemStack(ModItems.wire_dense, 1, Mats.MAT_DNT.id), ModBlocks.hadron_coil_mese );
		addShapelessAuto(new ItemStack(ModItems.plate_cast, 1, Mats.MAT_STEEL.id), ModBlocks.hadron_plating );
		addShapelessAuto(new ItemStack(ModItems.plate_cast, 1, Mats.MAT_STEEL.id), ModBlocks.hadron_plating_blue );
		addShapelessAuto(new ItemStack(ModItems.plate_cast, 1, Mats.MAT_STEEL.id), ModBlocks.hadron_plating_black );
		addShapelessAuto(new ItemStack(ModItems.plate_cast, 1, Mats.MAT_STEEL.id), ModBlocks.hadron_plating_yellow );
		addShapelessAuto(new ItemStack(ModItems.plate_cast, 1, Mats.MAT_STEEL.id), ModBlocks.hadron_plating_striped );
		addShapelessAuto(new ItemStack(ModItems.plate_cast, 1, Mats.MAT_STEEL.id), ModBlocks.hadron_plating_glass );
		addShapelessAuto(new ItemStack(ModItems.plate_cast, 1, Mats.MAT_STEEL.id), ModBlocks.hadron_plating_voltz );
		addShapelessAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), ModBlocks.hadron_analysis );
		addShapelessAuto(DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), ModBlocks.hadron_analysis_glass );

		addRecipeAuto(new ItemStack(ModBlocks.hadron_coil_alloy, 1), "WW", "WW", 'W', ALLOY.wireDense() );
		//addRecipeAuto(new ItemStack(ModBlocks.hadron_coil_gold, 1), "WG", "GW", 'W', ALLOY.wireDense(), 'G', GOLD.wireDense() );
		//addRecipeAuto(new ItemStack(ModBlocks.hadron_coil_neodymium, 1), "WG", "GW", 'W', ND.wireDense(), 'G', GOLD.wireDense() );
		//addRecipeAuto(new ItemStack(ModBlocks.hadron_coil_magtung, 1), "WW", "WW", 'W', MAGTUNG.wireDense() );
		//addRecipeAuto(new ItemStack(ModBlocks.hadron_coil_schrabidium, 1), "WS", "SW", 'W', MAGTUNG.wireDense(), 'S', SA326.wireDense() );
		//addRecipeAuto(new ItemStack(ModBlocks.hadron_coil_schrabidate, 1), "WS", "SW", 'W', SBD.wireDense(), 'S', SA326.wireDense() );
		//addRecipeAuto(new ItemStack(ModBlocks.hadron_coil_starmetal, 1), "SW", "WS", 'W', SBD.wireDense(), 'S', STAR.wireDense() );
		//addRecipeAuto(new ItemStack(ModBlocks.hadron_coil_chlorophyte, 1), "TC", "CT", 'T', CU.wireDense(), 'C', ModItems.powder_chlorophyte );
		//addRecipeAuto(new ItemStack(ModBlocks.hadron_diode, 1), "CIC", "ISI", "CIC", 'C', ModBlocks.hadron_coil_alloy, 'I', STEEL.ingot(), 'S', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED) );
		//addRecipeAuto(new ItemStack(ModBlocks.hadron_plating, 16), "CC", "CC", 'C', STEEL.plateCast());
		//addShapelessAuto(new ItemStack(ModBlocks.hadron_plating_blue, 1), ModBlocks.hadron_plating, KEY_BLUE );
		//addShapelessAuto(new ItemStack(ModBlocks.hadron_plating_black, 1), ModBlocks.hadron_plating, KEY_BLACK );
		//addShapelessAuto(new ItemStack(ModBlocks.hadron_plating_yellow, 1), ModBlocks.hadron_plating, KEY_YELLOW );
		//addShapelessAuto(new ItemStack(ModBlocks.hadron_plating_striped, 1), ModBlocks.hadron_plating, KEY_BLACK, KEY_YELLOW );
		//addShapelessAuto(new ItemStack(ModBlocks.hadron_plating_glass, 1), ModBlocks.hadron_plating, KEY_ANYGLASS );
		//addShapelessAuto(new ItemStack(ModBlocks.hadron_plating_voltz, 1), ModBlocks.hadron_plating, KEY_RED );
		//addRecipeAuto(new ItemStack(ModBlocks.hadron_power, 1), "SFS", "FTF", "SFS", 'S', BIGMT.ingot(), 'T', ModBlocks.machine_transformer, 'F', ModItems.fuse );
		//addRecipeAuto(new ItemStack(ModBlocks.hadron_power_10m, 1), "HF", 'H', ModBlocks.hadron_power, 'F', ModItems.fuse );
		//addRecipeAuto(new ItemStack(ModBlocks.hadron_power_100m, 1), "HF", 'H', ModBlocks.hadron_power_10m, 'F', ModItems.fuse );
		//addRecipeAuto(new ItemStack(ModBlocks.hadron_power_1g, 1), "HF", 'H', ModBlocks.hadron_power_100m, 'F', ModItems.fuse );
		//addRecipeAuto(new ItemStack(ModBlocks.hadron_power_10g, 1), "HF", 'H', ModBlocks.hadron_power_1g, 'F', ModItems.fuse );
		//addRecipeAuto(new ItemStack(ModBlocks.hadron_analysis, 1), "IPI", "PCP", "IPI", 'I', TI.ingot(), 'P', getReflector(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED) );
		//addShapelessAuto(new ItemStack(ModBlocks.hadron_analysis_glass, 1), ModBlocks.hadron_analysis, KEY_ANYGLASS );
		//addRecipeAuto(new ItemStack(ModBlocks.hadron_access, 1), "IGI", "CRC", "IPI", 'I', ModItems.plate_polymer, 'G', KEY_ANYPANE, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), 'R', REDSTONE.block(), 'P', ModBlocks.hadron_plating_blue );
		//addRecipeAuto(new ItemStack(ModBlocks.hadron_cooler, 1, 0), "PCP", "CHC", "PCP", 'P', ANY_RESISTANTALLOY.plateCast(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), 'H', Fluids.HELIUM4.getDict(16_000) );
		//addRecipeAuto(new ItemStack(ModBlocks.hadron_cooler, 1, 1), "PCP", "CHC", "PCP", 'P', GOLD.plateCast(), 'C', ModItems.motor_bismuth, 'H', new ItemStack(ModBlocks.hadron_cooler, 1, 0) );

		addRecipeAuto(new ItemStack(ModBlocks.fireworks, 1), "PPP", "PPP", "WIW", 'P', Items.PAPER, 'W', KEY_PLANKS, 'I', IRON.ingot() );
		addRecipeAuto(new ItemStack(ModItems.safety_fuse, 8), "SSS", "SGS", "SSS", 'S', Items.STRING, 'G', Items.GUNPOWDER );

		addRecipeAuto(new ItemStack(ModItems.rbmk_lid, 4), "PPP", "CCC", "PPP", 'P', STEEL.plate(), 'C', ModBlocks.concrete_asbestos );
		addRecipeAuto(new ItemStack(ModItems.rbmk_lid_glass, 4), "LLL", "BBB", "P P", 'P', STEEL.plate(), 'L', ModBlocks.glass_lead, 'B', ModBlocks.glass_boron );
		addRecipeAuto(new ItemStack(ModItems.rbmk_lid_glass, 4), "BBB", "LLL", "P P", 'P', STEEL.plate(), 'L', ModBlocks.glass_lead, 'B', ModBlocks.glass_boron );

		addRecipeAuto(new ItemStack(ModBlocks.rbmk_moderator, 1), " G ", "GRG", " G ", 'G', GRAPHITE.block(), 'R', ModBlocks.rbmk_blank );
		addRecipeAuto(new ItemStack(ModBlocks.rbmk_absorber, 1), "GGG", "GRG", "GGG", 'G', B.ingot(), 'R', ModBlocks.rbmk_blank );
		addRecipeAuto(new ItemStack(ModBlocks.rbmk_reflector, 1), "GGG", "GRG", "GGG", 'G', OreDictManager.getReflector(), 'R', ModBlocks.rbmk_blank );
		if(!GeneralConfig.enable528) {
			addRecipeAuto(new ItemStack(ModBlocks.rbmk_control, 1), " B ", "GRG", " B ", 'G', GRAPHITE.ingot(), 'B', ModItems.motor, 'R', ModBlocks.rbmk_absorber );
		} else {
			addRecipeAuto(new ItemStack(ModBlocks.rbmk_control, 1), "CBC", "GRG", "CBC", 'G', GRAPHITE.ingot(), 'B', ModItems.motor, 'R', ModBlocks.rbmk_absorber, 'C', CD.ingot() );
		}
		addRecipeAuto(new ItemStack(ModBlocks.rbmk_control_mod, 1), "BGB", "GRG", "BGB", 'G', GRAPHITE.block(), 'R', ModBlocks.rbmk_control, 'B', ModItems.nugget_bismuth );
		addRecipeAuto(new ItemStack(ModBlocks.rbmk_control_auto, 1), "C", "R", "D", 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'R', ModBlocks.rbmk_control, 'D', ModItems.crt_display );
		addRecipeAuto(new ItemStack(ModBlocks.rbmk_rod_reasim, 1), "ZCZ", "ZRZ", "ZCZ", 'C', STEEL.shell(), 'R', ModBlocks.rbmk_blank, 'Z', ZR.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.rbmk_rod_reasim_mod, 1), "BGB", "GRG", "BGB", 'G', GRAPHITE.block(), 'R', ModBlocks.rbmk_rod_reasim, 'B', ANY_RESISTANTALLOY.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.rbmk_outgasser, 1), "GHG", "GRG", "GTG", 'G', ModBlocks.steel_grate, 'H', Blocks.HOPPER, 'T', ModItems.tank_steel, 'R', ModBlocks.rbmk_blank );
		addRecipeAuto(new ItemStack(ModBlocks.rbmk_storage, 1), "C", "R", "C", 'C', ModBlocks.crate_steel, 'R', ModBlocks.rbmk_blank );
		addRecipeAuto(new ItemStack(ModBlocks.rbmk_loader, 1), "SCS", "CBC", "SCS", 'S', STEEL.plate(), 'C', CU.ingot(), 'B', ModItems.tank_steel );
		addRecipeAuto(new ItemStack(ModBlocks.rbmk_steam_inlet, 1), "SCS", "CBC", "SCS", 'S', STEEL.ingot(), 'C', IRON.plate(), 'B', ModItems.tank_steel );
		addRecipeAuto(new ItemStack(ModBlocks.rbmk_steam_outlet, 1), "SCS", "CBC", "SCS", 'S', STEEL.ingot(), 'C', CU.plate(), 'B', ModItems.tank_steel );
		//addRecipeAuto(new ItemStack(ModBlocks.rbmk_heatex, 1), "SCS", "CBC", "SCS", 'S', STEEL.ingot(), 'C', CU.plate(), 'B', ModItems.pipes_steel );

		addRecipeAuto(new ItemStack(ModBlocks.deco_rbmk, 8), "R", 'R', ModBlocks.rbmk_blank );
		addRecipeAuto(new ItemStack(ModBlocks.deco_rbmk_smooth, 1), "R", 'R', ModBlocks.deco_rbmk );
		addRecipeAuto(new ItemStack(ModBlocks.rbmk_blank, 1), "RRR", "R R", "RRR", 'R', ModBlocks.deco_rbmk );
		addRecipeAuto(new ItemStack(ModBlocks.rbmk_blank, 1), "RRR", "R R", "RRR", 'R', ModBlocks.deco_rbmk_smooth );

		addRecipeAuto(new ItemStack(ModBlocks.ladder_sturdy, 8), "LLL", "L#L", "LLL", 'L', Blocks.LADDER, '#', KEY_PLANKS );
		addRecipeAuto(new ItemStack(ModBlocks.ladder_iron, 8), "LLL", "L#L", "LLL", 'L', Blocks.LADDER, '#', IRON.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.ladder_gold, 8), "LLL", "L#L", "LLL", 'L', Blocks.LADDER, '#', GOLD.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.ladder_aluminium, 8), "LLL", "L#L", "LLL", 'L', Blocks.LADDER, '#', AL.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.ladder_copper, 8), "LLL", "L#L", "LLL", 'L', Blocks.LADDER, '#', CU.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.ladder_titanium, 8), "LLL", "L#L", "LLL", 'L', Blocks.LADDER, '#', TI.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.ladder_lead, 8), "LLL", "L#L", "LLL", 'L', Blocks.LADDER, '#', PB.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.ladder_cobalt, 8), "LLL", "L#L", "LLL", 'L', Blocks.LADDER, '#', CO.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.ladder_steel, 8), "LLL", "L#L", "LLL", 'L', Blocks.LADDER, '#', STEEL.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.ladder_tungsten, 8), "LLL", "L#L", "LLL", 'L', Blocks.LADDER, '#', W.ingot() );
		addShapelessAuto(new ItemStack(ModBlocks.trapdoor_steel, 1), Blocks.TRAPDOOR, STEEL.ingot() );

		addRecipeAuto(new ItemStack(ModBlocks.machine_storage_drum), "LLL", "L#L", "LLL", 'L', PB.plate(), '#', ModItems.tank_steel );

		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe, 6), "PP", 'P', STEEL.pipe() );
		addShapelessAuto(new ItemStack(ModBlocks.deco_pipe, 1), ModBlocks.deco_pipe_rim );
		addShapelessAuto(new ItemStack(ModBlocks.deco_pipe, 1), ModBlocks.deco_pipe_framed );
		addShapelessAuto(new ItemStack(ModBlocks.deco_pipe, 1), ModBlocks.deco_pipe_quad );

		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_rim, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe, 'C', STEEL.plate() );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_quad, 4), "PP", "PP", 'P', ModBlocks.deco_pipe );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_framed, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe, 'C', Blocks.IRON_BARS );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_framed, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe_rim, 'C', Blocks.IRON_BARS );

		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_rusted, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe, 'C', IRON.dust() );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_rim_rusted, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe_rim, 'C', IRON.dust() );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_quad_rusted, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe_quad, 'C', IRON.dust() );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_framed_rusted, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe_framed, 'C', IRON.dust() );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_green, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe, 'C', KEY_GREEN );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_rim_green, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe_rim, 'C', KEY_GREEN );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_quad_green, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe_quad, 'C', KEY_GREEN );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_framed_green, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe_framed, 'C', KEY_GREEN );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_green_rusted, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe_green, 'C', IRON.dust() );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_rim_green_rusted, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe_rim_green, 'C', IRON.dust() );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_quad_green_rusted, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe_quad_green, 'C', IRON.dust() );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_framed_green_rusted, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe_framed_green, 'C', IRON.dust() );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_red, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe, 'C', KEY_RED );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_rim_red, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe_rim, 'C', KEY_RED );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_quad_red, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe_quad, 'C', KEY_RED );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_framed_red, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe_framed, 'C', KEY_RED );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_marked, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe_green, 'C', KEY_GREEN );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_rim_marked, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe_rim_green, 'C', KEY_GREEN );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_quad_marked, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe_quad_green, 'C', KEY_GREEN );
		addRecipeAuto(new ItemStack(ModBlocks.deco_pipe_framed_marked, 8), "PPP", "PCP", "PPP", 'P', ModBlocks.deco_pipe_framed_green, 'C', KEY_GREEN );
		// TODO
		//addRecipeAuto(new ItemStack(ModBlocks.deco_emitter), "IDI", "DRD", "IDI", 'I', IRON.ingot(), 'D', DIAMOND.gem(), 'R', REDSTONE.block() );

		addRecipeAuto(new ItemStack(Items.NAME_TAG), "SB ", "BPB", " BP", 'S', Items.STRING, 'B', KEY_SLIME, 'P', Items.PAPER );
		addRecipeAuto(new ItemStack(Items.NAME_TAG), "SB ", "BPB", " BP", 'S', Items.STRING, 'B', ANY_TAR.any(), 'P', Items.PAPER );
		addRecipeAuto(new ItemStack(Items.LEAD, 4), "RSR", 'R', DictFrame.fromOne(ModItems.plant_item, ItemEnums.EnumPlantType.ROPE), 'S', KEY_SLIME );
		addRecipeAuto(new ItemStack(ModItems.rag, 4), "SW", "WS", 'S', Items.STRING, 'W', Blocks.WOOL );

		addShapelessAuto(new ItemStack(ModItems.solid_fuel, 3), Fluids.HEATINGOIL.getDict(16000), new ComplexOreIngredient(KEY_TOOL_CHEMISTRYSET));
		addShapelessAuto(new ItemStack(ModItems.canister_full, 2, Fluids.LUBRICANT.getID()), Fluids.HEATINGOIL.getDict(1000), Fluids.UNSATURATEDS.getDict(1000), ModItems.canister_empty, ModItems.canister_empty, new ComplexOreIngredient(KEY_TOOL_CHEMISTRYSET));

		addRecipeAuto(new ItemStack(ModBlocks.machine_condenser), "SIS", "ICI", "SIS", 'S', STEEL.ingot(), 'I', IRON.plate(), 'C', CU.plateCast() );

		addShapelessAuto(new ItemStack(ModItems.book_guide, 1, ItemGuideBook.BookType.RBMK.ordinal()), Items.BOOK, Items.POTATO );
		addShapelessAuto(new ItemStack(ModItems.book_guide, 1, ItemGuideBook.BookType.STARTER.ordinal()), Items.BOOK, Items.IRON_INGOT );
		addRecipeAuto(new ItemStack(ModBlocks.charger), "G", "S", "C", 'G', Items.GLOWSTONE_DUST, 'S', STEEL.ingot(), 'C', ModItems.coil_copper );
		addRecipeAuto(new ItemStack(ModBlocks.charger, 16), "G", "S", "C", 'G', Blocks.GLOWSTONE, 'S', STEEL.block(), 'C', ModItems.coil_copper_torus );
		addRecipeAuto(new ItemStack(ModBlocks.refueler), "SS", "HC", "SS", 'S', TI.plate(), 'H', DictFrame.fromOne(ModItems.part_generic, EnumPartType.PISTON_HYDRAULIC), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC) );
		addRecipeAuto(new ItemStack(ModBlocks.press_preheater), "CCC", "SLS", "TST", 'C', CU.plate(), 'S', Blocks.STONE, 'L', Fluids.LAVA.getDict(1000), 'T', W.ingot() );
		addRecipeAuto(new ItemStack(ModItems.fluid_identifier_multi), "D", "C", "P", 'D', "dye", 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ANALOG), 'P', IRON.plate() );

		addShapelessAuto(ItemBattery.getEmptyBattery(ModItems.anchor_remote), DIAMOND.gem(), ModItems.ducttape, DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC) );
		addRecipeAuto(new ItemStack(ModBlocks.teleanchor), "ODO", "EAE", "ODO", 'O', Blocks.OBSIDIAN, 'D', DIAMOND.gem(), 'E', ModItems.powder_magic, 'A', ModItems.gem_alexandrite );
		addRecipeAuto(new ItemStack(ModBlocks.field_disturber), "ICI", "CAC", "ICI", 'I', STAR.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BISMOID), 'A', ModItems.gem_alexandrite );
		// TODO: holotapes
		/*addShapelessAuto(new ItemStack(ModItems.holotape_image, 1, EnumHoloImage.HOLO_RESTORED.ordinal()), new ItemStack(ModItems.holotape_image, 1, EnumHoloImage.HOLO_DIGAMMA.ordinal()), KEY_TOOL_SCREWDRIVER, ModItems.ducttape, ModItems.armor_polish );
		addShapelessAuto(new ItemStack(ModItems.holotape_damaged), DictFrame.fromOne(ModItems.holotape_image, EnumHoloImage.HOLO_RESTORED), ModItems.upgrade_muffler, ModItems.crt_display, ModItems.gem_alexandrite ); */ // placeholder for amplifier

		addRecipeAuto(DictFrame.fromOne(ModItems.part_generic, EnumPartType.PISTON_PNEUMATIC, 4), " I ", "CPC", " I ", 'I', IRON.ingot(), 'C', CU.ingot(), 'P', IRON.plate() );
		addRecipeAuto(DictFrame.fromOne(ModItems.part_generic, EnumPartType.PISTON_HYDRAULIC, 4), " I ", "CPC", " I ", 'I', STEEL.ingot(), 'C', TI.ingot(), 'P', Fluids.LUBRICANT.getDict(1000) );
		addRecipeAuto(DictFrame.fromOne(ModItems.part_generic, EnumPartType.PISTON_ELECTRIC, 4), " I ", "CPC", " I ", 'I', ANY_RESISTANTALLOY.ingot(), 'C', ANY_PLASTIC.ingot(), 'P', ModItems.motor );

		Object[] craneCasing = new Object[] {
				Blocks.STONEBRICK, 1,
				IRON.ingot(), 2,
				STEEL.ingot(), 4
		};

		for(int i = 0; i < craneCasing.length / 2; i++) {
			Object casing = craneCasing[i * 2];
			int amount = (int) craneCasing[i * 2 + 1];
			addRecipeAuto(new ItemStack(ModBlocks.crane_inserter, amount), "CCC", "C C", "CBC", 'C', casing, 'B', DictFrame.fromOne(ModItems.conveyor_wand, ItemConveyorWand.ConveyorType.REGULAR) );
			addRecipeAuto(new ItemStack(ModBlocks.crane_inserter, amount), "CCC", "C C", "CBC", 'C', casing, 'B', ModBlocks.conveyor );
			addRecipeAuto(new ItemStack(ModBlocks.crane_extractor, amount), "CCC", "CPC", "CBC", 'C', casing, 'B', DictFrame.fromOne(ModItems.conveyor_wand, ItemConveyorWand.ConveyorType.REGULAR), 'P', DictFrame.fromOne(ModItems.part_generic, EnumPartType.PISTON_PNEUMATIC) );
			addRecipeAuto(new ItemStack(ModBlocks.crane_extractor, amount), "CCC", "CPC", "CBC", 'C', casing, 'B', ModBlocks.conveyor, 'P', DictFrame.fromOne(ModItems.part_generic, EnumPartType.PISTON_PNEUMATIC) );
			addRecipeAuto(new ItemStack(ModBlocks.crane_grabber, amount), "C C", "P P", "CBC", 'C', casing, 'B', DictFrame.fromOne(ModItems.conveyor_wand, ItemConveyorWand.ConveyorType.REGULAR), 'P', DictFrame.fromOne(ModItems.part_generic, EnumPartType.PISTON_PNEUMATIC) );
			addRecipeAuto(new ItemStack(ModBlocks.crane_grabber, amount), "C C", "P P", "CBC", 'C', casing, 'B', ModBlocks.conveyor, 'P', DictFrame.fromOne(ModItems.part_generic, EnumPartType.PISTON_PNEUMATIC) );
		}

		addRecipeAuto(new ItemStack(ModBlocks.crane_boxer), "WWW", "WPW", "CCC", 'W', KEY_PLANKS, 'P', DictFrame.fromOne(ModItems.part_generic, EnumPartType.PISTON_PNEUMATIC), 'C', DictFrame.fromOne(ModItems.conveyor_wand, ItemConveyorWand.ConveyorType.REGULAR) );
		addRecipeAuto(new ItemStack(ModBlocks.crane_unboxer), "WWW", "WPW", "CCC", 'W', KEY_STICK, 'P', Items.SHEARS, 'C', DictFrame.fromOne(ModItems.conveyor_wand, ItemConveyorWand.ConveyorType.REGULAR) );
		addRecipeAuto(new ItemStack(ModBlocks.crane_router), "PIP", "ICI", "PIP", 'P', DictFrame.fromOne(ModItems.part_generic, EnumPartType.PISTON_PNEUMATIC), 'I', ModItems.plate_polymer, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC) );
		addRecipeAuto(new ItemStack(ModBlocks.crane_splitter), "III", "PCP", "III", 'P', DictFrame.fromOne(ModItems.part_generic, EnumPartType.PISTON_PNEUMATIC), 'I', STEEL.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE) );
		addRecipeAuto(new ItemStack(ModBlocks.crane_partitioner), " M ", "BCB", 'M', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP), 'B', DictFrame.fromOne(ModItems.conveyor_wand, ItemConveyorWand.ConveyorType.REGULAR), 'C', ModBlocks.crate_steel );

		addRecipeAuto(new ItemStack(ModBlocks.machine_conveyor_press), "CPC", "CBC", "CCC", 'C', CU.plate(), 'P', ModBlocks.machine_epress, 'B', DictFrame.fromOne(ModItems.conveyor_wand, ItemConveyorWand.ConveyorType.REGULAR) );
		addRecipeAuto(new ItemStack(ModBlocks.radar_screen), "PCP", "SRS", "PCP", 'P', ANY_PLASTIC.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), 'S', STEEL.plate(), 'R', ModItems.crt_display );
		addRecipeAuto(new ItemStack(ModItems.radar_linker), "S", "C", "P", 'S', ModItems.crt_display, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), 'P', STEEL.plate() );

		addRecipeAuto(new ItemStack(ModItems.drone, 2, ItemDrone.EnumDroneType.PATROL.ordinal()), " P ", "HCH", " B ", 'P', ANY_PLASTIC.ingot(), 'H', STEEL.pipe(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE), 'B', STEEL.shell() );
		addRecipeAuto(new ItemStack(ModItems.drone, 1, ItemDrone.EnumDroneType.PATROL_CHUNKLOADING.ordinal()), "E", "D", 'E', Items.ENDER_PEARL, 'D', new ItemStack(ModItems.drone, 1, ItemDrone.EnumDroneType.PATROL.ordinal()) );
		addRecipeAuto(new ItemStack(ModItems.drone, 1, ItemDrone.EnumDroneType.PATROL_EXPRESS.ordinal()), " P ", "KDK", " P ", 'P', TI.plateWelded(), 'K', Fluids.KEROSENE.getDict(1_000), 'D', new ItemStack(ModItems.drone, 1, ItemDrone.EnumDroneType.PATROL.ordinal()) );
		addRecipeAuto(new ItemStack(ModItems.drone, 1, ItemDrone.EnumDroneType.PATROL_EXPRESS_CHUNKLOADING.ordinal()), "E", "D", 'E', Items.ENDER_PEARL, 'D', new ItemStack(ModItems.drone, 1, ItemDrone.EnumDroneType.PATROL_EXPRESS.ordinal()) );
		addRecipeAuto(new ItemStack(ModItems.drone, 1, ItemDrone.EnumDroneType.PATROL_EXPRESS_CHUNKLOADING.ordinal()), " P ", "KDK", " P ", 'P', TI.plateWelded(), 'K', Fluids.KEROSENE.getDict(1_000), 'D', new ItemStack(ModItems.drone, 1, ItemDrone.EnumDroneType.PATROL_CHUNKLOADING.ordinal()) );
		addShapelessAuto(new ItemStack(ModItems.drone, 1, ItemDrone.EnumDroneType.PATROL.ordinal()), new ItemStack(ModItems.drone, 1, ItemDrone.EnumDroneType.PATROL_CHUNKLOADING.ordinal()) );
		addShapelessAuto(new ItemStack(ModItems.drone, 1, ItemDrone.EnumDroneType.PATROL_EXPRESS.ordinal()), new ItemStack(ModItems.drone, 1, ItemDrone.EnumDroneType.PATROL_EXPRESS_CHUNKLOADING.ordinal()) );
		addRecipeAuto(new ItemStack(ModItems.drone, 1, ItemDrone.EnumDroneType.REQUEST.ordinal()), "E", "D", 'E', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP), 'D', new ItemStack(ModItems.drone, 1, ItemDrone.EnumDroneType.PATROL.ordinal()) );

		addRecipeAuto(new ItemStack(ModItems.drone_linker), "T", "C", 'T', ModBlocks.drone_waypoint, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC) );
		addRecipeAuto(new ItemStack(ModBlocks.drone_waypoint, 4), "G", "T", "C", 'G', KEY_GREEN, 'T', Blocks.REDSTONE_TORCH, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC) );
		addRecipeAuto(new ItemStack(ModBlocks.drone_crate), "T", "C", 'T', ModBlocks.drone_waypoint, 'C', ModBlocks.crate_steel );
		addRecipeAuto(new ItemStack(ModBlocks.drone_waypoint_request, 4), "G", "T", "C", 'G', KEY_BLUE, 'T', Blocks.REDSTONE_TORCH, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC) );
		addRecipeAuto(new ItemStack(ModBlocks.drone_crate_requester), "T", "C", "B", 'T', ModBlocks.drone_waypoint_request, 'C', ModBlocks.crate_steel, 'B', KEY_YELLOW );
		addRecipeAuto(new ItemStack(ModBlocks.drone_crate_provider), "T", "C", "B", 'T', ModBlocks.drone_waypoint_request, 'C', ModBlocks.crate_steel, 'B', KEY_ORANGE );
		addRecipeAuto(new ItemStack(ModBlocks.drone_dock), "T", "C", "B", 'T', ModBlocks.drone_waypoint_request, 'C', ModBlocks.crate_steel, 'B', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED) );

		addRecipeAuto(new ItemStack(ModItems.ball_resin), "DD", "DD", 'D', Blocks.YELLOW_FLOWER );

		addShapelessAuto(DictFrame.fromOne(ModItems.parts_legendary, ItemEnums.EnumLegendaryType.TIER1), ModItems.ingot_chainsteel, ASBESTOS.ingot(), ModItems.gem_alexandrite );
		addShapelessAuto(DictFrame.fromOne(ModItems.parts_legendary, ItemEnums.EnumLegendaryType.TIER1, 3), DictFrame.fromOne(ModItems.parts_legendary, ItemEnums.EnumLegendaryType.TIER2) );
		addShapelessAuto(DictFrame.fromOne(ModItems.parts_legendary, ItemEnums.EnumLegendaryType.TIER2), ModItems.ingot_chainsteel, ModItems.ingot_bismuth, ModItems.gem_alexandrite, ModItems.gem_alexandrite );
		addShapelessAuto(DictFrame.fromOne(ModItems.parts_legendary, ItemEnums.EnumLegendaryType.TIER2, 3), DictFrame.fromOne(ModItems.parts_legendary, ItemEnums.EnumLegendaryType.TIER3) );
		addShapelessAuto(DictFrame.fromOne(ModItems.parts_legendary, ItemEnums.EnumLegendaryType.TIER3), ModItems.ingot_chainsteel, ModItems.ingot_smore, ModItems.gem_alexandrite, ModItems.gem_alexandrite, ModItems.gem_alexandrite );

		addRecipeAuto(new ItemStack(ModItems.gear_large, 1, 0), "III", "ICI", "III", 'I', IRON.plate(), 'C', CU.ingot());
		addRecipeAuto(new ItemStack(ModItems.gear_large, 1, 1), "III", "ICI", "III", 'I', STEEL.plate(), 'C', TI.ingot());
		addRecipeAuto(new ItemStack(ModItems.sawblade), "III", "ICI", "III", 'I', STEEL.plate(), 'C', IRON.ingot());

		addRecipeAuto(new ItemStack(ModBlocks.foundry_basin), "B B", "B B", "BSB", 'B', ModItems.ingot_firebrick, 'S', Blocks.STONE_SLAB );
		addRecipeAuto(new ItemStack(ModBlocks.foundry_mold), "B B", "BSB", 'B', ModItems.ingot_firebrick, 'S', Blocks.STONE_SLAB );
		addRecipeAuto(new ItemStack(ModBlocks.foundry_channel, 4), "B B", " S ", 'B', ModItems.ingot_firebrick, 'S', Blocks.STONE_SLAB );
		addShapelessAuto(new ItemStack(ModBlocks.foundry_outlet), ModBlocks.foundry_channel, STEEL.plate() );
		// TODO: foundry tank/slagtap
		//addRecipeAuto(new ItemStack(ModBlocks.foundry_tank), "B B", "I I", "BSB", 'B', ModItems.ingot_firebrick, 'I', STEEL.ingot(), 'S', Blocks.STONE_SLAB );
		//addShapelessAuto(new ItemStack(ModBlocks.foundry_slagtap), ModBlocks.foundry_channel, Blocks.STONEBRICK );
		addRecipeAuto(new ItemStack(ModItems.mold_base), " B ", "BIB", " B ", 'B', ModItems.ingot_firebrick, 'I', IRON.ingot() );
		addRecipeAuto(new ItemStack(ModBlocks.brick_fire), "BB", "BB", 'B', ModItems.ingot_firebrick );
		addShapelessAuto(new ItemStack(ModItems.ingot_firebrick, 4), ModBlocks.brick_fire );

		addRecipeAuto(new ItemStack(ModBlocks.machine_drain), "PPP", "T  ", "PPP", 'P', STEEL.plateCast(), 'T', ModItems.tank_steel );
		addRecipeAuto(new ItemStack(ModBlocks.machine_intake), "GGG", "PMP", "PTP", 'G', ModBlocks.steel_grate, 'P', STEEL.plate(), 'M', ModItems.motor, 'T', ModItems.tank_steel );
		addRecipeAuto(new ItemStack(ModBlocks.filing_cabinet, 1, BlockEnums.DecoCabinetEnum.STEEL.ordinal()), " P ", "PIP", " P ", 'P', STEEL.plate(), 'I', ModItems.plate_polymer );

		addRecipeAuto(new ItemStack(ModBlocks.vinyl_tile, 4), " I ", "IBI", " I ", 'I', ModItems.plate_polymer, 'B', ModBlocks.brick_light );
		addRecipeAuto(new ItemStack(ModBlocks.vinyl_tile, 4, 1), "BB", "BB", 'B', new ItemStack(ModBlocks.vinyl_tile, 1, 0) );
		addShapelessAuto(new ItemStack(ModBlocks.vinyl_tile), new ItemStack(ModBlocks.vinyl_tile, 1, 1) );

		addShapelessAuto(new ItemStack(ModItems.upgrade_5g), ModItems.upgrade_template, ModItems.gem_alexandrite );

		addShapelessAuto(new ItemStack(ModItems.bdcl), ANY_TAR.any(), Fluids.WATER.getDict(1_000), KEY_WHITE );

		addShapelessAuto(new ItemStack(ModItems.book_of_), DictFrame.fromOne(ModItems.page_of_, ItemEnums.EnumPages.PAGE1), DictFrame.fromOne(ModItems.page_of_, ItemEnums.EnumPages.PAGE2), DictFrame.fromOne(ModItems.page_of_, ItemEnums.EnumPages.PAGE3), DictFrame.fromOne(ModItems.page_of_, ItemEnums.EnumPages.PAGE4), DictFrame.fromOne(ModItems.page_of_, ItemEnums.EnumPages.PAGE5), DictFrame.fromOne(ModItems.page_of_, ItemEnums.EnumPages.PAGE6), DictFrame.fromOne(ModItems.page_of_, ItemEnums.EnumPages.PAGE7), DictFrame.fromOne(ModItems.page_of_, ItemEnums.EnumPages.PAGE8), ModItems.egg_balefire );

		if(GeneralConfig.enableLBSM && GeneralConfig.enableLBSMSimpleCrafting) {
			addShapelessAuto(new ItemStack(ModItems.cordite, 3), ModItems.ballistite, Items.GUNPOWDER, new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE) );
			addShapelessAuto(new ItemStack(ModItems.ingot_semtex, 3), Items.SLIME_BALL, Blocks.TNT, KNO.dust() );
			addShapelessAuto(new ItemStack(ModItems.canister_full, 1, Fluids.DIESEL.getID()), new ItemStack(ModItems.canister_full, 1, Fluids.OIL.getID()), REDSTONE.dust(), ModItems.canister_empty );

			addShapelessAuto(new ItemStack(ModBlocks.ore_uranium, 1), ModBlocks.ore_uranium_scorched, Items.WATER_BUCKET );
			addRecipeAuto(new ItemStack(ModBlocks.ore_uranium, 8), "OOO", "OBO", "OOO", 'O', ModBlocks.ore_uranium_scorched, 'B', Items.WATER_BUCKET );
			addShapelessAuto(new ItemStack(ModBlocks.ore_nether_uranium, 1), ModBlocks.ore_nether_uranium_scorched, Items.WATER_BUCKET );
			addRecipeAuto(new ItemStack(ModBlocks.ore_nether_uranium, 8), "OOO", "OBO", "OOO", 'O', ModBlocks.ore_nether_uranium_scorched, 'B', Items.WATER_BUCKET );
			addShapelessAuto(new ItemStack(ModBlocks.ore_gneiss_uranium, 1), ModBlocks.ore_gneiss_uranium_scorched, Items.WATER_BUCKET );
			addRecipeAuto(new ItemStack(ModBlocks.ore_gneiss_uranium, 8), "OOO", "OBO", "OOO", 'O', ModBlocks.ore_gneiss_uranium_scorched, 'B', Items.WATER_BUCKET );
			addShapelessAuto(new ItemStack(ModBlocks.ore_uranium, 1), ModBlocks.ore_sellafield_uranium_scorched, Items.WATER_BUCKET );
			addRecipeAuto(new ItemStack(ModBlocks.ore_uranium, 8), "OOO", "OBO", "OOO", 'O', ModBlocks.ore_sellafield_uranium_scorched, 'B', Items.WATER_BUCKET );

			addRecipeAuto(new ItemStack(ModItems.plate_iron, 4), "##", "##", '#', IRON.ingot() );
			addRecipeAuto(new ItemStack(ModItems.plate_gold, 4), "##", "##", '#', GOLD.ingot() );
			addRecipeAuto(new ItemStack(ModItems.plate_aluminium, 4), "##", "##", '#', AL.ingot() );
			addRecipeAuto(new ItemStack(ModItems.plate_titanium, 4), "##", "##", '#', TI.ingot() );
			addRecipeAuto(new ItemStack(ModItems.plate_copper, 4), "##", "##", '#', CU.ingot() );
			addRecipeAuto(new ItemStack(ModItems.plate_lead, 4), "##", "##", '#', PB.ingot() );
			addRecipeAuto(new ItemStack(ModItems.plate_steel, 4), "##", "##", '#', STEEL.ingot() );
			addRecipeAuto(new ItemStack(ModItems.plate_schrabidium, 4), "##", "##", '#', SA326.ingot() );
			addRecipeAuto(new ItemStack(ModItems.plate_advanced_alloy, 4), "##", "##", '#', ALLOY.ingot() );
			addRecipeAuto(new ItemStack(ModItems.plate_saturnite, 4), "##", "##", '#', BIGMT.ingot() );
			addRecipeAuto(new ItemStack(ModItems.plate_combine_steel, 4), "##", "##", '#', CMB.ingot() );
			addRecipeAuto(new ItemStack(ModItems.neutron_reflector, 4), "##", "##", '#', W.ingot() );

			for(NTMMaterial mat : Mats.orderedList) {
				if(mat.autogen.contains(MaterialShapes.WIRE)) for(String name : mat.names) addRecipeAuto(new ItemStack(ModItems.wire_fine, 24, mat.id), "###", '#', MaterialShapes.INGOT.name() + name );
			}

			addRecipeAuto(new ItemStack(ModItems.book_of_), "BGB", "GAG", "BGB", 'B', ModItems.egg_balefire_shard, 'G', GOLD.ingot(), 'A', Items.BOOK );
		}

		for(NTMMaterial mat : Mats.orderedList) {
			if(mat.autogen.contains(MaterialShapes.BOLT)) for(String name : mat.names) addRecipeAuto(new ItemStack(ModItems.bolt, 16, mat.id), "#", "#", '#', MaterialShapes.INGOT.name() + name );
		}

		if(!GeneralConfig.enable528) {
			addRecipeAuto(new ItemStack(ModBlocks.struct_launcher_core, 1), "SCS", "SIS", "BEB", 'S', ModBlocks.steel_scaffold, 'I', Blocks.IRON_BARS, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), 'B', ModBlocks.struct_launcher, 'E', new ItemStack(ModItems.battery_pack, 1, ItemBatteryPack.EnumBatteryPack.BATTERY_LEAD.ordinal()) );
			addRecipeAuto(new ItemStack(ModBlocks.struct_launcher_core_large, 1), "SIS", "ICI", "BEB", 'S', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'I', Blocks.IRON_BARS, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'B', ModBlocks.struct_launcher, 'E', new ItemStack(ModItems.battery_pack, 1, ItemBatteryPack.EnumBatteryPack.BATTERY_LEAD.ordinal()) );
			addRecipeAuto(new ItemStack(ModBlocks.struct_soyuz_core, 1), "CUC", "TST", "TBT", 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'U', ModItems.upgrade_power_3, 'T', ModBlocks.barrel_steel, 'S', ModBlocks.steel_scaffold, 'B', new ItemStack(ModItems.battery_pack, 1, ItemBatteryPack.EnumBatteryPack.BATTERY_LITHIUM.ordinal()) );
			addRecipeAuto(new ItemStack(ModItems.reactor_sensor, 1), "WPW", "CMC", "PPP", 'W', W.wireFine(), 'P', PB.plate(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), 'M', ModItems.magnetron );
			addRecipeAuto(new ItemStack(ModBlocks.rbmk_console, 1), "BBB", "DGD", "DCD", 'B', B.ingot(), 'D', ModBlocks.deco_rbmk, 'G', KEY_ANYPANE, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ANALOG) );
			addRecipeAuto(new ItemStack(ModBlocks.rbmk_crane_console, 1), "BCD", "DDD", 'B', B.ingot(), 'D', ModBlocks.deco_rbmk, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ANALOG) );
			addRecipeAuto(new ItemStack(ModBlocks.rbmk_rod, 1), "C", "R", "C", 'C', STEEL.shell(), 'R', ModBlocks.rbmk_blank );
			addRecipeAuto(new ItemStack(ModBlocks.rbmk_rod_mod, 1), "BGB", "GRG", "BGB", 'G', GRAPHITE.block(), 'R', ModBlocks.rbmk_rod, 'B', ModItems.nugget_bismuth );
			addRecipeAuto(new ItemStack(ModBlocks.rbmk_boiler, 1), "CPC", "CRC", "CPC", 'C', CU.pipe(), 'P', CU.shell(), 'R', ModBlocks.rbmk_blank );
			addRecipeAuto(new ItemStack(ModBlocks.rbmk_heater, 1), "CIC", "PRP", "CIC", 'C', CU.pipe(), 'P', STEEL.shell(), 'R', ModBlocks.rbmk_blank, 'I', ANY_PLASTIC.ingot() );
			addRecipeAuto(new ItemStack(ModBlocks.rbmk_cooler, 1), "IGI", "GCG", "IGI", 'C', ModBlocks.rbmk_blank, 'I', ModItems.plate_polymer, 'G', ModBlocks.steel_grate );
		}

		addShapelessAuto(new ItemStack(ModItems.launch_code), new ItemStack(ModItems.launch_code_piece), new ItemStack(ModItems.launch_code_piece),
				new ItemStack(ModItems.launch_code_piece), new ItemStack(ModItems.launch_code_piece),
				new ItemStack(ModItems.launch_code_piece), new ItemStack(ModItems.launch_code_piece),
				new ItemStack(ModItems.launch_code_piece), new ItemStack(ModItems.launch_code_piece),
				DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED)
		);

		addShapelessAuto(ModItems.circuit_star_component.stackFromEnum(CircuitComponentType.CHIPSET), ModItems.circuit_star_piece.stackFromEnum(ScrapType.BRIDGE_BIOS),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.BRIDGE_BUS),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.BRIDGE_CHIPSET),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.BRIDGE_CMOS),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.BRIDGE_IO),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.BRIDGE_NORTH),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.BRIDGE_SOUTH)
		);

		addShapelessAuto(ModItems.circuit_star_component.stackFromEnum(CircuitComponentType.CPU), ModItems.circuit_star_piece.stackFromEnum(ScrapType.CPU_CACHE),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.CPU_CLOCK),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.CPU_EXT),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.CPU_LOGIC),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.CPU_REGISTER),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.CPU_SOCKET)
		);

		addShapelessAuto(ModItems.circuit_star_component.stackFromEnum(CircuitComponentType.RAM), ModItems.circuit_star_piece.stackFromEnum(ScrapType.MEM_SOCKET),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.MEM_16K_A),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.MEM_16K_B),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.MEM_16K_C),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.MEM_16K_D)
		);

		addShapelessAuto(ModItems.circuit_star_component.stackFromEnum(CircuitComponentType.CARD), ModItems.circuit_star_piece.stackFromEnum(ScrapType.CARD_BOARD),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.CARD_PROCESSOR)
		);

		addShapelessAuto(new ItemStack(ModItems.circuit_star), ModItems.circuit_star_component.stackFromEnum(CircuitComponentType.CHIPSET),
				ModItems.circuit_star_component.stackFromEnum(CircuitComponentType.CPU),
				ModItems.circuit_star_component.stackFromEnum(CircuitComponentType.RAM),
				ModItems.circuit_star_component.stackFromEnum(CircuitComponentType.CARD),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.BOARD_TRANSISTOR),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.BOARD_CONVERTER),
				ModItems.circuit_star_piece.stackFromEnum(ScrapType.BOARD_BLANK)
		);

		addRecipeAuto(new ItemStack(ModItems.sliding_blast_door_skin0), "SPS", "DPD", "SPS", 'P', Items.PAPER, 'D', "dye", 'S', STEEL.plate());
		addShapelessAuto(new ItemStack(ModItems.sliding_blast_door_skin1, 1, 1), new ItemStack(ModItems.sliding_blast_door_skin0, 1, 0));
		addShapelessAuto(new ItemStack(ModItems.sliding_blast_door_skin2, 1, 2), new ItemStack(ModItems.sliding_blast_door_skin1, 1, 1));
		addShapelessAuto(new ItemStack(ModItems.sliding_blast_door_skin0), new ItemStack(ModItems.sliding_blast_door_skin2, 1, 2));
		// TODO: custom machines?.. do we really need that?
		/*addRecipeAuto(new ItemStack(ModBlocks.cm_block, 4, 0), " I ", "IPI", " I ", 'I', STEEL.ingot(), 'P', STEEL.plateCast());
		addRecipeAuto(new ItemStack(ModBlocks.cm_block, 4, 1), " I ", "IPI", " I ", 'I', ALLOY.ingot(), 'P', ALLOY.plateCast());
		addRecipeAuto(new ItemStack(ModBlocks.cm_block, 4, 2), " I ", "IPI", " I ", 'I', DESH.ingot(), 'P', DESH.plateCast());
		addRecipeAuto(new ItemStack(ModBlocks.cm_block, 4, 3), " I ", "IPI", " I ", 'I', ANY_RESISTANTALLOY.ingot(), 'P', ANY_RESISTANTALLOY.plateCast());

		for(int i = 0; i < 4; i++) {
			addRecipeAuto(new ItemStack(ModBlocks.cm_sheet, 16, i), "BB", "BB", 'B', new ItemStack(ModBlocks.cm_block, 1, i));
			addRecipeAuto(new ItemStack(ModBlocks.cm_tank, 4, i), " B ", "BGB", " B ", 'B', new ItemStack(ModBlocks.cm_block, 1, i), 'G', KEY_ANYGLASS);
			addRecipeAuto(new ItemStack(ModBlocks.cm_port, 1, i), "P", "B", "P", 'B', new ItemStack(ModBlocks.cm_block, 1, i), 'P', IRON.plate());
		}

		addRecipeAuto(new ItemStack(ModBlocks.cm_engine, 1, 0), " I ", "IMI", " I ", 'I', STEEL.ingot(), 'M', ModItems.motor);
		addRecipeAuto(new ItemStack(ModBlocks.cm_engine, 1, 1), " I ", "IMI", " I ", 'I', STEEL.ingot(), 'M', ModItems.motor_desh);
		addRecipeAuto(new ItemStack(ModBlocks.cm_engine, 1, 2), " I ", "IMI", " I ", 'I', STEEL.ingot(), 'M', ModItems.motor_bismuth);
		addRecipeAuto(new ItemStack(ModBlocks.cm_circuit, 1, 0), " I ", "IMI", " I ", 'I', STEEL.ingot(), 'M', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE));
		addRecipeAuto(new ItemStack(ModBlocks.cm_circuit, 1, 1), " I ", "IMI", " I ", 'I', STEEL.ingot(), 'M', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ANALOG));
		addRecipeAuto(new ItemStack(ModBlocks.cm_circuit, 1, 2), " I ", "IMI", " I ", 'I', STEEL.ingot(), 'M', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC));
		addRecipeAuto(new ItemStack(ModBlocks.cm_circuit, 1, 3), " I ", "IMI", " I ", 'I', STEEL.ingot(), 'M', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED));
		addRecipeAuto(new ItemStack(ModBlocks.cm_circuit, 1, 4), " I ", "IMI", " I ", 'I', STEEL.ingot(), 'M', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BISMOID));
		addRecipeAuto(new ItemStack(ModBlocks.cm_flux, 1, 0), "NNN", "ZCZ", "NNN", 'Z', ZR.plateCast(), 'N', ModItems.neutron_reflector, 'C', ModItems.reactor_core);
		addRecipeAuto(new ItemStack(ModBlocks.cm_heat, 1, 0), "PCP", "PCP", "PCP", 'P', ModItems.plate_polymer, 'C', CU.ingot());*/

	}

	public static void addUpgradeContainers(IForgeRegistry<IRecipe> registry){
		registry.register(
				new ContainerUpgradeCraftingHandler(new ItemStack(ModBlocks.crate_desh, 1), " D ", "DSD", " D ", 'D', ModItems.plate_desh, 'S', ModBlocks.crate_steel)
						.setRegistryName(new ResourceLocation(Tags.MODID, "crate_desh_upgrade"))
		);

		registry.register(
				new ContainerUpgradeCraftingHandler(new ItemStack(ModBlocks.crate_tungsten, 1), "BPB", "PCP", "BPB", 'B', W.block(), 'P', CU.plateCast(), 'C', ModBlocks.crate_steel)
						.setRegistryName(new ResourceLocation(Tags.MODID, "crate_tungsten_upgrade"))
		);

		// Note: voids the last few slots when placed, because a safe's inventory is smaller than a crate's one
		registry.register(
				new ContainerUpgradeCraftingHandler(new ItemStack(ModBlocks.safe, 1), "LAL", "ACA", "LAL", 'L', PB.plate(), 'A', ALLOY.plate(), 'C', ModBlocks.crate_steel)
						.setRegistryName(new ResourceLocation(Tags.MODID, "safe_upgrade"))
		);

		registry.register(
				new ContainerUpgradeCraftingHandler(new ItemStack(ModBlocks.mass_storage_desh, 1), " C ", "PMP", " P ", 'P', DESH.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP), 'M', new ItemStack(ModBlocks.mass_storage_iron))
						.setRegistryName(new ResourceLocation(Tags.MODID, "mass_storage_upgrade_1"))
		);

		registry.register(
				new ContainerUpgradeCraftingHandler(new ItemStack(ModBlocks.mass_storage, 1), " C ", "PMP", " P ", 'P', ANY_RESISTANTALLOY.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'M', new ItemStack(ModBlocks.mass_storage_desh))
						.setRegistryName(new ResourceLocation(Tags.MODID, "mass_storage_upgrade_2"))
		);
	}

	public static void addSlabStair(Block slab, Block stair, Block block){
		addRecipeAuto(new ItemStack(slab, 6), "###", '#', block );
		addRecipeAuto(new ItemStack(stair, 8), "#  ","## ","###", '#', block );
		addShapelessAuto(new ItemStack(block, 3), stair, stair, stair, stair );
		addRecipeAuto(new ItemStack(stair, 4), "#  ","## ","###", '#', slab );
		addShapelessAuto(new ItemStack(block, 1), slab, slab );
	}

	public static void addSlabStairColConcrete(Block[] stairs, Block block){
		for(int meta = 0; meta < 16; meta++) {
			Block slab = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("hbm", "concrete_" + EnumDyeColor.byMetadata(meta).getName() + "_slab"));
			Block stair = stairs[meta];
			if(slab != null){
				addRecipeAuto(new ItemStack(slab, 6, 0), "###", '#', new ItemStack(block, 1, meta));
				addRecipeAuto(new ItemStack(stair, 4), "#  ", "## ", "###", '#', new ItemStack(slab, 1, 0));
				addShapelessAuto(new ItemStack(block, 1, meta), new ItemStack(slab, 1, meta), new ItemStack(slab, 1, 0));
			}
			addRecipeAuto(new ItemStack(stair, 8), "#  ", "## ", "###", '#', new ItemStack(block, 1, meta));
			addShapelessAuto(new ItemStack(block, 3, meta), new ItemStack(stair, 1), new ItemStack(stair, 1), new ItemStack(stair, 1), new ItemStack(stair, 1));

		}
	}

	public static void addStairColorExt(Block[] stairs, Block block){
		for(int meta = 0; meta < stairs.length; meta++) {
			Block stair = stairs[meta];
			addRecipeAuto(new ItemStack(stair, 8), "#  ", "## ", "###", '#', new ItemStack(block, 1, meta));
			addShapelessAuto(new ItemStack(block, 3, meta), new ItemStack(stair, 1), new ItemStack(stair, 1), new ItemStack(stair, 1), new ItemStack(stair, 1));
		}
	}

	public static void addBillet(Item billet, Item nugget, String... ore){
		for(String o : ore)
			addRecipeAuto(new ItemStack(billet), "###", "###", '#', o );

		addBillet(billet, nugget);
	}

	public static void addBilletFragmentForODM(ItemStack billet, ItemStack nugget) {
		ResourceLocation name = new ResourceLocation(Tags.MODID, Objects.requireNonNull(billet.getItem().getRegistryName()).getPath() + "_billet_odm_" + billet.getMetadata());
		GameRegistry.addShapedRecipe(name, null, billet.copy(), "###", "###", '#', nugget);
	}

	public static void addBillet(Item billet, Item nugget){
		addRecipeAuto(new ItemStack(billet), "###", "###", '#', nugget );
		addShapelessAuto(new ItemStack(nugget, 6), billet );
	}

	public static void addBilletByIngot(Item billet, Item ingot, String... ore){
		for(String o : ore)
			addShapelessAuto(new ItemStack(billet, 3), o, o );
		addShapelessAuto(new ItemStack(billet, 3), ingot, ingot );
		addShapelessAuto(new ItemStack(ingot, 2), billet, billet, billet );
	}

	public static void addBilletByIngot(Item billet, Item ingot){
		addShapelessAuto(new ItemStack(billet, 3), ingot, ingot );
		addShapelessAuto(new ItemStack(ingot, 2), billet, billet, billet );
	}

	//Fill rods with one billet
	public static void addRodBillet(Item billet, Item out){
		addShapelessAuto(new ItemStack(out), ModItems.rod_empty, billet );
	}

	/** Fill ZIRNOX rod with two billets **/
	public static void addZIRNOXRod(Item billet, EnumZirnoxType num) {
		addShapelessAuto(new ItemStack(ModItems.rod_zirnox, 1, num.ordinal()), ModItems.rod_zirnox_empty, billet, billet );
	}

	/** Fill ZIRNOX rod with two oredict billets **/
	public static void addZIRNOXRod(DictFrame mat, EnumZirnoxType num) {
		addShapelessAuto(new ItemStack(ModItems.rod_zirnox, 1, num.ordinal()), ModItems.rod_zirnox_empty, mat.billet(), mat.billet() );
	}


	//Fill rods with two billets
	public static void addDualRodBillet(Item billet, Item out){
		addShapelessAuto(new ItemStack(out), ModItems.rod_dual_empty, billet, billet );
	}

	//Fill rods with three billets
	public static void addQuadRodBillet(Item billet, Item out){
		addShapelessAuto(new ItemStack(out), ModItems.rod_quad_empty, billet, billet, billet, billet );
	}

	//Fill rods with one billet + unload
	public static void addRodBilletUnload(Item billet, Item out){
		addShapelessAuto(new ItemStack(out), ModItems.rod_empty, billet );
		addShapelessAuto(new ItemStack(billet, 1), new ItemStack(out, 1, 0) );
	}

	//Fill rods with two billets + unload
	public static void addDualRodBilletUnload(Item billet, Item out){
		addShapelessAuto(new ItemStack(out), ModItems.rod_dual_empty, billet, billet );
		addShapelessAuto(new ItemStack(billet, 2), new ItemStack(out, 1, 0) );
	}

	//Fill rods with three billets + unload
	public static void addQuadRodBilletUnload(Item billet, Item out){
		addShapelessAuto(new ItemStack(out), ModItems.rod_quad_empty, billet, billet, billet, billet );
		addShapelessAuto(new ItemStack(billet, 4), new ItemStack(out, 1, 0) );
	}

	/** Single, dual, quad rod loading + unloading **/
	public static void addBreedingRod(Item billet, ItemBreedingRod.BreedingRodType type) {
		addBreedingRodLoad(billet, type);
		addBreedingRodUnload(billet, type);
	}
	/** Single, dual, quad rod loading + unloading + oredict **/
	public static void addBreedingRod(DictFrame mat, Item billet, ItemBreedingRod.BreedingRodType type) {
		addBreedingRodLoad(mat, billet, type);
		addBreedingRodUnload(mat, billet, type);
	}

	/** Single, dual, quad rod loading **/
	public static void addBreedingRodLoad(Item billet, ItemBreedingRod.BreedingRodType type) {
		addShapelessAuto(new ItemStack(ModItems.rod, 1, type.ordinal()), ModItems.rod_empty, billet);
		addShapelessAuto(new ItemStack(ModItems.rod_dual, 1, type.ordinal()), ModItems.rod_dual_empty, billet, billet);
		addShapelessAuto(new ItemStack(ModItems.rod_quad, 1, type.ordinal()), ModItems.rod_quad_empty, billet, billet, billet, billet);
	}
	/** Single, dual, quad rod unloading **/
	public static void addBreedingRodUnload(Item billet, ItemBreedingRod.BreedingRodType type) {
		addShapelessAuto(new ItemStack(billet, 1), new ItemStack(ModItems.rod, 1, type.ordinal()) );
		addShapelessAuto(new ItemStack(billet, 2), new ItemStack(ModItems.rod_dual, 1, type.ordinal()) );
		addShapelessAuto(new ItemStack(billet, 4), new ItemStack(ModItems.rod_quad, 1, type.ordinal()) );
	}
	/** Single, dual, quad rod loading with OreDict **/
	public static void addBreedingRodLoad(DictFrame mat, Item billet, ItemBreedingRod.BreedingRodType type) {
		addShapelessAuto(new ItemStack(ModItems.rod, 1, type.ordinal()), ModItems.rod_empty, mat.billet());
		addShapelessAuto(new ItemStack(ModItems.rod_dual, 1, type.ordinal()), ModItems.rod_dual_empty, mat.billet(), mat.billet());
		addShapelessAuto(new ItemStack(ModItems.rod_quad, 1, type.ordinal()), ModItems.rod_quad_empty, mat.billet(), mat.billet(), mat.billet(), mat.billet());
	}
	/** Single, dual, quad rod unloading with OreDict **/
	public static void addBreedingRodUnload(DictFrame mat, Item billet, ItemBreedingRod.BreedingRodType type) {
		addShapelessAuto(new ItemStack(billet, 1), new ItemStack(ModItems.rod, 1, type.ordinal()) );
		addShapelessAuto(new ItemStack(billet, 2), new ItemStack(ModItems.rod_dual, 1, type.ordinal()) );
		addShapelessAuto(new ItemStack(billet, 4), new ItemStack(ModItems.rod_quad, 1, type.ordinal()) );
	}

	//Fill rods with 6 nuggets
	public static void addRBMKRod(Item billet, Item out){
		addShapelessAuto(new ItemStack(out), ModItems.rbmk_fuel_empty, billet, billet, billet, billet, billet, billet, billet, billet );
	}

	//Bundled 1/9 recipes
	public static void add1To9Pair(Item one, Item nine){
		add1To9(new ItemStack(one), new ItemStack(nine, 9));
		add9To1(new ItemStack(nine), new ItemStack(one));
	}

	public static void add1To9Pair(Block one, Item nine){
		add1To9(new ItemStack(one), new ItemStack(nine, 9));
		add9To1(new ItemStack(nine), new ItemStack(one));
	}

	public static void add1To9PairSameMeta(Item one, Item nine, int meta){
		add1To9SameMeta(one, nine, meta);
		add9To1SameMeta(nine, one, meta);
	}

	public static void add1To9SameMeta(Item one, Item nine, int meta){
		add1To9(new ItemStack(one, 1, meta), new ItemStack(nine, 9, meta));
	}

	//Full set of nugget, ingot and block
	public static void addMineralSet(Item nugget, Item ingot, Block block){
		add1To9(new ItemStack(ingot), new ItemStack(nugget, 9));
		add9To1(new ItemStack(nugget), new ItemStack(ingot));
		add1To9(new ItemStack(block), new ItemStack(ingot, 9));
		add9To1(new ItemStack(ingot), new ItemStack(block));
	}

	public static void add9To1SameMeta(Item nine, Item one, int meta){
		add9To1(new ItemStack(nine, 1, meta), new ItemStack(one, 1, meta));
	}

	//Decompress one item into nine
	public static void add1To9(Block one, Item nine){
		add1To9(new ItemStack(one), new ItemStack(nine, 9));
	}

	public static void add1To9(Item one, Item nine){
		add1To9(new ItemStack(one), new ItemStack(nine, 9));
	}

	public static void add1To9(ItemStack one, ItemStack nine){
		addShapelessAuto(nine, one );
	}

	//Compress nine items into one
	public static void add9To1(Item nine, Block one){
		add9To1(new ItemStack(nine), new ItemStack(one));
	}

	public static void add9To1(Item nine, Item one){
		add9To1(new ItemStack(nine), new ItemStack(one));
	}

	public static void add9To1(ItemStack nine, ItemStack one){
		addRecipeAuto(one, "###", "###", "###", '#', nine );
	}

	public static void add9To1ForODM(ItemStack nine, ItemStack one){
		ResourceLocation name = new ResourceLocation(Tags.MODID, Objects.requireNonNull(one.getItem().getRegistryName()).getPath() + "_9to1_odm_" + one.getMetadata());
		GameRegistry.addShapedRecipe(name, null, one, "###", "###", "###", '#', nine );
	}

	//Fill rods with 6 nuggets
	public static void addRod(Item nugget, Item out){
		addShapelessAuto(new ItemStack(out), ModItems.rod_empty, nugget, nugget, nugget, nugget, nugget, nugget );
	}

	//Fill rods with 12 nuggets
	public static void addDualRod(Item ingot, Item nugget, Item out){
		addShapelessAuto(new ItemStack(out), ModItems.rod_dual_empty, ingot, nugget, nugget, nugget );
	}

	//Fill rods with 24 nuggets
	public static void addQuadRod(Item ingot, Item nugget, Item out){
		addShapelessAuto(new ItemStack(out), ModItems.rod_quad_empty, ingot, ingot, nugget, nugget, nugget, nugget, nugget, nugget );
	}

	public static void addSword(Item ingot, Item sword){
		addRecipeAuto(new ItemStack(sword), "I", "I", "S", 'I', ingot, 'S', Items.STICK );
	}

	public static void addPickaxe(Item ingot, Item pick){
		addRecipeAuto(new ItemStack(pick), "III", " S ", " S ", 'I', ingot, 'S', Items.STICK );
	}

	public static void addAxe(Item ingot, Item axe){
		addRecipeAuto(new ItemStack(axe), "II", "IS", " S", 'I', ingot, 'S', Items.STICK );
	}

	public static void addShovel(Item ingot, Item shovel){
		addRecipeAuto(new ItemStack(shovel), "I", "S", "S", 'I', ingot, 'S', Items.STICK );
	}

	public static void addHoe(Item ingot, Item hoe){
		addRecipeAuto(new ItemStack(hoe), "II", " S", " S", 'I', ingot, 'S', Items.STICK );
	}

	public static void addRecipeAuto(ItemStack output, Object... args){

		boolean shouldUseOD = false;
		boolean patternEnded = false;
        for (Object arg : args) {
			if (arg instanceof OreIngredient) {
				shouldUseOD = true;
				break;
			} else if (arg instanceof String) {
                if (patternEnded) {
                    shouldUseOD = true;
                    break;
                }
            } else {
                patternEnded = true;
            }
        }

		ResourceLocation loc = getRecipeName(output);
		IRecipe recipe;
		if(shouldUseOD){
			recipe = new ShapedOreRecipe(loc, output, args);
		}else {
			CraftingHelper.ShapedPrimer primer = CraftingHelper.parseShaped(args);
			recipe = new ShapedRecipes(Objects.requireNonNull(output.getItem().getRegistryName()).toString(), primer.width, primer.height, primer.input, output);
		}
		recipe.setRegistryName(loc);
		hack.getRegistry().register(recipe);
	}

	public static void addRecipeAutoOreShapeless(ItemStack output, Object... args) {
		boolean shouldUseOD = false;
		for (Object arg : args) {
			if (arg instanceof String || arg instanceof OreIngredient) {
				shouldUseOD = true;
				break;
			}
		}

		ResourceLocation loc = getRecipeName(output);
		IRecipe recipe;
		if (shouldUseOD) {
			recipe = new ShapelessOreRecipe(loc, output, args);
		} else {
			NonNullList<Ingredient> input = NonNullList.create();
			for (Object obj : args) {
				Ingredient ing = CraftingHelper.getIngredient(obj);
				if (ing == null) {
					throw new IllegalArgumentException("Invalid shapeless ingredient: " + obj);
				}
				input.add(ing);
			}
			recipe = new ShapelessRecipes(Objects.requireNonNull(output.getItem().getRegistryName()).toString(), output, input);
		}
		recipe.setRegistryName(loc);
		hack.getRegistry().register(recipe);
	}

	public static void addShapelessAuto(ItemStack output, Object... args) {

		boolean shouldUseOD = false;

        for (Object arg : args) {
            if (arg instanceof String || arg instanceof OreIngredient) {
                shouldUseOD = true;
                break;
            }
        }

		ResourceLocation loc = getRecipeName(output);
		IRecipe recipe;
		if(shouldUseOD){
			recipe = new ShapelessOreRecipe(loc, output, args);
		}else{
			recipe = new ShapelessRecipes(loc.getNamespace(), output, buildInput(args));
		}
		recipe.setRegistryName(loc);
		hack.getRegistry().register(recipe);
	}

	public static ResourceLocation getRecipeName(ItemStack output){
		ResourceLocation loc = new ResourceLocation(Tags.MODID, Objects.requireNonNull(output.getItem().getRegistryName()).getPath());
		int i = 0;
		ResourceLocation r_loc = loc;
		while(net.minecraft.item.crafting.CraftingManager.REGISTRY.containsKey(r_loc)) {
			i++;
			r_loc = new ResourceLocation(Tags.MODID, loc.getPath() + "_" + i);
		}
		return r_loc;
	}

	public static NonNullList<Ingredient> buildInput(Object[] args){
		NonNullList<Ingredient> list = NonNullList.create();
		for(Object obj : args) {
			if(obj instanceof ItemFuelRod) {
				obj = new ItemStack((Item)obj);
			}
			if(obj instanceof Ingredient) {
				list.add((Ingredient)obj);
			} else {
				Ingredient i = CraftingHelper.getIngredient(obj);
				if(i == null) {
					i = Ingredient.EMPTY;
				}
				list.add(i);
			}
		}
		return list;
	}

	public static class IngredientContainsTag extends Ingredient {

		private final ItemStack stack;

		public IngredientContainsTag(ItemStack stack){
			super(stack);
			this.stack = stack;
		}

		@Override
		public boolean apply(ItemStack p_apply_1_){
			if(p_apply_1_ == null) {
				return false;
			} else {
				return Library.areItemStacksCompatible(stack, p_apply_1_, false);
			}
		}

		@Override
		public boolean isSimple(){
			return false;
		}
	}

	public static class ComplexOreIngredient extends OreIngredient {
		public ComplexOreIngredient(String ore) {
			super(ore);
		}

		@Override
		public boolean isSimple() {
			//mlbv: with isSimple() == true it fucking ignores apply()
			return false;
		}
	}
}
