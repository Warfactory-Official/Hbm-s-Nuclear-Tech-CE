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

public class ItemRenderAtlas extends ItemRenderWeaponBase {

	public ResourceLocation texture;

	public ItemRenderAtlas(ResourceLocation texture) {
		this.texture = texture;	}

	@Override
	protected float getTurnMagnitude(ItemStack stack) {
		return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.25F;
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
				-1.0F * offset, -0.75F * offset, 1F * offset,
				0, -3.125 / 8D, 0.25);
	}

	@Override
	public void renderFirstPerson(ItemStack stack) {

		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		double scale = 0.125D;
		GlStateManager.scale(scale, scale, scale);

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
		double[] reloadMove = HbmAnimationsSedna.getRelevantTransformation("RELOAD_MOVE");
		double[] reloadRot = HbmAnimationsSedna.getRelevantTransformation("RELOAD_ROT");
		double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");

		GlStateManager.translate(recoil[0], recoil[1], recoil[2]);
		GlStateManager.rotate((float) (recoil[2] * 10), 1, 0, 0);

		GlStateManager.translate(0, 0, -7);
		GlStateManager.rotate((float) equip[0], -1, 0, 0);
		GlStateManager.translate(0, 0, 7);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1.5, 9.25);
		GlStateManager.rotate((float) (-recoil[2] * 10), 1, 0, 0);
		GlStateManager.rotate(90F, 0, 1, 0);
		this.renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 0.5D);
		GlStateManager.popMatrix();

		GlStateManager.translate(reloadMove[0], reloadMove[1], reloadMove[2]);

		GlStateManager.rotate((float) reloadRot[0], 1, 0, 0);
		GlStateManager.rotate((float) reloadRot[2], 0, 0, 1);
		GlStateManager.rotate((float) reloadRot[1], 0, 1, 0);
		ResourceManager.bio_revolver.renderPart("Grip");

		GlStateManager.pushMatrix(); /// FRONT PUSH ///
		GlStateManager.rotate((float) HbmAnimationsSedna.getRelevantTransformation("FRONT")[2], 1, 0, 0);
		ResourceManager.bio_revolver.renderPart("Barrel");
		GlStateManager.pushMatrix(); /// LATCH PUSH ///
		GlStateManager.translate(0, 2.3125, -0.875);
		GlStateManager.rotate((float) HbmAnimationsSedna.getRelevantTransformation("LATCH")[2], 1, 0, 0);
		GlStateManager.translate(0, -2.3125, 0.875);
		ResourceManager.bio_revolver.renderPart("Latch");
		GlStateManager.popMatrix(); /// LATCH POP ///

		GlStateManager.pushMatrix(); /// DRUM PUSH ///
		GlStateManager.translate(0, 1, 0);
		GlStateManager.rotate((float) (HbmAnimationsSedna.getRelevantTransformation("DRUM")[2] * 60), 0, 0, 1);
		GlStateManager.translate(0, -1, 0);
		GlStateManager.translate(0, 0, HbmAnimationsSedna.getRelevantTransformation("DRUM_PUSH")[2]);
		ResourceManager.bio_revolver.renderPart("Drum");
		GlStateManager.popMatrix(); /// DRUM POP ///

		GlStateManager.popMatrix(); /// FRONT POP ///

		GlStateManager.pushMatrix(); /// HAMMER ///
		GlStateManager.translate(0, 0, -4.5);
		GlStateManager.rotate((float) (-45 + 45 * HbmAnimationsSedna.getRelevantTransformation("HAMMER")[2]), 1, 0, 0);
		GlStateManager.translate(0, 0, 4.5);
		ResourceManager.bio_revolver.renderPart("Hammer");
		GlStateManager.popMatrix();

		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1.5, 9.25);
		GlStateManager.rotate(90F, 0, 1, 0);
		this.renderMuzzleFlash(gun.lastShot[0], 75, 7.5);
		GlStateManager.popMatrix();
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		double scale = 0.75D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(0, 1, 3);
	}

	@Override
	public void setupInv(ItemStack stack) {
		super.setupInv(stack);
		double scale = 1.125D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(25F, 1, 0, 0);
		GlStateManager.rotate(45F, 0, 1, 0);
		GlStateManager.translate(-0.5, 1.5, 0);
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -5D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.translate(0, 1.5, 0);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {
		GlStateManager.enableLighting();

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		ResourceManager.bio_revolver.renderAll();
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}
}

