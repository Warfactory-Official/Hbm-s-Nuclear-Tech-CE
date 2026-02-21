package com.hbm.tileentity.turret;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.entity.IRadarDetectableNT;
import com.hbm.blocks.BlockDummyable;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.entity.logic.EntityBomber;
import com.hbm.entity.missile.EntityMissileBaseNT;
import com.hbm.entity.missile.EntityMissileCustom;
import com.hbm.entity.projectile.EntityArtilleryShell;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.handler.CasingEjector;
import com.hbm.handler.CompatHandler;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.control_panel.ControlEvent;
import com.hbm.inventory.control_panel.ControlEventSystem;
import com.hbm.inventory.control_panel.IControllable;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemTurretBiometry;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.SpentCasing;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.CompatExternal;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
public abstract class TileEntityTurretBaseNT extends TileEntityMachineBase implements IEnergyReceiverMK2, IControllable, IControlReceiver, ITickable, SimpleComponent, CompatHandler.OCComponent {

	@Override
	public boolean hasPermission(EntityPlayer player){
		return this.isUseableByPlayer(player);
	}
	
	@Override
	public void receiveControl(NBTTagCompound data){
		if(data.hasKey("del")) {
			this.removeName(data.getInteger("del"));
			
		} else if(data.hasKey("name")) {
			this.addName(data.getString("name"));
		}
	}
	
	//this time we do all rotations in radians
	//what way are we facing?
	public double rotationYaw;
	public double rotationPitch;
	//only used by clients for interpolation
	public double lastRotationYaw;
	public double lastRotationPitch;
	//only used by client for approach
	public double syncRotationYaw;
	public double syncRotationPitch;
	protected int turnProgress;
	//is the turret on?
	public boolean isOn = false;
	//is the turret aimed at the target?
	public boolean aligned = false;
	//how many ticks until the next check
	public int searchTimer;

	public long power;

	public boolean targetPlayers = false;
	public boolean targetAnimals = false;
	public boolean targetMobs = true;
	public boolean targetMachines = true;

	public boolean manualOverride = false;

	public Entity target;
	public Vec3d tPos;

	//tally marks!
	public int stattrak;
	public int casingDelay;
	protected SpentCasing cachedCasingConfig = null;

	/**
	 * X
	 * 
	 * YYY YYY YYY Z
	 * 
	 * X -> ai slot (0) Y -> ammo slots (1 - 9) Z -> battery slot (10)
	 */
	
	public TileEntityTurretBaseNT(){
		super(11);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt){
		this.power = nbt.getLong("power");
		this.isOn = nbt.getBoolean("isOn");
		this.targetPlayers = nbt.getBoolean("targetPlayers");
		this.targetAnimals = nbt.getBoolean("targetAnimals");
		this.targetMobs = nbt.getBoolean("targetMobs");
		this.targetMachines = nbt.getBoolean("targetMachines");
		this.stattrak = nbt.getInteger("stattrak");
		super.readFromNBT(nbt);
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt){
		nbt.setLong("power", this.power);
		nbt.setBoolean("isOn", this.isOn);
		nbt.setBoolean("targetPlayers", this.targetPlayers);
		nbt.setBoolean("targetAnimals", this.targetAnimals);
		nbt.setBoolean("targetMobs", this.targetMobs);
		nbt.setBoolean("targetMachines", this.targetMachines);
		nbt.setInteger("stattrak", this.stattrak);
		return super.writeToNBT(nbt);
	}
	
	public void manualSetup() { }
	
