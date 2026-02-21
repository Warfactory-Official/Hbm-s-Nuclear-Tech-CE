package com.hbm.render.entity.missile;

import com.hbm.entity.missile.EntityMissileTier2;
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
public class RenderMissileIncendiaryStrong extends Render<EntityMissileTier2.EntityMissileIncendiaryStrong> {

	public static final IRenderFactory<EntityMissileTier2.EntityMissileIncendiaryStrong> FACTORY = (RenderManager man) -> {return new RenderMissileIncendiaryStrong(man);};
	
	protected RenderMissileIncendiaryStrong(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(EntityMissileTier2.EntityMissileIncendiaryStrong missile, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
        boolean prevLighting = RenderUtil.isLightingEnabled();
        int prevShade = RenderUtil.getShadeModel();
        if (!prevLighting) GlStateManager.enableLighting();
        double[] renderPos = NTMRenderHelper.getRenderPosFromMissile(missile, partialTicks);
        x = renderPos[0];
        y = renderPos[1];
        z = renderPos[2];
        GlStateManager.translate(x, y, z);
        GlStateManager.scale(RenderLaunchPad.w_2, RenderLaunchPad.h_2, RenderLaunchPad.w_2);
        GlStateManager.rotate(missile.prevRotationYaw + (missile.rotationYaw - missile.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(missile.prevRotationPitch + (missile.rotationPitch - missile.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
        
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.missileStrong_IN_tex);
        ResourceManager.missileStrong.renderAll();
        GlStateManager.shadeModel(prevShade);
        if (!prevLighting) GlStateManager.disableLighting();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMissileTier2.EntityMissileIncendiaryStrong entity) {
		return ResourceManager.missileStrong_IN_tex;
	}

}
