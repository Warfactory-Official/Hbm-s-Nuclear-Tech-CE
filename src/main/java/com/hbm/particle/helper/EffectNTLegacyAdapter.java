package com.hbm.particle.helper;

import com.hbm.main.MainRegistry;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.hbm.particle.helper.HbmEffectNT.*;

public class EffectNTLegacyAdapter {
    private static final Map<String, Function<NBTTagCompound, HbmEffectNT>> CONVERSION_MAP = new HashMap<>();

    public static void runLegacyEffect(NBTTagCompound nbt) {
        String type = nbt.getString("type");
        double x = nbt.getDouble("posX");
        double y = nbt.getDouble("posY");
        double z = nbt.getDouble("posZ");
        MainRegistry.proxy.effectNT(CONVERSION_MAP.get(type).apply(nbt), x, y, z, nbt);
    }

    static {
        simpleAdapter("casingNT", CasingNT);
        simpleAdapter("flamethrower", Flamethrower);
        simpleAdapter("explosionSmall", ExplosionSmall);
        simpleAdapter("explosionLarge", ExplosionLarge);
        simpleAdapter("blackPowder", BlackPowder);
        simpleAdapter("ashes", Ashes);
        simpleAdapter("skeleton", Skeleton);

        simpleAdapter("waterSplash", WaterSplash);
        simpleAdapter("cloudFX2", CloudFX2);
        simpleAdapter("AMBContrail", ABMContrail);

        simpleAdapter("launchSmoke", LaunchSmoke);
        simpleAdapter("exKerosene", ExKerosene);
        simpleAdapter("exSolid", ExSolid);
        simpleAdapter("exHydrogen", ExHydrogen);
        simpleAdapter("exBalefire", ExBalefire);
        simpleAdapter("radFog", RadFog);
        simpleAdapter("radiationfog", RadFog);

        simpleAdapter("missileContrail", MissileContrail);
        modeAdapter("smoke", Map.of(
                "cloud", Smoke_Cloud,
                "radial", Smoke_Radial,
                "radialDigamma", Smoke_RadialDigamma,
                "shock", Smoke_Shock,
                "shockRand", Smoke_ShockRand,
                "wave", Smoke_Wave,
                "foamSplash", Smoke_FoamSplash
        ));
        simpleAdapter("debugdrone", DebugDrone);
        simpleAdapter("network", Network);
        modeAdapter("exhaust", Map.of(
                "soyuz", Exhaust_Soyuz,
                "meteor", Exhaust_Meteor
        ));
        simpleAdapter("muke", Muke);
        simpleAdapter("ufo", UFO);
        simpleAdapter("haze", Haze);
        simpleAdapter("plasmablast", PlasmaBlast);
        simpleAdapter("justTilt", JustTilt);
        simpleAdapter("properJolt", ProperJolt);
        simpleAdapter("gasfire", GasFlame);
        simpleAdapter("marker", Marker);
        simpleAdapter("casing", CasingOld);
        simpleAdapter("foundry", Foundry);
        simpleAdapter("fireworks", Fireworks);
        simpleAdapter("vomit", Vomit);
        simpleAdapter("sweat", Sweat);
        simpleAdapter("splash", Splash);
        simpleAdapter("fluidfill", FluidFill);
        simpleAdapter("radiation", RadiationFlash);
        modeAdapter("vanillaburst", Map.of(
                "flame", VanillaBurst_Flame,
                "cloud", VanillaBurst_Cloud,
                "reddust", VanillaBurst_RedDust,
                "greendust", VanillaBurst_GreenDust,
                "bluedust", VanillaBurst_BlueDust,
                "blockdust", VanillaBurst_BlockDust
        ));
        Map<String, HbmEffectNT> vanillaExtMap = new HashMap<>(12);
        vanillaExtMap.put("flame", VanillaExt_Flame);
        vanillaExtMap.put("cloud", VanillaExt_Cloud);
        vanillaExtMap.put("smoke", VanillaExt_Smoke);
        vanillaExtMap.put("volcano", VanillaExt_Volcano);
        vanillaExtMap.put("reddust", VanillaExt_RedDust);
        vanillaExtMap.put("bluedust", VanillaExt_BlueDust);
        vanillaExtMap.put("greendust", VanillaExt_GreenDust);
        vanillaExtMap.put("fireworks", VanillaExt_Fireworks);
        vanillaExtMap.put("largeexplode", VanillaExt_LargeExplode);
        vanillaExtMap.put("townaura", VanillaExt_TownAura);
        vanillaExtMap.put("blockdust", VanillaExt_BlockDust);
        vanillaExtMap.put("colordust", VanillaExt_ColorDust);
        modeAdapter("vanillaExt", vanillaExtMap);
        simpleAdapter("spark", Spark);
        simpleAdapter("hadron", Hadron);
        simpleAdapter("schrabfog", SchrabFog);
        simpleAdapter("rift", Rift);
        simpleAdapter("rbmkflame", RBMKFlame);
        simpleAdapter("rbmksteam", RBMKSteam);
        simpleAdapter("rbmkmush", RBMKMush);
        simpleAdapter("tower", Tower);
        simpleAdapter("jetpack", Jetpack);
        simpleAdapter("bnuuy", bnuuy);
        simpleAdapter("jetpack_bj", Jetpack_BJ);
        simpleAdapter("jetpack_dns", Jetpack_DNS);
        simpleAdapter("bf", BF);
        simpleAdapter("chlorinefx", FX_Chlorine);
        simpleAdapter("cloudfx", FX_Cloud);
        simpleAdapter("orangefx", FX_Orange);
        simpleAdapter("pinkcloudfx", FX_PinkCloud);
        simpleAdapter("bimpact", BulletImpact);
        simpleAdapter("vanilla", Vanilla);
        simpleAdapter("anim", Anim);
        simpleAdapter("tau", Tau);
        simpleAdapter("giblets", Giblets);
    }

    private static void simpleAdapter(String key, HbmEffectNT value) {
        CONVERSION_MAP.put(key, (_) -> value);
    }

    private static void modeAdapter(String key, Map<String, HbmEffectNT> modeMap) {
        CONVERSION_MAP.put(key, (data) -> modeMap.get(data.getString("mode")));
    }
}
