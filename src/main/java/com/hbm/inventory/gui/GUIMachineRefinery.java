package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineRefinery;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.recipes.RefineryRecipes;
import com.hbm.tileentity.machine.oil.TileEntityMachineRefinery;
import com.hbm.util.RenderUtil;
import com.hbm.util.Tuple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class GUIMachineRefinery extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/gui_refinery.png");
	private TileEntityMachineRefinery refinery;

	public GUIMachineRefinery(InventoryPlayer invPlayer, TileEntityMachineRefinery tedf) {
		super(new ContainerMachineRefinery(invPlayer, tedf));
		refinery = tedf;

		this.xSize = 210;
		this.ySize = 231;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		refinery.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 30, guiTop + 27, 21, 104);    // Render tooltip for column.
		refinery.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 86, guiTop + 42, 16, 52);
		refinery.tanks[2].renderTankInfo(this, mouseX, mouseY, guiLeft + 106, guiTop + 42, 16, 52);
		refinery.tanks[3].renderTankInfo(this, mouseX, mouseY, guiLeft + 126, guiTop + 42, 16, 52);
		refinery.tanks[4].renderTankInfo(this, mouseX, mouseY, guiLeft + 146, guiTop + 42, 16, 52);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 186, guiTop + 18, 16, 52, refinery.power, refinery.maxPower);
		super.renderHoveredToolTip(mouseX, mouseY);


	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.refinery.hasCustomName() ? this.refinery.getName() : I18n.format(this.refinery.getName());

		this.fontRenderer.drawString(name, this.xSize / 2 - 34/2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 4, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        boolean wasBlendEnabled = RenderUtil.isBlendEnabled();
        boolean wasLightingEnabled = RenderUtil.isLightingEnabled();
        boolean wasDepthTestEnabled = RenderUtil.isDepthEnabled();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		GlStateManager.colorMask(true, true, true, false);
		super.drawDefaultBackground();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, xSize, ySize, 350, 256);

		// power
		int j = (int)refinery.getPowerScaled(50);
		drawModalRectWithCustomSizedTexture(guiLeft + 186, guiTop + 69 - j, 210, 52 - j, 16, j, 350, 256);

		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0); // default

		// input tank
		FluidTankNTM inputOil = refinery.tanks[0];
		if (inputOil.getFill() != 0) {

			int targetHeight = inputOil.getFill() * 101 / inputOil.getMaxFill();
			Color color = new Color(inputOil.getTankType().getColor());

			GlStateManager.enableBlend();
			GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1F);
			drawModalRectWithCustomSizedTexture(guiLeft + 33, guiTop + 130 - targetHeight, 226, 101 - targetHeight, 16, targetHeight, 350, 256);
			GlStateManager.disableBlend();
		}

		// fucking kgjhgdfjgdhjfg
		// drawModalRectWithCustomSizedTexture lets you set the resolution of the source texture !!!!
		// 350x256 texture by behated (the pipes wouldn't fit)

		// pipes

		Tuple.Quintet<FluidStack, FluidStack, FluidStack, FluidStack, ItemStack> recipe = RefineryRecipes.getRefinery(inputOil.getTankType());

		if(recipe == null) {
			drawModalRectWithCustomSizedTexture(guiLeft + 52, guiTop + 63, 247, 1, 33, 48, 350, 256);
			drawModalRectWithCustomSizedTexture(guiLeft + 52, guiTop + 32, 247, 50, 66, 52, 350, 256);
			drawModalRectWithCustomSizedTexture(guiLeft + 52, guiTop + 24, 247, 145, 86, 35, 350, 256);
			drawModalRectWithCustomSizedTexture(guiLeft + 36, guiTop + 16, 211, 119, 122, 25, 350, 256);
		} else {

			// Heavy Oil Products
			Color color = new Color(recipe.getV().type.getColor());

			GlStateManager.enableBlend();
			GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1F);
			drawModalRectWithCustomSizedTexture(guiLeft + 52, guiTop + 63, 247, 1, 33, 48, 350, 256);

			// Naphtha Oil Products
			color = new Color(recipe.getW().type.getColor());
			GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1F);
			drawModalRectWithCustomSizedTexture(guiLeft + 52, guiTop + 32, 247, 50, 66, 52, 350, 256);

			// Light Oil Products
			color = new Color(recipe.getX().type.getColor());
			GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1F);
			drawModalRectWithCustomSizedTexture(guiLeft + 52, guiTop + 24, 247, 145, 86, 35, 350, 256);

			// Gaseous Products
			color = new Color(recipe.getY().type.getColor());
			GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1F);
			drawModalRectWithCustomSizedTexture(guiLeft + 36, guiTop + 16, 211, 119, 122, 25, 350, 256);

			GlStateManager.disableBlend();
			GlStateManager.color(1F, 1F, 1F, 1F);
		}

		// output tanks
		refinery.tanks[1].renderTank(guiLeft + 86, guiTop + 95, this.zLevel, 16, 52);
		refinery.tanks[2].renderTank(guiLeft + 106, guiTop + 95, this.zLevel, 16, 52);
		refinery.tanks[3].renderTank(guiLeft + 126, guiTop + 95, this.zLevel, 16, 52);
		refinery.tanks[4].renderTank(guiLeft + 146, guiTop + 95, this.zLevel, 16, 52);

		GlStateManager.colorMask(true, true, true, true);
		if (wasLightingEnabled) GlStateManager.enableLighting(); else GlStateManager.disableLighting();
		if (wasDepthTestEnabled) GlStateManager.enableDepth(); else GlStateManager.disableDepth();
		if (wasBlendEnabled) GlStateManager.enableBlend(); else GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
}
