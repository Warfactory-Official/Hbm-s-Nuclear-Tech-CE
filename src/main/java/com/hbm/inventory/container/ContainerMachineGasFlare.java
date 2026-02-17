package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.oil.TileEntityMachineGasFlare;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineGasFlare extends Container {

	private final TileEntityMachineGasFlare gasFlare;
	
	public ContainerMachineGasFlare(InventoryPlayer invPlayer, TileEntityMachineGasFlare te) {
		gasFlare = te;

		//Battery
		this.addSlotToContainer(new SlotBattery(te.inventory, 0, 143, 71));
		//Fluid in
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 1, 17, 17));
		//Fluid out
		this.addSlotToContainer(SlotFiltered.takeOnly(te.inventory, 2, 17, 53));
		//Fluid ID
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 3, 35, 71));
		//Upgrades
		this.addSlotToContainer(new SlotUpgrade(te.inventory, 4, 80, 71));
		this.addSlotToContainer(new SlotUpgrade(te.inventory, 5, 98, 71));

		int offset = 37;
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + offset));
			}
		}
		
		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142 + offset));
		}
	}
	
	@Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index)
    {
		return InventoryUtil.transferStack(this.inventorySlots, index, 6,
                Library::isBattery, 1,
                s -> Library.isStackFillableForTank(s, gasFlare.tank), 3,
                s -> s.getItem() instanceof IItemFluidIdentifier, 4,
                Library::isMachineUpgrade, 6);
    }

	@Override
	public boolean canInteractWith(@NotNull EntityPlayer player) {
		return gasFlare.isUseableByPlayer(player);
	}
}
