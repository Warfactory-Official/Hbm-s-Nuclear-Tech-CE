package com.hbm.render.tileentity;

import com.hbm.interfaces.AutoRegister;
import com.hbm.render.NTMRenderHelper;
import com.hbm.tileentity.machine.TileEntitySoyuzStruct;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

import static com.hbm.render.util.SmallBlockPronter.renderSimpleBlockAt;
@AutoRegister
public class RenderSoyuzMultiblock extends TileEntitySpecialRenderer<TileEntitySoyuzStruct> {
	

	public static TextureAtlasSprite[] blockIcons = new TextureAtlasSprite[]{null, null, null};
	@Override
	public void render(TileEntitySoyuzStruct te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		
		GlStateManager.translate((float)x, (float)y, (float)z);

		GlStateManager.enableBlend();
		GlStateManager.enableCull();
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.75F);
        GlStateManager.disableAlpha();
        GlStateManager.disableLighting();

        NTMRenderHelper.bindBlockTexture();
        NTMRenderHelper.startDrawingTexturedQuads();
        
		TextureAtlasSprite loc;

		loc = blockIcons[0];

		for(int i = -6; i <= 6; i++)
			for(int j = 3; j <= 4; j++)
				for(int k = -6; k <= 6; k++)
					renderSimpleBlockAt(loc, i, j, k);

		for(int i = -1; i <= 1; i++)
			for(int j = 3; j <= 4; j++)
				for(int k = -8; k <= -7; k++)
					renderSimpleBlockAt(loc, i, j, k);

		for(int i = -2; i <= 2; i++)
			for(int j = 3; j <= 4; j++)
				for(int k = 7; k <= 9; k++)
					renderSimpleBlockAt(loc, i, j, k);

		for(int i = -2; i <= 2; i++)
			for(int k = 5; k <= 9; k++)
				renderSimpleBlockAt(loc, i, 51, k);

		for(int i = -1; i <= 1; i++)
			for(int k = -8; k <= -6; k++)
				renderSimpleBlockAt(loc, i, 38, k);

		loc = blockIcons[1];

		for(int i = 3; i <= 6; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = 3; k <= 6; k++)
					renderSimpleBlockAt(loc, i, j, k);

		for(int i = -6; i <= -3; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = 3; k <= 6; k++)
					renderSimpleBlockAt(loc, i, j, k);

		for(int i = -6; i <= -3; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = -6; k <= -3; k++)
					renderSimpleBlockAt(loc, i, j, k);

		for(int i = 3; i <= 6; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = -6; k <= -3; k++)
					renderSimpleBlockAt(loc, i, j, k);

		for(int i = -1; i <= 1; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = -8; k <= -6; k++)
					renderSimpleBlockAt(loc, i, j, k);

		for(int i = -2; i <= 2; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = 5; k <= 9; k++)
					renderSimpleBlockAt(loc, i, j, k);

		loc = blockIcons[2];

		for(int i = -1; i <= 1; i++)
			for(int j = 5; j <= 50; j++)
				for(int k = 6; k <= 8; k++)
					renderSimpleBlockAt(loc, i, j, k);

		for(int j = 5; j <= 37; j++)
			renderSimpleBlockAt(loc, 0, j, -7);

		NTMRenderHelper.draw();
		
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableLighting();

		GlStateManager.popMatrix();
	}
	
}
