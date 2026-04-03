package com.hbm.render.chunk;

import net.minecraft.tileentity.TileEntity;

public interface IExtraExtentsHolder {

    TileEntity[] EMPTY_TE_ARR = new TileEntity[0];

    void hbm$setOversizedModelExtents(int negX, int posX, int negY, int posY, int negZ, int posZ);

    int hbm$getNegX();

    int hbm$getPosX();

    int hbm$getNegY();

    int hbm$getPosY();

    int hbm$getNegZ();

    int hbm$getPosZ();

    TileEntity[] hbm$getChunkSpanningTesrs();

    void hbm$setChunkSpanningTesrs(TileEntity[] tesrs);
}
