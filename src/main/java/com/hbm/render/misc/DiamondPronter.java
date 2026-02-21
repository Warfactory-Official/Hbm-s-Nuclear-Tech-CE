package com.hbm.render.misc;

import com.hbm.Tags;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class DiamondPronter {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/models/misc/danger_diamond.png");
	
	public static void pront(int poison, int flammability, int reactivity, EnumSymbol symbol) {
		
		GlStateManager.pushMatrix();
		
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		
		float p = 1F/256F;
		float s = 1F/139F;
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

		NTMRenderHelper.startDrawingTexturedQuads();
		NTMRenderHelper.addVertexWithUV(0.0, 0.5, -0.5, p * 144, p * 45);
		NTMRenderHelper.addVertexWithUV(0.0, 0.5, 0.5, p * 5, p * 45);
		NTMRenderHelper.addVertexWithUV(0.0, -0.5, 0.5, p * 5, p * 184);
		NTMRenderHelper.addVertexWithUV(0.0, -0.5, -0.5, p * 144, p * 184);
		NTMRenderHelper.draw();
		
		float width = 10F * s;
		float height = 14F * s;
		
		if(poison >= 0 && poison < 6) {
			
			float oY = 0;
			float oZ = 33 * s;
			
			int x = 5 + (poison - 1) * 24;
			int y = 5;
			
			if(poison == 0) x = 125;

			NTMRenderHelper.startDrawingTexturedQuads();
			NTMRenderHelper.addVertexWithUV(0.01, height + oY, -width + oZ, (x + 20) * p, y * p);
			NTMRenderHelper.addVertexWithUV(0.01, height + oY, width + oZ, x * p, y * p);
			NTMRenderHelper.addVertexWithUV(0.01, -height + oY, width + oZ, x * p, (y + 28) * p);
			NTMRenderHelper.addVertexWithUV(0.01, -height + oY, -width + oZ, (x + 20) * p, (y + 28) * p);
			NTMRenderHelper.draw();
		}
		
		if(flammability >= 0 && flammability < 6) {
			
			float oY = 33 * s;
			float oZ = 0;
			
			int x = 5 + (flammability - 1) * 24;
			int y = 5;
			
			if(flammability == 0) x = 125;

			NTMRenderHelper.startDrawingTexturedQuads();
			NTMRenderHelper.addVertexWithUV(0.01, height + oY, -width + oZ, (x + 20) * p, y * p);
			NTMRenderHelper.addVertexWithUV(0.01, height + oY, width + oZ, x * p, y * p);
			NTMRenderHelper.addVertexWithUV(0.01, -height + oY, width + oZ, x * p, (y + 28) * p);
			NTMRenderHelper.addVertexWithUV(0.01, -height + oY, -width + oZ, (x + 20) * p, (y + 28) * p);
			NTMRenderHelper.draw();
		}
		
		if(reactivity >= 0 && reactivity < 6) {
			
			float oY = 0;
			float oZ = -33 * s;
			
			int x = 5 + (reactivity - 1) * 24;
			int y = 5;
			
			if(reactivity == 0) x = 125;

			NTMRenderHelper.startDrawingTexturedQuads();
			NTMRenderHelper.addVertexWithUV(0.01, height + oY, -width + oZ, (x + 20) * p, y * p);
			NTMRenderHelper.addVertexWithUV(0.01, height + oY, width + oZ, x * p, y * p);
			NTMRenderHelper.addVertexWithUV(0.01, -height + oY, width + oZ, x * p, (y + 28) * p);
			NTMRenderHelper.addVertexWithUV(0.01, -height + oY, -width + oZ, (x + 20) * p, (y + 28) * p);
			NTMRenderHelper.draw();
		}
		

		float symSize = 59F/2F * s;
		
		if(symbol != EnumSymbol.NONE) {
			
			float oY = -33 * s;
			float oZ = 0;
			
			int x = symbol.x;
			int y = symbol.y;

			NTMRenderHelper.startDrawingTexturedQuads();
			NTMRenderHelper.addVertexWithUV(0.01, symSize + oY, -symSize + oZ, (x + 59) * p, y * p);
			NTMRenderHelper.addVertexWithUV(0.01, symSize + oY, symSize + oZ, x * p, y * p);
			NTMRenderHelper.addVertexWithUV(0.01, -symSize + oY, symSize + oZ, x * p, (y + 59) * p);
			NTMRenderHelper.addVertexWithUV(0.01, -symSize + oY, -symSize + oZ, (x + 59) * p, (y + 59) * p);
			NTMRenderHelper.draw();
		}

		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
}
