package com.hbm.mixin.mod.neonium;

import com.hbm.render.chunk.IExtraExtentsHolder;
import me.jellysquid.mods.sodium.client.render.chunk.cull.graph.ChunkGraphNode;
import me.jellysquid.mods.sodium.client.util.math.FrustumExtended;
import net.minecraft.client.renderer.chunk.SetVisibility;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChunkGraphNode.class, remap = false)
public abstract class MixinChunkGraphNode {

    @Unique
    private int hbm$negX, hbm$posX, hbm$negY, hbm$posY, hbm$negZ, hbm$posZ;

    @Shadow
    public abstract int getOriginX();

    @Shadow
    public abstract int getOriginY();

    @Shadow
    public abstract int getOriginZ();

    @Dynamic
    @Inject(method = "setOcclusionData", at = @At("RETURN"))
    private void hbm$cacheExpansion(SetVisibility occlusionData, CallbackInfo ci) {
        if (occlusionData != null) {
            IExtraExtentsHolder holder = (IExtraExtentsHolder) occlusionData;
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
    public boolean isCulledByFrustum(FrustumExtended frustum) {
        float x = getOriginX();
        float y = getOriginY();
        float z = getOriginZ();
        float epsilon = 1.125f;

        return !frustum.fastAabbTest(
                x - epsilon - hbm$negX, y - epsilon - hbm$negY, z - epsilon - hbm$negZ,
                x + 16.0f + epsilon + hbm$posX, y + 16.0f + epsilon + hbm$posY, z + 16.0f + epsilon + hbm$posZ);
    }
}
