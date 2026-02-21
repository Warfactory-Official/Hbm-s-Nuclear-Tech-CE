package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerFileCabinet;
import com.hbm.tileentity.machine.storage.TileEntityFileCabinet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIFileCabinet extends GuiContainer {

    private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_file_cabinet.png");
    private TileEntityFileCabinet cabinet;

    public GUIFileCabinet(InventoryPlayer invPlayer, TileEntityFileCabinet tile) {
        super(new ContainerFileCabinet(invPlayer, tile));
        cabinet = tile;

        this.xSize = 176;
        this.ySize = 170;
    }

    @Override
    public void initGui() {
        super.initGui();
        if (mc.player != null) {
            cabinet.openInventory(mc.player);
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (mc.player != null) {
            cabinet.closeInventory(mc.player);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.cabinet.hasCustomName() ? this.cabinet.getName() : I18n.format(this.cabinet.getName());

        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
