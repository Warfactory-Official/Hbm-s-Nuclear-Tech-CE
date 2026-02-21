package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.tileentity.machine.TileEntityMachineChemplant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineChemplant extends Container {

	private TileEntityMachineChemplant nukeBoy;

	private int progress;
	private int maxProgress;
	
	public ContainerMachineChemplant(InventoryPlayer invPlayer, TileEntityMachineChemplant tedf) {
		progress = 0;
		nukeBoy = tedf;

		//Battery
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 80, 18));
		//Upgrades
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 116, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 116, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 116, 54));
		//Schematic
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 80, 54));
		//Outputs
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 5, 134, 90));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 6, 152, 90));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 7, 134, 108));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 8, 152, 108));
		//Fluid Output In
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 9, 134, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 10, 152, 54));
		//Fluid Outputs Out
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 11, 134, 72));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 12, 152, 72));
		//Input
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 13, 8, 90));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 14, 26, 90));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 15, 8, 108));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 16, 26, 108));
		//Fluid Input In
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 17, 8, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 18, 26, 54));
		//Fluid Input Out
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 19, 8, 72));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 20, 26, 72));
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + 56));
			}
		}
		
		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142 + 56));
		}
	}
	
	@Override
    public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int par2)
    {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = (Slot) this.inventorySlots.get(par2);
		
		if (var4 != null && var4.getHasStack())
		{
			ItemStack var5 = var4.getStack();
			var3 = var5.copy();
			
            if (par2 <= 20) {
				if (!this.mergeItemStack(var5, 21, this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.mergeItemStack(var5, 4, 5, false))
				if (!this.mergeItemStack(var5, 13, 19, false))
					return ItemStack.EMPTY;
			
			if (var5.getCount() == 0)
			{
				var4.putStack(ItemStack.EMPTY);
			}
			else
			{
				var4.onSlotChanged();
			}
		}
		
		return var3;
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return nukeBoy.isUseableByPlayer(player);
	}
	
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		
		for(int i = 0; i < this.listeners.size(); i++)
		{
			IContainerListener par1 = (IContainerListener)this.listeners.get(i);
			
			if(this.progress != this.nukeBoy.progress)
			{
				par1.sendWindowProperty(this, 1, this.nukeBoy.progress);
			}
			
			if(this.maxProgress != this.nukeBoy.maxProgress)
			{
				par1.sendWindowProperty(this, 2, this.nukeBoy.maxProgress);
			}
		}

		this.progress= this.nukeBoy.progress;
		this.maxProgress= this.nukeBoy.maxProgress;
	}
	
	@Override
	public void updateProgressBar(int i, int j) {
		if(i == 1)
		{
			nukeBoy.progress = j;
		}
		if(i == 2)
		{
			nukeBoy.maxProgress = j;
		}
	}
}
