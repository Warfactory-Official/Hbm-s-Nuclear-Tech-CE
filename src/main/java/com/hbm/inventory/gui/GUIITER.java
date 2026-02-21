package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerITER;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.AuxButtonPacket;
import com.hbm.tileentity.machine.TileEntityITER;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIITER extends GuiInfoContainer {

	public static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/reactors/gui_fusion_multiblock.png");
	private TileEntityITER iter;

	public GUIITER(InventoryPlayer invPlayer, TileEntityITER laser) {
		super(new ContainerITER(invPlayer, laser));
		this.iter = laser;

		this.xSize = 176;
		this.ySize = 222;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 71, guiTop + 108, 34, 16, iter.power, TileEntityITER.maxPower);

		iter.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 26, guiTop + 54, 16, 52); //Water
		iter.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 134, guiTop + 54, 16, 52); //Steam
		iter.plasma.renderTankInfo(this, mouseX, mouseY, guiLeft + 71, guiTop + 54, 34, 34); //Plasma
		
		String text = "Magnets are " + ((iter.isOn && iter.power >= TileEntityITER.powerReq) ? "ON" : "OFF");
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 76, guiTop + 94, 24, 12, mouseX, mouseY, new String[] { text });
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	protected void mouseClicked(int x, int y, int i) throws IOException {
    	super.mouseClicked(x, y, i);

    	if(guiLeft + 52 <= x && guiLeft + 52 + 18 > x && guiTop + 107 < y && guiTop + 107 + 18 >= y) {

    		//toggle the magnets
			playClickSound();
    		PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(iter.getPos(), 0, 0));
    	}
    	super.renderHoveredToolTip(x, y);
    }

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.iter.hasCustomName() ? this.iter.getName() : I18n.format(this.iter.getName());

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		if(iter.isOn)
			drawTexturedModalRect(guiLeft + 52, guiTop + 107, 176, 0, 18, 18);

		if(iter.isOn && iter.power >= TileEntityITER.powerReq)
			drawTexturedModalRect(guiLeft + 76, guiTop + 94, 194, 0, 24, 12);

		if(iter.plasma.getFill() > 0 && iter.getShield() >= iter.plasma.getTankType().temperature)
			drawTexturedModalRect(guiLeft + 97, guiTop + 17, 218, 0, 18, 18);
		
		int i = (int)iter.getPowerScaled(34);
		drawTexturedModalRect(guiLeft + 71, guiTop + 108, 176, 25, i, 16);
		
		int j = (int)iter.getProgressScaled(17);
		drawTexturedModalRect(guiLeft + 44, guiTop + 22, 176, 18, j, 7);

		for(int t = 0; t < 2; t++) {
			iter.tanks[t].renderTank(guiLeft + 26 + 108 * t, guiTop + 106, this.zLevel, 16, 52);
		}

		iter.plasma.renderTank(guiLeft + 71, guiTop + 88, this.zLevel, 34, 34);
	}
}