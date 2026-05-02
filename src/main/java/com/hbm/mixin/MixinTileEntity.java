package com.hbm.mixin;

import com.hbm.render.chunk.IRenderFrameStamp;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TileEntity.class)
public abstract class MixinTileEntity implements IRenderFrameStamp {

    @Unique
    private int hbm$renderFrameStamp;

    @Override
    public int hbm$getFrameStamp() {
        return hbm$renderFrameStamp;
    }

    @Override
    public void hbm$setFrameStamp(int frame) {
        hbm$renderFrameStamp = frame;
    }
}
