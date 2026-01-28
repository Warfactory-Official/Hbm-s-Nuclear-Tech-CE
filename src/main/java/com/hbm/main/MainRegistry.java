package com.hbm.main;


import com.google.common.collect.ImmutableList;
import com.hbm.Tags;
import com.hbm.blocks.BlockEnums;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.fluid.ModFluids;
import com.hbm.blocks.generic.BlockCrate;
import com.hbm.capability.HbmCapability;
import com.hbm.capability.HbmLivingCapability;
import com.hbm.capability.NTMBatteryCapabilityHandler;
import com.hbm.command.CommandHbm;
import com.hbm.command.CommandLocate;
import com.hbm.command.CommandPacketInfo;
import com.hbm.command.CommandRadiation;
import com.hbm.config.*;
import com.hbm.creativetabs.*;
import com.hbm.datagen.AdvGen;
import com.hbm.entity.EntityMappings;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.entity.siege.SiegeTier;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.handler.*;
import com.hbm.handler.imc.IMCHandler;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.radiation.RadiationSystemNT;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.hazard.HazardData;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.inventory.BedrockOreRegistry;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.control_panel.ControlEvent;
import com.hbm.inventory.control_panel.ControlRegistry;
import com.hbm.inventory.control_panel.modular.StockNodesRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.*;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.GrenadeDispenserRegistry;
import com.hbm.items.weapon.sedna.mods.WeaponModManager;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.HbmWorld;
import com.hbm.packet.PacketDispatcher;
import com.hbm.potion.HbmDetox;
import com.hbm.potion.HbmPotion;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.tileentity.bomb.TileEntityLaunchPadBase;
import com.hbm.tileentity.bomb.TileEntityNukeCustom;
import com.hbm.tileentity.machine.TileEntityMachineRadarNT;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import com.hbm.util.*;
import com.hbm.world.feature.OreCave;
import com.hbm.world.feature.OreLayer3D;
import com.hbm.world.feature.SchistStratum;
import com.hbm.world.generator.CellularDungeonFactory;
import com.hbm.world.phased.PhasedEventHandler;
import com.hbm.world.phased.PhasedStructureRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatBasic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Random;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME,
        dependencies = "required-client:ctm;"
)
public class MainRegistry {

    @SidedProxy(clientSide = "com.hbm.main.ClientProxy", serverSide = "com.hbm.main.ServerProxy")
    public static ServerProxy proxy;
    @Mod.Instance(Tags.MODID)
    public static MainRegistry instance;
    public static Logger logger;
    // Creative Tabs
    // ingots, nuggets, wires, machine parts
    public static CreativeTabs partsTab = new PartsTab(CreativeTabs.getNextID(), "tabParts");
    // items that belong in machines, fuels, etc
    public static CreativeTabs controlTab = new ControlTab(CreativeTabs.getNextID(), "tabControl");
    // templates, siren tracks
    public static CreativeTabs templateTab = new TemplateTab(CreativeTabs.getNextID(), "tabTemplate");
    // ore and mineral blocks
    public static CreativeTabs resourceTab = new ResourceTab(CreativeTabs.getNextID(), "tabResource");
    // construction blocks
    public static CreativeTabs blockTab = new BlockTab(CreativeTabs.getNextID(), "tabBlocks");
    // machines, structure parts
    public static CreativeTabs machineTab = new MachineTab(CreativeTabs.getNextID(), "tabMachine");
    // bombs
    public static CreativeTabs nukeTab = new NukeTab(CreativeTabs.getNextID(), "tabNuke");
    // missiles, satellites
    public static CreativeTabs missileTab = new MissileTab(CreativeTabs.getNextID(), "tabMissile");
    // turrets, weapons, ammo
    public static CreativeTabs weaponTab = new WeaponTab(CreativeTabs.getNextID(), "tabWeapon");
    // drinks, kits, tools
    public static CreativeTabs consumableTab = new ConsumableTab(CreativeTabs.getNextID(), "tabConsumable");

