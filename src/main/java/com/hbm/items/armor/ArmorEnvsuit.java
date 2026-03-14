package com.hbm.items.armor;

import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorEnvsuit;
import com.hbm.render.tileentity.IItemRendererProvider;
import com.hbm.render.util.ViewModelPositonDebugger;
import com.hbm.util.Vec3NT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ArmorEnvsuit extends ArmorFSBPowered implements IItemRendererProvider {

  @SideOnly(Side.CLIENT)
  ModelArmorEnvsuit[] models;

  @SideOnly(Side.CLIENT)
  protected ViewModelPositonDebugger offsets;

  public ArmorEnvsuit(
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
      models = new ModelArmorEnvsuit[4];

      for (int i = 0; i < 4; i++) models[i] = new ModelArmorEnvsuit(i);
    }

    return models[3 - armorSlot.getIndex()];
  }

  private static final UUID speed = UUID.fromString("6ab858ba-d712-485c-bae9-e5e765fc555a");

  @Override
  public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {

    super.onArmorTick(world, player, stack);

    if (this != ModItems.envsuit_plate) return;

    /// SPEED ///
    Multimap<String, AttributeModifier> multimap =
        super.getAttributeModifiers(EntityEquipmentSlot.CHEST, stack);
    multimap.put(
        SharedMonsterAttributes.MOVEMENT_SPEED.getName(),
        new AttributeModifier(speed, "SQUIRREL SPEED", 0.1, 0));
    player.getAttributeMap().removeAttributeModifiers(multimap);

    if (hasFSBArmor(player)) {

      if (player.isSprinting()) player.getAttributeMap().applyAttributeModifiers(multimap);

      if (player.isInWater()) {

        if (!world.isRemote) {
          player.setAir(300);
          player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 15 * 20, 0));
        }

        double mo = 0.1 * player.moveForward;
        Vec3NT vec = new Vec3NT(player.getLookVec());
        vec.setX(vec.x * mo);
        vec.setY(vec.y * mo);
        vec.setZ(vec.z * mo);

        player.motionX += vec.x;
        player.motionY += vec.y;
        player.motionZ += vec.z;
      } else {
        boolean canRemoveNightVision = true;
        ItemStack helmet = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        ItemStack helmetMod =
            ArmorModHandler.pryMod(helmet, ArmorModHandler.helmet_only); // Get the modification!
        if (!helmetMod.isEmpty() && helmetMod.getItem() instanceof ItemModNightVision) {
          canRemoveNightVision = false;
        }

        if (!world.isRemote && canRemoveNightVision) {
          player.removePotionEffect(MobEffects.NIGHT_VISION);
        }
      }
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
        if (armorType == EntityEquipmentSlot.MAINHAND) {
          GlStateManager.scale(0.3125, 0.3125, 0.3125);
          GlStateManager.translate(0, 1, 0);
          Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.envsuit_helmet);
          ResourceManager.armor_envsuit.renderPart("Helmet");
          GlStateManager.disableLighting();
          GlStateManager.disableTexture2D();
          GlStateManager.color(1F, 1F, 0.8F);
          ResourceManager.armor_envsuit.renderPart("Lamps");
          GlStateManager.color(1F, 1F, 1F);
          GlStateManager.enableTexture2D();
          GlStateManager.enableLighting();
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

          renderStandard(
              ResourceManager.armor_envsuit,
              armorType,
              ResourceManager.envsuit_helmet,
              ResourceManager.envsuit_chest,
              ResourceManager.envsuit_arm,
              ResourceManager.envsuit_leg,
              "Helmet,Lamps",
              "Chest",
              "LeftArm",
              "RightArm",
              "LeftLeg",
              "RightLeg",
              "LeftFoot",
              "RightFoot");
        }
      }
    };
  }
}
