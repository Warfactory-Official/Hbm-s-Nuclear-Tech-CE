package com.hbm.mixin;

import com.hbm.render.chunk.IRenderFrameStamp;
import com.hbm.render.chunk.SectionGeometry;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

    @Unique
    private int hbm$currentRenderFrame;

    @Inject(method = "setWorldAndLoadRenderers", at = @At("HEAD"))
    private void hbm$clearSectionGeometry(@Nullable WorldClient world, CallbackInfo ci) {
        SectionGeometry.worldChanged();
    }

    @Inject(method = "renderEntities", at = @At("HEAD"), require = 1)
    private void hbm$beginTileEntityFrame(Entity renderViewEntity, ICamera camera, float partialTicks,
                                          CallbackInfo ci) {
        hbm$currentRenderFrame++;
    }

    // Section-owned tile entities appear in every section list they overlap.
    @WrapOperation(method = "renderEntities", require = 3, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;render(Lnet/minecraft/tileentity/TileEntity;FI)V"))
    private void hbm$renderTileEntityOnce(TileEntityRendererDispatcher dispatcher, TileEntity tileEntity,
                                          float partialTicks, int destroyStage, Operation<Void> original) {
        if (destroyStage >= 0) {
            original.call(dispatcher, tileEntity, partialTicks, destroyStage);
            return;
        }
        IRenderFrameStamp stamp = (IRenderFrameStamp) tileEntity;
        if (stamp.hbm$getFrameStamp() != hbm$currentRenderFrame && !tileEntity.isInvalid()) {
            stamp.hbm$setFrameStamp(hbm$currentRenderFrame);
            original.call(dispatcher, tileEntity, partialTicks, destroyStage);
        }
    }
}
