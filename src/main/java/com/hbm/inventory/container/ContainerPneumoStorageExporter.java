package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotPattern;
import com.hbm.tileentity.network.TileEntityPneumoStorageExporter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerPneumoStorageExporter extends ContainerBase {

	public ContainerPneumoStorageExporter(InventoryPlayer invPlayer, TileEntityPneumoStorageExporter exporter) {
		super(invPlayer, exporter.inventory);

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new SlotPattern(exporter.inventory, i, 17 + (i % 3) * 18, 17 + (i / 3) * 18).allowStackSize());
		}

		addTakeOnlySlots(exporter.inventory, 9, 80, 17, 3, 3);
		playerInv(invPlayer, 8, 103);
	}

	@Override
	public @NotNull ItemStack slotClick(int index, int button, @NotNull ClickType type, @NotNull EntityPlayer player) {

		if(index < 0 || index >= 9) {
			return super.slotClick(index, button, type, player);
		}

		if(type != ClickType.PICKUP) return ItemStack.EMPTY;

		Slot slot = this.getSlot(index);
		ItemStack ret = slot.getHasStack() ? slot.getStack().copy() : ItemStack.EMPTY;
		ItemStack held = player.inventory.getItemStack();

		slot.putStack(held.isEmpty() ? ItemStack.EMPTY : held.copy());
		slot.onSlotChanged();

		return ret;
	}

	@Override
	public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
		Slot slot = this.inventorySlots.get(index);

		if(index < 9 || index >= 18 || slot == null || !slot.getHasStack()) return ItemStack.EMPTY;

		ItemStack stack = slot.getStack();
		ItemStack result = stack.copy();

		if(!this.mergeItemStack(stack, 18, this.inventorySlots.size(), true)) return ItemStack.EMPTY;

		if(stack.isEmpty()) {
			slot.putStack(ItemStack.EMPTY);
		} else {
			slot.onSlotChanged();
		}

		slot.onTake(player, stack);

		return result;
	}
}
