package com.hbm.inventory.gui;

import com.hbm.blocks.generic.BlockSnowglobe.TileEntitySnowglobe;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.util.I18nUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GUIScreenSnowglobe extends GuiScreen {

    private final TileEntitySnowglobe snowglobe;

    public GUIScreenSnowglobe(TileEntitySnowglobe bobble) {
        this.snowglobe = bobble;
    }

    @Override
    public void initGui() {
        this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(HBMSoundHandler.bobble, 1.0F));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {

        this.drawDefaultBackground();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GlStateManager.disableTexture2D();

        double sizeX = 300;
        double sizeY = 150;
        double left = (this.width - sizeX) / 2;
        double top = (this.height - sizeY) / 2;

        float r = 0F;
        float g = 0.2F;
        float b = 0F;
        float a = 0.8F;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(left + sizeX, top, this.zLevel).color(r, g, b, a).endVertex();
        buf.pos(left, top, this.zLevel).color(r, g, b, a).endVertex();
        buf.pos(left, top + sizeY, this.zLevel).color(r, g, b, a).endVertex();
        buf.pos(left + sizeX, top + sizeY, this.zLevel).color(r, g, b, a).endVertex();
        tess.draw();

        GlStateManager.enableTexture2D();
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GlStateManager.enableBlend();

        int nextLevel = (int)top + 10;

        String bobbleTitle = "Nuclear Tech Commemorative Snowglobe";
        this.fontRenderer.drawStringWithShadow(bobbleTitle, (int)(left + sizeX / 2 - (double) this.fontRenderer.getStringWidth(bobbleTitle) / 2), nextLevel, 0x00ff00);

        nextLevel += 10;

        String bobbleName = this.snowglobe.type.label;
        this.fontRenderer.drawStringWithShadow(bobbleName, (int)(left + sizeX / 2 - (double) this.fontRenderer.getStringWidth(bobbleName) / 2), nextLevel, 0x009900);

        nextLevel += 20;

		/*if(this.snowglobe.type.contribution != null) {

			String title = "Has contributed";
			this.fontRendererObj.drawStringWithShadow(title, (int)(left + sizeX / 2 - this.fontRendererObj.getStringWidth(title) / 2), nextLevel, 0x00ff00);

			nextLevel += 10;


			String[] list = this.snowglobe.type.contribution.split("\\$");
			for(String text : list) {
				this.fontRendererObj.drawStringWithShadow(text, (int)(left + sizeX / 2 - this.fontRendererObj.getStringWidth(text) / 2), nextLevel, 0x009900);
				nextLevel += 10;
			}

			nextLevel += 10;
		}*/

        if(this.snowglobe.type.inscription != null) {

            String title = "On the bottom is the following inscription:";
            this.fontRenderer.drawStringWithShadow(title, (int)(left + sizeX / 2 - (double) this.fontRenderer.getStringWidth(title) / 2), nextLevel, 0x00ff00);

            nextLevel += 10;

            List<String> list = I18nUtil.autoBreak(this.fontRenderer, this.snowglobe.type.inscription, 280);
            for(String text : list) {
                this.fontRenderer.drawStringWithShadow(text, (int)(left + sizeX / 2 - (double) this.fontRenderer.getStringWidth(text) / 2), nextLevel, 0x009900);
                nextLevel += 10;
            }
        }

        GlStateManager.enableLighting();
    }

    @Override
    protected void keyTyped(char c, int key) {
        if(key == 1 || key == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            this.mc.player.closeScreen();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
