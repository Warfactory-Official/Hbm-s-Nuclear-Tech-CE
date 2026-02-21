package com.hbm.render.tileentity;

import com.hbm.blocks.network.energy.BlockCableGauge.TileEntityCableGauge;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.Library;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
@AutoRegister
public class RenderCableGauge extends TileEntitySpecialRenderer<TileEntityCableGauge> {
	
	protected static final float fontOffset = 0.501F;

	@Override
	public void render(TileEntityCableGauge te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		super.render(te, x, y, z, partialTicks, destroyStage, alpha);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5F, y + 0.5F, z + 0.5F);
		switch(te.getBlockMetadata()) {
		case 3: GlStateManager.rotate(270, 0F, 1F, 0F); break;
		case 5: GlStateManager.rotate(0, 0F, 1F, 0F); break;
		case 2: GlStateManager.rotate(90, 0F, 1F, 0F); break;
		case 4: GlStateManager.rotate(180, 0F, 1F, 0F); break;
		}
		GlStateManager.translate(fontOffset, 0, 0);
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.depthMask(false);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GlStateManager.color(1, 1, 1, 1);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
			
		String text = Library.getShortNumber(te.deltaLastSecond);
		if(text != null && ! text.isEmpty()) {

			int width = font.getStringWidth(text);
			int height = font.FONT_HEIGHT;
			
			float f3 = Math.min(0.03F, 0.8F / Math.max(width, 1));
			GlStateManager.scale(f3, -f3, f3);
			GlStateManager.color(0.0F, 0.0F, -1.0F);
			GlStateManager.rotate(90, 0, 1, 0);
			
			font.drawString(text, -width / 2, -height / 2, 0xff8000);
		}
		GlStateManager.depthMask(true);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GlStateManager.popMatrix();
	}
}
