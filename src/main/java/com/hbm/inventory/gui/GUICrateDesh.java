package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerCrateDesh;
import com.hbm.tileentity.machine.TileEntityCrateDesh;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUICrateDesh extends GUICrateBase<TileEntityCrateDesh, ContainerCrateDesh> {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_crate_desh.png");

    public GUICrateDesh(InventoryPlayer invPlayer, TileEntityCrateDesh tedf) {
        super(tedf, new ContainerCrateDesh(invPlayer, tedf), 248, 256, texture);
    }
}