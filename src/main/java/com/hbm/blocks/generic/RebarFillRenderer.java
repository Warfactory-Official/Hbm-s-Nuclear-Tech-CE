package com.hbm.blocks.generic;

import com.hbm.lib.Library;
import com.hbm.render.model.GeometryBakeUtil;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Iterator;

@ParametersAreNonnullByDefault
@SideOnly(Side.CLIENT)
final class RebarFillRenderer {

    private static final int FILL_MAX = 1_000;
    private static final int CHUNK_SIZE = 16;
    private static final int LAYER_SIZE = CHUNK_SIZE * CHUNK_SIZE;
    private static final int INTS_PER_VERTEX = 7;
    private static final int VERTICES_PER_QUAD = 4;
    private static final int INTS_PER_QUAD = INTS_PER_VERTEX * VERTICES_PER_QUAD;
    private static final int LIGHT_INT_OFFSET = 5;

    private static final int COLOR_UP = shade(EnumFacing.UP);
    private static final int COLOR_DOWN = shade(EnumFacing.DOWN);
    private static final int COLOR_NORTH = shade(EnumFacing.NORTH);
    private static final int COLOR_SOUTH = shade(EnumFacing.SOUTH);
    private static final int COLOR_WEST = shade(EnumFacing.WEST);
    private static final int COLOR_EAST = shade(EnumFacing.EAST);

    private static final Long2ObjectOpenHashMap<ReferenceLinkedOpenHashSet<BlockRebar.TileEntityRebar>> ACTIVE_BY_CHUNK = new Long2ObjectOpenHashMap<>();
    private static final Long2ObjectOpenHashMap<ChunkMesh> MESH_CACHE = new Long2ObjectOpenHashMap<>();
    private static final LongOpenHashSet DIRTY_CHUNKS = new LongOpenHashSet();
    private static final BlockPos.MutableBlockPos SCRATCH = new BlockPos.MutableBlockPos();

    private static World activeWorld;

    private RebarFillRenderer() {
    }

    static void reset() {
        activeWorld = null;
        ACTIVE_BY_CHUNK.clear();
        MESH_CACHE.clear();
        DIRTY_CHUNKS.clear();
    }

    static void render(Minecraft mc, double dx, double dy, double dz, World world, TextureAtlasSprite sprite) {
        ensureWorld(world);
        if (ACTIVE_BY_CHUNK.isEmpty()) return;

        rebuildDirtyMeshes(world, sprite);
        if (MESH_CACHE.isEmpty() || mc.player == null) return;

        int renderDistance = mc.gameSettings.renderDistanceChunks + 1;
        int playerChunkX = MathHelper.floor(mc.player.posX) >> 4;
        int playerChunkZ = MathHelper.floor(mc.player.posZ) >> 4;

        int totalVertices = 0;
        for (var iterator = MESH_CACHE.long2ObjectEntrySet().fastIterator(); iterator.hasNext(); ) {
            var entry = iterator.next();
            long key = entry.getLongKey();
            int chunkX = Library.getChunkPosX(key);
            int chunkZ = Library.getChunkPosZ(key);
            if (Math.abs(chunkX - playerChunkX) > renderDistance || Math.abs(chunkZ - playerChunkZ) > renderDistance) {
                continue;
            }
            totalVertices += entry.getValue().vertexCount;
        }
        if (totalVertices <= 0) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-dx, -dy, -dz);
        mc.entityRenderer.enableLightmap();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.disableCull();

        NTMBufferBuilder buffer = NTMImmediate.INSTANCE.beginPositionTexLmapColorQuads(totalVertices / VERTICES_PER_QUAD);
        for (var iterator = MESH_CACHE.long2ObjectEntrySet().fastIterator(); iterator.hasNext(); ) {
            var entry = iterator.next();
            long key = entry.getLongKey();
            int chunkX = Library.getChunkPosX(key);
            int chunkZ = Library.getChunkPosZ(key);
            if (Math.abs(chunkX - playerChunkX) > renderDistance || Math.abs(chunkZ - playerChunkZ) > renderDistance) {
                continue;
            }

            ChunkMesh mesh = entry.getValue();
            if (mesh.vertexCount <= 0) continue;

            mesh.updateDynamicLightmap(world);
            buffer.appendRawVertexData(mesh.vertexData, INTS_PER_VERTEX, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
        }
        NTMImmediate.INSTANCE.draw();

        GlStateManager.enableCull();
        mc.entityRenderer.disableLightmap();
        GlStateManager.popMatrix();
    }

