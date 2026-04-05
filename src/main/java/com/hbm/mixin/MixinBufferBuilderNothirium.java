package com.hbm.mixin;

import com.hbm.lib.internal.UnsafeHolder;
import com.hbm.render.util.NTMBufferBuilder;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static com.hbm.lib.internal.UnsafeHolder.U;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.*;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilderNothirium implements NTMBufferBuilder {

    @Shadow
    private ByteBuffer byteBuffer;
    @Shadow
    private IntBuffer rawIntBuffer;
    @Shadow
    private int vertexCount;
    @Shadow
    private VertexFormatElement vertexFormatElement;
    @Shadow
    private int vertexFormatIndex;
    @Shadow
    private double xOffset;
    @Shadow
    private double yOffset;
    @Shadow
    private double zOffset;
    @Shadow
    private VertexFormat vertexFormat;
    @Shadow
    private boolean isDrawing;

    @Shadow
    public abstract void begin(int glMode, VertexFormat format);

    @Shadow
    protected abstract void growBuffer(int increaseAmount);

    @Unique
    private long hbm$byteBufferAddress;

    @Inject(method = "<init>", at = @At("RETURN"), require = 1)
    private void hbm$init(int bufferSizeIn, CallbackInfo ci) {
        hbm$refreshByteBufferAddress();
    }

    @Inject(method = "growBuffer", at = @At("RETURN"), require = 1)
    private void hbm$afterGrowBuffer(int increaseAmount, CallbackInfo ci) {
        hbm$refreshByteBufferAddress();
    }

    @Unique
    private void hbm$refreshByteBufferAddress() {
        hbm$byteBufferAddress = NTMBufferBuilder.address(byteBuffer);
    }

    @Unique
    private long hbm$intAddress(int intIndex) {
        return hbm$byteBufferAddress + ((long) intIndex << 2);
    }

    @Unique
    private long hbm$elementAddress() {
        return hbm$byteBufferAddress + (long) vertexCount * vertexFormat.getSize()
                + vertexFormat.getOffset(vertexFormatIndex);
    }

    @Unique
    private int hbm$bufferSizeInts() {
        return vertexCount * vertexFormat.getIntegerSize();
    }

    @Unique
    private int hbm$getColorIndex(int vertexIndex) {
        return ((vertexCount - vertexIndex) * vertexFormat.getSize() + vertexFormat.getColorOffset()) >> 2;
    }

    @Unique
    private void hbm$nextVertexFormatIndex() {
        ++vertexFormatIndex;
        vertexFormatIndex %= vertexFormat.getElementCount();
        vertexFormatElement = vertexFormat.getElement(vertexFormatIndex);

        if (vertexFormatElement.getUsage() == VertexFormatElement.EnumUsage.PADDING) {
            hbm$nextVertexFormatIndex();
        }
    }

    @Override
    public void beginFast(int drawMode, VertexFormat format, int expectedVertices) {
        begin(drawMode, format);
        rawIntBuffer.clear();
        reserveVertices(expectedVertices);
    }

    @Unique
    private void reserveVertices(int expectedVertices) {
        if (expectedVertices > 0) {
            reserveAdditionalBytes(expectedVertices * vertexFormat.getSize());
        }
    }

    @Unique
    private void reserveAdditionalBytes(int additionalBytes) {
        if (additionalBytes > 0) {
            growBuffer(additionalBytes);
        }
    }

    @Override
    public void appendRawVertexData(int[] data, int intsPerVertex, VertexFormat requiredFormat) {
        if (data == null || data.length == 0) return;
        if (intsPerVertex <= 0 || data.length % intsPerVertex != 0) {
            throw new IllegalArgumentException("Invalid raw vertex payload length " + data.length + " for stride " + intsPerVertex);
        }

        ensureDrawing(requiredFormat);
        if (!hasRemainingInts(data.length)) {
            growBuffer(data.length * Integer.BYTES);
        }

        int dstIntIndex = hbm$bufferSizeInts();
        U.copyMemory(data, UnsafeHolder.IA_BASE, null, hbm$intAddress(dstIntIndex), (long) data.length << 2);
        rawIntBuffer.position(dstIntIndex + data.length);
        vertexCount += data.length / intsPerVertex;
    }

    @Override
    public void reservePositionColorQuads(int quadCount) {
        ensureDrawing(POSITION_COLOR);
        if (quadCount > 0) {
            growBuffer(quadCount * POSITION_COLOR_QUAD_INTS * Integer.BYTES);
        }
    }

    @Override
    public void appendPosition(double x, double y, double z) {
        ensureDrawing(POSITION);
        if (!hasRemainingInts(3)) {
            growBuffer(3 * Integer.BYTES);
        }
        appendPositionUnchecked(x, y, z);
    }

    @Override
    public void appendPositionColor(double x, double y, double z, int packedColor) {
        ensureDrawing(POSITION_COLOR);
        if (!hasRemainingInts(POSITION_COLOR_INTS_PER_VERTEX)) {
            growBuffer(POSITION_COLOR_INTS_PER_VERTEX * Integer.BYTES);
        }
        appendPositionColorUnchecked(x, y, z, packedColor);
    }

    @Override
    public void appendPositionColorQuad(double x0, double y0, double z0,
                                        double x1, double y1, double z1,
                                        double x2, double y2, double z2,
                                        double x3, double y3, double z3,
                                        int packedColor) {
        ensureDrawing(POSITION_COLOR);
        if (!hasRemainingInts(POSITION_COLOR_QUAD_INTS)) {
            growBuffer(POSITION_COLOR_QUAD_INTS * Integer.BYTES);
        }
        appendPositionColorQuadUnchecked(x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, packedColor);
    }

    @Override
    public void appendPositionTex(double x, double y, double z, double u, double v) {
        ensureDrawing(POSITION_TEX);
        if (!hasRemainingInts(5)) {
            growBuffer(5 * Integer.BYTES);
        }
        appendPositionTexUnchecked(x, y, z, u, v);
    }

    @Override
    public void appendPositionTexColor(double x, double y, double z, double u, double v, int packedColor) {
        ensureDrawing(POSITION_TEX_COLOR);
        if (!hasRemainingInts(6)) {
            growBuffer(6 * Integer.BYTES);
        }
        appendPositionTexColorUnchecked(x, y, z, u, v, packedColor);
    }

    @Override
    public void appendPositionTexNormal(double x, double y, double z, double u, double v, int packedNormal) {
        ensureDrawing(POSITION_TEX_NORMAL);
        if (!hasRemainingInts(6)) {
            growBuffer(6 * Integer.BYTES);
        }
        appendPositionTexNormalUnchecked(x, y, z, u, v, packedNormal);
    }

    @Override
    public void appendPositionTexLmapColor(double x, double y, double z, double u, double v, int packedLightmap,
                                           int packedColor) {
        ensureDrawing(POSITION_TEX_LMAP_COLOR);
        if (!hasRemainingInts(7)) {
            growBuffer(7 * Integer.BYTES);
        }
        appendPositionTexLmapColorUnchecked(x, y, z, u, v, packedLightmap, packedColor);
    }

    @Override
    public void appendPositionTexColorNormal(double x, double y, double z, double u, double v, int packedColor,
                                             int packedNormal) {
        ensureDrawing(POSITION_TEX_COLOR_NORMAL);
        if (!hasRemainingInts(7)) {
            growBuffer(7 * Integer.BYTES);
        }
        appendPositionTexColorNormalUnchecked(x, y, z, u, v, packedColor, packedNormal);
    }

    @Override
    public void appendPositionNormal(double x, double y, double z, int packedNormal) {
        ensureDrawing(POSITION_NORMAL);
        if (!hasRemainingInts(4)) {
            growBuffer(4 * Integer.BYTES);
        }
        appendPositionNormalUnchecked(x, y, z, packedNormal);
    }

    @Override
    public void appendParticlePositionTexColorLmap(double x, double y, double z, double u, double v, int packedColor,
                                                   int packedLightmap) {
        ensureDrawing(PARTICLE_POSITION_TEX_COLOR_LMAP);
        if (!hasRemainingInts(7)) {
            growBuffer(7 * Integer.BYTES);
        }
        appendParticlePositionTexColorLmapUnchecked(x, y, z, u, v, packedColor, packedLightmap);
    }

    @Override
    public void appendPositionUnchecked(double x, double y, double z) {
        long address = hbm$intAddress(vertexCount * 3);
        U.putFloat(address, applyXOffset(x));
        U.putFloat(address + 4L, applyYOffset(y));
        U.putFloat(address + 8L, applyZOffset(z));
        ++vertexCount;
    }

    @Override
    public void appendPositionQuadUnchecked(double x0, double y0, double z0,
                                            double x1, double y1, double z1,
                                            double x2, double y2, double z2,
                                            double x3, double y3, double z3) {
        appendPositionUnchecked(x0, y0, z0);
        appendPositionUnchecked(x1, y1, z1);
        appendPositionUnchecked(x2, y2, z2);
        appendPositionUnchecked(x3, y3, z3);
    }

    @Override
    public void appendPositionColorUnchecked(double x, double y, double z, int packedColor) {
        long address = hbm$intAddress(vertexCount * POSITION_COLOR_INTS_PER_VERTEX);
        U.putFloat(address, applyXOffset(x));
        U.putFloat(address + 4L, applyYOffset(y));
        U.putFloat(address + 8L, applyZOffset(z));
        U.putInt(address + 12L, packedColor);
        ++vertexCount;
    }

    @Override
    public void appendPositionColorQuadUnchecked(double x0, double y0, double z0,
                                                 double x1, double y1, double z1,
                                                 double x2, double y2, double z2,
                                                 double x3, double y3, double z3,
                                                 int packedColor) {
        long address = hbm$intAddress(vertexCount * POSITION_COLOR_INTS_PER_VERTEX);

        U.putFloat(address, applyXOffset(x0));
        U.putFloat(address + 4L, applyYOffset(y0));
        U.putFloat(address + 8L, applyZOffset(z0));
        U.putInt(address + 12L, packedColor);

        U.putFloat(address + 16L, applyXOffset(x1));
        U.putFloat(address + 20L, applyYOffset(y1));
        U.putFloat(address + 24L, applyZOffset(z1));
        U.putInt(address + 28L, packedColor);

        U.putFloat(address + 32L, applyXOffset(x2));
        U.putFloat(address + 36L, applyYOffset(y2));
        U.putFloat(address + 40L, applyZOffset(z2));
        U.putInt(address + 44L, packedColor);

        U.putFloat(address + 48L, applyXOffset(x3));
        U.putFloat(address + 52L, applyYOffset(y3));
        U.putFloat(address + 56L, applyZOffset(z3));
        U.putInt(address + 60L, packedColor);

        vertexCount += 4;
    }

    @Override
    public void appendPositionTexUnchecked(double x, double y, double z, double u, double v) {
        long address = hbm$intAddress(vertexCount * 5);
        U.putFloat(address, applyXOffset(x));
        U.putFloat(address + 4L, applyYOffset(y));
        U.putFloat(address + 8L, applyZOffset(z));
        U.putFloat(address + 12L, (float) u);
        U.putFloat(address + 16L, (float) v);
        ++vertexCount;
    }

    @Override
    public void appendPositionTexQuadUnchecked(double x0, double y0, double z0, double u0, double v0,
                                               double x1, double y1, double z1, double u1, double v1,
                                               double x2, double y2, double z2, double u2, double v2,
                                               double x3, double y3, double z3, double u3, double v3) {
        appendPositionTexUnchecked(x0, y0, z0, u0, v0);
        appendPositionTexUnchecked(x1, y1, z1, u1, v1);
        appendPositionTexUnchecked(x2, y2, z2, u2, v2);
        appendPositionTexUnchecked(x3, y3, z3, u3, v3);
    }

    @Override
    public void appendPositionTexColorUnchecked(double x, double y, double z, double u, double v, int packedColor) {
        long address = hbm$intAddress(vertexCount * 6);
        U.putFloat(address, applyXOffset(x));
        U.putFloat(address + 4L, applyYOffset(y));
        U.putFloat(address + 8L, applyZOffset(z));
        U.putFloat(address + 12L, (float) u);
        U.putFloat(address + 16L, (float) v);
        U.putInt(address + 20L, packedColor);
        ++vertexCount;
    }

    @Override
    public void appendPositionTexNormalUnchecked(double x, double y, double z, double u, double v, int packedNormal) {
        long address = hbm$intAddress(vertexCount * 6);
        U.putFloat(address, applyXOffset(x));
        U.putFloat(address + 4L, applyYOffset(y));
        U.putFloat(address + 8L, applyZOffset(z));
        U.putFloat(address + 12L, (float) u);
        U.putFloat(address + 16L, (float) v);
        U.putInt(address + 20L, packedNormal);
        ++vertexCount;
    }

    @Override
    public void appendPositionTexLmapColorUnchecked(double x, double y, double z, double u, double v,
                                                    int packedLightmap, int packedColor) {
        long address = hbm$intAddress(vertexCount * 7);
        U.putFloat(address, applyXOffset(x));
        U.putFloat(address + 4L, applyYOffset(y));
        U.putFloat(address + 8L, applyZOffset(z));
        U.putFloat(address + 12L, (float) u);
        U.putFloat(address + 16L, (float) v);
        U.putInt(address + 20L, packedLightmap);
        U.putInt(address + 24L, packedColor);
        ++vertexCount;
    }

    @Override
    public void appendPositionTexColorNormalUnchecked(double x, double y, double z, double u, double v, int packedColor,
                                                      int packedNormal) {
        long address = hbm$intAddress(vertexCount * 7);
        U.putFloat(address, applyXOffset(x));
        U.putFloat(address + 4L, applyYOffset(y));
        U.putFloat(address + 8L, applyZOffset(z));
        U.putFloat(address + 12L, (float) u);
        U.putFloat(address + 16L, (float) v);
        U.putInt(address + 20L, packedColor);
        U.putInt(address + 24L, packedNormal);
        ++vertexCount;
    }

    @Override
    public void appendPositionTexColorQuadUnchecked(double x0, double y0, double z0, double u0, double v0, int c0,
                                                    double x1, double y1, double z1, double u1, double v1, int c1,
                                                    double x2, double y2, double z2, double u2, double v2, int c2,
                                                    double x3, double y3, double z3, double u3, double v3, int c3) {
        appendPositionTexColorUnchecked(x0, y0, z0, u0, v0, c0);
        appendPositionTexColorUnchecked(x1, y1, z1, u1, v1, c1);
        appendPositionTexColorUnchecked(x2, y2, z2, u2, v2, c2);
        appendPositionTexColorUnchecked(x3, y3, z3, u3, v3, c3);
    }

    @Override
    public void appendPositionNormalUnchecked(double x, double y, double z, int packedNormal) {
        long address = hbm$intAddress(vertexCount * 4);
        U.putFloat(address, applyXOffset(x));
        U.putFloat(address + 4L, applyYOffset(y));
        U.putFloat(address + 8L, applyZOffset(z));
        U.putInt(address + 12L, packedNormal);
        ++vertexCount;
    }

    @Override
    public void appendParticlePositionTexColorLmapUnchecked(double x, double y, double z, double u, double v,
                                                            int packedColor, int packedLightmap) {
        NTMBufferBuilder.writeParticlePositionTexColorLmap(
                hbm$intAddress(vertexCount * 7),
                applyXOffset(x),
                applyYOffset(y),
                applyZOffset(z),
                (float) u,
                (float) v,
                packedColor,
                packedLightmap
        );
        ++vertexCount;
    }

    @Override
    public void appendParticlePositionTexColorLmapQuadUnchecked(double x0, double y0, double z0, double u0, double v0,
                                                                double x1, double y1, double z1, double u1, double v1,
                                                                double x2, double y2, double z2, double u2, double v2,
                                                                double x3, double y3, double z3, double u3, double v3,
                                                                int packedColor, int packedLightmap) {
        NTMBufferBuilder.writeParticlePositionTexColorLmapQuad(
                hbm$intAddress(vertexCount * 7),
                applyXOffset(x0), applyYOffset(y0), applyZOffset(z0), (float) u0, (float) v0,
                applyXOffset(x1), applyYOffset(y1), applyZOffset(z1), (float) u1, (float) v1,
                applyXOffset(x2), applyYOffset(y2), applyZOffset(z2), (float) u2, (float) v2,
                applyXOffset(x3), applyYOffset(y3), applyZOffset(z3), (float) u3, (float) v3,
                packedColor, packedLightmap
        );
        vertexCount += 4;
    }

    @Override
    public void setVertexCount(int vertexCount) {
        this.vertexCount = vertexCount;
    }

    @Unique
    private boolean hasRemainingInts(int additionalInts) {
        return hbm$bufferSizeInts() + additionalInts <= rawIntBuffer.capacity();
    }

    @Unique
    private void ensureDrawing(VertexFormat requiredFormat) {
        if (!isDrawing) {
            throw new IllegalStateException("Not building!");
        }
        if (vertexFormat != requiredFormat && !requiredFormat.equals(vertexFormat)) {
            throw new IllegalStateException("Expected " + requiredFormat + ", got " + vertexFormat);
        }
    }

    @Unique
    private float applyXOffset(double x) {
        return (float) (x + xOffset);
    }

    @Unique
    private float applyYOffset(double y) {
        return (float) (y + yOffset);
    }

    @Unique
    private float applyZOffset(double z) {
        return (float) (z + zOffset);
    }
}
