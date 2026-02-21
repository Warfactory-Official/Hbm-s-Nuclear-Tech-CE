package com.hbm.entity.mob;

import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IRadiationImmune;
import com.hbm.items.ModItems;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.MutableVec3d;
import com.hbm.world.WorldUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;
@AutoRegister(name = "entity_glowing_one", trackingRange = 1000)
public class EntityGlowingOne extends EntityZombie implements IRadiationImmune {

	public static final int effectRadius = 16;

	public EntityGlowingOne(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(250.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10.0D);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(2.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(10.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(5.0D);
	}

	@Override
    public void onLivingUpdate() {
    	ContaminationUtil.radiate(world, posX, posY, posZ, 16, 50);

		List<EntityZombie> entities = world.getEntitiesWithinAABB(EntityZombie.class, new AxisAlignedBB(posX, posY, posZ, posX, posY, posZ).grow(effectRadius, effectRadius, effectRadius));
		
		for(EntityZombie e : entities) {
			
			Vec3 vec = Vec3.createVectorHelper(e.posX - posX, (e.posY + e.getEyeHeight()) - posY, e.posZ - posZ);
			double len = vec.length();
			if(len < effectRadius){
				e.heal((float)(0.02 * (effectRadius-len)));
			}
		}
        super.onLivingUpdate();
    }

    public static void convertInRadiusToGlow(WorldServer world, double x, double y, double z, double radius){
    	List<EntityZombie> entities = WorldUtil.getEntitiesWithinAABB(world, EntityZombie.class, new AxisAlignedBB(x, y, z, x, y, z).grow(radius, radius, radius));
		MutableVec3d vec3d = new MutableVec3d();
		for(EntityZombie e : entities) {
			if(e instanceof EntityGlowingOne)
    			continue;
			vec3d.set(e.posX - x, (e.posY + e.getEyeHeight()) - y, e.posZ - z);
			double len = vec3d.length();
			if(len < radius){
				convertToGlow(world, e);
			}
		}
    }

    public static void convertToGlow(World world, EntityZombie zombie){
    	if(zombie instanceof EntityGlowingOne)
    		return;
    	EntityGlowingOne glowing = new EntityGlowingOne(world);
    	glowing.setChild(zombie.isChild());
		glowing.setLocationAndAngles(zombie.posX, zombie.posY, zombie.posZ, zombie.rotationYaw, zombie.rotationPitch);

		if(!zombie.isDead)
			if(!world.isRemote)
				world.spawnEntity(glowing);
		zombie.setDead();
    }

    @Override
    protected boolean canDespawn() {
    	return false;
    }

    @Override
    protected int getExperiencePoints(EntityPlayer player){
    	return 200;
    }

    @Override
    protected boolean shouldBurnInDay(){
    	return false;
    }

   	@Override
   	protected boolean isValidLightLevel(){
   		return false;
   	}

	@Override
	public boolean attackEntityAsMob(Entity x){
		return true;
	}

	@Override
	protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
		super.dropLoot(wasRecentlyHit, lootingModifier, source);
		this.dropItem(ModItems.cap_rad, Math.max(1, lootingModifier));
	}
}
