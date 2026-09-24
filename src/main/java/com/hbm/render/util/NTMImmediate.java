package com.hbm.render.util;

import com.hbm.core.ModPresence;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

@SideOnly(Side.CLIENT)
public final class NTMImmediate {

    public static final NTMImmediate INSTANCE = new NTMImmediate();

    private final BufferBuilder buffer;
    private final WorldVertexBufferUploader genericUploader = new WorldVertexBufferUploader();

    private NTMImmediate() {
        this.buffer = Tessellator.getInstance().getBuffer();
    }

    private NTMBufferBuilder fastBuffer() {
        return (NTMBufferBuilder) buffer;
    }

    public BufferBuilder begin(int drawMode, VertexFormat format) {
        return begin(drawMode, format, 0);
    }

    public BufferBuilder begin(int drawMode, VertexFormat format, int expectedVertices) {
        fastBuffer().beginFast(drawMode, format, expectedVertices);
        return buffer;
    }

    public NTMBufferBuilder beginPosition(int drawMode, int expectedVertices) {
        NTMBufferBuilder fastBuffer = fastBuffer();
        fastBuffer.beginFast(drawMode, DefaultVertexFormats.POSITION, expectedVertices);
        return fastBuffer;
    }

    public NTMBufferBuilder beginBlock(int drawMode, int expectedVertices) {
        NTMBufferBuilder fastBuffer = fastBuffer();
        fastBuffer.beginFast(drawMode, DefaultVertexFormats.BLOCK, expectedVertices);
        return fastBuffer;
    }

    public NTMBufferBuilder beginBlockQuads(int expectedQuads) {
        return beginBlock(GL11.GL_QUADS, expectedQuads * 4);
    }

    public NTMBufferBuilder beginItem(int drawMode, int expectedVertices) {
        NTMBufferBuilder fastBuffer = fastBuffer();
        fastBuffer.beginFast(drawMode, DefaultVertexFormats.ITEM, expectedVertices);
        return fastBuffer;
    }

    public NTMBufferBuilder beginItemQuads(int expectedQuads) {
        return beginItem(GL11.GL_QUADS, expectedQuads * 4);
    }

    public NTMBufferBuilder beginPositionQuads(int expectedQuads) {
        return beginPosition(GL11.GL_QUADS, expectedQuads * 4);
    }

    public NTMBufferBuilder beginPositionColor(int drawMode, int expectedVertices) {
        NTMBufferBuilder fastBuffer = fastBuffer();
        fastBuffer.beginFast(drawMode, DefaultVertexFormats.POSITION_COLOR, expectedVertices);
        return fastBuffer;
    }

    public NTMBufferBuilder beginPositionColorQuads(int expectedQuads) {
        return beginPositionColor(GL11.GL_QUADS, expectedQuads * 4);
    }

    public NTMBufferBuilder beginPositionTex(int drawMode, int expectedVertices) {
        NTMBufferBuilder fastBuffer = fastBuffer();
        fastBuffer.beginFast(drawMode, DefaultVertexFormats.POSITION_TEX, expectedVertices);
        return fastBuffer;
    }

    public NTMBufferBuilder beginPositionTexQuads(int expectedQuads) {
        return beginPositionTex(GL11.GL_QUADS, expectedQuads * 4);
    }

    public NTMBufferBuilder beginPositionTexColor(int drawMode, int expectedVertices) {
        NTMBufferBuilder fastBuffer = fastBuffer();
        fastBuffer.beginFast(drawMode, DefaultVertexFormats.POSITION_TEX_COLOR, expectedVertices);
        return fastBuffer;
    }

    public NTMBufferBuilder beginPositionTexColorQuads(int expectedQuads) {
        return beginPositionTexColor(GL11.GL_QUADS, expectedQuads * 4);
    }

    public NTMBufferBuilder beginPositionTexNormal(int drawMode, int expectedVertices) {
        NTMBufferBuilder fastBuffer = fastBuffer();
        fastBuffer.beginFast(drawMode, DefaultVertexFormats.POSITION_TEX_NORMAL, expectedVertices);
        return fastBuffer;
    }

    public NTMBufferBuilder beginPositionTexNormalQuads(int expectedQuads) {
        return beginPositionTexNormal(GL11.GL_QUADS, expectedQuads * 4);
    }

    public NTMBufferBuilder beginPositionTexLmapColor(int drawMode, int expectedVertices) {
        NTMBufferBuilder fastBuffer = fastBuffer();
        fastBuffer.beginFast(drawMode, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR, expectedVertices);
        return fastBuffer;
    }

    public NTMBufferBuilder beginPositionTexLmapColorQuads(int expectedQuads) {
        return beginPositionTexLmapColor(GL11.GL_QUADS, expectedQuads * 4);
    }

    public NTMBufferBuilder beginPositionTexColorNormal(int drawMode, int expectedVertices) {
        NTMBufferBuilder fastBuffer = fastBuffer();
        fastBuffer.beginFast(drawMode, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, expectedVertices);
        return fastBuffer;
    }

