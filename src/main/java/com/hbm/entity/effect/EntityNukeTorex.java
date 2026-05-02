package com.hbm.entity.effect;

import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IConstantRenderer;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.render.amlfrom1710.Vec3;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
/*
 * Toroidial Convection Simulation Explosion Effect
 * Tor                             Ex
 */
@AutoRegister(name = "entity_effect_torex", trackingRange = 1000)
public class EntityNukeTorex extends Entity implements IConstantRenderer {

	public static final DataParameter<Float> SCALE = EntityDataManager.createKey(EntityNukeTorex.class, DataSerializers.FLOAT);
	public static final DataParameter<Byte> TYPE = EntityDataManager.createKey(EntityNukeTorex.class, DataSerializers.BYTE);
	
	public static final int firstCondenseHeight = 130;
	public static final int secondCondenseHeight = 170;
	public static final int blastWaveHeadstart = 5;
	public static final int maxCloudlets = 20_000;

	//Nuke colors
	public static final double nr1 = 2.5;
	public static final double ng1 = 1.3;
	public static final double nb1 = 0.4;
	public static final double nr2 = 0.1;
	public static final double ng2 = 0.075;
	public static final double nb2 = 0.05;

	//Balefire colors
	public static final double br1 = 1;
	public static final double bg1 = 2;
	public static final double bb1 = 0.5;
	public static final double br2 = 0.1;
	public static final double bg2 = 0.1;
	public static final double bb2 = 0.1;

	public double coreHeight = 3;
	public double convectionHeight = 3;
	public double torusWidth = 3;
	public double rollerSize = 1;
	public double heat = 1;
	public double lastSpawnY = -1;
	public ArrayList<Cloudlet> cloudlets = new ArrayList<>();
	public int lastRenderSortTick = Integer.MIN_VALUE;
	public int maxAge = 1000;
	public float humidity = -1;

	public boolean didPlaySound = false;
	public boolean didShake = false;

	public EntityNukeTorex(World p_i1582_1_) {
		super(p_i1582_1_);
		this.setSize(20F, 40F);
		this.isImmuneToFire = true;
		this.ignoreFrustumCheck = true;
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(SCALE, 1.0F);
		this.dataManager.register(TYPE, Byte.valueOf((byte) 0));
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		if (nbt.hasKey("scale"))
			setScale(nbt.getFloat("scale"));
		if (nbt.hasKey("type"))
			this.dataManager.set(TYPE, nbt.getByte("type"));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setFloat("scale", this.dataManager.get(SCALE));
		nbt.setByte("type", this.dataManager.get(TYPE));
	}

	@Override
	public boolean writeToNBTOptional(NBTTagCompound compound) {
		return false;
	}

	@Override
	public boolean writeToNBTAtomically(NBTTagCompound compound){
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
		return true;
	}

