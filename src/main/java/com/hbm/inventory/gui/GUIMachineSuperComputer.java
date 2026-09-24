package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerMachineSuperComputer;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.modules.machine.ModuleMachineBase;
import com.hbm.tileentity.machine.TileEntityMachineSuperComputer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIMachineSuperComputer extends GuiInfoContainerProcessor {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_supercomputer.png");

	private final TileEntityMachineSuperComputer computer;

	public GUIMachineSuperComputer(InventoryPlayer invPlayer, TileEntityMachineSuperComputer computer) {
		super(new ContainerMachineSuperComputer(invPlayer, computer));
		this.computer = computer;

		this.processorModule = new ModuleMachineBase[] { computer.computerModule };

		this.xSize = 176;
		this.ySize = 211;
	}

	@Override public int[][] getSelectorPositions() { return new int[][] {{7, 80, 1}}; }
	@Override public IControlReceiver getControlReceiver() { return this.computer; }
	@Override public ResourceLocation getTexture() { return TEXTURE; }

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		computer.inputTank.renderTankInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 54, 52, 16);
		computer.outputTank.renderTankInfo(this, mouseX, mouseY, guiLeft + 80, guiTop + 54, 52, 16);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 18, 16, 61, computer.power, computer.maxPower);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String name = this.computer.hasCustomName() ? this.computer.getName() : I18n.format(this.computer.getDefaultName());

		this.fontRenderer.drawString(name, 70 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int p = (int) (computer.power * 61 / computer.maxPower);
		drawTexturedModalRect(guiLeft + 152, guiTop + 79 - p, 176, 61 - p, 16, p);

		if(computer.computerModule.progress > 0) {
			int j = (int) Math.ceil(70 * computer.computerModule.progress);
			drawTexturedModalRect(guiLeft + 62, guiTop + 81, 176, 61, j, 16);
		}

		GenericRecipe recipe = computer.computerModule.getRecipe();
		this.renderStandardLEDs(computer.didProcess, recipe, computer.power, 51, 76, 195, 0);
		this.renderRecipeIcons();

		computer.inputTank.renderTank(guiLeft + 8, guiTop + 70, this.zLevel, 52, 16, 1);
		computer.outputTank.renderTank(guiLeft + 80, guiTop + 70, this.zLevel, 52, 16, 1);
	}
}
