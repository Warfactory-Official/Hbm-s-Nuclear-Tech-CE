package com.hbm.inventory.container;

import com.hbm.items.ModItems;
import com.hbm.tileentity.bomb.TileEntityNukeBalefire;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerNukeBalefire extends Container {

	private TileEntityNukeBalefire balefireBomb;

	public ContainerNukeBalefire(InventoryPlayer invPlayer, TileEntityNukeBalefire tedf) {

		balefireBomb = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 17, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 53, 36));

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
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
		return InventoryUtil.transferStack(this.inventorySlots, index, 2,
                s -> s.getItem() == ModItems.egg_balefire, 1,
                s -> s.getItem() == ModItems.battery_spark || s.getItem() == ModItems.battery_trixite, 2);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return balefireBomb.isUseableByPlayer(player);
	}
}