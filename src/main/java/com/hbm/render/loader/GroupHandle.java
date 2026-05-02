package com.hbm.render.loader;

import com.hbm.render.GLCompat;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL11.glDrawElements;

public class GroupHandle {

    int vaoHandle = -1;
    int indexCount = 0;

    public final void render() {
        GLCompat.bindVertexArray(vaoHandle);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, 0L);
        GLCompat.bindVertexArray(0);
    }

    public final void bindAndDraw() {
        GLCompat.bindVertexArray(vaoHandle);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, 0L);
    }

    public static void unbind() {
        GLCompat.bindVertexArray(0);
    }
}
