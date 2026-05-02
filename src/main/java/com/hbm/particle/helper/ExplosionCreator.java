package com.hbm.particle.helper;

import com.hbm.interfaces.Untested;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.particle.ParticleDebris;
import com.hbm.particle.ParticleMukeWave;
import com.hbm.particle.ParticleRocketFlame;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.wiaj.WorldInAJar;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@Untested
public class ExplosionCreator implements IParticleCreator {


    public static final double speedOfSound = (17.15D) * 0.5;

    public static void composeEffect(World world, double x, double y, double z, int cloudCount, float cloudScale, float cloudSpeedMult, float waveScale,
                                     int debrisCount, int debrisSize, int debrisRetry, float debrisVelocity, float debrisHorizontalDeviation, float debrisVerticalOffset, float soundRange) {

        NBTTagCompound data = new NBTTagCompound();
        data.setByte("cloudCount", (byte) cloudCount);
        data.setFloat("cloudScale", cloudScale);
        data.setFloat("cloudSpeedMult", cloudSpeedMult);
        data.setFloat("waveScale", waveScale);
        data.setByte("debrisCount", (byte) debrisCount);
        data.setByte("debrisSize", (byte) debrisSize);
        data.setShort("debrisRetry", (byte) debrisRetry);
        data.setFloat("debrisVelocity", debrisVelocity);
        data.setFloat("debrisHorizontalDeviation", debrisHorizontalDeviation);
        data.setFloat("debrisVerticalOffset", debrisVerticalOffset);
        data.setFloat("soundRange", soundRange);
        IParticleCreator.sendPacket(world, HbmEffectNT.ExplosionLarge, x, y, z, Math.max(300, (int) soundRange), data);
    }

    /**
     * Downscaled for small bombs
     */
    public static void composeEffectSmall(World world, double x, double y, double z) {
        composeEffect(world, x, y, z, 10, 2F, 0.5F, 25F, 5, 8, 20, 0.75F, 1F, -2F, 150);
    }

    /**
     * Development version
     */
    public static void composeEffectStandard(World world, double x, double y, double z) {
        composeEffect(world, x, y, z, 15, 5F, 1F, 45F, 10, 16, 50, 1F, 3F, -2F, 200);
    }

    /**
     * Upscaled version, ATACMS go brrt
     */
    public static void composeEffectLarge(World world, double x, double y, double z) {
        composeEffect(world, x, y, z, 30, 6.5F, 2F, 65F, 25, 16, 50, 1.25F, 3F, -2F, 350);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void makeParticle(World world, EntityPlayer player, TextureManager man, Random rand, double x, double y, double z, NBTTagCompound data) {

        int cloudCount = data.getByte("cloudCount");
        float cloudScale = data.getFloat("cloudScale");
        float cloudSpeedMult = data.getFloat("cloudSpeedMult");
        float waveScale = data.getFloat("waveScale");
        int debrisCount = data.getByte("debrisCount");
        int debrisSize = data.getByte("debrisSize");
        int debrisRetry = data.getShort("debrisRetry");
        float debrisVelocity = data.getFloat("debrisVelocity");
        float debrisHorizontalDeviation = data.getFloat("debrisHorizontalDeviation");
        float debrisVerticalOffset = data.getFloat("debrisVerticalOffset");
        float soundRange = data.getFloat("soundRange");

        float dist = (float) player.getDistance(x, y, z);

        if (dist <= soundRange) {
            SoundEvent sound = dist <= soundRange * 0.4 ? HBMSoundHandler.explosionLargeNear : HBMSoundHandler.explosionLargeFar;

            PositionedSoundRecord soundEffect = new PositionedSoundRecord(
                    sound,
                    SoundCategory.PLAYERS,
                    1000F, 0.9F + rand.nextFloat() * 0.2F,
                    (float) x, (float) y, (float) z);

            Minecraft.getMinecraft().getSoundHandler().playDelayedSound(soundEffect, (int) (dist / speedOfSound));
        }

        // WAVE
        ParticleMukeWave wave = new ParticleMukeWave(world, x, y + 2, z);
        wave.setup(waveScale, (int) (25F * waveScale / 45));
        Minecraft.getMinecraft().effectRenderer.addEffect(wave);

        // SMOKE PLUME
        for (int i = 0; i < cloudCount; i++) {
            ParticleRocketFlame fx = new ParticleRocketFlame(world, x, y, z).setScale(cloudScale);
            fx.updateInterpPos();
            fx.motionX = rand.nextGaussian() * 0.5 * cloudSpeedMult;
            fx.motionY = rand.nextDouble() * 3 * cloudSpeedMult;
            fx.motionZ = rand.nextGaussian() * 0.5 * cloudSpeedMult;
            fx.setMaxAge(70 + rand.nextInt(20));
            fx.setCollision(false);
            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
        }

        // DEBRIS

        for (int c = 0; c < debrisCount; c++) {

            double oX = rand.nextGaussian() * debrisHorizontalDeviation;
            double oY = debrisVerticalOffset;
            double oZ = rand.nextGaussian() * debrisHorizontalDeviation;
            int cX = (int) Math.floor(x + oX + 0.5);
            int cY = (int) Math.floor(y + oY + 0.5);
            int cZ = (int) Math.floor(z + oZ + 0.5);

            Vec3 motion = Vec3.createVectorHelper(debrisVelocity, 0, 0);
            motion.rotateAroundZ((float) -Math.toRadians(45 + rand.nextFloat() * 25));
            motion.rotateAroundY((float) (rand.nextDouble() * Math.PI * 2));
            ParticleDebris particle = new ParticleDebris(world, x, y, z, motion.xCoord, motion.yCoord, motion.zCoord);
            WorldInAJar wiaj = new WorldInAJar(debrisSize, debrisSize, debrisSize);
            particle.worldInAJar = wiaj;

            if (debrisSize > 0) {
                int middle = debrisSize / 2 - 1;

                for (int i = 0; i < 2; i++)
                    for (int j = 0; j < 2; j++)
                        for (int k = 0; k < 2; k++)
                            wiaj.setBlock(middle + i, middle + j, middle + k, world.getBlockState(new BlockPos(cX + i, cY + j, cZ + k)));

                for (int layer = 2; layer <= (debrisSize / 2); layer++) {
                    for (int i = 0; i < debrisRetry; i++) {
                        int jx = -layer + rand.nextInt(layer * 2 + 1);
                        int jy = -layer + rand.nextInt(layer * 2 + 1);
                        int jz = -layer + rand.nextInt(layer * 2 + 1);

                        if (wiaj.getBlock(middle + jx + 1, middle + jy, middle + jz) != Blocks.AIR || wiaj.getBlock(middle + jx - 1, middle + jy, middle + jz) != Blocks.AIR ||
                                wiaj.getBlock(middle + jx, middle + jy + 1, middle + jz) != Blocks.AIR || wiaj.getBlock(middle + jx, middle + jy - 1, middle + jz) != Blocks.AIR ||
                                wiaj.getBlock(middle + jx, middle + jy, middle + jz + 1) != Blocks.AIR || wiaj.getBlock(middle + jx, middle + jy, middle + jz - 1) != Blocks.AIR) {

                            IBlockState state = world.getBlockState(new BlockPos(cX + jx, cY + jy, cZ + jz));
                            wiaj.setBlock(middle + jx, middle + jy, middle + jz, state);
                        }
                    }
                }
            }

            Minecraft.getMinecraft().effectRenderer.addEffect(particle);
        }
    }
}
