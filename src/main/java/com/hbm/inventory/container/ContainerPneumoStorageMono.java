package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotPattern;
import com.hbm.tileentity.network.TileEntityPneumoStorageMono;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerPneumoStorageMono extends ContainerBase {

	private final TileEntityPneumoStorageMono mono;

	public ContainerPneumoStorageMono(InventoryPlayer invPlayer, TileEntityPneumoStorageMono mono) {
		super(invPlayer, mono.inventory);
		this.mono = mono;

		for(int i = 0; i < 3; i++) {
			this.addSlotToContainer(new SlotPattern(mono.inventory, i, 8, 17 + i * 18));
		}

		playerInv(invPlayer, 8, 99);
	}

	@Override
	public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public @NotNull ItemStack slotClick(int index, int button, @NotNull ClickType type, @NotNull EntityPlayer player) {

		if(index < 0 || index >= 3) {
			return super.slotClick(index, button, type, player);
		}

		Slot slot = this.getSlot(index);

		if(mono.amounts[index] > 0 && slot.getHasStack()) return ItemStack.EMPTY;

		ItemStack ret = slot.getHasStack() ? slot.getStack().copy() : ItemStack.EMPTY;
		ItemStack held = player.inventory.getItemStack();
		slot.putStack(held.isEmpty() ? ItemStack.EMPTY : held.copy());

		return ret;
	}
}
