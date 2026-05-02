package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerCrateBase;
import com.hbm.tileentity.machine.TileEntityCrate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;

public class GUICrateBase extends GuiContainer {

    protected final TileEntityCrate crate;

    public GUICrateBase(InventoryPlayer invPlayer, TileEntityCrate crate) {
        super(new ContainerCrateBase(invPlayer, crate));
        this.crate = crate;
        this.xSize = crate.getGuiWidth();
        this.ySize = crate.getGuiHeight();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    public void initGui() {
        super.initGui();
        if (mc.player != null) {
            crate.openInventory(mc.player);
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (mc.player != null) {
            crate.closeInventory(mc.player);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.crate.hasCustomName() ? this.crate.getName() : I18n.format(this.crate.getName());
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, crate.getTitleColor());
        this.fontRenderer.drawString(I18n.format("container.inventory"), crate.getInventoryLabelX(), this.ySize - 96 + 2, crate.getInventoryLabelColor());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(crate.getTexture());
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
