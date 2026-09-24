package com.hbm.render.chunk;

import com.hbm.lib.Library;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Bootstrap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SectionGeometryLayoutTest {

    private static final float EPSILON = 1e-3F;
    private static final IBlockState AIR = (IBlockState) Proxy.newProxyInstance(IBlockState.class.getClassLoader(),
            new Class<?>[]{IBlockState.class}, (proxy, method, args) -> method.getReturnType() == boolean.class ? false : null);

    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
    }

    private static IBlockAccess uniform(int sky, int block) {
        return new IBlockAccess() {
            @Override
            public TileEntity getTileEntity(BlockPos pos) {
                return null;
            }

            @Override
            public int getCombinedLight(BlockPos pos, int lightValue) {
                return sky << 20 | block << 4;
            }

            @Override
            public IBlockState getBlockState(BlockPos pos) {
                return AIR;
            }

            @Override
            public boolean isAirBlock(BlockPos pos) {
                return true;
            }

            @Override
            public Biome getBiome(BlockPos pos) {
                return null;
            }

            @Override
            public int getStrongPower(BlockPos pos, EnumFacing direction) {
                return 0;
            }

            @Override
            public WorldType getWorldType() {
                return WorldType.DEFAULT;
            }

            @Override
            public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
                return false;
            }
        };
    }

    private static SectionGeometry.Mesh mesh(float[][] quads) {
        float[] vertices = new float[quads.length * 20];
        int[] colors = new int[quads.length * 4];
        float[] normals = new float[quads.length * 3];
        byte[] layers = new byte[quads.length];
        for (int q = 0; q < quads.length; q++) {
            for (int v = 0; v < 4; v++) {
                float x = quads[q][v * 3], y = quads[q][v * 3 + 1], z = quads[q][v * 3 + 2];
                int at = (q * 4 + v) * 5;
                vertices[at] = x;
                vertices[at + 1] = y;
                vertices[at + 2] = z;
                vertices[at + 3] = x + z;
                vertices[at + 4] = y;
                colors[q * 4 + v] = 0xFF000000 | Math.round(x + 64);
            }
            normals[q * 3 + 1] = 1;
        }
        return new SectionGeometry.Mesh(vertices, colors, normals, layers, new TextureAtlasSprite[quads.length]);
    }

    private static double area(float[] quad) {
        return triangle(quad, 0, 1, 2) + triangle(quad, 2, 3, 0);
    }

    private static double triangle(float[] quad, int a, int b, int c) {
        double ax = quad[b * 5] - quad[a * 5], ay = quad[b * 5 + 1] - quad[a * 5 + 1], az = quad[b * 5 + 2] - quad[a * 5 + 2];
        double bx = quad[c * 5] - quad[a * 5], by = quad[c * 5 + 1] - quad[a * 5 + 1], bz = quad[c * 5 + 2] - quad[a * 5 + 2];
        double nx = ay * bz - az * by, ny = az * bx - ax * bz, nz = ax * by - ay * bx;
        return Math.sqrt(nx * nx + ny * ny + nz * nz) / 2;
    }

    private record Emitted(int section, float[] quad, int[] colors, int[] lights) {}

    private static List<Emitted> emitAll(SectionGeometryLayout layout, SectionGeometry.Mesh mesh, int placement,
                                         IBlockAccess view) {
        List<Emitted> out = new ArrayList<>();
        int px = placement & 15, py = placement >> 4 & 15, pz = placement >> 8 & 15;
        for (int part = 0; part < layout.sections.length; part++) {
            long section = layout.sections[part];
            int x = px - 16 * Library.getSectionX(section);
            int y = py - 16 * Library.getSectionY(section);
            int z = pz - 16 * Library.getSectionZ(section);
            int index = part;
            SurfaceLight light = SurfaceLight.open(view, 0, 0, 0);
            layout.emit(part, AIR, mesh, x, y, z, light, (layer, owner, sprite, quad, colors, lights) -> {
                assertEquals(BlockRenderLayer.SOLID, layer);
                out.add(new Emitted(index, quad.clone(), colors.clone(), lights.clone()));
            }, new float[20], new int[4], new int[4]);
            light.close();
        }
        return out;
    }

    @Test
    void cutGeometryStaysInsideItsSectionAndConservesArea() {
        Random random = new Random(34);
        float[][] quads = new float[64][];
        double expected = 0;
        for (int q = 0; q < quads.length; q++) {
            float x = random.nextFloat() * 40 - 20, y = random.nextFloat() * 40 - 8, z = random.nextFloat() * 40 - 20;
            float sx = random.nextFloat() * 30, sz = random.nextFloat() * 30, tilt = random.nextFloat() * 20 - 10;
            quads[q] = new float[]{x, y, z, x, y + tilt, z + sz, x + sx, y + tilt, z + sz, x + sx, y, z};
            float[] flat = new float[20];
            for (int v = 0; v < 4; v++) System.arraycopy(quads[q], v * 3, flat, v * 5, 3);
            expected += area(flat);
        }
        SectionGeometry.Mesh mesh = mesh(quads);
        for (int placement : new int[]{0, 15 | 15 << 4 | 15 << 8, 7 | 3 << 4 | 11 << 8}) {
            SectionGeometryLayout layout = new SectionGeometryLayout(mesh.vertices(), placement);
            double actual = 0;
            for (Emitted emitted : emitAll(layout, mesh, placement, uniform(15, 0))) {
                for (int v = 0; v < 4; v++) {
                    for (int axis = 0; axis < 3; axis++) {
                        float value = emitted.quad[v * 5 + axis];
                        assertTrue(value >= -EPSILON && value <= 16 + EPSILON, "vertex outside [0,16]: " + value);
                    }
                }
                actual += area(emitted.quad);
            }
            assertEquals(expected, actual, expected * 1e-4);
        }
    }

    @Test
    void cutAttributesInterpolateLinearly() {
        SectionGeometry.Mesh mesh = mesh(new float[][]{{-20, 3, -9, -20, 3, 25, 30, 3, 25, 30, 3, -9}});
        int placement = 5 | 2 << 4 | 13 << 8;
        SectionGeometryLayout layout = new SectionGeometryLayout(mesh.vertices(), placement);
        List<Emitted> emitted = emitAll(layout, mesh, placement, uniform(15, 0));
        assertTrue(layout.sections.length > 1);
        for (Emitted part : emitted) {
            long section = layout.sections[part.section];
            int originX = 16 * Library.getSectionX(section) - 5;
            int originZ = 16 * Library.getSectionZ(section) - 13;
            for (int v = 0; v < 4; v++) {
                float meshX = part.quad[v * 5] + originX, meshZ = part.quad[v * 5 + 2] + originZ;
                assertEquals(meshX + meshZ, part.quad[v * 5 + 3], 1e-3F);
                assertEquals(Math.round(meshX + 64), part.colors[v] & 255, 1);
                assertEquals(0xFF, part.colors[v] >>> 24);
            }
        }
    }

    @Test
    void uniformLightSamplesUniformly() {
        SectionGeometry.Mesh mesh = mesh(new float[][]{{-3, 1, -3, -3, 17, 20, 18, 5, 20, 18, 1, -3}});
        SectionGeometryLayout layout = new SectionGeometryLayout(mesh.vertices(), 0);
        for (Emitted emitted : emitAll(layout, mesh, 0, uniform(13, 6))) {
            for (int light : emitted.lights) assertEquals(6 * 16 | 13 * 16 << 16, light);
        }
    }
}
