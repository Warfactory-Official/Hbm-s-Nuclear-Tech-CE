package com.hbm.inventory.container;

import com.hbm.tileentity.network.TileEntityPneumoStorageImporter;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerPneumoStorageImporter extends ContainerBase {

	public ContainerPneumoStorageImporter(InventoryPlayer invPlayer, TileEntityPneumoStorageImporter importer) {
		super(invPlayer, importer.inventory);

		addSlots(importer.inventory, 0, 62, 17, 3, 3);
		playerInv(invPlayer, 8, 103);
	}
}
