package com.hbm.inventory.gui;

import java.util.List;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerBlastFurnace;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.tileentity.machine.TileEntityMachineBlastFurnace;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

public class GUIBlastFurnace extends GuiInfoContainer {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_blast_furnace.png");
	private final TileEntityMachineBlastFurnace furnace;

	public GUIBlastFurnace(InventoryPlayer invPlayer, TileEntityMachineBlastFurnace tedf) {
		super(new ContainerBlastFurnace(invPlayer, tedf));
		furnace = tedf;

		this.xSize = 176;
		this.ySize = 222;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);

		if(this.mc.player.inventory.getItemStack().isEmpty()) {
			Slot slot = this.inventorySlots.inventorySlots.get(0);
			if(this.isMouseOverSlot(slot, mouseX, mouseY) && !slot.getHasStack()) {
				List<String> bonuses = this.furnace.burnModule.getHeatDesc();
				if(!bonuses.isEmpty()) this.drawHoveringText(bonuses, mouseX, mouseY);
			}
		}

		String label = "Speed: " + (int) (furnace.speed * 100) + "%";
		drawCustomInfoStat(mouseX, mouseY, guiLeft + 79, guiTop + 62, 18, 18, mouseX, mouseY, new String[] { label });

		furnace.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 25, guiTop + 71, 18, 18);
		furnace.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 25, guiTop + 17, 18, 18);

		super.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.furnace.hasCustomName() ? this.furnace.getName() : I18n.format(this.furnace.getDefaultName());

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int fuel = (int) Math.round((double) furnace.fuel * 26D / (double) TileEntityMachineBlastFurnace.MAX_FUEL);
		int prog = (int) Math.round(furnace.progress * (88D - fuel));

		drawTexturedModalRect(guiLeft + 62, guiTop + 106 - prog - fuel, 176, 102 - prog - fuel, 56, prog);

		drawTexturedModalRect(guiLeft + 62, guiTop + 106 - fuel, 176, 128 - fuel, 56, fuel);

		if(furnace.isProgressing) {
			drawTexturedModalRect(guiLeft + 81, guiTop + 64, 176, 0, 14, 14);
		}

		GUIElements.drawSmoothGauge(guiLeft + 34, guiTop + 80, this.zLevel, (double) furnace.tanks[0].getFill() / (double) furnace.tanks[0].getMaxFill(), 5, 2, 1, 0x800000);
		GUIElements.drawSmoothGauge(guiLeft + 34, guiTop + 26, this.zLevel, (double) furnace.tanks[1].getFill() / (double) furnace.tanks[1].getMaxFill(), 5, 2, 1, 0x800000);
	}
}
