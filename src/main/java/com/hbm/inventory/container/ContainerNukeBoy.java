package com.hbm.inventory.container;

import com.hbm.inventory.TransferStrategy;
import com.hbm.items.ModItems;
import com.hbm.tileentity.bomb.TileEntityNukeBoy;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerNukeBoy extends Container {

private TileEntityNukeBoy nukeBoy;

	private static final TransferStrategy TRANSFER_STRATEGY = TransferStrategy.builder(5)
                                                                              .rule(0, 1, s -> s.getItem() == ModItems.boy_shielding)
                                                                              .rule(1, 2, s -> s.getItem() == ModItems.boy_target)
                                                                              .rule(2, 3, s -> s.getItem() == ModItems.boy_bullet)
                                                                              .rule(3, 4, s -> s.getItem() == ModItems.boy_propellant)
                                                                              .rule(4, 5, s -> s.getItem() == ModItems.boy_igniter)
                                                                              .build();

	public ContainerNukeBoy(InventoryPlayer invPlayer, TileEntityNukeBoy tedf) {

		nukeBoy = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 26, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 44, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 62, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 80, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 98, 36));

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
		return InventoryUtil.transferStack(this.inventorySlots, index, this.TRANSFER_STRATEGY, player);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return nukeBoy.isUseableByPlayer(player);
	}

}
