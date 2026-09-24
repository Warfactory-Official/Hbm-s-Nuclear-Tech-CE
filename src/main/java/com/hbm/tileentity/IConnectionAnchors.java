package com.hbm.tileentity;

import com.hbm.lib.DirPos;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IConnectionAnchors {

    DirPos[] getConPos();

    static void notifyAnchors(TileEntity te) {
        if (te == null) return;
        World w = te.getWorld();
        if (w == null || w.isRemote) return;
        Block source = te.getBlockType();
        BlockPos from = te.getPos();
        if (te instanceof IConnectionAnchors anchors) {
            for (DirPos d : anchors.getConPos()) {
                w.neighborChanged(d.getPos(), source, from);
            }
        } else {
            w.notifyNeighborsOfStateChange(from, source, false);
        }
    }
}
