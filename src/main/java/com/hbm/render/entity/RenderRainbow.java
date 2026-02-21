package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.projectile.EntityRainbow;
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
public class RenderRainbow extends Render<EntityRainbow> {

    public static final IRenderFactory<EntityRainbow> FACTORY = RenderRainbow::new;

    protected static final ResourceLocation rainbow_rl =
            new ResourceLocation(Tags.MODID + ":textures/models/Rainbow.png");

    protected RenderRainbow(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityRainbow rocket, double x, double y, double z, float entityYaw, float partialTicks) {
        final float radius = 0.12F;
        final int distance = 4;

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buf = tessellator.getBuffer();

        GlStateManager.pushMatrix();

        final boolean prevTex2D  = RenderUtil.isTexture2DEnabled();
        final boolean prevCull   = RenderUtil.isCullEnabled();
        final boolean prevBlend  = RenderUtil.isBlendEnabled();
        final int prevSrc        = RenderUtil.getBlendSrcFactor();
        final int prevDst        = RenderUtil.getBlendDstFactor();
        final int prevSrcAlpha   = RenderUtil.getBlendSrcAlphaFactor();
        final int prevDstAlpha   = RenderUtil.getBlendDstAlphaFactor();

        if (prevTex2D) GlStateManager.disableTexture2D();
        if (prevCull)  GlStateManager.disableCull();
        if (!prevBlend) GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); // additive

        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.rotate(rocket.rotationYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-rocket.rotationPitch, 1.0F, 0.0F, 0.0F);

        final boolean red   = rocket.getDataManager().get(EntityRainbow.RED);
        final boolean green = rocket.getDataManager().get(EntityRainbow.GREEN);
        final boolean blue  = rocket.getDataManager().get(EntityRainbow.BLUE);

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (float o = 0.0F; o <= radius; o += radius / 8.0F) {
            float color = 1.0F - (o * 8.333F);
            if (color < 0.0F) color = 0.0F;

            buf.pos(0 + o, 0 - o, 0).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1F).endVertex();
            buf.pos(0 + o, 0 + o, 0).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1F).endVertex();
            buf.pos(0 + o, 0 + o, 0 + distance).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1F).endVertex();
            buf.pos(0 + o, 0 - o, 0 + distance).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1F).endVertex();

            buf.pos(0 - o, 0 - o, 0).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1F).endVertex();
            buf.pos(0 + o, 0 - o, 0).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1F).endVertex();
            buf.pos(0 + o, 0 - o, 0 + distance).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1F).endVertex();
            buf.pos(0 - o, 0 - o, 0 + distance).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1F).endVertex();

            buf.pos(0 - o, 0 + o, 0).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1F).endVertex();
            buf.pos(0 - o, 0 - o, 0).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1F).endVertex();
            buf.pos(0 - o, 0 - o, 0 + distance).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1F).endVertex();
            buf.pos(0 - o, 0 + o, 0 + distance).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1F).endVertex();

            buf.pos(0 + o, 0 + o, 0).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1F).endVertex();
            buf.pos(0 - o, 0 + o, 0).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1F).endVertex();
            buf.pos(0 - o, 0 + o, 0 + distance).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1F).endVertex();
            buf.pos(0 + o, 0 + o, 0 + distance).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1F).endVertex();
        }
        tessellator.draw();
        GlStateManager.tryBlendFuncSeparate(prevSrc, prevDst, prevSrcAlpha, prevDstAlpha);
        if (!prevBlend) GlStateManager.disableBlend();
        if (prevCull)   GlStateManager.enableCull();
        if (prevTex2D)  GlStateManager.enableTexture2D();

        GlStateManager.popMatrix();
    }

    @Override
    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {}

    @Override
    protected ResourceLocation getEntityTexture(EntityRainbow entity) {
        return rainbow_rl;
    }
}
