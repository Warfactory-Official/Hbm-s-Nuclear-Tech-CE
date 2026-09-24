package com.hbm.util;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class FontRendererUtil {
    public static void drawFittingString(FontRenderer fr, String text, int x, int y, int color, int maxWidth) {
        if (text == null || text.isEmpty()) return;
        int textWidth = fr.getStringWidth(text);

        float scale = 1.0F;
        if (textWidth > maxWidth) scale = (float) maxWidth / textWidth;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1.0F);
        fr.drawString(text, 0, 0, color);
        GlStateManager.popMatrix();
    }
}
