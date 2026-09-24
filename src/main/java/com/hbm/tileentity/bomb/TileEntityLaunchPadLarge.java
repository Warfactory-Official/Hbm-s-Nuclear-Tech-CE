package com.hbm.tileentity.bomb;

import com.hbm.entity.missile.EntityMissileBaseNT;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.ItemMissileStandard;
import com.hbm.items.weapon.ItemMissileStandard.MissileFormFactor;
import com.hbm.lib.DirPos;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.sound.AudioWrapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AutoRegister
public class TileEntityLaunchPadLarge extends TileEntityLaunchPadBase {

	public int formFactor = -1;
	/** Whether the missile has already been placed on the launchpad. Missile will render statically on the pad if true */
	public boolean erected = false;
	/** Whether the missile can be lifted. Missile will not render at all if false and not erected */
	public boolean readyToLoad = false;
	/** Instead of setting erected to true outright, this makes it so that it ties into the delay,
	 * which prevents a jerky transition due to the animation of the erector lagging behind a bit */
	public boolean scheduleErect = false;
	public float lift = 1F;
	public float erector = 90F;
	public float prevLift = 1F;
	public float prevErector = 90F;
	public float syncLift;
	public float syncErector;
	private int sync;
	/** Delay between erector movements */
	public int delay = 20;
	
	private AudioWrapper audioLift;
	private AudioWrapper audioErector;
	
	protected boolean liftMoving = false;
	protected boolean erectorMoving = false;

	public TileEntityLaunchPadLarge() {
		super(7);
	}

	@Override public boolean isReadyForLaunch() { return this.erected && this.readyToLoad; }
	@Override public double getLaunchOffset() { return 2D; }

