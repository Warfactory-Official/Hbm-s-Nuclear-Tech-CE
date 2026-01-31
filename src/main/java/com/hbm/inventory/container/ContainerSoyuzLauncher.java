package com.hbm.inventory.container;

import com.hbm.api.item.IDesignatorItem;
import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemSatellite;
import com.hbm.items.special.ItemSoyuz;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntitySoyuzLauncher;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerSoyuzLauncher extends Container {

	private TileEntitySoyuzLauncher launcher;
	
	public ContainerSoyuzLauncher(InventoryPlayer invPlayer, TileEntitySoyuzLauncher tedf) {
		
		launcher = tedf;

		//Soyuz
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 62, 18));
		//Designator
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 62, 36));
		//Satellite
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 116, 18));
		//Landing module
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 116, 36));
		//Kerosene IN
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 8, 90));
		//Kerosene OUT
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 5, 8, 108));
		//Peroxide IN
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 6, 26, 90));
		//Peroxide OUT
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 7, 26, 108));
		//Battery
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 8, 44, 108));
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 6; j++)
			{
				this.addSlotToContainer(new SlotItemHandler(tedf.inventory, j + i * 6 + 9, 62 + j * 18, 72 + i * 18));
			}
		}
		
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
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return InventoryUtil.transferStack(this.inventorySlots, index, 27,
                s -> s.getItem() instanceof ItemSoyuz, 1,
                s -> s.getItem() instanceof IDesignatorItem, 2,
                s -> s.getItem() instanceof ItemSatellite, 3,
                s -> s.getItem() == ModItems.missile_soyuz_lander, 4,
                s -> Library.isStackDrainableForTank(s, launcher.tanks[0]), 6,
                s -> Library.isStackDrainableForTank(s, launcher.tanks[1]), 8,
                Library::isBattery, 9
        );
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return launcher.isUseableByPlayer(player);
	}
}