    public NTMBufferBuilder beginPositionTexColorNormalQuads(int expectedQuads) {
        return beginPositionTexColorNormal(GL11.GL_QUADS, expectedQuads * 4);
    }

    public NTMBufferBuilder beginPositionNormal(int drawMode, int expectedVertices) {
        NTMBufferBuilder fastBuffer = fastBuffer();
        fastBuffer.beginFast(drawMode, DefaultVertexFormats.POSITION_NORMAL, expectedVertices);
        return fastBuffer;
    }

    public NTMBufferBuilder beginParticlePositionTexColorLmap(int drawMode, int expectedVertices) {
        NTMBufferBuilder fastBuffer = fastBuffer();
        fastBuffer.beginFast(drawMode, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP, expectedVertices);
        return fastBuffer;
    }

    public NTMBufferBuilder getBuffer() {
        return fastBuffer();
    }

    public void draw() {
        buffer.finishDrawing();
        if (buffer.getVertexCount() <= 0) {
            buffer.reset();
            return;
        }
        if (ModPresence.OPTIFINE) {
            genericUploader.draw(buffer);
            return;
        }
        SpecializedUploader uploader = findUploader(buffer.getVertexFormat());
        if (uploader != null) {
            uploader.draw(buffer);
            buffer.reset();
        } else {
            genericUploader.draw(buffer);
        }
    }

    private SpecializedUploader findUploader(VertexFormat format) {
        if (format == DefaultVertexFormats.BLOCK) return NTMImmediate::drawBlock;
        if (format == DefaultVertexFormats.ITEM) return NTMImmediate::drawItem;
        if (format == DefaultVertexFormats.POSITION) return NTMImmediate::drawPosition;
        if (format == DefaultVertexFormats.POSITION_COLOR) return NTMImmediate::drawPositionColor;
        if (format == DefaultVertexFormats.POSITION_TEX) return NTMImmediate::drawPositionTex;
        if (format == DefaultVertexFormats.POSITION_NORMAL) return NTMImmediate::drawPositionNormal;
        if (format == DefaultVertexFormats.POSITION_TEX_COLOR) return NTMImmediate::drawPositionTexColor;
        if (format == DefaultVertexFormats.POSITION_TEX_NORMAL) return NTMImmediate::drawPositionTexNormal;
        if (format == DefaultVertexFormats.POSITION_TEX_LMAP_COLOR) return NTMImmediate::drawPositionTexLmapColor;
        if (format == DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL) return NTMImmediate::drawPositionTexColorNormal;
        if (format == DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP) return NTMImmediate::drawParticlePositionTexColorLmap;

        if (format == null) return null;

        if (DefaultVertexFormats.BLOCK.equals(format)) return NTMImmediate::drawBlock;
        if (DefaultVertexFormats.ITEM.equals(format)) return NTMImmediate::drawItem;
        if (DefaultVertexFormats.POSITION.equals(format)) return NTMImmediate::drawPosition;
        if (DefaultVertexFormats.POSITION_COLOR.equals(format)) return NTMImmediate::drawPositionColor;
        if (DefaultVertexFormats.POSITION_TEX.equals(format)) return NTMImmediate::drawPositionTex;
        if (DefaultVertexFormats.POSITION_NORMAL.equals(format)) return NTMImmediate::drawPositionNormal;
        if (DefaultVertexFormats.POSITION_TEX_COLOR.equals(format)) return NTMImmediate::drawPositionTexColor;
        if (DefaultVertexFormats.POSITION_TEX_NORMAL.equals(format)) return NTMImmediate::drawPositionTexNormal;
        if (DefaultVertexFormats.POSITION_TEX_LMAP_COLOR.equals(format)) return NTMImmediate::drawPositionTexLmapColor;
        if (DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL.equals(format)) return NTMImmediate::drawPositionTexColorNormal;
        if (DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP.equals(format)) return NTMImmediate::drawParticlePositionTexColorLmap;
        return null;
    }

