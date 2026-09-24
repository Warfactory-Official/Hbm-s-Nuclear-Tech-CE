package com.hbm.render.util;

import com.hbm.inventory.control_panel.GuiControlEdit;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ControlPanelViewModelPositonDebugger {
    public static boolean ENABLED = false;

    private static boolean previewDebug = false;
    private static boolean blockPreviewInput = false;
    private static final Map<String, int[]> rectOverrides = new HashMap<>();

    public int[] tickAndResolve(String configClassName, int[] defaultRect) {
        int[] rect = rectOverrides.computeIfAbsent(configClassName, key -> defaultRect.clone());
        handleToggle();
        handleAdjustmentInput(configClassName, defaultRect, rect);
        return rect;
    }

    public void renderOverlay(GuiControlEdit gui, String configClassName, int[] rect) {
        if(!previewDebug || rect == null) {
            return;
        }

        FontRenderer font = gui.getFontRenderer();
        int x = gui.getGuiLeft() + 6;
        int y = gui.getGuiTop() + 212;
        font.drawString("Preview Debug ON  F8 toggle", x, y, 0xFFCC66, false);
        y += 10;
        font.drawString("Target: " + configClassName, x, y, 0xFFFFFFFF, false);
        y += 10;
        font.drawString("x=" + rect[0] + " y=" + rect[1] + " w=" + rect[2] + " h=" + rect[3], x, y, 0xFFB0FFB0, false);
        y += 10;
        font.drawString("Hold LALT or RALT to edit", x, y, 0xFFAAAAAA, false);
        y += 10;
        font.drawString("Arrows move  [ ] width  , . height  \\\\ reset  TAB fine", x, y, 0xFFAAAAAA, false);
    }

    private static void handleToggle() {
        if(Keyboard.isKeyDown(Keyboard.KEY_F8)) {
            if(!blockPreviewInput) {
                previewDebug = !previewDebug;
                blockPreviewInput = true;
            }
        } else if(!hasAdjustmentInput()) {
            blockPreviewInput = false;
        }
    }

    private static void handleAdjustmentInput(String configClassName, int[] defaultRect, int[] rect) {
        if(!previewDebug) {
            return;
        }

        boolean alt = Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
        if(!alt) {
            if(!Keyboard.isKeyDown(Keyboard.KEY_F8)) {
                blockPreviewInput = false;
            }
            return;
        }

        boolean[] inputs = new boolean[]{
                Keyboard.isKeyDown(Keyboard.KEY_UP),
                Keyboard.isKeyDown(Keyboard.KEY_DOWN),
                Keyboard.isKeyDown(Keyboard.KEY_LEFT),
                Keyboard.isKeyDown(Keyboard.KEY_RIGHT),
                Keyboard.isKeyDown(Keyboard.KEY_LBRACKET),
                Keyboard.isKeyDown(Keyboard.KEY_RBRACKET),
                Keyboard.isKeyDown(Keyboard.KEY_COMMA),
                Keyboard.isKeyDown(Keyboard.KEY_PERIOD),
                Keyboard.isKeyDown(Keyboard.KEY_BACKSLASH)
        };

        boolean anyInput = false;
        for(boolean input : inputs) {
            if(input) {
                anyInput = true;
                break;
            }
        }

        if(!anyInput) {
            blockPreviewInput = false;
            return;
        }
        if(blockPreviewInput) {
            return;
        }
        blockPreviewInput = true;

        int step = Keyboard.isKeyDown(Keyboard.KEY_TAB) ? 1 : 4;

        if(inputs[0]) rect[1] -= step;
        if(inputs[1]) rect[1] += step;
        if(inputs[2]) rect[0] -= step;
        if(inputs[3]) rect[0] += step;
        if(inputs[4]) rect[2] = Math.max(16, rect[2] - step);
        if(inputs[5]) rect[2] = Math.max(16, rect[2] + step);
        if(inputs[6]) rect[3] = Math.max(16, rect[3] - step);
        if(inputs[7]) rect[3] = Math.max(16, rect[3] + step);
        if(inputs[8]) {
            rectOverrides.put(configClassName, defaultRect.clone());
        }
    }

    private static boolean hasAdjustmentInput() {
        return Keyboard.isKeyDown(Keyboard.KEY_F8)
                || Keyboard.isKeyDown(Keyboard.KEY_UP)
                || Keyboard.isKeyDown(Keyboard.KEY_DOWN)
                || Keyboard.isKeyDown(Keyboard.KEY_LEFT)
                || Keyboard.isKeyDown(Keyboard.KEY_RIGHT)
                || Keyboard.isKeyDown(Keyboard.KEY_LBRACKET)
                || Keyboard.isKeyDown(Keyboard.KEY_RBRACKET)
                || Keyboard.isKeyDown(Keyboard.KEY_COMMA)
                || Keyboard.isKeyDown(Keyboard.KEY_PERIOD)
                || Keyboard.isKeyDown(Keyboard.KEY_BACKSLASH);
    }

}
