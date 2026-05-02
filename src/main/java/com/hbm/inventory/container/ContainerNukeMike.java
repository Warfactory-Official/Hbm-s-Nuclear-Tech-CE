package com.hbm.inventory.container;

import com.hbm.inventory.TransferStrategy;
import com.hbm.items.ModItems;
import com.hbm.tileentity.bomb.TileEntityNukeMike;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerNukeMike extends Container {

private TileEntityNukeMike nukeMike;

	private static final TransferStrategy TRANSFER_STRATEGY = TransferStrategy.builder(8)
                                                                              .rule(0, 4, s -> s.getItem() == ModItems.explosive_lenses)
                                                                              .rule(4, 5, s -> s.getItem() == ModItems.man_core)
                                                                              .rule(5, 6, s -> s.getItem() == ModItems.mike_core)
                                                                              .rule(6, 7, s -> s.getItem() == ModItems.mike_deut)
                                                                              .rule(7, 8, s -> s.getItem() == ModItems.mike_cooling_unit)
                                                                              .build();

	public ContainerNukeMike(InventoryPlayer invPlayer, TileEntityNukeMike tedf) {

		nukeMike = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 26, 83));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 26, 101));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 44, 83));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 44, 101));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 39, 35));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 98, 91));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 6, 116, 91));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 134, 91));

		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 135 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 193));
		}
	}

	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
        return InventoryUtil.transferStack(this.inventorySlots, index, this.TRANSFER_STRATEGY, player);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return nukeMike.isUseableByPlayer(player);
	}

}