    static void onLoad(BlockRebar.TileEntityRebar rebar) {
        if (rebar.getWorld() != activeWorld) return;
        addActiveRebar(rebar);
    }

    static void onUnload(BlockRebar.TileEntityRebar rebar) {
        if (rebar.getWorld() != activeWorld) return;
        removeActiveRebar(rebar);
    }

    static void onProgressUpdate(BlockRebar.TileEntityRebar rebar, int oldProgress, int newProgress) {
        if (rebar.getWorld() != activeWorld) return;

        boolean wasActive = oldProgress > 0;
        boolean isActive = newProgress > 0;

        if (!wasActive && isActive) {
            addActiveRebar(rebar);
            return;
        }
        if (wasActive && !isActive) {
            removeActiveRebar(rebar);
            return;
        }
        if (isActive) {
            markChunkAndNeighborsDirty(rebar.getPos());
        }
    }

    private static void ensureWorld(World world) {
        if (activeWorld == world) return;

        reset();
        activeWorld = world;

        for (var iterator = BlockRebar.TileEntityRebar.ACTIVE.iterator(); iterator.hasNext(); ) {
            BlockRebar.TileEntityRebar rebar = iterator.next();
            if (rebar.isInvalid() || rebar.progress <= 0) {
                iterator.remove();
                continue;
            }
            if (rebar.getWorld() != world) continue;
            addActiveRebar(rebar);
        }
    }

    private static void addActiveRebar(BlockRebar.TileEntityRebar rebar) {
        long key = Library.chunkKey(rebar.getPos());
        ReferenceLinkedOpenHashSet<BlockRebar.TileEntityRebar> set = ACTIVE_BY_CHUNK.get(key);
        if (set == null) {
            set = new ReferenceLinkedOpenHashSet<>();
            ACTIVE_BY_CHUNK.put(key, set);
        }
        set.add(rebar);
        markChunkAndNeighborsDirty(rebar.getPos());
    }

    private static void removeActiveRebar(BlockRebar.TileEntityRebar rebar) {
        long key = Library.chunkKey(rebar.getPos());
        ReferenceLinkedOpenHashSet<BlockRebar.TileEntityRebar> set = ACTIVE_BY_CHUNK.get(key);
        if (set != null) {
            set.remove(rebar);
            if (set.isEmpty()) {
                ACTIVE_BY_CHUNK.remove(key);
                MESH_CACHE.remove(key);
            }
        }
        markChunkAndNeighborsDirty(rebar.getPos());
    }

