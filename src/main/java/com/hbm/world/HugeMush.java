package com.hbm.world;

import com.hbm.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

// non-natural worldgen
public class HugeMush extends WorldGenerator {

	Block Block0 = ModBlocks.mush_block;
	Block Block1 = ModBlocks.mush_block_stem;
	private final MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
	
    public HugeMush()
    {
        super(false);
    }

    @Override
	public boolean generate(World world, Random rand, BlockPos pos1)
    {
    	MutableBlockPos pos = this.mutablePos;
    	pos.setPos(pos1);
    	for(int i = -1; i < 2; i++)
    	{
        	for(int j = -1; j < 2; j++)
        	{
        		pos.setPos(pos1.getX() + i, pos1.getY(), pos1.getZ() + j);
        		world.setBlockState(pos, Block0.getDefaultState());
        	}
    	}
    	for(int i = -1; i < 2; i++)
    	{
        	for(int j = -1; j < 2; j++)
        	{
        		pos.setPos(pos1.getX()+i, pos1.getY()+3, pos1.getZ()+j);
        		world.setBlockState(pos, Block0.getDefaultState());
        	}
    	}
    	for(int i = -2; i < 3; i++)
    	{
        	for(int j = -2; j < 3; j++)
        	{
        		pos.setPos(pos1.getX()+i, pos1.getY()+5, pos1.getZ()+j);
        		world.setBlockState(pos, Block0.getDefaultState());
        	}
    	}
    	for(int i = -4; i < 5; i++)
    	{
        	for(int j = -4; j < 5; j++)
        	{
            	for(int k = 0; k < 3; k++)
            	{
            		pos.setPos(pos1.getX()+i, pos1.getY()+6+k, pos1.getZ()+j);
            		world.setBlockState(pos, Block0.getDefaultState());
            	}
        	}
    	}
    	for(int i = -3; i < 4; i++)
    	{
        	for(int j = -3; j < 4; j++)
        	{
        		pos.setPos(pos1.getX()+i, pos1.getY()+9, pos1.getZ()+j);
        		world.setBlockState(pos, Block0.getDefaultState());
        	}
    	}
    	for(int i = -1; i < 2; i++)
    	{
        	for(int j = -1; j < 2; j++)
        	{
        		pos.setPos(pos1.getX()+i, pos1.getY()+10, pos1.getZ()+j);
        		world.setBlockState(pos, Block0.getDefaultState());
        	}
    	}
    	for(int i = 0; i < 8; i++)
    	{
    		pos.setPos(pos1.getX(), pos1.getY()+i, pos1.getZ());
    		world.setBlockState(pos, Block1.getDefaultState());
    	}
    	return true;
    }


}