	@Override
	public void onUpdate() {
		if(world.isRemote) {
			
			double s = this.getScale();
			double cs = 1.5;
			if(this.ticksExisted == 1) this.setScale((float) s);
			
			if(humidity == -1) humidity = world.getBiome(this.getPosition()).getRainfall();
			
			if(lastSpawnY == -1) {
				lastSpawnY = posY - 3;
			}
			
			int spawnTarget = Math.max(world.getHeight((int) Math.floor(posX), (int) Math.floor(posZ)) - 3, 1);
			double moveSpeed = 0.5D;
			
			if(Math.abs(spawnTarget - lastSpawnY) < moveSpeed) {
				lastSpawnY = spawnTarget;
			} else {
				lastSpawnY += moveSpeed * Math.signum(spawnTarget - lastSpawnY);
			}
			
			// spawn mush clouds
			double range = (torusWidth - rollerSize) * 0.5;
			double simSpeed = getSimulationSpeed();
			int lifetime = Math.min((this.ticksExisted * this.ticksExisted) + 200, maxAge - this.ticksExisted + 200);
			int toSpawn = (int) (0.6 * Math.min(Math.max(0, maxCloudlets-cloudlets.size()), Math.ceil(10 * simSpeed * simSpeed * Math.min(1, 1200/(double)lifetime))));
			

			for(int i = 0; i < toSpawn; i++) {
				double x = posX + rand.nextGaussian() * range;
				double z = posZ + rand.nextGaussian() * range;
				Cloudlet cloud = new Cloudlet(x, lastSpawnY, z, (float)(rand.nextDouble() * 2D * Math.PI), 0, lifetime);
				cloud.setScale((float) (Math.sqrt(s) * 3 + this.ticksExisted * 0.0025 * s), (float) (Math.sqrt(s) * 3 + this.ticksExisted * 0.0025 * 6 * cs * s));
				cloudlets.add(cloud);
			}

			if(this.ticksExisted < 120 * s){
				world.setLastLightningBolt(2);
			}
			
			// spawn shock clouds
			if(this.ticksExisted < 150) {
				
				int cloudCount = Math.min(this.ticksExisted * 2, 100);
				int shockLife = Math.max(400 - this.ticksExisted * 20, 50);
				
				for(int i = 0; i < cloudCount; i++) {
					Vec3 vec = Vec3.createVectorHelper((this.ticksExisted + rand.nextDouble() * 2) * 1.5, 0, 0);
					float rot = (float) (Math.PI * 2 * rand.nextDouble());
					vec.rotateAroundY(rot);
					this.cloudlets.add(new Cloudlet(vec.xCoord + posX, world.getHeight((int) (vec.xCoord + posX) + 1, (int) (vec.zCoord + posZ)), vec.zCoord + posZ, rot, 0, shockLife, TorexType.SHOCK)
							.setScale((float)s * 5F, (float)s * 2F).setMotion(MathHelper.clamp(0.25 * this.ticksExisted - 5, 0, 1)));
				}

				if(!didPlaySound) {
					if(MainRegistry.proxy.me() != null && MainRegistry.proxy.me().getDistance(this) < (ticksExisted * 1.5 + 1) * 1.5) {
						MainRegistry.proxy.playSoundClient(posX, posY, posZ, HBMSoundHandler.nuclearExplosion, SoundCategory.HOSTILE, 10_000F, 1F);
						didPlaySound = true;
					}
				}
			}
			
			// spawn ring clouds
			if(this.ticksExisted < 200) {
				lifetime *= s;
				for(int i = 0; i < 2; i++) {
					Cloudlet cloud = new Cloudlet(posX, posY + coreHeight, posZ, (float)(rand.nextDouble() * 2D * Math.PI), 0, lifetime, TorexType.RING);
					cloud.setScale((float) (Math.sqrt(s) * cs + this.ticksExisted * 0.0015 * s), (float) (Math.sqrt(s) * cs + this.ticksExisted * 0.0015 * 6 * cs * s));
					cloudlets.add(cloud);
				}
			}

			if(this.humidity > 0 && this.ticksExisted < 220){
				// spawn lower condensation clouds
				spawnCondensationClouds(this.ticksExisted, this.humidity, firstCondenseHeight, 80, 4, s, cs);

				// spawn upper condensation clouds
				spawnCondensationClouds(this.ticksExisted, this.humidity, secondCondenseHeight, 80, 2, s, cs);
			}

			for(int i = cloudlets.size() - 1; i >= 0; i--) {
				Cloudlet cloud = cloudlets.get(i);
				if(cloud.isDead) {
					cloudlets.remove(i);
					continue;
				}
				cloud.update();
			}
			
			coreHeight += 0.15/* * s*/;
			torusWidth += 0.05/* * s*/;
			rollerSize = torusWidth * 0.35;
			convectionHeight = coreHeight + rollerSize;
			
			int maxHeat = (int) (50 * s * s);
			heat = maxHeat - Math.pow((maxHeat * this.ticksExisted) / maxAge, 0.6);
		}
		
		if(!world.isRemote && this.ticksExisted > maxAge) {
			this.setDead();
		}
	}

	public void spawnCondensationClouds(int age, float humidity, int height, int count, int spreadAngle, double s, double cs){
		if((posY + age) > height) {
			
			for(int i = 0; i < (int)(5 * humidity * count/(double)spreadAngle); i++) {
				for(int j = 1; j < spreadAngle; j++) {
					float angle = (float) (Math.PI * 2 * rand.nextDouble());
					Vec3 vec = Vec3.createVectorHelper(0, age, 0);
					vec.rotateAroundZ((float)Math.acos((height-posY)/(age))+(float)Math.toRadians(humidity*humidity*90*j*(0.1*rand.nextDouble()-0.05)));
					vec.rotateAroundY(angle);
					Cloudlet cloud = new Cloudlet(posX + vec.xCoord, posY + vec.yCoord, posZ + vec.zCoord, angle, 0, (int) ((20 + age / 10) * (1 + rand.nextDouble() * 0.1)), TorexType.CONDENSATION);
					cloud.setScale(3F * (float) (cs * s), 4F * (float) (cs * s));
					cloudlets.add(cloud);
				}
			}
		}
	}
	
