package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotCraftingOutput;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineCyclotron;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineCyclotron extends Container {

	private TileEntityMachineCyclotron cyclotron;
	
	public ContainerMachineCyclotron(InventoryPlayer invPlayer, TileEntityMachineCyclotron tile) {
		cyclotron = tile;

		//Input
		this.addSlotToContainer(new SlotItemHandler(tile.inventory, 0, 11, 18));
		this.addSlotToContainer(new SlotItemHandler(tile.inventory, 1, 11, 36));
		this.addSlotToContainer(new SlotItemHandler(tile.inventory, 2, 11, 54));
		//Targets
		this.addSlotToContainer(new SlotItemHandler(tile.inventory, 3, 101, 18));
		this.addSlotToContainer(new SlotItemHandler(tile.inventory, 4, 101, 36));
		this.addSlotToContainer(new SlotItemHandler(tile.inventory, 5, 101, 54));
		//Output
		this.addSlotToContainer(new SlotCraftingOutput(invPlayer.player, tile.inventory, 6, 131, 18));
		this.addSlotToContainer(new SlotCraftingOutput(invPlayer.player, tile.inventory, 7, 131, 36));
		this.addSlotToContainer(new SlotCraftingOutput(invPlayer.player, tile.inventory, 8, 131, 54));
		//Battery
		this.addSlotToContainer(new SlotBattery(tile.inventory, 9, 168, 83));
		//Upgrades
		this.addSlotToContainer(new SlotUpgrade(tile.inventory, 10, 60, 81));
		this.addSlotToContainer(new SlotUpgrade(tile.inventory, 11, 78, 81));
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 15 + j * 18, 133 + i * 18));
			}
		}
		
		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 15 + i * 18, 191));
		}
	}

    private static boolean isNormal(ItemStack stack) {
        return !Library.isBattery(stack) && !Library.isMachineUpgrade(stack);
    }

	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return InventoryUtil.transferStack(this.inventorySlots, index, 12,
                s -> s.getItem() == ModItems.part_lithium || s.getItem() == ModItems.part_beryllium ||
                        s.getItem() == ModItems.part_carbon || s.getItem() == ModItems.part_copper || s.getItem() == ModItems.part_plutonium, 3,
                        ContainerMachineCyclotron::isNormal, 9,
                        Library::isBattery, 10,
                        Library::isMachineUpgrade, 12);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return cyclotron.isUseableByPlayer(player);
	}
	
}
