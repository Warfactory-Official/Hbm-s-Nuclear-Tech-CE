package com.hbm.render.chunk;

import com.hbm.core.ModPresence;
import com.hbm.lib.Library;
import com.hbm.util.OptifineHooks;
import com.hbm.util.ShaderHelper;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/** Baked models leaving their cell; each section they reach emits its part. */
@SideOnly(Side.CLIENT)
public final class SectionGeometry {
    private static final BlockRenderLayer[] LAYERS = BlockRenderLayer.values();
    private static final long CACHE_BYTES = 16L << 20;
    private static final Set<Block> CANDIDATES = new ReferenceOpenHashSet<>();
    private static final Object2ObjectLinkedOpenHashMap<LayoutKey, LayoutEntry> INACTIVE = new Object2ObjectLinkedOpenHashMap<>();
    private static final HashMap<LayoutKey, LayoutEntry> ACTIVE = new HashMap<>();
    private static final IdentityHashMap<SectionGeometryLayout, LayoutEntry> ENTRIES = new IdentityHashMap<>();
    private static final SectionGeometryIndex INDEX = new SectionGeometryIndex();
    private static final Set<TileEntity> TILE_DATA = ConcurrentHashMap.newKeySet();
    private static final ExtendedBlockStorage ADMITTED = new ExtendedBlockStorage(0, false);
    private static Reference2ObjectOpenHashMap<IBlockState, Mesh> published = new Reference2ObjectOpenHashMap<>();
    private static long cachedBytes;
    private static @Nullable BlockModelShapes pending;

    static {
        ADMITTED.set(0, 0, 0, Blocks.STONE.getDefaultState());
    }

    private SectionGeometry() {
    }

    /** Called by bakers during {@code ModelBakeEvent}; consumed by the following publication. */
    public static void candidate(Block block) {
        CANDIDATES.add(block);
    }

    public static void modelsReloaded(BlockModelShapes shapes) {
        // Minecraft.init reloads models before creating the dispatcher that CTM's Block.canRenderInLayer patch queries.
        if (Minecraft.getMinecraft().getBlockRendererDispatcher() == null) {
            pending = shapes;
        } else {
            publish(shapes);
        }
    }

    private static void publish(BlockModelShapes shapes) {
        for (IBlockState state : published.keySet()) ((ISectionGeometryState) state).hbm$sectioned(false);
        var meshes = new Reference2ObjectOpenHashMap<IBlockState, Mesh>();
        var canonical = new HashMap<MeshKey, Mesh>();
        for (Block block : CANDIDATES) {
            for (IBlockState state : block.getBlockState().getValidStates()) {
                Mesh mesh = capture(shapes.getModelForState(state), state);
                if (mesh == null) continue;
                meshes.put(state, canonical.computeIfAbsent(new MeshKey(mesh), key -> key.mesh));
                ((ISectionGeometryState) state).hbm$sectioned(true);
            }
        }
        CANDIDATES.clear();
        published = meshes;
        INACTIVE.clear();
        ACTIVE.clear();
        ENTRIES.clear();
        cachedBytes = 0;
        var world = Minecraft.getMinecraft().world;
        if (world != null) INDEX.reload(world);
    }

