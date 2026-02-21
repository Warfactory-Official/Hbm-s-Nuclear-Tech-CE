package com.hbm.inventory.container;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.gui.GUICoreStabilizer;
import com.hbm.packet.toclient.BufPacket;
import com.hbm.tileentity.machine.TileEntityCoreStabilizer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerCoreStabilizer extends Container {

	private final TileEntityCoreStabilizer nukeBoy;
	private EntityPlayerMP player;
	
	public ContainerCoreStabilizer(EntityPlayer player, TileEntityCoreStabilizer tile) {
		InventoryPlayer invPlayer = player.inventory;
		if(player instanceof EntityPlayerMP)
			this.player = (EntityPlayerMP) player;
		nukeBoy = tile;
		
		this.addSlotToContainer(new SlotItemHandler(tile.inventory, 0, 47, 26));
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 88 + i * 18));
			}
		}
		
		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 146));
		}
	}
	
	@Override
	public void addListener(@NotNull IContainerListener listener) {
		super.addListener(listener);
		listener.sendWindowProperty(this, 0, nukeBoy.watts);
	}
	
	@Override
	public void detectAndSendChanges() {
		BlockPos nukePos = nukeBoy.getPos();
		NBTTagCompound data = new NBTTagCompound();
		data.setLong("power", nukeBoy.power);
		data.setInteger("watts", nukeBoy.watts);
		data.setBoolean("isOn", nukeBoy.isOn);
		PacketThreading.createSendToThreadedPacket(new BufPacket(nukePos.getX(), nukePos.getY(), nukePos.getZ(), nukeBoy), player);
		super.detectAndSendChanges();
	}
	
	@Override
	public void updateProgressBar(int id, int data) {
		if(id == 0){
			if(Minecraft.getMinecraft().currentScreen instanceof GUICoreStabilizer){
				((GUICoreStabilizer)Minecraft.getMinecraft().currentScreen).syncTextField(data);
			}
		}
		super.updateProgressBar(id, data);
	}
	
	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		
		if (slot != null && slot.getHasStack())
		{
			ItemStack stack = slot.getStack();
			result = stack.copy();
			
            if (index == 0) {
				if (!this.mergeItemStack(stack, 1, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(stack, 0, 1, true)) {
				return ItemStack.EMPTY;
			}
			
			if (stack.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}
		}
		
		return result;
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return nukeBoy.isUseableByPlayer(player);
	}
}
