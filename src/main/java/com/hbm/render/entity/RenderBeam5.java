package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.projectile.EntityExplosiveBeam;
import com.hbm.interfaces.AutoRegister;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

@AutoRegister(factory = "FACTORY")
public class RenderBeam5 extends Render<EntityExplosiveBeam> {

    public static final IRenderFactory<EntityExplosiveBeam> FACTORY = RenderBeam5::new;

    protected RenderBeam5(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityExplosiveBeam rocket, double x, double y, double z, float entityYaw, float partialTicks) {
        final float radius = 0.175F;
        final int distance = 2;
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buf = tessellator.getBuffer();
        GlStateManager.pushMatrix();
        final boolean prevTex2D   = RenderUtil.isTexture2DEnabled();
        final boolean prevCull    = RenderUtil.isCullEnabled();
        final boolean prevBlend   = RenderUtil.isBlendEnabled();
        final int prevSrc         = RenderUtil.getBlendSrcFactor();
        final int prevDst         = RenderUtil.getBlendDstFactor();
        final int prevSrcAlpha    = RenderUtil.getBlendSrcAlphaFactor();
        final int prevDstAlpha    = RenderUtil.getBlendDstAlphaFactor();
        final float prevR         = RenderUtil.getCurrentColorRed();
        final float prevG         = RenderUtil.getCurrentColorGreen();
        final float prevB         = RenderUtil.getCurrentColorBlue();
        final float prevA         = RenderUtil.getCurrentColorAlpha();
        if (prevTex2D) GlStateManager.disableTexture2D();
        if (prevCull)  GlStateManager.disableCull();
        if (!prevBlend) GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); // additive

        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.rotate(rocket.rotationYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-rocket.rotationPitch, 1.0F, 0.0F, 0.0F);

        final boolean red = false;
        final boolean green = false;
        final boolean blue = true;

        for (float o = 0; o <= radius; o += radius / 8.0F) {
            float color = 1.0f - (o * 8.333f);
            if (color < 0.0f) color = 0.0f;
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            GlStateManager.color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f);
            buf.pos(0 + o, 0 - o, 0).endVertex();
            buf.pos(0 + o, 0 + o, 0).endVertex();
            buf.pos(0 + o, 0 + o, 0 + distance).endVertex();
            buf.pos(0 + o, 0 - o, 0 + distance).endVertex();
            tessellator.draw();
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            GlStateManager.color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f);
            buf.pos(0 - o, 0 - o, 0).endVertex();
            buf.pos(0 + o, 0 - o, 0).endVertex();
            buf.pos(0 + o, 0 - o, 0 + distance).endVertex();
            buf.pos(0 - o, 0 - o, 0 + distance).endVertex();
            tessellator.draw();
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            GlStateManager.color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f);
            buf.pos(0 - o, 0 + o, 0).endVertex();
            buf.pos(0 - o, 0 - o, 0).endVertex();
            buf.pos(0 - o, 0 - o, 0 + distance).endVertex();
            buf.pos(0 - o, 0 + o, 0 + distance).endVertex();
            tessellator.draw();
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            GlStateManager.color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f);
            buf.pos(0 + o, 0 + o, 0).endVertex();
            buf.pos(0 - o, 0 + o, 0).endVertex();
            buf.pos(0 - o, 0 + o, 0 + distance).endVertex();
            buf.pos(0 + o, 0 + o, 0 + distance).endVertex();
            tessellator.draw();
        }
        GlStateManager.tryBlendFuncSeparate(prevSrc, prevDst, prevSrcAlpha, prevDstAlpha);
        if (!prevBlend) GlStateManager.disableBlend();
        if (prevCull)   GlStateManager.enableCull();
        if (prevTex2D)  GlStateManager.enableTexture2D();
        GlStateManager.color(prevR, prevG, prevB, prevA);
        GlStateManager.popMatrix();
    }

    @Override
    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {}

    @Override
    protected ResourceLocation getEntityTexture(EntityExplosiveBeam entity) {
        return new ResourceLocation(Tags.MODID + ":textures/models/projectiles/PlasmaBeam.png");
    }
}
