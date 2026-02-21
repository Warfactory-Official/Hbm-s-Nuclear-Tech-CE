package com.hbm.entity.missile;

import com.hbm.blocks.ModBlocks;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.interfaces.AutoRegister;
import com.hbm.util.ParticleUtil;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
@AutoRegister(name = "entity_miner_rocket", trackingRange = 1000)
public class EntityMinerRocket extends Entity {

    public static final DataParameter<Integer> MODE = EntityDataManager.createKey(EntityMinerRocket.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> SAT = EntityDataManager.createKey(EntityMinerRocket.class, DataSerializers.VARINT);
    // ↓ Alcater addition
	public static final DataParameter<Byte> TYPE = EntityDataManager.createKey(EntityMinerRocket.class, DataSerializers.BYTE);

	// 0 landing, 1 unloading, 2 lifting
	public int timer = 0;
	public byte type = 0;

	public EntityMinerRocket(World p_i1582_1_) {
		super(p_i1582_1_);
		this.ignoreFrustumCheck = true;
		this.setSize(1F, 3F);
	}

	public EntityMinerRocket(World p_i1582_1_, byte type) {
		this(p_i1582_1_);
		this.type = type;
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(MODE, 0);
        this.dataManager.register(SAT, 0);
		this.dataManager.register(TYPE, type);
	}

	public byte getRocketType(){
		return this.dataManager.get(TYPE);
	}

	public void setRocketType(byte value){
		this.type = value;
		this.dataManager.set(TYPE, value);
	}
	
	@Override
	public void onUpdate() {
        switch (this.getDataManager().get(MODE)) {
            case 0 -> motionY = -0.75;
            case 1 -> motionY = 0;
            case 2 -> motionY = 1;
        }
		motionX = 0;
		motionZ = 0;
		
		this.setPositionAndRotation(posX + motionX, posY + motionY, posZ + motionZ, 0.0F, 0.0F);

		
		if(this.getDataManager().get(MODE) == 0 && world.getBlockState(new BlockPos((int)(posX - 0.5), (int)(posY - 0.5), (int)(posZ - 0.5))).getBlock() == ModBlocks.sat_dock) {
			this.getDataManager().set(MODE, 1);
			motionY = 0;
			posY = (int)posY;
		} else if(!world.isRemote && !world.isAirBlock(new BlockPos((int)(posX - 0.5), (int)(posY + 1), (int)(posZ - 0.5))) && this.getDataManager().get(MODE) != 1) {
			this.setDead();
			ExplosionLarge.explodeFire(world, null, posX - 0.5, posY, posZ - 0.5, 10F, true, false, true);
		}
		
		if(this.getDataManager().get(MODE) == 1) {
			
			if(ticksExisted % 4 == 0)
				ExplosionLarge.spawnShock(world, posX, posY, posZ, 1 + rand.nextInt(3), 1 + rand.nextGaussian());
			
			timer++;
			
			if(timer > 100) {
				this.getDataManager().set(MODE, 2);
			}
		}
		int mode = this.getDataManager().get(MODE);

        if (mode != 1 && ticksExisted % 2 == 0) {
            ParticleUtil.spawnGasFlame(world, posX, posY - 0.5, posZ, 0.0, -1.0, 0.0);
        }
		
		if(mode == 2 && posY > 300)
			this.setDead();
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
        dataManager.set(MODE, compound.getInteger("mode"));
        dataManager.set(SAT, compound.getInteger("sat"));
        dataManager.set(TYPE, compound.getByte("type"));
        timer = compound.getInteger("timer");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("mode", dataManager.get(MODE));
        compound.setInteger("sat", dataManager.get(SAT));
        compound.setByte("type", dataManager.get(TYPE));
        compound.setInteger("timer", timer);
	}
}