	@Override
	public void update(){
		if(world.isRemote) {
			this.lastRotationPitch = this.rotationPitch;
			this.lastRotationYaw = this.rotationYaw;
			this.rotationPitch = this.syncRotationPitch;
			this.rotationYaw = this.syncRotationYaw;
		}

		this.aligned = false;
		
		if(!world.isRemote) {
			this.updateConnections();
			//Target is dead - start searching
			if(this.target != null && !target.isEntityAlive()) {
				this.target = null;
				this.stattrak++;
			}
		}
		
		//check if we can see target
		if(target != null) {
			if(!this.entityInLOS(this.target)) {
				this.target = null;
			}
		}
		
		if(!world.isRemote) {
			
			if(target != null) {
				this.tPos = this.getEntityPos(target);
			} else if(!manualOverride){
				this.tPos = null;
			}
		}
		
		if(isOn() && hasPower()) {
			
			if(tPos != null)
				this.alignTurret();
		} else {

			this.target = null;
			this.tPos = null;
		}
		
		if(!world.isRemote) {
			
			if(this.target != null && !target.isEntityAlive() && !manualOverride) {
				this.target = null;
				this.tPos = null;
				this.stattrak++;
			}
			
			if(isOn() && hasPower()) {
				searchTimer--;
				
				this.setPower(this.getPower() - this.getConsumption());
				
				if(searchTimer <= 0) {
					searchTimer = this.getDecetorInterval();
					
					if(this.target == null && !manualOverride)
						this.seekNewTarget();
				}
			} else {
				searchTimer = 0;
			}
			
			if(this.aligned) {
				this.updateFiringTick();
			}
			
			this.power = Library.chargeTEFromItems(inventory, 10, this.power, this.getMaxPower());
			manualOverride = false;
			networkPackNT(250);

			if(usesCasings() && this.casingDelay() > 0) {
				if(casingDelay > 0) {
					casingDelay--;
				} else {
					spawnCasing();
				}
			}
			
		} else {

            //this will fix the interpolation error when the turret crosses the 360° point
			if(Math.abs(this.lastRotationYaw - this.rotationYaw) > Math.PI) {
				
				if(this.lastRotationYaw < this.rotationYaw)
					this.lastRotationYaw += Math.PI * 2;
				else
					this.lastRotationYaw -= Math.PI * 2;
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		BufferUtil.writeVec3(buf, this.tPos);
		buf.writeDouble(this.rotationPitch);
		buf.writeDouble(this.rotationYaw);
		buf.writeLong(this.power);
		buf.writeBoolean(this.isOn);
		buf.writeBoolean(this.targetPlayers);
		buf.writeBoolean(this.targetAnimals);
		buf.writeBoolean(this.targetMobs);
		buf.writeBoolean(this.targetMachines);
		buf.writeInt(this.stattrak);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.turnProgress = 2;
		this.tPos = BufferUtil.readVec3(buf);
		this.syncRotationPitch = buf.readDouble();
		this.syncRotationYaw = buf.readDouble();
		this.power = buf.readLong();
		this.isOn = buf.readBoolean();
		this.targetPlayers = buf.readBoolean();
		this.targetAnimals = buf.readBoolean();
		this.targetMobs = buf.readBoolean();
		this.targetMachines = buf.readBoolean();
		this.stattrak = buf.readInt();

	}
	
	@Override
	public void handleButtonPacket(int value, int meta){
		switch(meta) {
		case 0:this.isOn = !this.isOn; break;
		case 1:this.targetPlayers = !this.targetPlayers; break;
		case 2:this.targetAnimals = !this.targetAnimals; break;
		case 3:this.targetMobs = !this.targetMobs; break;
		case 4:this.targetMachines = !this.targetMachines; break;
		}
	}

	protected void updateConnections() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		//how did i even make this? what???
		this.trySubscribe(world, pos.getX() + dir.offsetX * -1, pos.getY(), pos.getZ() + dir.offsetZ * -1, dir.getOpposite());
		this.trySubscribe(world, pos.getX() + dir.offsetX * -1 + rot.offsetX * -1, pos.getY(), pos.getZ() + dir.offsetZ * -1 + rot.offsetZ * -1, dir.getOpposite());

		this.trySubscribe(world, pos.getX() + rot.offsetX * -2, pos.getY(), pos.getZ() + rot.offsetZ * -2, rot.getOpposite());
		this.trySubscribe(world, pos.getX() + dir.offsetX * 1 + rot.offsetX * -2, pos.getY(), pos.getZ() + dir.offsetZ * 1 + rot.offsetZ * -2, rot.getOpposite());

		this.trySubscribe(world, pos.getX() + rot.offsetX * 1, pos.getY(), pos.getZ() + rot.offsetZ * 1, rot);
		this.trySubscribe(world, pos.getX() + dir.offsetX * 1 + rot.offsetX * 1, pos.getY(), pos.getZ() + dir.offsetZ * 1 + rot.offsetZ * 1, rot);

		this.trySubscribe(world, pos.getX() + dir.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 2, dir);
		this.trySubscribe(world, pos.getX() + dir.offsetX * 2 + rot.offsetX * -1, pos.getY(), pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * -1, dir);

		//Down
		this.trySubscribe(world, pos.getX() + 0, pos.getY() - 1, pos.getZ(), ForgeDirection.DOWN);
		this.trySubscribe(world, pos.getX() + 0, pos.getY() - 1, pos.getZ() + dir.offsetZ-rot.offsetZ, ForgeDirection.DOWN);
		this.trySubscribe(world, pos.getX() + dir.offsetX-rot.offsetX, pos.getY() - 1, pos.getZ(), ForgeDirection.DOWN);
		this.trySubscribe(world, pos.getX() + dir.offsetX-rot.offsetX, pos.getY() - 1, pos.getZ() + dir.offsetZ-rot.offsetZ, ForgeDirection.DOWN);
	}
	
	public abstract void updateFiringTick();
	
	public BulletConfig getFirstConfigLoaded() {
		
		List<Integer> list = getAmmoList();
		
		if(list == null || list.isEmpty())
			return null;
		
		//doing it like this will fire slots in the right order, not in the order of the configs
		//you know, the weird thing the IItemGunBase does
		for(int i = 1; i < 10; i++) {
			
			if(!inventory.getStackInSlot(i).isEmpty()) {
				
				for(Integer c : list) { //we can afford all this extra iteration trash on the count that a turret has at most like 4 bullet configs

					BulletConfig conf = BulletConfig.configs.get(c);
					if(conf.ammo != null && conf.ammo.matchesRecipe(inventory.getStackInSlot(i), true)) return conf;
				}
			}
		}
		
		return null;
	}
	
	public void spawnBullet(BulletConfig bullet, float baseDamage) {
		
		Vec3 pos = new Vec3(this.getTurretPos());
		Vec3 vec = Vec3.createVectorHelper(this.getBarrelLength(), 0, 0);
		vec.rotateAroundZ((float) -this.rotationPitch);
		vec.rotateAroundY((float) -(this.rotationYaw + Math.PI * 0.5));

		EntityBulletBaseMK4 proj = new EntityBulletBaseMK4(world, bullet, baseDamage, bullet.spread, (float) rotationYaw, (float) rotationPitch);
		proj.setPositionAndRotation(pos.xCoord + vec.xCoord, pos.yCoord + vec.yCoord, pos.zCoord + vec.zCoord, proj.rotationYaw, proj.rotationPitch);
		world.spawnEntity(proj);

		if(usesCasings()) {
			if(this.casingDelay() == 0) {
				spawnCasing();
			} else {
				casingDelay = this.casingDelay();
			}
		}
	}

	public void consumeAmmo(RecipesCommon.ComparableStack ammo) {

		for(int i = 1; i < 10; i++) {

			if(!inventory.getStackInSlot(i).isEmpty() && ammo.matchesRecipe(inventory.getStackInSlot(i), true)) {

				inventory.getStackInSlot(i).shrink(1);
				return;
			}
		}

		this.markDirty();
	}

	
	/**
	 * Reads the namelist from the AI chip in slot 0
	 * @return null if there is either no chip to be found or if the name list is empty, otherwise it just reads the strings from the chip's NBT
	 */
	public List<String> getWhitelist() {
		
		if(inventory.getStackInSlot(0).getItem() == ModItems.turret_chip) {
			
			String[] array = ItemTurretBiometry.getNames(inventory.getStackInSlot(0));
			
			if(array == null)
				return null;
			
			return Arrays.asList(ItemTurretBiometry.getNames(inventory.getStackInSlot(0)));
		}
		
		return null;
	}
	
	/**
	 * Appends a new name to the chip
	 * @param name
	 */
	public void addName(String name) {
		
		if(inventory.getStackInSlot(0).getItem() == ModItems.turret_chip) {
			ItemTurretBiometry.addName(inventory.getStackInSlot(0), name);
		}
	}
	
	/**
	 * Removes the chip's entry at a given 
	 * @param index
	 */
	public void removeName(int index) {
		
		if(inventory.getStackInSlot(0).getItem() == ModItems.turret_chip) {
			
			String[] array = ItemTurretBiometry.getNames(inventory.getStackInSlot(0));
			
			if(array == null)
				return;
			
			List<String> names = new ArrayList<>(Arrays.asList(array));
			ItemTurretBiometry.clearNames(inventory.getStackInSlot(0));
			
			names.remove(index);
			
			for(String name : names)
				ItemTurretBiometry.addName(inventory.getStackInSlot(0), name);
		}
	}
	
	/**
	 * Finds the nearest acceptable target within range and in line of sight
	 */
	protected void seekNewTarget() {
		
		Vec3d pos = this.getTurretPos();
		double range = this.getDecetorRange();
		List<Entity> allEntities = world.getEntitiesWithinAABB(Entity.class,
				new AxisAlignedBB(pos.x, pos.y, pos.z, pos.x, pos.y, pos.z).grow(range, range, range));

		List<Entity> entities = new ArrayList<>(allEntities.size()); // Pre-size the list to avoid resizes
		for (Entity e : allEntities) {
			if (!(e instanceof EntityArtilleryShell)) {
				entities.add(e);
			}
		}
		Entity target = null;
		double closest = range;
		
		for(Entity entity : entities) {

			Vec3d ent = this.getEntityPos(entity);
			Vec3d delta = new Vec3d(ent.x - pos.x, ent.y - pos.y, ent.z - pos.z);
			
			double dist = delta.length();
			
			//check if it's in range
			if(dist > range)
				continue;
			
			//check if we should even fire at this entity
			if(!entityAcceptableTarget(entity))
				continue;
			
			//check for visibility
			if(!entityInLOS(entity))
				continue;
			
			//replace current target if this one is closer
			if(dist < closest) {
				closest = dist;
				target = entity;
			}
		}
		
		this.target = target;
		
		if(target != null)
			this.tPos = this.getEntityPos(this.target);
	}
	
	/**
	 * Turns the turret by a specific amount of degrees towards the target
	 * Assumes that the target is not null
	 */
	protected void alignTurret() {
		this.turnTowards(tPos);
	}
	
	/**
	 * Turns the turret towards the specified position
	 */
	public void turnTowards(Vec3d ent) {

		Vec3d pos = this.getTurretPos();
		Vec3d delta = new Vec3d(ent.x - pos.x, ent.y - pos.y, ent.z - pos.z);

		double targetPitch = Math.asin(delta.y / delta.length());
		double targetYaw = -Math.atan2(delta.x, delta.z);

		this.turnTowardsAngle(targetPitch, targetYaw);
	}

	public void turnTowardsAngle(double targetPitch, double targetYaw) {

		double turnYaw = Math.toRadians(this.getTurretYawSpeed());
		double turnPitch = Math.toRadians(this.getTurretPitchSpeed());
		double pi2 = Math.PI * 2;

		//if we are about to overshoot the target by turning, just snap to the correct rotation
		if(Math.abs(this.rotationPitch - targetPitch) < turnPitch || Math.abs(this.rotationPitch - targetPitch) > pi2 - turnPitch) {
			this.rotationPitch = targetPitch;
		} else {

			if(targetPitch > this.rotationPitch)
				this.rotationPitch += turnPitch;
			else
				this.rotationPitch -= turnPitch;
		}

		double deltaYaw = (targetYaw - this.rotationYaw) % pi2;

		//determines what direction the turret should turn
		//used to prevent situations where the turret would do almost a full turn when
		//the target is only a couple degrees off while being on the other side of the 360° line
		int dir = 0;

		if(deltaYaw < -Math.PI)
			dir = 1;
		else if(deltaYaw < 0)
			dir = -1;
		else if(deltaYaw > Math.PI)
			dir = -1;
		else if(deltaYaw > 0)
			dir = 1;

		if(Math.abs(this.rotationYaw - targetYaw) < turnYaw || Math.abs(this.rotationYaw - targetYaw) > pi2 - turnYaw) {
			this.rotationYaw = targetYaw;
		} else {
			this.rotationYaw += turnYaw * dir;
		}

		double deltaPitch = targetPitch - this.rotationPitch;
		deltaYaw = targetYaw - this.rotationYaw;

		double deltaAngle = Math.sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch);

		this.rotationYaw = this.rotationYaw % pi2;
		this.rotationPitch = this.rotationPitch % pi2;

		if(deltaAngle <= Math.toRadians(this.getAcceptableInaccuracy())) {
			this.aligned = true;
		}
	}
	
