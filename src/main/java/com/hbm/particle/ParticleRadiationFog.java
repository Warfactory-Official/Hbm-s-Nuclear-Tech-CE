package com.hbm.particle;

import com.hbm.main.client.NTMClientRegistry;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class ParticleRadiationFog extends Particle {

    private static final int MAX_AGE = 400;
    private static final int MAX_QUADS = 25;
    private static final double[] OFF_X = new double[MAX_QUADS];
    private static final double[] OFF_Y = new double[MAX_QUADS];
    private static final double[] OFF_Z = new double[MAX_QUADS];
    private static final double[] JIT_X = new double[MAX_QUADS];
    private static final double[] JIT_Y = new double[MAX_QUADS];
    private static final double[] JIT_Z = new double[MAX_QUADS];
    private static final double[] SIZE_MUL = new double[MAX_QUADS];
    private static final float[] ALPHA_LUT = new float[MAX_AGE + 1];

    static {
        Random r = new Random(50L);
        for (int i = 0; i < MAX_QUADS; i++) {
            OFF_X[i] = (r.nextGaussian() - 1D) * 2.5D;
            OFF_Y[i] = (r.nextGaussian() - 1D) * 0.15D;
            OFF_Z[i] = (r.nextGaussian() - 1D) * 2.5D;
            SIZE_MUL[i] = (0.75D + r.nextDouble() * 0.5D);
            JIT_X[i] = r.nextGaussian() * 0.5D;
            JIT_Y[i] = r.nextGaussian() * 0.5D;
            JIT_Z[i] = r.nextGaussian() * 0.5D;
        }

        for (int age = 0; age <= MAX_AGE; age++) {
            float a = (float) (Math.sin(age * Math.PI / (double) MAX_AGE) * 0.25D);
            ALPHA_LUT[age] = MathHelper.clamp(a, 0f, 1f);
        }
    }

    private final double spawnY;
    private final int maxAge;
    private int rng;

    public ParticleRadiationFog(World worldIn, double posXIn, double posYIn, double posZIn) {
        super(worldIn, posXIn, posYIn, posZIn);
        this.maxAge = MAX_AGE;
        this.particleRed = 0.8F;
        this.particleGreen = 0.8F;
        this.particleBlue = 0.8F;
        this.particleScale = 7.5F;
        this.particleTexture = NTMClientRegistry.fog;
        this.spawnY = posYIn;
        int s = System.identityHashCode(this) ^ (int) Double.doubleToLongBits(posXIn * 31.0 + posZIn) ^ (int) Double.doubleToLongBits(posYIn);
        if (s == 0) s = 0x9E3779B9;
        this.rng = s;
        s = xorshift32(s);
        double nx = ((s & 0x3FF) - 512) * (1.0 / 512.0);
        double ny = (((s >>> 10) & 0x3FF) - 512) * (1.0 / 512.0);
        double nz = (((s >>> 20) & 0x3FF) - 512) * (1.0 / 512.0);
        this.motionX = nx * 0.01D;
        this.motionY = ny * 0.003D;
        this.motionZ = nz * 0.01D;
    }

    private static int chooseQuadCount(double distSq) {
        if (distSq > (144.0 * 144.0)) return 0;
        if (distSq > (96.0 * 96.0)) return 4;
        if (distSq > (64.0 * 64.0)) return 8;
        if (distSq > (32.0 * 32.0)) return 16;
        return MAX_QUADS;
    }

    private static int xorshift32(int x) {
        x ^= (x << 13);
        x ^= (x >>> 17);
        x ^= (x << 5);
        return x;
    }

    private static double clampAbs(double v, double maxAbs) {
        if (v > maxAbs) return maxAbs;
        return Math.max(v, -maxAbs);
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (++this.particleAge >= this.maxAge) {
            this.setExpired();
            return;
        }

        int s = (this.rng = xorshift32(this.rng));
        double nx = ((s & 0x3FF) - 512) * (1.0 / 512.0);
        double ny = (((s >>> 10) & 0x3FF) - 512) * (1.0 / 512.0);
        double nz = (((s >>> 20) & 0x3FF) - 512) * (1.0 / 512.0);

        this.motionX += nx * 0.0008D;
        this.motionZ += nz * 0.0008D;
        this.motionY += ny * 0.0002D;
        this.motionY += (this.spawnY - this.posY) * 0.0020D;

        this.motionX = clampAbs(this.motionX, 0.025D);
        this.motionZ = clampAbs(this.motionZ, 0.025D);
        this.motionY = clampAbs(this.motionY, 0.015D);
        this.move(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.96D;
        this.motionY *= 0.96D;
        this.motionZ *= 0.96D;
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public boolean shouldDisableDepth() {
        return true;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ,
                               float rotationXY, float rotationXZ) {
        int age = this.particleAge;
        if (age < 0) age = 0;
        if (age > MAX_AGE) age = MAX_AGE;
        this.particleAlpha = ALPHA_LUT[age];
        double dx = entityIn.posX - this.posX;
        double dy = entityIn.posY - this.posY;
        double dz = entityIn.posZ - this.posZ;
        int quads = chooseQuadCount(dx * dx + dy * dy + dz * dz);
        if (quads <= 0) return;
        final int j = this.getBrightnessForRender(partialTicks);
        final int k = (j >> 16) & 0xFFFF;
        final int l = j & 0xFFFF;
        final float u0 = this.particleTexture.getMinU();
        final float u1 = this.particleTexture.getMaxU();
        final float v0 = this.particleTexture.getMinV();
        final float v1 = this.particleTexture.getMaxV();
        final double baseX = (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks) - interpPosX;
        final double baseY = (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks) - interpPosY;
        final double baseZ = (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks) - interpPosZ;
        final double scale = this.particleScale;
        for (int i = 0; i < quads; i++) {
            final double size = SIZE_MUL[i] * scale;
            final double px = baseX + JIT_X[i];
            final double py = baseY + JIT_Y[i];
            final double pz = baseZ + JIT_Z[i];
            final double ox = OFF_X[i];
            final double oy = OFF_Y[i];
            final double oz = OFF_Z[i];

            final double rx = (double) rotationX * size;
            final double rxy = (double) rotationXY * size;
            final double ry = (double) rotationZ * size;
            final double ryz = (double) rotationYZ * size;
            final double rxz = (double) rotationXZ * size;

            final double x1 = px - rx - rxy + ox;
            final double x2 = px - rx + rxy + ox;
            final double x3 = px + rx + rxy + ox;
            final double x4 = px + rx - rxy + ox;

            final double y1 = py - ry + oy;
            final double y2 = py + ry + oy;

            final double z1 = pz - ryz - rxz + oz;
            final double z2 = pz - ryz + rxz + oz;
            final double z3 = pz + ryz + rxz + oz;
            final double z4 = pz + ryz - rxz + oz;

            buffer.pos(x1, y1, z1).tex(u1, v1).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(k, l).endVertex();
            buffer.pos(x2, y2, z2).tex(u1, v0).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(k, l).endVertex();
            buffer.pos(x3, y2, z3).tex(u0, v0).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(k, l).endVertex();
            buffer.pos(x4, y1, z4).tex(u0, v1).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(k, l).endVertex();
        }
    }

    @Override
    public int getBrightnessForRender(float partialTicks) {
        return 70;
    }
}
