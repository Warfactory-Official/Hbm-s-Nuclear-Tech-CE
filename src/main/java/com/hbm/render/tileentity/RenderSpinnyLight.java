package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.GLCompat;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.BakedModelTransforms;
import com.hbm.tileentity.deco.TileEntitySpinnyLight;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

@AutoRegister
public class RenderSpinnyLight extends TileEntitySpecialRenderer<TileEntitySpinnyLight> implements IItemRendererProvider {

    private static final EnumDyeColor[] DYE_COLORS = EnumDyeColor.values();
    private static final int CONE_STRIDE = 7 * Float.BYTES;
    private static ConeMesh[] coneMeshes = null;

    private static ConeMesh generateConeMesh(float length, float radius, int sides, float r, float g, float b) {
        float oX = 0F;
        float oY = 0.1708F;
        float oZ = 0F;

        float[] vertices = new float[(1 + sides) * 3];
        vertices[0] = oX;
        vertices[1] = oY;
        vertices[2] = oZ;

        Vec3 vertex = new Vec3(0, radius, 0);
        for (int i = 0; i < sides; i++) {
            vertex.rotateAroundX((float) (2 * Math.PI * (1F / (float) sides)));
            vertices[(i + 1) * 3] = (float) vertex.xCoord + oX + length;
            vertices[(i + 1) * 3 + 1] = (float) vertex.yCoord + oY;
            vertices[(i + 1) * 3 + 2] = (float) vertex.zCoord + oZ;
        }

        int triangleCount = 2 * sides;
        float[] meshData = new float[triangleCount * 3 * 7];
        int dataIndex = 0;
        float alpha = 0.65F;
        for (int m = -1; m <= 1; m += 2) {
            for (int i = 2; i <= sides; i++) {
                dataIndex = putVertex(meshData, dataIndex, vertices[0], vertices[1], vertices[2], r, g, b, alpha);
                dataIndex = putVertex(meshData, dataIndex, vertices[(i - 1) * 3] * m, vertices[(i - 1) * 3 + 1],
                        vertices[(i - 1) * 3 + 2], r, g, b, 0);
                dataIndex = putVertex(meshData, dataIndex, vertices[i * 3] * m, vertices[i * 3 + 1],
                        vertices[i * 3 + 2], r, g, b, 0);
            }
            dataIndex = putVertex(meshData, dataIndex, vertices[0], vertices[1], vertices[2], r, g, b, alpha);
            dataIndex = putVertex(meshData, dataIndex, vertices[sides * 3] * m, vertices[sides * 3 + 1],
                    vertices[sides * 3 + 2], r, g, b, 0);
            dataIndex = putVertex(meshData, dataIndex, vertices[3] * m, vertices[4], vertices[5], r, g, b, 0);
        }

        ByteBuffer buffer = BufferUtils.createByteBuffer(meshData.length * Float.BYTES);
        buffer.asFloatBuffer().put(meshData);
        return new ConeMesh(buffer, meshData.length / 7);
    }

