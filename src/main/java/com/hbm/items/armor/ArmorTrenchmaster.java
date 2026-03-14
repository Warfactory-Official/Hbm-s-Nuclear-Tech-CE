package com.hbm.items.armor;

import com.hbm.capability.HbmCapability;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorTrenchmaster;
import com.hbm.render.tileentity.IItemRendererProvider;
import com.hbm.render.util.ViewModelPositonDebugger;
import com.hbm.util.I18nUtil;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ArmorTrenchmaster extends ArmorFSB implements IItemRendererProvider {

  @SideOnly(Side.CLIENT)
  ModelArmorTrenchmaster[] models;

  @SideOnly(Side.CLIENT)
  protected ViewModelPositonDebugger offsets;

  @SideOnly(Side.CLIENT)
  protected ViewModelPositonDebugger offsetsHelmetChestplate;

  public ArmorTrenchmaster(
      ArmorMaterial material, int layer, EntityEquipmentSlot slot, String texture, String s) {
    super(material, layer, slot, texture, s);
    this.setMaxDamage(0);
  }

  public static boolean isTrenchMaster(EntityPlayer player) {
    if (player == null) return false;
    return player.inventory.armorItemInSlot(2) != ItemStack.EMPTY
        && player.inventory.armorItemInSlot(2).getItem() == ModItems.trenchmaster_plate
        && ArmorFSB.hasFSBArmor(player);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public ModelBiped getArmorModel(
      @NotNull EntityLivingBase entityLiving,
      @NotNull ItemStack itemStack,
      @NotNull EntityEquipmentSlot armorSlot,
      @NotNull ModelBiped _default) {

    if (models == null) {
      models = new ModelArmorTrenchmaster[4];

      for (int i = 0; i < 4; i++) models[i] = new ModelArmorTrenchmaster(i);
    }

    return models[3 - armorSlot.getIndex()];
  }

  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn) {
    super.addInformation(stack, world, list, flagIn);

    // list.add(TextFormatting.RED + "  " + I18nUtil.resolveKey("armor.fasterReload"));
    list.add("§c" + "  " + I18nUtil.resolveKey("armor.moreAmmo"));
  }

  @Override
  public void handleHurt(LivingHurtEvent event) {
    super.handleHurt(event);
    if (event.getEntityLiving() instanceof EntityPlayer player) {
      if (ArmorFSB.hasFSBArmor(player)) {
        if (event.getSource().isExplosion()
            && event.getSource().getTrueSource() instanceof EntityPlayer) {
          event.setAmount(0);
        }
      }
    }
  }

  @Override
  public void handleAttack(LivingAttackEvent event) {
    super.handleAttack(event);
    EntityLivingBase e = event.getEntityLiving();

    if (e instanceof EntityPlayer player) {

      if (ArmorFSB.hasFSBArmor(player)) {

        if (e.getRNG().nextInt(3) == 0) {
          HbmCapability.plink(
              player, SoundEvents.ENTITY_ITEM_BREAK, 0.5F, 1.0F + e.getRNG().nextFloat() * 0.5F);
          event.setCanceled(true);
        }
      }
    }
  }

  public static boolean hasAoS(EntityPlayer player) {
    if (player == null) return false;
    if (!player.inventory.armorItemInSlot(3).isEmpty()) {
      ItemStack[] mods = ArmorModHandler.pryMods(player.inventory.armorItemInSlot(3));
      ItemStack helmet = mods[ArmorModHandler.helmet_only];
      return helmet != null && helmet.getItem() == ModItems.card_aos;
    }
    return false;
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
        if (armorType == EntityEquipmentSlot.MAINHAND) GlStateManager.translate(0, 1, 0);
        if (armorType == EntityEquipmentSlot.OFFHAND) GlStateManager.translate(0, 1.5, 0);
        setupRenderInv();
      }

      @Override
      public void renderNonInv() {
        setupRenderNonInv();
      }

      @Override
      public void renderCommon() {
        if (item == ModItems.trenchmaster_helmet || item == ModItems.trenchmaster_plate) {
          if (offsetsHelmetChestplate == null)
            offsetsHelmetChestplate =
                new ViewModelPositonDebugger()
                    .get(ItemCameraTransforms.TransformType.GUI)
                    .setScale(0.85F)
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

          offsetsHelmetChestplate.apply(type);
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
            ResourceManager.armor_trenchmaster,
            armorType,
            ResourceManager.trenchmaster_helmet,
            ResourceManager.trenchmaster_chest,
            ResourceManager.trenchmaster_arm,
            ResourceManager.trenchmaster_leg,
            "Helmet,Light",
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
