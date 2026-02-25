package com.hbm.items.armor;

import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorDigamma;
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
import org.jetbrains.annotations.NotNull;

public class ArmorDigamma extends ArmorFSBPowered implements IItemRendererProvider {

  @SideOnly(Side.CLIENT)
  ModelArmorDigamma[] models;

  @SideOnly(Side.CLIENT)
  protected ViewModelPositonDebugger offsets;

  @SideOnly(Side.CLIENT)
  protected ViewModelPositonDebugger offsetsHelmet;

  @SideOnly(Side.CLIENT)
  protected ViewModelPositonDebugger offsetsChestplate;

  public ArmorDigamma(
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
      models = new ModelArmorDigamma[4];

      for (int i = 0; i < 4; i++) models[i] = new ModelArmorDigamma(i);
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
        if (armorType == EntityEquipmentSlot.MAINHAND) {
          GlStateManager.scale(0.875, 0.875, 0.875);
          GlStateManager.translate(0, -2, 0);
        }
        setupRenderInv();
      }

      public void renderNonInv() {
        setupRenderNonInv();
      }

      public void renderCommon() {
        if (item == ModItems.fau_helmet) {
          if (offsetsHelmet == null)
            offsetsHelmet =
                new ViewModelPositonDebugger()
                    .get(ItemCameraTransforms.TransformType.GUI)
                    .setScale(0.8F)
                    .setPosition(-1.2, 0.0, 1.6)
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
        } else if (item == ModItems.fau_plate) {
          if (offsetsChestplate == null)
            offsetsChestplate =
                new ViewModelPositonDebugger()
                    .get(ItemCameraTransforms.TransformType.GUI)
                    .setScale(0.8F)
                    .setPosition(-1.2, 0.0, 1.6)
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

        renderStandard(
            ResourceManager.armor_fau,
            armorType,
            ResourceManager.fau_helmet,
            ResourceManager.fau_chest,
            ResourceManager.fau_arm,
            ResourceManager.fau_leg,
            "Head",
            "Body,Cassette",
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
