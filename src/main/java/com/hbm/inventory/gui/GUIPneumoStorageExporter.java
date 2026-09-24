package com.hbm.inventory.gui;

import net.minecraft.client.renderer.GlStateManager;
import com.hbm.Tags;
import com.hbm.inventory.container.ContainerPneumoStorageExporter;
import com.hbm.tileentity.network.TileEntityPneumoStorageExporter;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;


public class GUIPneumoStorageExporter extends GuiInfoContainer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_pneumatic_exporter.png");

	private final TileEntityPneumoStorageExporter exporter;

	public GUIPneumoStorageExporter(InventoryPlayer invPlayer, TileEntityPneumoStorageExporter exporter) {
		super(new ContainerPneumoStorageExporter(invPlayer, exporter));
		this.exporter = exporter;

		this.xSize = 176;
		this.ySize = 185;
	}

	@Override
	public void drawScreen(int x, int y, float interp) {
		super.drawScreen(x, y, interp);
		super.renderHoveredToolTip(x, y);

		this.drawCustomInfoStat(x, y, guiLeft + 142, guiTop + 16, 18, 18, x, y, new String[] {
				"Request mode: " + TextFormatting.YELLOW + (this.exporter.continuousRequest ? "Continuous" : "By request")});

		this.drawCustomInfoStat(x, y, guiLeft + 142, guiTop + 34, 18, 18, x, y, new String[] {
				"Request type: " + TextFormatting.YELLOW + (
						this.exporter.requestMode == TileEntityPneumoStorageExporter.MODE_AS_MUCH_AS_POSSIBLE ? "As much as possible" :
						this.exporter.requestMode == TileEntityPneumoStorageExporter.MODE_FULL_STACK ? "Only full stacks" : "Only full requests")});

		if(this.exporter.rorConfiguredMode) {
			String[] label = new String[10];
			label[0] = "Filter type: " + TextFormatting.YELLOW + "RoR configured";
			for(int i = 0; i < 9; i++) {
				boolean hasFilter = this.exporter.rorFilters[i][0] != 0 && this.exporter.rorFilters[i][2] > 0;
				label[i + 1] = "Slot " + (i + 1) + ": " + (!hasFilter ? "None" : ("Item #" + this.exporter.rorFilters[i][0] + " with Meta " + this.exporter.rorFilters[i][1] + " x" + this.exporter.rorFilters[i][2]));
			}
			this.drawCustomInfoStat(x, y, guiLeft + 142, guiTop + 52, 18, 18, x, y, label);
		} else {
			this.drawCustomInfoStat(x, y, guiLeft + 142, guiTop + 52, 18, 18, x, y, new String[] {
					"Filter type: " + TextFormatting.YELLOW + "Manually configured"});
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int button) throws IOException {
		super.mouseClicked(x, y, button);

		clickSendFlag(exporter, x, y, 142, 16, 18, 18, "continuous");
		clickSendFlag(exporter, x, y, 142, 34, 18, 18, "request");
		clickSendFlag(exporter, x, y, 142, 52, 18, 18, "ror");
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String name = this.exporter.getDisplayName().getUnformattedText();

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 5, 4210752);
		this.fontRenderer.drawString(I18nUtil.resolveKey("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(this.exporter.rorConfiguredMode) {
			drawTexturedModalRect(guiLeft + 142, guiTop + 52, xSize, 18, 18, 18);
			drawTexturedModalRect(guiLeft + 14, guiTop + 14, 77, 14, 58, 58);
		}
		if(!this.exporter.continuousRequest) drawTexturedModalRect(guiLeft + 142, guiTop + 16, xSize, 0, 18, 18);
		if(this.exporter.requestMode == TileEntityPneumoStorageExporter.MODE_FULL_STACK) drawTexturedModalRect(guiLeft + 142, guiTop + 34, xSize + 18, 0, 18, 18);
		if(this.exporter.requestMode == TileEntityPneumoStorageExporter.MODE_FULL_REQUEST) drawTexturedModalRect(guiLeft + 142, guiTop + 34, xSize + 18, 18, 18, 18);
	}
}
