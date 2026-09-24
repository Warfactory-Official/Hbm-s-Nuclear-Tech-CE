package me.jellysquid.mods.sodium.client.render.chunk.format;

import me.jellysquid.mods.sodium.client.model.vertex.VertexSink;

public interface ModelVertexSink extends VertexSink {
    void writeQuad(float x, float y, float z, int color, float u, float v, int light);
}
