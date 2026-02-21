package com.hbm.particle;

import com.hbm.main.client.NTMClientRegistry;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleCoolingTower extends Particle {

    private float baseScale = 1.0F;
    private float maxScale = 1.0F;
    private float lift = 0.3F;
    private float strafe = 0.075F;
    private boolean windDir = true;
    private float alphaMod = 0.25F;

    public ParticleCoolingTower(World world, double x, double y, double z) {
        super(world, x, y, z);
        this.particleAlpha = 0.30F; //Norwood: best solution to make the particle transparent with current minecraft version
        this.particleRed = this.particleGreen = this.particleBlue = 0.9F + world.rand.nextFloat() * 0.05F;
        this.canCollide = false;
        this.setParticleTexture(NTMClientRegistry.particle_base);
    }

    public void setBaseScale(float f) {
        this.baseScale = f;
    }

    public void setMaxScale(float f) {
        this.maxScale = f;
    }

    public void setLift(float f) {
        this.lift = f;
    }

    public void setLife(int i) {
        this.particleMaxAge = i;
    }

    public void setStrafe(float f) {
        this.strafe = f;
    }

    public void noWind() {
        this.windDir = false;
    }

    public void alphaMod(float mod) {
        this.alphaMod = mod;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        float ageScale = (float) this.particleAge / (float) this.particleMaxAge;
        this.particleAlpha = alphaMod - ageScale * alphaMod;
        this.particleScale = baseScale + (float) Math.pow((maxScale * ageScale - baseScale), 2);

        this.particleAge++;
        if (lift > 0 && this.motionY < this.lift) this.motionY += 0.01F;
        if (lift < 0 && this.motionY > this.lift) this.motionY -= 0.01F;

        this.motionX += rand.nextGaussian() * strafe * ageScale;
        this.motionZ += rand.nextGaussian() * strafe * ageScale;

        if (windDir) {
            this.motionX += 0.02 * ageScale;
            this.motionZ -= 0.01 * ageScale;
        }

        if (this.particleAge >= this.particleMaxAge) this.setExpired();
        this.move(this.motionX, this.motionY, this.motionZ);
        motionX *= 0.925;
        motionY *= 0.925;
        motionZ *= 0.925;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        if(particleAge == 0) return;

        GlStateManager.depthMask(true);

        float f4 = this.particleScale;
        float f = this.particleTexture.getMinU();
        float f1 = this.particleTexture.getMaxU();
        float f2 = this.particleTexture.getMinV();
        float f3 = this.particleTexture.getMaxV();

        float f5 = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
        float f6 = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
        float f7 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);
        int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;
        Vec3d[] avec3d = new Vec3d[]{new Vec3d(-rotationX * f4 - rotationXY * f4, -rotationZ * f4, -rotationYZ * f4 - rotationXZ * f4), new Vec3d(-rotationX * f4 + rotationXY * f4, rotationZ * f4, -rotationYZ * f4 + rotationXZ * f4), new Vec3d(rotationX * f4 + rotationXY * f4, rotationZ * f4, rotationYZ * f4 + rotationXZ * f4), new Vec3d(rotationX * f4 - rotationXY * f4, -rotationZ * f4, rotationYZ * f4 - rotationXZ * f4)};
        buffer.pos((double) f5 + avec3d[0].x, (double) f6 + avec3d[0].y, (double) f7 + avec3d[0].z).tex(f1, f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double) f5 + avec3d[1].x, (double) f6 + avec3d[1].y, (double) f7 + avec3d[1].z).tex(f1, f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double) f5 + avec3d[2].x, (double) f6 + avec3d[2].y, (double) f7 + avec3d[2].z).tex(f, f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double) f5 + avec3d[3].x, (double) f6 + avec3d[3].y, (double) f7 + avec3d[3].z).tex(f, f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();

        GlStateManager.depthMask(false);
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

}
