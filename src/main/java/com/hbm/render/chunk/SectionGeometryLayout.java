package com.hbm.render.chunk;

import com.hbm.lib.Library;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;

@SideOnly(Side.CLIENT)
final class SectionGeometryLayout {
    private static final BlockRenderLayer[] LAYERS = BlockRenderLayer.values();
    private static final int STRIDE = 7;
    final long[] sections;
    private final int[] wholeStarts;
    private final int[] wholeQuads;
    private final int[] cutStarts;
    private final int[] cutQuads;
    private final float[] vertices;

    SectionGeometryLayout(float[] mesh, int placement) {
        int px = placement & 15, py = placement >> 4 & 15, pz = placement >> 8 & 15;
        var parts = new Long2ObjectOpenHashMap<Builder>();
        var clip = new Clip();
        for (int q = 0; q < mesh.length / 20; q++) {
            int base = q * 20;
            float minX = Float.POSITIVE_INFINITY, minY = minX, minZ = minX;
            float maxX = Float.NEGATIVE_INFINITY, maxY = maxX, maxZ = maxX;
            for (int v = 0; v < 4; v++) {
                int at = base + v * 5;
                minX = Math.min(minX, mesh[at]); maxX = Math.max(maxX, mesh[at]);
                minY = Math.min(minY, mesh[at + 1]); maxY = Math.max(maxY, mesh[at + 1]);
                minZ = Math.min(minZ, mesh[at + 2]); maxZ = Math.max(maxZ, mesh[at + 2]);
            }
            int x0 = section(minX, px), x1 = section(maxX, px);
            int y0 = section(minY, py), y1 = section(maxY, py);
            int z0 = section(minZ, pz), z1 = section(maxZ, pz);
            if (x0 == x1 && y0 == y1 && z0 == z1) {
                parts.computeIfAbsent(Library.sectionToLong(x0, y0, z0), key -> new Builder()).whole.add(q);
                continue;
            }
            for (int z = z0; z <= z1; z++) {
                for (int y = y0; y <= y1; y++) {
                    for (int x = x0; x <= x1; x++) {
                        clip.bounds(x * 16 - px, y * 16 - py, z * 16 - pz);
                        clip.output.clear();
                        clip.triangle(mesh, base, 0, 1, 2);
                        clip.triangle(mesh, base, 2, 3, 0);
                        if (clip.output.isEmpty()) continue;
                        Builder part = parts.computeIfAbsent(Library.sectionToLong(x, y, z), key -> new Builder());
                        for (int i = 0; i < clip.output.size() / (4 * STRIDE); i++) part.cut.add(q);
                        part.vertices.addAll(clip.output);
                    }
                }
            }
        }
        sections = parts.keySet().toLongArray();
        Arrays.sort(sections);
        wholeStarts = new int[sections.length + 1];
        cutStarts = new int[sections.length + 1];
        var whole = new IntArrayList();
        var cut = new IntArrayList();
        var points = new FloatArrayList();
        for (int i = 0; i < sections.length; i++) {
            Builder part = parts.get(sections[i]);
            whole.addAll(part.whole);
            cut.addAll(part.cut);
            points.addAll(part.vertices);
            wholeStarts[i + 1] = whole.size();
            cutStarts[i + 1] = cut.size();
        }
        wholeQuads = whole.toIntArray();
        cutQuads = cut.toIntArray();
        vertices = points.toFloatArray();
    }

    private static int section(float coordinate, int placement) {
        // Double addition preserves which side of an integer boundary a baked float occupies.
        return (int) Math.floor(((double) coordinate + placement) / 16);
    }

    long bytes() {
        return 8L * sections.length + 4L * (wholeStarts.length + wholeQuads.length + cutStarts.length
                + cutQuads.length + vertices.length);
    }

