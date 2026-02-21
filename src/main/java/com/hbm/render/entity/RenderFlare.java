package com.hbm.render.entity;

import com.hbm.entity.grenade.EntityGrenadeFlare;
import com.hbm.entity.projectile.EntitySchrab;
import com.hbm.interfaces.AutoRegister;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import java.util.Random;

@AutoRegister(entity = EntityGrenadeFlare.class, factory = "FACTORY")
@AutoRegister(entity = EntitySchrab.class, factory = "FACTORY_SCHRAB")
public class RenderFlare<E extends Entity> extends Render<E> {

    public static final IRenderFactory<EntityGrenadeFlare> FACTORY = RenderFlare::new;
    public static final IRenderFactory<EntitySchrab> FACTORY_SCHRAB = RenderFlare::new;

    protected RenderFlare(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(E entity, double x, double y, double z, float entityYaw, float partialTicks) {
        final Tessellator tess = Tessellator.getInstance();
        final BufferBuilder buf = tess.getBuffer();
        RenderHelper.disableStandardItemLighting();

        float f1 = (entity.ticksExisted + 2.0F) / 250.0F;
        if (f1 > 1.0F) f1 = 1.0F;

        float f2 = 0.0F;
        int count = entity.ticksExisted < 250 ? entity.ticksExisted * 3 : 250;

        if (f1 > 0.8F) {
            f2 = (f1 - 0.8F) / 0.2F;
        }

        final Random random = new Random(432L);

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        final boolean prevTex2D = RenderUtil.isTexture2DEnabled();
        final boolean prevBlend = RenderUtil.isBlendEnabled();
        final int prevSrc = RenderUtil.getBlendSrcFactor();
        final int prevDst = RenderUtil.getBlendDstFactor();
        final int prevSrcAlpha = RenderUtil.getBlendSrcAlphaFactor();
        final int prevDstAlpha = RenderUtil.getBlendDstAlphaFactor();
        final boolean prevAlpha = RenderUtil.isAlphaEnabled();
        final boolean prevCull = RenderUtil.isCullEnabled();
        final boolean prevDepthMask = RenderUtil.isDepthMaskEnabled();
        final int prevShade = RenderUtil.getShadeModel();
        if (prevTex2D) GlStateManager.disableTexture2D();
        if (!prevBlend) GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        if (prevAlpha) GlStateManager.disableAlpha();
        if (!prevCull) GlStateManager.enableCull();
        if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(GL11.GL_SMOOTH);
        if (prevDepthMask) GlStateManager.depthMask(false);
        for (int i = 0; i < count; i++) {
            GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F + f1 * 90.0F, 0.0F, 0.0F, 1.0F);
            buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
            final float f3 = random.nextFloat() * 20.0F + 5.0F + f2 * 10.0F;
            final float f4 = random.nextFloat() * 2.0F + 1.0F + f2 * 2.0F;
            buf.pos(0.0D, 0.0D, 0.0D).color(0.5372549F, 0.54509807F, 0.2F, 1.0F - f2).endVertex();
            buf.pos(-0.866D * f4, f3, -0.5F * f4).color(0.5372549F, 0.54509807F, 0.27843139F, 0.0F).endVertex();
            buf.pos(0.866D * f4, f3, -0.5F * f4).color(0.5372549F, 0.54509807F, 0.27843139F, 0.0F).endVertex();
            buf.pos(0.0D, f3, f4).color(0.5372549F, 0.54509807F, 0.27843139F, 0.0F).endVertex();
            buf.pos(-0.866D * f4, f3, -0.5F * f4).color(0.5372549F, 0.54509807F, 0.27843139F, 0.0F).endVertex();
            GlStateManager.scale(0.99F, 0.99F, 0.99F);

            tess.draw();
        }
        if (prevDepthMask) GlStateManager.depthMask(true);
        if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(prevShade);
        GlStateManager.tryBlendFuncSeparate(prevSrc, prevDst, prevSrcAlpha, prevDstAlpha);
        if (prevAlpha) GlStateManager.enableAlpha();
        if (!prevCull) GlStateManager.disableCull();
        if (!prevBlend) GlStateManager.disableBlend();
        if (prevTex2D) GlStateManager.enableTexture2D();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.popMatrix();
        RenderHelper.enableStandardItemLighting();
    }

    @Override
    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
    }

    @Override
    protected ResourceLocation getEntityTexture(E entity) {
        return null;
    }
}
