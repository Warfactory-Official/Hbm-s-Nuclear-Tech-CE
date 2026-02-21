package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.projectile.EntityMeteor;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(factory = "FACTORY")
public class RenderMeteor extends Render<EntityMeteor> {

	public static final IRenderFactory<EntityMeteor> FACTORY = (RenderManager man) -> {return new RenderMeteor(man);};
	
	private static final ResourceLocation block_rl = new ResourceLocation(Tags.MODID + ":textures/blocks/block_meteor_molten.png");
	
	protected RenderMeteor(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(EntityMeteor rocket, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.scale(1.0F, 1.0F, 1.0F);
		GlStateManager.rotate(180, 1, 0, 0);
		GlStateManager.rotate(((rocket.ticksExisted % 360) + partialTicks) * 10, 1, 1, 1);
		

		GlStateManager.disableCull();
		GlStateManager.scale(5.0F, 5.0F, 5.0F);
		bindTexture(this.getEntityTexture(rocket));
		GlStateManager.disableLighting();
		renderBlock(0, 0, 0);
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		
		GlStateManager.popMatrix();
	}
	
	public void renderBlock(double x, double y, double z) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.rotate(180, 0F, 0F, 1F);
		NTMRenderHelper.startDrawingTexturedQuads();
		
			NTMRenderHelper.addVertexWithUV(-0.5, -0.5, -0.5, 1, 0);
			NTMRenderHelper.addVertexWithUV(+0.5, -0.5, -0.5, 0, 0);
			NTMRenderHelper.addVertexWithUV(+0.5, +0.5, -0.5, 0, 1);
			NTMRenderHelper.addVertexWithUV(-0.5, +0.5, -0.5, 1, 1);

			NTMRenderHelper.addVertexWithUV(-0.5, -0.5, +0.5, 1, 0);
			NTMRenderHelper.addVertexWithUV(-0.5, -0.5, -0.5, 0, 0);
			NTMRenderHelper.addVertexWithUV(-0.5, +0.5, -0.5, 0, 1);
			NTMRenderHelper.addVertexWithUV(-0.5, +0.5, +0.5, 1, 1);

			NTMRenderHelper.addVertexWithUV(+0.5, -0.5, +0.5, 1, 0);
			NTMRenderHelper.addVertexWithUV(-0.5, -0.5, +0.5, 0, 0);
			NTMRenderHelper.addVertexWithUV(-0.5, +0.5, +0.5, 0, 1);
			NTMRenderHelper.addVertexWithUV(+0.5, +0.5, +0.5, 1, 1);

			NTMRenderHelper.addVertexWithUV(+0.5, -0.5, -0.5, 1, 0);
			NTMRenderHelper.addVertexWithUV(+0.5, -0.5, +0.5, 0, 0);
			NTMRenderHelper.addVertexWithUV(+0.5, +0.5, +0.5, 0, 1);
			NTMRenderHelper.addVertexWithUV(+0.5, +0.5, -0.5, 1, 1);

			NTMRenderHelper.addVertexWithUV(-0.5, -0.5, +0.5, 1, 0);
			NTMRenderHelper.addVertexWithUV(+0.5, -0.5, +0.5, 0, 0);
			NTMRenderHelper.addVertexWithUV(+0.5, -0.5, -0.5, 0, 1);
			NTMRenderHelper.addVertexWithUV(-0.5, -0.5, -0.5, 1, 1);

			NTMRenderHelper.addVertexWithUV(+0.5, +0.5, +0.5, 1, 0);
			NTMRenderHelper.addVertexWithUV(-0.5, +0.5, +0.5, 0, 0);
			NTMRenderHelper.addVertexWithUV(-0.5, +0.5, -0.5, 0, 1);
			NTMRenderHelper.addVertexWithUV(+0.5, +0.5, -0.5, 1, 1);
		NTMRenderHelper.draw();
		GlStateManager.popMatrix();
		
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMeteor entity) {
		return block_rl;
	}

}
