package com.hbm.items.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorAJR;
import com.hbm.render.tileentity.IItemRendererProvider;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class ArmorAJR extends ArmorFSBPowered implements IItemRendererProvider {

    @SideOnly(Side.CLIENT)
    ModelArmorAJR[] models;

	public ArmorAJR(ArmorMaterial material, int layer, EntityEquipmentSlot slot, String texture, long maxPower, long chargeRate, long consumption, long drain, String s) {
		super(material, layer, slot, texture, maxPower, chargeRate, consumption, drain, s);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(@NotNull EntityLivingBase entityLiving, @NotNull ItemStack itemStack, @NotNull EntityEquipmentSlot armorSlot, @NotNull ModelBiped _default) {
		if(models == null) {
			models = new ModelArmorAJR[4];

			for(int i = 0; i < 4; i++)
				models[i] = new ModelArmorAJR(i);
		}

		return models[3-armorSlot.getIndex()];
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
                renderStandard(ResourceManager.armor_ajr, armorType, ResourceManager.ajr_helmet, ResourceManager.ajr_chest, ResourceManager.ajr_arm, ResourceManager.ajr_leg, "Head", "Body", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "LeftBoot", "RightBoot");
            }};
    }
}
