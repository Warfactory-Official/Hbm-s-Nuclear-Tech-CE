package com.hbm.mixin.mod.nothirium;

import com.hbm.render.chunk.IExtraExtentsHolder;
import meldexun.nothirium.api.renderer.chunk.IRenderChunk;
import meldexun.nothirium.renderer.chunk.AbstractRenderChunk;
import meldexun.nothirium.util.VisibilitySet;
import meldexun.renderlib.util.Frustum;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractRenderChunk.class, remap = false)
public abstract class MixinAbstractRenderChunk implements IRenderChunk {

    @Unique
    private int hbm$negX, hbm$posX, hbm$negY, hbm$posY, hbm$negZ, hbm$posZ;

    @Dynamic
    @Inject(method = "setVisibility", at = @At("RETURN"), require = 1)
    private void hbm$cacheExpansion(VisibilitySet visibilitySet, CallbackInfo ci) {
        if (visibilitySet != null) {
            IExtraExtentsHolder holder = (IExtraExtentsHolder) visibilitySet;
            hbm$negX = holder.hbm$getNegX();
            hbm$posX = holder.hbm$getPosX();
            hbm$negY = holder.hbm$getNegY();
            hbm$posY = holder.hbm$getPosY();
            hbm$negZ = holder.hbm$getNegZ();
            hbm$posZ = holder.hbm$getPosZ();
        } else {
            hbm$negX = 0;
            hbm$posX = 0;
            hbm$negY = 0;
            hbm$posY = 0;
            hbm$negZ = 0;
            hbm$posZ = 0;
        }
    }

    /**
     * @author movblock
     * @reason Expand frustum AABB for sections containing oversized baked multiblock models.
     */
    @Overwrite
    public boolean isFrustumCulled(Frustum frustum) {
        int x = getX();
        int y = getY();
        int z = getZ();
        return !frustum.isAABBInFrustum(
                x - hbm$negX, y - hbm$negY, z - hbm$negZ,
                x + 16 + hbm$posX, y + 16 + hbm$posY, z + 16 + hbm$posZ);
    }

    /**
     * @author movblock
     * @reason Expand fog distance AABB for sections containing oversized baked multiblock models.
     */
    @Overwrite
    public boolean isFogCulled(double cameraX, double cameraY, double cameraZ, double fogEndSqr) {
        int ox = getX();
        int oy = getY();
        int oz = getZ();
        double x = Math.max(ox - hbm$negX, Math.min(cameraX, ox + 16 + hbm$posX)) - cameraX;
        double y = Math.max(oy - hbm$negY, Math.min(cameraY, oy + 16 + hbm$posY)) - cameraY;
        double z = Math.max(oz - hbm$negZ, Math.min(cameraZ, oz + 16 + hbm$posZ)) - cameraZ;
        return Math.max(x * x + z * z, y * y) > fogEndSqr;
    }
}
