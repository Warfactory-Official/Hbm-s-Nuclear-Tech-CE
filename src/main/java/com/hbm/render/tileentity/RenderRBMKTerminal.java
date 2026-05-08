package com.hbm.render.tileentity;

import com.hbm.blocks.machine.rbmk.RBMKMiniPanelBase;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.gui.GUIScreenRBMKTerminal;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKTerminal;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;

@AutoRegister
public class RenderRBMKTerminal extends TileEntitySpecialRenderer<TileEntityRBMKTerminal> {

    @Override
    public void render(TileEntityRBMKTerminal terminal, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableCull();
        GlStateManager.enableLighting();

        EnumFacing facing = terminal.getWorld().getBlockState(terminal.getPos()).getValue(RBMKMiniPanelBase.FACING);
        switch (facing) {
            case NORTH: GlStateManager.rotate(90, 0F, 1F, 0F); break;
            case WEST: GlStateManager.rotate(180, 0F, 1F, 0F); break;
            case SOUTH: GlStateManager.rotate(270, 0F, 1F, 0F); break;
            case EAST: break;
            default: break;
        }

        GlStateManager.translate(0.25D, 0.0D, 0.0D);
        this.bindTexture(ResourceManager.rbmk_terminal_tex);
        ResourceManager.rbmk_terminal.renderAll();

        GlStateManager.translate(0.0635D, 0.125D, 0.0625D * 5.5D);

        RenderArcFurnace.fullbright(true);
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        int height = font.FONT_HEIGHT;
        float scale = 1F / 250F;
        String prefix = "> ";
        int prefixWidth = font.getStringWidth(prefix);
        int maxWidth = 172;
        int fontColor = terminal.doesRepeat ? 0xff8000 : 0x00ff00;

        String suffix = "";
        if (Minecraft.getMinecraft().currentScreen instanceof GUIScreenRBMKTerminal && BobMathUtil.getBlink()) {
            suffix = "_";
        }
        int suffixWidth = font.getStringWidth(suffix);

        for (int i = 0; i < 18; i++) {
            String label = i == 0 ? GUIScreenRBMKTerminal.getWorkingLine() : terminal.history[i - 1];
            if (label == null) label = "";

            StringBuilder builder = new StringBuilder(40);
            if (i == 0 || !label.isEmpty()) builder.append(prefix);

            int width = prefixWidth;
            for (int j = 0; j < label.length(); j++) {
                char c = label.charAt(j);
                width += font.getCharWidth(c);
                if (width <= maxWidth) {
                    builder.append(c);
                } else {
                    break;
                }
            }

            if (i == 0 && font.getStringWidth(builder.toString()) + suffixWidth <= maxWidth) {
                builder.append(suffix);
            }

            GlStateManager.translate(0.0D, 10D * scale, 0.0D);
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, -scale, scale);
            GlStateManager.glNormal3f(0.0F, 0.0F, -1.0F);
            GlStateManager.rotate(90, 0, 1, 0);
            font.drawString(builder.toString(), 0, -height / 2, fontColor);
            GlStateManager.popMatrix();
        }

        RenderArcFurnace.fullbright(false);
        GlStateManager.popMatrix();
    }
}
