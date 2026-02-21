package com.hbm.itempool;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ItemEnums;
import com.hbm.items.ItemEnums.EnumCircuitType;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBatteryPack;
import com.hbm.items.machine.ItemBreedingRod;
import com.hbm.items.machine.ItemZirnoxRod;
import com.hbm.items.weapon.sedna.factory.GunFactory;
import net.minecraft.init.Items;

import static com.hbm.lib.HbmChestContents.weighted;
/**
 * Item pools for "legacy" structures, i.e. schematic2java ones
 * @author hbm
 *
 */
public class ItemPoolsLegacy {
    public static final String POOL_GENERIC = "POOL_GENERIC";
    public static final String POOL_ANTENNA = "POOL_ANTENNA";
    public static final String POOL_EXPENSIVE = "POOL_EXPENSIVE";
    public static final String POOL_NUKE_TRASH = "POOL_NUKE_TRASH";
    public static final String POOL_NUKE_MISC = "POOL_NUKE_MISC";
    public static final String POOL_VERTIBIRD = "POOL_VERTIBIRD";
    public static final String POOL_SPACESHIP = "POOL_SPACESHIP";

    public static void init() {

        //"generic" set, found commonly in chests in many structures
        new ItemPool(POOL_GENERIC) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(Items.BREAD, 0, 1, 5, 8),
                    weighted(ModItems.twinkie, 0, 1, 3, 6),
                    weighted(Items.IRON_INGOT, 0, 2, 6, 10),
                    weighted(ModItems.ingot_steel, 0, 2, 5, 7),
                    weighted(ModItems.ingot_beryllium, 0, 1, 2, 4),
                    weighted(ModItems.ingot_titanium, 0, 1, 1, 3),
                    weighted(ModItems.circuit, ItemEnums.EnumCircuitType.VACUUM_TUBE.ordinal(), 1, 1, 5),
                    weighted(ModItems.gun_light_revolver, 0, 1, 1, 3),
                    weighted(ModItems.ammo_standard, GunFactory.EnumAmmo.M357_SP.ordinal(), 2, 6, 4),
                    weighted(ModItems.ammo_standard, GunFactory.EnumAmmo.G12_BP.ordinal(), 3, 6, 3),
                    weighted(ModItems.ammo_standard, GunFactory.EnumAmmo.G26_FLARE_SUPPLY.ordinal(), 1, 1, 1),
                    weighted(ModItems.gun_kit_1, 0, 1, 3, 4),
                    weighted(ModItems.gun_maresleg, 0, 1, 1, 1),
                    weighted(ModItems.casing, ItemEnums.EnumCasingType.SMALL.ordinal(), 4, 10, 3),
                    weighted(ModItems.casing, ItemEnums.EnumCasingType.SHOTSHELL.ordinal(), 4, 10, 3),
                    weighted(ModItems.cordite, 0, 4, 6, 5),
                    weighted(ModItems.battery_pack, ItemBatteryPack.EnumBatteryPack.BATTERY_REDSTONE.ordinal(), 1, 1, 1),
                    weighted(ModItems.scrap, 0, 1, 3, 10),
                    weighted(ModItems.dust, 0, 2, 4, 9),
                    weighted(ModItems.bottle_opener, 0, 1, 1, 2),
                    weighted(ModItems.bottle_nuka, 0, 1, 3, 4),
                    weighted(ModItems.bottle_cherry, 0, 1, 1, 2),
                    weighted(ModItems.stealth_boy, 0, 1, 1, 1),
                    weighted(ModItems.cap_nuka, 0, 1, 15, 7),
                    weighted(ModItems.canister_full, Fluids.DIESEL.getID(), 1, 2, 2),
                    weighted(ModItems.canister_full, Fluids.BIOFUEL.getID(), 1, 2, 3),
                    weighted(ModItems.gas_mask_m65, 60, 1, 1, 2),
                    weighted(ModItems.gas_mask_filter, 0, 1, 1, 3),
                    weighted(ModItems.blueprint_folder, 0, 1, 1, 1)
            };
        }};

        //"antenna" pool, found by antennas and in radio stations
        new ItemPool(POOL_ANTENNA) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ModItems.twinkie, 0, 1, 3, 4),
                    weighted(ModItems.ingot_steel, 0, 1, 2, 7),
                    weighted(ModItems.ingot_red_copper, 0, 1, 1, 4),
                    weighted(ModItems.ingot_titanium, 0, 1, 3, 5),
                    weighted(ModItems.wire_fine, Mats.MAT_MINGRADE.id, 2, 3, 7),
                    weighted(ModItems.circuit, ItemEnums.EnumCircuitType.VACUUM_TUBE.ordinal(), 1, 1, 4),
                    weighted(ModItems.circuit, ItemEnums.EnumCircuitType.CAPACITOR.ordinal(), 1, 1, 2),
                    weighted(ModItems.battery_pack, ItemBatteryPack.EnumBatteryPack.BATTERY_REDSTONE.ordinal(), 1, 1, 1),
                    weighted(ModItems.powder_iodine, 0, 1, 1, 1),
                    weighted(ModItems.powder_bromine, 0, 1, 1, 1),
                    weighted(ModBlocks.steel_poles, 0, 1, 4, 8),
                    weighted(ModBlocks.steel_scaffold, 0, 1, 3, 8),
                    weighted(ModBlocks.pole_top, 0, 1, 1, 4),
                    weighted(ModBlocks.pole_satellite_receiver, 0, 1, 1, 7),
                    weighted(ModItems.scrap, 0, 1, 3, 10),
                    weighted(ModItems.dust, 0, 2, 4, 9),
                    weighted(ModItems.bottle_opener, 0, 1, 1, 2),
                    weighted(ModItems.bottle_nuka, 0, 1, 3, 4),
                    weighted(ModItems.bottle_cherry, 0, 1, 1, 2),
                    weighted(ModItems.stealth_boy, 0, 1, 1, 1),
                    weighted(ModItems.cap_nuka, 0, 1, 15, 7),
                    weighted(ModItems.bomb_caller, 0, 1, 1, 1),
                    weighted(ModItems.gas_mask_filter, 0, 1, 1, 2)
            };
        }};

        //"hidden" loot
        new ItemPool(POOL_EXPENSIVE) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ModItems.chlorine_pinwheel, 0, 1, 1, 1),
                    weighted(ModItems.circuit, EnumCircuitType.VACUUM_TUBE.ordinal(), 1, 1, 4),
                    weighted(ModItems.circuit, EnumCircuitType.ANALOG.ordinal(), 1, 1, 3),
                    weighted(ModItems.circuit, EnumCircuitType.CHIP.ordinal(), 1, 1, 2),
                    weighted(ModItems.gun_kit_1, 0, 1, 3, 6),
                    weighted(ModItems.gun_kit_2, 0, 1, 2, 3),
                    weighted(ModItems.gun_panzerschreck, 0, 1, 1, 4),
                    weighted(ModItems.ammo_standard, GunFactory.EnumAmmo.ROCKET_HE.ordinal(), 1, 4, 5),
                    weighted(ModItems.ammo_standard, GunFactory.EnumAmmo.G26_FLARE_SUPPLY.ordinal(), 1, 1, 5),
                    weighted(ModItems.ammo_standard, GunFactory.EnumAmmo.G26_FLARE_WEAPON.ordinal(), 1, 1, 3),
                    weighted(ModItems.grenade_nuclear, 0, 1, 1, 2),
                    weighted(ModItems.grenade_smart, 0, 1, 3, 3),
                    weighted(ModItems.grenade_mirv, 0, 1, 1, 2),
                    weighted(ModItems.stealth_boy, 0, 1, 1, 2),
                    weighted(ModItems.battery_pack, ItemBatteryPack.EnumBatteryPack.BATTERY_LITHIUM.ordinal(), 1, 1, 1),
                    weighted(ModItems.syringe_awesome, 0, 1, 1, 1),
                    weighted(ModItems.fusion_core, 0, 1, 1, 4),
                    weighted(ModItems.bottle_nuka, 0, 1, 3, 6),
                    weighted(ModItems.bottle_quantum, 0, 1, 1, 3),
                    weighted(ModBlocks.red_barrel, 0, 1, 1, 6),
                    weighted(ModItems.canister_full, Fluids.DIESEL.getID(), 1, 2, 2),
                    weighted(ModItems.canister_full, Fluids.BIOFUEL.getID(), 1, 2, 3),
                    weighted(ModItems.gas_mask_m65, 60, 1, 1, 5),
                    weighted(ModItems.bomb_caller, 0, 1, 1, 2),
                    weighted(ModItems.bomb_caller, 1, 1, 1, 1),
                    weighted(ModItems.bomb_caller, 2, 1, 1, 1),
                    weighted(ModItems.gas_mask_filter, 0, 1, 1, 4),
                    weighted(ModItems.journal_pip, 0, 1, 1, 1),
                    weighted(ModItems.journal_bj, 0, 1, 1, 1),
                    // TODO
                    //weighted(ModItems.launch_code_piece, 0, 1, 1, 1),
                    weighted(ModItems.gun_double_barrel, 0, 1, 1, 1),
                    weighted(ModItems.blueprint_folder, 1, 1, 1, 1)
            };
        }};

        //nuclear waste products found in powerplants
        new ItemPool(POOL_NUKE_TRASH) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ModItems.nugget_u238, 0, 3, 12, 5),
                    weighted(ModItems.nugget_pu240, 0, 3, 8, 5),
                    weighted(ModItems.nugget_neptunium, 0, 1, 4, 3),
                    weighted(ModItems.rod, ItemBreedingRod.BreedingRodType.U238.ordinal(), 1, 1, 3),
                    weighted(ModItems.rod_dual, ItemBreedingRod.BreedingRodType.U238.ordinal(), 1, 1, 3),
                    weighted(ModItems.rod_quad, ItemBreedingRod.BreedingRodType.U238.ordinal(), 1, 1, 3),
                    weighted(ModItems.bottle_quantum, 0, 1, 1, 1),
                    weighted(ModItems.gas_mask_m65, 60, 1, 1, 5),
                    weighted(ModItems.hazmat_kit, 0, 1, 1, 1),
                    weighted(ModItems.gas_mask_filter, 0, 1, 1, 5),
                    weighted(ModBlocks.yellow_barrel, 0, 1, 1, 2)
            };
        }};

        //all sorts of nuclear related items, mostly fissile isotopes found in nuclear powerplants
        new ItemPool(POOL_NUKE_MISC) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ModItems.nugget_u235, 0, 3, 12, 5),
                    weighted(ModItems.nugget_pu238, 0, 3, 12, 5),
                    weighted(ModItems.nugget_ra226, 0, 3, 6, 5),
                    weighted(ModItems.rod, ItemBreedingRod.BreedingRodType.U235.ordinal(), 1, 1, 3),
                    weighted(ModItems.rod_dual, ItemBreedingRod.BreedingRodType.U235.ordinal(), 1, 1, 3),
                    weighted(ModItems.rod_quad, ItemBreedingRod.BreedingRodType.U235.ordinal(), 1, 1, 3),
                    weighted(ModItems.rod_zirnox, ItemZirnoxRod.EnumZirnoxType.URANIUM_FUEL.ordinal(), 1, 1, 4),
                    weighted(ModItems.rod_zirnox, ItemZirnoxRod.EnumZirnoxType.MOX_FUEL.ordinal(), 1, 1, 4),
                    weighted(ModItems.rod_zirnox, ItemZirnoxRod.EnumZirnoxType.LITHIUM_FUEL.ordinal(), 1, 1, 3),
                    weighted(ModItems.rod_zirnox, ItemZirnoxRod.EnumZirnoxType.THORIUM_FUEL.ordinal(), 1, 1, 3),
                    weighted(ModItems.rod_dual, ItemBreedingRod.BreedingRodType.THF.ordinal(), 1, 1, 3),
                    weighted(ModItems.rod_zirnox_tritium, 0, 1, 1, 1),
                    weighted(ModItems.rod_zirnox, ItemZirnoxRod.EnumZirnoxType.U233_FUEL.ordinal(), 1, 1, 1),
                    weighted(ModItems.rod_zirnox, ItemZirnoxRod.EnumZirnoxType.U235_FUEL.ordinal(), 1, 1, 1),
                    weighted(ModItems.pellet_rtg, 0, 1, 1, 3),
                    weighted(ModItems.powder_thorium, 0, 1, 1, 1),
                    weighted(ModItems.powder_neptunium, 0, 1, 1, 1),
                    weighted(ModItems.powder_strontium, 0, 1, 1, 1),
                    weighted(ModItems.powder_cobalt, 0, 1, 1, 1),
                    weighted(ModItems.bottle_quantum, 0, 1, 1, 1),
                    weighted(ModItems.gas_mask_m65, 60, 1, 1, 5),
                    weighted(ModItems.hazmat_kit, 0, 1, 1, 2),
                    weighted(ModItems.gas_mask_filter, 0, 1, 1, 5),
                    weighted(ModBlocks.yellow_barrel, 0, 1, 3, 3)
            };
        }};

        //loot found in vertibirds
        new ItemPool(POOL_VERTIBIRD) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ModItems.t51_helmet, 0, 1, 1, 15),
                    weighted(ModItems.t51_plate, 0, 1, 1, 15),
                    weighted(ModItems.t51_legs, 0, 1, 1, 15),
                    weighted(ModItems.t51_boots, 0, 1, 1, 15),
                    weighted(ModItems.t45_kit, 0, 1, 1, 3),
                    weighted(ModItems.fusion_core, 0, 1, 1, 10),
                    weighted(ModItems.gun_light_revolver, 0, 1, 1, 4),
                    weighted(ModItems.gun_kit_1, 0, 2, 3, 4),
                    weighted(ModItems.ammo_standard, GunFactory.EnumAmmo.M357_FMJ.ordinal(), 1, 24, 4),
                    weighted(ModItems.ammo_standard, GunFactory.EnumAmmo.G40_HE.ordinal(), 1, 6, 3),
                    weighted(ModItems.ammo_standard, GunFactory.EnumAmmo.G26_FLARE_WEAPON.ordinal(), 1, 1, 5),
                    weighted(ModItems.rod, ItemBreedingRod.BreedingRodType.U235.ordinal(), 1, 1, 2),
                    weighted(ModItems.billet_uranium_fuel, 0, 1, 1, 2),
                    weighted(ModItems.ingot_uranium_fuel, 0, 1, 1, 2),
                    weighted(ModItems.bottle_nuka, 0, 1, 3, 6),
                    weighted(ModItems.bottle_quantum, 0, 1, 1, 3),
                    weighted(ModItems.stealth_boy, 0, 1, 1, 7),
                    weighted(ModItems.gas_mask_m65, 0, 1, 1, 5),
                    weighted(ModItems.gas_mask_filter, 0, 1, 1, 5),
                    weighted(ModItems.grenade_nuclear, 0, 1, 2, 2),
                    weighted(ModItems.bomb_caller, 0, 1, 1, 1),
                    weighted(ModItems.bomb_caller, 1, 1, 1, 1),
                    weighted(ModItems.bomb_caller, 2, 1, 1, 2)
            };
        }};

        //spaceship double chests
        new ItemPool(POOL_SPACESHIP) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ModItems.battery_pack, ItemBatteryPack.EnumBatteryPack.BATTERY_LEAD.ordinal(), 1, 1, 2),
                    weighted(ModItems.ingot_advanced_alloy, 0, 2, 16, 5),
                    weighted(ModItems.wire_fine, Mats.MAT_ALLOY.id, 8, 32, 5),
                    weighted(ModItems.coil_advanced_alloy, 0, 2, 16, 5),
                    weighted(ModItems.cell, Fluids.DEUTERIUM.getID(), 1, 8, 5),
                    weighted(ModItems.cell, Fluids.TRITIUM.getID(), 1, 8, 5),
                    weighted(ModItems.cell, Fluids.AMAT.getID(), 1, 1, 1),
                    weighted(ModItems.powder_neodymium, 0, 1, 1, 1),
                    weighted(ModItems.powder_niobium, 0, 1, 1, 1),
                    weighted(ModItems.wire_dense, Mats.MAT_ALLOY.id, 2, 4, 5),
                    weighted(ModItems.wire_dense, Mats.MAT_GOLD.id, 1, 3, 5),
                    weighted(ModBlocks.pwr_fuelrod, 0, 1, 2, 5),
                    weighted(ModBlocks.block_tungsten, 0, 3, 8, 5),
                    weighted(ModBlocks.red_wire_coated, 0, 4, 8, 5),
                    weighted(ModBlocks.red_cable, 0, 8, 16, 5)
            };
        }};
    }
}
