package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineThresher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderThresher extends TileEntitySpecialRenderer<TileEntityMachineThresher> implements IItemRendererProvider {

	@Override
	public void render(TileEntityMachineThresher thresher, double x, double y, double z, float interp, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5D, y, z + 0.5D);
		GlStateManager.enableLighting();
		GlStateManager.enableCull();

		switch(thresher.getBlockMetadata()) {
		case 3: GlStateManager.rotate(180, 0F, 1F, 0F); break;
		case 5: GlStateManager.rotate(270, 0F, 1F, 0F); break;
		case 2: GlStateManager.rotate(0, 0F, 1F, 0F); break;
		case 4: GlStateManager.rotate(90, 0F, 1F, 0F); break;
		}

		double angle = thresher.prevAngle + (thresher.angle - thresher.prevAngle) * interp;
		double spin = thresher.lastSpin + (thresher.spin - thresher.lastSpin) * interp;
		double engine = thresher.isOn ? Math.sin(thresher.getWorld().getTotalWorldTime() * 2 % (Math.PI * 2) + interp) : 0;

		renderCommon(82.5 - angle, spin, engine);

		GlStateManager.popMatrix();
	}

	private void renderCommon(double angle, double spin, double engine) {

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.thresher_tex);
		ResourceManager.thresher.renderPart("Base");

		GlStateManager.pushMatrix(); {
			GlStateManager.translate(0, engine * 0.01, 0);
			ResourceManager.thresher.renderPart("Engine");
		} GlStateManager.popMatrix();

		GlStateManager.translate(0, 0.5, -1);
		GlStateManager.rotate((float) angle, 1, 0, 0);
		GlStateManager.translate(0, -0.5, 1);
		ResourceManager.thresher.renderPart("ArmUpper");

		GlStateManager.translate(0, 0.5, -5);
		GlStateManager.rotate((float) (angle * -2), 1, 0, 0);
		GlStateManager.translate(0, -0.5, 5);
		GlStateManager.translate(-0.01, 0, 0);
		ResourceManager.thresher.renderPart("ArmLower");
		GlStateManager.translate(0.01, 0, 0);

		GlStateManager.translate(0, 0.5, -9);
		GlStateManager.rotate((float) angle, 1, 0, 0);
		GlStateManager.translate(0, -0.5, 9);
		GlStateManager.translate(0.01, 0, 0);
		ResourceManager.thresher.renderPart("Front");

		GlStateManager.translate(0, 0.5, -11);
		GlStateManager.rotate((float) -spin, 1, 0, 0);
		GlStateManager.translate(0, -0.5, 11);
		ResourceManager.thresher.renderPart("Wheel");

		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_thresher);
	}

	@Override
	public ItemRenderBase getRenderer(Item item) {
		return new ItemRenderBase() {
			public void renderInventory() {
				GlStateManager.translate(0, 4, -8);
				GlStateManager.scale(4.5, 4.5, 4.5);
			}
			public void renderCommon(ItemStack stack) {
				GlStateManager.scale(0.5, 0.5, 0.5);
				GlStateManager.rotate(90, 0, -1, 0);
				RenderThresher.this.renderCommon(80D, System.currentTimeMillis() % 3600 * 0.25D, 0);
			}};
	}
}
