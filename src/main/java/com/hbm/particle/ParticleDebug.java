package com.hbm.particle;

import com.hbm.main.client.NTMClientRegistry;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleDebug extends Particle {
    private static final TextureAtlasSprite SPRITE_POWER = NTMClientRegistry.debugPower;
    private static final TextureAtlasSprite SPRITE_FLUID = NTMClientRegistry.debugFluid;

    private final byte type;

    public ParticleDebug(World worldIn, double posXIn, double posYIn, double posZIn) {
        super(worldIn, posXIn, posYIn, posZIn);
        this.particleMaxAge = 10;
        this.type = 0;
        this.canCollide = false;
    }

    public ParticleDebug(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int color) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        this.type = 1;
        this.particleRed = ((color & 0xff0000) >> 16) / 255F;
        this.particleGreen = ((color & 0x00ff00) >> 8) / 255F;
        this.particleBlue = (color & 0x0000ff) / 255F;
    }


    public int getFXLayer() {
        return 1;
    }


    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        this.particleTexture = type == 0 ? SPRITE_POWER : SPRITE_FLUID;
        super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);

    }


}
