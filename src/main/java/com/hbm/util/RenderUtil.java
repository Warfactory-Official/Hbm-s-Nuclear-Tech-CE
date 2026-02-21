package com.hbm.util;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Manual GL attrib snapshot/restore built on top of {@link GlStateManager}.
 *
 * <p><b>Important</b>: This only handles state that {@code GlStateManager} itself tracks.
 * Anything not tracked there is intentionally <i>not</i> snapshotted/restored:
 * <ul>
 *   <li>Stencil buffer state</li>
 *   <li>Viewport/scissor/matrix stacks</li>
 *   <li>Blend equation</li>
 *   <li>TexEnv / combine / sampler params</li>
 *   <li>VBO/VAO bindings</li>
 * </ul>
 */
public final class RenderUtil {

    private static final Deque<AttribSnapshot> ATTRIB_STACK = new ArrayDeque<>();
    private static final FloatBuffer DEBUG_COLOR_BUF = BufferUtils.createFloatBuffer(16);
    private static final ByteBuffer DEBUG_BOOL4_BUF = BufferUtils.createByteBuffer(16);
    private static final ByteBuffer DEBUG_BOOL1_BUF = BufferUtils.createByteBuffer(16);
    private static final float FLOAT_EPS = 1.0e-4F;

    private RenderUtil() {
    }

    public static int getAlphaFunc() {
        return GlStateManager.alphaState.func;
    }

    public static float getAlphaRef() {
        return GlStateManager.alphaState.ref;
    }

    public static boolean isAlphaEnabled() {
        return GlStateManager.alphaState.alphaTest.currentState;
    }

    public static int getBlendSrcFactor() {
        return GlStateManager.blendState.srcFactor;
    }

    public static int getBlendDstFactor() {
        return GlStateManager.blendState.dstFactor;
    }

    public static int getBlendSrcAlphaFactor() {
        return GlStateManager.blendState.srcFactorAlpha;
    }

    public static int getBlendDstAlphaFactor() {
        return GlStateManager.blendState.dstFactorAlpha;
    }

    public static boolean isBlendEnabled() {
        return GlStateManager.blendState.blend.currentState;
    }

    public static boolean isCullEnabled() {
        return GlStateManager.cullState.cullFace.currentState;
    }

    public static boolean isDepthMaskEnabled() {
        return GlStateManager.depthState.maskEnabled;
    }

    public static int getDepthFunc() {
        return GlStateManager.depthState.depthFunc;
    }

    public static boolean isDepthEnabled() {
        return GlStateManager.depthState.depthTest.currentState;
    }

    public static float getCurrentColorRed() {
        return GlStateManager.colorState.red;
    }

    public static float getCurrentColorGreen() {
        return GlStateManager.colorState.green;
    }

    public static float getCurrentColorBlue() {
        return GlStateManager.colorState.blue;
    }

    public static float getCurrentColorAlpha() {
        return GlStateManager.colorState.alpha;
    }

    public static boolean getColorWriteMaskRed() {
        return GlStateManager.colorMaskState.red;
    }

    public static boolean getColorWriteMaskGreen() {
        return GlStateManager.colorMaskState.green;
    }

    public static boolean getColorWriteMaskBlue() {
        return GlStateManager.colorMaskState.blue;
    }

    public static boolean getColorWriteMaskAlpha() {
        return GlStateManager.colorMaskState.alpha;
    }

    public static boolean isLightingEnabled() {
        return GlStateManager.lightingState.currentState;
    }

    public static boolean isTexture2DEnabled() {
        final int unit = GlStateManager.activeTextureUnit;
        return GlStateManager.textureState[unit].texture2DState.currentState;
    }

    public static boolean isTexture2DEnabled(int unit) {
        if (unit < 0 || unit >= GlStateManager.textureState.length) return false;
        return GlStateManager.textureState[unit].texture2DState.currentState;
    }

    public static int getActiveTextureUnitIndex() {
        return GlStateManager.activeTextureUnit;
    }

    public static int getActiveTextureUnitEnum() {
        return OpenGlHelper.defaultTexUnit + GlStateManager.activeTextureUnit;
    }

    public static int getShadeModel() {
        return GlStateManager.activeShadeModel;
    }

