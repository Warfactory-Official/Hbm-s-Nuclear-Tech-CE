package com.hbm.items.armor;

import com.hbm.items.gear.ArmorFSB;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorTaurun;
import com.hbm.render.tileentity.IItemRendererProvider;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class ArmorTaurun extends ArmorFSB implements IItemRendererProvider {

  @SideOnly(Side.CLIENT)
  ModelArmorTaurun[] models;

  public ArmorTaurun(
      ArmorMaterial material, int layer, EntityEquipmentSlot slot, String texture, String s) {
    super(material, layer, slot, texture, s);
    this.setMaxDamage(0);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public ModelBiped getArmorModel(
      @NotNull EntityLivingBase entityLiving,
      @NotNull ItemStack itemStack,
      @NotNull EntityEquipmentSlot armorSlot,
      @NotNull ModelBiped _default) {

    if (models == null) {
      models = new ModelArmorTaurun[4];

      for (int i = 0; i < 4; i++) models[i] = new ModelArmorTaurun(i);
    }

    return models[3 - armorSlot.getIndex()];
  }

  @Override
  public Item getItemForRenderer() {
    return this;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        if (armorType == EntityEquipmentSlot.HEAD) GlStateManager.translate(0, 1, 0);
        if (armorType == EntityEquipmentSlot.CHEST) GlStateManager.translate(0, 1.5, 0);
        setupRenderInv();
      }

      public void renderNonInv() {
        setupRenderNonInv();
      }

      public void renderCommon() {
        renderStandard(
            ResourceManager.armor_taurun,
            armorType,
            ResourceManager.taurun_helmet,
            ResourceManager.taurun_chest,
            ResourceManager.taurun_arm,
            ResourceManager.taurun_leg,
            "Helmet",
            "Chest",
            "LeftArm",
            "RightArm",
            "LeftLeg",
            "RightLeg",
            "LeftBoot",
            "RightBoot");
      }
    };
  }
}
