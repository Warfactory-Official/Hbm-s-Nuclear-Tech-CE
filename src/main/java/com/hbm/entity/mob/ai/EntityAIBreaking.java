package com.hbm.entity.mob.ai;

import com.hbm.util.Vec3NT;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;

public class EntityAIBreaking extends EntityAIBase {

	EntityLivingBase target;
	int[] markedLoc;
	EntityLiving entityDigger;
	int digTick = 0;
	int scanTick = 0;

	public EntityAIBreaking(EntityLiving entity)
	{
		this.entityDigger = entity;
	}

	@Override
	public boolean shouldExecute()
	{
		target = entityDigger.getAttackTarget();

		if(target != null && entityDigger.getNavigator().noPath() && entityDigger.getDistance(target) > 1D && (target.onGround || !entityDigger.canEntityBeSeen(target)))
		{
			RayTraceResult mop = GetNextObstical(entityDigger, 2D);

			if(mop == null || mop.typeOfHit != Type.BLOCK)
			{
				return false;
			}

			IBlockState block = entityDigger.world.getBlockState(mop.getBlockPos());

			if(block.getBlockHardness(entityDigger.world, mop.getBlockPos()) >= 0) {
				markedLoc = new int[]{mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ()};
				return true;
			}
		}

		return false;
	}
	
	@Override
	public boolean shouldContinueExecuting()
	{
		//return target != null && entityDigger != null && target.isEntityAlive() && entityDigger.isEntityAlive() && markedLoc != null && entityDigger.getNavigator().noPath() && entityDigger.getDistanceToEntity(target) > 1D && (target.onGround || !entityDigger.canEntityBeSeen(target));

		if(markedLoc != null)  {

			Vec3NT vector = Vec3NT.createVectorHelper(
					markedLoc[0] - entityDigger.posX,
					markedLoc[1] - (entityDigger.posY + entityDigger.getEyeHeight()),
					markedLoc[2] - entityDigger.posZ);

			return entityDigger != null && entityDigger.isEntityAlive() && vector.length() <= 4;
		}

		return false;
	}

	@Override
	public void updateTask()
	{
		RayTraceResult mop = null;

    	if(entityDigger.ticksExisted % 10 == 0)
    	{
    		mop = GetNextObstical(entityDigger, 2D);
    	}

		if(mop != null && mop.typeOfHit == Type.BLOCK)
		{
			markedLoc = new int[]{mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ()};
		}

		if(markedLoc == null || markedLoc.length != 3 || entityDigger.world.getBlockState(new BlockPos(markedLoc[0], markedLoc[1], markedLoc[2])).getBlock() == Blocks.AIR)
		{
			digTick = 0;
			return;
		}

		IBlockState block = entityDigger.world.getBlockState(new BlockPos(markedLoc[0], markedLoc[1], markedLoc[2]));
		digTick++;

		int health = (int) block.getBlockHardness(entityDigger.world, new BlockPos(markedLoc[0], markedLoc[1], markedLoc[2])) / 3;

		if(health < 0) {
			markedLoc = null;
			return;
		}

		float str = (digTick * 0.05F) / (float)health;

		if(str >= 1F)
		{
			digTick = 0;

			boolean canHarvest = false;
			entityDigger.world.destroyBlock(new BlockPos(markedLoc[0], markedLoc[1], markedLoc[2]), canHarvest);
			markedLoc = null;

			if(target != null)
				entityDigger.getNavigator().setPath(entityDigger.getNavigator().getPathToEntityLiving(target), 1D);
		} else
		{
			if(digTick % 5 == 0)
			{
				SoundType sound = block.getBlock().getSoundType(block, entityDigger.world, new BlockPos(markedLoc[0], markedLoc[1], markedLoc[2]), entityDigger);
				entityDigger.world.playSound(null, entityDigger.posX, entityDigger.posY, entityDigger.posZ, sound.getBreakSound(), SoundCategory.BLOCKS, sound.volume + 1F, sound.pitch);
				entityDigger.swingArm(EnumHand.MAIN_HAND);
				entityDigger.world.sendBlockBreakProgress(entityDigger.getEntityId(), new BlockPos(markedLoc[0], markedLoc[1], markedLoc[2]), (int)(str * 10F));
			}
		}
	}

	@Override
	public void resetTask()
	{
		markedLoc = null;
		digTick = 0;
	}

	/**
	 * Rolls through all the points in the bounding box of the entity and raycasts them toward it's current heading to return any blocks that may be obstructing it's path.
	 * The bigger the entity the longer this calculation will take due to the increased number of points (Generic bipeds should only need 2)
	 */
    public RayTraceResult GetNextObstical(EntityLivingBase entityLiving, double dist)
    {
    	// Returns true if something like Iguana Tweaks is nerfing the vanilla picks. This will then cause zombies to ignore the harvestability of blocks when holding picks
        float f = 1.0F;
        float f1 = entityLiving.prevRotationPitch + (entityLiving.rotationPitch - entityLiving.prevRotationPitch) * f;
        float f2 = entityLiving.prevRotationYaw + (entityLiving.rotationYaw - entityLiving.prevRotationYaw) * f;

        int digWidth = MathHelper.ceil(entityLiving.width);
        int digHeight = MathHelper.ceil(entityLiving.height);

        int passMax = digWidth * digWidth * digHeight;

        int x = scanTick%digWidth - (digWidth/2);
        int y = scanTick/(digWidth * digWidth);
        int z = (scanTick%(digWidth * digWidth))/digWidth - (digWidth/2);

		double rayX = x + entityLiving.posX;
		double rayY = y + entityLiving.posY;
		double rayZ = z + entityLiving.posZ;

		RayTraceResult mop = RayCastBlocks(entityLiving.world, rayX, rayY, rayZ, f2, f1, dist, false);

    	if(mop != null && mop.typeOfHit == Type.BLOCK)
    	{
    		IBlockState block = entityLiving.world.getBlockState(mop.getBlockPos());

    		if(block.getBlockHardness(entityLiving.world, mop.getBlockPos()) >= 0)
    		{
    			scanTick = 0;
    			return mop;
    		} else
    		{
    			scanTick = (scanTick + 1)%passMax;
    			return null;
    		}
    	} else
    	{
			scanTick = (scanTick + 1)%passMax;
			return null;
    	}
    }

    public static RayTraceResult RayCastBlocks(World world, double x, double y, double z, float yaw, float pitch, double dist, boolean liquids)
    {
        Vec3NT vec3 = Vec3NT.createVectorHelper(x, y, z);
        float f3 = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f4 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f5 = -MathHelper.cos(-pitch * 0.017453292F);
        float f6 = MathHelper.sin(-pitch * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d3 = dist; // Ray Distance
        Vec3NT vec31 = vec3.add((double)f7 * d3, (double)f6 * d3, (double)f8 * d3);
        return RayCastBlocks(world, vec3, vec31, liquids);
    }

    public static RayTraceResult RayCastBlocks(World world, Vec3NT vector1, Vec3NT vector2, boolean liquids)
    {
        return world.rayTraceBlocks(vector1.toVec3d(), vector2.toVec3d(), liquids, !liquids, false);
    }
}