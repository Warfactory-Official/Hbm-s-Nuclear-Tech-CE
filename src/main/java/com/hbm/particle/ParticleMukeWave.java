package com.hbm.particle;

import com.hbm.Tags;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ParticleMukeWave extends Particle {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID, "textures/particle/shockwave.png");
    private float waveScale = 45.0F;

    public ParticleMukeWave(World world, double x, double y, double z) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.particleMaxAge = 25;
        this.particleGravity = 0.0F;
        this.canCollide = false;
        this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
        this.particleAlpha = 1.0F;
        this.setSize(0.2F, 0.2F);
    }

    public void setup(float scale, int maxAge) {
        this.waveScale = scale;
        this.setMaxAge(maxAge);
    }

    @Override
    public void renderParticle(BufferBuilder ignoredBuffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
        GlStateManager.depthMask(false);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.disableCull();
        RenderHelper.disableStandardItemLighting();

        boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
        if (fog) GL11.glDisable(GL11.GL_FOG);

        this.particleAlpha = 1.0F - (((float) this.particleAge + partialTicks) / (float) this.particleMaxAge);
        float growth = (1.0F - (float) Math.exp((this.particleAge + partialTicks) * -0.125F)) * waveScale;
        float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - Particle.interpPosX);
        float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - Particle.interpPosY);
        float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - Particle.interpPosZ);
        final int j = 240;
        final int k = 240;
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        double y = pY - 0.25D;
        double x0 = pX - growth;
        double x1 = pX + growth;
        double z0 = pZ - growth;
        double z1 = pZ + growth;
        buf.pos(x0, y, z0).tex(1, 1).color(1F, 1F, 1F, this.particleAlpha).lightmap(j, k).endVertex();
        buf.pos(x0, y, z1).tex(1, 0).color(1F, 1F, 1F, this.particleAlpha).lightmap(j, k).endVertex();
        buf.pos(x1, y, z1).tex(0, 0).color(1F, 1F, 1F, this.particleAlpha).lightmap(j, k).endVertex();
        buf.pos(x1, y, z0).tex(0, 1).color(1F, 1F, 1F, this.particleAlpha).lightmap(j, k).endVertex();
        tess.draw();

// Restore GL state
        GlStateManager.doPolygonOffset(0,0);
        GlStateManager.enableCull();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableLighting();
        if (fog) GL11.glEnable(GL11.GL_FOG);
        GlStateManager.depthMask(true);
    }

    @Override
    public int getFXLayer() {
        return 3;
    }
}
