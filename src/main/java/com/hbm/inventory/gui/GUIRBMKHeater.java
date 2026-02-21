package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerRBMKHeater;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKHeater;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUIRBMKHeater extends GuiInfoContainer {
	
	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/reactors/gui_rbmk_heater.png");
	private TileEntityRBMKHeater rod;

	public GUIRBMKHeater(InventoryPlayer invPlayer, TileEntityRBMKHeater tedf) {
		super(new ContainerRBMKHeater(invPlayer, tedf));
		rod = tedf;
		
		this.xSize = 176;
		this.ySize = 186;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		rod.feed.renderTankInfo(this, mouseX, mouseY, guiLeft + 68, guiTop + 24, 16, 58);
		rod.steam.renderTankInfo(this, mouseX, mouseY, guiLeft + 126, guiTop + 24, 16, 58);
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = I18n.format(this.rod.getName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		rod.feed.renderTank(guiLeft + 68, guiTop + 82, this.zLevel, 14, 58);
		rod.steam.renderTank(guiLeft + 126, guiTop + 82, this.zLevel, 14, 58);
	}
}
