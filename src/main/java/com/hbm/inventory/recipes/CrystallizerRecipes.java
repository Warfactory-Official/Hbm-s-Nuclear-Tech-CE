package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemChemicalDye;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.items.machine.ItemScraps;
import com.hbm.items.special.ItemBedrockOreNew;
import com.hbm.util.Tuple;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import static com.hbm.inventory.OreDictManager.*;

//This time we're doing this right
//...right?

//yes this time i will

//.. hold my beer
public class CrystallizerRecipes extends SerializableRecipe {

	//'Object' is either a ComparableStack or the key for the ore dict
	private static HashMap<Tuple.Pair<Object, FluidType>, CrystallizerRecipe> recipes = new HashMap();
	private static HashMap<Object, Integer> amounts = new HashMap(); // for use in the partitioner
	private static List<CrystallizerRecipe> jeiCrystalRecipes = null;
	@Override
	public void registerDefaults() {
		final int baseTime = 600;
		final int utilityTime = 100;
		final int mixingTime = 20;
		FluidStack sulfur = new FluidStack(Fluids.SULFURIC_ACID, 500);

		registerRecipe(COAL.ore(),		new CrystallizerRecipe(ModItems.crystal_coal, baseTime).prod(0.05F));
		registerRecipe(IRON.ore(),		new CrystallizerRecipe(ModItems.crystal_iron, baseTime).prod(0.05F));
		registerRecipe(GOLD.ore(),		new CrystallizerRecipe(ModItems.crystal_gold, baseTime).prod(0.05F));
		registerRecipe(REDSTONE.ore(),	new CrystallizerRecipe(ModItems.crystal_redstone, baseTime).prod(0.05F));
		registerRecipe(LAPIS.ore(),		new CrystallizerRecipe(ModItems.crystal_lapis, baseTime).prod(0.05F));
		registerRecipe(DIAMOND.ore(),	new CrystallizerRecipe(ModItems.crystal_diamond, baseTime).prod(0.05F));
		registerRecipe(U.ore(),			new CrystallizerRecipe(ModItems.crystal_uranium, baseTime).prod(0.05F), sulfur);
		for(String ore : OreDictManager.TH232.all(MaterialShapes.ONLY_ORE)) registerRecipe(ore,	new CrystallizerRecipe(ModItems.crystal_thorium, baseTime).prod(0.05F), sulfur);
		registerRecipe(PU.ore(),		new CrystallizerRecipe(ModItems.crystal_plutonium, baseTime).prod(0.05F), sulfur);
		registerRecipe(TI.ore(),		new CrystallizerRecipe(ModItems.crystal_titanium, baseTime).prod(0.05F), sulfur);
		registerRecipe(S.ore(),			new CrystallizerRecipe(ModItems.crystal_sulfur, baseTime).prod(0.05F));
		registerRecipe(KNO.ore(),		new CrystallizerRecipe(ModItems.crystal_niter, baseTime).prod(0.05F));
		registerRecipe(CU.ore(),		new CrystallizerRecipe(ModItems.crystal_copper, baseTime).prod(0.05F));
		registerRecipe(W.ore(),			new CrystallizerRecipe(ModItems.crystal_tungsten, baseTime).prod(0.05F), sulfur);
		registerRecipe(AL.ore(),		new CrystallizerRecipe(ModItems.crystal_aluminium, baseTime).prod(0.05F));
		registerRecipe(F.ore(),			new CrystallizerRecipe(ModItems.crystal_fluorite, baseTime).prod(0.05F));
		registerRecipe(BE.ore(),		new CrystallizerRecipe(ModItems.crystal_beryllium, baseTime).prod(0.05F));
		registerRecipe(PB.ore(),		new CrystallizerRecipe(ModItems.crystal_lead, baseTime).prod(0.05F));
		registerRecipe(SA326.ore(),		new CrystallizerRecipe(ModItems.crystal_schrabidium, baseTime).prod(0.05F), sulfur);
		registerRecipe(LI.ore(),		new CrystallizerRecipe(ModItems.crystal_lithium, baseTime).prod(0.05F), sulfur);
		registerRecipe(CO.ore(),		new CrystallizerRecipe(ModItems.crystal_cobalt, baseTime).prod(0.05F), sulfur);

		registerRecipe(new ComparableStack(ModItems.powder_calcium),	new CrystallizerRecipe(new ItemStack(ModItems.powder_cement, 8), utilityTime).prod(0.1F), new FluidStack(Fluids.REDMUD, 75));
		registerRecipe(MALACHITE.ingot(), new CrystallizerRecipe(ItemScraps.create(new Mats.MaterialStack(Mats.MAT_COPPER, MaterialShapes.INGOT.q(1))), 300).prod(0.1F), new FluidStack(Fluids.SULFURIC_ACID, 250));

		registerRecipe("oreRareEarth",	new CrystallizerRecipe(ModItems.crystal_rare, baseTime).prod(0.05F), sulfur);
		registerRecipe("oreCinnabar",	new CrystallizerRecipe(ModItems.crystal_cinnabar, baseTime).prod(0.05F));

		registerRecipe(new ComparableStack(ModBlocks.ore_nether_fire),	new CrystallizerRecipe(ModItems.crystal_phosphorus, baseTime).prod(0.05F));
		registerRecipe(new ComparableStack(ModBlocks.ore_tikite),		new CrystallizerRecipe(ModItems.crystal_trixite, baseTime).prod(0.05F), sulfur);
		registerRecipe(new ComparableStack(ModBlocks.gravel_diamond),	new CrystallizerRecipe(ModItems.crystal_diamond, baseTime).prod(0.05F));
		registerRecipe(SRN.ingot(),										new CrystallizerRecipe(ModItems.crystal_schraranium, baseTime).prod(0.05F));

		registerRecipe(KEY_SAND,			new CrystallizerRecipe(ModItems.ingot_fiberglass, utilityTime).prod(0.15F));
		registerRecipe(SI.ingot(),			new CrystallizerRecipe(new ItemStack(Items.QUARTZ, 2), utilityTime).prod(0.1F), new FluidStack(Fluids.OXYGEN, 250));
		registerRecipe(REDSTONE.block(),	new CrystallizerRecipe(new ItemStack(ModItems.ingot_mercury, 9), baseTime).prod(0.25F));
		registerRecipe(CINNABAR.crystal(),	new CrystallizerRecipe(new ItemStack(ModItems.ingot_mercury, 27), baseTime).prod(0.25F));
		registerRecipe(BORAX.dust(),		new CrystallizerRecipe(new ItemStack(ModItems.powder_boron_tiny, 3), baseTime).prod(0.25F), sulfur);
		registerRecipe(COAL.block(),		new CrystallizerRecipe(ModBlocks.block_graphite, baseTime));

		registerRecipe(new ComparableStack(Blocks.COBBLESTONE),			new CrystallizerRecipe(ModBlocks.reinforced_stone, utilityTime));
		registerRecipe(new ComparableStack(ModBlocks.gravel_obsidian),	new CrystallizerRecipe(ModBlocks.brick_obsidian, utilityTime));
		registerRecipe(new ComparableStack(Items.ROTTEN_FLESH),			new CrystallizerRecipe(Items.LEATHER, utilityTime).prod(0.25F));
		registerRecipe(new ComparableStack(ModItems.coal_infernal),		new CrystallizerRecipe(ModItems.solid_fuel, utilityTime));
		registerRecipe(new ComparableStack(ModBlocks.stone_gneiss),		new CrystallizerRecipe(ModItems.powder_lithium, utilityTime).prod(0.25F));
		registerRecipe(new ComparableStack(Items.DYE, 1, 15),			new CrystallizerRecipe(new ItemStack(Items.SLIME_BALL, 4), mixingTime), new FluidStack(Fluids.SULFURIC_ACID, 250));
		registerRecipe(new ComparableStack(Items.BONE),					new CrystallizerRecipe(new ItemStack(Items.SLIME_BALL, 16), mixingTime), new FluidStack(Fluids.SULFURIC_ACID, 1_000));
		registerRecipe(new ComparableStack(DictFrame.fromOne(ModItems.plant_item, ItemEnums.EnumPlantType.MUSTARDWILLOW)), new CrystallizerRecipe(new ItemStack(ModItems.powder_cadmium), 100).setReq(10), new FluidStack(Fluids.RADIOSOLVENT, 250));
		registerRecipe(new ComparableStack(ModItems.scrap_oil),			new CrystallizerRecipe(new ItemStack(ModItems.nugget_arsenic), 100).prod(0.3F).setReq(16), new FluidStack(Fluids.RADIOSOLVENT, 100));
		registerRecipe(new ComparableStack(DictFrame.fromOne(ModItems.powder_ash, ItemEnums.EnumAshType.FULLERENE)), new CrystallizerRecipe(new ItemStack(ModItems.ingot_cft), baseTime).prod(0.1F).setReq(4), new FluidStack(Fluids.XYLENE, 1_000));

		registerRecipe(DIAMOND.dust(), 									new CrystallizerRecipe(Items.DIAMOND, utilityTime));
		registerRecipe(EMERALD.dust(), 									new CrystallizerRecipe(Items.EMERALD, utilityTime));
		registerRecipe(LAPIS.dust(),									new CrystallizerRecipe(new ItemStack(Items.DYE, 1, 4), utilityTime));
		registerRecipe(new ComparableStack(ModItems.powder_semtex_mix),	new CrystallizerRecipe(ModItems.ingot_semtex, baseTime));
		registerRecipe(new ComparableStack(ModItems.powder_desh_ready),	new CrystallizerRecipe(ModItems.ingot_desh, baseTime));
		registerRecipe(new ComparableStack(ModItems.powder_meteorite),	new CrystallizerRecipe(ModItems.fragment_meteorite, utilityTime));
		registerRecipe(CD.dust(),										new CrystallizerRecipe(ModItems.ingot_rubber, utilityTime), new FluidStack(Fluids.FISHOIL, 250));
		registerRecipe(LATEX.ingot(),									new CrystallizerRecipe(ModItems.ingot_rubber, mixingTime).prod(0.15F), new FluidStack(Fluids.SOURGAS, 25));
		registerRecipe(new ComparableStack(ModItems.powder_sawdust),	new CrystallizerRecipe(ModItems.cordite, mixingTime).prod(0.25F), new FluidStack(Fluids.NITROGLYCERIN, 250));

		registerRecipe(new ComparableStack(ModItems.meteorite_sword_treated),	new CrystallizerRecipe(ModItems.meteorite_sword_etched, baseTime));
		registerRecipe(new ComparableStack(ModItems.powder_impure_osmiridium),	new CrystallizerRecipe(ModItems.crystal_osmiridium, baseTime), new FluidStack(Fluids.SCHRABIDIC, 1_000));
		// old bedrock ores recipes are removed because why the fuck do we need them?...

		int bedrock = 200;
		int washing = 100;
		for(ItemBedrockOreNew.BedrockOreType type : ItemBedrockOreNew.BedrockOreType.VALUES) {
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.BASE, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.BASE_WASHED, type), washing), new FluidStack(Fluids.WATER, 250));
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.BASE_ROASTED, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.BASE_WASHED, type), washing), new FluidStack(Fluids.WATER, 250));

			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SULFURIC, type), bedrock), new FluidStack(Fluids.SULFURIC_ACID, 250));
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_ROASTED, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SULFURIC, type), bedrock), new FluidStack(Fluids.SULFURIC_ACID, 250));

			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SOLVENT, type), bedrock), new FluidStack(Fluids.SOLVENT, 250));
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_ROASTED, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SOLVENT, type), bedrock), new FluidStack(Fluids.SOLVENT, 250));
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_NOSULFURIC, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SOLVENT, type), bedrock), new FluidStack(Fluids.SOLVENT, 250));

			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_RAD, type), bedrock), new FluidStack(Fluids.RADIOSOLVENT, 250));
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_ROASTED, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_RAD, type), bedrock), new FluidStack(Fluids.RADIOSOLVENT, 250));
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_NOSULFURIC, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_RAD, type), bedrock), new FluidStack(Fluids.RADIOSOLVENT, 250));
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_NOSOLVENT, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_RAD, type), bedrock), new FluidStack(Fluids.RADIOSOLVENT, 250));

			int sulf = 4;
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SULFURIC_BYPRODUCT, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SULFURIC_WASHED, type), washing).setReq(sulf), new FluidStack(Fluids.WATER, 250));
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SULFURIC_ROASTED, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SULFURIC_WASHED, type), washing).setReq(sulf), new FluidStack(Fluids.WATER, 250));
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SULFURIC_ARC, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SULFURIC_WASHED, type), washing).setReq(sulf), new FluidStack(Fluids.WATER, 250));

			int solv = 4;
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SOLVENT_BYPRODUCT, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SOLVENT_WASHED, type), washing).setReq(solv), new FluidStack(Fluids.WATER, 250));
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SOLVENT_ROASTED, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SOLVENT_WASHED, type), washing).setReq(solv), new FluidStack(Fluids.WATER, 250));
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SOLVENT_ARC, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SOLVENT_WASHED, type), washing).setReq(solv), new FluidStack(Fluids.WATER, 250));

			int rad = 4;
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.RAD_BYPRODUCT, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.RAD_WASHED, type), washing).setReq(rad), new FluidStack(Fluids.WATER, 250));
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.RAD_ROASTED, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.RAD_WASHED, type), washing).setReq(rad), new FluidStack(Fluids.WATER, 250));
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.RAD_ARC, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.RAD_WASHED, type), washing).setReq(rad), new FluidStack(Fluids.WATER, 250));

			FluidStack primary = new FluidStack(Fluids.HYDROGEN, 250);
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_FIRST, type), bedrock), primary);
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_ROASTED, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_FIRST, type), bedrock), primary);
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SULFURIC, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_FIRST, type), bedrock), primary);
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_NOSULFURIC, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_FIRST, type), bedrock), primary);
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SOLVENT, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_FIRST, type), bedrock), primary);
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_NOSOLVENT, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_FIRST, type), bedrock), primary);
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_RAD, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_FIRST, type), bedrock), primary);
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_NORAD, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_FIRST, type), bedrock), primary);

			FluidStack secondary = new FluidStack(Fluids.CHLORINE, 250);
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SECOND, type), bedrock), secondary);
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_ROASTED, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SECOND, type), bedrock), secondary);
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SULFURIC, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SECOND, type), bedrock), secondary);
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_NOSULFURIC, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SECOND, type), bedrock), secondary);
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SOLVENT, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SECOND, type), bedrock), secondary);
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_NOSOLVENT, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SECOND, type), bedrock), secondary);
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_RAD, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SECOND, type), bedrock), secondary);
			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_NORAD, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SECOND, type), bedrock), secondary);

			registerRecipe(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.CRUMBS, type)), new CrystallizerRecipe(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.BASE, type), bedrock).setReq(64), new FluidStack(Fluids.NITRIC_ACID, 1000));
		}

		FluidStack[] dyes = new FluidStack[] {new FluidStack(Fluids.WOODOIL, 100), new FluidStack(Fluids.FISHOIL, 100), new FluidStack(Fluids.LIGHTOIL, 100)};
		for(FluidStack dye : dyes) {
			registerRecipe(COAL.dust(),		new CrystallizerRecipe(DictFrame.fromOne(ModItems.chemical_dye, ItemChemicalDye.EnumChemDye.BLACK, 4), mixingTime).prod(0.15F), dye);
			registerRecipe(TI.dust(),		new CrystallizerRecipe(DictFrame.fromOne(ModItems.chemical_dye, ItemChemicalDye.EnumChemDye.WHITE, 4), mixingTime).prod(0.15F), dye);
			registerRecipe(IRON.dust(),		new CrystallizerRecipe(DictFrame.fromOne(ModItems.chemical_dye, ItemChemicalDye.EnumChemDye.RED, 4), mixingTime).prod(0.15F), dye);
			registerRecipe(W.dust(),		new CrystallizerRecipe(DictFrame.fromOne(ModItems.chemical_dye, ItemChemicalDye.EnumChemDye.YELLOW, 4), mixingTime).prod(0.15F), dye);
			registerRecipe(CU.dust(),		new CrystallizerRecipe(DictFrame.fromOne(ModItems.chemical_dye, ItemChemicalDye.EnumChemDye.GREEN, 4), mixingTime).prod(0.15F), dye);
			registerRecipe(CO.dust(),		new CrystallizerRecipe(DictFrame.fromOne(ModItems.chemical_dye, ItemChemicalDye.EnumChemDye.BLUE, 4), mixingTime).prod(0.15F), dye);
		}

		registerRecipe(new ComparableStack(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.CRUDE)),		new CrystallizerRecipe(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.WAX), 20),	new FluidStack(Fluids.CHLORINE, 250));
		registerRecipe(new ComparableStack(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.CRACK)),		new CrystallizerRecipe(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.WAX), 20),	new FluidStack(Fluids.CHLORINE, 100));
		registerRecipe(new ComparableStack(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.PARAFFIN)),	new CrystallizerRecipe(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.WAX), 20),	new FluidStack(Fluids.CHLORINE, 100));
		registerRecipe(new ComparableStack(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.WAX)), 		new CrystallizerRecipe(new ItemStack(ModItems.pellet_charged), 200), 				new FluidStack(Fluids.IONGEL, 500));

		registerRecipe(KEY_SAND, new CrystallizerRecipe(Blocks.CLAY, 20), new FluidStack(Fluids.COLLOID, 1_000));
		registerRecipe(new ComparableStack(ModBlocks.sand_quartz), new CrystallizerRecipe(new ItemStack(ModItems.ball_dynamite, 16), 20), new FluidStack(Fluids.NITROGLYCERIN, 1_000));
		registerRecipe(NETHERQUARTZ.dust(), new CrystallizerRecipe(new ItemStack(ModItems.ball_dynamite, 4), 20), new FluidStack(Fluids.NITROGLYCERIN, 250));

		/// COMPAT CERTUS QUARTZ ///
		List<ItemStack> quartz = OreDictionary.getOres("crystalCertusQuartz");
		if(quartz != null && !quartz.isEmpty()) {
			ItemStack qItem = quartz.get(0).copy();
			qItem.setCount(12);
			registerRecipe("oreCertusQuartz", new CrystallizerRecipe(qItem, baseTime));
		}

		/// COMPAT WHITE PHOSPHORUS DUST ///
		List<ItemStack> dustWhitePhosphorus = OreDictionary.getOres(P_WHITE.dust());
		if(dustWhitePhosphorus != null && !dustWhitePhosphorus.isEmpty()) {
			registerRecipe(P_WHITE.dust(), new CrystallizerRecipe(new ItemStack(ModItems.ingot_phosphorus), utilityTime), new FluidStack(Fluids.AROMATICS, 50));
		}
	}

	public static CrystallizerRecipe getOutput(ItemStack stack, FluidType type) {

		if(stack == null || stack.isEmpty())
			return null;

		ComparableStack comp = new ComparableStack(stack.getItem(), 1, stack.getItemDamage());
		Tuple.Pair compKey = new Tuple.Pair(comp, type);

		if(recipes.containsKey(compKey)) return recipes.get(compKey);

		String[] dictKeys = comp.getDictKeys();

		for(String key : dictKeys) {
			Tuple.Pair dictKey = new Tuple.Pair(key, type);
			if(recipes.containsKey(dictKey)) return recipes.get(dictKey);
		}

		comp.meta = OreDictionary.WILDCARD_VALUE;
		if(recipes.containsKey(compKey)) return recipes.get(compKey);

		return null;
	}

	public static int getAmount(ItemStack stack) {

		if(stack == null || stack.isEmpty() || stack.getItem() == null)
			return 0;

		ComparableStack comp = new ComparableStack(stack.getItem(), 1, stack.getItemDamage());
		if(amounts.containsKey(comp)) return amounts.get(comp);

		String[] dictKeys = comp.getDictKeys();

		for(String key : dictKeys) {
			if(amounts.containsKey(key)) return amounts.get(key);
		}

		comp.meta = OreDictionary.WILDCARD_VALUE;
		if(amounts.containsKey(comp)) return amounts.get(comp);

		return 0;
	}

	public static HashMap<Object, Object> getRecipes() {

		HashMap<Object, Object> recipes = new HashMap<>();

		for(Entry<Tuple.Pair<Object, FluidType>, CrystallizerRecipe> entry : CrystallizerRecipes.recipes.entrySet()) {

			CrystallizerRecipe recipe = entry.getValue();

			Tuple.Pair<Object, FluidType> key = entry.getKey();
			Object input = key.getKey();
			FluidType acid = key.getValue();

			if(input instanceof String) {
				RecipesCommon.OreDictStack stack = new RecipesCommon.OreDictStack((String) input, recipe.itemAmount);
				recipes.put(new Object[] {ItemFluidIcon.make(acid, recipe.acidAmount), stack}, recipe.output);
			} else {
				ComparableStack stack = ((ComparableStack) input);
				stack = (ComparableStack) stack.copy();
				stack.stacksize = recipe.itemAmount;
				//if(stack.item == ModItems.scrap_plastic) continue;
				recipes.put(new Object[] {ItemFluidIcon.make(acid, recipe.acidAmount), stack}, recipe.output);
			}
		}

		return recipes;
	}

	public static void registerRecipe(Object input, CrystallizerRecipe recipe) {
		registerRecipe(input, recipe, new FluidStack(Fluids.PEROXIDE, 500));
	}

	public static void registerRecipe(Object input, CrystallizerRecipe recipe, FluidStack stack) {
		recipe.acidAmount = stack.fill;
		recipes.put(new Tuple.Pair(input, stack.type), recipe);
		amounts.put(input, recipe.itemAmount);
	}

	public static class CrystallizerRecipe {
		public int acidAmount;
		public int itemAmount = 1;
		public int duration;
		public float productivity = 0F;
		public ItemStack output;

		public CrystallizerRecipe(Block output, int duration) { this(new ItemStack(output), duration); }
		public CrystallizerRecipe(Item output, int duration) { this(new ItemStack(output), duration); }

		public CrystallizerRecipe setReq(int amount) {
			this.itemAmount = amount;
			return this;
		}

		public CrystallizerRecipe(ItemStack output, int duration) {
			this.output = output;
			this.duration = duration;
			this.acidAmount = 500;
		}

		public CrystallizerRecipe prod(float productivity) {
			this.productivity = productivity;
			return this;
		}
	}

	@Override
	public String getFileName() {
		return "hbmCrystallizer.json";
	}

	@Override
	public Object getRecipeObject() {
		return recipes;
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject obj = (JsonObject) recipe;

		ItemStack output = this.readItemStack(obj.get("output").getAsJsonArray());
		RecipesCommon.AStack input = this.readAStack(obj.get("input").getAsJsonArray());
		FluidStack fluid = this.readFluidStack(obj.get("fluid").getAsJsonArray());
		int duration = obj.get("duration").getAsInt();

		CrystallizerRecipe cRecipe = new CrystallizerRecipe(output, duration).setReq(input.stacksize);
		input.stacksize = 1;
		cRecipe.acidAmount = fluid.fill;
		if(input instanceof ComparableStack) {
			recipes.put(new Tuple.Pair(((ComparableStack) input), fluid.type), cRecipe);
		} else if(input instanceof RecipesCommon.OreDictStack) {
			recipes.put(new Tuple.Pair(((RecipesCommon.OreDictStack) input).name, fluid.type), cRecipe);
		}
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		Entry<Tuple.Pair, CrystallizerRecipe> rec = (Entry<Tuple.Pair, CrystallizerRecipe>) recipe;
		CrystallizerRecipe cRecipe = rec.getValue();
		Tuple.Pair<Object, FluidType> pair = rec.getKey();
		RecipesCommon.AStack input = pair.getKey() instanceof String ? new RecipesCommon.OreDictStack((String )pair.getKey()) : ((ComparableStack) pair.getKey()).copy();
		input.stacksize = cRecipe.itemAmount;
		FluidStack fluid = new FluidStack(pair.getValue(), cRecipe.acidAmount);

		writer.name("duration").value(cRecipe.duration);
		writer.name("fluid");
		this.writeFluidStack(fluid, writer);
		writer.name("input");
		this.writeAStack(input, writer);
		writer.name("output");
		this.writeItemStack(cRecipe.output, writer);
	}

	@Override
	public void deleteRecipes() {
		recipes.clear();
		amounts.clear();
	}

	@Override
	public String getComment() {
		return "The acidizer also supports stack size requirements for input items, eg. the cadmium recipe requires 10 willow leaves.";
	}
}