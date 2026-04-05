package com.hbm.mixin.mod.celeritas;

import com.hbm.render.chunk.IExtraExtentsHolder;
import org.embeddedt.embeddium.impl.render.chunk.RenderSection;
import org.embeddedt.embeddium.impl.render.chunk.data.BuiltRenderSectionData;
import org.embeddedt.embeddium.impl.render.chunk.occlusion.OcclusionCuller;
import org.embeddedt.embeddium.impl.render.chunk.occlusion.OcclusionNode;
import org.embeddedt.embeddium.impl.render.viewport.CameraTransform;
import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = OcclusionCuller.class, remap = false)
public abstract class MixinOcclusionCuller {

    // Upstream CHUNK_SECTION_SIZE = 8 (half chunk) + 1 (block-model allowance) + 0.125 (epsilon). The "+1 + 0.125"
    // is the per-side margin that Celeritas always applies on top of the chunk's 16-block bounds; we must preserve
    // it on axes where HBM has no additional overflow, otherwise the expanded AABB is tighter than stock on those
    // axes and can cull sections too aggressively.
    @Unique
    private static final double HBM$STOCK_SIDE_MARGIN = 1.125D;

    /**
     * Celeritas's {@code isWithinFrustum} uses a fixed 9.125-block half-extent which only tolerates block models
     * extending up to 1 block outside their source block. HBM's oversized baked multiblock models can extend
     * further; for sections containing any, expand the frustum test to the per-axis extents captured during
     * chunk compilation in {@link MixinChunkBuilderMeshingTask}.
     */
    @Dynamic
    @Inject(method = "isWithinFrustum", at = @At("HEAD"), cancellable = true, require = 1)
    private static void hbm$expandFrustumForOversizedModels(Viewport viewport, OcclusionNode section,
                                                            CallbackInfoReturnable<Boolean> cir) {
        RenderSection rs = section.getRenderSection();
        if (rs == null) {
            return;
        }
        BuiltRenderSectionData info = rs.getBuiltContext();
        if (info == null) {
            return;
        }
        IExtraExtentsHolder holder = (IExtraExtentsHolder) info;
        int negX = holder.hbm$getNegX();
        int posX = holder.hbm$getPosX();
        int negY = holder.hbm$getNegY();
        int posY = holder.hbm$getPosY();
        int negZ = holder.hbm$getNegZ();
        int posZ = holder.hbm$getPosZ();
        if ((negX | posX | negY | posY | negZ | posZ) == 0) {
            return;
        }
        double minX = section.getOriginX() - negX - HBM$STOCK_SIDE_MARGIN;
        double minY = section.getOriginY() - negY - HBM$STOCK_SIDE_MARGIN;
        double minZ = section.getOriginZ() - negZ - HBM$STOCK_SIDE_MARGIN;
        double maxX = section.getOriginX() + 16.0D + posX + HBM$STOCK_SIDE_MARGIN;
        double maxY = section.getOriginY() + 16.0D + posY + HBM$STOCK_SIDE_MARGIN;
        double maxZ = section.getOriginZ() + 16.0D + posZ + HBM$STOCK_SIDE_MARGIN;
        cir.setReturnValue(viewport.isBoxVisible(minX, minY, minZ, maxX, maxY, maxZ));
    }

    /**
     * {@code isSectionVisible} requires both frustum AND distance checks to pass. Celeritas's
     * {@code isWithinRenderDistance} measures against the strict 16-block section box, so oversized baked models
     * can still pop early at the fog/render-distance boundary even after the frustum expansion above. Mirror the
     * upstream "closest point on box" logic with the extents applied on each side.
     */
    @Dynamic
    @Inject(method = "isWithinRenderDistance", at = @At("HEAD"), cancellable = true, require = 1)
    private static void hbm$expandDistanceForOversizedModels(CameraTransform camera, OcclusionNode section,
                                                             float maxDistance,
                                                             CallbackInfoReturnable<Boolean> cir) {
        RenderSection rs = section.getRenderSection();
        if (rs == null) {
            return;
        }
        BuiltRenderSectionData info = rs.getBuiltContext();
        if (info == null) {
            return;
        }
        IExtraExtentsHolder holder = (IExtraExtentsHolder) info;
        int negX = holder.hbm$getNegX();
        int posX = holder.hbm$getPosX();
        int negY = holder.hbm$getNegY();
        int posY = holder.hbm$getPosY();
        int negZ = holder.hbm$getNegZ();
        int posZ = holder.hbm$getPosZ();
        if ((negX | posX | negY | posY | negZ | posZ) == 0) {
            return;
        }
        int ox = section.getOriginX() - camera.intX;
        int oy = section.getOriginY() - camera.intY;
        int oz = section.getOriginZ() - camera.intZ;
        float dx = hbm$nearestToZero(ox - negX, ox + 16 + posX) - camera.fracX;
        float dy = hbm$nearestToZero(oy - negY, oy + 16 + posY) - camera.fracY;
        float dz = hbm$nearestToZero(oz - negZ, oz + 16 + posZ) - camera.fracZ;
        cir.setReturnValue((((dx * dx) + (dz * dz)) < (maxDistance * maxDistance)) && (Math.abs(dy) < maxDistance));
    }

    @Unique
    private static int hbm$nearestToZero(int min, int max) {
        int clamped = 0;
        if (min > 0) {
            clamped = min;
        }
        if (max < 0) {
            clamped = max;
        }
        return clamped;
    }
}
