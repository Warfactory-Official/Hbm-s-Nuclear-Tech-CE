package com.hbm.entity.projectile;

import com.hbm.config.CompatibilityConfig;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.Library;
import com.hbm.lib.ModDamageSource;
import com.hbm.util.Vec3NT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
@AutoRegister(name = "entity_laser", trackingRange = 1000)
public class EntityLaser extends Entity {

	public static final DataParameter<String> PLAYER_NAME = EntityDataManager.createKey(EntityLaser.class, DataSerializers.STRING);
	
	public EntityLaser(World worldIn) {
		super(worldIn);
		this.ignoreFrustumCheck = true;
	}
	
	public EntityLaser(World world, EntityPlayer player, EnumHand hand) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.getDataManager().set(PLAYER_NAME, player.getName());
		
		Vec3d vec1 = player.getLookVec();
		Vec3NT vec = Vec3NT.createVectorHelper(vec1.x, vec1.y, vec1.z);
		if(hand == EnumHand.OFF_HAND){
			vec.rotateYawSelf(90F);
		} else {
			vec.rotateYawSelf(-90F);
		}
		float l = 0.25F;
		vec.setX(vec.x * (l));
		vec.setY(vec.y * (l));
		vec.setZ(vec.z * (l));
		
		this.setPosition(player.posX + vec.x, player.posY + player.getEyeHeight(), player.posZ + vec.z);
		
	}

	@Override
	protected void entityInit() {
		this.getDataManager().register(PLAYER_NAME, "");
	}

	@Override
	public void onUpdate() {
		if(this.ticksExisted > 1)
			this.setDead();
		
		int range = 100;
		
		EntityPlayer player = world.getPlayerEntityByName(this.getDataManager().get(PLAYER_NAME));
		
		if(player != null) {
			
			//this.setPosition(player.posX, player.posY + player.getEyeHeight(), player.posZ);
			
			RayTraceResult pos = Library.rayTrace(player, range, 1);
			
			//worldObj.createExplosion(this, pos.hitVec.x, pos.hitVec.y, pos.hitVec.z, 1, false);
			
			world.spawnParticle(EnumParticleTypes.CLOUD, pos.hitVec.x, pos.hitVec.y, pos.hitVec.z, 0, 0, 0);
			world.playSound(pos.hitVec.x, pos.hitVec.y, pos.hitVec.z, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1, 1, true);
			
			if(CompatibilityConfig.isWarDim(world)){
				List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.hitVec.x - 1, pos.hitVec.y - 1, pos.hitVec.z - 1, pos.hitVec.x + 1, pos.hitVec.y + 1, pos.hitVec.z + 1));
				
				for(Entity e : list)
					e.attackEntityFrom(ModDamageSource.radiation, 5);
			}
		}
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
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
