package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.network.TileEntityRadioTorchBase;
import com.hbm.util.I18nUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Arrays;

import static com.hbm.render.NTMRenderHelper.bindTexture;
import static com.hbm.util.SoundUtil.playClickSound;

public class GUIScreenRadioTorch extends GuiScreen {

	protected ResourceLocation texture;
	protected static final ResourceLocation textureSender = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_rtty_sender.png");
	protected static final ResourceLocation textureReceiver = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_rtty_receiver.png");
	protected TileEntityRadioTorchBase radio;
	protected static boolean isSenderTorch;
	protected String title;
	protected int xSize = 256;
	protected int ySize = 204;
	protected int guiLeft;
	protected int guiTop;
	protected GuiTextField frequency;
	protected GuiTextField[] remap;

	protected static final int textColor = 0x00FF00;
	protected static final int disabledTextColor = 0x00FF00;
    private static final int MAPPING_SIZE = 16;
	
	public GUIScreenRadioTorch(TileEntityRadioTorchBase radio, boolean isSender) {
        isSenderTorch = isSender;
		this.radio = radio;
        this.texture = isSender ? textureSender : textureReceiver;
        this.title = isSender ? "container.rttySender" : "container.rttyReceiver";
	}

	@Override
	public void initGui() {
		super.initGui();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;

		Keyboard.enableRepeatEvents(true);
		
		int oX = 4;
		int oY = 4;
		int in = isSenderTorch ? 18 : 0;

		this.frequency = new GuiTextField(0, this.fontRenderer, guiLeft + 25 + oX, guiTop + 18 + oY, 90 - oX * 2, 14);
		this.frequency.setTextColor(textColor);
		this.frequency.setDisabledTextColour(disabledTextColor);
		this.frequency.setEnableBackgroundDrawing(false);
		this.frequency.setMaxStringLength(10);
		this.frequency.setText(radio.channel == null ? "" : radio.channel);
		
		this.remap = new GuiTextField[MAPPING_SIZE];
		
		for(int i = 0; i < MAPPING_SIZE; i++) {
			this.remap[i] = new GuiTextField(i + 1, this.fontRenderer, guiLeft + 7 + (130 * (i / 8)) + oX + in, guiTop + 53 + (18 * (i % 8)) + oY, 90 - oX * 2, 14);
			this.remap[i].setTextColor(textColor);
			this.remap[i].setDisabledTextColour(disabledTextColor);
			this.remap[i].setEnableBackgroundDrawing(false);
			this.remap[i].setMaxStringLength(32);
			this.remap[i].setText(radio.mapping[i] == null ? "" : radio.mapping[i]);
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	public void drawCustomInfoStat(int mouseX, int mouseY, int x, int y, int width, int height, int tPosX, int tPosY, String[] text) {
		if(x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY){
			this.drawHoveringText(Arrays.asList(text), tPosX, tPosY);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		this.drawDefaultBackground();
        this.drawGuiContainerBackgroundLayer();
        GlStateManager.disableLighting();
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        GlStateManager.enableLighting();
	}

	private void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String name = I18nUtil.resolveKey(this.title);
		this.fontRenderer.drawString(name, this.guiLeft + this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, this.guiTop + 6, 0x404040);

        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 137, guiTop + 17, 18, 18, mouseX, mouseY, new String[] { radio.customMap ? "Custom Mapping" : "Redstone Passthrough" });
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 173, guiTop + 17, 18, 18, mouseX, mouseY, new String[] { radio.polling ? "Polling" : "State Change" });
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 209, guiTop + 17, 18, 18, mouseX, mouseY, new String[] { "Save Settings" });
	}

	private void drawGuiContainerBackgroundLayer() {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		bindTexture(texture);

		if(radio.customMap) {
			drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
			drawTexturedModalRect(guiLeft + 137, guiTop + 17, 0, 204, 18, 18);

            if(radio.polling) {
                drawTexturedModalRect(guiLeft + 173, guiTop + 17, 0, 222, 18, 18);
            }

			for(int j = 0; j < MAPPING_SIZE; j++) {
				this.remap[j].drawTextBox();
			}
		} else {
			drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, 35);
			drawTexturedModalRect(guiLeft, guiTop + 35, 0, 197, xSize, 7);

            if(radio.polling) {
                drawTexturedModalRect(guiLeft + 173, guiTop + 17, 0, 222, 18, 18);
            }
		}
		
		this.frequency.drawTextBox();
	}

	@Override
	protected void mouseClicked(int x, int y, int i) throws IOException {
		super.mouseClicked(x, y, i);
		
		this.frequency.mouseClicked(x, y, i);
		
		if(radio.customMap) {
			for(int j = 0; j < MAPPING_SIZE; j++) {
				this.remap[j].mouseClicked(x, y, i);
			}
		}
		
		if(guiLeft + 137 <= x && guiLeft + 137 + 18 > x && guiTop + 17 < y && guiTop + 17 + 18 >= y) {
			playClickSound();
			NBTTagCompound data = new NBTTagCompound();
			data.setBoolean("hasMapping", !radio.customMap);
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, radio.getPos()));
        }

        if(guiLeft + 173 <= x && guiLeft + 173 + 18 > x && guiTop + 17 < y && guiTop + 17 + 18 >= y) {
            playClickSound();
			NBTTagCompound data = new NBTTagCompound();
			data.setBoolean("isPolling", !radio.polling);
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, radio.getPos()));
        }

        if(guiLeft + 209 <= x && guiLeft + 209 + 18 > x && guiTop + 17 < y && guiTop + 17 + 18 >= y) {
			playClickSound();
			NBTTagCompound data = new NBTTagCompound();
			data.setString("channel", this.frequency.getText());
			for(int j = 0; j < MAPPING_SIZE; j++) {
				data.setString("mapping" + j, this.remap[j].getText());
			}
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, radio.getPos()));
        }
	}

	@Override
	protected void keyTyped(char c, int key) throws IOException {
		if(this.frequency.textboxKeyTyped(c, key)){
			return;
		}

		if(radio.customMap) {
			for(int j = 0; j < MAPPING_SIZE; j++) {
				if(this.remap[j].textboxKeyTyped(c, key)) 
					return;
			}
		}
		
		if(key == 1 || key == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
			this.mc.player.closeScreen();
		}
		super.keyTyped(c, key);
	}
}
