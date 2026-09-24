package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "gun_m2")
public class ItemRenderM2 extends ItemRenderWeaponBase {

	public ItemRenderM2() {}

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
				-1.5F * offset, -2.5F * offset, 1.75F * offset,
				0, -12.5 / 8D, 1.75);
	}

	@Override
	public void renderFirstPerson(ItemStack stack) {

		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.greasegun_tex);
		double scale = 0.75D;
		GlStateManager.scale(scale, scale, scale);

		double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
		double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");

		GlStateManager.translate(0, 1, -2.25);
		GlStateManager.rotate((float) equip[0], 1, 0, 0);
		GlStateManager.translate(0, -1, 2.25);

		GlStateManager.translate(0, 0, recoil[2]);

		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		GlStateManager.pushMatrix();
		GlStateManager.rotate(180, 0, 1, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.m2_tex);
		ResourceManager.m2.renderAll();
		GlStateManager.popMatrix();

		double smokeScale = 0.5;

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1.625, 5);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.scale(smokeScale, smokeScale, smokeScale);
		this.renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 0.375D);
		GlStateManager.popMatrix();

		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1.625, 5);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.rotate(90 * gun.shotRand, 1, 0, 0);
		GlStateManager.scale(0.5, 0.5, 0.5);
		this.renderMuzzleFlash(gun.lastShot[0], 75, 7.5);
		GlStateManager.popMatrix();
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		double scale = 5D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(0.5, -2, 3);
	}

	@Override
	public void setupInv(ItemStack stack) {
		super.setupInv(stack);
		double scale = 2.625D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(25, 1, 0, 0);
		GlStateManager.rotate(45, 0, 1, 0);
		GlStateManager.translate(0.5, -1.25, 0);
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -15D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.translate(0, -1.5, -0.5);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {
		GlStateManager.enableLighting();

		GlStateManager.rotate(180, 0, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.m2_tex);
		ResourceManager.m2.renderAll();
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}
}

