package com.hbm.mixin;

import com.hbm.render.chunk.IRenderFrameStamp;
import com.hbm.render.chunk.ISectionGeometryTile;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TileEntity.class)
public abstract class MixinTileEntity implements IRenderFrameStamp, ISectionGeometryTile {

    @Unique
    private int hbm$renderFrameStamp;
    @Unique
    private boolean hbm$globalRender;

    @Override
    public int hbm$getFrameStamp() {
        return hbm$renderFrameStamp;
    }

    @Override
    public void hbm$setFrameStamp(int frame) {
        hbm$renderFrameStamp = frame;
    }

    @Override
    public boolean hbm$globalRender() {
        return hbm$globalRender;
    }

    @Override
    public void hbm$globalRender(boolean global) {
        hbm$globalRender = global;
    }
}
