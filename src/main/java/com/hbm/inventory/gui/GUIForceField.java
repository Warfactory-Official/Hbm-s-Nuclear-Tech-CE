package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerForceField;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.AuxButtonPacket;
import com.hbm.tileentity.machine.TileEntityForceField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIForceField extends GuiInfoContainer {

	public static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/gui_field.png");
	private TileEntityForceField diFurnace;
	
	public GUIForceField(InventoryPlayer invPlayer, TileEntityForceField tedf) {
		super(new ContainerForceField(invPlayer, tedf));
		diFurnace = tedf;

		this.xSize = 176;
		this.ySize = 168;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
		
		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 69 - 52, 16, 52, diFurnace.power, TileEntityForceField.maxPower);
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 62, guiTop + 69 - 52, 16, 52, mouseX, mouseY, new String[]{ diFurnace.health + " / " + diFurnace.maxHealth + "HP"} );
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.diFurnace.hasCustomInventoryName() ? this.diFurnace.getInventoryName() : I18n.format(this.diFurnace.getInventoryName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

    protected void mouseClicked(int x, int y, int i) throws IOException {
    	super.mouseClicked(x, y, i);
		
    	if(guiLeft + 142 <= x && guiLeft + 142 + 18 > x && guiTop + 34 < y && guiTop + 34 + 18 >= y) {

			playClickSound();
    		PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(diFurnace.getPos(), 0, 0));
    	}
    }
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int i = (int)diFurnace.getPowerScaled(52);
		drawTexturedModalRect(guiLeft + 8, guiTop + 69 - i, 176, 52 - i, 16, i);
		
		int j = diFurnace.getHealthScaled(52);
		drawTexturedModalRect(guiLeft + 62, guiTop + 69 - j, 192, 52 - j, 16, j);
		
		if(diFurnace.isOn) {
			drawTexturedModalRect(guiLeft + 142, guiTop + 34, 176, 52, 18, 18);
		}
	}
}
