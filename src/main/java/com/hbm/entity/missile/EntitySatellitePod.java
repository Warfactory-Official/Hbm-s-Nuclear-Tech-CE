package com.hbm.entity.missile;

import com.hbm.api.entity.IRadarDetectableNT;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.projectile.EntityThrowableInterp;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.interfaces.AutoRegister;
import com.hbm.tileentity.machine.TileEntityMachineSatDock;
import com.hbm.tileentity.turret.TileEntityTurretBaseNT;
import com.hbm.util.InventoryUtil;
import com.hbm.util.ParticleUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

@AutoRegister(name = "entity_satellite_pod", trackingRange = 1000)
public class EntitySatellitePod extends EntityThrowableInterp implements IRadarDetectableNT {

	public static final DataParameter<Integer> STATE = EntityDataManager.createKey(EntitySatellitePod.class, DataSerializers.VARINT);
	public static final int STATE_LEGS_UP = 0;
	public static final int STATE_LEGS_DOWN = 1;

	public ItemStackHandler inventory = new ItemStackHandler(0);

	public int timer = 0;
	public int callerYPos;
	public double speed = 0.75D;

	public float legs = 0F;
	public float prevLegs = 0F;
	public static final float LEG_SPEED = 1F / 20F;

	public EntitySatellitePod(World world) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
		this.setSize(0.95F, 5.25F);
	}

	public EntitySatellitePod setup(int caller, ItemStackHandler cargo) {
		this.callerYPos = caller;
		this.inventory = new ItemStackHandler(cargo.getSlots());
		for(int i = 0; i < cargo.getSlots(); i++) this.inventory.setStackInSlot(i, cargo.getStackInSlot(i).copy());
		return this;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(STATE, 0);
	}

	public boolean doesDeployLegs() {
		return this.getDataManager().get(STATE) == STATE_LEGS_DOWN;
	}

	public void setDeployLegs(boolean deploy) {
		this.getDataManager().set(STATE, deploy ? STATE_LEGS_DOWN : STATE_LEGS_UP);
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {

		if(this.isEntityInvulnerable(source)) return false;

		if(amount >= 5F && !this.world.isRemote && !this.isDead) {
			this.setDead();

			ExplosionVNT xnt = new ExplosionVNT(world, posX, posY + 1.5, posZ, 15F);
			xnt.setEntityProcessor(new EntityProcessorCrossSmooth(1D, 50));
			xnt.setPlayerProcessor(new PlayerProcessorStandard());
			xnt.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
			xnt.explode();

			if(this.motionY != 0) ExplosionLarge.spawnShrapnelShower(world, posX, posY + 1.5, posZ, motionX, motionY, motionZ, 15, 0.25);
		}

		return true;
	}

	@Override
	public void onUpdate() {

		if(!world.isRemote) {

			if(this.isLanding()) {

				if(this.timer > 0) {
					this.timer++;

					this.posY = Math.ceil(posY);

					if(this.timer >= 100) {
						this.unloadItems();
					}

				} else if(this.onGround) {
					this.speed = 0D;
					this.timer = 1;
					this.setDeployLegs(true);

				} else {
					if(this.posY < this.callerYPos + 17 && !this.doesDeployLegs()) this.setDeployLegs(true);
					if(this.posY < this.callerYPos + 25) this.speed -= 0.01;
					this.speed = MathHelper.clamp(this.speed, 0.025D, 0.75D);
				}

				this.motionY = -this.speed;

			} else {

				this.onGround = false;

				this.speed += 0.01;
				if(this.speed >= 0.2) this.setDeployLegs(false);
				this.speed = MathHelper.clamp(this.speed, 0D, 2D);
				this.motionY = this.speed;

				if(this.posY > 300) this.setDead();
			}

		} else {

			this.prevLegs = this.legs;

			if(doesDeployLegs()) {
				this.legs += LEG_SPEED;
			} else {
				this.legs -= LEG_SPEED;
			}

			this.legs = MathHelper.clamp(this.legs, 0F, 1F);

			if(this.legs > 0 && this.motionY < 0 || this.motionY > 0) {
				ParticleUtil.spawnGasFlame(world, posX, posY + 0.5, posZ, 0, this.speed - 1, 0);
			}
		}

		super.onUpdate();
	}

	/** Tries to fill a sat dock that this pod is standing on. All items that cannot be added to a dock will be spilled and removed from the pod's inventory. */
	public void unloadItems() {

		BlockPos below = new BlockPos(MathHelper.floor(posX), MathHelper.floor(posY - 0.5), MathHelper.floor(posZ));

		if(world.getBlockState(below).getBlock() == ModBlocks.sat_dock) {
			TileEntity tile = world.getTileEntity(below);
			if(tile instanceof TileEntityMachineSatDock dock) {
				for(int i = 0; i < this.inventory.getSlots(); i++) {
					ItemStack stack = this.inventory.getStackInSlot(i);
					if(stack.isEmpty()) continue;
					this.inventory.setStackInSlot(i, InventoryUtil.tryAddItemToInventory(dock.inventory, 0, 14, stack));
				}
			}
		}

		for(int i = 0; i < this.inventory.getSlots(); i++) {
			ItemStack stack = this.inventory.getStackInSlot(i);
			if(stack.isEmpty()) continue;
			this.entityDropItem(stack, 0.25F);
		}

		this.inventory = new ItemStackHandler(0);
	}

	public boolean isLanding() {
		return this.inventory.getSlots() > 0;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.getDataManager().set(STATE, nbt.getInteger("state"));
		timer = nbt.getInteger("timer");
		callerYPos = nbt.getInteger("callerYPos");
		speed = nbt.getDouble("speed");

		this.inventory = new ItemStackHandler(0);
		if(nbt.hasKey("inventory")) this.inventory.deserializeNBT(nbt.getCompoundTag("inventory"));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setInteger("state", this.getDataManager().get(STATE));
		nbt.setInteger("timer", timer);
		nbt.setInteger("callerYPos", callerYPos);
		nbt.setDouble("speed", speed);
		nbt.setTag("inventory", this.inventory.serializeNBT());
	}

	@Override
	protected void onImpact(RayTraceResult mop) {

		if(mop.typeOfHit == RayTraceResult.Type.BLOCK) {
			this.setPosition(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
			this.onGround = true;

			if(this.speed < -0.02) {
				this.attackEntityFrom(DamageSource.GENERIC, 10F);
			}
		}
	}

	@Override protected boolean canTriggerWalking() { return false; }
	@Override public boolean doesImpactEntities() { return false; }

	@Override protected float getAirDrag() { return 1F; }
	@Override protected float getWaterDrag() { return 1F; }
	@Override public float getGravityVelocity() { return 0F; }

	@Override public String getTranslationKey() { return "radar.target.dropship"; }
	@Override public int getBlipLevel() { return IRadarDetectableNT.SPECIAL; }
	@Override public boolean canBeSeenBy(Object radar) { return !(radar instanceof TileEntityTurretBaseNT); }
	@Override public boolean paramsApplicable(RadarScanParams params) { return params.scanMissiles; }
	@Override public boolean suppliesRedstone(RadarScanParams params) { return false; }
}
