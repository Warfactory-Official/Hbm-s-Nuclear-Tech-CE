package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.tileentity.machine.TileEntityMachineRadGen;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineRadGen extends Container {
	
	private TileEntityMachineRadGen radGen;
	
	public ContainerMachineRadGen(InventoryPlayer invPlayer, TileEntityMachineRadGen tedf) {
		radGen = tedf;

		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 3; j++) {
				this.addSlotToContainer(new SlotItemHandler(tedf.inventory, j + i * 3, 8 + j * 18, 17 + i * 18));
			}
		}
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 3; j++) {
				this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, j + i * 3 + 12, 116 + j * 18, 17 + i * 18));
			}
		}

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 102 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 160));
		}
	}
	
	@Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
		return InventoryUtil.transferStack(this.inventorySlots, index, 24);
    }

	@Override
	public boolean canInteractWith(@NotNull EntityPlayer player) {
		return radGen.isUseableByPlayer(player);
	}
}