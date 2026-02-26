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
public class ItemRenderG3 extends ItemRenderWeaponBase {

	public ResourceLocation texture;

	public ItemRenderG3(ResourceLocation texture) {
		this.texture = texture;
		this.offsets = offsets.get(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND).setScale(0.85).setPosition(-0.85, 0.1, -1.1).getHelper();
	}

	@Override
	protected float getTurnMagnitude(ItemStack stack) { return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.25F; }

	@Override
	public float getViewFOV(ItemStack stack, float fov) {
		float aimingProgress = ItemGunBaseNT.prevAimingProgress + (ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
		return fov * (1 - aimingProgress * 0.33F);
	}

	@Override
	public void setupFirstPerson(ItemStack stack) {
		GlStateManager.translate(0, 0, 0.875);
		boolean isScoped = this.isScoped(stack);
		float offset = 0.8F;
		standardAimingTransform(stack,
				-1.25F * offset, -1F * offset, 2.75F * offset,
				0, isScoped ? (-5.53125 / 8D) : (-3.5625 / 8D), isScoped ? 1.46875 : 1.75);
	}

	@Override
	public void renderFirstPerson(ItemStack stack) {

		boolean isScoped = this.isScoped(stack);
		if(isScoped && ItemGunBaseNT.prevAimingProgress == 1 && ItemGunBaseNT.aimingProgress == 1) return;

		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.g3_tex);
		double scale = 0.375D;
		GlStateManager.scale(scale, scale, scale);

		double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
		double[] lift = HbmAnimationsSedna.getRelevantTransformation("LIFT");
		double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
		double[] mag = HbmAnimationsSedna.getRelevantTransformation("MAG");
		double[] speen = HbmAnimationsSedna.getRelevantTransformation("SPEEN");
		double[] bolt = HbmAnimationsSedna.getRelevantTransformation("BOLT");
		double[] plug = HbmAnimationsSedna.getRelevantTransformation("PLUG");
		double[] handle = HbmAnimationsSedna.getRelevantTransformation("HANDLE");
		double[] bullet = HbmAnimationsSedna.getRelevantTransformation("BULLET");

		GlStateManager.translate(0, -2, -6);
		GlStateManager.rotate(equip[0], 1, 0, 0);
		GlStateManager.translate(0, 2, 6);

		GlStateManager.translate(0, 0, -4);
		GlStateManager.rotate(lift[0], 1, 0, 0);
		GlStateManager.translate(0, 0, 4);

		GlStateManager.translate(0, 0, recoil[2]);

		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		ResourceManager.g3.renderPart("Rifle");
		if(hasStock(stack)) ResourceManager.g3.renderPart("Stock");
		boolean silenced = hasSilencer(stack);
		if(!silenced) ResourceManager.g3.renderPart("Flash_Hider");
		ResourceManager.g3.renderPart("Trigger");

		GlStateManager.pushMatrix();
		GlStateManager.translate(mag[0], mag[1], mag[2]);
		GlStateManager.translate(0, -1.75, -0.5);
		GlStateManager.rotate(speen[2], 0, 0, 1);
		GlStateManager.rotate(speen[1], 0, 1, 0);
		GlStateManager.translate(0, 1.75, 0.5);
		ResourceManager.g3.renderPart("Magazine");
		if(bullet[0] == 0) ResourceManager.g3.renderPart("Bullet");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, bolt[2]);
		ResourceManager.g3.renderPart("Guide_And_Bolt");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0.625, plug[2]);
		GlStateManager.rotate(handle[2], 0, 0, 1);
		GlStateManager.translate(0, -0.625, 0);
		ResourceManager.g3.renderPart("Plug");

		GlStateManager.translate(0, 0.625, 5.25);
		GlStateManager.rotate(22.5, 0, 0, 1);
		GlStateManager.rotate(handle[1], 0, 1, 0);
		GlStateManager.rotate(-22.5, 0, 0, 1);
		GlStateManager.translate(0, -0.625, -5.25);
		ResourceManager.g3.renderPart("Handle");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, -0.875, -3.5);
		GlStateManager.rotate(-30 * (1 - ItemGunBaseNT.getMode(stack, 0)), 1, 0, 0);
		GlStateManager.translate(0, 0.875, 3.5);
		ResourceManager.g3.renderPart("Selector");
		GlStateManager.popMatrix();

		if(silenced || isScoped) {
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.g3_attachments);
			if(silenced) ResourceManager.g3.renderPart("Silencer");
			if(isScoped) ResourceManager.g3.renderPart("Scope");
		}

		if(!silenced) {
			double smokeScale = 0.75;

			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 13);
			GlStateManager.rotate(90, 0, 1, 0);
			GlStateManager.scale(smokeScale, smokeScale, smokeScale);
			this.renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 0.5D);
			GlStateManager.popMatrix();

			GlStateManager.shadeModel(GL11.GL_FLAT);

			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 12);
			GlStateManager.rotate(90, 0, 1, 0);
			GlStateManager.rotate(-25 + gun.shotRand * 10, 1, 0, 0);
			GlStateManager.scale(0.75, 0.75, 0.75);
			this.renderMuzzleFlash(gun.lastShot[0], 75, 10);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		double scale = 1D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(0, 2, 4);
	}

	@Override
	public void setupInv(ItemStack stack) {
		super.setupInv(stack);
		if(hasStock(stack)) {
			double scale = 0.875D;
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.rotate(25, 1, 0, 0);
			GlStateManager.rotate(45, 0, 1, 0);
			GlStateManager.translate(-0.5, 0.5, 0);
		} else {
			double scale = 1.125D;
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.rotate(25, 1, 0, 0);
			GlStateManager.rotate(hasSilencer(stack) ? 55 : 45, 0, 1, 0); //preserves proportions whilst limiting size
			GlStateManager.translate(2.5, 0.5, 0);
		}
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -5D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.translate(0, 0.5, -0.5);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {
		GlStateManager.enableLighting();

		boolean silenced = hasSilencer(stack);
		boolean isScoped = this.isScoped(stack);

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().renderEngine.bindTexture(getTexture(stack));
		ResourceManager.g3.renderPart("Rifle");
		if(hasStock(stack)) ResourceManager.g3.renderPart("Stock");
		ResourceManager.g3.renderPart("Magazine");
		if(!silenced)ResourceManager.g3.renderPart("Flash_Hider");
		ResourceManager.g3.renderPart("Guide_And_Bolt");
		ResourceManager.g3.renderPart("Handle");
		ResourceManager.g3.renderPart("Trigger");

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, -0.875, -3.5);
		GlStateManager.rotate(-30, 1, 0, 0);
		GlStateManager.translate(0, 0.875, 3.5);
		ResourceManager.g3.renderPart("Selector_Rifle.001");
		GlStateManager.popMatrix();

		if(silenced || isScoped) {
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.g3_attachments);
			if(silenced) ResourceManager.g3.renderPart("Silencer");
			if(isScoped) ResourceManager.g3.renderPart("Scope");
		}
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	public boolean hasStock(ItemStack stack) {
		return !XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_NO_STOCK);
	}

	public boolean hasSilencer(ItemStack stack) {
		return stack.getItem() == ModItems.gun_g3_zebra || XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SILENCER);
	}

	public boolean isScoped(ItemStack stack) {
		return stack.getItem() == ModItems.gun_g3_zebra || XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SCOPE);
	}

	public ResourceLocation getTexture(ItemStack stack) {
		if(XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_FURNITURE_GREEN)) return ResourceManager.g3_green_tex;
		if(XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_FURNITURE_BLACK)) return ResourceManager.g3_black_tex;
		return texture;
	}
}