	/**
	 * Checks line of sight to the passed entity along with whether the angle falls within swivel range
	 * @return
	 */
	public boolean entityInLOS(Entity e) {
		
		if(e.isDead || !e.isEntityAlive())
			return false;
		
		if(!hasThermalVision() && e instanceof EntityLivingBase && ((EntityLivingBase)e).isPotionActive(MobEffects.INVISIBILITY))
			return false;
		
		Vec3d pos = this.getTurretPos();
		Vec3d ent = this.getEntityPos(e);
		Vec3d delta = new Vec3d(ent.x - pos.x, ent.y - pos.y, ent.z - pos.z);
		double length = delta.length();
		
		if(length < this.getDecetorGrace() || length > this.getDecetorRange() * 1.1) //the latter statement is only relevant for entities that have already been detected
			return false;
		
		delta = delta.normalize();
		double pitch = Math.asin(delta.y / delta.length());
		double pitchDeg = Math.toDegrees(pitch);
		
		//check if the entity is within swivel range
		if(pitchDeg < -this.getTurretDepression() || pitchDeg > this.getTurretElevation())
			return false;
		
		return !Library.isObstructed(world, ent.x, ent.y, ent.z, pos.x, pos.y, pos.z);
	}
	
	/**
	 * Returns true if the entity is considered for targeting
	 * @return
	 */
	public boolean entityAcceptableTarget(Entity e) {
		
		if(e.isDead || !e.isEntityAlive())
			return false;

		for(Class c : CompatExternal.turretTargetBlacklist) if(c.isAssignableFrom(e.getClass())) return false;

		for(Class c : CompatExternal.turretTargetCondition.keySet()) {
			if(c.isAssignableFrom(e.getClass())) {
				BiFunction<Entity, Object, Integer> lambda = CompatExternal.turretTargetCondition.get(c);
				if(lambda != null) {
					int result = lambda.apply(e, this);
					if(result == -1) return false;
					if(result == 1) return true;
				}
			}
		}

		List<String> wl = getWhitelist();

		if(wl != null) {

			if(e instanceof EntityPlayer) {
				if(wl.contains(e.getName())) {
					return false;
				}
			} else if(e instanceof EntityLiving) {
				if(wl.contains(e.getCustomNameTag())) {
					return false;
				}
			}
		}
		if(targetAnimals) {
			
			if(e instanceof IAnimals)
				return true;
			if(e instanceof INpc)
				return true;
			for(Class c : CompatExternal.turretTargetFriendly) if(c.isAssignableFrom(e.getClass())) return true;
		}
		
		if(targetMobs) {

			//never target the ender dragon directly
			if(e instanceof EntityDragon)
				return false;
			if(e instanceof MultiPartEntityPart)
				return true;
			if(e instanceof IMob)
				return true;
			for(Class c : CompatExternal.turretTargetHostile) if(c.isAssignableFrom(e.getClass())) return true;
		}
		
		if(targetMachines) {

			if(e instanceof IRadarDetectableNT && !((IRadarDetectableNT)e).canBeSeenBy(this)) return false;
			if(e instanceof EntityMissileBaseNT) return e.motionY < 0;
			if(e instanceof EntityMissileCustom) return e.motionY < 0;
			if(e instanceof EntityMinecart) return true;
			//if(e instanceof EntityRailCarBase) return true; //TODO
			if(e instanceof EntityBomber) return true;
			for(Class c : CompatExternal.turretTargetMachine) if(c.isAssignableFrom(e.getClass())) return true;

		}


		if(targetPlayers ) {

			if(e instanceof FakePlayer) return false;
			if(e instanceof EntityPlayer) return true;
			for(Class c : CompatExternal.turretTargetPlayer) if(c.isAssignableFrom(e.getClass())) return true;
		}
		return false;
	}
	
