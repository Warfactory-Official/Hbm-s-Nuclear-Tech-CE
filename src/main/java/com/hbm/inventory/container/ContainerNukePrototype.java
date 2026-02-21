package com.hbm.inventory.container;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBreedingRod;
import com.hbm.items.special.ItemCell;
import com.hbm.tileentity.bomb.TileEntityNukePrototype;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerNukePrototype extends Container {

	private TileEntityNukePrototype nukeTsar;
	
	public ContainerNukePrototype(InventoryPlayer invPlayer, TileEntityNukePrototype tedf) {
		nukeTsar = tedf;
		
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 8, 35));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 26, 35));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 44, 26));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 44, 44));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 62, 26));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 62, 44));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 6, 80, 26));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 80, 44));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 8, 98, 26));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 9, 98, 44));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 10, 116, 26));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 11, 116, 44));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 12, 134, 35));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 13, 152, 35));
		
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
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
        return InventoryUtil.transferStack(this.inventorySlots, index, 14,
                s -> ItemCell.isFullCell(s, Fluids.SAS3)
                        && !this.inventorySlots.getFirst().getHasStack() && !this.inventorySlots.get(1).getHasStack(), 2,
                s -> s.isItemEqual(new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.URANIUM.ordinal()))
                        && !this.inventorySlots.get(2).getHasStack() && !this.inventorySlots.get(3).getHasStack(), 4,
                s -> s.isItemEqual(new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.LEAD.ordinal()))
                        && !this.inventorySlots.get(4).getHasStack() && !this.inventorySlots.get(5).getHasStack(), 6,
                s -> s.isItemEqual(new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.NP237.ordinal())), 8,
                s -> s.isItemEqual(new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.LEAD.ordinal())), 10,
                s -> s.isItemEqual(new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.URANIUM.ordinal())), 12,
                s -> ItemCell.isFullCell(s, Fluids.SAS3), 14
        );
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return nukeTsar.isUseableByPlayer(player);
	}
}