	public EntityNukeTorex setScale(float scale) {
		if(!world.isRemote)
			this.dataManager.set(SCALE, scale);
		this.coreHeight = this.coreHeight * scale;
		this.convectionHeight = this.convectionHeight * scale;
		this.torusWidth = this.torusWidth * scale;
		this.rollerSize = this.rollerSize * scale;
		this.maxAge = (int) (45 * 20 * scale);
		return this;
	}
	
	public EntityNukeTorex setType(int type) {
		this.dataManager.set(TYPE, (byte) type);
		return this;
	}

	public double getScale() {
		return this.dataManager.get(SCALE);
	}

	public byte getType() {
		return this.dataManager.get(TYPE);
	}
	
	public double getSimulationSpeed() {
		
		int simSlow = maxAge / 4;
		int life = this.ticksExisted;
		
		if(life > maxAge) {
			return 0D;
		}
		
		if(life > simSlow) {
			return 1D - ((double)(life - simSlow) / (double)(maxAge - simSlow));
		}
		
		return 1.0D;
	}
	
	public float getAlpha() {
		
		int fadeOut = maxAge * 3 / 4;
		int life = this.ticksExisted;
		
		if(life > fadeOut) {
			float fac = (float)(life - fadeOut) / (float)(maxAge - fadeOut);
			return 1F - fac;
		}
		
		return 1.0F;
	}

	public Vec3 getInterpColor(double interp, byte type) {
		if(type == 0){
			return Vec3.createVectorHelper(
				(nr2 + (nr1 - nr2) * interp),
				(ng2 + (ng1 - ng2) * interp),
				(nb2 + (nb1 - nb2) * interp));
		}
		return Vec3.createVectorHelper(
			(br2 + (br1 - br2) * interp),
			(bg2 + (bg1 - bg2) * interp),
			(bb2 + (bb1 - bb2) * interp));
	}

	public class Cloudlet {

		public double posX;
		public double posY;
		public double posZ;
		public double prevPosX;
		public double prevPosY;
		public double prevPosZ;
		public double motionX;
		public double motionY;
		public double motionZ;
		public int age;
		public int cloudletLife;
		public float angle;
		public boolean isDead = false;
		float rangeMod = 1.0F;
		public float colorMod = 1.0F;
		public double colorR;
		public double colorG;
		public double colorB;
		public double prevColorR;
		public double prevColorG;
		public double prevColorB;
		public double renderSortDistanceSq;
		public TorexType type;
		public float startingScale = 3F;
		public float growingScale = 5F;
		private double computedMotionX;
		private double computedMotionY;
		private double computedMotionZ;
		
		public Cloudlet(double posX, double posY, double posZ, float angle, int age, int maxAge) {
			this(posX, posY, posZ, angle, age, maxAge, TorexType.STANDARD);
		}

		public Cloudlet(double posX, double posY, double posZ, float angle, int age, int maxAge, TorexType type) {
			this.posX = posX;
			this.posY = posY;
			this.posZ = posZ;
			this.age = age;
			this.cloudletLife = maxAge;
			this.angle = angle;
			this.rangeMod = 0.3F + rand.nextFloat() * 0.7F;
			this.colorMod = 0.8F + rand.nextFloat() * 0.2F;
			this.type = type;
			
			this.updateColor();
		}

		private double motionMult = 1F;
		private double motionConvectionMult = 0.5F;
		private double motionLiftMult = 0.625F;
		private double motionRingMult = 0.5F;
		private double motionCondensationMult = 1F;
		private double motionShockwaveMult = 1F;
		
		
		private void update() {
			age++;
			
			if(age > cloudletLife) {
				this.isDead = true;
			}

			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
			
			double simDeltaX = EntityNukeTorex.this.posX - this.posX;
			double simDeltaZ = EntityNukeTorex.this.posZ - this.posZ;
			double simPosX = EntityNukeTorex.this.posX + Math.sqrt(simDeltaX * simDeltaX + simDeltaZ * simDeltaZ);
			
			if(this.type == TorexType.STANDARD) {
				getConvectionMotion(simPosX);
				double convectionX = this.computedMotionX;
				double convectionY = this.computedMotionY;
				double convectionZ = this.computedMotionZ;
				getLiftMotion(simPosX);
				
				double factor = MathHelper.clamp((this.posY - EntityNukeTorex.this.posY) / EntityNukeTorex.this.coreHeight, 0, 1);
				double inverseFactor = 1D - factor;
				this.motionX = convectionX * factor + this.computedMotionX * inverseFactor;
				this.motionY = convectionY * factor + this.computedMotionY * inverseFactor;
				this.motionZ = convectionZ * factor + this.computedMotionZ * inverseFactor;
			} else if(this.type == TorexType.RING) {
				getRingMotion(simPosX);
				this.motionX = this.computedMotionX;
				this.motionY = this.computedMotionY;
				this.motionZ = this.computedMotionZ;
			} else if(this.type == TorexType.CONDENSATION) {
				getCondensationMotion();
				this.motionX = this.computedMotionX;
				this.motionY = this.computedMotionY;
				this.motionZ = this.computedMotionZ;
			} else if(this.type == TorexType.SHOCK) {
				getShockwaveMotion();
				this.motionX = this.computedMotionX;
				this.motionY = this.computedMotionY;
				this.motionZ = this.computedMotionZ;
			}
			
			double mult = this.motionMult * getSimulationSpeed();
			
			this.posX += this.motionX * mult;
			this.posY += this.motionY * mult;
			this.posZ += this.motionZ * mult;
			
			this.updateColor();
		}
		