    public static StatBase statLegendary;
    public static StatBase statMines;
    public static StatBase statBullets;
    public static int generalOverride = 0;
    public static int polaroidID = 1;
    public static int x;
    public static int y;
    public static int z;
    public static long time;
    public static long startupTime = 0;
    public static File configDir;
    public static File configHbmDir;

    static {
        HBMSoundHandler.init();
        FluidRegistry.enableUniversalBucket();
        MaterialRegistry.init();
    }

    Random rand = new Random();

    public static void reloadConfig() {
        Configuration config = new Configuration(new File(proxy.getDataDir().getPath() + "/config/hbm/hbm.cfg"));
        config.load();

        GeneralConfig.loadFromConfig(config);
        MachineConfig.loadFromConfig(config);
        BombConfig.loadFromConfig(config);
        RadiationConfig.loadFromConfig(config);
        PotionConfig.loadFromConfig(config);
        ToolConfig.loadFromConfig(config);
        WeaponConfig.loadFromConfig(config);
        MobConfig.loadFromConfig(config);
        SpaceConfig.loadFromConfig(config);
        StructureConfig.loadFromConfig(config);
        reloadCompatConfig();
        BedrockOreJsonConfig.init();
        config.save();
    }

    public static void reloadCompatConfig() {
        Configuration config = new Configuration(new File(proxy.getDataDir().getPath() + "/config/hbm/hbm_dimensions.cfg"));
        config.load();
        CompatibilityConfig.loadFromConfig(config);
        config.save();
    }

