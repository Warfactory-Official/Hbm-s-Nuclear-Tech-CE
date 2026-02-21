package com.hbm.particle;

import com.hbm.entity.particle.ParticleFXRotating;
import com.hbm.main.client.NTMClientRegistry;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@SideOnly(Side.CLIENT)
public class ParticleBlackPowderSmoke extends ParticleFXRotating {

    public float hue;

    public ParticleBlackPowderSmoke(World world, double x, double y, double z, float scale) {
        super(world, x, y, z);
        this.particleMaxAge = 30 + rand.nextInt(15);
        this.particleScale = scale * 0.9F + rand.nextFloat() * 0.2F;

        this.particleGravity = 0F;

        this.hue = 20F + rand.nextFloat() * 20F;
        Color color = Color.getHSBColor(hue / 255F, 1F, 1F);
        this.particleRed = color.getRed() / 255F;
        this.particleGreen = color.getGreen() / 255F;
        this.particleBlue = color.getBlue() / 255F;
        this.setParticleTexture(NTMClientRegistry.particle_base);
        this.canCollide = false;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        this.particleAge++;

        if(this.particleAge >= this.particleMaxAge) {
            this.setExpired();
        }

        this.motionY -= particleGravity;
        this.prevParticleAngle = this.particleAngle;

        float ageScaled = (float) this.particleAge / (float) this.particleMaxAge;
        this.particleAngle += (1 - ageScaled) * 2 * ((this.hashCode() % 2) - 0.5);

        this.motionX *= 0.65D;
        this.motionY *= 0.65D;
        this.motionZ *= 0.65D;

        this.move(this.motionX, this.motionY, this.motionZ);
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        double ageScaled = (this.particleAge + partialTicks) / (double) this.particleMaxAge;

        Color color = Color.getHSBColor(
                hue / 255F,
                Math.max(1F - (float) ageScaled * 4F, 0F),
                MathHelper.clamp(1.25F - (float) ageScaled * 2F, 0.7F, 1F)
        );

        this.setRBGColorF(
                color.getRed() / 255F,
                color.getGreen() / 255F,
                color.getBlue() / 255F
        );
        this.particleAlpha = (float) Math.pow(1 - Math.min(ageScaled, 1), 0.25) * 0.7F;


        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);

        GlStateManager.depthMask(false);

        newScale = (float) (0.25 + ageScaled + (this.particleAge + partialTicks) * 0.025) * this.particleScale;
        super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }
}
