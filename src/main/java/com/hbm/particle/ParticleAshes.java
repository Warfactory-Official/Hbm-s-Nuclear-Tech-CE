package com.hbm.particle;

import com.hbm.main.client.NTMClientRegistry;
import com.hbm.util.Vec3NT;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class ParticleAshes extends ParticleRotating {
    public ParticleAshes(World world, double x, double y, double z, float scale) {
        super(world, x, y, z);
        particleTexture = NTMClientRegistry.particle_base;
        this.particleMaxAge = 1200 + rand.nextInt(20);
        this.particleScale = scale * 0.9F + rand.nextFloat() * 0.2F;

        this.particleGravity = 0.01F;

        this.particleRed = this.particleGreen = this.particleBlue = this.rand.nextFloat() * 0.1F + 0.1F;
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

        if(!this.onGround) this.particleAngle += 2 * ((System.identityHashCode(this) % 2) - 0.5);

        this.motionX *= 0.95D;
        this.motionY *= 0.99D;
        this.motionZ *= 0.95D;

        boolean wasOnGround = this.onGround;
        this.move(this.motionX, this.motionY, this.motionZ);
        if(!wasOnGround && this.onGround) this.particleAngle = rand.nextFloat() * 360F;

        if(System.identityHashCode(this) % 5 == 0 && this.onGround && rand.nextInt(15) == 0) {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY + 0.125, posZ, 0, 0.05, 0);
        }
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

        float timeLeft = this.particleMaxAge - (this.particleAge + partialTicks);

        if (timeLeft < 40) {
            this.particleAlpha = timeLeft / 40F;
        } else {
            this.particleAlpha = 1F;
        }

        if (this.onGround) {
            float pX = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
            float pY = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
            float pZ = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);

            float minU = particleTexture.getMinU();
            float maxU = particleTexture.getMaxU();
            float minV = particleTexture.getMinV();
            float maxV = particleTexture.getMaxV();

            int i = this.getBrightnessForRender(partialTicks);
            int j = (i >> 16) & 65535;
            int k = i & 65535;
            Vec3NT vec = new Vec3NT(this.particleScale, 0, this.particleScale).rotateAroundYDeg(this.particleAngle);

            buffer.pos(pX + vec.x, pY + 0.05, pZ + vec.z).tex(maxU, maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
            vec.rotateAroundYDeg(90);
            buffer.pos(pX + vec.x, pY + 0.05, pZ + vec.z).tex(maxU, minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
            vec.rotateAroundYDeg(90);
            buffer.pos(pX + vec.z, pY + 0.05, pZ + vec.z).tex(minU, minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
            vec.rotateAroundYDeg(90);
            buffer.pos(pX + vec.x, pY + 0.05, pZ + vec.z).tex(minU, maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        } else {
            renderParticleRotated(buffer, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ, this.particleScale);
        }
    }
}
