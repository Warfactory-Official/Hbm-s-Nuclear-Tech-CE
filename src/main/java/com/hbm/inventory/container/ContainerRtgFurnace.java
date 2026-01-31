package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.tileentity.machine.TileEntityRtgFurnace;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerRtgFurnace extends Container {
	
	private TileEntityRtgFurnace rtgFurnace;
	private int dualCookTime;
	
	public ContainerRtgFurnace(InventoryPlayer invPlayer, TileEntityRtgFurnace tedf) {
		dualCookTime = 0;
		
		rtgFurnace = tedf;
		//Ore
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 56, 17));
		//RTG
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 38, 53));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 56, 53));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 74, 53));
		//Output
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 4, 116, 35));
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}
		
		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
		}
	}
	
	@Override
	public void addListener(IContainerListener crafting) {
		super.addListener(crafting);
		crafting.sendWindowProperty(this, 0, this.rtgFurnace.dualCookTime);
	}
	
	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
		return InventoryUtil.transferStack(this.inventorySlots, index, 5,
                s -> !(s.getItem() instanceof ItemRTGPellet), 1,
                s -> s.getItem() instanceof ItemRTGPellet, 5
        );
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return rtgFurnace.isUseableByPlayer(player);
	}
	
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

        for (IContainerListener listener : this.listeners) {
            if (this.dualCookTime != this.rtgFurnace.dualCookTime) {
                listener.sendWindowProperty(this, 0, this.rtgFurnace.dualCookTime);
            }
        }
		
		this.dualCookTime = this.rtgFurnace.dualCookTime;
	}
	
	@Override
	public void updateProgressBar(int i, int j) {
		if(i == 0)
		{
			rtgFurnace.dualCookTime = j;
		}
	}
}