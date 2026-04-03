package me.jellysquid.mods.sodium.client.render.chunk.data;

import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.tileentity.TileEntity;

/** Stub for compilation only — provided at runtime by Neonium. */
public class ChunkRenderData {

    public SetVisibility getOcclusionData() {
        throw new AssertionError();
    }

    public static class Builder {
        private SetVisibility occlusionData;
        private ChunkRenderBounds bounds;

        public void setOcclusionData(SetVisibility data) {
            this.occlusionData = data;
        }

        public void setBounds(ChunkRenderBounds bounds) {
            this.bounds = bounds;
        }

        public void addBlockEntity(TileEntity entity, boolean cull) {
            throw new AssertionError();
        }
    }
}
