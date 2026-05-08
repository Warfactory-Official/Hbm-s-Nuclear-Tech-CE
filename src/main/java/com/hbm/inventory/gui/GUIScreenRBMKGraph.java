package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKGraph;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

// Literally just GUIScreenRBMKDisplay but with three lines switched out - this sucks ass
public class GUIScreenRBMKGraph extends GuiScreen {

	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_rbmk_graph.png");
	public TileEntityRBMKGraph graph;
	protected int xSize = 256;
	protected int ySize = 150;
	protected int guiLeft;
	protected int guiTop;

	protected GuiTextField[] label = new GuiTextField[2];
	protected GuiTextField[] rtty = new GuiTextField[2];
	protected GuiTextField[] min = new GuiTextField[2];
	protected GuiTextField[] max = new GuiTextField[2];
	protected boolean[] active = new boolean[2];
	protected boolean[] polling = new boolean[2];
	
	public GUIScreenRBMKGraph(TileEntityRBMKGraph display) {
		this.graph = display;
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
			label[i] = new GuiTextField(0, this.fontRenderer, guiLeft + 27 + oX, guiTop + 73 + oY + i * 54, 72 - oX * 2, 14);
			GUIScreenRBMKKeyPad.setupTextFieldStandard(label[i], 30, graph.graphs[i].label);
			rtty[i] = new GuiTextField(0, this.fontRenderer, guiLeft + 27 + oX, guiTop + 55 + oY + i * 54, 72 - oX * 2, 14);
			GUIScreenRBMKKeyPad.setupTextFieldStandard(rtty[i], 10, graph.graphs[i].rtty);
			min[i] = new GuiTextField(0, this.fontRenderer, guiLeft + 175 + oX, guiTop + 55 + oY + i * 54, 72 - oX * 2, 14);
			GUIScreenRBMKKeyPad.setupTextFieldStandard(min[i], 15, graph.graphs[i].minBound ? Long.toString(graph.graphs[i].min) : "");
			max[i] = new GuiTextField(0, this.fontRenderer, guiLeft + 175 + oX, guiTop + 73 + oY + i * 54, 72 - oX * 2, 14);
			GUIScreenRBMKKeyPad.setupTextFieldStandard(max[i], 15, graph.graphs[i].maxBound ? Long.toString(graph.graphs[i].max) : "");

			active[i] = graph.graphs[i].active;
			polling[i] = graph.graphs[i].polling;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		this.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
		super.drawScreen(mouseX,mouseY,f);
		this.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	private void drawGuiContainerForegroundLayer(int x, int y) {
		String name = I18nUtil.resolveKey("tile.rbmk_graph.name");
		this.fontRenderer.drawString(name, this.guiLeft + this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, this.guiTop + 6, 4210752);
	}

	private void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		super.drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		for(int i = 0; i < 2; i++) {
			if(this.active[i]) drawTexturedModalRect(guiLeft + 111, guiTop + i * 54 + 54, 18, 150, 16, 16);
			if(this.polling[i]) drawTexturedModalRect(guiLeft + 128, guiTop + i * 54 + 53, 0, 150, 18, 18);
		}
		
		for(int i = 0; i < 2; i++) {
			this.label[i].drawTextBox();
			this.rtty[i].drawTextBox();
			this.min[i].drawTextBox();
			this.max[i].drawTextBox();
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int b) throws IOException {
		super.mouseClicked(x, y, b);
		
		for(int i = 0; i < 2; i++) {
			if(guiLeft + 111 <= x && guiLeft + 127 > x && guiTop + i * 54 + 54 < y && guiTop + i * 54 + 70 >= y) {
				this.active[i] = !this.active[i];
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 0.5F + (this.active[i] ? 0.25F : 0F)));
				return;
			}
			
			if(guiLeft + 128 <= x && guiLeft + 146 > x && guiTop + i * 54 + 53 < y && guiTop + i * 54 + 71 >= y) {
				this.polling[i] = !this.polling[i];
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 0.5F + (this.polling[i] ? 0.25F : 0F)));
				return;
			}
		}
		
		if(guiLeft + 209 <= x && guiLeft + 209 + 18 > x && guiTop + 17 < y && guiTop + 17 + 18 >= y) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			NBTTagCompound data = new NBTTagCompound();
			byte active = 0;
			byte polling = 0;
			for(int i = 0; i < 2; i++) {
				if(this.active[i]) active |= 1 << i;
				if(this.polling[i]) polling |= 1 << i;
			}
			data.setByte("active", active);
			data.setByte("polling", polling);
			
			for(int i = 0; i < 2; i++) {
				data.setString("label" + i, this.label[i].getText());
				data.setString("rtty" + i, this.rtty[i].getText());
				try {
					if(!this.min[i].getText().isEmpty()) data.setLong("min" + i, Long.parseLong(this.min[i].getText()));
				} catch(NumberFormatException ignored) { }
				try {
					if(!this.max[i].getText().isEmpty()) data.setLong("max" + i, Long.parseLong(this.max[i].getText()));
				} catch(NumberFormatException ignored) { }
			}
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, graph.getPos().getX(), graph.getPos().getY(), graph.getPos().getZ()));
			return;
		}
		
		for(int i = 0; i < 2; i++) {
			this.label[i].mouseClicked(x, y, b);
			this.rtty[i].mouseClicked(x, y, b);
			this.min[i].mouseClicked(x, y, b);
			this.max[i].mouseClicked(x, y, b);
		}
	}

	@Override
	protected void keyTyped(char c, int b) {
		
		for(int i = 0; i < 2; i++) {
			if(this.label[i].textboxKeyTyped(c, b)) return;
			if(this.rtty[i].textboxKeyTyped(c, b)) return;
			if(this.min[i].textboxKeyTyped(c, b)) return;
			if(this.max[i].textboxKeyTyped(c, b)) return;
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
