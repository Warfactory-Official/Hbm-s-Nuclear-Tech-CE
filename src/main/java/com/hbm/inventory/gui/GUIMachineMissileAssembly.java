package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineMissileAssembly;
import com.hbm.packet.toserver.AuxButtonPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.render.misc.MissileMultipart;
import com.hbm.render.misc.MissilePart;
import com.hbm.render.misc.MissilePronter;
import com.hbm.tileentity.machine.TileEntityMachineMissileAssembly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIMachineMissileAssembly extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/gui_missile_assembly.png");
	private TileEntityMachineMissileAssembly assembler;
	
	public GUIMachineMissileAssembly(InventoryPlayer invPlayer, TileEntityMachineMissileAssembly tedf) {
		super(new ContainerMachineMissileAssembly(invPlayer, tedf));
		assembler = tedf;
		
		this.xSize = 176;
		this.ySize = 222;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	protected void mouseClicked(int x, int y, int i) throws IOException {
    	super.mouseClicked(x, y, i);
		
    	if(guiLeft + 115 <= x && guiLeft + 115 + 18 > x && guiTop + 35 < y && guiTop + 35 + 18 >= y) {
    		
			playClickSound();
    		PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(assembler.getPos(), 0, 0));
    	}
    }

	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		String name = this.assembler.hasCustomInventoryName() ? this.assembler.getInventoryName() : I18n.format(this.assembler.getInventoryName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(assembler.fuselageState() == 1)
			drawTexturedModalRect(guiLeft + 49, guiTop + 23, 194, 0, 6, 8);
		if(assembler.warheadState() == 1)
			drawTexturedModalRect(guiLeft + 31, guiTop + 23, 194, 0, 6, 8);
		if(assembler.chipState() == 1)
			drawTexturedModalRect(guiLeft + 13, guiTop + 23, 194, 0, 6, 8);
		if(assembler.stabilityState() == 1)
			drawTexturedModalRect(guiLeft + 67, guiTop + 23, 194, 0, 6, 8);
		if(assembler.stabilityState() == 0)
			drawTexturedModalRect(guiLeft + 67, guiTop + 23, 200, 0, 6, 8);
		if(assembler.thrusterState() == 1)
			drawTexturedModalRect(guiLeft + 85, guiTop + 23, 194, 0, 6, 8);
		
		if(assembler.canBuild())
			drawTexturedModalRect(guiLeft + 115, guiTop + 35, 176, 0, 18, 18);
		
		/// DRAW MISSILE START
		GlStateManager.pushMatrix();

		MissileMultipart missile = new MissileMultipart();
		
		if(assembler.inventory.getStackInSlot(1) != null)
			missile.warhead = MissilePart.getPart(assembler.inventory.getStackInSlot(1).getItem());
		
		if(assembler.inventory.getStackInSlot(2) != null)
			missile.fuselage = MissilePart.getPart(assembler.inventory.getStackInSlot(2).getItem());
		
		if(assembler.inventory.getStackInSlot(3) != null)
			missile.fins = MissilePart.getPart(assembler.inventory.getStackInSlot(3).getItem());
		
		if(assembler.inventory.getStackInSlot(4) != null)
			missile.thruster = MissilePart.getPart(assembler.inventory.getStackInSlot(4).getItem());
		
		GlStateManager.translate(guiLeft + 88, guiTop + 98, 100);
		GlStateManager.rotate(System.currentTimeMillis() / 10 % 360, 0, -1, 0);
		
		double size = 8 * 18;
		double scale = size / Math.max(missile.getHeight(), 6);
		
		GlStateManager.translate(missile.getHeight() / 2 * scale, 0, 0);
		GL11.glScaled(scale, scale, scale);
		
		GlStateManager.rotate(90, 1, 0, 0);
		GlStateManager.rotate(-90, 0, 0, 1);
		GlStateManager.scale(-1, -1, -1);
		
		MissilePronter.prontMissile(missile, Minecraft.getMinecraft().getTextureManager());
		
		GlStateManager.popMatrix();
		/// DRAW MISSILE END
	}
}
