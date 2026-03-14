package com.hbm.handler.radiation;

import com.hbm.handler.radiation.RadiationSystemNT.WorldRadiationData.MultiSectionRef;
import com.hbm.handler.radiation.RadiationSystemNT.WorldRadiationData.SectionRef;
import com.hbm.handler.radiation.RadiationSystemNT.WorldRadiationData.SingleMaskedSectionRef;
import com.hbm.lib.Library;
import com.hbm.render.util.PositionColorVertexWriter;
import com.hbm.util.RenderUtil;
import com.hbm.util.SectionKeyHash;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenCustomHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.*;

import static com.hbm.handler.radiation.RadiationSystemNT.*;

@SideOnly(Side.CLIENT)
public final class RadVisOverlay {
    public static final Config CONFIG = new Config();

    private static final int MAX_RADIUS = 8;
    private static final double RAD_REF = 1000.0d;
    private static final int[] VERIFY_FACES = {5, 3, 1};

    private static final float[] COLOR_UNI = {0.0f, 0.9f, 1.0f};      // Neon Cyan
    private static final float[] COLOR_SINGLE = {0.1f, 1.0f, 0.1f};   // Bright Lime Green
    private static final float[] COLOR_MULTI = {1.0f, 0.85f, 0.0f};   // Vibrant Golden Yellow
    private static final float[] COLOR_RESIST = {0.4f, 0.3f, 0.6f};   // Deep Purple
    private static final float[] COLOR_WARN = {1.0f, 0.3f, 0.0f};     // Bright Orange
    private static final float[] COLOR_ERR = {1.0f, 0.0f, 0.2f};      // Crimson/Neon Red
    private static final float[] COLOR_INACTIVE = {1.0f, 0.0f, 0.6f}; // Hot Pink

    private static final float[][] POCKET_COLORS = new float[MAX_POCKETS][3];
    private static final String[] FACE_NAMES = {"DOWN", "UP", "NORTH", "SOUTH", "WEST", "EAST"};

    private static final List<ErrorRecord> ERROR_CACHE = new ArrayList<>(32);
    private static final int[] FACE_PLANE_AXIS = {1, 1, 2, 2, 0, 0};
    private static final int[] FACE_PLANE_ADD = {0, 1, 0, 1, 0, 1};
    private static final int[] FACE_U_AXIS = {0, 0, 0, 0, 2, 2};
    private static final int[] FACE_V_AXIS = {2, 2, 1, 1, 1, 1};
    private static final int[] FACE_BOUNDARY_COORD = {0, 15, 0, 15, 0, 15};
    private static final int[] FACE_INSET_SIGN = {1, -1, 1, -1, 1, -1};
    private static final int[] FACE_VERTEX_ORDER = {0, 1, 0, 1, 2, 0};
    // 6 faces * 17 planes * 256 blocks = 26,112
    private static final short[] PLANE_POCKETS = new short[26112];
    private static final LongArrayList QUADS_TEMP = new LongArrayList(512);
    private static final int[] PLANE16_TEMP = new int[16];
    private static final int[] PLANE16_COPY_TEMP = new int[16];
    private static final LongArrayList QUADS_TEMP2 = new LongArrayList(256);
    private static final WeakHashMap<MultiSectionRef, PocketMesh> POCKET_MESH_CACHE = new WeakHashMap<>(32);
    private static final WeakHashMap<MultiSectionRef, MultiOuterMeshes> MULTI_OUTER_CACHE = new WeakHashMap<>(32);
    private static final WeakHashMap<SingleMaskedSectionRef, SingleOuterMeshes> SINGLE_OUTER_CACHE = new WeakHashMap<>(
            32);
    private static final Long2ReferenceOpenCustomHashMap<int[]> ERROR_MASK = new Long2ReferenceOpenCustomHashMap<>(32,
            SectionKeyHash.STRATEGY);
    private static final ThreadLocal<RenderScratch> TL_RENDER_SCRATCH = ThreadLocal.withInitial(RenderScratch::new);
    private static int[] OUTER_ROWS_TEMP = new int[6 * 16 * 16];
    private static long lastVerifyTick = Long.MIN_VALUE;

    static {
        for (int i = 0; i < POCKET_COLORS.length; i++) {
            float h = (i * 0.618034f + 0.1f) % 1.0f;
            float[] rgb = hsvToRgb(h, 1.0f, 1.0f);
            POCKET_COLORS[i][0] = rgb[0];
            POCKET_COLORS[i][1] = rgb[1];
            POCKET_COLORS[i][2] = rgb[2];
        }
    }

    private RadVisOverlay() {
    }

    public static void clearCaches() {
        POCKET_MESH_CACHE.clear();
        MULTI_OUTER_CACHE.clear();
        SINGLE_OUTER_CACHE.clear();
        ERROR_CACHE.clear();
        ERROR_MASK.clear();
    }

    private static int[] ensureTempIntArray(int[] rows, int need) {
        if (rows.length >= need) return rows;
        int n = rows.length;
        while (n < need) n = n + (n >>> 1) + 16;
        return Arrays.copyOf(rows, n);
    }

    private static int[] ensureOuterRowsTemp(int need) {
        OUTER_ROWS_TEMP = ensureTempIntArray(OUTER_ROWS_TEMP, need);
        return OUTER_ROWS_TEMP;
    }

    // quad packing: pocket:11 | face:3 | plane:5 | u0:5 | v0:5 | u1:5 | v1:5
    private static long packQuad(int pocket, int face, int plane, int u0, int v0, int u1, int v1) {
        return (pocket & 0x7FFL) | ((face & 7L) << 11) | ((plane & 31L) << 14) | ((u0 & 31L) << 19) | ((v0 & 31L) << 24) | ((u1 & 31L) << 29) | ((v1 & 31L) << 34);
    }

    private static int coordAxis(int axis, int lx, int ly, int lz) {
        return switch (axis) {
            case 0 -> lx;
            case 1 -> ly;
            default -> lz;
        };
    }

    private static int boundaryPlaneForFace(int face) {
        return (face & 1) == 0 ? 0 : 16;
    }

    private static int faceUVToBlockIndex(int face, int u, int v) {
        return switch (face) {
            case 0 -> (0 << 8) | (v << 4) | u;
            case 1 -> (15 << 8) | (v << 4) | u;
            case 2 -> (v << 8) | (0 << 4) | u;
            case 3 -> (v << 8) | (15 << 4) | u;
            case 4 -> (v << 8) | (u << 4) | 0;
            case 5 -> (v << 8) | (u << 4) | 15;
            default -> 0;
        };
    }

    private static void drawPackedQuads(BufferBuilder buf, int baseX, int baseY, int baseZ, long[] quads, int quadCount,
                                        int packedColor, float inset, int[] quadVertexDataScratch) {
        for (int i = 0; i < quadCount; i++) {
            emitPackedPocketQuad(buf, baseX, baseY, baseZ, packedColor, inset, quads[i], quadVertexDataScratch);
        }
    }

    private static void drawPackedQuads(BufferBuilder buf, int baseX, int baseY, int baseZ, long[] quads, float r,
                                        float g, float b, float a, float inset, int[] quadVertexDataScratch) {
        int packedColor = PositionColorVertexWriter.packRgbaColorToNativeInt(r, g, b, a);
        drawPackedQuads(buf, baseX, baseY, baseZ, quads, quads.length, packedColor, inset, quadVertexDataScratch);
    }

    private static void drawPackedQuads(BufferBuilder buf, int baseX, int baseY, int baseZ, LongArrayList quads,
                                        float r, float g, float b, float a, float inset,
                                        int[] quadVertexDataScratch) {
        long[] arr = quads.elements();
        int packedColor = PositionColorVertexWriter.packRgbaColorToNativeInt(r, g, b, a);
        drawPackedQuads(buf, baseX, baseY, baseZ, arr, quads.size(), packedColor, inset, quadVertexDataScratch);
    }

    private static void emitPackedPocketQuad(BufferBuilder buf, int baseX, int baseY, int baseZ, int packedColor,
                                             float inset, long q, int[] quadVertexDataScratch) {
        int face = (int) ((q >>> 11) & 7);
        int plane = (int) ((q >>> 14) & 31);
        int u0 = (int) ((q >>> 19) & 31);
        int v0 = (int) ((q >>> 24) & 31);
        int u1 = (int) ((q >>> 29) & 31);
        int v1 = (int) ((q >>> 34) & 31);
        emitPocketQuad(buf, baseX, baseY, baseZ, face, plane, u0, v0, u1, v1, packedColor, inset,
                quadVertexDataScratch);
    }

    private static void emitPocketQuad(BufferBuilder buf, int baseX, int baseY, int baseZ, int face, int plane, int u0,
                                       int v0, int u1, int v1, int packedColor, float inset,
                                       int[] quadVertexDataScratch) {
        if (face < 0 || face > 5) return;

        int planeAxis = FACE_PLANE_AXIS[face];
        double planeBase = planeAxis == 0 ? baseX : planeAxis == 1 ? baseY : baseZ;
        double planeCoord = planeBase + plane + (inset * FACE_INSET_SIGN[face]);

        int uAxis = FACE_U_AXIS[face];
        int vAxis = FACE_V_AXIS[face];
        double uBase = uAxis == 0 ? baseX : baseZ;
        double vBase = vAxis == 1 ? baseY : baseZ;

        emitPocketQuadVertices(buf, planeAxis, planeCoord, uBase + u0, vBase + v0, uBase + u1, vBase + v1,
                FACE_VERTEX_ORDER[face], packedColor, quadVertexDataScratch);
    }

