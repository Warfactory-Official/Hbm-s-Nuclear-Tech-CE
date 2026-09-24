package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineElectricFurnace;
import com.hbm.tileentity.machine.TileEntityMachineElectricFurnace;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUIMachineElectricFurnace extends GuiInfoContainer {
	
	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_electric_furnace.png");
	private TileEntityMachineElectricFurnace furnace;

	public GUIMachineElectricFurnace(InventoryPlayer invPlayer, TileEntityMachineElectricFurnace tedf) {
		super(new ContainerMachineElectricFurnace(invPlayer, tedf));
		furnace = tedf;
		
		this.xSize = 176;
		this.ySize = 186;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 52 - 34, 16, 34, furnace.power, TileEntityMachineElectricFurnace.maxPower);
		String[] upgradeText = new String[3];
		upgradeText[0] = I18nUtil.resolveKey("desc.gui.upgrade");
		upgradeText[1] = I18nUtil.resolveKey("desc.gui.upgrade.speed");
		upgradeText[2] = I18nUtil.resolveKey("desc.gui.upgrade.power");
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 115, guiTop + 19, 8, 8, mouseX, mouseY, upgradeText);
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.furnace.hasCustomName() ? this.furnace.getName() : I18n.format(this.furnace.getName());

		this.fontRenderer.drawString(name, 70 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		//failsafe TE clone
		//if initial TE invalidates, new TE is fetched
		//if initial ZE is still present, it'll be used instead
		//works so that container packets can still be used
		//efficiency!
		
		if(furnace.isInvalid() && furnace.getWorld().getTileEntity(furnace.getPos()) instanceof TileEntityMachineElectricFurnace)
			furnace = (TileEntityMachineElectricFurnace) furnace.getWorld().getTileEntity(furnace.getPos());
		
		if(furnace.hasPower()) {
			int p = (int) furnace.getPowerScaled(34);
			drawTexturedModalRect(guiLeft + 152, guiTop + 52 - p, 176, 64 - p, 16, p);
		}
		
		if(furnace.canProcess() && furnace.hasPower()) {
			drawTexturedModalRect(guiLeft + 45, guiTop + 20, 192, 12, 18, 16);
			drawTexturedModalRect(guiLeft + 46, guiTop + 47, 192, 28, 18, 16);
		}

		int j1 = furnace.getProgressScaled(28);
		drawTexturedModalRect(guiLeft + 43, guiTop + 36, 176, 0, j1, 12);

		this.drawInfoPanel(guiLeft + 115, guiTop + 19, 8, 8, 8);
	}
}
