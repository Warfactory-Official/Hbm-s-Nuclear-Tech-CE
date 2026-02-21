package com.hbm.particle;

import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Tuple;
import com.hbm.util.Vec3NT;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@SideOnly(Side.CLIENT)
public class ParticleSpentCasing extends Particle {

    public static final Random rand = new Random();
    public static float dScale = 0.05F, smokeJitter = 0.001F;
    private final List<Tuple.Pair<Vec3NT, Double>> smokeNodes = new ArrayList();
    private final SpentCasing config;
    public boolean isSmoking;
    public float momentumPitch, momentumYaw;
    public float rotationPitch, rotationYaw;
    public float prevRotationPitch, prevRotationYaw;
    private int maxSmokeGen = 120;
    private double smokeLift = 0.5D;
    private int nodeLife = 30;
    /**
     * Used for frame-perfect translation of smoke
     */
    private boolean setupDeltas = false;
    private double prevRenderX;
    private double prevRenderY;
    private double prevRenderZ;

    public ParticleSpentCasing(World world, double x, double y, double z, double mx, double my, double mz, float momentumPitch, float momentumYaw, SpentCasing config, boolean smoking, int smokeLife, double smokeLift, int nodeLife) {
        super(world, x, y, z, 0, 0, 0);
        this.momentumPitch = momentumPitch;
        this.momentumYaw = momentumYaw;
        this.config = config;

        this.particleMaxAge = config.getMaxAge();
        this.setSize(2 * dScale * Math.max(config.getScaleX(), config.getScaleZ()), dScale * config.getScaleY());

        this.isSmoking = smoking;
        this.maxSmokeGen = smokeLife;
        this.smokeLift = smokeLift;
        this.nodeLife = nodeLife;

        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;

        this.motionX = mx;
        this.motionY = my;
        this.motionZ = mz;
        setPosition(x, y, z); //Added by bob, apparently causes some issues

        particleGravity = 1F;
    }

    private boolean isInWater() {
        return this.world.getBlockState(new BlockPos(this.posX, this.posY, this.posZ)).getMaterial() == Material.WATER;
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }

        this.motionY -= 0.04D * (double) this.particleGravity;
        this.move(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.98D;
        this.motionY *= 0.98D;
        this.motionZ *= 0.98D;

        if (this.onGround) {
            this.motionX *= 0.7D;
            this.motionZ *= 0.7D;

            this.rotationPitch = (float) (Math.floor(this.rotationPitch / 180F + 0.5F)) * 180F;
            this.momentumYaw *= 0.7F;
            this.onGround = false;
        }

        if (particleAge > maxSmokeGen && !smokeNodes.isEmpty())
            smokeNodes.clear();

        if (isSmoking && particleAge <= maxSmokeGen) {

            for (Tuple.Pair<Vec3NT, Double> pair : smokeNodes) {
                Vec3NT node = pair.getKey();

                node.add(rand.nextGaussian() * smokeJitter,
                        rand.nextGaussian() * smokeJitter,
                        smokeLift * dScale);

                pair.value = Math.max(0, pair.value - (1D / (double) nodeLife));
            }

            if (particleAge < maxSmokeGen || this.isInWater()) {
                smokeNodes.add(new Tuple.Pair<>(Vec3NT.ZERO(), smokeNodes.isEmpty() ? 0.0D : 1D));
            }
        }

        prevRotationPitch = rotationPitch;
        prevRotationYaw = rotationYaw;

        rotationPitch += momentumPitch;
        rotationYaw += momentumYaw;

        if (Math.abs(prevRotationPitch - rotationPitch) > 180) {
            if (prevRotationPitch < rotationPitch) prevRotationPitch += 360;
            if (prevRotationPitch > rotationPitch) prevRotationPitch -= 360;
        }

        if (Math.abs(prevRotationYaw - rotationYaw) > 180) {
            if (prevRotationYaw < rotationYaw) prevRotationYaw += 360;
            if (prevRotationYaw > rotationYaw) prevRotationYaw -= 360;
        }
    }

