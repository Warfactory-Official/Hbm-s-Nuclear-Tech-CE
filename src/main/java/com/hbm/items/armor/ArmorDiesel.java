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
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorDiesel;
import com.hbm.render.tileentity.IItemRendererProvider;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
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
  public void onArmorTick(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {
    super.onArmorTick(world, player, stack);

    if (!world.isRemote
        && this == ModItems.dieselsuit_legs
        && hasFSBArmor(player)
        && world.getTotalWorldTime() % 3 == 0) {
      NBTTagCompound data = new NBTTagCompound();
      data.setInteger("player", player.getEntityId());
      PacketThreading.createAllAroundThreadedPacket(
          new AuxParticlePacketNT(HbmEffectNT.bnuuy, data, player.posX, player.posY, player.posZ),
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
        if (armorType == EntityEquipmentSlot.HEAD) {
          GlStateManager.translate(0, 0.5, 0);
          GlStateManager.scale(0.875, 0.875, 0.875);
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
