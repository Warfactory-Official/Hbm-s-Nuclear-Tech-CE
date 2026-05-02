package com.hbm.mixin;

import com.hbm.render.chunk.ChunkSpanningTesrHelper;
import com.hbm.render.chunk.RenderPassFrameHolder;
import com.hbm.render.chunk.IRenderFrameStamp;
import com.hbm.render.chunk.IShadowRenderFrameStamp;
import com.hbm.util.ShaderHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.MinecraftForgeClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

    @Shadow
    public ViewFrustum viewFrustum;
    @Unique
    private int hbm$currentRenderFrame;
    @Unique
    private final BlockPos.MutableBlockPos hbm$sectionProbePos = new BlockPos.MutableBlockPos();

    @Inject(method = "loadRenderers", at = @At("HEAD"), require = 1)
    private void hbm$clearOnReload(CallbackInfo ci) {
        ChunkSpanningTesrHelper.clear();
    }

    @Inject(method = "renderEntities", at = @At("HEAD"), require = 1)
    private void hbm$beginTileEntityFrame(Entity renderViewEntity, ICamera camera, float partialTicks,
                                          CallbackInfo ci) {
        hbm$currentRenderFrame++;
    }

    @WrapOperation(method = "renderEntities", require = 3, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;render(Lnet/minecraft/tileentity/TileEntity;FI)V"))
    private void hbm$renderTileEntityOnce(TileEntityRendererDispatcher dispatcher, TileEntity tileEntity,
                                          float partialTicks, int destroyStage, Operation<Void> original) {
        if (destroyStage >= 0) {
            original.call(dispatcher, tileEntity, partialTicks, destroyStage);
            return;
        }
        IRenderFrameStamp stamp = (IRenderFrameStamp) tileEntity;
        if (stamp.hbm$getFrameStamp() != hbm$currentRenderFrame) {
            stamp.hbm$setFrameStamp(hbm$currentRenderFrame);
            original.call(dispatcher, tileEntity, partialTicks, destroyStage);
        }
    }

    @Inject(method = "renderEntities", require = 1, at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;drawBatch(I)V", shift = At.Shift.BEFORE))
    private void hbm$renderChunkSpanningTesrs(Entity renderViewEntity, ICamera camera,
                                              float partialTicks, CallbackInfo ci) {
        if (ChunkSpanningTesrHelper.isEmpty()) return;
        if (viewFrustum == null) return;
        int pass = MinecraftForgeClient.getRenderPass();
        boolean shadersActive = ShaderHelper.areShadersActive();
        int frame = hbm$currentRenderFrame;
        boolean shadowPass = ShaderHelper.isShadowPass();
        int terrainFrame = shadowPass
                ? RenderPassFrameHolder.currentShadowTerrainFrame
                : RenderPassFrameHolder.currentTerrainFrame;

        for (TileEntity tileEntity : ChunkSpanningTesrHelper.getChunkSpanningTesrs()) {
            if (tileEntity.isInvalid()) continue;
            if (!tileEntity.shouldRenderInPass(pass)) continue;
            AxisAlignedBB bb = tileEntity.getRenderBoundingBox();
            if (bb == null || bb == TileEntity.INFINITE_EXTENT_AABB) continue;
            if (!camera.isBoundingBoxInFrustum(bb)) continue;
            if (!hbm$intersectsCurrentVisibleSections(bb, shadowPass, terrainFrame)) continue;
            IRenderFrameStamp stamp = (IRenderFrameStamp) tileEntity;
            if (stamp.hbm$getFrameStamp() == frame) continue;
            stamp.hbm$setFrameStamp(frame);
            if (shadersActive) ShaderHelper.nextBlockEntity(tileEntity);
            TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, -1);
        }
    }

    @Unique
    private boolean hbm$intersectsCurrentVisibleSections(AxisAlignedBB bb, boolean shadowPass, int terrainFrame) {
        int minSectionX = MathHelper.floor(bb.minX) >> 4;
        int minSectionY = MathHelper.floor(bb.minY) >> 4;
        int minSectionZ = MathHelper.floor(bb.minZ) >> 4;
        int maxSectionX = (MathHelper.ceil(bb.maxX) - 1) >> 4;
        int maxSectionY = (MathHelper.ceil(bb.maxY) - 1) >> 4;
        int maxSectionZ = (MathHelper.ceil(bb.maxZ) - 1) >> 4;

        for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
            for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
                    int originX = sectionX << 4;
                    int originY = sectionY << 4;
                    int originZ = sectionZ << 4;
                    RenderChunk renderChunk = viewFrustum.getRenderChunk(hbm$sectionProbePos.setPos(
                            originX,
                            originY,
                            originZ));
                    if (renderChunk == null) continue;
                    BlockPos pos = renderChunk.getPosition();
                    if (pos.getX() != originX || pos.getY() != originY || pos.getZ() != originZ) continue;
                    int chunkStamp = shadowPass
                            ? ((IShadowRenderFrameStamp) renderChunk).hbm$getShadowFrameStamp()
                            : ((IRenderFrameStamp) renderChunk).hbm$getFrameStamp();
                    if (chunkStamp == terrainFrame) return true;
                }
            }
        }
        return false;
    }
}
