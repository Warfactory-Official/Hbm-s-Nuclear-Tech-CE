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

public class ItemRenderMaresleg extends ItemRenderWeaponBase {

	public ResourceLocation texture;

	public ItemRenderMaresleg(ResourceLocation texture) {
		this.texture = texture;
		offsets = offsets.get(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND).setScale(0.85).setPosition(-0.35, 0.15, -1.05).getHelper();
		if(texture == ResourceManager.maresleg_broken_tex) offsets = offsets.get(ItemCameraTransforms.TransformType.GUI).setPosition(0, 16.5, -5.75).getHelper();
	}

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
				-1.25F * offset, -1F * offset, 2F * offset,
				0, -3.875 / 8D, 1);
	}

	@Override
	public void renderFirstPerson(ItemStack stack) {

		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		double scale = 0.375D;
		GlStateManager.scale(scale, scale, scale);

		boolean shortened = getShort(stack);

		double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
		double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
		double[] lever = HbmAnimationsSedna.getRelevantTransformation("LEVER");
		double[] turn = HbmAnimationsSedna.getRelevantTransformation("TURN");
		double[] flip = HbmAnimationsSedna.getRelevantTransformation("FLIP");
		double[] lift = HbmAnimationsSedna.getRelevantTransformation("LIFT");
		double[] shell = HbmAnimationsSedna.getRelevantTransformation("SHELL");
		double[] flag = HbmAnimationsSedna.getRelevantTransformation("FLAG");

		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		GlStateManager.translate(recoil[0] * 2, recoil[1], recoil[2]);
		GlStateManager.rotate((float) recoil[2] * 5, 1, 0, 0);
		GlStateManager.rotate((float) turn[2], 0, 0, 1);

		GlStateManager.translate(0, 0, -4);
		GlStateManager.rotate((float) lift[0], 1, 0, 0);
		GlStateManager.translate(0, 0, 4);

		GlStateManager.translate(0, 0, -4);
		GlStateManager.rotate((float) equip[0], -1, 0, 0);
		GlStateManager.translate(0, 0, 4);

		GlStateManager.translate(0, 0, -2);
		GlStateManager.rotate((float) flip[0], -1, 0, 0);
		GlStateManager.translate(0, 0, 2);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1, shortened ? 3.75 : 8);
		GlStateManager.rotate((float) turn[2], 0, 0, -1);
		GlStateManager.rotate((float) flip[0], 1, 0, 0);
		GlStateManager.rotate(90, 0, 1, 0);
		this.renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 0.25D);
		GlStateManager.popMatrix();

		ResourceManager.maresleg.renderPart("Gun");
		if(!shortened) {
			ResourceManager.maresleg.renderPart("Stock");
			ResourceManager.maresleg.renderPart("Barrel");
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0.125, -2.875);
		GlStateManager.rotate((float) lever[0], 1, 0, 0);
		GlStateManager.translate(0, -0.125, 2.875);
		ResourceManager.maresleg.renderPart("Lever");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(shell[0], shell[1] - 0.75, shell[2]);
		ResourceManager.maresleg.renderPart("Shell");
		GlStateManager.popMatrix();

		if(flag[0] != 0) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, -0.5, 0);
			ResourceManager.maresleg.renderPart("Shell");
			GlStateManager.popMatrix();
		}

		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1, shortened ? 3.75 : 8);
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

		if(getShort(stack)) {
			double scale = 2.5D;
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.rotate(25, 1, 0, 0);
			GlStateManager.rotate(45, 0, 1, 0);
			GlStateManager.translate(-1, 0, 0);
		} else {
			double scale = 1.4375D;
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.rotate(25, 1, 0, 0);
			GlStateManager.rotate(45, 0, 1, 0);
			GlStateManager.translate(-0.5, 0.5, 0);
		}
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -8.75D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(90, 0, 1, 0);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {
		GlStateManager.enableLighting();

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		ResourceManager.maresleg.renderPart("Gun");
		ResourceManager.maresleg.renderPart("Lever");
		if(!getShort(stack)) {
			ResourceManager.maresleg.renderPart("Stock");
			ResourceManager.maresleg.renderPart("Barrel");
		}
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	public boolean getShort(ItemStack stack) {
		return stack.getItem() == ModItems.gun_maresleg_broken || XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SAWED_OFF);
	}
}

