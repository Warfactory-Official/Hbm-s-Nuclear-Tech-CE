package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerFurnaceSteel;
import com.hbm.tileentity.machine.TileEntityFurnaceSteel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUIFurnaceSteel extends GuiInfoContainer {
	
	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_furnace_steel.png");
	private TileEntityFurnaceSteel furnace;

	public GUIFurnaceSteel(InventoryPlayer invPlayer, TileEntityFurnaceSteel tedf) {
		super(new ContainerFurnaceSteel(invPlayer, tedf));
		furnace = tedf;
		
		this.xSize = 176;
		this.ySize = 166;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float interp) {
		super.drawScreen(mouseX, mouseY, interp);
		
		for(int i = 0; i < 3; i++) {
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 53, guiTop + 17 + 18 * i, 70, 7, mouseX, mouseY, new String[] { String.format("%,d", furnace.progress[i]) + " / " + String.format("%,d", furnace.processTime) + "TU" });
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 53, guiTop + 26 + 18 * i, 70, 7, mouseX, mouseY, new String[] { "Bonus: " + furnace.bonus[i] + "%" });
		}
		
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 151, guiTop + 18, 9, 50, mouseX, mouseY, new String[] { String.format("%,d", furnace.heat) + " / " + String.format("%,d", furnace.maxHeat) + "TU" });
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.furnace.hasCustomName() ? this.furnace.getName() : I18n.format(this.furnace.getName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		int h = furnace.heat * 48 / furnace.maxHeat;
		drawTexturedModalRect(guiLeft + 152, guiTop + 67 - h, 176, 76 - h, 7, h);
		
		for(int i = 0; i < 3; i++) {
			int p = furnace.progress[i] * 69 / furnace.processTime;
			drawTexturedModalRect(guiLeft + 54, guiTop + 18 + 18 * i, 176, 18, p, 5);
			int b = furnace.bonus[i] * 69 / 100;
			drawTexturedModalRect(guiLeft + 54, guiTop + 27 + 18 * i, 176, 23, b, 5);
			
			if(furnace.wasOn)
				drawTexturedModalRect(guiLeft + 16, guiTop + 16 + 18 * i, 176, 0, 18, 18);
		}
	}
}
