package com.hbm.render.item.weapon.sedna;

import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ItemRenderHeavyRevolver extends ItemRenderWeaponBase {

	protected ResourceLocation texture;

	public ItemRenderHeavyRevolver(ResourceLocation texture) {
		this.texture = texture;
		offsets = offsets.get(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND).setScale(0.75).setPosition(-0.9, 0.3, -1.15).getHelper();
	}

	@Override
	protected float getTurnMagnitude(ItemStack stack) {
		return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.25F;
	}

	@Override
	public float getViewFOV(ItemStack stack, float fov) {
		float aimingProgress = ItemGunBaseNT.prevAimingProgress +
				(ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
		return fov * (1 - aimingProgress * (isScoped(stack) ? 0.66F : 0.33F));
	}

	@Override
	public void setupFirstPerson(ItemStack stack) {
		GlStateManager.translate(0, 0, 1);

		boolean isScoped = this.isScoped(stack);

		float offset = 0.8F;
		standardAimingTransform(stack,
				-1.0F * offset, -0.75F * offset, 1F * offset,
				0, isScoped ? (-4.75 / 8D) : (-3.875 / 8D), isScoped ? -0.25 : 0);
	}

	@Override
	public void renderFirstPerson(ItemStack stack) {

		boolean isScoped = this.isScoped(stack);
		if (isScoped && ItemGunBaseNT.prevAimingProgress == 1 && ItemGunBaseNT.aimingProgress == 1) return;
		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();

		double scale = 0.125D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(90, 0, 1, 0);

		double[] equipSpin = HbmAnimationsSedna.getRelevantTransformation("ROTATE");
		double[] spin = HbmAnimationsSedna.getRelevantTransformation("SPIN");
		double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
		double[] reloadLift = HbmAnimationsSedna.getRelevantTransformation("RELOAD_LIFT");
		double[] reloadJolt = HbmAnimationsSedna.getRelevantTransformation("RELOAD_JOLT");
		double[] reloadTilt = HbmAnimationsSedna.getRelevantTransformation("RELAOD_TILT");
		double[] cylinderFlip = HbmAnimationsSedna.getRelevantTransformation("RELOAD_CYLINDER");
		double[] reloadBullets = HbmAnimationsSedna.getRelevantTransformation("RELOAD_BULLETS");

		GlStateManager.rotate((float) spin[0], 0, 0, 1);

		GlStateManager.translate(6, -3, 0);
		GlStateManager.rotate((float) equipSpin[0], 0, 0, 1);
		GlStateManager.translate(-6, 3, 0);

		standardAimingTransform(stack, 0, 0, recoil[2], -recoil[2], 0, 0);
		GlStateManager.rotate((float) (recoil[2] * 10), 0, 0, 1);

		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		GlStateManager.pushMatrix();
		GlStateManager.translate(-9, 2.5, 0);
		GlStateManager.rotate((float) (recoil[2] * -10), 0, 0, 1);
		this.renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 0.5D);
		GlStateManager.popMatrix();

		GlStateManager.rotate((float) reloadLift[0], 0, 0, 1);
		GlStateManager.translate(reloadJolt[0], 0, 0);
		GlStateManager.rotate((float) reloadTilt[0], 1, 0, 0);

		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		ResourceManager.lilmac.renderPart("Gun");

		GlStateManager.pushMatrix();
		GlStateManager.rotate((float) cylinderFlip[0], 1, 0, 0);
		ResourceManager.lilmac.renderPart("Pivot");
		GlStateManager.translate(0, 1.75, 0);
		GlStateManager.rotate((float) (HbmAnimationsSedna.getRelevantTransformation("DRUM")[2] * -60), 1, 0, 0);
		GlStateManager.translate(0, -1.75, 0);
		ResourceManager.lilmac.renderPart("Cylinder");
		GlStateManager.translate(reloadBullets[0], reloadBullets[1], reloadBullets[2]);
		if (HbmAnimationsSedna.getRelevantTransformation("RELOAD_BULLETS_CON")[0] != 1)
			ResourceManager.lilmac.renderPart("Bullets");
		ResourceManager.lilmac.renderPart("Casings");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix(); /// HAMMER ///
		GlStateManager.translate(4, 1.25, 0);
		GlStateManager.rotate(-30 + 30 * (float) HbmAnimationsSedna.getRelevantTransformation("HAMMER")[2], 0, 0, 1);
		GlStateManager.translate(-4, -1.25, 0);
		ResourceManager.lilmac.renderPart("Hammer");
		GlStateManager.popMatrix();

		if (isScoped) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.lilmac_scope_tex);
			ResourceManager.lilmac.renderPart("Scope");
		}

		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.125, 2.5, 0);
		this.renderGapFlash(gun.lastShot[0]);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(-9.5, 2.5, 0);
		GlStateManager.rotate(90 * gun.shotRand, 1, 0, 0);
		GlStateManager.popMatrix();
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		GlStateManager.scale(0.75, 0.75, 0.75);
		GlStateManager.translate(0, 1, 3);
	}

	@Override
	public void setupInv(ItemStack stack) {
		super.setupInv(stack);
		if (isScoped(stack)) {
			double scale = 1.125D;
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.rotate(25, 1, 0, 0);
			GlStateManager.rotate(45, 0, 1, 0);
			GlStateManager.translate(0, -0.5, 0);
		} else {
			double scale = 1.25D;
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.rotate(25, 1, 0, 0);
			GlStateManager.rotate(45, 0, 1, 0);
		}
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -5D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.translate(0, -0.5, 0);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {

		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.enableLighting();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
		GlStateManager.enableAlpha();

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		ResourceManager.lilmac.renderPart("Gun");
		ResourceManager.lilmac.renderPart("Cylinder");
		ResourceManager.lilmac.renderPart("Bullets");
		ResourceManager.lilmac.renderPart("Casings");
		ResourceManager.lilmac.renderPart("Pivot");
		ResourceManager.lilmac.renderPart("Hammer");
		if (isScoped(stack)) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.lilmac_scope_tex);
			ResourceManager.lilmac.renderPart("Scope");
		}
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	public boolean isScoped(ItemStack stack) {
		return stack.getItem() == ModItems.gun_heavy_revolver_lilmac || XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SCOPE);
	}
}

