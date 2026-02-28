package com.hbm.handler.neutron;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public abstract class NeutronNode {

    protected NeutronStream.NeutronType type;
    public BlockPos pos;
    public TileEntity tile;

    public NeutronNode(TileEntity tile, NeutronStream.NeutronType type) {
        this.type = type;
        this.tile = tile;
        this.pos = tile.getPos();
    }
}
