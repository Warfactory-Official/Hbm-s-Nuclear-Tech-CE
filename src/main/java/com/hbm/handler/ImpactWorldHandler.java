package com.hbm.handler;

import com.hbm.blocks.ModBlocks;
import com.hbm.saveddata.TomSaveData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockVine;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.stream.Collectors;

public class ImpactWorldHandler {

	public static void impactEffects(World world) {

		if(!(world instanceof WorldServer serv))
			return;

		if(world.provider.getDimension() != 0) {
			return;
		}

        ChunkProviderServer chunkProvider = serv.getChunkProvider();
		List<Chunk> loadedChunks = chunkProvider.getLoadedChunks().stream().collect(Collectors.toList());
		int listSize = loadedChunks.size();

		if (listSize > 0) {
			for (int i = 0; i < 3; i++) {

				Chunk chunk = loadedChunks.get(serv.rand.nextInt(listSize));
				ChunkPos coord = chunk.getPos();

				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {

						if (world.rand.nextBoolean()) continue;

						int X = coord.getXStart() + x;
						int Z = coord.getZStart() + z;
						int Y = world.getHeight(new BlockPos(X, 0, Z)).getY() - world.rand.nextInt(Math.max(1, world.getHeight(new BlockPos(X, 0, Z)).getY()));

						TomSaveData data = TomSaveData.forWorld(world);
						BlockPos pos = new BlockPos(X, Y, Z);

						if (data.dust > 0) {
							die(world, pos);
						}
						if (data.fire > 0) {
							burn(world, pos);
						}
					}
				}
			}
		}
	}

	/// Plants die without sufficient light.
	public static void die(World world, BlockPos pos) {

		TomSaveData data = TomSaveData.forWorld(world);
		int light = Math.max(world.getLightFor(EnumSkyBlock.BLOCK, pos.up()), (int) (world.getLight(pos.up()) * (1 - data.dust)));
		if(light < 4) {
			if(world.getBlockState(pos).getBlock() == Blocks.GRASS) {
				world.setBlockState(pos, Blocks.DIRT.getDefaultState());
			} else if(world.getBlockState(pos).getBlock() instanceof BlockBush) {
				world.setBlockToAir(pos);
			} else if(world.getBlockState(pos).getBlock() instanceof BlockLeaves) {
				world.setBlockToAir(pos);
			} else if(world.getBlockState(pos).getBlock() instanceof BlockVine) {
				world.setBlockToAir(pos);
			}
		}
	}

	/// Burn the world.
	public static void burn(World world, BlockPos pos) {

		Block b = world.getBlockState(pos).getBlock();
		if(b.isFlammable(world, pos, EnumFacing.UP) && world.getBlockState(pos.up()) == Blocks.AIR && world.getLightFor(EnumSkyBlock.SKY, pos.up()) >= 7) {
			if(b instanceof BlockLeaves || b instanceof BlockBush) {
				world.setBlockToAir(pos);
			}
			world.setBlockState(pos.up(), Blocks.FIRE.getDefaultState());
			
		} else if((b == Blocks.GRASS || b == Blocks.MYCELIUM || b == ModBlocks.waste_earth || b == ModBlocks.frozen_grass || b == ModBlocks.waste_mycelium) &&
				!world.provider.canDoLightning(new Chunk(world, pos.getX(), pos.getZ())) && world.getLightFor(EnumSkyBlock.SKY, pos.up()) >= 7) {
			world.setBlockState(pos, ModBlocks.burning_earth.getDefaultState());
			
		} else if(b == ModBlocks.frozen_dirt && world.getLightFor(EnumSkyBlock.SKY, pos.up()) >= 7) {
			world.setBlockState(pos, Blocks.DIRT.getDefaultState());
		}
	}

	public static World lastSyncWorld = null;
	public static float fire = 0F;
	public static float dust = 0F;
	public static long time = 0;
	public static boolean impact = false;

	@SideOnly(Side.CLIENT)
	public static float getFireForClient(World world) {
		if(world != lastSyncWorld) return 0F;
		return fire;
	}

	@SideOnly(Side.CLIENT)
	public static float getDustForClient(World world) {
		if(world != lastSyncWorld) return 0F;
		return dust;
	}

	@SideOnly(Side.CLIENT)
	public static boolean getImpactForClient(World world) {
		if(world != lastSyncWorld) return false;
		return impact;
	}
	
	@SideOnly(Side.CLIENT)
	public static long getTimeForClient(World world) {
		if(world != lastSyncWorld) return 0;
		return time;
	}
	
}