package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerNukeFleija;
import com.hbm.items.ModItems;
import com.hbm.tileentity.bomb.TileEntityNukeFleija;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUINukeFleija extends GuiContainer {

	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/fleijaSchematic.png");
	private TileEntityNukeFleija testNuke;
	
	public GUINukeFleija(InventoryPlayer invPlayer, TileEntityNukeFleija tedf) {
		super(new ContainerNukeFleija(invPlayer, tedf));
		testNuke = tedf;
		
		this.xSize = 176;
		this.ySize = 222;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String name = this.testNuke.hasCustomInventoryName() ? this.testNuke.getInventoryName() : I18n.format(this.testNuke.getInventoryName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		if(this.testNuke.inventory.getStackInSlot(0).getItem() == ModItems.fleija_igniter)
		{
			drawTexturedModalRect(guiLeft + 7, guiTop + 88, 176, 0, 30, 20);
		}
		
		if(this.testNuke.inventory.getStackInSlot(1).getItem() == ModItems.fleija_igniter)
		{
			drawTexturedModalRect(guiLeft + 139, guiTop + 88, 206, 0, 30, 20);
		}
		
		if(this.testNuke.inventory.getStackInSlot(2).getItem() == ModItems.fleija_propellant)
		{
			drawTexturedModalRect(guiLeft + 57, guiTop + 77, 176, 62, 18, 14);
		}
		
		if(this.testNuke.inventory.getStackInSlot(3).getItem() == ModItems.fleija_propellant)
		{
			drawTexturedModalRect(guiLeft + 57, guiTop + 91, 176, 76, 18, 14);
		}
		
		if(this.testNuke.inventory.getStackInSlot(4).getItem() == ModItems.fleija_propellant)
		{
			drawTexturedModalRect(guiLeft + 57, guiTop + 105, 176, 90, 18, 14);
		}
		
		if(this.testNuke.inventory.getStackInSlot(5).getItem() == ModItems.fleija_core)
		{
			drawTexturedModalRect(guiLeft + 85, guiTop + 77, 176, 20, 18, 15);
		}
		
		if(this.testNuke.inventory.getStackInSlot(6).getItem() == ModItems.fleija_core)
		{
			drawTexturedModalRect(guiLeft + 103, guiTop + 77, 194, 20, 18, 15);
		}
		
		if(this.testNuke.inventory.getStackInSlot(7).getItem() == ModItems.fleija_core)
		{
			drawTexturedModalRect(guiLeft + 85, guiTop + 92, 176, 35, 18, 12);
		}
		
		if(this.testNuke.inventory.getStackInSlot(8).getItem() == ModItems.fleija_core)
		{
			drawTexturedModalRect(guiLeft + 103, guiTop + 92, 194, 35, 18, 12);
		}
		
		if(this.testNuke.inventory.getStackInSlot(9).getItem() == ModItems.fleija_core)
		{
			drawTexturedModalRect(guiLeft + 85, guiTop + 104, 176, 47, 18, 15);
		}
		
		if(this.testNuke.inventory.getStackInSlot(10).getItem() == ModItems.fleija_core)
		{
			drawTexturedModalRect(guiLeft + 103, guiTop + 104, 194, 47, 18, 15);
		}
		
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

}
