package com.hbm.render.entity.missile;

import com.hbm.entity.missile.EntityMissileAntiBallistic;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.NTMRenderHelper;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

@AutoRegister(factory = "FACTORY")
public class RenderMissileAB extends Render<EntityMissileAntiBallistic> {

	public static final IRenderFactory<EntityMissileAntiBallistic> FACTORY = RenderMissileAB::new;
	
	protected RenderMissileAB(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(EntityMissileAntiBallistic missile, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
        boolean prevLighting = RenderUtil.isLightingEnabled();
        int prevShade = RenderUtil.getShadeModel();
		if (!prevLighting) GlStateManager.enableLighting();
		double[] pos = NTMRenderHelper.getRenderPosFromMissile(missile, partialTicks);
		x = pos[0];
		y = pos[1];
		z = pos[2];
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(missile.prevRotationYaw + (missile.rotationYaw - missile.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(missile.prevRotationPitch + (missile.rotationPitch - missile.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
        if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.missileAA_tex);
        ResourceManager.missileABM.renderAll();
        if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(prevShade);
        if (!prevLighting) GlStateManager.disableLighting();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMissileAntiBallistic entity) {
		return ResourceManager.missileAA_tex;
	}

}
