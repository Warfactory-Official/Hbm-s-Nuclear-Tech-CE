package com.hbm.items;

import com.hbm.Tags;
import com.hbm.api.block.IToolable.ToolType;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockModDoor;
import com.hbm.blocks.machine.ItemSelfcharger;
import com.hbm.blocks.network.FluidDuctBox;
import com.hbm.blocks.network.FluidDuctStandard;
import com.hbm.blocks.network.energy.PowerCableBox;
import com.hbm.config.BombConfig;
import com.hbm.handler.ability.IToolAreaAbility;
import com.hbm.handler.ability.IToolHarvestAbility;
import com.hbm.handler.ability.IWeaponAbility;
import com.hbm.handler.guncfg.Gun12GaugeFactory;
import com.hbm.handler.guncfg.GunEnergyFactory;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.items.armor.*;
import com.hbm.items.bomb.*;
import com.hbm.items.food.*;
import com.hbm.items.gear.*;
import com.hbm.items.machine.*;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.items.machine.ItemRBMKRod.EnumBurnFunc;
import com.hbm.items.machine.ItemRBMKRod.EnumDepleteFunc;
import com.hbm.items.special.*;
import com.hbm.items.special.weapon.GunB92;
import com.hbm.items.tool.*;
import com.hbm.items.tool.ItemToolAbility.EnumToolType;
import com.hbm.items.weapon.*;
import com.hbm.items.weapon.ItemMissile.FuelType;
import com.hbm.items.weapon.ItemMissile.PartSize;
import com.hbm.items.weapon.ItemMissile.Rarity;
import com.hbm.items.weapon.ItemMissile.WarheadType;
import com.hbm.items.weapon.sedna.factory.GunFactory;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.potion.HbmPotion;
import com.hbm.tileentity.machine.rbmk.IRBMKFluxReceiver.NType;
import com.hbm.util.EnchantmentUtil;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.*;

import static com.hbm.items.ItemEnums.*;

public class ModItems {

    public static HashSet<Item> excludeNEI = new HashSet<>();

    public static final List<Item> ALL_ITEMS = new ArrayList<>();

    public static final Item redstone_sword = new RedstoneSword(ToolMaterial.STONE, "redstone_sword").setCreativeTab(CreativeTabs.COMBAT);
    public static final Item big_sword = new BigSword(ToolMaterial.DIAMOND, "big_sword").setCreativeTab(CreativeTabs.COMBAT);


