package com.hbm.tileentity.network;

import com.hbm.interfaces.AutoRegister;
import com.hbm.uninos.UniNodespace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
@AutoRegister
public class TileEntityFluidValve extends TileEntityPipeBaseNT {

    @Override
    public boolean shouldCreateNode() {
        return this.getBlockMetadata() == 1;
    }

    public void updateState() {

        this.blockMetadata = -1; // delete cache

        if(!world.isRemote) {
            if(this.getBlockMetadata() == 0) {
                if(this.node != null) {
                    UniNodespace.destroyNode(world, node);
                    this.node = null;
                }
            } else {
                // при включении обязаны пересоздать/подцепить ноду, иначе update() больше не тикает
                this.node = null;
                this.markDirty();

                IBlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, 3);
            }
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

}
