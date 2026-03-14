package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.jetbrains.annotations.NotNull;

@AutoRegister(factory = "FACTORY")
public class RenderCloudFleija extends Render<EntityCloudFleija> {

    public static final IRenderFactory<EntityCloudFleija> FACTORY = RenderCloudFleija::new;
    private static final ResourceLocation objTesterModelRL = new ResourceLocation(/*"/assets/" + */Tags.MODID, "models/Sphere.obj");
    public float scale;
    public float ring = 0;
    private final IModelCustom blastModel;
    private final ResourceLocation blastTexture;

    protected RenderCloudFleija(RenderManager renderManager) {
        super(renderManager);
        blastModel = new HFRWavefrontObject(objTesterModelRL);
        blastTexture = new ResourceLocation(Tags.MODID, "textures/models/explosion/BlastFleija.png");
        scale = 0;
    }

    @Override
    public void doRender(EntityCloudFleija cloud, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.disableLighting();
        GlStateManager.disableLighting();
        GlStateManager.enableCull();

        float s = cloud.age + partialTicks;
        GlStateManager.scale(s, s, s);

        bindTexture(blastTexture);
        blastModel.renderAll();

        GlStateManager.enableLighting();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public void doRenderShadowAndFire(@NotNull Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
    }

    @Override
    protected ResourceLocation getEntityTexture(@NotNull EntityCloudFleija entity) {
        return blastTexture;
    }

}
