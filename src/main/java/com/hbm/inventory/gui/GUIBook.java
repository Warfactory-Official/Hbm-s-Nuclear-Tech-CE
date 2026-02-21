package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUIBook extends GuiContainer {

	public static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_book.png");
	
	public GUIBook(InventoryPlayer player) {
		super(new ContainerBook(player));
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		Minecraft.getMinecraft().standardGalacticFontRenderer.drawString("Extended 4-Slot Crafting", 28, 6, 4210752);
    	Minecraft.getMinecraft().standardGalacticFontRenderer.drawString("Standard Inventory", 8, this.ySize - 96 + 2, 4210752);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		super.drawDefaultBackground();
        this.mc.getTextureManager().bindTexture(texture);
        int left = (this.width - this.xSize) / 2;
        int top = (this.height - this.ySize) / 2;

        this.drawTexturedModalRect(left, top, 0, 0, this.xSize, this.ySize);

        if(this.inventorySlots.getSlot(0).getHasStack())
            this.drawTexturedModalRect(left + 29, top + 16, 176, 0, 54, 54);
		
	}
	
}