		private void getCondensationMotion() {
			double speed = motionCondensationMult * EntityNukeTorex.this.getScale() * 0.125D;
			setNormalizedMotion(this.posX - EntityNukeTorex.this.posX, 0D, this.posZ - EntityNukeTorex.this.posZ, speed);
		}

		private void getShockwaveMotion() {
			double speed = motionShockwaveMult * EntityNukeTorex.this.getScale() * 0.25D;
			setNormalizedMotion(this.posX - EntityNukeTorex.this.posX, 0D, this.posZ - EntityNukeTorex.this.posZ, speed);
		}
		
		private void getRingMotion(double simPosX) {
			
			if(simPosX > EntityNukeTorex.this.posX + torusWidth * 2) {
				setComputedMotion(0D, 0D, 0D);
				return;
			}
			
			double torusPosX = EntityNukeTorex.this.posX + torusWidth;
			double torusPosY = EntityNukeTorex.this.posY + coreHeight * 0.5D;
			
			double deltaX = torusPosX - simPosX;
			double deltaY = torusPosY - this.posY;
			
			double roller = EntityNukeTorex.this.rollerSize * this.rangeMod * 0.25D;
			double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY) / roller - 1D;
			
			double func = 1D - Math.exp(-dist);
			float angle = (float) (func * Math.PI * 0.5D); // [0;90°]
			
			double rotX = -deltaX / dist;
			double rotY = -deltaY / dist;
			float sin = MathHelper.sin(angle);
			float cos = MathHelper.cos(angle);
			double rotatedX = rotX * cos + rotY * sin;
			double rotatedY = rotY * cos - rotX * sin;
			
			setNormalizedMotion(torusPosX + rotatedX - simPosX, torusPosY + rotatedY - this.posY, 0D, motionRingMult * 0.5D);
			rotateComputedMotionAroundY();
		}
		
		/* simulated on a 2D-plane along the X/Y axis */
		private void getConvectionMotion(double simPosX) {
			
			if(simPosX > EntityNukeTorex.this.posX + torusWidth * 2) {
				setComputedMotion(0D, 0D, 0D);
				return;
			}
			
			double torusPosX = EntityNukeTorex.this.posX + torusWidth;
			double torusPosY = EntityNukeTorex.this.posY + coreHeight;
			
			double deltaX = torusPosX - simPosX;
			double deltaY = torusPosY - this.posY;
			
			double roller = EntityNukeTorex.this.rollerSize * this.rangeMod;
			double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY) / roller - 1D;
			
			double func = 1D - Math.exp(-dist);
			float angle = (float) (func * Math.PI * 0.5D); // [0;90°]
			
			double rotX = -deltaX / dist;
			double rotY = -deltaY / dist;
			float sin = MathHelper.sin(angle);
			float cos = MathHelper.cos(angle);
			double rotatedX = rotX * cos + rotY * sin;
			double rotatedY = rotY * cos - rotX * sin;
			