    private static int putVertex(float[] meshData, int index, float x, float y, float z, float r, float g, float b,
                                 float a) {
        meshData[index++] = x;
        meshData[index++] = y;
        meshData[index++] = z;
        meshData[index++] = r;
        meshData[index++] = g;
        meshData[index++] = b;
        meshData[index++] = a;
        return index;
    }
    @Override
    public void render(TileEntitySpinnyLight te, double x, double y, double z, float partialTicks, int destroyStage,
                       float alpha) {
        if (coneMeshes == null) {
            coneMeshes = new ConeMesh[DYE_COLORS.length];
            for (int i = 0; i < coneMeshes.length; i++) {
                EnumDyeColor dyeColor = DYE_COLORS[i];
                float[] color = dyeColor.getColorComponentValues();
                if (dyeColor == EnumDyeColor.RED) {
                    color = new float[]{1.0F, 0.0F, 0.0F};
                }
                coneMeshes[i] = generateConeMesh(5.0F, 3.0F, 12, color[0], color[1], color[2]);
            }
        }
        boolean powered = (te.getBlockMetadata() & 8) > 0;
        float time = powered ? (te.getWorld().getTotalWorldTime() - te.timeAdded) % 10000 + partialTicks : 0;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        switch (te.getBlockMetadata() & 7) {
            case 0:
                GL11.glRotated(180, 1, 0, 0);
                break;
            case 1:
                break;
            case 2:
                GL11.glRotated(180, 0, 1, 0);
                GL11.glRotated(90, 1, 0, 0);
                break;
            case 3:
                GL11.glRotated(90, 1, 0, 0);
                break;
            case 4:
                GL11.glRotated(270, 0, 1, 0);
                GL11.glRotated(90, 1, 0, 0);
                break;
            case 5:
                GL11.glRotated(90, 0, 1, 0);
                GL11.glRotated(90, 1, 0, 0);
                break;
        }
        GlStateManager.translate(0, -0.5, 0);
        GlStateManager.pushMatrix();
        GL11.glRotated((time * 7) % 360, 0, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.spinny_light_tex);
        if (powered) OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        ResourceManager.spinny_light.renderPart("light");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        ResourceManager.spinny_light.renderPart("base");
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, powered ? DestFactor.ONE : DestFactor.ONE_MINUS_SRC_ALPHA);
        float[] color = te.color.getColorComponentValues();
        if (te.color == EnumDyeColor.RED) {
            color = new float[]{1.0F, 0.0F, 0.0F};
        }
        GlStateManager.color(color[0], color[1], color[2], 0.61F);
        ResourceManager.spinny_light.renderPart("dome");
        GlStateManager.popMatrix();

        if (powered) {
            GlStateManager.pushMatrix();
            GL11.glRotated((time * 7) % 360, 0, 1, 0);
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
            GlStateManager.depthMask(false);
            GlStateManager.disableCull();
            coneMeshes[te.color.ordinal()].render();
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.enableCull();
            GlStateManager.depthMask(true);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.spinny_light);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            @Override
            protected ItemCameraTransforms getBindingTransforms(Item item) {
                return BakedModelTransforms.standardBlock();
            }

            @Override
            public void renderInventory() {
                GlStateManager.scale(10.0 , 10.0, 10.0);
                GlStateManager.translate(12.0, -1.9, 0.0);
                GlStateManager.rotate(21.0F, 1.0F, 0.0F, 0.0F);
            }

            @Override
            public void renderCommon() {
                bindTexture(ResourceManager.spinny_light_tex);
                GlStateManager.translate(1.25, -1.25, -1.25);
                GlStateManager.scale(5, 5, 5);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                ResourceManager.spinny_light.renderAll();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }

    private static final class ConeMesh {
        private final int vboHandle;
        private final int vaoHandle;
        private final int vertexCount;

        private ConeMesh(ByteBuffer data, int vertexCount) {
            this.vertexCount = vertexCount;
            this.vboHandle = GLCompat.genBuffers();
            GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, vboHandle);
            GLCompat.bufferData(GLCompat.GL_ARRAY_BUFFER, data, GLCompat.GL_STATIC_DRAW);
            GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, 0);

            this.vaoHandle = GLCompat.genVertexArrays();
            GLCompat.bindVertexArray(vaoHandle);
            GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, vboHandle);
            GL11.glVertexPointer(3, GL11.GL_FLOAT, CONE_STRIDE, 0L);
            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glColorPointer(4, GL11.GL_FLOAT, CONE_STRIDE, 3L * Float.BYTES);
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
            GLCompat.bindVertexArray(0);
            GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, 0);
        }

        private void render() {
            GLCompat.bindVertexArray(vaoHandle);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
            GLCompat.bindVertexArray(0);
        }
    }
}
