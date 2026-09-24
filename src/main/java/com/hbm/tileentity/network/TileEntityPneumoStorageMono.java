package com.hbm.tileentity.network;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerPneumoStorageMono;
import com.hbm.inventory.gui.GUIPneumoStorageMono;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IControlReceiverFilter;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister(name = "tileentity_pneumatic_storage_mono")
public class TileEntityPneumoStorageMono extends TileEntityPneumaticStorageBase implements IControlReceiverFilter {

	public static final int CAPACITY = 100_000;
	public int[] amounts;

	public TileEntityPneumoStorageMono() {
		super(3);

		this.amounts = new int[this.monitors.length];
	}

	@Override
	public String getDefaultName() {
		return "container.pneumoStorageMono";
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		for(int amount : amounts) buf.writeInt(amount);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		for(int i = 0; i < amounts.length; i++) amounts[i] = buf.readInt();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		int[] read = nbt.getIntArray("amounts");
		if(read.length == this.monitors.length) this.amounts = read;
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setIntArray("amounts", amounts);
		return super.writeToNBT(nbt);
	}

	@Override public boolean canConnectPneumatic(ForgeDirection dir) { return true; }

	@Override public long getAmountAt(int index) { return amounts[index]; }
	@Override public boolean allowTypeSetting() { return false; }

	@Override
	public void receiveControl(EntityPlayerMP player, NBTTagCompound data) {
		super.receiveControl(player, data);

		if(data.hasKey("slot")) {
			setFilterContents(data);
		}
	}

	@Override
	public long useUpItem(int index, long amount) {
		if(amounts[index] <= 0) return amount;
		int toRemove = (int) Math.min(amount, amounts[index]);
		amounts[index] -= toRemove;
		return amount - toRemove;
	}

	@Override
	public long addItem(int index, long amount) {
		int capacity = CAPACITY - amounts[index];
		if(capacity <= 0) return amount;
		int toAdd = (int) Math.min(amount, capacity);
		amounts[index] += toAdd;
		return amount - toAdd;
	}

	@Override public long setupType(int index, ItemStack zeroStack, long amount) { return amount; }

	@Override
	public void nextMode(int i) { }

	@Override
	public int[] getFilterSlots() {
		return new int[] {0, 3};
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerPneumoStorageMono(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIPneumoStorageMono(player.inventory, this);
	}
}
