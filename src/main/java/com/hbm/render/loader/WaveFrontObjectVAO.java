package com.hbm.render.loader;

import com.hbm.interfaces.SuppressCheckedExceptions;
import com.hbm.render.GLCompat;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.lwjgl.opengl.GL11.*;

public class WaveFrontObjectVAO implements IModelCustomNamed {

    public static final List<WaveFrontObjectVAO> allVBOs = new ArrayList<>();
    private static final int FLOATS_PER_VERTEX = 9; // pos3 + uv3 + normal3
    private static final int BASE_STRIDE = FLOATS_PER_VERTEX * Float.BYTES;
    private static final int COLOR_OFFSET = BASE_STRIDE;
    private static final int COLOR_BYTES = 4;
    private static final int MAX_INDEXED_VERTICES = 1 << 16;
    public static boolean uploaded = false;

    final List<VBOBufferData> groups = new ArrayList<>();
    private final Object2ObjectOpenHashMap<String, VBOBufferData> groupsByName = new Object2ObjectOpenHashMap<>();
    private final boolean colored;
    private final int stride;

    public WaveFrontObjectVAO(HFRWavefrontObject obj) {
        this(obj, false);
    }

    public WaveFrontObjectVAO(HFRWavefrontObject obj, boolean colored) {
        if (uploaded) throw new UnsupportedOperationException(
                "Cannot load new models after uploadModels() has been called. " +
                        "Move your model to ResourceManager's static initializer!"
        );
        this.colored = colored;
        this.stride = colored ? BASE_STRIDE + COLOR_BYTES : BASE_STRIDE;
        load(obj);
        allVBOs.add(this);
    }

    public void load(HFRWavefrontObject obj) {
        for (GroupObject g : obj.groupObjects) {
            String key = g.name.toLowerCase(Locale.ROOT);
            VBOBufferData data = groupsByName.get(key);
            //mlbv: keep VBOBufferData identity so that my javac plugin works
            boolean fresh = data == null;
            if (fresh) {
                data = new VBOBufferData();
                data.name = g.name;
            }
            buildGroupBuffers(g, data);
            if (uploaded) uploadGroupVao(data);
            if (fresh) {
                groups.add(data);
                groupsByName.put(key, data);
            }
        }
    }