    private static @Nullable Mesh capture(IBakedModel model, IBlockState state) {
        var vertices = new FloatArrayList();
        var colors = new IntArrayList();
        var normals = new FloatArrayList();
        var layers = new ByteArrayList();
        var sprites = new ArrayList<TextureAtlasSprite>();
        boolean tinted = false;
        float[] unpacked = new float[4];
        Block block = state.getBlock();
        for (BlockRenderLayer layer : LAYERS) {
            if (!block.canRenderInLayer(state, layer)) continue;
            ForgeHooksClient.setRenderLayer(layer);
            for (int face = -1; face < EnumFacing.VALUES.length; face++) {
                for (BakedQuad quad : model.getQuads(state, face < 0 ? null : EnumFacing.VALUES[face], 0)) {
                    tinted |= quad.hasTintIndex();
                    VertexFormat format = quad.getFormat();
                    int[] data = quad.getVertexData();
                    int position = element(format, VertexFormatElement.EnumUsage.POSITION);
                    int color = element(format, VertexFormatElement.EnumUsage.COLOR);
                    int uv = element(format, VertexFormatElement.EnumUsage.UV);
                    int base = vertices.size();
                    for (int v = 0; v < 4; v++) {
                        LightUtil.unpack(data, unpacked, format, v, position);
                        vertices.add(unpacked[0]);
                        vertices.add(unpacked[1]);
                        vertices.add(unpacked[2]);
                        LightUtil.unpack(data, unpacked, format, v, uv);
                        vertices.add(unpacked[0]);
                        vertices.add(unpacked[1]);
                    }
                    float ax = vertices.getFloat(base + 10) - vertices.getFloat(base);
                    float ay = vertices.getFloat(base + 11) - vertices.getFloat(base + 1);
                    float az = vertices.getFloat(base + 12) - vertices.getFloat(base + 2);
                    float bx = vertices.getFloat(base + 15) - vertices.getFloat(base + 5);
                    float by = vertices.getFloat(base + 16) - vertices.getFloat(base + 6);
                    float bz = vertices.getFloat(base + 17) - vertices.getFloat(base + 7);
                    float nx = ay * bz - az * by, ny = az * bx - ax * bz, nz = ax * by - ay * bx;
                    float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                    if (length != 0) {
                        nx /= length;
                        ny /= length;
                        nz /= length;
                    }
                    normals.add(nx);
                    normals.add(ny);
                    normals.add(nz);
                    float shade = quad.shouldApplyDiffuseLighting() ? LightUtil.diffuseLight(nx, ny, nz) : 1;
                    for (int v = 0; v < 4; v++) {
                        if (color < 0) {
                            Arrays.fill(unpacked, 1);
                        } else {
                            LightUtil.unpack(data, unpacked, format, v, color);
                        }
                        colors.add(channel(unpacked[0] * shade) | channel(unpacked[1] * shade) << 8
                                | channel(unpacked[2] * shade) << 16 | channel(unpacked[3]) << 24);
                    }
                    layers.add((byte) layer.ordinal());
                    sprites.add(quad.getSprite());
                }
            }
        }
        ForgeHooksClient.setRenderLayer(null);
        float[] points = vertices.toFloatArray();
        boolean contained = true;
        for (int i = 0; i < points.length; i += 5) {
            for (int axis = 0; axis < 3; axis++) {
                float value = points[i + axis];
                if (!Float.isFinite(value)) throw new IllegalStateException(state + " has a non-finite baked vertex");
                contained &= value >= 0 && value <= 1;
            }
        }
        if (contained) return null;
        if (tinted) throw new IllegalStateException(state + " has tinted quads leaving its cell");
        return new Mesh(points, colors.toIntArray(), normals.toFloatArray(), layers.toByteArray(),
                sprites.toArray(new TextureAtlasSprite[0]));
    }

    private static int element(VertexFormat format, VertexFormatElement.EnumUsage usage) {
        for (int e = 0; e < format.getElementCount(); e++) {
            VertexFormatElement element = format.getElement(e);
            if (element.getUsage() == usage && element.getIndex() == 0) return e;
        }
        if (usage == VertexFormatElement.EnumUsage.COLOR) return -1;
        throw new IllegalStateException(format + " lacks " + usage);
    }

    private static int channel(float value) {
        return Math.max(0, Math.min(255, Math.round(value * 255)));
    }

    static @Nullable Mesh mesh(IBlockState state) {
        return published.get(state);
    }

    static SectionGeometryLayout acquire(Mesh mesh, BlockPos core) {
        var key = new LayoutKey(mesh, core.getX() & 15 | (core.getY() & 15) << 4 | (core.getZ() & 15) << 8);
        var entry = ACTIVE.get(key);
        if (entry != null) {
            entry.references++;
            return entry.layout;
        }
        entry = INACTIVE.remove(key);
        if (entry == null) {
            entry = new LayoutEntry(key, new SectionGeometryLayout(mesh.vertices(), key.placement()));
            ENTRIES.put(entry.layout, entry);
        } else {
            cachedBytes -= entry.layout.bytes();
        }
        ACTIVE.put(key, entry);
        entry.references = 1;
        return entry.layout;
    }

