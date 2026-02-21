package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.model.ModelTestRender;
import com.hbm.tileentity.deco.TileEntityTestRender;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
@AutoRegister
public class RenderTestRender extends TileEntitySpecialRenderer<TileEntityTestRender> {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID, "textures/models/misc/modeltestrender.png");
	
	private ModelTestRender model;
	
	public RenderTestRender(){
		this.model = new ModelTestRender();
	}
	
	@Override
	public void render(TileEntityTestRender te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
			GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
			GlStateManager.rotate(180, 0F, 0F, 1F);
		
			this.bindTexture(texture);
		
			GlStateManager.pushMatrix();
			this.model.renderModel(0.0625F);
			GlStateManager.popMatrix();
		GlStateManager.popMatrix();
	}
	
}
