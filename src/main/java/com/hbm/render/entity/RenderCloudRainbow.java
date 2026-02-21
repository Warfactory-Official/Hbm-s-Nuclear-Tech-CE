package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.effect.EntityCloudFleijaRainbow;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.loader.IModelCustom;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

@AutoRegister(factory = "FACTORY")
public class RenderCloudRainbow extends Render<EntityCloudFleijaRainbow> {
    private static final ResourceLocation OBJ_SPHERE = new ResourceLocation(Tags.MODID, "models/sphere.obj");
    //Drillgon200: Hey I figured out how to use a lambda!
    public static final IRenderFactory<EntityCloudFleijaRainbow> FACTORY = RenderCloudRainbow::new;
    private final IModelCustom blastModel;
    public float scale = 0;
    public float ring = 0;

    protected RenderCloudRainbow(RenderManager renderManager) {
        super(renderManager);
        // TODO: move to ResourceManager
        this.blastModel = new HFRWavefrontObject(OBJ_SPHERE);
        this.scale = 0;
    }

    @Override
    public void doRender(EntityCloudFleijaRainbow cloud, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        final boolean prevTex2D = RenderUtil.isTexture2DEnabled();
        final boolean prevCull = RenderUtil.isCullEnabled();
        final boolean prevBlend = RenderUtil.isBlendEnabled();
        final int prevSrc = RenderUtil.getBlendSrcFactor();
        final int prevDst = RenderUtil.getBlendDstFactor();
        final int prevSrcAlpha = RenderUtil.getBlendSrcAlphaFactor();
        final int prevDstAlpha = RenderUtil.getBlendDstAlphaFactor();
        final boolean prevLighting = RenderUtil.isLightingEnabled();
        final float prevR = RenderUtil.getCurrentColorRed();
        final float prevG = RenderUtil.getCurrentColorGreen();
        final float prevB = RenderUtil.getCurrentColorBlue();
        final float prevA = RenderUtil.getCurrentColorAlpha();
        GlStateManager.translate(x, y, z);
        if (prevLighting) GlStateManager.disableLighting();
        if (!prevCull) GlStateManager.enableCull();
        if (prevTex2D) GlStateManager.disableTexture2D();
        if (!prevBlend) GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        final float s = cloud.age + partialTicks;
        GlStateManager.scale(s, s, s);
        GlStateManager.color((cloud.world.rand.nextInt(256)) / 255.0f, (cloud.world.rand.nextInt(256)) / 255.0f, (cloud.world.rand.nextInt(256)) / 255.0f, 1.0f);

        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        blastModel.renderAll();
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        for (float i = 0.6F; i <= 1.0F; i += 0.1F) {
            GlStateManager.color((cloud.world.rand.nextInt(256)) / 255.0f, (cloud.world.rand.nextInt(256)) / 255.0f, (cloud.world.rand.nextInt(256)) / 255.0f, 1.0f);
            GlStateManager.scale(i, i, i);
            blastModel.renderAll();
            GlStateManager.scale(1.0F / i, 1.0F / i, 1.0F / i);
        }

        GlStateManager.tryBlendFuncSeparate(prevSrc, prevDst, prevSrcAlpha, prevDstAlpha);
        if (!prevBlend) GlStateManager.disableBlend();
        if (prevTex2D) GlStateManager.enableTexture2D();
        if (!prevCull) GlStateManager.disableCull();
        if (prevLighting) GlStateManager.enableLighting();
        GlStateManager.color(prevR, prevG, prevB, prevA);

        GlStateManager.popMatrix();
    }

    @Override
    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCloudFleijaRainbow entity) {
        return null;
    }
}
