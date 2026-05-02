package com.hbm.mixin.vanilla.optifine;

import com.hbm.render.chunk.RenderPassFrameHolder;
import com.hbm.render.chunk.IShadowRenderFrameStamp;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

    @Dynamic
    @Inject(method = "setupTerrain",
            at = @At(value = "INVOKE",
                    target = "Lnet/optifine/shaders/ShadowUtils;makeShadowChunkIterator(Lnet/minecraft/client/multiplayer/WorldClient;DLnet/minecraft/entity/Entity;ILnet/minecraft/client/renderer/ViewFrustum;)Ljava/util/Iterator;",
                    remap = false),
            require = 1)
    private void hbm$captureShadowTerrainFrame(Entity viewEntity, double partialTicks, ICamera camera, int frameCount,
                                                boolean playerSpectator, CallbackInfo ci) {
        RenderPassFrameHolder.currentShadowTerrainFrame = frameCount;
    }

    @Dynamic
    @WrapOperation(method = "setupTerrain",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/chunk/RenderChunk;getRenderInfo()Lnet/minecraft/client/renderer/RenderGlobal$ContainerLocalRenderInformation;",
                    remap = false),
            slice = @Slice(
                    from = @At(value = "INVOKE",
                            target = "Lnet/optifine/shaders/ShadowUtils;makeShadowChunkIterator(Lnet/minecraft/client/multiplayer/WorldClient;DLnet/minecraft/entity/Entity;ILnet/minecraft/client/renderer/ViewFrustum;)Ljava/util/Iterator;",
                            remap = false),
                    to = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/chunk/RenderChunk;setFrameIndex(I)Z",
                            ordinal = 0)),
            require = 1)
    private RenderGlobal.ContainerLocalRenderInformation hbm$stampShadowAcceptedChunk(
            RenderChunk chunk, Operation<RenderGlobal.ContainerLocalRenderInformation> original) {
        ((IShadowRenderFrameStamp) chunk).hbm$setShadowFrameStamp(RenderPassFrameHolder.currentShadowTerrainFrame);
        return original.call(chunk);
    }
}
