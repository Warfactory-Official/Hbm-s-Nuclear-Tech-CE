package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.mods.WeaponModManager;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.nio.DoubleBuffer;
@AutoRegister(item = "gun_mas36")
public class ItemRenderMAS36 extends ItemRenderWeaponBase {

	@Override
	protected float getTurnMagnitude(ItemStack stack) {
		return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.5F;
	}

	@Override
	public float getViewFOV(ItemStack stack, float fov) {
		float aimingProgress = ItemGunBaseNT.prevAimingProgress + (ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
		return  fov * (1 - aimingProgress * (isScoped(stack) ? 0.66F : 0.33F));
	}

	@Override
	public void setupFirstPerson(ItemStack stack) {
		GlStateManager.translate(0, 0, 0.875);

		float offset = 0.8F;
		if(isScoped(stack)) {
			standardAimingTransform(stack,
					-1.5F * offset, -1.25F * offset, 1.75F * offset,
					-0.2, -5.875 / 8D, 1.125);
		} else {
			standardAimingTransform(stack,
					-1.5F * offset, -1.25F * offset, 1.75F * offset,
					0, -4.6825 / 8D, 0.75);
		}
	}

	private static DoubleBuffer buf = null;

	@Override
	public void renderFirstPerson(ItemStack stack) {
		boolean isScoped = isScoped(stack);
		if(isScoped && ItemGunBaseNT.prevAimingProgress == 1 && ItemGunBaseNT.aimingProgress == 1) return;
		if(buf == null) buf = GLAllocation.createDirectByteBuffer(8*4).asDoubleBuffer();

		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.mas36_tex);
		double scale = 0.375D;
		GlStateManager.scale(scale, scale, scale);

		double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
		double[] lift = HbmAnimationsSedna.getRelevantTransformation("LIFT");
		double[] stock = HbmAnimationsSedna.getRelevantTransformation("STOCK");
		double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
		double[] boltTurn = HbmAnimationsSedna.getRelevantTransformation("BOLT_TURN");
		double[] boltPull = HbmAnimationsSedna.getRelevantTransformation("BOLT_PULL");
		double[] bullet = HbmAnimationsSedna.getRelevantTransformation("BULLET");
		double[] showClip = HbmAnimationsSedna.getRelevantTransformation("SHOW_CLIP");
		double[] clip = HbmAnimationsSedna.getRelevantTransformation("CLIP");
		double[] bullets = HbmAnimationsSedna.getRelevantTransformation("BULLETS");
		double[] stab = HbmAnimationsSedna.getRelevantTransformation("STAB");

		GlStateManager.translate(0, -3, -3);
		GlStateManager.rotate((float) equip[0], 1, 0, 0);
		GlStateManager.rotate((float) lift[0], 1, 0, 0);
		GlStateManager.translate(0, 3, 3);

		GL11.glTranslated(stab[0], stab[1], stab[2]);

		GlStateManager.translate(0, 0, recoil[2]);

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		ResourceManager.mas36.renderPart("Gun");
		if(hasBayonet(stack))  ResourceManager.mas36.renderPart("Bayonet");

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0.3125, -2.125);
		GlStateManager.rotate((float) stock[0], 1, 0, 0);
		GlStateManager.translate(0, -0.3125, 2.125);
		ResourceManager.mas36.renderPart("Stock");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1.125, 0);
		GlStateManager.rotate((float) boltTurn[2], 0, 0, 1);
		GlStateManager.translate(0, -1.125, 0);
		GlStateManager.translate(0, 0, boltPull[2]);
		ResourceManager.mas36.renderPart("Bolt");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(bullet[0], bullet[1], bullet[2]);
		ResourceManager.mas36.renderPart("Bullet");
		GlStateManager.popMatrix();

		if(isScoped) ResourceManager.mas36.renderPart("Scope");

		if(showClip[0] != 0) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(clip[0], clip[1], clip[2]);
			ResourceManager.mas36.renderPart("Clip");
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			if(bullets[0] == 0) GL11.glEnable(GL11.GL_CLIP_PLANE0);
			buf.put(new double[] { 0, 1, 0, -0.5} );
			buf.rewind();
			GL11.glClipPlane(GL11.GL_CLIP_PLANE0, buf);
			GlStateManager.translate(bullets[0], bullets[1], bullets[2]);
			ResourceManager.mas36.renderPart("Bullets");
			GL11.glDisable(GL11.GL_CLIP_PLANE0);
			GlStateManager.popMatrix();
		}

		double smokeScale = 0.25;

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1.125, 8);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.scale(smokeScale, smokeScale, smokeScale);
		renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 1D);
		GlStateManager.popMatrix();

		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1, 8);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.rotate(90 * gun.shotRand, 1, 0, 0);
		GlStateManager.scale(0.5, 0.5, 0.5);
		renderMuzzleFlash(gun.lastShot[0], 75, 7.5);
		GlStateManager.popMatrix();
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		double scale = 1.5D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(0, 0.5, 3);
	}

	@Override
	public void setupInv(ItemStack stack) {
		super.setupInv(stack);
		double scale = 1.5D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(25, 1, 0, 0);
		GlStateManager.rotate(45, 0, 1, 0);
		GlStateManager.translate(-0.5, 0.5, 0);
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -7.5D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.translate(0, -0.25, -2.5);
	}

	@Override
	public void renderModTable(ItemStack stack, int index) {
		GlStateManager.enableLighting();

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.mas36_tex);
		ResourceManager.mas36.renderPart("Gun");
		ResourceManager.mas36.renderPart("Stock");
		ResourceManager.mas36.renderPart("Bolt");
		if(isScoped(stack)) ResourceManager.mas36.renderPart("Scope");
		if(hasBayonet(stack)) ResourceManager.mas36.renderPart("Bayonet");
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {
		GlStateManager.enableLighting();

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.mas36_tex);
		ResourceManager.mas36.renderPart("Gun");
		ResourceManager.mas36.renderPart("Stock");
		ResourceManager.mas36.renderPart("Bolt");
		if(isScoped(stack)) ResourceManager.mas36.renderPart("Scope");
		GlStateManager.translate(0, -1, -6);
		if(hasBayonet(stack)) ResourceManager.mas36.renderPart("Bayonet");
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	public boolean isScoped(ItemStack stack) {
		return WeaponModManager.hasUpgrade(stack, 0, WeaponModManager.ID_SCOPE);
	}

	public boolean hasBayonet(ItemStack stack) {
		return WeaponModManager.hasUpgrade(stack, 0, WeaponModManager.ID_MAS_BAYONET);
	}
}

