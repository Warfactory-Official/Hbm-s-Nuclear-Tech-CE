package com.hbm.items.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorT51;
import com.hbm.render.tileentity.IItemRendererProvider;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ArmorT51 extends ArmorFSBPowered implements IItemRendererProvider {

    @SideOnly(Side.CLIENT)
    ModelArmorT51[] models;

    public ArmorT51(ArmorMaterial material, int layer, EntityEquipmentSlot slot, String texture, long maxPower, long chargeRate, long consumption, long drain, String s) {
        super(material, layer, slot, texture, maxPower, chargeRate, consumption, drain, s);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        if (models == null) {
            models = new ModelArmorT51[4];

            for (int i = 0; i < 4; i++)
                models[i] = new ModelArmorT51(i);
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
                renderStandard(ResourceManager.armor_t51, armorType, ResourceManager.t51_helmet, ResourceManager.t51_chest, ResourceManager.t51_arm, ResourceManager.t51_leg, "Helmet", "Chest", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "LeftBoot", "RightBoot");
            }
        };
    }
}

