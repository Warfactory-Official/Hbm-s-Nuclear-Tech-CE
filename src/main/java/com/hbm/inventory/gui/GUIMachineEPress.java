package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineEPress;
import com.hbm.tileentity.machine.TileEntityMachineEPress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUIMachineEPress extends GuiInfoContainer {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_electric_press.png");
	private final TileEntityMachineEPress press;
	
	public GUIMachineEPress(InventoryPlayer invPlayer, TileEntityMachineEPress tedf) {
		super(new ContainerMachineEPress(invPlayer, tedf));
		press = tedf;
		
		this.xSize = 176;
		this.ySize = 186;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 52 - 34, 16, 34, press.power, press.maxPower);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		String name = this.press.hasCustomName() ? this.press.getName() : I18n.format(this.press.getName());

		this.fontRenderer.drawString(name, 89 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		int i = (int)press.getPowerScaled(34);
		drawTexturedModalRect(guiLeft + 152, guiTop + 52 - i, 176, 34 - i, 16, i);
		
		int k = press.getProgressScaled(16);
		this.drawTexturedModalRect(guiLeft + 18, guiTop + 33, 192, 0, 18, k);
	}
}
