package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineRTG;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineRTG;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUIMachineRTG extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/gui_rtg.png");
	private TileEntityMachineRTG rtg;
	
	public GUIMachineRTG(InventoryPlayer invPlayer, TileEntityMachineRTG tedf) {
		super(new ContainerMachineRTG(invPlayer, tedf));
		rtg = tedf;
		
		this.xSize = 176;
		this.ySize = 176;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 69 - 52, 16, 52, rtg.power, rtg.maxPower);
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 134, guiTop + 69 - 52, 16, 52, mouseX+8, mouseY-8, new String[] {"RTG Heat " + rtg.heat + "/" + rtg.heatMax, "RTG Power " + Library.getShortNumber(rtg.heat*100)+"HE/s"});
		
		String[] text = new String[] { "Heat to Power Conversion 1:5" };
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 16, guiTop + 36, 16, 16, guiLeft - 8, guiTop + 36 + 16, text);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		String name = this.rtg.hasCustomInventoryName() ? this.rtg.getInventoryName() : I18n.format(this.rtg.getInventoryName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(rtg.hasHeat())
		{
			int i = rtg.getHeatScaled(52);
			drawTexturedModalRect(guiLeft + 134, guiTop + 74 - i, 176, 52 - i, 16, i);
		}
		if(rtg.hasPower())
		{
			int i = (int)rtg.getPowerScaled(52);
			drawTexturedModalRect(guiLeft + 152, guiTop + 74 - i, 192, 52 - i, 16, i);
		}
		
		this.drawInfoPanel(guiLeft - 16, guiTop + 36, 16, 16, 2);
	}
}
