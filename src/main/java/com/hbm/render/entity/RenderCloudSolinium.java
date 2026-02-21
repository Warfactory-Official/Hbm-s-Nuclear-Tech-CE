package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.effect.EntityCloudSolinium;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(factory = "FACTORY")
public class RenderCloudSolinium extends Render<EntityCloudSolinium> {

	public static final IRenderFactory<EntityCloudSolinium> FACTORY = (RenderManager man) -> {return new RenderCloudSolinium(man);};
	
	private static final ResourceLocation objTesterModelRL = new ResourceLocation(/*"/assets/" + */Tags.MODID, "models/Sphere.obj");
	private IModelCustom blastModel;
    private ResourceLocation blastTexture;
    public float scale = 0;
    public float ring = 0;
    
	protected RenderCloudSolinium(RenderManager renderManager) {
		super(renderManager);
		blastModel = new HFRWavefrontObject(objTesterModelRL);
    	blastTexture = new ResourceLocation(Tags.MODID, "textures/models/explosion/BlastSolinium.png");
    	scale = 0;
	}
	
	@Override
	public void doRender(EntityCloudSolinium cloud, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
        GlStateManager.disableLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        
        GlStateManager.scale(cloud.age, cloud.age, cloud.age);
        
        bindTexture(blastTexture);
        blastModel.renderAll();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCloudSolinium entity) {
		return null;
	}

}
