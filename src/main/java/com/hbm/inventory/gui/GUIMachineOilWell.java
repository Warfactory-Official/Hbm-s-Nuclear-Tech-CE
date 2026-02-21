package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineOilWell;
import com.hbm.tileentity.machine.oil.TileEntityOilDrillBase;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GUIMachineOilWell extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_well.png");
	private TileEntityOilDrillBase derrick;
	
	public GUIMachineOilWell(InventoryPlayer invPlayer, TileEntityOilDrillBase tedf) {
		super(new ContainerMachineOilWell(invPlayer, tedf));
		derrick = tedf;
		
		this.xSize = 176;
		this.ySize = 222;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		derrick.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 62, guiTop + 69 - 52, 16, 52);
		derrick.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 107, guiTop + 69 - 52, 16, 52);

		if(derrick.tanks.length >= 3) {
			derrick.tanks[2].renderTankInfo(this, mouseX, mouseY, guiLeft + 40, guiTop + 37, 6, 32);
		}

		String[] upgradeText = new String[4];
		upgradeText[0] = I18nUtil.resolveKey("desc.gui.upgrade");
		upgradeText[1] = I18nUtil.resolveKey("desc.gui.upgrade.speed");
		upgradeText[2] = I18nUtil.resolveKey("desc.gui.upgrade.power");
		upgradeText[3] = I18nUtil.resolveKey("desc.gui.upgrade.afterburner");
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 156, guiTop + 3, 8, 8, mouseX, mouseY, upgradeText);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 17, 16, 34, derrick.power, derrick.getMaxPower());
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		String name = this.derrick.hasCustomName() ? this.derrick.getName() : I18n.format(this.derrick.getName());

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 151 + 2, 4210752);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int i = (int)(derrick.getPower() * 34 / derrick.getMaxPower());
		drawTexturedModalRect(guiLeft + 8, guiTop + 51 - i, 176, 34 - i, 16, i);

		int k = derrick.indicator;

		if(k != 0)
			drawTexturedModalRect(guiLeft + 35, guiTop + 17, 176 + (k - 1) * 16, 52, 16, 16);

		if(derrick.tanks.length < 3) {
			drawTexturedModalRect(guiLeft + 34, guiTop + 36, 192, 0, 18, 34);
		}

		derrick.tanks[0].renderTank(guiLeft + 62, guiTop + 69, this.zLevel, 16, 52);
		derrick.tanks[1].renderTank(guiLeft + 107, guiTop + 69, this.zLevel, 16, 52);

		if(derrick.tanks.length > 2) {
			derrick.tanks[2].renderTank(guiLeft + 40, guiTop + 69, this.zLevel, 6, 32);
		}

		this.drawInfoPanel(guiLeft + 156, guiTop + 3, 8, 8, 8);
	}
}
