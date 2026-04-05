package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.NTMRenderHelper;
import com.hbm.tileentity.machine.TileEntityMultiblock;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.lwjgl.opengl.GL11;

import static com.hbm.render.util.SmallBlockPronter.renderSimpleBlockAt;
@AutoRegister
public class RenderMultiblock extends TileEntitySpecialRenderer<TileEntityMultiblock> {

	public static TextureAtlasSprite structLauncher;
	public static TextureAtlasSprite structScaffold;
	@Override
	public void render(TileEntityMultiblock te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		
		GlStateManager.translate(x, y, z);

		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		GlStateManager.enableCull();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.75F);
        GlStateManager.disableAlpha();
		
        Block b = te.getBlockType();
        NTMRenderHelper.bindBlockTexture();
        
        NTMRenderHelper.startDrawingTexturedQuads();
        
        if(b == ModBlocks.struct_launcher_core)
        	renderCompactLauncher();
        
        if(b == ModBlocks.struct_launcher_core_large)
        	renderLaunchTable();
        
        NTMRenderHelper.draw();
        
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableAlpha();
		
		GlStateManager.popMatrix();
	}
	
	private void renderCompactLauncher() {
		
		for(int i = -1; i <= 1; i++)
			for(int j = -1; j <= 1; j++)
				if(i != 0 || j != 0)
					renderSimpleBlockAt(structLauncher, i, 0, j);
	}
	
	private void renderLaunchTable() {
		for(int i = -4; i <= 4; i++)
			for(int j = -4; j <= 4; j++)
				if(i != 0 || j != 0)
					renderSimpleBlockAt(structLauncher, i, 0, j);
        
		switch((int)(System.currentTimeMillis() % 4000 / 1000)) {
		case 0:
			for(int k = 1; k < 12; k++)
				renderSimpleBlockAt(structScaffold, 3, k, 0);
			break;
			
		case 1:
			for(int k = 1; k < 12; k++)
				renderSimpleBlockAt(structScaffold, 0, k, 3);
			break;
			
		case 2:
			for(int k = 1; k < 12; k++)
				renderSimpleBlockAt(structScaffold, -3, k, 0);
			break;
			
		case 3:
			for(int k = 1; k < 12; k++)
				renderSimpleBlockAt(structScaffold, 0, k, -3);
			break;
		}
	}
	
}
