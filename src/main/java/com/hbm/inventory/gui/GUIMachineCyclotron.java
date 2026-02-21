package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineCyclotron;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.AuxButtonPacket;
import com.hbm.tileentity.machine.TileEntityMachineCyclotron;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIMachineCyclotron extends GuiInfoContainer {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_cyclotron.png");
	private final TileEntityMachineCyclotron cyclotron;

	public GUIMachineCyclotron(InventoryPlayer invPlayer, TileEntityMachineCyclotron tile) {
		super(new ContainerMachineCyclotron(invPlayer, tile));
		cyclotron = tile;

		this.xSize = 190;
		this.ySize = 215;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 168, guiTop + 18, 16, 63, cyclotron.power, cyclotron.maxPower);

		cyclotron.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 11, guiTop + 81, 34, 7);
		cyclotron.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 11, guiTop + 90, 34, 7);
		cyclotron.tanks[2].renderTankInfo(this, mouseX, mouseY, guiLeft + 107, guiTop + 81, 34, 16);

		String[] upgradeText = new String[4];
		upgradeText[0] = I18nUtil.resolveKey("desc.gui.upgrade");
		upgradeText[1] = I18nUtil.resolveKey("desc.gui.upgrade.speed");
		upgradeText[2] = I18nUtil.resolveKey("desc.gui.upgrade.effectiveness");
		upgradeText[3] = I18nUtil.resolveKey("desc.gui.upgrade.power");
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 49, guiTop + 85, 8, 8, mouseX, mouseY, upgradeText);
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.cyclotron.hasCustomName() ? this.cyclotron.getName() : I18n.format(this.cyclotron.getName());

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 15, this.ySize - 96 + 2, 4210752);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);

    	if(guiLeft + 97 <= mouseX && guiLeft + 97 + 18 > mouseX && guiTop + 107 < mouseY && guiTop + 107 + 18 >= mouseY) {

			playClickSound();
    		PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(cyclotron.getPos(), 0, 0));
    	}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int k = (int) cyclotron.getPowerScaled(63);
		drawTexturedModalRect(guiLeft + 168, guiTop + 80 - k, 190, 62 - k, 16, k);

		int l = cyclotron.getProgressScaled(34);
		drawTexturedModalRect(guiLeft + 48, guiTop + 27, 206, 0, l, 34);

		if(l > 0)
			drawTexturedModalRect(guiLeft + 172, guiTop + 4, 190, 63, 9, 12);

		this.drawInfoPanel(guiLeft + 49, guiTop + 85, 8, 8, 8);

		cyclotron.tanks[0].renderTank(guiLeft + 11, guiTop + 88, this.zLevel, 34, 7, 1);
		cyclotron.tanks[1].renderTank(guiLeft + 11, guiTop + 97, this.zLevel, 34, 7, 1);
		cyclotron.tanks[2].renderTank(guiLeft + 107, guiTop + 97, this.zLevel, 34, 16, 1);
	}
}
