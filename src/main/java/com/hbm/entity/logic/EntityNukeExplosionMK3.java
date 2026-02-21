package com.hbm.entity.logic;

import com.hbm.config.BombConfig;
import com.hbm.config.CompatibilityConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.effect.EntityFalloutRain;
import com.hbm.explosion.*;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.Spaghetti;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.AdvancementManager;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

@Spaghetti("why???")
@AutoRegister(name = "entity_nuke_mk3", trackingRange = 1000)
public class EntityNukeExplosionMK3 extends EntityExplosionChunkloading {
	
	public int age = 0;
	public int destructionRange = 0;
	public ExplosionNukeAdvanced exp;
	public ExplosionNukeAdvanced wst;
	public ExplosionNukeAdvanced vap;
	public ExplosionFleija expl;
	public ExplosionSolinium sol;
	public ExplosionDrying dry;//mlbv: this one was added by Alcater
	public int speed = 1;
	public float coefficient = 1;
	public float coefficient2 = 1;
	public boolean did = false;
	public boolean did2 = false;
	public boolean waste = true;
	//Extended Type
	public int extType = 0;
	public UUID detonator = null;

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		age = nbt.getInteger("age");
		destructionRange = nbt.getInteger("destructionRange");
		speed = nbt.getInteger("speed");
		coefficient = nbt.getFloat("coefficient");
		coefficient2 = nbt.getFloat("coefficient2");
		did = nbt.getBoolean("did");
		did2 = nbt.getBoolean("did2");
		waste = nbt.getBoolean("waste");
		extType = nbt.getInteger("extType");
		if (nbt.hasKey("detonator"))
			detonator = nbt.getUniqueId("detonator");
		long time = nbt.getLong("milliTime");
		
		if(BombConfig.limitExplosionLifespan > 0 && System.currentTimeMillis() - time > BombConfig.limitExplosionLifespan * 1000) {
			this.setDead();
		}

    	if(this.waste)
    	{
        	exp = new ExplosionNukeAdvanced((int)this.posX, (int)this.posY, (int)this.posZ, this.world, this.destructionRange, this.coefficient, 0);
			exp.readFromNbt(nbt, "exp_");
			exp.detonator = detonator;
    		wst = new ExplosionNukeAdvanced((int)this.posX, (int)this.posY, (int)this.posZ, this.world, (int)(this.destructionRange * 1.8), this.coefficient, 2);
			wst.readFromNbt(nbt, "wst_");
			wst.detonator = detonator;
    		vap = new ExplosionNukeAdvanced((int)this.posX, (int)this.posY, (int)this.posZ, this.world, (int)(this.destructionRange * 2.5), this.coefficient, 1);
			vap.readFromNbt(nbt, "vap_");
			vap.detonator = detonator;
    	} else {

    		if(extType == 0) {
    			expl = new ExplosionFleija((int)this.posX, (int)this.posY, (int)this.posZ, this.world, this.destructionRange, this.coefficient, this.coefficient2);
				expl.readFromNbt(nbt, "expl_");
				expl.detonator = detonator;
    		}
    		if(extType == 1) {
    			sol = new ExplosionSolinium((int)this.posX, (int)this.posY, (int)this.posZ, this.world, this.destructionRange, this.coefficient, this.coefficient2);
    			sol.readFromNbt(nbt, "sol_");
				sol.detonator = detonator;
    		}
    		if(extType == 2) {
    			dry = new ExplosionDrying((int)this.posX, (int)this.posY, (int)this.posZ, this.world, this.destructionRange, this.coefficient, this.coefficient2);
    			dry.readFromNbt(nbt, "dry_");
				dry.detonator = detonator;
    		}
    	}
    	
    	this.did = true;
		
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("age", age);
		nbt.setInteger("destructionRange", destructionRange);
		nbt.setInteger("speed", speed);
		nbt.setFloat("coefficient", coefficient);
		nbt.setFloat("coefficient2", coefficient2);
		nbt.setBoolean("did", did);
		nbt.setBoolean("did2", did2);
		nbt.setBoolean("waste", waste);
		nbt.setInteger("extType", extType);
		
