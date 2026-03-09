package com.hbm.inventory.container;

import com.hbm.tileentity.machine.TileEntityCrateSteel;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerCrateSteel extends Container {

	// mlbv: do not rename this field, it's shadowed in one of bogosorter's mixin
	private final TileEntityCrateSteel diFurnace;
	
	public ContainerCrateSteel(InventoryPlayer invPlayer, TileEntityCrateSteel te) {
		diFurnace = te;
		
		for(int i = 0; i < 6; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new SlotItemHandler(te.inventory, j + i * 9, 8 + j * 18, 18 + i * 18));
			}
		}
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + (18 * 3) + 2));
			}
		}
		
		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142 + (18 * 3) + 2));
		}
	}
	
	@Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, this.diFurnace.inventory.getSlots());
    }

	@Override
	public boolean canInteractWith(@NotNull EntityPlayer player) {
		return diFurnace.isUseableByPlayer(player);
	}

	@Override
	public @NotNull ItemStack slotClick(int slotId, int dragType, @NotNull ClickType clickTypeIn, @NotNull EntityPlayer player) {
		if (this.diFurnace != null && !this.diFurnace.boundItem.isEmpty()) {
			if (slotId >= 0 && slotId < this.inventorySlots.size()) {
				Slot slot = this.inventorySlots.get(slotId);
				if (slot != null && slot.inventory == player.inventory && slot.getSlotIndex() == player.inventory.currentItem) {
					return ItemStack.EMPTY;
				}
			}

			if (clickTypeIn == ClickType.SWAP && dragType == player.inventory.currentItem) {
				return ItemStack.EMPTY;
			}
		}

		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}
}
