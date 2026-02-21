package com.hbm.entity.logic;

import com.hbm.config.BombConfig;
import com.hbm.config.CompatibilityConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.effect.EntityFalloutRain;
import com.hbm.entity.mob.EntityGlowingOne;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.explosion.ExplosionNukeRayBatched;
import com.hbm.explosion.ExplosionNukeRayParallelized;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IExplosionRay;
import com.hbm.main.AdvancementManager;
import com.hbm.main.MainRegistry;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.util.MutableVec3d;
import com.hbm.world.WorldUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AutoRegister(name = "entity_nuke_mk5", trackingRange = 1000)
public class EntityNukeExplosionMK5 extends EntityExplosionChunkloading {
    //Strength of the blast
    private int strength;
    //How many rays are calculated per tick
    private int radius;
    private boolean spawnFire = false;

    private boolean fallout = true;
    private IExplosionRay explosion;
    private boolean floodPlease = false;
    private boolean initialized = false;
    private int falloutAdd = 0;
    private int algorithm;
    private long explosionStart = 0;

    public UUID detonator;

    public EntityNukeExplosionMK5(World world) {
        super(world);
    }

    public EntityNukeExplosionMK5(World world, int strength, int speed, int radius) {
        super(world);
        this.strength = strength;
        this.radius = radius;
        this.algorithm = BombConfig.explosionAlgorithm;
    }

    private static boolean isWet(World world, BlockPos pos) {
        Biome b = world.getBiome(pos);
        return b.getTempCategory() == Biome.TempCategory.OCEAN || b.isHighHumidity() || b == Biomes.BEACH || b == Biomes.OCEAN || b == Biomes.RIVER || b == Biomes.DEEP_OCEAN || b == Biomes.FROZEN_OCEAN || b == Biomes.FROZEN_RIVER || b == Biomes.STONE_BEACH || b == Biomes.SWAMPLAND;
    }

    public static EntityNukeExplosionMK5 statFac(World world, int r, double x, double y, double z) {
        if (GeneralConfig.enableExtendedLogging && !world.isRemote)
            MainRegistry.logger.log(Level.INFO, "[NUKE] Initialized explosion at " + x + " / " + y + " / " + z + " with radius " + r + "!");

        if (r == 0) r = 25;

        EntityNukeExplosionMK5 mk5 = new EntityNukeExplosionMK5(world);

        mk5.strength = 2 * r;
        mk5.radius = r;
        mk5.algorithm = BombConfig.explosionAlgorithm;

        mk5.setPosition(x, y, z);
        mk5.floodPlease = isWet(world, new BlockPos(x, y, z));
        if (BombConfig.disableNuclear) mk5.fallout = false;
        return mk5;
    }

    public static EntityNukeExplosionMK5 statFacFire(World world, int r, double x, double y, double z) {

        EntityNukeExplosionMK5 mk5 = statFac(world, r, x, y, z);
        mk5.spawnFire = true;
        return mk5;
    }

    public static EntityNukeExplosionMK5 statFacNoRad(World world, int r, double x, double y, double z) {

        EntityNukeExplosionMK5 mk5 = statFac(world, r, x, y, z);
        mk5.fallout = false;
        return mk5;
    }

    public static EntityNukeExplosionMK5 statFacNoRadFire(World world, int r, double x, double y, double z) {

        EntityNukeExplosionMK5 mk5 = statFac(world, r, x, y, z);
        mk5.fallout = false;
        mk5.spawnFire = true;
        return mk5;
    }

