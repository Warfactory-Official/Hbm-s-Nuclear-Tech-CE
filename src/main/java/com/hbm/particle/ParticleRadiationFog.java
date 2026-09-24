package com.hbm.particle;

import com.hbm.Tags;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class ParticleRadiationFog extends Particle {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID, "textures/particle/fog.png");
    private static final int MIN_MAX_AGE = 400;
    private static final int QUAD_COUNT = 25;
    private static final double[] OFF_X = new double[QUAD_COUNT];
    private static final double[] OFF_Y = new double[QUAD_COUNT];
    private static final double[] OFF_Z = new double[QUAD_COUNT];
    private static final double[] JIT_X = new double[QUAD_COUNT];
    private static final double[] JIT_Y = new double[QUAD_COUNT];
    private static final double[] JIT_Z = new double[QUAD_COUNT];
    private static final double[] SIZE_MUL = new double[QUAD_COUNT];
    private static final float[] ALPHA_LUT = new float[MIN_MAX_AGE + 1];
    private static final float COLOR_RED = 0.85F;
    private static final float COLOR_GREEN = 0.9F;
    private static final float COLOR_BLUE = 0.5F;
    private static final int PACKED_FULLBRIGHT_LIGHTMAP = NTMBufferBuilder.packLightmap(240, 240);

    static {
        Random random = new Random(50L);
        double offX = 0D;
        double offY = 0D;
        double offZ = 0D;
        for (int i = 0; i < QUAD_COUNT; i++) {
            offX += (random.nextGaussian() - 1D) * 2.5D;
            offY += (random.nextGaussian() - 1D) * 0.15D;
            offZ += (random.nextGaussian() - 1D) * 2.5D;
            OFF_X[i] = offX;
            OFF_Y[i] = offY;
            OFF_Z[i] = offZ;
            SIZE_MUL[i] = random.nextDouble();
            JIT_X[i] = random.nextGaussian() * 0.5D;
            JIT_Y[i] = random.nextGaussian() * 0.5D;
            JIT_Z[i] = random.nextGaussian() * 0.5D;
        }

        for (int age = 0; age <= MIN_MAX_AGE; age++) {
            ALPHA_LUT[age] = (float) (Math.sin(age * Math.PI / (double) MIN_MAX_AGE) * 0.125D);
        }
    }

    public ParticleRadiationFog(World worldIn, double posXIn, double posYIn, double posZIn) {
        super(worldIn, posXIn, posYIn, posZIn);
        this.particleMaxAge = 100 + this.rand.nextInt(40);
        this.particleRed = this.particleGreen = this.particleBlue = 0F;
        this.particleScale = 7.5F;
    }

    public ParticleRadiationFog(World worldIn, double posXIn, double posYIn, double posZIn, float red, float green, float blue, float scale) {
        this(worldIn, posXIn, posYIn, posZIn);
        this.particleRed = red;
        this.particleGreen = green;
        this.particleBlue = blue;
        this.particleScale = scale;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleMaxAge < MIN_MAX_AGE) {
            this.particleMaxAge = MIN_MAX_AGE;
        }

        if (++this.particleAge >= this.particleMaxAge) {
            this.setExpired();
            return;
        }

        this.motionX *= 0.9599999785423279D;
        this.motionY *= 0.9599999785423279D;
        this.motionZ *= 0.9599999785423279D;

        if (this.onGround) {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public void renderParticle(BufferBuilder unusedBuffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ,
                               float rotationXY, float rotationXZ) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);

        int age = this.particleAge;
        if (age < 0) {
            age = 0;
        } else if (age > MIN_MAX_AGE) {
            age = MIN_MAX_AGE;
        }
        float alpha = ALPHA_LUT[age];
        this.particleAlpha = alpha;
        double baseX = this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX;
        double baseY = this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY;
        double baseZ = this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);

        int packedColor = NTMBufferBuilder.packColor(COLOR_RED, COLOR_GREEN, COLOR_BLUE, alpha);
        NTMBufferBuilder buffer = NTMImmediate.INSTANCE.beginParticlePositionTexColorLmap(GL11.GL_QUADS, QUAD_COUNT * 4);
        for (int i = 0; i < QUAD_COUNT; i++) {
            float size = (float) (SIZE_MUL[i] * this.particleScale);
            float pX = (float) (baseX + OFF_X[i] + JIT_X[i]);
            float pY = (float) (baseY + OFF_Y[i] + JIT_Y[i]);
            float pZ = (float) (baseZ + OFF_Z[i] + JIT_Z[i]);

            buffer.appendParticlePositionTexColorLmapUnchecked(pX - rotationX * size - rotationXY * size, pY - rotationZ * size, pZ - rotationYZ * size - rotationXZ * size, 1.0F, 1.0F, packedColor, PACKED_FULLBRIGHT_LIGHTMAP);
            buffer.appendParticlePositionTexColorLmapUnchecked(pX - rotationX * size + rotationXY * size, pY + rotationZ * size, pZ - rotationYZ * size + rotationXZ * size, 1.0F, 0.0F, packedColor, PACKED_FULLBRIGHT_LIGHTMAP);
            buffer.appendParticlePositionTexColorLmapUnchecked(pX + rotationX * size + rotationXY * size, pY + rotationZ * size, pZ + rotationYZ * size + rotationXZ * size, 0.0F, 0.0F, packedColor, PACKED_FULLBRIGHT_LIGHTMAP);
            buffer.appendParticlePositionTexColorLmapUnchecked(pX + rotationX * size - rotationXY * size, pY - rotationZ * size, pZ + rotationYZ * size - rotationXZ * size, 0.0F, 1.0F, packedColor, PACKED_FULLBRIGHT_LIGHTMAP);
        }
        NTMImmediate.INSTANCE.draw();

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
    }

    @Override
    public int getBrightnessForRender(float partialTicks) {
        return 240 | 240 << 16;
    }
}
