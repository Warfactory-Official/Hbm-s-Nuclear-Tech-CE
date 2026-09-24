package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
@AutoRegister(item = "gun_aberrator_eott")
public class ItemRenderEOTT extends ItemRenderWeaponBase {

	public ItemRenderEOTT() {}

	@Override
	public boolean isAkimbo() { return true; }

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
		GlStateManager.translate(0, 0, 1);
	}

	@Override
	public void renderFirstPerson(ItemStack stack) {
		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
		float offset = 0.8F;

		for(int i = -1; i <= 1; i += 2) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.eott_tex);
			int index = i == -1 ? 0 : 1;

			GlStateManager.pushMatrix();
			standardAimingTransform(stack,
					-1.0F * offset * i, -1.25F * offset, 1.25F * offset,
					0, -5.25 / 8D, 0.125);

			double scale = 0.25D;
			GlStateManager.scale(scale, scale, scale);

			double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP", index);
			double[] rise = HbmAnimationsSedna.getRelevantTransformation("RISE", index);
			double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL", index);
			double[] slide = HbmAnimationsSedna.getRelevantTransformation("SLIDE", index);
			double[] bullet = HbmAnimationsSedna.getRelevantTransformation("BULLET", index);
			double[] hammer = HbmAnimationsSedna.getRelevantTransformation("HAMMER", index);
			double[] roll = HbmAnimationsSedna.getRelevantTransformation("ROLL", index);
			double[] mag = HbmAnimationsSedna.getRelevantTransformation("MAG", index);
			double[] magroll = HbmAnimationsSedna.getRelevantTransformation("MAGROLL", index);
			double[] sight = HbmAnimationsSedna.getRelevantTransformation("SIGHT", index);

			GlStateManager.translate(0, rise[1], 0);

			GlStateManager.translate(0, 1, -2.25);
			GlStateManager.rotate((float) equip[0], 1, 0, 0);
			GlStateManager.translate(0, -1, 2.25);

			GlStateManager.translate(0, -1, -4);
			GlStateManager.rotate((float) recoil[0], 1, 0, 0);
			GlStateManager.translate(0, 1, 4);

			GlStateManager.translate(0, 1, 0);
			GlStateManager.rotate((float) (roll[2] * i), 0, 0, 1);
			GlStateManager.translate(0, -1, 0);

			GlStateManager.shadeModel(GL11.GL_SMOOTH);

			ResourceManager.aberrator.renderPart("Gun");

			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 2.4375, -1.9375);
			GlStateManager.rotate((float) sight[0], 1, 0, 0);
			GlStateManager.translate(0, -2.4375, 1.9375);
			ResourceManager.aberrator.renderPart("Sight");
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.translate(mag[0] * i, mag[1], mag[2]);

			GlStateManager.translate(0, 1, 0);
			GlStateManager.rotate((float) (magroll[2] * i), 0, 0, 1);
			GlStateManager.translate(0, -1, 0);

			ResourceManager.aberrator.renderPart("Magazine");
			GlStateManager.translate(bullet[0], bullet[1], bullet[2]);
			ResourceManager.aberrator.renderPart("Bullet");
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, slide[2]);
			ResourceManager.aberrator.renderPart("Slide");
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 1.25, -3.625);
			GlStateManager.rotate((float) (-45 + hammer[0]), 1, 0, 0);
			GlStateManager.translate(0, -1.25, 3.625);
			ResourceManager.aberrator.renderPart("Hammer");
			GlStateManager.popMatrix();

			double smokeScale = 0.5;

			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 2, 4);
			GlStateManager.rotate((float) recoil[0], -1, 0, 0);
			GlStateManager.rotate((float) (roll[2] * i), 0, 0, -1);
			GlStateManager.rotate(90F, 0, 1, 0);
			GlStateManager.scale(smokeScale, smokeScale, smokeScale);
			this.renderSmokeNodes(gun.getConfig(stack, index).smokeNodes, 0.5D);
			GlStateManager.popMatrix();

			GlStateManager.shadeModel(GL11.GL_FLAT);

			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 2, 4);
			GlStateManager.rotate(90F, 0, 1, 0);
			GlStateManager.rotate(90F * gun.shotRand, 1, 0, 0);
			GlStateManager.scale(0.75, 0.75, 0.75);
			this.renderMuzzleFlash(gun.lastShot[index], 75, 7.5);
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 2, -1.5);
			GlStateManager.scale(0.5, 0.5, 0.5);
			this.renderFireball(gun.lastShot[index]);
			GlStateManager.popMatrix();

			GlStateManager.popMatrix();
		}
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		GlStateManager.translate(0, -1, 4);
		double scale = 1.5D;
		GlStateManager.scale(scale, scale, scale);
	}

	public void setupThirdPersonAkimbo(ItemStack stack) {
		super.setupThirdPersonAkimbo(stack);
		GlStateManager.translate(0, -1, 4);
		double scale = 1.5D;
		GlStateManager.scale(scale, scale, scale);
	}

	@Override
	public void setupInv(ItemStack stack) {
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
		GlStateManager.translate(0,-1, 0.5);
	}

	@Override
	public void renderInv(ItemStack stack) {

		GlStateManager.enableLighting();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.translate(0, 1, 0);

		GlStateManager.pushMatrix();
		GlStateManager.rotate(225F, 0, 0, 1);
		GlStateManager.rotate(90F, 0, 1, 0);
		GlStateManager.rotate(25F, 1, 0, 0);
		GlStateManager.rotate(45F, 0, 1, 0);
		GlStateManager.translate(-1, 0, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.eott_tex);
		ResourceManager.aberrator.renderPart("Gun");
		ResourceManager.aberrator.renderPart("Hammer");
		ResourceManager.aberrator.renderPart("Magazine");
		ResourceManager.aberrator.renderPart("Slide");
		ResourceManager.aberrator.renderPart("Sight");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotate(-225, 0, 0, 1);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.rotate(25, 1, 0, 0);
		GlStateManager.rotate(45, 0, 1, 0);
		GlStateManager.rotate(180, 0, 1, 0);
		GlStateManager.translate(1, 3, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.eott_tex);
		ResourceManager.aberrator.renderPart("Gun");
		ResourceManager.aberrator.renderPart("Hammer");
		ResourceManager.aberrator.renderPart("Magazine");
		ResourceManager.aberrator.renderPart("Slide");
		ResourceManager.aberrator.renderPart("Sight");
		GlStateManager.popMatrix();

		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	@Override
	public void renderEquipped(ItemStack stack) {
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.eott_tex);
		ResourceManager.aberrator.renderPart("Gun");
		ResourceManager.aberrator.renderPart("Hammer");
		ResourceManager.aberrator.renderPart("Magazine");
		ResourceManager.aberrator.renderPart("Slide");
		ResourceManager.aberrator.renderPart("Sight");
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	@Override
	public void renderEquippedAkimbo(ItemStack stack) {
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.eott_tex);
		ResourceManager.aberrator.renderPart("Gun");
		ResourceManager.aberrator.renderPart("Hammer");
		ResourceManager.aberrator.renderPart("Magazine");
		ResourceManager.aberrator.renderPart("Slide");
		ResourceManager.aberrator.renderPart("Sight");
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {
		GlStateManager.enableLighting();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
		GlStateManager.enableAlpha();

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.eott_tex);
		ResourceManager.aberrator.renderPart("Gun");
		ResourceManager.aberrator.renderPart("Hammer");
		ResourceManager.aberrator.renderPart("Magazine");
		ResourceManager.aberrator.renderPart("Slide");
		ResourceManager.aberrator.renderPart("Sight");
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	public static void renderFireball(long lastShot) {
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buff = tess.getBuffer();

		int flash = 150;

		if (System.currentTimeMillis() - lastShot < flash) {

			double fire = (System.currentTimeMillis() - lastShot) / (double) flash;
			double height = 5 * fire;
			double length = 10 * fire;
			double offset = 1 * fire;
			double lengthOffset = -1.125;

			Minecraft.getMinecraft().getTextureManager().bindTexture(flash_plume);
			beginFullbrightAdditive();

			buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			buff.pos(height, -offset, 0).tex(0, 1).endVertex();
			buff.pos(-height, -offset, 0).tex(1, 1).endVertex();
			buff.pos(-height, -offset + length, -lengthOffset).tex(1, 0).endVertex();
			buff.pos(height, -offset + length, -lengthOffset).tex(0, 0).endVertex();

			buff.pos(height, -offset, 0).tex(0, 1).endVertex();
			buff.pos(-height, -offset, 0).tex(1, 1).endVertex();
			buff.pos(-height, -offset + length, lengthOffset).tex(1, 0).endVertex();
			buff.pos(height, -offset + length, lengthOffset).tex(0, 0).endVertex();

			tess.draw();

			endFullbrightAdditive();
		}
	}
}

