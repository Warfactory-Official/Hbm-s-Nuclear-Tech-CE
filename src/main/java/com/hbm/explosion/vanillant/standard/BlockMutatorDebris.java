package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IBlockMutator;
import com.hbm.inventory.RecipesCommon;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Optional;

public class BlockMutatorDebris implements IBlockMutator {

	protected RecipesCommon.MetaBlock metaBlock;

	public BlockMutatorDebris(Block block) {
		this(block, 0);
	}

    public BlockMutatorDebris(String loc) {
        this(
                Optional.ofNullable(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(loc))).orElse(Blocks.STONE)
                , 0);
    }

    public BlockMutatorDebris(String loc, int meta) {
        this(
                Optional.ofNullable(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(loc))).orElse(Blocks.STONE)
                , meta);
    }

	public BlockMutatorDebris(Block block, int meta) {
		this.metaBlock = new RecipesCommon.MetaBlock(block, meta);
	}


	@Override
	public void mutatePre(ExplosionVNT explosion, IBlockState blockState, BlockPos pos) {

	}

	@Override
	public void mutatePost(ExplosionVNT explosion, BlockPos pos) {
		World world = explosion.world;

		for (EnumFacing dir : EnumFacing.VALUES) {
			IBlockState state = world.getBlockState(pos.offset(dir));
			Block adjacentBlock = state.getBlock();

			if (adjacentBlock.isNormalCube(state) && (adjacentBlock != metaBlock.block || adjacentBlock.getMetaFromState(state) != metaBlock.meta)) {
				world.setBlockState(pos, metaBlock.block.getStateFromMeta(metaBlock.meta), 3);
				return;
			}
		}
	}
}