	/**
	 * How many degrees the turret can deviate from the target to be acceptable to fire at
	 * @return
	 */
	public double getAcceptableInaccuracy() {
		return 5;
	}
	
	/**
	 * How many degrees the turret can rotate per tick (4.5°/t = 90°/s or a half turn in two seconds)
	 * @return
	 */
	public double getTurretYawSpeed() {
		return 4.5D;
	}
	
	/**
	 * How many degrees the turret can lift per tick (3°/t = 60°/s or roughly the lowest to the highest point of an average turret in one second)
	 * @return
	 */
	public double getTurretPitchSpeed() {
		return 3D;
	}

	/**
	 * Makes turrets sad :'(
	 * @return
	 */
	public double getTurretDepression() {
		return 30D;
	}

	/**
	 * Makes turrets feel privileged
	 * @return
	 */
	public double getTurretElevation() {
		return 30D;
	}
	
	/**
	 * How many ticks until a target rescan is required
	 * @return
	 */
	public int getDecetorInterval() {
		return 10;
	}
	
	/**
	 * How far away an entity can be to be picked up
	 * @return
	 */
	public double getDecetorRange() {
		return 32D;
	}
	
	/**
	 * How far away an entity needs to be to be picked up
	 * @return
	 */
	public double getDecetorGrace() {
		return 3D;
	}
	
