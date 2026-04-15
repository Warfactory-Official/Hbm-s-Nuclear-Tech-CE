package com.hbm.mixin.vanilla;

import com.hbm.render.chunk.IRenderFrameStamp;
import com.hbm.render.chunk.RenderPassFrameHolder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

    @Inject(method = "setupTerrain",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;setRenderDistanceWeight(D)V"),
            require = 1)
    private void hbm$captureTerrainFrame(Entity viewEntity, double partialTicks, ICamera camera, int frameCount,
                                         boolean playerSpectator, CallbackInfo ci) {
        RenderPassFrameHolder.currentTerrainFrame = frameCount;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(method = "setupTerrain",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Ljava/util/Set;isEmpty()Z"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunk;setFrameIndex(I)Z", ordinal = 0)),
            require = 1)
    private boolean hbm$stampDirectlyVisibleRenderChunk(List list, Object element) {
        RenderChunk renderChunk = ((RenderGlobal.ContainerLocalRenderInformation) element).renderChunk;
        ((IRenderFrameStamp) renderChunk).hbm$setFrameStamp(RenderPassFrameHolder.currentTerrainFrame);
        return list.add(element);
    }
}
