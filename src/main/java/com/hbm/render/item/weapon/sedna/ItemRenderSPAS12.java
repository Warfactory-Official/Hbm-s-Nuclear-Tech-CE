package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.particle.SpentCasing;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.awt.*;
@AutoRegister(item = "gun_spas12")
public class ItemRenderSPAS12 extends ItemRenderWeaponBase {

	public ItemRenderSPAS12() {}

	@Override
	protected float getTurnMagnitude(ItemStack stack) {
		return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.5F;
	}

	@Override
	public float getViewFOV(ItemStack stack, float fov) {
		float aimingProgress = ItemGunBaseNT.prevAimingProgress + (ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
		return fov * (1 - aimingProgress * 0.33F);
	}

	@Override
	public void setupFirstPerson(ItemStack stack) {
		GlStateManager.translate(0, 0, 0.875);
		float offset = 0.8F;
		standardAimingTransform(stack,
				-1.25F * offset, -1.75F * offset, -0.5F * offset,
				0, 0, 0);
	}

	@Override
	public void renderFirstPerson(ItemStack stack) {
		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.spas_12_tex);
		double scale = 0.5D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(180, 0, 1, 0);

		double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
		GlStateManager.rotate((float) equip[0], 1, 0, 0);

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		HbmAnimationsSedna.applyRelevantTransformation("MainBody");
		ResourceManager.spas_12.renderPart("MainBody");

		GlStateManager.pushMatrix();
		HbmAnimationsSedna.applyRelevantTransformation("PumpGrip");
		ResourceManager.spas_12.renderPart("PumpGrip");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.casings_tex);
		HbmAnimationsSedna.applyRelevantTransformation("Shell");
		SpentCasing casing = gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack)
				.getCasing(stack, MainRegistry.proxy.me().inventory);

		int color0 = SpentCasing.COLOR_CASE_BRASS;
		int color1 = SpentCasing.COLOR_CASE_BRASS;
		if (casing != null) {
			int[] colors = casing.getColors();
			color0 = colors[0];
			color1 = colors[colors.length > 1 ? 1 : 0];
		}

		Color shellColor = new Color(color1);
		GlStateManager.color(shellColor.getRed() / 255F, shellColor.getGreen() / 255F, shellColor.getBlue() / 255F);
		ResourceManager.spas_12.renderPart("Shell");

		Color shellForeColor = new Color(color0);
		GlStateManager.color(shellForeColor.getRed() / 255F, shellForeColor.getGreen() / 255F, shellForeColor.getBlue() / 255F);
		ResourceManager.spas_12.renderPart("ShellFore");

		GlStateManager.color(1F, 1F, 1F);

		double smokeScale = 0.25;
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1.5, -11);
		GlStateManager.rotate(-90, 0, 1, 0);
		GlStateManager.scale(smokeScale, smokeScale, smokeScale);
		this.renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 0.75D);
		GlStateManager.popMatrix();

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1.5, -11);
		GlStateManager.rotate(-90, 0, 1, 0);
		GlStateManager.rotate(90 * gun.shotRand, 1, 0, 0);
		this.renderMuzzleFlash(gun.lastShot[0], 75, 7.5);
		GlStateManager.popMatrix();
		GlStateManager.popMatrix();
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		double scale = 1.75D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(0, -0.75, 0);
	}

	@Override
	public void setupInv(ItemStack stack) {
		super.setupInv(stack);
		double scale = 2D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(25, 1, 0, 0);
		GlStateManager.rotate(45, 0, 1, 0);
		GlStateManager.translate(4.25, -0.5, 0);
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -10D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.translate(0, -0.5, -4.25);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {
		GlStateManager.enableLighting();
		GlStateManager.rotate(180, 0, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.spas_12_tex);
		ResourceManager.spas_12.renderPart("MainBody");
		ResourceManager.spas_12.renderPart("PumpGrip");
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}
}

