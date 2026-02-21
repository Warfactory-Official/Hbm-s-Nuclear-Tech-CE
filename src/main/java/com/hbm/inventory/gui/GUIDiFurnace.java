package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerDiFurnace;
import com.hbm.tileentity.machine.TileEntityDiFurnace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.renderer.GlStateManager;

import java.util.Collections;

public class GUIDiFurnace extends GuiInfoContainer {
	public static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/GUIDiFurnace.png");
	private final TileEntityDiFurnace diFurnace;

	public GUIDiFurnace(InventoryPlayer invPlayer, TileEntityDiFurnace tedf) {
		super(new ContainerDiFurnace(invPlayer, tedf));
		diFurnace = tedf;

		this.xSize = 176;
		this.ySize = 166;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = I18n.format(this.diFurnace.getDefaultName());

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(diFurnace.hasPower())
		{
			int i1 = diFurnace.getPowerRemainingScaled(52);
			drawTexturedModalRect(guiLeft + 44, guiTop + 70 - i1, 201, 53 - i1, 16, i1);
		}

		int j1 = diFurnace.getDiFurnaceProgressScaled(24);
		if(diFurnace.isProcessing())
			drawTexturedModalRect(guiLeft + 101, guiTop + 35, 176, 14, j1 + 1, 17);

		if(diFurnace.hasPower() && diFurnace.canProcess()) {
			drawTexturedModalRect(guiLeft + 63, guiTop + 37, 176, 0, 14, 14);
		}

	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.mc.player.inventory.getItemStack().isEmpty()) {
			for (int i = 0; i < 3; i++) {
				Slot slot = this.inventorySlots.getSlot(i);

				if (this.isMouseOverSlot(slot, mouseX, mouseY)) {
					byte dir = switch (i) {
						case 0 -> diFurnace.sideUpper;
						case 1 -> diFurnace.sideLower;
						case 2 -> diFurnace.sideFuel;
						default -> 0;
					};
					String label = TextFormatting.YELLOW + "Accepts items from: " + EnumFacing.byIndex(dir).getName();
					this.drawHoveringText(Collections.singletonList(label), mouseX, mouseY);
					return;
				}
			}
		}
		drawCustomInfoStat(mouseX, mouseY, guiLeft + 43, guiTop + 17, 18, 54, mouseX, mouseY, new String[] {diFurnace.getPowerRemainingScaled(100) + "%"});
		super.renderHoveredToolTip(mouseX, mouseY);
	}
}