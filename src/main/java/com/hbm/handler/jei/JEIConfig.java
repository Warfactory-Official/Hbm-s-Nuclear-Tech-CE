package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.jei.transfer.ExposureChamberTransferInfo;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.container.ContainerFurnaceCombo;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.gui.*;
import com.hbm.inventory.recipes.CrucibleRecipes;
import com.hbm.inventory.recipes.DFCRecipes;
import com.hbm.items.EffectItem;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.items.machine.ItemFluidIDMulti;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.factory.GunFactory;
import com.hbm.main.MainRegistry;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@JEIPlugin
public class JEIConfig implements IModPlugin {

    public static final String AMMO_PRESS = "hbm.ammo_press";
    public static final String ALLOY = "hbm.alloy";
    public static final String ANVIL_CON = "hbm.anvil_construction";
    public static final String ANVIL_SMITH = "hbm.anvil_smithing";
    public static final String ARC_FURNACE_FLUID = "hbm.arc_furnace_fluid";
    public static final String ARC_FURNACE_SOLID = "hbm.arc_furnace_solid";
    public static final String ARC_WELDER = "hbm.arc_welder";
    public static final String ASHPIT = "hbm.ashpit";
    public static final String ASSEMBLY = "hbm.assembly";
    public static final String ASSEMBLY_MACHINE = "hbm.assembly_machine";
    public static final String BOILER = "hbm.boiler";
    public static final String BOOK = "hbm.book_of";
    public static final String BREEDER = "hbm.breeder";
    public static final String CENTRIFUGE = "hbm.centrifuge";
    public static final String CHEMICAL_PLANT = "hbm.chemical_plant";
    public static final String CMB = "hbm.cmb_furnace";
    public static final String COKER = "hbm.coker";
    public static final String CONSTRUCTION = "hbm.construction";
    public static final String COMPRESSING = "hbm.compressor";
    public static final String CRACKING = "hbm.cracking";
    public static final String CRUCIBLE_ALLOY = "hbm.crucible_alloy";
    public static final String CRUCIBLE_CAST = "hbm.crucible_foundry";
    public static final String CRUCIBLE_SMELT = "hbm.crucible_smelt";
    public static final String CRYSTALLIZER = "hbm.crystallizer";
    public static final String CYCLOTRON = "hbm.cyclotron";
    public static final String DEUTERIUM = "hbm.deuterium";
    public static final String DFC = "hbm.dfc";
    public static final String ELECTROLYSIS_FLUID = "hbm.electrolysis_fluid";
    public static final String ELECTROLYSIS_METAL = "hbm.electrolysis_metal";
    public static final String FLUIDS = "hbm.fluids";
    public static final String FRACTIONING = "hbm.fracturing";
    public static final String FUSION_BYPRODUCT = "hbm.fusionbyproduct";
    public static final String FUSION_BREEDER = "hbm.fusionbreeder";
    public static final String GAS_CENT = "hbm.gas_centrifuge";
    public static final String HADRON = "hbm.hadron";
    public static final String HYDROTREATING = "hbm.hydrotreating";
    public static final String LIQUEFACTION = "hbm.liquefaction";
    public static final String MIXER = "hbm.mixer";
    public static final String PYROLYSIS = "hbm.pyrolysis";
    public static final String PRESS = "hbm.press";
    public static final String RBMKFUEL = "hbm.rbmkfueluncrafting";
    public static final String RBMKOUTGASSER = "hbm.rbmk_outgasser";
    public static final String REFINERY = "hbm.refinery";
    public static final String REFORMING = "hbm.reforming";
    public static final String ROTARY_FURNACE = "hbm.rotary_furnace";
    public static final String SAWMILL = "hbm.sawmill";
    public static final String SHREDDER = "hbm.shredder";
    public static final String SILEX = "hbm.silex";
    public static final String SILEX_DIGAMMA = "hbm.silexdigamma";
    public static final String SILEX_GAMMA = "hbm.silexgamma";
    public static final String SILEX_IR = "hbm.silexir";
    public static final String SILEX_UV = "hbm.silexuv";
    public static final String SILEX_VISIBLE = "hbm.silexvisible";
    public static final String SOLDERING_STATION = "hbm.soldering_station";
    public static final String SOLIDIFICATION = "hbm.solidification";
    public static final String STORAGEDRUM = "hbm.storage_drum";
    public static final String TRANSMUTATION = "hbm.transmutation";
    public static final String WASTEDRUM = "hbm.waste_drum";
    static final String ORE_SLOPPER = "hbm.ore_slopper";
    static final String PA = "hbm.particle_accelerator";
    public static final String EXPOSURE = "hbm.exposure_chamber";
    public static final String RADIOLYSIS = "hbm.radiolysis";
    public static final String COMBINATION = "hbm.combination";
    public static final String RTG = "hbm.rtg";
    public static final String VACUUM = "hbm.vacuum";
    public static final String ZIRNOX = "hbm.zirnox";
    static final String PUREX = "hbm.purex";
    private AmmoPressHandler ammoPressHandler;
    private AnvilRecipeHandler anvilRecipeHandler;
    private AnvilSmithingRecipeHandler anvilSmithingRecipeHandler;
    private ArcFurnaceFluidHandler arcFurnaceFluidHandler;
    private ArcFurnaceSolidHandler arcFurnaceSolidHandler;
    private ArcWelderRecipeHandler arcWelderRecipeHandler;
    private AssemblyMachineRecipeHandler assemblyMachineRecipeHandler;
    private AshpitHandler ashpitHandler;
    private BoilingHandler boilingHandler;
    private CentrifugeRecipeHandler centrifugeRecipeHandler;
    private ChemicalPlantRecipeHandler chemicalPlantRecipeHandler;
    private CokingRecipeHandler cokingHandler;
    private CompressingRecipeHandler compressingHandler;
    private ConstructionHandler constructionHandler;
    private CrackingHandler crackingHandler;
    private CrucibleAlloyingHandler crucibleAlloyingHandler;
    private CrucibleCastingHandler crucibleCastingHandler;
    private CrucibleSmeltingHandler crucibleSmeltingHandler;
    private CrystallizerRecipeHandler crystallizerHandler;
    private DeuteriumHandler deuteriumHandler;
    private ElectrolyserFluidHandler electrolyserFluidHandler;
    private ElectrolyserMetalHandler electrolyserMetalHandler;
    private FractioningRecipeHandler fractioningHandler;
    private FusionBreederRecipeHandler fusionBreederRecipeHandler;
    private FusionRecipeHandler fusionRecipeHandler;
    private FuelPoolHandler fuelPoolHandler;
    private HydrotreatingHandler hydrotreatHandler;
    private LiquefactionHandler liquefactHandler;
    private MixerRecipeHandler mixerHandler;
    private PyroHandler pyroHandler;
    private RBMKOutgasserRecipeHandler outgasserHandler;
    private ReformingHandler reformingHandler;
    private RotaryFurnaceRecipeHandler rotaryFurnaceRecipeHandler;
    private RTGRecipeHandler rtgRecipeHandler;
    private SolderingStationRecipeHandler solderingStationHandler;
    private SolidificationHandler solidificationHandler;
    private OreSlopperHandler oreSlopperHandler;
    private ParticleAcceleratorHandler particleAcceleratorHandler;
    private ExposureChamberHandler exposureChamberHandler;
    private CombinationHandler combinationHandler;
    private SawmillHandler sawmillHandler;
    private ShredderRecipeHandler shredderHandler;
    private VacuumRecipeHandler vacuumHandler;
    private ZirnoxRecipeHandler zirnoxHandler;
    private PUREXRecipeHandler purexHandler;

