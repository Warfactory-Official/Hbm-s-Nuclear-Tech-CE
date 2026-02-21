package com.hbm.entity.projectile;

import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IConstantRenderer;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
@AutoRegister(name = "entity_zeta", trackingRange = 1000)
public class EntityBombletZeta extends EntityThrowable implements IConstantRenderer {

	public int type = 0;

	public EntityBombletZeta(World p_i1582_1_) {
		super(p_i1582_1_);
		this.ignoreFrustumCheck = true;
	}
	
	@Override
	public void onUpdate() {


		this.lastTickPosX = this.prevPosX = posX;
		this.lastTickPosY = this.prevPosY = posY;
		this.lastTickPosZ = this.prevPosZ = posZ;
		this.setPosition(posX + this.motionX, posY + this.motionY, posZ + this.motionZ);
		
		/*this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		
		this.posX += this.motionX;
		this.posY += this.motionY;
		this.posZ += this.motionZ;*/
		
		this.motionX *= 0.99;
		this.motionZ *= 0.99;
		this.motionY -= 0.05D;
        
        this.rotation();
        BlockPos pos = new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ);
        if(this.world.getBlockState(pos).getBlock() != Blocks.AIR)
        {
    		if(!this.world.isRemote)
    		{
    			if(type == 0) {
    				ExplosionLarge.explode(world, thrower, this.posX + 0.5F, this.posY + 0.5F, this.posZ + 0.5F, 5.0F, true, false, false);
    	        	world.playSound(posX + 0.5F, posY + 0.5F, posZ + 0.5F, HBMSoundHandler.bombDet, SoundCategory.HOSTILE, 25.0F, 0.8F + rand.nextFloat() * 0.4F, true);
    			}
    			if(type == 1) {
    				ExplosionLarge.explode(world, thrower, this.posX + 0.5F, this.posY + 0.5F, this.posZ + 0.5F, 2.5F, false, false, false);
    				ExplosionChaos.burn(world, thrower, pos, 9);
    				ExplosionChaos.flameDeath(world, thrower, pos, 14);
    	        	world.playSound(posX + 0.5F, posY + 0.5F, posZ + 0.5F, HBMSoundHandler.bombDet, SoundCategory.HOSTILE, 25.0F, 1.0F, true);
    	        	
    	        	for(int i = 0; i < 5; i++)
    	        		ExplosionLarge.spawnBurst(world, this.posX + 0.5F, this.posY + 1.0F, this.posZ + 0.5F, rand.nextInt(10) + 15, rand.nextFloat() * 2 + 2);
    			}
    			if(type == 2) {
    	        	world.playSound(posX + 0.5F, posY + 0.5F, posZ + 0.5F, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.HOSTILE, 5.0F, 2.6F + (rand.nextFloat() - rand.nextFloat()) * 0.8F, true);
    				ExplosionChaos.spawnChlorine(world, this.posX + 0.5F - motionX, this.posY + 0.5F - motionY, this.posZ + 0.5F - motionZ, 75, 2, 0);
    			}
    			if(type == 4) {
    				world.spawnEntity(EntityNukeExplosionMK5.statFac(world, (int) (BombConfig.fatmanRadius * 1.5), posX, posY, posZ).setDetonator(thrower));
    				
    				if(BombConfig.enableNukeClouds) {
                        if(rand.nextInt(100) == 0) {
                            EntityNukeTorex.statFacBale(world, this.posX, this.posY, this.posZ, (int) (BombConfig.fatmanRadius * 1.5));
                        } else {
                            EntityNukeTorex.statFac(world, this.posX, this.posY, this.posZ, (int) (BombConfig.fatmanRadius * 1.5));
                        }
					}
                    // mlbv: 1.7 needed to play sound manually because they used a AuxParticlePacketNT instead of EntityNukeTorex.
                    // This was kept for reference, DO NOT UNCOMMENT
//    				world.playSound(null, posX, posY, posZ, HBMSoundHandler.mukeExplosion, SoundCategory.HOSTILE, 15.0F, 1.0F);
    			}
    		}
    		this.setDead();
        }
	}
	
	public void rotation() {
        float f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

        for (this.rotationPitch = (float)(Math.atan2(this.motionY, f2) * 180.0D / Math.PI) - 90; this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
        {
            ;
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
        {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw < -180.0F)
        {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
        {
            this.prevRotationYaw += 360.0F;
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