	@Override
	public void update() {
		
		if(!world.isRemote) {

			this.prevLift = this.lift;
			this.prevErector = this.erector;
			
			float erectorSpeed = 1.5F;
			float liftSpeed = 0.025F;
			
			if(this.isMissileValid()) {
				if(inventory.getStackInSlot(0).getItem() instanceof ItemMissileStandard) {
					ItemMissileStandard missile = (ItemMissileStandard) inventory.getStackInSlot(0).getItem();
					this.formFactor = missile.formFactor.ordinal();
					
					if(missile.formFactor == MissileFormFactor.ATLAS || missile.formFactor == MissileFormFactor.HUGE) {
						erectorSpeed /= 2F;
						liftSpeed /= 2F;
					}
				}
				
				if(this.erector == 90F && this.lift == 1F) {
					this.readyToLoad = true;
				}
			} else {
				readyToLoad = false;
				erected = false;
				delay = 20;
			}
			
			if(this.power >= 75_000) {
				if(delay > 0) {
					delay--;
					
					if(delay < 10 && scheduleErect) {
						this.erected = true;
						this.scheduleErect = false;
					}
					
					// if there is no missile or the missile isn't ready (i.e. the erector hasn't returned to zero position yet), retract
					if(inventory.getStackInSlot(0).isEmpty() || !readyToLoad) {
						//fold back erector
						if(erector < 90F) {
							erector = Math.min(erector + erectorSpeed, 90F);
							if(erector == 90F) delay = 20;
						//extend lift
						} else if(lift < 1F) {
							lift = Math.min(lift + liftSpeed, 1F);
							if(erector == 1F) {
								//if the lift is fully extended, the loading can begin
								readyToLoad = true;
								delay = 20;
							}
						}
					}
					
				} else {
					
					//only extend if the erector isn't up yet and the missile can be loaded
					if(!erected && readyToLoad) {
						this.state = STATE_LOADING;
						
						//first, rotate the erector
						if(erector != 0F) {
							erector = Math.max(erector - erectorSpeed, 0F);
							if(erector == 0F) delay = 20;
						//then retract the lift
						} else if(lift > 0) {
							lift = Math.max(lift - liftSpeed, 0F);
							if(lift == 0F) {
								//once the lift is at the bottom, the missile is deployed
								scheduleErect = true;
								delay = 20;
							}
						}
					} else {
						//first, fold back the erector
						if(erector < 90F) {
							erector = Math.min(erector + erectorSpeed, 90F);
							if(erector == 90F) delay = 20;
						//then extend the lift again
						} else if(lift < 1F) {
							lift = Math.min(lift + liftSpeed, 1F);
							if(erector == 1F) {
								//if the lift is fully extended, the loading can begin
								readyToLoad = true;
								delay = 20;
							}
						}
					}
				}
			}
			
			if(!this.hasFuel() || !this.isMissileValid()) this.state = this.STATE_MISSING;
			if(this.erected && this.canLaunch()) this.state = this.STATE_READY;

			boolean prevLiftMoving = this.liftMoving;
			boolean prevErectorMoving = this.erectorMoving;
			this.liftMoving = false;
			this.erectorMoving = false;
			if(this.prevLift != this.lift) this.liftMoving = true;
			if(this.prevErector != this.erector) this.erectorMoving = true;

			if(prevLiftMoving && !this.liftMoving) world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.wgh_stop, SoundCategory.BLOCKS, 2F, 1F);
			if(prevErectorMoving && !this.erectorMoving) world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.garage_stop, SoundCategory.BLOCKS, 2F, 1F);
			
		} else {
			this.prevLift = this.lift;
			this.prevErector = this.erector;
			
			if(this.sync > 0) {
				this.lift = this.lift + ((this.syncLift - this.lift) / (float) this.sync);
				this.erector = this.erector + ((this.syncErector - this.erector) / (float) this.sync);
				--this.sync;
			} else {
				this.lift = this.syncLift;
				this.erector = this.syncErector;
			}
			
			if(this.liftMoving) {
				if(this.audioLift == null || !this.audioLift.isPlaying()) {
					this.audioLift = MainRegistry.proxy.getLoopedSound(HBMSoundHandler.wgh_start, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(),
					0.75F, 1.0F);
					this.audioLift.startSound();
				}
				this.audioLift.keepAlive();
			} else {
				if(this.audioLift != null) {
					this.audioLift.stopSound();
					this.audioLift = null;
				}
			}
			
			if(this.erectorMoving) {
				if(this.audioErector == null || !this.audioErector.isPlaying()) {
					this.audioErector = MainRegistry.proxy.getLoopedSound(HBMSoundHandler.garage, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 1.5F, 1.0F);
					this.audioErector.startSound();
				}
				this.audioErector.keepAlive();
			} else {
				if(this.audioErector != null) {
					this.audioErector.stopSound();
					this.audioErector = null;
				}
			}
			
			if(this.erected && (this.formFactor == MissileFormFactor.HUGE.ordinal() || this.formFactor == MissileFormFactor.ATLAS.ordinal()) && this.tanks[1].getFill() > 0) {
				NBTTagCompound data = new NBTTagCompound();
				data.setFloat("lift", 0F);
				data.setFloat("base", 0.5F);
				data.setFloat("max", 2F);
				data.setInteger("life", 70 + world.rand.nextInt(30));
				data.setBoolean("noWind", true);
				data.setFloat("alphaMod", 2F);
				data.setFloat("strafe", 0.05F);
				for(int i = 0; i < 3; i++) MainRegistry.proxy.effectNT(HbmEffectNT.Tower, pos.getX() + 0.5 + world.rand.nextGaussian() * 0.5, pos.getY() + 2, pos.getZ() + 0.5 + world.rand.nextGaussian() * 0.5, data);
			}
			
			List<EntityMissileBaseNT> entities = world.getEntitiesWithinAABB(EntityMissileBaseNT.class, new AxisAlignedBB(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 10, pos.getZ() + 1.5));
			
			if(!entities.isEmpty()) {
				for(int i = 0; i < 15; i++) MainRegistry.proxy.effectNT(HbmEffectNT.LaunchSmoke, pos.getX() + .5, pos.getY() + .25, pos.getZ() + .5);
			}
		}
		
		super.update();
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		
		buf.writeBoolean(this.liftMoving);
		buf.writeBoolean(this.erectorMoving);
		buf.writeBoolean(this.erected);
		buf.writeBoolean(this.readyToLoad);
		buf.writeByte((byte) this.formFactor);
		buf.writeFloat(this.lift);
		buf.writeFloat(this.erector);
	}
	
	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		this.liftMoving = buf.readBoolean();
		this.erectorMoving = buf.readBoolean();
		this.erected = buf.readBoolean();
		this.readyToLoad = buf.readBoolean();
		this.formFactor = buf.readByte();
		this.syncLift = buf.readFloat();
		this.syncErector = buf.readFloat();
		
		if(this.lift != this.syncLift || this.erector != this.syncErector) {
			this.sync = 3;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.erected = nbt.getBoolean("erected");
		this.readyToLoad = nbt.getBoolean("readyToLoad");
		this.lift = nbt.getFloat("lift");
		this.erector = nbt.getFloat("erector");
		this.formFactor = nbt.getInteger("formFactor");
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setBoolean("erected", erected);
		nbt.setBoolean("readyToLoad", readyToLoad);
		nbt.setFloat("lift", lift);
		nbt.setFloat("erector", erector);
		nbt.setInteger("formFactor", formFactor);
		return super.writeToNBT(nbt);
	}

	@Override
	public void finalizeLaunch(Entity missile) {
		super.finalizeLaunch(missile);
		this.erected = false;
	}
	
	@Override
	public DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(pos.getX() + 5, pos.getY(), pos.getZ() - 2, Library.POS_X),
				new DirPos(pos.getX() + 5, pos.getY(), pos.getZ() + 2, Library.POS_X),
				new DirPos(pos.getX() - 5, pos.getY(), pos.getZ() - 2, Library.NEG_X),
				new DirPos(pos.getX() - 5, pos.getY(), pos.getZ() + 2, Library.NEG_X),
				new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() + 5, Library.POS_Z),
				new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() + 5, Library.POS_Z),
				new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() - 5, Library.NEG_Z),
				new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() - 5, Library.NEG_Z)
		};
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = new AxisAlignedBB(
					pos.getX() - 10,
					pos.getY(),
					pos.getZ() - 10,
					pos.getX() + 11,
					pos.getY() + 15,
					pos.getZ() + 11
					);
		}
		
		return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
