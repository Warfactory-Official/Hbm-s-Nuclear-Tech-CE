package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemAssemblyTemplate;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineAssembler;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineAssembler extends Container {

	private TileEntityMachineAssembler assembler;

	public ContainerMachineAssembler(InventoryPlayer invPlayer, TileEntityMachineAssembler te) {
		assembler = te;

		//Battery
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 0, 80, 18){
			@Override
			public boolean isItemValid(@NotNull ItemStack stack) {
				return Library.isBattery(stack) || stack.getItem() == ModItems.meteorite_sword_alloyed;
			}
		});
		//Upgrades
		this.addSlotToContainer(new SlotUpgrade(te.inventory, 1, 152, 18));
		this.addSlotToContainer(new SlotUpgrade(te.inventory, 2, 152, 36));
		this.addSlotToContainer(new SlotUpgrade(te.inventory, 3, 152, 54));
		//Schematic
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 4, 80, 54){
			@Override
			public boolean isItemValid(@NotNull ItemStack stack) {
				return stack.getItem() instanceof ItemAssemblyTemplate;
			};
		});
		//Output
		this.addSlotToContainer(SlotFiltered.takeOnly(te.inventory, 5, 134, 90));
		//Input
		this.addSlotToContainer(new SlotAssemblerInput(te.inventory, 6, 8, 18));
		this.addSlotToContainer(new SlotAssemblerInput(te.inventory, 7, 26, 18));
		this.addSlotToContainer(new SlotAssemblerInput(te.inventory, 8, 8, 36));
		this.addSlotToContainer(new SlotAssemblerInput(te.inventory, 9, 26, 36));
		this.addSlotToContainer(new SlotAssemblerInput(te.inventory, 10, 8, 54));
		this.addSlotToContainer(new SlotAssemblerInput(te.inventory, 11, 26, 54));
		this.addSlotToContainer(new SlotAssemblerInput(te.inventory, 12, 8, 72));
		this.addSlotToContainer(new SlotAssemblerInput(te.inventory, 13, 26, 72));
		this.addSlotToContainer(new SlotAssemblerInput(te.inventory, 14, 8, 90));
		this.addSlotToContainer(new SlotAssemblerInput(te.inventory, 15, 26, 90));
		this.addSlotToContainer(new SlotAssemblerInput(te.inventory, 16, 8, 108));
		this.addSlotToContainer(new SlotAssemblerInput(te.inventory, 17, 26, 108));

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
		return InventoryUtil.transferStack(this.inventorySlots, index, 18,
                Library::isBattery, 1,
                Library::isMachineUpgrade, 4,
                s -> s.getItem() instanceof ItemAssemblyTemplate, 5);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return assembler.isUseableByPlayer(player);
	}

	public static class SlotAssemblerInput extends SlotItemHandler {
		public SlotAssemblerInput(IItemHandler itemHandler, int index, int xPosition, int yPosition)
		{
			super(itemHandler, index, xPosition, yPosition);
		}

		@Override
		public boolean isItemValid(ItemStack stack) {
			return stack != null && !(stack.getItem() instanceof ItemAssemblyTemplate);
		}
	}
	
}
