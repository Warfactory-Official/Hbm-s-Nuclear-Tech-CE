package com.hbm.items.armor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hbm.capability.HbmCapability;
import com.hbm.items.ModItems;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorNCRPA;
import com.hbm.render.tileentity.IItemRendererProvider;
import com.hbm.render.util.ViewModelPositonDebugger;
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

public class ArmorNCRPA extends ArmorFSBPowered implements IItemRendererProvider, IPAWeaponsProvider {

    @SideOnly(Side.CLIENT)
    ModelArmorNCRPA[] models;

    @SideOnly(Side.CLIENT)
    protected ViewModelPositonDebugger offsets;

    public ArmorNCRPA(ArmorMaterial material, int layer, EntityEquipmentSlot slot, String texture, long maxPower, long chargeRate, long consumption, long drain, String s) {
        super(material, layer, slot, texture, maxPower, chargeRate, consumption, drain, s);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(@NotNull EntityLivingBase entityLiving, @NotNull ItemStack itemStack, @NotNull EntityEquipmentSlot armorSlot, @NotNull ModelBiped _default){
        if(models == null) {
            models = new ModelArmorNCRPA[4];

            for(int i = 0; i < 4; i++)
                models[i] = new ModelArmorNCRPA(i);
        }

        return models[armorSlot.getIndex()];
    }

    private static final UUID speed = UUID.fromString("6ab858ba-d712-485c-bae9-e5e765fc555a");

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
        super.onArmorTick(world, player, stack);

        if(this != ModItems.ncrpa_plate) return;

        // SPEED //
        Multimap<String, AttributeModifier> multimap = HashMultimap.create();
        multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(speed, "NCRPA SPEED", 0.1, 0));
        player.getAttributeMap().removeAttributeModifiers(multimap);

        if(player.isSprinting()) {
            player.getAttributeMap().applyAttributeModifiers(multimap);
        }

        if(hasFSBArmor(player)) {
            if (world.getTotalWorldTime() % 20 != 0) return;
            if (HbmCapability.getData(player).getEnableHUD())
                player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 300, 0, true, false));
        }
    }

    @Override
    public Item getItemForRenderer() {
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase( ) {
            public void renderInventory() {
                setupRenderInv();
            }
            public void renderNonInv() {
                setupRenderNonInv();
            }
            public void renderCommon() {
                if(armorType == EntityEquipmentSlot.HEAD)
                    GlStateManager.translate(0, 0.5, 0);

                if (offsets == null)
                    offsets = new ViewModelPositonDebugger()
                            .get(ItemCameraTransforms.TransformType.GUI)
                            .setScale(1.0F).setPosition(-1.2, 0.0, 1.0).setRotation(255, -36, -143)
                            .getHelper()
                            .get(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND)
                            .setPosition(-1.00, -31.30, -4.95).setRotation(-23, -139, 85)
                            .getHelper()
                            .get(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND)
                            .setPosition(-0.5, 3, -2.75).setRotation(610, -115, -100)
                            .getHelper()
                            .get(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND)
                            .setScale(0.7F).setPosition(-0.25, -3.6, -1.25).setRotation(5, -90, 340)
                            .getHelper()
                            .get(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND)
                            .setPosition(-8, -5.50, -1.00).setRotation(0, 330, 180)
                            .getHelper()
                            .get(ItemCameraTransforms.TransformType.GROUND)
                            .setScale(1F).setPosition(0, 1, 0).setRotation(0, 0, 180)
                            .getHelper();

                offsets.apply(type);

                renderStandard(ResourceManager.armor_ncr, armorType,
                        ResourceManager.ncrpa_helmet, ResourceManager.ncrpa_chest, ResourceManager.ncrpa_arm, ResourceManager.ncrpa_leg,
                        "Helmet,Eyes", "Chest", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "LeftBoot", "RightBoot");
            }};
    }

    public static final ArmorNCRPAMelee meleeComponent = new ArmorNCRPAMelee();
    public static final ArmorNCRPARanged rangedComponent = new ArmorNCRPARanged();

    @Override
    public IPAMelee getMeleeComponent(EntityPlayer entity) {
        if(hasFSBArmorIgnoreCharge(entity)) return meleeComponent;
        return null;
    }

    @Override
    public IPARanged getRangedComponent(EntityPlayer entity) {
        if(hasFSBArmorIgnoreCharge(entity)) return rangedComponent;
        return null;
    }
}