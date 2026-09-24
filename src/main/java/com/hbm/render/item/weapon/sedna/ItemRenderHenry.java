package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
public class ItemRenderHenry extends ItemRenderWeaponBase {

	public ResourceLocation texture;

	public ItemRenderHenry(ResourceLocation texture) {
		this.texture = texture;
	}

	@Override
	protected float getTurnMagnitude(ItemStack stack) {
		return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.5F;
	}

	@Override
	public float getViewFOV(ItemStack stack, float fov) {
		float aimingProgress = ItemGunBaseNT.prevAimingProgress +
				(ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
		return fov * (1 - aimingProgress * 0.33F);
	}

	@Override
	public void setupFirstPerson(ItemStack stack) {
		GlStateManager.translate(0, 0, 0.875);

		float offset = 0.8F;
		standardAimingTransform(stack,
				-1.25F * offset, -1F * offset, 1.75F * offset,
				0, -5 / 8D, 1);

		float aimingProgress = ItemGunBaseNT.prevAimingProgress +
				(ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
		double r = -2.5 * aimingProgress;
		GlStateManager.rotate((float) r, 1, 0, 0);
	}

	@Override
	public void renderFirstPerson(ItemStack stack) {

		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		double scale = 0.375D;
		GlStateManager.scale(scale, scale, scale);

		double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
		double[] sight = HbmAnimationsSedna.getRelevantTransformation("SIGHT");
		double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
		double[] hammer = HbmAnimationsSedna.getRelevantTransformation("HAMMER");
		double[] lever = HbmAnimationsSedna.getRelevantTransformation("LEVER");
		double[] turn = HbmAnimationsSedna.getRelevantTransformation("TURN");
		double[] lift = HbmAnimationsSedna.getRelevantTransformation("LIFT");
		double[] twist = HbmAnimationsSedna.getRelevantTransformation("TWIST");
		double[] bullet = HbmAnimationsSedna.getRelevantTransformation("BULLET");
		double[] yeet = HbmAnimationsSedna.getRelevantTransformation("YEET");
		double[] roll = HbmAnimationsSedna.getRelevantTransformation("ROLL");

		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		GlStateManager.translate(recoil[0] * 2, recoil[1], recoil[2]);
		GlStateManager.rotate((float) (recoil[2] * 5), 1, 0, 0);
		GlStateManager.rotate((float) turn[2], 0, 0, 1);

		GlStateManager.translate(yeet[0], yeet[1], yeet[2]);

		GlStateManager.translate(0, 1, 0);
		GlStateManager.rotate((float) roll[2], 0, 0, 1);
		GlStateManager.translate(0, -1, 0);

		GlStateManager.translate(0, -4, 4);
		GlStateManager.rotate((float) lift[0], 1, 0, 0);
		GlStateManager.translate(0, 4, -4);

		GlStateManager.translate(0, 2, -4);
		GlStateManager.rotate((float) equip[0], -1, 0, 0);
		GlStateManager.translate(0, -2, 4);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1, 8);
		GlStateManager.rotate((float) turn[2], 0, 0, -1);
		GlStateManager.rotate(90, 0, 1, 0);
		this.renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 0.25D);
		GlStateManager.popMatrix();

		ResourceManager.henry.renderPart("Gun");

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1.25, -0.1875);
		GlStateManager.rotate((float) sight[0], 1, 0, 0);
		GlStateManager.translate(0, -1.25, 0.1875);
		ResourceManager.henry.renderPart("Sight");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0.625, -3);
		GlStateManager.rotate((float) (-30 + hammer[0]), 1, 0, 0);
		GlStateManager.translate(0, -0.625, 3);
		ResourceManager.henry.renderPart("Hammer");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0.25, -2.3125);
		GlStateManager.rotate((float) lever[0], 1, 0, 0);
		GlStateManager.translate(0, -0.25, 2.3125);
		ResourceManager.henry.renderPart("Lever");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1, 0);
		GlStateManager.rotate((float) twist[2], 0, 0, 1);
		GlStateManager.translate(0, -1, 0);
		ResourceManager.henry.renderPart("Front");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(bullet[0], bullet[1], bullet[2] - 1);
		ResourceManager.henry.renderPart("Bullet");
		GlStateManager.popMatrix();

		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1, 8);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.rotate(90 * gun.shotRand, 1, 0, 0);
		this.renderMuzzleFlash(gun.lastShot[0], 75, 5);
		GlStateManager.popMatrix();
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		double scale = 1.75D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(0, 0.25, 3);
	}

	@Override
	public void setupInv(ItemStack stack) {
		super.setupInv(stack);
		double scale = 1.5D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(25, 1, 0, 0);
		GlStateManager.rotate(45, 0, 1, 0);
		GlStateManager.translate(-0.5, 0.5, 0);
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -7.5D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(90, 0, 1, 0);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {
		GlStateManager.enableLighting();

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.henry_tex);
		ResourceManager.henry.renderAll();
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}
}

