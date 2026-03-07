package com.hbm.particle;

import com.hbm.Tags;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public class ParticleRBMKSteam extends Particle {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID + ":textures/particle/rbmk_jet_steam.png");

    public ParticleRBMKSteam(World world, double x, double y, double z) {
        super(world, x, y, z);
        this.particleMaxAge = 30;
        this.particleAlpha = 0.25F;
        this.particleScale = 4F;
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public void renderParticle(@NotNull BufferBuilder buffer, @NotNull Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(getTexture());

        boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
        if (fog) GlStateManager.disableFog();
        NTMRenderHelper.resetParticleInterpPos(entityIn, partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        GlStateManager.depthMask(false);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderHelper.disableStandardItemLighting();

        if (this.particleAge > this.particleMaxAge) {
            this.particleAge = this.particleMaxAge;
        }

        int texIndex = (int) (((double) this.particleAge / (double) this.particleMaxAge) * 20) % 20 - 1;
        if (texIndex < 0) texIndex = 0;
        float f0 = 1F / 20F;

        float uMin = texIndex * f0;
        float uMax = uMin + f0;
        float vMin = 0;
        float vMax = 1;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tess.getBuffer();

        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

        Entity player = Minecraft.getMinecraft().player;
        double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
        double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
        double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;

        float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - dX);
        float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - dY);
        float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - dZ);

        GlStateManager.translate(pX, pY, pZ);
        GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);

        double scale0 = this.particleScale * -0.25 - 1;
        double scale1 = this.particleScale - 0.25;
        double scale2 = this.particleScale * 0.25 - 1;
        float alpha = this.particleAlpha;

        vertexbuffer.pos(scale0, -0.25, 0).tex(uMax, vMax).color(1.0F, 1.0F, 1.0F, alpha).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexbuffer.pos(scale0, scale1, 0).tex(uMax, vMin).color(1.0F, 1.0F, 1.0F, alpha).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexbuffer.pos(scale2, scale1, 0).tex(uMin, vMin).color(1.0F, 1.0F, 1.0F, alpha).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexbuffer.pos(scale2, -0.25, 0).tex(uMin, vMax).color(1.0F, 1.0F, 1.0F, alpha).normal(0.0F, 1.0F, 0.0F).endVertex();

        tess.draw();

        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        if (fog) GlStateManager.enableFog();
    }

    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}

