package com.hbm.util;

public class GuiUtil {
    public static boolean checkMouseBoundary(int guiLeft, int guiTop, int x, int y, int left, int top, int sizeX, int sizeY) {
        return x >= guiLeft + left && x <= guiLeft + left + sizeX && y >= guiTop + top && y <= guiTop + top + sizeY;
    }
}
