package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerCraneInserter;
import com.hbm.tileentity.network.TileEntityCraneInserter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUICraneInserter extends GuiInfoContainer {
    private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_crane_inserter.png");
    private TileEntityCraneInserter inserter;

    public GUICraneInserter(InventoryPlayer invPlayer, TileEntityCraneInserter tedf) {
        super(new ContainerCraneInserter(invPlayer, tedf));
        inserter = tedf;

        this.xSize = 176;
        this.ySize = 185;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.inserter.hasCustomName() ? this.inserter.getName() : I18n.format(this.inserter.getName());
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    public void drawScreen(int x, int y, float interp) {
        super.drawScreen(x, y, interp);
        this.renderHoveredToolTip(x, y);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
