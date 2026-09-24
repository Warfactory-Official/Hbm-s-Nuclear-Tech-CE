package com.hbm.items.armor;

import com.hbm.items.gear.ArmorFSB;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorBismuth;
import com.hbm.render.tileentity.IItemRendererProvider;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ArmorBismuth extends ArmorFSB implements IItemRendererProvider {

  @SideOnly(Side.CLIENT)
  ModelArmorBismuth[] models;

  public ArmorBismuth(
      ArmorMaterial material, int layer, EntityEquipmentSlot slot, String texture, String s) {
    super(material, layer, slot, texture, s);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public ModelBiped getArmorModel(
      EntityLivingBase entityLiving,
      ItemStack itemStack,
      EntityEquipmentSlot armorSlot,
      ModelBiped _default) {

    if (models == null) {
      models = new ModelArmorBismuth[4];

      for (int i = 0; i < 4; i++) models[i] = new ModelArmorBismuth(i);
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
      @Override
      public void renderInventory() {
        setupRenderInv();
      }

      @Override
      public void renderNonInv() {
        setupRenderNonInv();
      }

      @Override
      public void renderCommon() {
        if (armorType == EntityEquipmentSlot.HEAD) {
          GlStateManager.translate(0, -0.5, 0);
          GlStateManager.scale(0.625, 0.625, 0.625);
        }

        if (armorType == EntityEquipmentSlot.CHEST) {
          GlStateManager.scale(0.875, 0.875, 0.875);
        }

        GlStateManager.disableCull();
        renderStandard(
            ResourceManager.armor_bismuth,
            armorType,
            ResourceManager.armor_bismuth_tex,
            ResourceManager.armor_bismuth_tex,
            ResourceManager.armor_bismuth_tex,
            ResourceManager.armor_bismuth_tex,
            "Head",
            "Body",
            "LeftArm",
            "RightArm",
            "LeftLeg",
            "RightLeg",
            "LeftFoot",
            "RightFoot");
        GlStateManager.enableCull();
      }
    };
  }
}
