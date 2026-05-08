package com.hbm.entity.projectile;

import com.hbm.config.CompatibilityConfig;
import com.hbm.entity.effect.EntityCloudTom;
import com.hbm.entity.logic.EntityTomBlast;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IConstantRenderer;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
@AutoRegister(name = "entity_tom_the_moonstone", trackingRange = 1000)
public class EntityTom extends EntityThrowable implements IConstantRenderer {

	public EntityTom(World p_i1582_1_) {
		super(p_i1582_1_);
		this.ignoreFrustumCheck = true;
	}
	
	@Override
	public void onUpdate() {


		this.lastTickPosX = this.prevPosX = posX;
		this.lastTickPosY = this.prevPosY = posY;
		this.lastTickPosZ = this.prevPosZ = posZ;
		this.setPosition(posX + this.motionX, posY + this.motionY, posZ + this.motionZ);
		
		if(this.ticksExisted % 100 == 0) {
			world.playSound(null, posX, posY, posZ, HBMSoundHandler.chime, SoundCategory.HOSTILE, 10000, 1.0F);
		}
        
		motionY = -0.5;
        
        if(this.world.getBlockState(new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ)).getBlock() != Blocks.AIR || this.posY < 10)
        {
    		if(!this.world.isRemote) {
    			if(CompatibilityConfig.isWarDim(world)){
	    			EntityTomBlast tom = new EntityTomBlast(world);
	    			tom.posX = posX;
	    			tom.posY = posY;
	    			tom.posZ = posZ;
	    			tom.destructionRange = 600;
	    			world.spawnEntity(tom);

					EntityCloudTom cloud = new EntityCloudTom(world, 500);
					cloud.setLocationAndAngles(posX, posY, posZ, 0, 0);
					world.spawnEntity(cloud);
	    		}
    		}
    		this.setDead();
        }
	}

	@Override
	protected void onImpact(RayTraceResult p_70184_1_) {
	}
	
    @Override
	@SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        return distance < 500000;
    }

    @Override
	@SideOnly(Side.CLIENT)
    public int getBrightnessForRender()
    {
        return 15728880;
    }

    @Override
	public float getBrightness()
    {
        return 1.0F;
    }
}
