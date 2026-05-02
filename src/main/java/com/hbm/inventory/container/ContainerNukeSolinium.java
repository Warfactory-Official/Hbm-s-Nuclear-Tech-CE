package com.hbm.inventory.container;

import com.hbm.inventory.TransferStrategy;
import com.hbm.items.ModItems;
import com.hbm.tileentity.bomb.TileEntityNukeSolinium;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerNukeSolinium extends Container {

	private TileEntityNukeSolinium nukeSol;

	private final TransferStrategy transferStrategy = TransferStrategy.builder(9)
                                                                      .rule(0, 1, s -> s.getItem() == ModItems.solinium_igniter && !this.inventorySlots.getFirst().getHasStack())
                                                                      .rule(1, 3, s -> s.getItem() == ModItems.solinium_propellant && !this.inventorySlots.get(1).getHasStack() && !this.inventorySlots.get(2).getHasStack())
                                                                      .rule(3, 4, s -> s.getItem() == ModItems.solinium_igniter && !this.inventorySlots.get(3).getHasStack())
                                                                      .rule(4, 5, s -> s.getItem() == ModItems.solinium_core)
                                                                      .rule(5, 6, s -> s.getItem() == ModItems.solinium_igniter && !this.inventorySlots.get(5).getHasStack())
                                                                      .rule(6, 8, s -> s.getItem() == ModItems.solinium_propellant)
                                                                      .rule(8, 9, s -> s.getItem() == ModItems.solinium_igniter)
                                                                      .build();

	public ContainerNukeSolinium(InventoryPlayer invPlayer, TileEntityNukeSolinium tedf) {

		nukeSol = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 26, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 53, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 107, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 134, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 80, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 26, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 6, 53, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 107, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 8, 134, 54));

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
        return InventoryUtil.transferStack(this.inventorySlots, index, this.transferStrategy, player);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return nukeSol.isUseableByPlayer(player);
	}
}