    private void buildGroupBuffers(GroupObject g, VBOBufferData data) {
        int maxVerts = 0;
        int triangleCount = 0;
        for (Face face : g.faces) {
            int n = face.vertices.length;
            maxVerts += n;
            if (n >= 3) triangleCount += n - 2;
        }

        ByteBuffer vBuf = BufferUtils.createByteBuffer(maxVerts * stride);
        short[] indices = new short[triangleCount * 3];
        Object2IntOpenHashMap<VertexKey> lookup = new Object2IntOpenHashMap<>(maxVerts * 2);
        lookup.defaultReturnValue(-1);
        VertexKey probe = new VertexKey();
        int[] faceIdx = new int[4];
        int vertexCount = 0;
        int indexCursor = 0;

        for (Face face : g.faces) {
            int n = face.vertices.length;
            if (faceIdx.length < n) faceIdx = new int[n];

            for (int i = 0; i < n; i++) {
                Vertex vert = face.vertices[i];
                Vertex normal = face.vertexNormals[i];
                float u = 0f, v = 0f, w = 0f;
                if (face.textureCoordinates != null && face.textureCoordinates.length > 0) {
                    TextureCoordinate tex = face.textureCoordinates[i];
                    u = tex.u;
                    v = tex.v;
                    w = tex.w;
                }

                probe.set(vert.x, vert.y, vert.z, u, v, w, normal.x, normal.y, normal.z, colored ? vert.color : 0);
                int idx = lookup.getInt(probe);
                if (idx < 0) {
                    if (vertexCount >= MAX_INDEXED_VERTICES) {
                        throw new IllegalStateException("Group '" + g.name + "' exceeds 16-bit index capacity");
                    }
                    idx = vertexCount++;
                    vBuf.putFloat(vert.x).putFloat(vert.y).putFloat(vert.z);
                    vBuf.putFloat(u).putFloat(v).putFloat(w);
                    vBuf.putFloat(normal.x).putFloat(normal.y).putFloat(normal.z);
                    if (colored) {
                        int c = vert.color;
                        vBuf.put((byte) ((c >>> 16) & 0xFF));
                        vBuf.put((byte) ((c >>>  8) & 0xFF));
                        vBuf.put((byte) ( c         & 0xFF));
                        vBuf.put((byte) ((c >>> 24) & 0xFF));
                    }
                    lookup.put(probe.clone(), idx);
                }
                faceIdx[i] = idx;
            }

            for (int i = 2; i < n; i++) {
                indices[indexCursor++] = (short) faceIdx[0];
                indices[indexCursor++] = (short) faceIdx[i - 1];
                indices[indexCursor++] = (short) faceIdx[i];
            }
        }

        vBuf.flip();

        ByteBuffer iBuf = BufferUtils.createByteBuffer(indexCursor * Short.BYTES);
        iBuf.asShortBuffer().put(indices, 0, indexCursor);
        iBuf.limit(indexCursor * Short.BYTES);

        data.vboHandle = GLCompat.genBuffers();
        GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, data.vboHandle);
        GLCompat.bufferData(GLCompat.GL_ARRAY_BUFFER, vBuf, GLCompat.GL_STATIC_DRAW);
        GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, 0);

        data.eboHandle = GLCompat.genBuffers();
        GLCompat.bindBuffer(GLCompat.GL_ELEMENT_ARRAY_BUFFER, data.eboHandle);
        GLCompat.bufferData(GLCompat.GL_ELEMENT_ARRAY_BUFFER, iBuf, GLCompat.GL_STATIC_DRAW);
        GLCompat.bindBuffer(GLCompat.GL_ELEMENT_ARRAY_BUFFER, 0);

        data.indexCount = indexCursor;
    }

    @SuppressWarnings("unused")
    public GroupHandle resolve(String name) {
        return groupsByName.get(name.toLowerCase(Locale.ROOT));
    }

    public void uploadModels() {
        for (VBOBufferData data : groups) {
            uploadGroupVao(data);
        }
    }

    private void uploadGroupVao(VBOBufferData data) {
        data.vaoHandle = GLCompat.genVertexArrays();
        GLCompat.bindVertexArray(data.vaoHandle);

        GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, data.vboHandle);
        GLCompat.bindBuffer(GLCompat.GL_ELEMENT_ARRAY_BUFFER, data.eboHandle);

        glVertexPointer(3, GL_FLOAT, stride, 0L);
        glEnableClientState(GL_VERTEX_ARRAY);

        glTexCoordPointer(3, GL_FLOAT, stride, 3L * Float.BYTES);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);

        glNormalPointer(GL_FLOAT, stride, 6L * Float.BYTES);
        glEnableClientState(GL_NORMAL_ARRAY);

        if (colored) {
            glColorPointer(4, GL_UNSIGNED_BYTE, stride, COLOR_OFFSET);
            glEnableClientState(GL_COLOR_ARRAY);
        }

        GLCompat.bindVertexArray(0);
        GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, 0);
        GLCompat.bindBuffer(GLCompat.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    // truth be told, i have no fucking idea what i'm doing
    // i know the VBO sends data to the GPU to be saved there directly which is where the optimization comes from in the first place
    // so logically, if we want to get rid of this, we need to blow the data up
    // documentation on GL15 functions seems nonexistant so fuck it we ball i guess
    public void destroy() {
        for (VBOBufferData data : groups) {
            GLCompat.deleteBuffers(data.vboHandle);
            GLCompat.deleteBuffers(data.eboHandle);
            GLCompat.deleteVertexArray(data.vaoHandle);
            data.vboHandle = -1;
            data.eboHandle = -1;
            data.vaoHandle = -1;
            data.indexCount = 0;
        }
    }

    @Override
    public String getType() {
        return "obj_vao";
    }

    @Override
    public void renderAll() {
        for (VBOBufferData data : groups) {
            data.bindAndDraw();
        }
        GroupHandle.unbind();
    }

    @Override
    public void renderOnly(String... groupNames) {
        for (VBOBufferData data : groups) {
            for (String name : groupNames) {
                if (data.name.equalsIgnoreCase(name)) {
                    data.bindAndDraw();
                }
            }
        }
        GroupHandle.unbind();
    }

    @Override
    public void renderPart(String partName) {
        for (VBOBufferData data : groups) {
            if (data.name.equalsIgnoreCase(partName)) {
                data.bindAndDraw();
            }
        }
        GroupHandle.unbind();
    }

    @Override
    public void renderAllExcept(String... excludedGroupNames) {
        for (VBOBufferData data : groups) {
            boolean skip = false;
            for (String name : excludedGroupNames) {
                if (data.name.equalsIgnoreCase(name)) {
                    skip = true;
                    break;
                }
            }
            if (!skip) {
                data.bindAndDraw();
            }
        }
        GroupHandle.unbind();
    }

    @Override
    public List<String> getPartNames() {
        List<String> names = new ArrayList<>();
        for (VBOBufferData data : groups) {
            names.add(data.name);
        }
        return names;
    }

    static final class VBOBufferData extends GroupHandle {
        String name;
        int vboHandle = -1;
    }

    @SuppressCheckedExceptions
    private static final class VertexKey implements Cloneable {
        int px, py, pz, u, v, w, nx, ny, nz, color;
        int hash;

        void set(float px, float py, float pz, float u, float v, float w, float nx, float ny, float nz, int color) {
            this.px = Float.floatToRawIntBits(px);
            this.py = Float.floatToRawIntBits(py);
            this.pz = Float.floatToRawIntBits(pz);
            this.u = Float.floatToRawIntBits(u);
            this.v = Float.floatToRawIntBits(v);
            this.w = Float.floatToRawIntBits(w);
            this.nx = Float.floatToRawIntBits(nx);
            this.ny = Float.floatToRawIntBits(ny);
            this.nz = Float.floatToRawIntBits(nz);
            this.color = color;
            int h = 1;
            h = 31 * h + this.px;
            h = 31 * h + this.py;
            h = 31 * h + this.pz;
            h = 31 * h + this.u;
            h = 31 * h + this.v;
            h = 31 * h + this.w;
            h = 31 * h + this.nx;
            h = 31 * h + this.ny;
            h = 31 * h + this.nz;
            h = 31 * h + this.color;
            this.hash = h;
        }

        @Override
        public VertexKey clone() {
            return (VertexKey) super.clone();
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof VertexKey)) return false;
            VertexKey k = (VertexKey) o;
            return px == k.px && py == k.py && pz == k.pz && u == k.u && v == k.v && w == k.w && nx == k.nx && ny == k.ny && nz == k.nz && color == k.color;
        }
    }
}
