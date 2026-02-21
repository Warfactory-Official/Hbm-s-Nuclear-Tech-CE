package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.logic.EntityDeathBlast;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ClientProxy;
import com.hbm.render.loader.IModelCustom;
import com.hbm.render.amlfrom1710.Vec3;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;
@AutoRegister(factory = "FACTORY")
public class RenderDeathBlast extends Render<EntityDeathBlast> {

	public static final IRenderFactory<EntityDeathBlast> FACTORY = (RenderManager man) -> {return new RenderDeathBlast(man);};
	
	private static final IModelCustom sphere = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/sphere.obj"));
	
	protected RenderDeathBlast(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(EntityDeathBlast entity, double x, double y, double z, float entityYaw, float partialTicks) {
		if(!ClientProxy.renderingConstant)
			return;
		GlStateManager.pushMatrix();
    	GlStateManager.translate((float)x, (float)y, (float)z);
    	GlStateManager.disableLighting();
    	GlStateManager.enableCull();
    	GlStateManager.disableTexture2D();
    	GlStateManager.shadeModel(GL11.GL_SMOOTH);
    	GlStateManager.depthMask(false);

		GlStateManager.pushMatrix();
	        GlStateManager.enableBlend();
	        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
	        
	        GlStateManager.color(1, 0, 0);
	        
	        Vec3 vector = Vec3.createVectorHelper(0.5D, 0, 0);
	
	        Tessellator tessellator = Tessellator.getInstance();
	        BufferBuilder buf = tessellator.getBuffer();
			RenderHelper.disableStandardItemLighting();
			
			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
	        for(int i = 0; i < 8; i++) {
	            buf.pos(vector.xCoord, 250.0D, vector.zCoord).color(1F, 0, 0, 1).endVertex();
	            buf.pos(vector.xCoord, 0.0D, vector.zCoord).color(1F, 0, 0, 1).endVertex();
	        	vector.rotateAroundY(45);
	        	buf.pos(vector.xCoord, 0.0D, vector.zCoord).color(1F, 0, 0, 1).endVertex();
	            buf.pos(vector.xCoord, 250.0D, vector.zCoord).color(1F, 0, 0, 1).endVertex();
	        }

	        for(int i = 0; i < 8; i++) {
	            buf.pos(vector.xCoord / 2, 250.0D, vector.zCoord / 2).color(1F, 0, 1, 1).endVertex();
	            buf.pos(vector.xCoord / 2, 0.0D, vector.zCoord / 2).color(1F, 0, 1, 1).endVertex();
	        	vector.rotateAroundY(45);
	            buf.pos(vector.xCoord / 2, 0.0D, vector.zCoord / 2).color(1F, 0, 1, 1).endVertex();
	            buf.pos(vector.xCoord / 2, 250.0D, vector.zCoord / 2).color(1F, 0, 1, 1).endVertex();
	        }
	        
	        tessellator.draw();
	    GlStateManager.popMatrix();
        
	    GlStateManager.depthMask(true);
	    GlStateManager.disableCull();
	    GlStateManager.disableBlend();
	    GlStateManager.enableTexture2D();
	    GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
        
        renderOrb(entity, x, y, z, entityYaw, partialTicks);
	}
	
	public void renderOrb(Entity entity, double x, double y, double z, float p_76986_8_, float p_76986_9_) {
		GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);
        GlStateManager.disableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableTexture2D();
        GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0);
		
		float scale = 10 - 10F * (((float)entity.ticksExisted) / ((float)EntityDeathBlast.maxAge));
		float alpha = (((float)entity.ticksExisted) / ((float)EntityDeathBlast.maxAge));
		
		if(scale < 0)
			scale = 0;
		
        GlStateManager.color(1, 0, 1, alpha);

		GlStateManager.enableBlend();
        GL11.glScaled(scale, scale, scale);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        sphere.renderAll();

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glScaled(1.25, 1.25, 1.25);
        GlStateManager.color(1.0F, 0, 0, alpha * 0.125F);
        
        for(int i = 0; i < 8; i++) {
        	sphere.renderAll();
            GL11.glScaled(1.05, 1.05, 1.05);
        }
        
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.1F);
        GlStateManager.color(1, 1, 1, 1);
        
        GlStateManager.popMatrix();
	}
	
	@Override
	public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {}

	@Override
	protected ResourceLocation getEntityTexture(EntityDeathBlast entity) {
		return null;
	}

	
	
}
