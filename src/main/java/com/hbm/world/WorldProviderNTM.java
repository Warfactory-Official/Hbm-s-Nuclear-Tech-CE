package com.hbm.world;

import com.hbm.main.MainRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProviderSurface;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class WorldProviderNTM extends WorldProviderSurface {

    public static final DimensionType IMPACT_TYPE =
            DimensionType.register("hbm_ntm_overworld", "", 1337, WorldProviderNTM.class, true);

    private final float[] colorsSunriseSunset = new float[4];

    @Override
    @Nullable
    @SideOnly(Side.CLIENT)
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
        float horizonBand = 0.4F;
        float cosine = MathHelper.cos(celestialAngle * (float) Math.PI * 2.0F);
        float dust = MainRegistry.proxy.getImpactDust(world);

        if (cosine >= -horizonBand && cosine <= horizonBand) {
            float blend = cosine / horizonBand * 0.5F + 0.5F;
            float alpha = 1.0F - (1.0F - MathHelper.sin(blend * (float) Math.PI)) * 0.99F;
            alpha *= alpha;
            this.colorsSunriseSunset[0] = (blend * 0.3F + 0.7F) * (1.0F - dust);
            this.colorsSunriseSunset[1] = (blend * blend * 0.7F + 0.2F) * (1.0F - dust);
            this.colorsSunriseSunset[2] = 0.2F * (1.0F - dust);
            this.colorsSunriseSunset[3] = alpha * (1.0F - dust);
            return this.colorsSunriseSunset;
        }

        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getStarBrightness(float partialTicks) {
        return super.getStarBrightness(partialTicks) * (1.0F - MainRegistry.proxy.getImpactDust(world));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getSunBrightness(float partialTicks) {
        float dust = MainRegistry.proxy.getImpactDust(world);
        return super.getSunBrightness(partialTicks) * (1.0F - dust);
    }

    @Override
    public boolean isDaytime() {
        if (MainRegistry.proxy.getImpactDust(world) >= 0.75F) {
            return false;
        }
        return super.isDaytime();
    }

    @Override
    public float getSunBrightnessFactor(float partialTicks) {
        float dust = MainRegistry.proxy.getImpactDust(world);
        return super.getSunBrightnessFactor(partialTicks) * (1.0F - dust);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getFogColor(float celestialAngle, float partialTicks) {
        Vec3d fog = super.getFogColor(celestialAngle, partialTicks);
        float dust = MainRegistry.proxy.getImpactDust(world);
        float fire = MainRegistry.proxy.getImpactFire(world);

        float red = (float) fog.x;
        float green = (float) fog.y * (1.0F - dust * 0.5F);
        float blue = (float) fog.z * (1.0F - dust);
        float scale = fire > 0.0F ? Math.max(1.0F - dust * 2.0F, 0.0F) : (1.0F - dust);

        return new Vec3d(red * scale, green * scale, blue * scale);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getSkyColor(Entity cameraEntity, float partialTicks) {
        //noinspection ConstantValue
        if (cameraEntity == null) {
            return Vec3d.ZERO;
        }
        Vec3d sky = super.getSkyColor(cameraEntity, partialTicks);
        float dust = MainRegistry.proxy.getImpactDust(world);
        float fire = MainRegistry.proxy.getImpactFire(world);

        float red;
        float green;
        float blue;

        if (fire > 0.0F) {
            red = (float) sky.x * 1.3F;
            green = (float) sky.y * Math.max(1.0F - dust * 1.4F, 0.0F);
            blue = (float) sky.z * Math.max(1.0F - dust * 4.0F, 0.0F);
        } else {
            red = (float) sky.x;
            green = (float) sky.y * (1.0F - dust * 0.5F);
            blue = (float) sky.z * (1.0F - dust);
        }

        float scale = fire + (1.0F - dust);
        return new Vec3d(red * scale, green * scale, blue * scale);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getCloudColor(float partialTicks) {
        Vec3d clouds = super.getCloudColor(partialTicks);
        float dust = MainRegistry.proxy.getImpactDust(world);
        float scale = 1.0F - dust;
        return new Vec3d(clouds.x * scale, clouds.y * scale, clouds.z * scale);
    }
}
