package com.hbm.entity.projectile;

import com.hbm.config.CompatibilityConfig;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.util.Vec3NT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
@AutoRegister(name = "entity_building", trackingRange = 1000)
public class EntityBuilding extends EntityThrowable {

	public EntityBuilding(World p_i1582_1_) {
		super(p_i1582_1_);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
	}
	
	@Override
	public void onUpdate() {

		this.lastTickPosX = this.prevPosX = posX;
		this.lastTickPosY = this.prevPosY = posY;
		this.lastTickPosZ = this.prevPosZ = posZ;
		this.setPosition(posX + this.motionX, posY + this.motionY, posZ + this.motionZ);
		
		this.motionY -= 0.03;
		if(motionY < -1.5)
			motionY = -1.5;
        
        if(this.world.getBlockState(new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ)).getBlock() != Blocks.AIR)
        {
            this.world.playSound(null, this.posX, this.posY, this.posZ, HBMSoundHandler.oldExplosion, SoundCategory.BLOCKS, 10000.0F, 0.5F + this.rand.nextFloat() * 0.1F);
    		this.setDead();
        	ExplosionLarge.spawnParticles(world, posX, posY + 3, posZ, 150);
        	ExplosionLarge.spawnShock(world, posX, posY + 1, posZ, 24, 6);
    		ExplosionLarge.spawnShock(world, posX, posY + 1, posZ, 24, 5);
    		ExplosionLarge.spawnShock(world, posX, posY + 1, posZ, 24, 4);
    		ExplosionLarge.spawnShock(world, posX, posY + 1, posZ, 24, 3);
    		ExplosionLarge.spawnShock(world, posX, posY + 1, posZ, 24, 3);
    		if(CompatibilityConfig.isWarDim(world)){
	    		List<Entity> list = (List<Entity>)world.getEntitiesWithinAABBExcludingEntity(null, 
	    				new AxisAlignedBB(posX - 8, posY - 8, posZ - 8, posX + 8, posY + 8, posZ + 8));
	    			
	    		for(Entity e : list) {
	    			e.attackEntityFrom(ModDamageSource.building, 1000);
	    		}
	    		
	    		for(int i = 0; i < 250; i++) {
	    			
	    			Vec3NT vec = Vec3NT.createVectorHelper(1, 0, 0);
	    			vec.rotateRollSelf((float) (-rand.nextFloat() * Math.PI / 2));
	    			vec.rotateYawSelf((float) (rand.nextFloat() * Math.PI * 2));
	    			
	    			EntityRubble rubble = new EntityRubble(world, posX, posY + 3, posZ);
	    			rubble.setMetaBasedOnBlock(Blocks.BRICK_BLOCK, 0);
	    			rubble.motionX = vec.x;
	    			rubble.motionY = vec.y;
	    			rubble.motionZ = vec.z;
	    			world.spawnEntity(rubble);
	    		}
	    	}
        }
    }

	@Override
	protected void onImpact(RayTraceResult p_70184_1_) {
		
	}
	
    @Override
	@SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        return distance < 25000;
    }
}
