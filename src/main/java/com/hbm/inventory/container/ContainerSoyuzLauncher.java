package com.hbm.inventory.container;

import com.hbm.api.item.IDesignatorItem;
import com.hbm.inventory.TransferStrategy;
import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.ModItems;
import com.hbm.items.ISatChip;
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
    private final TransferStrategy transferStrategy = TransferStrategy.builder(27)
                                                                      .rule(0, 1, s -> s.getItem() instanceof ItemSoyuz)
                                                                      .rule(1, 2, s -> s.getItem() instanceof IDesignatorItem)
                                                                      .rule(2, 3, s -> s.getItem() instanceof ISatChip)
                                                                      .rule(3, 4, s -> s.getItem() == ModItems.missile_soyuz_lander)
                                                                      .rule(4, 6, s -> Library.isStackDrainableForTank(s, launcher.tanks[0]))
                                                                      .rule(6, 8, s -> Library.isStackDrainableForTank(s, launcher.tanks[1]))
                                                                      .rule(8, 9, Library::isBattery)
                                                                      .genericMachineRange(9)
                                                                      .build();

	public ContainerSoyuzLauncher(InventoryPlayer invPlayer, TileEntitySoyuzLauncher tedf) {

		launcher = tedf;

		//Soyuz
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 98, 80));
		//Designator
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 80, 80));
		//Satellite
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 98, 26));
		//Landing module
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 80, 26));
		//Kerosene IN
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 152, 98));
		//Kerosene OUT
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 5, 152, 116));
		//Peroxide IN
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 6, 170, 98));
		//Peroxide OUT
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 7, 170, 116));
		//Battery
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 8, 134, 98));

		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 6; j++)
			{
				this.addSlotToContainer(new SlotItemHandler(tedf.inventory, j + i * 6 + 9, 44 - i * 18, 26 + j * 18));
			}
		}

		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 17 + j * 18, 162 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 17 + i * 18, 220));
		}
	}

	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return InventoryUtil.transferStack(this.inventorySlots, index, this.transferStrategy, player);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return launcher.isUseableByPlayer(player);
	}
}