    private static void emitPocketQuadVertices(BufferBuilder buf, int planeAxis, double planeCoord, double u0,
                                               double v0, double u1, double v1, int order, int packedColor,
                                               int[] quadVertexDataScratch) {
        double p0u, p0v, p1u, p1v, p2u, p2v, p3u, p3v;
        switch (order) {//@formatter:off
            case 1 -> {p0u = u0;p0v = v1;p1u = u1;p1v = v1;p2u = u1;p2v = v0;p3u = u0;p3v = v0;}
            case 2 -> {p0u = u1;p0v = v0;p1u = u0;p1v = v0;p2u = u0;p2v = v1;p3u = u1;p3v = v1;}
            default -> {p0u = u0;p0v = v0;p1u = u1;p1v = v0;p2u = u1;p2v = v1;p3u = u0;p3v = v1;}
        }//@formatter:on

        switch (planeAxis) {
            case 0 -> PositionColorVertexWriter.appendPositionColorQuad(buf, quadVertexDataScratch,
                    planeCoord, p0v, p0u,
                    planeCoord, p1v, p1u,
                    planeCoord, p2v, p2u,
                    planeCoord, p3v, p3u,
                    packedColor);
            case 1 -> PositionColorVertexWriter.appendPositionColorQuad(buf, quadVertexDataScratch,
                    p0u, planeCoord, p0v,
                    p1u, planeCoord, p1v,
                    p2u, planeCoord, p2v,
                    p3u, planeCoord, p3v,
                    packedColor);
            default -> PositionColorVertexWriter.appendPositionColorQuad(buf, quadVertexDataScratch,
                    p0u, p0v, planeCoord,
                    p1u, p1v, planeCoord,
                    p2u, p2v, planeCoord,
                    p3u, p3v, planeCoord,
                    packedColor);
        }
    }

    private static void greedyMeshPlane(int[] rows, int planeBase, int pocket, int face, int plane, LongArrayList out) {
        for (int v0 = 0; v0 < 16; v0++) {
            while (true) {
                int rowMask = rows[planeBase + v0] & 0xFFFF;
                if (rowMask == 0) break;

                int u0 = Integer.numberOfTrailingZeros(rowMask);

                int shifted = (rowMask >>> u0) & 0xFFFF;
                int inv = (~shifted) & 0xFFFF;
                int w = Integer.numberOfTrailingZeros(inv);
                if (w == 32) w = 16 - u0;
                if (w <= 0) break;

                int rectMask = ((1 << w) - 1) << u0;

                int h = 1;
                while (v0 + h < 16) {
                    int m = rows[planeBase + v0 + h] & 0xFFFF;
                    if ((m & rectMask) != rectMask) break;
                    h++;
                }

                for (int vv = 0; vv < h; vv++) {
                    rows[planeBase + v0 + vv] &= ~rectMask;
                }

                int u1 = u0 + w;
                int v1 = v0 + h;
                out.add(packQuad(pocket, face, plane, u0, v0, u1, v1));
            }
        }
    }

    private static long[] meshFaceRowsToPackedQuads(int face, int plane, int[] rows16) {
        int[] tmp = PLANE16_COPY_TEMP;
        System.arraycopy(rows16, 0, tmp, 0, 16);
        LongArrayList out = QUADS_TEMP2;
        out.clear();
        greedyMeshPlane(tmp, 0, 0, face, plane, out);
        int n = out.size();
        if (n == 0) return new long[0];
        long[] q = new long[n];
        System.arraycopy(out.elements(), 0, q, 0, n);
        return q;
    }

    private static PocketMesh getOrBuildPocketMesh(MultiSectionRef ms, int pocketCount) {
        short[] pd = ms.pocketData;
        if (pd == null || pocketCount <= 0) return null;

        PocketMesh cached = POCKET_MESH_CACHE.get(ms);
        if (cached != null && cached.pocketDataRef == pd && cached.pocketCount == pocketCount) {
            return cached;
        }

        PocketMesh built = buildPocketMesh(pd, pocketCount);
        POCKET_MESH_CACHE.put(ms, built);
        return built;
    }

    private static PocketMesh buildPocketMesh(short[] pocketData, int pocketCount) {
        int pc = Math.min(pocketCount, MAX_POCKETS);
        Arrays.fill(PLANE_POCKETS, (short) -1);

        for (int idx = 0; idx < 4096; idx++) {
            short pi = pocketData[idx];
            if (pi == NO_POCKET || pi < 0 || pi >= pc) continue;

            int lx = idx & 15;
            int lz = (idx >>> 4) & 15;
            int ly = (idx >>> 8) & 15;

            for (int face = 0; face < 6; face++) {
                int axis = FACE_PLANE_AXIS[face];
                int c = coordAxis(axis, lx, ly, lz);
                boolean boundary = (c == FACE_BOUNDARY_COORD[face]);
                if (!boundary) {
                    int nIdx = idx + LINEAR_OFFSETS[face];
                    if (pocketData[nIdx] != NO_POCKET) continue;
                }

                int plane = c + FACE_PLANE_ADD[face];
                int u = coordAxis(FACE_U_AXIS[face], lx, ly, lz);
                int v = coordAxis(FACE_V_AXIS[face], lx, ly, lz);

                int arrayIdx = (face * 17 + plane) * 256 + (v * 16 + u);
                PLANE_POCKETS[arrayIdx] = pi;
            }
        }

        LongArrayList quads = QUADS_TEMP;
        quads.clear();

        for (int face = 0; face < 6; face++) {
            for (int plane = 0; plane < 17; plane++) {
                int planeBase = (face * 17 + plane) * 256;

                for (int v0 = 0; v0 < 16; v0++) {
                    for (int u0 = 0; u0 < 16; u0++) {
                        short pi = PLANE_POCKETS[planeBase + v0 * 16 + u0];
                        if (pi == -1) continue;

                        int w = 1;
                        while (u0 + w < 16 && PLANE_POCKETS[planeBase + v0 * 16 + (u0 + w)] == pi) {
                            w++;
                        }

                        int h = 1;
                        boolean expand = true;
                        while (v0 + h < 16 && expand) {
                            for (int x = 0; x < w; x++) {
                                if (PLANE_POCKETS[planeBase + (v0 + h) * 16 + (u0 + x)] != pi) {
                                    expand = false;
                                    break;
                                }
                            }
                            if (expand) h++;
                        }

                        for (int y = 0; y < h; y++) {
                            for (int x = 0; x < w; x++) {
                                PLANE_POCKETS[planeBase + (v0 + y) * 16 + (u0 + x)] = -1;
                            }
                        }

                        int u1 = u0 + w;
                        int v1 = v0 + h;
                        quads.add(packQuad(pi, face, plane, u0, v0, u1, v1));
                    }
                }
            }
        }

        int n = quads.size();
        long[] elements = quads.elements();
        long[] q = new long[n];
        int[] counts = new int[pc];
        for (int i = 0; i < n; i++) {
            int pi = (int) (elements[i] & 0x7FFL);
            counts[pi]++;
        }
        int[] offsets = new int[pc];
        int sum = 0;
        for (int i = 0; i < pc; i++) {
            offsets[i] = sum;
            sum += counts[i];
        }
        for (int i = 0; i < n; i++) {
            long quad = elements[i];
            int pi = (int) (quad & 0x7FFL);
            q[offsets[pi]++] = quad;
        }
        return new PocketMesh(pocketData, pc, q);
    }

    private static MultiOuterMeshes getOrBuildOuterMeshes(MultiSectionRef ms, int pocketCount) {
        short[] pd = ms.pocketData;
        if (pd == null || pocketCount <= 0) return null;

        MultiOuterMeshes cached = MULTI_OUTER_CACHE.get(ms);
        if (cached != null && cached.pocketDataRef == pd && cached.pocketCount == pocketCount) return cached;

        MultiOuterMeshes built = buildMultiOuterMeshes(pd, pocketCount);
        MULTI_OUTER_CACHE.put(ms, built);
        return built;
    }

    private static SingleOuterMeshes getOrBuildOuterMeshes(SingleMaskedSectionRef s) {
        short[] pd = s.pocketData;
        if (pd == null) return null;

        SingleOuterMeshes cached = SINGLE_OUTER_CACHE.get(s);
        if (cached != null && cached.pocketDataRef == pd) return cached;

        SingleOuterMeshes built = buildSingleOuterMeshes(pd);
        SINGLE_OUTER_CACHE.put(s, built);
        return built;
    }

    private static void scanOuterFaceRowsMulti(short[] pocketData, int pc, int face, int[] rows, byte[] sampleU,
                                               byte[] sampleV, int[] sampleSetMask) {
        int faceRowBase = face * 16;
        for (int v = 0; v < 16; v++) {
            for (int u = 0; u < 16; u++) {
                int idx = faceUVToBlockIndex(face, u, v);
                int pi = pocketData[idx];
                if (pi == NO_POCKET || pi < 0 || pi >= pc) continue;

                int off = (pi * 96) + faceRowBase + v;
                rows[off] |= (1 << u);

                int arrIdx = face * MAX_POCKETS + pi;
                int word = arrIdx >>> 5;
                int bit = 1 << (arrIdx & 31);
                if ((sampleSetMask[word] & bit) == 0) {
                    sampleSetMask[word] |= bit;
                    sampleU[arrIdx] = (byte) u;
                    sampleV[arrIdx] = (byte) v;
                }
            }
        }
    }

