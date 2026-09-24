package me.jellysquid.mods.sodium.client.render.chunk;

/** Stub for compilation only — provided at runtime by Neonium. */
public class ChunkRenderManager<T extends ChunkGraphicsState> {
    private double fogRenderCutoff;

    private void reset() {
        throw new AssertionError();
    }

    private void addChunk(ChunkRenderContainer<T> render) {
        render.getSquaredDistanceXZ(0, 0);
        render.isEmpty();
        throw new AssertionError();
    }

    private void addEntitiesToRenderLists(ChunkRenderContainer<T> render) {
        throw new AssertionError();
    }
}
