package com.hbm.items.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorRPA;
import com.hbm.render.tileentity.IItemRendererProvider;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class ArmorRPA extends ArmorFSBPowered implements IItemRendererProvider, IPAWeaponsProvider {

    @SideOnly(Side.CLIENT)
    ModelArmorRPA[] models;

    public ArmorRPA(ArmorMaterial material, int layer, EntityEquipmentSlot slot, String texture, long maxPower, long chargeRate, long consumption, long drain, String s) {
        super(material, layer, slot, texture, maxPower, chargeRate, consumption, drain, s);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(@NotNull EntityLivingBase entityLiving, @NotNull ItemStack itemStack, @NotNull EntityEquipmentSlot armorSlot, @NotNull ModelBiped _default){
        if(models == null) {
            models = new ModelArmorRPA[4];

            for(int i = 0; i < 4; i++)
                models[i] = new ModelArmorRPA(i);
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
                setupRenderInv();
            }
            public void renderNonInv() {
                setupRenderNonInv();
            }
            public void renderCommon() {
                if(armorType == EntityEquipmentSlot.CHEST)
                    GlStateManager.translate(0, 0.25, 0);

                renderStandard(ResourceManager.armor_remnant, armorType,
                        ResourceManager.rpa_helmet, ResourceManager.rpa_chest, ResourceManager.rpa_arm, ResourceManager.rpa_leg,
                        "Head", "Body,Fan,Glow", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "LeftBoot", "RightBoot");
            }};
    }

    public static final ArmorRPAMelee meleeComponent = new ArmorRPAMelee();

    @Override
    public IPAMelee getMeleeComponent(EntityPlayer entity) {
        if(hasFSBArmorIgnoreCharge(entity)) return meleeComponent;
        return null;
    }

    @Override public IPARanged getRangedComponent(EntityPlayer entity) { return null; }
}
