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
public class ParticleExplosionSmall extends ParticleFXRotating {


    public ParticleExplosionSmall(World world, double x, double y, double z, float scale, float speedMult) {
        super(world, x, y, z);

        this.particleMaxAge = 25 + this.rand.nextInt(10);
        this.particleScale = scale * 0.9F + rand.nextFloat() * 0.2F;

        this.motionX = world.rand.nextGaussian() * speedMult;
        this.motionZ = world.rand.nextGaussian() * speedMult;
        this.particleGravity = rand.nextFloat() * -0.01F;

        this.hue = 20F + rand.nextFloat() * 20F;
        Color color = Color.getHSBColor(hue / 255F, 1F, 1F);
        this.setRBGColorF(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);

        this.canCollide = true;
        this.setParticleTexture(NTMClientRegistry.particle_base);
    }

    @Override
    public int getBrightnessForRender(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        this.particleAge++;

        if (this.particleAge >= this.particleMaxAge) {
            this.setExpired();
        }

        this.motionY -= particleGravity;
        this.prevParticleAngle = this.particleAngle;

        float ageScaled = (float) this.particleAge / (float) this.particleMaxAge;
        this.particleAngle += (float) ((1 - ageScaled) * 5 * ((this.hashCode() % 2) - 0.5));
        this.motionX *= 0.65D;
        this.motionZ *= 0.65D;

        this.move(this.motionX, this.motionY, this.motionZ);
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        double ageScaled = (double) (this.particleAge + partialTicks) / (double) this.particleMaxAge;

        Color color = Color.getHSBColor(
                hue / 255F,
                Math.max(1F - (float) ageScaled * 2F, 0F),
                MathHelper.clamp(1.25F - (float) ageScaled * 2F, hue * 0.01F - 0.1F, 1F)
        );

        this.setRBGColorF(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
        this.particleAlpha = (float) Math.pow(1 - Math.min(ageScaled, 1), 0.25) * 0.7F; //alpha correction as 1.7 particles are less opaque


        GlStateManager.color(particleRed, particleGreen, particleBlue, particleAlpha * 0.5F);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);


        newScale = (float) (0.25 + 1 - Math.pow(1 - ageScaled, 4) + (this.particleAge + partialTicks) * 0.02) * this.particleScale;
        super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }


}
