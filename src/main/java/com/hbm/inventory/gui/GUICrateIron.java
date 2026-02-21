package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerCrateIron;
import com.hbm.tileentity.machine.TileEntityCrateIron;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUICrateIron extends GUICrateBase<TileEntityCrateIron, ContainerCrateIron> {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_crate_iron.png");

    public GUICrateIron(InventoryPlayer invPlayer, TileEntityCrateIron tedf) {
        super(tedf, new ContainerCrateIron(invPlayer, tedf), 176, 186, texture);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.diFurnace.hasCustomName() ? this.diFurnace.getName() : I18n.format(this.diFurnace.getName());
        float percent = this.diFurnace.fillPercentage;
        String combinedTitle = combineTitle(name, percent);
        this.fontRenderer.drawString(combinedTitle, this.xSize / 2 - this.fontRenderer.getStringWidth(combinedTitle) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }
}