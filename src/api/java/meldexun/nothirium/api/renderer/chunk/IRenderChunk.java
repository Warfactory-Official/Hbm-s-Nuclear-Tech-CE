package meldexun.nothirium.api.renderer.chunk;

import meldexun.nothirium.util.SectionPos;

/** Stub for compilation only — provided at runtime by Nothirium. */
public interface IRenderChunk {

    SectionPos getPos();

    default int getX() {
        return getPos().getBlockX();
    }

    default int getY() {
        return getPos().getBlockY();
    }

    default int getZ() {
        return getPos().getBlockZ();
    }
}
