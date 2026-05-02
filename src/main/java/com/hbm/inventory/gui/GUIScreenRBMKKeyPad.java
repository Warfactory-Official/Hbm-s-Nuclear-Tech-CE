package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.util.I18nUtil;
import net.minecraft.init.SoundEvents;
import org.lwjgl.input.Keyboard;

import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKKeyPad;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GUIScreenRBMKKeyPad extends GuiScreen {

	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_rbmk_keypad.png");
	public TileEntityRBMKKeyPad keypad;
	protected int xSize = 256;
	protected int ySize = 204;
	protected int guiLeft;
	protected int guiTop;

	protected GuiTextField[] color = new GuiTextField[4];
	protected GuiTextField[] label = new GuiTextField[4];
	protected GuiTextField[] rtty = new GuiTextField[4];
	protected GuiTextField[] cmd = new GuiTextField[4];
	protected boolean[] active = new boolean[4];
	protected boolean[] polling = new boolean[4];

	public GUIScreenRBMKKeyPad(TileEntityRBMKKeyPad keypad) {
		this.keypad = keypad;

		this.xSize = 256;
		this.ySize = 204;
	}

	public static void setupTextFieldStandard(GuiTextField field, int length, String def) {
		field.setTextColor(0x00ff00);
		field.setDisabledTextColour(0x00ff00);
		field.setEnableBackgroundDrawing(false);
		field.setMaxStringLength(length);
		field.setText(def != null ? def : "");
	}

	@Override
	public void initGui() {
		super.initGui();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;

		Keyboard.enableRepeatEvents(true);

		int oX = 4;
		int oY = 4;

		for(int i = 0; i < 4; i++) {
			String col = Integer.toHexString(keypad.keys[i].color);
			while(col.length() < 6) col = "0" + col;
			color[i] = new GuiTextField(0, this.fontRenderer, guiLeft + 27 + oX, guiTop + 55 + oY + i * 36, 72 - oX * 2, 14);
			setupTextFieldStandard(color[i], 6, col);
			label[i] = new GuiTextField(0, this.fontRenderer, guiLeft + 175 + oX, guiTop + 55 + oY + i * 36, 72 - oX * 2, 14);
			setupTextFieldStandard(label[i], 15, keypad.keys[i].label);
			rtty[i] = new GuiTextField(0, this.fontRenderer, guiLeft + 27 + oX, guiTop + 73 + oY + i * 36, 72 - oX * 2, 14);
			setupTextFieldStandard(rtty[i], 10, keypad.keys[i].rtty);
			cmd[i] = new GuiTextField(0, this.fontRenderer, guiLeft + 121 + oX, guiTop + 73 + oY + i * 36, 126 - oX * 2, 14);
			setupTextFieldStandard(cmd[i], 32, keypad.keys[i].command);

			active[i] = keypad.keys[i].active;
			polling[i] = keypad.keys[i].polling;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		this.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
		super.drawScreen(mouseX,mouseY,f);
		this.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	private void drawGuiContainerForegroundLayer(int x, int y) {
		String name = I18nUtil.resolveKey("tile.rbmk_key_pad.name");
		this.fontRenderer.drawString(name, this.guiLeft + this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, this.guiTop + 6, 4210752);
	}

	private void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		for(int i = 0; i < 4; i++) {
			if(this.active[i]) drawTexturedModalRect(guiLeft + 111, guiTop + i * 36 + 54, 18, 204, 16, 16);
			if(this.polling[i]) drawTexturedModalRect(guiLeft + 128, guiTop + i * 36 + 53, 0, 204, 18, 18);
		}

		for(int i = 0; i < 4; i++) {
			this.color[i].drawTextBox();
			this.label[i].drawTextBox();
			this.rtty[i].drawTextBox();
			this.cmd[i].drawTextBox();
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int b) throws IOException {
		super.mouseClicked(x, y, b);

		for(int i = 0; i < 4; i++) {
			if(guiLeft + 111 <= x && guiLeft + 111 + 16 > x && guiTop + i * 36 + 54 < y && guiTop + i * 36 + 54 + 16 >= y) {
				this.active[i] = !this.active[i];
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 0.5F + (this.active[i] ? 0.25F : 0F)));
				return;
			}

			if(guiLeft + 128 <= x && guiLeft + 128 + 18 > x && guiTop + i * 36 + 53 < y && guiTop + i * 36 + 53 + 18 >= y) {
				this.polling[i] = !this.polling[i];
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 0.5F + (this.polling[i] ? 0.25F : 0F)));
				return;
			}
		}

		if(guiLeft + 209 <= x && guiLeft + 209 + 18 > x && guiTop + 17 < y && guiTop + 17 + 18 >= y) {
			NBTTagCompound data = new NBTTagCompound();
			byte active = 0;
			byte polling = 0;
			for(int i = 0; i < 4; i++) {
				if(this.active[i]) active |= 1 << i;
				if(this.polling[i]) polling |= 1 << i;
			}
			data.setByte("active", active);
			data.setByte("polling", polling);

			boolean valid = true;
			for(int i = 0; i < 4; i++) {
				try {
					data.setInteger("color" + i, Integer.parseInt(this.color[i].getText(), 16));
					this.color[i].setTextColor(0x00ff00);
				} catch(NumberFormatException ex) {
					this.color[i].setTextColor(0xff0000);
					valid = false;
				}
				data.setString("label" + i, this.label[i].getText());
				data.setString("rtty" + i, this.rtty[i].getText());
				data.setString("cmd" + i, this.cmd[i].getText());
			}
			if(!valid) return;

			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK,1));
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, keypad.getPos().getX(), keypad.getPos().getY(), keypad.getPos().getZ()));
			return;
		}

		for(int i = 0; i < 4; i++) {
			this.color[i].mouseClicked(x, y, b);
			this.label[i].mouseClicked(x, y, b);
			this.rtty[i].mouseClicked(x, y, b);
			this.cmd[i].mouseClicked(x, y, b);
		}
	}

	@Override
	protected void keyTyped(char c, int b) {

		for(int i = 0; i < 4; i++) {
			if(this.color[i].textboxKeyTyped(c, b)) return;
			if(this.label[i].textboxKeyTyped(c, b)) return;
			if(this.rtty[i].textboxKeyTyped(c, b)) return;
			if(this.cmd[i].textboxKeyTyped(c, b)) return;
		}

		if(b == 1 || b == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
			this.mc.displayGuiScreen((GuiScreen)null);

			if (this.mc.currentScreen == null)
			{
				this.mc.setIngameFocus();
			}
		}
	}

	@Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
	@Override public boolean doesGuiPauseGame() { return false; }
}