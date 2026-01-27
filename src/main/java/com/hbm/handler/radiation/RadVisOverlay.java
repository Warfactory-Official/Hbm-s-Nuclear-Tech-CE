package com.hbm.handler.radiation;

import com.hbm.lib.Library;
import com.hbm.util.RenderUtil;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.hbm.handler.radiation.RadiationSystemNT.*;

@SideOnly(Side.CLIENT)
public final class RadVisOverlay {
    public static final Config CONFIG = new Config();

    private static final int MAX_RADIUS = 8;
    private static final double RAD_REF = 1000.0d;
    private static final int[] VERIFY_FACES = {5, 3, 1};

    private static final float[] COLOR_UNI = {0.25f, 0.6f, 1.0f};
    private static final float[] COLOR_SINGLE = {0.25f, 1.0f, 0.5f};
    private static final float[] COLOR_MULTI = {1.0f, 0.6f, 0.25f};
    private static final float[] COLOR_RESIST = {0.2f, 0.2f, 0.25f};
    private static final float[] COLOR_WARN = {1.0f, 0.6f, 0.1f};
    private static final float[] COLOR_ERR = {1.0f, 0.1f, 0.1f};
    private static final float[] COLOR_INACTIVE = {1.0f, 0.0f, 1.0f};

    private static final float[][] POCKET_COLORS = new float[16][3];
    private static final String[] FACE_NAMES = {"DOWN", "UP", "NORTH", "SOUTH", "WEST", "EAST"};

    private static final List<ErrorRecord> ERROR_CACHE = new ArrayList<>();
    private static long lastVerifyTick = Long.MIN_VALUE;

    static {
        for (int i = 0; i < POCKET_COLORS.length; i++) {
            float h = (i * 0.21f + 0.1f) % 1.0f;
            float[] rgb = hsvToRgb(h, 0.7f, 0.95f);
            POCKET_COLORS[i][0] = rgb[0];
            POCKET_COLORS[i][1] = rgb[1];
            POCKET_COLORS[i][2] = rgb[2];
        }
    }

