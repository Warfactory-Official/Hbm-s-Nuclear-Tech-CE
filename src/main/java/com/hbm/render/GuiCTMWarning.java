package com.hbm.render;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCTMWarning extends GuiScreen {

        private final String[] lines = {
                "CTM is recommended for full functionality.",
                "Download CTM here",
                "Press any key to continue"
        };
        private final String downloadURL = "https://www.curseforge.com/minecraft/mc-mods/ctm";
        private int[] lineX;
        private int[] lineY;

        @Override
        public void initGui() {
            // Compute line positions for centering
            lineX = new int[lines.length];
            lineY = new int[lines.length];

            int totalHeight = lines.length * fontRenderer.FONT_HEIGHT + (lines.length - 1) * 5; // 5px spacing
            int startY = (height - totalHeight) / 2;

            for (int i = 0; i < lines.length; i++) {
                lineX[i] = width / 2 - fontRenderer.getStringWidth(lines[i]) / 2;
                lineY[i] = startY + i * (fontRenderer.FONT_HEIGHT + 5);
            }
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            drawDefaultBackground();

            for (int i = 0; i < lines.length; i++) {
                int color = 0xFFFFFF;

                if (i == 1) {
                    int x = lineX[i];
                    int y = lineY[i];
                    int w = fontRenderer.getStringWidth(lines[i]);
                    int h = fontRenderer.FONT_HEIGHT;

                    if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                        color = 0x00FFFF;
                    }
                }

                fontRenderer.drawStringWithShadow(lines[i], lineX[i], lineY[i], color);
            }
            super.drawScreen(mouseX, mouseY, partialTicks);
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
            int x = lineX[1];
            int y = lineY[1];
            int w = fontRenderer.getStringWidth(lines[1]);
            int h = fontRenderer.FONT_HEIGHT;

            if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(downloadURL));
                    super.mouseClicked(mouseX, mouseY, mouseButton);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                mc.displayGuiScreen(null);
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) {
            mc.displayGuiScreen(null);
        }

        @Override
        public boolean doesGuiPauseGame() {
            return false;
        }
    }


