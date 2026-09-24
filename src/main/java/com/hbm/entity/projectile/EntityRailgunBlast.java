package com.hbm.entity.projectile;

import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.MainRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;

import java.util.ArrayList;
import java.util.List;
@AutoRegister(name = "entity_railgun_pellet", trackingRange = 1000)
public class EntityRailgunBlast extends Entity implements IChunkLoader {
	private boolean awaitingTicketRestore;

	public EntityRailgunBlast(World w) {
		super(w);
	}

	private Ticket loaderTicket;

	@Override
	public void init(Ticket ticket) {
		if(!world.isRemote) {

			if(ticket != null) {

				if(loaderTicket == null) {

					loaderTicket = ticket;
					loaderTicket.bindEntity(this);
					loaderTicket.getModData();	
				} else if(loaderTicket != ticket) {
					ForgeChunkManager.releaseTicket(ticket);
				}

				ForgeChunkManager.forceChunk(loaderTicket, new ChunkPos(chunkCoordX, chunkCoordZ));
			}
		}
	}

	List<ChunkPos> loadedChunks = new ArrayList<ChunkPos>();
	
	public void loadNeighboringChunks(int newChunkX, int newChunkZ)
    {
        if(!world.isRemote && loaderTicket != null)
        {
            for(ChunkPos chunk : loadedChunks)
            {
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

            for(ChunkPos chunk : loadedChunks)
            {
                ForgeChunkManager.forceChunk(loaderTicket, chunk);
            }
        }
    }
	
	@Override
	protected void entityInit() {
	}

	@Override
	public void onUpdate() {
		if(!world.isRemote) {
			requestChunkLoaderTicketIfNeeded();
		}
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.setLocationAndAngles(posX + this.motionX, posY + this.motionY, posZ + this.motionZ, 0, 0);
		rotation();

		Vec3d vec3 = new Vec3d(this.posX, this.posY, this.posZ);
		Vec3d vec31 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
		RayTraceResult RayTraceResult = this.world.rayTraceBlocks(vec3, vec31);

		if(RayTraceResult != null) {

			if(!this.world.isRemote) {
				this.setLocationAndAngles(RayTraceResult.getBlockPos().getX(), RayTraceResult.getBlockPos().getY(), RayTraceResult.getBlockPos().getZ(), 0, 0);
				
				world.spawnEntity(EntityNukeExplosionMK5.statFac(world, BombConfig.missileRadius>>1, posX, posY, posZ));
				if(BombConfig.enableNukeClouds) {
					EntityNukeTorex.statFac(world, posX, posY, posZ, BombConfig.missileRadius>>1);
				}
			}
			this.setDead();
			return;
		}

		if(!world.isRemote) {
			loadNeighboringChunks((int) (posX / 16), (int) (posZ / 16));
		}

		// gravity needs the sec/tick converter squared since it's in seconds
		// squared
		motionY -= 9.81D * 0.05 * 0.05;
	}

	public void rotation() {
		float f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
		this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

		for(this.rotationPitch = (float) (Math.atan2(this.motionY, f2) * 180.0D / Math.PI) - 90; this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
			;
		}

		while(this.rotationPitch - this.prevRotationPitch >= 180.0F) {
			this.prevRotationPitch += 360.0F;
		}

		while(this.rotationYaw - this.prevRotationYaw < -180.0F) {
			this.prevRotationYaw -= 360.0F;
		}

		while(this.rotationYaw - this.prevRotationYaw >= 180.0F) {
			this.prevRotationYaw += 360.0F;
		}
	}

	@Override
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 500000;
	}

	@Override
	public void setDead() {
		super.setDead();
		this.clearChunkLoader();
	}

	private void clearChunkLoader() {
		if(!world.isRemote && loaderTicket != null) {
			for(ChunkPos chunk : loadedChunks) {
				ForgeChunkManager.unforceChunk(loaderTicket, chunk);
			}
			loadedChunks.clear();
			ForgeChunkManager.releaseTicket(loaderTicket);
			loaderTicket = null;
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		awaitingTicketRestore = true;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
	}

	private void requestChunkLoaderTicketIfNeeded() {
		if(world.isRemote || loaderTicket != null) return;
		if(awaitingTicketRestore) {
			awaitingTicketRestore = false;
			return;
		}
		init(ForgeChunkManager.requestTicket(MainRegistry.instance, world, Type.ENTITY));
	}
}
