package com.hbm.datagen;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.fluid.ModFluids;
import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ItemEnums.EnumAchievementType;
import com.hbm.items.ModItems;
import com.hbm.items.food.ItemConserve;
import com.hbm.datagen.dsl.AdvancementDSL;
import com.hbm.datagen.dsl.AdvancementDSL.Display;
import com.hbm.datagen.dsl.AdvancementDSL.Templates;
import net.minecraft.advancements.FrameType;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class AdvGen {
    // After modifying any content here, set doGen to true and runClient once,
    // the jsons will be generated automatically
    private static final boolean doGen = false;
    public static void generate() {
        if (!doGen) return;
        AdvancementDSL.Batch batch = AdvancementDSL.batch("hbm");
        batch.add(Templates.impossible("achimpossible",
                "hbm:root",
                new Display()
                        .titleKey("achievement.impossible")
                        .descKey("achievement.impossible.desc")
                        .icon(ModItems.nothing)
                        .frame(FrameType.CHALLENGE)
                        .toast(true)
                        .announce(true)
                        .hidden(true),
                "impossible")
        );
        batch.add(Templates.impossible("achtob",
                "hbm:root",
                new Display()
                        .titleKey("achievement.tasteofblood")
                        .descKey("achievement.tasteofblood.desc")
                        .icon(new ItemStack(ModItems.fluid_icon, 1, Fluids.ASCHRAB.getID()))
                        .frame(FrameType.CHALLENGE)
                        .toast(true)
                        .announce(true),
                "impossible")
        );
        batch.add(Templates.impossible("achgofish",
                "hbm:root",
                new Display()
                        .titleKey("achievement.gofish")
                        .descKey("achievement.gofish.desc")
                        .icon(DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.GOFISH))
                        .frame(FrameType.CHALLENGE)
                        .toast(true)
                        .announce(true),
                "impossible")
        );
        batch.add(Templates.obtainAnyItem("achpotato",
                "hbm:root",
                new Display()
                        .titleKey("achievement.potato")
                        .descKey("achievement.potato.desc")
                        .icon(ModItems.battery_potatos)
                        .frame(FrameType.CHALLENGE)
                        .toast(true)
                        .announce(true),
                "crafting", ModItems.battery_potatos)
        );
        batch.add(Templates.obtainAnyItemStack("achc20_5",
                "hbm:root",
                new Display()
                        .titleKey("achievement.c20_5")
                        .descKey("achievement.c20_5.desc")
                        .icon(DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.QUESTIONMARK))
                        .frame(FrameType.CHALLENGE)
                        .toast(true)
                        .announce(true),
                "crafting", new ItemStack(ModItems.canned_conserve, 1, ItemConserve.EnumFoodType.JIZZ.ordinal()))
        );
        batch.add(Templates.impossible("achstratum",
                "hbm:root",
                new Display()
                        .titleKey("achievement.stratum")
                        .descKey("achievement.stratum.desc")
                        .icon(new ItemStack(ModBlocks.stone_gneiss))
                        .frame(FrameType.CHALLENGE)
                        .toast(true)
                        .announce(true),
                "impossible")
        );
        batch.add(Templates.impossible("achno9",
                "hbm:root",
                new Display()
                        .titleKey("achievement.no9")
                        .descKey("achievement.no9.desc")
                        .icon(ModItems.no9)
                        .toast(true)
                        .announce(true),
                "impossible")
        );
        batch.add(Templates.obtainAnyItem("achslimeball",
                "hbm:root",
                new Display()
                        .titleKey("achievement.slimeball")
                        .descKey("achievement.slimeball.desc")
                        .icon(DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.ACID))
                        .toast(true)
                        .announce(true),
                "crafting", Items.SLIME_BALL)
        );
        batch.add(Templates.impossible("achsulfuric",
                "hbm:achslimeball",
                new Display()
                        .titleKey("achievement.sulfuric")
                        .descKey("achievement.sulfuric.desc")
                        .icon(DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.BALLS))
                        .frame(FrameType.CHALLENGE)
                        .toast(true)
                        .announce(true),
                "impossible")
        );
        batch.add(Templates.impossible("achinferno",
                "hbm:root",
                new Display()
                        .titleKey("achievement.inferno")
                        .descKey("achievement.inferno.desc")
                        .icon(ModItems.canister_napalm)
                        .frame(FrameType.CHALLENGE)
                        .toast(true)
                        .announce(true),
                "impossible")
        );
        batch.add(Templates.impossible("achredroom",
                "hbm:root",
                new Display()
                        .titleKey("achievement.redroom")
                        .descKey("achievement.redroom.desc")
                        .icon(ModItems.key_red)
                        .frame(FrameType.CHALLENGE)
                        .toast(true)
                        .announce(true),
                "impossible")
        );
        batch.add(Templates.impossible("bobhidden",
                "hbm:root",
                new Display()
                        .titleKey("achievement.hidden")
                        .descKey("achievement.hidden.desc")
                        .icon(DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.QUESTIONMARK))
                        .toast(true)
                        .announce(true),
                "impossible")
        );
        batch.add(Templates.impossible("digammasee",
                "hbm:root",
                new Display()
                        .titleKey("achievement.digammaSee")
                        .descKey("achievement.digammaSee.desc")
                        .icon(DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.DIGAMMASEE))
                        .toast(true)
                        .announce(true),
                "impossible")
        );
        batch.add(Templates.impossible("digammafeel",
                "hbm:digammasee",
                new Display()
                        .titleKey("achievement.digammaFeel")
                        .descKey("achievement.digammaFeel.desc")
                        .icon(DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.DIGAMMAFEEL))
                        .toast(true)
                        .announce(true),
                "impossible")
        );
        batch.add(Templates.impossible("digammaknow",
                "hbm:digammafeel",
                new Display()
                        .titleKey("achievement.digammaKnow")
                        .descKey("achievement.digammaKnow.desc")
                        .icon(DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.DIGAMMAKNOW))
                        .toast(true)
                        .announce(true)
                        .frame(FrameType.CHALLENGE),
                "impossible")
        );
        batch.add(Templates.impossible("digammakauaimoho",
                "hbm:digammaknow",
                new Display()
                        .titleKey("achievement.digammaKauaiMoho")
                        .descKey("achievement.digammaKauaiMoho.desc")
                        .icon(DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.DIGAMMAKAUAIMOHO))
                        .toast(true)
                        .announce(true)
                        .frame(FrameType.CHALLENGE),
                "impossible")
        );
        batch.add(Templates.impossible("digammaupontop",
                "hbm:digammakauaimoho",
                new Display()
                        .titleKey("achievement.digammaUpOnTop")
                        .descKey("achievement.digammaUpOnTop.desc")
                        .icon(DictFrame.fromOne(ModItems.achievement_icon, EnumAchievementType.DIGAMMAUPONTOP))
                        .toast(true)
                        .announce(true)
                        .frame(FrameType.CHALLENGE),
                "impossible")
        );
        batch.add(Templates.obtainAnyItemStack("achburnerpress",
                "hbm:root",
                new Display()
                        .key("burnerPress")
                        .icon(new ItemStack(ModBlocks.machine_press))
                        .toast(true)
                        .announce(true),
                "crafting", new ItemStack(ModBlocks.machine_press))
        );
        batch.add(Templates.obtainAnyItemStack("achblastfurnace",
                "hbm:achburnerpress",
                new Display()
                        .key("blastFurnace")
                        .icon(new ItemStack(ModBlocks.machine_press))
                        .toast(true)
                        .announce(true),
                "crafting", new ItemStack(ModBlocks.machine_difurnace_off))
        );
        batch.add(Templates.obtainAnyItemStack("achassembly",
                "hbm:achburnerpress",
                new Display()
                        .key("assembly")
                        .icon(new ItemStack(ModBlocks.machine_assembly_machine))
                        .toast(true)
                        .announce(true),
                "crafting", new ItemStack(ModBlocks.machine_assembly_machine), new ItemStack(ModBlocks.machine_assembler))
        );
        batch.add(Templates.obtainAnyItem("achselenium",
                "hbm:achburnerpress",
                new Display()
                        .key("selenium")
                        .icon(ModItems.ingot_starmetal)
                        .toast(true)
                        .announce(true)
                        .frame(FrameType.CHALLENGE),
                "crafting", ModItems.piston_selenium, ModItems.gun_b92)
        );
        batch.add(Templates.obtainAnyItemStack("achchemplant",
                "hbm:achassembly",
                new Display()
                        .key("chemplant")
                        .icon(new ItemStack(ModBlocks.machine_chemical_plant))
                        .toast(true)
                        .announce(true),
                "crafting", new ItemStack(ModBlocks.machine_chemical_plant), new ItemStack(ModBlocks.machine_chemplant))
        );
        batch.add(Templates.obtainAnyItemStack("achconcrete",
                "hbm:achchemplant",
                new Display()
                        .key("concrete")
                        .icon(new ItemStack(ModBlocks.concrete))
                        .toast(true)
                        .announce(true),
                "crafting", new ItemStack(ModBlocks.concrete_smooth), new ItemStack(ModBlocks.concrete_asbestos))
        );
        batch.add(Templates.obtainAnyItem("achpolymer",
                "hbm:achchemplant",
                new Display()
                        .key("polymer")
                        .icon(ModItems.ingot_polymer)
                        .toast(true)
                        .announce(true),
                "crafting", ModItems.ingot_polymer)
        );
        batch.add(Templates.obtainAnyItem("achdesh",
                "hbm:achchemplant",
                new Display()
                        .key("desh")
                        .icon(ModItems.ingot_desh)
                        .toast(true)
                        .announce(true),
                "crafting", ModItems.ingot_desh)
        );
        batch.add(Templates.obtainAnyItem("achtantalum",
                "hbm:achchemplant",
                new Display()
                        .key("tantalum")
                        .icon(ModItems.gem_tantalium)
                        .toast(true)
                        .announce(true)
                        .frame(FrameType.CHALLENGE),
                "crafting", ModItems.gem_tantalium)
        );
        batch.add(Templates.obtainAnyItemStack("achgascent",
                "hbm:achdesh",
                new Display()
                        .key("gasCent")
                        .icon(ModItems.ingot_uranium_fuel)
                        .toast(true)
                        .announce(true),
                "crafting", new ItemStack(ModBlocks.machine_gascent))
        );
        batch.add(Templates.obtainAnyItemStack("achcentrifuge",
                "hbm:achpolymer",
                new Display()
                        .key("centrifuge")
                        .icon(new ItemStack(ModBlocks.machine_centrifuge))
                        .toast(true)
                        .announce(true),
                "crafting", new ItemStack(ModBlocks.machine_centrifuge))
        );
        batch.add(Templates.impossible("achfoeq",
                "hbm:achdesh",
                new Display()
                        .key("FOEQ")
                        .icon(ModItems.sat_foeq)
                        .toast(true)
                        .announce(true)
                        .frame(FrameType.CHALLENGE),
                "impossible")
        );
        batch.add(Templates.impossible("achsoyuz",
                "hbm:achdesh",
                new Display()
                        .key("soyuz")
                        .icon(Items.BAKED_POTATO)
                        .toast(true)
                        .announce(true)
                        .frame(FrameType.CHALLENGE),
                "impossible")
        );
        batch.add(Templates.impossible("achspace",
                "hbm:achdesh",
                new Display()
                        .key("space")
                        .icon(ModItems.missile_soyuz)
                        .toast(true)
                        .announce(true)
                        .frame(FrameType.CHALLENGE),
                "impossible")
        );
        batch.add(Templates.obtainAnyItem("achschrab",
                "hbm:achdesh",
                new Display()
                        .key("schrab")
                        .icon(ModItems.ingot_schrabidium)
                        .toast(true)
                        .announce(true),
                "crafting", ModItems.ingot_schrabidium, ModItems.nugget_schrabidium)
        );
        batch.add(Templates.obtainAnyItemStack("achacidizer",
                "hbm:achdesh",
                new Display()
                        .key("acidizer")
                        .icon(new ItemStack(ModBlocks.machine_crystallizer))
                        .toast(true)
                        .announce(true),
                "crafting", new ItemStack(ModBlocks.machine_crystallizer))
        );
        batch.add(AdvancementDSL.builder("achradium")
                                .parent("hbm:achcentrifuge")
                                .display(AdvancementDSL
                                        .display()
                                        .icon(ModItems.coffee_radium)
                                        .key("radium")
                                        .frame(FrameType.CHALLENGE)
                                ).consumeAnyItem("eaten", ModItems.coffee_radium)
                                .build()
        );
        batch.add(Templates.obtainAnyItem("achtechnetium",
                "hbm:achcentrifuge",
                new Display()
                        .key("technetium")
                        .icon(ModItems.ingot_tcalloy)
                        .toast(true)
                        .announce(true),
                "crafting", ModItems.nugget_technetium)
        );
        batch.add(Templates.impossible("achzirnoxboom",
                "hbm:achcentrifuge",
                new Display()
                        .key("ZIRNOXBoom")
                        .icon(ModItems.debris_element)
                        .toast(true)
                        .announce(true)
                        .frame(FrameType.CHALLENGE),
                "impossible")
        );
        batch.add(Templates.obtainAnyItem("achchicagopile",
                "hbm:achcentrifuge",
                new Display()
                        .key("chicagoPile")
                        .icon(ModItems.pile_rod_plutonium)
                        .toast(true)
                        .announce(true),
                "crafting", ModItems.billet_pu_mix)
        );
        batch.add(Templates.obtainAnyItemStack("achsilex",
                "hbm:achacidizer",
                new Display()
                        .key("SILEX")
                        .icon(new ItemStack(ModBlocks.machine_silex))
                        .toast(true)
                        .announce(true),
                "crafting", new ItemStack(ModBlocks.machine_silex))
        );
        batch.add(Templates.obtainAnyItemStack("achwatz",
                "hbm:achschrab",
                new Display()
                        .key("watz")
                        .icon(ModItems.watz_pellet)
                        .toast(true)
                        .announce(true),
                "crafting", new ItemStack(ModBlocks.struct_watz_core))
        );
        batch.add(Templates.impossible("achwatzboom",
                "hbm:achwatz",
                new Display()
                        .key("watzBoom")
                        .icon(FluidUtil.getFilledBucket(new FluidStack(ModFluids.mud_fluid, 1000)))
                        .toast(true)
                        .announce(true)
                        .frame(FrameType.CHALLENGE),
                "impossible")
        );
        batch.add(Templates.obtainAnyItem("achrbmk",
                "hbm:achconcrete",
                new Display()
                        .key("RBMK")
                        .icon(ModItems.rbmk_fuel_ueu)
                        .toast(true)
                        .announce(true),
                "crafting", ModItems.rbmk_fuel_empty)
        );
        batch.add(Templates.impossible("achrbmkboom",
                "hbm:achrbmk",
                new Display()
                        .key("RBMKBoom")
                        .icon(ModItems.debris_fuel)
                        .toast(true)
                        .announce(true)
                        .frame(FrameType.CHALLENGE),
                "impossible")
        );
        batch.add(Templates.obtainAnyItem("achbismuth",
                "hbm:achrbmk",
                new Display()
                        .key("bismuth")
                        .icon(ModItems.ingot_bismuth)
                        .toast(true)
                        .announce(true),
                "crafting", ModItems.nugget_bismuth)
        );
        batch.add(Templates.obtainAnyItem("achbreeding",
                "hbm:achrbmk",
                new Display()
                        .key("breeding")
                        .icon(ModItems.ingot_am_mix)
                        .toast(true)
                        .announce(true)
                        .frame(FrameType.CHALLENGE),
                "crafting", ModItems.nugget_am241, ModItems.nugget_am242)
        );
        batch.add(Templates.obtainAnyItemStack("achfusion",
                "hbm:achbismuth",
                new Display()
                        .key("fusion")
                        .icon(new ItemStack(ModBlocks.iter))
                        .toast(true)
                        .announce(true)
                        .frame(FrameType.CHALLENGE),
                "crafting", new ItemStack(ModBlocks.struct_torus_core))
        );
        batch.add(Templates.impossible("achmeltdown",
                "hbm:achfusion",
                new Display()
                        .key("meltdown")
                        .icon(ModItems.powder_balefire)
                        .toast(true)
                        .announce(true)
                        .frame(FrameType.CHALLENGE),
                "impossible")
        );
        batch.add(Templates.obtainAnyItem("achredballoons",
                "hbm:achpolymer",
                new Display()
                        .key("redBalloons")
                        .icon(ModItems.missile_nuclear)
                        .toast(true)
                        .announce(true)
                        .frame(FrameType.CHALLENGE),
                "crafting", ModItems.missile_nuclear, ModItems.missile_nuclear_cluster, ModItems.missile_doomsday,
                ModItems.mp_warhead_10_nuclear, ModItems.mp_warhead_10_nuclear_large, ModItems.mp_warhead_15_nuclear,
                ModItems.mp_warhead_15_nuclear_shark, ModItems.mp_warhead_15_boxcar)
        );
        batch.add(Templates.impossible("achmanhattan",
                "hbm:achpolymer",
                new Display()
                        .key("manhattan")
                        .icon(new ItemStack(ModBlocks.nuke_boy))
                        .toast(true)
                        .announce(true)
                        .frame(FrameType.CHALLENGE),
                "impossible")
        );
        batch.add(Templates.impossible("progress_dfc",
                "hbm:achfusion",
                new Display()
                        .titleKey("hbm.achievement.progress_dfc")
                        .descKey("hbm.achievement.progress_dfc.desc")
                        .icon(new ItemStack(ModBlocks.dfc_core))
                        .toast(true)
                        .announce(true)
                        .frame(FrameType.CHALLENGE),
                "impossible")
        );


        // ↓ Reference for builder ↓
//        batch.add(AdvancementDSL.builder("bobchemistry")
//                                .parent("hbm:bobassembly")
//                                .display(AdvancementDSL.display()
//                                                       .iconRL("hbm:bob_chemistry")
//                                                       .titleKey("hbm.achievement.bobchemistry")
//                                                      .descKey("hbm.achievement.bobchemistry.desc")
//                                ).rewards(AdvancementDSL.Rewards.create().experience(100))
//                                .criterion("crafting_table",
//                                        AdvancementDSL.Triggers.INVENTORY_CHANGED, cond -> {
//                                    JsonArray items = new JsonArray();
//                                    JsonObject pred = new JsonObject();
//                                    pred.addProperty("item", "hbm:ingot_rubber");
//                                    items.add(pred);
//                                    cond.add("items", items);
//                                }).build()
//        );
        batch.writeAllSmart();
    }
}