    private static void buildSingleOuterFacePlane(short[] pocketData, int face, int[] plane16Out) {
        Arrays.fill(plane16Out, 0);
        for (int v = 0; v < 16; v++) {
            int mask = 0;
            for (int u = 0; u < 16; u++) {
                int idx = faceUVToBlockIndex(face, u, v);
                if (pocketData[idx] == 0) mask |= (1 << u);
            }
            plane16Out[v] = mask;
        }
    }

    private static MultiOuterMeshes buildMultiOuterMeshes(short[] pocketData, int pocketCount) {
        int pc = Math.min(pocketCount, MAX_POCKETS);

        int[] rows = ensureOuterRowsTemp(pc * 6 * 16);
        Arrays.fill(rows, 0, pc * 6 * 16, 0);

        byte[] sampleU = new byte[6 * MAX_POCKETS];
        byte[] sampleV = new byte[6 * MAX_POCKETS];
        int[] sampleSetMask = new int[(6 * MAX_POCKETS) / 32 + 1];

        for (int face = 0; face < 6; face++) {
            scanOuterFaceRowsMulti(pocketData, pc, face, rows, sampleU, sampleV, sampleSetMask);
        }

        long[][] sectionFaceQuads = new long[6][];
        long[][] warnFaceQuads = new long[6][];
        int[][] facePockets = new int[6][];

        int[] union = PLANE16_TEMP;
        int[] tmp = PLANE16_COPY_TEMP;

        for (int face = 0; face < 6; face++) {
            Arrays.fill(union, 0);

            IntArrayList fpList = new IntArrayList();
            int faceBase = face * 16;

            for (int pi = 0; pi < pc; pi++) {
                int base = (pi * 96) + faceBase;
                boolean any = false;
                for (int r = 0; r < 16; r++) {
                    int m = rows[base + r] & 0xFFFF;
                    union[r] |= m;
                    any |= (m != 0);
                }
                if (any) fpList.add(pi);
            }

            facePockets[face] = fpList.toIntArray();

            int plane = boundaryPlaneForFace(face);
            sectionFaceQuads[face] = meshFaceRowsToPackedQuads(face, plane, union);

            if (fpList.size() > 1) {
                Arrays.fill(tmp, 0);
                for (int i = 1; i < fpList.size(); i++) {
                    int pi = fpList.getInt(i);
                    int base = (pi * 96) + faceBase;
                    for (int r = 0; r < 16; r++) {
                        tmp[r] |= (rows[base + r] & 0xFFFF);
                    }
                }
                warnFaceQuads[face] = meshFaceRowsToPackedQuads(face, plane, tmp);
            } else {
                warnFaceQuads[face] = new long[0];
            }
        }

        return new MultiOuterMeshes(pocketData, pc, sectionFaceQuads, warnFaceQuads, facePockets, sampleU, sampleV);
    }

    private static SingleOuterMeshes buildSingleOuterMeshes(short[] pocketData) {
        long[][] sectionFaceQuads = new long[6][];
        int[] plane = PLANE16_TEMP;
        for (int face = 0; face < 6; face++) {
            buildSingleOuterFacePlane(pocketData, face, plane);
            sectionFaceQuads[face] = meshFaceRowsToPackedQuads(face, boundaryPlaneForFace(face), plane);
        }
        return new SingleOuterMeshes(pocketData, sectionFaceQuads);
    }

