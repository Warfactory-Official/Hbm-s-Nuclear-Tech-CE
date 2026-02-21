package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineTurbofan;
import com.hbm.render.util.GaugeUtil;
import com.hbm.tileentity.machine.TileEntityMachineTurbofan;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUIMachineTurbofan extends GuiInfoContainer {
	
	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/generators/gui_turbofan.png");
	private final TileEntityMachineTurbofan turbofan;

	public GUIMachineTurbofan(InventoryPlayer invPlayer, TileEntityMachineTurbofan tedf) {
		super(new ContainerMachineTurbofan(invPlayer, tedf));
		turbofan = tedf;
		
		this.xSize = 176;
		this.ySize = 203;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		turbofan.tank.renderTankInfo(this, mouseX, mouseY, guiLeft + 35, guiTop + 17, 34, 52);
		if(turbofan.showBlood) turbofan.blood.renderTankInfo(this, mouseX, mouseY, guiLeft + 98, guiTop + 17, 16, 16);
		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 143, guiTop + 17, 16, 52, turbofan.power, turbofan.maxPower);

		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.turbofan.hasCustomName() ? this.turbofan.getName() : I18n.format(this.turbofan.getName());
		
		this.fontRenderer.drawString(name, 43 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		if(turbofan.power > 0) {
			int i = (int)turbofan.getPowerScaled(52);
			drawTexturedModalRect(guiLeft + 152 - 9, guiTop + 69 - i, 176 + 16, 52 - i, 16, i);
		}
		
		if(turbofan.afterburner > 0) {
			int a = Math.min(turbofan.afterburner, 6);
			drawTexturedModalRect(guiLeft + 98, guiTop + 44, 176, (a - 1) * 16, 16, 16);
		}

		if(turbofan.showBlood) GaugeUtil.renderGauge(GaugeUtil.Gauge.ROUND_SMALL, guiLeft + 97, guiTop + 16, this.zLevel, (double) turbofan.blood.getFill() / (double) turbofan.blood.getMaxFill());
		turbofan.tank.renderTank(guiLeft + 35, guiTop + 69, this.zLevel, 34, 52);
	}
}
