package com.hbm.items.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorAJRO;
import com.hbm.render.tileentity.IItemRendererProvider;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class ArmorAJRO extends ArmorFSBPowered implements IItemRendererProvider {

  @SideOnly(Side.CLIENT)
  ModelArmorAJRO[] models;

  public ArmorAJRO(
      ArmorMaterial material,
      int layer,
      EntityEquipmentSlot slot,
      String texture,
      long maxPower,
      long chargeRate,
      long consumption,
      long drain,
      String s) {
    super(material, layer, slot, texture, maxPower, chargeRate, consumption, drain, s);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public ModelBiped getArmorModel(
      @NotNull EntityLivingBase entityLiving,
      @NotNull ItemStack itemStack,
      @NotNull EntityEquipmentSlot armorSlot,
      @NotNull ModelBiped _default) {
    if (models == null) {
      models = new ModelArmorAJRO[4];

      for (int i = 0; i < 4; i++) models[i] = new ModelArmorAJRO(i);
    }

    return models[armorSlot.getIndex()];
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
        setupRenderInv();
      }

      public void renderNonInv() {
        setupRenderNonInv();
      }

      public void renderCommon() {
        renderStandard(
            ResourceManager.armor_ajr,
            armorType,
            ResourceManager.ajro_helmet,
            ResourceManager.ajro_chest,
            ResourceManager.ajro_arm,
            ResourceManager.ajro_leg,
            "Head",
            "Body",
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
