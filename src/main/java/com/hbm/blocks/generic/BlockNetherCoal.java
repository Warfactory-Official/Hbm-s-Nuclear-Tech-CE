package com.hbm.blocks.generic;

import com.hbm.items.ModItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockNetherCoal extends BlockOutgas {

	public BlockNetherCoal(boolean randomTick, int rate, boolean onBreak, String s) {
		super(randomTick, rate, onBreak, s);
	}

	@Override
	public void onEntityWalk(World world, BlockPos pos, Entity entity){
		entity.setFire(3);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune){
		return ModItems.coal_infernal;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand){
		super.randomDisplayTick(stateIn, worldIn, pos, rand);
		for(EnumFacing dir : EnumFacing.VALUES) {

			if(dir == EnumFacing.DOWN) continue;
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
            BlockPos neighbour = pos.offset(dir);
            IBlockState blockState = worldIn.getBlockState(neighbour);
			if(blockState.getBlock().isAir(blockState, worldIn, neighbour)) {

				double ix = x + 0.5F + dir.getXOffset() + rand.nextDouble() - 0.5D;
				double iy = y + 0.5F + dir.getYOffset() + rand.nextDouble() - 0.5D;
				double iz = z + 0.5F + dir.getZOffset() + rand.nextDouble() - 0.5D;

				if(dir.getAxis() == EnumFacing.Axis.X)
					ix = x + 0.5F + dir.getXOffset() * 0.5 + rand.nextDouble() * 0.125 * dir.getXOffset();
				if(dir.getAxis() == EnumFacing.Axis.Y)
					iy = y + 0.5F + dir.getYOffset() * 0.5 + rand.nextDouble() * 0.125 * dir.getYOffset();
				if(dir.getAxis() == EnumFacing.Axis.Z)
					iz = z + 0.5F + dir.getZOffset() * 0.5 + rand.nextDouble() * 0.125 * dir.getZOffset();

				worldIn.spawnParticle(EnumParticleTypes.FLAME, ix, iy, iz, 0.0, 0.0, 0.0);
				worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, ix, iy, iz, 0.0, 0.0, 0.0);
				worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, ix, iy, iz, 0.0, 0.1, 0.0);
			}
		}
	}
}