    public void move(double x, double y, double z) {
        double origX = x;
        double origY = y;
        double origZ = z;


        List<AxisAlignedBB> list = this.world.getCollisionBoxes(null, this.getBoundingBox().expand(x, y, z));

        for (AxisAlignedBB axisalignedbb : list) {
            y = axisalignedbb.calculateYOffset(this.getBoundingBox(), y);
        }

        this.setBoundingBox(this.getBoundingBox().offset(0.0D, y, 0.0D));

        for (AxisAlignedBB axisalignedbb1 : list) {
            x = axisalignedbb1.calculateXOffset(this.getBoundingBox(), x);
        }

        this.setBoundingBox(this.getBoundingBox().offset(x, 0.0D, 0.0D));

        for (AxisAlignedBB axisalignedbb2 : list) {
            z = axisalignedbb2.calculateZOffset(this.getBoundingBox(), z);
        }

        this.setBoundingBox(this.getBoundingBox().offset(0.0D, 0.0D, z));


        this.resetPositionToBB();
        this.onGround = origY != y && origY < 0.0D;

        if (origX != x) {
            this.motionX *= -0.25D;
            if (Math.abs(momentumYaw) > 1e-7)
                momentumYaw *= -0.75F;
            else
                momentumYaw = (float) rand.nextGaussian() * 10F * this.config.getBounceYaw();
        }

        if (origY != y) {
            this.motionY *= -0.5D;

            boolean rotFromSpeed = Math.abs(this.motionY) > 0.04;
            if (rotFromSpeed || Math.abs(momentumPitch) > 1e-7) {
                momentumPitch *= -0.75F;
                if (rotFromSpeed) {
                    float mult = (float) BobMathUtil.safeClamp(origY / 0.2F, -1F, 1F);
                    momentumPitch += (float) (rand.nextGaussian() * 10F * this.config.getBouncePitch() * mult);
                    momentumYaw += (float) rand.nextGaussian() * 10F * this.config.getBounceYaw() * mult;
                }
            }
        }
        if (origZ != z) {
            this.motionZ *= -0.25D;

            if (Math.abs(momentumYaw) > 1e-7)
                momentumYaw *= -0.75F;
            else
                momentumYaw = (float) rand.nextGaussian() * 10F * this.config.getBounceYaw();
        }
        if (this.config.getSound() != null && onGround && Math.abs(origY) >= 0.2) {
            MainRegistry.proxy.playSoundClient(posX, posY, posZ, this.config.getSound(), SoundCategory.PLAYERS, SpentCasing.PLINK_LARGE.equals(this.config.getSound()) ? 1F : 0.5F, 1F + rand.nextFloat() * 0.2F);
        }

    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float interp, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableRescaleNormal();
        GlStateManager.depthMask(true);

        double pX = prevPosX + (posX - prevPosX) * interp;
        double pY = prevPosY + (posY - prevPosY) * interp;
        double pZ = prevPosZ + (posZ - prevPosZ) * interp;

        if (!setupDeltas) {
            prevRenderX = pX;
            prevRenderY = pY;
            prevRenderZ = pZ;
            setupDeltas = true;
        }

        BlockPos blockpos = new BlockPos(pX, pY, pZ);
        int brightness = world.getCombinedLight(blockpos, 0);
        int lX = brightness % 65536;
        int lY = brightness / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lX, (float) lY);

        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.casings_tex);

        EntityPlayer player = Minecraft.getMinecraft().player;
        double dX = player.prevPosX + (player.posX - player.prevPosX) * (double) interp;
        double dY = player.prevPosY + (player.posY - player.prevPosY) * (double) interp;
        double dZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) interp;

        GlStateManager.translate(pX - dX, pY - dY - this.height / 4 + config.getScaleY() * 0.01, pZ - dZ);
        GlStateManager.scale(dScale, dScale, dScale);

        GlStateManager.rotate(180 - (float) BobMathUtil.interp(prevRotationYaw, rotationYaw, interp), 0, 1, 0);
        GlStateManager.rotate((float) -BobMathUtil.interp(prevRotationPitch, rotationPitch, interp), 1, 0, 0);

        GlStateManager.scale(config.getScaleX(), config.getScaleY(), config.getScaleZ());

        int index = 0;
        for (String name : config.getType().partNames) {
            int col = this.config.getColors()[index];
            Color color = new Color(col);
            GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1.0F);
            ResourceManager.casings.renderPart(name);
            index++;
        }

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();

        //Smoke
        GlStateManager.pushMatrix();
        GlStateManager.translate(pX - dX, pY - dY - this.height / 4, pZ - dZ);

        if (!smokeNodes.isEmpty()) {
            bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

            float scale = config.getScaleX() * 0.5F * dScale;
            Vec3NT vec = new Vec3NT(scale, 0, 0);
            float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * interp;
            vec.rotateAroundYDeg(-yaw);

            double deltaX = prevRenderX - pX;
            double deltaY = prevRenderY - pY;
            double deltaZ = prevRenderZ - pZ;

            for (Tuple.Pair<Vec3NT, Double> pair : smokeNodes) {
                Vec3NT pos = pair.getKey();
                pos.add(deltaX, deltaY, deltaZ);
            }

            for (int i = 0; i < smokeNodes.size() - 1; i++) {
                final Tuple.Pair<Vec3NT, Double> node = smokeNodes.get(i), past = smokeNodes.get(i + 1);
                final Vec3NT nL = node.getKey(), pL = past.getKey();
                float nA = node.getValue().floatValue();
                float pA = past.getValue().floatValue();

                float timeAlpha = 1F - (float) particleAge / (float) maxSmokeGen;
                nA *= timeAlpha;
                pA *= timeAlpha;

                bufferbuilder.pos(nL.x, nL.y, nL.z).color(1F, 1F, 1F, nA).endVertex();
                bufferbuilder.pos(nL.x + vec.x, nL.y, nL.z + vec.z).color(1F, 1F, 1F, 0F).endVertex();
                bufferbuilder.pos(pL.x + vec.x, pL.y, pL.z + vec.z).color(1F, 1F, 1F, 0F).endVertex();
                bufferbuilder.pos(pL.x, pL.y, pL.z).color(1F, 1F, 1F, pA).endVertex();

                bufferbuilder.pos(nL.x, nL.y, nL.z).color(1F, 1F, 1F, nA).endVertex();
                bufferbuilder.pos(nL.x - vec.x, nL.y, nL.z - vec.z).color(1F, 1F, 1F, 0F).endVertex();
                bufferbuilder.pos(pL.x - vec.x, pL.y, pL.z - vec.z).color(1F, 1F, 1F, 0F).endVertex();
                bufferbuilder.pos(pL.x, pL.y, pL.z).color(1F, 1F, 1F, pA).endVertex();
            }

            GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.disableCull();

            tessellator.draw();

            GlStateManager.enableCull();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.1F);
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();

        RenderHelper.disableStandardItemLighting();

        prevRenderX = pX;
        prevRenderY = pY;
        prevRenderZ = pZ;
    }

    @Override
    public int getBrightnessForRender(float partialTicks) {
        int x = MathHelper.floor(this.posX);
        int z = MathHelper.floor(this.posZ);

        if (this.world.isBlockLoaded(new BlockPos(x, 0, z))) {
            double d0 = (this.getBoundingBox().maxY - this.getBoundingBox().minY) * 0.66D;
            int y = MathHelper.floor(this.posY + d0);
            return this.world.getCombinedLight(new BlockPos(x, y, z), 0);
        } else {
            return 0;
        }
    }

    public double getPosX() {
        return this.posX;
    }

    public double getPosY() {
        return this.posY;
    }

    public double getPosZ() {
        return this.posZ;
    }
}

