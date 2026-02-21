package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerNukeMan;
import com.hbm.tileentity.bomb.TileEntityNukeMan;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUINukeMan extends GuiContainer {

	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/fatManSchematic.png");
	private TileEntityNukeMan testNuke;
	
	public GUINukeMan(InventoryPlayer invPlayer, TileEntityNukeMan tedf) {
		super(new ContainerNukeMan(invPlayer, tedf));
		testNuke = tedf;
		
		this.xSize = 176;
		this.ySize = 166;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		String name = this.testNuke.hasCustomInventoryName() ? this.testNuke.getInventoryName() : I18n.format(this.testNuke.getInventoryName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		if(testNuke.exp1())
		{
			drawTexturedModalRect(guiLeft + 82, guiTop + 19, 176, 0, 24, 24);
		}
		
		if(testNuke.exp2())
		{
			drawTexturedModalRect(guiLeft + 106, guiTop + 19, 200, 0, 24, 24);
		}
		
		if(testNuke.exp3())
		{
			drawTexturedModalRect(guiLeft + 82, guiTop + 43, 176, 24, 24, 24);
		}
		
		if(testNuke.exp4())
		{
			drawTexturedModalRect(guiLeft + 106, guiTop + 43, 200, 24, 24, 24);
		}

		if(testNuke.isReady())
		{
			drawTexturedModalRect(guiLeft + 134, guiTop + 35, 176, 48, 16, 16);
		}
	}
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}
}