	/**
	 * The pivot point of the turret, larger models have a default of 1.5
	 * @return
	 */
	public double getHeightOffset() {
		return 1.5D;
	}
	
	/**
	 * Horizontal offset for the spawn point of bullets
	 * @return
	 */
	public double getBarrelLength() {
		return 1.0D;
	}

	/**
	 * Whether the turret can detect invisible targets or not
	 * @return
	 */
	public boolean hasThermalVision() {
		return true;
	}
	
	/**
	 * The pivot point of the turret, this position is used for LOS calculation and more
	 * @return
	 */
	public Vec3d getTurretPos() {
		Vec3d offset = byHorizontalIndexOffset();
		return new Vec3d(pos.getX() + offset.x, pos.getY() + getHeightOffset(), pos.getZ() + offset.z);
	}
	
	/**
	 * The XZ offset for a standard 2x2 turret base
	 * @return
	 */
	public Vec3d byHorizontalIndexOffset() {
		int meta = this.getBlockMetadata() - BlockDummyable.offset;

		if(meta == 2)
			return new Vec3d(1, 0, 1);
		if(meta == 4)
			return new Vec3d(1, 0, 0);
		if(meta == 5)
			return new Vec3d(0, 0, 1);
		
		return new Vec3d(0, 0, 0);
	}
	
