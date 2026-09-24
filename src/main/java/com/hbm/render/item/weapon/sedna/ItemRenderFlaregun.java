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
@AutoRegister(item = "gun_flaregun")
public class ItemRenderFlaregun extends ItemRenderWeaponBase {

	public ItemRenderFlaregun() {	}

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
				-1.25F * offset, -1.5F * offset, 2F * offset,
				0, -5.5 / 8D, 0.5);
	}

	@Override
	public void renderFirstPerson(ItemStack stack) {

		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.flaregun_tex);
		double scale = 0.125D;
		GlStateManager.scale(scale, scale, scale);

		double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
		double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
		double[] hammer = HbmAnimationsSedna.getRelevantTransformation("HAMMER");
		double[] open = HbmAnimationsSedna.getRelevantTransformation("OPEN");
		double[] shell = HbmAnimationsSedna.getRelevantTransformation("SHELL");
		double[] flip = HbmAnimationsSedna.getRelevantTransformation("FLIP");

		GlStateManager.translate(recoil[0], recoil[1], recoil[2]);
		GlStateManager.rotate((float) (recoil[2] * 10), 1, 0, 0);
		GlStateManager.rotate((float) flip[0], 1, 0, 0);

		GlStateManager.translate(0, 0, -8);
		GlStateManager.rotate((float) equip[0], -1, 0, 0);
		GlStateManager.translate(0, 0, 8);

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		ResourceManager.flaregun.renderPart("Gun");

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1.8125, -4);
		GlStateManager.rotate((float) (hammer[0] - 15), 1, 0, 0);
		GlStateManager.translate(0, -1.8125, 4);
		ResourceManager.flaregun.renderPart("Hammer");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 2.156, 1.78);
		GlStateManager.rotate((float) open[0], 1, 0, 0);
		GlStateManager.translate(0, -2.156, -1.78);
		ResourceManager.flaregun.renderPart("Barrel");
		GlStateManager.translate(shell[0], shell[1], shell[2]);
		ResourceManager.flaregun.renderPart("Flare");
		GlStateManager.popMatrix();

		double smokeScale = 0.5;

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 4, 9);
		GlStateManager.rotate(90F, 0, 1, 0);
		GlStateManager.scale(smokeScale, smokeScale, smokeScale);
		this.renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 2.5D);
		GlStateManager.translate(0, 0, 0.1);
		this.renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 2D);
		GlStateManager.popMatrix();

		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		double scale = 0.5D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(0, 0.25, 3);
	}

	@Override
	public void setupInv(ItemStack stack) {
		super.setupInv(stack);
		double scale = 1D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(25F, 1, 0, 0);
		GlStateManager.rotate(45F, 0, 1, 0);
		GlStateManager.translate(-0.5, 0, 0);
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -5D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(90, 0, 1, 0);
	}

	@Override
	public void setupEntity(ItemStack stack) {
		super.setupEntity(stack);
		double scale = 0.5D;
		GlStateManager.scale(scale, scale, scale);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {
		GlStateManager.enableLighting();

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.flaregun_tex);
		ResourceManager.flaregun.renderAll();
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}
}

