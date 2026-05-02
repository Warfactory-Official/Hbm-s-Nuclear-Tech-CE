package com.hbm.mixin.mod.celeritas;

import com.hbm.render.chunk.CeleritasCameraTransformAccess;
import com.hbm.render.chunk.IExtraExtentsHolder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
    @SuppressWarnings("UnreachableCode")
    @WrapOperation(
            method = {"isSectionVisible", "tryVisitNode"},
            at = @At(value = "INVOKE",
                    target = "Lorg/embeddedt/embeddium/impl/render/chunk/occlusion/OcclusionCuller;isWithinFrustum(Lorg/embeddedt/embeddium/impl/render/viewport/Viewport;Lorg/embeddedt/embeddium/impl/render/chunk/occlusion/OcclusionNode;)Z"),
            require = 2)
    private static boolean hbm$wrapIsWithinFrustum(Viewport viewport, OcclusionNode section,
                                                   Operation<Boolean> original) {
        RenderSection rs = section.getRenderSection();
        if (rs == null) {
            return original.call(viewport, section);
        }
        BuiltRenderSectionData info = rs.getBuiltContext();
        if (info == null) {
            return original.call(viewport, section);
        }
        IExtraExtentsHolder holder = (IExtraExtentsHolder) info;
        int negX = holder.hbm$getNegX();
        int posX = holder.hbm$getPosX();
        int negY = holder.hbm$getNegY();
        int posY = holder.hbm$getPosY();
        int negZ = holder.hbm$getNegZ();
        int posZ = holder.hbm$getPosZ();
        if ((negX | posX | negY | posY | negZ | posZ) == 0) {
            return original.call(viewport, section);
        }
        double minX = section.getOriginX() - negX - HBM$STOCK_SIDE_MARGIN;
        double minY = section.getOriginY() - negY - HBM$STOCK_SIDE_MARGIN;
        double minZ = section.getOriginZ() - negZ - HBM$STOCK_SIDE_MARGIN;
        double maxX = section.getOriginX() + 16.0D + posX + HBM$STOCK_SIDE_MARGIN;
        double maxY = section.getOriginY() + 16.0D + posY + HBM$STOCK_SIDE_MARGIN;
        double maxZ = section.getOriginZ() + 16.0D + posZ + HBM$STOCK_SIDE_MARGIN;
        return viewport.isBoxVisible(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * {@code isSectionVisible} requires both frustum AND distance checks to pass. Celeritas's
     * {@code isWithinRenderDistance} measures against the strict 16-block section box, so oversized baked models
     * can still pop early at the fog/render-distance boundary even after the frustum expansion above. Mirror the
     * upstream "closest point on box" logic with the extents applied on each side.
     */
    @Dynamic
    @SuppressWarnings("UnreachableCode")
    @WrapOperation(
            method = "isSectionVisible",
            at = @At(value = "INVOKE",
                    target = "Lorg/embeddedt/embeddium/impl/render/chunk/occlusion/OcclusionCuller;isWithinRenderDistance(Lorg/embeddedt/embeddium/impl/render/viewport/CameraTransform;Lorg/embeddedt/embeddium/impl/render/chunk/occlusion/OcclusionNode;F)Z"),
            require = 1)
    private static boolean hbm$wrapIsWithinRenderDistance(CameraTransform camera, OcclusionNode section,
                                                          float maxDistance, Operation<Boolean> original) {
        int negX = 0;
        int posX = 0;
        int negY = 0;
        int posY = 0;
        int negZ = 0;
        int posZ = 0;
        RenderSection rs = section.getRenderSection();
        if (rs != null) {
            BuiltRenderSectionData info = rs.getBuiltContext();
            if (info != null) {
                IExtraExtentsHolder holder = (IExtraExtentsHolder) info;
                negX = holder.hbm$getNegX();
                posX = holder.hbm$getPosX();
                negY = holder.hbm$getNegY();
                posY = holder.hbm$getPosY();
                negZ = holder.hbm$getNegZ();
                posZ = holder.hbm$getPosZ();
            }
        }
        int ox = section.getOriginX() - CeleritasCameraTransformAccess.getIntX(camera);
        int oy = section.getOriginY() - CeleritasCameraTransformAccess.getIntY(camera);
        int oz = section.getOriginZ() - CeleritasCameraTransformAccess.getIntZ(camera);
        float dx = hbm$nearestToZero(ox - negX, ox + 16 + posX) - CeleritasCameraTransformAccess.getFracX(camera);
        float dy = hbm$nearestToZero(oy - negY, oy + 16 + posY) - CeleritasCameraTransformAccess.getFracY(camera);
        float dz = hbm$nearestToZero(oz - negZ, oz + 16 + posZ) - CeleritasCameraTransformAccess.getFracZ(camera);
        return (((dx * dx) + (dz * dz)) < (maxDistance * maxDistance)) && (Math.abs(dy) < maxDistance);
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
