package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerFurnaceCombo;
import com.hbm.tileentity.machine.TileEntityFurnaceCombination;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;

public class GUIFurnaceCombo extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_furnace_combination.png");
    private final TileEntityFurnaceCombination furnace;

    public GUIFurnaceCombo(InventoryPlayer invPlayer, TileEntityFurnaceCombination tedf) {
        super(new ContainerFurnaceCombo(invPlayer, tedf));
        furnace = tedf;

        this.xSize = 176;
        this.ySize = 186;
    }

    @Override
    public void drawScreen(int x, int y, float interp) {
        super.drawScreen(x, y, interp);

        furnace.tank.renderTankInfo(this, x, y, guiLeft + 118, guiTop + 18, 16, 52);

        this.drawCustomInfoStat(x, y, guiLeft + 44, guiTop + 36, 39, 7, x, y, new String[]{String.format(Locale.US, "%,d", furnace.progress) + " / " +
                                                                                           String.format(Locale.US, "%,d",
                                                                                                   TileEntityFurnaceCombination.processTime) + "TU"});
        this.drawCustomInfoStat(x, y, guiLeft + 44, guiTop + 45, 39, 7, x, y, new String[]{
                String.format(Locale.US, "%,d", furnace.heat) + " / " + String.format(Locale.US, "%,d", TileEntityFurnaceCombination.maxHeat) +
                "TU"});
        super.renderHoveredToolTip(x, y);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.furnace.hasCustomName() ? this.furnace.getName() : I18n.format(this.furnace.getName());

        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float interp, int x, int y) {
        this.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int p = furnace.progress * 38 / TileEntityFurnaceCombination.processTime;
        drawTexturedModalRect(guiLeft + 45, guiTop + 37, 176, 0, p, 5);

        int h = furnace.heat * 37 / TileEntityFurnaceCombination.maxHeat;
        drawTexturedModalRect(guiLeft + 45, guiTop + 46, 176, 5, h, 5);

        furnace.tank.renderTank(guiLeft + 118, guiTop + 70, this.zLevel, 16, 52);
    }
}
