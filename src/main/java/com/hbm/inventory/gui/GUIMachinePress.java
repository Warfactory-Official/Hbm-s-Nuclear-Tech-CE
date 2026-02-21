package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachinePress;
import com.hbm.render.util.GaugeUtil;
import com.hbm.tileentity.machine.TileEntityMachinePress;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

import java.util.Collections;

public class GUIMachinePress extends GuiInfoContainer {
	private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID + ":textures/gui/gui_press.png");
	private final TileEntityMachinePress press;

	public GUIMachinePress(InventoryPlayer invPlayer, TileEntityMachinePress te) {
		super(new ContainerMachinePress(invPlayer, te));
		this.press = te;
		this.xSize = 176;
		this.ySize = 166;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 25, guiTop + 16, 18, 18, mouseX, mouseY, Collections.singletonList((press.speed * 100 / TileEntityMachinePress.maxSpeed) + "%"));
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 25, guiTop + 34, 18, 18, mouseX, mouseY, Collections.singletonList((press.burnTime / 200) + " operations left"));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String name = I18n.format(this.press.getDefaultName());
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.mc.getTextureManager().bindTexture(TEXTURE);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		if (this.press.burnTime > 0) {
			this.drawTexturedModalRect(guiLeft + 27, guiTop + 36, 176, 0, 14, 14);
		}
		int progress = this.press.getProgressScaled(16);
		this.drawTexturedModalRect(guiLeft + 79, guiTop + 35, 194, 0, 18, progress);
		double speed = (double) this.press.speed / (double) TileEntityMachinePress.maxSpeed;
		GaugeUtil.drawSmoothGauge(guiLeft + 34, guiTop + 25, this.zLevel, speed, 5, 2, 1, 0x7f0000);
	}
}