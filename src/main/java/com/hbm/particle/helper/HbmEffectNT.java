package com.hbm.particle.helper;

import com.hbm.animloader.AnimationWrapper;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.particle.*;
import com.hbm.handler.CasingEjector;
import com.hbm.items.IAnimatedItem;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.ClientProxy;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.particle.*;
import com.hbm.particle.bullet_hit.ParticleBloodParticle;
import com.hbm.particle.bullet_hit.ParticleBulletImpact;
import com.hbm.particle.bullet_hit.ParticleHitDebris;
import com.hbm.particle.bullet_hit.ParticleSmokeAnim;
import com.hbm.particle_instanced.*;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.render.util.RenderOverhead;
import com.hbm.util.BobMathUtil;
import com.hbm.util.MutableVec3d;
import com.hbm.util.Vec3NT;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedHardenedClay;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.Random;

public enum HbmEffectNT {
    // Particle creators
    CasingNT, Flamethrower, ExplosionSmall, ExplosionLarge, BlackPowder, Ashes, Skeleton,
    // MK1 port
    WaterSplash, CloudFX2, ABMContrail, 
    // MK2 port
    LaunchSmoke, ExKeroseneOld, ExKerosene, ExSolid, ExHydrogen, ExBalefire, RadFog,
    // MK3 stuff
    MissileContrail, Smoke_Cloud, Smoke_Radial, Smoke_RadialDigamma, Smoke_Shock, Smoke_ShockRand, Smoke_Wave, Smoke_FoamSplash,
    DebugDrone, Network, Exhaust_Soyuz, Exhaust_Meteor, Muke, TinyTot, UFO, Haze, PlasmaBlast, JustTilt, ProperJolt, GasFlame,
    Marker, CasingOld, Foundry, Fireworks, Vomit, Sweat, Splash, FluidFill, RadiationFlash, AmatExplosion, VanillaBurst_Flame,
    VanillaBurst_Cloud, VanillaBurst_RedDust, VanillaBurst_BlueDust, VanillaBurst_GreenDust, VanillaBurst_BlockDust, VanillaExt_Flame,
    VanillaExt_Cloud, VanillaExt_RedDust, VanillaExt_BlueDust, VanillaExt_GreenDust, VanillaExt_BlockDust, VanillaExt_Smoke,
    VanillaExt_Volcano, VanillaExt_Fireworks, VanillaExt_LargeExplode, VanillaExt_TownAura, VanillaExt_ColorDust, Spark, Hadron,
    SchrabFog, Rift, RBMKFlame, RBMKMush, RBMKSteam, Tower, Jetpack, bnuuy, Jetpack_BJ, Jetpack_DNS, BF, FX_Chlorine, FX_Cloud,
    FX_PinkCloud, FX_Orange, BulletImpact, Vanilla, Anim, Tau, Giblets
    ;
    
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public static void registerClientHandlers() {
        CasingNT.setHandler((world, x, y, z, data) -> ParticleCreators.CASING
                .makeParticle(world, Minecraft.getMinecraft().player, Minecraft.getMinecraft().renderEngine, world.rand, x, y, z, data));
        Flamethrower.setHandler((world, x, y, z, data) -> ParticleCreators.FLAME
                .makeParticle(world, Minecraft.getMinecraft().player, Minecraft.getMinecraft().renderEngine, world.rand, x, y, z, data));
        ExplosionSmall.setHandler((world, x, y, z, data) -> ParticleCreators.EXPLOSION_SMALL
                .makeParticle(world, Minecraft.getMinecraft().player, Minecraft.getMinecraft().renderEngine, world.rand, x, y, z, data));
        ExplosionLarge.setHandler((world, x, y, z, data) -> ParticleCreators.EXPLOSION_LARGE
                .makeParticle(world, Minecraft.getMinecraft().player, Minecraft.getMinecraft().renderEngine, world.rand, x, y, z, data));
        BlackPowder.setHandler((world, x, y, z, data) -> ParticleCreators.BLACK_POWDER
                .makeParticle(world, Minecraft.getMinecraft().player, Minecraft.getMinecraft().renderEngine, world.rand, x, y, z, data));
        Ashes.setHandler((world, x, y, z, data) -> ParticleCreators.ASHES
                .makeParticle(world, Minecraft.getMinecraft().player, Minecraft.getMinecraft().renderEngine, world.rand, x, y, z, data));
        Skeleton.setHandler((world, x, y, z, data) -> ParticleCreators.SKELETON
                .makeParticle(world, Minecraft.getMinecraft().player, Minecraft.getMinecraft().renderEngine, world.rand, x, y, z, data));

        WaterSplash.setHandler((world, x, y, z, _) -> {
            for (int i = 0; i < 10; i++) {
                EntityCloudFX smoke = new EntityCloudFX(world,
                        x + world.rand.nextGaussian(), y + world.rand.nextGaussian(), z + world.rand.nextGaussian(),
                        0.0, 0.0, 0.0);
                Minecraft.getMinecraft().effectRenderer.addEffect(smoke);
            }
        });
        CloudFX2.setHandler((world, x, y, z, _) -> {
            EntityCloudFX smoke = new EntityCloudFX(world, x, y, z, 0.0, 0.1, 0.0);
            Minecraft.getMinecraft().effectRenderer.addEffect(smoke);
        });
        ABMContrail.setHandler((world, x, y, z, _) -> {
            ParticleContrail contrail = new ParticleContrail(Minecraft.getMinecraft().renderEngine, world, x, y, z);
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
        });

        // MK2 port
        LaunchSmoke.setHandler((world, x, y, z, data) -> {
            ParticleSmokePlume contrail = new ParticleSmokePlume(Minecraft.getMinecraft().renderEngine, world, x, y, z);
            contrail.motionX = data.getDouble("moX");
            contrail.motionY = data.getDouble("moY");
            contrail.motionZ = data.getDouble("moZ");
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
        });
        ExKeroseneOld.setHandler((world, x, y, z, _) -> {
            ParticleContrail contrail = new ParticleContrailKerosene(Minecraft.getMinecraft().renderEngine, world, x, y, z);
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
        });
        ExKerosene.setHandler((world, x, y, z, _) -> {
            ParticleContrail contrail = new ParticleContrail(Minecraft.getMinecraft().renderEngine, world, x, y, z, 0F, 0F, 0F, 1F);
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
        });
        ExSolid.setHandler((world, x, y, z, _) -> {
            ParticleContrail contrail = new ParticleContrail(Minecraft.getMinecraft().renderEngine, world, x, y, z, 0.3F, 0.2F, 0.05F, 1F);
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
        });
        ExHydrogen.setHandler((world, x, y, z, _) -> {
            ParticleContrail contrail = new ParticleContrail(Minecraft.getMinecraft().renderEngine, world, x, y, z, 0.7F, 0.7F, 0.7F, 1F);
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
        });
        ExBalefire.setHandler((world, x, y, z, _) -> {
            ParticleContrail contrail = new ParticleContrail(Minecraft.getMinecraft().renderEngine, world, x, y, z, 0.2F, 0.7F, 0.2F, 1F);
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
        });
        RadFog.setHandler((world, x, y, z, _) -> {
            if (GeneralConfig.instancedParticles) {
                InstancedParticleRenderer.addParticle(new ParticleRadiationFogInstanced(world, x, y, z));
            } else {
                ParticleRadiationFog contrail = new ParticleRadiationFog(world, x, y, z);
                Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            }
        });

        // MK3 stuff
        MissileContrail.setHandler((world, x, y, z, data) -> {
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (new Vec3d(player.posX - x, player.posY - y, player.posZ - z).length() > 350) return;

            float scale = data.hasKey("scale") ? data.getFloat("scale") : 1F;
            double mX = data.getDouble("moX");
            double mY = data.getDouble("moY");
            double mZ = data.getDouble("moZ");

            ParticleRocketFlame fx = new ParticleRocketFlame(world, x, y, z).setScale(scale);
            fx.setMotion(mX, mY, mZ);
            if (data.hasKey("maxAge")) fx.setMaxAge(data.getInteger("maxAge"));
            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
        });
        Smoke_Cloud.setHandler((world, x, y, z, data) -> {
            Random rand = world.rand;
            int count = Math.max(1, data.getInteger("count"));
            for (int i = 0; i < count; i++) {
                if (GeneralConfig.instancedParticles) {
                    ParticleExSmokeInstanced fx = new ParticleExSmokeInstanced(world, x, y, z);
                    double motionY = rand.nextGaussian() * (1 + (count / 100D));
                    double motionX = rand.nextGaussian() * (1 + (count / 150D));
                    double motionZ = rand.nextGaussian() * (1 + (count / 150D));
                    if (rand.nextBoolean()) motionY = Math.abs(motionY);
                    fx.setMotion(motionX, motionY, motionZ);
                    InstancedParticleRenderer.addParticle(fx);
                } else {
                    ParticleExSmoke fx = new ParticleExSmoke(world, x, y, z);
                    double motionY = rand.nextGaussian() * (1 + (count / 100D));
                    double motionX = rand.nextGaussian() * (1 + (count / 150D));
                    double motionZ = rand.nextGaussian() * (1 + (count / 150D));
                    if (rand.nextBoolean()) motionY = Math.abs(motionY);
                    fx.setMotion(motionX, motionY, motionZ);
                    Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                }
            }
        });
        Smoke_Radial.setHandler((world, x, y, z, data) -> {
            Random rand = world.rand;
            int count = Math.max(1, data.getInteger("count"));
            for (int i = 0; i < count; i++) {
                if (GeneralConfig.instancedParticles) {
                    ParticleExSmokeInstanced fx = new ParticleExSmokeInstanced(world, x, y, z);
                    fx.setMotion(rand.nextGaussian() * (1 + (count / 50D)),
                            rand.nextGaussian() * (1 + (count / 50D)), rand.nextGaussian() * (1 + (count / 50D)));
                    InstancedParticleRenderer.addParticle(fx);
                } else {
                    ParticleExSmoke fx = new ParticleExSmoke(world, x, y, z);
                    fx.setMotion(rand.nextGaussian() * (1 + (count / 50D)),
                            rand.nextGaussian() * (1 + (count / 50D)), rand.nextGaussian() * (1 + (count / 50D)));
                    Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                }
            }
        });
        Smoke_RadialDigamma.setHandler((world, x, y, z, data) -> {
            Random rand = world.rand;
            int count = Math.max(1, data.getInteger("count"));
            MutableVec3d vec = new MutableVec3d(2, 0, 0);
            vec.rotateYawSelf(rand.nextFloat() * (float) Math.PI * 2F);

            for (int i = 0; i < count; i++) {
                ParticleDigammaSmoke fx = new ParticleDigammaSmoke(world, x, y, z);
                fx.motion((float) vec.x, 0, (float) vec.z);
                Minecraft.getMinecraft().effectRenderer.addEffect(fx);

                vec.rotateYawSelf((float) Math.PI * 2F / (float) count);
            }
        });
        Smoke_Shock.setHandler((world, x, y, z, data) -> {
            Random rand = world.rand;
            int count = Math.max(1, data.getInteger("count"));
            double strength = data.getDouble("strength");

            MutableVec3d vec = new MutableVec3d(strength, 0, 0);
            vec.rotateYawSelf(rand.nextInt(360));

            for (int i = 0; i < count; i++) {
                if (GeneralConfig.instancedParticles) {
                    ParticleExSmokeInstanced fx = new ParticleExSmokeInstanced(world, x, y, z);
                    fx.setMotion(vec.x, 0, vec.z);
                    InstancedParticleRenderer.addParticle(fx);
                } else {
                    ParticleExSmoke fx = new ParticleExSmoke(world, x, y, z);
                    fx.setMotion(vec.x, 0, vec.z);
                    Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                }

                vec.rotateYawSelf((float) Math.PI * 2F / count);
            }
        });
        Smoke_ShockRand.setHandler((world, x, y, z, data) -> {
            Random rand = world.rand;
            int count = Math.max(1, data.getInteger("count"));
            double strength = data.getDouble("strength");

            MutableVec3d vec = new MutableVec3d(strength, 0, 0);
            vec.rotateYawSelf(rand.nextInt(360));
            double r;

            for (int i = 0; i < count; i++) {
                r = rand.nextDouble();
                if (GeneralConfig.instancedParticles) {
                    ParticleExSmokeInstanced fx = new ParticleExSmokeInstanced(world, x, y, z);
                    fx.setMotion(vec.x * r, 0, vec.z * r);
                    InstancedParticleRenderer.addParticle(fx);
                } else {
                    ParticleExSmoke fx = new ParticleExSmoke(world, x, y, z);
                    fx.setMotion(vec.x * r, 0, vec.z * r);
                    Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                }

                vec.rotateYawSelf(360F / count);
            }
        });
        Smoke_Wave.setHandler((world, x, y, z, data) -> {
            Random rand = world.rand;
            int count = Math.max(1, data.getInteger("count"));
            double strength = data.getDouble("range");

            MutableVec3d vec = new MutableVec3d(strength, 0, 0);

            for (int i = 0; i < count; i++) {

                vec.rotateYawSelf((float) Math.toRadians(rand.nextFloat() * 360F));

                if (GeneralConfig.instancedParticles) {
                    ParticleExSmokeInstanced fx = new ParticleExSmokeInstanced(world, x + vec.x, y,
                            z + vec.z);
                    fx.setMotion(0, 0, 0);
                    fx.setMaxAge(50);
                    InstancedParticleRenderer.addParticle(fx);
                } else {
                    ParticleExSmoke fx = new ParticleExSmoke(world, x + vec.x, y, z + vec.z);
                    fx.setMotion(0, 0, 0);
                    fx.setMaxAge(50);
                    Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                }

                vec.rotateYawSelf(360F / count);
            }
        });
        Smoke_FoamSplash.setHandler((world, _, _, _, data) -> {
            Random rand = world.rand;
            int count = Math.max(1, data.getInteger("count"));
            double strength = data.getDouble("range");

            MutableVec3d vec = new MutableVec3d(strength, 0, 0);

            for (int i = 0; i < count; i++) {

                vec.rotateYawSelf((float) Math.toRadians(rand.nextFloat() * 360F));
                // TODO
    /*ParticleFoam fx = new ParticleFoam(man, world, x + vec.xCoord, y, z + vec.zCoord);
    fx.maxAge = 50;
    fx.motionY = 0;
    fx.motionX = 0;
    fx.motionZ = 0;
    Minecraft.getMinecraft().effectRenderer.addEffect(fx);

    vec.rotateYawSelf(360 / count);*/
            }
        });
        DebugDrone.setHandler((world, x, y, z, data) -> {
            EntityPlayer player = Minecraft.getMinecraft().player;
            Item held = player.getHeldItem(EnumHand.MAIN_HAND).isEmpty() ? null : player.getHeldItem(EnumHand.MAIN_HAND).getItem();

            if (held == ModItems.drone ||
                    held == Item.getItemFromBlock(ModBlocks.drone_crate_provider) ||
                    held == Item.getItemFromBlock(ModBlocks.drone_crate_requester) ||
                    held == Item.getItemFromBlock(ModBlocks.drone_dock) ||
                    held == Item.getItemFromBlock(ModBlocks.drone_waypoint_request) ||
                    held == Item.getItemFromBlock(ModBlocks.drone_waypoint) ||
                    held == Item.getItemFromBlock(ModBlocks.drone_crate) ||
                    held == ModItems.drone_linker) {
                double mX = data.getDouble("mX");
                double mY = data.getDouble("mY");
                double mZ = data.getDouble("mZ");
                int color = data.getInteger("color");
                ParticleDebugLine text = new ParticleDebugLine(world, x, y, z, mX, mY, mZ, color);
                Minecraft.getMinecraft().effectRenderer.addEffect(text);
            }
        });
        Network.setHandler((world, x, y, z, data) -> {
            double mX = data.getDouble("mX");
            double mY = data.getDouble("mY");
            double mZ = data.getDouble("mZ");

            ParticleDebug debug = switch (data.getString("mode")) {
                case "power" -> new ParticleDebug(world, x, y, z);
                case "fluid" -> {
                    int color = data.getInteger("color");
                    yield new ParticleDebug(world, x, y, z, mX, mY, mZ, color);
                }
                default -> throw new IllegalStateException("Unexpected value: " + data.getString("mode"));
            };
            Minecraft.getMinecraft().effectRenderer.addEffect(debug);
        });
        Exhaust_Soyuz.setHandler((world, x, y, z, data) -> {
            Random rand = world.rand;
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (new Vec3d(player.posX - x, player.posY - y, player.posZ - z).length() > 350) return;

            int count = Math.max(1, data.getInteger("count"));
            double width = data.getDouble("width");

            for (int i = 0; i < count; i++) {
                if (GeneralConfig.instancedParticles) {
                    ParticleRocketFlameInstanced fx = new ParticleRocketFlameInstanced(world,
                            x + rand.nextGaussian() * width, y, z + rand.nextGaussian() * width);
                    fx.setMotionY(-0.75 + rand.nextDouble() * 0.5);
                    InstancedParticleRenderer.addParticle(fx);
                } else {
                    ParticleRocketFlame fx = new ParticleRocketFlame(world,
                            x + rand.nextGaussian() * width, y, z + rand.nextGaussian() * width);
                    fx.setMotionY(-0.75 + rand.nextDouble() * 0.5);
                    Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                }
            }
        });
        Exhaust_Meteor.setHandler((world, x, y, z, data) -> {
            Random rand = world.rand;
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (new Vec3d(player.posX - x, player.posY - y, player.posZ - z).length() > 350) return;

            int count = Math.max(1, data.getInteger("count"));
            double width = data.getDouble("width");

            for (int i = 0; i < count; i++) {
                if (GeneralConfig.instancedParticles) {
                    ParticleRocketFlameInstanced fx = new ParticleRocketFlameInstanced(world,
                            x + rand.nextGaussian() * width, y + rand.nextGaussian() * width,
                            z + rand.nextGaussian() * width);
                    InstancedParticleRenderer.addParticle(fx);
                } else {
                    ParticleRocketFlame fx = new ParticleRocketFlame(world,
                            x + rand.nextGaussian() * width, y + rand.nextGaussian() * width,
                            z + rand.nextGaussian() * width);
                    Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                }
            }
        });
        Muke.setHandler((world, x, y, z, data) -> {
            EntityPlayer player = Minecraft.getMinecraft().player;
            ParticleMukeWave wave = new ParticleMukeWave(world, x, y, z);
            ParticleMukeFlash flash = new ParticleMukeFlash(world, x, y, z, data.getBoolean("balefire"));

            Minecraft.getMinecraft().effectRenderer.addEffect(wave);
            Minecraft.getMinecraft().effectRenderer.addEffect(flash);

            player.hurtTime = 15;
            player.maxHurtTime = 15;
            player.attackedAtYaw = 0F;
        });
        TinyTot.setHandler((world, x, y, z, _) -> {
            EntityPlayer player = Minecraft.getMinecraft().player;
            Random rand = world.rand;
            ParticleMukeWave wave = new ParticleMukeWave(world, x, y, z);
            Minecraft.getMinecraft().effectRenderer.addEffect(wave);

            for(double d = 0.0D; d <= 1.6D; d += 0.1) {
                ParticleMukeCloud cloud = new ParticleMukeCloud(world, x, y, z, rand.nextGaussian() * 0.05, d + rand.nextGaussian() * 0.02, rand.nextGaussian() * 0.05);
                Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
            }
            for(int i = 0; i < 50; i++) {
                ParticleMukeCloud cloud = new ParticleMukeCloud(world, x, y + 0.5, z, rand.nextGaussian() * 0.5, rand.nextInt(5) == 0 ? 0.02 : 0, rand.nextGaussian() * 0.5);
                Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
            }
            for(int i = 0; i < 15; i++) {
                double ix = rand.nextGaussian() * 0.2;
                double iz = rand.nextGaussian() * 0.2;

                if(ix * ix + iz * iz > 0.75) {
                    ix *= 0.5;
                    iz *= 0.5;
                }

                double iy = 1.6 + (rand.nextDouble() * 2 - 1) * (0.75 - (ix * ix + iz * iz)) * 0.5;

                ParticleMukeCloud cloud = new ParticleMukeCloud(world, x, y, z, ix, iy + rand.nextGaussian() * 0.02, iz);
                Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
            }
            player.hurtTime = 15;
            player.maxHurtTime = 15;
            player.attackedAtYaw = 0F;
        });
        UFO.setHandler((world, x, y, z, _) -> {
            if (GeneralConfig.instancedParticles) {
                ParticleRocketFlameInstanced fx = new ParticleRocketFlameInstanced(world, x, y, z);
                InstancedParticleRenderer.addParticle(fx);
            } else {
                ParticleRocketFlame fx = new ParticleRocketFlame(world, x, y, z);
                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
            }
        });
        Haze.setHandler((world, x, y, z, _) -> {
            ParticleHaze fog = new ParticleHaze(world, x, y, z);
            Minecraft.getMinecraft().effectRenderer.addEffect(fog);
        });
        PlasmaBlast.setHandler((world, x, y, z, data) -> {
            ParticlePlasmaBlast cloud = new ParticlePlasmaBlast(world, x, y, z, data.getFloat("r"),
                    data.getFloat("g"), data.getFloat("b"), data.getFloat("pitch"), data.getFloat("yaw"));
            cloud.setScale(data.getFloat("scale"));
            Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
        });
        JustTilt.setHandler((_, _, _, _, data) -> {
            EntityPlayer player = Minecraft.getMinecraft().player;
            player.hurtTime = player.maxHurtTime = data.getInteger("time");
            player.attackedAtYaw = 0F;
        });
        ProperJolt.setHandler((_, _, _, _, data) -> {
            EntityPlayer player = Minecraft.getMinecraft().player;
            player.hurtTime = data.getInteger("time");
            player.maxHurtTime = data.getInteger("maxTime");
            player.attackedAtYaw = 0F;
        });
        GasFlame.setHandler((world, x, y, z, data) -> {
            double mX = data.getDouble("mX");
            double mY = data.getDouble("mY");
            double mZ = data.getDouble("mZ");
            float scale = data.getFloat("scale");
            ParticleGasFlame text = new ParticleGasFlame(world, x, y, z, mX, mY, mZ, scale > 0 ? scale : 6.5F);
            Minecraft.getMinecraft().effectRenderer.addEffect(text);
        });
        Marker.setHandler((_, x, y, z, data) -> {
            int color = data.getInteger("color");
            String label = data.getString("label");
            int expires = data.getInteger("expires");
            double dist = data.getDouble("dist");

            RenderOverhead.queuedMarkers.put(new BlockPos(x, y, z),
                    new RenderOverhead.Marker(color).setDist(dist).setExpire(expires > 0 ?
                            System.currentTimeMillis() + expires : 0).withLabel(label.isEmpty() ? null : label));
        });
        CasingOld.setHandler((world, x, y, z, data) -> {
            CasingEjector ejector = CasingEjector.fromId(data.getInteger("ej"));
            if (ejector == null) return;
            SpentCasing casingConfig = SpentCasing.fromName((data.getString("name")));
            if (casingConfig == null) return;

            for (int i = 0; i < ejector.getAmount(); i++) {
                ejector.spawnCasing(casingConfig, world, x, y, z,
                        data.getFloat("pitch"), data.getFloat("yaw"), data.getBoolean("crouched"));
            }
        });
        Foundry.setHandler((world, x, y, z, data) -> {
            int color = data.getInteger("color");
            byte dir = data.getByte("dir");
            float length = data.getFloat("len");
            float base = data.getFloat("base");
            float offset = data.getFloat("off");

            ParticleFoundry sploosh = new ParticleFoundry(world, x, y, z, color, dir, length, base, offset);
            Minecraft.getMinecraft().effectRenderer.addEffect(sploosh);
        });
        Fireworks.setHandler((world, x, y, z, data) -> {
            int color = data.getInteger("color");
            char c = (char) data.getInteger("char");

            ParticleLetter fx = new ParticleLetter(world, x, y, z, color, c);
            Minecraft.getMinecraft().effectRenderer.addEffect(fx);

            for (int i = 0; i < 50; i++) {
                ParticleFirework.Spark blast = new ParticleFirework.Spark(world, x, y, z,
                        0.4 * world.rand.nextGaussian(),
                        0.4 * world.rand.nextGaussian(),
                        0.4 * world.rand.nextGaussian(), Minecraft.getMinecraft().effectRenderer);
                blast.setColor(color);
                Minecraft.getMinecraft().effectRenderer.addEffect(blast);
            }
        });
        Vomit.setHandler((world, _, _, _, data) -> {
            Random rand = world.rand;
            Entity e = world.getEntityByID(data.getInteger("entity"));
            int count = data.getInteger("count");
            if (e instanceof EntityLivingBase) {

                double ix = e.posX;
                double iy = e.posY - e.getYOffset() + e.getEyeHeight() + (e instanceof EntityPlayer ? -0.5 : 0);
                double iz = e.posZ;

                Vec3d vec = e.getLookVec();

                String mode = data.getString("mode");
                for (int i = 0; i < count; i++) {
                    switch (mode) {
                        case "normal" -> {
                            int stateId =
                                    Block.getStateId(Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR,
                                            rand.nextBoolean() ? EnumDyeColor.LIME : EnumDyeColor.GREEN));
                            Particle fx = new ParticleBlockDust.Factory().createParticle(-1, world, ix, iy, iz,
                                    (vec.x + rand.nextGaussian() * 0.2) * 0.2, (vec.y + rand.nextGaussian() * 0.2) * 0.2
                                    , (vec.z + rand.nextGaussian() * 0.2) * 0.2, stateId);
                            if (fx == null) return;
                            fx.setMaxAge(150 + rand.nextInt(50));
                            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                        }
                        case "blood" -> {
                            Particle fx = new ParticleBlockDust.Factory().createParticle(-1, world, ix, iy, iz,
                                    (vec.x + rand.nextGaussian() * 0.2) * 0.2, (vec.y + rand.nextGaussian() * 0.2) * 0.2
                                    , (vec.z + rand.nextGaussian() * 0.2) * 0.2,
                                    Block.getStateId(Blocks.REDSTONE_BLOCK.getDefaultState()));
                            if (fx == null) return;
                            fx.setMaxAge(150 + rand.nextInt(50));
                            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                        }
                        case "smoke" -> {
                            Particle fx = new ParticleSmokeNormal.Factory().createParticle(-1, world, ix, iy, iz,
                                    (vec.x + rand.nextGaussian() * 0.1) * 0.05,
                                    (vec.y + rand.nextGaussian() * 0.1) * 0.05,
                                    (vec.z + rand.nextGaussian() * 0.1) * 0.05);
                            fx.setMaxAge(10 + rand.nextInt(10));
                            fx.particleScale *= 0.2F;
                            ((ParticleSmokeNormal) fx).smokeParticleScale = fx.particleScale;
                            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + mode);
                    }
                }
            }
        });
        Sweat.setHandler((world, _, _, _, data) -> {
            Entity e = world.getEntityByID(data.getInteger("entity"));
            Block b = Block.getBlockById(data.getInteger("block"));
            int meta = data.getInteger("meta");

            if (e instanceof EntityLivingBase) {

                for (int i = 0; i < data.getInteger("count"); i++) {

                    double ix =
                            e.getEntityBoundingBox().minX - 0.2 + (e.getEntityBoundingBox().maxX - e.getEntityBoundingBox().minX + 0.4) * world.rand.nextDouble();
                    double iy =
                            e.getEntityBoundingBox().minY + (e.getEntityBoundingBox().maxY - e.getEntityBoundingBox().minY + 0.2) * world.rand.nextDouble();
                    double iz =
                            e.getEntityBoundingBox().minZ - 0.2 + (e.getEntityBoundingBox().maxZ - e.getEntityBoundingBox().minZ + 0.4) * world.rand.nextDouble();


                    Particle fx = new ParticleBlockDust.Factory().createParticle(-1, world, ix, iy, iz, 0, 0, 0,
                            Block.getStateId(b.getStateFromMeta(meta)));
                    if (fx == null) return;
                    fx.setMaxAge(150 + world.rand.nextInt(50));

                    Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                }
            }
        });
        Splash.setHandler((world, x, y, z, data) -> {
            int particleSetting = Minecraft.getMinecraft().gameSettings.particleSetting;
            if (particleSetting == 0 || (particleSetting == 1 && world.rand.nextBoolean())) {
                ParticleLiquidSplash fx = new ParticleLiquidSplash(world, x, y, z);

                if (data.hasKey("color")) {
                    Color color = new Color(data.getInteger("color"));
                    float f = 1F - world.rand.nextFloat() * 0.2F;
                    fx.setRBGColorF(color.getRed() / 255F * f, color.getGreen() / 255F * f, color.getBlue() / 255F * f);
                }

                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
            }
        });
        FluidFill.setHandler((world, x, y, z, data) -> {
            double mX = data.getDouble("mX");
            double mY = data.getDouble("mY");
            double mZ = data.getDouble("mZ");

            Particle fx = new ParticleCrit.Factory().createParticle(0, world, x, y, z, mX, mY, mZ);
            fx.nextTextureIndexX();

            if (data.hasKey("color")) {
                Color color = new Color(data.getInteger("color"));
                fx.setRBGColorF(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
            }

            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
        });
        RadiationFlash.setHandler((world, _, _, _, data) -> {
            Random rand = world.rand;
            EntityPlayer player = Minecraft.getMinecraft().player;
            for (int i = 0; i < data.getInteger("count"); i++) {
                Particle flash = new ParticleSuspendedTown.Factory().createParticle(-1, world,
                        player.posX + rand.nextGaussian() * 4,
                        player.posY + rand.nextGaussian() * 2,
                        player.posZ + rand.nextGaussian() * 4,
                        0, 0, 0);

                flash.setRBGColorF(0F, 0.75F, 1F);
                flash.motionX = rand.nextGaussian();
                flash.motionY = rand.nextGaussian();
                flash.motionZ = rand.nextGaussian();
                Minecraft.getMinecraft().effectRenderer.addEffect(flash);
            }
        });
        AmatExplosion.setHandler((world, x, y, z, data) -> {
            if(data.getInteger("scale") < 15)
                world.playSound(x, y, z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.4F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F, false);
            else
                world.playSound(x, y, z, HBMSoundHandler.mukeExplosion, SoundCategory.BLOCKS, 15.0F, 1.0F, false);
            Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleAmatFlash(world, x, y, z, data.getFloat("scale")));
        });
        VanillaBurst_Flame.setHandler((world, x, y, z, data) -> {
            double motion = data.getDouble("motion");
            for (int i = 0; i < data.getInteger("count"); i++) {
                double mX = world.rand.nextGaussian() * motion;
                double mY = world.rand.nextGaussian() * motion;
                double mZ = world.rand.nextGaussian() * motion;
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleFlame.Factory().createParticle(-1, world, x, y, z, mX, mY, mZ));
            }
        });
        VanillaBurst_Cloud.setHandler((world, x, y, z, data) -> {
            double motion = data.getDouble("motion");
            for (int i = 0; i < data.getInteger("count"); i++) {
                double mX = world.rand.nextGaussian() * motion;
                double mY = world.rand.nextGaussian() * motion;
                double mZ = world.rand.nextGaussian() * motion;
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleCloud.Factory().createParticle(-1, world, x, y, z, mX, mY, mZ));
            }
        });
        VanillaBurst_RedDust.setHandler((world, x, y, z, data) -> {
            for (int i = 0; i < data.getInteger("count"); i++)
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRedstone.Factory().createParticle(-1, world, x, y, z, 0.0F, 0.0F, 0.0F));
        });
        VanillaBurst_BlueDust.setHandler((world, x, y, z, data) -> {
            for (int i = 0; i < data.getInteger("count"); i++)
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRedstone.Factory().createParticle(-1, world, x, y, z, 0.01F, 0.01F, 1F));
        });
        VanillaBurst_GreenDust.setHandler((world, x, y, z, data) -> {
            for (int i = 0; i < data.getInteger("count"); i++)
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRedstone.Factory().createParticle(-1, world, x, y, z, 0.01F, 0.5F, 0.1F));
        });
        VanillaBurst_BlockDust.setHandler((world, x, y, z, data) -> {
            double motion = data.getDouble("motion");
            for (int i = 0; i < data.getInteger("count"); i++) {
                double mX = world.rand.nextGaussian() * motion;
                double mY = world.rand.nextGaussian() * motion;
                double mZ = world.rand.nextGaussian() * motion;
                Block b = Block.getBlockById(data.getInteger("block"));
                Particle particle = new ParticleBlockDust.Factory().createParticle(-1, world, x, y, z, mX, mY + 0.2, mZ,
                        Block.getStateId(b.getDefaultState()));
                if (particle == null) return;
                particle.setMaxAge(50 + world.rand.nextInt(50));
                Minecraft.getMinecraft().effectRenderer.addEffect(particle);
            }
        });
        VanillaExt_Flame.setHandler((world, x, y, z, data) -> {
            double mX = data.getDouble("mX");
            double mY = data.getDouble("mY");
            double mZ = data.getDouble("mZ");
            Particle fx = new ParticleFlame.Factory().createParticle(-1, world, x, y, z, mX, mY, mZ);
            fx.canCollide = !data.getBoolean("noclip");

            if (data.getInteger("overrideAge") > 0) fx.setMaxAge(data.getInteger("overrideAge"));

            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
        });
        VanillaExt_Smoke.setHandler((world, x, y, z, data) -> {
            double mX = data.getDouble("mX");
            double mY = data.getDouble("mY");
            double mZ = data.getDouble("mZ");
            Particle fx = new ParticleSmokeNormal.Factory().createParticle(-1, world, x, y, z, mX, mY, mZ);
            fx.canCollide = !data.getBoolean("noclip");

            if (data.getInteger("overrideAge") > 0) fx.setMaxAge(data.getInteger("overrideAge"));

            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
        });
        VanillaExt_Volcano.setHandler((world, x, y, z, data) -> {
            Random rand = world.rand;
            double mX = data.getDouble("mX");
            double mY = data.getDouble("mY");
            double mZ = data.getDouble("mZ");
            Particle fx = new ParticleSmokeNormal.Factory().createParticle(-1, world, x, y, z, mX, mY, mZ);
            ((ParticleSmokeNormal) fx).smokeParticleScale = 100f;
            fx.setMaxAge(200 + rand.nextInt(50));
            fx.canCollide = false;
            fx.motionX = rand.nextGaussian() * 0.2;
            fx.motionY = 2.5 + rand.nextDouble();
            fx.motionZ = rand.nextGaussian() * 0.2;
            fx.canCollide = !data.getBoolean("noclip");

            if (data.getInteger("overrideAge") > 0) fx.setMaxAge(data.getInteger("overrideAge"));

            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
        });
        VanillaExt_Cloud.setHandler((world, x, y, z, data) -> {
            double mX = data.getDouble("mX");
            double mY = data.getDouble("mY");
            double mZ = data.getDouble("mZ");
            Particle fx = new ParticleCloud.Factory().createParticle(-1, world, x, y, z, mX, mY, mZ);
            fx.canCollide = !data.getBoolean("noclip");

            if (data.getInteger("overrideAge") > 0) fx.setMaxAge(data.getInteger("overrideAge"));

            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
        });
        VanillaExt_RedDust.setHandler((world, x, y, z, data) -> {
            double mX = data.getDouble("mX");
            double mY = data.getDouble("mY");
            double mZ = data.getDouble("mZ");
            Particle fx = new ParticleRedstone.Factory().createParticle(-1, world, x, y, z, (float) mX, (float) mY, (float) mZ);
            fx.canCollide = !data.getBoolean("noclip");

            if (data.getInteger("overrideAge") > 0) fx.setMaxAge(data.getInteger("overrideAge"));

            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
        });
        VanillaExt_BlueDust.setHandler((world, x, y, z, data) -> {
            Particle fx = new ParticleRedstone.Factory().createParticle(-1, world, x, y, z, 0.01F, 0.01F, 1F);
            fx.canCollide = !data.getBoolean("noclip");

            if (data.getInteger("overrideAge") > 0) fx.setMaxAge(data.getInteger("overrideAge"));

            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
        });
        VanillaExt_GreenDust.setHandler((world, x, y, z, data) -> {
            Particle fx = new ParticleRedstone.Factory().createParticle(-1, world, x, y, z, 0.01F, 0.5F, 0.1F);
            fx.canCollide = !data.getBoolean("noclip");

            if (data.getInteger("overrideAge") > 0) fx.setMaxAge(data.getInteger("overrideAge"));

            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
        });
        VanillaExt_Fireworks.setHandler((world, x, y, z, _) -> world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, x, y, z, 0, 0, 0));
        VanillaExt_LargeExplode.setHandler((world, x, y, z, data) -> {
            Particle fx = new ParticleExplosionLarge.Factory().createParticle(-1, world, x, y, z, data.getFloat(
                    "size"), 0.0F, 0.0F);
            float r = 1.0F - world.rand.nextFloat() * 0.2F;
            fx.setRBGColorF(r, 0.9F * r, 0.5F * r);

            for (int i = 0; i < data.getByte("count"); i++) {
                ParticleExplosion sec =
                        (ParticleExplosion) new ParticleExplosion.Factory().createParticle(-1, world, x, y, z,
                                0.0F, 0.0F, 0.0F);
                float r2 = 1.0F - world.rand.nextFloat() * 0.5F;
                sec.setRBGColorF(0.5F * r2, 0.5F * r2, 0.5F * r2);
                sec.multipleParticleScaleBy(i + 1);
                Minecraft.getMinecraft().effectRenderer.addEffect(sec);
            }

            fx.canCollide = !data.getBoolean("noclip");

            if (data.getInteger("overrideAge") > 0) fx.setMaxAge(data.getInteger("overrideAge"));

            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
        });
        VanillaExt_TownAura.setHandler((world, x, y, z, data) -> {
            double mX = data.getDouble("mX");
            double mY = data.getDouble("mY");
            double mZ = data.getDouble("mZ");
            Particle fx = new ParticleSuspendedTown.Factory().createParticle(-1, world, x, y, z, 0, 0, 0);
            float color = 0.5F + world.rand.nextFloat() * 0.5F;
            fx.setRBGColorF(0.8F * color, 0.9F * color, color);
            fx.motionX = mX;
            fx.motionY = mY;
            fx.motionZ = mZ;
            fx.canCollide = !data.getBoolean("noclip");

            if (data.getInteger("overrideAge") > 0) fx.setMaxAge(data.getInteger("overrideAge"));

            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
        });
        VanillaExt_BlockDust.setHandler((world, x, y, z, data) -> {
            double mX = data.getDouble("mX");
            double mY = data.getDouble("mY");
            double mZ = data.getDouble("mZ");
            Block b = Block.getBlockById(data.getInteger("block"));
            int id = Block.getStateId(b.getDefaultState());
            Particle fx = new ParticleBlockDust.Factory().createParticle(-1, world, x, y, z, mX, mY + 0.2, mZ, id);
            if (fx == null) return;
            fx.setMaxAge(10 + world.rand.nextInt(20));
            fx.canCollide = !data.getBoolean("noclip");

            if (data.getInteger("overrideAge") > 0) fx.setMaxAge(data.getInteger("overrideAge"));

            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
        });
        VanillaExt_ColorDust.setHandler((world, x, y, z, data) -> {
            double mX = data.getDouble("mX");
            double mY = data.getDouble("mY");
            double mZ = data.getDouble("mZ");
            int id = Block.getStateId(Blocks.WOOL.getDefaultState());
            Particle fx = new ParticleBlockDust.Factory().createParticle(-1, world, x, y, z, mX, mY + 0.2, mZ, id);
            if (fx == null) return;
            fx.setRBGColorF(data.getFloat("r"), data.getFloat("g"), data.getFloat("b"));
            fx.setMaxAge(10 + world.rand.nextInt(20));
            fx.canCollide = !data.getBoolean("noclip");

            if (data.getInteger("overrideAge") > 0) fx.setMaxAge(data.getInteger("overrideAge"));

            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
        });
        Spark.setHandler((world, x, y, z, data) -> {
            Random rand = world.rand;
            double dirX = data.getDouble("dirX");
            double dirY = data.getDouble("dirY");
            double dirZ = data.getDouble("dirZ");
            float width = data.hasKey("width") ? data.getFloat("width") : 0.025F;
            float length = data.hasKey("length") ? data.getFloat("length") : 1.0F;
            float randLength = data.hasKey("randLength") ? data.getFloat("randLength") - length : 0;
            float gravity = data.hasKey("gravity") ? data.getFloat("gravity") : 9.81F * 0.01F;
            int lifetime = data.hasKey("lifetime") ? data.getInteger("lifetime") : 100;
            int randLifeTime = data.hasKey("randLifetime") ? data.getInteger("randLifetime") - lifetime : lifetime;
            float velocityRand = data.hasKey("randomVelocity") ? data.getFloat("randomVelocity") : 1.0F;
            float r = data.hasKey("r") ? data.getFloat("r") : 1.0F;
            float g = data.hasKey("g") ? data.getFloat("g") : 1.0F;
            float b = data.hasKey("b") ? data.getFloat("b") : 1.0F;
            float a = data.hasKey("a") ? data.getFloat("a") : 1.0F;
            float angle = data.hasKey("angle") ? data.getFloat("angle") : 10;
            float randAngle = data.hasKey("randAngle") ? data.getFloat("randAngle") - angle : 0;
            int count = data.hasKey("count") ? data.getInteger("count") : 1;
            for (int i = 0; i < count; i++) {
                //Gets a random vector rotated within a cone and then rotates it to the particle data's direction
                //Create a new vector and rotate it randomly about the x-axis within the angle specified, then rotate that by random degrees to get the random cone vector
                Vec3d up = new Vec3d(0, 1, 0);
                up = up.rotatePitch((float) Math.toRadians(rand.nextFloat() * (angle + rand.nextFloat() * randAngle)));
                up = up.rotateYaw((float) Math.toRadians(rand.nextFloat() * 360));
                //Finds the angles for the particle direction and rotate our random cone vector to it.
                Vec3d direction = new Vec3d(dirX, dirY, dirZ);
                Vec3d angles = BobMathUtil.getEulerAngles(direction);
                Vec3d newDirection = new Vec3d(up.x, up.y, up.z);
                newDirection = newDirection.rotatePitch((float) Math.toRadians(angles.y - 90));
                newDirection = newDirection.rotateYaw((float) Math.toRadians(angles.x));
                //Multiply it by the original vector's length to ensure it has the right magnitude
                newDirection = newDirection.scale((float) direction.length() + rand.nextFloat() * velocityRand);
                Particle fx = new ParticleSpark(world, x, y, z, length + rand.nextFloat() * randLength, width
                        , lifetime + rand.nextInt(randLifeTime), gravity).color(r, g, b, a).motion((float) newDirection.x, (float) newDirection.y, (float) newDirection.z);
                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
            }
        });
        Hadron.setHandler((world, x, y, z, _) -> Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleHadron(world, x, y, z)));
        SchrabFog.setHandler((world, x, y, z, _) -> {
            ParticleSuspendedTown flash =
                    (ParticleSuspendedTown) new ParticleSuspendedTown.Factory().createParticle(-1, world, x, y, z, 0, 0,
                            0);
            flash.setRBGColorF(0F, 1F, 1F);
            Minecraft.getMinecraft().effectRenderer.addEffect(flash);
        });
        Rift.setHandler((world, x, y, z, _) -> Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRift(world, x, y, z)));
        RBMKFlame.setHandler((world, x, y, z, data) -> {
            int maxAge = data.getInteger("maxAge");
            if(GeneralConfig.instancedParticles) {
                InstancedParticleRenderer.addParticle(new ParticleRBMKFlameInstanced(world, x, y, z, maxAge));
            } else {
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRBMKFlame(world, x, y, z, maxAge));
            }
        });
        RBMKSteam.setHandler((world, x, y, z, _) -> {
            if(GeneralConfig.instancedParticles) {
                InstancedParticleRenderer.addParticle(new ParticleRBMKSteamInstanced(world, x, y, z));
            } else {
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRBMKSteam(world, x, y, z));
            }
        });
        RBMKMush.setHandler((world, x, y, z, data) -> {
            float scale = data.getFloat("scale");
            if(GeneralConfig.instancedParticles) {
                InstancedParticleRenderer.addParticle(new ParticleRBMKMushInstanced(world, x, y, z, scale));
            } else {
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRBMKMush(world, x, y, z, scale));
            }
        });
        Tower.setHandler((world, x, y, z, data) -> {
            int particleSetting = Minecraft.getMinecraft().gameSettings.particleSetting;
            if (particleSetting == 0 || (particleSetting == 1 && world.rand.nextBoolean())) {
                ParticleCoolingTower fx = new ParticleCoolingTower(world, x, y, z);
                fx.setLift(data.getFloat("lift"));
                fx.setBaseScale(data.getFloat("base"));
                fx.setMaxScale(data.getFloat("max"));
                fx.setLife(data.getInteger("life") / (particleSetting + 1));
                if (data.hasKey("noWind")) fx.noWind();
                if (data.hasKey("strafe")) fx.setStrafe(data.getFloat("strafe"));
                if (data.hasKey("alpha")) fx.alphaMod(data.getFloat("alpha"));

                if (data.hasKey("color")) {
                    Color color = new Color(data.getInteger("color"));
                    fx.setRBGColorF(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
                }

                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
            }
        });
        Jetpack.setHandler((world, _, _, _, data) -> {
            Entity ent = world.getEntityByID(data.getInteger("player"));

            if (ent instanceof EntityPlayer p) {

                Vec3d vec = new Vec3d(0, 0, -0.25);
                Vec3d offset = new Vec3d(0.125, 0, 0);
                float angle = (float) -Math.toRadians(p.rotationYawHead - (p.rotationYawHead - p.renderYawOffset));

                vec = vec.rotateYaw(angle);
                offset = offset.rotateYaw(angle);

                double ix = p.posX + vec.x;
                double iy = p.posY + p.eyeHeight - 1;
                double iz = p.posZ + vec.z;
                double ox = offset.x;
                double oz = offset.z;

                double moX = 0;
                double moY = 0;
                double moZ = 0;

                int mode = data.getInteger("mode");

                if (mode == 0) {
                    moY -= 0.2;
                }

                if (mode == 1) {
                    Vec3d look = p.getLookVec();

                    moX -= look.x * 0.1D;
                    moY -= look.y * 0.1D;
                    moZ -= look.z * 0.1D;
                }

                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleFlame.Factory().createParticle(-1,
                        world, ix + ox, iy, iz + oz, p.motionX + moX * 2, p.motionY + moY * 2, p.motionZ + moZ * 2));
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleFlame.Factory().createParticle(-1,
                        world, ix - ox, iy, iz - oz, p.motionX + moX * 2, p.motionY + moY * 2, p.motionZ + moZ * 2));
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSmokeNormal.Factory().createParticle(-1, world, ix + ox, iy, iz + oz, p.motionX + moX * 3, p.motionY + moY * 3, p.motionZ + moZ * 3));
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSmokeNormal.Factory().createParticle(-1, world, ix - ox, iy,
                        iz - oz, p.motionX + moX * 3, p.motionY + moY * 3, p.motionZ + moZ * 3));
            }
        });
        bnuuy.setHandler((world, _, _, _, data) -> {
            if(Minecraft.getMinecraft().gameSettings.particleSetting == 2)
                return;

            Entity ent = world.getEntityByID(data.getInteger("player"));

            if(ent instanceof EntityPlayer p) {

                Vec3NT vec = new Vec3NT(0, 0, -0.6);
                Vec3NT offset = new Vec3NT(0.275, 0, 0);
                float angle = (float) -Math.toRadians(p.rotationYawHead - (p.rotationYawHead - p.renderYawOffset));

                vec.rotateYawSelf(angle);
                offset.rotateYawSelf(angle);

                double ix = p.posX + vec.x;
                double iy = p.posY + p.eyeHeight - 1 + 0.4;
                double iz = p.posZ + vec.z;
                double ox = offset.x;
                double oz = offset.z;

                if(Minecraft.getMinecraft().player.isSneaking()) {
                    iy += 0.25;
                }

                vec.normalizeSelf();
                double mult = 0.025D;
                double mX = vec.x * mult;
                double mZ = vec.z * mult;

                for(int i = 0; i < 2; i++) {
                    Particle fx = new ParticleSmokeNormal.Factory().createParticle(-1, world, ix + ox * (i == 0 ? -1 : 1), iy, iz + oz * (i == 0 ? -1 : 1), mX, 0, mZ);
                    fx.particleScale = 0.5F;
                    Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                }
            }
        });
        Jetpack_BJ.setHandler((world, _, _, _, data) -> {
            if(Minecraft.getMinecraft().gameSettings.particleSetting == 2)
                return;

            Entity ent = world.getEntityByID(data.getInteger("player"));

            if(ent instanceof EntityPlayer p) {

                Vec3NT vec = new Vec3NT(0, 0, -0.3125);
                Vec3NT offset = new Vec3NT(0.125, 0, 0);
                float angle = (float) -Math.toRadians(p.rotationYawHead - (p.rotationYawHead - p.renderYawOffset));

                vec.rotateYawSelf(angle);
                offset.rotateYawSelf(angle);

                double ix = p.posX + vec.x;
                double iy = p.posY + p.eyeHeight - 0.9375;
                double iz = p.posZ + vec.z;
                double ox = offset.x;
                double oz = offset.z;

                if(Minecraft.getMinecraft().gameSettings.particleSetting == 0) {
                    Vec3d pos = new Vec3d(ix, iy, iz);
                    Vec3d thrust = new Vec3d(0, -1, 0);
                    Vec3d target = pos.add(thrust.x * 10, thrust.y * 10, thrust.z * 10);
                    RayTraceResult ray = Minecraft.getMinecraft().player.world.rayTraceBlocks(pos, target, false, false, true);

                    if(ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK && ray.sideHit == EnumFacing.UP) {
                        IBlockState state = world.getBlockState(ray.getBlockPos());

                        Vec3d delta = new Vec3d(ix - ray.hitVec.x, iy - ray.hitVec.y, iz - ray.hitVec.z);
                        Vec3NT vel = new Vec3NT(0.75 - delta.length() * 0.075, 0, 0);

                        for(int i = 0; i < (10 - delta.length()); i++) {
                            vel.rotateYawSelf(world.rand.nextFloat() * (float)Math.PI * 2F);
                            Particle particle = new ParticleBlockDust.Factory().createParticle(-1, world, ray.hitVec.x, ray.hitVec.y + 0.1, ray.hitVec.z, vel.x, 0.1, vel.z, Block.getStateId(state));
                            if (particle == null) {
                                continue;
                            }

                            Minecraft.getMinecraft().effectRenderer.addEffect(particle);
                        }
                    }
                }

                Particle dust1 = new ParticleRedstone.Factory().createParticle(-1, world, ix + ox, iy, iz + oz, p.motionX, p.motionY, p.motionZ);
                Particle dust2 = new ParticleRedstone.Factory().createParticle(-1, world, ix - ox, iy, iz - oz, p.motionX, p.motionY, p.motionZ);
                dust1.setRBGColorF(0.8F, 0.5F, 1.0F);
                dust2.setRBGColorF(0.8F, 0.5F, 1.0F);

                Minecraft.getMinecraft().effectRenderer.addEffect(dust1);
                Minecraft.getMinecraft().effectRenderer.addEffect(dust2);
            }
        });
        Jetpack_DNS.setHandler((world, _, _, _, data) -> {

            if(Minecraft.getMinecraft().gameSettings.particleSetting == 2)
                return;

            Entity ent = world.getEntityByID(data.getInteger("player"));

            if(ent instanceof EntityPlayer p) {

                Vec3NT offset = new Vec3NT(0.125, 0, 0);
                float angle = (float) -Math.toRadians(p.rotationYawHead - (p.rotationYawHead - p.renderYawOffset));

                offset.rotateYawSelf(angle);

                double ix = p.posX;
                double iy = p.posY - p.getYOffset() - 0.5D;
                double iz = p.posZ;
                double ox = offset.x;
                double oz = offset.z;

                if(Minecraft.getMinecraft().gameSettings.particleSetting == 0) {
                    Vec3d pos = new Vec3d(ix, iy, iz);
                    Vec3d thrust = new Vec3d(0, -1, 0);
                    Vec3d target = pos.add(thrust.x * 10, thrust.y * 10, thrust.z * 10);
                    RayTraceResult ray = Minecraft.getMinecraft().player.world.rayTraceBlocks(pos, target, false, false, true);

                    if(ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK && ray.sideHit == EnumFacing.UP) {
                        IBlockState state = world.getBlockState(ray.getBlockPos());

                        Vec3d delta = new Vec3d(ix - ray.hitVec.x, iy - ray.hitVec.y, iz - ray.hitVec.z);
                        Vec3d vel = new Vec3d(0.75 - delta.length() * 0.075, 0, 0);

                        for(int i = 0; i < (10 - delta.length()); i++) {
                            vel = vel.rotateYaw(world.rand.nextFloat() * (float)Math.PI * 2F);
                            Particle particle = new ParticleBlockDust.Factory().createParticle(-1, world, ray.hitVec.x, ray.hitVec.y + 0.1, ray.hitVec.z, vel.x, 0.1, vel.z, Block.getStateId(state));
                            if (particle == null) {
                                continue;
                            }

                            Minecraft.getMinecraft().effectRenderer.addEffect(particle);
                        }
                    }
                }

                Particle dust1 = new ParticleRedstone.Factory().createParticle(-1, world, ix + ox, iy, iz + oz, p.motionX, p.motionY, p.motionZ);
                Particle dust2 = new ParticleRedstone.Factory().createParticle(-1, world, ix - ox, iy, iz - oz, p.motionX, p.motionY, p.motionZ);
                dust1.setRBGColorF(0.01F, 1.0F, 1.0F);
                dust2.setRBGColorF(0.01F, 1.0F, 1.0F);

                Minecraft.getMinecraft().effectRenderer.addEffect(dust1);
                Minecraft.getMinecraft().effectRenderer.addEffect(dust2);
            }
        });
        BF.setHandler((world, x, y, z, _) -> {
            ParticleMukeCloud cloud = new ParticleMukeCloudBF(world, x, y, z, 0, 0, 0);
            Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
        });
        FX_Chlorine.setHandler((world, x, y, z, data) -> {
            double mX = data.getDouble("moX");
            double mY = data.getDouble("moY");
            double mZ = data.getDouble("moZ");
            EntityChlorineFX eff = new EntityChlorineFX(world, x, y, z, mX, mY, mZ);
            Minecraft.getMinecraft().effectRenderer.addEffect(eff);
        });
        FX_Cloud.setHandler((world, x, y, z, data) -> {
            double mX = data.getDouble("moX");
            double mY = data.getDouble("moY");
            double mZ = data.getDouble("moZ");
            EntityCloudFX eff = new EntityCloudFX(world, x, y, z, mX, mY, mZ);
            Minecraft.getMinecraft().effectRenderer.addEffect(eff);
        });
        FX_Orange.setHandler((world, x, y, z, data) -> {
            double mX = data.getDouble("moX");
            double mY = data.getDouble("moY");
            double mZ = data.getDouble("moZ");
            EntityOrangeFX eff = new EntityOrangeFX(world, x, y, z, mX, mY, mZ);
            Minecraft.getMinecraft().effectRenderer.addEffect(eff);
        });
        FX_PinkCloud.setHandler((world, x, y, z, data) -> {
            double mX = data.getDouble("moX");
            double mY = data.getDouble("moY");
            double mZ = data.getDouble("moZ");
            EntityPinkCloudFX eff = new EntityPinkCloudFX(world, x, y, z, mX, mY, mZ);
            Minecraft.getMinecraft().effectRenderer.addEffect(eff);
        });
        BulletImpact.setHandler((world, x, y, z, data) -> {
            RayTraceResult.Type hitType = RayTraceResult.Type.values()[data.getByte("hitType")];
            Vec3d normal = new Vec3d(data.getFloat("nX"), data.getFloat("nY"), data.getFloat("nZ"));
            if (hitType == RayTraceResult.Type.BLOCK) {
                IBlockState state = Block.getBlockById(data.getInteger("block")).getStateFromMeta(data.getByte(
                        "meta"));
                Material mat = state.getMaterial();
                float r = 1;
                float g = 1;
                float b = 1;
                float scale = 1;
                float randMotion = 0.2F;
                int count = 10;
                int smokeCount = 3;
                int smokeScale = 5;
                int smokeLife = 15;
                if (mat == Material.IRON) {
                    world.playSound(x, y, z, HBMSoundHandler.hit_metal, SoundCategory.BLOCKS, 1,
                            0.9F + world.rand.nextFloat() * 0.2F, false);
                } else {
                    world.playSound(x, y, z, HBMSoundHandler.hit_dirt, SoundCategory.BLOCKS, 1,
                            0.7F + world.rand.nextFloat() * 0.3F, false);
                }
                if (mat == Material.ROCK || mat == Material.GROUND || mat == Material.GRASS || mat == Material.WOOD || mat == Material.LEAVES || mat == Material.SAND) {
                    ResourceLocation tex = ResourceManager.rock_fragments;
                    if (mat == Material.WOOD) {
                        tex = ResourceManager.wood_fragments;
                    } else if (mat == Material.LEAVES) {
                        tex = ResourceManager.twigs_and_leaves;
                        smokeLife = 5;
                        smokeScale = 10;
                        smokeCount = 2;
                    }
                    if (mat == Material.GROUND || mat == Material.GRASS) {
                        r = 0.8F;
                        g = 0.5F;
                        b = 0.3F;
                        scale = 0.6F;
                        count = 40;
                    }
                    if (mat == Material.SAND) {
                        g = 0.9F;
                        b = 0.6F;
                        scale = 0.1F;
                        randMotion = 0.5F;
                        count = 100;
                        smokeCount = 5;
                    }
                    for (int i = 0; i < count; i++) {
                        Vec3d dir = BobMathUtil.randVecInCone(normal, 45, world.rand);
                        dir = dir.scale(0.1F + world.rand.nextFloat() * randMotion);
                        Vec3d offset = normal.scale(0.2F);
                        ParticleHitDebris particle = new ParticleHitDebris(world, x + offset.x, y + offset.y,
                                z + offset.z, tex, world.rand.nextInt(16), scale, 40 + world.rand.nextInt(20));
                        particle.motion((float) dir.x, (float) dir.y, (float) dir.z);
                        particle.color(r, g, b);
                        ParticleBatchRenderer.addParticle(particle);
                    }
                    if (mat == Material.WOOD) {
                        r = 0.8F;
                        g = 0.5F;
                        b = 0.3F;
                    }
                    if (mat == Material.LEAVES) {
                        r = 0.2F;
                        g = 0.8F;
                        b = 0.4F;
                    }
                }
                if (mat != Material.LEAVES) {
                    ParticleBulletImpact impact = new ParticleBulletImpact(world, x + normal.x * 0.01F,
                            y + normal.y * 0.01F, z + normal.z * 0.01F, 0.1F, 60 + world.rand.nextInt(20), normal);
                    impact.color(r, g, b);
                    ParticleBatchRenderer.addParticle(impact);
                }
                if (mat == Material.SAND) {
                    r *= 1.5F;
                    g *= 1.5F;
                    b *= 1.5F;
                }
                if (mat == Material.IRON) {
                    NBTTagCompound nbt = new NBTTagCompound();
                    nbt.setDouble("posX", x);
                    nbt.setDouble("posY", y);
                    nbt.setDouble("posZ", z);
                    nbt.setDouble("dirX", normal.x * 0.6F);
                    nbt.setDouble("dirY", normal.y * 0.6F);
                    nbt.setDouble("dirZ", normal.z * 0.6F);
                    nbt.setFloat("r", 0.8F);
                    nbt.setFloat("g", 0.6F);
                    nbt.setFloat("b", 0.5F);
                    nbt.setFloat("a", 1.5F);
                    nbt.setInteger("lifetime", 1 + world.rand.nextInt(2));
                    nbt.setFloat("width", 0.03F);
                    nbt.setFloat("length", 0.3F);
                    nbt.setFloat("randLength", 0.6F);
                    nbt.setFloat("gravity", 0.1F);
                    nbt.setFloat("angle", 60F);
                    nbt.setInteger("count", 2 + world.rand.nextInt(2));
                    nbt.setFloat("randomVelocity", 0.3F);
                    MainRegistry.proxy.effectNT(Spark, x, y, z, nbt);
                } else {
                    for (int i = 0; i < smokeCount; i++) {
                        Vec3d dir = BobMathUtil.randVecInCone(normal, 30, world.rand);
                        dir = dir.scale(0.1 + world.rand.nextFloat() * 0.5);
                        ParticleSmokeAnim smoke = new ParticleSmokeAnim(world, x, y, z, 0.1F,
                                smokeScale + world.rand.nextFloat() * smokeScale, 1, smokeLife);
                        smoke.color(r * 0.5F, g * 0.5F, b * 0.5F);
                        smoke.motion((float) dir.x, (float) dir.y, (float) dir.z);
                        ParticleBatchRenderer.addParticle(smoke);
                    }
                }

            } else if (hitType == RayTraceResult.Type.ENTITY) {
                world.playSound(x, y, z, HBMSoundHandler.hit_flesh, SoundCategory.BLOCKS, 1,
                        0.8F + world.rand.nextFloat() * 0.4F, false);
                Vec3d bulletDirection = new Vec3d(data.getFloat("dirX"), data.getFloat("dirY"), data.getFloat(
                        "dirZ"));
                if (GeneralConfig.bloodFX) {
                    for (int i = 0; i < 2; i++) {
                        int age = 10 + world.rand.nextInt(5);
                        ParticleBloodParticle blood = new ParticleBloodParticle(world, x, y, z,
                                world.rand.nextInt(9), 1 + world.rand.nextFloat() * 3,
                                0.5F + world.rand.nextFloat() * 0.5F, age);
                        blood.color(0.5F, 0F, 0F);
                        Vec3d dir = BobMathUtil.randVecInCone(normal, 70, world.rand);
                        dir = dir.scale(0.05F + world.rand.nextFloat() * 0.25);
                        if (i > 0) {
                            dir = BobMathUtil.randVecInCone(bulletDirection.normalize(), 20, world.rand);
                            dir = dir.scale(1F + world.rand.nextFloat());
                            blood.setMaxAge((int) (age * 0.75F));
                        }
                        blood.motion((float) dir.x, (float) dir.y + 0.1F, (float) dir.z);
                        ParticleBatchRenderer.addParticle(blood);
                    }
                    for (int i = 0; i < 3; i++) {
                        Vec3d dir = BobMathUtil.randVecInCone(normal, 30, world.rand);
                        dir = dir.scale(0.1 + world.rand.nextFloat() * 0.5);
                        ParticleSmokeAnim smoke = new ParticleSmokeAnim(world, x, y, z, 0.1F,
                                3 + world.rand.nextFloat() * 3, 1, 10);
                        smoke.color(0.4F, 0, 0);
                        smoke.motion((float) dir.x, (float) dir.y, (float) dir.z);
                        ParticleBatchRenderer.addParticle(smoke);
                    }
                }
            }
        });
        Vanilla.setHandler((world, x, y, z, data) -> {
            double mX = data.getDouble("mX");
            double mY = data.getDouble("mY");
            double mZ = data.getDouble("mZ");
            EnumParticleTypes type = EnumParticleTypes.getByName(data.getString("mode"));
            if (type == null) throw new IllegalArgumentException("Unknown particle: " + data.getString("mode"));
            world.spawnParticle(type, x, y, z, mX, mY, mZ);
        });
        Anim.setHandler((world, _, _, _, data) -> {
            EntityPlayer player = Minecraft.getMinecraft().player;
            EnumHand hand = EnumHand.values()[data.getInteger("hand")];
            int slot = player.inventory.currentItem;
            if (hand == EnumHand.OFF_HAND) {
                slot = 9;
            }
            String mode = data.getString("mode");

            if ("generic".equals(mode)) {
                ItemStack stack = player.getHeldItem(hand);

                if (!stack.isEmpty() && stack.getItem() instanceof IAnimatedItem item) {
                    BusAnimation anim = item.getAnimation(data, stack);

                    if (anim != null) {
                        HbmAnimations.hotbar[slot] =
                                new HbmAnimations.Animation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                        System.currentTimeMillis(), anim);
                    }
                }
            }

            switch (mode) {
                case "equip" -> HbmAnimations.hotbar[slot] =
                        new HbmAnimations.BlenderAnimation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                System.currentTimeMillis(), 1, ResourceManager.crucible_equip,
                                new AnimationWrapper.EndResult(AnimationWrapper.EndType.STAY));
                case "crucible" -> {
                    BusAnimation animation = new BusAnimation()
                            .addBus("GUARD_ROT", new BusAnimationSequence()
                                    .addKeyframe(new BusAnimationKeyframe(90, 0, 1, 0))
                                    .addKeyframe(new BusAnimationKeyframe(90, 0, 1, 800))
                                    .addKeyframe(new BusAnimationKeyframe(0, 0, 1, 50)));

                    HbmAnimations.hotbar[slot] =
                            new HbmAnimations.Animation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                    System.currentTimeMillis(), animation);
                }
                case "swing" -> {
                    BusAnimation animation = new BusAnimation()
                            .addBus("SWING", new BusAnimationSequence()
                                    .addKeyframe(new BusAnimationKeyframe(120, 0, 0, 150))
                                    .addKeyframe(new BusAnimationKeyframe(0, 0, 0, 500)));
                    if (HbmAnimations.hotbar[slot] instanceof HbmAnimations.BlenderAnimation) {
                        HbmAnimations.hotbar[slot].animation = animation;
                        HbmAnimations.hotbar[slot].startMillis = System.currentTimeMillis();
                    } else {
                        HbmAnimations.hotbar[slot] =
                                new HbmAnimations.Animation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                        System.currentTimeMillis(), animation);
                    }
                }
                case "cSwing" -> {
                    if (HbmAnimations.getRelevantTransformation("SWING_ROT", hand)[0] == 0) {

                        int offset = world.rand.nextInt(80) - 20;

                        BusAnimation animation = new BusAnimation()
                                .addBus("SWING_ROT", new BusAnimationSequence()
                                        .addKeyframe(new BusAnimationKeyframe(60 - offset, 60 - offset,
                                                -55, 75))
                                        .addKeyframe(new BusAnimationKeyframe(60 + offset, 60 - offset,
                                                -45, 150))
                                        .addKeyframe(new BusAnimationKeyframe(0, 0, 0, 500)))
                                .addBus("SWING_TRANS", new BusAnimationSequence()
                                        .addKeyframe(new BusAnimationKeyframe(-0, -10, 0, 75))
                                        .addKeyframe(new BusAnimationKeyframe(0, -10, 0, 150))
                                        .addKeyframe(new BusAnimationKeyframe(0, 0, 0, 500)));

                        if (HbmAnimations.hotbar[slot] instanceof HbmAnimations.BlenderAnimation) {
                            HbmAnimations.hotbar[slot].animation = animation;
                            HbmAnimations.hotbar[slot].startMillis = System.currentTimeMillis();
                        } else {
                            HbmAnimations.hotbar[slot] =
                                    new HbmAnimations.Animation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                            System.currentTimeMillis(), animation);
                        }
                    }
                }
                case "sSwing", "lSwing" -> { //temp for lance

                    int forward = 150;
                    int sideways = 100;
                    int retire = 200;

                    if (HbmAnimationsSedna.getRelevantAnim() == null) {

                        BusAnimationSedna animation = new BusAnimationSedna()
                                .addBus("SWING_ROT", new BusAnimationSequenceSedna()
                                        .addPos(0, 0, 90, forward)
                                        .addPos(45, 0, 90, sideways)
                                        .addPos(0, 0, 0, retire))
                                .addBus("SWING_TRANS", new BusAnimationSequenceSedna()
                                        .addPos(0, 0, 3, forward)
                                        .addPos(2, 0, 2, sideways)
                                        .addPos(0, 0, 0, retire));


                        HbmAnimationsSedna.hotbar[player.inventory.currentItem][0] = new HbmAnimationsSedna.Animation(player.getHeldItemMainhand().getItem().getTranslationKey(), System.currentTimeMillis(), animation, null);

                    } else {

                        double[] rot = HbmAnimationsSedna.getRelevantTransformation("SWING_ROT");
                        double[] trans = HbmAnimationsSedna.getRelevantTransformation("SWING_TRANS");

                        if (System.currentTimeMillis() - HbmAnimationsSedna.getRelevantAnim().startMillis < 50)
                            return;

                        BusAnimationSedna animation = new BusAnimationSedna()
                                .addBus("SWING_ROT", new BusAnimationSequenceSedna()
                                        .addPos(rot[0], rot[1], rot[2], 0)
                                        .addPos(0, 0, 90, forward)
                                        .addPos(45, 0, 90, sideways)
                                        .addPos(0, 0, 0, retire))
                                .addBus("SWING_TRANS", new BusAnimationSequenceSedna()
                                        .addPos(trans[0], trans[1], trans[2], 0)
                                        .addPos(0, 0, 3, forward)
                                        .addPos(2, 0, 2, sideways)
                                        .addPos(0, 0, 0, retire));

                        HbmAnimationsSedna.hotbar[player.inventory.currentItem][0] = new HbmAnimationsSedna.Animation(player.getHeldItemMainhand().getItem().getTranslationKey(), System.currentTimeMillis(), animation, null);
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + mode);
            }
        });
        Tau.setHandler((world, x, y, z, data) -> {
            boolean small = data.getBoolean("small");
            for (int i = 0; i < data.getByte("count"); i++)
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleHbmSpark(world, x, y, z,
                        world.rand.nextGaussian() * 0.05, 0.05, world.rand.nextGaussian() * 0.05).makeSmall(small));
            Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleHadron(world, x, y, z).makeSmall(small));
        });
        Giblets.setHandler((world, x, y, z, data) -> {
            Random rand = world.rand;
            int ent = data.getInteger("ent");
            int gibType = data.getInteger("gibType");
            ClientProxy.vanish(ent);
            Entity e = world.getEntityByID(ent);

            if (e == null)
                return;

            float width = e.width;
            float height = e.height;
            int gW = (int) (width / 0.25F);
            int gH = (int) (height / 0.25F);

            boolean blowMeIntoTheGodDamnStratosphere = rand.nextInt(15) == 0;
            double mult = 1D;

            if (blowMeIntoTheGodDamnStratosphere)
                mult *= 10;

            for (int i = -(gW / 2); i <= gW; i++) {
                for (int j = 0; j <= gH; j++) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleGiblet(world, x, y, z,
                            rand.nextGaussian() * 0.25 * mult, rand.nextDouble() * mult,
                            rand.nextGaussian() * 0.25 * mult, gibType));
                }
            }
        });
    }

    public static final Class<?>[] PARAM_TYPES = {EffectHandler.class};
    private Object handler;

    @SideOnly(Side.CLIENT)
    public void setHandler(EffectHandler handler) {
        this.handler = handler;
    }

    @SideOnly(Side.CLIENT)
    public void summonParticle(World world, double x, double y, double z, @Nullable NBTTagCompound data) {
        ((EffectHandler) handler).summonParticle(world, x, y, z, data == null ? new NBTTagCompound() : data);
    }

    public static HbmEffectNT registerEffect(String name, EffectHandler handler) {
        return EnumHelper.addEnum(HbmEffectNT.class, name, PARAM_TYPES, handler);
    }
    
    @FunctionalInterface
    public interface EffectHandler {
        @SideOnly(Side.CLIENT)
        void summonParticle(World world, double x, double y, double z, @NotNull NBTTagCompound data);
    }
}
