package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerDiFurnaceRTG;
import com.hbm.tileentity.machine.TileEntityDiFurnaceRTG;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUIDiFurnaceRTG extends GuiInfoContainer {
	public static ResourceLocation texture = new ResourceLocation(Tags.MODID, "textures/gui/processing/gui_rtg_difurnace.png");
	private TileEntityDiFurnaceRTG bFurnace;

	public GUIDiFurnaceRTG(InventoryPlayer playerInv, TileEntityDiFurnaceRTG te) {
		super(new ContainerDiFurnaceRTG(playerInv, te));
		bFurnace = te;
		this.xSize = 176;
		this.ySize = 166;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
		String[] descText = I18nUtil.resolveKeyArray("desc.gui.rtgBFurnace.desc", TileEntityDiFurnaceRTG.maxRTGPower);
		String[] heatText = I18nUtil.resolveKeyArray("desc.gui.rtg.heat", bFurnace.getPower());
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 15, guiTop + 36, 16, 16, guiLeft - 8, guiTop + 36 + 16, descText);
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 58, guiTop + 36, 18, 16, mouseX, mouseY, heatText);
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
		String name = I18n.format(bFurnace.getDefaultName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		if(bFurnace.isInvalid()) {
			TileEntity te = bFurnace.getWorld().getTileEntity(bFurnace.getPos());
			if(te instanceof TileEntityDiFurnaceRTG) {
				bFurnace = (TileEntityDiFurnaceRTG) te;
			}
		}

		if(bFurnace.hasPower())
			drawTexturedModalRect(guiLeft + 58, guiTop + 36, 176, 31, 18, 16);

		int p = bFurnace.getDiFurnaceProgressScaled(24);
		drawTexturedModalRect(guiLeft + 101, guiTop + 35, 176, 14, p + 1, 17);
		this.drawInfoPanel(guiLeft - 15, guiTop + 36, 16, 16, 3);
	}
}
