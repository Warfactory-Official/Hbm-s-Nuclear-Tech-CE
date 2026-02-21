package com.hbm.entity.logic;

import com.hbm.config.CompatibilityConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.explosion.ExplosionBalefire;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.MainRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@AutoRegister(name = "entity_balefire", trackingRange = 1000)
public class EntityBalefire extends EntityExplosionChunkloading {

	public int age = 0;
	public int destructionRange = 0;
	public ExplosionBalefire exp;
	public int speed = 1;
	public boolean did = false;
	@Nullable
	public UUID detonator = null;

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		age = nbt.getInteger("age");
		destructionRange = nbt.getInteger("destructionRange");
		speed = nbt.getInteger("speed");
		did = nbt.getBoolean("did");
    	
		exp = new ExplosionBalefire((int)this.posX, (int)this.posY, (int)this.posZ, this.world, this.destructionRange);
		exp.readFromNbt(nbt, "exp_");
    	
    	this.did = true;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("age", age);
		nbt.setInteger("destructionRange", destructionRange);
		nbt.setInteger("speed", speed);
		nbt.setBoolean("did", did);
		
		if(exp != null)
			exp.saveToNbt(nbt, "exp_");
		
	}

	public EntityBalefire(World p_i1582_1_) {
		super(p_i1582_1_);
	}

    @Override
	public void onUpdate() {
        super.onUpdate();
        if(!CompatibilityConfig.isWarDim(world)){
			this.setDead();
			return;
		}
        if (!world.isRemote)
            loadChunk(chunkCoordX, chunkCoordZ);

        if(!this.did) {
    		if(GeneralConfig.enableExtendedLogging && !world.isRemote)
    			MainRegistry.logger.log(Level.INFO, "[NUKE] Initialized BF explosion at " + posX + " / " + posY + " / " + posZ + " with strength " + destructionRange + "!");
    		
        	exp = new ExplosionBalefire((int)this.posX, (int)this.posY, (int)this.posZ, this.world, this.destructionRange);
        	exp.detonator = detonator;
        	this.did = true;
        }
        
        speed += 1;	//increase speed to keep up with expansion
        
        boolean flag = false;
        for(int i = 0; i < this.speed; i++) {
        	flag = exp.update();
        	
        	if(flag) {
        		this.setDead();
        	}
        }
        
        if(!flag) {
            ExplosionNukeGeneric.dealDamage(this.world, this.posX, this.posY, this.posZ, this.destructionRange * 2);
        }
        
        age++;
    }

	public void setDetonator(Entity detonator){
		if (detonator instanceof EntityPlayerMP)
			this.detonator = detonator.getUniqueID();
	}

    @Override
    public void setDead() {
        clearChunkLoader(); //mlbv: upstream did this before setDead() but it's brittle; I moved it here
        super.setDead();
    }
}
