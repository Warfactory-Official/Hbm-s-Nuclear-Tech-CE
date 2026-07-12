package com.hbm.render.item.weapon.sedna;

import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ItemRenderDoubleBarrel extends ItemRenderWeaponBase {

	protected ResourceLocation texture;

	public ItemRenderDoubleBarrel(ResourceLocation texture) {
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
				-1.25F * offset, -1F * offset, 2F * offset,
				0, -2 / 8D, 1);
	}

	@Override
	public void renderFirstPerson(ItemStack stack) {

		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		double scale = 0.375D;
		GlStateManager.scale(scale, scale, scale);

		double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
		double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
		double[] turn = HbmAnimationsSedna.getRelevantTransformation("TURN");
		double[] barrel = HbmAnimationsSedna.getRelevantTransformation("BARREL");
		double[] lift = HbmAnimationsSedna.getRelevantTransformation("LIFT");
		double[] shells = HbmAnimationsSedna.getRelevantTransformation("SHELLS");
		double[] shellFlip = HbmAnimationsSedna.getRelevantTransformation("SHELL_FLIP");
		double[] lever = HbmAnimationsSedna.getRelevantTransformation("LEVER");
		double[] buckle = HbmAnimationsSedna.getRelevantTransformation("BUCKLE");
		double[] no_ammo = HbmAnimationsSedna.getRelevantTransformation("NO_AMMO");

		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		GlStateManager.translate(recoil[0] * 3, recoil[1], recoil[2]);
		GlStateManager.rotate((float) (recoil[2] * 10), 1, 0, 0);

		GlStateManager.translate(0, 0, -4);
		GlStateManager.rotate((float) equip[0], -1, 0, 0);
		GlStateManager.translate(0, 0, 4);

		GlStateManager.translate(0, 0, -4);
		GlStateManager.rotate((float) turn[1], 0, 1, 0);
		GlStateManager.translate(0, 0, 4);

		GlStateManager.translate(0, 0, -4);
		GlStateManager.rotate((float) lift[0], -1, 0, 0);
		GlStateManager.translate(0, 0, 4);

		ResourceManager.double_barrel.renderPart("Stock");

		GlStateManager.pushMatrix();

		GlStateManager.translate(0, -0.4375, -0.875);
		GlStateManager.rotate((float) barrel[0], 1, 0, 0);
		GlStateManager.translate(0, 0.4375, 0.875);

		ResourceManager.double_barrel.renderPart("BarrelShort");
		if(!isSawedOff(stack)) ResourceManager.double_barrel.renderPart("Barrel");

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.75, 0, -0.6875);
		GlStateManager.rotate((float) buckle[1], 0, 1, 0);
		GlStateManager.translate(-0.75, 0, 0.6875);
		ResourceManager.double_barrel.renderPart("Buckle");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(-0.3125, 0.3125, 0);
		GlStateManager.rotate((float) lever[2], 0, 0, 1);
		GlStateManager.translate(0.3125, -0.3125, 0);
		ResourceManager.double_barrel.renderPart("Lever");
		GlStateManager.popMatrix();

		if(no_ammo[0] == 0) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(shells[0], shells[1], shells[2]);
			GlStateManager.translate(0, 0, -1);
			GlStateManager.rotate((float) shellFlip[0], 1, 0, 0);
			GlStateManager.translate(0, 0, 1);
			ResourceManager.double_barrel.renderPart("Shells");
			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();

		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 8);
		GlStateManager.rotate(90F, 0, 1, 0);
		GlStateManager.rotate(90F * gun.shotRand, 1, 0, 0);
		GlStateManager.scale(2, 2, 2);
		this.renderMuzzleFlash(gun.lastShot[0], 75, 5);
		GlStateManager.popMatrix();
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		double scale = 1.75D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(0, 1, 3);
	}

	@Override
	public void setupInv(ItemStack stack) {
		super.setupInv(stack);
		if(isSawedOff(stack)) {
			double scale = 2D;
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.rotate(25F, 1, 0, 0);
			GlStateManager.rotate(45F, 0, 1, 0);
			GlStateManager.translate(-2, 0.5, 0);
		} else {
			double scale = 1.375D;
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.rotate(25F, 1, 0, 0);
			GlStateManager.rotate(45F, 0, 1, 0);
			GlStateManager.translate(0, 0.5, 0);
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
		ResourceManager.double_barrel.renderPart("Stock");
		ResourceManager.double_barrel.renderPart("BarrelShort");
		if(!isSawedOff(stack)) ResourceManager.double_barrel.renderPart("Barrel");
		ResourceManager.double_barrel.renderPart("Buckle");
		ResourceManager.double_barrel.renderPart("Lever");
		ResourceManager.double_barrel.renderPart("Shells");
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	public boolean isSawedOff(ItemStack stack) {
		return stack.getItem() == ModItems.gun_double_barrel_sacred_dragon;
	}
}

