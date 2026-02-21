package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerFurnaceIron;
import com.hbm.tileentity.machine.TileEntityFurnaceIron;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUIFurnaceIron extends GuiInfoContainer {
	
	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_furnace_iron.png");
	private TileEntityFurnaceIron furnace;

	public GUIFurnaceIron(InventoryPlayer invPlayer, TileEntityFurnaceIron tedf) {
		super(new ContainerFurnaceIron(invPlayer, tedf));
		furnace = tedf;
		
		this.xSize = 176;
		this.ySize = 166;
	}
	
	@Override
	public void drawScreen(int x, int y, float interp) {
		super.drawScreen(x, y, interp);

		this.drawCustomInfoStat(x, y, guiLeft + 52, guiTop + 35, 71, 7, x, y, new String[] { (furnace.progress * 100 / Math.max(furnace.processingTime, 1)) + "%" });
		this.drawCustomInfoStat(x, y, guiLeft + 52, guiTop + 44, 71, 7, x, y, new String[] { (furnace.burnTime / 20) + "s" });
		super.renderHoveredToolTip(x, y);
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
		
		int i = furnace.progress * 70 / Math.max(furnace.processingTime, 1);
		drawTexturedModalRect(guiLeft + 53, guiTop + 36, 176, 18, i, 5);
		
		int j = furnace.burnTime * 70 / Math.max(furnace.maxBurnTime, 1);
		drawTexturedModalRect(guiLeft + 53, guiTop + 45, 176, 23, j, 5);
		
		if(furnace.canSmelt())
			drawTexturedModalRect(guiLeft + 70, guiTop + 16, 176, 0, 18, 18);
	}
}
