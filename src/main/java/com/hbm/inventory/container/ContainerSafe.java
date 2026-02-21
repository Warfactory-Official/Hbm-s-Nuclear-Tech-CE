package com.hbm.inventory.container;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import com.hbm.tileentity.machine.TileEntitySafe;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.SlotItemHandler;

// see comments at ContainerCrateTemplate
@Optional.Interface(iface = "com.cleanroommc.bogosorter.api.ISortableContainer", modid = "bogosorter")
public class ContainerSafe extends Container implements ISortableContainer {
	
	private TileEntitySafe safe;
	
	public ContainerSafe(InventoryPlayer invPlayer, TileEntitySafe tedf) {
		safe = tedf;
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 5; j++)
			{
				this.addSlotToContainer(new SlotItemHandler(tedf.inventory, j + i * 5, 8 + j * 18 + 18 * 2, 18 + i * 18));
			}
		}
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + 2));
			}
		}
		
		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142 + 2));
		}
	}
	
	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
		return InventoryUtil.transferStack(this.inventorySlots, index, safe.inventory.getSlots());
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return safe.isUseableByPlayer(player);
	}

	@Override
	@Optional.Method(modid = "bogosorter")
	public void buildSortingContext(ISortingContextBuilder builder) {
		builder.addSlotGroup(0, safe.inventory.getSlots(), 5);
	}
}