package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerCrystallizer;
import com.hbm.tileentity.machine.TileEntityMachineCrystallizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GUICrystallizer extends GuiInfoContainer {

	public static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_crystallizer_alt.png");
	private TileEntityMachineCrystallizer acidomatic;

	public GUICrystallizer(InventoryPlayer invPlayer, TileEntityMachineCrystallizer acidomatic) {
		super(new ContainerCrystallizer(invPlayer, acidomatic));
		this.acidomatic = acidomatic;

		this.xSize = 176;
		this.ySize = 204;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.acidomatic.hasCustomName() ? this.acidomatic.getName() : I18n.format(this.acidomatic.getName());

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 17, 16, 52, acidomatic.power, TileEntityMachineCrystallizer.maxPower);
		acidomatic.tankNew.renderTankInfo(this, mouseX, mouseY, guiLeft + 35, guiTop + 18, 16, 52);
		String[] text = new String[] { "Acceptable upgrades:",
				" -Speed (stacks to level 3)",
				" -Effectiveness (stacks to level 3)",
				" -Overdrive (stacks to level 3)"};
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 117, guiTop + 22, 8, 8, guiLeft + 200, guiTop + 45, text);
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		int i = (int)acidomatic.getPowerScaled(52);
		drawTexturedModalRect(guiLeft + 152, guiTop + 70 - i, 176, 64 - i, 16, i);

		int j = acidomatic.getProgressScaled(28);
		drawTexturedModalRect(guiLeft + 80, guiTop + 47, 176, 0, j, 12);

		this.drawInfoPanel(guiLeft + 117, guiTop + 22, 8, 8, 8);

		acidomatic.tankNew.renderTank(guiLeft + 35, guiTop + 70, this.zLevel, 16, 52);
	}
}