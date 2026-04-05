package me.jellysquid.mods.sodium.client.render.chunk;

import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;

/** Stub for compilation only — provided at runtime by Neonium. */
public abstract class ChunkRenderContainer<T extends ChunkGraphicsState> {
    public int getOriginX() { throw new AssertionError(); }
    public int getOriginY() { throw new AssertionError(); }
    public int getOriginZ() { throw new AssertionError(); }
    public boolean isEmpty() { throw new AssertionError(); }
    public ChunkRenderData getData() { throw new AssertionError(); }
    public double getSquaredDistanceXZ(double x, double z) { throw new AssertionError(); }
}