    private static void markChunkAndNeighborsDirty(BlockPos pos) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        DIRTY_CHUNKS.add(ChunkPos.asLong(chunkX, chunkZ));
        DIRTY_CHUNKS.add(ChunkPos.asLong(chunkX - 1, chunkZ));
        DIRTY_CHUNKS.add(ChunkPos.asLong(chunkX + 1, chunkZ));
        DIRTY_CHUNKS.add(ChunkPos.asLong(chunkX, chunkZ - 1));
        DIRTY_CHUNKS.add(ChunkPos.asLong(chunkX, chunkZ + 1));
    }

    private static void rebuildDirtyMeshes(World world, TextureAtlasSprite sprite) {
        LongIterator it = DIRTY_CHUNKS.iterator();
        while (it.hasNext()) {
            long key = it.nextLong();
            rebuildChunkMesh(world, key, sprite);
            it.remove();
        }
    }

    private static void rebuildChunkMesh(World world, long key, TextureAtlasSprite sprite) {
        ReferenceLinkedOpenHashSet<BlockRebar.TileEntityRebar> set = ACTIVE_BY_CHUNK.get(key);
        if (set == null || set.isEmpty()) {
            MESH_CACHE.remove(key);
            return;
        }

        int chunkX = Library.getChunkPosX(key);
        int chunkZ = Library.getChunkPosZ(key);

        Int2ObjectOpenHashMap<int[]> layers = new Int2ObjectOpenHashMap<>();
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Iterator<BlockRebar.TileEntityRebar> iterator = set.iterator(); iterator.hasNext(); ) {
            BlockRebar.TileEntityRebar rebar = iterator.next();
            if (rebar.isInvalid() || rebar.getWorld() != world) {
                iterator.remove();
                continue;
            }

            BlockPos pos = rebar.getPos();
            if ((pos.getX() >> 4) != chunkX || (pos.getZ() >> 4) != chunkZ || rebar.progress <= 0) {
                iterator.remove();
                continue;
            }

            int y = pos.getY();
            int[] layer = layers.get(y);
            if (layer == null) {
                layer = new int[LAYER_SIZE];
                layers.put(y, layer);
            }

            int localX = pos.getX() & 15;
            int localZ = pos.getZ() & 15;
            layer[index(localX, localZ)] = MathHelper.clamp(rebar.progress, 0, FILL_MAX);
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        if (set.isEmpty()) {
            ACTIVE_BY_CHUNK.remove(key);
            MESH_CACHE.remove(key);
            return;
        }
        if (layers.isEmpty()) {
            MESH_CACHE.remove(key);
            return;
        }

        IntArrayList data = new IntArrayList();
        LongArrayList lightSamplePositions = new LongArrayList();
        ByteArrayList lightSampleFaces = new ByteArrayList();
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;

        for (int y = minY; y <= maxY; y++) {
            int[] layer = layers.get(y);
            if (layer == null) continue;
            buildLayerQuads(data, lightSamplePositions, lightSampleFaces, world, sprite, layers, baseX, baseZ, y, layer);
        }

        if (data.isEmpty()) {
            MESH_CACHE.remove(key);
            return;
        }

        MESH_CACHE.put(key, new ChunkMesh(
                data.toIntArray(),
                lightSamplePositions.toLongArray(),
                lightSampleFaces.toByteArray(),
                data.size() / INTS_PER_VERTEX));
    }

    private static void buildLayerQuads(IntArrayList out, LongArrayList lightSamplePositions, ByteArrayList lightSampleFaces,
                                        World world, TextureAtlasSprite sprite, Int2ObjectOpenHashMap<int[]> layers,
                                        int baseX, int baseZ, int y, int[] layer) {
        int[] below = new int[LAYER_SIZE];
        int[] north = new int[LAYER_SIZE];
        int[] south = new int[LAYER_SIZE];
        int[] west = new int[LAYER_SIZE];
        int[] east = new int[LAYER_SIZE];

        for (int localZ = 0; localZ < CHUNK_SIZE; localZ++) {
            for (int localX = 0; localX < CHUNK_SIZE; localX++) {
                int idx = index(localX, localZ);
                int progress = layer[idx];
                if (progress <= 0) continue;

                int worldX = baseX + localX;
                int worldZ = baseZ + localZ;

                below[idx] = sampleVerticalProgress(world, layers, worldX, y - 1, worldZ);
                north[idx] = sampleHorizontalProgress(world, layers, baseX, baseZ, y, localX, localZ - 1);
                south[idx] = sampleHorizontalProgress(world, layers, baseX, baseZ, y, localX, localZ + 1);
                west[idx] = sampleHorizontalProgress(world, layers, baseX, baseZ, y, localX - 1, localZ);
                east[idx] = sampleHorizontalProgress(world, layers, baseX, baseZ, y, localX + 1, localZ);
            }
        }

        emitTopFaces(out, lightSamplePositions, lightSampleFaces, sprite, baseX, baseZ, y, layer);
        emitBottomFaces(out, lightSamplePositions, lightSampleFaces, sprite, baseX, baseZ, y, layer, below);
        emitNorthSouthFaces(out, lightSamplePositions, lightSampleFaces, sprite, baseX, baseZ, y, layer, north, EnumFacing.NORTH);
        emitNorthSouthFaces(out, lightSamplePositions, lightSampleFaces, sprite, baseX, baseZ, y, layer, south, EnumFacing.SOUTH);
        emitWestEastFaces(out, lightSamplePositions, lightSampleFaces, sprite, baseX, baseZ, y, layer, west, EnumFacing.WEST);
        emitWestEastFaces(out, lightSamplePositions, lightSampleFaces, sprite, baseX, baseZ, y, layer, east, EnumFacing.EAST);
    }

    private static void emitTopFaces(IntArrayList out, LongArrayList lightSamplePositions, ByteArrayList lightSampleFaces,
                                     TextureAtlasSprite sprite, int baseX, int baseZ, int y, int[] layer) {
        for (int localZ = 0; localZ < CHUNK_SIZE; localZ++) {
            for (int localX = 0; localX < CHUNK_SIZE; localX++) {
                int idx = index(localX, localZ);
                int progress = layer[idx];
                if (progress <= 0) continue;

                int worldX = baseX + localX;
                int worldZ = baseZ + localZ;
                double topY = y + height(progress);
                appendQuadXZ(out, lightSamplePositions, lightSampleFaces, sprite,
                        worldX, topY, worldZ,
                        worldX + 1, worldZ + 1,
                        true, COLOR_UP, worldX, y, worldZ, EnumFacing.UP);
            }
        }
    }

    private static void emitBottomFaces(IntArrayList out, LongArrayList lightSamplePositions, ByteArrayList lightSampleFaces,
                                        TextureAtlasSprite sprite, int baseX, int baseZ, int y, int[] layer, int[] below) {
        for (int localZ = 0; localZ < CHUNK_SIZE; localZ++) {
            for (int localX = 0; localX < CHUNK_SIZE; localX++) {
                int idx = index(localX, localZ);
                if (layer[idx] <= 0 || below[idx] >= FILL_MAX) continue;

                int worldX = baseX + localX;
                int worldZ = baseZ + localZ;
                appendQuadXZ(out, lightSamplePositions, lightSampleFaces, sprite,
                        worldX, y, worldZ,
                        worldX + 1, worldZ + 1,
                        false, COLOR_DOWN, worldX, y, worldZ, EnumFacing.DOWN);
            }
        }
    }

    private static void emitNorthSouthFaces(IntArrayList out, LongArrayList lightSamplePositions, ByteArrayList lightSampleFaces,
                                            TextureAtlasSprite sprite, int baseX, int baseZ, int y, int[] layer, int[] neighbor,
                                            EnumFacing face) {
        for (int localZ = 0; localZ < CHUNK_SIZE; localZ++) {
            for (int localX = 0; localX < CHUNK_SIZE; localX++) {
                int idx = index(localX, localZ);
                int progress = layer[idx];
                int neighborProgress = neighbor[idx];
                if (progress <= neighborProgress) continue;

                int worldX = baseX + localX;
                int worldZ = baseZ + localZ;
                double minY = y + height(neighborProgress);
                double maxY = y + height(progress);
                double planeZ = face == EnumFacing.NORTH ? worldZ : worldZ + 1;
                appendQuadXVertical(out, lightSamplePositions, lightSampleFaces, sprite,
                        worldX, worldX + 1,
                        planeZ, minY, maxY,
                        face == EnumFacing.NORTH ? COLOR_NORTH : COLOR_SOUTH,
                        worldX, y, worldZ, face);
            }
        }
    }

    private static void emitWestEastFaces(IntArrayList out, LongArrayList lightSamplePositions, ByteArrayList lightSampleFaces,
                                          TextureAtlasSprite sprite, int baseX, int baseZ, int y, int[] layer, int[] neighbor,
                                          EnumFacing face) {
        for (int localX = 0; localX < CHUNK_SIZE; localX++) {
            for (int localZ = 0; localZ < CHUNK_SIZE; localZ++) {
                int idx = index(localX, localZ);
                int progress = layer[idx];
                int neighborProgress = neighbor[idx];
                if (progress <= neighborProgress) continue;

                int worldX = baseX + localX;
                int worldZ = baseZ + localZ;
                double minY = y + height(neighborProgress);
                double maxY = y + height(progress);
                double planeX = face == EnumFacing.WEST ? worldX : worldX + 1;
                appendQuadZVertical(out, lightSamplePositions, lightSampleFaces, sprite,
                        planeX, worldZ, worldZ + 1,
                        minY, maxY,
                        face == EnumFacing.WEST ? COLOR_WEST : COLOR_EAST,
                        worldX, y, worldZ, face);
            }
        }
    }

    private static void appendQuadXZ(IntArrayList out, LongArrayList lightSamplePositions, ByteArrayList lightSampleFaces,
                                     TextureAtlasSprite sprite, double x0, double y, double z0, double x1, double z1,
                                     boolean top, int packedColor, int sampleX, int sampleY, int sampleZ, EnumFacing sampleFace) {
        float u0 = sprite.getMinU();
        float u1 = sprite.getMaxU();
        float v0 = sprite.getMinV();
        float v1 = sprite.getMaxV();

        if (top) {
            appendVertex(out, x0, y, z1, u0, v1, packedColor);
            appendVertex(out, x1, y, z1, u1, v1, packedColor);
            appendVertex(out, x1, y, z0, u1, v0, packedColor);
            appendVertex(out, x0, y, z0, u0, v0, packedColor);
        } else {
            appendVertex(out, x0, y, z0, u0, v0, packedColor);
            appendVertex(out, x1, y, z0, u1, v0, packedColor);
            appendVertex(out, x1, y, z1, u1, v1, packedColor);
            appendVertex(out, x0, y, z1, u0, v1, packedColor);
        }
        recordLightSample(lightSamplePositions, lightSampleFaces, sampleX, sampleY, sampleZ, sampleFace);
    }

    private static void appendQuadXVertical(IntArrayList out, LongArrayList lightSamplePositions, ByteArrayList lightSampleFaces,
                                            TextureAtlasSprite sprite, double x0, double x1, double z, double y0, double y1,
                                            int packedColor, int sampleX, int sampleY, int sampleZ, EnumFacing sampleFace) {
        float u0 = sprite.getMinU();
        float u1 = sprite.getMaxU();
        float v0 = interpolatedSideV(sprite, sampleY, y1);
        float v1 = interpolatedSideV(sprite, sampleY, y0);

        appendVertex(out, x0, y0, z, u0, v1, packedColor);
        appendVertex(out, x1, y0, z, u1, v1, packedColor);
        appendVertex(out, x1, y1, z, u1, v0, packedColor);
        appendVertex(out, x0, y1, z, u0, v0, packedColor);
        recordLightSample(lightSamplePositions, lightSampleFaces, sampleX, sampleY, sampleZ, sampleFace);
    }

    private static void appendQuadZVertical(IntArrayList out, LongArrayList lightSamplePositions, ByteArrayList lightSampleFaces,
                                            TextureAtlasSprite sprite, double x, double z0, double z1, double y0, double y1,
                                            int packedColor, int sampleX, int sampleY, int sampleZ, EnumFacing sampleFace) {
        float u0 = sprite.getMinU();
        float u1 = sprite.getMaxU();
        float v0 = interpolatedSideV(sprite, sampleY, y1);
        float v1 = interpolatedSideV(sprite, sampleY, y0);

        appendVertex(out, x, y0, z0, u0, v1, packedColor);
        appendVertex(out, x, y0, z1, u1, v1, packedColor);
        appendVertex(out, x, y1, z1, u1, v0, packedColor);
        appendVertex(out, x, y1, z0, u0, v0, packedColor);
        recordLightSample(lightSamplePositions, lightSampleFaces, sampleX, sampleY, sampleZ, sampleFace);
    }

    private static float interpolatedSideV(TextureAtlasSprite sprite, int blockY, double y) {
        float localY = MathHelper.clamp((float) (y - blockY), 0.0F, 1.0F);
        return sprite.getInterpolatedV((1.0F - localY) * 16.0F);
    }

    private static void appendVertex(IntArrayList out, double x, double y, double z, float u, float v, int packedColor) {
        out.add(Float.floatToRawIntBits((float) x));
        out.add(Float.floatToRawIntBits((float) y));
        out.add(Float.floatToRawIntBits((float) z));
        out.add(Float.floatToRawIntBits(u));
        out.add(Float.floatToRawIntBits(v));
        out.add(0);
        out.add(packedColor);
    }

    private static void recordLightSample(LongArrayList lightSamplePositions, ByteArrayList lightSampleFaces,
                                          int sampleX, int sampleY, int sampleZ, EnumFacing sampleFace) {
        lightSamplePositions.add(Library.blockPosToLong(sampleX, sampleY, sampleZ));
        lightSampleFaces.add((byte) sampleFace.ordinal());
    }

    private static int sampleHorizontalProgress(World world, Int2ObjectOpenHashMap<int[]> layers, int baseX, int baseZ, int y, int localX, int localZ) {
        if (localX >= 0 && localX < CHUNK_SIZE && localZ >= 0 && localZ < CHUNK_SIZE) {
            int[] layer = layers.get(y);
            return layer == null ? 0 : layer[index(localX, localZ)];
        }
        return sampleWorldProgress(world, baseX + localX, y, baseZ + localZ);
    }

    private static int sampleVerticalProgress(World world, Int2ObjectOpenHashMap<int[]> layers, int worldX, int y, int worldZ) {
        int[] layer = layers.get(y);
        if (layer != null) {
            return layer[index(worldX & 15, worldZ & 15)];
        }
        return sampleWorldProgress(world, worldX, y, worldZ);
    }

    private static int sampleWorldProgress(World world, int x, int y, int z) {
        SCRATCH.setPos(x, y, z);
        if (!world.isBlockLoaded(SCRATCH)) return 0;
        TileEntity tile = world.getTileEntity(SCRATCH);
        if (tile instanceof BlockRebar.TileEntityRebar rebar && !rebar.isInvalid()) {
            return MathHelper.clamp(rebar.progress, 0, FILL_MAX);
        }
        return 0;
    }

    private static int packedLight(World world, long samplePos, byte faceOrdinal) {
        Library.fromLong(SCRATCH, samplePos);
        SCRATCH.move(EnumFacing.VALUES[faceOrdinal & 0xFF]);
        int combined = world.isBlockLoaded(SCRATCH) ? world.getCombinedLight(SCRATCH, 0) : 0;
        return NTMBufferBuilder.packLightmap((combined >>> 16) & 0xFFFF, combined & 0xFFFF);
    }

    private static float height(int progress) {
        return progress / (float) FILL_MAX;
    }

    private static int index(int localX, int localZ) {
        return (localZ << 4) | localX;
    }

    private static int shade(EnumFacing face) {
        float nx = face.getXOffset();
        float ny = face.getYOffset();
        float nz = face.getZOffset();
        int shade = GeometryBakeUtil.computeShade(nx, ny, nz);
        return NTMBufferBuilder.packColor(shade, shade, shade, 255);
    }

    private static final class ChunkMesh {
        private final int[] vertexData;
        private final long[] lightSamplePositions;
        private final byte[] lightSampleFaces;
        private final int vertexCount;

        private ChunkMesh(int[] vertexData, long[] lightSamplePositions, byte[] lightSampleFaces, int vertexCount) {
            this.vertexData = vertexData;
            this.lightSamplePositions = lightSamplePositions;
            this.lightSampleFaces = lightSampleFaces;
            this.vertexCount = vertexCount;
        }

        private void updateDynamicLightmap(World world) {
            for (int quadIndex = 0; quadIndex < lightSamplePositions.length; quadIndex++) {
                int packedLight = packedLight(world, lightSamplePositions[quadIndex], lightSampleFaces[quadIndex]);
                int quadBase = quadIndex * INTS_PER_QUAD;
                vertexData[quadBase + LIGHT_INT_OFFSET] = packedLight;
                vertexData[quadBase + LIGHT_INT_OFFSET + INTS_PER_VERTEX] = packedLight;
                vertexData[quadBase + LIGHT_INT_OFFSET + INTS_PER_VERTEX * 2] = packedLight;
                vertexData[quadBase + LIGHT_INT_OFFSET + INTS_PER_VERTEX * 3] = packedLight;
            }
        }
    }
}
