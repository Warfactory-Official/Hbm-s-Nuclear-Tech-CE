package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.projectile.EntityAAShell;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(factory = "FACTORY")
public class RenderAAShell extends Render<EntityAAShell> {

	public static final IRenderFactory<EntityAAShell> FACTORY = (RenderManager man) -> {return new RenderAAShell(man);};
	
	private static final ResourceLocation objTesterModelRL = new ResourceLocation(/*"/assets/" + */Tags.MODID, "models/Mirv.obj");
	private IModelCustom boyModel;
    private ResourceLocation boyTexture;
	
	protected RenderAAShell(RenderManager renderManager) {
		super(renderManager);
		boyModel = new HFRWavefrontObject(objTesterModelRL).asVBO();
		boyTexture = new ResourceLocation(Tags.MODID, "textures/models/TheGadget3_.png");
	}
	
	@Override
	public void doRender(EntityAAShell entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
        bindTexture(boyTexture);
        boyModel.renderAll();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityAAShell entity) {
		return boyTexture;
	}

}
