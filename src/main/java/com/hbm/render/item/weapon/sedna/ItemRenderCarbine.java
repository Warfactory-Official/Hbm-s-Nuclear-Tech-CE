package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "gun_carbine")
public class ItemRenderCarbine extends ItemRenderWeaponBase {

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
				-1.5F * offset, -1.5F * offset, 0.875F * offset,
				0, -6.25 / 8D, 0.25);
	}

	@Override
	public void renderFirstPerson(ItemStack stack) {

		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.carbine_tex);
		double scale = 0.5D;
		GlStateManager.scale(scale, scale, scale);

		double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
		double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
		double[] slide = HbmAnimationsSedna.getRelevantTransformation("SLIDE");
		double[] mag = HbmAnimationsSedna.getRelevantTransformation("MAG");
		double[] lift = HbmAnimationsSedna.getRelevantTransformation("LIFT");
		double[] bullet = HbmAnimationsSedna.getRelevantTransformation("BULLET");
		double[] rel = HbmAnimationsSedna.getRelevantTransformation("REL");
		double[] stab = HbmAnimationsSedna.getRelevantTransformation("STAB");

		GlStateManager.translate(0, -1, -2);
		GlStateManager.rotate((float) equip[0], 1, 0, 0);
		GlStateManager.translate(0, 1, 2);

		GlStateManager.translate(0, 0, -2);
		GlStateManager.rotate((float) lift[0], 1, 0, 0);
		GlStateManager.translate(0, 0, 2);

		GlStateManager.translate(stab[0], stab[1], stab[2]);

		GlStateManager.translate(0, 0, recoil[2]);

		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		ResourceManager.carbine.renderPart("Gun");

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, slide[2]);
		ResourceManager.carbine.renderPart("Slide");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(mag[0], mag[1], mag[2]);
		ResourceManager.carbine.renderPart("Magazine");
		GlStateManager.translate(rel[0], rel[1], rel[2]);
		if (bullet[0] != 1) ResourceManager.carbine.renderPart("Bullet");
		GlStateManager.popMatrix();

		if(hasBayonet(stack)) {
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.carbine_bayonet_tex);
			ResourceManager.carbine.renderPart("Bayonet");
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1, 8);
		GlStateManager.rotate(90F, 0, 1, 0);
		this.renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 0.25D);
		GlStateManager.popMatrix();

		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1, 8);
		GlStateManager.rotate(90F, 0, 1, 0);
		GlStateManager.rotate(90F * gun.shotRand, 1, 0, 0);
		GlStateManager.scale(0.5, 0.5, 0.5);
		this.renderMuzzleFlash(gun.lastShot[0], 75, 7.5);
		GlStateManager.popMatrix();
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		double scale = 1.375D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(0, 0, 2);
	}

	@Override
	public void setupInv(ItemStack stack) {
		super.setupInv(stack);
		if(hasBayonet(stack)) {
			double scale = 1.1875D;
			GL11.glScaled(scale, scale, scale);
			GL11.glRotated(25, 1, 0, 0);
			GL11.glRotated(45, 0, 1, 0);
			GL11.glTranslated(1.5, 0, 0);
		} else {
			double scale = 1.375D;
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.rotate(25F, 1, 0, 0);
			GlStateManager.rotate(45F, 0, 1, 0);
			GlStateManager.translate(-0.5, 0, 0);
		}
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -7.75D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.translate(0, 0, -1.75);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {
		GlStateManager.enableLighting();

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.carbine_tex);
		ResourceManager.carbine.renderPart("Gun");
		ResourceManager.carbine.renderPart("Slide");
		ResourceManager.carbine.renderPart("Magazine");
		if(hasBayonet(stack)) {
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.carbine_bayonet_tex);
			ResourceManager.carbine.renderPart("Bayonet");
		}
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	public boolean hasBayonet(ItemStack stack) {
		return XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_CARBINE_BAYONET);
	}
}

