package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineDiesel;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineDiesel;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIMachineDiesel extends GuiInfoContainer {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/generators/gui_diesel.png");
	private final TileEntityMachineDiesel diesel;

	public GUIMachineDiesel(InventoryPlayer invPlayer, TileEntityMachineDiesel tedf) {
		super(new ContainerMachineDiesel(invPlayer, tedf));
		diesel = tedf;

		this.xSize = 176;
		this.ySize = 203;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
		diesel.tank.renderTankInfo(this, mouseX, mouseY, guiLeft + 35, guiTop + 69 - 52, 16, 52);
		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 141, guiTop + 69 - 52, 16, 52, diesel.power, diesel.powerCap);

		String[] text1 = I18nUtil.resolveKeyArray("desc.guimachinediesel1");
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 16, guiTop + 36 + 16, 16, 16, guiLeft - 8, guiTop + 36 + 16, text1);
		
		if(!diesel.hasAcceptableFuel()) {
			String[] text2 = I18nUtil.resolveKeyArray("desc.guimachinediesel2");
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 8, guiTop + 36 + 32, 16, 16, guiLeft, guiTop + 36 + 16 + 32, text2);
		}
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void mouseClicked(int x, int y, int i) throws IOException {
		super.mouseClicked(x, y, i);

		if(guiLeft + 89 <= x && guiLeft + 89 + 16 > x && guiTop + 61 < y && guiTop + 61 + 14 >= y) {
			playClickSound();
			NBTTagCompound data = new NBTTagCompound();
			data.setBoolean("turnOn", true);
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, diesel.getPos()));
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		if(diesel.power > 0) {
			int i = (int) diesel.getPowerScaled(52);
			drawTexturedModalRect(guiLeft + 141, guiTop + 69 - i, 176, 52 - i, 16, i);
		}

		if(diesel.isOn) drawTexturedModalRect(guiLeft + 79, guiTop + 61, 192, 16, 35, 14);
		if(diesel.wasOn) drawTexturedModalRect(guiLeft + 89, guiTop + 42, 192, 0, 16, 16);

		this.drawInfoPanel(guiLeft - 8, guiTop + 36, 16, 16, 2);

		if(!diesel.hasAcceptableFuel())
			this.drawInfoPanel(guiLeft - 8, guiTop + 36 + 32, 16, 16, 6);

		diesel.tank.renderTank(guiLeft + 35, guiTop + 69, this.zLevel, 16, 52);
	}
}
