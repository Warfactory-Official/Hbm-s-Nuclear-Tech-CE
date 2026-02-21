package com.hbm.crafting;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.fluid.ModFluids;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ItemAmmoEnums.Ammo240Shell;
import com.hbm.items.ItemEnums;
import com.hbm.items.ItemEnums.EnumCircuitType;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemCell;
import com.hbm.items.weapon.GunB92Cell;
import com.hbm.items.weapon.sedna.factory.GunFactory;
import com.hbm.main.CraftingManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import static com.hbm.inventory.OreDictManager.*;

/**
 * For guns, ammo and the like
 * @author hbm
 */
public class WeaponRecipes {

    public static void register() {

        //Weapon mod table
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.machine_weapon_table, 1),"PPP", "TCT", "TST", 'P', GUNMETAL.plate(), 'T', STEEL.ingot(), 'C', Blocks.CRAFTING_TABLE, 'S', STEEL.block() );

        //SEDNA Parts
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.part_stock, 1, Mats.MAT_WOOD.id), "WWW", "  W", 'W', KEY_PLANKS);
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.part_grip, 1, Mats.MAT_WOOD.id), "W ", " W", " W", 'W', KEY_PLANKS);
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.part_stock, 1, Mats.MAT_POLYMER.id), "WWW", "  W", 'W', POLYMER.ingot());
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.part_grip, 1, Mats.MAT_POLYMER.id), "W ", " W", " W", 'W', POLYMER.ingot());
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.part_stock, 1, Mats.MAT_BAKELITE.id), "WWW", "  W", 'W', BAKELITE.ingot());
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.part_grip, 1, Mats.MAT_BAKELITE.id), "W ", " W", " W", 'W', BAKELITE.ingot());
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.part_stock, 1, Mats.MAT_HARDPLASTIC.id), "WWW", "  W", 'W', PC.ingot());
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.part_grip, 1, Mats.MAT_HARDPLASTIC.id), "W ", " W", " W", 'W', PC.ingot());
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.part_stock, 1, Mats.MAT_PVC.id), "WWW", "  W", 'W', PVC.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.part_grip, 1, Mats.MAT_PVC.id), "W ", " W", " W", 'W', PVC.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.part_grip, 1, Mats.MAT_RUBBER.id), "W ", " W", " W", 'W', RUBBER.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.part_grip, 1, Mats.MAT_IVORY.id), "W ", " W", " W", 'W', Items.BONE );

        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.casing, ItemEnums.EnumCasingType.SHOTSHELL, 2), "P", "C", 'P', GUNMETAL.plate(), 'C', DictFrame.fromOne(ModItems.casing, ItemEnums.EnumCasingType.LARGE) );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.casing, ItemEnums.EnumCasingType.BUCKSHOT, 2), "P", "C", 'P', ANY_PLASTIC.ingot(), 'C', DictFrame.fromOne(ModItems.casing, ItemEnums.EnumCasingType.LARGE) );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.casing, ItemEnums.EnumCasingType.BUCKSHOT_ADVANCED, 2), "P", "C", 'P', ANY_PLASTIC.ingot(), 'C', DictFrame.fromOne(ModItems.casing, ItemEnums.EnumCasingType.LARGE_STEEL) );

        //SEDNA Guns
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_pepperbox, 1), "IIW", "  C", 'I', IRON.ingot(), 'W', KEY_PLANKS, 'C', CU.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_light_revolver, 1), "BRM", "  G", 'B', STEEL.lightBarrel(), 'R', STEEL.lightReceiver(), 'M', GUNMETAL.mechanism(), 'G', WOOD.grip() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_light_revolver_atlas, 1), " M ", "MAM", " M ", 'M', WEAPONSTEEL.mechanism(), 'A', ModItems.gun_light_revolver );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_henry, 1), "BRP", "BMS", 'B', STEEL.lightBarrel(), 'R', GUNMETAL.lightReceiver(), 'M', GUNMETAL.mechanism(), 'S', WOOD.stock(), 'P', GUNMETAL.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_henry_lincoln, 1)," M ", "PGP", " M ", 'M', WEAPONSTEEL.mechanism(), 'P', GOLD.plateCast(), 'G', ModItems.gun_henry );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_greasegun, 1), "BRS", "SMG", 'B', STEEL.lightBarrel(), 'R', STEEL.lightReceiver(), 'S', STEEL.bolt(), 'M', GUNMETAL.mechanism(), 'G', STEEL.grip() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_maresleg, 1), "BRM", "BGS", 'B', STEEL.lightBarrel(), 'R', STEEL.lightReceiver(), 'M', GUNMETAL.mechanism(), 'G', STEEL.bolt(), 'S', WOOD.stock() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_maresleg_akimbo, 1), "SMS", 'S', ModItems.gun_maresleg, 'M', WEAPONSTEEL.mechanism() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_flaregun, 1), "BRM", "  G", 'B', STEEL.heavyBarrel(), 'R', STEEL.lightReceiver(), 'M', GUNMETAL.mechanism(), 'G', STEEL.grip() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_am180, 1), "BBR", "GMS", 'B', DURA.lightBarrel(), 'R', DURA.lightReceiver(), 'M', GUNMETAL.mechanism(), 'G', WOOD.grip(), 'S', WOOD.stock() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_liberator, 1), "BB ", "BBM", "G G", 'B', DURA.lightBarrel(), 'M', GUNMETAL.mechanism(), 'G', WOOD.grip() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_congolake, 1), "BM ", "BRS", "G  ", 'B', DURA.heavyBarrel(), 'M', GUNMETAL.mechanism(), 'R', DURA.lightReceiver(), 'S', WOOD.stock(), 'G', WOOD.grip() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_flamer, 1), " MG", "BBR", " GM", 'M', GUNMETAL.mechanism(), 'G', DURA.grip(), 'B', DURA.heavyBarrel(), 'R', DURA.heavyReceiver() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_flamer_topaz, 1), " M ", "MFM", " M ", 'M', WEAPONSTEEL.mechanism(), 'F', ModItems.gun_flamer );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_heavy_revolver, 1), "BRM", "  G", 'B', DESH.lightBarrel(), 'R', DESH.lightReceiver(), 'M', GUNMETAL.mechanism(), 'G', WOOD.grip() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_carbine, 1), "BRM", "G S", 'B', DESH.lightBarrel(), 'R',DESH.lightReceiver(), 'M', GUNMETAL.mechanism(), 'G', WOOD.grip(), 'S', WOOD.stock() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_uzi, 1), "BRS", " GM", 'B', DESH.lightBarrel(), 'R', DESH.lightReceiver(), 'S', ANY_PLASTIC.stock(), 'G', ANY_PLASTIC.grip(), 'M', GUNMETAL.mechanism() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_uzi_akimbo, 1), "UMU", 'U', ModItems.gun_uzi, 'M', WEAPONSTEEL.mechanism() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_spas12, 1), "BRM", "BGS", 'B', DESH.lightBarrel(), 'R', DESH.lightReceiver(), 'M', GUNMETAL.mechanism(), 'G', ANY_PLASTIC.grip(), 'S', DESH.stock() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_panzerschreck, 1), "BBB", "PGM", 'B', DESH.heavyBarrel(), 'P', STEEL.plateCast(), 'G', DESH.grip(), 'M', GUNMETAL.mechanism() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_g3, 1), "BRM", "WGS", 'B', WEAPONSTEEL.lightBarrel(), 'R', WEAPONSTEEL.lightReceiver(), 'M', WEAPONSTEEL.mechanism(), 'W', WOOD.grip(), 'G', RUBBER.grip(), 'S', WOOD.stock() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_stinger, 1), "BBB", "PGM", 'B', WEAPONSTEEL.heavyBarrel(), 'P', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'G', WEAPONSTEEL.grip(), 'M', WEAPONSTEEL.mechanism() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_chemthrower, 1), "MHW", "PSS", 'M', WEAPONSTEEL.mechanism(), 'H', RUBBER.pipe(), 'W', ModItems.wrench, 'P', WEAPONSTEEL.heavyBarrel(), 'S', WEAPONSTEEL.shell() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_amat, 1)," C ", "BRS", " MG", 'G', WOOD.grip(), 'B', FERRO.heavyBarrel(), 'R', FERRO.heavyReceiver(), 'M', WEAPONSTEEL.mechanism(), 'C', DictFrame.fromOne(ModItems.weapon_mod_special, GunFactory.EnumModSpecial.SCOPE), 'S', WOOD.stock() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_m2, 1), "  G", "BRM", "  G", 'G', WOOD.grip(), 'B', FERRO.heavyBarrel(), 'R', FERRO.heavyReceiver(), 'M', WEAPONSTEEL.mechanism() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_autoshotgun, 1), "BRM", "G G", 'B', FERRO.heavyBarrel(), 'R', FERRO.heavyReceiver(), 'M', WEAPONSTEEL.mechanism(), 'G', ANY_PLASTIC.grip() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_autoshotgun_shredder, 1), " M ", "MAM", " M ", 'M', BIGMT.mechanism(), 'A', ModItems.gun_autoshotgun );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_quadro, 1), "BCB", "BMB", "GG ", 'B', FERRO.heavyBarrel(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'M', WEAPONSTEEL.mechanism(), 'G', ANY_PLASTIC.grip() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_lag, 1), "BRM", "  G", 'B', ANY_RESISTANTALLOY.lightBarrel(), 'R', ANY_RESISTANTALLOY.lightReceiver(), 'M', WEAPONSTEEL.mechanism(), 'G', ANY_PLASTIC.grip() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_minigun, 1), "BMG", "BRE", "BGM", 'B', ANY_RESISTANTALLOY.lightBarrel(), 'M', WEAPONSTEEL.mechanism(), 'G', ANY_PLASTIC.grip(), 'R', ANY_RESISTANTALLOY.heavyReceiver(), 'E', ModItems.motor_desh );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_missile_launcher, 1), " CM", "BBB", "G  ", 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'M', WEAPONSTEEL.mechanism(), 'B', ANY_RESISTANTALLOY.heavyBarrel(), 'G', ANY_PLASTIC.grip() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_tesla_cannon, 1), "CCC", "BRB", "MGE", 'C', ModItems.coil_advanced_alloy, 'B', ANY_RESISTANTALLOY.heavyBarrel(), 'R', ANY_RESISTANTALLOY.heavyReceiver(), 'M', WEAPONSTEEL.mechanism(), 'G', ANY_PLASTIC.grip(), 'E', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_stg77, 1), " D ", "BRS", "GM ", 'D', DIAMOND.gem(), 'B', BIGMT.lightBarrel(), 'R', BIGMT.lightReceiver(), 'S', ANY_HARDPLASTIC.stock(), 'G', ANY_HARDPLASTIC.grip(), 'M', BIGMT.mechanism() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_fatman, 1), "PPP", "BSR", "G M", 'P', BIGMT.plate(), 'B', BIGMT.heavyBarrel(), 'S', BIGMT.shell(), 'R', BIGMT.heavyReceiver(), 'G', ANY_HARDPLASTIC.grip(), 'M', BIGMT.mechanism() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_tau, 1), " RD", "CTT", "GMS", 'D', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BISMOID), 'C', CU.pipe(), 'T', ModItems.coil_advanced_torus, 'G', ANY_HARDPLASTIC.grip(), 'R', BIGMT.lightReceiver(), 'M', BIGMT.mechanism(), 'S', ANY_HARDPLASTIC.stock() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_lasrifle, 1), "LC ", "BRS", "MG ", 'L', ModItems.crystal_redstone, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BISMOID), 'B', ANY_BISMOIDBRONZE.lightBarrel(), 'R', ANY_BISMOIDBRONZE.lightReceiver(), 'S', ANY_HARDPLASTIC.stock(), 'M', BIGMT.mechanism(), 'G', ANY_HARDPLASTIC.grip() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.gun_double_barrel_sacred_dragon, 1), ModItems.gun_double_barrel, DictFrame.fromOne(ModItems.item_secret, ItemEnums.EnumSecretType.SELENIUM_STEEL) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_charge_thrower, 1),"MMM", "BBL", "GG ", 'M', GUNMETAL.mechanism(), 'B', STEEL.heavyBarrel(), 'G', STEEL.grip(), 'L', Items.LEATHER );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_charge_thrower, 1),"MMM", "BBL", "GG ", 'M', GUNMETAL.mechanism(), 'B', STEEL.heavyBarrel(), 'G', STEEL.grip(), 'L', ANY_RUBBER.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_drill, 1), " GL", "IBP", " GL", 'G', GUNMETAL.ingot(), 'L', ANY_RUBBER.ingot(), 'I', TI.ingot(), 'B', STEEL.block(), 'P', ModItems.piston_selenium );

        //SEDNA Ammo
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.STONE, 6), "C", "P", "G", 'C', KEY_COBBLESTONE, 'P', Items.PAPER, 'G', Items.GUNPOWDER );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.STONE_AP, 6), "C", "P", "G", 'C', Items.FLINT, 'P', Items.PAPER, 'G', Items.GUNPOWDER );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.STONE_SHOT, 6), "C", "P", "G", 'C', Blocks.GRAVEL, 'P', Items.PAPER, 'G', Items.GUNPOWDER );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.STONE_IRON, 6), "C", "P", "G", 'C', IRON.ingot(), 'P', Items.PAPER, 'G', Items.GUNPOWDER );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.CT_MORTAR_CHARGE, 1), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.CT_MORTAR), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.CT_MORTAR), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.CT_MORTAR), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.CT_MORTAR), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.CT_MORTAR), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.CT_MORTAR), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.CT_MORTAR), ModItems.ducttape, ModItems.ducttape);

        //SEDNA Mods
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weapon_mod_generic, 1, GunFactory.EnumModGeneric.IRON_DAMAGE.ordinal()),GUNMETAL.ingot(), IRON.ingot(), IRON.ingot(), IRON.ingot(), ModItems.ducttape );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weapon_mod_generic, 1, GunFactory.EnumModGeneric.IRON_DURA.ordinal()),GUNMETAL.ingot(), IRON.ingot(), ModItems.ducttape );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weapon_mod_generic, 1, GunFactory.EnumModGeneric.STEEL_DAMAGE.ordinal()),GUNMETAL.mechanism(), STEEL.plateCast(), STEEL.plateCast(), STEEL.plateCast(), ModItems.ducttape );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weapon_mod_generic, 1, GunFactory.EnumModGeneric.STEEL_DURA.ordinal()),GUNMETAL.plate(), STEEL.plateCast(), ModItems.ducttape );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weapon_mod_generic, 1, GunFactory.EnumModGeneric.DURA_DAMAGE.ordinal()),GUNMETAL.mechanism(), DURA.plateCast(), DURA.plateCast(), DURA.plateCast(), ModItems.ducttape );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weapon_mod_generic, 1, GunFactory.EnumModGeneric.DURA_DURA.ordinal()),GUNMETAL.plate(), DURA.plateCast(), ModItems.ducttape );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weapon_mod_generic, 1, GunFactory.EnumModGeneric.DESH_DAMAGE.ordinal()),GUNMETAL.mechanism(), DESH.plateCast(), DESH.plateCast(), DESH.plateCast(), ModItems.ducttape );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weapon_mod_generic, 1, GunFactory.EnumModGeneric.DESH_DURA.ordinal()),GUNMETAL.plate(), DESH.plateCast(), ModItems.ducttape );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weapon_mod_generic, 1, GunFactory.EnumModGeneric.WSTEEL_DAMAGE.ordinal()),WEAPONSTEEL.mechanism(), WEAPONSTEEL.plateCast(), WEAPONSTEEL.plateCast(), WEAPONSTEEL.plateCast(), ModItems.ducttape );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weapon_mod_generic, 1, GunFactory.EnumModGeneric.WSTEEL_DURA.ordinal()),WEAPONSTEEL.plate(), WEAPONSTEEL.plateCast(), ModItems.ducttape );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weapon_mod_generic, 1, GunFactory.EnumModGeneric.FERRO_DAMAGE.ordinal()),WEAPONSTEEL.mechanism(), FERRO.plateCast(), FERRO.plateCast(), FERRO.plateCast(), ModItems.ducttape );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weapon_mod_generic, 1, GunFactory.EnumModGeneric.FERRO_DURA.ordinal()),WEAPONSTEEL.plate(), FERRO.plateCast(), ModItems.ducttape );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weapon_mod_generic, 1, GunFactory.EnumModGeneric.TCALLOY_DAMAGE.ordinal()),WEAPONSTEEL.mechanism(), ANY_RESISTANTALLOY.plateCast(), ANY_RESISTANTALLOY.plateCast(), ANY_RESISTANTALLOY.plateCast(), ModItems.ducttape );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weapon_mod_generic, 1, GunFactory.EnumModGeneric.TCALLOY_DURA.ordinal()),WEAPONSTEEL.plate(), ANY_RESISTANTALLOY.plateCast(), ModItems.ducttape );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weapon_mod_generic, 1, GunFactory.EnumModGeneric.BIGMT_DAMAGE.ordinal()),BIGMT.mechanism(), BIGMT.plateCast(), BIGMT.plateCast(), BIGMT.plateCast(), ModItems.ducttape );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weapon_mod_generic, 1, GunFactory.EnumModGeneric.BIGMT_DURA.ordinal()),BIGMT.plate(), BIGMT.plateCast(), ModItems.ducttape );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weapon_mod_generic, 1, GunFactory.EnumModGeneric.BRONZE_DAMAGE.ordinal()),BIGMT.mechanism(), ANY_BISMOIDBRONZE.plateCast(), ANY_BISMOIDBRONZE.plateCast(), ANY_BISMOIDBRONZE.plateCast(), ModItems.ducttape );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weapon_mod_generic, 1, GunFactory.EnumModGeneric.BRONZE_DURA.ordinal()),BIGMT.plate(), ANY_BISMOIDBRONZE.plateCast(), ModItems.ducttape );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.SILENCER.ordinal()),"P", "B", "P", 'P', ANY_PLASTIC.ingot(), 'B', STEEL.lightBarrel() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.SCOPE.ordinal()),"SPS", "G G", "SPS", 'P', ANY_PLASTIC.ingot(), 'S', STEEL.plate(), 'G', KEY_ANYPANE );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.SAW.ordinal()),"BBS", "BHS", 'B', STEEL.bolt(), 'S', KEY_STICK, 'H', DURA.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.SPEEDLOADER.ordinal())," B ", "BSB", " B ", 'B', STEEL.bolt(), 'S', WEAPONSTEEL.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.SLOWDOWN.ordinal())," I ", " M ", "I I", 'I', WEAPONSTEEL.ingot(), 'M', WEAPONSTEEL.mechanism() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.SPEEDUP.ordinal()),"PIP", "WWW", "PIP", 'P', WEAPONSTEEL.plate(), 'I', GUNMETAL.ingot(), 'W', GOLD.wireDense() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.GREASEGUN.ordinal()),"BRM", "P G", 'B', WEAPONSTEEL.lightBarrel(), 'R', WEAPONSTEEL.lightReceiver(), 'M', WEAPONSTEEL.mechanism(), 'P', DURA.plate(), 'G', ANY_PLASTIC.grip() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.CHOKE.ordinal()),"P", "B", "P", 'P', WEAPONSTEEL.plate(), 'B', DURA.lightBarrel() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.FURNITURE_GREEN.ordinal()),"PDS", "  G", 'P', ANY_PLASTIC.ingot(), 'D', KEY_GREEN, 'S', ANY_PLASTIC.stock(), 'G', ANY_PLASTIC.grip() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.FURNITURE_BLACK.ordinal()),"PDS", "  G", 'P', ANY_PLASTIC.ingot(), 'D', KEY_BLACK, 'S', ANY_PLASTIC.stock(), 'G', ANY_PLASTIC.grip() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.SKIN_SATURNITE.ordinal()),"BRM", " P ", 'B', BIGMT.lightBarrel(), 'R', BIGMT.lightReceiver(), 'M', BIGMT.mechanism(), 'P', BIGMT.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.STACK_MAG.ordinal()),"P P", "P P", "PMP", 'P', WEAPONSTEEL.plate(), 'M', BIGMT.mechanism() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.BAYONET.ordinal()),"  P", "BBB", 'P', WEAPONSTEEL.plate(), 'B', STEEL.bolt() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.LAS_SHOTGUN.ordinal()),"PPP", "RCR", "PPP", 'P', ANY_HARDPLASTIC.ingot(), 'R', ModItems.crystal_redstone, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.LAS_CAPACITOR.ordinal()),"CCC", "PIP", 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CAPACITOR_TANTALIUM), 'P', ANY_HARDPLASTIC.ingot(), 'I', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP_BISMOID) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.LAS_AUTO.ordinal())," C ", "RFR", " C ", 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP_BISMOID), 'R', ModItems.crystal_redstone, 'F', ANY_BISMOIDBRONZE.heavyReceiver() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.DRILL_HSS.ordinal()), " IP", "IIM", " IP", 'I', DURA.ingot(), 'P', ANY_PLASTIC.ingot(), 'M', GUNMETAL.mechanism() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.DRILL_WEAPONSTEEL.ordinal()), " IP", "IIM", " IP", 'I', WEAPONSTEEL.ingot(), 'P', RUBBER.ingot(), 'M', GUNMETAL.mechanism() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.DRILL_TCALLOY.ordinal()), " IP", "IIM", " IP", 'I', ANY_RESISTANTALLOY.ingot(), 'P', RUBBER.ingot(), 'M', WEAPONSTEEL.mechanism() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.DRILL_SATURNITE.ordinal()), " IP", "IIM", " IP", 'I', BIGMT.ingot(), 'P', ANY_HARDPLASTIC.ingot(), 'M', WEAPONSTEEL.mechanism() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.ENGINE_DIESEL.ordinal()), "DSD", "PPP", "DSD", 'D', DURA.plate(), 'P', ModItems.piston_selenium, 'S', STEEL.pipe() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.ENGINE_AVIATION.ordinal()), "DSD", "PPP", "DSD", 'D', DURA.plateCast(), 'P', ModItems.piston_selenium, 'S', GUNMETAL.mechanism() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.ENGINE_ELECTRIC.ordinal()), "DSD", "PPP", "DSD", 'D', ANY_PLASTIC.ingot(), 'P', GOLD.wireDense(), 'S', ModBlocks.capacitor_gold );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.ENGINE_TURBO.ordinal()), "DSD", "PPP", "DSD", 'D', ANY_BISMOIDBRONZE.plateCast(), 'P', ModItems.piston_selenium, 'S', WEAPONSTEEL.mechanism() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.MAGNET.ordinal()), "RGR", "GBG", "RGR", 'R', RUBBER.ingot(), 'G', GOLD.wireDense(), 'B', NB.block() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.SIFTER.ordinal()), "IGI", "IGI", 'I', DURA.ingot(), 'G', ModBlocks.steel_grate );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.weapon_mod_special, 1, GunFactory.EnumModSpecial.CANISTERS.ordinal()), " R ", "CCC", "SSS", 'R', RUBBER.pipe(), 'C', ModItems.canister_empty, 'S', STEEL.plate() );

        //Nitra!
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.M357_SP, 6), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.M357_SP), ModItems.nitra );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.M44_SP, 6), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.M44_SP), ModItems.nitra );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.P9_SP, 12), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.P9_SP), ModItems.nitra );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.P22_SP, 32), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.P22_SP), ModItems.nitra );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.R556_SP, 8), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.R556_SP), ModItems.nitra );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.R762_SP, 6), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.R762_SP), ModItems.nitra );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.BMG50_SP, 4), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.BMG50_SP), ModItems.nitra );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.G40_HE, 3), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.G40_HE), ModItems.nitra );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.ROCKET_HE, 2), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.ROCKET_HE), ModItems.nitra );

        //secrets!
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.ammo_secret, GunFactory.EnumAmmoSecret.M44_EQUESTRIAN, 6), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.M44_JHP), DictFrame.fromOne(ModItems.item_secret, ItemEnums.EnumSecretType.SELENIUM_STEEL) );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.ammo_secret, GunFactory.EnumAmmoSecret.G12_EQUESTRIAN, 6), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.G12), DictFrame.fromOne(ModItems.item_secret, ItemEnums.EnumSecretType.SELENIUM_STEEL) );
        CraftingManager.addShapelessAuto(DictFrame.fromOne(ModItems.ammo_secret, GunFactory.EnumAmmoSecret.BMG50_EQUESTRIAN, 6), DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.BMG50_FMJ), DictFrame.fromOne(ModItems.item_secret, ItemEnums.EnumSecretType.SELENIUM_STEEL) );

        //Missiles
        // TODO: ik modforgefluids is deprecated, I'm lazy to deal with it now
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.missile_taint, 1), ModItems.missile_assembly, new CraftingManager.IngredientContainsTag(FluidUtil.getFilledBucket(new FluidStack(ModFluids.mud_fluid, 1000))), ModItems.powder_spark_mix, ModItems.powder_magic );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.missile_micro, 1), ModItems.missile_assembly, ModItems.ducttape, DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.NUKE_HIGH) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.missile_bhole, 1), ModItems.missile_assembly, ModItems.ducttape, ModItems.grenade_black_hole );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.missile_schrabidium, 1), ModItems.missile_assembly, ModItems.ducttape, ItemCell.getFullCell(Fluids.AMAT), ANY_HARDPLASTIC.ingot() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.missile_emp, 1), ModItems.missile_assembly, ModItems.ducttape, ModBlocks.emp_bomb );

        //Missile fins
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_stability_10_flat, 1), "PSP", "P P", 'P', STEEL.plate(), 'S', ModBlocks.steel_scaffold );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_stability_10_cruise, 1), "ASA", " S ", "PSP", 'A', TI.plate(), 'P', STEEL.plate(), 'S', ModBlocks.steel_scaffold );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_stability_10_space, 1), "ASA", "PSP", 'A', AL.plate(), 'P', STEEL.ingot(), 'S', ModBlocks.steel_scaffold );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_stability_15_flat, 1), "ASA", "PSP", 'A', AL.plate(), 'P', STEEL.plate(), 'S', ModBlocks.steel_scaffold );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_stability_15_thin, 1), "A A", "PSP", "PSP", 'A', AL.plate(), 'P', STEEL.plate(), 'S', ModBlocks.steel_scaffold );

        //Missile thrusters
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_thruster_15_balefire_large_rad, 1), "CCC", "CTC", "CCC", 'C', CU.plateCast(), 'T', ModItems.mp_thruster_15_balefire_large );

        //Missile fuselages
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_fuselage_10_kerosene_insulation, 1), "CCC", "CTC", "CCC", 'C', ANY_RUBBER.ingot(), 'T', ModItems.mp_fuselage_10_kerosene );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_fuselage_10_long_kerosene_insulation, 1), "CCC", "CTC", "CCC", 'C', ANY_RUBBER.ingot(), 'T', ModItems.mp_fuselage_10_long_kerosene );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_fuselage_15_kerosene_insulation, 1), "CCC", "CTC", "CCC", 'C', ANY_RUBBER.ingot(), 'T', ModItems.mp_fuselage_15_kerosene );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_fuselage_10_solid_insulation, 1), "CCC", "CTC", "CCC", 'C', ANY_RUBBER.ingot(), 'T', ModItems.mp_fuselage_10_solid );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_fuselage_10_long_solid_insulation, 1), "CCC", "CTC", "CCC", 'C', ANY_RUBBER.ingot(), 'T', ModItems.mp_fuselage_10_long_solid );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_fuselage_15_solid_insulation, 1), "CCC", "CTC", "CCC", 'C', ANY_RUBBER.ingot(), 'T', ModItems.mp_fuselage_15_solid );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_fuselage_15_solid_desh, 1), "CCC", "CTC", "CCC", 'C', DESH.ingot(), 'T', ModItems.mp_fuselage_15_solid );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_fuselage_10_kerosene_metal, 1), "ICI", "CTC", "ICI", 'C', STEEL.plate(), 'I', IRON.plate(), 'T', ModItems.mp_fuselage_10_kerosene );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_fuselage_10_long_kerosene_metal, 1), "ICI", "CTC", "ICI", 'C', STEEL.plate(), 'I', IRON.plate(), 'T', ModItems.mp_fuselage_10_long_kerosene );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_fuselage_15_kerosene_metal, 1), "ICI", "CTC", "ICI", 'C', STEEL.plate(), 'I', IRON.plate(), 'T', ModItems.mp_fuselage_15_kerosene );

        //Missile warheads
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_warhead_15_boxcar, 1), "SNS", "CBC", "SFS", 'S', STAR.ingot(), 'N', ModBlocks.det_nuke, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'B', ModBlocks.boxcar, 'F', ModItems.tritium_deuterium_cake );

        //Missile chips
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_chip_1, 1), "P", "C", "S", 'P', ANY_RUBBER.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE), 'S', ModBlocks.steel_scaffold );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_chip_2, 1), "P", "C", "S", 'P', ANY_RUBBER.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ANALOG), 'S', ModBlocks.steel_scaffold );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_chip_3, 1), "P", "C", "S", 'P', ANY_RUBBER.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), 'S', ModBlocks.steel_scaffold );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_chip_4, 1), "P", "C", "S", 'P', ANY_RUBBER.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'S', ModBlocks.steel_scaffold );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mp_chip_5, 1), "P", "C", "S", 'P', ANY_RUBBER.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BISMOID), 'S', ModBlocks.steel_scaffold );

        //Turrets
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.turret_sentry, 1), "PPL", " MD", " SC", 'P', STEEL.plate(), 'M', ModItems.motor, 'L', GUNMETAL.mechanism(), 'S', ModBlocks.steel_scaffold, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), 'D', ModItems.crt_display );

        //Guns
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_b92), "DDD", "SSC", "  R", 'D', ModItems.plate_dineutronium, 'S', STAR.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BISMOID), 'R', ModItems.gun_lasrifle );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_b92_ammo, 1), "PSP", "ESE", "PSP", 'P', STEEL.plate(), 'S', STAR.ingot(), 'E', ModItems.powder_spark_mix );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.weaponized_starblaster_cell, 1), new ItemStack(ModItems.fluid_tank_full, 1, Fluids.PEROXIDE.getID()), GunB92Cell.getFullCell(), CU.wireFine() );
         CraftingManager.addRecipeAuto(new ItemStack(ModItems.gun_fireext, 1), "HB", " T", 'H', STEEL.pipe(), 'B', STEEL.bolt(), 'T', ModItems.tank_steel );


        //Ammo assemblies
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.assembly_nuke, 1), " WP", "SEP", " WP", 'W', GOLD.wireFine(), 'P', STEEL.plate(), 'S', STEEL.shell(), 'E', ModItems.ball_tatb );

        //240mm Shells
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ammo_shell, 4), " T ", "GHG", "CCC", 'T', ModBlocks.tnt, 'G', Items.GUNPOWDER, 'H', STEEL.shell(), 'C', CU.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ammo_shell, 4), " T ", "GHG", "CCC", 'T', ModBlocks.tnt, 'G', ModItems.ballistite, 'H', STEEL.shell(), 'C', CU.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ammo_shell, 6), " T ", "GHG", "CCC", 'T', ModBlocks.tnt, 'G', ModItems.cordite, 'H', STEEL.shell(), 'C', CU.ingot() );
        CraftingManager.addRecipeAuto(ModItems.ammo_shell.stackFromEnum(4, Ammo240Shell.EXPLOSIVE), " T ", "GHG", "CCC", 'T', ANY_PLASTICEXPLOSIVE.ingot(), 'G', Items.GUNPOWDER, 'H', STEEL.shell(), 'C', CU.ingot() );
        CraftingManager.addRecipeAuto(ModItems.ammo_shell.stackFromEnum(4, Ammo240Shell.EXPLOSIVE), " T ", "GHG", "CCC", 'T', ANY_PLASTICEXPLOSIVE.ingot(), 'G', ModItems.ballistite, 'H', STEEL.shell(), 'C', CU.ingot() );
        CraftingManager.addRecipeAuto(ModItems.ammo_shell.stackFromEnum(6, Ammo240Shell.EXPLOSIVE), " T ", "GHG", "CCC", 'T', ANY_PLASTICEXPLOSIVE.ingot(), 'G', ModItems.cordite, 'H', STEEL.shell(), 'C', CU.ingot() );
        CraftingManager.addRecipeAuto(ModItems.ammo_shell.stackFromEnum(4, Ammo240Shell.APFSDS_T), " I ", "GIG", "CCC", 'I', W.ingot(), 'G', Items.GUNPOWDER, 'C', CU.ingot() );
        CraftingManager.addRecipeAuto(ModItems.ammo_shell.stackFromEnum(4, Ammo240Shell.APFSDS_T), " I ", "GIG", "CCC", 'I', W.ingot(), 'G', ModItems.ballistite, 'C', CU.ingot() );
        CraftingManager.addRecipeAuto(ModItems.ammo_shell.stackFromEnum(6, Ammo240Shell.APFSDS_T), " I ", "GIG", "CCC", 'I', W.ingot(), 'G', ModItems.cordite, 'C', CU.ingot() );
        CraftingManager.addRecipeAuto(ModItems.ammo_shell.stackFromEnum(4, Ammo240Shell.APFSDS_DU), " I ", "GIG", "CCC", 'I', U238.ingot(), 'G', Items.GUNPOWDER, 'C', CU.ingot() );
        CraftingManager.addRecipeAuto(ModItems.ammo_shell.stackFromEnum(4, Ammo240Shell.APFSDS_DU), " I ", "GIG", "CCC", 'I', U238.ingot(), 'G', ModItems.ballistite, 'C', CU.ingot() );
        CraftingManager.addRecipeAuto(ModItems.ammo_shell.stackFromEnum(6, Ammo240Shell.APFSDS_DU), " I ", "GIG", "CCC", 'I', U238.ingot(), 'G', ModItems.cordite, 'C', CU.ingot() );
        CraftingManager.addRecipeAuto(ModItems.ammo_shell.stackFromEnum(Ammo240Shell.W9), " P ", "NSN", " P ", 'P', PU239.nugget(), 'N', OreDictManager.getReflector(), 'S', ModItems.ammo_shell.stackFromEnum(Ammo240Shell.EXPLOSIVE) );

        //Artillery Shells
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ammo_arty, 1, 0), "CIC", "CSC", "CCC", 'C', ModItems.cordite, 'I', IRON.block(), 'S', CU.shell() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ammo_arty, 1, 1), " D ", "DSD", " D ", 'D', ModItems.ball_dynamite, 'S', new ItemStack(ModItems.ammo_arty, 1, 0) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ammo_arty, 1, 2), "TTT", "TST", "TTT", 'T', ModItems.ball_tnt, 'S', new ItemStack(ModItems.ammo_arty, 1, 0) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ammo_arty, 1, 5), "D", "S", "D", 'D', P_WHITE.ingot(), 'S', new ItemStack(ModItems.ammo_arty, 1, 0) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ammo_arty, 1, 7), "DSD", "SCS", "DSD", 'D', P_WHITE.ingot(), 'S', new ItemStack(ModItems.ammo_arty, 1, 5), 'C', ModBlocks.det_cord );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ammo_arty, 1, 3), " P ", "NSN", " P ", 'P', PU239.nugget(), 'N', OreDictManager.getReflector(), 'S', new ItemStack(ModItems.ammo_arty, 1, 0) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ammo_arty, 1, 6), "DSD", "SCS", "DSD", 'D', OreDictManager.getReflector(), 'S', new ItemStack(ModItems.ammo_arty, 1, 3), 'C', ModBlocks.det_cord );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.ammo_arty, 1, 4), new ItemStack(ModItems.ammo_arty, 1, 2), ModItems.boy_bullet, ModItems.boy_target, ModItems.boy_shielding, DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CONTROLLER), ModItems.ducttape );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ammo_arty, 1, 8), " I ", " S ", "CCC", 'C', ModItems.cordite, 'I', ModItems.sphere_steel, 'S', CU.shell() );

        //DGK Belts
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ammo_dgk, 1), "LLL", "GGG", "CCC", 'L', PB.plate(), 'G', ModItems.ballistite, 'C', CU.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ammo_dgk, 1), "LLL", "GGG", "CCC", 'L', PB.plate(), 'G', ModItems.cordite, 'C', CU.ingot() );

        //Fire Extingusisher Tanks
        /*CraftingManager.addRecipeAuto(new ItemStack(ModItems.ammo_fireext, 1), " P ", "BDB", " P ", 'P', STEEL.plate(), 'B', STEEL.bolt(), 'D', new ItemStack(ModItems.fluid_tank_full, 1, Fluids.WATER.getID()) );
        CraftingManager.addRecipeAuto(ModItems.ammo_fireext.stackFromEnum(AmmoFireExt.FOAM), " N ", "NFN", " N ", 'N', KNO.dust(), 'F', ModItems.ammo_fireext );
        CraftingManager.addRecipeAuto(ModItems.ammo_fireext.stackFromEnum(AmmoFireExt.SAND), "NNN", "NFN", "NNN", 'N', ModBlocks.sand_boron, 'F', ModItems.ammo_fireext );*/

        //Grenades
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_generic, 4), "RS ", "ITI", " I ", 'I', IRON.plate(), 'R', MINGRADE.wireFine(), 'S', STEEL.plate(), 'T', Item.getItemFromBlock(Blocks.TNT) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_strong, 2), " G ", "SGS", " S ", 'G', ModItems.grenade_generic, 'S', Items.GUNPOWDER );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_frag, 2), " G ", "WGW", " K ", 'G', ModItems.grenade_generic, 'W', KEY_PLANKS, 'K', Item.getItemFromBlock(Blocks.GRAVEL) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_poison, 2), " G ", "PGP", " P ", 'G', ModItems.grenade_generic, 'P', ModItems.powder_poison );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_gas, 2), " G ", "CGC", " C ", 'G', ModItems.grenade_generic, 'C', ModItems.pellet_gas );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_aschrab, 1), "RS ", "ITI", " S ", 'I', KEY_CLEARGLASS, 'R', MINGRADE.wireFine(), 'S', STEEL.plate(), 'T', ItemCell.getFullCell(Fluids.ASCHRAB) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_mk2, 2), " G ", "SGS", " S ", 'G', ModItems.grenade_strong, 'S', Items.GUNPOWDER );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.grenade_gascan, 1), Fluids.DIESEL.getDict(1000), Items.FLINT );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.grenade_gascan, 1), Fluids.DIESEL_CRACK.getDict(1000), Items.FLINT );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.grenade_gascan, 1), Fluids.PETROIL.getDict(1000), Items.FLINT );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.grenade_gascan, 1), Fluids.PETROIL_LEADED.getDict(1000), Items.FLINT );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.grenade_gascan, 1), Fluids.GASOLINE.getDict(1000), Items.FLINT );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.grenade_gascan, 1), Fluids.GASOLINE_LEADED.getDict(1000), Items.FLINT );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.grenade_gascan, 1), Fluids.BIOFUEL.getDict(1000), Items.FLINT );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.grenade_lemon, 1), ModItems.lemon, ModItems.grenade_strong );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_smart, 4), " A ", "ACA", " A ", 'A', ModItems.grenade_strong, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_mirv, 1), "GGG", "GCG", "GGG", 'G', ModItems.grenade_smart, 'C', ModItems.grenade_generic );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_breach, 1), "G", "G", "P", 'G', ModItems.grenade_smart, 'P', BIGMT.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_burst, 1), "GGG", "GCG", "GGG", 'G', ModItems.grenade_breach, 'C', ModItems.grenade_generic );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_cloud), "SPS", "CAC", "SPS", 'S', S.dust(), 'P', ModItems.powder_poison, 'C', CU.dust(), 'A', new ItemStack(ModItems.fluid_tank_full, 1, Fluids.PEROXIDE.getID()) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_pink_cloud), " S ", "ECE", " E ", 'S', ModItems.powder_spark_mix, 'E', ModItems.powder_magic, 'C', ModItems.grenade_cloud );
//        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nuclear_waste_pearl), "WWW", "WFW", "WWW", 'W', ModItems.nuclear_waste_tiny, 'F', ModBlocks.block_fallout );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.grenade_kyiv), ModItems.canister_napalm, ModItems.bottle2_empty, ModItems.rag );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.disperser_canister_empty, 4), " P ", "PGP", " P ", 'P', ANY_HARDPLASTIC.ingot(), 'G', ModBlocks.glass_boron );

        //Sticks of explosives
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.stick_dynamite, 4), " S ", "PDP", "PDP", 'S', ModItems.safety_fuse, 'P', Items.PAPER, 'D', ModItems.ball_dynamite );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.stick_dynamite_fishing, 1), ModItems.stick_dynamite, ModItems.stick_dynamite, ModItems.stick_dynamite, Items.PAPER, ANY_TAR.any() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.stick_tnt, 4), " S ", "PDP", "PDP", 'S', ModBlocks.det_cord, 'P', Items.PAPER, 'D', ModItems.ball_tnt );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.stick_semtex, 4), " S ", "PDP", "PDP", 'S', ModBlocks.det_cord, 'P', Items.PAPER, 'D', ModItems.ingot_semtex );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.stick_c4, 4), " S ", "PDP", "PDP", 'S', ModBlocks.det_cord, 'P', Items.PAPER, 'D', ModItems.ingot_c4 );

        //Blocks of explosives
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.dynamite, 1), "DDD", "DSD", "DDD", 'D', ModItems.stick_dynamite, 'S', ModItems.safety_fuse );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.tnt, 1), "DDD", "DSD", "DDD", 'D', ModItems.stick_tnt, 'S', ModItems.safety_fuse );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.semtex, 1), "DDD", "DSD", "DDD", 'D', ModItems.stick_semtex, 'S', ModItems.safety_fuse );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.c4, 1), "DDD", "DSD", "DDD", 'D', ModItems.stick_c4, 'S', ModItems.safety_fuse );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.fissure_bomb, 1), "SUS", "RPR", "SUS", 'S', ModBlocks.semtex, 'U', U238.block(), 'R', TA.ingot(), 'P', PU239.billet() );


        //IF Grenades
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_if_generic, 1), " C ", "PTP", " P ", 'C', ModItems.coil_tungsten, 'P', STEEL.plate(), 'T', Blocks.TNT );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_if_he, 1), "A", "G", "A", 'G', ModItems.grenade_if_generic, 'A', Items.GUNPOWDER );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_if_bouncy, 1), "G", "A", 'G', ModItems.grenade_if_generic, 'A', ANY_RUBBER.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_if_sticky, 1), "G", "A", 'G', ModItems.grenade_if_generic, 'A', KEY_SLIME );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_if_impact, 1), "G", "A", 'G', ModItems.grenade_if_generic, 'A', REDSTONE.dust() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_if_concussion, 1), "G", "A", 'G', ModItems.grenade_if_generic, 'A', Items.GLOWSTONE_DUST );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_if_toxic, 1), "G", "A", 'G', ModItems.grenade_if_generic, 'A', ModItems.powder_poison );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_if_incendiary, 1), "G", "A", 'G', ModItems.grenade_if_generic, 'A', P_RED.dust() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_if_brimstone, 1), "R", "G", "A", 'G', ModItems.grenade_if_generic, 'R', REDSTONE.dust(), 'A', S.dust() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_if_mystery, 1), "A", "G", "A", 'G', ModItems.grenade_if_generic, 'A', ModItems.powder_magic );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_if_spark, 1), " A ", "AGA", " A ", 'G', ModItems.grenade_if_generic, 'A', ModItems.powder_spark_mix );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_if_hopwire, 1), " A ", "AGA", " A ", 'G', ModItems.grenade_if_generic, 'A', ModItems.powder_power );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.grenade_if_null, 1), "BAB", "AGA", "BAB", 'G', ModItems.grenade_if_generic, 'A', ModItems.undefined, 'B', BIGMT.ingot() );

        //Mines
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.mine_ap, 4), "I", "C", "S", 'I', ModItems.plate_polymer, 'C', ANY_SMOKELESS.dust(), 'S', STEEL.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.mine_shrap, 1), "L", "M", 'M', ModBlocks.mine_ap, 'L', ModItems.pellet_buckshot );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.mine_he, 1), " C ", "PTP", 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), 'P', STEEL.plate(), 'T', ANY_HIGHEXPLOSIVE.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.mine_fat, 1), "CDN", 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ANALOG), 'D', ModItems.ducttape, 'N', DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.NUKE_DEMO) );

        //Nuke parts
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.n2_charge, 1), " D ", "ERE", " D ", 'D', ModItems.ducttape, 'E', ModBlocks.det_charge, 'R', REDSTONE.block() );

        //Custom nuke rods
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.custom_tnt, 1), " C ", "TIT", "TIT", 'C', CU.plate(), 'I', IRON.plate(), 'T', ANY_HIGHEXPLOSIVE.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.custom_nuke, 1), " C ", "LUL", "LUL", 'C', CU.plate(), 'L', PB.plate(), 'U', U235.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.custom_hydro, 1), " C ", "LTL", "LIL", 'C', CU.plate(), 'L', PB.plate(), 'I', IRON.plate(), 'T', ItemCell.getFullCell(Fluids.TRITIUM) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.custom_amat, 1), " C ", "MMM", "AAA", 'C', CU.plate(), 'A', AL.plate(), 'M', ItemCell.getFullCell(Fluids.AMAT) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.custom_dirty, 1), " C ", "WLW", "WLW", 'C', CU.plate(), 'L', PB.plate(), 'W', ModItems.nuclear_waste );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.custom_schrab, 1), " C ", "LUL", "LUL", 'C', CU.plate(), 'L', PB.plate(), 'U', SA326.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.custom_sol, 1), " C ", "LUL", "LUL", 'C', CU.plate(), 'L', PB.plate(), 'U', SA327.ingot());
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.custom_euph, 1), " C ", "LUL", "LUL", 'C', CU.plate(), 'L', PB.plate(), 'U', EUPH.ingot());

        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.lamp_demon, 1), " D ", "S S", 'D', ModItems.demon_core_closed, 'S', STEEL.ingot() );

        CraftingManager.addRecipeAuto(new ItemStack(ModItems.crucible, 1, 3), "MEM", "YDY", "YCY", 'M', ModItems.ingot_meteorite_forged, 'E', EUPH.ingot(), 'Y', ModItems.billet_yharonite, 'D', ModItems.demon_core_closed, 'C', ModItems.ingot_chainsteel );
        //CraftingManager.addRecipeAuto(new ItemStack(ModItems.hf_sword), "MEM", "YDY", "YCY", 'M', ModItems.blade_meteorite, 'E', ModItems.nothing, 'Y', UNOBTAINIUM.billet(), 'D', ModItems.particle_strange, 'C', ModItems.ingot_chainsteel);
        //CraftingManager.addRecipeAuto(new ItemStack(ModItems.hs_sword), "MEM", "YDY", "YCY", 'M', ModItems.blade_meteorite, 'E', GH336.ingot(), 'Y', ModItems.nothing, 'D', ModItems.particle_dark, 'C', ModItems.ingot_chainsteel);
    }
}
