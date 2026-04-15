package com.hbm.mixin.vanilla.neonium;

import com.hbm.render.chunk.RenderPassFrameHolder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderGlobal.class, priority = 1100)
public abstract class MixinRenderGlobal {

    @Inject(method = "setupTerrain", at = @At("HEAD"), require = 1)
    private void hbm$captureTerrainFrame(Entity viewEntity, double partialTicks, ICamera camera, int frameCount,
                                         boolean playerSpectator, CallbackInfo ci) {
        RenderPassFrameHolder.currentTerrainFrame = frameCount;
    }
}
