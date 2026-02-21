package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerFEL;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.packet.toserver.AuxButtonPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.machine.TileEntityFEL;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIFEL extends GuiInfoContainer {

	public static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_fel.png");
	private TileEntityFEL fel;

	public GUIFEL(InventoryPlayer invPlayer, TileEntityFEL laser) {
		super(new ContainerFEL(invPlayer, laser));
		fel = laser;

		this.xSize = 203;
		this.ySize = 169;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
		
		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 182, guiTop + 27, 16, 113, fel.power, fel.maxPower);
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	protected void mouseClicked(int mouseX, int mouseY, int i) throws IOException {
		super.mouseClicked(mouseX, mouseY, i);

		if(guiLeft + 142 <= mouseX && guiLeft + 142 + 29 > mouseX && guiTop + 41 < mouseY && guiTop + 41 + 17 >= mouseY) {
			playClickSound();
			PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(fel.getPos(), 0, 2));
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.fel.hasCustomName() ? this.fel.getName() : I18n.format(this.fel.getName());

		this.fontRenderer.drawString(name, 90 + this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 7, 0xffffff);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 98, 4210752);
		
		if(fel.missingValidSilex && fel.isOn) {
			this.fontRenderer.drawString(I18n.format("ERR."), 55 + this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 9, 0xFF0000);
		} else if(fel.isOn) {
			this.fontRenderer.drawString(I18n.format("LIVE"), 54 + this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 9, 0x00FF00);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		if(fel.isOn)
			drawTexturedModalRect(guiLeft + 142, guiTop + 41, 203, 0, 29, 17);
		
		int k = (int)fel.getPowerScaled(113);
		drawTexturedModalRect(guiLeft + 182, guiTop + 27 + 113 - k, 203, 17 + 113 - k, 16, k);
		
		int color = !(fel.mode == EnumWavelengths.VISIBLE) ? fel.mode.guiColor : Color.HSBtoRGB(fel.getWorld().getTotalWorldTime() / 50.0F, 0.5F, 1F) & 16777215;
		
		if(fel.power > fel.powerReq * Math.pow(2, fel.mode.ordinal()) && fel.isOn && !(fel.mode == EnumWavelengths.NULL) && fel.distance > 0) {
	
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GlStateManager.glLineWidth(10F);
			GlStateManager.color(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, 1.0F);
			
			BufferBuilder buf = Tessellator.getInstance().getBuffer();
			buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
			
			buf.pos(guiLeft + 135, guiTop + 31.5F, this.zLevel).endVertex();

			buf.pos(guiLeft + 113, guiTop + 31.5F, this.zLevel).endVertex();
			
			
			buf.pos(0, guiTop + 31.5F, this.zLevel).endVertex();

			buf.pos(guiLeft + 4, guiTop + 31.5F, this.zLevel).endVertex();
			
			Tessellator.getInstance().draw();
	        GlStateManager.enableTexture2D();
		}
	}	
}