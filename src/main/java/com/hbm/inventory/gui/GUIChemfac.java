package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerChemfac;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.tileentity.machine.TileEntityMachineChemfac;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;
import org.lwjgl.input.Keyboard;

public class GUIChemfac extends GuiInfoContainer {
	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_chemfac.png");

	private final TileEntityMachineChemfac chemfac;

	public GUIChemfac(InventoryPlayer playerInv, TileEntityMachineChemfac tile) {
		super(new ContainerChemfac(playerInv, tile));

		this.chemfac = tile;

		this.xSize = 256;
		this.ySize = 256;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		super.renderHoveredToolTip(mouseX, mouseY);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 234, guiTop + 25, 16, 52, chemfac.power, chemfac.getMaxPower());

		for (int i = 0; i < 8; i++) {
			int offX = guiLeft + 110 * (i % 2);
			int offY = guiTop + 38 * (i / 2);

			chemfac.tanksNew[i * 4 + 0].renderTankInfo(this, mouseX, mouseY, offX + 40, offY + 45 - 32, 5, 34);
			chemfac.tanksNew[i * 4 + 1].renderTankInfo(this, mouseX, mouseY, offX + 45, offY + 45 - 32, 5, 34);
			chemfac.tanksNew[i * 4 + 2].renderTankInfo(this, mouseX, mouseY, offX + 102, offY + 45 - 32, 5, 34);
			chemfac.tanksNew[i * 4 + 3].renderTankInfo(this, mouseX, mouseY, offX + 107, offY + 45 - 32, 5, 34);
		}

		chemfac.waterNew.renderTankInfo(this, mouseX, mouseY, guiLeft + 233, guiTop + 108, 9, 54);
		chemfac.steamNew.renderTankInfo(this, mouseX, mouseY, guiLeft + 242, guiTop + 108, 9, 54);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawDefaultBackground();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		GlStateManager.colorMask(true, true, true, false);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, 167);
		drawTexturedModalRect(guiLeft + 26, guiTop + 167, 26, 167, 230, 44);
		drawTexturedModalRect(guiLeft + 26, guiTop + 211, 26, 211, 176, 45);

		int p = (int) (chemfac.power * 52 / chemfac.getMaxPower());
		drawTexturedModalRect(guiLeft + 234, guiTop + 77 - p, 0, 219 - p, 16, p);

		if (chemfac.power > 0)
			drawTexturedModalRect(guiLeft + 238, guiTop + 11, 0, 219, 9, 12);

		for (int i = 0; i < 8; i++) {
			int offX = guiLeft + 110 * (i % 2);
			int offY = guiTop + 38 * (i / 2);

			int prog = chemfac.progress[i];
			int j = prog * 17 / Math.max(chemfac.maxProgress[i], 1);
			Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
			drawTexturedModalRect(guiLeft + offX + 51, guiTop + offY + 16, 202, 247, j, 11);

			chemfac.tanksNew[i * 4 + 0].renderTank(offX + 41, offY + 46, this.zLevel, 3, 32);
			chemfac.tanksNew[i * 4 + 1].renderTank(offX + 46, offY + 46, this.zLevel, 3, 32);
			chemfac.tanksNew[i * 4 + 2].renderTank(offX + 103, offY + 46, this.zLevel, 3, 32);
			chemfac.tanksNew[i * 4 + 3].renderTank(offX + 108, offY + 46, this.zLevel, 3, 32);
		}
		GlStateManager.colorMask(true, true, true, true);
		GlStateManager.enableLighting();
		GlStateManager.enableDepth();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();

		chemfac.waterNew.renderTank(guiLeft + 234, guiTop + 161, this.zLevel, 7, 52);
		chemfac.steamNew.renderTank(guiLeft + 243, guiTop + 161, this.zLevel, 7, 52);

		if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
			for (int i = 0; i < this.inventorySlots.inventorySlots.size(); i++) {
				Slot s = this.inventorySlots.getSlot(i);

				this.fontRenderer.drawStringWithShadow(i + "", guiLeft + s.xPos + 2, guiTop + s.yPos, 0xffffff);
				this.fontRenderer.drawStringWithShadow(s.getSlotIndex() + "", guiLeft + s.xPos + 2, guiTop + s.yPos + 8, 0xff8080);
			}
	}
	public ItemStackHandler getInventory() {
		return chemfac.inventory;
	}

	public FluidTankNTM[] getTanks() {
		return chemfac.tanksNew;
	}
}
