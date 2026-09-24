package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineSatLinker;
import com.hbm.tileentity.machine.TileEntityMachineSatLinker;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUIMachineSatLinker extends GuiInfoContainer {
	
	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_sat_linker.png");
	private final TileEntityMachineSatLinker siren;

	public GUIMachineSatLinker(InventoryPlayer invPlayer, TileEntityMachineSatLinker tedf) {
		super(new ContainerMachineSatLinker(invPlayer, tedf));
		siren = tedf;
		
		this.xSize = 176;
		this.ySize = 186;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		String[] chipText = I18nUtil.resolveKeyArray("desc.gui.satlinker.chip");
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 12, guiTop + 28, 16, 16, guiLeft - 8, guiTop + 36 + 16, chipText);

		String[] randomText = I18nUtil.resolveKeyArray("desc.gui.satlinker.random");
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 12, guiTop + 28 + 16, 16, 16, guiLeft - 8, guiTop + 36 + 16, randomText);
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.siren.hasCustomInventoryName() ? this.siren.getInventoryName() : I18n.format(this.siren.getInventoryName());
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		this.drawInfoPanel(guiLeft + 12, guiTop + 28, 16, 16, 2);
		this.drawInfoPanel(guiLeft + 12, guiTop + 28 + 16, 16, 16, 3);
	}
}