    private RadVisOverlay() {
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
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

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
                case FACES -> renderFaces(data, pcx, pcz, radius, filter);
                case STATE -> renderState(data, pcx, pcz, radius, filter, camX, camY, camZ);
                case ERRORS -> renderErrors(data, pcx, pcz, radius, filter);
            }
        } catch (Throwable ignored) {
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
        runVerification(data, pcx, pcz, radius, filter, ERROR_CACHE);
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
                    float tint = (sec.kind == ChunkRef.KIND_MULTI) ? (0.5f + 0.5f * (sec.pocketCount / 15.0f)) : 1.0f;
                    addBoxLines(buf, baseX, baseY, baseZ, baseX + 16, baseY + 16, baseZ + 16, color[0] * tint, color[1] * tint, color[2] * tint, CONFIG.alpha);
                }
            }
        }

        tess.draw();
    }

    private static void renderSlice(WorldRadiationData data, int pcx, int pcz, int radius, int sliceY, FocusFilter filter) {
        int sy = sliceY >> 4;
        if (sy < 0 || sy > 15) return;

        GlStateManager.disableTexture2D();

        if (CONFIG.depth) {
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(-1.0f, -1.0f);
        }

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        int localY = sliceY & 15;
        int idxBase = localY << 8;
        double y = sliceY + 0.02;

        float baseAlpha = MathHelper.clamp(CONFIG.alpha, 0.0f, 1.0f);

        float[] radAlpha = new float[16];
        boolean[] active = new boolean[16];
        double[] rad = new double[16];

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

                if (kind == ChunkRef.KIND_UNI) {
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
                        if (kind == ChunkRef.KIND_UNI) {
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
                            float[] c = POCKET_COLORS[pi & 15];
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

        if (CONFIG.depth) GlStateManager.disablePolygonOffset();
    }

    private static void renderFaces(WorldRadiationData data, int pcx, int pcz, int radius, FocusFilter filter) {
        GlStateManager.disableTexture2D();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        int[] counts = new int[16];

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
                    if (kind == ChunkRef.KIND_UNI) continue;

                    int pocketCount = sec.pocketCount;

                    for (int face = 0; face < 6; face++) {
                        if (kind == ChunkRef.KIND_SINGLE) {
                            int exposed = sec.single.getFaceCount(face);
                            if (exposed <= 0) continue;
                            float[] color = kindColor(kind);
                            addFaceQuad(buf, baseX, baseY, baseZ, face, color[0], color[1], color[2], CONFIG.alpha * 0.5f, 0.01f);
                            continue;
                        }

                        for (int i = 0; i < pocketCount; i++) counts[i] = 0;

                        int planeBase = face << 8;
                        for (int t = 0; t < 256; t++) {
                            int idx = FACE_PLANE[planeBase + t];
                            int pi = pocketIndexAt(sec, idx);
                            if (pi >= 0) counts[pi]++;
                        }

                        int nonZero = 0;
                        for (int i = 0; i < pocketCount; i++) if (counts[i] > 0) nonZero++;
                        if (nonZero == 0) continue;

                        long neighborKey = Library.sectionToLong(cx + FACE_DX[face], sy + FACE_DY[face], cz + FACE_DZ[face]);
                        boolean leakRisk = resolveSection(data, neighborKey, nei) && nei.kind == ChunkRef.KIND_UNI && nonZero > 1;

                        float[] color = leakRisk ? COLOR_WARN : kindColor(kind);
                        addFaceQuad(buf, baseX, baseY, baseZ, face, color[0], color[1], color[2], CONFIG.alpha * 0.5f, 0.01f);
                    }
                }
            }
        }

        tess.draw();
    }

    private static void renderState(WorldRadiationData data, int pcx, int pcz, int radius, FocusFilter filter, double camX, double camY, double camZ) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRenderer;
        RenderManager rm = mc.getRenderManager();

        double maxDistSq = 64.0 * 64.0;

        int[] counts = new int[16];
        int[] sumX = new int[16];
        int[] sumY = new int[16];
        int[] sumZ = new int[16];
        int[] linkCounts = new int[16];

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

                    if (kind == ChunkRef.KIND_UNI) {
                        double r = readRad(sec, 0);
                        boolean act = readActive(sec, 0);

                        lines.clear();
                        lines.add("UNI r=" + formatRad(r) + (act ? " A" : " I"));
                        lines.add("sec " + cx + "," + sy + "," + cz);

                        appendLinks(lines, data, sck, cx, sy, cz, sec, 0, linkCounts, nei);
                        renderLabelLines(fr, rm, lines, cxp + 0.5, cyp + 0.5, czp + 0.5, 0xFFFFFF, 0.02f);
                        continue;
                    }

                    if (kind == ChunkRef.KIND_SINGLE) {
                        double r = readRad(sec, 0);
                        boolean act = readActive(sec, 0);

                        byte[] pocketData = sec.pocketData;
                        double px = cxp + 0.5;
                        double py = cyp + 0.5;
                        double pz = czp + 0.5;

                        if (pocketData != null) {
                            counts[0] = 0;
                            sumX[0] = 0;
                            sumY[0] = 0;
                            sumZ[0] = 0;

                            for (int idx = 0; idx < 4096; idx++) {
                                if (readNibble(pocketData, idx) != 0) continue;
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

                        appendLinks(lines, data, sck, cx, sy, cz, sec, 0, linkCounts, nei);
                        renderLabelLines(fr, rm, lines, px, py, pz, 0xFFFFFF, 0.02f);
                        continue;
                    }

                    // MULTI
                    int pocketCount = sec.pocketCount;
                    byte[] pocketData = sec.pocketData;
                    if (pocketData == null || pocketCount <= 0) continue;

                    for (int i = 0; i < pocketCount; i++) {
                        counts[i] = 0;
                        sumX[i] = 0;
                        sumY[i] = 0;
                        sumZ[i] = 0;
                    }

                    for (int idx = 0; idx < 4096; idx++) {
                        int nibble = readNibble(pocketData, idx);
                        if (nibble == NO_POCKET || nibble < 0 || nibble >= pocketCount) continue;
                        counts[nibble]++;
                        sumX[nibble] += (idx & 15);
                        sumY[nibble] += (idx >> 8) & 15;
                        sumZ[nibble] += (idx >> 4) & 15;
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

                        appendLinks(lines, data, sck, cx, sy, cz, sec, pi, linkCounts, nei);
                        renderLabelLines(fr, rm, lines, px, py, pz, 0xFFFFFF, 0.02f);
                    }
                }
            }
        }
    }

    private static void renderErrors(WorldRadiationData data, int pcx, int pcz, int radius, FocusFilter filter) {
        GlStateManager.disableTexture2D();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        int[] counts = new int[16];
        int[] sample = new int[16];

        int[] crossData = new int[128];
        int crossCount = 0;

        Sec sec = new Sec();
        Sec nei = new Sec();

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

                    if (sec.kind != ChunkRef.KIND_MULTI) continue;

                    int pocketCount = sec.pocketCount;
                    if (pocketCount <= 1) continue;

                    for (int face = 0; face < 6; face++) {
                        long neighborKey = Library.sectionToLong(cx + FACE_DX[face], sy + FACE_DY[face], cz + FACE_DZ[face]);
                        if (!resolveSection(data, neighborKey, nei) || nei.kind != ChunkRef.KIND_UNI) continue;

                        for (int i = 0; i < pocketCount; i++) {
                            counts[i] = 0;
                            sample[i] = -1;
                        }

                        int planeBase = face << 8;
                        for (int t = 0; t < 256; t++) {
                            int idx = FACE_PLANE[planeBase + t];
                            int pi = pocketIndexAt(sec, idx);
                            if (pi >= 0) {
                                if (counts[pi] == 0) sample[pi] = idx;
                                counts[pi]++;
                            }
                        }

                        int nonZero = 0;
                        int firstPocket = -1;
                        for (int i = 0; i < pocketCount; i++) {
                            if (counts[i] > 0) {
                                nonZero++;
                                if (firstPocket == -1) firstPocket = i;
                            }
                        }

                        if (nonZero <= 1) continue;

                        addFaceQuad(buf, baseX, baseY, baseZ, face, COLOR_WARN[0], COLOR_WARN[1], COLOR_WARN[2], CONFIG.alpha * 0.5f, 0.015f);

                        for (int i = 0; i < pocketCount; i++) {
                            if (counts[i] <= 0 || i == firstPocket) continue;

                            int idx = sample[i] >= 0 ? sample[i] : FACE_PLANE[face << 8];
                            int lx = idx & 15;
                            int ly = (idx >> 8) & 15;
                            int lz = (idx >> 4) & 15;

                            if ((crossCount + 1) * 4 > crossData.length) {
                                crossData = Arrays.copyOf(crossData, crossData.length + (crossData.length >>> 1) + 16);
                            }

                            int off = crossCount * 4;
                            crossData[off] = baseX + lx;
                            crossData[off + 1] = baseY + ly;
                            crossData[off + 2] = baseZ + lz;
                            crossData[off + 3] = face;
                            crossCount++;
                        }
                    }
                }
            }
        }

        if (CONFIG.verify) {
            for (ErrorRecord rec : ERROR_CACHE) {
                int ax = Library.getSectionX(rec.sectionA) << 4;
                int ay = Library.getSectionY(rec.sectionA) << 4;
                int az = Library.getSectionZ(rec.sectionA) << 4;
                addFaceQuad(buf, ax, ay, az, rec.faceA, COLOR_ERR[0], COLOR_ERR[1], COLOR_ERR[2], CONFIG.alpha * 0.5f, 0.015f);
            }
        }

        tess.draw();

        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < crossCount; i++) {
            int off = i * 4;
            addCross(buf, crossData[off] + 0.5, crossData[off + 1] + 0.5, crossData[off + 2] + 0.5, crossData[off + 3], 0.35, COLOR_WARN[0], COLOR_WARN[1], COLOR_WARN[2], CONFIG.alpha);
        }

        if (CONFIG.verify) {
            for (ErrorRecord rec : ERROR_CACHE) {
                Vec3d a = sectionCenter(rec.sectionA);
                Vec3d b = sectionCenter(rec.sectionB);
                addLine(buf, a.x, a.y, a.z, b.x, b.y, b.z, COLOR_ERR[0], COLOR_ERR[1], COLOR_ERR[2], CONFIG.alpha);
                addCross(buf, rec.sampleX + 0.5, rec.sampleY + 0.5, rec.sampleZ + 0.5, rec.faceA, 0.35, COLOR_ERR[0], COLOR_ERR[1], COLOR_ERR[2], CONFIG.alpha);
            }
        }

        tess.draw();
    }

    private static void runVerification(WorldRadiationData data, int pcx, int pcz, int radius, FocusFilter filter, List<ErrorRecord> out) {
        int[] counts = new int[16 * 16];
        int[] samplesA = new int[16 * 16];
        int[] samplesB = new int[16 * 16];

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
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }
        }
    }

    private static void verifyPair(long aKey, long bKey, int faceA, Sec a, Sec b, int[] counts, int[] samplesA, int[] samplesB, List<ErrorRecord> out) {
        int faceB = faceA ^ 1;

        int aCount = (a.kind == ChunkRef.KIND_MULTI) ? a.pocketCount : 1;
        int bCount = (b.kind == ChunkRef.KIND_MULTI) ? b.pocketCount : 1;
        if (aCount <= 0 || bCount <= 0) return;

        if (a.kind >= ChunkRef.KIND_SINGLE && a.pocketData == null) return;
        if (b.kind >= ChunkRef.KIND_SINGLE && b.pocketData == null) return;

        int lim = aCount * bCount;

        for (int i = 0; i < lim; i++) {
            counts[i] = 0;
            samplesA[i] = -1;
            samplesB[i] = -1;
        }

        int planeA = faceA << 8;
        int planeB = faceB << 8;

        for (int t = 0; t < 256; t++) {
            int idxA = FACE_PLANE[planeA + t];
            int idxB = FACE_PLANE[planeB + t];

            int pa = paletteIndexForSection(a, idxA);
            int pb = paletteIndexForSection(b, idxB);
            if (pa < 0 || pb < 0) continue;

            int off = pa * bCount + pb;
            if (counts[off] == 0) {
                samplesA[off] = idxA;
                samplesB[off] = idxB;
            }
            counts[off]++;
        }

        if (a.kind == ChunkRef.KIND_MULTI && b.kind == ChunkRef.KIND_MULTI) {
            MultiSectionRef am = a.multi;
            MultiSectionRef bm = b.multi;

            int stride = 6 * NEI_SLOTS;

            for (int pa = 0; pa < aCount; pa++) {
                for (int pb = 0; pb < bCount; pb++) {
                    int off = pa * bCount + pb;
                    int actual = counts[off];

                    int storedA = readConn(am, stride, pa, faceA, pb);
                    int storedB = readConn(bm, stride, pb, faceB, pa);

                    if (storedA != actual) addMismatch(out, aKey, bKey, faceA, pa, pb, storedA, actual, samplesA[off]);
                    if (storedB != actual) addMismatch(out, bKey, aKey, faceB, pb, pa, storedB, actual, samplesB[off]);
                }
            }
            return;
        }

        if (a.kind == ChunkRef.KIND_MULTI && b.kind == ChunkRef.KIND_SINGLE) {
            MultiSectionRef am = a.multi;
            int stride = 6 * NEI_SLOTS;

            for (int pa = 0; pa < aCount; pa++) {
                int off = pa * bCount;
                int actual = counts[off];
                int stored = readConn(am, stride, pa, faceA, 0);
                if (stored != actual) addMismatch(out, aKey, bKey, faceA, pa, 0, stored, actual, samplesA[off]);
            }
            return;
        }

        if (a.kind == ChunkRef.KIND_SINGLE && b.kind == ChunkRef.KIND_MULTI) {
            MultiSectionRef bm = b.multi;
            int stride = 6 * NEI_SLOTS;

            for (int pb = 0; pb < bCount; pb++) {
                int actual = counts[pb];
                int stored = readConn(bm, stride, pb, faceB, 0);
                if (stored != actual) addMismatch(out, bKey, aKey, faceB, pb, 0, stored, actual, samplesB[pb]);
            }
            return;
        }

        if (a.kind == ChunkRef.KIND_SINGLE && b.kind == ChunkRef.KIND_SINGLE) {
            int actual = counts[0];

            int storedA = readSingleConn(a.single, faceA);
            int storedB = readSingleConn(b.single, faceB);

            if (storedA != actual) addMismatch(out, aKey, bKey, faceA, 0, 0, storedA, actual, samplesA[0]);
            if (storedB != actual) addMismatch(out, bKey, aKey, faceB, 0, 0, storedB, actual, samplesB[0]);
            return;
        }

        if (a.kind == ChunkRef.KIND_SINGLE && b.kind == ChunkRef.KIND_UNI) {
            int actual = counts[0];
            int exposedA = a.single.getFaceCount(faceA);
            if (exposedA != actual) addMismatch(out, aKey, bKey, faceA, 0, 0, exposedA, actual, samplesA[0]);
            return;
        }

        if (a.kind == ChunkRef.KIND_UNI && b.kind == ChunkRef.KIND_SINGLE) {
            int actual = counts[0];
            int exposedB = b.single.getFaceCount(faceB);
            if (exposedB != actual) addMismatch(out, bKey, aKey, faceB, 0, 0, exposedB, actual, samplesB[0]);
            return;
        }

        if (a.kind == ChunkRef.KIND_MULTI && b.kind == ChunkRef.KIND_UNI) {
            MultiSectionRef am = a.multi;
            int stride = 6 * NEI_SLOTS;

            for (int pa = 0; pa < aCount; pa++) {
                int off = pa * bCount;
                int actual = counts[off];
                int stored = readConn(am, stride, pa, faceA, 0);
                if (stored != actual) addMismatch(out, aKey, bKey, faceA, pa, 0, stored, actual, samplesA[off]);
            }
            return;
        }

        if (a.kind == ChunkRef.KIND_UNI && b.kind == ChunkRef.KIND_MULTI) {
            MultiSectionRef bm = b.multi;
            int stride = 6 * NEI_SLOTS;

            for (int pb = 0; pb < bCount; pb++) {
                int actual = counts[pb];
                int stored = readConn(bm, stride, pb, faceB, 0);
                if (stored != actual) addMismatch(out, bKey, aKey, faceB, pb, 0, stored, actual, samplesB[pb]);
            }
        }
    }

    private static void addMismatch(List<ErrorRecord> out, long sectionA, long sectionB, int faceA, int pocketA, int pocketB, int expected, int actual, int sampleIdx) {
        int sx = Library.getSectionX(sectionA) << 4;
        int sy = Library.getSectionY(sectionA) << 4;
        int sz = Library.getSectionZ(sectionA) << 4;

        int idx = sampleIdx >= 0 ? sampleIdx : FACE_PLANE[faceA << 8];
        int lx = idx & 15;
        int ly = (idx >> 8) & 15;
        int lz = (idx >> 4) & 15;

        out.add(new ErrorRecord(sectionA, sectionB, faceA, pocketA, pocketB, expected, actual, sx + lx, sy + ly, sz + lz));
    }

    private static void appendLinks(List<String> lines, WorldRadiationData data, long sectionKey, int cx, int sy, int cz, Sec cur, int pocketIndex, int[] linkCounts, Sec nei) {
        int linkId = 1;

        int myPi = (cur.kind == ChunkRef.KIND_MULTI) ? pocketIndex : 0;
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

            if (cur.kind == ChunkRef.KIND_MULTI) {
                MultiSectionRef m = cur.multi;

                int stride = 6 * NEI_SLOTS;
                int base = myPi * stride + face * NEI_SLOTS;

                boolean faceAct = m.faceActive[myPi * 6 + face] != 0;
                boolean sentinel = m.connectionArea[base] != 0;

                if (nKind == ChunkRef.KIND_UNI) {
                    int area = readConn(m, stride, myPi, face, 0);
                    if (area > 0)
                        lines.add(formatLink(linkId++, "UNI", ncx, nsy, ncz, 0, faceName, area, faceAct, sentinel));
                } else if (nKind == ChunkRef.KIND_SINGLE) {
                    int area = readConn(m, stride, myPi, face, 0);
                    if (area > 0)
                        lines.add(formatLink(linkId++, "S", ncx, nsy, ncz, 0, faceName, area, faceAct, sentinel));
                } else if (nKind == ChunkRef.KIND_MULTI) {
                    int nCount = nei.pocketCount;
                    for (int pb = 0; pb < nCount; pb++) {
                        int area = readConn(m, stride, myPi, face, pb);
                        if (area > 0)
                            lines.add(formatLink(linkId++, "M", ncx, nsy, ncz, pb, faceName, area, faceAct, sentinel));
                    }
                }
                continue;
            }

            if (cur.kind == ChunkRef.KIND_SINGLE) {
                if (nKind == ChunkRef.KIND_UNI || nKind == ChunkRef.KIND_SINGLE) {
                    int area = readSingleConn(cur.single, face);
                    if (area > 0)
                        lines.add(formatLink(linkId++, nKind == ChunkRef.KIND_UNI ? "UNI" : "S", ncx, nsy, ncz, 0, faceName, area, active, null));
                    continue;
                }

                if (nKind == ChunkRef.KIND_MULTI) {
                    byte[] myData = cur.pocketData;
                    byte[] nData = nei.pocketData;
                    int nCount = nei.pocketCount;

                    if (myData == null || nData == null || nCount <= 0) continue;

                    for (int i = 0; i < nCount; i++) linkCounts[i] = 0;

                    int planeA = face << 8;
                    int planeB = faceB << 8;

                    for (int t = 0; t < 256; t++) {
                        int idxA = FACE_PLANE[planeA + t];
                        if (readNibble(myData, idxA) != 0) continue;

                        int idxB = FACE_PLANE[planeB + t];
                        int pb = pocketIndexAt(nei, idxB);
                        if (pb >= 0) linkCounts[pb]++;
                    }

                    for (int pb = 0; pb < nCount; pb++) {
                        int area = linkCounts[pb];
                        if (area > 0)
                            lines.add(formatLink(linkId++, "M", ncx, nsy, ncz, pb, faceName, area, active, null));
                    }
                }

                continue;
            }

            // cur is UNI
            if (nKind == ChunkRef.KIND_SINGLE) {
                int area = readSingleConn(nei.single, faceB);
                if (area > 0) lines.add(formatLink(linkId++, "S", ncx, nsy, ncz, 0, faceName, area, active, null));
                continue;
            }

            if (nKind == ChunkRef.KIND_MULTI) {
                MultiSectionRef nm = nei.multi;
                int stride = 6 * NEI_SLOTS;

                int nCount = nei.pocketCount;
                for (int pb = 0; pb < nCount; pb++) {
                    int area = readConn(nm, stride, pb, faceB, 0);
                    if (area > 0) lines.add(formatLink(linkId++, "M", ncx, nsy, ncz, pb, faceName, area, active, null));
                }
            }
        }
    }

    private static String formatLink(int idx, String type, int cx, int sy, int cz, int id, String face, int area, boolean active, Boolean sentinel) {
        StringBuilder sb = new StringBuilder(64);
        sb.append("Link").append(idx).append(": ").append(type).append(" (").append(cx).append(',').append(sy).append(',').append(cz).append(')').append(" id=").append(id).append(' ').append(face).append(" area=").append(area).append(" active=").append(active ? "Y" : "N");
        if (sentinel != null) sb.append(" sentinel=").append(sentinel ? "Y" : "N");
        return sb.toString();
    }

    private static boolean resolveSection(WorldRadiationData data, long sectionKey, Sec out) {
        int sy = Library.getSectionY(sectionKey);
        if ((sy & ~15) != 0) {
            out.clear();
            return false;
        }

        long ck = Library.sectionToChunkLong(sectionKey);
        ChunkRef cr = data.chunkRefs.get(ck);
        if (cr == null || cr.mcChunk == null) {
            out.clear();
            return false;
        }

        int kind = cr.getKind(sy);
        if (kind == ChunkRef.KIND_NONE) {
            out.clear();
            return false;
        }

        out.cr = cr;
        out.sy = sy;
        out.kind = kind;

        if (kind == ChunkRef.KIND_UNI) {
            out.sc = null;
            out.multi = null;
            out.single = null;
            out.pocketData = null;
            out.pocketCount = 1;
            return true;
        }

        SectionRef sc = cr.sec[sy];
        if (sc == null || (sc.pocketCount & 0xFF) <= 0) {
            out.clear();
            return false;
        }

        out.sc = sc;

        if (kind == ChunkRef.KIND_SINGLE) {
            if (!(sc instanceof SingleMaskedSectionRef s)) {
                out.clear();
                return false;
            }
            out.single = s;
            out.multi = null;
            out.pocketData = s.pocketData;
            out.pocketCount = 1;
            return out.pocketData != null;
        }

        if (kind == ChunkRef.KIND_MULTI) {
            if (!(sc instanceof MultiSectionRef m)) {
                out.clear();
                return false;
            }
            out.multi = m;
            out.single = null;
            out.pocketData = m.pocketData;
            int pc = m.pocketCount & 0xFF;
            out.pocketCount = Math.min(pc, NO_POCKET);
            return out.pocketData != null && out.pocketCount > 0;
        }

        out.clear();
        return false;
    }

    private static double readRad(Sec sec, int pocketIndex) {
        int sy = sec.sy;
        if (sec.kind == ChunkRef.KIND_UNI) return sec.cr.uniformRads[sy];
        if (sec.kind == ChunkRef.KIND_SINGLE) return sec.single.rad;
        if (sec.kind == ChunkRef.KIND_MULTI) {
            if ((pocketIndex & ~15) != 0 || pocketIndex >= sec.pocketCount) return 0.0d;
            return sec.multi.data[pocketIndex << 1];
        }
        return 0.0d;
    }

    private static boolean readActive(Sec sec, int pocketIndex) {
        if (sec.cr == null) return false;
        int sy = sec.sy;
        if ((pocketIndex & ~15) != 0) return false;
        return !sec.cr.isInactive(sy, pocketIndex);
    }

    private static int pocketIndexAt(Sec sec, int blockIndex) {
        int kind = sec.kind;
        if (kind == ChunkRef.KIND_UNI) return 0;

        byte[] data = sec.pocketData;
        if (data == null) return -1;

        int nibble = readNibble(data, blockIndex);
        if (nibble == NO_POCKET) return -1;

        if (kind == ChunkRef.KIND_SINGLE) return (nibble == 0) ? 0 : -1;

        // MULTI
        return (nibble >= 0 && nibble < sec.pocketCount) ? nibble : -1;
    }

    private static int paletteIndexForSection(Sec sec, int blockIndex) {
        return pocketIndexAt(sec, blockIndex);
    }

    private static int readConn(MultiSectionRef ms, int stride, int pocketIndex, int face, int neighborPocket) {
        if (ms == null) return 0;
        if ((face & ~5) != 0) return 0;
        if ((neighborPocket & ~15) != 0) return 0;

        int base = pocketIndex * stride + face * NEI_SLOTS + NEI_SHIFT + neighborPocket;
        char[] conn = ms.connectionArea;
        if (base < 0 || base >= conn.length) return 0;
        return conn[base];
    }

    private static int readSingleConn(SingleMaskedSectionRef s, int face) {
        if (s == null) return 0;
        if ((face & ~5) != 0) return 0;
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
            case ChunkRef.KIND_UNI -> COLOR_UNI;
            case ChunkRef.KIND_SINGLE -> COLOR_SINGLE;
            case ChunkRef.KIND_MULTI -> COLOR_MULTI;
            default -> COLOR_RESIST;
        };
    }

    private static void addBoxLines(BufferBuilder buf, double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float a) {
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

    private static void addFaceQuad(BufferBuilder buf, int x0, int y0, int z0, int face, float r, float g, float b, float a, float inset) {
        double x1 = x0 + 16;
        double y1 = y0 + 16;
        double z1 = z0 + 16;

        switch (face) {
            case 0 -> {
                double y = (double) y0 + inset;
                buf.pos(x0, y, z0).color(r, g, b, a).endVertex();
                buf.pos(x1, y, z0).color(r, g, b, a).endVertex();
                buf.pos(x1, y, z1).color(r, g, b, a).endVertex();
                buf.pos(x0, y, z1).color(r, g, b, a).endVertex();
            }
            case 1 -> {
                double y = y1 - inset;
                buf.pos(x0, y, z1).color(r, g, b, a).endVertex();
                buf.pos(x1, y, z1).color(r, g, b, a).endVertex();
                buf.pos(x1, y, z0).color(r, g, b, a).endVertex();
                buf.pos(x0, y, z0).color(r, g, b, a).endVertex();
            }
            case 2 -> {
                double z = (double) z0 + inset;
                buf.pos(x0, y0, z).color(r, g, b, a).endVertex();
                buf.pos(x1, y0, z).color(r, g, b, a).endVertex();
                buf.pos(x1, y1, z).color(r, g, b, a).endVertex();
                buf.pos(x0, y1, z).color(r, g, b, a).endVertex();
            }
            case 3 -> {
                double z = z1 - inset;
                buf.pos(x0, y1, z).color(r, g, b, a).endVertex();
                buf.pos(x1, y1, z).color(r, g, b, a).endVertex();
                buf.pos(x1, y0, z).color(r, g, b, a).endVertex();
                buf.pos(x0, y0, z).color(r, g, b, a).endVertex();
            }
            case 4 -> {
                double x = (double) x0 + inset;
                buf.pos(x, y0, z1).color(r, g, b, a).endVertex();
                buf.pos(x, y0, z0).color(r, g, b, a).endVertex();
                buf.pos(x, y1, z0).color(r, g, b, a).endVertex();
                buf.pos(x, y1, z1).color(r, g, b, a).endVertex();
            }
            case 5 -> {
                double x = x1 - inset;
                buf.pos(x, y0, z0).color(r, g, b, a).endVertex();
                buf.pos(x, y0, z1).color(r, g, b, a).endVertex();
                buf.pos(x, y1, z1).color(r, g, b, a).endVertex();
                buf.pos(x, y1, z0).color(r, g, b, a).endVertex();
            }
        }
    }

    private static void addCross(BufferBuilder buf, double x, double y, double z, int face, double size, float r, float g, float b, float a) {
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

    private static void addLine(BufferBuilder buf, double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float a) {
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

    private static void renderLabelLines(FontRenderer fr, RenderManager rm, List<String> lines, double x, double y, double z, int color, float scale) {
        if (lines.isEmpty()) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(rm.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);

        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();

        int lineHeight = fr.FONT_HEIGHT + 1;
        int yBase = -((lines.size() - 1) * lineHeight) / 2;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int width = fr.getStringWidth(line);
            fr.drawString(line, -width / 2, yBase + i * lineHeight, color, false);
        }

        GlStateManager.popMatrix();
    }

    private static String formatRad(double v) {
        if (Double.isNaN(v)) return "nan";
        if (v == Double.POSITIVE_INFINITY) return "inf";
        if (v == Double.NEGATIVE_INFINITY) return "-inf";
        return String.format(Locale.ROOT, "%.3e", v);
    }

    public enum Mode {WIRE, SLICE, FACES, STATE, ERRORS}

    public static final class Config {
        public boolean enabled = false;
        public int radiusChunks = 2;
        public Mode mode = Mode.WIRE;
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

            return new FocusFilter(anchor.getX() - dx, anchor.getX() + dx, anchor.getY() - dy, anchor.getY() + dy, anchor.getZ() - dz, anchor.getZ() + dz);
        }

        boolean isSectionOutsideFilter(int baseX, int baseY, int baseZ) {
            int maxX = baseX + 15;
            int maxY = baseY + 15;
            int maxZ = baseZ + 15;
            return this.maxX < baseX || minX > maxX || this.maxY < baseY || minY > maxY || this.maxZ < baseZ || minZ > maxZ;
        }
    }

    private static final class Sec {
        ChunkRef cr;
        int sy;
        int kind;

        SectionRef sc;
        MultiSectionRef multi;
        SingleMaskedSectionRef single;

        byte[] pocketData;
        int pocketCount;

        void clear() {
            cr = null;
            sc = null;
            multi = null;
            single = null;
            pocketData = null;
            pocketCount = 0;
            sy = 0;
            kind = ChunkRef.KIND_NONE;
        }
    }
}