    public static void render(RenderWorldLastEvent evt) {
        if (!CONFIG.enabled) return;

        Minecraft mc = Minecraft.getMinecraft();
        WorldRadiationData data = getData(mc);
        if (data == null) return;

        Entity cam = mc.getRenderViewEntity();
        if (cam == null) return;

        float partial = evt.getPartialTicks();
        double camX = cam.lastTickPosX + (cam.posX - cam.lastTickPosX) * (double) partial;
        double camY = cam.lastTickPosY + (cam.posY - cam.lastTickPosY) * (double) partial;
        double camZ = cam.lastTickPosZ + (cam.posZ - cam.lastTickPosZ) * (double) partial;

        RenderUtil.pushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(-camX, -camY, -camZ);
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            if (CONFIG.depth) GlStateManager.enableDepth();
            else GlStateManager.disableDepth();

            int radius = MathHelper.clamp(CONFIG.radiusChunks, 0, MAX_RADIUS);
            int pcx = MathHelper.floor(mc.player.posX) >> 4;
            int pcz = MathHelper.floor(mc.player.posZ) >> 4;

            int sliceY = CONFIG.sliceAutoY ? MathHelper.floor(mc.player.posY) : CONFIG.sliceY;
            sliceY = MathHelper.clamp(sliceY, 0, 255);

            FocusFilter filter = FocusFilter.fromConfig(CONFIG, mc.player);

            switch (CONFIG.mode) {
                case WIRE -> renderWire(data, pcx, pcz, radius, filter);
                case SLICE -> renderSlice(data, pcx, pcz, radius, sliceY, filter);
                case SECTIONS -> renderSections(data, pcx, pcz, radius, filter);
                case POCKETS -> renderPockets(data, pcx, pcz, radius, filter);
                case STATE -> renderState(data, pcx, pcz, radius, filter, camX, camY, camZ);
                case ERRORS -> renderErrors(data, pcx, pcz, radius, filter);
            }
        } catch (Throwable _) {
        } finally {
            GlStateManager.popMatrix();
            RenderUtil.popAttrib();
        }
    }

    public static void clientTick(Minecraft mc) {
        if (!CONFIG.enabled || !CONFIG.verify) return;
        WorldRadiationData data = getData(mc);
        if (data == null) return;

        long tick = mc.world.getTotalWorldTime();
        int interval = Math.max(1, CONFIG.verifyInterval);
        if (tick - lastVerifyTick < interval) return;
        lastVerifyTick = tick;

        ERROR_CACHE.clear();

        int radius = MathHelper.clamp(CONFIG.radiusChunks, 0, MAX_RADIUS);
        int pcx = MathHelper.floor(mc.player.posX) >> 4;
        int pcz = MathHelper.floor(mc.player.posZ) >> 4;

        FocusFilter filter = FocusFilter.fromConfig(CONFIG, mc.player);
        try {
            runVerification(data, pcx, pcz, radius, filter, ERROR_CACHE);
        } catch (Throwable _) {
            ERROR_CACHE.clear();
        }
    }

    private static @Nullable WorldRadiationData getData(Minecraft mc) {
        if (mc.world == null || mc.player == null) return null;
        IntegratedServer server = mc.getIntegratedServer();
        if (server == null) return null;
        WorldServer ws = DimensionManager.getWorld(mc.world.provider.getDimension());
        if (ws == null) return null;
        return worldMap.get(ws);
    }

    private static void renderWire(WorldRadiationData data, int pcx, int pcz, int radius, FocusFilter filter) {
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        Sec sec = new Sec();

        for (int cx = pcx - radius; cx <= pcx + radius; cx++) {
            for (int cz = pcz - radius; cz <= pcz + radius; cz++) {
                for (int sy = 0; sy < 16; sy++) {
                    int baseX = cx << 4;
                    int baseY = sy << 4;
                    int baseZ = cz << 4;

                    if (filter != null && filter.isSectionOutsideFilter(baseX, baseY, baseZ)) continue;

                    long sck = Library.sectionToLong(cx, sy, cz);
                    if (!resolveSection(data, sck, sec)) continue;

                    float[] color = kindColor(sec.kind);
                    float tint = (sec.kind == KIND_MULTI) ? (0.5f + 0.5f * (sec.pocketCount / 15.0f)) : 1.0f;
                    addBoxLines(buf, baseX, baseY, baseZ, baseX + 16, baseY + 16, baseZ + 16, color[0] * tint,
                            color[1] * tint, color[2] * tint, Math.min(1.0f, CONFIG.alpha + 0.2f));
                }
            }
        }

        tess.draw();
        GlStateManager.depthMask(true);
    }

    private static void renderSlice(WorldRadiationData data, int pcx, int pcz, int radius, int sliceY,
                                    FocusFilter filter) {
        int sy = sliceY >> 4;
        if (sy < 0 || sy > 15) return;

        GlStateManager.disableTexture2D();

        depthAndPolygon(Mode.SLICE);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        int localY = sliceY & 15;
        int idxBase = localY << 8;
        double y = sliceY + 0.02;

        float baseAlpha = MathHelper.clamp(CONFIG.alpha, 0.0f, 1.0f);

        RenderScratch scratch = TL_RENDER_SCRATCH.get();
        float[] radAlpha = scratch.radAlpha;
        boolean[] active = scratch.active;
        double[] rad = scratch.rad;

        Sec sec = new Sec();

        for (int cx = pcx - radius; cx <= pcx + radius; cx++) {
            for (int cz = pcz - radius; cz <= pcz + radius; cz++) {
                int baseX = cx << 4;
                int baseZ = cz << 4;

                if (filter != null && filter.isSectionOutsideFilter(baseX, sy << 4, baseZ)) continue;

                long sck = Library.sectionToLong(cx, sy, cz);
                if (!resolveSection(data, sck, sec)) continue;

                int kind = sec.kind;
                int pocketCount = sec.pocketCount;

                if (kind == KIND_UNI) {
                    rad[0] = readRad(sec, 0);
                    active[0] = readActive(sec, 0);
                    radAlpha[0] = computeRadAlpha(rad[0], baseAlpha, active[0]);
                } else {
                    for (int pi = 0; pi < pocketCount; pi++) {
                        rad[pi] = readRad(sec, pi);
                        active[pi] = readActive(sec, pi);
                        radAlpha[pi] = computeRadAlpha(rad[pi], baseAlpha, active[pi]);
                    }
                }

                for (int z = 0; z < 16; z++) {
                    int rowBase = idxBase | (z << 4);
                    for (int x = 0; x < 16; x++) {
                        int idx = rowBase | x;

                        int pi;
                        if (kind == KIND_UNI) {
                            pi = 0;
                        } else {
                            pi = pocketIndexAt(sec, idx);
                        }

                        float r, g, b, a;

                        if (pi < 0) {
                            r = COLOR_RESIST[0];
                            g = COLOR_RESIST[1];
                            b = COLOR_RESIST[2];
                            a = baseAlpha * 0.6f;
                        } else {
                            float[] c = POCKET_COLORS[pi & 2047];
                            r = c[0];
                            g = c[1];
                            b = c[2];
                            a = radAlpha[pi];

                            if (!active[pi] && Math.abs(rad[pi]) > 1.0e-6) {
                                r = COLOR_INACTIVE[0];
                                g = COLOR_INACTIVE[1];
                                b = COLOR_INACTIVE[2];
                                a = baseAlpha;
                            }
                        }

                        double x0 = baseX + x;
                        double z0 = baseZ + z;

                        buf.pos(x0, y, z0).color(r, g, b, a).endVertex();
                        buf.pos(x0 + 1, y, z0).color(r, g, b, a).endVertex();
                        buf.pos(x0 + 1, y, z0 + 1).color(r, g, b, a).endVertex();
                        buf.pos(x0, y, z0 + 1).color(r, g, b, a).endVertex();
                    }
                }
            }
        }

        tess.draw();
        cleanDepthAndPolygon();
    }

    private static void renderSections(WorldRadiationData data, int pcx, int pcz, int radius, FocusFilter filter) {
        GlStateManager.disableTexture2D();
        depthAndPolygon(Mode.SECTIONS);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        float inset = Mode.SECTIONS.inset;
        float a = CONFIG.alpha * 0.5f;
        int[] quadVertexDataScratch = TL_RENDER_SCRATCH.get().quadVertexDataScratch;

        Sec sec = new Sec();
        Sec nei = new Sec();

        for (int cx = pcx - radius; cx <= pcx + radius; cx++) {
            for (int cz = pcz - radius; cz <= pcz + radius; cz++) {
                for (int sy = 0; sy < 16; sy++) {
                    int baseX = cx << 4;
                    int baseY = sy << 4;
                    int baseZ = cz << 4;

                    if (filter != null && filter.isSectionOutsideFilter(baseX, baseY, baseZ)) continue;

                    long sck = Library.sectionToLong(cx, sy, cz);
                    if (!resolveSection(data, sck, sec)) continue;

                    int kind = sec.kind;
                    if (kind == KIND_UNI) continue;

                    if (kind == KIND_SINGLE) {
                        SingleMaskedSectionRef s = sec.single;
                        if (s == null) continue;

                        SingleOuterMeshes mesh = getOrBuildOuterMeshes(s);
                        if (mesh == null) continue;

                        float[] col = kindColor(kind);
                        for (int face = 0; face < 6; face++) {
                            long[] quads = mesh.sectionFaceQuads[face];
                            if (quads.length == 0) continue;
                            drawPackedQuads(buf, baseX, baseY, baseZ, quads, col[0], col[1], col[2], a, inset,
                                    quadVertexDataScratch);
                        }
                        continue;
                    }

                    // MULTI
                    MultiSectionRef m = sec.multi;
                    if (m == null) continue;

                    MultiOuterMeshes mesh = getOrBuildOuterMeshes(m, sec.pocketCount);
                    if (mesh == null) continue;

                    for (int face = 0; face < 6; face++) {
                        long[] quads = mesh.sectionFaceQuads[face];
                        if (quads.length == 0) continue;

                        boolean leakRisk = false;
                        if (mesh.facePockets[face] != null && mesh.facePockets[face].length > 1) {
                            long neighborKey = Library.sectionToLong(cx + FACE_DX[face], sy + FACE_DY[face],
                                    cz + FACE_DZ[face]);
                            if (resolveSection(data, neighborKey, nei) && nei.kind == KIND_UNI) leakRisk = true;
                        }

                        float[] col = leakRisk ? COLOR_WARN : kindColor(kind);
                        drawPackedQuads(buf, baseX, baseY, baseZ, quads, col[0], col[1], col[2], a, inset,
                                quadVertexDataScratch);
                    }
                }
            }
        }

        tess.draw();
        cleanDepthAndPolygon();
    }

    private static void renderPockets(WorldRadiationData data, int pcx, int pcz, int radius, FocusFilter filter) {
        GlStateManager.disableTexture2D();
        depthAndPolygon(Mode.POCKETS);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        float baseAlpha = MathHelper.clamp(CONFIG.alpha, 0.0f, 1.0f);
        float inset = Mode.POCKETS.inset;

        RenderScratch scratch = TL_RENDER_SCRATCH.get();
        double[] rad = scratch.rad;
        boolean[] active = scratch.active;
        float[] radAlpha = scratch.radAlpha;
        int[] packedPocketColor = scratch.packedPocketColor;
        int[] quadVertexDataScratch = scratch.quadVertexDataScratch;

        Sec sec = new Sec();

        for (int cx = pcx - radius; cx <= pcx + radius; cx++) {
            for (int cz = pcz - radius; cz <= pcz + radius; cz++) {
                for (int sy = 0; sy < 16; sy++) {
                    int baseX = cx << 4;
                    int baseY = sy << 4;
                    int baseZ = cz << 4;

                    if (filter != null && filter.isSectionOutsideFilter(baseX, baseY, baseZ)) continue;

                    long sck = Library.sectionToLong(cx, sy, cz);
                    if (!resolveSection(data, sck, sec)) continue;

                    if (sec.kind != KIND_MULTI) continue;

                    MultiSectionRef ms = sec.multi;
                    if (ms == null || sec.pocketData == null || sec.pocketCount <= 0) continue;

                    int pocketCount = sec.pocketCount;
                    for (int pi = 0; pi < pocketCount; pi++) {
                        rad[pi] = readRad(sec, pi);
                        active[pi] = readActive(sec, pi);
                        radAlpha[pi] = computeRadAlpha(rad[pi], baseAlpha, active[pi]);
                        if (!active[pi] && Math.abs(rad[pi]) > 1.0e-6) {
                            packedPocketColor[pi] = PositionColorVertexWriter.packRgbaColorToNativeInt(
                                    COLOR_INACTIVE[0], COLOR_INACTIVE[1], COLOR_INACTIVE[2], baseAlpha);
                        } else {
                            float[] c = POCKET_COLORS[pi & 2047];
                            packedPocketColor[pi] = PositionColorVertexWriter.packRgbaColorToNativeInt(c[0], c[1],
                                    c[2], radAlpha[pi]);
                        }
                    }

                    PocketMesh mesh = getOrBuildPocketMesh(ms, pocketCount);
                    if (mesh == null) continue;

                    long[] quads = mesh.quads;
                    for (long q : quads) {
                        int pi = (int) (q & 0x7FFL);
                        if (pi >= pocketCount) continue;
                        emitPackedPocketQuad(buf, baseX, baseY, baseZ, packedPocketColor[pi], inset, q,
                                quadVertexDataScratch);
                    }
                }
            }
        }

        tess.draw();
        cleanDepthAndPolygon();
    }

    private static void renderErrors(WorldRadiationData data, int pcx, int pcz, int radius, FocusFilter filter) {
        GlStateManager.disableTexture2D();
        depthAndPolygon(Mode.ERRORS);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        float inset = Mode.ERRORS.inset;
        float a = CONFIG.alpha * 0.5f;
        int[] quadVertexDataScratch = TL_RENDER_SCRATCH.get().quadVertexDataScratch;

        Sec sec = new Sec();
        Sec nei = new Sec();

        int[] crossData = new int[128];
        int crossCount = 0;

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        for (int cx = pcx - radius; cx <= pcx + radius; cx++) {
            for (int cz = pcz - radius; cz <= pcz + radius; cz++) {
                for (int sy = 0; sy < 16; sy++) {
                    int baseX = cx << 4;
                    int baseY = sy << 4;
                    int baseZ = cz << 4;

                    if (filter != null && filter.isSectionOutsideFilter(baseX, baseY, baseZ)) continue;

                    long sck = Library.sectionToLong(cx, sy, cz);
                    if (!resolveSection(data, sck, sec)) continue;

                    if (sec.kind != KIND_MULTI) continue;
                    if (sec.pocketCount <= 1) continue;

                    MultiSectionRef m = sec.multi;
                    if (m == null || sec.pocketData == null) continue;

                    MultiOuterMeshes mesh = getOrBuildOuterMeshes(m, sec.pocketCount);
                    if (mesh == null) continue;

                    for (int face = 0; face < 6; face++) {
                        long[] warnQuads = mesh.warnFaceQuads[face];
                        if (warnQuads.length == 0) continue;

                        long neighborKey = Library.sectionToLong(cx + FACE_DX[face], sy + FACE_DY[face],
                                cz + FACE_DZ[face]);
                        if (!resolveSection(data, neighborKey, nei) || nei.kind != KIND_UNI) continue;

                        drawPackedQuads(buf, baseX, baseY, baseZ, warnQuads, COLOR_WARN[0], COLOR_WARN[1],
                                COLOR_WARN[2], a, inset, quadVertexDataScratch);

                        int[] facePockets = mesh.facePockets[face];
                        if (facePockets != null && facePockets.length > 1) {
                            for (int i = 1; i < facePockets.length; i++) {
                                int pi = facePockets[i];
                                int off = face * MAX_POCKETS + pi;
                                int u = mesh.sampleU[off] & 15;
                                int v = mesh.sampleV[off] & 15;
                                int idx = faceUVToBlockIndex(face, u, v);
                                int lx = idx & 15;
                                int lz = (idx >>> 4) & 15;
                                int ly = (idx >>> 8) & 15;
                                if ((crossCount + 1) * 4 > crossData.length) {
                                    crossData = Arrays.copyOf(crossData,
                                            crossData.length + (crossData.length >>> 1) + 16);
                                }
                                int cOff = crossCount * 4;
                                crossData[cOff] = baseX + lx;
                                crossData[cOff + 1] = baseY + ly;
                                crossData[cOff + 2] = baseZ + lz;
                                crossData[cOff + 3] = face;
                                crossCount++;
                            }
                        }
                    }
                }
            }
        }

        if (CONFIG.verify && !ERROR_CACHE.isEmpty()) {
            ERROR_MASK.clear();
            for (ErrorRecord rec : ERROR_CACHE) {
                long sectionA = rec.sectionA();
                int face = rec.faceA();
                int[] fr = ERROR_MASK.get(sectionA);
                if (fr == null) {
                    fr = new int[6 * 16];
                    ERROR_MASK.put(sectionA, fr);
                }
                int lx = rec.sampleX() & 15;
                int ly = rec.sampleY() & 15;
                int lz = rec.sampleZ() & 15;
                int row = coordAxis(FACE_V_AXIS[face], lx, ly, lz);
                int col = coordAxis(FACE_U_AXIS[face], lx, ly, lz);
                fr[face * 16 + row] |= (1 << col);
            }
            var iterator = ERROR_MASK.long2ReferenceEntrySet().fastIterator();
            while (iterator.hasNext()) {
                var e = iterator.next();
                long sectionA = e.getLongKey();
                int baseX = (Library.getSectionX(sectionA) << 4);
                int baseY = (Library.getSectionY(sectionA) << 4);
                int baseZ = (Library.getSectionZ(sectionA) << 4);
                int[] fr = e.getValue();
                for (int face = 0; face < 6; face++) {
                    System.arraycopy(fr, face * 16, PLANE16_TEMP, 0, 16);
                    int[] tmp = PLANE16_COPY_TEMP;
                    System.arraycopy(PLANE16_TEMP, 0, tmp, 0, 16);
                    QUADS_TEMP2.clear();
                    greedyMeshPlane(tmp, 0, 0, face, boundaryPlaneForFace(face), QUADS_TEMP2);
                    if (!QUADS_TEMP2.isEmpty())
                        drawPackedQuads(buf, baseX, baseY, baseZ, QUADS_TEMP2, COLOR_ERR[0], COLOR_ERR[1], COLOR_ERR[2],
                                a, inset, quadVertexDataScratch);
                }
            }
        }

        tess.draw();

        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < crossCount; i++) {
            int off = i * 4;
            addCross(buf, crossData[off] + 0.5, crossData[off + 1] + 0.5, crossData[off + 2] + 0.5, crossData[off + 3],
                    0.35, COLOR_WARN[0], COLOR_WARN[1], COLOR_WARN[2], CONFIG.alpha);
        }

        if (CONFIG.verify) {
            for (ErrorRecord rec : ERROR_CACHE) {
                Vec3d aC = sectionCenter(rec.sectionA());
                Vec3d bC = sectionCenter(rec.sectionB());
                addLine(buf, aC.x, aC.y, aC.z, bC.x, bC.y, bC.z, COLOR_ERR[0], COLOR_ERR[1], COLOR_ERR[2],
                        CONFIG.alpha);
                addCross(buf, rec.sampleX() + 0.5, rec.sampleY() + 0.5, rec.sampleZ() + 0.5, rec.faceA(), 0.35,
                        COLOR_ERR[0], COLOR_ERR[1], COLOR_ERR[2], CONFIG.alpha);
            }
        }

        tess.draw();
        cleanDepthAndPolygon();
    }

    private static void depthAndPolygon(Mode mode) {
        GlStateManager.depthMask(false);
        if (!CONFIG.depth) return;
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(mode.polygonFactor, mode.polygonUnits);
    }

    private static void cleanDepthAndPolygon() {
        if (CONFIG.depth) GlStateManager.disablePolygonOffset();
        GlStateManager.depthMask(true);
    }

    private static void runVerification(WorldRadiationData data, int pcx, int pcz, int radius, FocusFilter filter,
                                        List<? super ErrorRecord> out) {
        RenderScratch scratch = TL_RENDER_SCRATCH.get();
        Long2IntOpenHashMap counts = scratch.counts;
        Long2IntOpenHashMap samplesA = scratch.samplesA;
        Long2IntOpenHashMap samplesB = scratch.samplesB;

        Sec a = new Sec();
        Sec b = new Sec();

        for (int cx = pcx - radius; cx <= pcx + radius; cx++) {
            for (int cz = pcz - radius; cz <= pcz + radius; cz++) {
                for (int sy = 0; sy < 16; sy++) {
                    int baseX = cx << 4;
                    int baseY = sy << 4;
                    int baseZ = cz << 4;
                    if (filter != null && filter.isSectionOutsideFilter(baseX, baseY, baseZ)) continue;

                    long aKey = Library.sectionToLong(cx, sy, cz);
                    if (!resolveSection(data, aKey, a)) continue;

                    for (int faceA : VERIFY_FACES) {
                        int nx = cx + FACE_DX[faceA];
                        int ny = sy + FACE_DY[faceA];
                        int nz = cz + FACE_DZ[faceA];
                        if (ny < 0 || ny > 15) continue;
                        if (nx < pcx - radius || nx > pcx + radius || nz < pcz - radius || nz > pcz + radius) continue;

                        long bKey = Library.sectionToLong(nx, ny, nz);
                        if (!resolveSection(data, bKey, b)) continue;

                        try {
                            verifyPair(aKey, bKey, faceA, a, b, counts, samplesA, samplesB, out);
                        } catch (Throwable _) {
                        }
                    }
                }
            }
        }
    }

    private static void buildFaceOverlapCounts(int faceA, Sec a, int faceB, Sec b, Long2IntOpenHashMap counts,
                                               Long2IntOpenHashMap samplesA, Long2IntOpenHashMap samplesB) {
        int planeA = faceA << 8;
        int planeB = faceB << 8;

        for (int t = 0; t < 256; t++) {
            int idxA = FACE_PLANE[planeA + t];
            int idxB = FACE_PLANE[planeB + t];

            int pa = pocketIndexAt(a, idxA);
            int pb = pocketIndexAt(b, idxB);
            if (pa < 0 || pb < 0) continue;

            long key = ((long) pa << 16) | (pb & 0xFFFFL);
            if (!counts.containsKey(key)) {
                samplesA.put(key, idxA);
                samplesB.put(key, idxB);
            }
            counts.put(key, counts.get(key) + 1);
        }
    }

    private static void reportStaleEdges(List<? super ErrorRecord> out, Long2IntOpenHashMap counts, long multiKey,
                                         long otherKey, int faceOnMulti, boolean multiIsAInCounts, boolean otherIsMulti,
                                         MultiSectionRef ms) {
        int[] edges = ms.edgesByFace[faceOnMulti];
        int edgeCount = ms.edgeCounts[faceOnMulti] & 0xFFFF;
        if (edges == null || edgeCount == 0) return;

        for (int i = 0; i < edgeCount; i++) {
            int edge = edges[i];
            int myPi = (edge >>> 20) & 0x7FF;
            int otherPi = (edge >>> 9) & 0x7FF;
            int otherPocket = otherIsMulti ? otherPi : 0;

            long key = multiIsAInCounts ? (((long) myPi) << 16) | (otherPocket & 0xFFFFL) : (((long) otherPocket) << 16) | (myPi & 0xFFFFL);

            if (!counts.containsKey(key)) {
                addMismatch(out, multiKey, otherKey, faceOnMulti, myPi, otherPocket, edge & 0x1FF, 0, -1);
            }
        }
    }

    private static void verifyMultiMulti(long aKey, long bKey, int faceA, int faceB, Sec a, Sec b,
                                         Long2IntOpenHashMap counts, Long2IntOpenHashMap samplesA,
                                         Long2IntOpenHashMap samplesB, List<? super ErrorRecord> out) {
        MultiSectionRef am = a.multi;
        MultiSectionRef bm = b.multi;

        var iterator = counts.long2IntEntrySet().fastIterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            long key = entry.getLongKey();
            int pa = (int) (key >>> 16);
            int pb = (int) (key & 0xFFFF);
            int actual = entry.getIntValue();

            int storedA = readConnMulti(am, pa, faceA, pb);
            int storedB = readConnMulti(bm, pb, faceB, pa);

            if (storedA != actual) addMismatch(out, aKey, bKey, faceA, pa, pb, storedA, actual, samplesA.get(key));
            if (storedB != actual) addMismatch(out, bKey, aKey, faceB, pb, pa, storedB, actual, samplesB.get(key));
        }

        // edges present but no actual overlap
        reportStaleEdges(out, counts, aKey, bKey, faceA, true, true, am);
        reportStaleEdges(out, counts, bKey, aKey, faceB, false, true, bm);
    }

    private static void verifyMultiToNonMulti(long multiKey, long otherKey, int faceOnMulti, boolean multiIsAInCounts,
                                              MultiSectionRef ms, Long2IntOpenHashMap counts,
                                              Long2IntOpenHashMap sampleMapForMismatch, List<? super ErrorRecord> out) {
        var iterator = counts.long2IntEntrySet().fastIterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            long key = entry.getLongKey();
            int myPi = multiIsAInCounts ? (int) (key >>> 16) : (int) (key & 0xFFFF);
            int actual = entry.getIntValue();
            int stored = readConnUniSingle(ms, myPi, faceOnMulti);
            if (stored != actual) addMismatch(out, multiKey, otherKey, faceOnMulti, myPi, 0, stored, actual,
                    sampleMapForMismatch.get(key));
        }

        reportStaleEdges(out, counts, multiKey, otherKey, faceOnMulti, multiIsAInCounts, false, ms);
    }

    private static void verifySingleSingle(long aKey, long bKey, int faceA, int faceB, Long2IntOpenHashMap counts,
                                           Long2IntOpenHashMap samplesA, Long2IntOpenHashMap samplesB,
                                           SingleMaskedSectionRef sa, SingleMaskedSectionRef sb,
                                           List<? super ErrorRecord> out) {
        int actual = counts.get(0L);

        int storedA = readSingleConn(sa, faceA);
        int storedB = readSingleConn(sb, faceB);

        if (storedA != actual) addMismatch(out, aKey, bKey, faceA, 0, 0, storedA, actual, samplesA.get(0L));
        if (storedB != actual) addMismatch(out, bKey, aKey, faceB, 0, 0, storedB, actual, samplesB.get(0L));
    }

    private static void verifySingleToUni(long singleKey, long uniKey, int faceOnSingle, Long2IntOpenHashMap counts,
                                          Long2IntOpenHashMap sampleMap, SingleMaskedSectionRef s,
                                          List<? super ErrorRecord> out) {
        int actual = counts.get(0L);
        int exposed = s.getFaceCount(faceOnSingle);
        if (exposed != actual)
            addMismatch(out, singleKey, uniKey, faceOnSingle, 0, 0, exposed, actual, sampleMap.get(0L));
    }

    private static void verifyPair(long aKey, long bKey, int faceA, Sec a, Sec b, Long2IntOpenHashMap counts,
                                   Long2IntOpenHashMap samplesA, Long2IntOpenHashMap samplesB,
                                   List<? super ErrorRecord> out) {
        int faceB = faceA ^ 1;

        int aCount = (a.kind == KIND_MULTI) ? a.pocketCount : 1;
        int bCount = (b.kind == KIND_MULTI) ? b.pocketCount : 1;
        if (aCount <= 0 || bCount <= 0) return;

        if (a.kind >= KIND_SINGLE && a.pocketData == null) return;
        if (b.kind >= KIND_SINGLE && b.pocketData == null) return;

        counts.clear();
        samplesA.clear();
        samplesB.clear();

        buildFaceOverlapCounts(faceA, a, faceB, b, counts, samplesA, samplesB);

        if (a.kind == KIND_MULTI && b.kind == KIND_MULTI) {
            verifyMultiMulti(aKey, bKey, faceA, faceB, a, b, counts, samplesA, samplesB, out);
            return;
        }

        if (a.kind == KIND_MULTI && (b.kind == KIND_SINGLE || b.kind == KIND_UNI)) {
            verifyMultiToNonMulti(aKey, bKey, faceA, true, a.multi, counts, samplesA, out);
            return;
        }

        if ((a.kind == KIND_SINGLE || a.kind == KIND_UNI) && b.kind == KIND_MULTI) {
            verifyMultiToNonMulti(bKey, aKey, faceB, false, b.multi, counts, samplesB, out);
            return;
        }

        if (a.kind == KIND_SINGLE && b.kind == KIND_SINGLE) {
            verifySingleSingle(aKey, bKey, faceA, faceB, counts, samplesA, samplesB, a.single, b.single, out);
            return;
        }

        if (a.kind == KIND_SINGLE && b.kind == KIND_UNI) {
            verifySingleToUni(aKey, bKey, faceA, counts, samplesA, a.single, out);
            return;
        }

        if (a.kind == KIND_UNI && b.kind == KIND_SINGLE) {
            verifySingleToUni(bKey, aKey, faceB, counts, samplesB, b.single, out);
        }
    }

    private static void addMismatch(List<? super ErrorRecord> out, long sectionA, long sectionB, int faceA, int pocketA,
                                    int pocketB, int expected, int actual, int sampleIdx) {
        int sx = Library.getSectionX(sectionA) << 4;
        int sy = Library.getSectionY(sectionA) << 4;
        int sz = Library.getSectionZ(sectionA) << 4;

        int idx = sampleIdx >= 0 ? sampleIdx : FACE_PLANE[faceA << 8];
        int lx = idx & 15;
        int ly = (idx >> 8) & 15;
        int lz = (idx >> 4) & 15;

        out.add(new ErrorRecord(sectionA, sectionB, faceA, pocketA, pocketB, expected, actual, sx + lx, sy + ly,
                sz + lz));
    }

    private static void renderState(WorldRadiationData data, int pcx, int pcz, int radius, FocusFilter filter,
                                    double camX, double camY, double camZ) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRenderer;
        RenderManager rm = mc.getRenderManager();

        double maxDistSq = 64.0 * 64.0;

        int[] counts = new int[MAX_POCKETS];
        int[] sumX = new int[MAX_POCKETS];
        int[] sumY = new int[MAX_POCKETS];
        int[] sumZ = new int[MAX_POCKETS];

        List<String> lines = new ArrayList<>(16);

        GlStateManager.enableTexture2D();

        Sec sec = new Sec();
        Sec nei = new Sec();

        for (int cx = pcx - radius; cx <= pcx + radius; cx++) {
            for (int cz = pcz - radius; cz <= pcz + radius; cz++) {
                for (int sy = 0; sy < 16; sy++) {
                    int baseX = cx << 4;
                    int baseY = sy << 4;
                    int baseZ = cz << 4;

                    if (filter != null && filter.isSectionOutsideFilter(baseX, baseY, baseZ)) continue;

                    long sck = Library.sectionToLong(cx, sy, cz);
                    if (!resolveSection(data, sck, sec)) continue;

                    double cxp = baseX + 8.0;
                    double cyp = baseY + 8.0;
                    double czp = baseZ + 8.0;

                    double dx = (cxp + 0.5) - camX;
                    double dy = (cyp + 0.5) - camY;
                    double dz = (czp + 0.5) - camZ;
                    if (dx * dx + dy * dy + dz * dz > maxDistSq) continue;

                    int kind = sec.kind;

                    if (kind == KIND_UNI) {
                        double r = readRad(sec, 0);
                        boolean act = readActive(sec, 0);

                        lines.clear();
                        lines.add("UNI r=" + formatRad(r) + (act ? " A" : " I"));
                        lines.add("sec " + cx + "," + sy + "," + cz);

                        appendLinks(lines, data, cx, sy, cz, sec, 0, nei);
                        renderLabelLines(fr, rm, lines, cxp + 0.5, cyp + 0.5, czp + 0.5, 0xFFFFFF, 0.02f);
                        continue;
                    }

                    if (kind == KIND_SINGLE) {
                        double r = readRad(sec, 0);
                        boolean act = readActive(sec, 0);

                        short[] pocketData = sec.pocketData;
                        double px = cxp + 0.5;
                        double py = cyp + 0.5;
                        double pz = czp + 0.5;

                        if (pocketData != null) {
                            counts[0] = 0;
                            sumX[0] = 0;
                            sumY[0] = 0;
                            sumZ[0] = 0;

                            for (int idx = 0; idx < 4096; idx++) {
                                if (pocketData[idx] != 0) continue;
                                counts[0]++;
                                sumX[0] += (idx & 15);
                                sumY[0] += (idx >> 8) & 15;
                                sumZ[0] += (idx >> 4) & 15;
                            }

                            if (counts[0] > 0) {
                                px = baseX + (sumX[0] / (double) counts[0]) + 0.5;
                                py = baseY + (sumY[0] / (double) counts[0]) + 0.5;
                                pz = baseZ + (sumZ[0] / (double) counts[0]) + 0.5;
                            }
                        }

                        lines.clear();
                        lines.add("S r=" + formatRad(r) + (act ? " A" : " I"));
                        lines.add("sec " + cx + "," + sy + "," + cz);

                        appendLinks(lines, data, cx, sy, cz, sec, 0, nei);
                        renderLabelLines(fr, rm, lines, px, py, pz, 0xFFFFFF, 0.02f);
                        continue;
                    }

                    int pocketCount = sec.pocketCount;
                    short[] pocketData = sec.pocketData;
                    if (pocketData == null || pocketCount <= 0) continue;

                    for (int i = 0; i < pocketCount; i++) {
                        counts[i] = 0;
                        sumX[i] = 0;
                        sumY[i] = 0;
                        sumZ[i] = 0;
                    }

                    for (int idx = 0; idx < 4096; idx++) {
                        int pi = pocketData[idx];
                        if (pi == NO_POCKET || pi < 0 || pi >= pocketCount) continue;
                        counts[pi]++;
                        sumX[pi] += (idx & 15);
                        sumY[pi] += (idx >> 8) & 15;
                        sumZ[pi] += (idx >> 4) & 15;
                    }

                    for (int pi = 0; pi < pocketCount; pi++) {
                        if (counts[pi] <= 0) continue;

                        double px = baseX + (sumX[pi] / (double) counts[pi]) + 0.5;
                        double py = baseY + (sumY[pi] / (double) counts[pi]) + 0.5;
                        double pz = baseZ + (sumZ[pi] / (double) counts[pi]) + 0.5;

                        double r = readRad(sec, pi);
                        boolean act = readActive(sec, pi);

                        lines.clear();
                        lines.add("P" + pi + " r=" + formatRad(r) + (act ? " A" : " I"));
                        lines.add("sec " + cx + "," + sy + "," + cz + " id=" + pi);

                        appendLinks(lines, data, cx, sy, cz, sec, pi, nei);
                        renderLabelLines(fr, rm, lines, px, py, pz, 0xFFFFFF, 0.02f);
                    }
                }
            }
        }
    }

    private static void appendLinks(List<? super String> lines, WorldRadiationData data, int cx, int sy, int cz,
                                    Sec cur, int myPi, Sec nei) {
        int linkId = 1;
        boolean active = readActive(cur, myPi);

        for (int face = 0; face < 6; face++) {
            int nsy = sy + FACE_DY[face];
            if (nsy < 0 || nsy > 15) continue;

            int ncx = cx + FACE_DX[face];
            int ncz = cz + FACE_DZ[face];

            long nKey = Library.sectionToLong(ncx, nsy, ncz);
            if (!resolveSection(data, nKey, nei)) continue;

            int nKind = nei.kind;
            int faceB = face ^ 1;
            String faceName = FACE_NAMES[face];

            if (cur.kind == KIND_MULTI) {
                MultiSectionRef m = cur.multi;

                if (nKind == KIND_UNI || nKind == KIND_SINGLE) {
                    int area = readConnUniSingle(m, myPi, face);
                    if (area > 0) lines.add(
                            formatLink(linkId++, nKind == KIND_UNI ? "UNI" : "S", ncx, nsy, ncz, 0, faceName, area,
                                    active));
                } else if (nKind == KIND_MULTI) {
                    int[] edges = m.edgesByFace[face];
                    int edgeCount = m.edgeCounts[face] & 0xFFFF;
                    if (edges != null && edgeCount > 0) {
                        for (int i = 0; i < edgeCount; i++) {
                            int edge = edges[i];
                            int pa = (edge >>> 20) & 0x7FF;
                            if (pa == myPi) {
                                int pb = (edge >>> 9) & 0x7FF;
                                int area = edge & 0x1FF;
                                lines.add(formatLink(linkId++, "M", ncx, nsy, ncz, pb, faceName, area, active));
                            }
                        }
                    }
                }
                continue;
            }

            if (cur.kind == KIND_SINGLE) {
                if (nKind == KIND_UNI || nKind == KIND_SINGLE) {
                    int area = readSingleConn(cur.single, face);
                    if (area > 0) lines.add(
                            formatLink(linkId++, nKind == KIND_UNI ? "UNI" : "S", ncx, nsy, ncz, 0, faceName, area,
                                    active));
                    continue;
                }

                if (nKind == KIND_MULTI) {
                    MultiSectionRef nm = nei.multi;
                    int[] edges = nm.edgesByFace[faceB];
                    int edgeCount = nm.edgeCounts[faceB] & 0xFFFF;
                    if (edges != null && edgeCount > 0) {
                        for (int i = 0; i < edgeCount; i++) {
                            int edge = edges[i];
                            int pb = (edge >>> 20) & 0x7FF; // neighbor's local pocket
                            int area = edge & 0x1FF;
                            lines.add(formatLink(linkId++, "M", ncx, nsy, ncz, pb, faceName, area, active));
                        }
                    }
                }
                continue;
            }

            if (nKind == KIND_SINGLE) {
                int area = readSingleConn(nei.single, faceB);
                if (area > 0) lines.add(formatLink(linkId++, "S", ncx, nsy, ncz, 0, faceName, area, active));
                continue;
            }

            if (nKind == KIND_MULTI) {
                MultiSectionRef nm = nei.multi;
                int[] edges = nm.edgesByFace[faceB];
                int edgeCount = nm.edgeCounts[faceB] & 0xFFFF;
                if (edges != null && edgeCount > 0) {
                    for (int i = 0; i < edgeCount; i++) {
                        int edge = edges[i];
                        int pb = (edge >>> 20) & 0x7FF;
                        int area = edge & 0x1FF;
                        lines.add(formatLink(linkId++, "M", ncx, nsy, ncz, pb, faceName, area, active));
                    }
                }
            }
        }
    }

    private static String formatLink(int idx, String type, int cx, int sy, int cz, int id, String face, int area,
                                     boolean active) {
        return "Link" + idx + ": " + type + " (" + cx + ',' + sy + ',' + cz + ')' + " id=" + id + ' ' + face + " area=" + area + " active=" + (active ? "Y" : "N");
    }

    private static boolean resolveSection(WorldRadiationData data, long sectionKey, Sec out) {
        try {
            int sy = Library.getSectionY(sectionKey);
            if ((sy & ~15) != 0) {
                out.clear();
                return false;
            }

            long ck = Library.sectionToChunkLong(sectionKey);
            int ownerId = data.getId(ck);
            if (ownerId < 0 || ownerId >= data.mcChunks.length) {
                out.clear();
                return false;
            }
            if (data.cks[ownerId] != ck || data.mcChunks[ownerId] == null) {
                out.clear();
                return false;
            }
            int secIdx = (ownerId << 4) | sy;

            int kind = data.getKind(ownerId, sy);
            if (kind == KIND_NONE) {
                out.clear();
                return false;
            }

            out.data = data;
            out.idx = secIdx;
            out.kind = kind;

            if (kind == KIND_UNI) {
                out.sc = null;
                out.multi = null;
                out.single = null;
                out.pocketData = null;
                out.pocketCount = 1;
                return true;
            }

            SectionRef sc = data.complexSecs[secIdx];
            if (sc == null || sc.pocketCount <= 0) {
                out.clear();
                return false;
            }
            out.sc = sc;

            if (kind == KIND_SINGLE) {
                if (!(sc instanceof SingleMaskedSectionRef s)) {
                    out.clear();
                    return false;
                }
                out.single = s;
                out.multi = null;
                out.pocketData = s.pocketData;
                out.pocketCount = 1;
                return true;
            }

            if (kind == KIND_MULTI) {
                if (!(sc instanceof MultiSectionRef m)) {
                    out.clear();
                    return false;
                }
                out.multi = m;
                out.single = null;
                out.pocketData = m.pocketData;
                int pc = m.pocketCount & 0xFFFF;
                out.pocketCount = Math.min(pc, MAX_POCKETS);
                return out.pocketCount > 0;
            }

            out.clear();
            return false;
        } catch (Throwable _) {
            out.clear();
            return false;
        }
    }

    private static double readRad(Sec sec, int pocketIndex) {
        if (sec.kind == KIND_UNI || sec.kind == KIND_SINGLE) return sec.data.uniformRads[sec.idx];
        if (sec.kind == KIND_MULTI) {
            if (pocketIndex < 0 || pocketIndex >= sec.pocketCount) return 0.0d;
            return sec.multi.data[pocketIndex << 1];
        }
        return 0.0d;
    }

    private static boolean readActive(Sec sec, int pocketIndex) {
        if (sec.data == null || sec.idx < 0) return false;
        int ownerId = sec.idx >>> 4;
        int sy = sec.idx & 15;
        if (!sec.data.isSectionActive(ownerId, sy)) return false;
        return readRad(sec, pocketIndex) > 0.0D;
    }

    private static int pocketIndexAt(Sec sec, int blockIndex) {
        int kind = sec.kind;
        if (kind == KIND_UNI) return 0;

        short[] data = sec.pocketData;
        if (data == null) return -1;

        int pi = data[blockIndex];
        if (pi == NO_POCKET) return -1;

        if (kind == KIND_SINGLE) return (pi == 0) ? 0 : -1;

        return (pi >= 0 && pi < sec.pocketCount) ? pi : -1;
    }

    private static int readConnMulti(MultiSectionRef ms, int myPi, int face, int neighborPocket) {
        if (ms == null) return 0;
        int[] edges = ms.edgesByFace[face];
        int edgeCount = ms.edgeCounts[face] & 0xFFFF;
        if (edges == null || edgeCount == 0) return 0;
        for (int i = 0; i < edgeCount; i++) {
            int edge = edges[i];
            int pa = (edge >>> 20) & 0x7FF;
            if (pa != myPi) continue;
            int pb = (edge >>> 9) & 0x7FF;
            if (pb == neighborPocket) return edge & 0x1FF;
        }
        return 0;
    }

    private static int readConnUniSingle(MultiSectionRef ms, int myPi, int face) {
        if (ms == null) return 0;
        int[] edges = ms.edgesByFace[face];
        int edgeCount = ms.edgeCounts[face] & 0xFFFF;
        if (edges == null || edgeCount == 0) return 0;
        for (int i = 0; i < edgeCount; i++) {
            int edge = edges[i];
            int pa = (edge >>> 20) & 0x7FF;
            if (pa == myPi) return edge & 0x1FF;
        }
        return 0;
    }

    private static int readSingleConn(SingleMaskedSectionRef s, int face) {
        if (s == null) return 0;
        if (face < 0 || face > 5) return 0;
        return (int) ((s.connections >>> (face * 9)) & 0x1FFL);
    }

    private static float computeRadAlpha(double rad, float baseAlpha, boolean active) {
        float scale;
        double abs = Math.abs(rad);

        if (abs <= 0.0d) {
            scale = 0.2f;
        } else {
            scale = (float) (Math.log1p(abs) / Math.log1p(RAD_REF));
            if (scale < 0.0f) scale = 0.0f;
            if (scale > 1.0f) scale = 1.0f;
            scale = 0.2f + 0.8f * scale;
        }

        float a = baseAlpha * scale;
        if (active) a = Math.min(1.0f, a + 0.15f);
        return a;
    }

    private static float[] kindColor(int kind) {
        return switch (kind) {
            case KIND_UNI -> COLOR_UNI;
            case KIND_SINGLE -> COLOR_SINGLE;
            case KIND_MULTI -> COLOR_MULTI;
            default -> COLOR_RESIST;
        };
    }

    private static void addBoxLines(BufferBuilder buf, double x1, double y1, double z1, double x2, double y2, double z2,
                                    float r, float g, float b, float a) {
        addLine(buf, x1, y1, z1, x2, y1, z1, r, g, b, a);
        addLine(buf, x1, y1, z1, x1, y2, z1, r, g, b, a);
        addLine(buf, x1, y1, z1, x1, y1, z2, r, g, b, a);

        addLine(buf, x2, y2, z2, x1, y2, z2, r, g, b, a);
        addLine(buf, x2, y2, z2, x2, y1, z2, r, g, b, a);
        addLine(buf, x2, y2, z2, x2, y2, z1, r, g, b, a);

        addLine(buf, x1, y2, z1, x1, y2, z2, r, g, b, a);
        addLine(buf, x1, y2, z1, x2, y2, z1, r, g, b, a);

        addLine(buf, x2, y1, z1, x2, y1, z2, r, g, b, a);
        addLine(buf, x2, y1, z1, x2, y2, z1, r, g, b, a);

        addLine(buf, x1, y1, z2, x2, y1, z2, r, g, b, a);
        addLine(buf, x1, y1, z2, x1, y2, z2, r, g, b, a);
    }

    private static void addCross(BufferBuilder buf, double x, double y, double z, int face, double size, float r,
                                 float g, float b, float a) {
        double s = size * 0.5;
        switch (face) {
            case 0, 1 -> {
                double yy = y + (face == 0 ? -0.01 : 0.01);
                addLine(buf, x - s, yy, z, x + s, yy, z, r, g, b, a);
                addLine(buf, x, yy, z - s, x, yy, z + s, r, g, b, a);
            }
            case 2, 3 -> {
                double zz = z + (face == 2 ? -0.01 : 0.01);
                addLine(buf, x - s, y, zz, x + s, y, zz, r, g, b, a);
                addLine(buf, x, y - s, zz, x, y + s, zz, r, g, b, a);
            }
            case 4, 5 -> {
                double xx = x + (face == 4 ? -0.01 : 0.01);
                addLine(buf, xx, y - s, z, xx, y + s, z, r, g, b, a);
                addLine(buf, xx, y, z - s, xx, y, z + s, r, g, b, a);
            }
        }
    }

    private static void addLine(BufferBuilder buf, double x1, double y1, double z1, double x2, double y2, double z2,
                                float r, float g, float b, float a) {
        buf.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        buf.pos(x2, y2, z2).color(r, g, b, a).endVertex();
    }

    private static Vec3d sectionCenter(long sectionKey) {
        int cx = Library.getSectionX(sectionKey) << 4;
        int cy = Library.getSectionY(sectionKey) << 4;
        int cz = Library.getSectionZ(sectionKey) << 4;
        return new Vec3d(cx + 8.0, cy + 8.0, cz + 8.0);
    }

    private static float[] hsvToRgb(float h, float s, float v) {
        float r, g, b;

        int i = (int) Math.floor(h * 6.0f);
        float f = h * 6.0f - i;
        float p = v * (1.0f - s);
        float q = v * (1.0f - f * s);
        float t = v * (1.0f - (1.0f - f) * s);

        switch (i % 6) { //@formatter:off
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        } //@formatter:on

        return new float[]{r, g, b};
    }

    private static void renderLabelLines(FontRenderer fr, RenderManager rm, List<String> lines, double x, double y,
                                         double z, int color, float scale) {
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(rm.playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-scale, -scale, scale);
            GlStateManager.disableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.depthMask(false);
            int lineHeight = fr.FONT_HEIGHT + 1;
            int size = lines.size();
            int yBase = -((size - 1) * lineHeight) / 2;
            for (int i = 0; i < size; i++) {
                String line = lines.get(i);
                int width = fr.getStringWidth(line);
                fr.drawString(line, -width / 2, yBase + i * lineHeight, color, false);
            }
        } finally {
            GlStateManager.depthMask(true);
            GlStateManager.popMatrix();
        }
    }

    private static String formatRad(double v) {
        if (Double.isNaN(v)) return "nan";
        if (v == Double.POSITIVE_INFINITY) return "inf";
        if (v == Double.NEGATIVE_INFINITY) return "-inf";
        return String.format(Locale.ROOT, "%.3e", v);
    }

    public enum Mode {
        WIRE, SLICE, SECTIONS(0.01f, -16f, -1f), POCKETS(0.01f, -2f, -512f), STATE, ERRORS(0.01f, -16f, -256f);
        final float inset, polygonFactor, polygonUnits;

        Mode() {
            this(0.01f, -1f, -1f);
        }

        Mode(float i, float pf, float ps) {
            inset = i;
            polygonFactor = pf;
            polygonUnits = ps;
        }
    }

    private static final class RenderScratch {
        final float[] radAlpha = new float[MAX_POCKETS];
        final boolean[] active = new boolean[MAX_POCKETS];
        final double[] rad = new double[MAX_POCKETS];
        final int[] packedPocketColor = new int[MAX_POCKETS];
        final int[] quadVertexDataScratch = new int[PositionColorVertexWriter.QUAD_VERTEX_DATA_INTS];
        final Long2IntOpenHashMap counts = new Long2IntOpenHashMap();
        final Long2IntOpenHashMap samplesA = new Long2IntOpenHashMap();
        final Long2IntOpenHashMap samplesB = new Long2IntOpenHashMap();
    }

    public static final class Config {
        public boolean enabled = false;
        public int radiusChunks = 2;
        public Mode mode = Mode.POCKETS;
        public boolean sliceAutoY = true;
        public int sliceY = 0;
        public boolean depth = true;
        public float alpha = 0.4f;
        public boolean verify = false;
        public int verifyInterval = 10;
        public boolean focusEnabled = false;
        public BlockPos focusAnchor = null;
        public int focusDx = 0;
        public int focusDy = 0;
        public int focusDz = 0;

        public void reset() {
            enabled = false;
            radiusChunks = 2;
            mode = Mode.POCKETS;
            sliceAutoY = true;
            sliceY = 0;
            depth = true;
            alpha = 0.4f;
            verify = false;
            verifyInterval = 10;
            focusEnabled = false;
            focusAnchor = null;
            focusDx = 0;
            focusDy = 0;
            focusDz = 0;
        }
    }

    private record PocketMesh(short[] pocketDataRef, int pocketCount, long[] quads) {
    }

    private record MultiOuterMeshes(short[] pocketDataRef, int pocketCount, long[][] sectionFaceQuads,
                                    long[][] warnFaceQuads, int[][] facePockets, byte[] sampleU, byte[] sampleV) {
    }

    private record SingleOuterMeshes(short[] pocketDataRef, long[][] sectionFaceQuads) {
    }

    private record ErrorRecord(long sectionA, long sectionB, int faceA, int pocketA, int pocketB, int expected,
                               int actual, int sampleX, int sampleY, int sampleZ) {
    }

    private record FocusFilter(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        static FocusFilter fromConfig(Config cfg, EntityPlayer player) {
            if (!cfg.focusEnabled) return null;

            BlockPos anchor = cfg.focusAnchor != null ? cfg.focusAnchor : player.getPosition();
            int dx = Math.max(0, cfg.focusDx);
            int dy = Math.max(0, cfg.focusDy);
            int dz = Math.max(0, cfg.focusDz);
            if (dx == 0 && dy == 0 && dz == 0) return null;

            return new FocusFilter(anchor.getX() - dx, anchor.getX() + dx, anchor.getY() - dy, anchor.getY() + dy,
                    anchor.getZ() - dz, anchor.getZ() + dz);
        }

        boolean isSectionOutsideFilter(int baseX, int baseY, int baseZ) {
            int maxX = baseX + 15;
            int maxY = baseY + 15;
            int maxZ = baseZ + 15;
            return this.maxX < baseX || minX > maxX || this.maxY < baseY || minY > maxY || this.maxZ < baseZ || minZ > maxZ;
        }
    }

    private static final class Sec {
        WorldRadiationData data;
        int idx = -1;
        int kind;

        SectionRef sc;
        MultiSectionRef multi;
        SingleMaskedSectionRef single;

        short[] pocketData;
        int pocketCount;

        void clear() {
            data = null;
            idx = -1;
            sc = null;
            multi = null;
            single = null;
            pocketData = null;
            pocketCount = 0;
            kind = KIND_NONE;
        }
    }
}
