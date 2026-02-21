package com.hbm.main;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;

public final class MaterialRegistry {

    // Armor Materials
    // Drillgon200: I have no idea what the two strings and the number at the
    // end are.
    public static ArmorMaterial enumArmorMaterialT51;
    public static ArmorMaterial aMatBJ;
    public static ArmorMaterial aMatAJR;
    public static ArmorMaterial aMatSteamsuit;
    public static ArmorMaterial aMatDieselsuit;
    public static ArmorMaterial aMatTrench;
    public static ArmorMaterial aMatTaurun;
    public static ArmorMaterial aMatBismuth;
    public static ArmorMaterial aMatZirconium;
    public static ArmorMaterial aMatDNT;
    public static ArmorMaterial aMatEnvsuit;
    public static ArmorMaterial aMatRPA;
    public static ArmorMaterial aMatHEV;
    public static ArmorMaterial aMatHaz;
    public static ArmorMaterial aMatHaz2;
    public static ArmorMaterial aMatHaz3;
    public static ArmorMaterial aMatPaa;
    public static ArmorMaterial aMatSchrab;
    public static ArmorMaterial aMatEuph;
    public static ArmorMaterial aMatSteel;
    public static ArmorMaterial aMatAlloy;
    public static ArmorMaterial aMatAus3;
    public static ArmorMaterial aMatTitan;
    public static ArmorMaterial aMatCMB;
    public static ArmorMaterial aMatSecurity;
    public static ArmorMaterial aMatAsbestos;
    public static ArmorMaterial aMatCobalt;
    public static ArmorMaterial aMatStarmetal;
    public static ArmorMaterial aMatLiquidator;
    public static ArmorMaterial aMatFau;
    public static ArmorMaterial aMatDNS;
    // Tool Materials
    public static ToolMaterial enumToolMaterialSchrabidium;
    public static ToolMaterial enumToolMaterialHammer;
    public static ToolMaterial enumToolMaterialChainsaw;
    public static ToolMaterial enumToolMaterialSteel;
    public static ToolMaterial enumToolMaterialTitanium;
    public static ToolMaterial enumToolMaterialAlloy;
    public static ToolMaterial enumToolMaterialCmb;
    public static ToolMaterial enumToolMaterialElec;
    public static ToolMaterial enumToolMaterialDesh;
    public static ToolMaterial enumToolMaterialCobalt;
    public static ToolMaterial enumToolMaterialSaw;
    public static ToolMaterial enumToolMaterialBat;
    public static ToolMaterial enumToolMaterialBatNail;
    public static ToolMaterial enumToolMaterialGolfClub;
    public static ToolMaterial enumToolMaterialPipeRusty;
    public static ToolMaterial enumToolMaterialPipeLead;
    public static ToolMaterial enumToolMaterialBottleOpener;
    public static ToolMaterial enumToolMaterialSledge;
    public static ToolMaterial enumToolMaterialMultitool;
    public static ToolMaterial matMeteorite;
    public static ToolMaterial matCrucible;
    public static ToolMaterial matHS;
    public static ToolMaterial matHF;

