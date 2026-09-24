package com.hbm.qmaw.components;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.opengl.GL11;

import com.hbm.qmaw.GuiQMAW;
import com.hbm.qmaw.ManualElement;
import com.hbm.qmaw.QMAWLoader;
import com.hbm.qmaw.QuickManualAndWiki;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class QComponentLink extends ManualElement {
    protected String link;
    protected ItemStack icon;
    protected String text;
    protected FontRenderer font;
    protected int color = 0x0094FF;
    protected int hoverColor = 0xFFD800;

    protected static RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();

    public QComponentLink(String link, String text) {
        this.text = text;
        this.link = link;

        QuickManualAndWiki qmaw = QMAWLoader.qmaw.get(link);
        if(qmaw == null) {
            this.color = this.hoverColor = 0xFF7F7F;
        } else {
            this.icon = qmaw.icon;
        }

        this.font = Minecraft.getMinecraft().fontRenderer;
    }

    public QComponentLink setColor(int color, int hoverColor) {
        this.color = color;
        this.hoverColor = hoverColor;
        return this;
    }

    @Override
    public int getWidth() {
        return font.getStringWidth(text) + (icon != null ? 18 : 0);
    }

    @Override
    public int getHeight() {
        return Math.max(font.FONT_HEIGHT, icon != null ? 16 : 0);
    }

    @Override
    public void render(boolean isMouseOver, int x, int y, int mouseX, int mouseY) {

        if(this.icon != null) {

            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();
            Minecraft mc = Minecraft.getMinecraft();
            GlStateManager.rotate(180, 1, 0, 0);
            RenderHelper.enableStandardItemLighting();
            GlStateManager.rotate(-180, 1, 0, 0);
            itemRender.renderItemAndEffectIntoGUI( this.icon, x, y - 1);
            itemRender.renderItemOverlayIntoGUI(this.font, this.icon, x, y - 1, null);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableDepth();
            GlStateManager.popMatrix();

            x += 18;
            y += (16 - font.FONT_HEIGHT) / 2;
        }

        font.drawString(text, x, y, isMouseOver ? hoverColor : color);
    }

    @Override
    public void onClick(GuiQMAW gui) {
        QuickManualAndWiki qmaw = QMAWLoader.qmaw.get(link);
        if(qmaw != null) {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F, 1.0F));
            GuiQMAW screen = new GuiQMAW(qmaw);
            screen.back.addAll(gui.back);
            screen.back.add(gui.qmawID);
            FMLCommonHandler.instance().showGuiScreen(screen);
        }
    }
}
