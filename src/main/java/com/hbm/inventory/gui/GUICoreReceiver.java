package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerCoreReceiver;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityCoreReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

import java.math.BigDecimal;

public class GUICoreReceiver extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/dfc/gui_receiver.png");
	private TileEntityCoreReceiver receiver;
	
	public GUICoreReceiver(EntityPlayer invPlayer, TileEntityCoreReceiver tedf) {
		super(new ContainerCoreReceiver(invPlayer, tedf));
		receiver = tedf;
		
		this.xSize = 176;
		this.ySize = 166;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		receiver.tank.renderTankInfo(this, mouseX, mouseY, guiLeft + 17, guiTop + 17, 16, 52);
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		String name = this.receiver.hasCustomName() ? this.receiver.getName() : I18n.format(this.receiver.getName());
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);

		this.fontRenderer.drawString("Input:", 54, 22, 4210752);
		String sparks = Library.getShortNumber(receiver.joules) + "SPK";
		this.fontRenderer.drawString(sparks, 161-this.fontRenderer.getStringWidth(sparks), 22, 0x4EB3DB);
		this.fontRenderer.drawString("Output:", 54, 58, 4210752);
		String power = Library.getShortNumber(new BigDecimal(receiver.joules).multiply(new BigDecimal(100000))) + "HE/s";
		this.fontRenderer.drawString(power, 161-this.fontRenderer.getStringWidth(power), 58, 0x4EB3DB);
		
		String inventory = I18n.format("container.inventory");
		this.fontRenderer.drawString(inventory, this.xSize - 8 - this.fontRenderer.getStringWidth(inventory), this.ySize - 96 + 2, 4210752);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		receiver.tank.renderTank(guiLeft + 17, guiTop + 69, this.zLevel, 16, 52);
	}
}