package com.hbm.particle;

import com.hbm.main.client.NTMClientRegistry;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleLiquidSplash extends Particle {

    public ParticleLiquidSplash(World world, double x, double y, double z) {
        super(world, x, y, z);
        this.setParticleTexture(NTMClientRegistry.particle_base);
        this.particleRed = this.particleGreen = this.particleBlue = 1F - world.rand.nextFloat() * 0.2F;
        this.particleAlpha = 0.5F;
        this.particleScale = 0.4F;
        this.particleMaxAge = 200 + world.rand.nextInt(50);
        this.particleGravity = 0.4F;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!this.onGround) {
            this.motionX += rand.nextGaussian() * 0.002D;
            this.motionZ += rand.nextGaussian() * 0.002D;

            if (this.motionY < -0.5D)
                this.motionY = -0.5D;
        } else {
            this.setExpired();
        }
    }

    @Override
    public int getFXLayer() {
        return 1; // Use particle texture sheet
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        assert this.particleTexture != null;

        float f4 = this.particleScale;

        boolean flipU = this.hashCode() % 2 == 0; //Hashcode substitutes entityID
        boolean flipV = this.hashCode() % 4 < 2;


        double minU = flipU ? this.particleTexture.getMaxU() : this.particleTexture.getMinU();
        double maxU = flipU ? this.particleTexture.getMinU() : this.particleTexture.getMaxU();
        double minV = flipV ? this.particleTexture.getMaxV() : this.particleTexture.getMinV();
        double maxV = flipV ? this.particleTexture.getMinV() : this.particleTexture.getMaxV();

        float f5 = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
        float f6 = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
        float f7 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);
        int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;
        Vec3d[] avec3d = new Vec3d[]{new Vec3d((double) (-rotationX * f4 - rotationXY * f4), (double) (-rotationZ * f4), (double) (-rotationYZ * f4 - rotationXZ * f4)), new Vec3d((double) (-rotationX * f4 + rotationXY * f4), (double) (rotationZ * f4), (double) (-rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double) (rotationX * f4 + rotationXY * f4), (double) (rotationZ * f4), (double) (rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double) (rotationX * f4 - rotationXY * f4), (double) (-rotationZ * f4), (double) (rotationYZ * f4 - rotationXZ * f4))};
        buffer.pos((double) f5 + avec3d[0].x, (double) f6 + avec3d[0].y, (double) f7 + avec3d[0].z).tex((double) maxU, (double) maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double) f5 + avec3d[1].x, (double) f6 + avec3d[1].y, (double) f7 + avec3d[1].z).tex((double) maxU, (double) minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double) f5 + avec3d[2].x, (double) f6 + avec3d[2].y, (double) f7 + avec3d[2].z).tex((double) minU, (double) minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double) f5 + avec3d[3].x, (double) f6 + avec3d[3].y, (double) f7 + avec3d[3].z).tex((double) minU, (double) maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();

    }

}
