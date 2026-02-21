package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.effect.EntityEMPBlast;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.loader.IModelCustom;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@AutoRegister(factory = "FACTORY")
public class RenderEMPBlast extends Render<EntityEMPBlast> {

    private static final ResourceLocation RING_MODEL_RL = new ResourceLocation(Tags.MODID, "models/Ring.obj");
    public static final IRenderFactory<EntityEMPBlast> FACTORY = RenderEMPBlast::new;
    private final IModelCustom ringModel;
    private final ResourceLocation ringTexture;

    protected RenderEMPBlast(RenderManager renderManager) {
        super(renderManager);
        // TODO: move to ResourceManager
        this.ringModel = new HFRWavefrontObject(RING_MODEL_RL);
        this.ringTexture = new ResourceLocation(Tags.MODID, "textures/models/explosion/EMPBlast.png");
    }

    @Override
    public void doRender(EntityEMPBlast entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        final boolean prevLighting = RenderUtil.isLightingEnabled();
        final boolean prevCull = RenderUtil.isCullEnabled();

        GlStateManager.translate(x, y, z);
        if (prevLighting) GlStateManager.disableLighting();
        if (prevCull) GlStateManager.disableCull();

        GlStateManager.scale(entity.scale + partialTicks, 1F, entity.scale + partialTicks);

        bindTexture(ringTexture);
        ringModel.renderAll();
        if (prevCull) GlStateManager.enableCull();
        if (prevLighting) GlStateManager.enableLighting();

        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityEMPBlast entity) {
        return ringTexture;
    }
}
