package com.hbm.mixin.mod.nothirium;

import com.hbm.render.chunk.IExtraExtentsHolder;
import meldexun.nothirium.util.VisibilitySet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = VisibilitySet.class, remap = false)
public abstract class MixinVisibilitySet implements IExtraExtentsHolder {

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
}
