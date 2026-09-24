package com.hbm.inventory.gui;

import net.minecraft.client.renderer.GlStateManager;
import com.hbm.Tags;
import com.hbm.inventory.container.ContainerTapeDrive;
import com.hbm.tileentity.machine.TileEntityMachineTapeDrive;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GUITapeDrive extends GuiInfoContainer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_tape_drive.png");

	private final TileEntityMachineTapeDrive drive;

	public GUITapeDrive(InventoryPlayer invPlayer, TileEntityMachineTapeDrive drive) {
		super(new ContainerTapeDrive(invPlayer, drive));
		this.drive = drive;

		this.xSize = 176;
		this.ySize = 186;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String name = this.drive.getDisplayName().getUnformattedText();

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18nUtil.resolveKey("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		for(int i = 0; i < 12; i++) {
			if(this.drive.tapes[i] == TileEntityMachineTapeDrive.SLOT_FILLED_TAPE) {
				drawTexturedModalRect(guiLeft + 34 + (i % 6) * 18, guiTop + 26 + (i / 6) * 18, 176, 0, 18, 18);
			}
		}
	}
}
