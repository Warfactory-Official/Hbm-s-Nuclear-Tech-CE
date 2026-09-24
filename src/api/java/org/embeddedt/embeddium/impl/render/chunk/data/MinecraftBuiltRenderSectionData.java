package org.embeddedt.embeddium.impl.render.chunk.data;

import java.util.List;

public class MinecraftBuiltRenderSectionData<SPRITE, BLOCKENTITY> extends BuiltRenderSectionData {
    public List<BLOCKENTITY> culledBlockEntities;
}
