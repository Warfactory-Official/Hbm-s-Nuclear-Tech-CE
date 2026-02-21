package com.hbm.entity.effect;

import com.hbm.config.CompatibilityConfig;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
@AutoRegister(name = "entity_cloud_fleija", trackingRange = 1000)
public class EntityCloudFleija extends Entity {

	public static final DataParameter<Integer> MAXAGE = EntityDataManager.createKey(EntityCloudFleija.class, DataSerializers.VARINT);
	
	public int maxAge = 100;
	public int age;
    public float scale = 0;
	
	public EntityCloudFleija(World worldIn) {
		super(worldIn);
		this.setSize(1, 4);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
		this.age = 0;
    	scale = 0;
	}
	
	public EntityCloudFleija(World p_i1582_1_, int maxAge) {
		super(p_i1582_1_);
		this.setSize(20, 40);
		this.isImmuneToFire = true;
		this.setMaxAge(maxAge);
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(MAXAGE, 0);
		
	}
	
	@Override
	public int getBrightnessForRender() {
		return 15728880;
	}
	
	@Override
	public float getBrightness() {
		return 1.0F;
	}
	@Override
	public void onUpdate() {
		if(!CompatibilityConfig.isWarDim(world)){
			this.setDead();
			return;
		}
		this.age++;
        this.world.spawnEntity(new EntityLightningBolt(this.world, this.posX, this.posY + 200, this.posZ, true));
        
        if(this.age >= this.getMaxAge())
        {
    		this.age = 0;
        	this.setDead();
        }
        
        this.scale++;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.age = compound.getShort("age");
		this.scale = compound.getShort("scale");
		
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setShort("age", (short) age);
		compound.setShort("scale", (short) scale);
	}
	
	public void setMaxAge(int maxAge) {
		this.dataManager.set(MAXAGE, maxAge);
	}
	
	public int getMaxAge() {
		return this.dataManager.get(MAXAGE);
	}
	
	@Override
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 25000;
	}

}
