package com.hbm.inventory.gui;

import net.minecraft.client.renderer.GlStateManager;
import com.hbm.Tags;
import com.hbm.util.I18nUtil;
import net.minecraft.init.SoundEvents;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKNumitron;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GUIScreenRBMKDisplay extends GuiScreen {

	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_rbmk_numitron.png");
	public TileEntityRBMKNumitron display;
	protected int xSize = 256;
	protected int ySize = 149;
	protected int guiLeft;
	protected int guiTop;

	protected GuiTextField[] label = new GuiTextField[2];
	protected GuiTextField[] rtty = new GuiTextField[2];
	protected boolean[] active = new boolean[2];
	protected boolean[] polling = new boolean[2];
	protected boolean[] shorten_number = new boolean[2];
	protected boolean[] leading_zeroes = new boolean[2];

	public GUIScreenRBMKDisplay(TileEntityRBMKNumitron display) {
		this.display = display;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;

		Keyboard.enableRepeatEvents(true);
		
		int oX = 4;
		int oY = 4;
		
		for(int i = 0; i < 2; i++) {
			rtty[i] = new GuiTextField(0, this.fontRenderer, guiLeft + 27 + oX, guiTop + 55 + oY + i * 54, 85 - oX * 2, 14);
			GUIScreenRBMKKeyPad.setupTextFieldStandard(rtty[i], 10, display.displays[i].rtty);
			label[i] = new GuiTextField(0, this.fontRenderer, guiLeft + 27 + oX, guiTop + 73 + oY + i * 54, 85 - oX * 2, 14);
			GUIScreenRBMKKeyPad.setupTextFieldStandard(label[i], 30, display.displays[i].label);

			active[i] = display.displays[i].active;
			polling[i] = display.displays[i].polling;
			shorten_number[i] = display.displays[i].shorten_number;
			leading_zeroes[i] = display.displays[i].leading_zeroes;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		this.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
		super.drawScreen(mouseX,mouseY,f);
		this.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	private void drawGuiContainerForegroundLayer(int x, int y) {
		String name = I18nUtil.resolveKey("tile.rbmk_numitron.name");
		this.fontRenderer.drawString(name, this.guiLeft + this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, this.guiTop + 6, 4210752);
	}

	private void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		for(int i = 0; i < 2; i++) {
			if(this.active[i]) drawTexturedModalRect(guiLeft + 124, guiTop + i * 54 + 54, 18, 150, 16, 16);
			if(this.polling[i]) drawTexturedModalRect(guiLeft + 159, guiTop + i * 54 + 53, 0, 150, 18, 18);
			if(this.shorten_number[i]) drawTexturedModalRect(guiLeft + 195, guiTop + i * 54 + 53, 34, 150, 18, 18);
			if(this.leading_zeroes[i]) drawTexturedModalRect(guiLeft + 231, guiTop + i * 54 + 53, 52, 150, 18, 18);
		}
		
		for(int i = 0; i < 2; i++) {
			this.label[i].drawTextBox();
			this.rtty[i].drawTextBox();
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int b) throws IOException {
		super.mouseClicked(x, y, b);
		
		for(int i = 0; i < 2; i++) {
			if(guiLeft + 124 <= x && guiLeft + 124 + 16 > x && guiTop + i * 54 + 54 < y && guiTop + i * 54 + 54 + 16 >= y) {
				this.active[i] = !this.active[i];
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 0.5F + (this.active[i] ? 0.25F : 0F)));
				return;
			}

			if(guiLeft + 159 <= x && guiLeft + 159 + 18 > x && guiTop + i * 54 + 53 < y && guiTop + i * 54 + 53 + 18 >= y) {
				this.polling[i] = !this.polling[i];
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 0.5F + (this.polling[i] ? 0.25F : 0F)));
				return;
			}

			if(guiLeft + 195 <= x && guiLeft + 195 + 18 > x && guiTop + i * 54 + 53 < y && guiTop + i * 54 + 53 + 18 >= y) {
				this.shorten_number[i] = !this.shorten_number[i];
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 0.5F + (this.shorten_number[i] ? 0.25F : 0F)));
				return;
			}

			if(guiLeft + 231 <= x && guiLeft + 231 + 18 > x && guiTop + i * 54 + 53 < y && guiTop + i * 54 + 53 + 18 >= y) {
				this.leading_zeroes[i] = !this.leading_zeroes[i];
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 0.5F + (this.leading_zeroes[i] ? 0.25F : 0F)));
				return;
			}
		}
		
		if(guiLeft + 209 <= x && guiLeft + 209 + 18 > x && guiTop + 17 < y && guiTop + 17 + 18 >= y) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			NBTTagCompound data = new NBTTagCompound();
			byte active = 0;
			byte polling = 0;
			byte shorten = 0;
			byte leading = 0;
			for(int i = 0; i < 2; i++) {
				if(this.active[i]) active |= 1 << i;
				if(this.polling[i]) polling |= 1 << i;
				if(this.shorten_number[i]) shorten |= 1 << i;
				if(this.leading_zeroes[i]) leading |= 1 << i;
			}
			data.setByte("active", active);
			data.setByte("polling", polling);
			data.setByte("shorten_number", shorten);
			data.setByte("leading_zeroes", leading);

			for(int i = 0; i < 2; i++) {
				data.setString("label" + i, this.label[i].getText());
				data.setString("rtty" + i, this.rtty[i].getText());
			}
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, display.getPos().getX(), display.getPos().getY(), display.getPos().getZ()));
			return;
		}
		
		for(int i = 0; i < 2; i++) {
			this.label[i].mouseClicked(x, y, b);
			this.rtty[i].mouseClicked(x, y, b);
		}
	}

	@Override
	protected void keyTyped(char c, int b) {
		
		for(int i = 0; i < 2; i++) {
			if(this.label[i].textboxKeyTyped(c, b)) return;
			if(this.rtty[i].textboxKeyTyped(c, b)) return;
		}
		
		if(b == 1 || b == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
			this.mc.displayGuiScreen(null);

			if (this.mc.currentScreen == null)
			{
				this.mc.setIngameFocus();
			}
		}
	}

	@Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
	@Override public boolean doesGuiPauseGame() { return false; }
}