    static void release(SectionGeometryLayout layout) {
        var entry = ENTRIES.get(layout);
        // Publication drops the previous epoch before the index releases its owners.
        if (entry == null) return;
        if (--entry.references != 0) return;
        ACTIVE.remove(entry.key);
        INACTIVE.putAndMoveToLast(entry.key, entry);
        cachedBytes += entry.layout.bytes();
        while (cachedBytes > CACHE_BYTES) {
            var evicted = INACTIVE.removeFirst();
            cachedBytes -= evicted.layout.bytes();
            ENTRIES.remove(evicted.layout);
        }
    }

    public static @Nullable Snapshot capture(int sectionX, int sectionY, int sectionZ) {
        assert Minecraft.getMinecraft().isCallingFromMinecraftThread();
        var world = Minecraft.getMinecraft().world;
        return INDEX.capture(world == null ? null : world.getChunkProvider().getLoadedChunk(sectionX, sectionZ),
                sectionX, sectionY, sectionZ);
    }

    public static boolean admits(int sectionX, int sectionY, int sectionZ) {
        return INDEX.has(Library.sectionToLong(sectionX, sectionY, sectionZ));
    }

    /**
     * For renderers gating on {@code storage == null || storage.isEmpty()}: a non-empty stand-in when this section
     * has contributions. Only the gate reads it.
     */
    public static @Nullable ExtendedBlockStorage admit(@Nullable ExtendedBlockStorage storage, int sectionX,
                                                       int sectionY, int sectionZ) {
        return (storage == null || storage.isEmpty()) && admits(sectionX, sectionY, sectionZ) ? ADMITTED : storage;
    }

    public static void blockChanged(BlockPos pos, IBlockState state) {
        INDEX.update(pos, state);
    }

    public static void tileChanged(TileEntity tile) {
        INDEX.tile(tile, true);
    }

    /**
     * Any thread. For render boxes changing outside chunk data, vanilla tile entity packets and block changes;
     * indexed on the next {@link #clientTick}.
     */
    public static void renderBoundsChanged(TileEntity tile) {
        TILE_DATA.add(tile);
    }

    public static void clientTick() {
        if (TILE_DATA.isEmpty()) return;
        var world = Minecraft.getMinecraft().world;
        for (TileEntity tile : TILE_DATA) {
            TILE_DATA.remove(tile);
            if (world == null || tile.getWorld() != world) continue;
            Chunk chunk = world.getChunkProvider().getLoadedChunk(tile.getPos().getX() >> 4, tile.getPos().getZ() >> 4);
            if (chunk != null && chunk.getTileEntityMap().get(tile.getPos()) == tile) INDEX.tile(tile, true);
        }
    }

    public static void tileRemoved(BlockPos pos) {
        INDEX.removeTile(pos.toLong());
    }

    public static void chunkLoaded(Chunk chunk) {
        INDEX.load(chunk);
    }

    public static void chunkUnloaded(int chunkX, int chunkZ) {
        INDEX.unload(chunkX, chunkZ);
    }

    public static void worldChanged() {
        if (pending != null) {
            BlockModelShapes shapes = pending;
            pending = null;
            publish(shapes);
        }
        INDEX.clear();
    }

    record Mesh(float[] vertices, int[] colors, float[] normals, byte[] layers, TextureAtlasSprite[] sprites) {}

    record Part(IBlockState state, Mesh mesh, SectionGeometryLayout layout, int section, int x, int y, int z) {}

    public static final class Snapshot {
        final int originX, originY, originZ;
        final Part[] parts;
        final TileEntity[] tiles;

        Snapshot(int originX, int originY, int originZ, Part[] parts, TileEntity[] tiles) {
            this.originX = originX;
            this.originY = originY;
            this.originZ = originZ;
            this.parts = parts;
            this.tiles = tiles;
        }

        public int originX() {
            return originX;
        }

        public int originY() {
            return originY;
        }

        public int originZ() {
            return originZ;
        }

