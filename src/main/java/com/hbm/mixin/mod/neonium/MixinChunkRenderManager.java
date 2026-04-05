package com.hbm.mixin.mod.neonium;

import com.hbm.lib.Library;
import com.hbm.render.chunk.IExtraExtentsHolder;
import com.hbm.render.chunk.IVisibleSectionSetHolder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkGraphicsState;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderContainer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderManager;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChunkRenderManager.class, remap = false)
public abstract class MixinChunkRenderManager<T extends ChunkGraphicsState> implements IVisibleSectionSetHolder {

    @Dynamic
    @Shadow
    private double fogRenderCutoff;

    @Unique
    private final LongOpenHashSet hbm$visibleSections = new LongOpenHashSet();

    @Dynamic
    @Inject(method = "reset", at = @At("HEAD"), require = 1)
    private void hbm$resetVisibleSections(CallbackInfo ci) {
        hbm$visibleSections.clear();
    }

    @Dynamic
    @Redirect(method = "addChunk", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderContainer;getSquaredDistanceXZ(DD)D"), remap = false, require = 1)
    private double hbm$fogDistanceForOversizedChunk(ChunkRenderContainer<T> render, double x, double z) {
        double dist = render.getSquaredDistanceXZ(x, z);
        if (dist < fogRenderCutoff) return dist;
        Object occlusion = render.getData().getOcclusionData();
        if (occlusion == null) return dist;
        IExtraExtentsHolder holder = (IExtraExtentsHolder) occlusion;
        int negX = holder.hbm$getNegX(), posX = holder.hbm$getPosX();
        int negZ = holder.hbm$getNegZ(), posZ = holder.hbm$getPosZ();
        if ((negX | posX | negZ | posZ) == 0) return dist;
        double minX = render.getOriginX() - negX;
        double maxX = render.getOriginX() + 16 + posX;
        double minZ = render.getOriginZ() - negZ;
        double maxZ = render.getOriginZ() + 16 + posZ;
        double dx = Math.max(minX - x, Math.max(0, x - maxX));
        double dz = Math.max(minZ - z, Math.max(0, z - maxZ));
        return dx * dx + dz * dz;
    }

    @Dynamic
    @Redirect(method = "addChunk", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderContainer;isEmpty()Z"), remap = false, require = 1)
    private boolean hbm$trackVisibleSection(ChunkRenderContainer<T> render) {
        hbm$visibleSections.add(Library.sectionToLong(
                render.getOriginX() >> 4,
                render.getOriginY() >> 4,
                render.getOriginZ() >> 4));
        return render.isEmpty();
    }

    @Override
    public LongSet hbm$getVisibleSections() {
        return hbm$visibleSections;
    }
}
