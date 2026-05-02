package com.hbm.mixin;

import com.hbm.render.chunk.IExtraExtentsHolder;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CompiledChunk.class)
public abstract class MixinCompiledChunk implements IExtraExtentsHolder {

    @Unique
    private int hbm$negX;
    @Unique
    private int hbm$posX;
    @Unique
    private int hbm$negY;
    @Unique
    private int hbm$posY;
    @Unique
    private int hbm$negZ;
    @Unique
    private int hbm$posZ;
    @Unique
    private TileEntity[] hbm$chunkSpanningTesrs = EMPTY_TE_ARR;

    @Override
    public void hbm$setOversizedModelExtents(int negX, int posX, int negY, int posY, int negZ, int posZ) {
        hbm$negX = negX;
        hbm$posX = posX;
        hbm$negY = negY;
        hbm$posY = posY;
        hbm$negZ = negZ;
        hbm$posZ = posZ;
    }

    @Override
    public int hbm$getNegX() {
        return hbm$negX;
    }

    @Override
    public int hbm$getPosX() {
        return hbm$posX;
    }

    @Override
    public int hbm$getNegY() {
        return hbm$negY;
    }

    @Override
    public int hbm$getPosY() {
        return hbm$posY;
    }

    @Override
    public int hbm$getNegZ() {
        return hbm$negZ;
    }

    @Override
    public int hbm$getPosZ() {
        return hbm$posZ;
    }

    @Override
    public TileEntity[] hbm$getChunkSpanningTesrs() {
        return hbm$chunkSpanningTesrs;
    }

    @Override
    public void hbm$setChunkSpanningTesrs(TileEntity[] tesrs) {
        hbm$chunkSpanningTesrs = tesrs;
    }
}
