package com.hbm.particle.helper;

import com.hbm.lib.HBMSoundHandler;
import com.hbm.particle.ParticleExplosionSmall;
import com.hbm.particle.ParticleLargeBlockDebris;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class ExplosionSmallCreator implements IParticleCreator {

    public static final double speedOfSound = (17.15D) * 0.5;

    public static void composeEffect(World world, double x, double y, double z, int cloudCount, float cloudScale, float cloudSpeedMult) {

        NBTTagCompound data = new NBTTagCompound();
        data.setString("type", "explosionSmall");
        data.setInteger("cloudCount", cloudCount);
        data.setFloat("cloudScale", cloudScale);
        data.setFloat("cloudSpeedMult", cloudSpeedMult);
        data.setInteger("debris", 15);
        IParticleCreator.sendPacket(world, x, y, z, 200, data);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {
        int cloudCount = data.getInteger("cloudCount");
        float cloudScale = data.getFloat("cloudScale");
        float cloudSpeedMult = data.getFloat("cloudSpeedMult");
        int debris = data.getInteger("debris");

        // Sound
        float dist = (float) player.getDistance(x, y, z);
        if (dist < 200) {
            SoundEvent sound = dist < 80 ? HBMSoundHandler.explosionSmallNear : HBMSoundHandler.explosionSmallFar;
            Minecraft.getMinecraft().getSoundHandler().playDelayedSound(
                    new PositionedSoundRecord(sound, SoundCategory.PLAYERS, 100F, 1.0F, (float) x, (float) y, (float) z),
                    (int) (dist / speedOfSound)
            );
        }

        // Particle cloud
        for (int i = 0; i < cloudCount; i++) {
            Particle p = new ParticleExplosionSmall(world, x, y, z, cloudScale, cloudSpeedMult);
            Minecraft.getMinecraft().effectRenderer.addEffect(p);
        }

        // Debris particles
        BlockPos found = null;
        IBlockState state = null;
        for (EnumFacing dir : EnumFacing.VALUES) {
            BlockPos pos = new BlockPos((int) Math.floor(x) + dir.getXOffset(), (int) Math.floor(y) + dir.getYOffset(), (int) Math.floor(z) + dir.getZOffset());
            IBlockState candidate = world.getBlockState(pos);
            if (candidate.getMaterial() != Material.AIR) {
                found = pos;
                state = candidate;
                break;
            }
        }

        if (state != null) {
            for (int i = 0; i < debris; i++) {
                Particle fx = new ParticleLargeBlockDebris(
                        world,
                        x, y + 0.1, z,
                        world.rand.nextGaussian() * 0.2,
                        0.5F + world.rand.nextDouble() * 0.7,
                        world.rand.nextGaussian() * 0.2,
                        state,
                        50 + rand.nextInt(20)
                ).init();
                fx.multipleParticleScaleBy(2);
                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
            }
        }
    }
}

