package com.hbm.render.util;

import com.hbm.util.Vec3NT;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

import java.util.Random;

public class BeamPronter {

    private static boolean depthMask = false;

    public static void prontBeamwithDepth(Vec3d skeleton, EnumWaveType wave, EnumBeamType beam, int outerColor, int innerColor, int start, int segments, float size, int layers, float thickness) {
        depthMask = true;
        prontBeam(skeleton, wave, beam, outerColor, innerColor, start, segments, size, layers, thickness);
        depthMask = false;
    }

    public static void prontBeam(Vec3d skeleton, EnumWaveType wave, EnumBeamType beam, int outerColor, int innerColor, int start, int segments, float size, int layers, float thickness) {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(depthMask);

        float sYaw = (float) (Math.atan2(skeleton.x, skeleton.z) * 180F / Math.PI);
        float sqrt = MathHelper.sqrt(skeleton.x * skeleton.x + skeleton.z * skeleton.z);
        float sPitch = (float) (Math.atan2(skeleton.y, sqrt) * 180F / Math.PI);

        GlStateManager.rotate(180, 0, 1F, 0);
        GlStateManager.rotate(sYaw, 0, 1F, 0);
        GlStateManager.rotate(sPitch - 90, 1F, 0, 0);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();

        if (beam == EnumBeamType.SOLID) {
            GlStateManager.disableCull();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        Vec3NT unit = new Vec3NT(0, 1, 0);
        Random rand = new Random(start);
        double length = skeleton.length();
        double segLength = length / segments;
        double lastX = 0;
        double lastY = 0;
        double lastZ = 0;

        for (int i = 0; i <= segments; i++) {
            Vec3NT spinner = new Vec3NT(size, 0, 0);

            if (wave == EnumWaveType.SPIRAL) {
                spinner.rotateAroundYRad((float) Math.PI * (float) start / 180F);
                spinner.rotateAroundYRad((float) Math.PI * 45F / 180F * i);
            } else if (wave == EnumWaveType.RANDOM) {
                spinner.rotateAroundYRad((float) Math.PI * 2 * rand.nextFloat());
                spinner.rotateAroundYRad((float) Math.PI * 2 * rand.nextFloat());
            }

            double pX = unit.x * segLength * i + spinner.x;
            double pY = unit.y * segLength * i + spinner.y;
            double pZ = unit.z * segLength * i + spinner.z;

            if (beam == EnumBeamType.LINE && i > 0) {
                buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                addVertex(buffer, pX, pY, pZ, outerColor);
                addVertex(buffer, lastX, lastY, lastZ, outerColor);
                tessellator.draw();
            }

            if (beam == EnumBeamType.SOLID && i > 0) {
                float radius = thickness / layers;

                for (int j = 1; j <= layers; j++) {
                    float inter = (float) (j - 1) / (float) (layers - 1);

                    int r1 = (outerColor >> 16) & 0xFF;
                    int g1 = (outerColor >> 8) & 0xFF;
                    int b1 = outerColor & 0xFF;

                    int r2 = (innerColor >> 16) & 0xFF;
                    int g2 = (innerColor >> 8) & 0xFF;
                    int b2 = innerColor & 0xFF;

                    int r = (int) (r1 + (r2 - r1) * inter);
                    int g = (int) (g1 + (g2 - g1) * inter);
                    int b = (int) (b1 + (b2 - b1) * inter);

                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

                    // Quad 1
                    buffer.pos(lastX + radius * j, lastY, lastZ + radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(lastX + radius * j, lastY, lastZ - radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(pX + radius * j, pY, pZ - radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(pX + radius * j, pY, pZ + radius * j).color(r, g, b, 255).endVertex();

                    // Quad 2
                    buffer.pos(lastX - radius * j, lastY, lastZ + radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(lastX - radius * j, lastY, lastZ - radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(pX - radius * j, pY, pZ - radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(pX - radius * j, pY, pZ + radius * j).color(r, g, b, 255).endVertex();

                    // Quad 3
                    buffer.pos(lastX + radius * j, lastY, lastZ + radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(lastX - radius * j, lastY, lastZ + radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(pX - radius * j, pY, pZ + radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(pX + radius * j, pY, pZ + radius * j).color(r, g, b, 255).endVertex();

                    // Quad 4
                    buffer.pos(lastX + radius * j, lastY, lastZ - radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(lastX - radius * j, lastY, lastZ - radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(pX - radius * j, pY, pZ - radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(pX + radius * j, pY, pZ - radius * j).color(r, g, b, 255).endVertex();

                    tessellator.draw();
                }
            }

            lastX = pX;
            lastY = pY;
            lastZ = pZ;
        }

        if (beam == EnumBeamType.LINE) {
            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            addVertex(buffer, 0, 0, 0, innerColor);
            addVertex(buffer, 0, skeleton.length(), 0, innerColor);
            tessellator.draw();
        }

        if (beam == EnumBeamType.SOLID) {
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableBlend();
            GlStateManager.enableCull();
        }

        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    private static void addVertex(BufferBuilder buffer, double x, double y, double z, int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        buffer.pos(x, y, z).color(r, g, b, 255).endVertex();
    }

    public enum EnumWaveType {
        RANDOM, SPIRAL
    }

    public enum EnumBeamType {
        SOLID, LINE
    }


}
