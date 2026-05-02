package com.hbm.mixin.mod.celeritas;

import com.hbm.render.chunk.IExtraExtentsHolder;
import net.minecraft.tileentity.TileEntity;
import org.embeddedt.embeddium.impl.render.chunk.data.BuiltRenderSectionData;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(value = BuiltRenderSectionData.class, remap = false)
public abstract class MixinBuiltRenderSectionData implements IExtraExtentsHolder {

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

    /**
     * {@link org.embeddedt.embeddium.impl.render.chunk.RenderSection#setInfo} only accepts a new info object when
     * {@link java.util.Objects#equals} reports a change. Without folding the injected extent/spanning-TESR fields
     * into equality, a rebuild that only alters those fields is discarded — which leaves stale spanning TESRs
     * stuck in {@link com.hbm.render.chunk.ChunkSpanningTesrHelper} and skips oversized-model AABB updates.
     */
    @Dynamic
    @Inject(method = "equals", at = @At("RETURN"), cancellable = true, require = 2)
    private void hbm$includeExtentsInEquals(Object o, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }
        IExtraExtentsHolder other = (IExtraExtentsHolder) o;
        if (hbm$negX != other.hbm$getNegX() || hbm$posX != other.hbm$getPosX()
                || hbm$negY != other.hbm$getNegY() || hbm$posY != other.hbm$getPosY()
                || hbm$negZ != other.hbm$getNegZ() || hbm$posZ != other.hbm$getPosZ()
                || !Arrays.equals(hbm$chunkSpanningTesrs, other.hbm$getChunkSpanningTesrs())) {
            cir.setReturnValue(false);
        }
    }

    @Dynamic
    @Inject(method = "hashCode", at = @At("RETURN"), cancellable = true, require = 1)
    private void hbm$includeExtentsInHashCode(CallbackInfoReturnable<Integer> cir) {
        int hash = cir.getReturnValueI();
        hash = 31 * hash + hbm$negX;
        hash = 31 * hash + hbm$posX;
        hash = 31 * hash + hbm$negY;
        hash = 31 * hash + hbm$posY;
        hash = 31 * hash + hbm$negZ;
        hash = 31 * hash + hbm$posZ;
        hash = 31 * hash + Arrays.hashCode(hbm$chunkSpanningTesrs);
        cir.setReturnValue(hash);
    }
}
