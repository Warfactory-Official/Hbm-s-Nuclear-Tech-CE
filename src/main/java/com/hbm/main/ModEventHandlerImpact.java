package com.hbm.main;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.ImpactWorldHandler;
import com.hbm.saveddata.TomSaveData;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ModEventHandlerImpact {
	
	//////////////////////////////////////////
	private static Random rand = new Random();
	//////////////////////////////////////////
	
	@SubscribeEvent
	public void worldTick(TickEvent.WorldTickEvent event) {
		if (event.world == null || event.world.isRemote || event.phase != TickEvent.Phase.START) {
			return;
		}

		float settle = 1F / 14400000F;  // 600 days to clear all dust
		float cool = 1F / 24000F;       // One MC day for fire duration

		ImpactWorldHandler.impactEffects(event.world);
		TomSaveData data = TomSaveData.forWorld(event.world);
		// Th3_Sl1ze: for now I'll remove whatever NTM Space handled, we'll rely on default NTM processing..
		if(data.dust > 0 && data.fire == 0) {
			data.dust = Math.max(0, data.dust - settle);
			data.markDirty();
		}

		if(data.fire > 0) {
			data.fire = Math.max(0, (data.fire - cool));
			data.dust = Math.min(1, (data.dust + cool));
			data.markDirty();
		}

		if(!event.world.loadedEntityList.isEmpty()) {

			List<Object> oList = new ArrayList<Object>();
			oList.addAll(event.world.loadedEntityList);

			for(Object e : oList) {
				if(e instanceof EntityLivingBase) {
					EntityLivingBase entity = (EntityLivingBase) e;

					if(entity.world.provider.getDimension() == 0 && data.fire > 0 && data.dust < 0.75f &&
							event.world.getLightFor(EnumSkyBlock.SKY, new BlockPos((int) entity.posX, (int) entity.posY, (int) entity.posZ)) > 7) {

						entity.setFire(5);
						entity.attackEntityFrom(DamageSource.ON_FIRE, 2);
					}
				}
			}
		}

	}

	//data is always pooled out of the perWorld save data so resetting values isn't needed
	/*@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onUnload(WorldEvent.Unload event) {
		// We don't want Tom's impact data transferring between worlds.
		TomSaveData data = TomSaveData.forWorld(event.world);
		this.fire = 0;
		this.dust = 0;
		this.impact = false;
		data.fire = 0;
		data.dust = 0;
		data.impact = false;
	}*/

	@SubscribeEvent
	public void extinction(CheckSpawn event) {
		
		TomSaveData data = TomSaveData.forWorld(event.getWorld());
		
		if(data.impact) {
			if(!(event.getEntity() instanceof EntityPlayer) && event.getEntity() instanceof EntityLivingBase) {
				if(event.getWorld().provider.getDimension() == 0) {
					if(event.getEntity().height >= 0.85F || event.getEntity().width >= 0.85F && !(event.getEntity() instanceof EntityWaterMob) && !((EntityLivingBase) event.getEntity()).isChild()) {
						event.setResult(Event.Result.DENY);
						event.getEntity().setDead();
					}
				}
				if(event.getEntity() instanceof EntityWaterMob) {
					Random rand = new Random();
					if(rand.nextInt(5) != 0) {
						event.setResult(Event.Result.DENY);
						event.getEntity().setDead();
					}
				}
			}		
		}		
	}

	@SubscribeEvent
	public void onPopulate(Populate event) {
		
		if(event.getType() == Populate.EventType.ANIMALS) {
			
			TomSaveData data = TomSaveData.forWorld(event.getWorld());
			
			if(data.impact) { // OHHH THIS IS WHAT I WAS FUCKING MISSING. WHY FORGE WHY???? WHY THE FUCK DID YOU ADVERTISE THE CANCELSPAWN EVENTHANDLER WHEN THIS EXISTS???
				event.setResult(Event.Result.DENY);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLoad(WorldEvent.Load event) {
		TomSaveData.resetLastCached();
	}

	@SubscribeEvent
	public void modifyVillageGen(BiomeEvent.GetVillageBlockID event) {
		Block b = event.getOriginal().getBlock();
		Material mat = event.getOriginal().getMaterial();
		
		TomSaveData data = TomSaveData.getLastCachedOrNull();
		
		if(data == null || event.getBiome() == null) {
			return;
		}
		
		if(data.impact) {
			if(mat == Material.WOOD || mat == Material.GLASS || b == Blocks.LADDER || b instanceof BlockCrops ||
					b == Blocks.CHEST || b instanceof BlockDoor || mat == Material.CLOTH || mat == Material.WATER || b == Blocks.STONE_SLAB) {
				event.setReplacement(Blocks.AIR.getDefaultState());
				
			} else if(b == Blocks.COBBLESTONE || b == Blocks.STONEBRICK) {
				if(rand.nextInt(3) == 1) {
					event.setReplacement(Blocks.GRAVEL.getDefaultState());
				}
			} else if(b == Blocks.SANDSTONE) {
				if(rand.nextInt(3) == 1) {
					event.setReplacement(Blocks.SAND.getDefaultState());
				}
			} else if(b == Blocks.FARMLAND) {
				event.setReplacement(Blocks.DIRT.getDefaultState());
			}
		}
		
		if(event.getReplacement() != null) {
			event.setResult(Event.Result.DENY);
		}
	}

	
	@SubscribeEvent
	public void postImpactGeneration(BiomeEvent event) {
		/// Disables post-impact surface replacement for superflat worlds
		/// because they are retarded and crash with a NullPointerException if
		/// you try to look for biome-specific blocks.
		TomSaveData data = TomSaveData.getLastCachedOrNull(); //despite forcing the data, we cannot rule out canceling events or custom firing shenanigans 
		if(data != null && event.getBiome() != null) {
			if(event.getBiome().topBlock != null) {
				if(event.getBiome().topBlock.getBlock() == Blocks.GRASS) {
					if(data.impact && (data.dust > 0 || data.fire > 0)) {
						event.getBiome().topBlock = ModBlocks.impact_dirt.getDefaultState();
					} else {
						event.getBiome().topBlock = Blocks.GRASS.getDefaultState();
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void postImpactDecoration(DecorateBiomeEvent.Decorate event) {
		
		TomSaveData data = TomSaveData.forWorld(event.getWorld());
		
		if(data.impact) {
			EventType type = event.getType();
			
			if(data.dust > 0 || data.fire > 0) {
				if(type == event.getType().TREE || type == event.getType().BIG_SHROOM || type == event.getType().GRASS || type == event.getType().REED || type == event.getType().FLOWERS || type == event.getType().DEAD_BUSH
						|| type == event.getType().CACTUS || type == event.getType().PUMPKIN || type == event.getType().LILYPAD) {
					event.setResult(Event.Result.DENY);
				}
				
			} else if(data.dust == 0 && data.fire == 0) {
				if(type == event.getType().TREE || type == event.getType().BIG_SHROOM || type == event.getType().CACTUS) {
					Random rand = new Random();
					if(rand.nextInt(4) == 0) {
						event.setResult(Event.Result.DEFAULT);
					} else {
						event.setResult(Event.Result.DENY);
					}
				}
				
				if(type == event.getType().GRASS || type == event.getType().REED) {
					event.setResult(Event.Result.DEFAULT);
				}
			}
			
		} else {
			event.setResult(Event.Result.DEFAULT);
		}
	}

	@SubscribeEvent
	public void populateChunkPre(PopulateChunkEvent.Pre event) {
		TomSaveData.forWorld(event.getWorld()); /* forces the data to be cached so it is accurate by the time ModEventHandlerImpact#modifyVillageGen is called. */
	}

	@SubscribeEvent
	public void populateChunkPost(PopulateChunkEvent.Post event) {
		
		TomSaveData data = TomSaveData.forWorld(event.getWorld());
		
		if(data.impact) {
			Chunk chunk = event.getWorld().getChunk(event.getChunkX(), event.getChunkZ());
			
			for(ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
				
				if(storage != null) {
					
					for(int x = 0; x < 16; ++x) {
						for(int y = 0; y < 16; ++y) {
							for(int z = 0; z < 16; ++z) {
								
								if(data.dust > 0.25 || data.fire > 0) {
									if(storage.get(x, y, z) == Blocks.GRASS.getDefaultState()) {
										storage.set(x, y, z, ModBlocks.impact_dirt.getDefaultState());
									} else if(storage.get(x, y, z) instanceof BlockLog) {
										storage.set(x, y, z, Blocks.AIR.getDefaultState());
									} else if(storage.get(x, y, z) instanceof BlockLeaves) {
										storage.set(x, y, z, Blocks.AIR.getDefaultState());
									} else if(storage.get(x, y, z).getMaterial() == Material.LEAVES) {
										storage.set(x, y, z, Blocks.AIR.getDefaultState());
									} else if(storage.get(x, y, z).getMaterial() == Material.PLANTS) {
										storage.set(x, y, z, Blocks.AIR.getDefaultState());
									} else if(storage.get(x, y, z) instanceof BlockBush) {
										storage.set(x, y, z, Blocks.AIR.getDefaultState());
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
