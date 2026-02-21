package com.hbm.render.entity.missile;

import com.hbm.entity.missile.EntityMissileTier3;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.tileentity.RenderLaunchPad;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;
@AutoRegister(factory = "FACTORY")
public class RenderMissileInferno extends Render<EntityMissileTier3.EntityMissileInferno> {
	
	public static final IRenderFactory<EntityMissileTier3.EntityMissileInferno> FACTORY = (RenderManager man) -> {return new RenderMissileInferno(man);};
	
	protected RenderMissileInferno(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(EntityMissileTier3.EntityMissileInferno missile, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
        boolean prevLighting = RenderUtil.isLightingEnabled();
        int prevShade = RenderUtil.getShadeModel();
        if (!prevLighting) GlStateManager.enableLighting();
		double[] renderPos = NTMRenderHelper.getRenderPosFromMissile(missile, partialTicks);
		x = renderPos[0];
		y = renderPos[1];
		z = renderPos[2];
		GlStateManager.translate(x, y, z);
        GlStateManager.scale(RenderLaunchPad.h_3, RenderLaunchPad.h_3, RenderLaunchPad.h_3);
        GlStateManager.rotate(missile.prevRotationYaw + (missile.rotationYaw - missile.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(missile.prevRotationPitch + (missile.rotationPitch - missile.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.missileHuge_IN_tex);
        ResourceManager.missileHuge.renderAll();
        GlStateManager.shadeModel(prevShade);
        if (!prevLighting) GlStateManager.disableLighting();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMissileTier3.EntityMissileInferno entity) {
		return ResourceManager.missileHuge_IN_tex;
	}
}