    public static final Item dosimeter = new ItemDosimeter("dosimeter").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item geiger_counter = new ItemGeigerCounter("geiger_counter").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item digamma_diagnostic = new ItemDigammaDiagnostic("digamma_diagnostic").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item lung_diagnostic = new ItemLungDiagnostic("lung_diagnostic").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);

    //Tools
    public static final Item screwdriver = new ItemTooling(ToolType.SCREWDRIVER, 100, "screwdriver");
    public static final Item screwdriver_desh = new ItemTooling(ToolType.SCREWDRIVER, -1, "screwdriver_desh");
    public static final Item hand_drill = new ItemTooling(ToolType.HAND_DRILL, 100, "hand_drill");
    public static final Item hand_drill_desh = new ItemTooling(ToolType.HAND_DRILL, -1, "hand_drill_desh");
    public static final Item wrench_archineer = new ItemToolingWeapon("wrench_archineer", ToolType.WRENCH, 1000, 12F);
    public static final Item boltgun = new ItemBoltgun("boltgun");
    public static final Item blowtorch = new ItemBlowtorch("blowtorch");
    public static final Item acetylene_torch = new ItemBlowtorch("acetylene_torch");
    public static final Item chemistry_set = new ItemCraftingDegradation("chemistry_set", 100).setCreativeTab(MainRegistry.controlTab);
    public static final Item chemistry_set_boron = new ItemCraftingDegradation("chemistry_set_boron", 0).setCreativeTab(MainRegistry.controlTab);
    public static final Item reacher = new ItemCustomLore("reacher").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.consumableTab);
    public static final Item bismuth_tool = new ItemAmatExtractor("bismuth_tool").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.consumableTab);
    public static final Item wiring_red_copper = new ItemWiring("wiring_red_copper").setCreativeTab(MainRegistry.consumableTab);
    public static final Item survey_scanner = new ItemSurveyScanner("survey_scanner").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item oil_detector = new ItemOilDetector("oil_detector").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item mirror_tool = new ItemMirrorTool("mirror_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item rbmk_tool = new ItemRBMKTool("rbmk_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item coltan_tool = new ItemColtanCompass("coltan_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item power_net_tool = new ItemPowerNetTool("power_net_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item analysis_tool = new ItemAnalysisTool("analysis_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item linker = new ItemTeleLink("linker").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item reactor_sensor = new ItemReactorSensor("reactor_sensor").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item drone_linker = new ItemDroneLinker("drone_linker").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item radar_linker = new ItemRadarLinker("radar_linker").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item settings_tool = new ItemSettingsTool("settings_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item pollution_detector = new ItemPollutionDetector("pollution_detector").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item ore_density_scanner = new ItemOreDensityScanner("ore_density_scanner").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);

    //Syringe
    public static final Item syringe_empty = new ItemBase("syringe_empty").setFull3D().setCreativeTab(MainRegistry.consumableTab);
    public static final Item syringe_awesome = new ItemConsumable("syringe_awesome").setEffect().setFull3D().setCreativeTab(MainRegistry.consumableTab);
    public static final Item syringe_antidote = new ItemConsumable("syringe_antidote").setFull3D().setCreativeTab(MainRegistry.consumableTab);
    public static final Item syringe_poison = new ItemConsumable("syringe_poison").setFull3D().setCreativeTab(MainRegistry.consumableTab);
    public static final Item syringe_metal_empty = new ItemBase("syringe_metal_empty").setFull3D().setCreativeTab(MainRegistry.consumableTab);
    public static final Item syringe_metal_medx = new ItemConsumable("syringe_metal_medx").setFull3D().setCreativeTab(MainRegistry.consumableTab);
    public static final Item syringe_metal_psycho = new ItemConsumable("syringe_metal_psycho").setFull3D().setCreativeTab(MainRegistry.consumableTab);
    public static final Item syringe_metal_stimpak = new ItemConsumable("syringe_metal_stimpak").setFull3D().setCreativeTab(MainRegistry.consumableTab);
    public static final Item syringe_metal_super = new ItemConsumable("syringe_metal_super").setFull3D().setCreativeTab(MainRegistry.consumableTab);
    public static final Item syringe_taint = new ItemConsumable("syringe_taint").setFull3D().setCreativeTab(MainRegistry.consumableTab);
    public static final Item syringe_mkunicorn = new ItemConsumable("syringe_mkunicorn").setFull3D().setCreativeTab(null);
    public static final Item med_bag = new ItemConsumable("med_bag").setCreativeTab(MainRegistry.consumableTab);
    public static final Item radx = new ItemPill(0, "radx").setCreativeTab(MainRegistry.consumableTab);
    public static final Item siox = new ItemPill(0, "siox").setCreativeTab(MainRegistry.consumableTab);
    public static final Item iv_xp_empty = new ItemSimpleConsumable("iv_xp_empty").setUseActionServer((stack, user) -> {
        if (user.experienceTotal >= 100) {
            ItemSimpleConsumable.giveSoundAndDecrement(stack, user, HBMSoundHandler.syringeUse, new ItemStack(ModItems.iv_xp));
            EnchantmentUtil.removeExperience(user, 100);
        }
    }).setCreativeTab(MainRegistry.consumableTab);
    public static final Item pill_herbal = new ItemPill(0, "pill_herbal").setCreativeTab(MainRegistry.consumableTab);
    public static final Item xanax = new ItemPill(0, "xanax", "xanax_2").setCreativeTab(MainRegistry.consumableTab);
    public static final Item iv_xp = new ItemSimpleConsumable("iv_xp").setUseActionServer((stack, user) -> {
        ItemSimpleConsumable.giveSoundAndDecrement(stack, user, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, new ItemStack(ModItems.iv_xp_empty));
        user.addExperience(100);
    }).setCreativeTab(MainRegistry.consumableTab);
    public static final Item fmn = new ItemPill(0, "fmn").setCreativeTab(MainRegistry.consumableTab);
    public static final Item five_htp = new ItemPill(0, "five_htp").setCreativeTab(MainRegistry.consumableTab);
    public static final Item iv_empty = new ItemSimpleConsumable("iv_empty").setUseActionServer((stack, user) -> {
        if (user.hurtResistantTime <= 0) {
            ItemSimpleConsumable.giveSoundAndDecrement(stack, user, HBMSoundHandler.syringeUse, new ItemStack(ModItems.iv_blood));
            user.attackEntityFrom(DamageSource.MAGIC, 5F);
        }
    }).setCreativeTab(MainRegistry.consumableTab);
    public static final Item pill_iodine = new ItemPill(0, "pill_iodine").setCreativeTab(MainRegistry.consumableTab);
    public static final Item plan_c = new ItemPill(0, "plan_c").setCreativeTab(MainRegistry.consumableTab);
    public static final Item iv_blood = new ItemSimpleConsumable("iv_blood").setUseActionServer((stack, user) -> {
        ItemSimpleConsumable.giveSoundAndDecrement(stack, user, HBMSoundHandler.radawayUse, new ItemStack(ModItems.iv_empty));
        user.heal(3F);
    }).setCreativeTab(MainRegistry.consumableTab);
    public static final Item stealth_boy = new ItemStarterKit("stealth_boy").setCreativeTab(MainRegistry.consumableTab);
    public static final Item jetpack_tank = new ItemConsumable("jetpack_tank").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item radaway = new ItemSimpleConsumable("radaway").setUseActionServer((stack, user) -> {
        ItemSimpleConsumable.giveSoundAndDecrement(stack, user, HBMSoundHandler.radawayUse, new ItemStack(ModItems.iv_empty));
        ItemSimpleConsumable.addPotionEffect(user, HbmPotion.radaway, 200, 24);
    }).setCreativeTab(MainRegistry.consumableTab);
    public static final Item gun_kit_1 = new ItemRepairKit("gun_kit_1", 10).setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item gun_kit_2 = new ItemRepairKit("gun_kit_2",100).setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item radaway_strong = new ItemSimpleConsumable("radaway_strong").setUseActionServer((stack, user) -> {
        ItemSimpleConsumable.giveSoundAndDecrement(stack, user, HBMSoundHandler.radawayUse, new ItemStack(ModItems.iv_empty));
        ItemSimpleConsumable.addPotionEffect(user, HbmPotion.radaway, 100, 99);
    }).setCreativeTab(MainRegistry.consumableTab);
    public static final Item euphemium_kit = new ItemStarterKit("euphemium_kit").setMaxStackSize(1).setCreativeTab(null);
    public static final Item cbt_device = new ItemConsumable("cbt_device").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item radaway_flush = new ItemSimpleConsumable("radaway_flush").setUseActionServer((stack, user) -> {
        ItemSimpleConsumable.giveSoundAndDecrement(stack, user, HBMSoundHandler.radawayUse, new ItemStack(ModItems.iv_empty));
        ItemSimpleConsumable.addPotionEffect(user, HbmPotion.radaway, 50, 399);
    }).setCreativeTab(MainRegistry.consumableTab);
    public static final Item gas_mask_filter_rag = new ItemFilter("gas_mask_filter_rag", 4000).setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item gas_mask_filter_piss = new ItemFilter("gas_mask_filter_piss", 4000).setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item gas_mask_filter_mono = new ItemFilter("gas_mask_filter_mono", 12000).setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item gas_mask_filter = new ItemFilter("gas_mask_filter", 18000).setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item gas_mask_filter_combo = new ItemFilter("gas_mask_filter_combo", 24000).setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item gas_mask_filter_radon = new ItemFilter("gas_mask_filter_radon", 32000).setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item attachment_mask = new ItemModGasmask("attachment_mask").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item attachment_mask_mono = new ItemModGasmask("attachment_mask_mono").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item cigarette = new ItemCigarette("cigarette").setMaxStackSize(16).setCreativeTab(MainRegistry.consumableTab);
    public static final Item crackpipe = new ItemCigarette("crackpipe").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item bdcl = new ItemBDCL("bdcl").setCreativeTab(MainRegistry.consumableTab);
    public static final Item back_tesla = new ItemModTesla("back_tesla").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item pads_rubber = new ItemModPads(0.5F, "pads_rubber").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item pads_slime = new ItemModPads(0.25F, "pads_slime").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item pads_static = new ItemModPads(0.75F, "pads_static").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item cladding_paint = new ItemModCladding(0.025, "cladding_paint").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item cladding_rubber = new ItemModCladding(0.05, "cladding_rubber").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item cladding_lead = new ItemModCladding(0.1, "cladding_lead").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item cladding_desh = new ItemModCladding(0.2, "cladding_desh").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item cladding_ghiorsium = new ItemModCladding(0.5, "cladding_ghiorsium").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item cladding_iron = new ItemModIron("cladding_iron").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item cladding_obsidian = new ItemModObsidian("cladding_obsidian").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item insert_kevlar = new ItemModInsert(1500, 1F, 0.9F, 1F, 1F, "insert_kevlar").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item insert_sapi = new ItemModInsert(1750, 1F, 0.85F, 1F, 1F, "insert_sapi").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item insert_esapi = new ItemModInsert(2000, 0.95F, 0.8F, 1F, 1F, "insert_esapi").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item insert_xsapi = new ItemModInsert(2500, 0.9F, 0.75F, 1F, 1F, "insert_xsapi").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item insert_steel = new ItemModInsert(1000, 1F, 0.95F, 0.75F, 0.95F, "insert_steel").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item insert_du = new ItemModInsert(1500, 0.9F, 0.85F, 0.5F, 0.9F, "insert_du").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item insert_polonium = new ItemModInsert(500, 0.9F, 1F, 0.25F, 0.9F, "insert_polonium").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item insert_ghiorsium = new ItemModInsert(2000, 0.8F, 0.75F, 0.35F, 0.9F, "insert_ghiorsium").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item insert_era = new ItemModInsert(25, 0.5F, 1F, 0.25F, 1F, "insert_era").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item insert_yharonite = new ItemModInsert(9999, 0.01F, 1F, 1F, 1F, "insert_yharonite").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item insert_doxium = new ItemModInsert(9999, 5.0F, 1F, 1F, 1F, "insert_doxium").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item armor_polish = new ItemModPolish("armor_polish").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item bandaid = new ItemModBandaid("bandaid").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item serum = new ItemModSerum("serum").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item quartz_plutonium = new ItemModQuartz("quartz_plutonium").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item morning_glory = new ItemModMorningGlory("morning_glory").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item lodestone = new ItemModLodestone(5, "lodestone").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item horseshoe_magnet = new ItemModLodestone(8, "horseshoe_magnet").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item industrial_magnet = new ItemModLodestone(12, "industrial_magnet").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item bathwater = new ItemModBathwater("bathwater").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item bathwater_mk2 = new ItemModBathwater("bathwater_mk2").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item spider_milk = new ItemModMilk("spider_milk").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item ink = new ItemModInk("ink").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item heart_piece = new ItemModHealth(5F, "heart_piece").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item heart_container = new ItemModHealth(20F, "heart_container").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item heart_booster = new ItemModHealth(40F, "heart_booster").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item heart_fab = new ItemModHealth(60F, "heart_fab").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item black_diamond = new ItemModHealth(40F, "black_diamond").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item wd40 = new ItemModWD40("wd40").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item scrumpy = new ItemModRevive(1, "scrumpy").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item wild_p = new ItemModRevive(3, "wild_p").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item fabsols_vodka = new ItemModRevive(9999, "fabsols_vodka").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item shackles = new ItemModShackles("shackles").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item injector_5htp = new ItemModAuto("injector_5htp").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item injector_knife = new ItemModKnife("injector_knife").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item medal_liquidator = new ItemModMedal("medal_liquidator", 0.5F).setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item bottled_cloud = new ItemModCloud("bottled_cloud").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item servo_set = new ItemModServos("servo_set").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item servo_set_desh = new ItemModServos("servo_set_desh").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item v1 = new ItemModV1("v1").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item neutrino_lens = new ItemModLens("neutrino_lens").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item gas_tester = new ItemModSensor("gas_tester");
    public static final Item defuser_gold = new ItemModDefuser("defuser_gold");
    public static final Item ballistic_gauntlet = new ItemModTwoKick("ballistic_gauntlet");
    public static final Item night_vision = new ItemModNightVision("night_vision").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item card_aos = new ItemModCard("card_aos").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item card_qos = new ItemModCard("card_qos").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item protection_charm = new ItemModCharm("protection_charm").setCreativeTab(MainRegistry.consumableTab);
    public static final Item meteor_charm = new ItemModCharm("meteor_charm").setCreativeTab(MainRegistry.consumableTab);
    //Stamps
    public static final Item stamp_stone_flat = new ItemStamp("stamp_stone_flat", 32, ItemStamp.StampType.FLAT);
    public static final Item stamp_stone_plate = new ItemStamp("stamp_stone_plate", 32, ItemStamp.StampType.PLATE);
    public static final Item stamp_stone_wire = new ItemStamp("stamp_stone_wire", 32, ItemStamp.StampType.WIRE);
    public static final Item stamp_stone_circuit = new ItemStamp("stamp_stone_circuit", 32, ItemStamp.StampType.CIRCUIT);
    public static final Item stamp_iron_flat = new ItemStamp("stamp_iron_flat", 64, ItemStamp.StampType.FLAT);
    public static final Item stamp_iron_plate = new ItemStamp("stamp_iron_plate", 64, ItemStamp.StampType.PLATE);
    public static final Item stamp_iron_wire = new ItemStamp("stamp_iron_wire", 64, ItemStamp.StampType.WIRE);
    public static final Item stamp_iron_circuit = new ItemStamp("stamp_iron_circuit", 64, ItemStamp.StampType.CIRCUIT);
    public static final Item stamp_steel_flat = new ItemStamp("stamp_steel_flat", 192, ItemStamp.StampType.FLAT);
    public static final Item stamp_steel_plate = new ItemStamp("stamp_steel_plate", 192, ItemStamp.StampType.PLATE);
    public static final Item stamp_steel_wire = new ItemStamp("stamp_steel_wire", 192, ItemStamp.StampType.WIRE);
    public static final Item stamp_steel_circuit = new ItemStamp("stamp_steel_circuit", 192, ItemStamp.StampType.CIRCUIT);
    public static final Item stamp_titanium_flat = new ItemStamp("stamp_titanium_flat", 256, ItemStamp.StampType.FLAT);
    public static final Item stamp_titanium_plate = new ItemStamp("stamp_titanium_plate", 256, ItemStamp.StampType.PLATE);
    public static final Item stamp_titanium_wire = new ItemStamp("stamp_titanium_wire", 256, ItemStamp.StampType.WIRE);
    public static final Item stamp_titanium_circuit = new ItemStamp("stamp_titanium_circuit", 256, ItemStamp.StampType.CIRCUIT);
    public static final Item stamp_obsidian_flat = new ItemStamp("stamp_obsidian_flat", 512, ItemStamp.StampType.FLAT);
    public static final Item stamp_obsidian_plate = new ItemStamp("stamp_obsidian_plate", 512, ItemStamp.StampType.PLATE);
    public static final Item stamp_obsidian_wire = new ItemStamp("stamp_obsidian_wire", 512, ItemStamp.StampType.WIRE);
    public static final Item stamp_obsidian_circuit = new ItemStamp("stamp_obsidian_circuit", 512, ItemStamp.StampType.CIRCUIT);
    public static final Item stamp_desh_flat = new ItemStamp("stamp_desh_flat", 0, ItemStamp.StampType.FLAT);
    public static final Item stamp_desh_plate = new ItemStamp("stamp_desh_plate", 0, ItemStamp.StampType.PLATE);
    public static final Item stamp_desh_wire = new ItemStamp("stamp_desh_wire", 0, ItemStamp.StampType.WIRE);
    public static final Item stamp_desh_circuit = new ItemStamp("stamp_desh_circuit", 0, ItemStamp.StampType.CIRCUIT);
    public static final Item stamp_desh_357 = new ItemStamp("stamp_desh_357", 0, ItemStamp.StampType.C357);
    public static final Item stamp_desh_44 = new ItemStamp("stamp_desh_44", 0, ItemStamp.StampType.C44);
    public static final Item stamp_desh_9 = new ItemStamp("stamp_desh_9", 0, ItemStamp.StampType.C9);
    public static final Item stamp_desh_50 = new ItemStamp("stamp_desh_50", 0, ItemStamp.StampType.C50);
    public static final Item stamp_357 = new ItemStamp("stamp_357", 1000, ItemStamp.StampType.C357);
    public static final Item stamp_44 = new ItemStamp("stamp_44", 1000, ItemStamp.StampType.C44);
    public static final Item stamp_9 = new ItemStamp("stamp_9", 1000, ItemStamp.StampType.C9);
    public static final Item stamp_50 = new ItemStamp("stamp_50", 1000, ItemStamp.StampType.C50);
    public static final Item stamp_book = new ItemStampBook("stamp_book").setMaxStackSize(1).setCreativeTab(null);
    public static final Item blades_steel = new ItemBlades("blades_steel", 200);
    public static final Item blades_titanium = new ItemBlades("blades_titanium", 350);
    public static final Item blades_advanced_alloy = new ItemBlades("blades_advanced_alloy", 700);
    public static final Item blades_desh = new ItemBlades("blades_desh", 0).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item fuse = new ItemCustomLore("fuse").setMaxStackSize(16).setCreativeTab(MainRegistry.controlTab);
    public static final Item redcoil_capacitor = new ItemCapacitor(10, "redcoil_capacitor").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item euphemium_capacitor = new ItemCustomLore("euphemium_capacitor").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item overfuse = new ItemCustomLore("overfuse").setMaxStackSize(1).setFull3D();
    public static final Item arc_electrode = new ItemArcElectrode("arc_electrode").setCreativeTab(MainRegistry.controlTab);
    public static final Item arc_electrode_burnt = new ItemArcElectrodeBurnt("arc_electrode_burnt").setCreativeTab(MainRegistry.controlTab);
    public static final Item piston_selenium = new ItemBase("piston_selenium").setCreativeTab(MainRegistry.controlTab);
    public static final Item piston_set = new ItemPistons("piston_set").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    public static final Item drillbit = new ItemDrillbit("drillbit").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    public static final Item thermo_element = new ItemBase("thermo_element").setCreativeTab(MainRegistry.controlTab);
    public static final Item catalytic_converter = new ItemBase("catalytic_converter").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item part_lithium = new ItemBase("part_lithium").setCreativeTab(MainRegistry.controlTab);
    public static final Item part_beryllium = new ItemBase("part_beryllium").setCreativeTab(MainRegistry.controlTab);
    public static final Item part_carbon = new ItemBase("part_carbon").setCreativeTab(MainRegistry.controlTab);
    public static final Item part_copper = new ItemBase("part_copper").setCreativeTab(MainRegistry.controlTab);
    public static final Item part_plutonium = new ItemBase("part_plutonium").setCreativeTab(MainRegistry.controlTab);
    //Reactor Blanket
    public static final Item fusion_shield_tungsten = new ItemFusionShield(20 * 60 * 60 * 6, 35000, "fusion_shield_tungsten").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item fusion_shield_desh = new ItemFusionShield(20 * 60 * 60 * 12, 60000, "fusion_shield_desh").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item fusion_shield_chlorophyte = new ItemFusionShield(20 * 60 * 60 * 24, 90000, "fusion_shield_chlorophyte").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item fusion_shield_vaporwave = new ItemFusionShield(20 * 60 * 60 * 24 * 7, 1916169, "fusion_shield_vaporwave").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_sc_uranium = new ItemSelfcharger(5, "battery_sc_uranium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_sc_technetium = new ItemSelfcharger(25, "battery_sc_technetium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_sc_plutonium = new ItemSelfcharger(100, "battery_sc_plutonium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_sc_polonium = new ItemSelfcharger(500, "battery_sc_polonium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_sc_gold = new ItemSelfcharger(2500, "battery_sc_gold").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_sc_lead = new ItemSelfcharger(5000, "battery_sc_lead").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_sc_americium = new ItemSelfcharger(10000, "battery_sc_americium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_creative = new ItemSelfcharger(Long.MAX_VALUE, "battery_creative").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    //Energy items
    public static final Item battery_generic = new ItemBattery(5000, 100, 100, "battery_generic").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_red_cell = new ItemBattery(15000, 100, 100, "battery_red_cell").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_red_cell_6 = new ItemBattery(15000 * 6, 100, 100, "battery_red_cell_6").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_red_cell_24 = new ItemBattery(15000 * 24, 100, 100, "battery_red_cell_24").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_advanced = new ItemBattery(20000, 500, 500, "battery_advanced").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_advanced_cell = new ItemBattery(60000, 500, 500, "battery_advanced_cell").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_advanced_cell_4 = new ItemBattery(60000 * 4, 500, 500, "battery_advanced_cell_4").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_advanced_cell_12 = new ItemBattery(60000 * 12, 500, 500, "battery_advanced_cell_12").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_lithium = new ItemBattery(250000, 1000, 1000, "battery_lithium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_lithium_cell = new ItemBattery(750000, 1000, 1000, "battery_lithium_cell").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_lithium_cell_3 = new ItemBattery(750000 * 3, 1000, 1000, "battery_lithium_cell_3").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_lithium_cell_6 = new ItemBattery(750000 * 6, 1000, 1000, "battery_lithium_cell_6").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_schrabidium = new ItemBattery(1000000, 5000, 5000, "battery_schrabidium").setMaxStackSize(1);
    public static final Item battery_schrabidium_cell = new ItemBattery(3000000, 15000, 15000, "battery_schrabidium_cell").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_schrabidium_cell_2 = new ItemBattery(3000000 * 2, 30000, 30000, "battery_schrabidium_cell_2").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_schrabidium_cell_4 = new ItemBattery(3000000 * 4, 60000, 60000, "battery_schrabidium_cell_4").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_trixite = new ItemBattery(5000000, 40000, 200000, "battery_trixite").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_spark = new ItemBattery(100000000, 2000000, 2000000, "battery_spark").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_spark_cell_6 = new ItemBattery(100000000 * 6, 2000000, 2000000, "battery_spark_cell_6").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_spark_cell_25 = new ItemBattery(100000000L * 25L, 2000000, 2000000, "battery_spark_cell_25").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_spark_cell_100 = new ItemBattery(1000000000L * 10L, 1000000L * 5L, 1000000L * 5L, "battery_spark_cell_100").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_spark_cell_1000 = new ItemBattery(1000000000L * 100L, 10000000L * 5L, 10000000L * 5L, "battery_spark_cell_1000").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_spark_cell_2500 = new ItemBattery(1000000000L * 250L, 100000000L * 5L, 100000000L * 5L, "battery_spark_cell_2500").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_spark_cell_10000 = new ItemBattery(1000000000L * 1000L, 1000000000L * 5L, 1000000000L * 5L, "battery_spark_cell_10000").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_spark_cell_power = new ItemBattery(1000000000L * 100000L, 1000000000L * 500L, 1000000000L * 500L, "battery_spark_cell_power").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_potato = new ItemBattery(100, 0, 100, "battery_potato").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_potatos = new ItemPotatos(5000, 0, 100, "battery_potatos").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_su = new ItemBattery(1500, 0, 100, "battery_su").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_su_l = new ItemBattery(3500, 0, 100, "battery_su_l").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_steam = new ItemBattery(60000, 3, 6000, "battery_steam").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item battery_steam_large = new ItemBattery(100000, 5, 10000, "battery_steam_large").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item hev_battery = new ItemFusionCore(150000, "hev_battery").setMaxStackSize(4).setCreativeTab(MainRegistry.controlTab);
    public static final Item fusion_core = new ItemFusionCore(2500000, "fusion_core").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item energy_core = new ItemBattery(10000000, 0, 1000, "energy_core").setMaxStackSize(1);
    public static final Item fusion_core_infinite = new ItemBase("fusion_core_infinite").setMaxStackSize(1);
    public static final Item laser_crystal_co2 = new ItemFELCrystal(EnumWavelengths.IR, "laser_crystal_co2").setCreativeTab(MainRegistry.controlTab);
    public static final Item laser_crystal_bismuth = new ItemFELCrystal(EnumWavelengths.VISIBLE, "laser_crystal_bismuth").setCreativeTab(MainRegistry.controlTab);
    public static final Item laser_crystal_cmb = new ItemFELCrystal(EnumWavelengths.UV, "laser_crystal_cmb").setCreativeTab(MainRegistry.controlTab);
    public static final Item laser_crystal_bale = new ItemFELCrystal(EnumWavelengths.GAMMA, "laser_crystal_bale").setCreativeTab(MainRegistry.controlTab);
    public static final Item laser_crystal_digamma = new ItemFELCrystal(EnumWavelengths.DRX, "laser_crystal_digamma").setCreativeTab(MainRegistry.controlTab);
    //Upgrade
    public static final Item upgrade_speed_1 = new ItemMachineUpgrade("upgrade_speed_1", UpgradeType.SPEED, 1).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_speed_2 = new ItemMachineUpgrade("upgrade_speed_2", UpgradeType.SPEED, 2).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_speed_3 = new ItemMachineUpgrade("upgrade_speed_3", UpgradeType.SPEED, 3).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_effect_1 = new ItemMachineUpgrade("upgrade_effect_1", UpgradeType.EFFECT, 1).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_effect_2 = new ItemMachineUpgrade("upgrade_effect_2", UpgradeType.EFFECT, 2).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_effect_3 = new ItemMachineUpgrade("upgrade_effect_3", UpgradeType.EFFECT, 3).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_power_1 = new ItemMachineUpgrade("upgrade_power_1", UpgradeType.POWER, 1).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_power_2 = new ItemMachineUpgrade("upgrade_power_2", UpgradeType.POWER, 2).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_power_3 = new ItemMachineUpgrade("upgrade_power_3", UpgradeType.POWER, 3).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_fortune_1 = new ItemMachineUpgrade("upgrade_fortune_1", UpgradeType.FORTUNE, 1).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_fortune_2 = new ItemMachineUpgrade("upgrade_fortune_2", UpgradeType.FORTUNE, 2).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_fortune_3 = new ItemMachineUpgrade("upgrade_fortune_3", UpgradeType.FORTUNE, 3).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_afterburn_1 = new ItemMachineUpgrade("upgrade_afterburn_1", UpgradeType.AFTERBURN, 1).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_afterburn_2 = new ItemMachineUpgrade("upgrade_afterburn_2", UpgradeType.AFTERBURN, 2).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_afterburn_3 = new ItemMachineUpgrade("upgrade_afterburn_3", UpgradeType.AFTERBURN, 3).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_radius = new ItemMachineUpgrade("upgrade_radius").setMaxStackSize(16).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_health = new ItemMachineUpgrade("upgrade_health").setMaxStackSize(16).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_overdrive_1 = new ItemMachineUpgrade("upgrade_overdrive_1", UpgradeType.OVERDRIVE, 1).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_overdrive_2 = new ItemMachineUpgrade("upgrade_overdrive_2", UpgradeType.OVERDRIVE, 2).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_overdrive_3 = new ItemMachineUpgrade("upgrade_overdrive_3", UpgradeType.OVERDRIVE, 3).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_smelter = new ItemMachineUpgrade("upgrade_smelter").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_shredder = new ItemMachineUpgrade("upgrade_shredder").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_centrifuge = new ItemMachineUpgrade("upgrade_centrifuge").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_crystallizer = new ItemMachineUpgrade("upgrade_crystallizer").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_nullifier = new ItemMachineUpgrade("upgrade_nullifier", UpgradeType.NULLIFIER, 1).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_screm = new ItemMachineUpgrade("upgrade_screm", UpgradeType.SCREAM, 1).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_gc_speed = new ItemMachineUpgrade("upgrade_gc_speed").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_5g = new ItemMachineUpgrade("upgrade_5g", UpgradeType.SPECIAL).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_ejector_1 = new ItemMachineUpgrade("upgrade_ejector_1").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_ejector_2 = new ItemMachineUpgrade("upgrade_ejector_2").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_ejector_3 = new ItemMachineUpgrade("upgrade_ejector_3").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_stack_1 = new ItemMachineUpgrade("upgrade_stack_1", UpgradeType.SPEED, 1).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_stack_2 = new ItemMachineUpgrade("upgrade_stack_2", UpgradeType.SPEED, 1).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item upgrade_stack_3 = new ItemMachineUpgrade("upgrade_stack_3", UpgradeType.SPEED, 1).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    //Fluid handling items
    public static final Item fuel_additive = new ItemEnumMulti("fuel_additive", ItemEnums.EnumFuelAdditive.class, true, true).setCreativeTab(MainRegistry.controlTab);

    public static final Item canister_empty = new ItemBakedBase("canister_empty").setCreativeTab(MainRegistry.controlTab);
    public static final Item canister_full = new ItemCanister("canister_fuel", 1000).setCreativeTab(MainRegistry.controlTab);
    public static final Item canister_napalm = new ItemCustomLore("canister_napalm").setCreativeTab(MainRegistry.controlTab);
    public static final Item gas_empty = new ItemBase("gas_empty").setCreativeTab(MainRegistry.controlTab);
    public static final Item gas_full = new ItemGasCanister("gas_full").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.gas_empty);
    /** our cells are meta-sensitive. If you need empty cells in recipes, use <code>new ItemStack(ModItems.cell)</code> instead of <code>ModItems.cell</code> */
    public static final Item cell = new ItemCell("cell").setCreativeTab(MainRegistry.controlTab);

    // Th3_Sl1ze: welp, technically balefire is not a fluid, so I guess I have to make it a separate item?..
    public static final Item cell_balefire = new ItemBakedBase("cell_balefire").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell);

    public static final Item fluid_tank_empty = new ItemBakedBase("fluid_tank_empty").setCreativeTab(MainRegistry.controlTab);
    public static final Item fluid_tank_full = new ItemFluidTank("fluid_tank_full", 1000).setCreativeTab(MainRegistry.controlTab);
    public static final Item fluid_tank_lead_empty = new ItemBakedBase("fluid_tank_lead_empty").setCreativeTab(MainRegistry.controlTab);
    public static final Item fluid_tank_lead_full = new ItemFluidTank("fluid_tank_lead_full", 1000).setCreativeTab(MainRegistry.controlTab);
    public static final Item fluid_barrel_empty = new ItemBakedBase("fluid_barrel_empty").setCreativeTab(MainRegistry.controlTab);
    public static final Item fluid_barrel_full = new ItemFluidTank("fluid_barrel_full", 16000).setCreativeTab(MainRegistry.controlTab);
    public static final Item fluid_barrel_infinite = new ItemFluidContainerInfinite(null, 1_000_000_000, "fluid_barrel_infinite").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item fluid_pack_empty = new ItemBakedBase("fluid_pack_empty").setCreativeTab(MainRegistry.controlTab);
    public static final Item fluid_pack_full = new ItemFluidTank("fluid_pack_full", 32000).setContainerItem(ModItems.fluid_pack_empty).setCreativeTab(MainRegistry.controlTab);
    public static final Item pipette = new ItemPipette("pipette").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item pipette_boron = new ItemPipette("pipette_boron").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item pipette_laboratory = new ItemPipette("pipette_laboratory").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item siphon = new ItemFluidSiphon("siphon").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item inf_water = new ItemFluidContainerInfinite(Fluids.WATER, 50, "inf_water").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item inf_water_mk2 = new ItemFluidContainerInfinite(Fluids.WATER, 500, "inf_water_mk2").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);

    public static final Item disperser_canister_empty = new ItemBase("disperser_canister_empty").setCreativeTab(MainRegistry.weaponTab);
    public static final Item disperser_canister = new ItemDisperser("disperser_canister", 2000).setContainerItem(ModItems.disperser_canister_empty).setCreativeTab(MainRegistry.weaponTab);

    public static final Item glyphid_gland_empty = new ItemBase("glyphid_gland_empty").setCreativeTab(MainRegistry.weaponTab);
    public static final Item glyphid_gland = new ItemDisperser("glyphid_gland", 4000).setContainerItem(ModItems.glyphid_gland_empty).setCreativeTab(MainRegistry.weaponTab);

    //Activators
    public static final Item detonator = new ItemDetonator("detonator").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab);
    public static final Item detonator_multi = new ItemMultiDetonator("detonator_multi").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab);
    public static final Item detonator_laser = new ItemLaserDetonator("detonator_laser").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab);
    public static final Item detonator_deadman = new ItemDrop("detonator_deadman").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab);
    public static final Item detonator_de = new ItemDrop("detonator_de").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab);
    public static final Item igniter = new ItemCustomLore("igniter", "trigger").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab);
    public static final Item spawn_chopper = new ItemChopper("chopper").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item spawn_worm = new ItemChopper("spawn_worm").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item spawn_ufo = new ItemChopper("spawn_ufo").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item bomb_caller = new ItemBombCaller("bomb_caller").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.consumableTab);
    public static final Item crate_caller = new ItemCrateCaller("crate_caller").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.consumableTab);
    public static final Item meteor_remote = new ItemMeteorRemote("meteor_remote").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.consumableTab);
    public static final Item anchor_remote = new ItemAnchorRemote("anchor_remote").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.consumableTab);
    //Armor
    public static final Item hazmat_helmet = new ArmorGasMask(MainRegistry.aMatHaz, -1, EntityEquipmentSlot.HEAD, "hazmat_helmet").setMaxStackSize(1);
    public static final Item hazmat_plate = new ArmorHazmat(MainRegistry.aMatHaz, -1, EntityEquipmentSlot.CHEST, "hazmat_plate").setMaxStackSize(1);
    public static final Item hazmat_legs = new ArmorHazmat(MainRegistry.aMatHaz, -1, EntityEquipmentSlot.LEGS, "hazmat_legs").setMaxStackSize(1);
    public static final Item hazmat_boots = new ArmorHazmat(MainRegistry.aMatHaz, -1, EntityEquipmentSlot.FEET, "hazmat_boots").setMaxStackSize(1);
    public static final Item hazmat_helmet_red = new ArmorGasMask(MainRegistry.aMatHaz2, -1, EntityEquipmentSlot.HEAD, "hazmat_helmet_red").setMaxStackSize(1);
    public static final Item hazmat_plate_red = new ArmorHazmat(MainRegistry.aMatHaz2, -1, EntityEquipmentSlot.CHEST, "hazmat_plate_red").setMaxStackSize(1);
    public static final Item hazmat_legs_red = new ArmorHazmat(MainRegistry.aMatHaz2, -1, EntityEquipmentSlot.LEGS, "hazmat_legs_red").setMaxStackSize(1);
    public static final Item hazmat_boots_red = new ArmorHazmat(MainRegistry.aMatHaz2, -1, EntityEquipmentSlot.FEET, "hazmat_boots_red").setMaxStackSize(1);
    public static final Item hazmat_helmet_grey = new ArmorGasMask(MainRegistry.aMatHaz3, -1, EntityEquipmentSlot.HEAD, "hazmat_helmet_grey").setMaxStackSize(1);
    public static final Item hazmat_plate_grey = new ArmorHazmat(MainRegistry.aMatHaz3, -1, EntityEquipmentSlot.CHEST, "hazmat_plate_grey").setMaxStackSize(1);
    public static final Item hazmat_legs_grey = new ArmorHazmat(MainRegistry.aMatHaz3, -1, EntityEquipmentSlot.LEGS, "hazmat_legs_grey").setMaxStackSize(1);
    public static final Item hazmat_boots_grey = new ArmorHazmat(MainRegistry.aMatHaz3, -1, EntityEquipmentSlot.FEET, "hazmat_boots_grey").setMaxStackSize(1);
    public static final Item liquidator_helmet = new ArmorLiquidator(MainRegistry.aMatLiquidator, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/liquidator_helmet.png", "liquidator_helmet")
            .setStep(HBMSoundHandler.iron)
            .setJump(HBMSoundHandler.ironJump)
            .setFall(HBMSoundHandler.ironLand).setMaxStackSize(1);
    public static final Item liquidator_plate = new ArmorLiquidator(MainRegistry.aMatLiquidator, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/liquidator_1.png", "liquidator_plate").cloneStats((ArmorFSB) liquidator_helmet).setMaxStackSize(1);
    public static final Item liquidator_legs = new ArmorLiquidator(MainRegistry.aMatLiquidator, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/liquidator_2.png", "liquidator_legs").cloneStats((ArmorFSB) liquidator_helmet).setMaxStackSize(1);
    public static final Item liquidator_boots = new ArmorLiquidator(MainRegistry.aMatLiquidator, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/liquidator_1.png", "liquidator_boots").cloneStats((ArmorFSB) liquidator_helmet).setMaxStackSize(1);
    public static final Item hazmat_paa_helmet = new ArmorGasMask(MainRegistry.aMatPaa, -1, EntityEquipmentSlot.HEAD, "hazmat_paa_helmet").setMaxStackSize(1);
    public static final Item hazmat_paa_plate = new ArmorHazmat(MainRegistry.aMatPaa, -1, EntityEquipmentSlot.CHEST, "hazmat_paa_plate").setMaxStackSize(1);
    public static final Item hazmat_paa_legs = new ArmorHazmat(MainRegistry.aMatPaa, -1, EntityEquipmentSlot.LEGS, "hazmat_paa_legs").setMaxStackSize(1);
    public static final Item hazmat_paa_boots = new ArmorHazmat(MainRegistry.aMatPaa, -1, EntityEquipmentSlot.FEET, "hazmat_paa_boots").setMaxStackSize(1);
    public static final Item paa_helmet = new ArmorFSB(MainRegistry.aMatPaa, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/paa_1.png", "paa_helmet").addEffect(new PotionEffect(MobEffects.HASTE, 30, 0)).addEffect(new PotionEffect(MobEffects.SPEED, 30, 0)).addEffect(new PotionEffect(MobEffects.WATER_BREATHING, 30, 0)).setMaxStackSize(1);
    public static final Item paa_plate = new ArmorFSB(MainRegistry.aMatPaa, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/paa_1.png", "paa_plate").cloneStats((ArmorFSB) paa_helmet).setMaxStackSize(1);
    public static final Item paa_legs = new ArmorFSB(MainRegistry.aMatPaa, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/paa_2.png", "paa_legs").cloneStats((ArmorFSB) paa_helmet).setMaxStackSize(1);
    public static final Item paa_boots = new ArmorFSB(MainRegistry.aMatPaa, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/paa_1.png", "paa_boots").cloneStats((ArmorFSB) paa_helmet).setMaxStackSize(1);
    public static final Item australium_iii = new ItemModShield(25F, "australium_iii").setMaxStackSize(1);
    public static final Item armor_battery = new ItemModBattery(1.25D, "armor_battery");
    public static final Item armor_battery_mk2 = new ItemModBattery(1.5D, "armor_battery_mk2");
    public static final Item armor_battery_mk3 = new ItemModBattery(2D, "armor_battery_mk3");
    public static final Item asbestos_helmet = new ArmorFSB(MainRegistry.aMatAsbestos, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/asbestos_1.png", "asbestos_helmet").setOverlay(Tags.MODID + ":textures/misc/overlay_asbestos.png").setMaxStackSize(1);
    public static final Item asbestos_plate = new ArmorFSB(MainRegistry.aMatAsbestos, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/asbestos_1.png", "asbestos_plate").setMaxStackSize(1);
    public static final Item asbestos_legs = new ArmorFSB(MainRegistry.aMatAsbestos, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/asbestos_2.png", "asbestos_legs").setMaxStackSize(1);
    public static final Item asbestos_boots = new ArmorFSB(MainRegistry.aMatAsbestos, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/asbestos_1.png", "asbestos_boots").setMaxStackSize(1);
    public static final Item euphemium_helmet = new ArmorEuphemium(MainRegistry.aMatEuph, -1, EntityEquipmentSlot.HEAD, "euphemium_helmet").setMaxStackSize(1);
    public static final Item euphemium_plate = new ArmorEuphemium(MainRegistry.aMatEuph, -1, EntityEquipmentSlot.CHEST, "euphemium_plate").setMaxStackSize(1);
    public static final Item euphemium_legs = new ArmorEuphemium(MainRegistry.aMatEuph, -1, EntityEquipmentSlot.LEGS, "euphemium_legs").setMaxStackSize(1);
    public static final Item euphemium_boots = new ArmorEuphemium(MainRegistry.aMatEuph, -1, EntityEquipmentSlot.FEET, "euphemium_boots").setMaxStackSize(1);
    public static final Item jackt = new ModArmor(MainRegistry.aMatSteel, -1, EntityEquipmentSlot.CHEST, "jackt").setMaxStackSize(1);
    public static final Item jackt2 = new ModArmor(MainRegistry.aMatSteel, -1, EntityEquipmentSlot.CHEST, "jackt2").setMaxStackSize(1);
    // TODO: item renderer for chainsaw
    public static final Item chainsaw = new ItemChainsaw("chainsaw", 25, -2.8F, -0.05, MainRegistry.enumToolMaterialChainsaw, EnumToolType.AXE, 5000, 1, 250,
            Fluids.DIESEL, Fluids.DIESEL_CRACK, Fluids.KEROSENE, Fluids.BIOFUEL, Fluids.GASOLINE, Fluids.GASOLINE_LEADED, Fluids.PETROIL, Fluids.PETROIL_LEADED, Fluids.COALGAS, Fluids.COALGAS_LEADED)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolAreaAbility.RECURSION, 2)
            .addAbility(IWeaponAbility.CHAINSAW, 1)
            .addAbility(IWeaponAbility.BEHEADER, 0).setShears();
    public static final Item steel_helmet = new ArmorFSB(MainRegistry.aMatSteel, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/steel_1.png", "steel_helmet").setMaxStackSize(1);
    public static final Item steel_plate = new ArmorFSB(MainRegistry.aMatSteel, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/steel_1.png", "steel_plate").cloneStats((ArmorFSB) steel_helmet).setMaxStackSize(1);
    public static final Item steel_legs = new ArmorFSB(MainRegistry.aMatSteel, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/steel_2.png", "steel_legs").cloneStats((ArmorFSB) steel_helmet).setMaxStackSize(1);
    public static final Item steel_boots = new ArmorFSB(MainRegistry.aMatSteel, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/steel_1.png", "steel_boots").cloneStats((ArmorFSB) steel_helmet).setMaxStackSize(1);
    public static final Item titanium_helmet = new ArmorFSB(MainRegistry.aMatTitan, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/titanium_1.png", "titanium_helmet").setMaxStackSize(1);
    public static final Item titanium_plate = new ArmorFSB(MainRegistry.aMatTitan, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/titanium_1.png", "titanium_plate").cloneStats((ArmorFSB) titanium_helmet).setMaxStackSize(1);
    public static final Item titanium_legs = new ArmorFSB(MainRegistry.aMatTitan, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/titanium_2.png", "titanium_legs").cloneStats((ArmorFSB) titanium_helmet).setMaxStackSize(1);
    public static final Item titanium_boots = new ArmorFSB(MainRegistry.aMatTitan, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/titanium_1.png", "titanium_boots").cloneStats((ArmorFSB) titanium_helmet).setMaxStackSize(1);
    public static final Item alloy_helmet = new ArmorFSB(MainRegistry.aMatAlloy, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/alloy_1.png", "alloy_helmet").setMaxStackSize(1);
    public static final Item alloy_plate = new ArmorFSB(MainRegistry.aMatAlloy, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/alloy_1.png", "alloy_plate").cloneStats((ArmorFSB) alloy_helmet).setMaxStackSize(1);
    public static final Item alloy_legs = new ArmorFSB(MainRegistry.aMatAlloy, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/alloy_2.png", "alloy_legs").cloneStats((ArmorFSB) alloy_helmet).setMaxStackSize(1);
    public static final Item alloy_boots = new ArmorFSB(MainRegistry.aMatAlloy, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/alloy_1.png", "alloy_boots").cloneStats((ArmorFSB) alloy_helmet).setMaxStackSize(1);
    public static final Item cobalt_helmet = new ArmorFSB(MainRegistry.aMatCobalt, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/cobalt_1.png", "cobalt_helmet");
    public static final Item cobalt_plate = new ArmorFSB(MainRegistry.aMatCobalt, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/cobalt_1.png", "cobalt_plate").cloneStats((ArmorFSB) cobalt_helmet).setMaxStackSize(1);
    public static final Item cobalt_legs = new ArmorFSB(MainRegistry.aMatCobalt, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/cobalt_2.png", "cobalt_legs").cloneStats((ArmorFSB) cobalt_helmet).setMaxStackSize(1);
    public static final Item cobalt_boots = new ArmorFSB(MainRegistry.aMatCobalt, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/cobalt_1.png", "cobalt_boots").cloneStats((ArmorFSB) cobalt_helmet).setMaxStackSize(1);
    public static final Item security_helmet = new ArmorFSB(MainRegistry.aMatSecurity, 7, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/security_1.png", "security_helmet").setMaxStackSize(1);
    public static final Item security_plate = new ArmorFSB(MainRegistry.aMatSecurity, 7, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/security_1.png", "security_plate").cloneStats((ArmorFSB) security_helmet).setMaxStackSize(1);
    public static final Item security_legs = new ArmorFSB(MainRegistry.aMatSecurity, 7, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/security_2.png", "security_legs").cloneStats((ArmorFSB) security_helmet).setMaxStackSize(1);
    public static final Item security_boots = new ArmorFSB(MainRegistry.aMatSecurity, 7, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/security_1.png", "security_boots").cloneStats((ArmorFSB) security_helmet).setMaxStackSize(1);
    public static final Item starmetal_helmet = new ArmorFSB(MainRegistry.aMatStarmetal, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/starmetal_1.png", "starmetal_helmet");
    public static final Item starmetal_plate = new ArmorFSB(MainRegistry.aMatStarmetal, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/starmetal_1.png", "starmetal_plate").cloneStats((ArmorFSB) starmetal_helmet);
    public static final Item starmetal_legs = new ArmorFSB(MainRegistry.aMatStarmetal, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/starmetal_2.png", "starmetal_legs").cloneStats((ArmorFSB) starmetal_helmet);
    public static final Item starmetal_boots = new ArmorFSB(MainRegistry.aMatStarmetal, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/starmetal_1.png", "starmetal_boots").cloneStats((ArmorFSB) starmetal_helmet);
    public static final Item robes_helmet = new ArmorFSB(MainRegistry.aMatSteel, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/robes_1.png", "robes_helmet");
    public static final Item robes_plate = new ArmorFSB(MainRegistry.aMatSteel, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/robes_1.png", "robes_plate");
    public static final Item robes_legs = new ArmorFSB(MainRegistry.aMatSteel, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/robes_2.png", "robes_legs");
    public static final Item robes_boots = new ArmorFSB(MainRegistry.aMatSteel, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/robes_1.png", "robes_boots");
    public static final Item zirconium_legs = new ArmorFSB(MainRegistry.aMatZirconium, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/zirconium_2.png", "zirconium_legs");
    public static final Item dnt_helmet = new ArmorFSB(MainRegistry.aMatDNT, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/dnt_1.png", "dnt_helmet");
    public static final Item dnt_plate = new ArmorFSB(MainRegistry.aMatDNT, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/dnt_1.png", "dnt_plate").cloneStats((ArmorFSB) dnt_helmet);
    public static final Item dnt_legs = new ArmorFSB(MainRegistry.aMatDNT, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/dnt_2.png", "dnt_legs").cloneStats((ArmorFSB) dnt_helmet);
    public static final Item dnt_boots = new ArmorFSB(MainRegistry.aMatDNT, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/dnt_1.png", "dnt_boots").cloneStats((ArmorFSB) dnt_helmet);
    public static final Item cmb_helmet = new ArmorFSB(MainRegistry.aMatCMB, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/cmb_1.png", "cmb_helmet")
            .addEffect(new PotionEffect(MobEffects.SPEED, 30, 2))
            .addEffect(new PotionEffect(MobEffects.HASTE, 30, 0))
            .addEffect(new PotionEffect(MobEffects.STRENGTH, 30, 0));
    public static final Item cmb_plate = new ArmorFSB(MainRegistry.aMatCMB, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/cmb_1.png", "cmb_plate").cloneStats((ArmorFSB) cmb_helmet).setMaxStackSize(1);
    public static final Item cmb_legs = new ArmorFSB(MainRegistry.aMatCMB, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/cmb_2.png", "cmb_legs").cloneStats((ArmorFSB) cmb_helmet).setMaxStackSize(1);
    public static final Item cmb_boots = new ArmorFSB(MainRegistry.aMatCMB, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/cmb_1.png", "cmb_boots").cloneStats((ArmorFSB) cmb_helmet).setMaxStackSize(1);
    public static final Item schrabidium_helmet = new ArmorFSB(MainRegistry.aMatSchrab, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/schrabidium_1.png", "schrabidium_helmet")
            .addEffect(new PotionEffect(MobEffects.HASTE, 20, 2))
            .addEffect(new PotionEffect(MobEffects.STRENGTH, 20, 2))
            .addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 20, 1))
            .addEffect(new PotionEffect(MobEffects.SPEED, 20, 2));
    public static final Item schrabidium_plate = new ArmorFSB(MainRegistry.aMatSchrab, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/schrabidium_1.png", "schrabidium_plate").cloneStats((ArmorFSB) schrabidium_helmet).setMaxStackSize(1);
    public static final Item schrabidium_legs = new ArmorFSB(MainRegistry.aMatSchrab, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/schrabidium_2.png", "schrabidium_legs").cloneStats((ArmorFSB) schrabidium_helmet).setMaxStackSize(1);
    public static final Item schrabidium_boots = new ArmorFSB(MainRegistry.aMatSchrab, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/schrabidium_1.png", "schrabidium_boots").cloneStats((ArmorFSB) schrabidium_helmet).setMaxStackSize(1);
    public static final Item t51_helmet = new ArmorT51(MainRegistry.enumArmorMaterialT51, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/starmetal_1.png", 1000000, 10000, 1000, 5, "t51_helmet")
            .enableVATS(true)
            .setHasGeigerSound(true)
            .setHasHardLanding(true)
            .addEffect(new PotionEffect(MobEffects.STRENGTH, 20, 0))
            .setStep(HBMSoundHandler.iron)
            .setJump(HBMSoundHandler.ironJump)
            .setFall(HBMSoundHandler.ironLand);
    public static final Item t51_plate = new ArmorT51(MainRegistry.enumArmorMaterialT51, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/starmetal_1.png", 1000000, 10000, 1000, 5, "t51_plate").cloneStats((ArmorFSB) t51_helmet).setCreativeTab(CreativeTabs.COMBAT);
    public static final Item t51_legs = new ArmorT51(MainRegistry.enumArmorMaterialT51, -1, EntityEquipmentSlot.LEGS, ":textures/armor/starmetal_2.png",1000000, 10000, 1000, 5, "t51_legs").cloneStats((ArmorFSB) t51_helmet).setCreativeTab(CreativeTabs.COMBAT);
    public static final Item t51_boots = new ArmorT51(MainRegistry.enumArmorMaterialT51, -1, EntityEquipmentSlot.FEET, ":textures/armor/starmetal_1.png",1000000, 10000, 1000, 5, "t51_boots").cloneStats((ArmorFSB) t51_helmet).setCreativeTab(CreativeTabs.COMBAT);
    public static final Item steamsuit_helmet = new ArmorDesh(MainRegistry.aMatSteamsuit, 7, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/starmetal_1.png", Fluids.STEAM, 64000, 500, 50, 1, "steamsuit_helmet")
            .addEffect(new PotionEffect(MobEffects.HASTE, 30, 0))
            .setHasHardLanding(true)
            .setStep(HBMSoundHandler.iron) //Norwood: Upstream doesn't have it but i'll allow it
            .setJump(HBMSoundHandler.ironJump)
            .setFall(HBMSoundHandler.ironLand);
    public static final Item steamsuit_plate = new ArmorDesh(MainRegistry.aMatSteamsuit, 7, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/starmetal_1.png", Fluids.STEAM, 64000, 500, 50, 1, "steamsuit_plate").cloneStats((ArmorFSB) steamsuit_helmet);
    public static final Item steamsuit_legs = new ArmorDesh(MainRegistry.aMatSteamsuit, 7, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/starmetal_2.png", Fluids.STEAM, 64000, 500, 50, 1, "steamsuit_legs").cloneStats((ArmorFSB) steamsuit_helmet);
    public static final Item steamsuit_boots = new ArmorDesh(MainRegistry.aMatSteamsuit, 7, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/starmetal_1.png", Fluids.STEAM, 64000, 500, 50, 1, "steamsuit_boots").cloneStats((ArmorFSB) steamsuit_helmet);
    public static final Item trenchmaster_helmet = new ArmorTrenchmaster(MainRegistry.aMatTrench, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/starmetal_1.png", "trenchmaster_helmet")
            .addEffect(new PotionEffect(MobEffects.HASTE, 20, 1))
            .addEffect(new PotionEffect(MobEffects.STRENGTH, 20, 2))
            .addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 20, 1))
            .addEffect(new PotionEffect(MobEffects.SPEED, 20, 0));
    public static final Item trenchmaster_plate = new ArmorTrenchmaster(MainRegistry.aMatTrench, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/starmetal_1.png", "trenchmaster_plate").cloneStats((ArmorFSB) trenchmaster_helmet).setMaxStackSize(1);
    public static final Item trenchmaster_legs = new ArmorTrenchmaster(MainRegistry.aMatTrench, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/starmetal_2.png", "trenchmaster_legs").cloneStats((ArmorFSB) trenchmaster_helmet).setMaxStackSize(1);
    public static final Item trenchmaster_boots = new ArmorTrenchmaster(MainRegistry.aMatTrench, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/starmetal_1.png", "trenchmaster_boots").cloneStats((ArmorFSB) trenchmaster_helmet).setMaxStackSize(1);
    public static final Item taurun_helmet = new ArmorTaurun(MainRegistry.aMatTaurun, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/starmetal_1.png", "taurun_helmet")
            .addEffect(new PotionEffect(MobEffects.STRENGTH, 20, 0));
    public static final Item taurun_plate = new ArmorTaurun(MainRegistry.aMatTaurun, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/starmetal_1.png", "taurun_plate").cloneStats((ArmorFSB) taurun_helmet).setMaxStackSize(1);
    public static final Item taurun_legs = new ArmorTaurun(MainRegistry.aMatTaurun, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/starmetal_2.png", "taurun_legs").cloneStats((ArmorFSB) taurun_helmet).setMaxStackSize(1);

    public static final Item taurun_boots = new ArmorTaurun(MainRegistry.aMatTaurun, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/starmetal_1.png", "taurun_boots").cloneStats((ArmorFSB) taurun_helmet).setMaxStackSize(1);
    public static final Item bismuth_helmet = new ArmorBismuth(MainRegistry.aMatBismuth, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/starmetal_1.png", "bismuth_helmet")
            .addEffect(new PotionEffect(MobEffects.NIGHT_VISION, 20, 0))
            .addEffect(new PotionEffect(MobEffects.REGENERATION, 20, 1))
            .addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 20, 0))
            .addEffect(new PotionEffect(MobEffects.SPEED, 20, 0))
            .setDashCount(3);
    public static final Item bismuth_plate = new ArmorBismuth(MainRegistry.aMatBismuth, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/starmetal_1.png", "bismuth_plate").cloneStats((ArmorFSB) bismuth_helmet).setMaxStackSize(1);
    public static final Item bismuth_legs = new ArmorBismuth(MainRegistry.aMatBismuth, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/starmetal_2.png", "bismuth_legs").cloneStats((ArmorFSB) bismuth_helmet).setMaxStackSize(1);
    public static final Item bismuth_boots = new ArmorBismuth(MainRegistry.aMatBismuth, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/starmetal_1.png", "bismuth_boots").cloneStats((ArmorFSB) bismuth_helmet).setMaxStackSize(1);
    public static final Item envsuit_helmet = new ArmorEnvsuit(MainRegistry.aMatEnvsuit, 7, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/starmetal_1.png", 100000, 1000, 250, 0, "envsuit_helmet")
            .addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 30, 0))
            .addEffect(new PotionEffect(MobEffects.SPEED, 30, 1));
    public static final Item envsuit_plate = new ArmorEnvsuit(MainRegistry.aMatEnvsuit, 7, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/starmetal_1.png", 100000, 1000, 250, 0, "envsuit_plate").cloneStats((ArmorFSB) envsuit_helmet);
    public static final Item envsuit_legs = new ArmorEnvsuit(MainRegistry.aMatEnvsuit, 7, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/starmetal_2.png", 100000, 1000, 250, 0, "envsuit_legs").cloneStats((ArmorFSB) envsuit_helmet);
    public static final Item envsuit_boots = new ArmorEnvsuit(MainRegistry.aMatEnvsuit, 7, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/starmetal_1.png", 100000, 1000, 250, 0, "envsuit_boots").cloneStats((ArmorFSB) envsuit_helmet);
    public static final Item dieselsuit_helmet = new ArmorDiesel(MainRegistry.aMatDieselsuit, 7, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/starmetal_1.png", Fluids.DIESEL, 64000, 500, 50, 1, "dieselsuit_helmet")
            .addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 30, 2))
            .addEffect(new PotionEffect(MobEffects.SPEED, 30, 2));
    public static final Item dieselsuit_plate = new ArmorDiesel(MainRegistry.aMatDieselsuit, 7, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/starmetal_1.png", Fluids.DIESEL, 64000, 500, 50, 1, "dieselsuit_plate").cloneStats((ArmorFSB) dieselsuit_helmet);
    public static final Item dieselsuit_legs = new ArmorDiesel(MainRegistry.aMatDieselsuit, 7, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/starmetal_2.png", Fluids.DIESEL, 64000, 500, 50, 1, "dieselsuit_legs").cloneStats((ArmorFSB) dieselsuit_helmet);
    public static final Item dieselsuit_boots = new ArmorDiesel(MainRegistry.aMatDieselsuit, 7, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/starmetal_1.png", Fluids.DIESEL, 64000, 500, 50, 1, "dieselsuit_boots").cloneStats((ArmorFSB) dieselsuit_helmet);
    public static final Item ajr_helmet = new ArmorAJR(MainRegistry.aMatAJR, 7, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25, "ajr_helmet")
            .enableVATS(true)
            .setHasGeigerSound(true)
            .setHasHardLanding(true)
            .addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 30, 0))
            .addEffect(new PotionEffect(MobEffects.STRENGTH, 30, 0))
            .setStep(HBMSoundHandler.iron)
            .setJump(HBMSoundHandler.ironJump)
            .setFall(HBMSoundHandler.ironLand);
    public static final Item ajr_plate = new ArmorAJR(MainRegistry.aMatAJR, 7, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25, "ajr_plate").cloneStats((ArmorFSB) ajr_helmet);
    public static final Item ajr_legs = new ArmorAJR(MainRegistry.aMatAJR, 7, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/starmetal_2.png", 2500000, 10000, 2000, 25, "ajr_legs").cloneStats((ArmorFSB) ajr_helmet);
    public static final Item ajr_boots = new ArmorAJR(MainRegistry.aMatAJR, 7, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25, "ajr_boots").cloneStats((ArmorFSB) ajr_helmet);
    public static final Item ajro_helmet = new ArmorAJRO(MainRegistry.aMatAJR, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25, "ajro_helmet")
            .enableVATS(true)
            .setHasGeigerSound(true)
            .setHasHardLanding(true)
            .addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 30, 0))
            .addEffect(new PotionEffect(MobEffects.STRENGTH, 30, 0))
            .setStep(HBMSoundHandler.iron)
            .setJump(HBMSoundHandler.ironJump)
            .setFall(HBMSoundHandler.ironLand);
    public static final Item ajro_plate = new ArmorAJRO(MainRegistry.aMatAJR, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25, "ajro_plate").cloneStats((ArmorFSB) ajro_helmet);
    public static final Item ajro_legs = new ArmorAJRO(MainRegistry.aMatAJR, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/starmetal_2.png", 2500000, 10000, 2000, 25, "ajro_legs").cloneStats((ArmorFSB) ajro_helmet);
    public static final Item ajro_boots = new ArmorAJRO(MainRegistry.aMatAJR, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25, "ajro_boots").cloneStats((ArmorFSB) ajro_helmet);
    public static final Item hev_helmet = new ArmorHEV(MainRegistry.aMatHEV, 7, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/starmetal_1.png", 1000000, 10000, 2500, 0, "hev_helmet")
            .addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 30, 0))
            .addEffect(new PotionEffect(MobEffects.SPEED, 30, 1))
            .setHasGeigerSound(true)
            .setHasCustomGeiger(true);
    public static final Item hev_plate = new ArmorHEV(MainRegistry.aMatHEV, 7, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/starmetal_1.png", 1000000, 10000, 2500, 0, "hev_plate").cloneStats((ArmorFSB) hev_helmet);
    public static final Item hev_legs = new ArmorHEV(MainRegistry.aMatHEV, 7, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/starmetal_2.png", 1000000, 10000, 2500, 0, "hev_legs").cloneStats((ArmorFSB) hev_helmet);
    public static final Item hev_boots = new ArmorHEV(MainRegistry.aMatHEV, 7, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/starmetal_1.png", 1000000, 10000, 2500, 0, "hev_boots").cloneStats((ArmorFSB) hev_helmet);
    public static final Item bj_helmet = new ArmorBJ(MainRegistry.aMatBJ, 7, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/starmetal_1.png", 10000000, 10000, 1000, 100, "bj_helmet")
            .enableVATS(true)
            .enableThermalSight(true)
            .setHasHardLanding(true)
            .setHasGeigerSound(true)
            .addEffect(new PotionEffect(MobEffects.SPEED, 30, 1))
            .addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 30, 0))
            .addEffect(new PotionEffect(MobEffects.SATURATION, 30, 0))
            .addEffect(new PotionEffect(HbmPotion.radx, 30, 0))
            .setStep(HBMSoundHandler.iron)
            .setJump(HBMSoundHandler.ironJump)
            .setFall(HBMSoundHandler.ironLand);
    public static final Item bj_plate = new ArmorBJ(MainRegistry.aMatBJ, 7, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/starmetal_1.png", 10000000, 10000, 1000, 100, "bj_plate").cloneStats((ArmorFSB) bj_helmet);
    public static final Item bj_plate_jetpack = new ArmorBJJetpack(MainRegistry.aMatBJ, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/starmetal_1.png", 10000000, 10000, 1000, 100, "bj_plate_jetpack").cloneStats((ArmorFSB) bj_helmet);
    public static final Item bj_legs = new ArmorBJ(MainRegistry.aMatBJ, 7, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/starmetal_2.png", 10000000, 10000, 1000, 100, "bj_legs").cloneStats((ArmorFSB) bj_helmet);
    public static final Item bj_boots = new ArmorBJ(MainRegistry.aMatBJ, 7, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/starmetal_1.png", 10000000, 10000, 1000, 100, "bj_boots").cloneStats((ArmorFSB) bj_helmet);
    public static final Item rpa_helmet = new ArmorRPA(MainRegistry.aMatRPA, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 1000, 25, "RPA_helmet")
            .enableVATS(true)
            .setHasGeigerSound(true)
            .setHasHardLanding(true)
            .setStep(HBMSoundHandler.poweredStep)
            .setJump(HBMSoundHandler.poweredStep)
            .setFall(HBMSoundHandler.poweredStep)
            .addEffect(new PotionEffect(MobEffects.STRENGTH, 20, 3));
    public static final Item rpa_plate = new ArmorRPA(MainRegistry.aMatRPA, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 10000, 25, "RPA_plate").cloneStats((ArmorFSB) rpa_helmet);
    public static final Item rpa_legs = new ArmorRPA(MainRegistry.aMatRPA, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/starmetal_2.png", 2500000, 10000, 10000, 25, "RPA_legs").cloneStats((ArmorFSB) rpa_helmet);
    public static final Item rpa_boots = new ArmorRPA(MainRegistry.aMatRPA, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 10000, 25, "RPA_boots").cloneStats((ArmorFSB) rpa_helmet);
    public static final Item fau_helmet = new ArmorDigamma(MainRegistry.aMatFau, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/starmetal_1.png", 10000000, 100000, 25000, 1000, "fau_helmet")
            .addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 30, 1))
            .setHasGeigerSound(true)
            .enableThermalSight(true)
            .setHasHardLanding(true)
            .setStep(HBMSoundHandler.iron)
            .setJump(HBMSoundHandler.ironJump)
            .setFall(HBMSoundHandler.ironLand);
    public static final Item fau_plate = new ArmorDigamma(MainRegistry.aMatFau, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/starmetal_1.png", 10000000, 100000, 25000, 1000, "fau_plate").cloneStats((ArmorFSB) fau_helmet);
    public static final Item fau_legs = new ArmorDigamma(MainRegistry.aMatFau, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/starmetal_2.png", 10000000, 100000, 25000, 1000, "fau_legs").cloneStats((ArmorFSB) fau_helmet);
    public static final Item fau_boots = new ArmorDigamma(MainRegistry.aMatFau, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/starmetal_1.png", 10000000, 100000, 25000, 1000, "fau_boots").cloneStats((ArmorFSB) fau_helmet);
    public static final Item dns_helmet = new ArmorDNT(MainRegistry.aMatDNS, -1, EntityEquipmentSlot.HEAD, Tags.MODID + ":textures/armor/starmetal_1.png", 1000000000, 1000000, 100000, 115, "dns_helmet")
            .addEffect(new PotionEffect(MobEffects.STRENGTH, 30, 9))
            .addEffect(new PotionEffect(MobEffects.HASTE, 30, 7))
            .addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 30, 2))
            .setHasGeigerSound(true)
            .enableVATS(true)
            .enableThermalSight(true)
            .setHasHardLanding(true)
            .setStep(HBMSoundHandler.iron)
            .setJump(HBMSoundHandler.ironJump)
            .setFall(HBMSoundHandler.ironLand);
    public static final Item dns_plate = new ArmorDNT(MainRegistry.aMatDNS, -1, EntityEquipmentSlot.CHEST, Tags.MODID + ":textures/armor/starmetal_1.png", 1000000000, 1000000, 100000, 115, "dns_plate").cloneStats((ArmorFSB) dns_helmet);
    public static final Item dns_legs = new ArmorDNT(MainRegistry.aMatDNS, -1, EntityEquipmentSlot.LEGS, Tags.MODID + ":textures/armor/starmetal_2.png", 1000000000, 1000000, 100000, 115, "dns_legs").cloneStats((ArmorFSB) dns_helmet);
    public static final Item dns_boots = new ArmorDNT(MainRegistry.aMatDNS, -1, EntityEquipmentSlot.FEET, Tags.MODID + ":textures/armor/starmetal_1.png", 1000000000, 1000000, 100000, 115, "dns_boots").cloneStats((ArmorFSB) dns_helmet);
    public static final Item goggles = new ArmorModel(ArmorMaterial.IRON, -1, EntityEquipmentSlot.HEAD, "goggles").setMaxStackSize(1);
    public static final Item ashglasses = new ArmorAshGlasses(ArmorMaterial.IRON, -1, EntityEquipmentSlot.HEAD, "ashglasses").setMaxStackSize(1);
    public static final Item mask_rag = new ItemRag("mask_rag").setMaxStackSize(1).setCreativeTab(CreativeTabs.COMBAT);
    public static final Item mask_damp = new ArmorModel(ArmorMaterial.IRON, -1, EntityEquipmentSlot.HEAD, "mask_damp").setMaxStackSize(1);
    public static final Item mask_piss = new ArmorModel(ArmorMaterial.IRON, -1, EntityEquipmentSlot.HEAD, "mask_piss").setMaxStackSize(1);
    public static final Item gas_mask = new ArmorGasMask(ArmorMaterial.IRON, -1, EntityEquipmentSlot.HEAD, "gas_mask").setMaxStackSize(1);
    public static final Item gas_mask_m65 = new ArmorGasMask(ArmorMaterial.IRON, -1, EntityEquipmentSlot.HEAD, "gas_mask_m65").setMaxStackSize(1);
    public static final Item gas_mask_mono = new ArmorGasMask(ArmorMaterial.IRON, -1, EntityEquipmentSlot.HEAD, "gas_mask_mono").setMaxStackSize(1);
    public static final Item gas_mask_olde = new ArmorGasMask(ArmorMaterial.IRON, -1, EntityEquipmentSlot.HEAD, "gas_mask_olde").setMaxStackSize(1);
    public static final Item hat = new ArmorHat(ArmorMaterial.IRON, 7, EntityEquipmentSlot.HEAD, "nossy_hat").setMaxStackSize(1);
    public static final Item no9 = new ArmorNo9(MainRegistry.aMatSteel, 7, EntityEquipmentSlot.HEAD, "no9").setMaxStackSize(1);
    public static final Item beta = new ItemDrop("beta").setMaxStackSize(1);
    public static final Item jetpack_fly = new JetpackRegular(Fluids.KEROSENE, 12000, "jetpack_fly").setCreativeTab(CreativeTabs.COMBAT).setMaxStackSize(1);
    public static final Item jetpack_break = new JetpackBreak(Fluids.KEROSENE, 12000, "jetpack_break").setCreativeTab(CreativeTabs.COMBAT).setMaxStackSize(1);
    public static final Item jetpack_vector = new JetpackVectorized(Fluids.KEROSENE, 16000, "jetpack_vector").setCreativeTab(CreativeTabs.COMBAT).setMaxStackSize(1);
    public static final Item jetpack_boost = new JetpackBooster(Fluids.BALEFIRE, 32000, "jetpack_boost").setCreativeTab(CreativeTabs.COMBAT).setMaxStackSize(1);
    public static final Item jetpack_glider = new JetpackGlider(MainRegistry.aMatSteel, -1, EntityEquipmentSlot.CHEST, 20000, "jetpack_glider").setMaxStackSize(1).setCreativeTab(CreativeTabs.COMBAT);
    public static final Item wings_murk = new WingsMurk("wings_murk").setCreativeTab(CreativeTabs.COMBAT).setMaxStackSize(1);
    public static final Item wings_limp = new WingsMurk("wings_limp").setCreativeTab(CreativeTabs.COMBAT).setMaxStackSize(1);
    public static final Item cape_radiation = new ArmorModel(ArmorMaterial.CHAIN, -1, EntityEquipmentSlot.CHEST, "cape_radiation").setCreativeTab(MainRegistry.consumableTab).setMaxStackSize(1);
    public static final Item cape_gasmask = new ArmorModel(ArmorMaterial.CHAIN, -1, EntityEquipmentSlot.CHEST, "cape_gasmask").setCreativeTab(MainRegistry.consumableTab).setMaxStackSize(1);
    public static final Item cape_schrabidium = new ArmorModel(MainRegistry.aMatSchrab, -1, EntityEquipmentSlot.CHEST, "cape_schrabidium").setCreativeTab(MainRegistry.consumableTab).setMaxStackSize(1);
    public static final Item schrabidium_hammer = new WeaponSpecial(MainRegistry.enumToolMaterialHammer, "schrabidium_hammer").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab);
    public static final Item shimmer_sledge = new WeaponSpecial(MainRegistry.enumToolMaterialSledge, "shimmer_sledge").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab);
    public static final Item shimmer_axe = new WeaponSpecial(MainRegistry.enumToolMaterialSledge, "shimmer_axe").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab);
    public static final Item ullapool_caber = new WeaponSpecial(MainRegistry.enumToolMaterialSteel, "ullapool_caber").setCreativeTab(MainRegistry.weaponTab);
    public static final Item euphemium_stopper = new ItemConsumable("euphemium_stopper").setMaxStackSize(1).setFull3D().setCreativeTab(null);
    public static final Item matchstick = new ItemMatch("matchstick").setCreativeTab(CreativeTabs.TOOLS).setFull3D().setCreativeTab(MainRegistry.weaponTab);
    public static final Item balefire_and_steel = new ItemBalefireMatch("balefire_and_steel").setFull3D().setCreativeTab(MainRegistry.weaponTab);
    public static final Item wrench = new ItemWrench(MainRegistry.enumToolMaterialSteel, "wrench").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab);
    public static final Item wrench_flipped = new WeaponSpecial(MainRegistry.enumToolMaterialElec, "wrench_flipped").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab);
    public static final Item memespoon = new WeaponSpecial(MainRegistry.enumToolMaterialSteel, "memespoon").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab);
    public static final Item wood_gavel = new WeaponSpecial(ToolMaterial.WOOD, "wood_gavel").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab);
    public static final Item lead_gavel = new WeaponSpecial(MainRegistry.enumToolMaterialSteel, "lead_gavel").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab);
    public static final Item diamond_gavel = new WeaponSpecial(ToolMaterial.DIAMOND, "diamond_gavel").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab);
    public static final Item multitool_hit = new ItemMultitoolPassive("multitool_hit").setCreativeTab(null);
    public static final Item multitool_dig = new ItemMultitoolTool(4.0F, MainRegistry.enumToolMaterialMultitool, Collections.emptySet(), "multitool_dig").setFull3D().setCreativeTab(MainRegistry.consumableTab);
    public static final Item multitool_silk = new ItemMultitoolTool(4.0F, MainRegistry.enumToolMaterialMultitool, Collections.emptySet(), "multitool_silk").setFull3D().setCreativeTab(null);
    public static final Item multitool_ext = new ItemMultitoolPassive("multitool_ext").setCreativeTab(null);
    public static final Item multitool_miner = new ItemMultitoolPassive("multitool_miner").setCreativeTab(null);
    public static final Item multitool_beam = new ItemMultitoolPassive("multitool_beam").setCreativeTab(null);
    public static final Item multitool_sky = new ItemMultitoolPassive("multitool_sky").setCreativeTab(null);
    public static final Item multitool_mega = new ItemMultitoolPassive("multitool_mega").setCreativeTab(null);
    public static final Item multitool_joule = new ItemMultitoolPassive("multitool_joule").setCreativeTab(null);
    public static final Item multitool_decon = new ItemMultitoolPassive("multitool_decon").setCreativeTab(null);
    //Guns
    public static final Item gun_b92 = new GunB92("gun_b92").setCreativeTab(MainRegistry.weaponTab);
    public static final Item gun_b93 = new GunB93("gun_b93").setCreativeTab(MainRegistry.weaponTab);
    public static final Item gun_supershotgun = new ItemGunShotty(Gun12GaugeFactory.getShottyConfig(), "gun_supershotgun").setCreativeTab(MainRegistry.weaponTab);
    public static final Item jshotgun = new ItemGunJShotty(Gun12GaugeFactory.getJShotgunConfig(), "gun_jshotgun").setCreativeTab(MainRegistry.weaponTab);
    //Drillgon200: The SQUID!
    public static final Item gun_vortex = new ItemGunVortex(GunEnergyFactory.getVortexConfig(), "gun_vortex").setCreativeTab(MainRegistry.weaponTab);
    public static final Item gun_egon = new ItemGunEgon(GunEnergyFactory.getEgonConfig(), "gun_egon").setCreativeTab(MainRegistry.weaponTab);
    public static final Item gun_moist_nugget = new ItemNugget(3, false, "gun_moist_nugget").setCreativeTab(MainRegistry.weaponTab);
    public static final Item crucible = new ItemCrucible(500, 1F, MainRegistry.matCrucible, "crucible").setCreativeTab(MainRegistry.weaponTab);
    //Yeah it's supposed to be "ultrahard steel", but I don't feel like adding that, so high-speed steel sword it is.
    public static final Item hs_sword = new ItemSwordCutter(10F, 1F, MainRegistry.matCrucible, "hs_sword").setCreativeTab(MainRegistry.weaponTab);
    //High frequency sword
    public static final Item hf_sword = new ItemSwordCutter(15F, 1F, MainRegistry.matCrucible, "hf_sword").setCreativeTab(MainRegistry.weaponTab);

    public static final Item stick_dynamite = new ItemGrenade(3, "stick_dynamite").setCreativeTab(MainRegistry.weaponTab);
    public static final Item stick_dynamite_fishing = new ItemGrenadeFishing(3, "stick_dynamite_fishing").setCreativeTab(MainRegistry.weaponTab);
    public static final Item stick_tnt = new ItemBakedBase("stick_tnt").setCreativeTab(MainRegistry.weaponTab);
    public static final Item stick_semtex = new ItemBakedBase("stick_semtex").setCreativeTab(MainRegistry.weaponTab);
    public static final Item stick_c4 = new ItemBakedBase("stick_c4").setCreativeTab(MainRegistry.weaponTab);

    //Materials
    public static final Item ingot_steel = new ItemBakedBase("ingot_steel").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_titanium = new ItemBakedBase("ingot_titanium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_copper = new ItemBakedBase("ingot_copper").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_red_copper = new ItemBakedBase("ingot_red_copper").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_advanced_alloy = new ItemBakedBase("ingot_advanced_alloy").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_tungsten = new ItemBakedBase("ingot_tungsten").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_aluminium = new ItemBakedBase("ingot_aluminium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_beryllium = new ItemBakedBase("ingot_beryllium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_lead = new ItemBakedBase("ingot_lead").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_asbestos = new ItemBakedBase("ingot_asbestos").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_magnetized_tungsten = new ItemBakedBase("ingot_magnetized_tungsten").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_combine_steel = new ItemCustomLore("ingot_combine_steel").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_dura_steel = new ItemCustomLore("ingot_dura_steel").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_technetium = new ItemBakedBase("ingot_technetium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_tcalloy = new ItemBakedBase("ingot_tcalloy").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_cdalloy = new ItemBakedBase("ingot_cdalloy").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_polymer = new ItemCustomLore("ingot_polymer").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_bakelite = new ItemCustomLore("ingot_bakelite").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_rubber = new ItemCustomLore("ingot_rubber").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_biorubber = new ItemCustomLore("ingot_biorubber").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_pc = new ItemCustomLore("ingot_pc").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_pvc = new ItemCustomLore("ingot_pvc").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_desh = new ItemCustomLore("ingot_desh").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_saturnite = new ItemCustomLore("ingot_saturnite").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_ferrouranium = new ItemBakedBase("ingot_ferrouranium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_starmetal = new ItemCustomLore("ingot_starmetal").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_osmiridium = new ItemCustomLore("ingot_osmiridium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_euphemium = new ItemCustomLore("ingot_euphemium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_dineutronium = new ItemCustomLore("ingot_dineutronium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_cadmium = new ItemBakedBase("ingot_cadmium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_bismuth = new ItemCustomLore("ingot_bismuth").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_arsenic = new ItemCustomLore("ingot_arsenic").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_zirconium = new ItemBakedBase("ingot_zirconium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_bismuth_bronze = new ItemBakedBase("ingot_bismuth_bronze").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_arsenic_bronze = new ItemBakedBase("ingot_arsenic_bronze").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_bscco = new ItemBakedBase("ingot_bscco").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_calcium = new ItemBakedBase("ingot_calcium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_silicon = new ItemBakedBase("ingot_silicon").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_gunmetal = new ItemBakedBase("ingot_gunmetal").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_weaponsteel = new ItemBakedBase("ingot_weaponsteel").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_cft = new ItemBakedBase("ingot_cft").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_th232 = new ItemCustomLore("ingot_th232").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_uranium = new ItemCustomLore("ingot_uranium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_u233 = new ItemCustomLore("ingot_u233").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_u235 = new ItemCustomLore("ingot_u235").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_u238 = new ItemCustomLore("ingot_u238").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_u238m2 = new ItemUnstable(350, 200, "ingot_u238m2").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_plutonium = new ItemCustomLore("ingot_plutonium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_pu238 = new ItemCustomLore("ingot_pu238").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_pu239 = new ItemCustomLore("ingot_pu239").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_pu240 = new ItemCustomLore("ingot_pu240").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_pu241 = new ItemCustomLore("ingot_pu241").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_pu_mix = new ItemCustomLore("ingot_pu_mix").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_am241 = new ItemCustomLore("ingot_am241").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_am242 = new ItemCustomLore("ingot_am242").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_am_mix = new ItemCustomLore("ingot_am_mix").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_schraranium = new ItemCustomLore("ingot_schraranium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_schrabidium = new ItemCustomLore("ingot_schrabidium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_schrabidate = new ItemCustomLore("ingot_schrabidate").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_solinium = new ItemCustomLore("ingot_solinium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_mud = new ItemBakedBase("ingot_mud").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_thorium_fuel = new ItemCustomLore("ingot_thorium_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_uranium_fuel = new ItemCustomLore("ingot_uranium_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_mox_fuel = new ItemCustomLore("ingot_mox_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_plutonium_fuel = new ItemCustomLore("ingot_plutonium_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_neptunium_fuel = new ItemCustomLore("ingot_neptunium_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_americium_fuel = new ItemCustomLore("ingot_americium_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_les = new ItemCustomLore("ingot_les").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_schrabidium_fuel = new ItemCustomLore("ingot_schrabidium_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_hes = new ItemCustomLore("ingot_hes").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_neptunium = new ItemCustomLore("ingot_neptunium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_tennessine = new ItemCustomLore("ingot_tennessine").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_polonium = new ItemCustomLore("ingot_polonium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_phosphorus = new ItemCustomLore("ingot_phosphorus").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_boron = new ItemBakedBase("ingot_boron").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_graphite = new ItemFuel("ingot_graphite", 1600).setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_fiberglass = new ItemCustomLore("ingot_fiberglass").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_smore = new ItemFoodBase(10, 20F, false, "ingot_smore").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_niobium = new ItemCustomLore("ingot_niobium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_actinium = new ItemCustomLore("ingot_actinium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_bromine = new ItemCustomLore("ingot_bromine").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_caesium = new ItemBakedBase("ingot_caesium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_cerium = new ItemCustomLore("ingot_cerium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_lanthanium = new ItemCustomLore("ingot_lanthanium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_tantalium = new ItemCustomLore("ingot_tantalium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_astatine = new ItemCustomLore("ingot_astatine").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_firebrick = new ItemBakedBase("ingot_firebrick").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_cobalt = new ItemCustomLore("ingot_cobalt").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_co60 = new ItemBakedBase("ingot_co60").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_sr90 = new ItemBakedBase("ingot_sr90").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_iodine = new ItemCustomLore("ingot_iodine").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_i131 = new ItemBakedBase("ingot_i131").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_au198 = new ItemBakedBase("ingot_au198").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_pb209 = new ItemBakedBase("ingot_pb209").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_ra226 = new ItemBakedBase("ingot_ra226").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_gh336 = new ItemCustomLore("ingot_gh336").setRarity(EnumRarity.EPIC).setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_electronium = new ItemUnstable(30, 6000, "ingot_electronium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_reiium = new ItemCustomLore("ingot_reiium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_weidanium = new ItemCustomLore("ingot_weidanium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_australium = new ItemCustomLore("ingot_australium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_verticium = new ItemCustomLore("ingot_verticium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_unobtainium = new ItemCustomLore("ingot_unobtainium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_daffergon = new ItemCustomLore("ingot_daffergon").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_steel_dusted = new ItemHotDusted(200, "ingot_steel_dusted").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_chainsteel = new ItemHot(100, "ingot_chainsteel").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_meteorite = new ItemHot(200, "ingot_meteorite").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_meteorite_forged = new ItemHot(200, "ingot_meteorite_forged").setCreativeTab(MainRegistry.partsTab);
    public static final Item blade_meteorite = new ItemHot(200, "blade_meteorite").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_cobalt = new ItemBakedBase("billet_cobalt").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_silicon = new ItemCustomLore("billet_silicon").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_th232 = new ItemCustomLore("billet_th232").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_uranium = new ItemCustomLore("billet_uranium").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_u233 = new ItemCustomLore("billet_u233").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_u235 = new ItemCustomLore("billet_u235").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_u238 = new ItemCustomLore("billet_u238").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_plutonium = new ItemCustomLore("billet_plutonium").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_pu238 = new ItemCustomLore("billet_pu238").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_pu239 = new ItemCustomLore("billet_pu239").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_pu240 = new ItemCustomLore("billet_pu240").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_pu241 = new ItemCustomLore("billet_pu241").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_pu_mix = new ItemCustomLore("billet_pu_mix").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_am241 = new ItemCustomLore("billet_am241").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_am242 = new ItemCustomLore("billet_am242").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_am_mix = new ItemCustomLore("billet_am_mix").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_neptunium = new ItemCustomLore("billet_neptunium").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_polonium = new ItemCustomLore("billet_polonium").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_technetium = new ItemCustomLore("billet_technetium").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_co60 = new ItemCustomLore("billet_co60").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_sr90 = new ItemCustomLore("billet_sr90").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_au198 = new ItemCustomLore("billet_au198").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_pb209 = new ItemCustomLore("billet_pb209").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_ra226 = new ItemCustomLore("billet_ra226").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_actinium = new ItemCustomLore("billet_actinium").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_gh336 = new ItemCustomLore("billet_gh336").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_beryllium = new ItemBakedBase("billet_beryllium").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_bismuth = new ItemBakedBase("billet_bismuth").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_zirconium = new ItemBakedBase("billet_zirconium").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_zfb_bismuth = new ItemCustomLore("billet_zfb_bismuth").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_zfb_pu241 = new ItemCustomLore("billet_zfb_pu241").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_zfb_am_mix = new ItemCustomLore("billet_zfb_am_mix").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_schrabidium = new ItemCustomLore("billet_schrabidium").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_solinium = new ItemCustomLore("billet_solinium").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_thorium_fuel = new ItemCustomLore("billet_thorium_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_uranium_fuel = new ItemCustomLore("billet_uranium_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_mox_fuel = new ItemCustomLore("billet_mox_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_plutonium_fuel = new ItemCustomLore("billet_plutonium_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_neptunium_fuel = new ItemCustomLore("billet_neptunium_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_americium_fuel = new ItemCustomLore("billet_americium_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_les = new ItemCustomLore("billet_les").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_schrabidium_fuel = new ItemCustomLore("billet_schrabidium_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_hes = new ItemCustomLore("billet_hes").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_po210be = new ItemCustomLore("billet_po210be").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_ra226be = new ItemCustomLore("billet_ra226be").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_pu238be = new ItemCustomLore("billet_pu238be").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_australium = new ItemCustomLore("billet_australium").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_australium_lesser = new ItemCustomLore("billet_australium_lesser").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_australium_greater = new ItemCustomLore("billet_australium_greater").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_unobtainium = new ItemCustomLore("billet_unobtainium").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_yharonite = new ItemBakedBase("billet_yharonite").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_balefire_gold = new ItemCustomLore("billet_balefire_gold").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_flashlead = new ItemCustomLore("billet_flashlead").setCreativeTab(MainRegistry.partsTab);
    public static final Item billet_nuclear_waste = new ItemCustomLore("billet_nuclear_waste").setCreativeTab(MainRegistry.partsTab);
    public static final Item bio_wafer = new ItemLemon(8, 8, false, "bio_wafer").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_uranium = new ItemCustomLore("nugget_uranium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_u233 = new ItemCustomLore("nugget_u233").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_u235 = new ItemCustomLore("nugget_u235").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_u238 = new ItemCustomLore("nugget_u238").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_u238m2 = new ItemUnstable(60, 2000, "nugget_u238m2").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_plutonium = new ItemCustomLore("nugget_plutonium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_pu238 = new ItemCustomLore("nugget_pu238").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_pu239 = new ItemCustomLore("nugget_pu239").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_pu240 = new ItemCustomLore("nugget_pu240").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_th232 = new ItemCustomLore("nugget_th232").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_pu241 = new ItemCustomLore("nugget_pu241").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_pu_mix = new ItemCustomLore("nugget_pu_mix").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_am241 = new ItemCustomLore("nugget_am241").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_am242 = new ItemCustomLore("nugget_am242").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_am_mix = new ItemCustomLore("nugget_am_mix").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_technetium = new ItemCustomLore("nugget_technetium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_neptunium = new ItemCustomLore("nugget_neptunium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_polonium = new ItemCustomLore("nugget_polonium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_thorium_fuel = new ItemCustomLore("nugget_thorium_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_uranium_fuel = new ItemCustomLore("nugget_uranium_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_mox_fuel = new ItemCustomLore("nugget_mox_fuel").setCreativeTab(MainRegistry.partsTab).setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_plutonium_fuel = new ItemCustomLore("nugget_plutonium_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_neptunium_fuel = new ItemCustomLore("nugget_neptunium_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_americium_fuel = new ItemCustomLore("nugget_americium_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_les = new ItemCustomLore("nugget_les").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_schrabidium_fuel = new ItemCustomLore("nugget_schrabidium_fuel").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_hes = new ItemCustomLore("nugget_hes").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_lead = new ItemCustomLore("nugget_lead").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_beryllium = new ItemBakedBase("nugget_beryllium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_cadmium = new ItemBakedBase("nugget_cadmium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_bismuth = new ItemBakedBase("nugget_bismuth").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_arsenic = new ItemCustomLore("nugget_arsenic").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_zirconium = new ItemCustomLore("nugget_zirconium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_tantalium = new ItemCustomLore("nugget_tantalium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_desh = new ItemCustomLore("nugget_desh").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_osmiridium = new ItemCustomLore("nugget_osmiridium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_schrabidium = new ItemCustomLore("nugget_schrabidium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_solinium = new ItemCustomLore("nugget_solinium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_euphemium = new ItemCustomLore("nugget_euphemium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_dineutronium = new ItemCustomLore("nugget_dineutronium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_niobium = new ItemBakedBase("nugget_niobium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_silicon = new ItemBakedBase("nugget_silicon").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_actinium = new ItemBakedBase("nugget_actinium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_cobalt = new ItemCustomLore("nugget_cobalt").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_co60 = new ItemCustomLore("nugget_co60").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_strontium = new ItemCustomLore("nugget_strontium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_sr90 = new ItemCustomLore("nugget_sr90").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_pb209 = new ItemCustomLore("nugget_pb209").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_gh336 = new ItemCustomLore("nugget_gh336").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_au198 = new ItemCustomLore("nugget_au198").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_ra226 = new ItemCustomLore("nugget_ra226").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_reiium = new ItemCustomLore("nugget_reiium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_weidanium = new ItemCustomLore("nugget_weidanium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_australium = new ItemCustomLore("nugget_australium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_australium_lesser = new ItemCustomLore("nugget_australium_lesser").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_australium_greater = new ItemCustomLore("nugget_australium_greater").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_verticium = new ItemCustomLore("nugget_verticium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_unobtainium = new ItemCustomLore("nugget_unobtainium").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_unobtainium_lesser = new ItemCustomLore("nugget_unobtainium_lesser").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_unobtainium_greater = new ItemCustomLore("nugget_unobtainium_greater").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_daffergon = new ItemCustomLore("nugget_daffergon").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_mercury = new ItemCustomLore("nugget_mercury").setCreativeTab(MainRegistry.partsTab);
    public static final Item nugget_mercury = new ItemCustomLore("nugget_mercury_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item bottle_mercury = new ItemCustomLore("bottle_mercury").setContainerItem(Items.GLASS_BOTTLE).setCreativeTab(MainRegistry.partsTab);
    //POWDERS
    public static final Item powder_iron = new ItemBase("powder_iron").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_gold = new ItemBase("powder_gold").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_diamond = new ItemBase("powder_diamond").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_emerald = new ItemBase("powder_emerald").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_lapis = new ItemBase("powder_lapis").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_titanium = new ItemBase("powder_titanium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_tungsten = new ItemBase("powder_tungsten").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_sodium = new ItemBase("powder_sodium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_chlorocalcite = new ItemBase("powder_chlorocalcite").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_molysite = new ItemBase("powder_molysite").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_copper = new ItemBase("powder_copper").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_beryllium = new ItemBase("powder_beryllium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_aluminium = new ItemBase("powder_aluminium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_lead = new ItemCustomLore("powder_lead").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_advanced_alloy = new ItemBase("powder_advanced_alloy").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_combine_steel = new ItemBase("powder_combine_steel").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_tcalloy = new ItemCustomLore("powder_tcalloy").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_cdalloy = new ItemBase("powder_cdalloy").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_magnetized_tungsten = new ItemCustomLore("powder_magnetized_tungsten").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_chlorophyte = new ItemBase("powder_chlorophyte").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_red_copper = new ItemBase("powder_red_copper").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_steel = new ItemBase("powder_steel").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_steel_tiny = new ItemBase("powder_steel_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_lithium = new ItemCustomLore("powder_lithium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_lithium_tiny = new ItemCustomLore("powder_lithium_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item redstone_depleted = new ItemBase("redstone_depleted").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_quartz = new ItemBase("powder_quartz").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_borax = new ItemBase("powder_borax").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_dura_steel = new ItemCustomLore("powder_dura_steel").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_polymer = new ItemCustomLore("powder_polymer").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_bakelite = new ItemCustomLore("powder_bakelite").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_lanthanium = new ItemCustomLore("powder_lanthanium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_lanthanium_tiny = new ItemBase("powder_lanthanium_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_actinium = new ItemCustomLore("powder_actinium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_actinium_tiny = new ItemBase("powder_actinium_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_boron = new ItemCustomLore("powder_boron").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_boron_tiny = new ItemBase("powder_boron_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_semtex_mix = new ItemBase("powder_semtex_mix").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_desh = new ItemBase("powder_desh").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_zirconium = new ItemBase("powder_zirconium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_lignite = new ItemFuel("powder_lignite", 1200).setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_asbestos = new ItemBase("powder_asbestos").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_cadmium = new ItemBase("powder_cadmium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_bismuth = new ItemBase("powder_bismuth").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_coal = new ItemFuel("powder_coal", 1600).setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_coal_tiny = new ItemFuel("powder_coal_tiny", 160).setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_yellowcake = new ItemCustomLore("powder_yellowcake").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_thorium = new ItemCustomLore("powder_thorium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_uranium = new ItemCustomLore("powder_uranium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_plutonium = new ItemCustomLore("powder_plutonium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_neptunium = new ItemCustomLore("powder_neptunium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_polonium = new ItemCustomLore("powder_polonium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_schrabidium = new ItemCustomLore("powder_schrabidium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_schrabidate = new ItemCustomLore("powder_schrabidate").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_euphemium = new ItemCustomLore("powder_euphemium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_dineutronium = new ItemCustomLore("powder_dineutronium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_iodine = new ItemCustomLore("powder_iodine").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_iodine_tiny = new ItemCustomLore("powder_iodine_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_astatine = new ItemCustomLore("powder_astatine").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_neodymium = new ItemCustomLore("powder_neodymium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_neodymium_tiny = new ItemCustomLore("powder_neodymium_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_caesium = new ItemBase("powder_caesium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_reiium = new ItemCustomLore("powder_reiium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_weidanium = new ItemCustomLore("powder_weidanium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_australium = new ItemCustomLore("powder_australium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_verticium = new ItemCustomLore("powder_verticium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_unobtainium = new ItemCustomLore("powder_unobtainium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_daffergon = new ItemCustomLore("powder_daffergon").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_strontium = new ItemBase("powder_strontium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_cobalt = new ItemCustomLore("powder_cobalt").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_cobalt_tiny = new ItemCustomLore("powder_cobalt_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_bromine = new ItemCustomLore("powder_bromine").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_niobium = new ItemCustomLore("powder_niobium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_niobium_tiny = new ItemCustomLore("powder_niobium_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_tantalium = new ItemCustomLore("powder_tantalium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_tennessine = new ItemCustomLore("powder_tennessine").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_cerium = new ItemCustomLore("powder_cerium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_cerium_tiny = new ItemCustomLore("powder_cerium_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_calcium = new ItemBase("powder_calcium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_ice = new ItemBase("powder_ice").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_limestone = new ItemBase("powder_limestone").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_desh_mix = new ItemBase("powder_desh_mix").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_desh_ready = new ItemBase("powder_desh_ready").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_nitan_mix = new ItemCustomLore("powder_nitan_mix").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_spark_mix = new ItemCustomLore("powder_spark_mix").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_fire = new ItemFuel("powder_fire", 6400).setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_meteorite = new ItemBase("powder_meteorite").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_meteorite_tiny = new ItemBase("powder_meteorite_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_flux = new ItemBakedBase("powder_flux").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_fertilizer = new ItemFertilizer("powder_fertilizer").setCreativeTab(MainRegistry.partsTab);
    //Osmiridium
    public static final Item powder_tektite = new ItemCustomLore("powder_tektite").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_paleogenite_tiny = new ItemCustomLore("powder_paleogenite_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_paleogenite = new ItemCustomLore("powder_paleogenite").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_impure_osmiridium = new ItemCustomLore("powder_impure_osmiridium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_osmiridium = new ItemCustomLore("powder_osmiridium").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_magic = new ItemCustomLore("powder_magic").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_cloud = new ItemCustomLore("powder_cloud").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_balefire = new ItemCustomLore("powder_balefire").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_sawdust = new ItemBakedBase("powder_sawdust").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_coltan_ore = new ItemCustomLore("powder_coltan_ore").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_coltan = new ItemBase("powder_coltan").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_poison = new ItemBase("powder_poison").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_thermite = new ItemCustomLore("powder_thermite").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_power = new ItemCustomLore("powder_power", "powder_energy_alt").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_co60 = new ItemCustomLore("powder_co60").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_co60_tiny = new ItemCustomLore("powder_co60_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_sr90 = new ItemCustomLore("powder_sr90").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_sr90_tiny = new ItemCustomLore("powder_sr90_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_at209 = new ItemCustomLore("powder_at209").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_at209_tiny = new ItemCustomLore("powder_at209_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_pb209 = new ItemCustomLore("powder_pb209").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_pb209_tiny = new ItemCustomLore("powder_pb209_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_i131 = new ItemCustomLore("powder_i131").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_i131_tiny = new ItemCustomLore("powder_i131_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_cs137 = new ItemCustomLore("powder_cs137").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_cs137_tiny = new ItemCustomLore("powder_cs137_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_xe135 = new ItemCustomLore("powder_xe135").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_xe135_tiny = new ItemCustomLore("powder_xe135_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_au198 = new ItemCustomLore("powder_au198").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_au198_tiny = new ItemCustomLore("powder_au198_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_ra226 = new ItemCustomLore("powder_ra226").setCreativeTab(MainRegistry.partsTab);
    public static final Item sulfur = new ItemBase("sulfur").setCreativeTab(MainRegistry.partsTab);
    public static final Item nitra = new ItemBakedBase("nitra").setCreativeTab(MainRegistry.partsTab);
    public static final Item nitra_small = new ItemBakedBase("nitra_small").setCreativeTab(MainRegistry.partsTab);
    public static final Item niter = new ItemBase("niter").setCreativeTab(MainRegistry.partsTab);
    public static final Item fluorite = new ItemBase("fluorite").setCreativeTab(MainRegistry.partsTab);
    public static final Item lithium = new ItemCustomLore("lithium").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_semtex = new ItemLemon(4, 5, true, "ingot_semtex").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_c4 = new ItemFuel("ingot_c4", 1600).setCreativeTab(MainRegistry.partsTab);
    public static final Item trinitite = new ItemCustomLore("trinitite").setCreativeTab(MainRegistry.partsTab);
    public static final Item nuclear_waste_long = new ItemWasteLong("nuclear_waste_long").setCreativeTab(MainRegistry.partsTab);
    public static final Item nuclear_waste_long_tiny = new ItemWasteLong("nuclear_waste_long_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item nuclear_waste_short = new ItemWasteShort("nuclear_waste_short").setCreativeTab(MainRegistry.partsTab);
    public static final Item nuclear_waste_short_tiny = new ItemWasteShort("nuclear_waste_short_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item nuclear_waste_long_depleted = new ItemWasteLong("nuclear_waste_long_depleted").setCreativeTab(MainRegistry.partsTab);
    public static final Item nuclear_waste_long_depleted_tiny = new ItemWasteLong("nuclear_waste_long_depleted_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item nuclear_waste_short_depleted = new ItemWasteShort("nuclear_waste_short_depleted").setCreativeTab(MainRegistry.partsTab);
    public static final Item nuclear_waste_short_depleted_tiny = new ItemWasteShort("nuclear_waste_short_depleted_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item nuclear_waste = new ItemCustomLore("nuclear_waste").setCreativeTab(MainRegistry.partsTab);
    public static final Item nuclear_waste_tiny = new ItemCustomLore("nuclear_waste_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item nuclear_waste_vitrified = new ItemCustomLore("nuclear_waste_vitrified").setCreativeTab(MainRegistry.partsTab);
    public static final Item nuclear_waste_vitrified_tiny = new ItemCustomLore("nuclear_waste_vitrified_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item scrap_plastic = new ItemEnumMulti("scrap_plastic", ScrapType.class, false, "scrap_plastic").setCreativeTab(null);
    public static final Item scrap = new ItemBase("scrap").setCreativeTab(MainRegistry.partsTab);
    public static final Item scrap_oil = new ItemBase("scrap_oil").setCreativeTab(MainRegistry.partsTab);
    public static final Item scrap_nuclear = new ItemBakedBase("scrap_nuclear").setCreativeTab(MainRegistry.partsTab);
    public static final Item dust = new ItemBase("dust").setCreativeTab(MainRegistry.partsTab);
    public static final Item dust_tiny = new ItemBakedBase("dust_tiny").setCreativeTab(MainRegistry.partsTab);
    public static final Item fallout = new ItemBakedBase("falloutitem", "fallout").setCreativeTab(MainRegistry.partsTab);
    public static final Item containment_box = new ItemLeadBox("containment_box").setCreativeTab(MainRegistry.consumableTab);
    public static final Item plastic_bag = new ItemPlasticBag("plastic_bag").setCreativeTab(MainRegistry.consumableTab);
    public static final Item ammo_bag = new ItemAmmoBag("ammo_bag").setCreativeTab(MainRegistry.consumableTab);
    public static final Item ammo_bag_infinite = new ItemAmmoBag("ammo_bag_infinite").setCreativeTab(MainRegistry.consumableTab);
    public static final Item casing_bag = new ItemCasingBag("casing_bag").setCreativeTab(MainRegistry.consumableTab);
    public static final Item tritium_deuterium_cake = new ItemCustomLore("tritium_deuterium_cake").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    public static final Item pile_rod_uranium = new ItemPileRod("pile_rod_uranium").setCreativeTab(MainRegistry.controlTab);
    public static final Item pile_rod_pu239 = new ItemPileRod("pile_rod_pu239").setCreativeTab(MainRegistry.controlTab);
    public static final Item pile_rod_plutonium = new ItemPileRod("pile_rod_plutonium").setCreativeTab(MainRegistry.controlTab);
    public static final Item pile_rod_source = new ItemPileRod("pile_rod_source").setCreativeTab(MainRegistry.controlTab);
    public static final Item pile_rod_boron = new ItemPileRod("pile_rod_boron").setCreativeTab(MainRegistry.controlTab);
    public static final Item pile_rod_lithium = new ItemPileRod("pile_rod_lithium").setCreativeTab(MainRegistry.controlTab);
    public static final Item pile_rod_detector = new ItemPileRod("pile_rod_detector").setCreativeTab(MainRegistry.controlTab);
    //That's a lot of rods
    public static final Item rod_empty = new ItemBase("rod_empty").setCreativeTab(MainRegistry.controlTab);
    public static final Item rod_dual_empty = new ItemBase("rod_dual_empty").setCreativeTab(MainRegistry.controlTab);
    public static final Item rod_quad_empty = new ItemBase("rod_quad_empty").setCreativeTab(MainRegistry.controlTab);
    public static final Item rod_zirnox_empty = new ItemBase("rod_zirnox_empty").setMaxStackSize(64).setCreativeTab(MainRegistry.controlTab);
    public static final Item rod_zirnox_tritium = new ItemBase("rod_zirnox_tritium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty);
    public static final Item rod_zirnox = new ItemZirnoxRod("rod_zirnox").setCreativeTab(MainRegistry.controlTab);
    //TODO: Make this metaitems
    // for now, let's just make it work before making it metaitems..
    public static final Item rod_zirnox_depleted = new ItemZirnoxRodDepleted("rod_zirnox_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty);
    public static final Item waste_natural_uranium = new ItemDepletedFuel("waste_natural_uranium").setCreativeTab(MainRegistry.partsTab);
    public static final Item waste_uranium = new ItemDepletedFuel("waste_uranium").setCreativeTab(MainRegistry.partsTab);
    public static final Item waste_thorium = new ItemDepletedFuel("waste_thorium").setCreativeTab(MainRegistry.partsTab);
    public static final Item waste_mox = new ItemDepletedFuel("waste_mox").setCreativeTab(MainRegistry.partsTab);
    public static final Item waste_plutonium = new ItemDepletedFuel("waste_plutonium").setCreativeTab(MainRegistry.partsTab);
    public static final Item waste_u233 = new ItemDepletedFuel("waste_u233").setCreativeTab(MainRegistry.partsTab);
    public static final Item waste_u235 = new ItemDepletedFuel("waste_u235").setCreativeTab(MainRegistry.partsTab);
    public static final Item waste_schrabidium = new ItemDepletedFuel("waste_schrabidium").setCreativeTab(MainRegistry.partsTab);
    public static final Item waste_zfb_mox = new ItemDepletedFuel("waste_zfb_mox").setCreativeTab(MainRegistry.partsTab);
    public static final Item waste_plate_u233 = new ItemDepletedFuel("waste_plate_u233").setCreativeTab(MainRegistry.partsTab);
    public static final Item waste_plate_u235 = new ItemDepletedFuel("waste_plate_u235").setCreativeTab(MainRegistry.partsTab);
    public static final Item waste_plate_mox = new ItemDepletedFuel("waste_plate_mox").setCreativeTab(MainRegistry.partsTab);
    public static final Item waste_plate_pu239 = new ItemDepletedFuel("waste_plate_pu239").setCreativeTab(MainRegistry.partsTab);
    public static final Item waste_plate_sa326 = new ItemDepletedFuel("waste_plate_sa326").setCreativeTab(MainRegistry.partsTab);
    public static final Item waste_plate_ra226be = new ItemDepletedFuel("waste_plate_ra226be").setCreativeTab(MainRegistry.partsTab);
    public static final Item waste_plate_pu238be = new ItemDepletedFuel("waste_plate_pu238be").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_fuel_u233 = new ItemPlateFuel(2200000, "plate_fuel_u233").setFunction(ItemPlateFuel.FunctionEnum.SQUARE_ROOT, 50).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item plate_fuel_u235 = new ItemPlateFuel(2200000, "plate_fuel_u235").setFunction(ItemPlateFuel.FunctionEnum.SQUARE_ROOT, 40).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item plate_fuel_mox = new ItemPlateFuel(2400000, "plate_fuel_mox").setFunction(ItemPlateFuel.FunctionEnum.LOGARITHM, 50).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item plate_fuel_pu239 = new ItemPlateFuel(2000000, "plate_fuel_pu239").setFunction(ItemPlateFuel.FunctionEnum.NEGATIVE_QUADRATIC, 50).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item plate_fuel_sa326 = new ItemPlateFuel(2000000, "plate_fuel_sa326").setFunction(ItemPlateFuel.FunctionEnum.LINEAR, 80).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item plate_fuel_ra226be = new ItemPlateFuel(1300000, "plate_fuel_ra226be").setFunction(ItemPlateFuel.FunctionEnum.PASSIVE, 30).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item plate_fuel_pu238be = new ItemPlateFuel(1000000, "plate_fuel_pu238be").setFunction(ItemPlateFuel.FunctionEnum.PASSIVE, 50).setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item pwr_fuel = new ItemPWRFuel().setCreativeTab(MainRegistry.controlTab);
    public static final Item pwr_fuel_hot = new ItemEnumMulti("pwr_fuel_hot", ItemPWRFuel.EnumPWRFuel.class, true, "pwr_fuel_hot").setCreativeTab(MainRegistry.controlTab);
    public static final Item pwr_fuel_depleted = new ItemEnumMulti("pwr_fuel_depleted", ItemPWRFuel.EnumPWRFuel.class, true, "pwr_fuel_depleted").setCreativeTab(MainRegistry.controlTab);
    public static final Item rod = new ItemBreedingRod("rod").setContainerItem(ModItems.rod_empty).setCreativeTab(MainRegistry.controlTab);
    public static final Item rod_dual = new ItemBreedingRod("rod_dual").setContainerItem(ModItems.rod_dual_empty).setCreativeTab(MainRegistry.controlTab);
    public static final Item rod_quad = new ItemBreedingRod("rod_quad").setContainerItem(ModItems.rod_quad_empty).setCreativeTab(MainRegistry.controlTab);
    //RTG
    public static final Item pellet_rtg_depleted = new ItemEnumMulti("pellet_rtg_depleted", EnumDepletedRTGMaterial.class, true, true).setContainerItem(ModItems.plate_iron).setCreativeTab(MainRegistry.controlTab);

    public static final Item pellet_rtg_radium = new ItemRTGPellet(3, "pellet_rtg_radium").setDecays(EnumDepletedRTGMaterial.LEAD, 14016000000L, 1).setCreativeTab(MainRegistry.controlTab);
    public static final Item pellet_rtg_weak = new ItemRTGPellet(5, "pellet_rtg_weak").setDecays(EnumDepletedRTGMaterial.LEAD, 876000000L, 2).setCreativeTab(MainRegistry.controlTab);
    public static final Item pellet_rtg = new ItemRTGPellet(10, "pellet_rtg").setDecays(EnumDepletedRTGMaterial.LEAD, 768252000L, 2).setCreativeTab(MainRegistry.controlTab);
    public static final Item pellet_rtg_strontium = new ItemRTGPellet(12, "pellet_rtg_strontium").setDecays(EnumDepletedRTGMaterial.ZIRCONIUM, 252200400L, 2).setCreativeTab(MainRegistry.controlTab);
    public static final Item pellet_rtg_cobalt = new ItemRTGPellet(16, "pellet_rtg_cobalt").setDecays(EnumDepletedRTGMaterial.ZIRCONIUM, 46176588L, 2).setCreativeTab(MainRegistry.controlTab);
    public static final Item pellet_rtg_actinium = new ItemRTGPellet(20, "pellet_rtg_actinium").setDecays(EnumDepletedRTGMaterial.LEAD, 190705200L, 2).setCreativeTab(MainRegistry.controlTab);
    public static final Item pellet_rtg_americium = new ItemRTGPellet(25, "pellet_rtg_americium").setDecays(EnumDepletedRTGMaterial.NEPTUNIUM, 3786072000L, 2).setCreativeTab(MainRegistry.controlTab);
    public static final Item pellet_rtg_polonium = new ItemRTGPellet(50, "pellet_rtg_polonium").setDecays(EnumDepletedRTGMaterial.LEAD, 3321024L, 3).setCreativeTab(MainRegistry.controlTab);
    public static final Item pellet_rtg_gold = new ItemRTGPellet(200, "pellet_rtg_gold").setDecays(EnumDepletedRTGMaterial.MERCURY, 64728L, 4).setCreativeTab(MainRegistry.controlTab);
    public static final Item pellet_rtg_lead = new ItemRTGPellet(600, "pellet_rtg_lead").setDecays(EnumDepletedRTGMaterial.BISMUTH, 3253L, 6).setCreativeTab(MainRegistry.controlTab);
    public static final Item pellet_rtg_balefire = new ItemRTGPellet(6000, "pellet_rtg_balefire").setDecays(EnumDepletedRTGMaterial.NEPTUNIUM, 1000L, 12).setCreativeTab(MainRegistry.controlTab);
    //Generic Items
    public static final Item pellet_coal = new ItemFuel("pellet_coal", 320).setCreativeTab(MainRegistry.partsTab);
    public static final Item chlorine_pinwheel = new ItemFluidContainerInfinite(Fluids.CHLORINE, 1, 2, "chlorine_pinwheel").setCreativeTab(MainRegistry.partsTab);
    public static final Item ring_starmetal = new ItemBase("ring_starmetal").setCreativeTab(MainRegistry.partsTab);
    public static final Item flywheel_beryllium = new ItemBase("flywheel_beryllium").setCreativeTab(MainRegistry.partsTab);
    public static final Item biomass = new ItemFuel("biomass", 20).setCreativeTab(MainRegistry.partsTab);
    public static final Item biomass_compressed = new ItemFuel("biomass_compressed", 800).setCreativeTab(MainRegistry.partsTab);
    public static final Item ball_resin = new ItemFuel("ball_resin", 80).setCreativeTab(MainRegistry.partsTab);
    public static final Item cordite = new ItemBase("cordite").setCreativeTab(MainRegistry.partsTab);
    public static final Item ballistite = new ItemBase("ballistite").setCreativeTab(MainRegistry.partsTab);
    public static final Item ball_dynamite = new ItemBase("ball_dynamite").setCreativeTab(MainRegistry.partsTab);
    public static final Item ball_tnt = new ItemBase("ball_tnt").setCreativeTab(MainRegistry.partsTab);
    public static final Item ball_tatb = new ItemBase("ball_tatb").setCreativeTab(MainRegistry.partsTab);
    public static final Item ball_fireclay = new ItemBase("ball_fireclay").setCreativeTab(MainRegistry.partsTab);
    public static final Item plant_item = new ItemEnumMulti("plant_item", EnumPlantType.class, true, true).setCreativeTab(MainRegistry.partsTab);

    //Autogen
    public static final Item mold_base = new ItemBase("mold_base").setCreativeTab(MainRegistry.controlTab);
    public static final Item mold = new ItemMold("mold");
    public static final Item scraps = new ItemScraps("scraps").aot(Mats.MAT_BISMUTH, "scraps_bismuth").setCreativeTab(MainRegistry.partsTab);
    public static final Item shell = new ItemAutogen(MaterialShapes.SHELL, "shell").setCreativeTab(MainRegistry.partsTab);
    public static final Item pipe = new ItemAutogen(MaterialShapes.PIPE, "pipe").setCreativeTab(MainRegistry.partsTab);
    public static final Item ingot_raw = new ItemAutogen(MaterialShapes.INGOT, "ingot_raw").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_cast = new ItemAutogen(MaterialShapes.CASTPLATE, "plate_cast").aot(Mats.MAT_BISMUTH, "plate_cast_bismuth").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_welded = new ItemAutogen(MaterialShapes.WELDEDPLATE, "plate_welded").setCreativeTab(MainRegistry.partsTab);
    public static final Item heavy_component = new ItemAutogen(MaterialShapes.HEAVY_COMPONENT, "heavy_component").setCreativeTab(MainRegistry.partsTab);
    public static final Item wire_fine = new ItemAutogen(MaterialShapes.WIRE, "wire_fine")
            .aot(Mats.MAT_ALUMINIUM, "wire_aluminium").aot(Mats.MAT_COPPER, "wire_copper")
            .aot(Mats.MAT_MINGRADE, "wire_red_copper").aot(Mats.MAT_GOLD, "wire_gold")
            .aot(Mats.MAT_TUNGSTEN, "wire_tungsten").aot(Mats.MAT_ALLOY, "wire_advanced_alloy")
            .aot(Mats.MAT_CARBON, "wire_carbon").aot(Mats.MAT_SCHRABIDIUM, "wire_schrabidium")
            .aot(Mats.MAT_MAGTUNG, "wire_magnetized_tungsten").setCreativeTab(MainRegistry.partsTab);
    public static final Item wire_dense = new ItemAutogen(MaterialShapes.DENSEWIRE, "wire_dense").setCreativeTab(MainRegistry.partsTab);
    public static final Item bolt = new ItemAutogen(MaterialShapes.BOLT, "bolt").oun("boltntm").setCreativeTab(MainRegistry.partsTab);
    public static final Item bolt_spike = new ItemCustomLore("bolt_spike").setCreativeTab(MainRegistry.partsTab);
    public static final Item part_barrel_light = new ItemAutogen(MaterialShapes.LIGHTBARREL, "part_barrel_light").setCreativeTab(MainRegistry.partsTab);
    public static final Item part_barrel_heavy = new ItemAutogen(MaterialShapes.HEAVYBARREL, "part_barrel_heavy").setCreativeTab(MainRegistry.partsTab);
    public static final Item part_receiver_light = new ItemAutogen(MaterialShapes.LIGHTRECEIVER, "part_receiver_light").setCreativeTab(MainRegistry.partsTab);
    public static final Item part_receiver_heavy = new ItemAutogen(MaterialShapes.HEAVYRECEIVER, "part_receiver_heavy").setCreativeTab(MainRegistry.partsTab);
    public static final Item part_mechanism = new ItemAutogen(MaterialShapes.MECHANISM, "part_mechanism").setCreativeTab(MainRegistry.partsTab);
    public static final Item part_stock = new ItemAutogen(MaterialShapes.STOCK, "part_stock").setCreativeTab(MainRegistry.partsTab);
    public static final Item part_grip = new ItemAutogen(MaterialShapes.GRIP, "part_grip").setCreativeTab(MainRegistry.partsTab);
    public static final Item casing = new ItemEnumMulti("casing", ItemEnums.EnumCasingType.class, true, true).setCreativeTab(MainRegistry.partsTab);
    //Bedrock Ores (new, gottverdammt)
    public static final Item bedrock_ore = new ItemBedrockOreNew("bedrock_ore_new").setCreativeTab(MainRegistry.partsTab);
    public static final Item bedrock_ore_base = new ItemBedrockOreBase("bedrock_ore_base").setCreativeTab(MainRegistry.partsTab);
    public static final Item bedrock_ore_fragment = new ItemAutogen(MaterialShapes.FRAGMENT, "bedrock_ore_fragment").aot(Mats.MAT_BISMUTH, "bedrock_ore_fragment_bismuth").setCreativeTab(MainRegistry.partsTab);
    public static final Item chunk_ore = new ItemEnumMulti("chunk_ore", EnumChunkType.class, true, true).setCreativeTab(MainRegistry.partsTab);
    public static final Item neutron_reflector = new ItemBase("neutron_reflector").setCreativeTab(MainRegistry.partsTab);
    public static final Item rtg_unit = new ItemBase("rtg_unit").setCreativeTab(MainRegistry.partsTab);
    public static final Item thermo_unit_empty = new ItemBase("thermo_unit_empty").setCreativeTab(MainRegistry.partsTab);
    public static final Item thermo_unit_endo = new ItemCustomLore("thermo_unit_endo").setCreativeTab(MainRegistry.partsTab);
    public static final Item thermo_unit_exo = new ItemCustomLore("thermo_unit_exo").setCreativeTab(MainRegistry.partsTab);
    public static final Item levitation_unit = new ItemBase("levitation_unit").setCreativeTab(MainRegistry.partsTab);
    public static final Item magnetron = new ItemCustomLore("magnetron", "magnetron_alt").setCreativeTab(MainRegistry.partsTab);
    public static final Item pellet_buckshot = new ItemBase("pellet_buckshot").setCreativeTab(MainRegistry.partsTab);
    public static final Item pellet_flechette = new ItemBase("pellet_flechette").setCreativeTab(MainRegistry.partsTab);
    public static final Item pellet_chlorophyte = new ItemBase("pellet_chlorophyte").setCreativeTab(MainRegistry.partsTab);
    public static final Item pellet_meteorite = new ItemBase("pellet_meteorite").setCreativeTab(MainRegistry.partsTab);
    public static final Item pellet_canister = new ItemBase("pellet_canister").setCreativeTab(MainRegistry.partsTab);
    public static final Item pellet_claws = new ItemBase("pellet_claws").setCreativeTab(MainRegistry.partsTab);
    public static final Item pellet_charged = new ItemCustomLore("pellet_charged", "pellets_charged").setCreativeTab(MainRegistry.partsTab);
    //Powders
    public static final Item pellet_cluster = new ItemCustomLore("pellet_cluster").setCreativeTab(MainRegistry.partsTab);
    public static final Item pellet_gas = new ItemCustomLore("pellet_gas").setCreativeTab(MainRegistry.partsTab);
    public static final Item coal_infernal = new ItemFuel("coal_infernal", 4800).setCreativeTab(MainRegistry.partsTab);
    public static final Item cinnabar = new ItemBase("cinnabar").setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_ash = new ItemEnumMulti("powder_ash", EnumAshType.class, true, true).setCreativeTab(MainRegistry.partsTab);
    public static final Item powder_cement = new ItemLemon(2, 0.5F, false, "powder_cement").setCreativeTab(MainRegistry.partsTab);
    //Misc/crafting items
    public static final Item toothpicks = new ItemBase("toothpicks").setCreativeTab(MainRegistry.partsTab);
    public static final Item ducttape = new ItemBase("ducttape").setCreativeTab(MainRegistry.partsTab);
    public static final Item catalyst_clay = new ItemBase("catalyst_clay").setCreativeTab(MainRegistry.partsTab);
    public static final Item motor = new ItemBakedBase("motor").setCreativeTab(MainRegistry.partsTab);
    public static final Item motor_desh = new ItemBakedBase("motor_desh").setCreativeTab(MainRegistry.partsTab);
    public static final Item motor_bismuth = new ItemBakedBase("motor_bismuth").setCreativeTab(MainRegistry.partsTab);
    public static final Item photo_panel = new ItemBase("photo_panel").setCreativeTab(MainRegistry.partsTab);
    public static final Item sat_base = new ItemBase("sat_base").setCreativeTab(MainRegistry.partsTab);
    public static final Item thruster_nuclear = new ItemBase("thruster_nuclear").setCreativeTab(MainRegistry.partsTab);
    public static final Item safety_fuse = new ItemBakedBase("safety_fuse").setCreativeTab(MainRegistry.partsTab);
    public static final Item blade_titanium = new ItemBase("blade_titanium").setCreativeTab(MainRegistry.partsTab);
    public static final Item turbine_titanium = new ItemBase("turbine_titanium").setCreativeTab(MainRegistry.partsTab);
    public static final Item blade_tungsten = new ItemBase("blade_tungsten").setCreativeTab(MainRegistry.partsTab);
    public static final Item turbine_tungsten = new ItemBase("turbine_tungsten").setCreativeTab(MainRegistry.partsTab);
    public static final Item board_copper = new ItemBase("board_copper").setCreativeTab(MainRegistry.partsTab);
    public static final Item pipes_steel = new ItemBase("pipes_steel").setCreativeTab(MainRegistry.partsTab);
    public static final Item drill_titanium = new ItemBase("drill_titanium").setCreativeTab(MainRegistry.partsTab);
    public static final Item bolt_compound = new ItemBase("bolt_compound").setCreativeTab(MainRegistry.partsTab);
    public static final Item hazmat_cloth = new ItemBase("hazmat_cloth").setCreativeTab(MainRegistry.partsTab);
    public static final Item hazmat_cloth_red = new ItemBase("hazmat_cloth_red").setCreativeTab(MainRegistry.partsTab);
    public static final Item hazmat_cloth_grey = new ItemBase("hazmat_cloth_grey").setCreativeTab(MainRegistry.partsTab);
    public static final Item asbestos_cloth = new ItemBase("asbestos_cloth").setCreativeTab(MainRegistry.partsTab);
    public static final Item rag = new ItemRag("rag").setCreativeTab(MainRegistry.partsTab);
    public static final Item rag_damp = new ItemBase("rag_damp").setCreativeTab(MainRegistry.partsTab);
    public static final Item rag_piss = new ItemBase("rag_piss").setCreativeTab(MainRegistry.partsTab);
    public static final Item filter_coal = new ItemBase("filter_coal").setCreativeTab(MainRegistry.partsTab);
    public static final Item magnet_circular = new ItemBase("magnet_circular").setCreativeTab(MainRegistry.partsTab);
    public static final Item centrifuge_element = new ItemBase("centrifuge_element").setCreativeTab(MainRegistry.partsTab);
    public static final Item reactor_core = new ItemBase("reactor_core").setCreativeTab(MainRegistry.partsTab);
    //Fuels
    public static final Item oil_tar = new ItemEnumMulti("oil_tar", EnumTarType.class, true, true).setCreativeTab(MainRegistry.partsTab);
    public static final Item solid_fuel = new ItemFuel("solid_fuel", 3200).setCreativeTab(MainRegistry.partsTab);
    public static final Item solid_fuel_presto = new ItemFuel("solid_fuel_presto", 6400).setCreativeTab(MainRegistry.partsTab);
    public static final Item solid_fuel_presto_triplet = new ItemFuel("solid_fuel_presto_triplet", 19200).setCreativeTab(MainRegistry.partsTab);
    public static final Item solid_fuel_bf = new ItemBakedBase("solid_fuel_bf").setCreativeTab(MainRegistry.partsTab);
    public static final Item solid_fuel_presto_bf = new ItemBakedBase("solid_fuel_presto_bf").setCreativeTab(MainRegistry.partsTab);
    public static final Item solid_fuel_presto_triplet_bf = new ItemBakedBase("solid_fuel_presto_triplet_bf").setCreativeTab(MainRegistry.partsTab);
    public static final Item rocket_fuel = new ItemFuel("rocket_fuel", 6400).setCreativeTab(MainRegistry.partsTab);
    public static final Item briquette = new ItemEnumMulti("briquette", EnumBriquetteType.class, true, true).setCreativeTab(MainRegistry.partsTab);
    public static final Item coke = new ItemEnumMulti("coke", EnumCokeType.class, true, true).setCreativeTab(MainRegistry.partsTab);
    public static final Item lignite = new ItemFuel("lignite", 1200).setCreativeTab(MainRegistry.partsTab);
    //Circuit
    public static final Item circuit = new ItemEnumMulti("circuit", EnumCircuitType.class, true, true).setCreativeTab(MainRegistry.partsTab);
    //Fragments
    public static final Item fragment_neodymium = new ItemBase("fragment_neodymium").setCreativeTab(MainRegistry.partsTab);
    public static final Item fragment_cobalt = new ItemBase("fragment_cobalt").setCreativeTab(MainRegistry.partsTab);
    public static final Item fragment_niobium = new ItemBase("fragment_niobium").setCreativeTab(MainRegistry.partsTab);
    public static final Item fragment_cerium = new ItemBase("fragment_cerium").setCreativeTab(MainRegistry.partsTab);
    public static final Item fragment_lanthanium = new ItemBase("fragment_lanthanium").setCreativeTab(MainRegistry.partsTab);
    public static final Item fragment_actinium = new ItemBase("fragment_actinium").setCreativeTab(MainRegistry.partsTab);
    public static final Item fragment_meteorite = new ItemBase("fragment_meteorite").setCreativeTab(MainRegistry.partsTab);
    public static final Item fragment_boron = new ItemBase("fragment_boron").setCreativeTab(MainRegistry.partsTab);
    public static final Item fragment_coltan = new ItemBase("fragment_coltan").setCreativeTab(MainRegistry.partsTab);
    public static final Item demon_core_open = new ItemDemonCore("demon_core_open").setCreativeTab(MainRegistry.nukeTab);
    public static final Item demon_core_closed = new ItemCustomLore("demon_core_closed").setCreativeTab(MainRegistry.nukeTab);
    //Consume
    public static final Item bottle_opener = new WeaponSpecial(MainRegistry.enumToolMaterialBottleOpener, "bottle_opener").setCreativeTab(MainRegistry.consumableTab).setMaxStackSize(1);
    public static final Item bottle_empty = new ItemBase("bottle_empty").setCreativeTab(MainRegistry.consumableTab);
    public static final Item bottle_nuka = new ItemEnergy("bottle_nuka").setContainerItem(bottle_empty).setCreativeTab(MainRegistry.consumableTab);
    public static final Item bottle_cherry = new ItemEnergy("bottle_cherry").setContainerItem(ModItems.bottle_empty).setCreativeTab(MainRegistry.consumableTab);
    public static final Item bottle_quantum = new ItemEnergy("bottle_quantum").setContainerItem(ModItems.bottle_empty).setCreativeTab(MainRegistry.consumableTab);
    public static final Item bottle_sparkle = new ItemEnergy("bottle_sparkle").setContainerItem(ModItems.bottle_empty).setCreativeTab(MainRegistry.consumableTab);
    public static final Item bottle_rad = new ItemEnergy("bottle_rad").setContainerItem(ModItems.bottle_empty).setCreativeTab(MainRegistry.consumableTab);
    public static final Item bottle2_empty = new ItemBase("bottle2_empty").setCreativeTab(MainRegistry.consumableTab);
    public static final Item bottle2_korl = new ItemEnergy("bottle2_korl").setContainerItem(ModItems.bottle2_empty).setCreativeTab(MainRegistry.consumableTab);
    public static final Item bottle2_fritz = new ItemEnergy("bottle2_fritz").setContainerItem(ModItems.bottle2_empty).setCreativeTab(MainRegistry.consumableTab);
    public static final Item bottle2_korl_special = new ItemEnergy("bottle2_korl_special").setContainerItem(ModItems.bottle2_empty).setCreativeTab(MainRegistry.consumableTab);
    public static final Item bottle2_fritz_special = new ItemEnergy("bottle2_fritz_special").setContainerItem(ModItems.bottle2_empty).setCreativeTab(MainRegistry.consumableTab);
    public static final Item bottle2_sunset = new ItemEnergy("bottle2_sunset").setContainerItem(ModItems.bottle2_empty).setCreativeTab(MainRegistry.consumableTab);
    public static final Item flask_infusion = new ItemFlask("flask_infusion").setCreativeTab(MainRegistry.consumableTab);
    public static final Item chocolate_milk = new ItemEnergy("chocolate_milk").setCreativeTab(MainRegistry.consumableTab);
    public static final Item coffee = new ItemEnergy("coffee").setCreativeTab(MainRegistry.consumableTab);
    public static final Item coffee_radium = new ItemEnergy("coffee_radium").setCreativeTab(MainRegistry.consumableTab);
    public static final Item chocolate = new ItemPill(0, "chocolate").setCreativeTab(MainRegistry.consumableTab);
    public static final Item cap_nuka = new ItemBase("cap_nuka").setCreativeTab(MainRegistry.consumableTab);
    public static final Item cap_quantum = new ItemBase("cap_quantum").setCreativeTab(MainRegistry.consumableTab);
    public static final Item cap_sparkle = new ItemBase("cap_sparkle").setCreativeTab(MainRegistry.consumableTab);
    public static final Item cap_rad = new ItemBase("cap_rad").setCreativeTab(MainRegistry.consumableTab);
    public static final Item cap_korl = new ItemBase("cap_korl").setCreativeTab(MainRegistry.consumableTab);
    public static final Item cap_fritz = new ItemBase("cap_fritz").setCreativeTab(MainRegistry.consumableTab);
    public static final Item cap_sunset = new ItemBase("cap_sunset").setCreativeTab(MainRegistry.consumableTab);
    public static final Item cap_star = new ItemBase("cap_star").setCreativeTab(MainRegistry.consumableTab);
    public static final Item ring_pull = new ItemBase("ring_pull").setCreativeTab(MainRegistry.consumableTab);
    public static final Item bomb_waffle = new ItemFoodBase(20, 0, true, "bomb_waffle").setCreativeTab(MainRegistry.consumableTab);
    public static final Item schnitzel_vegan = new ItemFoodBase(3, 6, true, "schnitzel_vegan").setCreativeTab(MainRegistry.consumableTab);
    public static final Item cotton_candy = new ItemFoodBase(5, 0, true, "cotton_candy").setCreativeTab(MainRegistry.consumableTab).setFull3D();
    public static final Item apple_lead = new ItemAppleSchrabidium(5, 0, false, "apple_lead").setCreativeTab(MainRegistry.consumableTab);
    public static final Item apple_schrabidium = new ItemAppleSchrabidium(20, 100, false, "apple_schrabidium").setCreativeTab(MainRegistry.consumableTab);
    public static final Item tem_flakes = new ItemTemFlakes(0, 0, false, "tem_flakes").setCreativeTab(MainRegistry.consumableTab);
    public static final Item glowing_stew = new ItemFoodSoup(6, "glowing_stew").setCreativeTab(MainRegistry.consumableTab);
    public static final Item balefire_scrambled = new ItemFoodSoup(30, "balefire_scrambled").setCreativeTab(MainRegistry.consumableTab);
    public static final Item balefire_and_ham = new ItemFoodSoup(60, "balefire_and_ham").setCreativeTab(MainRegistry.consumableTab);
    public static final Item lemon = new ItemLemon(3, 5, false, "lemon").setCreativeTab(MainRegistry.consumableTab);
    public static final Item definitelyfood = new ItemLemon(3, 0.5F, false, "definitelyfood").setCreativeTab(MainRegistry.consumableTab);
    public static final Item med_ipecac = new ItemLemon(0, 0, false, "med_ipecac").setCreativeTab(MainRegistry.consumableTab);
    public static final Item med_ptsd = new ItemLemon(0, 0, false, "med_ptsd").setCreativeTab(MainRegistry.consumableTab);
    public static final Item med_schizophrenia = new ItemLemon(0, 0, false, "med_schizophrenia").setCreativeTab(null);
    public static final Item loops = new ItemLemon(4, 5, false, "loops").setCreativeTab(MainRegistry.consumableTab);
    public static final Item loop_stew = new ItemLemon(10, 10, false, "loop_stew").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item fooditem = new ItemLemon(2, 5, false, "fooditem").setCreativeTab(null);
    public static final Item twinkie = new ItemLemon(3, 5, false, "twinkie").setCreativeTab(MainRegistry.consumableTab);
    public static final Item static_sandwich = new ItemLemon(6, 5, false, "static_sandwich").setCreativeTab(MainRegistry.consumableTab);
    public static final Item canteen_13 = new ItemCanteen(60, "canteen_13").setCreativeTab(MainRegistry.consumableTab);
    public static final Item canteen_vodka = new ItemCanteen(3 * 60, "canteen_vodka").setCreativeTab(MainRegistry.consumableTab);
    public static final Item canteen_fab = new ItemCanteen(2 * 60, "canteen_fab").setCreativeTab(MainRegistry.consumableTab);
    public static final Item pancake = new ItemPancake(20, 20, false, "pancake").setCreativeTab(MainRegistry.consumableTab);
    public static final Item nugget = new ItemLemon(200, 200, false, "nugget").setCreativeTab(MainRegistry.consumableTab);
    public static final Item peas = new ItemPeas("peas").setCreativeTab(MainRegistry.consumableTab);
    public static final Item marshmallow = new ItemLemon(2, 2, false, "marshmallow").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.consumableTab);
    public static final Item cheese = new ItemLemon(5, 0.75F, false, "cheese").setCreativeTab(MainRegistry.consumableTab);
    public static final Item quesadilla = new ItemLemon(8, 1F, false, "cheese_quesadilla").setCreativeTab(MainRegistry.consumableTab);
    public static final Item mucho_mango = new ItemMuchoMango(10, "mucho_mango").setCreativeTab(MainRegistry.consumableTab);
    public static final Item marshmallow_roasted = new ItemLemon(6, 6, false, "marshmallow_roasted").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.consumableTab);
    public static final Item glyphid_meat = new ItemLemon(3, 0.5F, true, "glyphid_meat").setCreativeTab(MainRegistry.consumableTab);
    public static final Item glyphid_meat_grilled = new ItemLemon(8, 0.75F, true, "glyphid_meat_grilled").setPotionEffect(new PotionEffect(Objects.requireNonNull(Potion.getPotionFromResourceLocation("strength")), 180, 1), 1F).setCreativeTab(MainRegistry.consumableTab);
    public static final Item egg_glyphid = new ItemBase("egg_glyphid").setCreativeTab(MainRegistry.consumableTab);
    //Bongespob arcarmoni
    public static final Item spongebob_macaroni = new ItemLemon(5, 5, false, "spongebob_macaroni").setCreativeTab(MainRegistry.consumableTab);
    public static final ItemEnumMultiFood canned_conserve = (ItemEnumMultiFood) new ItemConserve().setCreativeTab(MainRegistry.consumableTab);
    public static final Item can_key = new ItemBase("can_key").setCreativeTab(MainRegistry.consumableTab);
    public static final Item coin_maskman = new ItemCustomLore("coin_maskman").setRarity(EnumRarity.UNCOMMON).setCreativeTab(MainRegistry.consumableTab);
    public static final Item coin_creeper = new ItemCustomLore("coin_creeper").setRarity(EnumRarity.UNCOMMON).setCreativeTab(MainRegistry.consumableTab);
    public static final Item coin_radiation = new ItemCustomLore("coin_radiation").setRarity(EnumRarity.UNCOMMON).setCreativeTab(MainRegistry.consumableTab);
    public static final Item coin_worm = new ItemCustomLore("coin_worm").setRarity(EnumRarity.UNCOMMON).setCreativeTab(MainRegistry.consumableTab);
    public static final Item coin_ufo = new ItemCustomLore("coin_ufo").setRarity(EnumRarity.UNCOMMON).setCreativeTab(MainRegistry.consumableTab);
    public static final Item coin_siege = new ItemSiegeCoin("coin_siege").setCreativeTab(MainRegistry.consumableTab);
    public static final Item pudding = new ItemLemon(6, 15, false, "pudding").setCreativeTab(MainRegistry.consumableTab);
    public static final Item can_empty = new ItemBase("can_empty").setCreativeTab(MainRegistry.consumableTab);
    public static final Item can_smart = new ItemEnergy("can_smart").setContainerItem(ModItems.can_empty).setCreativeTab(MainRegistry.consumableTab);
    public static final Item can_creature = new ItemEnergy("can_creature").setContainerItem(ModItems.can_empty).setCreativeTab(MainRegistry.consumableTab);
    public static final Item can_redbomb = new ItemEnergy("can_redbomb").setContainerItem(ModItems.can_empty).setCreativeTab(MainRegistry.consumableTab);
    public static final Item can_mrsugar = new ItemEnergy("can_mrsugar").setContainerItem(ModItems.can_empty).setCreativeTab(MainRegistry.consumableTab);
    public static final Item can_overcharge = new ItemEnergy("can_overcharge").setContainerItem(ModItems.can_empty).setCreativeTab(MainRegistry.consumableTab);
    public static final Item can_luna = new ItemEnergy("can_luna").setContainerItem(ModItems.can_empty).setCreativeTab(MainRegistry.consumableTab);
    public static final Item can_bepis = new ItemEnergy("can_bepis").setContainerItem(ModItems.can_empty).setCreativeTab(MainRegistry.consumableTab);
    public static final Item can_breen = new ItemEnergy("can_breen").setContainerItem(ModItems.can_empty).setCreativeTab(MainRegistry.consumableTab);
    public static final Item drone = new ItemDrone("drone");
    //Tools
    public static final Item titanium_sword = new ItemSwordAbility(6.5F, 0, MainRegistry.enumToolMaterialTitanium, "titanium_sword");
    public static final Item titanium_pickaxe = new ItemToolAbility(4.5F, -2.8F, 0, MainRegistry.enumToolMaterialTitanium, EnumToolType.PICKAXE, "titanium_pickaxe");
    public static final Item titanium_axe = new ItemToolAbility(5.5F, -2.8F, 0, MainRegistry.enumToolMaterialTitanium, EnumToolType.AXE, "titanium_axe")
            .addAbility(IWeaponAbility.BEHEADER, 0).setMaxStackSize(1);
    public static final Item titanium_shovel = new ItemToolAbility(3.5F, -2.8F, 0, MainRegistry.enumToolMaterialTitanium, EnumToolType.SHOVEL, "titanium_shovel");
    public static final Item titanium_hoe = new ModHoe(MainRegistry.enumToolMaterialTitanium, "titanium_hoe").setMaxStackSize(1).setCreativeTab(CreativeTabs.TOOLS);
    public static final Item steel_sword = new ItemSwordAbility(6F, 0, MainRegistry.enumToolMaterialSteel, "steel_sword");
    public static final Item steel_pickaxe = new ItemToolAbility(4F, -2.8F, 0, MainRegistry.enumToolMaterialSteel, EnumToolType.PICKAXE, "steel_pickaxe");
    public static final Item steel_axe = new ItemToolAbility(5F, -2.8F, 0, MainRegistry.enumToolMaterialSteel, EnumToolType.AXE, "steel_axe")
            .addAbility(IWeaponAbility.BEHEADER, 0);
    public static final Item steel_shovel = new ItemToolAbility(3F, -2.8F, 0, MainRegistry.enumToolMaterialSteel, EnumToolType.SHOVEL, "steel_shovel");
    public static final Item steel_hoe = new ModHoe(MainRegistry.enumToolMaterialSteel, "steel_hoe").setMaxStackSize(1).setCreativeTab(CreativeTabs.TOOLS);
    public static final Item alloy_sword = new ItemSwordAbility(8F, 0, MainRegistry.enumToolMaterialAlloy, "alloy_sword")
            .addAbility(IWeaponAbility.STUN, 0);
    public static final Item alloy_pickaxe = new ItemToolAbility(5F, -2.8F, 0, MainRegistry.enumToolMaterialAlloy, EnumToolType.PICKAXE, "alloy_pickaxe")
            .addAbility(IToolAreaAbility.RECURSION, 0);
    public static final Item alloy_axe = new ItemToolAbility(7F, -2.8F, 0, MainRegistry.enumToolMaterialAlloy, EnumToolType.AXE, "alloy_axe")
            .addAbility(IToolAreaAbility.RECURSION, 0)
            .addAbility(IWeaponAbility.BEHEADER, 0);
    public static final Item alloy_shovel = new ItemToolAbility(4F, -2.8F, 0, MainRegistry.enumToolMaterialAlloy, EnumToolType.SHOVEL, "alloy_shovel")
            .addAbility(IToolAreaAbility.RECURSION, 0);
    public static final Item alloy_hoe = new ModHoe(MainRegistry.enumToolMaterialAlloy, "alloy_hoe").setCreativeTab(CreativeTabs.TOOLS);
    public static final Item elec_sword = new ItemSwordAbilityPower(12.5F, 0, MainRegistry.enumToolMaterialElec, 500000, 1000, 100, "elec_sword")
            .addAbility(IWeaponAbility.STUN, 2);
    public static final Item elec_pickaxe = new ItemToolAbilityPower(10F, -2.8F, 0, MainRegistry.enumToolMaterialElec, EnumToolType.PICKAXE, 500000, 1000, 100, "elec_pickaxe")
            .addAbility(IToolAreaAbility.HAMMER, 1)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
            .addAbility(IToolAreaAbility.RECURSION, 2)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 1);
    public static final Item elec_axe = new ItemToolAbilityPower(10F, -2.8F, 0, MainRegistry.enumToolMaterialElec, EnumToolType.AXE, 500000, 1000, 100, "elec_axe")
            .addAbility(IToolAreaAbility.HAMMER, 1)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
            .addAbility(IToolAreaAbility.RECURSION, 2)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 1)
            .addAbility(IWeaponAbility.CHAINSAW, 0)
            .addAbility(IWeaponAbility.BEHEADER, 0);
    public static final Item elec_shovel = new ItemToolAbilityPower(7.5F, -2.8F, 0, MainRegistry.enumToolMaterialElec, EnumToolType.SHOVEL, 500000, 1000, 100, "elec_shovel")
            .addAbility(IToolAreaAbility.HAMMER, 1)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
            .addAbility(IToolAreaAbility.RECURSION, 2)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 1);
    public static final Item desh_sword = new ItemSwordAbility(15F, 0, MainRegistry.enumToolMaterialDesh, "desh_sword")
            .addAbility(IWeaponAbility.STUN, 0);
    public static final Item desh_pickaxe = new ItemToolAbility(5F, -2.8F, -0.05, MainRegistry.enumToolMaterialDesh, EnumToolType.PICKAXE, "desh_pickaxe")
            .addAbility(IToolAreaAbility.HAMMER, 0)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 0)
            .addAbility(IToolAreaAbility.RECURSION, 0)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 1);
    public static final Item desh_axe = new ItemToolAbility(6.5F, -2.8F, -0.05, MainRegistry.enumToolMaterialDesh, EnumToolType.AXE, "desh_axe")
            .addAbility(IToolAreaAbility.HAMMER, 0)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 0)
            .addAbility(IToolAreaAbility.RECURSION, 0)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 1)
            .addAbility(IWeaponAbility.BEHEADER, 0);
    public static final Item desh_shovel = new ItemToolAbility(4F, -2.8F, -0.05, MainRegistry.enumToolMaterialDesh, EnumToolType.SHOVEL, "desh_shovel")
            .addAbility(IToolAreaAbility.HAMMER, 0)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 0)
            .addAbility(IToolAreaAbility.RECURSION, 0)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 1);
    public static final Item desh_hoe = new ModHoe(MainRegistry.enumToolMaterialDesh, "desh_hoe").setCreativeTab(CreativeTabs.TOOLS);
    public static final Item cobalt_sword = new ItemSwordAbility(12F, 0, MainRegistry.enumToolMaterialCobalt, "cobalt_sword");
    public static final Item cobalt_pickaxe = new ItemToolAbility(4F, -2.8F, 0, MainRegistry.enumToolMaterialCobalt, EnumToolType.PICKAXE, "cobalt_pickaxe")
            .addAbility(IToolAreaAbility.RECURSION, 1)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 0);
    public static final Item cobalt_axe = new ItemToolAbility(6F, -2.8F, 0, MainRegistry.enumToolMaterialCobalt, EnumToolType.AXE, "cobalt_axe")
            .addAbility(IToolAreaAbility.RECURSION, 1)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 0)
            .addAbility(IWeaponAbility.BEHEADER, 0);
    public static final Item cobalt_shovel = new ItemToolAbility(3.5F, -2.8F, 0, MainRegistry.enumToolMaterialCobalt, EnumToolType.SHOVEL, "cobalt_shovel")
            .addAbility(IToolAreaAbility.RECURSION, 1)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 0);
    public static final Item cobalt_hoe = new ModHoe(MainRegistry.enumToolMaterialCobalt, "cobalt_hoe").setCreativeTab(CreativeTabs.TOOLS);
    public static final Item centri_stick = new ItemToolAbility(3F, -2.8F, 0, MainRegistry.enumToolMaterialElec, EnumToolType.MINER, "centri_stick")
            .addAbility(IToolHarvestAbility.CENTRIFUGE, 0).setMaxDamage(50);
    public static final Item smashing_hammer = new ItemToolAbility(12F, -2.8F, -0.1, MainRegistry.enumToolMaterialSteel, EnumToolType.MINER, "smashing_hammer")
            .addAbility(IToolHarvestAbility.SHREDDER, 0).setMaxDamage(2500);
    public static final ToolMaterial matDecCobalt = EnumHelper.addToolMaterial("HBM_COBALT2", 4, 1000, 15.0F, 2.5F, 25).setRepairItem(new ItemStack(ModItems.ingot_cobalt));
    public static final Item cobalt_decorated_sword = new ItemSwordAbility(15F, 0, matDecCobalt, "cobalt_decorated_sword")
            .addAbility(IWeaponAbility.BOBBLE, 0);
    public static final Item cobalt_decorated_pickaxe = new ItemToolAbility(6F, -2.8F, 0, matDecCobalt, EnumToolType.PICKAXE, "cobalt_decorated_pickaxe")
            .addAbility(IToolAreaAbility.RECURSION, 1)
            .addAbility(IToolAreaAbility.HAMMER, 0)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 0)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 2);
    public static final Item cobalt_decorated_axe = new ItemToolAbility(8F, -2.8F, 0, matDecCobalt, EnumToolType.AXE, "cobalt_decorated_axe")
            .addAbility(IToolAreaAbility.RECURSION, 1)
            .addAbility(IToolAreaAbility.HAMMER, 0)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 0)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 2)
            .addAbility(IWeaponAbility.BEHEADER, 0);
    public static final Item cobalt_decorated_shovel = new ItemToolAbility(5F, -2.8F, 0, matDecCobalt, EnumToolType.SHOVEL, "cobalt_decorated_shovel")
            .addAbility(IToolAreaAbility.RECURSION, 1)
            .addAbility(IToolAreaAbility.HAMMER, 0)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 0)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 2);
    public static final Item cobalt_decorated_hoe = new ModHoe(matDecCobalt, "cobalt_decorated_hoe").setCreativeTab(CreativeTabs.TOOLS);
    public static final ToolMaterial matStarmetal = EnumHelper.addToolMaterial("HBM_STARMETAL", 3, 1000, 20.0F, 2.5F, 30).setRepairItem(new ItemStack(ModItems.ingot_starmetal));
    public static final Item starmetal_sword = new ItemSwordAbility(25F, 0, matStarmetal, "starmetal_sword")
            .addAbility(IWeaponAbility.BEHEADER, 0)
            .addAbility(IWeaponAbility.STUN, 1)
            .addAbility(IWeaponAbility.BOBBLE, 0);
    public static final Item starmetal_pickaxe = new ItemToolAbility(8F, -2.8F, 0, matStarmetal, EnumToolType.PICKAXE, "starmetal_pickaxe")
            .addAbility(IToolAreaAbility.RECURSION, 3)
            .addAbility(IToolAreaAbility.HAMMER, 1)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 4)
            .addAbility(IWeaponAbility.STUN, 1);
    public static final Item starmetal_axe = new ItemToolAbility(12F, -2.8F, 0, matStarmetal, EnumToolType.AXE, "starmetal_axe")
            .addAbility(IToolAreaAbility.RECURSION, 3)
            .addAbility(IToolAreaAbility.HAMMER, 1)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 4)
            .addAbility(IWeaponAbility.BEHEADER, 0)
            .addAbility(IWeaponAbility.STUN, 1);
    public static final Item starmetal_shovel = new ItemToolAbility(7F, -2.8F, 0, matStarmetal, EnumToolType.SHOVEL, "starmetal_shovel")
            .addAbility(IToolAreaAbility.RECURSION, 3)
            .addAbility(IToolAreaAbility.HAMMER, 1)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 4)
            .addAbility(IWeaponAbility.STUN, 1);
    public static final Item starmetal_hoe = new ModHoe(matStarmetal, "starmetal_hoe").setCreativeTab(CreativeTabs.TOOLS);
    public static final Item cmb_sword = new ItemSwordAbility(35F, 0, MainRegistry.enumToolMaterialCmb, "cmb_sword")
            .addAbility(IWeaponAbility.STUN, 0)
            .addAbility(IWeaponAbility.VAMPIRE, 0);
    public static final Item cmb_pickaxe = new ItemToolAbility(10, -2.8F, 0, MainRegistry.enumToolMaterialCmb, EnumToolType.PICKAXE, "cmb_pickaxe")
            .addAbility(IToolAreaAbility.RECURSION, 2)
            .addAbility(IToolHarvestAbility.SMELTER, 0)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 2);
    public static final Item cmb_axe = new ItemToolAbility(30.0F, -2.8F, 0, MainRegistry.enumToolMaterialCmb, EnumToolType.AXE, "cmb_axe")
            .addAbility(IToolAreaAbility.RECURSION, 2)
            .addAbility(IToolHarvestAbility.SMELTER, 0)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 2)
            .addAbility(IWeaponAbility.BEHEADER, 0);
    public static final Item cmb_shovel = new ItemToolAbility(8, -2.8F, 0, MainRegistry.enumToolMaterialCmb, EnumToolType.SHOVEL, "cmb_shovel")
            .addAbility(IToolAreaAbility.RECURSION, 2)
            .addAbility(IToolHarvestAbility.SMELTER, 0)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 2);
    public static final Item cmb_hoe = new ModHoe(MainRegistry.enumToolMaterialCmb, "cmb_hoe").setMaxStackSize(1).setCreativeTab(CreativeTabs.TOOLS);
    public static final ToolMaterial matBismuth = EnumHelper.addToolMaterial("HBM_BISMUTH", 4, 0, 50F, 0.0F, 200).setRepairItem(new ItemStack(ModItems.ingot_bismuth));
    public static final Item bismuth_axe = new ItemToolAbility(25F, -2.8F, 0, matBismuth, EnumToolType.AXE, "bismuth_axe")
            .addAbility(IToolAreaAbility.HAMMER, 1)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
            .addAbility(IToolAreaAbility.RECURSION, 1)
            .addAbility(IToolHarvestAbility.SHREDDER, 0)
            .addAbility(IToolHarvestAbility.LUCK, 1)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IWeaponAbility.STUN, 3)
            .addAbility(IWeaponAbility.VAMPIRE, 1)
            .addAbility(IWeaponAbility.BEHEADER, 0);
    public static final Item bismuth_pickaxe = new ItemToolAbility(15F, -2.8F, 0, matBismuth, EnumToolType.MINER, "bismuth_pickaxe")
            .addAbility(IToolAreaAbility.HAMMER, 1)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
            .addAbility(IToolAreaAbility.RECURSION, 1)
            .addAbility(IToolHarvestAbility.SHREDDER, 0)
            .addAbility(IToolHarvestAbility.LUCK, 1)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IWeaponAbility.STUN, 2)
            .addAbility(IWeaponAbility.VAMPIRE, 0)
            .addAbility(IWeaponAbility.BEHEADER, 0)
            .setDepthRockBreaker();
    public static final ToolMaterial matVolcano = EnumHelper.addToolMaterial("HBM_VOLCANIC", 4, 0, 50F, 0.0F, 200).setRepairItem(new ItemStack(ModItems.ingot_bismuth));
    public static final Item volcanic_axe = new ItemToolAbility(25F, -2.8F, 0, matVolcano, EnumToolType.AXE, "volcanic_axe")
            .addAbility(IToolAreaAbility.HAMMER, 1)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
            .addAbility(IToolAreaAbility.RECURSION, 1)
            .addAbility(IToolHarvestAbility.SMELTER, 0)
            .addAbility(IToolHarvestAbility.LUCK, 2)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IWeaponAbility.FIRE, 1)
            .addAbility(IWeaponAbility.VAMPIRE, 1)
            .addAbility(IWeaponAbility.BEHEADER, 0);
    public static final Item volcanic_pickaxe = new ItemToolAbility(15F, -2.8F, 0, matVolcano, EnumToolType.MINER, "volcanic_pickaxe")
            .addAbility(IToolAreaAbility.HAMMER, 1)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
            .addAbility(IToolAreaAbility.RECURSION, 1)
            .addAbility(IToolHarvestAbility.SMELTER, 0)
            .addAbility(IToolHarvestAbility.LUCK, 2)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IWeaponAbility.FIRE, 0)
            .addAbility(IWeaponAbility.VAMPIRE, 0)
            .addAbility(IWeaponAbility.BEHEADER, 0)
            .setDepthRockBreaker();
    public static final ToolMaterial matChlorophyte = EnumHelper.addToolMaterial("HBM_CHLOROPHYTE", 5, 0, 50F, 0.0F, 200).setRepairItem(new ItemStack(ModItems.powder_chlorophyte));
    public static final Item chlorophyte_axe = new ItemToolAbility(50F, -2.8F, 0, matChlorophyte, EnumToolType.AXE, "chlorophyte_axe")
            .addAbility(IToolAreaAbility.HAMMER, 1)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
            .addAbility(IToolAreaAbility.RECURSION, 1)
            .addAbility(IToolHarvestAbility.LUCK, 3)
            .addAbility(IWeaponAbility.STUN, 4)
            .addAbility(IWeaponAbility.VAMPIRE, 3)
            .addAbility(IWeaponAbility.BEHEADER, 0);
    public static final Item chlorophyte_pickaxe = new ItemToolAbility(20F, -2.8F, 0, matChlorophyte, EnumToolType.MINER, "chlorophyte_pickaxe")
            .addAbility(IToolAreaAbility.HAMMER, 1)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
            .addAbility(IToolAreaAbility.RECURSION, 1)
            .addAbility(IToolHarvestAbility.LUCK, 3)
            .addAbility(IToolHarvestAbility.CENTRIFUGE, 0)
            .addAbility(IToolHarvestAbility.MERCURY, 0)
            .addAbility(IWeaponAbility.STUN, 3)
            .addAbility(IWeaponAbility.VAMPIRE, 2)
            .addAbility(IWeaponAbility.BEHEADER, 0)
            .setDepthRockBreaker();
    public static final Item schrabidium_hoe = new HoeSchrabidium(MainRegistry.enumToolMaterialSchrabidium, "schrabidium_hoe").setCreativeTab(CreativeTabs.TOOLS);
    public static final Item schrabidium_sword = new ItemSwordAbility(75, 0, MainRegistry.enumToolMaterialSchrabidium, "schrabidium_sword")
            .addAbility(IWeaponAbility.RADIATION, 1)
            .addAbility(IWeaponAbility.VAMPIRE, 0)
            .setRarity(EnumRarity.RARE);
    public static final Item schrabidium_pickaxe = new ItemToolAbility(20, -2.8F, 0, MainRegistry.enumToolMaterialSchrabidium, EnumToolType.PICKAXE, "schrabidium_pickaxe")
            .addAbility(IWeaponAbility.RADIATION, 0)
            .addAbility(IToolAreaAbility.HAMMER, 1)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
            .addAbility(IToolAreaAbility.RECURSION, 6)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 4)
            .addAbility(IToolHarvestAbility.SMELTER, 0)
            .addAbility(IToolHarvestAbility.SHREDDER, 0)
            .setRarity(EnumRarity.RARE);
    public static final Item schrabidium_axe = new ItemToolAbility(25, -2.8F, 0, MainRegistry.enumToolMaterialSchrabidium, EnumToolType.AXE, "schrabidium_axe")
            .addAbility(IWeaponAbility.RADIATION, 0)
            .addAbility(IToolAreaAbility.HAMMER, 1)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
            .addAbility(IToolAreaAbility.RECURSION, 6)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 4)
            .addAbility(IToolHarvestAbility.SMELTER, 0)
            .addAbility(IToolHarvestAbility.SHREDDER, 0)
            .addAbility(IWeaponAbility.BEHEADER, 0)
            .setRarity(EnumRarity.RARE);
    public static final Item schrabidium_shovel = new ItemToolAbility(15, -2.8F, 0, MainRegistry.enumToolMaterialSchrabidium, EnumToolType.SHOVEL, "schrabidium_shovel")
            .addAbility(IWeaponAbility.RADIATION, 0)
            .addAbility(IToolAreaAbility.HAMMER, 1)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
            .addAbility(IToolAreaAbility.RECURSION, 6)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 4)
            .addAbility(IToolHarvestAbility.SMELTER, 0)
            .addAbility(IToolHarvestAbility.SHREDDER, 0)
            .setRarity(EnumRarity.RARE);
    public static final Item crowbar = new ModSword(MainRegistry.enumToolMaterialSteel, "crowbar").setCreativeTab(CreativeTabs.TOOLS);
    public static final Item saw = new ModSword(MainRegistry.enumToolMaterialSaw, "weapon_saw").setFull3D().setCreativeTab(CreativeTabs.TOOLS);
    public static final Item bat = new ModSword(MainRegistry.enumToolMaterialBat, "weapon_bat").setFull3D().setCreativeTab(CreativeTabs.COMBAT);
    public static final Item bat_nail = new ModSword(MainRegistry.enumToolMaterialBatNail, "weapon_bat_nail").setFull3D().setCreativeTab(CreativeTabs.COMBAT);
    public static final Item golf_club = new ModSword(MainRegistry.enumToolMaterialGolfClub, "weapon_golf_club").setFull3D().setCreativeTab(CreativeTabs.COMBAT);
    public static final Item pipe_rusty = new ModSword(MainRegistry.enumToolMaterialPipeRusty, "weapon_pipe_rusty").setFull3D().setCreativeTab(CreativeTabs.COMBAT);
    public static final Item pipe_lead = new ModSword(MainRegistry.enumToolMaterialPipeLead, "weapon_pipe_lead").setFull3D().setCreativeTab(CreativeTabs.COMBAT);
    public static final Item reer_graar = new ModSword(MainRegistry.enumToolMaterialTitanium, "reer_graar").setFull3D().setCreativeTab(CreativeTabs.COMBAT);
    public static final Item stopsign = new WeaponSpecial(MainRegistry.enumToolMaterialAlloy, "stopsign").setCreativeTab(CreativeTabs.COMBAT);
    public static final Item sopsign = new WeaponSpecial(MainRegistry.enumToolMaterialAlloy, "sopsign").setCreativeTab(CreativeTabs.COMBAT);
    public static final Item chernobylsign = new WeaponSpecial(MainRegistry.enumToolMaterialAlloy, "chernobylsign").setCreativeTab(CreativeTabs.COMBAT);
    public static final Item mask_of_infamy = new MaskOfInfamy(ArmorMaterial.IRON, -1, EntityEquipmentSlot.HEAD, "mask_of_infamy").setMaxStackSize(1);
    public static final Item meteorite_sword = new ItemSwordMeteorite(9F, 0, MainRegistry.matMeteorite, "meteorite_sword");
    public static final Item meteorite_sword_seared = new ItemSwordMeteorite(10F, 0, MainRegistry.matMeteorite, "meteorite_sword_seared");
    public static final Item meteorite_sword_reforged = new ItemSwordMeteorite(12.5F, 0, MainRegistry.matMeteorite, "meteorite_sword_reforged");
    public static final Item meteorite_sword_hardened = new ItemSwordMeteorite(15F, 0, MainRegistry.matMeteorite, "meteorite_sword_hardened");
    public static final Item meteorite_sword_alloyed = new ItemSwordMeteorite(17.5F, 0, MainRegistry.matMeteorite, "meteorite_sword_alloyed");
    public static final Item meteorite_sword_machined = new ItemSwordMeteorite(20F, 0, MainRegistry.matMeteorite, "meteorite_sword_machined");
    public static final Item meteorite_sword_treated = new ItemSwordMeteorite(22.5F, 0, MainRegistry.matMeteorite, "meteorite_sword_treated");
    public static final Item meteorite_sword_etched = new ItemSwordMeteorite(25F, 0, MainRegistry.matMeteorite, "meteorite_sword_etched");
    public static final Item meteorite_sword_bred = new ItemSwordMeteorite(30F, 0, MainRegistry.matMeteorite, "meteorite_sword_bred");
    public static final Item meteorite_sword_irradiated = new ItemSwordMeteorite(35F, 0, MainRegistry.matMeteorite, "meteorite_sword_irradiated");
    public static final Item meteorite_sword_fused = new ItemSwordMeteorite(50F, 0, MainRegistry.matMeteorite, "meteorite_sword_fused");
    public static final Item meteorite_sword_baleful = new ItemSwordMeteorite(75F, 0, MainRegistry.matMeteorite, "meteorite_sword_baleful");

    //Plates
    public static final Item plate_iron = new ItemBase("plate_iron").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_copper = new ItemBase("plate_copper").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_titanium = new ItemBase("plate_titanium").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_aluminium = new ItemBase("plate_aluminium").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_gold = new ItemBase("plate_gold").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_lead = new ItemCustomLore("plate_lead").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_steel = new ItemBase("plate_steel").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_advanced_alloy = new ItemBase("plate_advanced_alloy").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_combine_steel = new ItemBase("plate_combine_steel").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_paa = new ItemCustomLore("plate_paa").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_saturnite = new ItemBase("plate_saturnite").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_schrabidium = new ItemCustomLore("plate_schrabidium").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_dalekanium = new ItemBase("plate_dalekanium").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_mixed = new ItemBase("plate_mixed").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_kevlar = new ItemBase("plate_kevlar").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_polymer = new ItemBase("plate_polymer").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_desh = new ItemBase("plate_desh").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_bismuth = new ItemBakedBase("plate_bismuth").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_euphemium = new ItemCustomLore("plate_euphemium").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_dineutronium = new ItemBase("plate_dineutronium").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_armor_titanium = new ItemBase("plate_armor_titanium").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_armor_ajr = new ItemBase("plate_armor_ajr").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_armor_hev = new ItemBase("plate_armor_hev").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_armor_lunar = new ItemBase("plate_armor_lunar").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_armor_fau = new ItemBase("plate_armor_fau").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_armor_dnt = new ItemBase("plate_armor_dnt").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_dura_steel = new ItemBase("plate_dura_steel").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_gunmetal = new ItemBase("plate_gunmetal").setCreativeTab(MainRegistry.partsTab);
    public static final Item plate_weaponsteel = new ItemBase("plate_weaponsteel").setCreativeTab(MainRegistry.partsTab);

    //Templates
    public static final Item blueprints = new ItemBlueprints("blueprints").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab);
    public static final Item blueprint_folder = new ItemBlueprintFolder("blueprint_folder").setCreativeTab(MainRegistry.templateTab);
    public static final Item template_folder = new ItemTemplateFolder("template_folder").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab);
    // TODO: implment this
    public static final Item journal_pip = new ItemTemplateFolder("journal_pip").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab);
    public static final Item journal_bj = new ItemTemplateFolder("journal_bj").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab);
    public static final Item journal_silver = new ItemTemplateFolder("journal_silver").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab);
    public static final Item bobmazon = new ItemCatalog("bobmazon").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab);
    public static final Item bobmazon_hidden = new ItemCatalog("bobmazon_hidden").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab);
    public static final Item siren_track = new ItemCassette("siren_track").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab);
    public static final Item assembly_template = new ItemAssemblyTemplate("assembly_template").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab);
    public static final Item chemistry_template = new ItemChemistryTemplate("chemistry_template").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab);
    public static final Item chemistry_icon = new ItemChemistryIcon("chemistry_icon").setMaxStackSize(1).setCreativeTab(null);
    public static final Item crucible_template = new ItemCrucibleTemplate("crucible_template").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab);
    public static final Item fluid_identifier_multi = new ItemFluidIDMulti("fluid_identifier_multi").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab);
    public static final Item fluid_duct = new ItemFFFluidDuct("ff_fluid_duct").setCreativeTab(MainRegistry.templateTab);
    public static final ToolMaterial matMeseGavel = EnumHelper.addToolMaterial("HBM_MESEGAVEL", 4, 0, 50F, 0.0F, 200).setRepairItem(new ItemStack(ModItems.plate_paa));
    public static final Item mese_gavel = new ItemSwordAbility(250, 1.5, matMeseGavel, "mese_gavel")
            .addAbility(IWeaponAbility.PHOSPHORUS, 0)
            .addAbility(IWeaponAbility.RADIATION, 2)
            .addAbility(IWeaponAbility.STUN, 3)
            .addAbility(IWeaponAbility.VAMPIRE, 4)
            .addAbility(IWeaponAbility.BEHEADER, 0).setCreativeTab(MainRegistry.weaponTab);
    public static final ToolMaterial matMese = EnumHelper.addToolMaterial("HBM_MESE", 6, 0, 50F, 0.0F, 200).setRepairItem(new ItemStack(ModItems.plate_paa));
    public static final Item mese_pickaxe = new ItemToolAbility(35F, -2.8F, 0, matMese, EnumToolType.MINER, "mese_pickaxe")
            .addAbility(IToolAreaAbility.HAMMER, 2)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 2)
            .addAbility(IToolAreaAbility.RECURSION, 2)
            .addAbility(IToolHarvestAbility.CRYSTALLIZER, 0)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 5)
            .addAbility(IToolAreaAbility.EXPLOSION, 3)
            .addAbility(IWeaponAbility.STUN, 3)
            .addAbility(IWeaponAbility.PHOSPHORUS, 0)
            .addAbility(IWeaponAbility.BEHEADER, 0)
            .setDepthRockBreaker();
    public static final Item mese_axe = new ItemToolAbility(75F, -2.8F, 0, matMese, EnumToolType.AXE, "mese_axe")
				.addAbility(IToolAreaAbility.HAMMER, 2)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 2)
				.addAbility(IToolAreaAbility.RECURSION, 2)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 5)
				.addAbility(IToolAreaAbility.EXPLOSION, 3)
				.addAbility(IWeaponAbility.STUN, 4)
				.addAbility(IWeaponAbility.PHOSPHORUS, 1)
				.addAbility(IWeaponAbility.BEHEADER, 0);

    public static final Item dnt_sword = new ItemSwordAbility(12F, 0, matMese, "dnt_sword");

    public static final ToolMaterial matDwarf = EnumHelper.addToolMaterial("HBM_DWARVEN", 2, 0, 4F, 0.0F, 10).setRepairItem(new ItemStack(ModItems.ingot_copper));
    public static final Item dwarven_pickaxe = new ItemToolAbility(5F, -2.8F, -0.1, matDwarf, EnumToolType.MINER, "dwarven_pickaxe")
				.addAbility(IToolAreaAbility.HAMMER, 0)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 0).setMaxDamage(250);

    //Crystals
    public static final Item crystal_coal = new ItemFuel("crystal_coal", 6400).setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_iron = new ItemBase("crystal_iron").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_gold = new ItemBase("crystal_gold").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_redstone = new ItemBase("crystal_redstone").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_lapis = new ItemBase("crystal_lapis").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_diamond = new ItemBase("crystal_diamond").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_uranium = new ItemCustomLore("crystal_uranium").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_thorium = new ItemCustomLore("crystal_thorium").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_plutonium = new ItemCustomLore("crystal_plutonium").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_titanium = new ItemBase("crystal_titanium").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_sulfur = new ItemBase("crystal_sulfur").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_niter = new ItemBase("crystal_niter").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_copper = new ItemBase("crystal_copper").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_tungsten = new ItemBase("crystal_tungsten").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_aluminium = new ItemBase("crystal_aluminium").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_fluorite = new ItemBase("crystal_fluorite").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_beryllium = new ItemBase("crystal_beryllium").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_lead = new ItemCustomLore("crystal_lead").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_asbestos = new ItemCustomLore("crystal_asbestos").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_schraranium = new ItemCustomLore("crystal_schraranium").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_schrabidium = new ItemCustomLore("crystal_schrabidium").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_rare = new ItemBase("crystal_rare").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_phosphorus = new ItemCustomLore("crystal_phosphorus").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_lithium = new ItemCustomLore("crystal_lithium").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_cinnabar = new ItemBase("crystal_cinnabar").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_cobalt = new ItemBase("crystal_cobalt").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_starmetal = new ItemBase("crystal_starmetal").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_trixite = new ItemCustomLore("crystal_trixite").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_osmiridium = new ItemCustomLore("crystal_osmiridium").setCreativeTab(MainRegistry.partsTab);
    public static final Item gem_tantalium = new ItemCustomLore("gem_tantalium").setCreativeTab(MainRegistry.partsTab);
    public static final Item gem_volcanic = new ItemCustomLore("gem_volcanic").setRarity(EnumRarity.UNCOMMON).setCreativeTab(MainRegistry.partsTab);
    public static final Item gem_sodalite = new ItemCustomLore("gem_sodalite").setCreativeTab(MainRegistry.partsTab);
    public static final Item gem_rad = new ItemBakedBase("gem_rad").setCreativeTab(MainRegistry.partsTab);
    //TODO: Port ItemAlexandrite for color logic
    public static final Item gem_alexandrite = new ItemBase("gem_alexandrite").setCreativeTab(MainRegistry.partsTab);
    public static final Item upgrade_muffler = new ItemMuffler("upgrade_muffler").setCreativeTab(MainRegistry.partsTab);
    public static final Item upgrade_template = new ItemCustomLore("upgrade_template").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab);
    public static final Item deuterium_filter = new ItemBase("deuterium_filter").setCreativeTab(MainRegistry.partsTab);
    public static final Item parts_legendary = new ItemEnumMulti("parts_legendary", EnumLegendaryType.class, false, true).setCreativeTab(MainRegistry.partsTab);
    public static final Item sawblade = new ItemBase("sawblade").setCreativeTab(MainRegistry.partsTab);
    public static final Item gear_large = new ItemGear("gear_large").setCreativeTab(MainRegistry.partsTab);
    public static final Item crt_display = new ItemBakedBase("crt_display").setCreativeTab(MainRegistry.partsTab);
    public static final ItemEnumMulti circuit_star_piece = (ItemEnumMulti) new ItemEnumMulti("circuit_star_piece", ScrapType.class, true, true).setCreativeTab(null);
    public static final ItemEnumMulti circuit_star_component = (ItemEnumMulti) new ItemEnumMulti("circuit_star_component", CircuitComponentType.class, true, true).setCreativeTab(null);
    public static final Item circuit_star = new ItemCustomLore("circuit_star").setRarity(EnumRarity.UNCOMMON).setCreativeTab(null);
    public static final Item mechanism_revolver_1 = new ItemBase("mechanism_revolver_1").setCreativeTab(MainRegistry.partsTab);
    public static final Item mechanism_revolver_2 = new ItemBase("mechanism_revolver_2").setCreativeTab(MainRegistry.partsTab);
    public static final Item mechanism_rifle_1 = new ItemBase("mechanism_rifle_1").setCreativeTab(MainRegistry.partsTab);
    public static final Item mechanism_rifle_2 = new ItemBase("mechanism_rifle_2").setCreativeTab(MainRegistry.partsTab);
    public static final Item mechanism_launcher_1 = new ItemBase("mechanism_launcher_1").setCreativeTab(MainRegistry.partsTab);
    public static final Item mechanism_launcher_2 = new ItemBase("mechanism_launcher_2").setCreativeTab(MainRegistry.partsTab);
    public static final Item mechanism_special = new ItemBase("mechanism_special").setCreativeTab(MainRegistry.partsTab);
    public static final Item primer_357 = new ItemBase("primer_357").setCreativeTab(MainRegistry.partsTab);
    public static final Item primer_44 = new ItemBase("primer_44").setCreativeTab(MainRegistry.partsTab);
    public static final Item primer_9 = new ItemBase("primer_9").setCreativeTab(MainRegistry.partsTab);
    public static final Item primer_50 = new ItemBase("primer_50").setCreativeTab(MainRegistry.partsTab);
    public static final Item primer_buckshot = new ItemBase("primer_buckshot").setCreativeTab(MainRegistry.partsTab);
    public static final Item casing_357 = new ItemBase("casing_357").setCreativeTab(MainRegistry.partsTab);
    public static final Item casing_44 = new ItemBase("casing_44").setCreativeTab(MainRegistry.partsTab);
    public static final Item casing_9 = new ItemBase("casing_9").setCreativeTab(MainRegistry.partsTab);
    public static final Item casing_50 = new ItemBase("casing_50").setCreativeTab(MainRegistry.partsTab);
    public static final Item casing_buckshot = new ItemBase("casing_buckshot").setCreativeTab(MainRegistry.partsTab);
    // Wires and things
    public static final Item coil_advanced_alloy = new ItemBase("coil_advanced_alloy").setCreativeTab(MainRegistry.partsTab);
    public static final Item coil_advanced_torus = new ItemBase("coil_advanced_torus").setCreativeTab(MainRegistry.partsTab);
    public static final Item coil_gold = new ItemBase("coil_gold").setCreativeTab(MainRegistry.partsTab);
    public static final Item coil_gold_torus = new ItemBase("coil_gold_torus").setCreativeTab(MainRegistry.partsTab);
    public static final Item coil_tungsten = new ItemBase("coil_tungsten").setCreativeTab(MainRegistry.partsTab);
    public static final Item coil_copper = new ItemBase("coil_copper").setCreativeTab(MainRegistry.partsTab);
    public static final Item coil_copper_torus = new ItemBase("coil_copper_torus").setCreativeTab(MainRegistry.partsTab);
    public static final Item coil_magnetized_tungsten = new ItemCustomLore("coil_magnetized_tungsten").setCreativeTab(MainRegistry.partsTab);
    //Gun ammo
    public static final Item assembly_nuke = new ItemBase("assembly_nuke").setCreativeTab(MainRegistry.partsTab);

    public static Item gun_debug;

    public static Item gun_pepperbox;
    public static Item gun_light_revolver;
    public static Item gun_light_revolver_atlas;
    public static Item gun_light_revolver_dani;
    public static Item gun_henry;
    public static Item gun_henry_lincoln;
    public static Item gun_greasegun;
    public static Item gun_maresleg;
    public static Item gun_maresleg_akimbo;
    public static Item gun_maresleg_broken;
    public static Item gun_flaregun;
    public static Item gun_heavy_revolver;
    public static Item gun_heavy_revolver_lilmac;
    public static Item gun_heavy_revolver_protege;
    public static Item gun_carbine;
    public static Item gun_am180;
    public static Item gun_liberator;
    public static Item gun_congolake;
    public static Item gun_flamer;
    public static Item gun_flamer_topaz;
    public static Item gun_flamer_daybreaker;
    public static Item gun_uzi;
    public static Item gun_uzi_akimbo;
    public static Item gun_spas12;
    public static Item gun_panzerschreck;
    public static Item gun_g3;
    public static Item gun_g3_zebra;
    public static Item gun_stinger;
    public static Item gun_chemthrower;
    public static Item gun_amat;
    public static Item gun_amat_subtlety;
    public static Item gun_amat_penance;
    public static Item gun_m2;
    public static Item gun_autoshotgun;
    public static Item gun_autoshotgun_shredder;
    public static Item gun_autoshotgun_sexy;
    public static Item gun_autoshotgun_heretic;
    public static Item gun_quadro;
    public static Item gun_lag;
    public static Item gun_minigun;
    public static Item gun_minigun_dual;
    public static Item gun_minigun_lacunae;
    public static Item gun_missile_launcher;
    public static Item gun_tesla_cannon;
    public static Item gun_laser_pistol;
    public static Item gun_laser_pistol_pew_pew;
    public static Item gun_laser_pistol_morning_glory;
    public static Item gun_stg77;
    public static Item gun_tau;
    public static Item gun_fatman;
    public static Item gun_lasrifle;
    public static Item gun_coilgun;
    public static Item gun_hangman;
    public static Item gun_mas36;
    public static Item gun_bolter;
    public static Item gun_folly;
    public static Item gun_aberrator;
    public static Item gun_aberrator_eott;
    public static Item gun_double_barrel;
    public static Item gun_double_barrel_sacred_dragon;

    public static Item gun_charge_thrower;
    public static Item gun_drill;
    public static Item gun_fireext;

    public static Item weapon_mod_test;
    public static Item weapon_mod_generic;
    public static Item weapon_mod_special;
    public static Item weapon_mod_caliber;


    public static Item ammo_debug;
    public static Item ammo_standard;
    public static Item ammo_secret;
    public static final ItemEnumMulti ammo_shell = (ItemEnumMulti) new ItemAmmo("ammo_shell", ItemAmmoEnums.Ammo240Shell.class).setCreativeTab(MainRegistry.weaponTab);
    public static final Item ammo_dgk = new ItemCustomLore("ammo_dgk").setCreativeTab(MainRegistry.weaponTab);
    public static final ItemEnumMulti ammo_fireext = (ItemEnumMulti) new ItemAmmo("ammo_fireext", ItemAmmoEnums.AmmoFireExt.class).setCreativeTab(MainRegistry.weaponTab);
    public static final ItemEnumMulti ammo_misc = new ItemAmmo("ammo_misc", ItemAmmoEnums.AmmoMisc.class);
    public static final Item ammo_arty = new ItemAmmoArty("ammo_arty");
    public static final Item ammo_himars = new ItemAmmoHIMARS("ammo_himars");
    public static final Item gun_b92_ammo = new GunB92Cell("gun_b92_ammo").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab);
    //Drillgon200: screw organization, porting takes less time if I don't have to search for each individual spot.
    public static final Item charge_railgun = new ItemCustomLore("charge_railgun").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab);
    public static final Item ammo_container = new ItemClip("ammo_container").setCreativeTab(MainRegistry.weaponTab);
    //Grenade
    public static final Item grenade_generic = new ItemGrenade(4, "grenade_generic").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_strong = new ItemGrenade(5, "grenade_strong").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_frag = new ItemGrenade(4, "grenade_frag").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_fire = new ItemGrenade(4, "grenade_fire").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_shrapnel = new ItemGrenade(4, "grenade_shrapnel").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_cluster = new ItemGrenade(5, "grenade_cluster").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_flare = new ItemGrenade(0, "grenade_flare").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_electric = new ItemGrenade(5, "grenade_electric").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_poison = new ItemGrenade(4, "grenade_poison").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_gas = new ItemGrenade(4, "grenade_gas").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_cloud = new ItemGrenade(-1, "grenade_cloud").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_pink_cloud = new ItemGrenade(-1, "grenade_pink_cloud").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_smart = new ItemGrenade(-1, "grenade_smart").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_mirv = new ItemGrenade(1, "grenade_mirv").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_breach = new ItemGrenade(-1, "grenade_breach").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_burst = new ItemGrenade(1, "grenade_burst").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_pulse = new ItemGrenade(4, "grenade_pulse").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_plasma = new ItemGrenade(5, "grenade_plasma").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_tau = new ItemGrenade(5, "grenade_tau").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_schrabidium = new ItemGrenade(7, "grenade_schrabidium").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_nuke = new ItemGrenade(-1, "grenade_nuke").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_lemon = new ItemGrenade(4, "grenade_lemon").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_gascan = new ItemGrenade(-1, "grenade_gascan").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_kyiv = new ItemGrenadeKyiv(-1, "grenade_kyiv").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_mk2 = new ItemGrenade(5, "grenade_mk2").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_aschrab = new ItemGrenade(-1, "grenade_aschrab").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_nuclear = new ItemGrenade(7, "grenade_nuclear").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_zomg = new ItemGrenade(7, "grenade_zomg").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_solinium = new ItemGrenade(6, "grenade_solinium").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_black_hole = new ItemGrenade(7, "grenade_black_hole").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_if_generic = new ItemGrenade(4, "grenade_if_generic").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_if_he = new ItemGrenade(5, "grenade_if_he").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_if_bouncy = new ItemGrenade(4, "grenade_if_bouncy").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_if_sticky = new ItemGrenade(4, "grenade_if_sticky").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_if_impact = new ItemGrenade(-1, "grenade_if_impact").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_if_incendiary = new ItemGrenade(4, "grenade_if_incendiary").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_if_toxic = new ItemGrenade(4, "grenade_if_toxic").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_if_concussion = new ItemGrenade(4, "grenade_if_concussion").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_if_brimstone = new ItemGrenade(5, "grenade_if_brimstone").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_if_mystery = new ItemGrenade(5, "grenade_if_mystery").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_if_hopwire = new ItemGrenade(6, "grenade_if_hopwire").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_if_spark = new ItemGrenade(7, "grenade_if_spark").setCreativeTab(MainRegistry.weaponTab);
    public static final Item grenade_if_null = new ItemGrenade(8, "grenade_if_null").setCreativeTab(MainRegistry.weaponTab);
    public static final Item weaponized_starblaster_cell = new WeaponizedCell("weaponized_starblaster_cell").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab);
    //Turret
    public static final Item turret_control = new ItemTurretControl("turret_control").setFull3D().setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab);
    public static final Item turret_chip = new ItemTurretChip("turret_chip").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab);
    public static final Item turret_biometry = new ItemTurretBiometry("turret_biometry").setFull3D().setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab);
    public static final Item designator_arty_range = new ItemDesignatorArtyRange("designator_arty_range").setCreativeTab(MainRegistry.missileTab);
    public static final Item book_guide = new ItemGuideBook("book_guide_book").setCreativeTab(MainRegistry.consumableTab);
    public static final Item book_lore = new ItemBookLore("book_lore").setCreativeTab(null);
    public static final Item rune_blank = new ItemCustomLore("rune_blank").setCreativeTab(MainRegistry.partsTab).setMaxStackSize(1);
    public static final Item rune_isa = new ItemCustomLore("rune_isa").setCreativeTab(MainRegistry.partsTab).setMaxStackSize(1);
    public static final Item rune_dagaz = new ItemCustomLore("rune_dagaz").setCreativeTab(MainRegistry.partsTab).setMaxStackSize(1);
    public static final Item rune_hagalaz = new ItemCustomLore("rune_hagalaz").setCreativeTab(MainRegistry.partsTab).setMaxStackSize(1);
    public static final Item rune_jera = new ItemCustomLore("rune_jera").setCreativeTab(MainRegistry.partsTab).setMaxStackSize(1);
    public static final Item rune_thurisaz = new ItemCustomLore("rune_thurisaz").setCreativeTab(MainRegistry.partsTab).setMaxStackSize(1);
    public static final Item rbmk_lid = new ItemRBMKLid("rbmk_lid").setCreativeTab(MainRegistry.machineTab);
    public static final Item rbmk_lid_glass = new ItemRBMKLid("rbmk_lid_glass").setCreativeTab(MainRegistry.machineTab);
    public static final Item debris_graphite = new ItemBase("debris_graphite").setCreativeTab(MainRegistry.controlTab);
    public static final Item debris_metal = new ItemBase("debris_metal").setCreativeTab(MainRegistry.controlTab);
    public static final Item debris_fuel = new ItemBase("debris_fuel").setCreativeTab(MainRegistry.controlTab);
    public static final Item debris_concrete = new ItemBase("debris_concrete").setCreativeTab(MainRegistry.controlTab);
    public static final Item debris_shrapnel = new ItemBase("debris_shrapnel").setCreativeTab(MainRegistry.controlTab);
    public static final Item debris_exchanger = new ItemBase("debris_exchanger").setCreativeTab(MainRegistry.controlTab);
    public static final Item debris_element = new ItemBase("debris_element").setCreativeTab(MainRegistry.controlTab);
    public static final ItemRBMKPellet rbmk_pellet_zfb_bismuth = new ItemRBMKPellet("Zirconium Fast Breeder - LEU/HEP-241 -> Bi", "rbmk_pellet_zfb_bismuth");
    public static final ItemRBMKPellet rbmk_pellet_zfb_pu241 = new ItemRBMKPellet("Zirconium Fast Breeder - HEU-235/HEP-240 -> Pu241", "rbmk_pellet_zfb_pu241");
    public static final ItemRBMKPellet rbmk_pellet_zfb_am_mix = new ItemRBMKPellet("Zirconium Fast Breeder - HEP-241 -> HEA", "rbmk_pellet_zfb_am_mix");
    public static final ItemRBMKPellet rbmk_pellet_ueu = new ItemRBMKPellet("Unenriched Uranium", "rbmk_pellet_ueu");
    public static final ItemRBMKPellet rbmk_pellet_meu = new ItemRBMKPellet("Medium Enriched Uranium-235", "rbmk_pellet_meu");
    public static final ItemRBMKPellet rbmk_pellet_heu233 = new ItemRBMKPellet("Highly Enriched Uranium-233", "rbmk_pellet_heu233");
    public static final ItemRBMKPellet rbmk_pellet_heu235 = new ItemRBMKPellet("Highly Enriched Uranium-235", "rbmk_pellet_heu235");
    public static final ItemRBMKPellet rbmk_pellet_thmeu = new ItemRBMKPellet("Thorium with MEU Driver Fuel", "rbmk_pellet_thmeu");
    public static final ItemRBMKPellet rbmk_pellet_lep = new ItemRBMKPellet("Low Enriched Plutonium-239", "rbmk_pellet_lep");
    public static final ItemRBMKPellet rbmk_pellet_mep = new ItemRBMKPellet("Medium Enriched Plutonium-239", "rbmk_pellet_mep");
    public static final ItemRBMKPellet rbmk_pellet_hep239 = new ItemRBMKPellet("Highly Enriched Plutonium-239", "rbmk_pellet_hep239");
    public static final ItemRBMKPellet rbmk_pellet_hep241 = new ItemRBMKPellet("Highly Enriched Plutonium-241", "rbmk_pellet_hep241");
    public static final ItemRBMKPellet rbmk_pellet_lea = new ItemRBMKPellet("Low Enriched Americium-242", "rbmk_pellet_lea");
    public static final ItemRBMKPellet rbmk_pellet_mea = new ItemRBMKPellet("Medium Enriched Americium-242", "rbmk_pellet_mea");
    public static final ItemRBMKPellet rbmk_pellet_hea241 = new ItemRBMKPellet("Highly Enriched Americium-241", "rbmk_pellet_hea241");
    public static final ItemRBMKPellet rbmk_pellet_hea242 = new ItemRBMKPellet("Highly Enriched Americium-242", "rbmk_pellet_hea242");
    public static final ItemRBMKPellet rbmk_pellet_men = new ItemRBMKPellet("Medium Enriched Neptunium-237", "rbmk_pellet_men");
    public static final ItemRBMKPellet rbmk_pellet_hen = new ItemRBMKPellet("Highly Enriched Neptunium-237", "rbmk_pellet_hen");
    public static final ItemRBMKPellet rbmk_pellet_mox = new ItemRBMKPellet("Mixed LEU & LEP Oxide", "rbmk_pellet_mox");
    public static final ItemRBMKPellet rbmk_pellet_les = new ItemRBMKPellet("Low Enriched Schrabidium-326", "rbmk_pellet_les");
    public static final ItemRBMKPellet rbmk_pellet_mes = new ItemRBMKPellet("Medium Enriched Schrabidium-326", "rbmk_pellet_mes");
    public static final ItemRBMKPellet rbmk_pellet_hes = new ItemRBMKPellet("Highly Enriched Schrabidium-326", "rbmk_pellet_hes");
    public static final ItemRBMKPellet rbmk_pellet_leaus = new ItemRBMKPellet("Low Enriched Australium (Tasmanite)", "rbmk_pellet_leaus");
    public static final ItemRBMKPellet rbmk_pellet_heaus = new ItemRBMKPellet("Highly Enriched Australium (Ayerite)", "rbmk_pellet_heaus");
    public static final ItemRBMKPellet rbmk_pellet_po210be = new ItemRBMKPellet("Polonium-210 & Beryllium Neutron Source", "rbmk_pellet_po210be");
    public static final ItemRBMKPellet rbmk_pellet_ra226be = new ItemRBMKPellet("Radium-226 & Beryllium Neutron Source", "rbmk_pellet_ra226be");
    public static final ItemRBMKPellet rbmk_pellet_pu238be = new ItemRBMKPellet("Plutonium-238 & Beryllium Neutron Source", "rbmk_pellet_pu238be");
    public static final ItemRBMKPellet rbmk_pellet_balefire_gold = new ItemRBMKPellet("Antihydrogen in a Magnetized Gold-198 Lattice", "rbmk_pellet_balefire_gold");
    public static final ItemRBMKPellet rbmk_pellet_flashlead = new ItemRBMKPellet("Antihydrogen confined by a Magnetized Gold-198 & Lead-209 Lattice", "rbmk_pellet_flashlead");
    public static final ItemRBMKPellet rbmk_pellet_balefire = new ItemRBMKPellet("Draconic Flames", "rbmk_pellet_balefire");
    public static final ItemRBMKPellet rbmk_pellet_drx = new ItemRBMKPellet(TextFormatting.OBFUSCATED + "can't you hear, can't you hear the thunder?", "rbmk_pellet_drx");
    public static final Item rbmk_fuel_empty = new ItemBase("rbmk_fuel_empty").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final ItemRBMKRod rbmk_fuel_ueu = new ItemRBMKRod(rbmk_pellet_ueu, "rbmk_fuel_ueu")
           .setYield(100000000D)
				.setStats(15)
				.setFunction(EnumBurnFunc.LOG_TEN)
				.setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
				.setHeat(0.65) //0.5 is too much of a nerf in heat; pu239 buildup justifies it being on par with MEU ig
				.setMeltingPoint(2865)
            .setFuelColor(0.513F, 0.541F, 0.498F);
    public static final ItemRBMKRod rbmk_fuel_meu = new ItemRBMKRod(rbmk_pellet_meu, "rbmk_fuel_meu")
            .setYield(100000000D)
            .setStats(20)
            .setFunction(EnumBurnFunc.LOG_TEN)
            .setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
            .setHeat(0.65) //0.75 was a bit too much...
            .setMeltingPoint(2865)
            .setFuelColor(0.513F, 0.541F, 0.498F);
    public static final ItemRBMKRod rbmk_fuel_heu233 = new ItemRBMKRod(rbmk_pellet_heu233, "rbmk_fuel_heu233")
           .setYield(100000000D)
				.setStats(27.5D)
				.setFunction(EnumBurnFunc.LINEAR)
				.setHeat(1.25D)
				.setMeltingPoint(2865)
            .setFuelColor(0.513F, 0.541F, 0.498F);
    public static final ItemRBMKRod rbmk_fuel_heu235 = new ItemRBMKRod(rbmk_pellet_heu235, "rbmk_fuel_heu235")
            .setYield(100000000D)
            .setStats(50) //Consistency with HEN; its critical mass is too high to justify a linear function
            .setFunction(EnumBurnFunc.SQUARE_ROOT)
            .setMeltingPoint(2865)
            .setFuelColor(0.513F, 0.541F, 0.498F);
    public static final ItemRBMKRod rbmk_fuel_thmeu = new ItemRBMKRod(rbmk_pellet_thmeu, "rbmk_fuel_thmeu")
            .setYield(100000000D)
            .setStats(20)
            .setFunction(EnumBurnFunc.PLATEU)
            .setDepletionFunction(EnumDepleteFunc.BOOSTED_SLOPE)
            .setHeat(0.65D) //Consistency with MEU
            .setMeltingPoint(3350)
            .setFuelColor(0.360F, 0.259F, 0.212F);
    public static final ItemRBMKRod rbmk_fuel_lep = new ItemRBMKRod(rbmk_pellet_lep, "rbmk_fuel_lep")
            .setYield(100000000D)
            .setStats(35)
            .setFunction(EnumBurnFunc.LOG_TEN)
            .setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
            .setHeat(0.75D)
            .setMeltingPoint(2744)
            .setFuelColor(0.314F, 0.349F, 0.337F);
    public static final ItemRBMKRod rbmk_fuel_mep = new ItemRBMKRod(rbmk_pellet_mep, "rbmk_fuel_mep")
            .setYield(100000000D)
            .setStats(35)
            .setFunction(EnumBurnFunc.SQUARE_ROOT)
            .setMeltingPoint(2744)
            .setFuelColor(0.314F, 0.349F, 0.337F);
    public static final ItemRBMKRod rbmk_fuel_hep239 = new ItemRBMKRod(rbmk_pellet_hep239, "rbmk_fuel_hep")
            .setYield(100000000D)
            .setStats(30)
            .setFunction(EnumBurnFunc.LINEAR)
            .setHeat(1.25D)
            .setMeltingPoint(2744)
            .setFuelColor(0.314F, 0.349F, 0.337F);
    public static final ItemRBMKRod rbmk_fuel_hep241 = new ItemRBMKRod(rbmk_pellet_hep241, "rbmk_fuel_hep241")
            .setYield(100000000D)
            .setStats(40)
            .setFunction(EnumBurnFunc.LINEAR)
            .setHeat(1.75D)
            .setMeltingPoint(2744)
            .setFuelColor(0.314F, 0.349F, 0.337F);
    public static final ItemRBMKRod rbmk_fuel_lea = new ItemRBMKRod(rbmk_pellet_lea, "rbmk_fuel_lea")
            .setYield(100000000D)
            .setStats(60, 10)
            .setFunction(EnumBurnFunc.SQUARE_ROOT)
            .setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
            .setHeat(1.5D)
            .setMeltingPoint(2386)
            .setFuelColor(0.514F, 0.467F, 0.455F);
    public static final ItemRBMKRod rbmk_fuel_mea = new ItemRBMKRod(rbmk_pellet_mea, "rbmk_fuel_mea")
            .setYield(100000000D)
            .setStats(35D, 20)
            .setFunction(EnumBurnFunc.ARCH)
            .setHeat(1.75D)
            .setMeltingPoint(2386)
            .setFuelColor(0.545F, 0.424F, 0.443F);
    public static final ItemRBMKRod rbmk_fuel_hea241 = new ItemRBMKRod(rbmk_pellet_hea241, "rbmk_fuel_hea241")
            .setYield(100000000D)
            .setStats(65, 15)
            .setFunction(EnumBurnFunc.SQUARE_ROOT)
            .setHeat(1.85D)
            .setMeltingPoint(2386)
            .setNeutronTypes(NType.FAST, NType.FAST)
            .setFuelColor(0.545F, 0.424F, 0.443F);
    public static final ItemRBMKRod rbmk_fuel_hea242 = new ItemRBMKRod(rbmk_pellet_hea242, "rbmk_fuel_hea242")
            .setYield(100000000D)
            .setStats(45)
            .setFunction(EnumBurnFunc.LINEAR)
            .setHeat(2D)
            .setMeltingPoint(3386)
            .setFuelColor(0.545F, 0.424F, 0.443F);
    public static final ItemRBMKRod rbmk_fuel_men = new ItemRBMKRod(rbmk_pellet_men, "rbmk_fuel_men")
            .setYield(100000000D)
            .setStats(30)
            .setFunction(EnumBurnFunc.SQUARE_ROOT)
            .setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
            .setHeat(0.75)
            .setMeltingPoint(2800)
            .setNeutronTypes(NType.ANY, NType.FAST)
            .setFuelColor(0.447F, 0.482F, 0.439F);
    public static final ItemRBMKRod rbmk_fuel_hen = new ItemRBMKRod(rbmk_pellet_hen, "rbmk_fuel_hen")
            .setYield(100000000D)
            .setStats(40)
            .setFunction(EnumBurnFunc.SQUARE_ROOT)
            .setMeltingPoint(2800)
            .setNeutronTypes(NType.FAST, NType.FAST)
            .setFuelColor(0.376F, 0.423F, 0.376F);
    public static final ItemRBMKRod rbmk_fuel_mox = new ItemRBMKRod(rbmk_pellet_mox, "rbmk_fuel_mox")
            .setYield(100000000D)
            .setStats(40)
            .setFunction(EnumBurnFunc.LOG_TEN)
            .setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
            .setMeltingPoint(2815)
            .setFuelColor(0.423F, 0.455F, 0.427F);
    public static final ItemRBMKRod rbmk_fuel_les = new ItemRBMKRod(rbmk_pellet_les, "rbmk_fuel_les")
            .setYield(100000000D)
            .setStats(50)
            .setFunction(EnumBurnFunc.SQUARE_ROOT)
            .setHeat(1.25D)
            .setMeltingPoint(2500)
            .setNeutronTypes(NType.SLOW, NType.SLOW) //Beryllium Moderation
            .setFuelColor(0.498F, 0.596F, 0.620F);
    public static final ItemRBMKRod rbmk_fuel_mes = new ItemRBMKRod(rbmk_pellet_mes, "rbmk_fuel_mes")
            .setYield(100000000D)
            .setStats(75D)
            .setFunction(EnumBurnFunc.ARCH)
            .setHeat(1.5D)
            .setMeltingPoint(2750)
            .setFuelColor(0.408F, 0.651F, 0.710F);
    public static final ItemRBMKRod rbmk_fuel_hes = new ItemRBMKRod(rbmk_pellet_hes, "rbmk_fuel_hes")
            .setYield(100000000D)
            .setStats(90)
            .setFunction(EnumBurnFunc.LINEAR)
            .setDepletionFunction(EnumDepleteFunc.LINEAR)
            .setHeat(1.75D)
            .setMeltingPoint(3000)
            .setFuelColor(0F, 0.580F, 0.651F);
    public static final ItemRBMKRod rbmk_fuel_leaus = new ItemRBMKRod(rbmk_pellet_leaus, "rbmk_fuel_leaus")
            .setYield(100000000D)
            .setStats(30)
            .setFunction(EnumBurnFunc.SIGMOID)
            .setDepletionFunction(EnumDepleteFunc.LINEAR)
            .setXenon(0.05D, 50D)
            .setHeat(1.5D)
            .setMeltingPoint(7029)
            .setFuelColor(0.929F, 0.812F, 0F)
            .setCherenkovColor(1F, 0.9F, 0F);
    public static final ItemRBMKRod rbmk_fuel_heaus = new ItemRBMKRod(rbmk_pellet_heaus, "rbmk_fuel_heaus")
            .setYield(100000000D)
            .setStats(35)
            .setFunction(EnumBurnFunc.SQUARE_ROOT)
            .setXenon(0.05D, 50D)
            .setHeat(2D)
            .setMeltingPoint(5211)
            .setFuelColor(0.929F, 0.812F, 0F)
            .setCherenkovColor(1F, 0.9F, 0F);
    public static final ItemRBMKRod rbmk_fuel_ra226be = new ItemRBMKRod(rbmk_pellet_ra226be, "rbmk_fuel_ra226be")
            .setYield(100000000D)
            .setStats(0D, 20)
            .setFunction(EnumBurnFunc.PASSIVE)
            .setDepletionFunction(EnumDepleteFunc.LINEAR)
            .setXenon(0.0D, 50D)
            .setHeat(0.035D)
            .setDiffusion(0.5D)
            .setMeltingPoint(700)
            .setNeutronTypes(NType.SLOW, NType.SLOW) //Beryllium Moderation
            .setFuelColor(0.710F, 0.722F, 0.686F);
    public static final ItemRBMKRod rbmk_fuel_po210be = new ItemRBMKRod(rbmk_pellet_po210be, "rbmk_fuel_po210be")
            .setYield(25000000D)
            .setStats(0D, 50)
            .setFunction(EnumBurnFunc.PASSIVE)
            .setDepletionFunction(EnumDepleteFunc.LINEAR)
            .setXenon(0.0D, 50D)
            .setHeat(0.1D)
            .setDiffusion(0.05D)
            .setMeltingPoint(1287)
            .setNeutronTypes(NType.SLOW, NType.SLOW) //Beryllium Moderation
            .setFuelColor(0.463F, 0.392F, 0.318F);
    public static final ItemRBMKRod rbmk_fuel_pu238be = new ItemRBMKRod(rbmk_pellet_pu238be, "rbmk_fuel_pu238be")
            .setYield(50000000D)
            .setStats(40, 40)
            .setFunction(EnumBurnFunc.SQUARE_ROOT)
            .setHeat(0.1D)
            .setDiffusion(0.05D)
            .setMeltingPoint(1287)
            .setNeutronTypes(NType.SLOW, NType.SLOW) //Beryllium Moderation
            .setFuelColor(0.459F, 0.475F, 0.443F);
    public static final ItemRBMKRod rbmk_fuel_balefire_gold = new ItemRBMKRod(rbmk_pellet_balefire_gold, "rbmk_fuel_balefire_gold")
            .setYield(100000000D)
            .setStats(50, 10)
            .setFunction(EnumBurnFunc.ARCH)
            .setDepletionFunction(EnumDepleteFunc.LINEAR)
            .setXenon(0.0D, 50D)
            .setMeltingPoint(2000)
            .setFuelColor(0.902F, 0.714F, 0.227F)
            .setCherenkovColor(0.6F, 0F, 1F);
    public static final ItemRBMKRod rbmk_fuel_flashlead = new ItemRBMKRod(rbmk_pellet_flashlead, "rbmk_fuel_flashlead")
            .setYield(250000000D)
            .setStats(40, 50)
            .setFunction(EnumBurnFunc.ARCH)
            .setDepletionFunction(EnumDepleteFunc.LINEAR)
            .setXenon(0.0D, 50D)
            .setMeltingPoint(2050)
            .setFuelColor(0.682F, 0.521F, 0.125F)
            .setCherenkovColor(0.6F, 0F, 1F);
    public static final ItemRBMKRod rbmk_fuel_zfb_bismuth = new ItemRBMKRod(rbmk_pellet_zfb_bismuth, "rbmk_fuel_zfb_bismuth")
            .setYield(50000000D)
            .setStats(20)
            .setFunction(EnumBurnFunc.SQUARE_ROOT)
            .setHeat(1.75D)
            .setMeltingPoint(2744)
            .setFuelColor(0.643F, 0.620F, 0.643F);
    public static final ItemRBMKRod rbmk_fuel_zfb_pu241 = new ItemRBMKRod(rbmk_pellet_zfb_pu241, "rbmk_fuel_zfb_pu241")
            .setYield(50000000D)
            .setStats(20)
            .setFunction(EnumBurnFunc.SQUARE_ROOT)
            .setMeltingPoint(2865)
            .setFuelColor(0.462F, 0.459F, 0.384F);
    public static final ItemRBMKRod rbmk_fuel_zfb_am_mix = new ItemRBMKRod(rbmk_pellet_zfb_am_mix, "rbmk_fuel_zfb_am_mix")
            .setYield(50000000D)
            .setStats(20)
            .setFunction(EnumBurnFunc.LINEAR)
            .setHeat(1.75D)
            .setMeltingPoint(2744)
            .setFuelColor(0.600F, 0.565F, 0.525F);
    public static final ItemRBMKRod rbmk_fuel_balefire = new ItemRBMKRod(rbmk_pellet_balefire, "rbmk_fuel_balefire")
            .setYield(100000000D)
            .setStats(100, 35)
            .setFunction(EnumBurnFunc.LINEAR)
            .setXenon(0.0D, 50D)
            .setHeat(3D)
            .setMeltingPoint(3652)
            .setFuelColor(0.369F, 0.878F, 0F)
            .setCherenkovColor(0.25F, 1F, 0F);
    public static final ItemRBMKRod rbmk_fuel_drx = new ItemRBMKRod(rbmk_pellet_drx, "rbmk_fuel_drx")
            .setYield(100000000D)
            .setStats(1000, 10)
            .setFunction(EnumBurnFunc.QUADRATIC)
            .setHeat(0.1D)
            .setMeltingPoint(100000)
            .setFuelColor(0.733F, 0F, 0F)
            .setCherenkovColor(1F, 0.25F, 0F);
    public static final ItemRBMKRod rbmk_fuel_test = new ItemRBMKRod("THE VOICES", "rbmk_fuel_test")
            .setYield(1000000D)
            .setStats(100)
            .setFunction(EnumBurnFunc.EXPERIMENTAL)
            .setHeat(1.0D)
            .setMeltingPoint(100000);
    public static final Item icf_pellet_empty = new ItemBase("icf_pellet_empty").setCreativeTab(MainRegistry.controlTab);
    public static final Item icf_pellet = new ItemICFPellet("icf_pellet").setCreativeTab(MainRegistry.controlTab);
    public static final Item icf_pellet_depleted = new ItemBase("icf_pellet_depleted").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);

    public static final Item watz_pellet = new ItemWatzPellet("watz_pellet", false);
    public static final Item watz_pellet_depleted = new ItemWatzPellet("watz_pellet_depleted", true);
    public static final Item pa_coil = new ItemPACoil("pa_coil").setCreativeTab(MainRegistry.controlTab);
    public static final Item particle_empty = new ItemBase("particle_empty").setCreativeTab(MainRegistry.controlTab);
    public static final Item particle_hydrogen = new ItemBase("particle_hydrogen").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty);
    public static final Item particle_copper = new ItemBase("particle_copper").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty);
    public static final Item particle_lead = new ItemBase("particle_lead").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty);
    public static final Item particle_aproton = new ItemBase("particle_aproton").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty);
    public static final Item particle_aelectron = new ItemBase("particle_aelectron").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty);
    public static final Item particle_muon = new ItemBase("particle_muon").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty);
    public static final Item particle_amat = new ItemBase("particle_amat").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty);
    public static final Item particle_aschrab = new ItemBase("particle_aschrab").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty);
    public static final Item particle_higgs = new ItemBase("particle_higgs").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty);
    public static final Item particle_tachyon = new ItemBase("particle_tachyon").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty);
    public static final Item particle_dark = new ItemBase("particle_dark").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty);
    public static final Item particle_strange = new ItemBase("particle_strange").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty);
    public static final Item particle_sparkticle = new ItemBase("particle_sparkticle").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty);
    public static final Item particle_digamma = new ItemDigamma(60, "particle_digamma").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty);
    public static final Item capsule_xen = new ItemDrop("capsule_xen").setCreativeTab(MainRegistry.controlTab);
    public static final Item singularity = new ItemDrop("singularity").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.nuclear_waste);
    public static final Item singularity_counter_resonant = new ItemDrop("singularity_counter_resonant").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.nuclear_waste);
    public static final Item singularity_super_heated = new ItemDrop("singularity_super_heated").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.nuclear_waste);
    public static final Item black_hole = new ItemDrop("black_hole").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.nuclear_waste);
    public static final Item singularity_spark = new ItemDrop("singularity_spark").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.nuclear_waste);
    public static final Item pellet_antimatter = new ItemDrop("pellet_antimatter").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell);
    public static final Item crystal_xen = new ItemDrop("crystal_xen").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item crystal_energy = new ItemCustomLore("crystal_energy").setCreativeTab(null).setMaxStackSize(1);
    public static final Item pellet_coolant = new ItemBase("pellet_coolant").setMaxDamage(41400).setCreativeTab(null).setMaxStackSize(1);
    //Keys + locks
    public static final Item key = new ItemKey("key").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item key_red = new ItemCustomLore("key_red").setMaxStackSize(1).setCreativeTab(null);
    public static final Item key_red_cracked = new ItemCustomLore("key_red_cracked").setMaxStackSize(1).setCreativeTab(null);
    public static final Item key_kit = new ItemCounterfitKeys("key_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item key_fake = new ItemKey("key_fake").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item pin = new ItemCustomLore("pin").setMaxStackSize(8).setCreativeTab(MainRegistry.consumableTab);
    public static final Item padlock_rusty = new ItemLock(1, "padlock_rusty").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item padlock = new ItemLock(0.1, "padlock").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item padlock_reinforced = new ItemLock(0.02, "padlock_reinforced").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item padlock_unbreakable = new ItemLock(0, "padlock_unbreakable").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item mech_key = new ItemCustomLore("mech_key").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    //AMS Catalysts
    public static final Item ams_catalyst_blank = new ItemBase("ams_catalyst_blank").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    public static final Item ams_catalyst_iron = new ItemCatalyst(0xFF7E22, 10, 0.50F, 1.50F, 1.50F, "ams_catalyst_iron").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    public static final Item ams_catalyst_copper = new ItemCatalyst(0xAADE29, 100, 0.60F, 1.20F, 0.60F, "ams_catalyst_copper").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    public static final Item ams_catalyst_aluminium = new ItemCatalyst(0xCCCCCC, 250, 0.70F, 0.85F, 0.85F, "ams_catalyst_aluminium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    public static final Item ams_catalyst_lithium = new ItemCatalyst(0xFF2727, 500, 0.80F, 0.75F, 1.15F, "ams_catalyst_lithium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    public static final Item ams_catalyst_beryllium = new ItemCatalyst(0x97978B, 1000, 0.90F, 1.15F, 0.75F, "ams_catalyst_beryllium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    public static final Item ams_catalyst_tungsten = new ItemCatalyst(0xF5FF48, 5000, 1.00F, 1.00F, 0.95F, "ams_catalyst_tungsten").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    public static final Item ams_catalyst_cobalt = new ItemCatalyst(0x789BBE, 10000, 1.02F, 0.95F, 1.00F, "ams_catalyst_cobalt").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    public static final Item ams_catalyst_niobium = new ItemCatalyst(0x3BF1B6, 25000, 1.05F, 1.15F, 1.00F, "ams_catalyst_niobium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    public static final Item ams_catalyst_cerium = new ItemCatalyst(0x1D3FFF, 50000, 1.05F, 1.00F, 1.15F, "ams_catalyst_cerium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    public static final Item ams_catalyst_thorium = new ItemCatalyst(0x653B22, 100000, 1.10F, 0.95F, 1.20F, "ams_catalyst_thorium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    public static final Item ams_catalyst_strontium = new ItemCatalyst(0xDD0D35, 200000, 1.15F, 0.90F, 1.30F, "ams_catalyst_strontium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    public static final Item ams_catalyst_caesium = new ItemCatalyst(0x6400FF, 400000, 1.20F, 0.85F, 1.40F, "ams_catalyst_caesium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    public static final Item ams_catalyst_schrabidium = new ItemCatalyst(0x32FFFF, 600000, 1.30F, 0.70F, 1.25F, "ams_catalyst_schrabidium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    public static final Item ams_catalyst_euphemium = new ItemCatalyst(0xFF9CD2, 800000, 1.50F, 1.25F, 0.70F, "ams_catalyst_euphemium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    public static final Item ams_catalyst_dineutronium = new ItemCatalyst(0x334077, 1000000, 2.00F, 1.50F, 2.00F, "ams_catalyst_dineutronium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
    //AMS
    public static final Item ams_lens = new ItemLens(60 * 60 * 60 * 20 * 100, "ams_lens").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item ams_core_sing = new ItemAMSCore(500, 0.8F, 1.5F, "ams_core_sing").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item ams_core_wormhole = new ItemAMSCore(650, 1.5F, 0.8F, "ams_core_wormhole").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item ams_core_eyeofharmony = new ItemAMSCore(800, 1.5F, 2.0F, "ams_core_eyeofharmony").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    public static final Item ams_core_thingy = new ItemAMSCore(2500, 0.7F, 0.7F, "ams_core_thingy").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item ams_muzzle = new ItemCustomLore("ams_muzzle").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    //Special tools
    public static final Item fluid_icon = new ItemFluidIcon("fluid_icon").setCreativeTab(null);
    //Nuke parts
    public static final Item gadget_explosive = new ItemBase("gadget_explosive").setCreativeTab(MainRegistry.nukeTab);
    public static final Item early_explosive_lenses = new ItemGadget("gadget_explosive8").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item gadget_wireing = new ItemGadget("gadget_wireing").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item gadget_core = new ItemGadget("gadget_core").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item boy_shielding = new ItemBoy("boy_shielding").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item boy_target = new ItemBoy("boy_target").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item boy_bullet = new ItemBoy("boy_bullet").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item boy_propellant = new ItemBoy("boy_propellant").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item boy_igniter = new ItemBoy("boy_igniter").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item man_core = new ItemManMike("man_core").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item man_explosive = new ItemBase("man_explosive").setCreativeTab(MainRegistry.nukeTab);
    public static final Item explosive_lenses = new ItemManMike("man_explosive8").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item man_igniter = new ItemMan("man_igniter").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item mike_core = new ItemMike("mike_core").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item mike_cooling_unit = new ItemMike("mike_cooling_unit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item fleija_igniter = new ItemFleija("fleija_igniter").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item fleija_propellant = new ItemFleija("fleija_propellant").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item fleija_core = new ItemFleija("fleija_core").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item solinium_core = new ItemSolinium("solinium_core").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item solinium_igniter = new ItemSolinium("solinium_igniter").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item solinium_propellant = new ItemSolinium("solinium_propellant").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item n2_charge = new ItemN2("n2_charge").setMaxStackSize(12).setCreativeTab(MainRegistry.nukeTab);
    public static final Item egg_balefire_shard = new ItemCustomLore("egg_balefire_shard").setMaxStackSize(16).setCreativeTab(MainRegistry.nukeTab);
    public static final Item egg_balefire = new ItemCustomLore("egg_balefire").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item custom_tnt = new ItemCustomLore("custom_tnt").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item custom_nuke = new ItemCustomLore("custom_nuke").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item custom_hydro = new ItemCustomLore("custom_hydro").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item custom_amat = new ItemCustomLore("custom_amat").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item custom_dirty = new ItemCustomLore("custom_dirty").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item custom_schrab = new ItemCustomLore("custom_schrab").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item custom_sol = new ItemCustomLore("custom_sol").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item custom_euph = new ItemCustomLore("custom_euph").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item custom_fall = new ItemCustomLore("custom_fall").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    //Kits
    public static final Item grenade_kit = new ItemStarterKit("grenade_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab);
    public static final Item gadget_kit = new ItemStarterKit("gadget_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item boy_kit = new ItemStarterKit("boy_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item man_kit = new ItemStarterKit("man_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item mike_kit = new ItemStarterKit("mike_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item tsar_kit = new ItemStarterKit("tsar_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item prototype_kit = new ItemStarterKit("prototype_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item fleija_kit = new ItemStarterKit("fleija_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item solinium_kit = new ItemStarterKit("solinium_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item balefire_kit = new ItemStarterKit("balefire_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item multi_kit = new ItemStarterKit("multi_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item custom_kit = new ItemStarterKit("custom_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab);
    public static final Item missile_kit = new ItemStarterKit("missile_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item t45_kit = new ItemStarterKit("t45_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item hazmat_kit = new ItemStarterKit("hazmat_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item hazmat_red_kit = new ItemStarterKit("hazmat_red_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item hazmat_grey_kit = new ItemStarterKit("hazmat_grey_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item nuke_starter_kit = new ItemStarterKit("nuke_starter_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item nuke_advanced_kit = new ItemStarterKit("nuke_advanced_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item nuke_commercially_kit = new ItemStarterKit("nuke_commercially_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item kit_custom = new ItemKitCustom("kit_custom").setCreativeTab(MainRegistry.consumableTab);
    public static final Item toolbox = new ItemToolBox("toolbox").setCreativeTab(MainRegistry.consumableTab);
    public static final Item legacy_toolbox = new ItemKitNBT("toolbox_legacy").setContainerItem(toolbox).setCreativeTab(MainRegistry.consumableTab);
    public static final Item loot_10 = new ItemLootCrate("loot_10").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item loot_15 = new ItemLootCrate("loot_15").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item loot_misc = new ItemLootCrate("loot_misc").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    //Satellites
    public static final Item sat_mapper = new ItemSatellite("sat_mapper").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item sat_scanner = new ItemSatellite("sat_scanner").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item sat_radar = new ItemSatellite("sat_radar").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item sat_laser = new ItemSatellite("sat_laser").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item sat_foeq = new ItemSatellite("sat_foeq").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item sat_resonator = new ItemSatellite("sat_resonator").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item sat_miner = new ItemSatellite("sat_miner").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item sat_lunar_miner = new ItemSatellite("sat_lunar_miner").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item sat_gerald = new ItemSatellite("sat_gerald").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item sat_chip = new ItemSatellite("sat_chip").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item sat_interface = new ItemSatInterface("sat_interface").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item sat_coord = new ItemSatInterface("sat_coord").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item sat_relay = new ItemSatellite("sat_relay").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    //Missiles
    public static final Item rangefinder = new ItemRangefinder("rangefinder").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item designator = new ItemDesignator("designator").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item designator_range = new ItemDesignatorRange("designator_range").setFull3D().setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item designator_manual = new ItemDesignatorManual("designator_manual").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item launch_code_piece = new ItemBakedBase("launch_code_piece").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab);
    public static final Item launch_code = new ItemBakedBase("launch_code").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab);
    public static final Item launch_key = new ItemBakedBase("launch_key").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab);
    public static final Item missile_assembly = new ItemBase("missile_assembly").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab);
    public static final Item missile_generic = new ItemMissileStandard("missile_generic", ItemMissileStandard.MissileFormFactor.V2, ItemMissileStandard.MissileTier.TIER1).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_strong = new ItemMissileStandard("missile_strong", ItemMissileStandard.MissileFormFactor.STRONG, ItemMissileStandard.MissileTier.TIER2).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_burst = new ItemMissileStandard("missile_burst", ItemMissileStandard.MissileFormFactor.HUGE, ItemMissileStandard.MissileTier.TIER3).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_incendiary = new ItemMissileStandard("missile_incendiary", ItemMissileStandard.MissileFormFactor.V2, ItemMissileStandard.MissileTier.TIER1).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_incendiary_strong = new ItemMissileStandard("missile_incendiary_strong", ItemMissileStandard.MissileFormFactor.STRONG, ItemMissileStandard.MissileTier.TIER2).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_inferno = new ItemMissileStandard("missile_inferno", ItemMissileStandard.MissileFormFactor.HUGE, ItemMissileStandard.MissileTier.TIER3).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_cluster = new ItemMissileStandard("missile_cluster", ItemMissileStandard.MissileFormFactor.V2, ItemMissileStandard.MissileTier.TIER1).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_cluster_strong = new ItemMissileStandard("missile_cluster_strong", ItemMissileStandard.MissileFormFactor.STRONG, ItemMissileStandard.MissileTier.TIER2).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_rain = new ItemMissileStandard("missile_rain", ItemMissileStandard.MissileFormFactor.HUGE, ItemMissileStandard.MissileTier.TIER3).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_buster = new ItemMissileStandard("missile_buster", ItemMissileStandard.MissileFormFactor.V2, ItemMissileStandard.MissileTier.TIER1).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_buster_strong = new ItemMissileStandard("missile_buster_strong", ItemMissileStandard.MissileFormFactor.STRONG, ItemMissileStandard.MissileTier.TIER2).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_drill = new ItemMissileStandard("missile_drill", ItemMissileStandard.MissileFormFactor.HUGE, ItemMissileStandard.MissileTier.TIER3).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_n2 = new ItemMissileStandard("missile_n2", ItemMissileStandard.MissileFormFactor.HUGE, ItemMissileStandard.MissileTier.TIER3).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_nuclear = new ItemMissileStandard("missile_nuclear", ItemMissileStandard.MissileFormFactor.ATLAS, ItemMissileStandard.MissileTier.TIER4).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_nuclear_cluster = new ItemMissileStandard("missile_nuclear_cluster", ItemMissileStandard.MissileFormFactor.ATLAS, ItemMissileStandard.MissileTier.TIER4).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_volcano = new ItemMissileStandard("missile_volcano", ItemMissileStandard.MissileFormFactor.ATLAS, ItemMissileStandard.MissileTier.TIER4).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_endo = new ItemMissileStandard("missile_endo", ItemMissileStandard.MissileFormFactor.HUGE, ItemMissileStandard.MissileTier.TIER3).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_exo = new ItemMissileStandard("missile_exo", ItemMissileStandard.MissileFormFactor.HUGE, ItemMissileStandard.MissileTier.TIER3).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_doomsday = new ItemMissileStandard("missile_doomsday", ItemMissileStandard.MissileFormFactor.ATLAS, ItemMissileStandard.MissileTier.TIER4).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_doomsday_rusted = new ItemMissileStandard("missile_doomsday_rusted", ItemMissileStandard.MissileFormFactor.ATLAS, ItemMissileStandard.MissileTier.TIER4).notLaunchable().setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_taint = new ItemMissileStandard("missile_taint", ItemMissileStandard.MissileFormFactor.MICRO, ItemMissileStandard.MissileTier.TIER0).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_micro = new ItemMissileStandard("missile_micro", ItemMissileStandard.MissileFormFactor.MICRO, ItemMissileStandard.MissileTier.TIER0).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_bhole = new ItemMissileStandard("missile_bhole", ItemMissileStandard.MissileFormFactor.MICRO, ItemMissileStandard.MissileTier.TIER0).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_schrabidium = new ItemMissileStandard("missile_schrabidium", ItemMissileStandard.MissileFormFactor.MICRO, ItemMissileStandard.MissileTier.TIER0).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_emp = new ItemMissileStandard("missile_emp", ItemMissileStandard.MissileFormFactor.MICRO, ItemMissileStandard.MissileTier.TIER0).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_emp_strong = new ItemMissileStandard("missile_emp_strong", ItemMissileStandard.MissileFormFactor.STRONG, ItemMissileStandard.MissileTier.TIER2).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_anti_ballistic = new ItemMissileStandard("missile_anti_ballistic", ItemMissileStandard.MissileFormFactor.ABM, ItemMissileStandard.MissileTier.TIER1).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_carrier = new ItemMissileStandard("missile_carrier", ItemMissileStandard.MissileFormFactor.STRONG, ItemMissileStandard.MissileTier.TIER2).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_decoy = new ItemMissileStandard("missile_decoy", ItemMissileStandard.MissileFormFactor.V2, ItemMissileStandard.MissileTier.TIER1).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_stealth = new ItemMissileStandard("missile_stealth", ItemMissileStandard.MissileFormFactor.STRONG, ItemMissileStandard.MissileTier.TIER1).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_soyuz_lander = new ItemCustomLore("missile_soyuz_lander", "soyuz_lander").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_soyuz = new ItemSoyuz("missile_soyuz").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item warhead_generic_small = new ItemBase("warhead_generic_small").setCreativeTab(MainRegistry.partsTab);
    public static final Item warhead_incendiary_small = new ItemBase("warhead_incendiary_small").setCreativeTab(MainRegistry.partsTab);
    public static final Item warhead_cluster_small = new ItemBase("warhead_cluster_small").setCreativeTab(MainRegistry.partsTab);
    public static final Item warhead_buster_small = new ItemBase("warhead_buster_small").setCreativeTab(MainRegistry.partsTab);
    public static final Item warhead_generic_medium = new ItemBase("warhead_generic_medium").setCreativeTab(MainRegistry.partsTab);
    public static final Item warhead_incendiary_medium = new ItemBase("warhead_incendiary_medium").setCreativeTab(MainRegistry.partsTab);
    public static final Item warhead_cluster_medium = new ItemBase("warhead_cluster_medium").setCreativeTab(MainRegistry.partsTab);
    public static final Item warhead_buster_medium = new ItemBase("warhead_buster_medium").setCreativeTab(MainRegistry.partsTab);
    public static final Item warhead_generic_large = new ItemBase("warhead_generic_large").setCreativeTab(MainRegistry.partsTab);
    public static final Item warhead_incendiary_large = new ItemBase("warhead_incendiary_large").setCreativeTab(MainRegistry.partsTab);
    public static final Item warhead_cluster_large = new ItemBase("warhead_cluster_large").setCreativeTab(MainRegistry.partsTab);
    public static final Item warhead_buster_large = new ItemBase("warhead_buster_large").setCreativeTab(MainRegistry.partsTab);
    public static final Item warhead_n2 = new ItemBase("warhead_n2").setCreativeTab(MainRegistry.partsTab);
    public static final Item warhead_nuclear = new ItemBase("warhead_nuclear").setCreativeTab(MainRegistry.partsTab);
    public static final Item warhead_mirvlet = new ItemBase("warhead_mirvlet").setCreativeTab(MainRegistry.partsTab);
    public static final Item warhead_mirv = new ItemBase("warhead_mirv").setCreativeTab(MainRegistry.partsTab);
    public static final Item warhead_volcano = new ItemBase("warhead_volcano").setCreativeTab(MainRegistry.partsTab);
    public static final Item warhead_thermo_endo = new ItemBase("warhead_thermo_endo").setCreativeTab(MainRegistry.partsTab);
    public static final Item warhead_thermo_exo = new ItemBase("warhead_thermo_exo").setCreativeTab(MainRegistry.partsTab);
    public static final Item thruster_small = new ItemBase("thruster_small").setCreativeTab(MainRegistry.partsTab);
    public static final Item thruster_medium = new ItemBase("thruster_medium").setCreativeTab(MainRegistry.partsTab);
    public static final Item thruster_large = new ItemBase("thruster_large").setCreativeTab(MainRegistry.partsTab);
    public static final Item cap_aluminium = new ItemBase("cap_aluminium").setCreativeTab(MainRegistry.partsTab);
    public static final Item fins_flat = new ItemBase("fins_flat").setCreativeTab(MainRegistry.partsTab);
    public static final Item fins_small_steel = new ItemBase("fins_small_steel").setCreativeTab(MainRegistry.partsTab);
    public static final Item fins_big_steel = new ItemBase("fins_big_steel").setCreativeTab(MainRegistry.partsTab);
    public static final Item fins_tri_steel = new ItemBase("fins_tri_steel").setCreativeTab(MainRegistry.partsTab);
    public static final Item fins_quad_titanium = new ItemBase("fins_quad_titanium").setCreativeTab(MainRegistry.partsTab);
    public static final Item sphere_steel = new ItemBase("sphere_steel").setCreativeTab(MainRegistry.partsTab);
    public static final Item pedestal_steel = new ItemBase("pedestal_steel").setCreativeTab(MainRegistry.partsTab);
    public static final Item dysfunctional_reactor = new ItemBase("dysfunctional_reactor").setCreativeTab(MainRegistry.partsTab);
    public static final Item rotor_steel = new ItemBase("rotor_steel").setCreativeTab(MainRegistry.partsTab);
    public static final Item generator_steel = new ItemBase("generator_steel").setCreativeTab(MainRegistry.partsTab);
    public static final Item sat_head_mapper = new ItemBase("sat_head_mapper").setCreativeTab(MainRegistry.partsTab);
    public static final Item sat_head_scanner = new ItemBase("sat_head_scanner").setCreativeTab(MainRegistry.partsTab);
    public static final Item sat_head_radar = new ItemBase("sat_head_radar").setCreativeTab(MainRegistry.partsTab);
    public static final Item sat_head_laser = new ItemBase("sat_head_laser").setCreativeTab(MainRegistry.partsTab);
    public static final Item sat_head_resonator = new ItemBase("sat_head_resonator").setCreativeTab(MainRegistry.partsTab);
    public static final Item seg_10 = new ItemBase("seg_10").setCreativeTab(MainRegistry.partsTab);
    public static final Item seg_15 = new ItemBase("seg_15").setCreativeTab(MainRegistry.partsTab);
    public static final Item seg_20 = new ItemBase("seg_20").setCreativeTab(MainRegistry.partsTab);
    public static final Item fuel_tank_small = new ItemBase("fuel_tank_small").setCreativeTab(MainRegistry.partsTab);
    public static final Item fuel_tank_medium = new ItemBase("fuel_tank_medium").setCreativeTab(MainRegistry.partsTab);
    public static final Item fuel_tank_large = new ItemBase("fuel_tank_large").setCreativeTab(MainRegistry.partsTab);
    public static final Item tank_steel = new ItemBase("tank_steel").setCreativeTab(MainRegistry.partsTab);
    public static final Item mike_deut = new ItemMike("mike_deut").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setContainerItem(ModItems.tank_steel);
    public static final Item mp_thruster_10_kerosene = new ItemMissile("mp_thruster_10_kerosene").makeThruster(FuelType.KEROSENE, 1F, 1.5F, PartSize.SIZE_10).setHealth(10F);
    public static final Item mp_thruster_10_solid = new ItemMissile("mp_thruster_10_solid").makeThruster(FuelType.SOLID, 1F, 1.5F, PartSize.SIZE_10).setHealth(15F);
    public static final Item mp_thruster_10_xenon = new ItemMissile("mp_thruster_10_xenon").makeThruster(FuelType.XENON, 1F, 1.5F, PartSize.SIZE_10).setHealth(5F);
    public static final Item mp_thruster_15_kerosene = new ItemMissile("mp_thruster_15_kerosene").makeThruster(FuelType.KEROSENE, 1F, 7.5F, PartSize.SIZE_15).setHealth(15F);
    public static final Item mp_thruster_15_kerosene_dual = new ItemMissile("mp_thruster_15_kerosene_dual").makeThruster(FuelType.KEROSENE, 1F, 6.5F, PartSize.SIZE_15).setHealth(15F);
    public static final Item mp_thruster_15_kerosene_triple = new ItemMissile("mp_thruster_15_kerosene_triple").makeThruster(FuelType.KEROSENE, 1F, 5F, PartSize.SIZE_15).setHealth(15F);
    public static final Item mp_thruster_15_solid = new ItemMissile("mp_thruster_15_solid").makeThruster(FuelType.SOLID, 1F, 5F, PartSize.SIZE_15).setHealth(20F);
    public static final Item mp_thruster_15_solid_hexdecuple = new ItemMissile("mp_thruster_15_solid_hexdecuple").makeThruster(FuelType.SOLID, 1F, 7F, PartSize.SIZE_15).setHealth(25F).setRarity(Rarity.UNCOMMON);
    public static final Item mp_thruster_15_hydrogen = new ItemMissile("mp_thruster_15_hydrogen").makeThruster(FuelType.HYDROGEN, 1F, 7.5F, PartSize.SIZE_15).setHealth(20F);
    public static final Item mp_thruster_15_hydrogen_dual = new ItemMissile("mp_thruster_15_hydrogen_dual").makeThruster(FuelType.HYDROGEN, 1F, 5.0F, PartSize.SIZE_15).setHealth(15F);
    public static final Item mp_thruster_15_balefire_short = new ItemMissile("mp_thruster_15_balefire_short").makeThruster(FuelType.BALEFIRE, 1F, 5F, PartSize.SIZE_15).setHealth(25F);
    public static final Item mp_thruster_15_balefire = new ItemMissile("mp_thruster_15_balefire").makeThruster(FuelType.BALEFIRE, 1F, 6.5F, PartSize.SIZE_15).setHealth(25F);
    public static final Item mp_thruster_15_balefire_large = new ItemMissile("mp_thruster_15_balefire_large").makeThruster(FuelType.BALEFIRE, 1F, 7.0F, PartSize.SIZE_15).setHealth(35F);
    public static final Item mp_thruster_15_balefire_large_rad = new ItemMissile("mp_thruster_15_balefire_large_rad").makeThruster(FuelType.BALEFIRE, 1F, 7.5F, PartSize.SIZE_15).setAuthor("The Master").setHealth(35F).setRarity(Rarity.UNCOMMON);
    public static final Item mp_thruster_20_kerosene = new ItemMissile("mp_thruster_20_kerosene").makeThruster(FuelType.KEROSENE, 1F, 100F, PartSize.SIZE_20).setHealth(30F);
    public static final Item mp_thruster_20_kerosene_dual = new ItemMissile("mp_thruster_20_kerosene_dual").makeThruster(FuelType.KEROSENE, 1F, 100F, PartSize.SIZE_20).setHealth(30F);
    public static final Item mp_thruster_20_kerosene_triple = new ItemMissile("mp_thruster_20_kerosene_triple").makeThruster(FuelType.KEROSENE, 1F, 100F, PartSize.SIZE_20).setHealth(30F);
    public static final Item mp_thruster_20_solid = new ItemMissile("mp_thruster_20_solid").makeThruster(FuelType.SOLID, 1F, 100F, PartSize.SIZE_20).setHealth(35F).setWittyText("It's basically just a big hole at the end of the fuel tank.");
    public static final Item mp_thruster_20_solid_multi = new ItemMissile("mp_thruster_20_solid_multi").makeThruster(FuelType.SOLID, 1F, 100F, PartSize.SIZE_20).setHealth(35F);
    public static final Item mp_thruster_20_solid_multier = new ItemMissile("mp_thruster_20_solid_multier").makeThruster(FuelType.SOLID, 1F, 100F, PartSize.SIZE_20).setHealth(35F).setWittyText("Did I miscount? Hope not.");
    public static final Item mp_stability_10_flat = new ItemMissile("mp_stability_10_flat").makeStability(0.5F, PartSize.SIZE_10).setHealth(10F);
    public static final Item mp_stability_10_cruise = new ItemMissile("mp_stability_10_cruise").makeStability(0.25F, PartSize.SIZE_10).setHealth(5F);
    public static final Item mp_stability_10_space = new ItemMissile("mp_stability_10_space").makeStability(0.35F, PartSize.SIZE_10).setHealth(5F).setRarity(Rarity.COMMON).setWittyText("Standing there alone, the ship is waiting / All systems are go, are you sure?");
    public static final Item mp_stability_15_flat = new ItemMissile("mp_stability_15_flat").makeStability(0.5F, PartSize.SIZE_15).setHealth(10F);
    public static final Item mp_stability_15_thin = new ItemMissile("mp_stability_15_thin").makeStability(0.35F, PartSize.SIZE_15).setHealth(5F);
    public static final Item mp_stability_15_soyuz = new ItemMissile("mp_stability_15_soyuz").makeStability(0.25F, PartSize.SIZE_15).setHealth(15F).setRarity(Rarity.COMMON).setWittyText("Союз!");
    public static final Item mp_fuselage_10_kerosene = new ItemMissile("mp_fuselage_10_kerosene").makeFuselage(FuelType.KEROSENE, 2500F, 1000, PartSize.SIZE_10, PartSize.SIZE_10).setAuthor("Hoboy").setHealth(20F);
    public static final Item mp_fuselage_10_kerosene_camo = ((ItemMissile) mp_fuselage_10_kerosene).copy("mp_fuselage_10_kerosene_camo").setRarity(Rarity.COMMON).setTitle("Camo");
    public static final Item mp_fuselage_10_kerosene_desert = ((ItemMissile) mp_fuselage_10_kerosene).copy("mp_fuselage_10_kerosene_desert").setRarity(Rarity.COMMON).setTitle("Desert Camo");
    public static final Item mp_fuselage_10_kerosene_sky = ((ItemMissile) mp_fuselage_10_kerosene).copy("mp_fuselage_10_kerosene_sky").setRarity(Rarity.COMMON).setTitle("Sky Camo");
    public static final Item mp_fuselage_10_kerosene_flames = ((ItemMissile) mp_fuselage_10_kerosene).copy("mp_fuselage_10_kerosene_flames").setRarity(Rarity.UNCOMMON).setTitle("Sick Flames");
    public static final Item mp_fuselage_10_kerosene_insulation = ((ItemMissile) mp_fuselage_10_kerosene).copy("mp_fuselage_10_kerosene_insulation").setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(25F);
    public static final Item mp_fuselage_10_kerosene_sleek = ((ItemMissile) mp_fuselage_10_kerosene).copy("mp_fuselage_10_kerosene_sleek").setRarity(Rarity.RARE).setTitle("IF-R&D").setHealth(35F);
    public static final Item mp_fuselage_10_kerosene_metal = ((ItemMissile) mp_fuselage_10_kerosene).copy("mp_fuselage_10_kerosene_metal").setRarity(Rarity.UNCOMMON).setTitle("Bolted Metal").setHealth(30F).setAuthor("Hoboy");
    public static final Item mp_fuselage_10_kerosene_taint = ((ItemMissile) mp_fuselage_10_kerosene).copy("mp_fuselage_10_kerosene_taint").setRarity(Rarity.UNCOMMON).setAuthor("Sam").setTitle("Tainted");
    public static final Item mp_fuselage_10_solid = new ItemMissile("mp_fuselage_10_solid").makeFuselage(FuelType.SOLID, 2500F, 1000, PartSize.SIZE_10, PartSize.SIZE_10).setHealth(25F);
    public static final Item mp_fuselage_10_solid_flames = ((ItemMissile) mp_fuselage_10_solid).copy("mp_fuselage_10_solid_flames").setRarity(Rarity.UNCOMMON).setTitle("Sick Flames");
    public static final Item mp_fuselage_10_solid_insulation = ((ItemMissile) mp_fuselage_10_solid).copy("mp_fuselage_10_solid_insulation").setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(30F);
    public static final Item mp_fuselage_10_solid_sleek = ((ItemMissile) mp_fuselage_10_solid).copy("mp_fuselage_10_solid_sleek").setRarity(Rarity.RARE).setTitle("IF-R&D").setHealth(35F);
    public static final Item mp_fuselage_10_solid_soviet_glory = ((ItemMissile) mp_fuselage_10_solid).copy("mp_fuselage_10_solid_soviet_glory").setRarity(Rarity.EPIC).setAuthor("Hoboy").setHealth(35F).setTitle("Soviet Glory");
    public static final Item mp_fuselage_10_solid_cathedral = ((ItemMissile) mp_fuselage_10_solid).copy("mp_fuselage_10_solid_cathedral").setRarity(Rarity.RARE).setAuthor("Satan").setTitle("Unholy Cathedral").setWittyText("Quakeesque!");
    public static final Item mp_fuselage_10_solid_moonlit = ((ItemMissile) mp_fuselage_10_solid).copy("mp_fuselage_10_solid_moonlit").setRarity(Rarity.UNCOMMON).setAuthor("The Master & Hoboy").setTitle("Moonlit");
    public static final Item mp_fuselage_10_solid_battery = ((ItemMissile) mp_fuselage_10_solid).copy("mp_fuselage_10_solid_battery").setRarity(Rarity.UNCOMMON).setAuthor("wolfmonster222").setHealth(30F).setTitle("Ecstatic").setWittyText("I got caught eating batteries again :(");
    public static final Item mp_fuselage_10_solid_duracell = ((ItemMissile) mp_fuselage_10_solid).copy("mp_fuselage_10_solid_duracell").setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Duracell").setHealth(30F).setWittyText("The crunchiest battery on the market!");
    public static final Item mp_fuselage_10_xenon = new ItemMissile("mp_fuselage_10_xenon").makeFuselage(FuelType.XENON, 5000F, 1000, PartSize.SIZE_10, PartSize.SIZE_10).setHealth(20F);
    public static final Item mp_fuselage_10_xenon_bhole = ((ItemMissile) mp_fuselage_10_xenon).copy("mp_fuselage_10_xenon_bhole").setRarity(Rarity.RARE).setAuthor("Sten89").setTitle("Morceus-1457");
    public static final Item mp_fuselage_10_long_kerosene = new ItemMissile("mp_fuselage_10_long_kerosene").makeFuselage(FuelType.KEROSENE, 5000F, 1000, PartSize.SIZE_10, PartSize.SIZE_10).setAuthor("Hoboy").setHealth(30F);
    public static final Item mp_fuselage_10_long_kerosene_camo = ((ItemMissile) mp_fuselage_10_long_kerosene).copy("mp_fuselage_10_long_kerosene_camo").setRarity(Rarity.COMMON).setTitle("Camo");
    public static final Item mp_fuselage_10_long_kerosene_desert = ((ItemMissile) mp_fuselage_10_long_kerosene).copy("mp_fuselage_10_long_kerosene_desert").setRarity(Rarity.COMMON).setTitle("Desert Camo");
    public static final Item mp_fuselage_10_long_kerosene_sky = ((ItemMissile) mp_fuselage_10_long_kerosene).copy("mp_fuselage_10_long_kerosene_sky").setRarity(Rarity.COMMON).setTitle("Sky Camo");
    public static final Item mp_fuselage_10_long_kerosene_flames = ((ItemMissile) mp_fuselage_10_long_kerosene).copy("mp_fuselage_10_long_kerosene_flames").setRarity(Rarity.UNCOMMON).setTitle("Sick Flames");
    public static final Item mp_fuselage_10_long_kerosene_insulation = ((ItemMissile) mp_fuselage_10_long_kerosene).copy("mp_fuselage_10_long_kerosene_insulation").setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(35F);
    public static final Item mp_fuselage_10_long_kerosene_sleek = ((ItemMissile) mp_fuselage_10_long_kerosene).copy("mp_fuselage_10_long_kerosene_sleek").setRarity(Rarity.RARE).setTitle("IF-R&D").setHealth(40F);
    public static final Item mp_fuselage_10_long_kerosene_metal = ((ItemMissile) mp_fuselage_10_long_kerosene).copy("mp_fuselage_10_long_kerosene_metal").setRarity(Rarity.UNCOMMON).setAuthor("Hoboy").setHealth(35F);
    public static final Item mp_fuselage_10_long_kerosene_dash = ((ItemMissile) mp_fuselage_10_long_kerosene).copy("mp_fuselage_10_long_kerosene_dash").setRarity(Rarity.EPIC).setAuthor("Sam").setTitle("Dash").setWittyText("I wash my hands of it.");
    public static final Item mp_fuselage_10_long_kerosene_taint = ((ItemMissile) mp_fuselage_10_long_kerosene).copy("mp_fuselage_10_long_kerosene_taint").setRarity(Rarity.UNCOMMON).setAuthor("Sam").setTitle("Tainted");
    public static final Item mp_fuselage_10_long_kerosene_vap = ((ItemMissile) mp_fuselage_10_long_kerosene).copy("mp_fuselage_10_long_kerosene_vap").setRarity(Rarity.EPIC).setAuthor("VT-6/24").setTitle("Minty Contrail").setWittyText("Upper rivet!");
    public static final Item mp_fuselage_10_long_solid = new ItemMissile("mp_fuselage_10_long_solid").makeFuselage(FuelType.SOLID, 5000F, 1000, PartSize.SIZE_10, PartSize.SIZE_10).setHealth(35F);
    public static final Item mp_fuselage_10_long_solid_flames = ((ItemMissile) mp_fuselage_10_long_solid).copy("mp_fuselage_10_long_solid_flames").setRarity(Rarity.UNCOMMON).setTitle("Sick Flames");
    public static final Item mp_fuselage_10_long_solid_insulation = ((ItemMissile) mp_fuselage_10_long_solid).copy("mp_fuselage_10_long_solid_insulation").setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(40F);
    public static final Item mp_fuselage_10_long_solid_sleek = ((ItemMissile) mp_fuselage_10_long_solid).copy("mp_fuselage_10_long_solid_sleek").setRarity(Rarity.RARE).setTitle("IF-R&D").setHealth(45F);
    public static final Item mp_fuselage_10_long_solid_soviet_glory = ((ItemMissile) mp_fuselage_10_long_solid).copy("mp_fuselage_10_long_solid_soviet_glory").setRarity(Rarity.EPIC).setAuthor("Hoboy").setHealth(45F).setTitle("Soviet Glory").setWittyText("Fully Automated Luxury Gay Space Communism!");
    public static final Item mp_fuselage_10_long_solid_bullet = ((ItemMissile) mp_fuselage_10_long_solid).copy("mp_fuselage_10_long_solid_bullet").setRarity(Rarity.COMMON).setAuthor("Sam").setTitle("Bullet Bill");
    public static final Item mp_fuselage_10_long_solid_silvermoonlight = ((ItemMissile) mp_fuselage_10_long_solid).copy("mp_fuselage_10_long_solid_silvermoonlight").setRarity(Rarity.UNCOMMON).setAuthor("The Master").setTitle("Silver Moonlight");
    public static final Item mp_fuselage_10_15_kerosene = new ItemMissile("mp_fuselage_10_15_kerosene").makeFuselage(FuelType.KEROSENE, 10000F, 1000, PartSize.SIZE_10, PartSize.SIZE_15).setHealth(40F);
    public static final Item mp_fuselage_10_15_solid = new ItemMissile("mp_fuselage_10_15_solid").makeFuselage(FuelType.SOLID, 10000F, 1000, PartSize.SIZE_10, PartSize.SIZE_15).setHealth(40F);
    public static final Item mp_fuselage_10_15_hydrogen = new ItemMissile("mp_fuselage_10_15_hydrogen").makeFuselage(FuelType.HYDROGEN, 10000F, 1000, PartSize.SIZE_10, PartSize.SIZE_15).setHealth(40F);
    public static final Item mp_fuselage_10_15_balefire = new ItemMissile("mp_fuselage_10_15_balefire").makeFuselage(FuelType.BALEFIRE, 10000F, 1000, PartSize.SIZE_10, PartSize.SIZE_15).setHealth(40F);
    public static final Item mp_fuselage_15_kerosene = new ItemMissile("mp_fuselage_15_kerosene").makeFuselage(FuelType.KEROSENE, 15000F, 1000, PartSize.SIZE_15, PartSize.SIZE_15).setAuthor("Hoboy").setHealth(50F);
    public static final Item mp_fuselage_15_kerosene_camo = ((ItemMissile) mp_fuselage_15_kerosene).copy("mp_fuselage_15_kerosene_camo").setRarity(Rarity.COMMON).setTitle("Camo");
    public static final Item mp_fuselage_15_kerosene_desert = ((ItemMissile) mp_fuselage_15_kerosene).copy("mp_fuselage_15_kerosene_desert").setRarity(Rarity.COMMON).setTitle("Desert Camo");
    public static final Item mp_fuselage_15_kerosene_sky = ((ItemMissile) mp_fuselage_15_kerosene).copy("mp_fuselage_15_kerosene_sky").setRarity(Rarity.COMMON).setTitle("Sky Camo");
    public static final Item mp_fuselage_15_kerosene_insulation = ((ItemMissile) mp_fuselage_15_kerosene).copy("mp_fuselage_15_kerosene_insulation").setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(55F).setWittyText("Rest in spaghetti Columbia :(");
    public static final Item mp_fuselage_15_kerosene_metal = ((ItemMissile) mp_fuselage_15_kerosene).copy("mp_fuselage_15_kerosene_metal").setRarity(Rarity.UNCOMMON).setAuthor("Hoboy").setTitle("Bolted Metal").setHealth(60F).setWittyText("Metal frame with metal plating reinforced with bolted metal sheets and metal.");
    public static final Item mp_fuselage_15_kerosene_decorated = ((ItemMissile) mp_fuselage_15_kerosene).copy("mp_fuselage_15_kerosene_decorated").setRarity(Rarity.UNCOMMON).setAuthor("Hoboy").setTitle("Decorated").setHealth(60F);
    public static final Item mp_fuselage_15_kerosene_steampunk = ((ItemMissile) mp_fuselage_15_kerosene).copy("mp_fuselage_15_kerosene_steampunk").setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Steampunk").setHealth(60F);
    public static final Item mp_fuselage_15_kerosene_polite = ((ItemMissile) mp_fuselage_15_kerosene).copy("mp_fuselage_15_kerosene_polite").setRarity(Rarity.LEGENDARY).setAuthor("Hoboy").setTitle("Polite").setHealth(60F);
    public static final Item mp_fuselage_15_kerosene_blackjack = ((ItemMissile) mp_fuselage_15_kerosene).copy("mp_fuselage_15_kerosene_blackjack").setRarity(Rarity.LEGENDARY).setTitle("Queen Whiskey").setHealth(100F);
    public static final Item mp_fuselage_15_kerosene_lambda = ((ItemMissile) mp_fuselage_15_kerosene).copy("mp_fuselage_15_kerosene_lambda").setRarity(Rarity.RARE).setAuthor("VT-6/24").setTitle("Lambda Complex").setHealth(75F).setWittyText("MAGNIFICENT MICROWAVE CASSEROLE");
    public static final Item mp_fuselage_15_kerosene_minuteman = ((ItemMissile) mp_fuselage_15_kerosene).copy("mp_fuselage_15_kerosene_minuteman").setRarity(Rarity.UNCOMMON).setAuthor("Spexta").setTitle("MX 1702");
    public static final Item mp_fuselage_15_kerosene_pip = ((ItemMissile) mp_fuselage_15_kerosene).copy("mp_fuselage_15_kerosene_pip").setRarity(Rarity.EPIC).setAuthor("The Doctor").setTitle("LittlePip").setWittyText("31!");
    public static final Item mp_fuselage_15_kerosene_taint = ((ItemMissile) mp_fuselage_15_kerosene).copy("mp_fuselage_15_kerosene_taint").setRarity(Rarity.UNCOMMON).setAuthor("Sam").setTitle("Tainted").setWittyText("DUN-DUN!");
    public static final Item mp_fuselage_15_kerosene_yuck = ((ItemMissile) mp_fuselage_15_kerosene).copy("mp_fuselage_15_kerosene_yuck").setRarity(Rarity.EPIC).setAuthor("Hoboy").setTitle("Flesh").setWittyText("Note: Never clean DNA vials with your own spit.").setHealth(60F);
    public static final Item mp_fuselage_15_solid = new ItemMissile("mp_fuselage_15_solid").makeFuselage(FuelType.SOLID, 15000F, 1000, PartSize.SIZE_15, PartSize.SIZE_15).setHealth(60F).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_fuselage_15_solid_insulation = ((ItemMissile) mp_fuselage_15_solid).copy("mp_fuselage_15_solid_insulation").setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(65F);
    public static final Item mp_fuselage_15_solid_desh = ((ItemMissile) mp_fuselage_15_solid).copy("mp_fuselage_15_solid_desh").setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Desh Plating").setHealth(80F);
    public static final Item mp_fuselage_15_solid_soviet_glory = ((ItemMissile) mp_fuselage_15_solid).copy("mp_fuselage_15_solid_soviet_glory").setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Soviet Glory").setHealth(70F);
    public static final Item mp_fuselage_15_solid_soviet_stank = ((ItemMissile) mp_fuselage_15_solid).copy("mp_fuselage_15_solid_soviet_stank").setRarity(Rarity.EPIC).setAuthor("Hoboy").setTitle("Soviet Stank").setHealth(15F).setWittyText("Aged like a fine wine! Well, almost.");
    public static final Item mp_fuselage_15_solid_faust = ((ItemMissile) mp_fuselage_15_solid).copy("mp_fuselage_15_solid_faust").setRarity(Rarity.LEGENDARY).setAuthor("Dr.Nostalgia").setTitle("Mighty Lauren").setHealth(250F).setWittyText("Welcome to Subway, may I take your order?");
    public static final Item mp_fuselage_15_solid_silvermoonlight = ((ItemMissile) mp_fuselage_15_solid).copy("mp_fuselage_15_solid_silvermoonlight").setRarity(Rarity.UNCOMMON).setAuthor("The Master").setTitle("Silver Moonlight");
    public static final Item mp_fuselage_15_solid_snowy = ((ItemMissile) mp_fuselage_15_solid).copy("mp_fuselage_15_solid_snowy").setRarity(Rarity.UNCOMMON).setAuthor("Dr.Nostalgia").setTitle("Chilly Day");
    public static final Item mp_fuselage_15_solid_panorama = ((ItemMissile) mp_fuselage_15_solid).copy("mp_fuselage_15_solid_panorama").setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Panorama");
    public static final Item mp_fuselage_15_solid_roses = ((ItemMissile) mp_fuselage_15_solid).copy("mp_fuselage_15_solid_roses").setRarity(Rarity.UNCOMMON).setAuthor("Hoboy").setTitle("Bed of roses");
    public static final Item mp_fuselage_15_solid_mimi = ((ItemMissile) mp_fuselage_15_solid).copy("mp_fuselage_15_solid_mimi").setRarity(Rarity.RARE).setTitle("Mimi-chan");
    public static final Item mp_fuselage_15_hydrogen = new ItemMissile("mp_fuselage_15_hydrogen").makeFuselage(FuelType.HYDROGEN, 15000F, 1000, PartSize.SIZE_15, PartSize.SIZE_15).setHealth(50F).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_fuselage_15_hydrogen_cathedral = ((ItemMissile) mp_fuselage_15_hydrogen).copy("mp_fuselage_15_hydrogen_cathedral").setRarity(Rarity.UNCOMMON).setAuthor("Satan").setTitle("Unholy Cathedral");
    public static final Item mp_fuselage_15_balefire = new ItemMissile("mp_fuselage_15_balefire").makeFuselage(FuelType.BALEFIRE, 15000F, 1000, PartSize.SIZE_15, PartSize.SIZE_15).setHealth(75F).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_fuselage_15_20_kerosene = new ItemMissile("mp_fuselage_15_20_kerosene").makeFuselage(FuelType.KEROSENE, 20000, 1000, PartSize.SIZE_15, PartSize.SIZE_20).setAuthor("Hoboy").setHealth(70F).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_fuselage_15_20_kerosene_magnusson = ((ItemMissile) mp_fuselage_15_20_kerosene).copy("mp_fuselage_15_20_kerosene_magnusson").setRarity(Rarity.RARE).setAuthor("VT-6/24").setTitle("White Forest Rocket").setWittyText("And get your cranio-conjugal parasite away from my nose cone!");
    public static final Item mp_fuselage_15_20_solid = new ItemMissile("mp_fuselage_15_20_solid").makeFuselage(FuelType.SOLID, 20000, 1000, PartSize.SIZE_15, PartSize.SIZE_20).setHealth(70F).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_warhead_10_he = new ItemMissile("mp_warhead_10_he").makeWarhead(WarheadType.HE, 15F, 1.5F, PartSize.SIZE_10).setHealth(5F).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_warhead_10_incendiary = new ItemMissile("mp_warhead_10_incendiary").makeWarhead(WarheadType.INC, 15F, 1.5F, PartSize.SIZE_10).setHealth(5F).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_warhead_10_buster = new ItemMissile("mp_warhead_10_buster").makeWarhead(WarheadType.BUSTER, 15F, 1.5F, PartSize.SIZE_10).setHealth(5F).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_warhead_10_nuclear = new ItemMissile("mp_warhead_10_nuclear").makeWarhead(WarheadType.NUCLEAR, 35F, 1.5F, PartSize.SIZE_10).setTitle("Tater Tot").setHealth(10F).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_warhead_10_nuclear_large = new ItemMissile("mp_warhead_10_nuclear_large").makeWarhead(WarheadType.NUCLEAR, 75F, 2.5F, PartSize.SIZE_10).setTitle("Chernobyl Boris").setHealth(15F).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_warhead_10_taint = new ItemMissile("mp_warhead_10_taint").makeWarhead(WarheadType.TAINT, 15F, 1.5F, PartSize.SIZE_10).setHealth(20F).setRarity(Rarity.UNCOMMON).setWittyText("Eat my taint! Bureaucracy is dead and we killed it!").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_warhead_10_cloud = new ItemMissile("mp_warhead_10_cloud").makeWarhead(WarheadType.CLOUD, 15F, 1.5F, PartSize.SIZE_10).setHealth(20F).setRarity(Rarity.RARE).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_warhead_15_he = new ItemMissile("mp_warhead_15_he").makeWarhead(WarheadType.HE, 50F, 2.5F, PartSize.SIZE_15).setHealth(10F).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_warhead_15_incendiary = new ItemMissile("mp_warhead_15_incendiary").makeWarhead(WarheadType.INC, 35F, 2.5F, PartSize.SIZE_15).setHealth(10F).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_warhead_15_nuclear = new ItemMissile("mp_warhead_15_nuclear").makeWarhead(WarheadType.NUCLEAR, 125F, 5F, PartSize.SIZE_15).setTitle("Auntie Bertha").setHealth(15F).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_warhead_15_nuclear_shark = ((ItemMissile) mp_warhead_15_nuclear).copy("mp_warhead_15_nuclear_shark").setRarity(Rarity.UNCOMMON).setTitle("Discount Bullet Bill").setWittyText("Nose art on a cannon bullet? Who does that?");
    public static final Item mp_warhead_15_thermo = new ItemMissile("mp_warhead_15_thermo").makeWarhead(WarheadType.TX, 250F, 6.5F, PartSize.SIZE_15).setHealth(25F).setRarity(Rarity.RARE).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_warhead_15_mirv = new ItemMissile("mp_warhead_15_mirv").makeWarhead(WarheadType.MIRV, (float) BombConfig.mirvRadius, 7.0F, PartSize.SIZE_15).setRarity(Rarity.LEGENDARY).setAuthor("Seven").setHealth(20F).setWittyText("I wanna know, have you ever seen the rain?").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_warhead_15_boxcar = new ItemMissile("mp_warhead_15_boxcar").makeWarhead(WarheadType.TX, 500F, 7.5F, PartSize.SIZE_15).setWittyText("?!?!").setHealth(35F).setRarity(Rarity.LEGENDARY).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_warhead_15_n2 = new ItemMissile("mp_warhead_15_n2").makeWarhead(WarheadType.N2, 100F, 5F, PartSize.SIZE_15).setWittyText("[screams geometrically]").setHealth(20F).setRarity(Rarity.RARE).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_warhead_15_balefire = new ItemMissile("mp_warhead_15_balefire").makeWarhead(WarheadType.BALEFIRE, 100F, 7.5F, PartSize.SIZE_15).setRarity(Rarity.LEGENDARY).setAuthor("VT-6/24").setHealth(15F).setWittyText("Hightower, never forgetti.").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_warhead_15_volcano = new ItemMissile("mp_warhead_15_volcano").makeWarhead(WarheadType.VOLCANO, 10F, 6.5F, PartSize.SIZE_15).setHealth(25F).setRarity(Rarity.LEGENDARY).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_warhead_15_turbine = new ItemMissile("mp_warhead_15_turbine").makeWarhead(WarheadType.TURBINE, 200F, 5F, PartSize.SIZE_15).setHealth(250F).setRarity(Rarity.SEWS_CLOTHES_AND_SUCKS_HORSE_COCK).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_chip_1 = new ItemMissile("mp_c_1").makeChip(0.1F).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_chip_2 = new ItemMissile("mp_c_2").makeChip(0.05F).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_chip_3 = new ItemMissile("mp_c_3").makeChip(0.01F).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_chip_4 = new ItemMissile("mp_c_4").makeChip(0.005F).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item mp_chip_5 = new ItemMissile("mp_c_5").makeChip(0.0F).setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_skin_camo = new ItemCustomLore("missile_skin_camo").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_skin_desert = new ItemCustomLore("missile_skin_desert").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_skin_flames = new ItemCustomLore("missile_skin_flames").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_skin_manly_pink = new ItemCustomLore("missile_skin_manly_pink").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_skin_orange_insulation = new ItemCustomLore("missile_skin_orange_insulation").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_skin_sleek = new ItemCustomLore("missile_skin_sleek").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_skin_soviet_glory = new ItemCustomLore("missile_skin_soviet_glory").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_skin_soviet_stank = new ItemCustomLore("missile_skin_soviet_stank").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_skin_metal = new ItemCustomLore("missile_skin_metal").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab);
    public static final Item missile_custom = new ItemCustomMissile("missile_custom").setMaxStackSize(1).setCreativeTab(null);
    public static final Item sliding_blast_door_skin0 = new ItemDoorSkin("sliding_blast_door_skin0", "sliding_blast_door").setCreativeTab(MainRegistry.controlTab);
    public static final Item sliding_blast_door_skin1 = new ItemDoorSkin("sliding_blast_door_skin1", "sliding_blast_door_variant1").setCreativeTab(MainRegistry.controlTab);
    public static final Item sliding_blast_door_skin2 = new ItemDoorSkin("sliding_blast_door_skin2", "sliding_blast_door_variant2").setCreativeTab(MainRegistry.controlTab);
    //Door items
    public static final Item door_metal = new ItemModDoor("door_metal").setCreativeTab(MainRegistry.blockTab);
    public static final Item door_office = new ItemModDoor("door_office").setCreativeTab(MainRegistry.blockTab);
    public static final Item door_bunker = new ItemModDoor("door_bunker").setCreativeTab(MainRegistry.blockTab);
    public static final Item door_red = new ItemModDoor("door_red").setCreativeTab(MainRegistry.blockTab);
    //Music
    public static final Item record_lc = new ItemModRecord("lc", HBMSoundHandler.lambdaCore, "record_lc").setCreativeTab(CreativeTabs.MISC);
    public static final Item record_ss = new ItemModRecord("ss", HBMSoundHandler.sectorSweep, "record_ss").setCreativeTab(CreativeTabs.MISC);
    public static final Item record_vc = new ItemModRecord("vc", HBMSoundHandler.vortalCombat, "record_vc").setCreativeTab(CreativeTabs.MISC);
    public static final Item record_glass = new ItemModRecord("glass", HBMSoundHandler.glass, "record_glass").setCreativeTab(CreativeTabs.MISC);
    //Weird items
    public static final Item flame_pony = new ItemCustomLore("flame_pony").setCreativeTab(MainRegistry.partsTab);
    public static final Item flame_conspiracy = new ItemCustomLore("flame_conspiracy").setCreativeTab(MainRegistry.partsTab);
    public static final Item flame_politics = new ItemCustomLore("flame_politics").setCreativeTab(MainRegistry.partsTab);
    public static final Item flame_opinion = new ItemCustomLore("flame_opinion").setCreativeTab(MainRegistry.partsTab);
    public static final Item polaroid = new ItemPolaroid("polaroid").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item glitch = new ItemGlitch("glitch").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item burnt_bark = new ItemCustomLore("burnt_bark").setCreativeTab(MainRegistry.consumableTab);
    public static final Item letter = new ItemStarterKit("letter").setCreativeTab(MainRegistry.consumableTab);
    public static final Item book_secret = new ItemCustomLore("book_secret").setCreativeTab(MainRegistry.polaroidID == 11 ? MainRegistry.consumableTab : null);
    public static final Item clay_tablet = new ItemClayTablet("clay_tablet").setCreativeTab(null);
    //Drillgon200: Shaking my head... just had to put "FOE" right on it.
    public static final Item book_of_ = new ItemBook("book_of_").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item book_lemegeton = new ItemBookLemegeton("book_lemegeton").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item page_of_ = new ItemEnumMulti("page_of_", ItemEnums.EnumPages.class, true, "page_of_").setMaxStackSize(1).setCreativeTab(null);
    public static final Item crystal_horn = new ItemCustomLore("crystal_horn").setCreativeTab(MainRegistry.partsTab);
    public static final Item crystal_charred = new ItemCustomLore("crystal_charred").setCreativeTab(MainRegistry.partsTab);
    public static final Item watch = new ItemCustomLore("watch").setCreativeTab(MainRegistry.consumableTab).setMaxStackSize(1);
    public static final Item apple_euphemium = new ItemAppleEuphemium(20, 100, false, "apple_euphemium").setAlwaysEdible().setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item wand = new ItemWand("wand_k").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item wand_s = new ItemWandS("wand_s").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item wand_d = new ItemWandD("wand_d").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item rod_of_discord = new ItemDiscord("rod_of_discord").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab);
    public static final Item analyzer = new ItemAnalyzer("analyzer").setMaxStackSize(1).setCreativeTab(null);
    public static final Item defuser = new ItemDefuser(ToolType.DEFUSER, 100, "defuser").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab);
    @Deprecated //mlbv: no longer exists in 1.7, consider removing it
    public static final Item defuser_desh = new ItemDefuser(ToolType.DEFUSER, -1, "defuser_desh").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab);
    public static final Item meltdown_tool = new ItemDyatlov("meltdown_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab);
    //Chopper
    public static final Item chopper_head = new ItemBase("chopper_head").setCreativeTab(MainRegistry.partsTab);
    public static final Item chopper_gun = new ItemBase("chopper_gun").setCreativeTab(MainRegistry.partsTab);
    public static final Item chopper_torso = new ItemBase("chopper_torso").setCreativeTab(MainRegistry.partsTab);
    public static final Item chopper_tail = new ItemBase("chopper_tail").setCreativeTab(MainRegistry.partsTab);
    public static final Item chopper_wing = new ItemBase("chopper_wing").setCreativeTab(MainRegistry.partsTab);
    public static final Item chopper_blades = new ItemBase("chopper_blades").setCreativeTab(MainRegistry.partsTab);
    public static final Item combine_scrap = new ItemBase("combine_scrap").setCreativeTab(MainRegistry.partsTab);
    public static final Item shimmer_head = new ItemBase("shimmer_head").setCreativeTab(MainRegistry.partsTab);
    public static final Item shimmer_axe_head = new ItemBase("shimmer_axe_head").setCreativeTab(MainRegistry.partsTab);
    public static final Item shimmer_handle = new ItemBase("shimmer_handle").setCreativeTab(MainRegistry.partsTab);
    //Stuff
    public static final Item telepad = new ItemBase("telepad").setCreativeTab(MainRegistry.partsTab);
    public static final Item entanglement_kit = new ItemCustomLore("entanglement_kit").setCreativeTab(MainRegistry.partsTab);
    //Dummy texture items; Note: Use EffectItem to autohide them from JEI
    public static final Item achievement_icon = new ItemEnumMulti("achievement_icon", ItemEnums.EnumAchievementType.class, true, true).setCreativeTab(null);
    public static final Item cloud1 = new EffectItem("cloud1").setCreativeTab(null);
    public static final Item cloud2 = new EffectItem("cloud2").setCreativeTab(null);
    public static final Item cloud3 = new EffectItem("cloud3").setCreativeTab(null);
    public static final Item cloud4 = new EffectItem("cloud4").setCreativeTab(null);
    public static final Item cloud5 = new EffectItem("cloud5").setCreativeTab(null);
    public static final Item cloud6 = new EffectItem("cloud6").setCreativeTab(null);
    public static final Item cloud7 = new EffectItem("cloud7").setCreativeTab(null);
    public static final Item cloud8 = new EffectItem("cloud8").setCreativeTab(null);
    public static final Item flame_1 = new EffectItem("flame_1").setCreativeTab(null);
    public static final Item flame_2 = new EffectItem("flame_2").setCreativeTab(null);
    public static final Item flame_3 = new EffectItem("flame_3").setCreativeTab(null);
    public static final Item flame_4 = new EffectItem("flame_4").setCreativeTab(null);
    public static final Item flame_5 = new EffectItem("flame_5").setCreativeTab(null);
    public static final Item flame_6 = new EffectItem("flame_6").setCreativeTab(null);
    public static final Item flame_7 = new EffectItem("flame_7").setCreativeTab(null);
    public static final Item flame_8 = new EffectItem("flame_8").setCreativeTab(null);
    public static final Item flame_9 = new EffectItem("flame_9").setCreativeTab(null);
    public static final Item flame_10 = new EffectItem("flame_10").setCreativeTab(null);
    public static final Item orange1 = new EffectItem("orange1").setCreativeTab(null);
    public static final Item orange2 = new EffectItem("orange2").setCreativeTab(null);
    public static final Item orange3 = new EffectItem("orange3").setCreativeTab(null);
    public static final Item orange4 = new EffectItem("orange4").setCreativeTab(null);
    public static final Item orange5 = new EffectItem("orange5").setCreativeTab(null);
    public static final Item orange6 = new EffectItem("orange6").setCreativeTab(null);
    public static final Item orange7 = new EffectItem("orange7").setCreativeTab(null);
    public static final Item orange8 = new EffectItem("orange8").setCreativeTab(null);
    public static final Item pc1 = new EffectItem("pc1").setCreativeTab(null);
    public static final Item pc2 = new EffectItem("pc2").setCreativeTab(null);
    public static final Item pc3 = new EffectItem("pc3").setCreativeTab(null);
    public static final Item pc4 = new EffectItem("pc4").setCreativeTab(null);
    public static final Item pc5 = new EffectItem("pc5").setCreativeTab(null);
    public static final Item pc6 = new EffectItem("pc6").setCreativeTab(null);
    public static final Item pc7 = new EffectItem("pc7").setCreativeTab(null);
    public static final Item pc8 = new EffectItem("pc8").setCreativeTab(null);
    public static final Item chlorine1 = new EffectItem("chlorine1").setCreativeTab(null);
    public static final Item chlorine2 = new EffectItem("chlorine2").setCreativeTab(null);
    public static final Item chlorine3 = new EffectItem("chlorine3").setCreativeTab(null);
    public static final Item chlorine4 = new EffectItem("chlorine4").setCreativeTab(null);
    public static final Item chlorine5 = new EffectItem("chlorine5").setCreativeTab(null);
    public static final Item chlorine6 = new EffectItem("chlorine6").setCreativeTab(null);
    public static final Item chlorine7 = new EffectItem("chlorine7").setCreativeTab(null);
    public static final Item chlorine8 = new EffectItem("chlorine8").setCreativeTab(null);
    public static final Item ln2_1 = new EffectItem("ln2_1").setCreativeTab(null);
    public static final Item ln2_2 = new EffectItem("ln2_2").setCreativeTab(null);
    public static final Item ln2_3 = new EffectItem("ln2_3").setCreativeTab(null);
    public static final Item ln2_4 = new EffectItem("ln2_4").setCreativeTab(null);
    public static final Item ln2_5 = new EffectItem("ln2_5").setCreativeTab(null);
    public static final Item ln2_6 = new EffectItem("ln2_6").setCreativeTab(null);
    public static final Item ln2_7 = new EffectItem("ln2_7").setCreativeTab(null);
    public static final Item ln2_8 = new EffectItem("ln2_8").setCreativeTab(null);
    public static final Item ln2_9 = new EffectItem("ln2_9").setCreativeTab(null);
    public static final Item ln2_10 = new EffectItem("ln2_10").setCreativeTab(null);
    public static final Item nothing = new EffectItem("nothing").setCreativeTab(null);
    public static final Item ducc = new ItemBase("ducc").setCreativeTab(MainRegistry.controlTab);
    public static final Item discharge = new EffectItem("discharge").setCreativeTab(null);
    public static final Item undefined = new ItemCustomLore("undefined").setCreativeTab(MainRegistry.partsTab);
    public static final Item mysteryshovel = new ItemMS("mysteryshovel").setFull3D().setMaxStackSize(1).setCreativeTab(CreativeTabs.TOOLS);
    public static final Item memory = new ItemBattery(Long.MAX_VALUE / 100L, 100000000000000L, 100000000000000L, "memory").setMaxStackSize(1).setCreativeTab(null);
    public static final Item conveyor_wand = new ItemConveyorWand("conveyor_wand").setFull3D().setCreativeTab(MainRegistry.machineTab);

    public static final Item part_generic = new ItemEnumMulti("part_generic", EnumPartType.class, true, true).setCreativeTab(MainRegistry.partsTab);
    public static final Item item_secret = new ItemEnumMulti("item_secret", EnumSecretType.class, true, true).setCreativeTab(null);
    public static final Item item_expensive = new ItemEnumMulti("item_expensive", EnumExpensiveType.class, true, true).setCreativeTab(MainRegistry.partsTab);
    public static final Item chemical_dye = new ItemChemicalDye("chemical_dye").setCreativeTab(MainRegistry.partsTab);
    public static final Item crayon = new ItemCrayon("crayon").setCreativeTab(MainRegistry.partsTab);
    public static ToolMaterial enumToolMaterialElecTerra = EnumHelper.addToolMaterial(Tags.MODID + ":ELECTERRA", 4, 0, 20.0F, 12.0F, 2);
    public static final Item drax = new ItemToolAbilityPower(15F, -2.8F, -0.05, enumToolMaterialElecTerra, EnumToolType.MINER, 500000000, 100000, 5000, "drax")
            .addAbility(IToolHarvestAbility.SMELTER, 0)
            .addAbility(IToolHarvestAbility.SHREDDER, 0)
            .addAbility(IToolHarvestAbility.LUCK, 1)
            .addAbility(IToolAreaAbility.HAMMER, 1)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
            .addAbility(IToolAreaAbility.RECURSION, 2);
    public static final Item drax_mk2 = new ItemToolAbilityPower(20F, -2.8F, -0.05, enumToolMaterialElecTerra, EnumToolType.MINER, 1000000000, 250000, 7500, "drax_mk2")
            .addAbility(IToolHarvestAbility.SMELTER, 0)
            .addAbility(IToolHarvestAbility.SHREDDER, 0)
            .addAbility(IToolHarvestAbility.CENTRIFUGE, 0)
            .addAbility(IToolHarvestAbility.LUCK, 2)
            .addAbility(IToolAreaAbility.HAMMER, 2)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 2)
            .addAbility(IToolAreaAbility.RECURSION, 4);
    public static final Item drax_mk3 = new ItemToolAbilityPower(20F, -2.8F, -0.05, enumToolMaterialElecTerra, EnumToolType.MINER, 2500000000L, 500000, 10000, "drax_mk3")
            .addAbility(IToolHarvestAbility.SMELTER, 0)
            .addAbility(IToolHarvestAbility.SHREDDER, 0)
            .addAbility(IToolHarvestAbility.CENTRIFUGE, 0)
            .addAbility(IToolHarvestAbility.CRYSTALLIZER, 0)
            .addAbility(IToolHarvestAbility.SILK, 0)
            .addAbility(IToolHarvestAbility.LUCK, 3)
            .addAbility(IToolAreaAbility.HAMMER, 3)
            .addAbility(IToolAreaAbility.HAMMER_FLAT, 3)
            .addAbility(IToolAreaAbility.RECURSION, 5);

    public static void preInit() {
        GunFactory.init();
        excludeNEI.add(item_secret);

        for (Block block : ModBlocks.ALL_BLOCKS) {
            if (block instanceof ICustomBlockItem) {
                ((ICustomBlockItem) block).registerItem();
            } else if (block instanceof BlockModDoor) {
            } else if (block instanceof FluidDuctBox || block instanceof FluidDuctStandard) {
                ForgeRegistries.ITEMS.register(new ItemBlock(block).setRegistryName(block.getRegistryName()).setHasSubtypes(true));
            } else if (block instanceof PowerCableBox) {
                ForgeRegistries.ITEMS.register(new ItemBlock(block).setRegistryName(block.getRegistryName()).setHasSubtypes(true));
            } else {
                ForgeRegistries.ITEMS.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));

            }
        }

        for (Item item : ALL_ITEMS) {
            ForgeRegistries.ITEMS.register(item);
        }
    }

    public static void init() {
    }

    public static void postInit() {
    }
}
