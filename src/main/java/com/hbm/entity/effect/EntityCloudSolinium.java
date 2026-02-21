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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
@AutoRegister(name = "entity_clound_solinium", trackingRange = 1000)
public class EntityCloudSolinium extends Entity {

	public static final DataParameter<Integer> MAXAGE = EntityDataManager.createKey(EntityCloudSolinium.class, DataSerializers.VARINT);
	
	public int maxAge = 100;
	public int age;
    public float scale = 0;
	
    public EntityCloudSolinium(World p_i1582_1_) {
		super(p_i1582_1_);
		this.setSize(1, 4);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
		this.age = 0;
    	scale = 0;
	}
    
    public EntityCloudSolinium(World p_i1582_1_, int maxAge) {
		super(p_i1582_1_);
		this.setSize(20, 40);
		this.isImmuneToFire = true;
		this.setMaxAge(maxAge);
	}

	@Override
	protected void entityInit() {
		this.getDataManager().register(MAXAGE, 0);
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
	
	
	
}
