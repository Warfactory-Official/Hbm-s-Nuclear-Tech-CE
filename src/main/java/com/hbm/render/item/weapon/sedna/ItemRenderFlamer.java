package com.hbm.render.item.weapon.sedna;

import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ItemRenderFlamer extends ItemRenderWeaponBase {

	public ResourceLocation texture;

	public ItemRenderFlamer(ResourceLocation texture) {
		this.texture = texture;	}

	@Override
	protected float getTurnMagnitude(ItemStack stack) {
		return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.5F;
	}

	@Override
	public float getViewFOV(ItemStack stack, float fov) {
		float aimingProgress = ItemGunBaseNT.prevAimingProgress +
				(ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
		return  fov * (1 - aimingProgress * 0.33F);
	}

	@Override
	public void setupFirstPerson(ItemStack stack) {
		GlStateManager.translate(0, 0, 0.875);

		float offset = 0.8F;
		standardAimingTransform(stack,
				-1.5F * offset, -1.5F * offset, 2.75F * offset,
				0, -4.625 / 8D, 0.25);
	}

	@Override
	public void renderFirstPerson(ItemStack stack) {

		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		double scale = 0.375D;
		GlStateManager.scale(scale, scale, scale);

		double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
		double[] rotate = HbmAnimationsSedna.getRelevantTransformation("ROTATE");

		GlStateManager.translate(0, 2, -6);
		GlStateManager.rotate((float) -equip[0], 1, 0, 0);
		GlStateManager.translate(0, -2, 6);

		GlStateManager.translate(0, 1, 0);
		GlStateManager.rotate((float) rotate[2], 0, 0, 1);
		GlStateManager.translate(0, -1, 0);

		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		GlStateManager.pushMatrix();
		HbmAnimationsSedna.applyRelevantTransformation("Gun");
		ResourceManager.flamethrower.renderPart("Gun");
		if(hasShield(stack)) ResourceManager.flamethrower.renderPart("HeatShield");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		HbmAnimationsSedna.applyRelevantTransformation("Tank");
		ResourceManager.flamethrower.renderPart("Tank");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		HbmAnimationsSedna.applyRelevantTransformation("Gauge");
		GlStateManager.translate(1.25, 1.25, 0);
		IMagazine mag = gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);
		GlStateManager.rotate((float) (-135 + (mag.getAmount(stack, MainRegistry.proxy.me().inventory) * 270D / mag.getCapacity(stack))), 0, 0, 1);
		GlStateManager.translate(-1.25, -1.25, 0);
		ResourceManager.flamethrower.renderPart("Gauge");
		GlStateManager.popMatrix();

		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		double scale = 1.75D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(0, -3, 4);
	}

	@Override
	public void setupInv(ItemStack stack) {
		super.setupInv(stack);
		double scale = 1.25D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(25F, 1, 0, 0);
		GlStateManager.rotate(45F, 0, 1, 0);
		GlStateManager.translate(-1, 1, 0);
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -7.5D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.translate(0, 0, 1);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {
		GlStateManager.enableLighting();

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		ResourceManager.flamethrower.renderPart("Gun");
		ResourceManager.flamethrower.renderPart("Tank");
		ResourceManager.flamethrower.renderPart("Gauge");
		if(hasShield(stack)) ResourceManager.flamethrower.renderPart("HeatShield");
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	public boolean hasShield(ItemStack stack) {
		return stack.getItem() == ModItems.gun_flamer_daybreaker;
	}
}

