package me.jellysquid.mods.sodium.client.render;

import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderManager;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.tileentity.TileEntity;

import java.util.Map;

/** Stub for compilation only — provided at runtime by Neonium. */
public class SodiumWorldRenderer {
    WorldClient world;
    ChunkRenderManager<?> chunkRenderManager;

    public void renderTileEntities(float partialTicks, Map<Integer, DestroyBlockProgress> damagedBlocks) {
        throw new AssertionError();
    }

    private void renderTE(TileEntity tileEntity, int pass, float partialTicks, int damageProgress) {
        throw new AssertionError();
    }

    public void onChunkRenderUpdated(int x, int y, int z, ChunkRenderData meshBefore, ChunkRenderData meshAfter) {
        throw new AssertionError();
    }

    private void initRenderer() {
        throw new AssertionError();
    }
}
