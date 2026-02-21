package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.projectile.EntityPlasmaBeam;
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
public class RenderBeam extends Render<EntityPlasmaBeam> {

    public static final IRenderFactory<EntityPlasmaBeam> FACTORY = RenderBeam::new;

    protected ResourceLocation beam_rl = new ResourceLocation(Tags.MODID + ":textures/models/projectiles/PlasmaBeam.png");

    protected RenderBeam(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityPlasmaBeam rocket, double x, double y, double z, float entityYaw, float partialTicks) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buf = tessellator.getBuffer();
        final boolean wasCull = RenderUtil.isCullEnabled();
        final boolean wasBlend = RenderUtil.isBlendEnabled();
        final int src = RenderUtil.getBlendSrcFactor();
        final int dst = RenderUtil.getBlendDstFactor();
        final int srcA = RenderUtil.getBlendSrcAlphaFactor();
        final int dstA = RenderUtil.getBlendDstAlphaFactor();
        final int unit = RenderUtil.getActiveTextureUnitIndex();
        final boolean wasTex2D = RenderUtil.isTexture2DEnabled(unit);

        GlStateManager.pushMatrix();
        if (wasTex2D) GlStateManager.disableTexture2D();
        if (wasCull) GlStateManager.disableCull();
        if (!wasBlend) GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE);
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.rotate(rocket.rotationYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-rocket.rotationPitch, 1.0F, 0.0F, 0.0F);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        Tessellator.getInstance().draw();
        GlStateManager.tryBlendFuncSeparate(src, dst, srcA, dstA);
        if (!wasBlend) GlStateManager.disableBlend();
        if (wasCull) GlStateManager.enableCull();
        if (wasTex2D) GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    @Override
    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPlasmaBeam entity) {
        return beam_rl;
    }

}
