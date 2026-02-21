package com.hbm.render.entity;

import com.hbm.entity.projectile.EntityBurningFOEQ;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import java.util.Random;

@AutoRegister(factory = "FACTORY")
public class RenderFOEQ extends Render<EntityBurningFOEQ> {

    public static final IRenderFactory<EntityBurningFOEQ> FACTORY = RenderFOEQ::new;

    public RenderFOEQ(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityBurningFOEQ e, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        final boolean prevTex2D = RenderUtil.isTexture2DEnabled();
        final boolean prevCull = RenderUtil.isCullEnabled();
        final boolean prevBlend = RenderUtil.isBlendEnabled();
        final boolean prevLighting = RenderUtil.isLightingEnabled();
        final int prevSrc = RenderUtil.getBlendSrcFactor();
        final int prevDst = RenderUtil.getBlendDstFactor();
        final int prevSrcAlpha = RenderUtil.getBlendSrcAlphaFactor();
        final int prevDstAlpha = RenderUtil.getBlendDstAlphaFactor();
        final float prevR = RenderUtil.getCurrentColorRed();
        final float prevG = RenderUtil.getCurrentColorGreen();
        final float prevB = RenderUtil.getCurrentColorBlue();
        final float prevA = RenderUtil.getCurrentColorAlpha();

        GlStateManager.translate((float) x, (float) y - 10F, (float) z);
        GlStateManager.rotate(e.prevRotationYaw + (e.rotationYaw - e.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180F, 0F, 0F, 1F);
        GlStateManager.rotate(e.prevRotationPitch + (e.rotationPitch - e.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);

        if (!prevLighting) GlStateManager.enableLighting();
        if (!prevCull) GlStateManager.enableCull();

        bindTexture(ResourceManager.sat_foeq_burning_tex);
        ResourceManager.sat_foeq_burning.renderAll();

        if (prevTex2D) GlStateManager.disableTexture2D();
        if (prevCull) GlStateManager.disableCull();
        if (!prevBlend) GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        final Random rand = new Random(System.currentTimeMillis() / 50L);

        GlStateManager.scale(1.15F, 0.75F, 1.15F);
        GlStateManager.translate(0F, -0.5F, 0.3F);

        for (int i = 0; i < 10; i++) {
            GlStateManager.color(1F, 0.75F, 0.25F, 1F);
            GlStateManager.rotate(rand.nextInt(360), 0F, 1F, 0F);
            ResourceManager.sat_foeq_fire.renderAll();
            GlStateManager.translate(0F, 2F, 0F);

            GlStateManager.color(1F, 0.5F, 0F, 1F);
            GlStateManager.rotate(rand.nextInt(360), 0F, 1F, 0F);
            ResourceManager.sat_foeq_fire.renderAll();
            GlStateManager.translate(0F, 2F, 0F);

            GlStateManager.color(1F, 0.25F, 0F, 1F);
            GlStateManager.rotate(rand.nextInt(360), 0F, 1F, 0F);
            ResourceManager.sat_foeq_fire.renderAll();
            GlStateManager.translate(0F, 2F, 0F);

            GlStateManager.color(1F, 0.15F, 0F, 1F);
            GlStateManager.rotate(rand.nextInt(360), 0F, 1F, 0F);
            ResourceManager.sat_foeq_fire.renderAll();

            GlStateManager.translate(0F, -3.8F, 0F);
            GlStateManager.scale(0.95F, 1.2F, 0.95F);
        }
        GlStateManager.tryBlendFuncSeparate(prevSrc, prevDst, prevSrcAlpha, prevDstAlpha);
        if (!prevBlend) GlStateManager.disableBlend();
        if (prevCull) GlStateManager.enableCull();
        if (prevTex2D) GlStateManager.enableTexture2D();
        if (!prevLighting) GlStateManager.disableLighting();
        GlStateManager.color(prevR, prevG, prevB, prevA);

        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBurningFOEQ entity) {
        return ResourceManager.sat_foeq_tex;
    }
}
