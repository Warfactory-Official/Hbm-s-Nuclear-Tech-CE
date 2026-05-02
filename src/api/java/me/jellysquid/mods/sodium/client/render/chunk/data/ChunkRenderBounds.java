package me.jellysquid.mods.sodium.client.render.chunk.data;

import me.jellysquid.mods.sodium.client.util.math.ChunkSectionPos;

/** Stub for compilation only — provided at runtime by Neonium. */
public class ChunkRenderBounds {

    public final float x1;
    public final float y1;
    public final float z1;
    public final float x2;
    public final float y2;
    public final float z2;

    public ChunkRenderBounds(float x1, float y1, float z1, float x2, float y2, float z2) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    /** Stub for compilation only — provided at runtime by Neonium. */
    public static class Builder {
        public void addBlock(int x, int y, int z) {
            throw new AssertionError();
        }

        public ChunkRenderBounds build(ChunkSectionPos origin) {
            throw new AssertionError();
        }
    }
}
