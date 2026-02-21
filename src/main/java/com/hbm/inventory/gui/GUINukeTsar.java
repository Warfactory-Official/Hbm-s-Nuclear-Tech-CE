package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.config.BombConfig;
import com.hbm.inventory.container.ContainerNukeTsar;
import com.hbm.tileentity.bomb.TileEntityNukeTsar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUINukeTsar extends GuiInfoContainer {
	
	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/tsarBombaSchematic.png");
	private TileEntityNukeTsar testNuke;
	
	public GUINukeTsar(InventoryPlayer invPlayer, TileEntityNukeTsar tedf) {
		super(new ContainerNukeTsar(invPlayer, tedf));
		testNuke = tedf;
		
		this.xSize = 256;
		this.ySize = 220;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		String name = this.testNuke.hasCustomInventoryName() ? this.testNuke.getInventoryName() : I18n.format(this.testNuke.getInventoryName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 0xD8D8D8);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		String[] info = null;
		if(testNuke.isStage3Filled())
			info = new String[] { "Nuke Radius: "+ (int)BombConfig.tsarRadius +"m"};
		else if(testNuke.isStage1Filled())
			info = new String[] { "Nuke Radius: "+ (int)BombConfig.tsarRadius/2 + "m"};
		else if(testNuke.isStage2Filled())
			info = new String[] { "Nuke Radius: "+ (int)BombConfig.tsarRadius/3 + "m"};
		else if(testNuke.isReady())
			info = new String[] { "Nuke Radius: "+ (int)BombConfig.tsarRadius/5 + "m"};
		if(info != null)
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 40, guiTop + 17, 176, 60, mouseX, mouseY, info);
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(testNuke.isReady()){
			drawTexturedModalRect(guiLeft + 221, guiTop + 39, 150, 240, 16, 16);
		}

		if(testNuke.isStage1Filled()){
			drawTexturedModalRect(guiLeft + 221, guiTop + 39, 166, 240, 16, 16);
		}

		if(testNuke.isStage2Filled()){
			drawTexturedModalRect(guiLeft + 221, guiTop + 39, 150, 224, 16, 16);
		}

		if(testNuke.isStage3Filled()){
			drawTexturedModalRect(guiLeft + 221, guiTop + 39, 166, 224, 16, 16);
		}

		if(testNuke.isCoreFilled()){
			drawTexturedModalRect(guiLeft + 43, guiTop + 41, 58, 220, 12, 12);
		}
		if(testNuke.isTopLeftLenseFilled()){
			drawTexturedModalRect(guiLeft + 26, guiTop + 24, 58, 233, 23, 23);
		}
		if(testNuke.isTopRightLenseFilled()){
			drawTexturedModalRect(guiLeft + 49, guiTop + 24, 81, 233, 23, 23);
		}
		if(testNuke.isBottomLeftLenseFilled()){
			drawTexturedModalRect(guiLeft + 26, guiTop + 47, 104, 233, 23, 23);
		}
		if(testNuke.isBottomRightLenseFilled()){
			drawTexturedModalRect(guiLeft + 49, guiTop + 47, 127, 233, 23, 23);
		}
		if(testNuke.isStage1UFilled()){
			drawTexturedModalRect(guiLeft + 74, guiTop + 29, 190, 220, 66, 36);
		}
		if(testNuke.isStage2UFilled()){
			drawTexturedModalRect(guiLeft + 148, guiTop + 29, 190, 220, 66, 36);
		}
		if(testNuke.isStage1DFilled()){
			drawTexturedModalRect(guiLeft + 78, guiTop + 34, 0, 230, 58, 26);
		}
		if(testNuke.isStage2DFilled()){
			drawTexturedModalRect(guiLeft + 152, guiTop + 34, 0, 230, 58, 26);
		}
	}
}
