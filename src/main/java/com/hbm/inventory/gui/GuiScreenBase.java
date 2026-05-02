package com.hbm.inventory.gui;

import net.minecraft.client.gui.GuiScreen;

import java.util.Arrays;

public abstract class GuiScreenBase extends GuiScreen {
    protected void drawCustomInfo(int mouseX, int mouseY, int x, int y, int width, int height, String[] text) {
        if (x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY) {
            drawHoveringText(Arrays.asList(text), mouseX, mouseY);
        }
    }
}
