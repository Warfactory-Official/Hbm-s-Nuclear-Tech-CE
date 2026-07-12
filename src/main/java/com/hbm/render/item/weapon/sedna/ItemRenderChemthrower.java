package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "gun_chemthrower")
public class ItemRenderChemthrower extends ItemRenderWeaponBase {

	public ItemRenderChemthrower() {}

	@Override
	protected float getTurnMagnitude(ItemStack stack) {
		return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.25F;
	}

	@Override
	public void setupFirstPerson(ItemStack stack) {
		GlStateManager.translate(0, 0, 0.875);

		float offset = 0.8F;
		standardAimingTransform(stack,
				-2F * offset, -2F * offset, 2.5F * offset,
				0, -4.375 / 8D, 1);
	}

	@Override
	public void renderFirstPerson(ItemStack stack) {

		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.chemthrower_tex);
		double scale = 0.75D;
		GlStateManager.scale(scale, scale, scale);

		double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");

		GlStateManager.translate(0, -2, -4);
		GlStateManager.rotate((float) equip[0], -1, 0, 0);
		GlStateManager.translate(0, 2, 4);

		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		GlStateManager.rotate(90F, 0, 1, 0);
		ResourceManager.chemthrower.renderPart("Gun");
		ResourceManager.chemthrower.renderPart("Hose");
		ResourceManager.chemthrower.renderPart("Nozzle");

		GlStateManager.translate(0, 0.875, 1.75);
		IMagazine mag = gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);
		double d = (double) mag.getAmount(stack, MainRegistry.proxy.me().inventory) / (double) mag.getCapacity(stack);
		GlStateManager.rotate((float) (135 - d * 270), 1, 0, 0);
		GlStateManager.translate(0, -0.875, -1.75);

		ResourceManager.chemthrower.renderPart("Gauge");

		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		double scale = 2D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(0, -2.5, 0.5);
	}

	@Override
	public void setupInv(ItemStack stack) {
		super.setupInv(stack);
		double scale = 2D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(25F, 1, 0, 0);
		GlStateManager.rotate(45F, 0, 1, 0);
		GlStateManager.translate(0.875, 0, 0);
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -10D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.translate(0, -0.5, -0.5);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {
		GlStateManager.enableLighting();

		GlStateManager.rotate(90F, 0, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.chemthrower_tex);
		ResourceManager.chemthrower.renderPart("Gun");
		ResourceManager.chemthrower.renderPart("Hose");
		ResourceManager.chemthrower.renderPart("Nozzle");
		ResourceManager.chemthrower.renderPart("Gauge");
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}
}

