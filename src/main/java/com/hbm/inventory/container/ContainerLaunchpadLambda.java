package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.tileentity.machine.TileEntityLaunchpadLambda;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerLaunchpadLambda extends ContainerBase {

	public ContainerLaunchpadLambda(InventoryPlayer invPlayer, TileEntityLaunchpadLambda pad) {
		super(invPlayer, pad.inventory);

		// Lambda
		this.addSlotToContainer(new SlotNonRetarded(pad.inventory, 0, 35, 17));
		// Satellite
		this.addSlotToContainer(new SlotNonRetarded(pad.inventory, 1, 53, 17));
		// Gasoline IN / OUT
		this.addSlotToContainer(new SlotItemHandler(pad.inventory, 2, 107, 80));
		this.addSlotToContainer(new SlotItemHandler(pad.inventory, 3, 107, 98));
		// Peroxide IN / OUT
		this.addSlotToContainer(new SlotItemHandler(pad.inventory, 4, 125, 80));
		this.addSlotToContainer(new SlotItemHandler(pad.inventory, 5, 125, 98));
		// Battery
		this.addSlotToContainer(new SlotItemHandler(pad.inventory, 6, 89, 80));

		this.playerInv(invPlayer, 8, 144);
	}
}
