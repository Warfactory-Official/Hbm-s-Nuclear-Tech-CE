package com.hbm.entity.missile;

import com.hbm.api.entity.IRadarDetectable;
import com.hbm.api.entity.IThrowable;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IConstantRenderer;
import com.hbm.main.MainRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@AutoRegister(name = "entity_mirvlet", trackingRange = 1000)
public class EntityMIRV extends EntityThrowable implements IChunkLoader, IConstantRenderer, IRadarDetectable, IThrowable {
	private Ticket loaderTicket;
	public static final DataParameter<Integer> HEALTH = EntityDataManager.createKey(EntityMIRV.class, DataSerializers.VARINT);
	public int health = 25;
	
	public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_) {
			if(!this.isDead && !this.world.isRemote) {
				health -= p_70097_2_;

				if(this.health <= 0) {
					this.setDead();
					this.killMissile();
				}
			}

			return true;
		}
	

	private void killMissile() {
		ExplosionLarge.explode(world, thrower, posX, posY, posZ, 5, true, false, true);
		ExplosionLarge.spawnShrapnelShower(world, posX, posY, posZ, motionX, motionY, motionZ, 15, 0.075);
	}
	public EntityMIRV(World worldIn) {
		super(worldIn);
		this.ignoreFrustumCheck = true;
	}
	@Override
	protected void entityInit() {
		init(ForgeChunkManager.requestTicket(MainRegistry.instance, world, Type.ENTITY));
		this.getDataManager().register(HEALTH, Integer.valueOf(this.health));
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		
		this.posX += this.motionX;
		this.posY += this.motionY;
		this.posZ += this.motionZ;
		
		this.motionY -= 0.03;
        
        this.rotation();
        
        if(this.world.getBlockState(new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ)).getBlock() != Blocks.AIR)
        {
    		if(!this.world.isRemote)
    		{
    	    	world.spawnEntity(EntityNukeExplosionMK5.statFac(world, BombConfig.mirvRadius, posX, posY, posZ).setDetonator(thrower));
    	    	if(BombConfig.enableNukeClouds) {
					EntityNukeTorex.statFac(world, posX, posY, posZ, BombConfig.mirvRadius);
				}
    		}
    		this.setDead();
    		ForgeChunkManager.unforceChunk(loaderTicket, new ChunkPos(chunkCoordX, chunkCoordZ));
        }
	}
	
	protected void rotation() {
        float f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

        for (this.rotationPitch = (float)(Math.atan2(this.motionY, f2) * 180.0D / Math.PI) - 90; this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
        {
            ;
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
        {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw < -180.0F)
        {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
        {
            this.prevRotationYaw += 360.0F;
        }
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        return distance < 25000;
    }
	
	@Override
	protected void onImpact(RayTraceResult result) {
		
	}

	@Override
	public RadarTargetType getTargetType() {
		return RadarTargetType.MIRVLET;
	}

	@Override
	public void init(Ticket ticket) {
		if(!world.isRemote) {

	          if(ticket != null) {

	              if(loaderTicket == null) {

	            	loaderTicket = ticket;
	            	loaderTicket.bindEntity(this);
	            	loaderTicket.getModData();
	              }

	            ForgeChunkManager.forceChunk(loaderTicket, new ChunkPos(chunkCoordX, chunkCoordZ));
	        }
	     }
	   }
	List<ChunkPos> loadedChunks = new ArrayList<ChunkPos>();
	public void loadNeighboringChunks(int newChunkX, int newChunkZ) {
		if (!world.isRemote && loaderTicket != null) {
			for (ChunkPos chunk : loadedChunks) {
				ForgeChunkManager.unforceChunk(loaderTicket, chunk);
			}

			loadedChunks.clear();
			loadedChunks.add(new ChunkPos(newChunkX, newChunkZ));
			loadedChunks.add(new ChunkPos(newChunkX + 1, newChunkZ + 1));
			loadedChunks.add(new ChunkPos(newChunkX - 1, newChunkZ - 1));
			loadedChunks.add(new ChunkPos(newChunkX + 1, newChunkZ - 1));
			loadedChunks.add(new ChunkPos(newChunkX - 1, newChunkZ + 1));
			loadedChunks.add(new ChunkPos(newChunkX + 1, newChunkZ));
			loadedChunks.add(new ChunkPos(newChunkX, newChunkZ + 1));
			loadedChunks.add(new ChunkPos(newChunkX - 1, newChunkZ));
			loadedChunks.add(new ChunkPos(newChunkX, newChunkZ - 1));

			for (ChunkPos chunk : loadedChunks) {
				ForgeChunkManager.forceChunk(loaderTicket, chunk);
			}
		}
	}

	@Override
	public void setThrower(EntityLivingBase thrower) {
		this.thrower = thrower;
	}
}
