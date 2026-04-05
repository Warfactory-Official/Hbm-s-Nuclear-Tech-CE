package com.hbm.render.model;

/**
 * Marker interface on HBM's own {@link net.minecraft.client.renderer.block.model.BakedQuad} implementations.
 * Used by {@code com.hbm.mixin.mod.optifine.MixinRenderItem} to scope its fix to HBM-owned quads without
 * affecting other mods that may legitimately pass {@code applyDiffuseLighting = false} to vanilla
 * {@code BakedQuad}s with different semantics.
 */
public interface IHbmBakedQuad {
}
