package com.hbm.render.loader;

import com.hbm.render.GLCompat;

import static org.lwjgl.opengl.GL11.*;

public class GroupHandle {

    int vaoHandle = -1;
    int eboHandle = -1;
    int indexCount = 0;

    public final void render() {
        GLCompat.bindVertexArray(vaoHandle);
        if (!GLCompat.vaoTracksElementBuffer) GLCompat.bindBuffer(GLCompat.GL_ELEMENT_ARRAY_BUFFER, eboHandle);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, 0L);
        GLCompat.bindVertexArray(0);
    }

    public final void bindAndDraw() {
        GLCompat.bindVertexArray(vaoHandle);
        if (!GLCompat.vaoTracksElementBuffer) GLCompat.bindBuffer(GLCompat.GL_ELEMENT_ARRAY_BUFFER, eboHandle);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, 0L);
    }

    public static void unbind() {
        GLCompat.bindVertexArray(0);
    }
}
