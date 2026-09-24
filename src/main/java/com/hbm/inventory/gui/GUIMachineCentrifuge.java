package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerCentrifuge;
import com.hbm.tileentity.machine.TileEntityMachineCentrifuge;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUIMachineCentrifuge extends GuiInfoContainer {

	public static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_centrifuge.png");
	private final TileEntityMachineCentrifuge centrifuge;
	
	public GUIMachineCentrifuge(InventoryPlayer invPlayer, TileEntityMachineCentrifuge tedf) {
		super(new ContainerCentrifuge(invPlayer, tedf));
		centrifuge = tedf;

		this.xSize = 182;
		this.ySize = 189;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
		super.renderHoveredToolTip(mouseX, mouseY);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 18, 16, 37, centrifuge.power, TileEntityMachineCentrifuge.maxPower);

		String[] upgradeText = new String[4];
		upgradeText[0] = I18nUtil.resolveKey("desc.gui.upgrade");
		upgradeText[1] = I18nUtil.resolveKey("desc.gui.upgrade.speed");
		upgradeText[2] = I18nUtil.resolveKey("desc.gui.upgrade.power");
		upgradeText[3] = I18nUtil.resolveKey("desc.gui.upgrade.overdrive");
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 160, guiTop + 16, 8, 8, mouseX, mouseY, upgradeText);
	}

	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		String name = this.centrifuge.hasCustomName() ? this.centrifuge.getName() : I18n.format(this.centrifuge.getDefaultName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(centrifuge.hasPower()) {
			int i1 = (int) centrifuge.getPowerRemainingScaled(37);
			drawTexturedModalRect(guiLeft + 8, guiTop + 55 - i1, 182, 37 - i1, 16, i1);
		}

		if(centrifuge.isProcessing()) {
			int p = centrifuge.getCentrifugeProgressScaled(145);

			for(int i = 0; i < 4; i++) {
				int h = Math.min(p, 36);
				drawTexturedModalRect(guiLeft + 72 + i * 20, guiTop + 57 - h, 182, 73 - h, 12, h);
				p -= h;
				if(p <= 0)
					break;
			}
		}

		this.drawInfoPanel(guiLeft + 160, guiTop + 16, 8, 8, 8);
	}
}
