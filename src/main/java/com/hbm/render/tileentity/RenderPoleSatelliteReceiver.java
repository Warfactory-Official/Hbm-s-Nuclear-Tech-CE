package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.model.ModelSatelliteReceiver;
import com.hbm.tileentity.deco.TileEntityDecoPoleSatelliteReceiver;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
@AutoRegister
public class RenderPoleSatelliteReceiver extends TileEntitySpecialRenderer<TileEntityDecoPoleSatelliteReceiver> {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":" + "textures/models/deco/PoleSatelliteReceiver.png");
	
	private ModelSatelliteReceiver model;
	
	public RenderPoleSatelliteReceiver() {
		this.model = new ModelSatelliteReceiver();
	}
	
	@Override
	public void render(TileEntityDecoPoleSatelliteReceiver te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GlStateManager.rotate(180, 0F, 0F, 1F);
		
		switch(te.getBlockMetadata())
		{
		case 5:
			GlStateManager.rotate(90, 0F, 1F, 0F); break;
		case 3:
			GlStateManager.rotate(180, 0F, 1F, 0F); break;
		case 4:
			GlStateManager.rotate(270, 0F, 1F, 0F); break;
		case 2:
			GlStateManager.rotate(0, 0F, 1F, 0F); break;
		}
		
		this.bindTexture(texture);
		
		GlStateManager.pushMatrix();
			this.model.renderModel(0.0625F);
		GlStateManager.popMatrix();
		GlStateManager.popMatrix();
	}
}
