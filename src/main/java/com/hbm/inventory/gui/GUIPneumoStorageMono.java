package com.hbm.inventory.gui;

import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.tileentity.network.TileEntityPneumoTube;
import net.minecraft.client.renderer.GlStateManager;
import com.hbm.Tags;
import com.hbm.inventory.container.ContainerPneumoStorageMono;
import com.hbm.tileentity.network.TileEntityPneumoStorageMono;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Locale;

public class GUIPneumoStorageMono extends GuiInfoContainer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_pneumatic_mono.png");

	private final TileEntityPneumoStorageMono mono;

	public GUIPneumoStorageMono(InventoryPlayer invPlayer, TileEntityPneumoStorageMono mono) {
		super(new ContainerPneumoStorageMono(invPlayer, mono));
		this.mono = mono;

		this.xSize = 200;
		this.ySize = 181;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		super.renderHoveredToolTip(mouseX, mouseY);
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 174, guiTop + 36, 20, 8, mouseX, mouseY, new String[] {"Compressor: " + mono.compair.getPressure() + " PU", "Max range: " + TileEntityPneumoTube.getRangeFromPressure(mono.compair.getPressure()) + "m"});
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		clickSendFlag(mono, mouseX, mouseY, 174, 36, 20, 8, "pressure");
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String name = this.mono.getDisplayName().getUnformattedText();

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 5, 4210752);
		this.fontRenderer.drawString(I18nUtil.resolveKey("container.inventory"), 8, this.ySize - 96 + 2, 4210752);

		for(int k = 0; k < 3; k++) {
			if(!this.mono.inventory.getStackInSlot(k).isEmpty()) {
				int amount = this.mono.amounts[k];
				String percent = " (" + (((int) (amount * 1000D / (double) TileEntityPneumoStorageMono.CAPACITY)) / 10D) + "%)";
				this.fontRenderer.drawString(String.format(Locale.US, "%,d", amount) + percent, 50, 22 + k * 18, 0x000000);
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		for(int i = 0; i < 3; i++) {
			if(!this.mono.inventory.getStackInSlot(i).isEmpty()) {
				int bar = this.mono.amounts[i] * 124 / TileEntityPneumoStorageMono.CAPACITY;
				drawTexturedModalRect(guiLeft + 44, guiTop + 17 + i * 18, 0, 181, bar, 16);
			}
		}

		drawTexturedModalRect(guiLeft + 174 + 4 * (mono.compair.getPressure() - 1), guiTop + 36, 200, 0, 4, 8);
		GUIElements.drawSmoothGauge(guiLeft + 184, guiTop + 25, this.zLevel, (double) mono.compair.getFill() / (double) mono.compair.getMaxFill(), 5, 2, 1, 0xCA6C43, 0xAB4223);
	}
}
