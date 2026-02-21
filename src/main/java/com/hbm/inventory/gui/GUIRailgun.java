package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerRailgun;
import com.hbm.packet.toserver.AuxButtonPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.bomb.TileEntityRailgun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;

public class GUIRailgun extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/gui_railgun.png");
	private TileEntityRailgun railgun;

	public GUIRailgun(InventoryPlayer invPlayer, TileEntityRailgun tedf) {
		super(new ContainerRailgun(invPlayer, tedf));
		railgun = tedf;

		this.xSize = 176;
		this.ySize = 166;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		super.renderHoveredToolTip(mouseX, mouseY);
		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 51, 160, 16, this.railgun.getPower(), this.railgun.getMaxPower());
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.railgun.hasCustomInventoryName() ? this.railgun.getInventoryName() : I18n.format(this.railgun.getInventoryName());

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	protected void mouseClicked(int x, int y, int i) throws IOException {
		super.mouseClicked(x, y, i);
		
		if(!isTargeting() && !isFiring()) {
			if(guiLeft + 79 <= x && guiLeft + 79 + 18 > x && guiTop + 16 < y && guiTop + 16 + 18 >= y) {
				PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(railgun.getPos().getX(), railgun.getPos().getY(), railgun.getPos().getZ(), 0, 0));
			}

			if(guiLeft + 106 <= x && guiLeft + 106 + 18 > x && guiTop + 16 < y && guiTop + 16 + 18 >= y) {
				PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(railgun.getPos().getX(), railgun.getPos().getY(), railgun.getPos().getZ(), 0, 1));
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int j1 = (int) railgun.getPowerScaled(160);
		drawTexturedModalRect(guiLeft + 8, guiTop + 53, 8, 166, j1, 16);

		if(isTargeting())
			drawTexturedModalRect(guiLeft + 79, guiTop + 16, 176, 0, 18, 18);

		if(isFiring())
			drawTexturedModalRect(guiLeft + 106, guiTop + 16, 194, 0, 18, 18);
	}

	private boolean isTargeting() {

		return System.currentTimeMillis() < railgun.startTime + TileEntityRailgun.cooldownDurationMillis;
	}

	private boolean isFiring() {

		return System.currentTimeMillis() < railgun.fireTime + TileEntityRailgun.cooldownDurationMillis;
	}
}
