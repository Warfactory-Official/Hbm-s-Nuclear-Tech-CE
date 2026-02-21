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
public class ParticleMukeCloud extends Particle {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID, "textures/particle/explosion.png");
    private final float friction;

    public ParticleMukeCloud(World world, double x, double y, double z, double mx, double my, double mz) {
        super(world, x, y, z, mx, my, mz);
        this.motionX = mx;
        this.motionY = my;
        this.motionZ = mz;

        if (motionY > 0) {
            this.friction = 0.9F;
            if (motionY > 0.1D) {
                this.particleMaxAge = 92 + rand.nextInt(11) + (int) (motionY * 20);
            } else {
                this.particleMaxAge = 72 + rand.nextInt(11);
            }
        } else if (motionY == 0D) {
            this.friction = 0.95F;
            this.particleMaxAge = 52 + rand.nextInt(11);
        } else {
            this.friction = 0.85F;
            this.particleMaxAge = 122 + rand.nextInt(31);
            this.particleAge = 80;
        }

        this.particleGravity = 0.0F;
        this.canCollide = false;
        this.setSize(0.2F, 0.2F);
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public void onUpdate() {
        this.canCollide = this.particleAge > 2;

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge - 2) {
            this.setExpired();
        }

        this.motionY -= 0.04D * (double) this.particleGravity;
        this.move(this.motionX, this.motionY, this.motionZ);
        this.motionX *= friction;
        this.motionY *= friction;
        this.motionZ *= friction;

        if (this.onGround) {
            this.motionX *= 0.7D;
            this.motionZ *= 0.7D;
        }
    }

    @Override
    public void renderParticle(BufferBuilder unusedBuffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        Minecraft.getMinecraft().renderEngine.bindTexture(getTexture());

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.disableStandardItemLighting();

        if (this.particleAge > this.particleMaxAge) this.particleAge = this.particleMaxAge;
        int texIndex = this.particleAge * 25 / this.particleMaxAge;
        float f0 = 1F / 5F;

        float uMin = (texIndex % 5) * f0;
        float uMax = uMin + f0;
        float vMin = (texIndex / 5) * f0;
        float vMax = vMin + f0;
        float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - Particle.interpPosX);
        float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - Particle.interpPosY);
        float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - Particle.interpPosZ);

        this.particleAlpha = 1F;
        this.particleScale = 3F;
        final int j = 240, k = 240;
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        float r = 1F, g = 1F, b = 1F, a = this.particleAlpha;
        double x0 = pX - rotationX * particleScale - rotationXY * particleScale;
        double y0 = pY - 1 * particleScale;
        double z0 = pZ - rotationYZ * particleScale - rotationXZ * particleScale;

        double x1 = pX - rotationX * particleScale + rotationXY * particleScale;
        double y1 = pY + 1 * particleScale;
        double z1 = pZ - rotationYZ * particleScale + rotationXZ * particleScale;

        double x2 = pX + rotationX * particleScale + rotationXY * particleScale;
        double y2 = pY + 1 * particleScale;
        double z2 = pZ + rotationYZ * particleScale + rotationXZ * particleScale;

        double x3 = pX + rotationX * particleScale - rotationXY * particleScale;
        double y3 = pY - 1 * particleScale;
        double z3 = pZ + rotationYZ * particleScale - rotationXZ * particleScale;

        buf.pos(x0, y0, z0).tex(uMax, vMax).color(r, g, b, a).lightmap(j, k).endVertex();
        buf.pos(x1, y1, z1).tex(uMax, vMin).color(r, g, b, a).lightmap(j, k).endVertex();
        buf.pos(x2, y2, z2).tex(uMin, vMin).color(r, g, b, a).lightmap(j, k).endVertex();
        buf.pos(x3, y3, z3).tex(uMin, vMax).color(r, g, b, a).lightmap(j, k).endVertex();

        tess.draw();

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
    }

    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
