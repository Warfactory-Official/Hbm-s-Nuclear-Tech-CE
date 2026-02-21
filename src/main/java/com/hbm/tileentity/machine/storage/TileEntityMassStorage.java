package com.hbm.tileentity.machine.storage;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMassStorage;
import com.hbm.inventory.gui.GUIMassStorage;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.toclient.BufPacket;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IControlReceiverFilter;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityMassStorage extends TileEntityCrateBase implements IBufPacketReceiver, ITickable, IControlReceiverFilter, IGUIProvider {
	
	private int stack = 0;
	public boolean output = false;
	private int capacity;
	public int redstone = 0;
	
	@SideOnly(Side.CLIENT) public ItemStack type;
	
	public TileEntityMassStorage() {
		super(3);
	}
	
	public TileEntityMassStorage(int capacity) {
		this();
		this.capacity = capacity;
	}
	@Override
	public String getName() {
		return this.hasCustomName() ? this.customName : "container.massStorage";
	}

	@Override
	public void update() {
		
		if(!world.isRemote) {
			
			int newRed = this.getStockpile() * 15 / this.capacity;
			
			if(newRed != this.redstone) {
				this.redstone = newRed;
				this.markDirty();
			}
			
			if(!inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(0).getItem() == ModItems.fluid_barrel_infinite) {
				this.stack = this.getCapacity();
			}
			
			if(this.getType() == null)
				this.stack = 0;
			
			if(getType() != null && getStockpile() < getCapacity() && !inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(0).isItemEqual(getType()) && ItemStack.areItemStackTagsEqual(inventory.getStackInSlot(0), getType())) {
				
				int remaining = getCapacity() - getStockpile();
				int toRemove = Math.min(remaining, inventory.getStackInSlot(0).getCount());
				this.inventory.getStackInSlot(0).shrink(toRemove);
				this.stack += toRemove;
				this.world.markChunkDirty(pos, this);
			}
			
			if(output && getType() != null) {
				
				if(!inventory.getStackInSlot(2).isEmpty() && !(inventory.getStackInSlot(2).isItemEqual(getType()) && ItemStack.areItemStackTagsEqual(inventory.getStackInSlot(2), getType()))) {
					return;
				}
				
				int amount = Math.min(getStockpile(), getType().getMaxStackSize());
				
				if(amount > 0) {
					if(inventory.getStackInSlot(2).isEmpty()) {
						inventory.setStackInSlot(2, inventory.getStackInSlot(1).copy());
						inventory.getStackInSlot(2).setCount(amount);
						this.stack -= amount;
					} else {
						amount = Math.min(amount, inventory.getStackInSlot(2).getMaxStackSize() - inventory.getStackInSlot(2).getCount());
						inventory.getStackInSlot(2).grow(amount);
						this.stack -= amount;
					}
				}
			}

			networkPackNT(15);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
        buf.writeInt(getStockpile());
        buf.writeBoolean(output);
        ByteBufUtils.writeItemStack(buf, inventory.getStackInSlot(1));
    }

	@Override
	public void deserialize(ByteBuf buf) {
		this.stack = buf.readInt();
		this.output = buf.readBoolean();
		this.type = ByteBufUtils.readItemStack(buf);
	}

	public void networkPackNT(int range) {
		if (!world.isRemote)
			PacketThreading.createAllAroundThreadedPacket(new BufPacket(pos.getX(), pos.getY(), pos.getZ(), this), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), range));
	}
	
	public int getCapacity() {
		return capacity;
	}

	// а, ну и да. соответственно любое взаимодействие с ItemStack-ом больше не должно возвращать null. иначе майнкрафт выёбываться будет
	public ItemStack getType() {
		return inventory.getStackInSlot(1).isEmpty() ? ItemStack.EMPTY : inventory.getStackInSlot(1).copy();
	}
	
	public int getStockpile() {
		return stack;
	}
	
	public void setStockpile(int stack) {
		this.stack = stack;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return Vec3.createVectorHelper(pos.getX() - player.posX, pos.getY() - player.posY, pos.getZ() - player.posZ).length() < 20;
	}

	public void openInventory(EntityPlayer player) {
		player.world.playSound(player.posX, player.posY, player.posZ, HBMSoundHandler.storageOpen, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
	}

	public void closeInventory(EntityPlayer player) {
		player.world.playSound(player.posX, player.posY, player.posZ, HBMSoundHandler.storageClose, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.stack = nbt.getInteger("stack");
		this.output = nbt.getBoolean("output");
		this.capacity = nbt.getInteger("capacity");
		this.redstone = nbt.getByte("redstone");

		if(this.capacity <= 0) {
			this.capacity = 10_000;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("stack", stack);
		nbt.setBoolean("output", output);
		nbt.setInteger("capacity", capacity);
		nbt.setByte("redstone", (byte) redstone);
		return super.writeToNBT(nbt);
	}

	@Override
	public void nextMode(int i) {
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		
		if(data.hasKey("provide") && !inventory.getStackInSlot(1).isEmpty()) {
			
			if(this.getStockpile() == 0) {
				return;
			}
			
			int amount = data.getBoolean("provide") ? inventory.getStackInSlot(1).getMaxStackSize() : 1;
			amount = Math.min(amount, getStockpile());
			
			if(!inventory.getStackInSlot(2).isEmpty() && !(inventory.getStackInSlot(2).isItemEqual(getType()) && ItemStack.areItemStackTagsEqual(inventory.getStackInSlot(2), getType()))) {
				return;
			}
			
			if(inventory.getStackInSlot(2).isEmpty()) {
				inventory.setStackInSlot(2, inventory.getStackInSlot(1).copy());
				inventory.getStackInSlot(2).setCount(amount);
				this.stack -= amount;
			} else {
				amount = Math.min(amount, inventory.getStackInSlot(2).getMaxStackSize() - inventory.getStackInSlot(2).getCount());
				inventory.getStackInSlot(2).grow(amount);
				this.stack -= amount;
			}
		}
		
		if(data.hasKey("toggle")) {
			this.output = !output;
		}
		if(data.hasKey("slot") && this.getStockpile() <= 0){
			setFilterContents(data);
			if(!inventory.getStackInSlot(1).isEmpty()) inventory.getStackInSlot(1).setCount(1);
		}
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStackIn, int amount) {
		return !this.isLocked() && i == 0 && (this.getType() == null || (getType().isItemEqual(itemStackIn) && ItemStack.areItemStackTagsEqual(itemStackIn, getType())));
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int amount) {
		return !this.isLocked() && i == 2;
	}


	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		return new int[] { 0, 2 };
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMassStorage(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMassStorage(player.inventory, this);
	}

	@Override
	public int[] getFilterSlots() {
		return new int[]{1,2};
	}
}
