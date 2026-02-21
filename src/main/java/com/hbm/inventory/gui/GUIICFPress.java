package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerICFPress;
import com.hbm.tileentity.machine.TileEntityICFPress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIICFPress extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_icf_press.png");
    private final TileEntityICFPress press;

    public GUIICFPress(InventoryPlayer invPlayer, TileEntityICFPress tedf) {
        super(new ContainerICFPress(invPlayer, tedf));
        press = tedf;

        this.xSize = 176;
        this.ySize = 179;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);

        press.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 44, guiTop + 18, 16, 52);
        press.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 18, 16, 52);

        if (this.isMouseOverSlot(this.inventorySlots.getSlot(4), mouseX, mouseY) && !this.inventorySlots.getSlot(4).getHasStack())
            this.drawHoveringText("Item input: Top/Bottom", mouseX, mouseY);
        if (this.isMouseOverSlot(this.inventorySlots.getSlot(5), mouseX, mouseY) && !this.inventorySlots.getSlot(5).getHasStack())
            this.drawHoveringText("Item input: Sides", mouseX, mouseY);
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.press.hasCustomName() ? this.press.getName() : I18n.format(this.press.getName());

        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int m = press.muon * 52 / TileEntityICFPress.maxMuon;
        drawTexturedModalRect(guiLeft + 28, guiTop + 70 - m, 176, 52 - m, 4, m);

        press.tanks[0].renderTank(guiLeft + 44, guiTop + 70, this.zLevel, 16, 52);
        press.tanks[1].renderTank(guiLeft + 152, guiTop + 70, this.zLevel, 16, 52);
    }
}