    public static void pushAttrib(int mask) {
        if (mask == GL11.GL_ALL_ATTRIB_BITS) {
            mask = GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_POLYGON_BIT | GL11.GL_FOG_BIT;
        }
        AttribSnapshot s = new AttribSnapshot();
        if ((mask & GL11.GL_ENABLE_BIT) != 0) s.enable = EnableAttrib.capture();
        if ((mask & GL11.GL_LIGHTING_BIT) != 0) s.lighting = LightingAttrib.capture();
        if ((mask & GL11.GL_TEXTURE_BIT) != 0) s.texture = TextureAttrib.capture();
        if ((mask & GL11.GL_COLOR_BUFFER_BIT) != 0) s.color = ColorBufferAttrib.capture();
        if ((mask & GL11.GL_DEPTH_BUFFER_BIT) != 0) s.depth = DepthBufferAttrib.capture();
        if ((mask & GL11.GL_POLYGON_BIT) != 0) s.polygon = PolygonAttrib.capture();
        if ((mask & GL11.GL_FOG_BIT) != 0) s.fog = FogAttrib.capture();
        s.shade = ShadeAttrib.capture();
        ATTRIB_STACK.push(s);
    }

    public static void popAttrib() {
        AttribSnapshot s = ATTRIB_STACK.pollFirst();
        if (s == null) return;
        if (s.texture != null) s.texture.restore();
        if (s.enable != null) s.enable.restore();
        if (s.lighting != null) s.lighting.restore();
        if (s.polygon != null) s.polygon.restore();
        if (s.depth != null) s.depth.restore();
        if (s.fog != null) s.fog.restore();
        if (s.color != null) s.color.restore();
        if (s.shade != null) s.shade.restore();
    }

    public static void pushAllAttribs() {
        pushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    }

