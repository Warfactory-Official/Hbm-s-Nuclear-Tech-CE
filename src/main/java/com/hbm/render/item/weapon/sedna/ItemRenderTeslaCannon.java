package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.render.tileentity.RenderPlushie;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "gun_tesla_cannon")
public class ItemRenderTeslaCannon extends ItemRenderWeaponBase {

	@Override
	protected float getTurnMagnitude(ItemStack stack) { return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.5F; }

	@Override
	public float getViewFOV(ItemStack stack, float fov) {
		float aimingProgress = ItemGunBaseNT.prevAimingProgress + (ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
		return  fov * (1 - aimingProgress * 0.33F);
	}

	@Override
	public void setupFirstPerson(ItemStack stack) {
		GlStateManager.translate(0, 0, 0.875);

		float offset = 0.8F;
		standardAimingTransform(stack,
				-1.75F * offset, -0.5F * offset, 1.75F * offset,
				-1.3125F * offset, 0F * offset, -0.5F * offset);
	}

	protected static String label = "AUTO";

	@Override
	public void renderFirstPerson(ItemStack stack) {

		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
		Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.tesla_cannon_tex);
		double scale = 0.75D;
		GlStateManager.scale(scale, scale, scale);

		double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
		double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
		double[] cycle = HbmAnimationsSedna.getRelevantTransformation("CYCLE");
		double[] count = HbmAnimationsSedna.getRelevantTransformation("COUNT");
		double[] yomi = HbmAnimationsSedna.getRelevantTransformation("YOMI");
		double[] squeeze = HbmAnimationsSedna.getRelevantTransformation("SQUEEZE");

		GlStateManager.translate(0, -2, -2);
		GlStateManager.rotate((float)equip[0], 1, 0, 0);
		GlStateManager.translate(0, 2, 2);

		GlStateManager.translate(0, 0, recoil[2]);
		GlStateManager.rotate((float)(recoil[2] * 2), 1, 0, 0);

		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		int amount = Math.max((int) count[0], gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, MainRegistry.proxy.me().inventory));

		ResourceManager.tesla_cannon.renderPart("Gun");
		ResourceManager.tesla_cannon.renderPart("Extension");

		double cogAngle = cycle[2];

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, -1.625, 0);
		GlStateManager.rotate((float)cogAngle, 0, 0, 1);
		GlStateManager.translate(0, 1.625, 0);
		ResourceManager.tesla_cannon.renderPart("Cog");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();

		GlStateManager.translate(0, -1.625, 0);
		GlStateManager.rotate((float)cogAngle, 0, 0, 1);
		GlStateManager.translate(0, 1.625, 0);

		for (int i = 0; i < Math.min(amount, 8); i++) {
			ResourceManager.tesla_cannon.renderPart("Capacitor");

			if (i < 4) {
				GlStateManager.translate(0, -1.625, 0);
				GlStateManager.rotate(-22.5F, 0, 0, 1);
				GlStateManager.translate(0, 1.625, 0);
			} else {
				if (i == 4) {
					GlStateManager.translate(0, -1.625, 0);
					GlStateManager.rotate((float)-cogAngle, 0, 0, 1);
					GlStateManager.translate(0, 1.625, 0);
					GlStateManager.translate(-cogAngle * 0.5 / 22.5, 0, 0);
				}
				GlStateManager.translate(0.5, 0, 0);
			}
		}
		GlStateManager.popMatrix();

		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.pushMatrix();
		GlStateManager.translate(yomi[0], yomi[1], yomi[2]);
		GlStateManager.rotate(135F, 0, 1, 0);
		GlStateManager.scale(squeeze[0], squeeze[1], squeeze[2]);
		Minecraft.getMinecraft().renderEngine.bindTexture(RenderPlushie.yomiTex);
		ResourceManager.plushie_yomi.renderAll();
		GlStateManager.popMatrix();
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		double scale = 2.75D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(0, 1.5, 1);
	}

	@Override
	public void setupInv(ItemStack stack) {
		super.setupInv(stack);
		double scale = 1.25D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(25, 1, 0, 0);
		GlStateManager.rotate(45, 0, 1, 0);
		GlStateManager.translate(0, 0.5, 0);
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -8.75D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.translate(0, 0.5, 0);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {
		GlStateManager.enableLighting();

		Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.tesla_cannon_tex);

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		ResourceManager.tesla_cannon.renderPart("Gun");
		ResourceManager.tesla_cannon.renderPart("Extension");
		ResourceManager.tesla_cannon.renderPart("Cog");

		GlStateManager.pushMatrix();
		for (int i = 0; i < 10; i++) {
			ResourceManager.tesla_cannon.renderPart("Capacitor");

			if (i < 4) {
				GlStateManager.translate(0, -1.625, 0);
				GlStateManager.rotate(-22.5F, 0, 0, 1);
				GlStateManager.translate(0, 1.625, 0);
			} else {
				GlStateManager.translate(0.5, 0, 0);
			}
		}
		GlStateManager.popMatrix();
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}
}

