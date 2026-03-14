package com.hbm.items.armor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorDiesel;
import com.hbm.render.tileentity.IItemRendererProvider;
import com.hbm.render.util.ViewModelPositonDebugger;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class ArmorDiesel extends ArmorFSBFueled implements IItemRendererProvider {

  @SideOnly(Side.CLIENT)
  ModelArmorDiesel[] models;

  @SideOnly(Side.CLIENT)
  protected ViewModelPositonDebugger offsets;

  @SideOnly(Side.CLIENT)
  protected ViewModelPositonDebugger offsetsHelmet;

  public ArmorDiesel(
      ArmorMaterial material,
      int layer,
      EntityEquipmentSlot slot,
      String texture,
      FluidType fuelType,
      int maxFuel,
      int fillRate,
      int consumption,
      int drain,
      String s) {
    super(material, layer, slot, texture, fuelType, maxFuel, fillRate, consumption, drain, s);
  }

  @Override
  public @NotNull Multimap<String, AttributeModifier> getItemAttributeModifiers(
      @NotNull EntityEquipmentSlot slot) {
    Multimap<String, AttributeModifier> multimap = HashMultimap.create();

    if (slot == this.armorType) {
      multimap.put(
          SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getName(),
          new AttributeModifier(
              ArmorModHandler.fixedUUIDs[this.armorType.getIndex()], "Armor modifier", -0.025D, 1));
    }

    return multimap;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public ModelBiped getArmorModel(
      @NotNull EntityLivingBase entityLiving,
      @NotNull ItemStack itemStack,
      @NotNull EntityEquipmentSlot armorSlot,
      @NotNull ModelBiped _default) {

    if (models == null) {
      models = new ModelArmorDiesel[4];

      for (int i = 0; i < 4; i++) models[i] = new ModelArmorDiesel(i);
    }

    return models[3 - armorSlot.getIndex()];
  }

  @Override
  public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
    super.onArmorTick(world, player, stack);

    if (!world.isRemote
        && this == ModItems.dieselsuit_legs
        && hasFSBArmor(player)
        && world.getTotalWorldTime() % 3 == 0) {
      NBTTagCompound data = new NBTTagCompound();
      data.setString("type", "bnuuy");
      data.setInteger("player", player.getEntityId());
      PacketThreading.createAllAroundThreadedPacket(
          new AuxParticlePacketNT(data, player.posX, player.posY, player.posZ),
          new TargetPoint(
              world.provider.getDimension(), player.posX, player.posY, player.posZ, 100));
    }
  }

  @Override
  public boolean acceptsFluid(FluidType type, ItemStack stack) {
    return type == Fluids.DIESEL || type == Fluids.DIESEL_CRACK;
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
          GlStateManager.translate(0, 0.5, 0);
          GlStateManager.scale(0.875, 0.875, 0.875);
        }

        if (item == ModItems.dieselsuit_helmet) {
          if (offsetsHelmet == null)
            offsetsHelmet =
                new ViewModelPositonDebugger()
                    .get(ItemCameraTransforms.TransformType.GUI)
                    .setScale(0.8F)
                    .setPosition(-1.2, 0.0, 1.5)
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
            ResourceManager.armor_dieselsuit,
            armorType,
            ResourceManager.dieselsuit_helmet,
            ResourceManager.dieselsuit_chest,
            ResourceManager.dieselsuit_arm,
            ResourceManager.dieselsuit_leg,
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
