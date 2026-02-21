package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineSiren;
import com.hbm.items.machine.ItemCassette.TrackType;
import com.hbm.tileentity.machine.TileEntityMachineSiren;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUIMachineSiren extends GuiContainer {
	
	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/gui_siren.png");
	private TileEntityMachineSiren siren;

	public GUIMachineSiren(InventoryPlayer invPlayer, TileEntityMachineSiren tedf) {
		super(new ContainerMachineSiren(invPlayer, tedf));
		siren = tedf;
		
		this.xSize = 176;
		this.ySize = 166;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.siren.hasCustomInventoryName() ? this.siren.getInventoryName() : I18n.format(this.siren.getInventoryName());
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
		
		//Draw record meta here//
		if(!siren.getCurrentType().getTrackTitle().equals(TrackType.NULL.getTrackTitle())) {
			int color = siren.getCurrentType().getColor();
			this.fontRenderer.drawString(siren.getCurrentType().getTrackTitle(), 46, 28, color);
			this.fontRenderer.drawString("Type: " + siren.getCurrentType().getType().name(), 46, 40, color);
			this.fontRenderer.drawString("Volume: " + siren.getCurrentType().getVolume(), 46, 52, color);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}
}