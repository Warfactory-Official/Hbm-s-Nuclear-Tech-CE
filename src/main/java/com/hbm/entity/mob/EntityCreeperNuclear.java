package com.hbm.entity.mob;

import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.explosion.ExplosionNukeSmall;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.OreDictManager;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.factory.GunFactory;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.AdvancementManager;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.ContaminationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AutoRegister(name = "entity_nuclear_creeper", trackingRange = 80, eggColors = {0x204131, 0x75CE00})
public class EntityCreeperNuclear extends EntityCreeper {

	public EntityCreeperNuclear(World worldIn) {
		super(worldIn);
		this.fuseTime = 75;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
	}

    @Nullable
    protected ResourceLocation getLootTable() {
        return null;
    }

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
        // for some reason the nuclear explosion would damage the already dead entity, reviving it and forcing it to play the death animation
        if(this.isDead) return false;

		if (source == ModDamageSource.radiation || source == ModDamageSource.mudPoisoning) {
            if(this.isEntityAlive()) this.heal(amount);
			return false;
		}
		return super.attackEntityFrom(source, amount);
	}

	@Override
	public void onUpdate() {
        if (!world.isRemote) {
            List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow(5, 5, 5));

            for (Entity e : list) {
                if (e instanceof EntityLivingBase livingBase) {
                    ContaminationUtil.contaminate(livingBase, ContaminationUtil.HazardType.RADIATION, ContaminationUtil.ContaminationType.CREATIVE, 0.25F);
                }
            }
        }
		super.onUpdate();
        if (!world.isRemote && this.isEntityAlive() && this.getHealth() < this.getMaxHealth() && this.ticksExisted % 10 == 0) {
            this.heal(1.0F);
        }
	}

    @Override
    protected Item getDropItem() {
        return Item.getItemFromBlock(Blocks.TNT);
    }

	@Override
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		super.dropFewItems(wasRecentlyHit, lootingModifier);

		if (rand.nextInt(3) == 0)
			this.dropItem(ModItems.coin_creeper, 1);
	}

	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);

		List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow(50, 50, 50));
		for (EntityPlayer player : players) {
			AdvancementManager.grantAchievement(player, AdvancementManager.bossCreeper);
		}
		if (cause.getTrueSource() instanceof EntitySkeleton || (cause.isProjectile() && cause.getImmediateSource() instanceof EntityArrow arrow && arrow.shootingEntity == null)) {
            this.entityDropItem(OreDictManager.DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.NUKE_STANDARD), 1);
		}
	}

	@Override
	protected void explode() {
		if (!this.world.isRemote) {
			boolean flag = ForgeEventFactory.getMobGriefingEvent(this.world, this);

			if (this.getPowered()) {
                NBTTagCompound data = new NBTTagCompound();
                data.setString("type", "muke");
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, posX, posY + 0.5, posZ), new NetworkRegistry.TargetPoint(dimension, posX, posY, posZ, 250));
                world.playSound(null, posX, posY + 0.5, posZ, HBMSoundHandler.mukeExplosion, SoundCategory.HOSTILE, 15.0F, 1.0F);

                if (flag) {
					world.spawnEntity(EntityNukeExplosionMK5.statFac(world, 50, posX, posY, posZ).setDetonator(this));
				} else {
                    ExplosionNukeGeneric.dealDamage(world, posX, posY + 0.5, posZ, 100);
				}
			} else {
				if (flag) {
                    ExplosionNukeSmall.explode(world, posX, posY + 0.5, posZ, ExplosionNukeSmall.PARAMS_MEDIUM);
				} else {
                    ExplosionNukeSmall.explode(world, posX, posY + 0.5, posZ, ExplosionNukeSmall.PARAMS_SAFE);
                }
			}

			this.setDead();
		}
	}

	public void setPowered(boolean power) {
		this.dataManager.set(POWERED, power);
	}
}
