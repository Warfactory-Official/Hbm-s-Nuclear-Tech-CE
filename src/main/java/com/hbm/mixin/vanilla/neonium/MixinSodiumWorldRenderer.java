package com.hbm.mixin.vanilla.neonium;

import com.hbm.render.chunk.IRenderFrameStamp;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public abstract class MixinSodiumWorldRenderer {

    @Unique
    private int hbm$currentRenderFrame;

    @Invoker("renderTE")
    protected abstract void hbm$invokeRenderTE(TileEntity tileEntity, int pass, float partialTicks, int damageProgress);

    @Dynamic
    @Inject(method = "renderTileEntities", at = @At("HEAD"), require = 1)
    private void hbm$beginTileEntityFrame(float partialTicks, Map<Integer, DestroyBlockProgress> damagedBlocks,
                                          CallbackInfo ci) {
        hbm$currentRenderFrame++;
    }

    @Dynamic
    @Redirect(method = "renderTileEntities", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/SodiumWorldRenderer;renderTE(Lnet/minecraft/tileentity/TileEntity;IFI)V"), require = 1)
    private void hbm$renderTileEntityOnce(SodiumWorldRenderer instance, TileEntity tileEntity, int pass,
                                          float partialTicks, int damageProgress) {
        if (damageProgress >= 0) {
            hbm$invokeRenderTE(tileEntity, pass, partialTicks, damageProgress);
            return;
        }
        IRenderFrameStamp stamp = (IRenderFrameStamp) tileEntity;
        if (stamp.hbm$getFrameStamp() != hbm$currentRenderFrame && !tileEntity.isInvalid()) {
            stamp.hbm$setFrameStamp(hbm$currentRenderFrame);
            hbm$invokeRenderTE(tileEntity, pass, partialTicks, damageProgress);
        }
    }
}
