package com.hbm.entity.logic;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import cofh.redstoneflux.api.IEnergyProvider;
import com.hbm.config.CompatibilityConfig;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.ParticleBurstPacket;
import com.hbm.util.Compat;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.List;
@AutoRegister(name = "entity_emp", trackingRange = 1000)
public class EntityEMP extends Entity implements IChunkLoader {

	List<BlockPos> machines;
	int life = 10 * 60 * 20;
	private Ticket loaderTicket;
	private boolean awaitingTicketRestore;

	public EntityEMP(World p_i1582_1_) {
		super(p_i1582_1_);
	}
	
	@Override
	public void onUpdate() {
		
		if(!world.isRemote) {
			requestChunkLoaderTicketIfNeeded();
			if(!CompatibilityConfig.isWarDim(world)){
				this.setDead();
				return;
			}
			if(machines == null) {
				allocate();
			} else {
				shock();
			}
			
			if(this.ticksExisted > life)
				this.setDead();
		}
	}
	
	private void allocate() {
		
		machines = new ArrayList<BlockPos>();
		
		int radius = 100;
		
		for(int x = -radius; x <= radius; x++) {
			
			int x2 = (int) Math.pow(x, 2);
			
			for(int y = -radius; y <= radius; y++) {
				
				int y2 = (int) Math.pow(y, 2);
				
				for(int z = -radius; z <= radius; z++) {
					
					int z2 = (int) Math.pow(z, 2);
					
					if(Math.sqrt(x2 + y2 + z2) <= radius) {
						add(new BlockPos((int)posX + x, (int)posY + y, (int)posZ + z));
					}
				}
			}
		}
	}
	
	private void shock() {
		
		for(BlockPos pos : machines) {
			emp(pos);
		}
	}
	
	private void add(BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if(te == null)
			return;
		if(te instanceof IEnergyReceiverMK2 || te.hasCapability(CapabilityEnergy.ENERGY, null) || Compat.REDSTONE_FLUX_LOADED && te instanceof IEnergyProvider){
            machines.add(pos);
        }
	}
	
	private void emp(BlockPos pos) {
		
		TileEntity te = world.getTileEntity(pos);
		if(te == null)
			return;
		boolean flag = false;
		
		if (te instanceof IEnergyReceiverMK2) {
			((IEnergyReceiverMK2)te).setPower(0);
			flag = true;
		} else if(te.hasCapability(CapabilityEnergy.ENERGY, null)){
            IEnergyStorage handle = te.getCapability(CapabilityEnergy.ENERGY, null);
            handle.extractEnergy(handle.getEnergyStored(), false);
            flag = true;
        } else if (Compat.REDSTONE_FLUX_LOADED && te instanceof IEnergyProvider p) {
            p.extractEnergy(EnumFacing.UP, p.getEnergyStored(EnumFacing.UP), false);
            p.extractEnergy(EnumFacing.DOWN, p.getEnergyStored(EnumFacing.DOWN), false);
            p.extractEnergy(EnumFacing.NORTH, p.getEnergyStored(EnumFacing.NORTH), false);
            p.extractEnergy(EnumFacing.SOUTH, p.getEnergyStored(EnumFacing.SOUTH), false);
            p.extractEnergy(EnumFacing.EAST, p.getEnergyStored(EnumFacing.EAST), false);
            p.extractEnergy(EnumFacing.WEST, p.getEnergyStored(EnumFacing.WEST), false);
            flag = true;
        }
		if(flag && rand.nextInt(20) == 0) {
			PacketDispatcher.wrapper.sendToAll(new ParticleBurstPacket(pos.getX(), pos.getY(), pos.getZ(), Block.getIdFromBlock(Blocks.STAINED_GLASS), 3));
		}
	}

	@Override
	protected void entityInit() {
	}

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
                loadNeighboringChunks(chunkCoordX, chunkCoordZ);
            }
        }
	}

	List<ChunkPos> loadedChunks = new ArrayList<ChunkPos>();
	@Override
	public void loadNeighboringChunks(int newChunkX, int newChunkZ) {
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
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		awaitingTicketRestore = true;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) { }

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

	private void requestChunkLoaderTicketIfNeeded() {
		if(world.isRemote || loaderTicket != null) return;
		if(awaitingTicketRestore) {
			awaitingTicketRestore = false;
			return;
		}
		init(ForgeChunkManager.requestTicket(MainRegistry.instance, world, Type.ENTITY));
	}
}
