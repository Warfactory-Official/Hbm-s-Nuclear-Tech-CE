package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerNukeN2;
import com.hbm.items.ModItems;
import com.hbm.tileentity.bomb.TileEntityNukeN2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUINukeN2 extends GuiContainer {
	
	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/n2Schematic.png");
	private TileEntityNukeN2 testNuke;
	
	public GUINukeN2(InventoryPlayer invPlayer, TileEntityNukeN2 tedf) {
		super(new ContainerNukeN2(invPlayer, tedf));
		testNuke = tedf;
		
		this.xSize = 176;
		this.ySize = 222;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		String name = this.testNuke.hasCustomInventoryName() ? this.testNuke.getInventoryName() : I18n.format(this.testNuke.getInventoryName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), this.xSize-this.fontRenderer.getStringWidth(I18n.format("container.inventory"))-6, this.ySize - 96 + 2, 4210752);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		int count = 0;
		
		for(int i = 0; i < 12; i++) {
			if(this.testNuke.inventory.getStackInSlot(i).getItem() == ModItems.n2_charge)
				count++;
		}
		
		if(count > 0)
		{
			drawTexturedModalRect(guiLeft + 35, guiTop + 120 - 6 * count, 176, 0, 34, 6 * count);
		}
	}
}
