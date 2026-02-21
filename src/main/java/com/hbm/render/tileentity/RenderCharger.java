package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityCharger;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderCharger extends TileEntitySpecialRenderer<TileEntityCharger> implements IItemRendererProvider {
	@Override
	public void render(TileEntityCharger charger, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y, z + 0.5);
		GlStateManager.enableLighting();
		GlStateManager.rotate(90F, 0F, 1F, 0F);
		switch (charger.getBlockMetadata()) {
			case 4 -> GlStateManager.rotate(90F, 0F, 1F, 0F);
			case 3 -> GlStateManager.rotate(180F, 0F, 1F, 0F);
			case 5 -> GlStateManager.rotate(270F, 0F, 1F, 0F);
			case 2 -> GlStateManager.rotate(0F, 0F, 1F, 0F);
		}

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.charger_tex);
		ResourceManager.charger.renderPart("Base");

		double time = (charger.lastUsingTicks + (charger.usingTicks - charger.lastUsingTicks) * partialTicks) / (double) charger.delay;

		double extend = Math.min(1, time * 2);
		double swivel = Math.max(0, (time - 0.5) * 2);

		GlStateManager.pushMatrix();

		GlStateManager.translate(-0.34375D, 0.25D, 0);
		GlStateManager.rotate(10F, 0F, 0F, 1F);
		GlStateManager.translate(0.34375D, -0.25D, 0);

		GlStateManager.translate(0, -0.25 * extend, 0);

		//ResourceManager.charger.renderPart("Slide");

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0.28D, 0);
		GlStateManager.rotate(30F * (float) swivel, 1F, 0F, 0F);
		GlStateManager.translate(0, -0.28D, 0);
		ResourceManager.charger.renderPart("Left");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0.28D, 0);
		GlStateManager.rotate(-30F * (float) swivel, 1F, 0F, 0F);
		GlStateManager.translate(0, -0.28D, 0);
		ResourceManager.charger.renderPart("Right");
		GlStateManager.popMatrix();

		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		boolean prevLighting = RenderUtil.isLightingEnabled();

		GlStateManager.disableTexture2D();
		if (prevLighting) GlStateManager.disableLighting();
		GlStateManager.disableCull();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

		GlStateManager.color(1F, 0.75F, 0F, 1F);
		ResourceManager.charger.renderPart("Light");
		GlStateManager.color(1F, 1F, 1F, 1F);

		GlStateManager.translate(-0.34375D, 0.25D, 0);
		GlStateManager.rotate(10F, 0F, 0F, 1F);
		GlStateManager.translate(0.34375D, -0.25D, 0);
		GlStateManager.translate(0, -0.25 * extend, 0);
		GlStateManager.enableTexture2D();

		ResourceManager.charger.renderPart("Slide");

		if (prevLighting) GlStateManager.enableLighting();

		GlStateManager.popMatrix();

		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.popMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.charger);
	}

	@Override
	public ItemRenderBase getRenderer(Item item) {
		return new ItemRenderBase() {
			public void renderInventory() {
				GlStateManager.translate(0, -3, 0);
                GlStateManager.scale(6, 6, 6);
			}
			public void renderCommon() {
                GlStateManager.scale(2, 2, 2);
                GlStateManager.translate(0.5, 0, 0);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.charger_tex);
                ResourceManager.charger.renderAll();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
		};
	}
}
