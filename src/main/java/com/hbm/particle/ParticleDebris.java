package com.hbm.particle;

import com.hbm.render.util.NTMImmediate;
import com.hbm.wiaj.WorldInAJar;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class ParticleDebris extends Particle {

    public static Random rng = new Random();
    public WorldInAJar worldInAJar;
    private final BlockRendererDispatcher renderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
    private double prevRotationPitch;
    private double prevRotationYaw;
    private double rotationPitch;
    private double rotationYaw;

    public ParticleDebris(World world, double x, double y, double z, double mX, double mY, double mZ) {
        super(world, x, y, z);
        double mult = 3;
        this.motionX = mX * mult;
        this.motionY = mY * mult;
        this.motionZ = mZ * mult;
        this.particleMaxAge = 100;
        this.particleGravity = 0.15F;
        this.canCollide = false;
    }

    public ParticleDebris(World world, double x, double y, double z) {
        super(world, x, y, z);
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

        if (this.particleAge > 5) this.canCollide = true;

        rng.setSeed(this.hashCode());
        this.prevRotationPitch = this.rotationPitch;
        this.prevRotationYaw = this.rotationYaw;
        this.rotationPitch += rng.nextFloat() * 10;
        this.rotationYaw += rng.nextFloat() * 10;

        if (this.hashCode() % 3 == 0) {
            ParticleRocketFlame fx = new ParticleRocketFlame(world, posX, posY, posZ).setScale(1F * Math.max(worldInAJar.sizeY, 6) / 16F);
            fx.updateInterpPos();
            fx.setMaxAge(50);
            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
        }

        this.motionY -= this.particleGravity;

        this.move(this.motionX, this.motionY, this.motionZ);

        this.particleAge++;
        BlockPos pos = new BlockPos(this.posX, this.posY, this.posZ);
        IBlockState state = worldInAJar.getBlockState(pos);

        if (this.onGround || state.getBlock() == Blocks.WEB) this.setExpired();
    }

    @Override
    public void renderParticle(BufferBuilder _buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        if (worldInAJar == null) return;

        EntityPlayer player = Minecraft.getMinecraft().player;
        double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
        double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
        double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;

        float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - dX);
        float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - dY);
        float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - dZ);

        BufferBuilder buffer = NTMImmediate.INSTANCE.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        worldInAJar.lightlevel = world.getCombinedLight(new BlockPos((int) Math.floor(posX), (int) Math.floor(posY), (int) Math.floor(posZ)), 0);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();

        GlStateManager.translate(pX, pY, pZ);

        GlStateManager.rotate((float) (prevRotationPitch + (rotationPitch - prevRotationPitch) * partialTicks), 0, 1, 0);
        GlStateManager.rotate((float) (prevRotationYaw + (rotationYaw - prevRotationYaw) * partialTicks), 0, 0, 1);

        GlStateManager.translate(-worldInAJar.sizeX / 2.0, -worldInAJar.sizeY / 2.0, -worldInAJar.sizeZ / 2.0);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        for (int x = 0; x < worldInAJar.sizeX; x++) {
            for (int y = 0; y < worldInAJar.sizeY; y++) {
                for (int z = 0; z < worldInAJar.sizeZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    IBlockState state = worldInAJar.getBlockState(pos);

                    try {
                        renderer.renderBlock(
                                state, pos, worldInAJar, buffer
                        );

                    } catch (Exception ignored) {

                    }
                }
            }
        }

        NTMImmediate.INSTANCE.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

}
