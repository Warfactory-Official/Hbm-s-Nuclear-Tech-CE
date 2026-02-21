package com.hbm.entity.mob.glyphid;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.MobConfig;
import com.hbm.entity.logic.EntityWaypoint;
import com.hbm.entity.mob.glyphid.GlyphidStats.StatBundle;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.world.feature.GlyphidHive;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@AutoRegister(name = "entity_glyphid_scout", eggColors = {0x273038, 0xB9E36B})
public class EntityGlyphidScout extends EntityGlyphid {

    boolean hasTarget = false;
    int timer;
    int scoutingRange = 45;
    int minDistanceToHive = 8;
    boolean useLargeHive = false;
    float largeHiveChance = MobConfig.largeHiveChance;

    public EntityGlyphidScout(World world) {
        super(world);
        this.setSize(1.25F, 0.75F);
    }

    //extreme measures for anti-scout bullying
    @Override
    public boolean attackEntityAsMob(@NotNull Entity victim) {
        if(super.attackEntityAsMob(victim) && victim instanceof EntityLivingBase){
            ((EntityLivingBase)victim).addPotionEffect(new PotionEffect(Objects.requireNonNull(Potion.getPotionFromResourceLocation("poison")), 10 * 20, 3));
            return true;
        }
        return false;
    }

    @Override
    public ResourceLocation getSkin() {
        return ResourceManager.glyphid_scout_tex;
    }

    @Override
    public double getScale() {
        return 0.75D;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(GlyphidStats.getStats().getScout().health());
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(GlyphidStats.getStats().getScout().speed());
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(GlyphidStats.getStats().getScout().damage());
    }

    public StatBundle getStats() {
        return GlyphidStats.getStats().statsScout;
    }

    @Override
    public boolean isArmorBroken(float amount) {
        return this.rand.nextInt(100) <= Math.min(Math.pow(amount, 2), 100);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if((getCurrentTask() != TASK_BUILD_HIVE || getCurrentTask() != TASK_TERRAFORM) && taskWaypoint == null) {

            if(MobConfig.rampantGlyphidGuidance && PollutionHandler.targetCoords != null){
                if(!hasTarget) {
                    Vec3 dirVec = playerBaseDirFinder(Vec3.createVectorHelper(posX, posY, posZ), getPlayerTargetDirection());

                    EntityWaypoint target = new EntityWaypoint(world);
                    target.setLocationAndAngles(dirVec.xCoord, dirVec.yCoord, dirVec.zCoord, 0, 0);
                    target.maxAge = 300;
                    target.radius = 6;
                    target.setWaypointType(TASK_BUILD_HIVE);
                    world.spawnEntity(target);
                    hasTarget = true;

                    setCurrentTask(TASK_RETREAT_FOR_REINFORCEMENTS, target);
                }

                if(super.isAtDestination()) {
                    setCurrentTask(TASK_BUILD_HIVE, null) ;
                    hasTarget = false;
                }

            } else {
                setCurrentTask(TASK_BUILD_HIVE, null);
            }

        }

        if(getCurrentTask() == TASK_BUILD_HIVE || getCurrentTask() == TASK_TERRAFORM) {

            if(!world.isRemote && !hasTarget) {
                //Check for whether a big man johnson is nearby, this makes the scout switch into its terraforming task
                if(scoutingRange != 60 && hasNuclearGlyphidNearby()){
                    setCurrentTask(TASK_TERRAFORM, null);
                }

                if(expandHive()) {
                    this.addPotionEffect(new PotionEffect(Objects.requireNonNull(Potion.getPotionFromResourceLocation("fire_resistance")), 180 * 20, 1));
                    hasTarget = true;
                }
            }
            //fixes edge case where glyphids have no task and yet hasTarget is true
            if(taskWaypoint == null && hasTarget){
                hasTarget = false;
            }

            if (getCurrentTask() == TASK_TERRAFORM && super.isAtDestination() && canBuildHiveHere()) {
                communicate(TASK_TERRAFORM, taskWaypoint);
            }

            if (ticksExisted % 10 == 0 && isAtDestination()) {
                timer++;

                if (!world.isRemote && canBuildHiveHere()) {
                    if(timer == 1) {

                        EntityWaypoint home = getHomeWaypoint();
                        home.maxAge = 1200;
                        home.radius = 6;

                        world.spawnEntity(home);

                        this.taskWaypoint = home;
                        this.addPotionEffect(new PotionEffect(Objects.requireNonNull(Potion.getPotionFromResourceLocation("slowness")), 40 * 20, 10));
                        communicate(TASK_RETREAT_FOR_REINFORCEMENTS, taskWaypoint);

                    } else if(timer >= 5) {

                        world.newExplosion(this, posX, posY, posZ, 5F, false, false);
                        GlyphidHive.generateSmall(world, (int) Math.floor(posX), (int) Math.floor(posY), (int) Math.floor(posZ), rand, this.dataManager.get(SUBTYPE) != TYPE_NORMAL, false);
                        this.setDead();

                    } else {
                        communicate(TASK_FOLLOW, taskWaypoint);
                    }
                }
            }
        }
    }

