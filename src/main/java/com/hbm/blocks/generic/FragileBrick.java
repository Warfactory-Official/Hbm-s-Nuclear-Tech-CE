package com.hbm.blocks.generic;

import com.hbm.blocks.BlockBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class FragileBrick extends BlockBase {

	public FragileBrick(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		if(world.isRemote)
			return;

		world.destroyBlock(pos, false);
		notifyNeighbors(world, pos);
	}

	@Override
	public void onEntityWalk(World world, BlockPos pos, Entity entity) {

		if(world.isRemote)
			return;

		world.destroyBlock(pos, false);
		notifyNeighbors(world, pos);
	}

	private void notifyNeighbors(World world, BlockPos pos) {

		for(EnumFacing dir : EnumFacing.VALUES) {

			BlockPos next = pos.offset(dir);

			if(world.getBlockState(next).getBlock() == this) {
				world.scheduleBlockUpdate(next, this, world.rand.nextInt(4) + 8, 0);
			}
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		notifyNeighbors(world, pos);
	}
}
