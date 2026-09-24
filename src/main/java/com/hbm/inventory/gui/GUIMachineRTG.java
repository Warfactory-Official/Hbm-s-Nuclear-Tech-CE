package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineRTG;
import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineRTG;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

import java.util.List;

public class GUIMachineRTG extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/gui_rtg.png");
	private TileEntityMachineRTG rtg;
	
	public GUIMachineRTG(InventoryPlayer invPlayer, TileEntityMachineRTG tedf) {
		super(new ContainerMachineRTG(invPlayer, tedf));
		rtg = tedf;

		this.xSize = 176;
		this.ySize = 188;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 146, guiTop + 11, 16, 50, rtg.power, rtg.maxPower);
		String[] heatText = I18nUtil.resolveKeyArray("desc.gui.rtg.heat", rtg.heat);
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 124, guiTop + 11, 16, 50, mouseX, mouseY, heatText);

		List<ItemRTGPellet> pellets = List.copyOf(ItemRTGPellet.pelletMap.keySet());
		String[] pelletText = new String[pellets.size() + 1];
		pelletText[0] = I18nUtil.resolveKey("desc.gui.rtg.pellets");

		for(int i = 0; i < pellets.size(); i++) {
			ItemRTGPellet pellet = pellets.get(i);
			pelletText[i + 1] = I18nUtil.resolveKey("desc.gui.rtg.pelletPower", I18nUtil.resolveKey(pellet.getTranslationKey() + ".name"), pellet.getHeat() * 5);
		}

		this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 12, guiTop + 25, 16, 16, guiLeft - 8, guiTop + 36 + 16, pelletText);
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		String name = this.rtg.hasCustomInventoryName() ? this.rtg.getInventoryName() : I18n.format(this.rtg.getInventoryName());

		this.fontRenderer.drawString(name, 60 - this.fontRenderer.getStringWidth(name) / 2, 7, 10925486);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if (rtg.hasHeat()) {
			int i = rtg.getHeatScaled(50);
			drawTexturedModalRect(guiLeft + 124, guiTop + 61 - i, 176, 61 - i, 16, i);
		}

		if (rtg.hasPower()) {
			int i = (int) rtg.getPowerScaled(50);
			drawTexturedModalRect(guiLeft + 146, guiTop + 61 - i, 192, 61 - i, 16, i);
		}

		this.drawInfoPanel(guiLeft - 12, guiTop + 25, 16, 16, 2);
	}
}