        /** Section-owned tile entities whose own section is elsewhere. */
        public TileEntity[] tiles() {
            return tiles;
        }

        public boolean hasGeometry() {
            return parts.length != 0;
        }

        /** {@code view}: the block access the compile task meshes, covering this section and its neighbours. */
        public void emit(IBlockAccess view, Sink sink) {
            if (parts.length == 0) return;
            SurfaceLight light = SurfaceLight.open(view, originX, originY, originZ);
            float[] quad = new float[20];
            int[] colors = new int[4], lights = new int[4];
            try {
                for (Part part : parts) {
                    part.layout.emit(part.section, part.state, part.mesh, part.x, part.y, part.z, light, sink, quad,
                            colors, lights);
                }
            } finally {
                light.close();
            }
        }
    }

    /** Section-local {@code x, y, z, u, v} per vertex; packed {@code sky << 16 | block} light. */
    public interface Sink {
        void quad(BlockRenderLayer layer, IBlockState owner, TextureAtlasSprite sprite, float[] quad, int[] colors,
                  int[] lights);
    }

    /** {@code buffers} returns begun buffers translated by {@code -origin}. */
    public static final class BufferSink implements Sink {
        private final int originX, originY, originZ;
        private final BlockPos origin;
        private final IBlockAccess view;
        private final Function<BlockRenderLayer, BufferBuilder> buffers;
        private final boolean shaders = ShaderHelper.areShadersActive();

        public BufferSink(Snapshot snapshot, IBlockAccess view, Function<BlockRenderLayer, BufferBuilder> buffers) {
            originX = snapshot.originX;
            originY = snapshot.originY;
            originZ = snapshot.originZ;
            origin = new BlockPos(originX, originY, originZ);
            this.view = view;
            this.buffers = buffers;
        }

        @Override
        public void quad(BlockRenderLayer layer, IBlockState owner, TextureAtlasSprite sprite, float[] quad,
                         int[] colors, int[] lights) {
            BufferBuilder buffer = buffers.apply(layer);
            // OptiFine multi-texture draws only quads carrying a sprite; tex() stays sprite-relative until cleared.
            if (ModPresence.OPTIFINE) OptifineHooks.setSprite(buffer, sprite);
            if (shaders) OptifineHooks.pushEntity(owner, origin, view, buffer);
            for (int v = 0; v < 4; v++) {
                int at = v * 5, color = colors[v], light = lights[v];
                buffer.pos((double) originX + quad[at], (double) originY + quad[at + 1], (double) originZ + quad[at + 2])
                        .color(color & 255, color >> 8 & 255, color >> 16 & 255, color >>> 24)
                        .tex(quad[at + 3], quad[at + 4])
                        .lightmap(light >>> 16, light & 0xFFFF)
                        .endVertex();
            }
            if (shaders) OptifineHooks.popEntity(buffer);
            if (ModPresence.OPTIFINE) OptifineHooks.setSprite(buffer, null);
        }
    }

    private record LayoutKey(Mesh mesh, int placement) {}

    private static final class LayoutEntry {
        final LayoutKey key;
        final SectionGeometryLayout layout;
        int references;

        LayoutEntry(LayoutKey key, SectionGeometryLayout layout) {
            this.key = key;
            this.layout = layout;
        }
    }

    private static final class MeshKey {
        final Mesh mesh;
        final int hash;

        MeshKey(Mesh mesh) {
            this.mesh = mesh;
            hash = (((Arrays.hashCode(mesh.vertices()) * 31 + Arrays.hashCode(mesh.colors())) * 31
                    + Arrays.hashCode(mesh.normals())) * 31 + Arrays.hashCode(mesh.layers())) * 31
                    + Arrays.hashCode(mesh.sprites());
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof MeshKey key && Arrays.equals(mesh.vertices(), key.mesh.vertices())
                    && Arrays.equals(mesh.colors(), key.mesh.colors()) && Arrays.equals(mesh.normals(), key.mesh.normals())
                    && Arrays.equals(mesh.layers(), key.mesh.layers()) && Arrays.equals(mesh.sprites(), key.mesh.sprites());
        }
    }
}
