package com.hbm.entity.effect;

import com.hbm.explosion.ExplosionNT;
import com.hbm.explosion.ExplosionNT.ExAttrib;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.AdvancementManager;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
@AutoRegister(name = "entity_spear", trackingRange = 1000)
public class EntitySpear extends Entity {
	
	public int ticksInGround;

	public EntitySpear(World p_i1582_1_) {
		super(p_i1582_1_);
		this.setSize(2F, 10F);
		this.isImmuneToFire = true;
		this.ignoreFrustumCheck = true;
	}

	@Override
	protected void entityInit() { }

	@Override
	public void onUpdate() {
		
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		
		this.motionX = 0;
		this.motionY = -0.2;
		this.motionZ = 0;

		int x = (int) Math.floor(posX);
		int y = (int) Math.floor(posY);
		int z = (int) Math.floor(posZ);
		
		if(world.getBlockState(new BlockPos(x, y - 1, z)).getMaterial() == Material.AIR) {
			this.setPositionAndRotation(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ, 0, 0);

			if(!world.isRemote) {
				double ix = posX + rand.nextGaussian() * 25;
				double iz = posZ + rand.nextGaussian() * 25;
				double iy = world.getHeight((int)Math.floor(ix), (int)Math.floor(iz)) + 2;
				
				ExAttrib at = new Vec3d(ix - posX, 0, iz - posZ).length() < 20 ? ExAttrib.DIGAMMA_CIRCUIT : ExAttrib.DIGAMMA;
				
				new ExplosionNT(world, this, ix, iy, iz, 7.5F)
				.addAttrib(ExAttrib.NOHURT)
				.addAttrib(ExAttrib.NOPARTICLE)
				.addAttrib(ExAttrib.NODROP)
				.addAttrib(ExAttrib.NOSOUND)
				.addAttrib(at).explode();
				
				for(EntityPlayer player : world.playerEntities) {
					ContaminationUtil.contaminate(player, HazardType.DIGAMMA, ContaminationType.DIGAMMA, 0.05F);
					AdvancementManager.grantAchievement(player, AdvancementManager.digammaKauaiMoho);
				}
			}
			
			if(world.isRemote) {
				
				double dy = world.getHeight((int)Math.floor(posX), (int)Math.floor(posZ)) + 2;
				
				NBTTagCompound data = new NBTTagCompound();
				data.setInteger("count", 5);
				MainRegistry.proxy.effectNT(HbmEffectNT.Smoke_RadialDigamma, posX, dy, posZ, data);
			}

			
			if(world.getBlockState(new BlockPos(x, y - 3, z)).getMaterial() == Material.AIR)
				ticksInGround = 0;
			
		} else {
			
			ticksInGround++;
			
			if(!world.isRemote && ticksInGround > 100) {
				
				List<Entity> entities =  new ArrayList<>(world.loadedEntityList);
				for(Object obj : entities) {
					
					if(obj instanceof EntityLivingBase)
						ContaminationUtil.contaminate((EntityLivingBase) obj, HazardType.DIGAMMA, ContaminationType.DIGAMMA2, 10F);
				}
				this.setDead();
				
				world.playSound(null, posX, posY, posZ, HBMSoundHandler.dflash, SoundCategory.HOSTILE, 25000.0F, 1.0F);
				
				NBTTagCompound data = new NBTTagCompound();
				data.setInteger("count", 100);
				MainRegistry.proxy.effectNT(HbmEffectNT.Smoke_RadialDigamma, posX, posY + 7, posZ, data);
			}
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound p_70037_1_) { }

	@Override
	protected void writeEntityToNBT(NBTTagCompound p_70014_1_) { }
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 25000;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender() {
		return 15728880;
	}

	@Override
	public float getBrightness() {
		return 1.0F;
	}
}