		nbt.setLong("milliTime", System.currentTimeMillis());
		if (detonator != null)
    		nbt.setUniqueId("detonator", detonator);
		if(exp != null)
			exp.saveToNbt(nbt, "exp_");
		if(wst != null)
			wst.saveToNbt(nbt, "wst_");
		if(vap != null)
			vap.saveToNbt(nbt, "vap_");
		if(expl != null)
			expl.saveToNbt(nbt, "expl_");
		if(sol != null)
			sol.saveToNbt(nbt, "sol_");
		if(dry != null)
			dry.saveToNbt(nbt, "dry_");
		
	}

	public EntityNukeExplosionMK3(World p_i1582_1_) {
		super(p_i1582_1_);
	}

    @Override
	public void onUpdate() {
        super.onUpdate();
        if(world.isRemote)
        	return;
       	if(!CompatibilityConfig.isWarDim(world)){
			this.setDead();
			return;
		}

        if (!world.isRemote)
            loadChunk(chunkCoordX, chunkCoordZ);

        if(!this.did)
        {
			for (EntityPlayer player : this.world.playerEntities) {
				AdvancementManager.grantAchievement(player, AdvancementManager.achManhattan);
			}
    		if(GeneralConfig.enableExtendedLogging && !world.isRemote)
    			MainRegistry.logger.log(Level.INFO, "[NUKE] Initialized mk3 explosion at " + posX + " / " + posY + " / " + posZ + " with strength " + destructionRange + "!");
    		
        	if(this.waste)
        	{
            	exp = new ExplosionNukeAdvanced((int)this.posX, (int)this.posY, (int)this.posZ, this.world, this.destructionRange, this.coefficient, 0);
        		wst = new ExplosionNukeAdvanced((int)this.posX, (int)this.posY, (int)this.posZ, this.world, (int)(this.destructionRange * 1.8), this.coefficient, 2);
        		vap = new ExplosionNukeAdvanced((int)this.posX, (int)this.posY, (int)this.posZ, this.world, (int)(this.destructionRange * 2.5), this.coefficient, 1);
        		exp.detonator = detonator;
				wst.detonator = detonator;
				vap.detonator = detonator;
			} else {
        		if(extType == 0) {
					expl = new ExplosionFleija((int) this.posX, (int) this.posY, (int) this.posZ, this.world, this.destructionRange, this.coefficient, this.coefficient2);
					expl.detonator = detonator;
				}
				if(extType == 1) {
					sol = new ExplosionSolinium((int) this.posX, (int) this.posY, (int) this.posZ, this.world, this.destructionRange, this.coefficient, this.coefficient2);
					sol.detonator = detonator;
				}
				if(extType == 2) {
					dry = new ExplosionDrying((int) this.posX, (int) this.posY, (int) this.posZ, this.world, this.destructionRange, this.coefficient, this.coefficient2);
					dry.detonator = detonator;
				}
			}
        	
        	this.did = true;
        }
        
        speed += 1;	//increase speed to keep up with expansion
        
        boolean flag = false;
        boolean flag3 = false;
        
        for(int i = 0; i < this.speed; i++)
        {
        	if(waste) {
        		flag = exp.update();
        		if (wst != null) {
        			wst.update();
        		}
        		flag3 = vap.update();
        		
        		if(flag3) {
        			this.setDead();
        		}
        	} else {
        		if(extType == 0)
        			if(expl.update())
        				this.setDead();
        		if(extType == 1)
        			if(sol.update())
        				this.setDead();
        		if(extType == 2)
        			if(dry.update())
        				this.setDead();
        	}
        }
        	
        if(!flag)
        {
        	this.world.playSound(this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.AMBIENT, 10000.0F, 0.8F + this.rand.nextFloat() * 0.2F, true);
			if(waste || extType != 1) {
				ExplosionNukeGeneric.dealDamage(this.world, this.posX, this.posY, this.posZ, this.destructionRange * 2);
			} else {
				ExplosionHurtUtil.doRadiation(world, posX, posY, posZ, 15000, 250000, this.destructionRange);
			}
        } else {
			if (!did2 && waste) {
				EntityFalloutRain fallout = new EntityFalloutRain(this.world, (int)(this.destructionRange * 1.8) * 10);
				fallout.posX = this.posX;
				fallout.posY = this.posY;
				fallout.posZ = this.posZ;
				fallout.detonator = detonator;
				fallout.setScale((int)(this.destructionRange * 1.8));

				this.world.spawnEntity(fallout);
				//this.world.getWorldInfo().setRaining(true);
				
				did2 = true;
        	}
        }
        age++;
    }

	public static HashMap<ATEntry, Long> at = new HashMap<>();

	private static void createParticle(World world, int dim, double x, double y, double z, float r, float g, float b) {
		world.playSound(null, x+0.5D, y+0.5D, z+0.5D, HBMSoundHandler.ufoBlast, SoundCategory.HOSTILE, 15.0F, 1.0F);
						
		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "plasmablast");
		data.setFloat("r", r);
		data.setFloat("g", g);
		data.setFloat("b", b);
		data.setFloat("scale", 7.5F);
		PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x+0.5D, y+0.5D, z+0.5D), new TargetPoint(dim, x, y, z, 150));
	}

	public static boolean isJammed(World world, Entity entity) {
		
		Iterator<Entry<ATEntry, Long>> it = at.entrySet().iterator();
		
		while(it.hasNext()) { // checking each jammer if it is in range
			
			Entry<ATEntry, Long> next = it.next();
			if(next.getValue() < world.getTotalWorldTime()) {
				it.remove();
				continue;
			}
			
			ATEntry jammer = next.getKey();
			if(jammer.dim != world.provider.getDimension())
				continue;
			
			double distance = Math.sqrt(Math.pow(entity.posX - jammer.x, 2) + Math.pow(entity.posY - jammer.y, 2) + Math.pow(entity.posZ - jammer.z, 2));
			
			if(distance < 300) {
				
				if(!world.isRemote) {
					createParticle(world, jammer.dim, entity.posX, entity.posY, entity.posZ, 1.0F, 0.5F, 0.0F);
					createParticle(world, jammer.dim, jammer.x, jammer.y, jammer.z, 0.0F, 0.75F, 1.0F);
				}
				entity.setDead();
				return true;
			}
		}
		return false;
	}

	public static EntityNukeExplosionMK3 statFacFleija(World world, double x, double y, double z, int range) {

		EntityNukeExplosionMK3 entity = new EntityNukeExplosionMK3(world);
		entity.posX = x;
		entity.posY = y;
		entity.posZ = z;
		entity.destructionRange = range;
		entity.speed = BombConfig.blastSpeed;
		entity.coefficient = 1.0F;
		entity.waste = false;

		Iterator<Entry<ATEntry, Long>> it = at.entrySet().iterator();

		while(it.hasNext()) {

			Entry<ATEntry, Long> next = it.next();
			if(next.getValue() < world.getTotalWorldTime()) {
				it.remove();
				continue;
			}

			ATEntry entry = next.getKey();
			if(entry.dim != world.provider.getDimension())  continue;

			Vec3d vec = new Vec3d(x - entry.x, y - entry.y, z - entry.z);

			if(vec.length() < 300) {
				entity.setDead();

				/* just to make sure */
				if(!world.isRemote) {

					for(int i = 0; i < 2; i++) {
						double ix = i == 0 ? x : (entry.x + 0.5);
						double iy = i == 0 ? y : (entry.y + 0.5);
						double iz = i == 0 ? z : (entry.z + 0.5);

						world.playSound(null, ix, iy, iz, HBMSoundHandler.ufoBlast, SoundCategory.PLAYERS, 15.0F, 0.7F + world.rand.nextFloat() * 0.2F);

						NBTTagCompound data = new NBTTagCompound();
						data.setString("type", "plasmablast");
						data.setFloat("r", 0.0F);
						data.setFloat("g", 0.75F);
						data.setFloat("b", 1.0F);
						data.setFloat("scale", 7.5F);
						PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, ix, iy, iz), new TargetPoint(entry.dim, ix, iy, iz, 150));
					}
				}

				break;
			}
		}

		return entity;
	}

	public void setDetonator(Entity detonator){
		if(detonator instanceof EntityPlayerMP)
			this.detonator = detonator.getUniqueID();
	}

	@Override
	public void setDead() {
        clearChunkLoader();
		super.setDead();
	}

	public static class ATEntry {
		public int dim;
		public int x;
		public int y;
		public int z;
		
		public ATEntry(int dim, int x, int y, int z) {
			this.dim = dim;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public int hashCode() {
			final int prime = 27644437;
			int result = 1;
			result = prime * result + dim;
			result = prime * result + x;
			result = prime * result + y;
			result = prime * result + z;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			ATEntry other = (ATEntry) obj;
			if(dim != other.dim)
				return false;
			if(x != other.x)
				return false;
			if(y != other.y)
				return false;
			if(z != other.z)
				return false;
			return true;
		}
	}
}
