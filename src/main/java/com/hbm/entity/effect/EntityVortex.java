package com.hbm.entity.effect;

import com.hbm.interfaces.AutoRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
@AutoRegister(name = "entity_vortex", trackingRange = 1000)
public class EntityVortex extends EntityBlackHole {

	protected float shrinkRate = 0.0025F;

	public EntityVortex(World world) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
	}

	public EntityVortex(World world, float size) {
		super(world);
		this.getDataManager().set(SIZE, size);
	}

	public EntityVortex setShrinkRate(float shrinkRate) {
		this.shrinkRate = shrinkRate;
		return this;
	}
	
	@Override
	public void onUpdate() {
		
		this.getDataManager().set(SIZE, this.getDataManager().get(SIZE) - 0.0025F);
		if(this.getDataManager().get(SIZE) <= 0) {
			this.setDead();
			return;
		}
		
		super.onUpdate();
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.shrinkRate = nbt.getFloat("shrinkRate");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setFloat("shrinkRate", this.shrinkRate);
	}
}
