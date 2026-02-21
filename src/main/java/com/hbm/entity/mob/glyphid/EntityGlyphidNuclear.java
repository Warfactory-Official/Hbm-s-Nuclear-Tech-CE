package com.hbm.entity.mob.glyphid;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.logic.EntityWaypoint;
import com.hbm.entity.mob.EntityParasiteMaggot;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.*;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import com.hbm.entity.mob.glyphid.GlyphidStats.StatBundle;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@AutoRegister(name = "entity_glyphid_nuclear", eggColors = {0x267F00, 0xA0A0A0})
public class EntityGlyphidNuclear extends EntityGlyphid {

    public int deathTicks;
    public EntityGlyphidNuclear(World world) {
        super(world);
        this.setSize(2.5F, 1.75F);
        this.isImmuneToFire = true;
    }

    @Override
    public ResourceLocation getSkin() {
        return ResourceManager.glyphid_nuclear_tex;
    }

    @Override
    public double getScale() {
        return 2D;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(GlyphidStats.getStats().getNuclear().health());
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(GlyphidStats.getStats().getNuclear().speed());
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(GlyphidStats.getStats().getNuclear().damage());
    }

    public StatBundle getStats() {
        return GlyphidStats.getStats().statsNuclear;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if(ticksExisted % 20 == 0) {
            if(isAtDestination() && getCurrentTask() == TASK_FOLLOW) {
                setCurrentTask(TASK_IDLE, null);
            }

            if(getCurrentTask() == TASK_BUILD_HIVE && getAttackTarget() == null) {
                this.addPotionEffect(new PotionEffect(Objects.requireNonNull(Potion.getPotionFromResourceLocation("speed")), 10 * 20, 3));
            }

            if(getCurrentTask() == TASK_TERRAFORM) {
                this.setHealth(0);
            }
        }
    }

    /** Communicates only with glyphid scouts, unlike the super implementation which does the opposite */
    @Override
    public void communicate(byte task, @Nullable EntityWaypoint waypoint) {
        int radius = waypoint != null ? waypoint.radius : 4;

        AxisAlignedBB bb = new AxisAlignedBB(
                this.posX - radius,
                this.posY - radius,
                this.posZ - radius,
                this.posX + radius,
                this.posY + radius,
                this.posZ + radius);

        List<Entity> bugs = world.getEntitiesWithinAABBExcludingEntity(this, bb);
        for (Entity e: bugs){
            if(e instanceof EntityGlyphidScout){
                if(((EntityGlyphid) e).getCurrentTask() != task){
                    ((EntityGlyphid) e).setCurrentTask(task, waypoint);
                }
            }
        }
    }

    @Override
    public boolean isArmorBroken(float amount) {
        return this.rand.nextInt(100) <= Math.min(Math.pow(amount * 0.12, 2), 100);
    }

    @Override
    public boolean doesInfectedSpawnMaggots() {
        return false;
    }

    public boolean hasWaypoint = false;
    @Override
    protected void onDeathUpdate() {
        ++this.deathTicks;

        if(!hasWaypoint) {
            // effectively causes neighboring EntityGlyphidScout to retreat
            communicate(TASK_INITIATE_RETREAT, null);
            hasWaypoint = true;
        }

        if(deathTicks == 90){
            int radius = 8;
            AxisAlignedBB bb = new AxisAlignedBB(this.posX, this.posY, this.posZ, this.posX, this.posY, this.posZ).expand(radius, radius, radius);

            List<Entity> bugs = world.getEntitiesWithinAABBExcludingEntity(this, bb);
            for (Entity e: bugs){
                if(e instanceof EntityGlyphid){
                    addPotionEffect(new PotionEffect(Objects.requireNonNull(Potion.getPotionFromResourceLocation("resistance")), 20, 6));
                    addPotionEffect(new PotionEffect(Objects.requireNonNull(Potion.getPotionFromResourceLocation("fire_resistance")), 15 * 20, 1));
                }
            }
        }
        if(this.deathTicks == 100) {

            if(!world.isRemote) {
                ExplosionVNT vnt = new ExplosionVNT(world, posX, posY, posZ, 25, this);

                if(this.dataManager.get(SUBTYPE) == TYPE_INFECTED) {
                    int j = 15 + this.rand.nextInt(6);
                    for(int k = 0; k < j; ++k) {
                        float f = ((float) (k % 2) - 0.5F) * 0.5F;
                        float f1 = ((float) (k / 2) - 0.5F) * 0.5F;
                        EntityParasiteMaggot maggot = new EntityParasiteMaggot(world);
                        maggot.setLocationAndAngles(this.posX + (double) f, this.posY + 0.5D, this.posZ + (double) f1, this.rand.nextFloat() * 360.0F, 0.0F);
                        maggot.motionX = f;
                        maggot.motionZ = f1;
                        maggot.velocityChanged = true;
                        this.world.spawnEntity(maggot);
                    }
                } else {
                    vnt.setBlockAllocator(new BlockAllocatorStandard(24));
                    vnt.setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorDebris(ModBlocks.volcanic_lava_block, 0)).setNoDrop());
                }

                vnt.setEntityProcessor(new EntityProcessorStandard());
                vnt.setPlayerProcessor(new PlayerProcessorStandard());
                vnt.explode();

                world.playSound(null, getPosition(), HBMSoundHandler.mukeExplosion, SoundCategory.HOSTILE, 15.0F, 1.0F);

                NBTTagCompound data = new NBTTagCompound();
                data.setString("type", "muke");
                // if the FX type is "muke", apply random BF effect
                if(MainRegistry.polaroidID == 11 || rand.nextInt(100) == 0) {
                    data.setBoolean("balefire", true);
                }
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, posX, posY + 0.5, posZ), new NetworkRegistry.TargetPoint(dimension, posX, posY, posZ, 250));
            }

            this.setDead();
        } else {
            if(!world.isRemote && this.deathTicks % 10 == 0) {
                world.playSound(null, getPosition(), HBMSoundHandler.fstbmbPing, SoundCategory.HOSTILE, 5.0F, 1.0F);
            }
        }
    }
}