    void emit(int part, IBlockState owner, SectionGeometry.Mesh mesh, int x, int y, int z, SurfaceLight light,
              SectionGeometry.Sink sink, float[] quad, int[] colors, int[] lights) {
        float[] points = mesh.vertices(), normals = mesh.normals();
        int[] meshColors = mesh.colors();
        byte[] layers = mesh.layers();
        TextureAtlasSprite[] sprites = mesh.sprites();
        for (int i = wholeStarts[part]; i < wholeStarts[part + 1]; i++) {
            int q = wholeQuads[i];
            float nx = normals[q * 3], ny = normals[q * 3 + 1], nz = normals[q * 3 + 2];
            for (int v = 0; v < 4; v++) {
                int at = q * 20 + v * 5, out = v * 5;
                quad[out] = points[at] + x;
                quad[out + 1] = points[at + 1] + y;
                quad[out + 2] = points[at + 2] + z;
                quad[out + 3] = points[at + 3];
                quad[out + 4] = points[at + 4];
                colors[v] = meshColors[q * 4 + v];
                lights[v] = light.light(quad[out], quad[out + 1], quad[out + 2], nx, ny, nz);
            }
            sink.quad(LAYERS[layers[q]], owner, sprites[q], quad, colors, lights);
        }
        for (int i = cutStarts[part]; i < cutStarts[part + 1]; i++) {
            int q = cutQuads[i];
            float nx = normals[q * 3], ny = normals[q * 3 + 1], nz = normals[q * 3 + 2];
            for (int v = 0; v < 4; v++) {
                int at = (i * 4 + v) * STRIDE, out = v * 5;
                quad[out] = vertices[at] + x;
                quad[out + 1] = vertices[at + 1] + y;
                quad[out + 2] = vertices[at + 2] + z;
                quad[out + 3] = interpolate(points, q * 20 + 3, 5, at + 3);
                quad[out + 4] = interpolate(points, q * 20 + 4, 5, at + 3);
                colors[v] = channel(meshColors, q * 4, at + 3, 0) | channel(meshColors, q * 4, at + 3, 8) << 8
                        | channel(meshColors, q * 4, at + 3, 16) << 16 | channel(meshColors, q * 4, at + 3, 24) << 24;
                lights[v] = light.light(quad[out], quad[out + 1], quad[out + 2], nx, ny, nz);
            }
            sink.quad(LAYERS[layers[q]], owner, sprites[q], quad, colors, lights);
        }
    }

    private float interpolate(float[] data, int source, int stride, int weights) {
        float value = 0;
        for (int i = 0; i < 4; i++) value += data[source + i * stride] * vertices[weights + i];
        return value;
    }

    private int channel(int[] values, int source, int weights, int shift) {
        float value = 0;
        for (int i = 0; i < 4; i++) value += (values[source + i] >>> shift & 255) * vertices[weights + i];
        return Math.round(value);
    }

    private static final class Builder {
        final IntArrayList whole = new IntArrayList();
        final IntArrayList cut = new IntArrayList();
        final FloatArrayList vertices = new FloatArrayList();
    }

    private static final class Clip {
        final float[] a = new float[12 * STRIDE];
        final float[] b = new float[12 * STRIDE];
        final float[] bounds = new float[6];
        final FloatArrayList output = new FloatArrayList();

        void bounds(int x, int y, int z) {
            bounds[0] = x; bounds[1] = y; bounds[2] = z;
            bounds[3] = x + 16; bounds[4] = y + 16; bounds[5] = z + 16;
        }

        void triangle(float[] mesh, int base, int p, int q, int r) {
            vertex(mesh, base, p, 0);
            vertex(mesh, base, q, STRIDE);
            vertex(mesh, base, r, STRIDE * 2);
            for (int axis = 0; axis < 3; axis++) {
                if (a[axis] == bounds[axis + 3] && a[STRIDE + axis] == bounds[axis + 3]
                        && a[2 * STRIDE + axis] == bounds[axis + 3]) return;
            }
            int count = 3;
            float[] in = a, out = b;
            for (int plane = 0; plane < 6 && count != 0; plane++) {
                int axis = plane % 3, produced = 0, previous = (count - 1) * STRIDE;
                float edge = bounds[plane];
                float prevDistance = plane < 3 ? in[previous + axis] - edge : edge - in[previous + axis];
                for (int i = 0; i < count; i++) {
                    int current = i * STRIDE;
                    float distance = plane < 3 ? in[current + axis] - edge : edge - in[current + axis];
                    if ((distance >= 0) != (prevDistance >= 0)) {
                        float t = prevDistance / (prevDistance - distance);
                        int dst = produced++ * STRIDE;
                        for (int component = 0; component < STRIDE; component++) {
                            out[dst + component] = in[previous + component]
                                    + t * (in[current + component] - in[previous + component]);
                        }
                        out[dst + axis] = edge;
                    }
                    if (distance >= 0) System.arraycopy(in, current, out, produced++ * STRIDE, STRIDE);
                    previous = current;
                    prevDistance = distance;
                }
                count = produced;
                float[] swap = in; in = out; out = swap;
            }
            for (int i = 1; i + 1 < count; i++) {
                int first = i * STRIDE, second = (i + 1) * STRIDE;
                float ax = in[first] - in[0], ay = in[first + 1] - in[1], az = in[first + 2] - in[2];
                float bx = in[second] - in[0], by = in[second + 1] - in[1], bz = in[second + 2] - in[2];
                float nx = ay * bz - az * by, ny = az * bx - ax * bz, nz = ax * by - ay * bx;
                if (nx * nx + ny * ny + nz * nz == 0) continue;
                output.addElements(output.size(), in, 0, STRIDE);
                output.addElements(output.size(), in, first, STRIDE);
                output.addElements(output.size(), in, second, STRIDE);
                output.addElements(output.size(), in, second, STRIDE);
            }
        }

        private void vertex(float[] mesh, int base, int v, int out) {
            System.arraycopy(mesh, base + v * 5, a, out, 3);
            for (int i = 0; i < 4; i++) a[out + 3 + i] = i == v ? 1 : 0;
        }
    }
}