    @Override
    public void register(@NotNull IModRegistry registry) {
        if (!GeneralConfig.jei)
            return;

        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_electric_furnace_off), VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.furnace_iron), VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.furnace_steel), VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_microwave), VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_rtg_furnace_off), VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.crate_tungsten), VanillaRecipeCategoryUid.SMELTING);

        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_ammo_press), AMMO_PRESS);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_ashpit), ASHPIT);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_coker), COKER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_compressor), COMPRESSING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_deuterium_extractor), DEUTERIUM);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_assembly_machine), ASSEMBLY_MACHINE);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_assembly_factory), ASSEMBLY_MACHINE);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_arc_furnace), ARC_FURNACE_FLUID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_arc_furnace), ARC_FURNACE_SOLID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_chemical_plant), CHEMICAL_PLANT);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_chemical_factory), CHEMICAL_PLANT);
        registry.addRecipeCatalyst(new ItemStack(ModItems.boltgun), CONSTRUCTION);
        registry.addRecipeCatalyst(new ItemStack(ModItems.blowtorch), CONSTRUCTION);
        registry.addRecipeCatalyst(new ItemStack(ModItems.acetylene_torch), CONSTRUCTION);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_crucible), CRUCIBLE_ALLOY);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.foundry_basin), CRUCIBLE_CAST);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.foundry_mold), CRUCIBLE_CAST);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_strand_caster), CRUCIBLE_CAST);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_crucible), CRUCIBLE_SMELT);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_cyclotron), CYCLOTRON);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_press), PRESS);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_epress), PRESS);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_conveyor_press), PRESS);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_difurnace_off), ALLOY);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_difurnace_rtg_off), ALLOY);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_sawmill), SAWMILL);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_boiler), BOILER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_solar_boiler), BOILER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_industrial_boiler), BOILER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.rbmk_heater), BOILER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_reactor_breeding), BREEDER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_centrifuge), CENTRIFUGE);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_gascent), GAS_CENT);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_waste_drum), WASTEDRUM);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_storage_drum), STORAGEDRUM);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_refinery), REFINERY);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_catalytic_cracker), CRACKING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_fraction_tower), FRACTIONING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_electrolyser), ELECTROLYSIS_FLUID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_electrolyser), ELECTROLYSIS_METAL);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_hydrotreater), HYDROTREATING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_liquefactor), LIQUEFACTION);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_catalytic_reformer), REFORMING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_solidifier), SOLIDIFICATION);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_mixer), MIXER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_shredder), SHREDDER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_fluidtank), FLUIDS);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_crystallizer), CRYSTALLIZER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_soldering_station), SOLDERING_STATION);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_arc_welder), ARC_WELDER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_rotary_furnace), ROTARY_FURNACE);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_pyrooven), PYROLYSIS);
        //This recipe catalyst doesn't work, since the book of is blacklisted.
        registry.addRecipeCatalyst(new ItemStack(ModItems.book_of_), BOOK);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.fusion_torus), FUSION_BYPRODUCT);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.fusion_breeder), FUSION_BREEDER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.hadron_core), HADRON);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_silex), SILEX);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_rtg_grey), RTG);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_difurnace_rtg_off), RTG);
        registry.addRecipeCatalyst(new ItemStack(ModItems.laser_crystal_co2), SILEX_IR);
        registry.addRecipeCatalyst(new ItemStack(ModItems.laser_crystal_bismuth), SILEX_VISIBLE);
        registry.addRecipeCatalyst(new ItemStack(ModItems.laser_crystal_cmb), SILEX_UV);
        registry.addRecipeCatalyst(new ItemStack(ModItems.laser_crystal_bale), SILEX_GAMMA);
        registry.addRecipeCatalyst(new ItemStack(ModItems.laser_crystal_digamma), SILEX_DIGAMMA);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_vacuum_distill), VACUUM);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.reactor_zirnox), ZIRNOX);
        AnvilRecipeHandler.addAnvilCatalysts(registry, ANVIL_CON);
        AnvilRecipeHandler.addAnvilCatalysts(registry, ANVIL_SMITH);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.rbmk_outgasser), RBMKOUTGASSER);
        registry.addRecipeCatalyst(new ItemStack(Objects.requireNonNull(Blocks.CRAFTING_TABLE)), RBMKFUEL);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.crate_tungsten), DFC);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_ore_slopper), ORE_SLOPPER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.pa_detector), PA);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.pa_source), PA);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_exposure_chamber), EXPOSURE);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_radiolysis), RADIOLYSIS);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.furnace_combination), COMBINATION);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_purex), PUREX);

        registry.addRecipes(assemblyMachineRecipeHandler.getRecipes(), ASSEMBLY_MACHINE);
        registry.addRecipes(JeiRecipes.getCyclotronRecipes(), CYCLOTRON);
        registry.addRecipes(JeiRecipes.getTransmutationRecipes(), TRANSMUTATION);
        registry.addRecipes(PressRecipeHandler.getRecipes(), PRESS);
        registry.addRecipes(JeiRecipes.getAlloyRecipes(), ALLOY);
        registry.addRecipes(JeiRecipes.getCMBRecipes(), CMB);
        registry.addRecipes(JeiRecipes.getGasCentrifugeRecipes(), GAS_CENT);
        registry.addRecipes(fuelPoolHandler.getRecipes(), WASTEDRUM);
        registry.addRecipes(JeiRecipes.getStorageDrumRecipes(), STORAGEDRUM);
        registry.addRecipes(JeiRecipes.getRefineryRecipe(), REFINERY);
        registry.addRecipes(anvilRecipeHandler.getRecipes(), ANVIL_CON);
        registry.addRecipes(anvilSmithingRecipeHandler.getRecipes(), ANVIL_SMITH);
        registry.addRecipes(centrifugeRecipeHandler.getRecipes(), CENTRIFUGE);
        registry.addRecipes(ammoPressHandler.getRecipes(), AMMO_PRESS);
        registry.addRecipes(ashpitHandler.getRecipes(), ASHPIT);
        registry.addRecipes(arcFurnaceFluidHandler.getRecipes(), ARC_FURNACE_FLUID);
        registry.addRecipes(arcFurnaceSolidHandler.getRecipes(), ARC_FURNACE_SOLID);
        registry.addRecipes(boilingHandler.getRecipes(), BOILER);
        registry.addRecipes(crackingHandler.getRecipes(), CRACKING);
        registry.addRecipes(chemicalPlantRecipeHandler.getRecipes(), CHEMICAL_PLANT);
        registry.addRecipes(compressingHandler.getRecipes(), COMPRESSING);
        registry.addRecipes(constructionHandler.getRecipes(), CONSTRUCTION);
        registry.addRecipes(crucibleAlloyingHandler.getRecipes(), CRUCIBLE_ALLOY);
        registry.addRecipes(crucibleCastingHandler.getRecipes(), CRUCIBLE_CAST);
        registry.addRecipes(crucibleSmeltingHandler.getRecipes(), CRUCIBLE_SMELT);
        registry.addRecipes(deuteriumHandler.getRecipes(), DEUTERIUM);
        registry.addRecipes(fractioningHandler.getRecipes(), FRACTIONING);
        registry.addRecipes(fusionBreederRecipeHandler.getRecipes(), FUSION_BREEDER);
        registry.addRecipes(fusionRecipeHandler.getRecipes(), FUSION_BYPRODUCT);
        registry.addRecipes(hydrotreatHandler.getRecipes(), HYDROTREATING);
        registry.addRecipes(liquefactHandler.getRecipes(), LIQUEFACTION);
        registry.addRecipes(mixerHandler.getRecipes(), MIXER);
        registry.addRecipes(outgasserHandler.getRecipes(), RBMKOUTGASSER);
        registry.addRecipes(reformingHandler.getRecipes(), REFORMING);
        registry.addRecipes(solidificationHandler.getRecipes(), SOLIDIFICATION);
        registry.addRecipes(solderingStationHandler.getRecipes(), SOLDERING_STATION);
        registry.addRecipes(arcWelderRecipeHandler.getRecipes(), ARC_WELDER);
        registry.addRecipes(rotaryFurnaceRecipeHandler.getRecipes(), ROTARY_FURNACE);
        registry.addRecipes(electrolyserFluidHandler.getRecipes(), ELECTROLYSIS_FLUID);
        registry.addRecipes(electrolyserMetalHandler.getRecipes(), ELECTROLYSIS_METAL);
        registry.addRecipes(rtgRecipeHandler.getRecipes(), RTG);
        registry.addRecipes(pyroHandler.getRecipes(), PYROLYSIS);
        registry.addRecipes(crystallizerHandler.getRecipes(), CRYSTALLIZER);
        registry.addRecipes(cokingHandler.getRecipes(), COKER);
        registry.addRecipes(sawmillHandler.getRecipes(), SAWMILL);
        registry.addRecipes(vacuumHandler.getRecipes(), VACUUM);
        registry.addRecipes(zirnoxHandler.getRecipes(), ZIRNOX);
        registry.addRecipes(shredderHandler.getRecipes(), SHREDDER);
        registry.addRecipes(JeiRecipes.getFluidEquivalences(), FLUIDS);
        registry.addRecipes(JeiRecipes.getBookRecipes(), BOOK);
        registry.addRecipes(JeiRecipes.getBreederRecipes(), BREEDER);
        registry.addRecipes(JeiRecipes.getHadronRecipes(), HADRON);
        registry.addRecipes(JeiRecipes.getSILEXRecipes(), SILEX);
        registry.addRecipes(JeiRecipes.getSILEXRecipes(EnumWavelengths.IR), SILEX_IR);
        registry.addRecipes(JeiRecipes.getSILEXRecipes(EnumWavelengths.VISIBLE), SILEX_VISIBLE);
        registry.addRecipes(JeiRecipes.getSILEXRecipes(EnumWavelengths.UV), SILEX_UV);
        registry.addRecipes(JeiRecipes.getSILEXRecipes(EnumWavelengths.GAMMA), SILEX_GAMMA);
        registry.addRecipes(JeiRecipes.getSILEXRecipes(EnumWavelengths.DRX), SILEX_DIGAMMA);
        registry.addRecipes(JeiRecipes.getRBMKFuelRecipes(), RBMKFUEL);
        registry.addRecipes(DFCRecipes.getDFCRecipes(), DFC);
        registry.addRecipes(oreSlopperHandler.getRecipes(), ORE_SLOPPER);
        registry.addRecipes(particleAcceleratorHandler.getRecipes(), PA);
        registry.addRecipes(exposureChamberHandler.getRecipes(), EXPOSURE);
        registry.addRecipes(JeiRecipes.getRadiolysisRecipes(), RADIOLYSIS);
        registry.addRecipes(combinationHandler.getRecipes(), COMBINATION);
        registry.addRecipes(purexHandler.getRecipes(), PUREX);

        registry.addRecipeClickArea(GUIMachineCoker.class, 60, 22, 32, 18, COKER);
        registry.addRecipeClickArea(GUIMachineArcFurnaceLarge.class, 17, 36, 7, 70, ARC_FURNACE_SOLID);
        registry.addRecipeClickArea(GUIMachineCatalyticReformer.class, 67, 82, 24, 24, REFORMING);
        registry.addRecipeClickArea(GUIMachineHydrotreater.class, 85, 82, 24, 24, HYDROTREATING);
        registry.addRecipeClickArea(GUIMachinePUREX.class, 62, 25, 47, 9, PUREX);
        registry.addRecipeClickArea(GUIMachinePUREX.class, 62, 90, 47, 9, PUREX);
        registry.addRecipeClickArea(GUILiquefactor.class, 42, 18, 41, 16, LIQUEFACTION);
        registry.addRecipeClickArea(GUILiquefactor.class, 42, 34, 2, 18, LIQUEFACTION);
        registry.addRecipeClickArea(GUIFurnaceBrick.class, 86, 35, 22, 15, VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeClickArea(GUIMachineElectricFurnace.class, 80, 35, 22, 15, VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeClickArea(GUIFurnaceIron.class, 52, 36, 70, 5, VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeClickArea(GUIFurnaceSteel.class, 54, 18, 68, 5, VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeClickArea(GUIFurnaceSteel.class, 54, 36, 68, 5, VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeClickArea(GUIFurnaceSteel.class, 54, 54, 68, 5, VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeClickArea(GUIMicrowave.class, 104, 35, 22, 15, VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeClickArea(GUIRtgFurnace.class, 102, 36, 22, 15, RTG);
        registry.addRecipeClickArea(GUISolidifier.class, 42, 18, 38, 16, SOLIDIFICATION);
        registry.addRecipeClickArea(GUISolidifier.class, 75, 34, 8, 9, SOLIDIFICATION);
        registry.addRecipeClickArea(GUICompressor.class, 42, 27, 54, 16, COMPRESSING);
        registry.addRecipeClickArea(GUIOreSlopper.class, 62, 18, 6, 34, ORE_SLOPPER);
        registry.addRecipeClickArea(GUIOreSlopper.class, 68, 18, 22, 6, ORE_SLOPPER);
        registry.addRecipeClickArea(GUIOreSlopper.class, 68, 46, 22, 6, ORE_SLOPPER);
        registry.addRecipeClickArea(GUIOreSlopper.class, 90, 18, 6, 34, ORE_SLOPPER);
		registry.addRecipeClickArea(GUIMixer.class, 62, 36, 52, 44, MIXER);
		registry.addRecipeClickArea(GUIMachineCyclotron.class, 50, 24, 40, 40, CYCLOTRON);
		registry.addRecipeClickArea(GUIMachinePress.class, 80, 35, 15, 15, PRESS);
		registry.addRecipeClickArea(GUIMachineEPress.class, 80, 35, 15, 15, PRESS);
		registry.addRecipeClickArea(GUIDiFurnace.class, 102, 36, 21, 14, ALLOY);
		registry.addRecipeClickArea(GUIDiFurnaceRTG.class, 102, 36, 21, 14, ALLOY);
		registry.addRecipeClickArea(GUIMachineCentrifuge.class, 35, 9, 106, 40, CENTRIFUGE);
		registry.addRecipeClickArea(GUIMachineGasCent.class, 70, 36, 36, 12, GAS_CENT);
        registry.addRecipeClickArea(GUIMachineSolderingStation.class, 72, 29, 32, 13, SOLDERING_STATION);
		registry.addRecipeClickArea(GUIMachineReactorBreeding.class, 73, 32, 30, 20, BREEDER);
		registry.addRecipeClickArea(GUIMachineRefinery.class, 53, 16, 31, 100, REFINERY);
		registry.addRecipeClickArea(GUIMachineShredder.class, 43, 89, 53, 17, SHREDDER);
		registry.addRecipeClickArea(GUICrystallizer.class, 79, 40, 29, 26, CRYSTALLIZER);
		registry.addRecipeClickArea(GUIBook.class, 89, 34, 23, 16, BOOK);
		registry.addRecipeClickArea(GUIHadron.class, 71, 28, 32, 32, HADRON);
		registry.addRecipeClickArea(GUISILEX.class, 45, 82, 113-45, 125-82, SILEX);
		registry.addRecipeClickArea(GUIAnvil.class, 34, 26, 52-34, 44-26, ANVIL_SMITH);
		registry.addRecipeClickArea(GUIAnvil.class, 12, 50, 48-12, 66-50, ANVIL_CON);
		registry.addRecipeClickArea(GUIRBMKOutgasser.class, 64, 53, 48, 16, RBMKOUTGASSER);
        registry.addRecipeClickArea(GUIMachineArcWelder.class, 72, 38, 32, 13, ARC_WELDER);
        registry.addRecipeClickArea(GUIMachineRotaryFurnace.class, 63, 31, 32, 9, ROTARY_FURNACE);
        registry.addRecipeClickArea(GUIElectrolyserFluid.class, 62, 26, 12, 40, ELECTROLYSIS_FLUID);
        registry.addRecipeClickArea(GUIElectrolyserMetal.class, 7, 46, 22, 25, ELECTROLYSIS_METAL);
        registry.addRecipeClickArea(GUIPyroOven.class, 57, 48, 27, 11, PYROLYSIS);
        registry.addRecipeClickArea(GUIPADetector.class, 75, 35, 82-75, 43-35, PA);
        registry.addRecipeClickArea(GUIPASource.class, 75, 35, 82-75, 43-35, PA);
        registry.addRecipeClickArea(GUIMachineExposureChamber.class, 36, 40, 76-36, 48-40, EXPOSURE);
        registry.addRecipeClickArea(GUIRadiolysis.class, 71, 35, 99-71, 50-35, RADIOLYSIS);
        registry.addRecipeClickArea(GUIFurnaceCombo.class, 54, 55, 17, 17, JEIConfig.COMBINATION);
        registry.addRecipeClickArea(GUIFusionBreeder.class, 67, 49, 42, 9, JEIConfig.FUSION_BREEDER);
        registry.addRecipeClickArea(GUIFusionTorus.class, 99, 39, 28, 10, JEIConfig.FUSION_BYPRODUCT);

        IRecipeTransferRegistry transferRegistry = registry.getRecipeTransferRegistry();
        transferRegistry.addRecipeTransferHandler(new ExposureChamberTransferInfo());
        transferRegistry.addRecipeTransferHandler(ContainerFurnaceCombo.class, COMBINATION, 0, 1, 4, 36);

        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
        for(Item item : ItemGunBaseNT.secrets) {
            blacklist.addIngredientToBlacklist(new ItemStack(item));
        }
        // Some things are even beyond my control...or are they?
        blacklist.addIngredientToBlacklist(new ItemStack(ModItems.memory));
        for(int i = 0; i < GunFactory.EnumAmmoSecret.values().length; i++) blacklist.addIngredientToBlacklist(new ItemStack(ModItems.ammo_secret, 1, i));
        for(int i = 0; i < ItemEnums.EnumSecretType.values().length; i++) blacklist.addIngredientToBlacklist(new ItemStack(ModItems.item_secret, 1, i));

        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.machine_electric_furnace_on));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.machine_difurnace_on));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.machine_rtg_furnace_on));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.reinforced_lamp_on));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.statue_elb));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.statue_elb_g));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.statue_elb_w));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.statue_elb_f));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.cheater_virus));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.cheater_virus_seed));
        blacklist.addIngredientToBlacklist(new ItemStack(ModItems.chemistry_icon, 1, 0));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.euphemium_helmet));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.euphemium_plate));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.euphemium_legs));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.euphemium_boots));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.apple_euphemium));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.ingot_euphemium));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.nugget_euphemium));
        blacklist.addIngredientToBlacklist(new ItemStack(ModItems.euphemium_kit));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.euphemium_stopper));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.watch));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.rod_quad_euphemium));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.rod_euphemium));
        blacklist.addIngredientToBlacklist(new ItemStack(ModItems.bobmazon_hidden));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.zirnox_destroyed));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.machine_furnace_brick_on));
        blacklist.addIngredientToBlacklist(new ItemStack(ModItems.ammo_misc));
        if(!GeneralConfig.enableDebugMode) {
            blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.obj_tester));
            blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.test_render));
            blacklist.addIngredientToBlacklist(new ItemStack(ModItems.ammo_debug));
            blacklist.addIngredientToBlacklist(new ItemStack(ModItems.gun_debug));
            blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.keypad_test));
        }
        if (MainRegistry.polaroidID != 11) {
            blacklist.addIngredientToBlacklist(new ItemStack(ModItems.book_secret));
            // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.book_of_));
            blacklist.addIngredientToBlacklist(new ItemStack(ModItems.ams_core_thingy));
        }
        blacklist.addIngredientToBlacklist(new ItemStack(ModItems.achievement_icon));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.dummy_block_uf6));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.dummy_block_puf6));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.dummy_block_vault));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.dummy_block_blast));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.dummy_plate_compact_launcher));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.dummy_port_compact_launcher));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.dummy_plate_launch_table));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.dummy_port_launch_table));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.dummy_plate_cargo));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.dummy_block_silo_hatch));


        for (Item item : ModItems.ALL_ITEMS) {
            if (item instanceof EffectItem) {
                blacklist.addIngredientToBlacklist(new ItemStack(item));
            }
        }
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registry) {
        if (!GeneralConfig.jei)
            return;
        IGuiHelper help = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(
                new PressRecipeHandler(help),
                new AlloyFurnaceRecipeHandler(help),
                shredderHandler = new ShredderRecipeHandler(help),
                new RefineryRecipeHandler(help),
                new RadiolysisRecipeHandler(help),
                ammoPressHandler = new AmmoPressHandler(help),
                anvilRecipeHandler = new AnvilRecipeHandler(help),
                anvilSmithingRecipeHandler = new AnvilSmithingRecipeHandler(help),
                arcFurnaceFluidHandler = new ArcFurnaceFluidHandler(help),
                arcFurnaceSolidHandler = new ArcFurnaceSolidHandler(help),
                arcWelderRecipeHandler = new ArcWelderRecipeHandler(help),
                assemblyMachineRecipeHandler = new AssemblyMachineRecipeHandler(help),
                ashpitHandler = new AshpitHandler(help),
                boilingHandler = new BoilingHandler(help),
                centrifugeRecipeHandler = new CentrifugeRecipeHandler(help),
                chemicalPlantRecipeHandler = new ChemicalPlantRecipeHandler(help),
                cokingHandler = new CokingRecipeHandler(help),
                constructionHandler = new ConstructionHandler(help),
                compressingHandler = new CompressingRecipeHandler(help),
                crackingHandler = new CrackingHandler(help),
                crucibleAlloyingHandler = new CrucibleAlloyingHandler(help),
                crucibleCastingHandler = new CrucibleCastingHandler(help),
                crucibleSmeltingHandler = new CrucibleSmeltingHandler(help),
                crystallizerHandler = new CrystallizerRecipeHandler(help),
                deuteriumHandler = new DeuteriumHandler(help),
                electrolyserFluidHandler = new ElectrolyserFluidHandler(help),
                electrolyserMetalHandler = new ElectrolyserMetalHandler(help),
                fractioningHandler = new FractioningRecipeHandler(help),
                fusionBreederRecipeHandler = new FusionBreederRecipeHandler(help),
                fusionRecipeHandler = new FusionRecipeHandler(help),
                fuelPoolHandler = new FuelPoolHandler(help),
                hydrotreatHandler = new HydrotreatingHandler(help),
                liquefactHandler = new LiquefactionHandler(help),
                mixerHandler = new MixerRecipeHandler(help),
                outgasserHandler = new RBMKOutgasserRecipeHandler(help),
                pyroHandler = new PyroHandler(help),
                reformingHandler = new ReformingHandler(help),
                rotaryFurnaceRecipeHandler = new RotaryFurnaceRecipeHandler(help),
                solderingStationHandler = new SolderingStationRecipeHandler(help),
                solidificationHandler = new SolidificationHandler(help),
                oreSlopperHandler = new OreSlopperHandler(help),
                particleAcceleratorHandler = new ParticleAcceleratorHandler(help),
                exposureChamberHandler = new ExposureChamberHandler(help),
                combinationHandler = new CombinationHandler(help),
                rtgRecipeHandler = new RTGRecipeHandler(help),
                sawmillHandler = new SawmillHandler(help),
                vacuumHandler = new VacuumRecipeHandler(help),
                zirnoxHandler = new ZirnoxRecipeHandler(help),
                purexHandler = new PUREXRecipeHandler(help),
                new GasCentrifugeRecipeHandler(help),
                new BreederRecipeHandler(help),
                new CyclotronRecipeHandler(help),
                new TransmutationRecipeHandler(help),
                new CMBFurnaceRecipeHandler(help),
                new StorageDrumRecipeHandler(help),
                new FluidRecipeHandler(help),
                new SILEXRecipeHandler(help),
                new SILEXIrRecipeHandler(help),
                new SILEXVisibleRecipeHandler(help),
                new SILEXUVRecipeHandler(help),
                new SILEXGammaRecipeHandler(help),
                new SILEXDigammaRecipeHandler(help),
                new RBMKFuelRecipeHandler(help),
                new HadronRecipeHandler(help),
                new DFCRecipeHandler(help),
                new BookRecipeHandler(help));
    }

    private static final ISubtypeRegistry.ISubtypeInterpreter metadataFluidContainerInterpreter = stack -> {
        FluidType type = Fluids.fromID(stack.getMetadata());
        if (FluidContainerRegistry.getFluidContainer(stack) == null) return "";
        return type.getName();
    };

    @Override
	public void registerSubtypes(@NotNull ISubtypeRegistry subtypeRegistry) {
		if(!GeneralConfig.jei)
			return;
        subtypeRegistry.useNbtForSubtypes(ModItems.fluid_identifier_multi);
        subtypeRegistry.registerSubtypeInterpreter(ModItems.cell, metadataFluidContainerInterpreter);
        subtypeRegistry.registerSubtypeInterpreter(ModItems.fluid_tank_full, metadataFluidContainerInterpreter);
        subtypeRegistry.registerSubtypeInterpreter(ModItems.fluid_barrel_full, metadataFluidContainerInterpreter);
        subtypeRegistry.registerSubtypeInterpreter(ModItems.fluid_tank_lead_full, metadataFluidContainerInterpreter);
        subtypeRegistry.registerSubtypeInterpreter(ModItems.canister_full, metadataFluidContainerInterpreter);
        subtypeRegistry.registerSubtypeInterpreter(ModItems.disperser_canister, metadataFluidContainerInterpreter);
        subtypeRegistry.registerSubtypeInterpreter(ModItems.glyphid_gland, metadataFluidContainerInterpreter);
		subtypeRegistry.registerSubtypeInterpreter(ModItems.missile_custom, stack -> ModItems.missile_custom.getTranslationKey() + "w" +
                ItemCustomMissile.readFromNBT(stack, "warhead") + "f" + ItemCustomMissile.readFromNBT(stack, "fuselage") + "s" +
                ItemCustomMissile.readFromNBT(stack, "stability") + "t" + ItemCustomMissile.readFromNBT(stack, "thruster"));
        subtypeRegistry.registerSubtypeInterpreter(ModItems.fluid_icon, stack -> {
            FluidType fluidType = ItemFluidIcon.getFluidType(stack);
            if (fluidType == null) return "";
            return fluidType.getTranslationKey();
        });
        subtypeRegistry.registerSubtypeInterpreter(ModItems.crucible_template, itemStack -> {
            CrucibleRecipes.CrucibleRecipe recipe = CrucibleRecipes.indexMapping.get(itemStack.getItemDamage());
            if(recipe == null) return "";
            return recipe.getName();
        });
	}

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
    }
}
