package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.tileentity.turret.TileEntityTurretBaseNT;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUITurretJeremy extends GUITurretBase {
	
	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/weapon/gui_turret_cannon.png");

	public GUITurretJeremy(InventoryPlayer invPlayer, TileEntityTurretBaseNT tedf) {
		super(invPlayer, tedf);
	}
	
	protected ResourceLocation getTexture() {
		return texture;
	}

	@Override
	public int getTurretFontColor(){
		return 0x373737;
	}
}
