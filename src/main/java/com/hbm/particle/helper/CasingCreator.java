package com.hbm.particle.helper;

import com.hbm.particle.ParticleSpentCasing;
import com.hbm.particle.SpentCasing;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.util.Vec3NT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class CasingCreator implements IParticleCreator {

    /** Default casing without smoke */
    public static void composeEffect(World world, EntityPlayer player, double frontOffset, double heightOffset, double sideOffset, double frontMotion, double heightMotion, double sideMotion, double motionVariance, String casing) {
        composeEffect(world, player, frontOffset, heightOffset, sideOffset, frontMotion, heightMotion, sideMotion, motionVariance, 5F, 10F, casing, false, 0, 0, 0);
    }

    /** Casing without smoke */
    public static void composeEffect(World world, EntityPlayer player, double frontOffset, double heightOffset, double sideOffset, double frontMotion, double heightMotion, double sideMotion, double motionVariance, float multPitch, float multYaw, String casing) {
        composeEffect(world, player, frontOffset, heightOffset, sideOffset, frontMotion, heightMotion, sideMotion, motionVariance, multPitch, multYaw, casing, false, 0, 0, 0);
    }

    /** Default casing, but with smoke*/
    public static void composeEffect(World world, EntityPlayer player, double frontOffset, double heightOffset, double sideOffset, double frontMotion, double heightMotion, double sideMotion, double motionVariance, String casing, boolean smoking, int smokeLife, double smokeLift, int nodeLife) {
        composeEffect(world, player, frontOffset, heightOffset, sideOffset, frontMotion, heightMotion, sideMotion, motionVariance, 5F, 10F, casing, false, 0, 0, 0);
    }

    public static void composeEffect(World world, EntityPlayer player, double frontOffset, double heightOffset, double sideOffset, double frontMotion, double heightMotion, double sideMotion, double motionVariance, float mPitch, float mYaw, String casing, boolean smoking, int smokeLife, double smokeLift, int nodeLife) {

        if(player == null || world == null) return;//Fixes crashes whenever mobs pickup weapons
        if(player.isSneaking()) heightOffset -= 0.075F;

        Vec3NT offset = new Vec3NT(sideOffset, heightOffset, frontOffset);
        offset.rotateAroundXRad(player.rotationPitch / 180F * (float) Math.PI);
        offset.rotateAroundYRad(-player.rotationYaw / 180F * (float) Math.PI);

        double x = player.posX + offset.x;
        double y = player.posY + player.getEyeHeight() + offset.y;
        double z = player.posZ + offset.z;

        Vec3NT motion = new Vec3NT(sideMotion, heightMotion, frontMotion);
        motion.rotateAroundXRad(-player.rotationPitch / 180F * (float) Math.PI);
        motion.rotateAroundYRad(-player.rotationYaw / 180F * (float) Math.PI);

        double mX = player.motionX + motion.x + player.getRNG().nextGaussian() * motionVariance;
        double mY = player.motionY + motion.y + player.getRNG().nextGaussian() * motionVariance;
        double mZ = player.motionZ + motion.z + player.getRNG().nextGaussian() * motionVariance;

        NBTTagCompound data = new NBTTagCompound();
        data.setString("type", "casingNT");
        data.setDouble("mX", mX);
        data.setDouble("mY", mY);
        data.setDouble("mZ", mZ);
        data.setFloat("yaw", player.rotationYaw);
        data.setFloat("pitch", player.rotationPitch);
        data.setFloat("mPitch", mPitch);
        data.setFloat("mYaw", mYaw);
        data.setString("name", casing);
        data.setBoolean("smoking", smoking);
        data.setInteger("smokeLife", smokeLife);
        data.setDouble("smokeLift", smokeLift);
        data.setInteger("nodeLife", nodeLife);

        IParticleCreator.sendPacket(world, x, y, z, 50, data);
    }
    public static void composeEffect(World world, Vec3d vec, float yaw, float pitch, double frontMotion, double heightMotion, double sideMotion, double motionVariance, float mPitch, float mYaw, String casing, boolean smoking, int smokeLife, double smokeLift, int nodeLife) {

        Vec3d motion = new Vec3d(sideMotion, heightMotion, frontMotion);
        motion.rotatePitch(-pitch / 180F * (float) Math.PI);
        motion.rotateYaw(-yaw / 180F * (float) Math.PI);

        double mX = motion.x+ world.rand.nextGaussian() * motionVariance;
        double mY = motion.y+ world.rand.nextGaussian() * motionVariance;
        double mZ = motion.z+ world.rand.nextGaussian() * motionVariance;

        NBTTagCompound data = new NBTTagCompound();
        data.setString("type", "casingNT");
        data.setDouble("mX", mX);
        data.setDouble("mY", mY);
        data.setDouble("mZ", mZ);
        data.setFloat("yaw", yaw);
        data.setFloat("pitch", pitch);
        data.setFloat("mPitch", mPitch);
        data.setFloat("mYaw", mYaw);
        data.setString("name", casing);
        data.setBoolean("smoking", smoking);
        data.setInteger("smokeLife", smokeLife);
        data.setDouble("smokeLift", smokeLift);
        data.setInteger("nodeLife", nodeLife);

        IParticleCreator.sendPacket(world, vec.x, vec.y, vec.z, 50, data);
    }
    @Override
    @SideOnly(Side.CLIENT)
    public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

        String name = data.getString("name");
        SpentCasing casingConfig = SpentCasing.casingMap.get(name);
        double mX = data.getDouble("mX");
        double mY = data.getDouble("mY");
        double mZ = data.getDouble("mZ");
        float yaw = data.getFloat("yaw");
        float pitch = data.getFloat("pitch");
        float mPitch = data.getFloat("mPitch");
        float mYaw = data.getFloat("mYaw");
        boolean smoking = data.getBoolean("smoking");
        int smokeLife = data.getInteger("smokeLife");
        double smokeLift = data.getDouble("smokeLift");
        int nodeLife = data.getInteger("nodeLife");
        ParticleSpentCasing casing = new ParticleSpentCasing(world, x, y, z, mX, mY, mZ, mPitch, mYaw, casingConfig, smoking, smokeLife, smokeLift, nodeLife);
        casing.prevRotationYaw = casing.rotationYaw = yaw;
        casing.prevRotationPitch = casing.rotationPitch = pitch;
        Minecraft.getMinecraft().effectRenderer.addEffect(casing);
    }
}
