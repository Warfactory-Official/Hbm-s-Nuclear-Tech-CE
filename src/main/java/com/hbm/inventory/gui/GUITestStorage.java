package com.hbm.inventory.gui;

import net.minecraft.client.renderer.GlStateManager;
import com.hbm.Tags;
import com.hbm.blocks.test.TestEventTester;
import com.hbm.blocks.test.TestEventTester.TileEntityTestStorage;
import com.hbm.inventory.container.ContainerTestStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GUITestStorage extends GuiInfoContainer {

	public static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID + ":textures/gui/gui_test_storage.png");

	protected static final int scrollBounds = 50 - 7;

	protected int scrollIndex = 0;
	protected boolean wasClicking = false;
	protected boolean draggingScroll = false;

	public GUITestStorage(InventoryPlayer invPlayer, TileEntityTestStorage tile) {
		super(new ContainerTestStorage(invPlayer, tile));

		this.xSize = 176;
		this.ySize = 222;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float interp) {

		boolean isClicking = Mouse.isButtonDown(0);
		int sx = this.guiLeft + 155;
		int sy = this.guiTop + 18;

		if(!this.wasClicking && isClicking && mouseX >= sx && mouseX < sx + 12 && mouseY >= sy && mouseY < sy + 106) {
			this.draggingScroll = true;
		}
		if(!isClicking) {
			this.draggingScroll = false;
		}
		this.wasClicking = isClicking;

		if(this.draggingScroll) {
			float f = ((float) (mouseY - sy) - 7.5F) / (106F - 15F);
			int next = Math.max(0, Math.min(scrollBounds, Math.round(f * scrollBounds)));

			if(next != this.scrollIndex) {
				this.scrollIndex = next;
				this.mc.playerController.windowClick(this.inventorySlots.windowId,
						TestEventTester.SLOT_CLICK_ID_REFRESH, this.scrollIndex, ClickType.PICKUP, this.mc.player);
			}
		}

		super.drawScreen(mouseX, mouseY, interp);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRenderer.drawString("Test Storage", 8, 6, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int travel = (int) ((106F - 15F) * ((float) scrollIndex / (float) scrollBounds));
		drawTexturedModalRect(guiLeft + 155, guiTop + 18 + travel, 176, 0, 12, 15);
	}
}
