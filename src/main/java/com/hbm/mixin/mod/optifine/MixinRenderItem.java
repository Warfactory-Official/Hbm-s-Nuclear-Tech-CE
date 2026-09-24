package com.hbm.mixin.mod.optifine;

import com.hbm.render.model.IHbmBakedQuad;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * OptiFine's {@code RenderItem.renderQuads} replaces vanilla's {@code LightUtil.renderQuadColor} path with its own
 * {@code renderQuad} that unconditionally calls {@code putQuadNormal} after {@code addVertexData}. That helper
 * writes {@code quad.getFace().getDirectionVec()} into all four vertex normal slots of the buffer, clobbering the
 * per-vertex normals that HBM's {@link com.hbm.render.model.AbstractWavefrontBakedModel} bakes in.
 *
 * <p>With the per-vertex normals replaced by a single face normal, the two directional lights enabled by
 * {@link net.minecraft.client.renderer.RenderHelper#enableStandardItemLighting()} produce a stepped appearance
 * at face boundaries on curved meshes — the "sharp edges" symptom. Vanilla's item pipeline never reaches this
 * call because Forge routes {@code renderQuads} through {@code LightUtil.renderQuadColor}, which skips
 * {@code putQuadNormal} entirely.
 *
 * <p>Scoped to {@link IHbmBakedQuad} via instanceof so other mods that legitimately pass
 * {@code applyDiffuseLighting = false} with different semantics are not affected.
 */
@Mixin(RenderItem.class)
public abstract class MixinRenderItem {

    @Inject(method = "putQuadNormal", at = @At("HEAD"), cancellable = true, require = 1)
    private void hbm$preserveVertexNormalsForPreShadedQuads(BufferBuilder renderer, BakedQuad quad, CallbackInfo ci) {
        if (quad instanceof IHbmBakedQuad) {
            ci.cancel();
        }
    }
}
