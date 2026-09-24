package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotCraftingOutput;
import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.inventory.slot.SlotPattern;
import com.hbm.tileentity.machine.TileEntityCustomMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineCustom extends ContainerBase {

	private final TileEntityCustomMachine custom;

	public ContainerMachineCustom(InventoryPlayer playerInv, TileEntityCustomMachine tile) {
		super(playerInv, tile.inventory);
		custom = tile;

		//Input
		this.addSlotToContainer(new SlotNonRetarded(tile.inventory, 0, 150, 72));
		//Fluid IDs
		for(int i = 0; i < tile.inputTanks.length; i++) {
			this.addSlotToContainer(new SlotNonRetarded(tile.inventory, 1 + i, 8 + 18 * i, 54));
		}
		//Item inputs
		if(tile.config.itemInCount > 0) this.addSlotToContainer(new SlotNonRetarded(tile.inventory, 4, 8, 72));
		if(tile.config.itemInCount > 1) this.addSlotToContainer(new SlotNonRetarded(tile.inventory, 5, 26, 72));
		if(tile.config.itemInCount > 2) this.addSlotToContainer(new SlotNonRetarded(tile.inventory, 6, 44, 72));
		if(tile.config.itemInCount > 3) this.addSlotToContainer(new SlotNonRetarded(tile.inventory, 7, 8, 90));
		if(tile.config.itemInCount > 4) this.addSlotToContainer(new SlotNonRetarded(tile.inventory, 8, 26, 90));
		if(tile.config.itemInCount > 5) this.addSlotToContainer(new SlotNonRetarded(tile.inventory, 9, 44, 90));
		//Templates
		if(tile.config.itemInCount > 0) this.addSlotToContainer(new SlotPattern(tile.inventory, 10, 8, 108));
		if(tile.config.itemInCount > 1) this.addSlotToContainer(new SlotPattern(tile.inventory, 11, 26, 108));
		if(tile.config.itemInCount > 2) this.addSlotToContainer(new SlotPattern(tile.inventory, 12, 44, 108));
		if(tile.config.itemInCount > 3) this.addSlotToContainer(new SlotPattern(tile.inventory, 13, 8, 126));
		if(tile.config.itemInCount > 4) this.addSlotToContainer(new SlotPattern(tile.inventory, 14, 26, 126));
		if(tile.config.itemInCount > 5) this.addSlotToContainer(new SlotPattern(tile.inventory, 15, 44, 126));
		//Output
		if(tile.config.itemOutCount > 0) this.addSlotToContainer(new SlotCraftingOutput(playerInv.player, tile.inventory, 16, 78, 72));
		if(tile.config.itemOutCount > 1) this.addSlotToContainer(new SlotCraftingOutput(playerInv.player, tile.inventory, 17, 96, 72));
		if(tile.config.itemOutCount > 2) this.addSlotToContainer(new SlotCraftingOutput(playerInv.player, tile.inventory, 18, 114, 72));
		if(tile.config.itemOutCount > 3) this.addSlotToContainer(new SlotCraftingOutput(playerInv.player, tile.inventory, 19, 78, 90));
		if(tile.config.itemOutCount > 4) this.addSlotToContainer(new SlotCraftingOutput(playerInv.player, tile.inventory, 20, 96, 90));
		if(tile.config.itemOutCount > 5) this.addSlotToContainer(new SlotCraftingOutput(playerInv.player, tile.inventory, 21, 114, 90));

		playerInv(playerInv, 8, 174, 232);
	}

	@Override
	public boolean canInteractWith(@NotNull EntityPlayer player) {
		return custom.isUseableByPlayer(player);
	}

	@Override
	public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public @NotNull ItemStack slotClick(int index, int button, @NotNull ClickType mode, @NotNull EntityPlayer player) {

		if(index < 0 || index >= this.inventorySlots.size() || !(this.inventorySlots.get(index) instanceof SlotPattern)) {
			return super.slotClick(index, button, mode, player);
		}

		Slot slot = this.getSlot(index);
		int tileIndex = slot.getSlotIndex();

		ItemStack ret = ItemStack.EMPTY;
		ItemStack held = player.inventory.getItemStack();

		if(slot.getHasStack())
			ret = slot.getStack().copy();

		if(button == 1 && mode == ClickType.PICKUP && slot.getHasStack()) {
			custom.matcher.nextMode(player.world, slot.getStack(), tileIndex - 10);
			return ret;

		} else {

			slot.putStack(held.isEmpty() ? ItemStack.EMPTY : held.copy());
			custom.matcher.initPatternSmart(player.world, slot.getStack(), tileIndex - 10);

			return ret;
		}
	}
}
