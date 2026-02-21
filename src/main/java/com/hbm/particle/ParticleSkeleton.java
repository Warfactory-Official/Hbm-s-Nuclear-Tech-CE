package com.hbm.particle;

import com.hbm.Tags;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.SkeletonCreator;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ParticleSkeleton extends Particle {
    public static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/particle/skeleton.png");
    public static final ResourceLocation texture_ext = new ResourceLocation(Tags.MODID + ":textures/particle/skoilet.png");
    public static final IModelCustom skeleton = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/effect/skeleton.obj")).asVBO();
    protected SkeletonCreator.EnumSkeletonType type;

    private float momentumYaw;
    private float momentumPitch;
    private int initialDelay;

    private final TextureManager textureManager;

    public ParticleSkeleton(TextureManager textureManager, World world, double x, double y, double z, float r, float g, float b, SkeletonCreator.EnumSkeletonType type) {
        super(world, x, y, z);
        this.textureManager = textureManager;
        this.type = type;

        this.particleMaxAge = 1200 + rand.nextInt(20);

        this.particleRed = r;
        this.particleGreen = g;
        this.particleBlue = b;
        this.particleGravity = 0.02F;
        this.initialDelay = 20;

        this.momentumPitch = rand.nextFloat() * 5 * (rand.nextBoolean() ? 1 : -1);
        this.momentumYaw = rand.nextFloat() * 5 * (rand.nextBoolean() ? 1 : -1);
    }

    public void setPrevPAngle(float angle) {
        this.prevParticleAngle = angle;
    }

    public void setPAngle(float angle) {
        this.particleAngle = angle;
    }

    @Override
    public void onUpdate() {

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.prevParticleAngle = this.particleAngle;

        if (initialDelay-- > 0) return;

        if (initialDelay == -1) {
            this.motionX = rand.nextGaussian() * 0.025;
            this.motionZ = rand.nextGaussian() * 0.025;
        }

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }
        boolean wasOnGround = this.onGround;

        this.motionY -= this.particleGravity;
        this.move(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.98D;
        this.motionY *= 0.98D;
        this.motionZ *= 0.98D;

        if (!this.onGround) {
            this.particleAngle += this.momentumYaw;
        } else {
            this.motionX = 0;
            this.motionY = 0;
            this.motionZ = 0;

            if (!wasOnGround) {
                MainRegistry.proxy.playSoundClient(posX, posY, posZ, SoundEvents.ENTITY_SKELETON_HURT, SoundCategory.PLAYERS, 0.25F, 0.8F + rand.nextFloat() * 0.4F);
            }
        }
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float interp, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

        GlStateManager.pushMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderHelper.enableStandardItemLighting();

        double pX = prevPosX + (posX - prevPosX) * interp;
        double pY = prevPosY + (posY - prevPosY) * interp;
        double pZ = prevPosZ + (posZ - prevPosZ) * interp;

        double dX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) interp;
        double dY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) interp;
        double dZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) interp;

        GlStateManager.translate(pX - dX, pY - dY, pZ - dZ);

        GlStateManager.rotate(this.prevParticleAngle + (this.particleAngle - this.prevParticleAngle) * interp, 1.0F, 1.0F, 0.0F);

        float timeLeft = this.particleMaxAge - (this.particleAge + interp);
        if (timeLeft < 40) {
            this.particleAlpha = timeLeft / 40F;
        } else {
            this.particleAlpha = 1F;
        }

        int brightness = this.world.getCombinedLight(new BlockPos(pX, pY, pZ), 0);
        int lX = brightness & 65535;
        int lY = (brightness >> 16) & 65535;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lX, (float) lY);

        GlStateManager.color(particleRed, particleGreen, particleBlue, particleAlpha);
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);

        switch (type) {
            case SKULL:
                this.textureManager.bindTexture(texture);
                skeleton.renderPart("Skull");
                break;
            case TORSO:
                this.textureManager.bindTexture(texture);
                skeleton.renderPart("Torso");
                break;
            case LIMB:
                this.textureManager.bindTexture(texture);
                skeleton.renderPart("Limb");
                break;
            case SKULL_VILLAGER:
                this.textureManager.bindTexture(texture_ext);
                skeleton.renderPart("SkullVillager");
                break;
        }

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.disableCull();

        GlStateManager.popMatrix();
    }
}
