package com.hbm.inventory.gui;

import com.hbm.blocks.generic.BlockBobble.BobbleType;
import com.hbm.blocks.generic.BlockBobble.TileEntityBobble;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.util.Tuple.Pair;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GUIScreenBobble extends GuiScreen {

    private final TileEntityBobble bobble;

    public GUIScreenBobble(TileEntityBobble bobble) {
        this.bobble = bobble;
    }

    @Override
    public void initGui() {
        this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(HBMSoundHandler.bobble, 1.0F));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawDefaultBackground();

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableTexture2D();

        double sizeX = 300;
        double sizeY = 150;
        double left = (this.width - sizeX) / 2.0;
        double top = (this.height - sizeY) / 2.0;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        float r = 0F, g = 0.2F, b = 0F, a = 0.8F;
        buf.pos(left + sizeX, top, this.zLevel).color(r, g, b, a).endVertex();
        buf.pos(left, top, this.zLevel).color(r, g, b, a).endVertex();
        buf.pos(left, top + sizeY, this.zLevel).color(r, g, b, a).endVertex();
        buf.pos(left + sizeX, top + sizeY, this.zLevel).color(r, g, b, a).endVertex();
        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();

        int nextY = (int) top + 10;

        String bobbleTitle = "Nuclear Tech Commemorative Bobblehead";
        this.fontRenderer.drawStringWithShadow(bobbleTitle, (int) (left + sizeX / 2 - this.fontRenderer.getStringWidth(bobbleTitle) / 2.0), nextY,
                0x00ff00);
        nextY += 10;

        String bobbleName = this.bobble.type.name;
        if (this.bobble.type == BobbleType.MELLOW) {
            bobbleName = anagramIt(bobbleName, "GEORGEWILLIAMPATON");
        }
        this.fontRenderer.drawStringWithShadow(bobbleName, (int) (left + sizeX / 2 - this.fontRenderer.getStringWidth(bobbleName) / 2.0), nextY,
                0x009900);
        nextY += 20;

        if (this.bobble.type.contribution != null) {
            String title = "Has contributed";
            this.fontRenderer.drawStringWithShadow(title, (int) (left + sizeX / 2 - this.fontRenderer.getStringWidth(title) / 2.0), nextY, 0x00ff00);
            nextY += 10;

            String[] list = this.bobble.type.contribution.split("\\$");
            for (String text : list) {
                this.fontRenderer.drawStringWithShadow(text, (int) (left + sizeX / 2 - this.fontRenderer.getStringWidth(text) / 2.0), nextY,
                        0x009900);
                nextY += 10;
            }
            nextY += 10;
        }

        if (this.bobble.type.inscription != null) {
            String title = "On the bottom is the following inscription:";
            this.fontRenderer.drawStringWithShadow(title, (int) (left + sizeX / 2 - this.fontRenderer.getStringWidth(title) / 2.0), nextY, 0x00ff00);
            nextY += 10;

            String[] list = this.bobble.type.inscription.split("\\$");
            for (String text : list) {
                this.fontRenderer.drawStringWithShadow(text, (int) (left + sizeX / 2 - this.fontRenderer.getStringWidth(text) / 2.0), nextY,
                        0x009900);
                nextY += 10;
            }
        }

        GlStateManager.enableLighting();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            this.mc.player.closeScreen();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private String anagramIt(String from, String to) {
        double t = Math.sin((double) System.currentTimeMillis() / 1500.0) * 0.75 + 0.5;

        char[] lettersFrom = from.toCharArray();
        char[] lettersTo = to.toCharArray();
        boolean[] hasPairedLetter = new boolean[lettersFrom.length];
        List<Pair<Double, Character>> letterTargets = new ArrayList<>();

        for (int i = 0; i < lettersFrom.length; i++) {
            char letterFrom = lettersFrom[i];
            for (int o = 0; o < lettersTo.length; o++) {
                char letterTo = lettersTo[o];
                if (letterFrom == letterTo && !hasPairedLetter[o]) {
                    double v = lerp(i, o, t);
                    letterTargets.add(new Pair<>(v, lettersFrom[i]));
                    hasPairedLetter[o] = true;
                    break;
                }
            }
        }

        for (int i = 0; i < letterTargets.size(); i++) {
            for (int j = i + 1; j < letterTargets.size(); j++) {
                if (letterTargets.get(i).key > letterTargets.get(j).key) {
                    Pair<Double, Character> temp = letterTargets.get(i);
                    letterTargets.set(i, letterTargets.get(j));
                    letterTargets.set(j, temp);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Pair<Double, Character> in : letterTargets) sb.append(in.value);
        return sb.toString();
    }

    private double lerp(double a, double b, double t) {
        t = Math.max(Math.min(t, 1), 0);
        return a * (1 - t) + b * t;
    }
}
