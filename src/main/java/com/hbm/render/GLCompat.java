package com.hbm.render;

import com.hbm.main.MainRegistry;
import com.hbm.render.util.AppleVAO;
import net.minecraft.client.Minecraft;
import org.lwjgl.Sys;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class GLCompat {

    public static String error = "";

    public static int GL_ARRAY_BUFFER;
    public static int GL_ELEMENT_ARRAY_BUFFER;
    public static int GL_DYNAMIC_DRAW;
    public static int GL_STATIC_DRAW;

    public static int GL_TEXTURE0;

    public static int GL_RENDERBUFFER;
    public static int GL_FRAMEBUFFER;
    public static int GL_READ_FRAMEBUFFER;
    public static int GL_DRAW_FRAMEBUFFER;
    public static int GL_RGBA16F;
    public static int GL_DEPTH_ATTACHMENT;
    public static int GL_COLOR_ATTACHMENT0;
    public static int GL_DEPTH_COMPONENT24;

    public static int GL_VERTEX_SHADER;
    public static int GL_FRAGMENT_SHADER;
    public static int GL_COMPILE_STATUS;
    public static int GL_LINK_STATUS;
    public static int GL_INFO_LOG_LENGTH;
    public static int GL_CURRENT_PROGRAM;

    public static int GL_FUNC_ADD;
    public static int GL_MAX;

    public static int GL_SAMPLES_PASSED;
    public static int GL_QUERY_RESULT_AVAILABLE;
    public static int GL_QUERY_RESULT;

    public static VAOType vaoType;
    public static FBOType fboType;
    public static InstancingType instancingType;
    public static boolean arbInstancedArrays;
    public static boolean arbImaging;
    public static boolean arbVbo;
    public static boolean arbShaderObject;
    public static boolean arbVertexProgram;
    public static boolean arbVertexShader;
    public static boolean arbFragmentShader;
    public static boolean arbMultitexture;
    public static boolean arbOcclusionQuery;
    @Deprecated
    public static boolean aplLwjglWorkaround;

    public static int genVertexArrays() {
        return switch (vaoType) {
            case NORMAL -> GL30.glGenVertexArrays();
            case ARB -> ARBVertexArrayObject.glGenVertexArrays();
            case APPLE -> APPLEVertexArrayObject.glGenVertexArraysAPPLE();
            case APPLE_COMPAT -> AppleVAO.glGenVertexArraysAPPLE();
        };
    }

    public static void bindVertexArray(int vao) {
        switch (vaoType) {
            case NORMAL -> GL30.glBindVertexArray(vao);
            case ARB -> ARBVertexArrayObject.glBindVertexArray(vao);
            case APPLE -> APPLEVertexArrayObject.glBindVertexArrayAPPLE(vao);
            case APPLE_COMPAT -> AppleVAO.glBindVertexArrayAPPLE(vao);
        }
    }

    public static void deleteVertexArray(int vao) {
        switch (vaoType) {
            case NORMAL -> GL30.glDeleteVertexArrays(vao);
            case ARB -> ARBVertexArrayObject.glDeleteVertexArrays(vao);
            case APPLE -> APPLEVertexArrayObject.glDeleteVertexArraysAPPLE(vao);
            case APPLE_COMPAT -> AppleVAO.glDeleteVertexArraysAPPLE(vao);
        }
    }

    public static void deleteVertexArray(IntBuffer buffer) {
        switch (vaoType) {
            case NORMAL -> GL30.glDeleteVertexArrays(buffer);
            case ARB -> ARBVertexArrayObject.glDeleteVertexArrays(buffer);
            case APPLE -> APPLEVertexArrayObject.glDeleteVertexArraysAPPLE(buffer);
            case APPLE_COMPAT -> AppleVAO.glDeleteVertexArraysAPPLE(buffer);
        }
    }

    public static int genBuffers() {
        if (arbVbo)
            return ARBVertexBufferObject.glGenBuffersARB();
        else
            return GL15.glGenBuffers();
    }

    public static void bindBuffer(int target, int buf) {
        if (arbVbo)
            ARBVertexBufferObject.glBindBufferARB(target, buf);
        else
            GL15.glBindBuffer(target, buf);
    }

    public static void bufferData(int target, ByteBuffer data, int usage) {
        if (arbVbo)
            ARBVertexBufferObject.glBufferDataARB(target, data, usage);
        else
            GL15.glBufferData(target, data, usage);
    }

    public static void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long offset) {
        if (arbVertexProgram)
            ARBVertexProgram.glVertexAttribPointerARB(index, size, type, normalized, stride, offset);
        else
            GL20.glVertexAttribPointer(index, size, type, normalized, stride, offset);
    }

    public static void enableVertexAttribArray(int index) {
        if (arbVertexProgram)
            ARBVertexProgram.glEnableVertexAttribArrayARB(index);
        else
            GL20.glEnableVertexAttribArray(index);
    }

    public static void disableVertexAttribArray(int index) {
        if (arbVertexProgram)
            ARBVertexProgram.glDisableVertexAttribArrayARB(index);
        else
            GL20.glDisableVertexAttribArray(index);
    }

    public static void bindAttribLocation(int program, int index, CharSequence name) {
        if (arbVertexShader)
            ARBVertexShader.glBindAttribLocationARB(program, index, name);
        else
            GL20.glBindAttribLocation(program, index, name);
    }

    public static void deleteFramebuffers(int framebuffer) {
        switch (fboType) {
            case NORMAL:
                GL30.glDeleteFramebuffers(framebuffer);
                break;
            case ARB:
                ARBFramebufferObject.glDeleteFramebuffers(framebuffer);
                break;
            case EXT:
                EXTFramebufferObject.glDeleteFramebuffersEXT(framebuffer);
                break;
        }
    }

    public static void bindFramebuffer(int target, int framebuffer) {
        switch (fboType) {
            case NORMAL:
                GL30.glBindFramebuffer(target, framebuffer);
                break;
            case ARB:
                ARBFramebufferObject.glBindFramebuffer(target, framebuffer);
                break;
            case EXT:
                EXTFramebufferObject.glBindFramebufferEXT(target, framebuffer);
                break;
        }
    }

    public static void framebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        switch (fboType) {
            case NORMAL:
                GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
                break;
            case ARB:
                ARBFramebufferObject.glFramebufferTexture2D(target, attachment, textarget, texture, level);
                break;
            case EXT:
                EXTFramebufferObject.glFramebufferTexture2DEXT(target, attachment, textarget, texture, level);
                break;
        }
    }

    public static void bindRenderbuffer(int target, int renderbuffer) {
        switch (fboType) {
            case NORMAL:
                GL30.glBindRenderbuffer(target, renderbuffer);
                break;
            case ARB:
                ARBFramebufferObject.glBindRenderbuffer(target, renderbuffer);
                break;
            case EXT:
                EXTFramebufferObject.glBindRenderbufferEXT(target, renderbuffer);
                break;
        }
    }

    public static int genRenderbuffers() {
        switch (fboType) {
            case NORMAL:
                return GL30.glGenRenderbuffers();
            case ARB:
                return ARBFramebufferObject.glGenRenderbuffers();
            case EXT:
                return EXTFramebufferObject.glGenRenderbuffersEXT();
        }
        return 0;
    }

    public static void renderbufferStorage(int target, int internalformat, int width, int height) {
        switch (fboType) {
            case NORMAL:
                GL30.glRenderbufferStorage(target, internalformat, width, height);
                break;
            case ARB:
                ARBFramebufferObject.glRenderbufferStorage(target, internalformat, width, height);
                break;
            case EXT:
                EXTFramebufferObject.glRenderbufferStorageEXT(target, internalformat, width, height);
                break;
        }
    }

    public static void framebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
        switch (fboType) {
            case NORMAL:
                GL30.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
                break;
            case ARB:
                ARBFramebufferObject.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
                break;
            case EXT:
                EXTFramebufferObject.glFramebufferRenderbufferEXT(target, attachment, renderbuffertarget, renderbuffer);
                break;
        }
    }

    public static void deleteRenderbuffers(int renderbuffer) {
        switch (fboType) {
            case NORMAL:
                GL30.glDeleteRenderbuffers(renderbuffer);
                break;
            case ARB:
                ARBFramebufferObject.glDeleteRenderbuffers(renderbuffer);
                break;
            case EXT:
                EXTFramebufferObject.glDeleteRenderbuffersEXT(renderbuffer);
                break;
        }
    }

    public static int genFramebuffers() {
        switch (fboType) {
            case NORMAL:
                return GL30.glGenFramebuffers();
            case ARB:
                return ARBFramebufferObject.glGenFramebuffers();
            case EXT:
                return EXTFramebufferObject.glGenFramebuffersEXT();
        }
        return 0;
    }

    public static void blitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
        switch (fboType) {
            case NORMAL:
                GL30.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
                break;
            case ARB:
                ARBFramebufferObject.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
                break;
            case EXT:
                EXTFramebufferBlit.glBlitFramebufferEXT(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
                break;
        }
    }

    public static void activeTexture(int tex) {
        if (arbMultitexture)
            ARBMultitexture.glActiveTextureARB(tex);
        else
            GL13.glActiveTexture(tex);
    }

    public static void blendEquation(int mode) {
        if (arbImaging)
            ARBImaging.glBlendEquation(mode);
        else
            GL14.glBlendEquation(mode);
    }

    public static void drawArraysInstanced(int mode, int first, int count, int primcount) {
        switch (instancingType) {
            case NORMAL:
                GL31.glDrawArraysInstanced(mode, first, count, primcount);
                break;
            case ARB:
                ARBDrawInstanced.glDrawArraysInstancedARB(mode, first, count, primcount);
                break;
            case EXT:
                EXTDrawInstanced.glDrawArraysInstancedEXT(mode, first, count, primcount);
                break;
        }
    }

    public static void vertexAttribDivisor(int index, int divisor) {
        if (arbInstancedArrays)
            ARBInstancedArrays.glVertexAttribDivisorARB(index, divisor);
        else
            GL33.glVertexAttribDivisor(index, divisor);
    }

    public static int genQueries() {
        if (arbOcclusionQuery)
            return ARBOcclusionQuery.glGenQueriesARB();
        else
            return GL15.glGenQueries();
    }

    public static void beginQuery(int target, int id) {
        if (arbOcclusionQuery)
            ARBOcclusionQuery.glBeginQueryARB(target, id);
        else
            GL15.glBeginQuery(target, id);
    }

    public static void endQuery(int target) {
        if (arbOcclusionQuery)
            ARBOcclusionQuery.glEndQueryARB(target);
        else
            GL15.glEndQuery(target);
    }

    public static void deleteQueries(int id) {
        if (arbOcclusionQuery)
            ARBOcclusionQuery.glDeleteQueriesARB(id);
        else
            GL15.glDeleteQueries(id);
    }

    public static int getQueryObject(int id, int pname) {
        if (arbOcclusionQuery)
            return ARBOcclusionQuery.glGetQueryObjectuiARB(id, pname);
        else
            return GL15.glGetQueryObjectui(id, pname);
    }


    public static int getUniformLocation(int program, CharSequence name) {
        if (arbShaderObject)
            return ARBShaderObjects.glGetUniformLocationARB(program, name);
        else
            return GL20.glGetUniformLocation(program, name);
    }

    public static void uniform1i(int location, int i) {
        if (arbShaderObject)
            ARBShaderObjects.glUniform1iARB(location, i);
        else
            GL20.glUniform1i(location, i);
    }

    public static void uniform1f(int location, float f) {
        if (arbShaderObject)
            ARBShaderObjects.glUniform1fARB(location, f);
        else
            GL20.glUniform1f(location, f);
    }

    public static void uniform2f(int location, float f1, float f2) {
        if (arbShaderObject)
            ARBShaderObjects.glUniform2fARB(location, f1, f2);
        else
            GL20.glUniform2f(location, f1, f2);
    }

    public static void uniform3f(int location, float f1, float f2, float f3) {
        if (arbShaderObject)
            ARBShaderObjects.glUniform3fARB(location, f1, f2, f3);
        else
            GL20.glUniform3f(location, f1, f2, f3);
    }

    public static void uniform4f(int location, float f1, float f2, float f3, float f4) {
        if (arbShaderObject)
            ARBShaderObjects.glUniform4fARB(location, f1, f2, f3, f4);
        else
            GL20.glUniform4f(location, f1, f2, f3, f4);
    }

    public static void uniformMatrix3(int location, boolean transpose, FloatBuffer matrices) {
        if (arbShaderObject)
            ARBShaderObjects.glUniformMatrix3ARB(location, transpose, matrices);
        else
            GL20.glUniformMatrix3(location, transpose, matrices);
    }

    public static void uniformMatrix4(int location, boolean transpose, FloatBuffer matrices) {
        if (arbShaderObject)
            ARBShaderObjects.glUniformMatrix4ARB(location, transpose, matrices);
        else
            GL20.glUniformMatrix4(location, transpose, matrices);
    }

    public static int createProgram() {
        if (arbShaderObject)
            return ARBShaderObjects.glCreateProgramObjectARB();
        else
            return GL20.glCreateProgram();
    }

    public static int createShader(int shaderType) {
        if (arbShaderObject)
            return ARBShaderObjects.glCreateShaderObjectARB(shaderType);
        else
            return GL20.glCreateShader(shaderType);
    }

    public static void shaderSource(int shader, ByteBuffer string) {
        if (arbShaderObject)
            ARBShaderObjects.glShaderSourceARB(shader, string);
        else
            GL20.glShaderSource(shader, string);
    }

    public static void compileShader(int shader) {
        if (arbShaderObject)
            ARBShaderObjects.glCompileShaderARB(shader);
        else
            GL20.glCompileShader(shader);
    }

    public static int getShaderi(int shader, int pname) {
        if (arbShaderObject)
            return ARBShaderObjects.glGetObjectParameteriARB(shader, pname);
        else
            return GL20.glGetShaderi(shader, pname);
    }

    public static String getShaderInfoLog(int shader, int length) {
        if (arbShaderObject)
            return ARBShaderObjects.glGetInfoLogARB(shader, length);
        else
            return GL20.glGetShaderInfoLog(shader, length);
    }

    public static void attachShader(int program, int shader) {
        if (arbShaderObject)
            ARBShaderObjects.glAttachObjectARB(program, shader);
        else
            GL20.glAttachShader(program, shader);
    }

    public static void linkProgram(int program) {
        if (arbShaderObject)
            ARBShaderObjects.glLinkProgramARB(program);
        else
            GL20.glLinkProgram(program);
    }

    public static int getProgrami(int program, int pname) {
        if (arbShaderObject)
            return ARBShaderObjects.glGetObjectParameteriARB(program, pname);
        else
            return GL20.glGetProgrami(program, pname);
    }

    public static String getProgramInfoLog(int program, int length) {
        if (arbShaderObject)
            return ARBShaderObjects.glGetInfoLogARB(program, length);
        else
            return GL20.glGetProgramInfoLog(program, length);
    }

    public static void deleteShader(int shader) {
        if (arbShaderObject)
            ARBShaderObjects.glDeleteObjectARB(shader);
        else
            GL20.glDeleteShader(shader);
    }

    public static void useProgram(int program) {
        if (arbShaderObject)
            ARBShaderObjects.glUseProgramObjectARB(program);
        else
            GL20.glUseProgram(program);
    }


    public static String init() {
        ContextCapabilities cap = GLContext.getCapabilities();

        if (cap.OpenGL30)
            vaoType = VAOType.NORMAL;
        else if (Minecraft.IS_RUNNING_ON_MAC) {
            vaoType = VAOType.APPLE;
            if (Sys.getVersion().startsWith("3.")) {
                MainRegistry.logger.info("Apple + LWJGL 3.X.X environment detected, using workaround");
                vaoType = VAOType.APPLE_COMPAT;
                if (!AppleVAO.init()) throw new UnsupportedOperationException("VAO not supported");           }
        } else if (cap.GL_ARB_vertex_array_object)
            vaoType = VAOType.ARB;
        else
            throw new UnsupportedOperationException("Your system does not support Vertex Array Objects");

        if (cap.OpenGL15)
            arbVbo = false;
        else if (cap.GL_ARB_vertex_buffer_object)
            arbVbo = true;
        else
            return "VBO not supported";

        if (cap.OpenGL31)
            instancingType = InstancingType.NORMAL;
        else if (cap.GL_ARB_draw_instanced)
            instancingType = InstancingType.ARB;
        else if (cap.GL_EXT_draw_instanced)
            instancingType = InstancingType.EXT;
        else
            return "Instancing not supported";

        if (cap.OpenGL33)
            arbInstancedArrays = false;
        else if (cap.GL_ARB_instanced_arrays)
            arbInstancedArrays = true;
        else
            return "Instanced arrays not supported";

        if (cap.OpenGL20)
            arbShaderObject = false;
        else if (cap.GL_ARB_shader_objects)
            arbShaderObject = true;
        else
            return "Shaders not supported";

        if (cap.OpenGL20)
            arbVertexProgram = false;
        else if (cap.GL_ARB_vertex_program)
            arbVertexProgram = true;
        else
            return "Vertex program not supported";

        if (cap.OpenGL20)
            arbVertexShader = false;
        else if (cap.GL_ARB_vertex_shader)
            arbVertexShader = true;
        else
            return "Vertex shader not supported";

        if (cap.OpenGL20)
            arbFragmentShader = false;
        else if (cap.GL_ARB_fragment_shader)
            arbFragmentShader = true;
        else
            return "Fragment shader not supported";

        if (cap.OpenGL30)
            fboType = FBOType.NORMAL;
        else if (cap.GL_ARB_framebuffer_object)
            fboType = FBOType.ARB;
        else if (cap.GL_EXT_framebuffer_object && cap.GL_EXT_framebuffer_blit)
            fboType = FBOType.EXT;
        else
            return "Framebuffer objects not supported";

        if (cap.OpenGL13)
            arbMultitexture = false;
        else if (cap.GL_ARB_multitexture)
            arbMultitexture = true;
        else
            return "Multitexturing not supported";

        if (cap.OpenGL14)
            arbImaging = false;
        else if (cap.GL_ARB_imaging)
            arbImaging = true;
        else
            return "Imaging not supported";

        if (cap.OpenGL15)
            arbOcclusionQuery = false;
        else if (cap.GL_ARB_occlusion_query)
            arbOcclusionQuery = true;
        else
            return "Occlusion queries not supported";

        if (arbVertexProgram) {
            GL_ARRAY_BUFFER = ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB;
            GL_ELEMENT_ARRAY_BUFFER = ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB;
            GL_DYNAMIC_DRAW = ARBVertexBufferObject.GL_DYNAMIC_DRAW_ARB;
            GL_STATIC_DRAW = ARBVertexBufferObject.GL_STATIC_DRAW_ARB;
        } else {
            GL_ARRAY_BUFFER = GL15.GL_ARRAY_BUFFER;
            GL_ELEMENT_ARRAY_BUFFER = GL15.GL_ELEMENT_ARRAY_BUFFER;
            GL_DYNAMIC_DRAW = GL15.GL_DYNAMIC_DRAW;
            GL_STATIC_DRAW = GL15.GL_STATIC_DRAW;
        }

        if (arbMultitexture) {
            GL_TEXTURE0 = ARBMultitexture.GL_TEXTURE0_ARB;
        } else {
            GL_TEXTURE0 = GL13.GL_TEXTURE0;
        }

        if (fboType == FBOType.NORMAL) {
            GL_RENDERBUFFER = GL30.GL_RENDERBUFFER;
            GL_FRAMEBUFFER = GL30.GL_FRAMEBUFFER;
            GL_READ_FRAMEBUFFER = GL30.GL_READ_FRAMEBUFFER;
            GL_DRAW_FRAMEBUFFER = GL30.GL_DRAW_FRAMEBUFFER;
            GL_DEPTH_ATTACHMENT = GL30.GL_DEPTH_ATTACHMENT;
            GL_COLOR_ATTACHMENT0 = GL30.GL_COLOR_ATTACHMENT0;
        } else if (fboType == FBOType.ARB) {
            GL_RENDERBUFFER = ARBFramebufferObject.GL_RENDERBUFFER;
            GL_FRAMEBUFFER = ARBFramebufferObject.GL_FRAMEBUFFER;
            GL_READ_FRAMEBUFFER = ARBFramebufferObject.GL_READ_FRAMEBUFFER;
            GL_DRAW_FRAMEBUFFER = ARBFramebufferObject.GL_DRAW_FRAMEBUFFER;
            GL_DEPTH_ATTACHMENT = ARBFramebufferObject.GL_DEPTH_ATTACHMENT;
            GL_COLOR_ATTACHMENT0 = ARBFramebufferObject.GL_COLOR_ATTACHMENT0;
        } else if (fboType == FBOType.EXT) {
            GL_RENDERBUFFER = EXTFramebufferObject.GL_RENDERBUFFER_EXT;
            GL_FRAMEBUFFER = EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
            GL_READ_FRAMEBUFFER = EXTFramebufferBlit.GL_READ_FRAMEBUFFER_EXT;
            GL_DRAW_FRAMEBUFFER = EXTFramebufferBlit.GL_DRAW_FRAMEBUFFER_EXT;
            GL_DEPTH_ATTACHMENT = EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT;
            GL_COLOR_ATTACHMENT0 = EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT;
        }

        if (cap.OpenGL30)
            GL_RGBA16F = GL30.GL_RGBA16F;
        else if (cap.GL_APPLE_float_pixels)
            GL_RGBA16F = APPLEFloatPixels.GL_RGBA_FLOAT16_APPLE;
        else if (cap.GL_ARB_texture_float)
            GL_RGBA16F = ARBTextureFloat.GL_RGBA16F_ARB;
        else if (cap.GL_ATI_texture_float)
            GL_RGBA16F = ATITextureFloat.GL_RGBA_FLOAT16_ATI;
        else
            return "Floating point texture format not supported";

        if (cap.OpenGL14)
            GL_DEPTH_COMPONENT24 = GL14.GL_DEPTH_COMPONENT24;
        else if (cap.GL_ARB_depth_texture)
            GL_DEPTH_COMPONENT24 = ARBDepthTexture.GL_DEPTH_COMPONENT24_ARB;
        else
            return "24 bit depth not supported";

        if (arbShaderObject) {
            GL_COMPILE_STATUS = ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB;
            GL_LINK_STATUS = ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB;
            GL_INFO_LOG_LENGTH = ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB;
            //Apparently the ARB thing doesn't have this, let's just hope it works
            GL_CURRENT_PROGRAM = GL20.GL_CURRENT_PROGRAM;
        } else {
            GL_COMPILE_STATUS = GL20.GL_COMPILE_STATUS;
            GL_LINK_STATUS = GL20.GL_LINK_STATUS;
            GL_INFO_LOG_LENGTH = GL20.GL_INFO_LOG_LENGTH;
            GL_CURRENT_PROGRAM = GL20.GL_CURRENT_PROGRAM;
        }

        if (arbVertexShader) {
            GL_VERTEX_SHADER = ARBVertexShader.GL_VERTEX_SHADER_ARB;
        } else {
            GL_VERTEX_SHADER = GL20.GL_VERTEX_SHADER;
        }

        if (arbFragmentShader) {
            GL_FRAGMENT_SHADER = ARBFragmentShader.GL_FRAGMENT_SHADER_ARB;
        } else {
            GL_FRAGMENT_SHADER = GL20.GL_FRAGMENT_SHADER;
        }

        if (arbImaging) {
            GL_FUNC_ADD = ARBImaging.GL_FUNC_ADD;
            GL_MAX = ARBImaging.GL_MAX;
        } else {
            GL_FUNC_ADD = GL14.GL_FUNC_ADD;
            GL_MAX = GL14.GL_MAX;
        }

        if (arbOcclusionQuery) {
            GL_SAMPLES_PASSED = ARBOcclusionQuery.GL_SAMPLES_PASSED_ARB;
            GL_QUERY_RESULT_AVAILABLE = ARBOcclusionQuery.GL_QUERY_RESULT_AVAILABLE_ARB;
            GL_QUERY_RESULT = ARBOcclusionQuery.GL_QUERY_RESULT_ARB;
        } else {
            GL_SAMPLES_PASSED = GL15.GL_SAMPLES_PASSED;
            GL_QUERY_RESULT_AVAILABLE = GL15.GL_QUERY_RESULT_AVAILABLE;
            GL_QUERY_RESULT = GL15.GL_QUERY_RESULT;
        }
        return "";

    }



    public static enum VAOType {
        NORMAL,
        ARB,
        APPLE,
        APPLE_COMPAT
    }

    public static enum FBOType {
        NORMAL,
        ARB,
        EXT;
    }

    public static enum InstancingType {
        NORMAL,
        ARB,
        EXT;
    }

}