    private static void drawPosition(BufferBuilder buffer) {
        ByteBuffer byteBuffer = buffer.getByteBuffer();
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        byteBuffer.position(0);
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 12, byteBuffer);
        GlStateManager.glDrawArrays(buffer.getDrawMode(), 0, buffer.getVertexCount());
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }

    private static void drawBlock(BufferBuilder buffer) {
        ByteBuffer byteBuffer = buffer.getByteBuffer();
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        byteBuffer.position(0);
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 28, byteBuffer);
        GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
        byteBuffer.position(12);
        GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 28, byteBuffer);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        byteBuffer.position(16);
        GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, 28, byteBuffer);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        byteBuffer.position(24);
        GlStateManager.glTexCoordPointer(2, GL11.GL_SHORT, 28, byteBuffer);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glDrawArrays(buffer.getDrawMode(), 0, buffer.getVertexCount());
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GlStateManager.resetColor();
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }

    private static void drawItem(BufferBuilder buffer) {
        ByteBuffer byteBuffer = buffer.getByteBuffer();
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        byteBuffer.position(0);
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 28, byteBuffer);
        GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
        byteBuffer.position(12);
        GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 28, byteBuffer);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        byteBuffer.position(16);
        GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, 28, byteBuffer);
        GlStateManager.glEnableClientState(GL11.GL_NORMAL_ARRAY);
        byteBuffer.position(24);
        GlStateManager.glNormalPointer(GL11.GL_BYTE, 28, byteBuffer);
        GlStateManager.glDrawArrays(buffer.getDrawMode(), 0, buffer.getVertexCount());
        GlStateManager.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GlStateManager.resetColor();
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }

    private static void drawPositionColor(BufferBuilder buffer) {
        ByteBuffer byteBuffer = buffer.getByteBuffer();
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        byteBuffer.position(0);
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 16, byteBuffer);
        GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
        byteBuffer.position(12);
        GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 16, byteBuffer);
        GlStateManager.glDrawArrays(buffer.getDrawMode(), 0, buffer.getVertexCount());
        GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GlStateManager.resetColor();
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }

    private static void drawPositionTex(BufferBuilder buffer) {
        ByteBuffer byteBuffer = buffer.getByteBuffer();
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        byteBuffer.position(0);
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 20, byteBuffer);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        byteBuffer.position(12);
        GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, 20, byteBuffer);
        GlStateManager.glDrawArrays(buffer.getDrawMode(), 0, buffer.getVertexCount());
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }

    private static void drawPositionNormal(BufferBuilder buffer) {
        ByteBuffer byteBuffer = buffer.getByteBuffer();
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        byteBuffer.position(0);
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 16, byteBuffer);
        GlStateManager.glEnableClientState(GL11.GL_NORMAL_ARRAY);
        byteBuffer.position(12);
        GlStateManager.glNormalPointer(GL11.GL_BYTE, 16, byteBuffer);
        GlStateManager.glDrawArrays(buffer.getDrawMode(), 0, buffer.getVertexCount());
        GlStateManager.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }

    private static void drawPositionTexColor(BufferBuilder buffer) {
        ByteBuffer byteBuffer = buffer.getByteBuffer();
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        byteBuffer.position(0);
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 24, byteBuffer);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        byteBuffer.position(12);
        GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, 24, byteBuffer);
        GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
        byteBuffer.position(20);
        GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 24, byteBuffer);
        GlStateManager.glDrawArrays(buffer.getDrawMode(), 0, buffer.getVertexCount());
        GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GlStateManager.resetColor();
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }

    private static void drawPositionTexNormal(BufferBuilder buffer) {
        ByteBuffer byteBuffer = buffer.getByteBuffer();
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        byteBuffer.position(0);
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 24, byteBuffer);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        byteBuffer.position(12);
        GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, 24, byteBuffer);
        GlStateManager.glEnableClientState(GL11.GL_NORMAL_ARRAY);
        byteBuffer.position(20);
        GlStateManager.glNormalPointer(GL11.GL_BYTE, 24, byteBuffer);
        GlStateManager.glDrawArrays(buffer.getDrawMode(), 0, buffer.getVertexCount());
        GlStateManager.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }

    private static void drawPositionTexLmapColor(BufferBuilder buffer) {
        ByteBuffer byteBuffer = buffer.getByteBuffer();
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        byteBuffer.position(0);
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 28, byteBuffer);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        byteBuffer.position(12);
        GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, 28, byteBuffer);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        byteBuffer.position(20);
        GlStateManager.glTexCoordPointer(2, GL11.GL_SHORT, 28, byteBuffer);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
        byteBuffer.position(24);
        GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 28, byteBuffer);
        GlStateManager.glDrawArrays(buffer.getDrawMode(), 0, buffer.getVertexCount());
        GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GlStateManager.resetColor();
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }

    private static void drawPositionTexColorNormal(BufferBuilder buffer) {
        ByteBuffer byteBuffer = buffer.getByteBuffer();
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        byteBuffer.position(0);
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 28, byteBuffer);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        byteBuffer.position(12);
        GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, 28, byteBuffer);
        GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
        byteBuffer.position(20);
        GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 28, byteBuffer);
        GlStateManager.glEnableClientState(GL11.GL_NORMAL_ARRAY);
        byteBuffer.position(24);
        GlStateManager.glNormalPointer(GL11.GL_BYTE, 28, byteBuffer);
        GlStateManager.glDrawArrays(buffer.getDrawMode(), 0, buffer.getVertexCount());
        GlStateManager.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GlStateManager.resetColor();
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }

    private static void drawParticlePositionTexColorLmap(BufferBuilder buffer) {
        ByteBuffer byteBuffer = buffer.getByteBuffer();
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        byteBuffer.position(0);
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 28, byteBuffer);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        byteBuffer.position(12);
        GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, 28, byteBuffer);
        GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
        byteBuffer.position(20);
        GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 28, byteBuffer);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        byteBuffer.position(24);
        GlStateManager.glTexCoordPointer(2, GL11.GL_SHORT, 28, byteBuffer);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glDrawArrays(buffer.getDrawMode(), 0, buffer.getVertexCount());
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GlStateManager.resetColor();
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }

    @FunctionalInterface
    private interface SpecializedUploader {
        void draw(BufferBuilder buffer);
    }
}
