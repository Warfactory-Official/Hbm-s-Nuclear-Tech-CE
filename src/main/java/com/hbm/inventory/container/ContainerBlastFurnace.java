package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotCraftingOutput;
import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.tileentity.machine.TileEntityMachineBlastFurnace;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerBlastFurnace extends ContainerBase {

	protected TileEntityMachineBlastFurnace tile;

	public ContainerBlastFurnace(InventoryPlayer invPlayer, TileEntityMachineBlastFurnace tedf) {
		super(invPlayer, tedf.getCheckedInventory());
		this.tile = tedf;

		// Fuel
		this.addSlotToContainer(new SlotNonRetarded(tedf.getCheckedInventory(), 0, 80, 81));
		// Input
		this.addSlotToContainer(new SlotNonRetarded(tedf.getCheckedInventory(), 1, 80, 27));
		this.addSlotToContainer(new SlotNonRetarded(tedf.getCheckedInventory(), 2, 80, 45));
		// Output
		this.addSlotToContainer(new SlotCraftingOutput(invPlayer.player, tedf.getCheckedInventory(), 3, 134, 72));
		this.addSlotToContainer(new SlotCraftingOutput(invPlayer.player, tedf.getCheckedInventory(), 4, 134, 90));

		this.playerInv(invPlayer, 8, 140);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return tile.isUseableByPlayer(player);
	}
}
