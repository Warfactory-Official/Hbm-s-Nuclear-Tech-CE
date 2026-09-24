package com.hbm.mixin;

import com.hbm.render.chunk.ISectionGeometryHolder;
import com.hbm.render.chunk.SectionGeometry;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChunkCompileTaskGenerator.class)
public abstract class MixinChunkCompileTaskGenerator implements ISectionGeometryHolder {

    @Unique
    private SectionGeometry.@Nullable Snapshot hbm$sectionGeometry;

    @Override
    public SectionGeometry.@Nullable Snapshot hbm$sectionGeometry() {
        return hbm$sectionGeometry;
    }

    @Override
    public void hbm$sectionGeometry(SectionGeometry.@Nullable Snapshot snapshot) {
        hbm$sectionGeometry = snapshot;
    }
}
