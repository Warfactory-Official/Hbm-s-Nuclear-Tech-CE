package com.hbm.items.armor;

import com.hbm.items.ModItems;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorBismuth;
import com.hbm.render.tileentity.IItemRendererProvider;
import com.hbm.render.util.ViewModelPositonDebugger;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ArmorBismuth extends ArmorFSB implements IItemRendererProvider {

  @SideOnly(Side.CLIENT)
  ModelArmorBismuth[] models;

  @SideOnly(Side.CLIENT)
  protected ViewModelPositonDebugger offsets;

  @SideOnly(Side.CLIENT)
  protected ViewModelPositonDebugger offsetsHelmet;

  @SideOnly(Side.CLIENT)
  protected ViewModelPositonDebugger offsetsChestplate;

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
        if (armorType == EntityEquipmentSlot.MAINHAND) {
          GlStateManager.translate(0, -0.5, 0);
          GlStateManager.scale(0.625, 0.625, 0.625);
        }

        if (armorType == EntityEquipmentSlot.OFFHAND) {
          GlStateManager.scale(0.875, 0.875, 0.875);
        }

        if (item == ModItems.bismuth_helmet) {
          if (offsetsHelmet == null)
            offsetsHelmet =
                new ViewModelPositonDebugger()
                    .get(ItemCameraTransforms.TransformType.GUI)
                    .setScale(0.6F)
                    .setPosition(-1.2, 3.0, 1.0)
                    .setRotation(255, -36, -143)
                    .getHelper()
                    .get(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND)
                    .setPosition(-1.00, -31.30, -4.95)
                    .setRotation(-23, -139, 85)
                    .getHelper()
                    .get(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND)
                    .setPosition(-0.5, 3, -2.75)
                    .setRotation(610, -115, -100)
                    .getHelper()
                    .get(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND)
                    .setScale(0.7F)
                    .setPosition(-0.25, -3.6, -1.25)
                    .setRotation(5, -90, 340)
                    .getHelper()
                    .get(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND)
                    .setPosition(-8, -5.50, -1.00)
                    .setRotation(0, 330, 180)
                    .getHelper()
                    .get(ItemCameraTransforms.TransformType.GROUND)
                    .setScale(1F)
                    .setPosition(0, 1, 0)
                    .setRotation(0, 0, 180)
                    .getHelper();

          offsetsHelmet.apply(type);
        } else if (item == ModItems.bismuth_plate) {
          if (offsetsChestplate == null)
            offsetsChestplate =
                new ViewModelPositonDebugger()
                    .get(ItemCameraTransforms.TransformType.GUI)
                    .setScale(0.8F)
                    .setPosition(-1.2, 3.0, 1.0)
                    .setRotation(255, -36, -143)
                    .getHelper()
                    .get(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND)
                    .setPosition(-1.00, -31.30, -4.95)
                    .setRotation(-23, -139, 85)
                    .getHelper()
                    .get(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND)
                    .setPosition(-0.5, 3, -2.75)
                    .setRotation(610, -115, -100)
                    .getHelper()
                    .get(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND)
                    .setScale(0.7F)
                    .setPosition(-0.25, -3.6, -1.25)
                    .setRotation(5, -90, 340)
                    .getHelper()
                    .get(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND)
                    .setPosition(-8, -5.50, -1.00)
                    .setRotation(0, 330, 180)
                    .getHelper()
                    .get(ItemCameraTransforms.TransformType.GROUND)
                    .setScale(1F)
                    .setPosition(0, 1, 0)
                    .setRotation(0, 0, 180)
                    .getHelper();

          offsetsChestplate.apply(type);
        } else {
          if (offsets == null)
            offsets =
                new ViewModelPositonDebugger()
                    .get(ItemCameraTransforms.TransformType.GUI)
                    .setScale(1.0F)
                    .setPosition(-1.2, 0.0, 1.0)
                    .setRotation(255, -36, -143)
                    .getHelper()
                    .get(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND)
                    .setPosition(-1.00, -31.30, -4.95)
                    .setRotation(-23, -139, 85)
                    .getHelper()
                    .get(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND)
                    .setPosition(-0.5, 3, -2.75)
                    .setRotation(610, -115, -100)
                    .getHelper()
                    .get(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND)
                    .setScale(0.7F)
                    .setPosition(-0.25, -3.6, -1.25)
                    .setRotation(5, -90, 340)
                    .getHelper()
                    .get(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND)
                    .setPosition(-8, -5.50, -1.00)
                    .setRotation(0, 330, 180)
                    .getHelper()
                    .get(ItemCameraTransforms.TransformType.GROUND)
                    .setScale(1F)
                    .setPosition(0, 1, 0)
                    .setRotation(0, 0, 180)
                    .getHelper();

          offsets.apply(type);
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
