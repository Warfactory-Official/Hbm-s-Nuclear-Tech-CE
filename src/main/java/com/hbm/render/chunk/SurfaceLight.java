package com.hbm.render.chunk;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;

@SideOnly(Side.CLIENT)
final class SurfaceLight {
    private static final int SPAN = 19;
    private static final int SOLID = 1 << 8;
    private static final float EPSILON = 1e-5F;
    private static final int[][] FACES = {{1, 4, 10, 13}, {0, 3, 9, 12}, {9, 12, 10, 13}, {0, 3, 1, 4}, {3, 12, 4, 13}, {0, 9, 1, 10}};
    private static final ThreadLocal<SurfaceLight> LOCAL = ThreadLocal.withInitial(SurfaceLight::new);
    private final int[] cells = new int[SPAN * SPAN * SPAN];
    private final int[] stamps = new int[cells.length];
    private final int[] around = new int[27];
    private final float[] corners = new float[16];
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    private IBlockAccess view;
    private int epoch, originX, originY, originZ;
    private float block, sky;

    private SurfaceLight() {
    }

    static SurfaceLight open(IBlockAccess view, int originX, int originY, int originZ) {
        SurfaceLight light = LOCAL.get();
        light.view = view;
        light.originX = originX;
        light.originY = originY;
        light.originZ = originZ;
        if (++light.epoch == 0) {
            Arrays.fill(light.stamps, 0);
            light.epoch = 1;
        }
        return light;
    }

    void close() {
        view = null;
    }

    int light(float x, float y, float z, float nx, float ny, float nz) {
        int bx = MathHelper.floor(x), by = MathHelper.floor(y), bz = MathHelper.floor(z);
        boolean open = false;
        for (int dy = -1, i = 0; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dx = -1; dx <= 1; dx++, i++) {
                    int cell = cell(bx + dx, by + dy, bz + dz);
                    around[i] = cell;
                    open |= (cell & SOLID) == 0;
                }
            }
        }
        float length = nx * nx + ny * ny + nz * nz;
        if (!open) {
            block = sky = 0;
        } else if (length < EPSILON * EPSILON) {
            block = around[13] & 15;
            sky = around[13] >> 4 & 15;
        } else {
            float scale = 1 / (float) Math.sqrt(length);
            float fx = x - bx, fy = y - by, fz = z - bz;
            float totalBlock = 0, totalSky = 0;
            for (int axis = 0; axis < 3; axis++) {
                float n = (axis == 0 ? nx : axis == 1 ? ny : nz) * scale;
                if (n <= EPSILON && n >= -EPSILON) continue;
                direction(fx, fy, fz, FACES[axis * 2 + (n > 0 ? 0 : 1)]);
                totalBlock += block * n * n;
                totalSky += sky * n * n;
            }
            block = totalBlock;
            sky = totalSky;
        }
        return Math.min(Math.round(block * 16), 240) | Math.min(Math.round(sky * 16), 240) << 16;
    }

    private void direction(float fx, float fy, float fz, int[] face) {
        for (int k = 0; k < 8; k++) {
            int corner = (k & 1) + (k >> 1 & 1) * 3 + (k >> 2) * 9;
            int a = around[face[0] + corner], b = around[face[1] + corner];
            int c = around[face[2] + corner], d = around[face[3] + corner];
            int valid = 4 - ((a & SOLID) + (b & SOLID) + (c & SOLID) + (d & SOLID) >> 8);
            float normalizer = valid == 0 ? 0 : 1F / valid;
            corners[k] = ((a & 15) + (b & 15) + (c & 15) + (d & 15)) * normalizer;
            corners[k + 8] = ((a >> 4 & 15) + (b >> 4 & 15) + (c >> 4 & 15) + (d >> 4 & 15)) * normalizer;
        }
        block = trilinear(0, fx, fy, fz);
        sky = trilinear(8, fx, fy, fz);
    }

    private float trilinear(int at, float fx, float fy, float fz) {
        float c00 = lerp(fx, corners[at], corners[at + 1]), c01 = lerp(fx, corners[at + 2], corners[at + 3]);
        float c10 = lerp(fx, corners[at + 4], corners[at + 5]), c11 = lerp(fx, corners[at + 6], corners[at + 7]);
        return lerp(fy, lerp(fz, c00, c01), lerp(fz, c10, c11));
    }

    private static float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }

    private int cell(int x, int y, int z) {
        int index = x + 1 + (z + 1) * SPAN + (y + 1) * SPAN * SPAN;
        if (stamps[index] == epoch) return cells[index];
        cursor.setPos(originX + x, originY + y, originZ + z);
        int combined = view.getCombinedLight(cursor, 0);
        int cell = combined >> 4 & 15 | (combined >> 20 & 15) << 4 | (view.getBlockState(cursor).isOpaqueCube() ? SOLID : 0);
        stamps[index] = epoch;
        return cells[index] = cell;
    }
}