    private @NotNull EntityWaypoint getHomeWaypoint() {
        EntityWaypoint additional = new EntityWaypoint(world);
        additional.setLocationAndAngles(posX, posY, posZ, 0, 0);
        additional.setWaypointType(TASK_IDLE);

        // First, go home and get reinforcements
        EntityWaypoint home = new EntityWaypoint(world);
        home.setWaypointType(TASK_RETREAT_FOR_REINFORCEMENTS);
        home.setAdditionalWaypoint(additional);
        home.setLocationAndAngles(homeX, homeY, homeZ, 0, 0);
        return home;
    }

    /** Returns true if the position is far enough away from other hives. Also resets the task if unsuccessful. */
    public boolean canBuildHiveHere() {
        int length = useLargeHive ? 16 : 8;

        for(int i = 0; i < 8; i++) {

            float angle = (float) Math.toRadians(360D / 16 * i);
            Vec3 rot = Vec3.createVectorHelper(0, 0, length);
            rot.rotateAroundY(angle);
            Vec3 pos = Vec3.createVectorHelper(this.posX, this.posY + 1, this.posZ);
            Vec3 nextPos = Vec3.createVectorHelper(this.posX + rot.xCoord, this.posY + 1, this.posZ + rot.zCoord);
            RayTraceResult result = this.world.rayTraceBlocks(pos.toVec3d(), nextPos.toVec3d());

            if(result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {

                Block block = world.getBlockState(result.getBlockPos()).getBlock();

                if(block == ModBlocks.glyphid_base) {
                    setCurrentTask(TASK_IDLE, null);
                    hasTarget = false;
                    return false;
                }

            }
        }
        return true;
    }

    @Override
    public boolean isAtDestination() {
        return this.getCurrentTask() == TASK_BUILD_HIVE && super.isAtDestination();
    }

    public boolean hasNuclearGlyphidNearby(){
        int radius = 8;

        AxisAlignedBB bb = new AxisAlignedBB(
                this.posX - radius,
                this.posY - radius,
                this.posZ - radius,
                this.posX + radius,
                this.posY + radius,
                this.posZ + radius);

        List<Entity> bugs = world.getEntitiesWithinAABBExcludingEntity(this, bb);

        for (Entity e: bugs){
            if(e instanceof EntityGlyphidNuclear){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean expandHive() {

        int nestX = rand.nextInt((homeX + scoutingRange) - (homeX - scoutingRange)) + (homeX - scoutingRange);
        int nestZ = rand.nextInt((homeZ + scoutingRange) - (homeZ - scoutingRange)) + (homeZ - scoutingRange);
        int nestY = world.getHeight(nestX, nestZ);
        IBlockState state = world.getBlockState(new BlockPos(nestX, nestY - 1, nestZ));
        Block b = state.getBlock();

        boolean distanceCheck = Vec3.createVectorHelper(nestX - homeX, nestY - homeY, nestZ - homeZ).length() > minDistanceToHive;

        if(distanceCheck && state.getMaterial() != Material.AIR && state.isNormalCube() && b != ModBlocks.glyphid_base) {

            if(b == ModBlocks.basalt) {
                useLargeHive = true;
                largeHiveChance /= 2;
                this.addPotionEffect(new PotionEffect(Objects.requireNonNull(Potion.getPotionFromResourceLocation("speed")), 60 * 20, 3));
            }

            if(!world.isRemote) {
                EntityWaypoint nest = new EntityWaypoint(world);
                nest.setWaypointType(getCurrentTask());
                nest.radius = 5;

                if(useLargeHive)
                    nest.setHighPriority();

                nest.setLocationAndAngles(nestX, nestY, nestZ, 0, 0);
                world.spawnEntity(nest);

                taskWaypoint = nest;

                // updates the task coordinates
                setCurrentTask(getCurrentTask(), taskWaypoint);
                communicate(TASK_BUILD_HIVE, taskWaypoint);
            }

            return true;
        }

        return false;
    }

    @Override
    public void carryOutTask() {
        if (!world.isRemote && taskWaypoint == null) {
            switch(getCurrentTask()){
                case TASK_INITIATE_RETREAT:
                    this.removePotionEffect(Objects.requireNonNull(Potion.getPotionFromResourceLocation("slowness")));
                    this.addPotionEffect(new PotionEffect(Objects.requireNonNull(Potion.getPotionFromResourceLocation("speed")), 20 * 20, 4));

                    //then, come back later
                    EntityWaypoint additional = new EntityWaypoint(world);
                    additional.setLocationAndAngles(posX, posY, posZ, 0, 0);
                    additional.setWaypointType((byte) 0);

                    //First, go home and get reinforcements
                    EntityWaypoint home = new EntityWaypoint(world);
                    home.setWaypointType((byte) 2);
                    home.setAdditionalWaypoint(additional);
                    home.setHighPriority();
                    home.radius = 6;
                    home.setLocationAndAngles(homeX, homeY, homeZ, 0, 0);
                    world.spawnEntity(home);

                    communicate((byte) 4, home);
                    break;

                //terraforming task, only used if a big man johnson is near the scout
                case TASK_TERRAFORM:
                    scoutingRange = 60;
                    minDistanceToHive = 20;
                    break;
            }
        }
        super.carryOutTask();

    }

    @Override
    public boolean useExtendedTargeting() {
        return false;
    }

    @Override
    public EntityLivingBase getAttackTarget() {
        if (this.isBlind()) return null;
        //no extended targeting, and a low attack distance, ensures the scouts are focused in expanding, and not in chasing the player
        return this.world.getNearestAttackablePlayer(this, 10, 5);
    }

    ///RAMPANT MODE STUFFS

    /** Finds the direction from the bug's location to the target and adds it to their current coord
     * Used as a performant way to make scouts expand toward the player's spawn point
     * @return An adjusted direction vector, to be added into the bug's current position for it to path in the required direction**/
    public static Vec3 playerBaseDirFinder(Vec3 currentLocation, Vec3 target){
        Vec3 dirVec = currentLocation.subtract(target).normalize();
        return Vec3.createVectorHelper(
                currentLocation.xCoord + dirVec.xCoord * 10,
                currentLocation.yCoord + dirVec.yCoord * 10,
                currentLocation.zCoord + dirVec.zCoord * 10
        );
    }

    protected Vec3 getPlayerTargetDirection() {
        EntityPlayer player = world.getClosestPlayerToEntity(this, 300);
        if(player != null) {
            return Vec3.createVectorHelper(player.posX, player.posY, player.posZ);
        }
        return PollutionHandler.targetCoords;
    }

    /** Vanilla implementation, minus the RNG */
    @Override
    public boolean isValidLightLevel() {
        int x = MathHelper.floor(this.posX);
        int y = MathHelper.floor(this.getEntityBoundingBox().minY);
        int z = MathHelper.floor(this.posZ);

        int skylightSubtracted = this.world.skylightSubtracted;

        if(this.world.isThundering()) this.world.skylightSubtracted = 10;
        int light = world.getChunk(x >> 4, z >> 4).getLightSubtracted(new BlockPos(x & 15, y, z & 15), world.skylightSubtracted);

        this.world.skylightSubtracted = skylightSubtracted;
        return light <= 7;

    }
}
