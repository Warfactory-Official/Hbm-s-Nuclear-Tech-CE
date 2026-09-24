package com.hbm.particle;

import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

public class ParticleAmatFlash extends Particle {
    public ParticleAmatFlash(World world, double x, double y, double z, float scale) {
        super(world, x, y, z);
        this.particleMaxAge = 10;
        this.particleScale = scale;
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void renderParticle(BufferBuilder _buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
        double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
        double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;

        float pX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - dX));
        float pY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - dY));
        float pZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - dZ));


        GlStateManager.pushMatrix();
        GlStateManager.translate(pX, pY, pZ);

        GlStateManager.scale(0.2F * particleScale, 0.2F * particleScale, 0.2F * particleScale);

        double intensity = (double) (this.particleAge + partialTicks) / (double) this.particleMaxAge;
        double inverse = 1.0D - intensity;

        RenderHelper.disableStandardItemLighting();

        Random random = new Random(432L);
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.disableAlpha();
        GlStateManager.enableCull();
        GlStateManager.depthMask(false);

        float scale = 0.5F;

        for(int i = 0; i < 100; i++) {

            GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);

            float vert1 = (random.nextFloat() * 20.0F + 5.0F + 1 * 10.0F) * (float) (intensity * scale);
            float vert2 = (random.nextFloat() * 2.0F + 1.0F + 1 * 2.0F) * (float) (intensity * scale);

            BufferBuilder buffer = NTMImmediate.INSTANCE.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);

            buffer.pos(0.0, 0.0, 0.0).color(1f, 1f, 1f, (float) inverse).endVertex();
            buffer.pos(-0.866D * vert2, vert1, -0.5F * vert2).color(1f, 1f, 1f, 0).endVertex();
            buffer.pos(0.866D * vert2, vert1, -0.5F * vert2).color(1f, 1f, 1f, 0).endVertex();
            buffer.pos(0.0D, vert1, vert2).color(1f, 1f, 1f, 0).endVertex();
            buffer.pos(-0.866D * vert2, vert1, -0.5F * vert2).color(1f, 1f, 1f, 0).endVertex();
            NTMImmediate.INSTANCE.draw();
        }

        GlStateManager.popMatrix();

        GlStateManager.depthMask(true);
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        RenderHelper.enableStandardItemLighting();
    }
}