			setNormalizedMotion(torusPosX + rotatedX - simPosX, torusPosY + rotatedY - this.posY, 0D, motionConvectionMult);
			rotateComputedMotionAroundY();
		}
		
		private void getLiftMotion(double simPosX) {
			double scale = MathHelper.clamp(1D - (simPosX - (EntityNukeTorex.this.posX + torusWidth)), 0, 1) * motionLiftMult;
			
			setNormalizedMotion(
					EntityNukeTorex.this.posX - this.posX,
					(EntityNukeTorex.this.posY + convectionHeight) - this.posY,
					EntityNukeTorex.this.posZ - this.posZ,
					scale);
		}

		private void setComputedMotion(double x, double y, double z) {
			this.computedMotionX = x;
			this.computedMotionY = y;
			this.computedMotionZ = z;
		}

		private void setNormalizedMotion(double x, double y, double z, double speed) {
			double lengthSq = x * x + y * y + z * z;
			if(lengthSq < 1.0E-8D) {
				setComputedMotion(0D, 0D, 0D);
				return;
			}

			double scale = speed / Math.sqrt(lengthSq);
			setComputedMotion(x * scale, y * scale, z * scale);
		}

		private void rotateComputedMotionAroundY() {
			float cos = MathHelper.cos(this.angle);
			float sin = MathHelper.sin(this.angle);
			double motionX = this.computedMotionX;
			double motionZ = this.computedMotionZ;
			this.computedMotionX = motionX * cos + motionZ * sin;
			this.computedMotionZ = motionZ * cos - motionX * sin;
		}
		
		private void updateColor() {
			this.prevColorR = this.colorR;
			this.prevColorG = this.colorG;
			this.prevColorB = this.colorB;

			double exX = EntityNukeTorex.this.posX;
			double exY = EntityNukeTorex.this.posY + EntityNukeTorex.this.coreHeight;
			double exZ = EntityNukeTorex.this.posZ;

			double distX = exX - posX;
			double distY = exY - posY;
			double distZ = exZ - posZ;
			
			double distSq = distX * distX + distY * distY + distZ * distZ;
			distSq /= this.type == TorexType.SHOCK ? EntityNukeTorex.this.heat * 3 : EntityNukeTorex.this.heat;
			
			double col = 2D / Math.max(distSq, 1); //col goes from 2-0

			byte type = EntityNukeTorex.this.getType();
			if(type == 0) {
				this.colorR = nr2 + (nr1 - nr2) * col;
				this.colorG = ng2 + (ng1 - ng2) * col;
				this.colorB = nb2 + (nb1 - nb2) * col;
				return;
			}

			this.colorR = br2 + (br1 - br2) * col;
			this.colorG = bg2 + (bg1 - bg2) * col;
			this.colorB = bb2 + (bb1 - bb2) * col;
		}
		
		public Vec3 getInterpPos(float interp) {
			return Vec3.createVectorHelper(
					prevPosX + (posX - prevPosX) * interp,
					prevPosY + (posY - prevPosY) * interp,
					prevPosZ + (posZ - prevPosZ) * interp);
		}
		
		public Vec3 getInterpColor(float interp) {
			
			if(this.type == TorexType.CONDENSATION) {
				return Vec3.createVectorHelper(1F, 1F, 1F);
			}
			
			double greying = 0;
			
			if(this.type == TorexType.RING) {
				greying += 0.05;
			}
			
			return Vec3.createVectorHelper(
					(prevColorR + (colorR - prevColorR) * interp) + greying,
					(prevColorG + (colorG - prevColorG) * interp) + greying,
					(prevColorB + (colorB - prevColorB) * interp) + greying);
		}
		
		public float getAlpha() {
			float alpha = (1F - ((float)age / (float)cloudletLife)) * EntityNukeTorex.this.getAlpha();
			if(this.type == TorexType.CONDENSATION) alpha *= 0.25;
			return MathHelper.clamp(alpha, 0.0001F, 1F);
		}
		
		
		public float getScale() {
			return startingScale + ((float)age / (float)cloudletLife) * growingScale;
		}
		
		public Cloudlet setScale(float start, float grow) {
			this.startingScale = start;
			this.growingScale = grow;
			return this;
		}
		
		public Cloudlet setMotion(double mult) {
			this.motionMult = mult;
			return this;
		}
	}
	
	public static enum TorexType {
		STANDARD,
		RING,
		CONDENSATION,
		SHOCK
	}

    /**
     * Spawns a standard Torex. Does play sound!
     */
	public static void statFac(World world, double x, double y, double z, float scale) {
		EntityNukeTorex torex = new EntityNukeTorex(world).setScale(MathHelper.clamp(scale * 0.01F, 0.25F, 5F));
		torex.setPosition(x, y, z);
		world.spawnEntity(torex);
	}

    /**
     * Spawns a Torex, balefire variant. Does play sound!
     */
	public static void statFacBale(World world, double x, double y, double z, float scale) {
		EntityNukeTorex torex = new EntityNukeTorex(world).setScale(MathHelper.clamp(scale * 0.01F, 0.25F, 5F)).setType(1);
		torex.setPosition(x, y, z);
		world.spawnEntity(torex);
	}
}
