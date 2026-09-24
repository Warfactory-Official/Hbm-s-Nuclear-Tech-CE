package com.hbm.inventory.gui;

import com.hbm.items.ModItems;
import com.hbm.items.special.ItemHolotapeImage.EnumHoloImage;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.render.util.NTMImmediate;
import com.hbm.util.EnumUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GUIScreenHolotape extends GuiScreen {

	EnumHoloImage holo;

	@Override
	public void initGui() {
		mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(HBMSoundHandler.bobble, 1.0F));

		ItemStack stack = Minecraft.getMinecraft().player.getHeldItemMainhand();

		if(stack.getItem() == ModItems.holotape_image) {
			this.holo = EnumUtil.grabEnumSafely(EnumHoloImage.VALUES, stack.getItemDamage());
		} else {
			this.mc.player.closeScreen();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {

		if(this.holo == null)
			return;

		this.drawDefaultBackground();
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GlStateManager.disableTexture2D();

		double sizeX = 300;
		double sizeY = 150;
		double left = (this.width - sizeX) / 2;
		double top = (this.height - sizeY) / 2;

		BufferBuilder buffer = NTMImmediate.INSTANCE.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(left + sizeX, top, this.zLevel).color(0F, 0.2F, 0F, 0.8F).endVertex();
		buffer.pos(left, top, this.zLevel).color(0F, 0.2F, 0F, 0.8F).endVertex();
		buffer.pos(left, top + sizeY, this.zLevel).color(0F, 0.2F, 0F, 0.8F).endVertex();
		buffer.pos(left + sizeX, top + sizeY, this.zLevel).color(0F, 0.2F, 0F, 0.8F).endVertex();
		NTMImmediate.INSTANCE.draw();

		GlStateManager.enableTexture2D();
		GlStateManager.enableAlpha();
		GlStateManager.disableBlend();

		int nextLevel = (int) top + 30;

		if(this.holo.getText() != null) {

			List<String> lines = I18nUtil.autoBreak(this.fontRenderer, this.holo.getText(), 275);

			for(String text : lines) {
				this.fontRenderer.drawStringWithShadow(text, (int) (left + sizeX / 2 - this.fontRenderer.getStringWidth(text) / 2), nextLevel, 0x009900);
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
