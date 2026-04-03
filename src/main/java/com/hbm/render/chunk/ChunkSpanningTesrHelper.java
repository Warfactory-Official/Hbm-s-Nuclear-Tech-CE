package com.hbm.render.chunk;

import com.hbm.lib.Library;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ChunkSpanningTesrHelper {

    private static final ReferenceOpenHashSet<TileEntity> chunkSpanningTesrs = new ReferenceOpenHashSet<>();

    private ChunkSpanningTesrHelper() {
    }

    public static void addVisibleSection(LongSet visibleSections, BlockPos origin) {
        visibleSections.add(Library.sectionToLong(origin.getX() >> 4, origin.getY() >> 4, origin.getZ() >> 4));
    }

    public static boolean isChunkSpanningLocalTesr(TileEntity tileEntity) {
        TileEntitySpecialRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(tileEntity);
        if (renderer == null || renderer.isGlobalRenderer(tileEntity)) {
            return false;
        }

        AxisAlignedBB bb = tileEntity.getRenderBoundingBox();
        if (bb == null || bb == TileEntity.INFINITE_EXTENT_AABB) {
            return false;
        }

        BlockPos pos = tileEntity.getPos();
        int sx = pos.getX() & ~15;
        int sy = pos.getY() & ~15;
        int sz = pos.getZ() & ~15;
        return bb.minX < sx || bb.maxX > sx + 16
                || bb.minY < sy || bb.maxY > sy + 16
                || bb.minZ < sz || bb.maxZ > sz + 16;
    }

    public static boolean intersectsVisibleSections(TileEntity tileEntity, LongSet visibleSections) {
        if (visibleSections.isEmpty()) {
            return false;
        }

        AxisAlignedBB bb = tileEntity.getRenderBoundingBox();
        if (bb == null || bb == TileEntity.INFINITE_EXTENT_AABB) {
            return false;
        }

        int minSectionX = MathHelper.floor(bb.minX) >> 4;
        int minSectionY = MathHelper.floor(bb.minY) >> 4;
        int minSectionZ = MathHelper.floor(bb.minZ) >> 4;
        int maxSectionX = MathHelper.floor(Math.nextAfter(bb.maxX, Double.NEGATIVE_INFINITY)) >> 4;
        int maxSectionY = MathHelper.floor(Math.nextAfter(bb.maxY, Double.NEGATIVE_INFINITY)) >> 4;
        int maxSectionZ = MathHelper.floor(Math.nextAfter(bb.maxZ, Double.NEGATIVE_INFINITY)) >> 4;

        for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
            for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
                    if (visibleSections.contains(Library.sectionToLong(sectionX, sectionY, sectionZ))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /** Called on main thread from setCompiledChunk / onChunkRenderUpdated. */
    public static void updateChunkSpanningTesrs(TileEntity[] removed, TileEntity[] added) {
        for (TileEntity te : removed) {
            chunkSpanningTesrs.remove(te);
        }
        for (TileEntity te : added) {
            chunkSpanningTesrs.add(te);
        }
    }

    public static void clear() {
        chunkSpanningTesrs.clear();
    }

    public static ReferenceOpenHashSet<TileEntity> getChunkSpanningTesrs() {
        return chunkSpanningTesrs;
    }
}
