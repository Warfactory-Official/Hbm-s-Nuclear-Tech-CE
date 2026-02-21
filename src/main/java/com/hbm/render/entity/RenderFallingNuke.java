package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.projectile.EntityFallingNuke;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(factory = "FACTORY")
public class RenderFallingNuke extends Render<EntityFallingNuke> {

	public static final IRenderFactory<EntityFallingNuke> FACTORY = (RenderManager man) -> {return new RenderFallingNuke(man);};
	
	private static final ResourceLocation objTesterModelRL = new ResourceLocation(/*"/assets/" + */Tags.MODID, "models/bombs/LilBoy.obj");
	private IModelCustom boyModel;
    private ResourceLocation boyTexture;
    private static final ResourceLocation gadget_rl = new ResourceLocation(Tags.MODID +":textures/models/bombs/gadget.png");
	
	protected RenderFallingNuke(RenderManager renderManager) {
		super(renderManager);
		boyModel = new HFRWavefrontObject(objTesterModelRL);
		boyTexture = new ResourceLocation(Tags.MODID, "textures/models/bombs/CustomNuke.png");
	}

	@Override
	public void doRender(EntityFallingNuke entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);
        
		switch(entity.getDataManager().get(EntityFallingNuke.FACING))
		{
		case NORTH:
			GlStateManager.rotate(0, 0F, 1F, 0F);
	        GlStateManager.translate(-2.0D, 0.0D, 0.0D); break;
		case WEST:
			GlStateManager.rotate(90, 0F, 1F, 0F);
	        GlStateManager.translate(-2.0D, 0.0D, 0.0D); break;
		case SOUTH:
			GlStateManager.rotate(180, 0F, 1F, 0F);
	        GlStateManager.translate(-2.0D, 0.0D, 0.0D); break;
		case EAST:
			GlStateManager.rotate(-90, 0F, 1F, 0F);
	        GlStateManager.translate(-2.0D, 0.0D, 0.0D); break;
	    default:
	       	break;
		}
        
		float f = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
		
		if(f < -80)
			f = 0;
		
        GlStateManager.rotate(f, 0.0F, 0.0F, 1.0F);

        bindTexture(boyTexture);
        boyModel.renderAll();
        
		GlStateManager.popMatrix();
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityFallingNuke entity) {
		return gadget_rl;
	}

}
