package org.taumc.celeritas.impl.render.terrain;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import org.embeddedt.embeddium.impl.render.terrain.SimpleWorldRenderer;

import java.util.List;
import java.util.Map;

/** Stub for compilation only — provided at runtime by Celeritas. */
public class CeleritasWorldRenderer extends SimpleWorldRenderer<WorldClient, VintageRenderSectionManager, BlockRenderLayer, TileEntity, CeleritasWorldRenderer.TileEntityRenderContext> {

    public record TileEntityRenderContext(Map<Integer, DestroyBlockProgress> damagedBlocks, float partialTicks) {}

    public int renderBlockEntities(TileEntityRenderContext tileEntityRenderContext) {
        throw new AssertionError();
    }

    protected void renderBlockEntityList(List<TileEntity> list, TileEntityRenderContext tileEntityRenderContext) {
        throw new AssertionError();
    }
}
