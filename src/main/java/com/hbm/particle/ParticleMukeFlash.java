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

import java.util.Random;

@SideOnly(Side.CLIENT)
public class ParticleMukeFlash extends Particle {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID, "textures/particle/flare.png");
    private final boolean bf;

    public ParticleMukeFlash(World world, double x, double y, double z, boolean bf) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.particleMaxAge = 20;
        this.particleGravity = 0.0F;
        this.canCollide = false;
        this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
        this.particleAlpha = 1.0F;
        this.bf = bf;
        this.setSize(0.2F, 0.2F);
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.particleAge == 15) {
            // Stem
            for (double d = 0.0D; d <= 1.8D; d += 0.1D) {
                ParticleMukeCloud cloud = getCloud(world, posX, posY, posZ, rand.nextGaussian() * 0.05, d + rand.nextGaussian() * 0.02, rand.nextGaussian() * 0.05);
                Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
            }

            // Ground
            for (int i = 0; i < 100; i++) {
                ParticleMukeCloud cloud = getCloud(world, posX, posY + 0.5, posZ, rand.nextGaussian() * 0.5, rand.nextInt(5) == 0 ? 0.02 : 0.0, rand.nextGaussian() * 0.5);
                Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
            }

            // Mush
            for (int i = 0; i < 75; i++) {
                double x = rand.nextGaussian() * 0.5;
                double z = rand.nextGaussian() * 0.5;

                if (x * x + z * z > 1.5) {
                    x *= 0.5;
                    z *= 0.5;
                }

                double y = 1.8 + (rand.nextDouble() * 3.0 - 1.5) * (0.75 - (x * x + z * z)) * 0.5;

                ParticleMukeCloud cloud = getCloud(world, posX, posY, posZ, x, y + rand.nextGaussian() * 0.02, z);
                Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
            }
        }
    }

    private ParticleMukeCloud getCloud(World world, double x, double y, double z, double mx, double my, double mz) {
        return bf ? new ParticleMukeCloudBF(world, x, y, z, mx, my, mz) : new ParticleMukeCloud(world, x, y, z, mx, my, mz);
    }

    @Override
    public void renderParticle(BufferBuilder unusedBuffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);

        boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
        if (fog) GL11.glDisable(GL11.GL_FOG);

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderHelper.disableStandardItemLighting();

        this.particleAlpha = 1.0F - (((float) this.particleAge + partialTicks) / (float) this.particleMaxAge);
        float scale = (this.particleAge + partialTicks) * 3.0F + 1.0F;

        float dX = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - Particle.interpPosX);
        float dY = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - Particle.interpPosY);
        float dZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - Particle.interpPosZ);
        final int lightJ = 240;
        final int lightK = 240;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        float r = 1.0F, g = 0.9F, b = 0.75F, a = this.particleAlpha * 0.5F;
        Random seeded = new Random();

        for (int i = 0; i < 24; i++) {
            seeded.setSeed(i * 31L + 1L);

            float pX = (float) (dX + seeded.nextDouble() * 15.0 - 7.5);
            float pY = (float) (dY + seeded.nextDouble() * 7.5 - 3.75);
            float pZ = (float) (dZ + seeded.nextDouble() * 15.0 - 7.5);

            double x0 = pX - rotationX * scale - rotationXY * scale;
            double y0 = pY - rotationZ * scale;
            double z0 = pZ - rotationYZ * scale - rotationXZ * scale;

            double x1 = pX - rotationX * scale + rotationXY * scale;
            double y1 = pY + rotationZ * scale;
            double z1 = pZ - rotationYZ * scale + rotationXZ * scale;

            double x2 = pX + rotationX * scale + rotationXY * scale;
            double y2 = pY + rotationZ * scale;
            double z2 = pZ + rotationYZ * scale + rotationXZ * scale;

            double x3 = pX + rotationX * scale - rotationXY * scale;
            double y3 = pY - rotationZ * scale;
            double z3 = pZ + rotationYZ * scale - rotationXZ * scale;

            buf.pos(x0, y0, z0).tex(1, 1).color(r, g, b, a).lightmap(lightJ, lightK).endVertex();
            buf.pos(x1, y1, z1).tex(1, 0).color(r, g, b, a).lightmap(lightJ, lightK).endVertex();
            buf.pos(x2, y2, z2).tex(0, 0).color(r, g, b, a).lightmap(lightJ, lightK).endVertex();
            buf.pos(x3, y3, z3).tex(0, 1).color(r, g, b, a).lightmap(lightJ, lightK).endVertex();
        }

        tess.draw();

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableLighting();
        if (fog) GL11.glEnable(GL11.GL_FOG);
        GlStateManager.depthMask(true);
    }
}
