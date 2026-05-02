package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.items.tool.ItemRTTYPager;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTItemControlPacket;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GUIScreenPager extends GuiScreen {

	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_rtty_pager.png");
	public ItemStack pager;
	public String startingChannel;
	protected int xSize = 184;
	protected int ySize = 42;
	protected int guiLeft;
	protected int guiTop;

	protected GuiTextField chan;

	public GUIScreenPager(ItemStack pager) {
		this.pager = pager;
		if(pager != null && pager.hasTagCompound()) {
			startingChannel = pager.getTagCompound().getString(ItemRTTYPager.KEY_CHANNEL);
		}
		if(startingChannel == null) this.startingChannel = "";
	}

	@Override
	public void initGui() {
		super.initGui();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;

		Keyboard.enableRepeatEvents(true);

		int oX = 4;
		int oY = 4;

		chan = new GuiTextField(0, this.fontRenderer, guiLeft + 27 + oX, guiTop + 19 + oY, 90 - oX * 2, 14);
		GUIScreenRBMKKeyPad.setupTextFieldStandard(chan, 10, startingChannel);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		this.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
		super.drawScreen(mouseX, mouseY, f);
		this.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	private void drawGuiContainerForegroundLayer(int x, int y) {
		String name = I18nUtil.resolveKey("container.rttyPager");
		this.fontRenderer.drawString(name, this.guiLeft + this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, this.guiTop + 6, 4210752);
	}

	private void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		super.drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		chan.drawTextBox();
	}

	@Override
	protected void mouseClicked(int x, int y, int b) throws IOException {
		super.mouseClicked(x, y, b);

		if(guiLeft + 137 <= x && guiLeft + 137 + 18 > x && guiTop + 17 < y && guiTop + 17 + 18 >= y) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			NBTTagCompound data = new NBTTagCompound();
			data.setString(ItemRTTYPager.KEY_CHANNEL, this.chan.getText());
			PacketDispatcher.wrapper.sendToServer(new NBTItemControlPacket(data));
			return;
		}

		this.chan.mouseClicked(x, y, b);
	}

	@Override
	protected void keyTyped(char c, int b) {
		if(this.chan.textboxKeyTyped(c, b)) return;

		if(b == 1 || b == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
			this.mc.displayGuiScreen((GuiScreen)null);

			if (this.mc.currentScreen == null) {
				this.mc.setIngameFocus();
			}
		}
	}

	@Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
	@Override public boolean doesGuiPauseGame() { return false; }
}
