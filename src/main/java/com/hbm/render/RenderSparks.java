package com.hbm.render;

import com.hbm.util.Vec3NT;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class RenderSparks {

    public static void renderSpark(int seed, double x, double y, double z, float length, int min, int max, int color1, int color2) {
        float r1 = (color1 >> 16 & 255) / 255F;
        float g1 = (color1 >> 8 & 255) / 255F;
        float b1 = (color1 & 255) / 255F;

        float r2 = (color2 >> 16 & 255) / 255F;
        float g2 = (color2 >> 8 & 255) / 255F;
        float b2 = (color2 & 255) / 255F;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.glLineWidth(3F);


        Random rand = new Random(seed);
        Vec3NT vec = new Vec3NT(rand.nextDouble() - 0.5, rand.nextDouble() - 0.5, rand.nextDouble() - 0.5);
        vec = vec.normalize();

        double prevX;
        double prevY;
        double prevZ;

        for (int i = 0; i < min + rand.nextInt(max); i++) {

            prevX = x;
            prevY = y;
            prevZ = z;

            Vec3NT dir = vec.normalize();
            dir.multiply(length * rand.nextFloat());

            x = prevX + dir.x;
            y = prevY + dir.y;
            z = prevZ + dir.z;

            GlStateManager.glLineWidth(5F);
            GL11.glBegin(3);
            GlStateManager.color(r1, g1, b1, 1.0F);
            GL11.glVertex3d(prevX, prevY, prevZ);
            GL11.glVertex3d(x, y, z);
            GL11.glEnd();
            GlStateManager.glLineWidth(2F);
            GL11.glBegin(3);
            GlStateManager.color(r2, g2, b2, 1.0F);
            GL11.glVertex3d(prevX, prevY, prevZ);
            GL11.glVertex3d(x, y, z);
            GL11.glEnd();


        }

        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}


