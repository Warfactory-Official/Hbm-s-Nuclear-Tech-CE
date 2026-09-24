package com.hbm.inventory.gui;

import net.minecraft.client.renderer.GlStateManager;
import com.hbm.util.Calculator;
import com.hbm.util.Tuple;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

public class GUICalculator extends GuiScreen {
    private static final int SIZE_X = 220;
    private static final int SIZE_Y = 50;
    private static final int BORDER_WIDTH = 2;
    private GuiTextField inputField;

    private int selectedHist = -1;
    private static final int maxHistory = 6;
    private static final Deque<Tuple.Pair<String, Double>> history = new ArrayDeque<>();
    private String latestResult = "?";

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        int x = (width - SIZE_X) / 2;
        int y = (height - SIZE_Y) / 2;
        inputField = new GuiTextField(0, fontRenderer, x + 5, y + 8, 210, 13);
        inputField.setTextColor(-1);
        inputField.setCanLoseFocus(false);
        inputField.setFocused(true);
        inputField.setMaxStringLength(1000);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!inputField.textboxKeyTyped(typedChar, keyCode))
            super.keyTyped(typedChar, keyCode);

        String input = inputField.getText().replaceAll("[^\\d+\\-*/^!.()\\sA-Za-z]+", "");

        if (typedChar == 13 || typedChar == 10) { // when pressing enter (CR or LF)
            if (selectedHist != -1) {
                input = new ArrayList<>(history).get(selectedHist).key;
                inputField.setText(input);
                selectedHist = -1;
            } else {
                try {
                    double result = Calculator.evaluateExpression(input);
                    history.addFirst(new Tuple.Pair<>(input, result));
                    if (history.size() > maxHistory) history.removeLast();
                    String plainStringRepresentation = (new BigDecimal(result, MathContext.DECIMAL64)).toPlainString();
                    GuiScreen.setClipboardString(plainStringRepresentation);
                    inputField.setText(plainStringRepresentation);
                    inputField.setCursorPositionEnd();
                    inputField.setSelectionPos(0);
                } catch (Exception ignored) {}
                return;
            }
        }

        if (keyCode == 200) { // up arrow
            selectedHist = Math.max(selectedHist - 1, -1);
        } else if (keyCode == 208) { // down arrow
            selectedHist = Math.min(selectedHist + 1, history.size() - 1);
        } else {
            selectedHist = -1;
        }

        if (input.isEmpty()) {
            latestResult = "?";
            return;
        }

        try {
            latestResult = Double.toString(Calculator.evaluateExpression(input));
        } catch (Exception e) { latestResult = e.toString(); }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        GlStateManager.color(1F, 1F, 1F, 1F);
        int x = (width - SIZE_X) / 2;
        int y = (height - SIZE_Y) / 2;
        int histHeight = (fontRenderer.FONT_HEIGHT + 2) * maxHistory;
        int histStart = y + 30 + fontRenderer.FONT_HEIGHT + 8;

        drawRect(x, y, x+ SIZE_X, y+ SIZE_Y +histHeight, 0xFF2d2d2d);
        drawRect(x+ BORDER_WIDTH, y+ BORDER_WIDTH, x+ SIZE_X - BORDER_WIDTH, y+ SIZE_Y - BORDER_WIDTH +histHeight, 0xFF3d3d3d);
        drawRect(x, histStart - 5, x+ SIZE_X, histStart - 3, 0xFF2d2d2d);

        inputField.drawTextBox();
        fontRenderer.drawString("=" + latestResult, x + 5, y + 30, -1);

        int i = 0;
        for (Tuple.Pair<String, Double> prevInput : history) {
            int hy = y + 50 + (fontRenderer.FONT_HEIGHT+1)*i;
            if (i == selectedHist) {
                drawRect(x + 4, hy - 1, x + 4 + SIZE_X - 9, hy + fontRenderer.FONT_HEIGHT, 0xFF111111);
            }
            fontRenderer.drawString(prevInput.key + " = " + prevInput.value, x + 5, hy, -1);
            i++;
        }
    }
}
