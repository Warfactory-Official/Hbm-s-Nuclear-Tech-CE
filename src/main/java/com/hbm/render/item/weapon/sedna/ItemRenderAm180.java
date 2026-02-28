package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "gun_am180")
public class ItemRenderAm180 extends ItemRenderWeaponBase {

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
				-1F * offset, -1F * offset, offset,
				0, -4.1875 / 8D, 0.25);
	}

	@Override
	public void renderFirstPerson(ItemStack stack) {

		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.am180_tex);
		double scale = 0.1875D;
		GlStateManager.scale(scale, scale, scale);

		boolean silenced = this.hasSilencer(stack);

		double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
		double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
		double[] magazine = HbmAnimationsSedna.getRelevantTransformation("MAG");
		double[] magTurn = HbmAnimationsSedna.getRelevantTransformation("MAGTURN");
		double[] magSpin = HbmAnimationsSedna.getRelevantTransformation("MAGSPIN");
		double[] bolt = HbmAnimationsSedna.getRelevantTransformation("BOLT");
		double[] turn = HbmAnimationsSedna.getRelevantTransformation("TURN");

		GlStateManager.translate(0, -2, -6);
		GlStateManager.rotate((float) equip[0], 1, 0, 0);
		GlStateManager.translate(0, 2, 6);

		GlStateManager.rotate((float) turn[2], 0, 0, 1);

		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		GlStateManager.translate(0, 0, recoil[2]);

		HbmAnimationsSedna.applyRelevantTransformation("Gun");
		ResourceManager.am180.renderPart("Gun");
		if(silenced) ResourceManager.am180.renderPart("Silencer");

		GlStateManager.pushMatrix();
		HbmAnimationsSedna.applyRelevantTransformation("Trigger");
		ResourceManager.am180.renderPart("Trigger");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, bolt[2]);
		HbmAnimationsSedna.applyRelevantTransformation("Bolt");
		ResourceManager.am180.renderPart("Bolt");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(magazine[0], magazine[1], magazine[2]);

		GlStateManager.translate(0, 2.0625, 3.75);
		GlStateManager.rotate((float) magTurn[0], 1, 0, 0);
		GlStateManager.rotate((float) magTurn[2], 0, 0, 1);
		GlStateManager.translate(0, -2.0625, -3.75);

		GlStateManager.translate(0, 2.3125, 1.5);
		GlStateManager.rotate((float) magSpin[0], 1, 0, 0);
		GlStateManager.translate(0, -2.3125, -1.5);

		HbmAnimationsSedna.applyRelevantTransformation("Mag");

		GlStateManager.pushMatrix();
		int mag = gun.getConfig(stack, 0).getReceivers(stack)[0]
				.getMagazine(stack).getAmount(stack, MainRegistry.proxy.me().inventory);
		GlStateManager.translate(0, 0, 1.5);
		GlStateManager.rotate((float) (mag / 59D * 360D), 0, -1, 0);
		GlStateManager.translate(0, 0, -1.5);
		ResourceManager.am180.renderPart("Mag");
		GlStateManager.popMatrix();

		ResourceManager.am180.renderPart("MagPlate");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1.875, silenced ? 17 : 13);
		GlStateManager.rotate((float) turn[2], 0, 0, -1);
		GlStateManager.rotate(90F, 0, 1, 0);
		this.renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 0.25D);
		GlStateManager.popMatrix();

		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1.875, silenced ? 16.75 : 12);
		GlStateManager.rotate(90F, 0, 1, 0);
		GlStateManager.rotate(90F * gun.shotRand, 1, 0, 0);
		double flashScale = silenced ? 0.5 : 0.75;
		GlStateManager.scale(flashScale, flashScale, flashScale);
		this.renderMuzzleFlash(gun.lastShot[0], silenced ? 75 : 50, silenced ? 5 : 7.5);
		GlStateManager.popMatrix();
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		double scale = 1D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(0, -0.5, 3);
	}

	@Override
	public void setupInv(ItemStack stack) {
		super.setupInv(stack);
		double scale = 0.75D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(25F, 1, 0, 0);
		GlStateManager.rotate(45F, 0, 1, 0);
		GlStateManager.translate(1.5, 0, 0);
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -5D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.translate(0, 0, -2);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {
		GlStateManager.enableLighting();

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.am180_tex);
		ResourceManager.am180.renderPart("Gun");
		if(this.hasSilencer(stack)) ResourceManager.am180.renderPart("Silencer");
		ResourceManager.am180.renderPart("Trigger");
		ResourceManager.am180.renderPart("Bolt");
		ResourceManager.am180.renderPart("Mag");
		ResourceManager.am180.renderPart("MagPlate");
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	public boolean hasSilencer(ItemStack stack) {
		return XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SILENCER);
	}
}

