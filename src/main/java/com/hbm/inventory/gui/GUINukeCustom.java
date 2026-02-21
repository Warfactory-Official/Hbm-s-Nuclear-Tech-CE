package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.config.BombConfig;
import com.hbm.inventory.container.ContainerNukeCustom;
import com.hbm.tileentity.bomb.TileEntityNukeCustom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.renderer.GlStateManager;

public class GUINukeCustom extends GuiInfoContainer {
	
	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/gunBombSchematic.png");
	private TileEntityNukeCustom testNuke;
	
	public GUINukeCustom(InventoryPlayer invPlayer, TileEntityNukeCustom tedf) {
		super(new ContainerNukeCustom(invPlayer, tedf));
		testNuke = tedf;
		
		this.xSize = 176;
		this.ySize = 222;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		String name = this.testNuke.hasCustomInventoryName() ? this.testNuke.getInventoryName() : I18n.format(this.testNuke.getInventoryName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		String[] text;

		text = new String[] { TextFormatting.YELLOW + "Conventional Explosives (Radius " + testNuke.tnt + "/" + BombConfig.maxCustomTNTRadius + ")",
				"Caps at " + BombConfig.maxCustomTNTRadius,
				"N²-like above level 75",
				TextFormatting.ITALIC + "\"Goes boom\"" };
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 16, guiTop + 88, 18, 18, mouseX, mouseY, text);
		
		text = new String[] { TextFormatting.YELLOW + "Nuclear (Radius " + testNuke.nuke + "(" + testNuke.getNukeAdj() + ")/"+ BombConfig.maxCustomNukeRadius + ")",
				"Requires TNT level 16",
				"Caps at " + BombConfig.maxCustomNukeRadius,
				"Has fallout",
				TextFormatting.ITALIC + "\"Now I am become death, destroyer of worlds.\"" };
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 34, guiTop + 88, 18, 18, mouseX, mouseY, text);
		
