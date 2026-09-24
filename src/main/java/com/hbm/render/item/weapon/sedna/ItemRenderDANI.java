package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "gun_light_revolver_dani")
public class ItemRenderDANI extends ItemRenderWeaponBase {

	@Override
	public boolean isAkimbo() {
		return true;
	}

	@Override
	protected float getTurnMagnitude(ItemStack stack) {
		return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.25F;
	}

	@Override
	public void setupFirstPerson(ItemStack stack) {
		GlStateManager.translate(0, 0, 0.875);
	}

	@Override
	public void renderFirstPerson(ItemStack stack) {

		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
		float offset = 0.8F;

		for(int i = -1; i <= 1; i += 2) {

			int index = i == -1 ? 0 : 1;
			Minecraft.getMinecraft().getTextureManager().bindTexture(index == 0 ? ResourceManager.dani_celestial_tex : ResourceManager.dani_lunar_tex);

			GlStateManager.pushMatrix();

			standardAimingTransform(stack,
					-1.5F * offset * i, -0.75F * offset, 1F * offset,
					0, -3.125 / 8D, 0.25);

			double scale = 0.125D;
			GlStateManager.scale(scale, scale, scale);

			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL", index);
			double[] reloadMove = HbmAnimationsSedna.getRelevantTransformation("RELOAD_MOVE", index);
			double[] reloadRot = HbmAnimationsSedna.getRelevantTransformation("RELOAD_ROT", index);
			double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP", index);

			GlStateManager.translate(recoil[0], recoil[1], recoil[2]);
			GlStateManager.rotate((float) (recoil[2] * 10), 1, 0, 0);

			GlStateManager.translate(0, -2, -2);
			GlStateManager.rotate((float) equip[0], -1, 0, 0);
			GlStateManager.translate(0, 2, 2);

			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 1.5, 9.25);
			GlStateManager.rotate((float) (-recoil[2] * 10), 1, 0, 0);
			GlStateManager.rotate(90F, 0, 1, 0);
			this.renderSmokeNodes(gun.getConfig(stack, index).smokeNodes, 0.5D);
			GlStateManager.popMatrix();

			GlStateManager.translate(reloadMove[0], reloadMove[1], reloadMove[2]);

			GlStateManager.rotate((float) reloadRot[0], 1, 0, 0);
			GlStateManager.rotate((float) (reloadRot[2] * i), 0, 0, 1);
			GlStateManager.rotate((float) (reloadRot[1] * i), 0, 1, 0);
			ResourceManager.bio_revolver.renderPart("Grip");

			GlStateManager.pushMatrix(); /// FRONT PUSH ///
			GlStateManager.rotate((float) HbmAnimationsSedna.getRelevantTransformation("FRONT", index)[2], 1, 0, 0);
			ResourceManager.bio_revolver.renderPart("Barrel");
			GlStateManager.pushMatrix(); /// LATCH PUSH ///
			GlStateManager.translate(0, 2.3125, -0.875);
			GlStateManager.rotate((float) HbmAnimationsSedna.getRelevantTransformation("LATCH", index)[2], 1, 0, 0);
			GlStateManager.translate(0, -2.3125, 0.875);
			ResourceManager.bio_revolver.renderPart("Latch");
			GlStateManager.popMatrix(); /// LATCH POP ///

			GlStateManager.pushMatrix(); /// DRUM PUSH ///
			GlStateManager.translate(0, 1, 0);
			GlStateManager.rotate((float) (HbmAnimationsSedna.getRelevantTransformation("DRUM", index)[2] * 60), 0, 0, 1);
			GlStateManager.translate(0, -1, 0);
			GlStateManager.translate(0, 0, HbmAnimationsSedna.getRelevantTransformation("DRUM_PUSH", index)[2]);
			ResourceManager.bio_revolver.renderPart("Drum");
			GlStateManager.popMatrix(); /// DRUM POP ///

			GlStateManager.popMatrix(); /// FRONT POP ///

			GlStateManager.pushMatrix(); /// HAMMER ///
			GlStateManager.translate(0, 0, -4.5);
			GlStateManager.rotate((float) (-45 + 45 * HbmAnimationsSedna.getRelevantTransformation("HAMMER", index)[2]), 1, 0, 0);
			GlStateManager.translate(0, 0, 4.5);
			ResourceManager.bio_revolver.renderPart("Hammer");
			GlStateManager.popMatrix();

			GlStateManager.shadeModel(GL11.GL_FLAT);

			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 1.5, 9.25);
			GlStateManager.rotate(90F, 0, 1, 0);
			this.renderMuzzleFlash(gun.lastShot[index], 75, 7.5);
			GlStateManager.popMatrix();

			GlStateManager.popMatrix();
		}
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		GlStateManager.translate(0, 1, 3);
	}

	@Override
	public void setupThirdPersonAkimbo(ItemStack stack) {
		super.setupThirdPersonAkimbo(stack);
		GlStateManager.translate(0, 1, 3);
	}

	@Override
	public void setupInv(ItemStack stack) {
		GlStateManager.scale(1, 1, -1);
		GlStateManager.translate(8, 6, 0);
		double scale = 1.125D;
		GlStateManager.scale(scale, scale, scale);
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -5D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.translate(0, 1.5, 0);
	}

	@Override
	public void renderInv(ItemStack stack) {

		GlStateManager.enableLighting();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		GlStateManager.pushMatrix();
		GlStateManager.rotate(225F, 0, 0, 1);
		GlStateManager.rotate(90F, 0, 1, 0);
		GlStateManager.rotate(25F, 1, 0, 0);
		GlStateManager.rotate(45F, 0, 1, 0);
		GlStateManager.translate(2, 0, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.dani_celestial_tex);
		ResourceManager.bio_revolver.renderAll();
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotate(-225, 0, 0, 1);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.rotate(25, 1, 0, 0);
		GlStateManager.rotate(45, 0, 1, 0);
		GlStateManager.rotate(180, 0, 1, 0);
		GlStateManager.translate(2, 0, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.dani_lunar_tex);
		ResourceManager.bio_revolver.renderAll();
		GlStateManager.popMatrix();

		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	@Override
	public void renderEquipped(ItemStack stack) {
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.dani_lunar_tex);
		ResourceManager.bio_revolver.renderAll();
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	@Override
	public void renderEquippedAkimbo(ItemStack stack) {
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.dani_celestial_tex);
		ResourceManager.bio_revolver.renderAll();
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	@Override
	public void renderModTable(ItemStack stack, int index) {
		GlStateManager.enableLighting();

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().renderEngine.bindTexture(index == 1 ? ResourceManager.dani_celestial_tex : ResourceManager.dani_lunar_tex);
		ResourceManager.bio_revolver.renderAll();
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.dani_celestial_tex);
		ResourceManager.bio_revolver.renderAll();
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}
}

