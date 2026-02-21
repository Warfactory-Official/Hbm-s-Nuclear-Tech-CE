package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerCrateSteel;
import com.hbm.tileentity.machine.TileEntityCrateSteel;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUICrateSteel extends GUICrateBase<TileEntityCrateSteel, ContainerCrateSteel> {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_crate_steel.png");

    public GUICrateSteel(InventoryPlayer invPlayer, TileEntityCrateSteel tedf) {
        super(tedf, new ContainerCrateSteel(invPlayer, tedf), 176, 222, texture);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.diFurnace.hasCustomName() ? this.diFurnace.getName() : I18n.format(this.diFurnace.getName());
        float percent = this.diFurnace.fillPercentage;
        String title = combineTitle(name, percent);
        this.fontRenderer.drawString(title, this.xSize / 2 - this.fontRenderer.getStringWidth(title) / 2, 6, 0x1C1C1C);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 0x1C1C1C);
    }
}