		text = new String[] { TextFormatting.YELLOW + "Thermonuclear (Radius " + testNuke.hydro + "(" + testNuke.getHydroAdj() + ")/" + BombConfig.maxCustomHydroRadius + ")",
				"Requires nuclear level 100",
				"Caps at " + BombConfig.maxCustomHydroRadius,
				"Reduces added fallout by salted stage by 75%",
				TextFormatting.ITALIC + "\"And for my next trick, I'll make",
				TextFormatting.ITALIC + "the island of Elugelab disappear!\"" };
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 52, guiTop + 88, 18, 18, mouseX, mouseY, text);
		
		text = new String[] { TextFormatting.YELLOW + "Balefire (Radius " + testNuke.bale + "/(" + testNuke.getBaleAdj() + ")/" + BombConfig.maxCustomBaleRadius + ")",
				"Requires nuclear level 50",
				"Caps at " + BombConfig.maxCustomBaleRadius,
				TextFormatting.ITALIC + "\"Antimatter, Balefire, whatever.\"" };
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 70, guiTop + 88, 18, 18, mouseX, mouseY, text);
		
		text = new String[] { TextFormatting.YELLOW + "Salted (Radius " + testNuke.dirty + "/" + BombConfig.maxCustomDirtyRadius + ")",
				"Extends fallout of nuclear and",
				"thermonuclear stages",
				"Caps at " + BombConfig.maxCustomDirtyRadius,
				TextFormatting.ITALIC + "\"Not to be confused with tablesalt.\"" };
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 53, guiTop + 83, 25, 5, mouseX, mouseY, text);
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 53, guiTop + 106, 25, 5, mouseX, mouseY, text);
		
		text = new String[] { TextFormatting.YELLOW + "Schrabidium (Radius " + testNuke.schrab + "(" + testNuke.getSchrabAdj() + ")/" + BombConfig.maxCustomSchrabRadius + ")",
				"Requires nuclear level 50",
				"Caps at " + BombConfig.maxCustomSchrabRadius,
				TextFormatting.ITALIC + "\"For the hundredth time,",
				TextFormatting.ITALIC + "you can't bypass these caps!\"" };
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 88, guiTop + 88, 18, 18, mouseX, mouseY, text);

		text = new String[] { TextFormatting.YELLOW + "Solinium (Radius " + testNuke.sol + "(" + testNuke.getSolAdj() + ")/" + BombConfig.maxCustomSolRadius + ")",
				"Requires nuclear level 25",
				"Caps at " + BombConfig.maxCustomSolRadius,
				TextFormatting.ITALIC + "\"For the hundredth time,",
				TextFormatting.ITALIC + "you can't bypass these caps!\"" };
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 106, guiTop + 88, 18, 18, mouseX, mouseY, text);
		
		text = new String[] { TextFormatting.YELLOW + "Ice cream (Level " + testNuke.euph + "/" + BombConfig.maxCustomEuphLvl + ")",
				"Requires schrabidium and solinium level 1",
				"Caps at " + BombConfig.maxCustomEuphLvl,
				TextFormatting.ITALIC + "\"Probably not ice cream but the label came off.\"" };
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 142, guiTop + 88, 18, 18, mouseX, mouseY, text);
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		byte best = 10;

		if(this.testNuke.euph > 0){
			drawTexturedModalRect(guiLeft + 142, guiTop + 89, 176, 108, 18, 18); //Euph strongest
			best = 9;
		}

		if(this.testNuke.sol > 0){
			if(best == 10){
				drawTexturedModalRect(guiLeft + 106, guiTop + 89, 194, 90, 18, 18); //Sol strongest
				best = 8;
			}
			else{
				drawTexturedModalRect(guiLeft + 106, guiTop + 89, 176, 90, 18, 18);
			}
		}

		if(this.testNuke.schrab > 0){
			if(best == 10){
				drawTexturedModalRect(guiLeft + 88, guiTop + 89, 194, 72, 18, 18); //Schrab strongest
				best = 7;
			}
			else{
				drawTexturedModalRect(guiLeft + 88, guiTop + 89, 176, 72, 18, 18);
			}
		}

		if(this.testNuke.bale > 0){
			if(best == 10){
				drawTexturedModalRect(guiLeft + 70, guiTop + 89, 194, 54, 18, 18); //Bale strongest
				best = 6;
			}
			else{
				drawTexturedModalRect(guiLeft + 70, guiTop + 89, 176, 54, 18, 18);
			}
		}
			
		if(this.testNuke.hydro > 0){
			if(best == 10){
				drawTexturedModalRect(guiLeft + 52, guiTop + 89, 194, 36, 18, 18); //Hydro strongest
				best = 5;
			}
			else{
				drawTexturedModalRect(guiLeft + 52, guiTop + 89, 176, 36, 18, 18);
			}
		}
			
		if(this.testNuke.nuke > 0){
			if(best == 10){
				drawTexturedModalRect(guiLeft + 34, guiTop + 89, 194, 18, 18, 18); //Nuke strongest
				best = 4;
			}
			else{
				drawTexturedModalRect(guiLeft + 34, guiTop + 89, 176, 18, 18, 18);
			}
		}
			
		if(this.testNuke.tnt > 0){
			if(best == 10){
				drawTexturedModalRect(guiLeft + 16, guiTop + 89, 194, 0, 18, 18); //TNT strongest
				best = 3;
			}
			else{
				drawTexturedModalRect(guiLeft + 16, guiTop + 89, 176, 0, 18, 18);
			}
		}
			
		
		if(this.testNuke.dirty > 0){
			if(best < 6 && best > 3){
				drawTexturedModalRect(guiLeft + 53, guiTop + 83, 201, 125, 25, 29);
			}
			else{
				drawTexturedModalRect(guiLeft + 53, guiTop + 83, 176, 125, 25, 29);
			}
		}
	}
}
