package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.util.IconUtil;
import com.hbm.tileentity.machine.TileEntityITERStruct;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

import static com.hbm.render.util.SmallBlockPronter.renderSimpleBlockAt;
@Deprecated
public class RenderITERMultiblock {


	public boolean isGlobalRenderer(TileEntityITERStruct te) {
		return true;
	}

	public void render(TileEntityITERStruct te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();

		GlStateManager.translate((float)x-1, (float)y-1, (float)z);

		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		GlStateManager.enableCull();
		GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.75F);
        GlStateManager.disableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(false);

        TextureAtlasSprite magnet = RenderStructureMarker.fusion[0][1];
        TextureAtlasSprite solenoid = RenderStructureMarker.fusion[4][1];
        TextureAtlasSprite motor = RenderStructureMarker.fusion[3][0];
        TextureAtlasSprite glass = IconUtil.getTextureFromBlock(ModBlocks.reinforced_glass);
        
        TextureAtlasSprite active = magnet;
        
        NTMRenderHelper.bindBlockTexture();
        NTMRenderHelper.startDrawingTexturedQuads();

        int[][][] layout = TileEntityITERStruct.layout;

        for(int iy = -2; iy <= 2; iy ++) {

        	int iny = 2 - Math.abs(iy);

	        for(int ix = 0; ix < layout[0].length; ix++) {

	            for(int iz = 0; iz < layout[0][0].length; iz++) {

	            	int block = layout[iny][ix][iz];

	            	switch(block) {
	            	case 0: continue;
	            	case 1: active = magnet; break;
	            	case 2: active = solenoid; break;
	            	case 3: active = motor; break;
	            	case 4: active = glass; break;
	            	}

	            	renderSimpleBlockAt(active, ix - 6F, iy + 3, iz - 7F);
	            }
	        }
        }

        NTMRenderHelper.draw();
        
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.depthMask(true);

		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}
}
