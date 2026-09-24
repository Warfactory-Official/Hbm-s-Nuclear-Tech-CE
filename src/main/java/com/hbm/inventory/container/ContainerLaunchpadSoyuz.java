package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.tileentity.machine.TileEntityLaunchpadSoyuz;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerLaunchpadSoyuz extends ContainerBase {

	public ContainerLaunchpadSoyuz(InventoryPlayer invPlayer, TileEntityLaunchpadSoyuz pad) {
		super(invPlayer, pad.inventory);

		// Soyuz
		this.addSlotToContainer(new SlotNonRetarded(pad.inventory, 0, 98, 80));
		// Designator
		this.addSlotToContainer(new SlotNonRetarded(pad.inventory, 1, 80, 80));
		// Satellite
		this.addSlotToContainer(new SlotNonRetarded(pad.inventory, 2, 98, 26));
		// Landing module
		this.addSlotToContainer(new SlotNonRetarded(pad.inventory, 3, 80, 26));
		// Kerosene IN / OUT
		this.addSlotToContainer(new SlotItemHandler(pad.inventory, 4, 152, 98));
		this.addSlotToContainer(new SlotItemHandler(pad.inventory, 5, 152, 116));
		// Oxygen IN / OUT
		this.addSlotToContainer(new SlotItemHandler(pad.inventory, 6, 170, 98));
		this.addSlotToContainer(new SlotItemHandler(pad.inventory, 7, 170, 116));
		// Battery
		this.addSlotToContainer(new SlotItemHandler(pad.inventory, 8, 134, 98));

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 6; j++) {
				this.addSlotToContainer(new SlotNonRetarded(pad.inventory, j + i * 6 + 9, 44 - i * 18, 26 + j * 18));
			}
		}

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 17 + j * 18, 162 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 17 + i * 18, 220));
		}
	}
}
