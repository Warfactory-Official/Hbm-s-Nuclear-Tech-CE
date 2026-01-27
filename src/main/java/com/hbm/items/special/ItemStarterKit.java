package com.hbm.items.special;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.ArmorUtil;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBattery;
import com.hbm.items.machine.ItemBreedingRod;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.util.I18nUtil;
import com.hbm.util.ShadyUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;

public class ItemStarterKit extends Item {

    public ItemStarterKit(String s) {
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.maxStackSize = 1;

        ModItems.ALL_ITEMS.add(this);
    }

    private void giveHaz(World world, EntityPlayer p, int tier) {

        for(int i = 0; i < 4; i++) {

            if(!p.inventory.armorInventory.get(i).isEmpty() && !world.isRemote) {
                world.spawnEntity(new EntityItem(world, p.posX, p.posY + p.eyeHeight, p.posZ, p.inventory.armorInventory.get(i)));
            }
        }
        switch(tier) {
            case 0:
                ItemStack mask1 = new ItemStack(ModItems.hazmat_helmet);
                ArmorUtil.installGasMaskFilter(mask1, new ItemStack(ModItems.gas_mask_filter));

                p.inventory.armorInventory.set(3, mask1);
                p.inventory.armorInventory.set(2, new ItemStack(ModItems.hazmat_plate));
                p.inventory.armorInventory.set(1, new ItemStack(ModItems.hazmat_legs));
                p.inventory.armorInventory.set(0, new ItemStack(ModItems.hazmat_boots));
                break;
            case 1:
                ItemStack mask2 = new ItemStack(ModItems.hazmat_helmet_red);
                ArmorUtil.installGasMaskFilter(mask2, new ItemStack(ModItems.gas_mask_filter));

                p.inventory.armorInventory.set(3, mask2);
                p.inventory.armorInventory.set(2, new ItemStack(ModItems.hazmat_plate_red));
                p.inventory.armorInventory.set(1, new ItemStack(ModItems.hazmat_legs_red));
                p.inventory.armorInventory.set(0, new ItemStack(ModItems.hazmat_boots_red));
                break;
            case 2:
                ItemStack mask3 = new ItemStack(ModItems.hazmat_helmet_grey);
                ArmorUtil.installGasMaskFilter(mask3, new ItemStack(ModItems.gas_mask_filter_combo));

                p.inventory.armorInventory.set(3, mask3);
                p.inventory.armorInventory.set(2, new ItemStack(ModItems.hazmat_plate_grey));
                p.inventory.armorInventory.set(1, new ItemStack(ModItems.hazmat_legs_grey));
                p.inventory.armorInventory.set(0, new ItemStack(ModItems.hazmat_boots_grey));
                break;
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if(this == ModItems.nuke_starter_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.ingot_uranium, 32));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.powder_yellowcake, 32));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.template_folder, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_press, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_difurnace_off, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_gascent, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_reactor_breeding, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_assembly_machine, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_chemical_plant, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.reactor_research, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_turbine, 2));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.radaway, 8));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.radx, 2));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.stamp_titanium_flat, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.stamp_titanium_flat, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.stamp_titanium_flat, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.ingot_steel, 64));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.ingot_lead, 64));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.ingot_copper, 64));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.gas_mask_m65, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.geiger_counter, 1));

            giveHaz(world, player, 1);
        }

        if(this == ModItems.nuke_advanced_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.powder_yellowcake, 64));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.powder_plutonium, 64));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.ingot_steel, 64));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.ingot_copper, 64));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.ingot_tungsten, 64));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.ingot_lead, 64));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.ingot_polymer, 64));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_difurnace_off, 3));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_gascent, 3));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_centrifuge, 2));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_uf6_tank, 2));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_puf6_tank, 2));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_reactor_breeding, 2));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.reactor_research, 4));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_turbine, 4));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_radgen, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_rtg_grey, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_assembler, 3));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_chemplant, 2));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_fluidtank, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.pellet_rtg, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.pellet_rtg, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.pellet_rtg, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.pellet_rtg_weak, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.pellet_rtg_weak, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.pellet_rtg_weak, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.cell, 32));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.rod_empty, 32));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.fluid_barrel_full, 4, Fluids.COOLANT.getID()));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.radaway_strong, 4));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.radx, 4));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.pill_iodine, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.geiger_counter, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.survey_scanner, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.gas_mask_m65, 1));

            giveHaz(world, player, 2);
        }

        if(this == ModItems.nuke_commercially_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.reactor_research, 8));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_reactor_breeding, 8));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.machine_fluidtank, 8));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.billet_pu238be, 40));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.ingot_u233, 40));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.ingot_uranium_fuel, 32));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.ingot_plutonium_fuel, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.ingot_mox_fuel, 8));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.inf_water_mk2, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.inf_water_mk2, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.inf_water_mk2, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.rod_empty, 64));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.rod_dual_empty, 64));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.rod_quad_empty, 64));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.fluid_tank_lead_empty, 64));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.fluid_barrel_empty, 64));
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.barrel_steel, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.plate_iron, 64));
            player.inventory.addItemStackToInventory(new ItemStack(Items.DYE, 64));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.template_folder, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.radaway_flush, 8));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.iv_blood, 8));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.pill_iodine, 8));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.gas_mask_filter_combo, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.gas_mask_filter_combo, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.gas_mask_filter_combo, 1));

            giveHaz(world, player, 2);
        }

        if(this == ModItems.grenade_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_generic, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_strong, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_frag, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_fire, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_shrapnel, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_cluster, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_flare, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_electric, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_poison, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_gas, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_cloud, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_pink_cloud, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_smart, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_mirv, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_breach, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_burst, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_pulse, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_plasma, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_tau, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_schrabidium, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_lemon, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_gascan, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_mk2, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_aschrab, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_nuke, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_nuclear, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_zomg, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_black_hole, 16));
        }

        if(this == ModItems.gadget_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(Item.getItemFromBlock(ModBlocks.nuke_gadget), 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.early_explosive_lenses, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.early_explosive_lenses, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.early_explosive_lenses, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.early_explosive_lenses, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.gadget_wireing, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.gadget_core, 1));

            giveHaz(world, player, 0);
        }

        if(this == ModItems.boy_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(Item.getItemFromBlock(ModBlocks.nuke_boy), 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.boy_shielding, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.boy_target, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.boy_bullet, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.boy_propellant, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.boy_igniter, 1));

            giveHaz(world, player, 0);
        }

        if(this == ModItems.man_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(Item.getItemFromBlock(ModBlocks.nuke_man), 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.early_explosive_lenses, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.early_explosive_lenses, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.early_explosive_lenses, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.early_explosive_lenses, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.man_igniter, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.man_core, 1));

            giveHaz(world, player, 0);
        }

        if(this == ModItems.mike_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(Item.getItemFromBlock(ModBlocks.nuke_mike), 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.explosive_lenses, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.explosive_lenses, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.explosive_lenses, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.explosive_lenses, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.man_core, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.mike_core, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.mike_deut, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.mike_cooling_unit, 1));

            giveHaz(world, player, 1);
        }

        if(this == ModItems.tsar_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(Item.getItemFromBlock(ModBlocks.nuke_tsar), 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.explosive_lenses, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.explosive_lenses, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.explosive_lenses, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.explosive_lenses, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.man_core, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.mike_core, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.mike_deut, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.mike_core, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.mike_deut, 1));

            giveHaz(world, player, 2);
        }

        if(this == ModItems.multi_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(Item.getItemFromBlock(ModBlocks.bomb_multi), 6));
            player.inventory.addItemStackToInventory(new ItemStack(Item.getItemFromBlock(Blocks.TNT), 26));
            player.inventory.addItemStackToInventory(new ItemStack(Items.GUNPOWDER, 2));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.pellet_cluster, 2));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.powder_fire, 2));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.powder_poison, 2));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.pellet_gas, 2));
        }

        if(this == ModItems.custom_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.nuke_custom));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_tnt, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_tnt, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_tnt, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_tnt, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_tnt, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_tnt, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_nuke, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_nuke, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_nuke, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_nuke, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_hydro, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_hydro, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_amat, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_amat, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_dirty, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_dirty, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_dirty, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_schrab, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.custom_fall, 1));
        }

        if(this == ModItems.missile_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(Item.getItemFromBlock(ModBlocks.launch_pad), 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.designator, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.designator_range, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.designator_manual, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.battery_schrabidium_cell_4, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_generic, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_strong, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_burst, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_incendiary, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_incendiary_strong, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_inferno, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_cluster, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_cluster_strong, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_rain, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_buster, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_buster_strong, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_drill, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_nuclear, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_nuclear_cluster, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_volcano, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_doomsday, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_taint, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_micro, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_bhole, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_schrabidium, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.missile_emp, 1));
        }

        if(this == ModItems.t45_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.t51_helmet, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.t51_plate, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.t51_legs, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.t51_boots, 1));
            player.inventory.addItemStackToInventory(ItemBattery.getFullBattery(ModItems.fusion_core));
            player.inventory.addItemStackToInventory(ItemBattery.getFullBattery(ModItems.fusion_core));
            player.inventory.addItemStackToInventory(ItemBattery.getFullBattery(ModItems.fusion_core));
            player.inventory.addItemStackToInventory(ItemBattery.getFullBattery(ModItems.fusion_core));
            player.inventory.addItemStackToInventory(ItemBattery.getFullBattery(ModItems.fusion_core));
            player.inventory.addItemStackToInventory(ItemBattery.getFullBattery(ModItems.fusion_core));
            player.inventory.addItemStackToInventory(ItemBattery.getFullBattery(ModItems.fusion_core));
        }

        if(this == ModItems.grenade_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_generic, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_strong, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_frag, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_fire, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_shrapnel, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_cluster, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_flare, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_electric, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_poison, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_gas, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_cloud, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_pink_cloud, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_smart, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_mirv, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_breach, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_burst, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_pulse, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_plasma, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_tau, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_schrabidium, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_lemon, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_gascan, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_mk2, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_aschrab, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_nuke, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_nuclear, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_zomg, 16));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.grenade_black_hole, 16));
        }

        if(this == ModItems.fleija_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(Item.getItemFromBlock(ModBlocks.nuke_fleija), 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.fleija_igniter, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.fleija_igniter, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.fleija_propellant, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.fleija_propellant, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.fleija_propellant, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.fleija_core, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.fleija_core, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.fleija_core, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.fleija_core, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.fleija_core, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.fleija_core, 1));

            giveHaz(world, player, 2);
        }

        if(this == ModItems.solinium_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(Item.getItemFromBlock(ModBlocks.nuke_solinium), 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.solinium_igniter, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.solinium_igniter, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.solinium_igniter, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.solinium_igniter, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.solinium_propellant, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.solinium_propellant, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.solinium_propellant, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.solinium_propellant, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.solinium_core, 1));

            giveHaz(world, player, 1);
        }

        if(this == ModItems.balefire_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(Item.getItemFromBlock(ModBlocks.nuke_fstbmb), 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.egg_balefire, 1));
            player.inventory.addItemStackToInventory(ItemBattery.getFullBattery(ModItems.battery_spark));

            giveHaz(world, player, 2);
        }

        if(this == ModItems.prototype_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(Item.getItemFromBlock(ModBlocks.nuke_prototype), 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.igniter, 1));
            for(int i = 0; i < 4; i ++)
                player.inventory.addItemStackToInventory(ItemCell.getFullCell(Fluids.SAS3));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.rod_quad, 4, ItemBreedingRod.BreedingRodType.URANIUM.ordinal()));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.rod_quad, 4, ItemBreedingRod.BreedingRodType.LEAD.ordinal()));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.rod_quad, 2, ItemBreedingRod.BreedingRodType.NP237.ordinal()));

            giveHaz(world, player, 2);
        }
        if(this == ModItems.hazmat_kit)
        {
            giveHaz(world, player, 0);
        }

        if(this == ModItems.hazmat_red_kit)
        {
            giveHaz(world, player, 1);
        }

        if(this == ModItems.hazmat_grey_kit)
        {
            giveHaz(world, player, 2);
        }

        if(this == ModItems.stealth_boy)
        {
            player.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 30 * 20, 1, false, false));
        }

        if(this == ModItems.euphemium_kit)
        {
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.euphemium_helmet, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.euphemium_plate, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.euphemium_legs, 1));
            player.inventory.addItemStackToInventory(new ItemStack(ModItems.euphemium_boots, 1));
            player.inventory.addItemStackToInventory(new ItemStack(Item.getItemFromBlock(ModBlocks.statue_elb_f), 1));
        }

        if(this == ModItems.letter && world.isRemote)
        {
            if(player.getUniqueID().equals(ShadyUtil.a20)) {
                player.sendMessage(new TextComponentTranslation("chat.letter.a20"));
            } else {
                player.sendMessage(new TextComponentTranslation("chat.letter.open"));
            }
        }

        world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.itemUnpack, SoundCategory.PLAYERS, 1.0F, 1.0F);
        stack.shrink(1);
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if(this == ModItems.gadget_kit ||
                this == ModItems.boy_kit ||
                this == ModItems.man_kit ||
                this == ModItems.mike_kit ||
                this == ModItems.tsar_kit ||
                this == ModItems.prototype_kit ||
                this == ModItems.fleija_kit ||
                this == ModItems.solinium_kit ||
                this == ModItems.balefire_kit ||
                this == ModItems.grenade_kit ||
                this == ModItems.missile_kit ||
                this == ModItems.t45_kit ||
                this == ModItems.multi_kit) {
            tooltip.add(I18nUtil.resolveKey("desc.kit.inventory"));
        }
        if(this == ModItems.nuke_starter_kit ||
                this == ModItems.nuke_advanced_kit ||
                this == ModItems.gadget_kit ||
                this == ModItems.boy_kit ||
                this == ModItems.man_kit ||
                this == ModItems.mike_kit ||
                this == ModItems.tsar_kit ||
                this == ModItems.prototype_kit ||
                this == ModItems.fleija_kit ||
                this == ModItems.solinium_kit ||
                this == ModItems.balefire_kit ||
                this == ModItems.hazmat_kit ||
                this == ModItems.hazmat_red_kit ||
                this == ModItems.hazmat_grey_kit) {
            tooltip.add(I18nUtil.resolveKey("desc.kit.armor"));
        }
    }
}