    public static void pushGuiBits() {
        pushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_COLOR_BUFFER_BIT);
    }

    public static void popGuiBits() {
        popAttrib();
    }

    public static void clearAttribStack() {
        ATTRIB_STACK.clear();
    }

    /**
     * Verifies that the state tracked by {@link GlStateManager} matches the real OpenGL state. Expensive, don't call frequently.
     *
     * @param message free-form label identifying the call site in any thrown error
     * @throws IllegalStateException if any of the checks fail
     */
    public static void assertTrackedState(String message) {
        StringBuilder diff = new StringBuilder();
        boolean alphaTracked = isAlphaEnabled();
        boolean blendTracked = isBlendEnabled();
        boolean cullTracked = isCullEnabled();
        boolean depthTracked = isDepthEnabled();

        boolean alphaGL = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        boolean blendGL = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cullGL = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean depthGL = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);

        if (alphaTracked != alphaGL) {
            diff.append("\n  alphaTest enabled tracked=").append(alphaTracked).append(" real=").append(alphaGL);
        }
        if (blendTracked != blendGL) {
            diff.append("\n  blend enabled tracked=").append(blendTracked).append(" real=").append(blendGL);
        }
        if (cullTracked != cullGL) {
            diff.append("\n  cull enabled tracked=").append(cullTracked).append(" real=").append(cullGL);
        }
        if (depthTracked != depthGL) {
            diff.append("\n  depthTest enabled tracked=").append(depthTracked).append(" real=").append(depthGL);
        }

        int alphaFuncTracked = getAlphaFunc();
        int alphaFuncGL = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC);
        if (alphaFuncTracked != alphaFuncGL) {
            diff.append("\n  alphaFunc tracked=").append(alphaFuncTracked).append(" real=").append(alphaFuncGL);
        }
        DEBUG_COLOR_BUF.clear();
        GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF, DEBUG_COLOR_BUF);
        float alphaRefGL = DEBUG_COLOR_BUF.get(0);
        float alphaRefTracked = getAlphaRef();
        if (Math.abs(alphaRefTracked - alphaRefGL) > FLOAT_EPS) {
            diff.append("\n  alphaRef tracked=").append(alphaRefTracked).append(" real=").append(alphaRefGL);
        }

        DEBUG_BOOL1_BUF.clear();
        GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK, DEBUG_BOOL1_BUF);
        boolean depthMaskGL = DEBUG_BOOL1_BUF.get(0) != 0;
        boolean depthMaskTracked = isDepthMaskEnabled();
        if (depthMaskTracked != depthMaskGL) {
            diff.append("\n  depthMask tracked=").append(depthMaskTracked).append(" real=").append(depthMaskGL);
        }

        int depthFuncTracked = getDepthFunc();
        int depthFuncGL = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        if (depthFuncTracked != depthFuncGL) {
            diff.append("\n  depthFunc tracked=").append(depthFuncTracked).append(" real=").append(depthFuncGL);
        }

        DEBUG_COLOR_BUF.clear();
        GL11.glGetFloat(GL11.GL_DEPTH_CLEAR_VALUE, DEBUG_COLOR_BUF);
        float clearDepthGL = DEBUG_COLOR_BUF.get(0);
        double clearDepthTracked = GlStateManager.clearState.depth;
        if (Math.abs(clearDepthTracked - clearDepthGL) > FLOAT_EPS) {
            diff.append("\n  clearDepth tracked=").append(clearDepthTracked).append(" real=").append(clearDepthGL);
        }

        DEBUG_BOOL4_BUF.clear();
        GL11.glGetBoolean(GL11.GL_COLOR_WRITEMASK, DEBUG_BOOL4_BUF);
        boolean maskRgl = DEBUG_BOOL4_BUF.get(0) != 0;
        boolean maskGgl = DEBUG_BOOL4_BUF.get(1) != 0;
        boolean maskBgl = DEBUG_BOOL4_BUF.get(2) != 0;
        boolean maskAgl = DEBUG_BOOL4_BUF.get(3) != 0;

        if (getColorWriteMaskRed() != maskRgl) {
            diff.append("\n  colorMask.red tracked=").append(getColorWriteMaskRed()).append(" real=").append(maskRgl);
        }
        if (getColorWriteMaskGreen() != maskGgl) {
            diff.append("\n  colorMask.green tracked=").append(getColorWriteMaskGreen()).append(" real=").append(maskGgl);
        }
        if (getColorWriteMaskBlue() != maskBgl) {
            diff.append("\n  colorMask.blue tracked=").append(getColorWriteMaskBlue()).append(" real=").append(maskBgl);
        }
        if (getColorWriteMaskAlpha() != maskAgl) {
            diff.append("\n  colorMask.alpha tracked=").append(getColorWriteMaskAlpha()).append(" real=").append(maskAgl);
        }

        DEBUG_COLOR_BUF.clear();
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, DEBUG_COLOR_BUF);
        float rGL = DEBUG_COLOR_BUF.get(0);
        float gGL = DEBUG_COLOR_BUF.get(1);
        float bGL = DEBUG_COLOR_BUF.get(2);
        float aGL = DEBUG_COLOR_BUF.get(3);

        float rTracked = getCurrentColorRed();
        float gTracked = getCurrentColorGreen();
        float bTracked = getCurrentColorBlue();
        float aTracked = getCurrentColorAlpha();

        if (Math.abs(rTracked - rGL) > FLOAT_EPS || Math.abs(gTracked - gGL) > FLOAT_EPS || Math.abs(bTracked - bGL) > FLOAT_EPS || Math.abs(aTracked - aGL) > FLOAT_EPS) {
            diff.append("\n  color tracked=(").append(rTracked).append(',').append(gTracked).append(',').append(bTracked).append(',').append(aTracked)
                .append(") real=(").append(rGL).append(',').append(gGL).append(',').append(bGL).append(',').append(aGL).append(')');
        }
        int srcRgbTracked = getBlendSrcFactor();
        int dstRgbTracked = getBlendDstFactor();
        int srcAlphaTracked = getBlendSrcAlphaFactor();
        int dstAlphaTracked = getBlendDstAlphaFactor();

        int srcRgbGL = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        int dstRgbGL = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int srcAlphaGL = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        int dstAlphaGL = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);

        if (srcRgbTracked != srcRgbGL || dstRgbTracked != dstRgbGL || srcAlphaTracked != srcAlphaGL || dstAlphaTracked != dstAlphaGL) {
            diff.append("\n  blendFunc tracked=(").append(srcRgbTracked).append(',').append(dstRgbTracked).append(" | ").append(srcAlphaTracked)
                .append(',').append(dstAlphaTracked).append(") real=(").append(srcRgbGL).append(',').append(dstRgbGL).append(" | ").append(srcAlphaGL)
                .append(',').append(dstAlphaGL).append(')');
        }

        boolean colorLogicTracked = GlStateManager.colorLogicState.colorLogicOp.currentState;
        boolean colorLogicGL = GL11.glIsEnabled(GL11.GL_COLOR_LOGIC_OP);
        if (colorLogicTracked != colorLogicGL) {
            diff.append("\n  colorLogicOp enabled tracked=").append(colorLogicTracked).append(" real=").append(colorLogicGL);
        }
        int logicOpTracked = GlStateManager.colorLogicState.opcode;
        int logicOpGL = GL11.glGetInteger(GL11.GL_LOGIC_OP_MODE);
        if (logicOpTracked != logicOpGL) {
            diff.append("\n  colorLogicOp opcode tracked=").append(logicOpTracked).append(" real=").append(logicOpGL);
        }

        DEBUG_COLOR_BUF.clear();
        GL11.glGetFloat(GL11.GL_COLOR_CLEAR_VALUE, DEBUG_COLOR_BUF);
        float clearRgl = DEBUG_COLOR_BUF.get(0);
        float clearGgl = DEBUG_COLOR_BUF.get(1);
        float clearBgl = DEBUG_COLOR_BUF.get(2);
        float clearAgl = DEBUG_COLOR_BUF.get(3);

        float clearRTracked = GlStateManager.clearState.color.red;
        float clearGTracked = GlStateManager.clearState.color.green;
        float clearBTracked = GlStateManager.clearState.color.blue;
        float clearATracked = GlStateManager.clearState.color.alpha;

        if (Math.abs(clearRTracked - clearRgl) > FLOAT_EPS || Math.abs(clearGTracked - clearGgl) > FLOAT_EPS || Math.abs(clearBTracked - clearBgl) > FLOAT_EPS || Math.abs(clearATracked - clearAgl) > FLOAT_EPS) {
            diff.append("\n  clearColor tracked=(").append(clearRTracked).append(',').append(clearGTracked).append(',').append(clearBTracked)
                .append(',').append(clearATracked).append(") real=(").append(clearRgl).append(',').append(clearGgl).append(',').append(clearBgl)
                .append(',').append(clearAgl).append(')');
        }

        int activeTracked = getActiveTextureUnitIndex();
        int activeEnumGL = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        int activeIndexGL = activeEnumGL - GL13.GL_TEXTURE0;

        if (activeTracked != activeIndexGL) {
            diff.append("\n  activeTexture tracked=").append(activeTracked).append(" real=").append(activeIndexGL);
        }

        int units = GlStateManager.textureState.length;

        for (int i = 0; i < units; i++) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
            boolean tex2Dgl = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
            boolean tex2DTracked = isTexture2DEnabled(i);

            if (tex2DTracked != tex2Dgl) {
                diff.append("\n  texture2D unit ").append(i).append(" enabled tracked=").append(tex2DTracked).append(" real=").append(tex2Dgl);
            }

            int boundGL = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            int boundTracked = GlStateManager.textureState[i].textureName;
            if (boundTracked != boundGL) {
                diff.append("\n  textureBinding2D unit ").append(i).append(" tracked=").append(boundTracked).append(" real=").append(boundGL);
            }
        }
        GL13.glActiveTexture(activeEnumGL);
        int shadeTracked = getShadeModel();
        int shadeGL = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
        if (shadeTracked != shadeGL) {
            diff.append("\n  shadeModel tracked=").append(shadeTracked).append(" real=").append(shadeGL);
        }

        boolean lightingTracked = isLightingEnabled();
        boolean lightingGL = GL11.glIsEnabled(GL11.GL_LIGHTING);
        if (lightingTracked != lightingGL) {
            diff.append("\n  lighting enabled tracked=").append(lightingTracked).append(" real=").append(lightingGL);
        }

        for (int i = 0; i < GlStateManager.lightState.length; i++) {
            boolean lightTracked = GlStateManager.lightState[i].currentState;
            boolean lightGL = GL11.glIsEnabled(GL11.GL_LIGHT0 + i);
            if (lightTracked != lightGL) {
                diff.append("\n  light[").append(i).append("] enabled tracked=").append(lightTracked).append(" real=").append(lightGL);
            }
        }

        boolean colorMaterialTracked = GlStateManager.colorMaterialState.colorMaterial.currentState;
        boolean colorMaterialGL = GL11.glIsEnabled(GL11.GL_COLOR_MATERIAL);
        if (colorMaterialTracked != colorMaterialGL) {
            diff.append("\n  colorMaterial enabled tracked=").append(colorMaterialTracked).append(" real=").append(colorMaterialGL);
        }
        int colorMaterialFaceTracked = GlStateManager.colorMaterialState.face;
        int colorMaterialFaceGL = GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_FACE);
        if (colorMaterialFaceTracked != colorMaterialFaceGL) {
            diff.append("\n  colorMaterial face tracked=").append(colorMaterialFaceTracked).append(" real=").append(colorMaterialFaceGL);
        }
        int colorMaterialModeTracked = GlStateManager.colorMaterialState.mode;
        int colorMaterialModeGL = GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_PARAMETER);
        if (colorMaterialModeTracked != colorMaterialModeGL) {
            diff.append("\n  colorMaterial mode tracked=").append(colorMaterialModeTracked).append(" real=").append(colorMaterialModeGL);
        }

        boolean normalizeTracked = GlStateManager.normalizeState.currentState;
        boolean normalizeGL = GL11.glIsEnabled(GL11.GL_NORMALIZE);
        if (normalizeTracked != normalizeGL) {
            diff.append("\n  normalize enabled tracked=").append(normalizeTracked).append(" real=").append(normalizeGL);
        }

        boolean rescaleTracked = GlStateManager.rescaleNormalState.currentState;
        boolean rescaleGL = GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);
        if (rescaleTracked != rescaleGL) {
            diff.append("\n  rescaleNormal enabled tracked=").append(rescaleTracked).append(" real=").append(rescaleGL);
        }

        int cullModeTracked = GlStateManager.cullState.mode;
        int cullModeGL = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);
        if (cullModeTracked != cullModeGL) {
            diff.append("\n  cullFace mode tracked=").append(cullModeTracked).append(" real=").append(cullModeGL);
        }

        boolean polyOffsetFillTracked = GlStateManager.polygonOffsetState.polygonOffsetFill.currentState;
        boolean polyOffsetFillGL = GL11.glIsEnabled(GL11.GL_POLYGON_OFFSET_FILL);
        if (polyOffsetFillTracked != polyOffsetFillGL) {
            diff.append("\n  polygonOffsetFill enabled tracked=").append(polyOffsetFillTracked).append(" real=").append(polyOffsetFillGL);
        }

        boolean polyOffsetLineTracked = GlStateManager.polygonOffsetState.polygonOffsetLine.currentState;
        boolean polyOffsetLineGL = GL11.glIsEnabled(GL11.GL_POLYGON_OFFSET_LINE);
        if (polyOffsetLineTracked != polyOffsetLineGL) {
            diff.append("\n  polygonOffsetLine enabled tracked=").append(polyOffsetLineTracked).append(" real=").append(polyOffsetLineGL);
        }

        DEBUG_COLOR_BUF.clear();
        GL11.glGetFloat(GL11.GL_POLYGON_OFFSET_FACTOR, DEBUG_COLOR_BUF);
        float polyFactorGL = DEBUG_COLOR_BUF.get(0);
        float polyFactorTracked = GlStateManager.polygonOffsetState.factor;
        if (Math.abs(polyFactorTracked - polyFactorGL) > FLOAT_EPS) {
            diff.append("\n  polygonOffset factor tracked=").append(polyFactorTracked).append(" real=").append(polyFactorGL);
        }

        DEBUG_COLOR_BUF.clear();
        GL11.glGetFloat(GL11.GL_POLYGON_OFFSET_UNITS, DEBUG_COLOR_BUF);
        float polyUnitsGL = DEBUG_COLOR_BUF.get(0);
        float polyUnitsTracked = GlStateManager.polygonOffsetState.units;
        if (Math.abs(polyUnitsTracked - polyUnitsGL) > FLOAT_EPS) {
            diff.append("\n  polygonOffset units tracked=").append(polyUnitsTracked).append(" real=").append(polyUnitsGL);
        }

        boolean fogTracked = GlStateManager.fogState.fog.currentState;
        boolean fogGL = GL11.glIsEnabled(GL11.GL_FOG);
        if (fogTracked != fogGL) {
            diff.append("\n  fog enabled tracked=").append(fogTracked).append(" real=").append(fogGL);
        }

        int fogModeTracked = GlStateManager.fogState.mode;
        int fogModeGL = GL11.glGetInteger(GL11.GL_FOG_MODE);
        if (fogModeTracked != fogModeGL) {
            diff.append("\n  fog mode tracked=").append(fogModeTracked).append(" real=").append(fogModeGL);
        }

        DEBUG_COLOR_BUF.clear();
        GL11.glGetFloat(GL11.GL_FOG_DENSITY, DEBUG_COLOR_BUF);
        float fogDensityGL = DEBUG_COLOR_BUF.get(0);
        float fogDensityTracked = GlStateManager.fogState.density;
        if (Math.abs(fogDensityTracked - fogDensityGL) > FLOAT_EPS) {
            diff.append("\n  fog density tracked=").append(fogDensityTracked).append(" real=").append(fogDensityGL);
        }

        DEBUG_COLOR_BUF.clear();
        GL11.glGetFloat(GL11.GL_FOG_START, DEBUG_COLOR_BUF);
        float fogStartGL = DEBUG_COLOR_BUF.get(0);
        float fogStartTracked = GlStateManager.fogState.start;
        if (Math.abs(fogStartTracked - fogStartGL) > FLOAT_EPS) {
            diff.append("\n  fog start tracked=").append(fogStartTracked).append(" real=").append(fogStartGL);
        }

        DEBUG_COLOR_BUF.clear();
        GL11.glGetFloat(GL11.GL_FOG_END, DEBUG_COLOR_BUF);
        float fogEndGL = DEBUG_COLOR_BUF.get(0);
        float fogEndTracked = GlStateManager.fogState.end;
        if (Math.abs(fogEndTracked - fogEndGL) > FLOAT_EPS) {
            diff.append("\n  fog end tracked=").append(fogEndTracked).append(" real=").append(fogEndGL);
        }

        if (diff.length() > 0) {
            String location = (message == null || message.isEmpty()) ? "" : (" at " + message);
            throw new IllegalStateException("RenderUtil/GlStateManager state mismatch with real GL state" + location + ":" + diff);
        }
    }

    private static GlStateManager.FogMode fogModeFromId(int id) {
        for (GlStateManager.FogMode m : GlStateManager.FogMode.values()) {
            if (m.capabilityId == id) return m;
        }
        return null;
    }

    public record EnableAttrib(boolean alphaTest, boolean blend, boolean cull, boolean depthTest, boolean fog, boolean lighting, boolean normalize,
                               boolean rescaleNormal, boolean colorMaterial, boolean polygonOffsetFill, boolean polygonOffsetLine,
                               boolean colorLogicOp) {
        static EnableAttrib capture() {
            return new EnableAttrib(GlStateManager.alphaState.alphaTest.currentState, GlStateManager.blendState.blend.currentState, GlStateManager.cullState.cullFace.currentState, GlStateManager.depthState.depthTest.currentState, GlStateManager.fogState.fog.currentState, GlStateManager.lightingState.currentState, GlStateManager.normalizeState.currentState, GlStateManager.rescaleNormalState.currentState, GlStateManager.colorMaterialState.colorMaterial.currentState, GlStateManager.polygonOffsetState.polygonOffsetFill.currentState, GlStateManager.polygonOffsetState.polygonOffsetLine.currentState, GlStateManager.colorLogicState.colorLogicOp.currentState);
        }

        void restore() {
            if (alphaTest) GlStateManager.enableAlpha();
            else GlStateManager.disableAlpha();
            if (blend) GlStateManager.enableBlend();
            else GlStateManager.disableBlend();
            if (cull) GlStateManager.enableCull();
            else GlStateManager.disableCull();
            if (depthTest) GlStateManager.enableDepth();
            else GlStateManager.disableDepth();
            if (fog) GlStateManager.enableFog();
            else GlStateManager.disableFog();
            if (lighting) GlStateManager.enableLighting();
            else GlStateManager.disableLighting();
            if (normalize) GlStateManager.enableNormalize();
            else GlStateManager.disableNormalize();
            if (rescaleNormal) GlStateManager.enableRescaleNormal();
            else GlStateManager.disableRescaleNormal();
            if (colorMaterial) GlStateManager.enableColorMaterial();
            else GlStateManager.disableColorMaterial();
            GlStateManager.polygonOffsetState.polygonOffsetFill.setState(polygonOffsetFill);
            GlStateManager.polygonOffsetState.polygonOffsetLine.setState(polygonOffsetLine);
            if (colorLogicOp) GlStateManager.enableColorLogic();
            else GlStateManager.disableColorLogic();
        }
    }

    public record LightingAttrib(boolean lightingEnabled, boolean[] lightEnabled, // length 8
                                 boolean colorMaterialEnabled, int colorMaterialFace, int colorMaterialMode) {
        static LightingAttrib capture() {
            boolean[] lights = new boolean[GlStateManager.lightState.length];
            for (int i = 0; i < lights.length; i++) {
                lights[i] = GlStateManager.lightState[i].currentState;
            }
            return new LightingAttrib(GlStateManager.lightingState.currentState, lights, GlStateManager.colorMaterialState.colorMaterial.currentState, GlStateManager.colorMaterialState.face, GlStateManager.colorMaterialState.mode);
        }

        void restore() {
            if (lightingEnabled) GlStateManager.enableLighting();
            else GlStateManager.disableLighting();
            for (int i = 0; i < lightEnabled.length && i < GlStateManager.lightState.length; i++) {
                GlStateManager.lightState[i].setState(lightEnabled[i]);
            }
            GlStateManager.colorMaterial(colorMaterialFace, colorMaterialMode);
            GlStateManager.colorMaterialState.colorMaterial.setState(colorMaterialEnabled);
        }
    }

    public record TextureAttrib(int activeUnit, boolean[] tex2DEnabled, int[] boundTex2D) {
        static TextureAttrib capture() {
            int n = GlStateManager.textureState.length;
            boolean[] e = new boolean[n];
            int[] names = new int[n];
            for (int i = 0; i < n; i++) {
                e[i] = GlStateManager.textureState[i].texture2DState.currentState;
                names[i] = GlStateManager.textureState[i].textureName;
            }
            return new TextureAttrib(GlStateManager.activeTextureUnit, e, names);
        }

        void restore() {
            int n = Math.min(tex2DEnabled.length, GlStateManager.textureState.length);
            for (int i = 0; i < n; i++) {
                GlStateManager.setActiveTexture(GL13.GL_TEXTURE0 + i);
                GlStateManager.textureState[i].texture2DState.setState(tex2DEnabled[i]);
                GlStateManager.bindTexture(boundTex2D[i]);
            }
            GlStateManager.setActiveTexture(GL13.GL_TEXTURE0 + activeUnit);
        }
    }

    public record ColorBufferAttrib(float r, float g, float b, float a, boolean maskR, boolean maskG, boolean maskB, boolean maskA,
                                    boolean blendEnabled, int srcRGB, int dstRGB, int srcA, int dstA, boolean colorLogicEnabled, int colorLogicOpcode,
                                    float clearR, float clearG, float clearB, float clearA) {
        static ColorBufferAttrib capture() {
            return new ColorBufferAttrib(GlStateManager.colorState.red, GlStateManager.colorState.green, GlStateManager.colorState.blue, GlStateManager.colorState.alpha, GlStateManager.colorMaskState.red, GlStateManager.colorMaskState.green, GlStateManager.colorMaskState.blue, GlStateManager.colorMaskState.alpha, GlStateManager.blendState.blend.currentState, GlStateManager.blendState.srcFactor, GlStateManager.blendState.dstFactor, GlStateManager.blendState.srcFactorAlpha, GlStateManager.blendState.dstFactorAlpha, GlStateManager.colorLogicState.colorLogicOp.currentState, GlStateManager.colorLogicState.opcode, GlStateManager.clearState.color.red, GlStateManager.clearState.color.green, GlStateManager.clearState.color.blue, GlStateManager.clearState.color.alpha);
        }

        void restore() {
            GlStateManager.color(r, g, b, a);
            GlStateManager.colorMask(maskR, maskG, maskB, maskA);
            if (blendEnabled) GlStateManager.enableBlend();
            else GlStateManager.disableBlend();
            GlStateManager.tryBlendFuncSeparate(srcRGB, dstRGB, srcA, dstA);
            if (colorLogicEnabled) GlStateManager.enableColorLogic();
            else GlStateManager.disableColorLogic();
            GlStateManager.colorLogicOp(colorLogicOpcode);
            GlStateManager.clearColor(clearR, clearG, clearB, clearA);
        }
    }

    public record DepthBufferAttrib(boolean depthEnabled, int depthFunc, boolean maskEnabled, double clearDepth) {
        static DepthBufferAttrib capture() {
            return new DepthBufferAttrib(GlStateManager.depthState.depthTest.currentState, GlStateManager.depthState.depthFunc, GlStateManager.depthState.maskEnabled, GlStateManager.clearState.depth);
        }

        void restore() {
            if (depthEnabled) GlStateManager.enableDepth();
            else GlStateManager.disableDepth();
            GlStateManager.depthFunc(depthFunc);
            GlStateManager.depthMask(maskEnabled);
            GlStateManager.clearDepth(clearDepth);
        }
    }

    public record PolygonAttrib(boolean cullEnabled, int cullMode, boolean polyOffsetFill, boolean polyOffsetLine, float polyOffsetFactor,
                                float polyOffsetUnits) {
        static PolygonAttrib capture() {
            return new PolygonAttrib(GlStateManager.cullState.cullFace.currentState, GlStateManager.cullState.mode, GlStateManager.polygonOffsetState.polygonOffsetFill.currentState, GlStateManager.polygonOffsetState.polygonOffsetLine.currentState, GlStateManager.polygonOffsetState.factor, GlStateManager.polygonOffsetState.units);
        }

        void restore() {
            if (cullEnabled) GlStateManager.enableCull();
            else GlStateManager.disableCull();
            GlStateManager.cullState.mode = cullMode;
            GL11.glCullFace(cullMode);
            GlStateManager.polygonOffsetState.polygonOffsetFill.setState(polyOffsetFill);
            GlStateManager.polygonOffsetState.polygonOffsetLine.setState(polyOffsetLine);
            GlStateManager.doPolygonOffset(polyOffsetFactor, polyOffsetUnits);
        }
    }

    public record FogAttrib(boolean fogEnabled, int mode, float density, float start, float end) {
        static FogAttrib capture() {
            return new FogAttrib(GlStateManager.fogState.fog.currentState, GlStateManager.fogState.mode, GlStateManager.fogState.density, GlStateManager.fogState.start, GlStateManager.fogState.end);
        }

        void restore() {
            if (fogEnabled) GlStateManager.enableFog();
            else GlStateManager.disableFog();
            GlStateManager.FogMode fm = fogModeFromId(mode);
            if (fm != null) {
                GlStateManager.setFog(fm);
            } else {
                GlStateManager.fogState.mode = mode;
                GL11.glFogi(GL11.GL_FOG_MODE, mode);
            }
            GlStateManager.setFogDensity(density);
            GlStateManager.setFogStart(start);
            GlStateManager.setFogEnd(end);
        }
    }

    public record ShadeAttrib(int shadeModel) {
        static ShadeAttrib capture() {
            return new ShadeAttrib(GlStateManager.activeShadeModel);
        }

        void restore() {
            GlStateManager.shadeModel(shadeModel);
        }
    }

    public static final class AttribSnapshot {
        EnableAttrib enable;
        LightingAttrib lighting;
        TextureAttrib texture;
        ColorBufferAttrib color;
        DepthBufferAttrib depth;
        PolygonAttrib polygon;
        FogAttrib fog;
        ShadeAttrib shade;

        @Override
        public String toString() {
            return "AttribSnapshot{" + "enable=" + (enable != null) + ", lighting=" + (lighting != null) + ", texture=" + (texture != null ? ("units=" + texture.tex2DEnabled.length + ", active=" + texture.activeUnit) : "false") + ", color=" + (color != null) + ", depth=" + (depth != null) + ", polygon=" + (polygon != null) + ", fog=" + (fog != null) + ", shade=" + (shade != null ? shade.shadeModel : "false") + '}';
        }
    }
}
