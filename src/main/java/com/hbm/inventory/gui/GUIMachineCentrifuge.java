package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerCentrifuge;
import com.hbm.tileentity.machine.TileEntityMachineCentrifuge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUIMachineCentrifuge extends GuiInfoContainer {

	public static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/gui_centrifuge.png");
	private TileEntityMachineCentrifuge centrifuge;
	
	public GUIMachineCentrifuge(InventoryPlayer invPlayer, TileEntityMachineCentrifuge tedf) {
		super(new ContainerCentrifuge(invPlayer, tedf));
		centrifuge = tedf;
		
		this.xSize = 176;
		this.ySize = 186;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 9, guiTop + 47 - 35, 16, 35, centrifuge.power, TileEntityMachineCentrifuge.maxPower);
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		// String name = this.centrifuge.hasCustomInventoryName() ? this.centrifuge.getInventoryName() : I18n.format(this.centrifuge.getInventoryName());
		
		// this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		if(centrifuge.hasPower()){
			int i1 = (int)centrifuge.getPowerRemainingScaled(35);
			drawTexturedModalRect(guiLeft + 9, guiTop + 48 - i1, 176, 35 - i1, 16, i1);
		}

		if(centrifuge.isProcessing()){
			int p = centrifuge.getCentrifugeProgressScaled(145);

			for(int i = 0; i < 4; i++) {
				int h = Math.min(p, 36);
				drawTexturedModalRect(guiLeft + 65 + i * 20, guiTop + 50 - h, 176, 71 - h, 12, h);
				p -= h;
				if(p <= 0)
					break;
			}
		}
	}
}
