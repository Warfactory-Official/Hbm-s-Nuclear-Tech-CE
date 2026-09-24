package com.hbm.inventory.container;

import com.hbm.tileentity.machine.TileEntityMachineTapeDrive;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerTapeDrive extends ContainerBase {

	public ContainerTapeDrive(InventoryPlayer invPlayer, TileEntityMachineTapeDrive drive) {
		super(invPlayer, drive.inventory);

		this.addSlots(drive.inventory, 0, 35, 27, 2, 6);
		this.playerInv(invPlayer, 8, 104);
	}
}
