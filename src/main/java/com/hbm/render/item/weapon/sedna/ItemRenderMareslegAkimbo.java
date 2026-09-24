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
@AutoRegister(item = "gun_maresleg_akimbo")
public class ItemRenderMareslegAkimbo extends ItemRenderWeaponBase {

	public ItemRenderMareslegAkimbo() {}

	@Override
	public boolean isAkimbo() { return true; }

	@Override
	protected float getTurnMagnitude(ItemStack stack) {
		return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.5F;
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

			Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.maresleg_tex);
			GlStateManager.pushMatrix();

			int index = i == -1 ? 0 : 1;

			standardAimingTransform(stack, -1.5F * offset * i, -1F * offset, 2F * offset, 0, -3.875 / 8D, 1);

			double scale = 0.375D;
			GlStateManager.scale(scale, scale, scale);

			double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP", index);
			double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL", index);
			double[] lever = HbmAnimationsSedna.getRelevantTransformation("LEVER", index);
			double[] turn = HbmAnimationsSedna.getRelevantTransformation("TURN", index);
			double[] flip = HbmAnimationsSedna.getRelevantTransformation("FLIP", index);
			double[] lift = HbmAnimationsSedna.getRelevantTransformation("LIFT", index);
			double[] shell = HbmAnimationsSedna.getRelevantTransformation("SHELL", index);
			double[] flag = HbmAnimationsSedna.getRelevantTransformation("FLAG", index);

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
			GlStateManager.translate(0, 1, 3.75);
			GlStateManager.rotate((float) turn[2], 0, 0, -1);
			GlStateManager.rotate((float) flip[0], 1, 0, 0);
			GlStateManager.rotate(90, 0, 1, 0);
			this.renderSmokeNodes(gun.getConfig(stack, index).smokeNodes, 0.25D);
			GlStateManager.popMatrix();

			ResourceManager.maresleg.renderPart("Gun");

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
			GlStateManager.translate(0, 1, 3.75);
			GlStateManager.rotate(90, 0, 1, 0);
			GlStateManager.rotate(90 * gun.shotRand, 1, 0, 0);
			this.renderMuzzleFlash(gun.lastShot[index], 75, 5);
			GlStateManager.popMatrix();

			GlStateManager.popMatrix();
		}
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		double scale = 1.75D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(0, 0.25, 3);
	}

	@Override
	public void setupThirdPersonAkimbo(ItemStack stack) {
		super.setupThirdPersonAkimbo(stack);
		double scale = 1.75D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(0, 0.25, 3);
	}

	@Override
	public void setupInv(ItemStack stack) {
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
		GlStateManager.enableAlpha();
		GlStateManager.scale(1, 1, -1);
		GlStateManager.translate(8, 8, 0);
		double scale = 2.5D;
		GlStateManager.scale(scale, scale, scale);
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -12.5D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.translate(0, -0.5, 1);
	}

	@Override
	public void renderInv(ItemStack stack) {

		GlStateManager.enableLighting();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.maresleg_tex);

		GlStateManager.pushMatrix();
		GlStateManager.rotate(225, 0, 0, 1);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.rotate(25, 1, 0, 0);
		GlStateManager.rotate(45, 0, 1, 0);
		GlStateManager.translate(-1, 0, 0);
		ResourceManager.maresleg.renderPart("Gun");
		ResourceManager.maresleg.renderPart("Lever");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotate(-225, 0, 0, 1);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.rotate(25, 1, 0, 0);
		GlStateManager.rotate(45, 0, 1, 0);
		GlStateManager.rotate(180, 0, 1, 0);
		GlStateManager.translate(1.2, 2.25, 0);
		ResourceManager.maresleg.renderPart("Gun");
		ResourceManager.maresleg.renderPart("Lever");
		GlStateManager.popMatrix();

		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.maresleg_tex);
		ResourceManager.maresleg.renderPart("Gun");
		ResourceManager.maresleg.renderPart("Lever");
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}
}

