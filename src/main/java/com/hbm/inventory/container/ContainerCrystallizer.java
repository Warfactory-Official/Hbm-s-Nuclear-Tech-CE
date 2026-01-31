package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineCrystallizer;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerCrystallizer extends Container {

	private TileEntityMachineCrystallizer crystallizer;

	public ContainerCrystallizer(InventoryPlayer invPlayer, TileEntityMachineCrystallizer te) {
		crystallizer = te;

		this.addSlotToContainer(new SlotItemHandler(te.inventory, 0, 62, 45));
		this.addSlotToContainer(new SlotBattery(te.inventory, 1, 152, 72));
		this.addSlotToContainer(SlotFiltered.takeOnly(te.inventory, 2, 113, 45));
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 3, 17, 18));
		this.addSlotToContainer(SlotFiltered.takeOnly(te.inventory, 4, 17, 54));
		this.addSlotToContainer(new SlotUpgrade(te.inventory, 5, 80, 18));
		this.addSlotToContainer(new SlotUpgrade(te.inventory, 6, 98, 18));
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 7, 35, 72));

		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 180));
		}
	}

    private static boolean isNormal(ItemStack stack) {
        return !Library.isBattery(stack) && !Library.isMachineUpgrade(stack) && !(stack.getItem() instanceof IItemFluidIdentifier);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 8,
                ContainerCrystallizer::isNormal, 1,
                Library::isBattery, 2,
                ContainerCrystallizer::isNormal, 5,
                Library::isMachineUpgrade, 7,
                s -> s.getItem() instanceof IItemFluidIdentifier, 8);
    }

    @Override
	public boolean canInteractWith(EntityPlayer player) {
		return crystallizer.isUseableByPlayer(player);
	}
}