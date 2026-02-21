package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineAssembler;
import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.TileEntityMachineAssembler;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraft.client.renderer.GlStateManager;

public class GUIMachineAssembler extends GuiInfoContainer {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/gui_assembler.png");
	private final TileEntityMachineAssembler assembler;

	public GUIMachineAssembler(InventoryPlayer invPlayer, TileEntityMachineAssembler tile) {
		super(new ContainerMachineAssembler(invPlayer, tile));
		assembler = tile;
		
		this.xSize = 176;
		this.ySize = 222;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 116, guiTop + 70 - 52, 16, 52, assembler.power, TileEntityMachineAssembler.maxPower);
		
		if(assembler.inventory.getStackInSlot(4).getItem() == Items.AIR || assembler.inventory.getStackInSlot(4).getItem()!= ModItems.assembly_template) {

			String[] text = I18nUtil.resolveKeyArray("desc.guimachassembler");
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 16, guiTop + 36, 16, 16, guiLeft - 8, guiTop + 36 + 16, text);
		}

		String[] text = I18nUtil.resolveKeyArray("desc.guiacceptupgrades1");
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 141, guiTop + 40, 8, 8, guiLeft + 141, guiTop + 40 + 16, text);
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 141, guiTop + 40, 8, 8, guiLeft + 225, guiTop + 40 + 16 + 8, this.getUpgradeInfo(assembler));

		super.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		this.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		int i = (int)assembler.getPowerScaled(52);
		drawTexturedModalRect(guiLeft + 116, guiTop + 70 - i, 176, 52 - i, 16, i);
		if(assembler.isProgressing){
		int j = assembler.getProgressScaled(83);
		drawTexturedModalRect(guiLeft + 45, guiTop + 82, 2, 222, j, 32);
		} else {
			drawTexturedModalRect(guiLeft + 45, guiTop + 82, 2, 222, 0, 32);
		}
		
		if(assembler.inventory.getStackInSlot(4).getItem() == Items.AIR || assembler.inventory.getStackInSlot(4).getItem()!= ModItems.assembly_template) {
			this.drawInfoPanel(guiLeft - 16, guiTop + 36, 16, 16, 6);
		}
		
		this.drawInfoPanel(guiLeft + 141, guiTop + 40, 8, 8, 8);
	}

	public ItemStackHandler getInventory() {
		return assembler.inventory;
	}
}