    @EventHandler //Apparently this is "legacy", well I am not making my own protocol
    public static void initIMC(FMLInterModComms.IMCEvent event) {

        ImmutableList<FMLInterModComms.IMCMessage> inbox = event.getMessages();

        for (FMLInterModComms.IMCMessage message : inbox) {
            IMCHandler handler = IMCHandler.getHandler(message.key);

            if (handler != null) {
                MainRegistry.logger.info("Received IMC of type >" + message.key + "< from " + message.getSender() + "!");
                handler.process(message);
            } else {
                MainRegistry.logger.error("Could not process unknown IMC type \"" + message.key + "\"");
            }
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        startupTime = System.currentTimeMillis();
        CrashHelper.init();
        Compat.exitOnIncompatible();

        if (logger == null)
            logger = event.getModLog();

        if (generalOverride > 0 && generalOverride < 19) {
            polaroidID = generalOverride;
        } else {
            polaroidID = rand.nextInt(18) + 1;
            while (polaroidID == 4 || polaroidID == 9)
                polaroidID = rand.nextInt(18) + 1;
        }

        configDir = event.getModConfigurationDirectory();
        configHbmDir = new File(configDir.getAbsolutePath() + File.separatorChar + "hbmConfig");

        if (!configHbmDir.exists()) configHbmDir.mkdir();

        if (SharedMonsterAttributes.MAX_HEALTH.clampValue(Integer.MAX_VALUE) <= 2000) {
            ((RangedAttribute) SharedMonsterAttributes.MAX_HEALTH).maximumValue = Integer.MAX_VALUE;
        }
        proxy.checkGLCaps();
        reloadConfig();

        OreDictManager.registerGroups();
        OreDictManager oreMan = new OreDictManager();

        MinecraftForge.EVENT_BUS.register(oreMan); //OreRegisterEvent

        MinecraftForge.EVENT_BUS.register(new ModEventHandler());
        MinecraftForge.TERRAIN_GEN_BUS.register(new ModEventHandler());
        MinecraftForge.ORE_GEN_BUS.register(new ModEventHandler());
        MinecraftForge.EVENT_BUS.register(new ModEventHandlerImpact());
        MinecraftForge.TERRAIN_GEN_BUS.register(new ModEventHandlerImpact());
        MinecraftForge.EVENT_BUS.register(new PollutionHandler());
        MinecraftForge.EVENT_BUS.register(new DamageResistanceHandler());

        if (event.getSide() == Side.CLIENT) {
            HbmKeybinds keyHandler = new HbmKeybinds();
            MinecraftForge.EVENT_BUS.register(keyHandler);
        }

        HbmPotion.init();

        CapabilityManager.INSTANCE.register(HbmLivingCapability.IEntityHbmProps.class, new HbmLivingCapability.EntityHbmPropsStorage(), HbmLivingCapability.EntityHbmProps.FACTORY);
        CapabilityManager.INSTANCE.register(HbmCapability.IHBMData.class, new HbmCapability.HBMDataStorage(), HbmCapability.HBMData.FACTORY);
        Fluids.init();
        ModFluids.init();
        ModItems.preInit();
        ModBlocks.preInit();
        BulletConfigSyncingUtil.loadConfigsForSync();
        CellularDungeonFactory.init();
        Satellite.register();
        HTTPHandler.loadStats();
        MultiblockBBHandler.init();
        ControlEvent.init();
        SiegeTier.registerTiers();
        HazardRegistry.registerItems();
        HazardRegistry.registerTrafos();
        WeaponModManager.init();

        proxy.registerRenderInfo();
        HbmWorld.mainRegistry();
        proxy.preInit(event);

        StockNodesRegister.register();


        MaterialRegistry.initFixMaterials();
        AutoRegistry.registerTileEntities();
        AutoRegistry.loadAuxiliaryData();
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

        int i = 0;


        AutoRegistry.registerEntities(i);
        ForgeChunkManager.setForcedChunkLoadingCallback(this, (tickets, world) -> {
            for (Ticket ticket : tickets) {

                if (ticket.getEntity() instanceof IChunkLoader) {
                    ((IChunkLoader) ticket.getEntity()).init(ticket);
                }
            }
        });

        GrenadeDispenserRegistry.registerDispenserBehaviors();
        GrenadeDispenserRegistry.registerDispenserBehaviorFertilizer();
        TileEntityLaunchPadBase.registerLaunchables();
        TileEntityMachineRadarNT.registerEntityClasses();
        TileEntityMachineRadarNT.registerConverters();

        EntityMappings.writeSpawns();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        statLegendary = new StatBasic("stat.ntmLegendary", new TextComponentTranslation("stat.ntmLegendary")).registerStat();
        statMines = new StatBasic("stat.ntmMines", new TextComponentTranslation("stat.ntmMines")).registerStat();
        statBullets = new StatBasic("stat.ntmBullets", new TextComponentTranslation("stat.ntmBullets")).registerStat();
        ModItems.init();
        proxy.init(event);
        ModBlocks.init();
        HazmatRegistry.registerHazmats();
        ControlRegistry.init();
        OreDictManager.registerOres();
        if (RadiationConfig.enableContaminationOnGround)
            HazardRegistry.registerContaminatingDrops();
        Fluids.initForgeFluidCompat();
        PacketDispatcher.registerPackets();
        PacketThreading.init();
        IMCHandler.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ModItems.postInit();
        ModBlocks.postInit();
        DamageResistanceHandler.init();
        BlockCrate.setDrops();
        BedrockOreRegistry.registerBedrockOres();
        ExplosionNukeGeneric.loadSoliniumFromFile();
        HadronRecipes.register();
        MagicRecipes.register();
        SILEXRecipes.register();
        GasCentrifugeRecipes.register();
        NTMToolHandler.register();
        SerializableRecipe.registerAllHandlers();
        SerializableRecipe.initialize();
        AnvilRecipes.register();
        ClientConfig.initConfig();
        RefineryRecipes.registerRefinery();
        ModFluids.setFromRegistry();
        FluidContainerRegistry.register();
        TileEntityNukeCustom.registerBombItems();
        ArmorUtil.register();
        RBMKFuelRecipes.registerRecipes();
        DFCRecipes.register();
        StorageDrumRecipes.registerRecipes();
        NuclearTransmutationRecipes.registerRecipes();
        EngineRecipes.registerEngineRecipes();
        FluidCombustionRecipes.registerFluidCombustionRecipes();
        HbmDetox.init();
        NTMBatteryCapabilityHandler.initialize();

        //has to register after cracking, and therefore after all serializable recipes
        RadiolysisRecipes.registerRadiolysis();

        ItemPoolConfigJSON.initialize();

        MobUtil.intializeMobPools();

        //Drillgon200: expand the max entity radius for the hunter chopper
        if (World.MAX_ENTITY_RADIUS < 5)
            World.MAX_ENTITY_RADIUS = 5;
        MinecraftForge.EVENT_BUS.register(new SchistStratum(ModBlocks.stone_gneiss.getDefaultState(), 0.01D, 5, 8, 30)); //DecorateBiomeEvent.Pre
        if (WorldConfig.enableSulfurCave)
            new OreCave(ModBlocks.stone_resource, 0).setThreshold(1.5D).setRangeMult(20).setYLevel(30).setMaxRange(20).withFluid(ModBlocks.sulfuric_acid_block);    //sulfur
        if (WorldConfig.enableAsbestosCave)
            new OreCave(ModBlocks.stone_resource, 1).setThreshold(1.75D).setRangeMult(20).setYLevel(25).setMaxRange(20);                                            //asbestos
        if (WorldConfig.enableHematite)
            new OreLayer3D(ModBlocks.stone_resource, BlockEnums.EnumStoneType.HEMATITE.ordinal()).setScaleH(0.04D).setScaleV(0.25D).setThreshold(230);
        if (WorldConfig.enableBauxite)
            new OreLayer3D(ModBlocks.stone_resource, BlockEnums.EnumStoneType.BAUXITE.ordinal()).setScaleH(0.03D).setScaleV(0.15D).setThreshold(300);
        if (WorldConfig.enableMalachite)
            new OreLayer3D(ModBlocks.stone_resource, BlockEnums.EnumStoneType.MALACHITE.ordinal()).setScaleH(0.1D).setScaleV(0.15D).setThreshold(275);

        if (event.getSide() == Side.CLIENT) {
            BedrockOreRegistry.registerOreColors();
        }
        proxy.postInit(event);
        AdvGen.generate();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent evt) {
        RBMKDials.createDials(evt.getServer().getEntityWorld());
        evt.registerServerCommand(new CommandRadiation());
        evt.registerServerCommand(new CommandHbm());
        evt.registerServerCommand(new CommandLocate());
        evt.registerServerCommand(new CommandPacketInfo());
        AdvancementManager.init(evt.getServer());
        //MUST be initialized AFTER achievements!!
        BobmazonOfferFactory.init();

        PhasedStructureRegistry.onServerStarting(evt.getServer());
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent evt) {
        RadiationSystemNT.onServerStopping();
        ChunkUtil.onServerStopping();
        RecipesCommon.onServerStopping();
        ModEventHandler.RBMK_COL_HEIGHT_MAP.clear();
    }

    @EventHandler
    public void serverStopped(FMLServerStoppedEvent evt) {
        RadiationSystemNT.onServerStopped();
        PhasedEventHandler.onServerStopped();
        PhasedStructureRegistry.onServerStopped();
    }

    @EventHandler
    public void fMLLoadCompleteEvent(FMLLoadCompleteEvent evt) {
        proxy.onLoadComplete(evt);
        RadiationSystemNT.onLoadComplete();
        FalloutConfigJSON.initialize();
        for (Tuple<ResourceLocation, HazardData> tuple : HazardSystem.locationRateRegisterList)
            HazardSystem.register(tuple.getFirst(), tuple.getSecond());

        HazardSystem.clearCaches();
        if (!HazardSystem.locationRateRegisterList.isEmpty()) {
            HazardSystem.locationRateRegisterList.clear();
        }
    }
}