	/**
	 * The pivot point of the turret, this position is used for LOS calculation and more
	 * @return
	 */
	public Vec3d getEntityPos(Entity e) {
		return new Vec3d(e.posX, e.posY + e.height * 0.5 - e.getYOffset(), e.posZ);
	}
	
	/**
	 * Yes, new turrets fire BulletNTs.
	 * @return
	 */
	protected abstract List<Integer> getAmmoList();
	
	@SideOnly(Side.CLIENT)
	protected List<ItemStack> ammoStacks;

	@SideOnly(Side.CLIENT)
	public List<ItemStack> getAmmoTypesForDisplay() {
		
		if(ammoStacks != null)
			return ammoStacks;
		
		ammoStacks = new ArrayList();
		
		for(Integer i : getAmmoList()) {
			BulletConfig config = BulletConfig.configs.get(i);
			
			if(config != null && !config.ammo.getStack().isEmpty()) {
				ammoStacks.add(config.ammo.getStack());
			}
		}
		
		return ammoStacks;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e){
		return new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	}
	
	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack){
		return true;
	}
	
	public boolean hasPower() {
		return this.getPower() >= this.getConsumption();
	}
	
	public boolean isOn() {
		return this.isOn;
	}

	@Override
	public void setPower(long newPower) {
		this.power = Math.max(0, Math.min(newPower, this.getMaxPower()));
	}
	
	@Override
	public long getPower() {
		return this.power;
	}
	
	public int getPowerScaled(int scale) {
		return (int)(power * scale / this.getMaxPower());
	}
	
	public long getConsumption() {
		return 100;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}
	
	@Override
	public BlockPos getControlPos(){
		return getPos();
	}
	
	@Override
	public World getControlWorld(){
		return getWorld();
	}


	public static void openInventory(EntityPlayer player) {
		player.world.playSound(player.posX + 0.5, player.posY + 0.5, player.posZ + 0.5, HBMSoundHandler.openC, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
	}


	public static void closeInventory(EntityPlayer player) {
		player.world.playSound(player.posX + 0.5, player.posY + 0.5, player.posZ + 0.5, HBMSoundHandler.closeC, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
	}

	public boolean usesCasings() { return false; }

	public int casingDelay() { return 0; }

	protected Vec3d getCasingSpawnPos() {
		return this.getTurretPos();
	}

	protected CasingEjector getEjector() {
		return null;
	}

	protected void spawnCasing() {

		if(cachedCasingConfig == null) return;
		CasingEjector ej = getEjector();

		Vec3d spawn = this.getCasingSpawnPos();
		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "casing");
		data.setFloat("pitch", (float) -rotationPitch);
		data.setFloat("yaw", (float) rotationYaw);
		data.setBoolean("crouched", false);
		data.setString("name", cachedCasingConfig.getName());
		if(ej != null) data.setInteger("ej", ej.getId());
		PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, spawn.x, spawn.y, spawn.z), new TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 50));

		cachedCasingConfig = null;
	}
	
	@Override
	public void receiveEvent(BlockPos from, ControlEvent e){
		if(e.name.equals("turret_set_target")){
			this.targetPlayers = e.vars.get("players").getBoolean();
			this.targetMobs = e.vars.get("hostile").getBoolean();
			this.targetAnimals = e.vars.get("passive").getBoolean();
			this.targetMachines = e.vars.get("machines").getBoolean();
		} else if(e.name.equals("turret_switch")){
			this.isOn = e.vars.get("isOn").getBoolean();
		}
	}
	
	@Override
	public List<String> getInEvents(){
		return Arrays.asList("turret_set_target", "turret_switch");
	}
	
	@Override
	public void validate(){
		super.validate();
		ControlEventSystem.get(world).addControllable(this);
	}
	
	@Override
	public void invalidate(){
		super.invalidate();
		ControlEventSystem.get(world).removeControllable(this);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(
					new NTMEnergyCapabilityWrapper(this)
			);
		}
		return super.getCapability(capability, facing);
	}

	// OC stuff
	// This is a large compat, so I have to leave comments to know what I'm doing

	@Override
	@Optional.Method(modid = "opencomputers")
	public String getComponentName() {
		return "ntm_turret";
	}

	// On/Off
	@Callback(direct = true, limit = 4)
	@Optional.Method(modid = "opencomputers")
	public Object[] setActive(Context context, Arguments args) {
		this.isOn = args.checkBoolean(0);
		return new Object[] {};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] isActive(Context context, Arguments args) {
		return new Object[] {this.isOn};
	}

	// Energy information
	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getEnergyInfo(Context context, Arguments args) {
		return new Object[] {this.getPower(), this.getMaxPower()};
	}

	///////////////////////
	// Whitelist Control //
	///////////////////////
	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getWhitelisted(Context context, Arguments args) {
		if(inventory.getStackInSlot(0).getItem() == ModItems.turret_chip) {
			String[] array = ItemTurretBiometry.getNames(inventory.getStackInSlot(0));
			return new Object[] {array};
		}
		return new Object[] {};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] addWhitelist(Context context, Arguments args) {
		if(this.getWhitelist() != null) {
			List<String> names = this.getWhitelist();
			if (names.contains(args.checkString(0)))
				return new Object[]{false};
		}
		this.addName(args.checkString(0));
		return new Object[]{true};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] removeWhitelist(Context context, Arguments args) {
		List<String> names = this.getWhitelist();
		if(!names.contains(args.checkString(0)))
			return new Object[] {false};
		this.removeName(names.indexOf(args.checkString(0)));
		return new Object[] {true};
	}

	///////////////////////
	// Targeting Control //
	///////////////////////
	@Callback(direct = true, limit = 4)
	@Optional.Method(modid = "opencomputers")
	public Object[] setTargeting(Context context, Arguments args) {
		this.targetPlayers = args.checkBoolean(0);
		this.targetAnimals = args.checkBoolean(1);
		this.targetMobs = args.checkBoolean(2);
		this.targetMachines = args.checkBoolean(3);
		return new Object[] {};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getTargeting(Context context, Arguments args) {
		return new Object[] {this.targetPlayers, this.targetAnimals, this.targetMobs, this.targetMachines};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] hasTarget(Context context, Arguments args) {
		return new Object[] {this.target != null};
	}

	///////////////////
	// Angle Control //
	///////////////////

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getAngle(Context context, Arguments args) {
		return new Object[] {Math.toDegrees(this.rotationPitch), Math.toDegrees(this.rotationYaw)};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] isAligned(Context context, Arguments args) {
		return new Object[] {this.aligned};
	}

	@Override
	@Optional.Method(modid = "opencomputers")
	public boolean canConnectNode(EnumFacing side) {
		return side == EnumFacing.DOWN;
	}

	@Override
	@Optional.Method(modid = "opencomputers")
	public String[] methods() { // :vomit:
		return new String[] {
				"setActive",
				"isActive",
				"getEnergyInfo",
				"getWhitelisted",
				"addWhitelist",
				"removeWhitelist",
				"setTargeting",
				"getTargeting",
				"hasTarget",
				"getAngle",
				"isAligned"
		};
	}

	@Override
	@Optional.Method(modid = "opencomputers")
	public Object[] invoke(String method, Context context, Arguments args) throws Exception {
		switch (method) {
			case "setActive":
				return setActive(context, args);
			case "isActive":
				return isActive(context, args);
			case "getEnergyInfo":
				return getEnergyInfo(context, args);
			case "getWhitelisted":
				return getWhitelisted(context, args);
			case "addWhitelist":
				return addWhitelist(context, args);
			case "removeWhitelist":
				return removeWhitelist(context, args);
			case "setTargeting":
				return setTargeting(context, args);
			case "getTargeting":
				return getTargeting(context, args);
			case "hasTarget":
				return hasTarget(context, args);
			case "getAngle":
				return getAngle(context, args);
			case "isAligned":
				return isAligned(context, args);
		}
		throw new NoSuchMethodException();
	}
}
