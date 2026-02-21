package com.hbm.render.entity.missile;

import com.hbm.entity.missile.EntityMissileTier0;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.NTMRenderHelper;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(factory = "FACTORY")
public class RenderMissileSchrabidium extends Render<EntityMissileTier0.EntityMissileSchrabidium> {

	public static final IRenderFactory<EntityMissileTier0.EntityMissileSchrabidium> FACTORY = (RenderManager man) -> {return new RenderMissileSchrabidium(man);};
	
	protected RenderMissileSchrabidium(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(EntityMissileTier0.EntityMissileSchrabidium missile, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
        boolean prevLighting = RenderUtil.isLightingEnabled();
        if (!prevLighting) GlStateManager.enableLighting();
		double[] renderPos = NTMRenderHelper.getRenderPosFromMissile(missile, partialTicks);
		x = renderPos[0];
		y = renderPos[1];
		z = renderPos[2];
		GlStateManager.translate(x, y, z);
        GlStateManager.rotate(missile.prevRotationYaw + (missile.rotationYaw - missile.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(missile.prevRotationPitch + (missile.rotationPitch - missile.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(2F, 2F, 2F);

        GlStateManager.disableCull();
        bindTexture(getEntityTexture(missile));
        ResourceManager.missileTaint.renderAll();
        GlStateManager.enableCull();
        if (!prevLighting) GlStateManager.disableLighting();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMissileTier0.EntityMissileSchrabidium entity) {
		return ResourceManager.missileMicroSchrab_tex;
	}

}
