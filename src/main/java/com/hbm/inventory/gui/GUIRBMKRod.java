package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerRBMKRod;
import com.hbm.items.machine.ItemRBMKRod;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKRod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUIRBMKRod extends GuiInfoContainer {
	
	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/reactors/gui_rbmk_element.png");
	private TileEntityRBMKRod rod;

	public GUIRBMKRod(InventoryPlayer invPlayer, TileEntityRBMKRod tedf) {
		super(new ContainerRBMKRod(invPlayer, tedf));
		rod = tedf;
		
		this.xSize = 176;
		this.ySize = 186;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = I18n.format(this.rod.getName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){
		super.drawScreen(mouseX, mouseY, partialTicks);
		super.renderHoveredToolTip(mouseX, mouseY);

		if(!rod.coldEnoughForAutoloader())
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 16, guiTop + 20, 16, 16, guiLeft - 8, guiTop + 20 + 16, new String[]{"Fuel skin temperature has exceeded 1,000°C,", "autoloaders can no longer cycle fuel!"});
		if(!rod.coldEnoughForManual())
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 16, guiTop + 36, 16, 16, guiLeft - 8, guiTop + 36 + 16, new String[]{"Fuel skin temperature has exceeded 200°C,", "fuel can no longer be removed by hand!"});
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		if(rod.inventory.getStackInSlot(0).getItem() instanceof ItemRBMKRod) {
			drawTexturedModalRect(guiLeft + 34, guiTop + 21, 176, 0, 18, 67);
			
			double depletion = 1D - ItemRBMKRod.getEnrichment(rod.inventory.getStackInSlot(0));
			int d = (int)(depletion * 67);
			drawTexturedModalRect(guiLeft + 34, guiTop + 21, 194, 0, 18, d);
			
			double xenon = ItemRBMKRod.getPoisonLevel(rod.inventory.getStackInSlot(0));
			int x = (int)(xenon * 58);
			drawTexturedModalRect(guiLeft + 126, guiTop + 82 - x, 212, 58 - x, 14, x);
		}

		if(!rod.coldEnoughForAutoloader()) this.drawInfoPanel(guiLeft - 16, guiTop + 20, 16, 16, 6);
		if(!rod.coldEnoughForManual()) this.drawInfoPanel(guiLeft - 16, guiTop + 36, 16, 16, 7);
	}
}