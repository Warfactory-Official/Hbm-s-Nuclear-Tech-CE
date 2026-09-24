package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineSatDock;
import com.hbm.tileentity.machine.TileEntityMachineSatDock;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUIMachineSatDock extends GuiInfoContainer {

	public static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_sat_dock.png");
	private final TileEntityMachineSatDock tileSatelliteDock;
	
	public GUIMachineSatDock(InventoryPlayer invPlayer, TileEntityMachineSatDock tedf) {
		super(new ContainerMachineSatDock(invPlayer, tedf));
		tileSatelliteDock = tedf;

		this.xSize = 176;
		this.ySize = 186;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		String[] text = I18nUtil.resolveKeyArray("desc.gui.satdock.desc");
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 7, guiTop + 36, 16, 16, guiLeft - 7, guiTop + 36 + 16, text);
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.tileSatelliteDock.hasCustomName() ? this.tileSatelliteDock.getName() : I18n.format(this.tileSatelliteDock.getName());

		this.fontRenderer.drawString(name, 115 - this.fontRenderer.getStringWidth(name) / 2, 6, 0x404040);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 0x404040);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		this.drawInfoPanel(guiLeft - 7, guiTop + 36, 16, 16, 2);
	}
}
