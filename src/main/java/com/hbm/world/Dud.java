package com.hbm.world;

import com.hbm.blocks.BlockEnumMeta;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockCrashedBomb;
import com.hbm.config.GeneralConfig;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

// mlbv: this can't cause cascading worldgen..
public class Dud extends WorldGenerator
{
	
	protected Block[] GetValidSpawnBlocks()
	{
		return new Block[]
		{
			Blocks.GRASS,
			Blocks.DIRT,
			Blocks.STONE,
			Blocks.SAND,
			Blocks.SANDSTONE,
		};
	}

	public boolean LocationIsValidSpawn(World world, BlockPos pos)
 {

		IBlockState checkBlockState = world.getBlockState(pos.down());
		Block checkBlock = checkBlockState.getBlock();
		Block blockAbove = world.getBlockState(pos).getBlock();
		Block blockBelow = world.getBlockState(pos.down(2)).getBlock();

		for (Block i : GetValidSpawnBlocks())
		{
			if (blockAbove != Blocks.AIR)
			{
				return false;
			}
			if (checkBlock == i)
			{
				return true;
			}
			else if (checkBlock == Blocks.SNOW_LAYER && blockBelow == i)
			{
				return true;
			}
			else if (checkBlockState.getMaterial() == Material.PLANTS && blockBelow == i)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean generate(World world, Random rand, BlockPos pos)
	{
		if(!LocationIsValidSpawn(world, pos))
		{
			return false;
		}

		world.setBlockState(pos, ModBlocks.crashed_bomb.getDefaultState().withProperty(BlockEnumMeta.META, rand.nextInt(BlockCrashedBomb.EnumDudType.VALUES.length)), 2 | 16);
		return true;
	}
}
