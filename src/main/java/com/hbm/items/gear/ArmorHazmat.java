package com.hbm.items.gear;

import com.hbm.Tags;
import com.hbm.items.ModItems;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ArmorHazmat extends ItemArmor {

	private ResourceLocation hazmatBlur = new ResourceLocation(Tags.MODID + ":textures/misc/overlay_hazmat.png");
	
	public ArmorHazmat(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn, String s) {
		super(materialIn, renderIndexIn, equipmentSlotIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(CreativeTabs.COMBAT);
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		if(stack.getItem().equals(ModItems.hazmat_plate) || stack.getItem().equals(ModItems.hazmat_boots)) {
			return (Tags.MODID + ":textures/armor/hazmat_1.png");
		}
		if(stack.getItem().equals(ModItems.hazmat_legs)) {
			return (Tags.MODID + ":textures/armor/hazmat_2.png");
		}
		if(stack.getItem().equals(ModItems.hazmat_paa_plate) || stack.getItem().equals(ModItems.hazmat_paa_boots)) {
			return (Tags.MODID + ":textures/armor/hazmat_paa_1.png");
		}
		if(stack.getItem().equals(ModItems.hazmat_paa_legs)) {
			return (Tags.MODID + ":textures/armor/hazmat_paa_2.png");
		}
		if(stack.getItem().equals(ModItems.hazmat_plate_red) || stack.getItem().equals(ModItems.hazmat_boots_red)) {
			return (Tags.MODID + ":textures/armor/hazmat_1_red.png");
		}
		if(stack.getItem().equals(ModItems.hazmat_legs_red)) {
			return (Tags.MODID + ":textures/armor/hazmat_2_red.png");
		}
		if(stack.getItem().equals(ModItems.hazmat_plate_grey) || stack.getItem().equals(ModItems.hazmat_boots_grey)) {
			return (Tags.MODID + ":textures/armor/hazmat_1_grey.png");
		}
		if(stack.getItem().equals(ModItems.hazmat_legs_grey)) {
			return (Tags.MODID + ":textures/armor/hazmat_2_grey.png");
		}
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void renderHelmetOverlay(ItemStack stack, EntityPlayer player, ScaledResolution resolution, float partialTicks) {
		if(this != ModItems.hazmat_helmet && this != ModItems.hazmat_paa_helmet)
    		return;
		GlStateManager.disableDepth();
		GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableAlpha();
        Minecraft.getMinecraft().getTextureManager().bindTexture(hazmatBlur);
        NTMRenderHelper.startDrawingTexturedQuads();
        NTMRenderHelper.addVertexWithUV(0.0D, (double)resolution.getScaledHeight(), -90.0D, 0.0D, 1.0D);
        NTMRenderHelper.addVertexWithUV((double)resolution.getScaledWidth(), (double)resolution.getScaledHeight(), -90.0D, 1.0D, 1.0D);
        NTMRenderHelper.addVertexWithUV((double)resolution.getScaledWidth(), 0.0D, -90.0D, 1.0D, 0.0D);
        NTMRenderHelper.addVertexWithUV(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);
        NTMRenderHelper.draw();
		GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		super.renderHelmetOverlay(stack, player, resolution, partialTicks);
	}
}
