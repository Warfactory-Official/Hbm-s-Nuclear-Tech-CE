package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineShredder;
import com.hbm.tileentity.machine.TileEntityMachineShredder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIMachineShredder extends GuiInfoContainer {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/gui_shredder.png");
	private TileEntityMachineShredder shredder;

	public GUIMachineShredder(InventoryPlayer invPlayer, TileEntityMachineShredder teMachineShredder) {
		super(new ContainerMachineShredder(invPlayer, teMachineShredder));
		shredder = teMachineShredder;
		
		this.xSize = 176;
		this.ySize = 233;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 106 - 88, 16, 88, shredder.power, TileEntityMachineShredder.maxPower);
		
		boolean flag = false;

		if(shredder.getGearLeft() == 0 || shredder.getGearLeft() == 3)
			flag = true;

		if(shredder.getGearRight() == 0 || shredder.getGearRight() == 3)
			flag = true;
		
		if(flag) {
			String[] text = new String[] { "Error: Shredder blades are broken or missing!" };
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 16, guiTop + 36, 16, 16, guiLeft - 8, guiTop + 36 + 16, text);
		}
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawDefaultBackground();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		if(shredder.power > 0) {
			int i = (int)shredder.getPowerScaled(88);
			drawTexturedModalRect(guiLeft + 8, guiTop + 106 - i, 176, 160 - i, 16, i);
		}
		
		int j1 = shredder.getDiFurnaceProgressScaled(34);
		drawTexturedModalRect(guiLeft + 63, guiTop + 89, 176, 54, j1 + 1, 18);
		
		boolean flag = false;
		
		if(shredder.getGearLeft() != 0)
		{
			int i = shredder.getGearLeft();
			if(i == 1)
			{
				drawTexturedModalRect(guiLeft + 43, guiTop + 71, 176, 0, 18, 18);
			}
			if(i == 2)
			{
				drawTexturedModalRect(guiLeft + 43, guiTop + 71, 176, 18, 18, 18);
			}
			if(i == 3)
			{
				drawTexturedModalRect(guiLeft + 43, guiTop + 71, 176, 36, 18, 18);
				flag = true;
			}
		} else {
			flag = true;
		}
		
		if(shredder.getGearRight() != 0)
		{
			int i = shredder.getGearRight();
			if(i == 1)
			{
				drawTexturedModalRect(guiLeft + 79, guiTop + 71, 194, 0, 18, 18);
			}
			if(i == 2)
			{
				drawTexturedModalRect(guiLeft + 79, guiTop + 71, 194, 18, 18, 18);
			}
			if(i == 3)
			{
				drawTexturedModalRect(guiLeft + 79, guiTop + 71, 194, 36, 18, 18);
				flag = true;
			}
		} else {
			flag = true;
		}

		if(flag)
			this.drawInfoPanel(guiLeft - 16, guiTop + 36, 16, 16, 6);
	}
}