    public static void init() {
         enumArmorMaterialT51 = EnumHelper.addArmorMaterial(Tags.MODID + ":T51", Tags.MODID + ":T51", 150, new int[]{3, 8, 6, 3}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatBJ = EnumHelper.addArmorMaterial(Tags.MODID + ":BLACKJACK", Tags.MODID + ":HBM_BLACKJACK", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatAJR = EnumHelper.addArmorMaterial(Tags.MODID + ":T45AJR", Tags.MODID + ":T45AJR", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatSteamsuit = EnumHelper.addArmorMaterial(Tags.MODID + ":Steamsuit", Tags.MODID + ":Steamsuit", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatDieselsuit = EnumHelper.addArmorMaterial(Tags.MODID + ":Dieselsuit", Tags.MODID + ":Dieselsuit", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatTrench = EnumHelper.addArmorMaterial(Tags.MODID + ":Trenchmaster", Tags.MODID + ":Trenchmaster", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatTaurun = EnumHelper.addArmorMaterial(Tags.MODID + ":Taurun", Tags.MODID + ":Taurun", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatBismuth = EnumHelper.addArmorMaterial(Tags.MODID + ":Bismuth", Tags.MODID + ":Bismuth", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatZirconium = EnumHelper.addArmorMaterial(Tags.MODID + ":Zirconium", Tags.MODID + ":Zirconium", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatDNT = EnumHelper.addArmorMaterial(Tags.MODID + ":DNT", Tags.MODID + ":DNT", 3, new int[]{1, 1, 1, 1}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
         aMatEnvsuit = EnumHelper.addArmorMaterial(Tags.MODID + ":Envsuit", Tags.MODID + ":Envsuit", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatRPA = EnumHelper.addArmorMaterial(Tags.MODID + ":RPA", Tags.MODID + ":RPA", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatHEV = EnumHelper.addArmorMaterial(Tags.MODID + ":HEV", Tags.MODID + ":HEV", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatHaz = EnumHelper.addArmorMaterial(Tags.MODID + ":HAZMAT", Tags.MODID + ":HAZMAT", 60, new int[]{1, 4, 5, 2}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
         aMatHaz2 = EnumHelper.addArmorMaterial(Tags.MODID + ":HAZMAT2", Tags.MODID + ":HAZMAT2", 60, new int[]{1, 4, 5, 2}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
         aMatHaz3 = EnumHelper.addArmorMaterial(Tags.MODID + ":HAZMAT3", Tags.MODID + ":HAZMAT3", 60, new int[]{1, 4, 5, 2}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
         aMatPaa = EnumHelper.addArmorMaterial(Tags.MODID + ":PAA", Tags.MODID + ":PAA", 75, new int[]{3, 6, 8, 3}, 25, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatSchrab = EnumHelper.addArmorMaterial(Tags.MODID + ":SCHRABIDIUM", Tags.MODID + ":SCHRABIDIUM", 100, new int[]{3, 6, 8, 3}, 50, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatEuph = EnumHelper.addArmorMaterial(Tags.MODID + ":EUPHEMIUM", Tags.MODID + ":EUPHEMIUM", 15000000, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatSteel = EnumHelper.addArmorMaterial(Tags.MODID + ":STEEL", Tags.MODID + ":STEEL", 20, new int[]{2, 5, 6, 2}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
         aMatAlloy = EnumHelper.addArmorMaterial(Tags.MODID + ":ALLOY", Tags.MODID + ":ALLOY", 40, new int[]{3, 6, 8, 3}, 12, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
         aMatAus3 = EnumHelper.addArmorMaterial(Tags.MODID + ":AUSIII", Tags.MODID + ":AUSIII", 375, new int[]{2, 5, 6, 2}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
         aMatTitan = EnumHelper.addArmorMaterial(Tags.MODID + ":TITANIUM", Tags.MODID + ":TITANIUM", 25, new int[]{3, 6, 8, 3}, 9, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatCMB = EnumHelper.addArmorMaterial(Tags.MODID + ":CMB", Tags.MODID + ":CMB", 60, new int[]{3, 6, 8, 3}, 50, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatSecurity = EnumHelper.addArmorMaterial(Tags.MODID + ":SECURITY", Tags.MODID + ":SECURITY", 100, new int[]{3, 6, 8, 3}, 15, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatAsbestos = EnumHelper.addArmorMaterial(Tags.MODID + ":ASBESTOS", Tags.MODID + ":ASBESTOS", 20, new int[]{1, 3, 4, 1}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
         aMatCobalt = EnumHelper.addArmorMaterial(Tags.MODID + ":COBALT", Tags.MODID + ":COBALT", 70, new int[]{3, 6, 8, 3}, 25, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatStarmetal = EnumHelper.addArmorMaterial(Tags.MODID + ":STARMETAL", Tags.MODID + ":STARMETAL", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatLiquidator = EnumHelper.addArmorMaterial(Tags.MODID + ":LIQUIDATOR", Tags.MODID + ":LIQUIDATOR", 750, new int[]{3, 6, 8, 3}, 10, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatFau = EnumHelper.addArmorMaterial(Tags.MODID + ":DIGAMMA", Tags.MODID + ":DIGAMMA", 150, new int[]{3, 8, 6, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
         aMatDNS = EnumHelper.addArmorMaterial(Tags.MODID + ":DNT_NANO", Tags.MODID + ":DNT_NANO", 150, new int[]{3, 8, 6, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);

         enumToolMaterialSchrabidium = EnumHelper.addToolMaterial(Tags.MODID + ":SCHRABIDIUM", 4, 10000, 50.0F, 100.0F, 200);
         enumToolMaterialHammer = EnumHelper.addToolMaterial(Tags.MODID + ":SCHRABIDIUMHAMMER", 3, 0, 50.0F, 999999996F, 200);
         enumToolMaterialChainsaw = EnumHelper.addToolMaterial(Tags.MODID + ":CHAINSAW", 3, 1500, 50.0F, 22.0F, 0);
         enumToolMaterialSteel = EnumHelper.addToolMaterial(Tags.MODID + ":STEEL", 2, 500, 7.5F, 2.0F, 10);
         enumToolMaterialTitanium = EnumHelper.addToolMaterial(Tags.MODID + ":TITANIUM", 2, 750, 9.0F, 2.5F, 15);
         enumToolMaterialAlloy = EnumHelper.addToolMaterial(Tags.MODID + ":ALLOY", 3, 2000, 15.0F, 5.0F, 5);
         enumToolMaterialCmb = EnumHelper.addToolMaterial(Tags.MODID + ":CMB", 4, 8500, 40.0F, 55F, 100);
         enumToolMaterialElec = EnumHelper.addToolMaterial(Tags.MODID + ":ELEC", 2, 0, 30.0F, 12.0F, 2);
         enumToolMaterialDesh = EnumHelper.addToolMaterial(Tags.MODID + ":DESH", 2, 0, 7.5F, 2.0F, 10);
         enumToolMaterialCobalt = EnumHelper.addToolMaterial(Tags.MODID + ":COBALT", 4, 750, 9.0F, 2.5F, 15);
         enumToolMaterialSaw = EnumHelper.addToolMaterial(Tags.MODID + ":SAW", 2, 750, 2.0F, 3.5F, 25);
         enumToolMaterialBat = EnumHelper.addToolMaterial(Tags.MODID + ":BAT", 0, 500, 1.5F, 3F, 25);
         enumToolMaterialBatNail = EnumHelper.addToolMaterial(Tags.MODID + ":BATNAIL", 0, 450, 1.0F, 4F, 25);
         enumToolMaterialGolfClub = EnumHelper.addToolMaterial(Tags.MODID + ":GOLFCLUB", 1, 1000, 2.0F, 5F, 25);
         enumToolMaterialPipeRusty = EnumHelper.addToolMaterial(Tags.MODID + ":PIPERUSTY", 1, 350, 1.5F, 4.5F, 25);
         enumToolMaterialPipeLead = EnumHelper.addToolMaterial(Tags.MODID + ":PIPELEAD", 1, 250, 1.5F, 5.5F, 25);
         enumToolMaterialBottleOpener = EnumHelper.addToolMaterial(Tags.MODID + ":OPENER", 1, 250, 1.5F, 0.5F, 200);
         enumToolMaterialSledge = EnumHelper.addToolMaterial(Tags.MODID + ":SHIMMERSLEDGE", 1, 0, 25.0F, 26F, 200);
         enumToolMaterialMultitool = EnumHelper.addToolMaterial(Tags.MODID + ":MULTITOOL", 3, 5000, 25F, 5.5F, 25);
         matMeteorite = EnumHelper.addToolMaterial("HBM_METEORITE", 4, 0, 50F, 0.0F, 200);
         matCrucible = EnumHelper.addToolMaterial("CRUCIBLE", 3, 10000, 50.0F, 100.0F, 200);
         matHS = EnumHelper.addToolMaterial("CRUCIBLE", 3, 10000, 50.0F, 100.0F, 200);
         matHF = EnumHelper.addToolMaterial("CRUCIBLE", 3, 10000, 50.0F, 100.0F, 200);
    }

   public static void initFixMaterials() {
       MaterialRegistry.aMatSchrab.setRepairItem(new ItemStack(ModItems.ingot_schrabidium));
       MaterialRegistry.aMatHaz.setRepairItem(new ItemStack(ModItems.hazmat_cloth));
       MaterialRegistry.aMatHaz2.setRepairItem(new ItemStack(ModItems.hazmat_cloth_red));
       MaterialRegistry.aMatHaz3.setRepairItem(new ItemStack(ModItems.hazmat_cloth_grey));
       MaterialRegistry.aMatBJ.setRepairItem(new ItemStack(ModItems.plate_armor_lunar));
       MaterialRegistry.aMatAJR.setRepairItem(new ItemStack(ModItems.plate_armor_ajr));
       MaterialRegistry.aMatHEV.setRepairItem(new ItemStack(ModItems.plate_armor_hev));
       MaterialRegistry.aMatTitan.setRepairItem(new ItemStack(ModItems.ingot_titanium));
       MaterialRegistry.aMatSteel.setRepairItem(new ItemStack(ModItems.ingot_steel));
       MaterialRegistry.aMatAlloy.setRepairItem(new ItemStack(ModItems.ingot_advanced_alloy));
       MaterialRegistry.aMatPaa.setRepairItem(new ItemStack(ModItems.plate_paa));
       MaterialRegistry.aMatCMB.setRepairItem(new ItemStack(ModItems.ingot_combine_steel));
       MaterialRegistry.aMatAus3.setRepairItem(new ItemStack(ModItems.ingot_australium));
       MaterialRegistry.aMatSecurity.setRepairItem(new ItemStack(ModItems.plate_kevlar));
       MaterialRegistry.enumToolMaterialSchrabidium.setRepairItem(new ItemStack(ModItems.ingot_schrabidium));
       MaterialRegistry.enumToolMaterialHammer.setRepairItem(new ItemStack(Item.getItemFromBlock(ModBlocks.block_schrabidium)));
       MaterialRegistry.enumToolMaterialChainsaw.setRepairItem(new ItemStack(ModItems.ingot_steel));
       MaterialRegistry.enumToolMaterialTitanium.setRepairItem(new ItemStack(ModItems.ingot_titanium));
       MaterialRegistry.enumToolMaterialSteel.setRepairItem(new ItemStack(ModItems.ingot_steel));
       MaterialRegistry.enumToolMaterialAlloy.setRepairItem(new ItemStack(ModItems.ingot_advanced_alloy));
       MaterialRegistry.enumToolMaterialCmb.setRepairItem(new ItemStack(ModItems.ingot_combine_steel));
       MaterialRegistry.enumToolMaterialBottleOpener.setRepairItem(new ItemStack(ModItems.plate_steel));
       MaterialRegistry.enumToolMaterialDesh.setRepairItem(new ItemStack(ModItems.ingot_desh));
       MaterialRegistry.aMatAsbestos.setRepairItem(new ItemStack(ModItems.asbestos_cloth));
       MaterialRegistry.matMeteorite.setRepairItem(new ItemStack(ModItems.plate_paa));
       MaterialRegistry.aMatLiquidator.setRepairItem(new ItemStack(ModItems.plate_lead));
       MaterialRegistry.aMatFau.setRepairItem(new ItemStack(ModItems.plate_armor_fau));
       MaterialRegistry.aMatDNS.setRepairItem(new ItemStack(ModItems.plate_armor_dnt));
   }
}
