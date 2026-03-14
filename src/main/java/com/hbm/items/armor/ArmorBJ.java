package com.hbm.items.armor;

import com.hbm.items.ModItems;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorBJ;
import com.hbm.render.tileentity.IItemRendererProvider;
import com.hbm.render.util.ViewModelPositonDebugger;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import static com.hbm.render.NTMRenderHelper.bindTexture;

public class ArmorBJ extends ArmorFSBPowered implements IItemRendererProvider {

  public ArmorBJ(
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

  @SideOnly(Side.CLIENT)
  ModelArmorBJ[] models;

  @SideOnly(Side.CLIENT)
  protected ViewModelPositonDebugger offsets;

  @SideOnly(Side.CLIENT)
  protected ViewModelPositonDebugger offsetsChestplate;

  @SideOnly(Side.CLIENT)
  protected ViewModelPositonDebugger offsetsChestplateJetpack;

  @Override
  @SideOnly(Side.CLIENT)
  public ModelBiped getArmorModel(
      @NotNull EntityLivingBase entityLiving,
      @NotNull ItemStack itemStack,
      @NotNull EntityEquipmentSlot armorSlot,
      @NotNull ModelBiped _default) {
    if (models == null) {
      models = new ModelArmorBJ[4];

      for (int i = 0; i < 4; i++) models[i] = new ModelArmorBJ(i);
    }
    return models[3 - armorSlot.getIndex()];
  }

  @Override
  public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
    super.onArmorTick(world, player, itemStack);

    if (this == ModItems.bj_helmet
        && ArmorFSB.hasFSBArmorIgnoreCharge(player)
        && !ArmorFSB.hasFSBArmor(player)) {

      ItemStack helmet = player.inventory.armorInventory.get(3);

      if (!player.inventory.addItemStackToInventory(helmet)) player.dropItem(helmet, false);

      player.inventory.armorInventory.set(3, ItemStack.EMPTY);

      player.attackEntityFrom(ModDamageSource.lunar, 1000);
    }
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
        if (armorType == EntityEquipmentSlot.OFFHAND) {
          if (ArmorBJ.this == ModItems.bj_plate_jetpack) {
            GlStateManager.scale(0.6875, 0.6875, 0.6875);
          } else {
            GlStateManager.scale(0.875, 0.875, 0.875);
          }
        }

        if (item == ModItems.bj_plate) {
          if (offsetsChestplate == null)
            offsetsChestplate =
                new ViewModelPositonDebugger()
                    .get(ItemCameraTransforms.TransformType.GUI)
                    .setScale(0.9F)
                    .setPosition(-1.3, 0.0, 1.0)
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
        } else if (item == ModItems.bj_plate_jetpack) {
          if (offsetsChestplateJetpack == null)
            offsetsChestplateJetpack =
                new ViewModelPositonDebugger()
                    .get(ItemCameraTransforms.TransformType.GUI)
                    .setScale(0.67F)
                    .setPosition(-1.6, 0.0, 1.0)
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

          offsetsChestplateJetpack.apply(type);
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
            ResourceManager.armor_bj,
            armorType,
            ResourceManager.bj_eyepatch,
            ResourceManager.bj_chest,
            ResourceManager.bj_arm,
            ResourceManager.bj_leg,
            "Head",
            "Body",
            "LeftArm",
            "RightArm",
            "LeftLeg",
            "RightLeg",
            "LeftFoot",
            "RightFoot");

        if (ArmorBJ.this == ModItems.bj_plate_jetpack) {
          GlStateManager.translate(0, 0, -0.1);
          bindTexture(ResourceManager.bj_jetpack);
          ResourceManager.armor_bj.renderPart("Jetpack");
        }
      }
    };
  }
}
