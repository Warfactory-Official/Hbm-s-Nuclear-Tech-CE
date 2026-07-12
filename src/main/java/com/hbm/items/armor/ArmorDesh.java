package com.hbm.items.armor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorDesh;
import com.hbm.render.tileentity.IItemRendererProvider;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class ArmorDesh extends ArmorFSBFueled implements IItemRendererProvider {

    @SideOnly(Side.CLIENT)
    ModelArmorDesh[] models;

    public ArmorDesh(ArmorMaterial material, int layer, EntityEquipmentSlot slot, String texture, FluidType fuelType, int maxFuel, int fillRate, int consumption, int drain, String s) {
        super(material, layer, slot, texture, fuelType, maxFuel, fillRate, consumption, drain, s);
    }

    @Override
    public @NotNull Multimap<String, AttributeModifier> getItemAttributeModifiers(@NotNull EntityEquipmentSlot slot) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.create(super.getItemAttributeModifiers(slot));
        if (slot == this.armorType) {
            multimap.put(
                    SharedMonsterAttributes.MOVEMENT_SPEED.getName(),
                    new AttributeModifier(
                            ArmorModHandler.fixedUUIDs[this.armorType.getIndex()],
                            "Armor modifier",
                            -0.025D,
                            1
                    )
            );
        }
        return multimap;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(@NotNull EntityLivingBase entityLiving, @NotNull ItemStack itemStack, @NotNull EntityEquipmentSlot armorSlot, @NotNull ModelBiped _default) {

        if (models == null) {
            models = new ModelArmorDesh[4];

            for (int i = 0; i < 4; i++)
                models[i] = new ModelArmorDesh(i);
        }

        return models[3 - armorSlot.getIndex()];
    }

    @Override
    public Item getItemForRenderer() {
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemRenderBase getRenderer(Item _item) {
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
                renderStandard(ResourceManager.armor_steamsuit, armorType, ResourceManager.steamsuit_helmet, ResourceManager.steamsuit_chest, ResourceManager.steamsuit_arm, ResourceManager.steamsuit_leg, "Head", "Body", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "LeftBoot", "RightBoot");
            }
        };
    }
}
