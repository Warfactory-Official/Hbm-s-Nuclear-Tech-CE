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
	
	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/GUIElectricFurnace.png");
	private TileEntityMachineElectricFurnace diFurnace;

	public GUIMachineElectricFurnace(InventoryPlayer invPlayer, TileEntityMachineElectricFurnace tedf) {
		super(new ContainerMachineElectricFurnace(invPlayer, tedf));
		diFurnace = tedf;
		
		this.xSize = 176;
		this.ySize = 166;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 20, guiTop + 69 - 52, 16, 52, diFurnace.power, TileEntityMachineElectricFurnace.maxPower);
		String[] upgradeText = new String[3];
		upgradeText[0] = I18nUtil.resolveKey("desc.gui.upgrade");
		upgradeText[1] = I18nUtil.resolveKey("desc.gui.upgrade.speed");
		upgradeText[2] = I18nUtil.resolveKey("desc.gui.upgrade.power");
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 151, guiTop + 19, 8, 8, mouseX, mouseY, upgradeText);
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.diFurnace.hasCustomName() ? this.diFurnace.getName() : I18n.format(this.diFurnace.getName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
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
		
		if(diFurnace.isInvalid() && diFurnace.getWorld().getTileEntity(diFurnace.getPos()) instanceof TileEntityMachineElectricFurnace)
			diFurnace = (TileEntityMachineElectricFurnace) diFurnace.getWorld().getTileEntity(diFurnace.getPos());
		
		if(diFurnace.hasPower()) {
			int i = (int)diFurnace.getPowerScaled(52);
			drawTexturedModalRect(guiLeft + 20, guiTop + 69 - i, 200, 52 - i, 16, i);
		}
		
		if(diFurnace.canProcess() && diFurnace.hasPower()) {
			drawTexturedModalRect(guiLeft + 56, guiTop + 35, 176, 0, 16, 16);
		}

		int j1 = diFurnace.getProgressScaled(24);
		drawTexturedModalRect(guiLeft + 79, guiTop + 34, 176, 17, j1 + 1, 17);
		this.drawInfoPanel(guiLeft + 151, guiTop + 19, 8, 8, 8);
	}
}
