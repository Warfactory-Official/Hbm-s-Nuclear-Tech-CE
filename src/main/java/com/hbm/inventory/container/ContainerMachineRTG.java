package com.hbm.inventory.container;

import com.hbm.tileentity.machine.TileEntityMachineRTG;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineRTG extends Container {

	private final TileEntityMachineRTG testNuke;
	private int heat;
	
	public ContainerMachineRTG(InventoryPlayer invPlayer, TileEntityMachineRTG tedf) {
		heat = 0;
		
		testNuke = tedf;
		
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 26, 22));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 44, 22));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 62, 22));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 80, 22));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 98, 22));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 26, 40));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 6, 44, 40));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 62, 40));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 8, 80, 40));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 9, 98, 40));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 10, 26, 58));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 11, 44, 58));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 12, 62, 58));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 13, 80, 58));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 14, 98, 58));
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 94 + i * 18));
			}
		}
		
		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 152));
		}
	}
	
	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		listener.sendWindowProperty(this, 0, testNuke.heat);
	}
	
	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
		return InventoryUtil.transferStack(this.inventorySlots, index, 15);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return testNuke.isUseableByPlayer(player);
	}
	
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		
		for(int i = 0; i < this.listeners.size(); i++)
		{
			IContainerListener par1 = this.listeners.get(i);

			if(this.heat != this.testNuke.heat)
			{
				par1.sendWindowProperty(this, 0, this.testNuke.heat);
			}
		}

		this.heat = this.testNuke.heat;
	}
	
	@Override
	public void updateProgressBar(int i, int j) {
		if(i == 0)
		{
			testNuke.heat = j;
		}
	}
}
