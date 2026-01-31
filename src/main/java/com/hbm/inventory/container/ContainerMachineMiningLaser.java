package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineMiningLaser;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineMiningLaser extends Container {

	private TileEntityMachineMiningLaser laser;

	public ContainerMachineMiningLaser(InventoryPlayer invPlayer, TileEntityMachineMiningLaser tedf) {
		laser = tedf;

		//Battery
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 8, 108));
		//Upgrades
		for(int i = 0; i < 2; i++)
			for(int j = 0; j < 4; j++)
				this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 1 + i * 4 + j, 98 + j * 18, 18 + i * 18));
		//Output
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 7; j++)
				this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 9 + i * 7 + j, 44 + j * 18, 72 + i * 18));

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + 56));

		for(int i = 0; i < 9; i++)
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142 + 56));
	}

	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
		return InventoryUtil.transferStack(this.inventorySlots, index, laser.inventory.getSlots(),
                Library::isBattery, 1,
                Library::isMachineUpgrade, 9);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return laser.isUseableByPlayer(player);
	}
}
