package com.hbm.explosion.vanillant.standard;

import com.hbm.blocks.ModBlocks;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IBlockMutator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class BlockMutatorBalefire implements IBlockMutator {

    @Override
    public void mutatePre(ExplosionVNT explosion, IBlockState blockState, BlockPos pos) {
    }

    @Override
    public void mutatePost(ExplosionVNT explosion, BlockPos pos) {
        IBlockState blockDown = explosion.world.getBlockState(pos.down());
        if(explosion.world.isAirBlock(pos) && blockDown.isOpaqueCube() && explosion.world.rand.nextInt(3) == 0) {
            explosion.world.setBlockState(pos, ModBlocks.balefire.getDefaultState());
        }
    }
}
