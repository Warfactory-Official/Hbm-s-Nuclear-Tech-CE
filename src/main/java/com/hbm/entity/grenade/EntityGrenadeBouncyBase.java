package com.hbm.entity.grenade;

import com.hbm.config.CompatibilityConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.util.Vec3NT;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ReportedException;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.util.List;

public abstract class EntityGrenadeBouncyBase extends Entity implements IProjectile {

	protected EntityLivingBase thrower;
	protected String throwerName;
	protected int timer = 0;

	public EntityGrenadeBouncyBase(World world) {
		super(world);
		this.setSize(0.25F, 0.25F);
	}

	public EntityGrenadeBouncyBase(World world, EntityLivingBase living, EnumHand hand) {
		super(world);
		this.thrower = living;
		this.setSize(0.25F, 0.25F);
		this.setLocationAndAngles(living.posX, living.posY + (double) living.getEyeHeight(), living.posZ, living.rotationYaw, living.rotationPitch);
		if (hand == EnumHand.MAIN_HAND) {
			this.posX -= (double) (MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
			this.posY -= 0.10000000149011612D;
			this.posZ -= (double) (MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
		} else {
			this.posX += (double) (MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
			this.posY -= 0.10000000149011612D;
			this.posZ += (double) (MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
		}

		this.setPosition(this.posX, this.posY, this.posZ);
		float f = 0.4F;
		this.motionX = (double) (-MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI) * f);
		this.motionZ = (double) (MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI) * f);
		this.motionY = (double) (-MathHelper.sin((this.rotationPitch + this.func_70183_g()) / 180.0F * (float) Math.PI) * f);
		this.shoot(this.motionX, this.motionY, this.motionZ, this.func_70182_d(), 1.0F);
		this.rotationPitch = 0;
        this.prevRotationPitch = 0;
	}

	public EntityGrenadeBouncyBase(World world, double posX, double posY, double posZ) {
		super(world);
		this.setSize(0.25F, 0.25F);
		this.setPosition(posX, posY, posZ);
	}

	@Override
	protected void entityInit() {
	}

	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double p_70112_1_) {
		double d1 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;
		d1 *= 64.0D;
		return p_70112_1_ < d1 * d1;
	}

	protected float func_70182_d() {
		return 1.5F;
	}

	protected float func_70183_g() {
		return 0.0F;
	}

	protected float getGravityVelocity() {
		return 0.03F;
	}

	public void shoot(double motionX, double motionY, double motionZ, float f0, float f1) {
		float f2 = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
		motionX /= (double) f2;
		motionY /= (double) f2;
		motionZ /= (double) f2;
		motionX += this.rand.nextGaussian() * 0.007499999832361937D * (double) f1;
		motionY += this.rand.nextGaussian() * 0.007499999832361937D * (double) f1;
		motionZ += this.rand.nextGaussian() * 0.007499999832361937D * (double) f1;
		motionX *= (double) f0;
		motionY *= (double) f0;
		motionZ *= (double) f0;
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
		this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
	}

	@SideOnly(Side.CLIENT)
	public void setVelocity(double motionX, double motionY, double motionZ) {
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.prevRotationPitch = this.rotationPitch;

        this.rotationPitch -= Vec3NT.createVectorHelper(motionX, motionY, motionZ).length() * 25;
		
		double d0 = this.motionX;
		double d1 = this.motionY;
		double d2 = this.motionZ;

		if (!this.hasNoGravity()) {
			this.motionY -= 0.03999999910593033D;
		}

		if (this.world.isRemote) {
			this.noClip = false;
		} else {
			this.noClip = this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);
		}

		this.moveBounce(this.motionX, this.motionY, this.motionZ);

		float f = 0.98F;

		if (this.onGround) {
			BlockPos underPos = new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ));
			net.minecraft.block.state.IBlockState underState = this.world.getBlockState(underPos);
			f = underState.getBlock().getSlipperiness(underState, this.world, underPos, this) * 0.98F;
		}

		this.motionX *= (double) f;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= (double) f;

		this.handleWaterMovement();

		if (!this.world.isRemote) {
			double d3 = this.motionX - d0;
			double d4 = this.motionY - d1;
			double d5 = this.motionZ - d2;
			double d6 = d3 * d3 + d4 * d4 + d5 * d5;

			if (d6 > 0.01D) {
				this.isAirBorne = true;
			}
		}
		timer++;

		if (timer >= getMaxTimer() && !world.isRemote) {
			if(!CompatibilityConfig.isWarDim(world)){
	            this.setDead();
	            return;
	        }
			explode();

			String s = "null";

			if (thrower != null && thrower instanceof EntityPlayer)
				s = ((EntityPlayer) thrower).getDisplayName().getUnformattedText();

			if (GeneralConfig.enableExtendedLogging)
				MainRegistry.logger.log(Level.INFO, "[GREN] Set off grenade at " + ((int) posX) + " / " + ((int) posY) + " / " + ((int) posZ) + " by " + s + "!");
		}
	}

	// Drillgon200: Grenade bounce mark 2, copy from Entity#move
	public void moveBounce(double x, double y, double z) {
		if (this.noClip) {
			this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, y, z));
			this.resetPositionToBB();
		} else {

			this.world.profiler.startSection("move");
			if (this.isInWeb) {
				this.isInWeb = false;
				x *= 0.25D;
				y *= 0.05000000074505806D;
				z *= 0.25D;
				this.motionX = 0.0D;
				this.motionY = 0.0D;
				this.motionZ = 0.0D;
			}

			double d2 = x;
			double d3 = y;
			double d4 = z;

			List<AxisAlignedBB> list1 = this.world.getCollisionBoxes(this, this.getEntityBoundingBox().expand(x, y, z));

			if (y != 0.0D) {
				int k = 0;

				for (int l = list1.size(); k < l; ++k) {
					y = ((AxisAlignedBB) list1.get(k)).calculateYOffset(this.getEntityBoundingBox(), y);
				}

				this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));
			}

			if (x != 0.0D) {
				int j5 = 0;

				for (int l5 = list1.size(); j5 < l5; ++j5) {
					x = ((AxisAlignedBB) list1.get(j5)).calculateXOffset(this.getEntityBoundingBox(), x);
				}

				if (x != 0.0D) {
					this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));
				}
			}

			if (z != 0.0D) {
				int k5 = 0;

				for (int i6 = list1.size(); k5 < i6; ++k5) {
					z = ((AxisAlignedBB) list1.get(k5)).calculateZOffset(this.getEntityBoundingBox(), z);
				}

				if (z != 0.0D) {
					this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));
				}
			}

			this.world.profiler.endSection();
			this.world.profiler.startSection("rest");
			this.resetPositionToBB();
			this.collidedHorizontally = d2 != x || d4 != z;
			this.collidedVertically = d3 != y;
			this.onGround = this.collidedVertically && d3 < 0.0D;
			this.collided = this.collidedHorizontally || this.collidedVertically;
			int j6 = MathHelper.floor(this.posX);
			int i1 = MathHelper.floor(this.posY - 0.20000000298023224D);
			int k6 = MathHelper.floor(this.posZ);
			BlockPos blockpos = new BlockPos(j6, i1, k6);
			IBlockState iblockstate = this.world.getBlockState(blockpos);

			if (iblockstate.getMaterial() == Material.AIR) {
				BlockPos blockpos1 = blockpos.down();
				IBlockState iblockstate1 = this.world.getBlockState(blockpos1);
				Block block1 = iblockstate1.getBlock();

				if (block1 instanceof BlockFence || block1 instanceof BlockWall || block1 instanceof BlockFenceGate) {
					iblockstate = iblockstate1;
					blockpos = blockpos1;
				}
			}

			this.updateFallState(y, this.onGround, iblockstate, blockpos);

			if (d2 != x) {
				this.motionX = 0.0D;
			}

			if (d4 != z) {
				this.motionZ = 0.0D;
			}

			
			
			// Start bounce
			boolean bounce = false;
			boolean collidedX = d2 != x;
			boolean collidedY = d3 != y;
			boolean collidedZ = d4 != z;
			if (this.collided) {


				if (collidedY)
					motionY *= -1;
				if (collidedZ)
					motionZ *= -1;
				if (collidedX)
					motionX *= -1;
				
				bounce = true;
				Vec3d mot = new Vec3d(motionX, motionY, motionZ);
				if (mot.length() > 0.05)
					world.playSound(null, this.posX, this.posY, this.posZ, HBMSoundHandler.grenadeBounce, SoundCategory.HOSTILE, 2.0F, 1.0F);

				motionX *= getBounceMod()*1.5;
				motionY *= getBounceMod()*1.5;
				motionZ *= getBounceMod()*1.5;
			}
			// End bounce

			Block block = iblockstate.getBlock();

			if (d3 != y && !bounce) {
				block.onLanded(this.world, this);
			}

			try {
				this.doBlockCollisions();
			} catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
				CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being checked for collision");
				this.addEntityCrashInfo(crashreportcategory);
				throw new ReportedException(crashreport);
			}

			this.world.profiler.endSection();
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		timer = nbt.getInteger("timer");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("timer", timer);
	}

	@SideOnly(Side.CLIENT)
	public float getShadowSize() {
		return 0.0F;
	}

	public EntityLivingBase getThrower() {
		if (this.thrower == null && this.throwerName != null && this.throwerName.length() > 0) {
			this.thrower = this.world.getPlayerEntityByName(this.throwerName);
		}

		return this.thrower;
	}

	public abstract void explode();

	protected abstract int getMaxTimer();

	protected abstract double getBounceMod();
}
