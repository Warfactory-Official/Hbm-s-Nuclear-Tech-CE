package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineCentrifuge;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerCentrifuge extends Container {

	private TileEntityMachineCentrifuge centrifuge;
	
	public ContainerCentrifuge(InventoryPlayer invPlayer, TileEntityMachineCentrifuge te) {
		
		centrifuge = te;
		
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 0, 36, 50));
		this.addSlotToContainer(new SlotBattery(te.inventory, 1, 9, 50));
		this.addSlotToContainer(SlotFiltered.takeOnly(te.inventory, 2, 63, 50));
		this.addSlotToContainer(SlotFiltered.takeOnly(te.inventory, 3, 83, 50));
		this.addSlotToContainer(SlotFiltered.takeOnly(te.inventory, 4, 103, 50));
		this.addSlotToContainer(SlotFiltered.takeOnly(te.inventory, 5, 123, 50));
		this.addSlotToContainer(new SlotUpgrade(te.inventory, 6, 149, 22));
		this.addSlotToContainer(new SlotUpgrade(te.inventory, 7, 149, 40));
		
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 104 + i * 18));
			}
		}
		
		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 162));
		}
	}

    private static boolean isNormal(ItemStack stack) {
        return !Library.isBattery(stack) && !Library.isMachineUpgrade(stack);
    }

	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return InventoryUtil.transferStack(this.inventorySlots, index, 8,
                ContainerCentrifuge::isNormal, 1,
                Library::isBattery, 2,
                ContainerCentrifuge::isNormal, 6,
                Library::isMachineUpgrade, 8);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return centrifuge.isUseableByPlayer(player);
	}
}
