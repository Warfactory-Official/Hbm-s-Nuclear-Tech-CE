package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.projectile.EntityBombletZeta;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;
@AutoRegister(factory = "FACTORY")
public class RenderBombletZeta extends Render<EntityBombletZeta> {

	public static final IRenderFactory<EntityBombletZeta> FACTORY = (RenderManager man) -> new RenderBombletZeta(man);
	
	private static final ResourceLocation objTesterModelRL = new ResourceLocation(/*"/assets/" + */Tags.MODID, "models/bombletTheta.obj");
	private IModelCustom boyModel;
    private ResourceLocation boyTexture;
	
	protected RenderBombletZeta(RenderManager renderManager) {
		super(renderManager);
		boyModel = new HFRWavefrontObject(objTesterModelRL);
		boyTexture = new ResourceLocation(Tags.MODID, "textures/models/projectiles/bombletZetaTexture.png");
	}
	
	@Override
	public void doRender(EntityBombletZeta entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
        
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        bindTexture(new ResourceLocation(Tags.MODID, "textures/models/projectiles/bombletZetaTexture.png"));
        
        boyModel.renderAll();
		GlStateManager.popMatrix();
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityBombletZeta entity) {
		return boyTexture;
	}
    
    
}
