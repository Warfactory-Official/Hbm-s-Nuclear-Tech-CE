package com.hbm.mixin.mod.celeritas;

import com.hbm.render.chunk.IRenderFrameStamp;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import org.embeddedt.embeddium.impl.render.terrain.SimpleWorldRenderer;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.taumc.celeritas.impl.render.terrain.CeleritasWorldRenderer;
import org.taumc.celeritas.impl.render.terrain.VintageRenderSectionManager;

@Mixin(value = CeleritasWorldRenderer.class, remap = false)
public abstract class MixinCeleritasWorldRenderer extends SimpleWorldRenderer<WorldClient, VintageRenderSectionManager, BlockRenderLayer, TileEntity, CeleritasWorldRenderer.TileEntityRenderContext> {

    @Unique
    private int hbm$currentRenderFrame;

    @Dynamic
    @Inject(method = "renderBlockEntities", at = @At("HEAD"), require = 1)
    private void hbm$beginTileEntityFrame(CeleritasWorldRenderer.TileEntityRenderContext context,
                                          CallbackInfoReturnable<Integer> cir) {
        hbm$currentRenderFrame++;
    }

    @Dynamic
    @Redirect(method = "renderBlockEntityList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;render(Lnet/minecraft/tileentity/TileEntity;FI)V"), remap = true, require = 1)
    private void hbm$renderTileEntityOnce(TileEntityRendererDispatcher dispatcher, TileEntity tileEntity,
                                          float partialTicks, int destroyStage) {
        if (destroyStage >= 0) {
            dispatcher.render(tileEntity, partialTicks, destroyStage);
            return;
        }
        IRenderFrameStamp stamp = (IRenderFrameStamp) tileEntity;
        if (stamp.hbm$getFrameStamp() != hbm$currentRenderFrame && !tileEntity.isInvalid()) {
            stamp.hbm$setFrameStamp(hbm$currentRenderFrame);
            dispatcher.render(tileEntity, partialTicks, destroyStage);
        }
    }
}
