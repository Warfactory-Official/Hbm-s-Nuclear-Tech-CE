package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.tileentity.turret.TileEntityTurretBaseNT;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUITurretSentry extends GUITurretBase {
    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/weapon/gui_turret_sentry.png");

    public GUITurretSentry(InventoryPlayer invPlayer, TileEntityTurretBaseNT tile) {
        super(invPlayer, tile);
    }

    protected ResourceLocation getTexture() {
        return texture;
    }
}
