package com.hbm.entity.mob.botprime;

import com.hbm.entity.mob.ai.EntityAINearestAttackableTargetNT;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.main.AdvancementManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import java.util.List;
@AutoRegister(name = "entity_balls_o_tron", trackingRange = 1000)
public class EntityBOTPrimeHead extends EntityBOTPrimeBase {

	/*   ___   _   _   _   ___           ___           _____ ___  ___ _  _       ___ ___  _ _   _ ___
	 *  | _ ) /_\ | | | | / __|   ___   |   |   ___   |_   _| _ )|   | \| |     | _ \ _ )| | \ / | __|
	 *  | _ \/ _ \| |_| |_\__ \  |___|  | | |  |___|    | | |   \| | |    |     |  _/   \| |  V  | _|
	 *  |___/_/ \_\___|___|___/         |___|           |_| |_|\_\___|_|\_|     |_| |_|\_\_|_|V|_|___|
	 */
	
	//TODO: clean-room implementation of the movement behavior classes (again)

	private final BossInfoServer bossInfo = (BossInfoServer)(new BossInfoServer(this.getDisplayName(), BossInfo.Color.GREEN, BossInfo.Overlay.PROGRESS));
	private final WormMovementHeadNT movement = new WormMovementHeadNT(this);
	
	public EntityBOTPrimeHead(World world) {
		super(world);
		this.experienceValue = 1000;
		this.wasNearGround = false;
		this.attackRange = 150.0D;
		this.setSize(3.0F, 3.0F);
		this.maxSpeed = 1.0D;
		this.fallSpeed = 0.006D;
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTargetNT(this, EntityPlayer.class, 0, false, false, null, 128.0D));
		this.ignoreFrustumCheck = true;
	}
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.15D);
	}
	
	@Override
	public boolean getIsHead() {
		return true;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if(super.attackEntityFrom(source, amount)) {
			this.dmgCooldown = 4;
			return true;
		}

		return false;
	}
	
	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
		//TODO: check if this is even needed
    	setHeadID(this.getEntityId());

    	int x = MathHelper.floor(this.posX);
        int y = MathHelper.floor(this.posY);
        int z = MathHelper.floor(this.posZ);

        for (int i = 0; i < 74; i++) {

          EntityBOTPrimeBody bodyPart = new EntityBOTPrimeBody(this.world);
          bodyPart.setPartNumber(i);
          bodyPart.setPosition(x, y, z);
          bodyPart.setHeadID(getEntityId());
          this.world.spawnEntity(bodyPart);
        }

        setPosition(x, y, z);
        this.spawnPoint = new BlockPos(x, y, z);

        return super.onInitialSpawn(difficulty, livingdata);
	}
	
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
	}
	
	@Override
	public void addTrackingPlayer(EntityPlayerMP player) {
		super.addTrackingPlayer(player);
		bossInfo.addPlayer(player);
	}
	
	@Override
	public void removeTrackingPlayer(EntityPlayerMP player) {
		super.removeTrackingPlayer(player);
		bossInfo.removePlayer(player);
	}
	
	@Override
	protected void updateAITasks() {
		super.updateAITasks();
		this.movement.updateMovement();

		if((getHealth() < getMaxHealth()) && (this.ticksExisted % 6 == 0)) {
			if(this.targetedEntity != null) {
				heal(1.0F);
			} else if(this.recentlyHit == 0) {
				heal(4.0F);
			}
		}

		if((this.targetedEntity != null) && (this.targetedEntity.getDistanceSq(this) < this.attackRange * this.attackRange)) {
			if(canEntityBeSeen(this.targetedEntity)) {

				this.attackCounter ++;

				if(this.attackCounter == 30) {

					laserAttack(this.targetedEntity, true);
					this.attackCounter = 0;
				}
			} else {
				this.attackCounter = 0;
			}
		} else {
			this.attackCounter = 0;
		}
	}

	protected void onDeathUpdate() {

		if(this.deathTime == 19 && !world.isRemote) {

			List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow(200, 200, 200));
			for(EntityPlayer player : players) {
				AdvancementManager.grantAchievement(player, AdvancementManager.bossWorm);
				player.inventory.addItemStackToInventory(new ItemStack(ModItems.coin_worm));
			}
		}
		
		super.onDeathUpdate();
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		double dx = motionX;
		double dy = motionY;
		double dz = motionZ;
		float f3 = MathHelper.sqrt(dx * dx + dz * dz);
		this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(dx, dz) * 180.0D / Math.PI);
		this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(dy, f3) * 180.0D / Math.PI);
	}
	
	@Override
	public float getAttackStrength(Entity target) {
		return 1000;
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("spawnX", this.spawnPoint.getX());
		compound.setInteger("spawnY", this.spawnPoint.getY());
		compound.setInteger("spawnZ", this.spawnPoint.getZ());
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
	    this.spawnPoint = new BlockPos(compound.getInteger("spawnX"), compound.getInteger("spawnY"), compound.getInteger("spawnZ"));
	}
}