    @Override
    public void onUpdate() {
        if (world.isRemote) return;

        if (strength == 0 || !CompatibilityConfig.isWarDim(world)) {
            this.setDead();
            return;
        }
        if (!world.isRemote) loadChunk(chunkCoordX, chunkCoordZ);

        for (EntityPlayer player : this.world.playerEntities) {
            AdvancementManager.grantAchievement(player, AdvancementManager.achManhattan);
        }
        List<Entity> list = WorldUtil.getEntitiesInRadius(world, this.posX, this.posY, this.posZ, this.radius * 2.0D);
        if (fallout && explosion != null && this.ticksExisted < 10 && strength >= 75) {
            List<EntityLivingBase> livingList = new ArrayList<>(list.size());
            for (Entity e : list) if (e instanceof EntityLivingBase livingBase) livingList.add(livingBase);
            radiate(livingList, 2_500_000F / (this.ticksExisted * 5 + 1));
        }

        ExplosionNukeGeneric.dealDamage(world, list, this.posX, this.posY, this.posZ, this.radius * 2.0D);
        //radiate until there is fallout rain
        if (fallout && ticksExisted == 42)
            EntityGlowingOne.convertInRadiusToGlow((WorldServer) world, this.posX, this.posY, this.posZ, radius * 1.5);

        //Create Explosion Rays
        if (!initialized) {
            explosionStart = System.currentTimeMillis();
            if (BombConfig.explosionAlgorithm == 1 || BombConfig.explosionAlgorithm == 2)
                explosion = new ExplosionNukeRayParallelized(world, posX, posY, posZ, strength, radius, algorithm);
            else explosion = new ExplosionNukeRayBatched(world, (int) posX, (int) posY, (int) posZ, strength, radius);
            explosion.setDetonator(detonator);
            initialized = true;
        }

        //Calculating crater
        if (!explosion.isComplete()) {
            explosion.update(BombConfig.mk5);
        } else {
            if (GeneralConfig.enableExtendedLogging && explosionStart != 0)
                MainRegistry.logger.log(Level.INFO, "[NUKE] Explosion complete. Time elapsed: {}ms", (System.currentTimeMillis() - explosionStart));
            if (fallout) {
                EntityFalloutRain fallout = new EntityFalloutRain(this.world);
                fallout.posX = this.posX;
                fallout.posY = this.posY;
                fallout.posZ = this.posZ;
                fallout.setScale((int)(this.radius * 2.5 + falloutAdd) * BombConfig.falloutRange / 100);
                this.world.spawnEntity(fallout);
//                EntityFalloutUnderGround falloutBall = new EntityFalloutUnderGround(this.world);
//                falloutBall.posX = this.posX;
//                falloutBall.posY = this.posY;
//                falloutBall.posZ = this.posZ;
//                falloutBall.setScale((int) (this.radius * (BombConfig.falloutRange / 100F) + falloutAdd));
//
//                falloutBall.falloutRainDoFallout = fallout && !explosion.isContained();
//                falloutBall.falloutRainDoFlood = floodPlease;
//                falloutBall.falloutRainFire = spawnFire;
//                falloutBall.falloutRainRadius1 = (int) ((this.radius * 2.5F + falloutAdd) * BombConfig.falloutRange * 0.01F);
//                falloutBall.falloutRainRadius2 = this.radius + 4;
//                this.world.spawnEntity(falloutBall);
            }
            this.setDead();
        }
    }

    private void radiate(List<EntityLivingBase> entities, float rads) {
        MutableVec3d vec = new MutableVec3d();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (EntityLivingBase e : entities) {
            vec.set(e.posX - posX, (e.posY + e.getEyeHeight()) - posY, e.posZ - posZ);
            double len = vec.length();
            if (len <= 0.0001D) continue;
            vec.normalizeSelf();

            double res = 0F;
            int steps = MathHelper.floor(len);
            for (int i = 1; i < steps; i++) {
                int ix = MathHelper.floor(posX + vec.x * i);
                int iy = MathHelper.floor(posY + vec.y * i);
                int iz = MathHelper.floor(posZ + vec.z * i);
                float blockRes = world.getBlockState(pos.setPos(ix, iy, iz)).getBlock().getExplosionResistance(null);
                res += blockRes;
            }

            if (res < 1.0) res = 1.0;
            double eRads = rads;
            eRads /= res;
            eRads /= len * len;
            ContaminationUtil.contaminate(e, HazardType.RADIATION, ContaminationType.RAD_BYPASS, eRads);
        }
    }

    public EntityNukeExplosionMK5 setDetonator(Entity detonator){
        if (detonator instanceof EntityPlayerMP){
            this.detonator = detonator.getUniqueID();
        }
        return this;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        radius = nbt.getInteger("radius");
        strength = nbt.getInteger("strength");
        falloutAdd = nbt.getInteger("falloutAdd");
        fallout = nbt.getBoolean("fallout");
        floodPlease = nbt.getBoolean("floodPlease");
        spawnFire = nbt.getBoolean("spawnFire");
        algorithm = nbt.getInteger("algorithm");
        if (nbt.hasKey("detonator"))
            detonator = nbt.getUniqueId("detonator");
        if (!initialized) {
            if (algorithm == 1 || algorithm == 2)
                explosion = new ExplosionNukeRayParallelized(world, this.posX, this.posY, this.posZ, this.strength, this.radius);
            else
                explosion = new ExplosionNukeRayBatched(world, (int) this.posX, (int) this.posY, (int) this.posZ, this.strength, this.radius);
            explosion.setDetonator(this.detonator);
        }
        explosion.readEntityFromNBT(nbt);
        initialized = true;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setInteger("radius", radius);
        nbt.setInteger("strength", strength);
        nbt.setInteger("falloutAdd", falloutAdd);
        nbt.setBoolean("fallout", fallout);
        nbt.setBoolean("floodPlease", floodPlease);
        nbt.setBoolean("spawnFire", spawnFire);
        nbt.setInteger("algorithm", algorithm);
        if (detonator != null)
            nbt.setUniqueId("detonator", detonator);
        if (explosion != null) {
            explosion.writeEntityToNBT(nbt);
        }
    }

    @Override
    public void setDead() {
        if (explosion != null) explosion.cancel();
        clearChunkLoader();
        super.setDead();
    }

    @Override
    public void onRemovedFromWorld() {
        if (explosion != null) explosion.cancel();
        super.onRemovedFromWorld();
    }

    public EntityNukeExplosionMK5 moreFallout(int fallout) {
        falloutAdd = fallout;
        return this;
    }

    public EntityNukeExplosionMK5 forceSpawn() {
        this.forceSpawn = true;
        return this;
    }
}
