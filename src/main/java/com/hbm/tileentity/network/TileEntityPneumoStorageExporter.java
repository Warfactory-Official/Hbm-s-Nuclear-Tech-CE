package com.hbm.tileentity.network;

import com.hbm.api.ntl.StackCache;
import com.hbm.api.ntl.StackCache.CacheSlot;
import com.hbm.api.redstoneoverradio.IRORInteractive;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerPneumoStorageExporter;
import com.hbm.inventory.gui.GUIPneumoStorageExporter;
import com.hbm.lib.ForgeDirection;
import com.hbm.util.BobMathUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister(name = "tileentity_pneumatic_storage_exporter")
public class TileEntityPneumoStorageExporter extends TileEntityPneumaticMachineBase implements IRORInteractive, IControlReceiver {

	/** If requests should be pulled repeatedly every tick */
	public boolean continuousRequest = false;
	/** If ROR configuration has taken place, ignore manually defined filters entirely */
	public boolean rorConfiguredMode = false;
	/** What strategy to use for handling request filters */
	public int requestMode = 0;
	/** Item ID and meta pairs with amount for RoR controlled filters */
	public short[][] rorFilters = new short[9][3];
	/** Delay for non-forced (i.e. continuous request) grabs, if not successful */
	public int[] slotDelay = new int[9];
	public static final int SLOT_DELAY = 10;

	/** Each slot individually tries to pull as much as it can of the configured item */
	public static final int MODE_AS_MUCH_AS_POSSIBLE = 0;
	/** Each slot individually tries to pull the exact quantity configured */
	public static final int MODE_FULL_STACK = 1;
	/** All request slots try to pull the desired quantities simultaneously */
	public static final int MODE_FULL_REQUEST = 2;

	public boolean lastRedstone = false;

	public int[] SLOT_ACCESS = new int[] {9, 10, 11, 12, 13, 14, 15, 16, 17};

	public TileEntityPneumoStorageExporter() {
		super(18);
	}

	@Override
	public String getDefaultName() {
		return "container.pneumoStorageExporter";
	}

	@Override public boolean canExtractItem(int slot, ItemStack stack, int amount) { return slot >= 9; }
	@Override public int[] getAccessibleSlotsFromSide(EnumFacing side) { return SLOT_ACCESS; }
	@Override public boolean canConnectPneumatic(ForgeDirection dir) { return true; }

	@Override
	public void update() {
		super.update();

		if(!world.isRemote) {

			for(int i = 0; i < 9; i++) {
				if(slotDelay[i] > 0) slotDelay[i]--;
			}

			boolean redstone = world.isBlockPowered(pos);

			if(continuousRequest) {
				this.doRequest(false);
			} else {
				if(redstone && !lastRedstone) this.doRequest(true);
			}

			this.lastRedstone = redstone;

			this.networkPackNT(15);
		}
	}

	public void doRequest(boolean force) {

		if(this.requestMode != MODE_FULL_REQUEST) {
			for(int i = 0; i < 9; i++) if(!requestSlot(i, force)) this.slotDelay[i] = SLOT_DELAY;
			return;
		}

		if(this.cache == null || this.cache.hasExpired) return;

		if(!force) for(int i = 0; i < 9; i++) {
			if(this.getFilter(i) != null && slotDelay[i] > 0) return;
		}

		// check filter demands are met and space is free before pulling anything
		for(int i = 0; i < 9; i++) {
			short[] filter = this.getFilter(i);
			if(filter == null) continue;

			int itemId = filter[0];
			Item item = Item.getItemById(itemId);
			int meta = filter[1];
			int requestSize = filter[2];

			int existingSize = 0;
			ItemStack existingStack = inventory.getStackInSlot(i + 9);

			if(!existingStack.isEmpty()) {
				if(existingStack.getItem() == item && existingStack.getItemDamage() == meta && !existingStack.hasTagCompound()) {
					existingSize = existingStack.getCount();
				} else {
					this.slotDelay[i] = SLOT_DELAY;
					return;
				}
			}

			ItemStack newStack = new ItemStack(item, 1, meta);
			int capacityLeft = newStack.getMaxStackSize() - existingSize;

			if(capacityLeft < requestSize || getAvailability(itemId, meta) < requestSize) {
				this.slotDelay[i] = SLOT_DELAY;
				return;
			}
		}

		for(int i = 0; i < 9; i++) {
			short[] filter = this.getFilter(i);
			if(filter == null) continue;

			int itemId = filter[0];
			Item item = Item.getItemById(itemId);
			int meta = filter[1];
			int requestSize = filter[2];

			ItemStack existingStack = inventory.getStackInSlot(i + 9);
			int existingSize = existingStack.isEmpty() ? 0 : existingStack.getCount();

			ItemStack newStack = new ItemStack(item, 1, meta);

			long hash = StackCache.getStackIdentity(item, meta, null);
			if(hash == StackCache.getNullIdentity()) continue;

			CacheSlot cacheSlot = this.cache.cacheSlots.get(hash);
			if(cacheSlot == null) continue;

			newStack.setCount(existingSize + (int) this.cache.consumeItemsAndReturnQuantity(newStack, requestSize));
			inventory.setStackInSlot(i + 9, newStack);
		}

		this.markDirty();
	}

	/** Returns false if the slot delay should be reset (unsuccessful) or true if not */
	public boolean requestSlot(int slot, boolean force) {

		if(!force && slotDelay[slot] > 0) return true;
		if(this.cache == null || this.cache.hasExpired) return false;

		short[] filter = this.getFilter(slot);
		if(filter == null) return false;

		int itemId = filter[0];
		Item item = Item.getItemById(itemId);
		int meta = filter[1];
		int requestSize = filter[2];

		int existingSize = 0;
		ItemStack existingStack = inventory.getStackInSlot(slot + 9);

		if(!existingStack.isEmpty()) {
			if(existingStack.getItem() == item && existingStack.getItemDamage() == meta && !existingStack.hasTagCompound()) {
				existingSize = existingStack.getCount();
			} else {
				return false;
			}
		}

		ItemStack newStack = new ItemStack(item, 1, meta);
		int capacityLeft = newStack.getMaxStackSize() - existingSize;

		if(capacityLeft < requestSize && this.requestMode != MODE_AS_MUCH_AS_POSSIBLE) return false;

		long hash = StackCache.getStackIdentity(item, meta, null);
		if(hash == StackCache.getNullIdentity()) return false;

		CacheSlot cacheSlot = this.cache.cacheSlots.get(hash);
		if(cacheSlot == null) return false;

		if(cacheSlot.stacksize < requestSize && this.requestMode != MODE_AS_MUCH_AS_POSSIBLE) return false;
		if(cacheSlot.stacksize <= 0) return false;

		int toPull = (int) BobMathUtil.min(requestSize, cacheSlot.stacksize, capacityLeft);

		newStack.setCount(existingSize + (int) this.cache.consumeItemsAndReturnQuantity(newStack, toPull));
		inventory.setStackInSlot(slot + 9, newStack);
		this.markDirty();

		return true;
	}

	/** Returns item id, meta and request size for the given filter, or null if unset */
	public short[] getFilter(int slot) {
		if(rorConfiguredMode) {
			if(Item.getItemById(rorFilters[slot][0]) == null) return null;
			return rorFilters[slot];
		}

		ItemStack stack = inventory.getStackInSlot(slot);
		if(stack.isEmpty()) return null;
		return new short[] {(short) Item.getIdFromItem(stack.getItem()), (short) stack.getItemDamage(), (short) stack.getCount()};
	}

	public long getAvailability(int item, int meta) {
		if(this.cache == null || this.cache.hasExpired) return 0;
		long hash = StackCache.getStackIdentity(Item.getItemById(item), meta, null);
		if(hash == StackCache.getNullIdentity()) return 0;
		CacheSlot slot = this.cache.cacheSlots.get(hash);
		if(slot == null) return 0;
		return slot.stacksize;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(continuousRequest);
		buf.writeBoolean(rorConfiguredMode);
		buf.writeByte((byte) requestMode);
		for(int i = 0; i < 9; i++) {
			buf.writeShort(rorFilters[i][0]);
			buf.writeShort(rorFilters[i][1]);
			buf.writeShort(rorFilters[i][2]);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.continuousRequest = buf.readBoolean();
		this.rorConfiguredMode = buf.readBoolean();
		this.requestMode = buf.readByte();
		for(int i = 0; i < 9; i++) {
			rorFilters[i][0] = buf.readShort();
			rorFilters[i][1] = buf.readShort();
			rorFilters[i][2] = buf.readShort();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.continuousRequest = nbt.getBoolean("continuousRequest");
		this.rorConfiguredMode = nbt.getBoolean("rorConfiguredMode");
		this.requestMode = nbt.getByte("requestMode");
		for(int i = 0; i < 9; i++) {
			rorFilters[i][0] = nbt.getShort("filter_" + i + "_0");
			rorFilters[i][1] = nbt.getShort("filter_" + i + "_1");
			rorFilters[i][2] = nbt.getShort("filter_" + i + "_2");
		}

		this.lastRedstone = nbt.getBoolean("lastRedstone");
		int[] delay = nbt.getIntArray("slotDelay");
		if(delay.length == 9) this.slotDelay = delay;
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		nbt.setBoolean("continuousRequest", continuousRequest);
		nbt.setBoolean("rorConfiguredMode", rorConfiguredMode);
		nbt.setByte("requestMode", (byte) requestMode);

		for(int i = 0; i < 9; i++) {
			nbt.setShort("filter_" + i + "_0", rorFilters[i][0]);
			nbt.setShort("filter_" + i + "_1", rorFilters[i][1]);
			nbt.setShort("filter_" + i + "_2", rorFilters[i][2]);
		}

		nbt.setBoolean("lastRedstone", lastRedstone);
		nbt.setIntArray("slotDelay", slotDelay);

		return super.writeToNBT(nbt);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerPneumoStorageExporter(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIPneumoStorageExporter(player.inventory, this);
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return player.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 16;
	}

	@Override
	public void receiveControl(EntityPlayerMP player, NBTTagCompound data) {
		if(data.hasKey("continuous")) {
			this.continuousRequest = !this.continuousRequest;
		}
		if(data.hasKey("request")) {
			this.requestMode++;
			if(this.requestMode >= 3) this.requestMode = 0;
		}
		if(data.hasKey("ror")) {
			this.rorConfiguredMode = !this.rorConfiguredMode;
		}
		this.markDirty();
	}

	@Override
	public String[] getFunctionInfo() {
		return new String[] {
				PREFIX_FUNCTION + "setfilter" + NAME_SEPARATOR + "slot" + PARAM_SEPARATOR + "itemid" + PARAM_SEPARATOR + "itemmeta" + PARAM_SEPARATOR + "amount",
				PREFIX_FUNCTION + "setcontinuous" + NAME_SEPARATOR + "on/off",
				PREFIX_FUNCTION + "request",
				PREFIX_FUNCTION + "requestslot" + NAME_SEPARATOR + "slot",
				PREFIX_FUNCTION + "checkavailability" + NAME_SEPARATOR + "itemid" + PARAM_SEPARATOR + "itemmeta" + PARAM_SEPARATOR + "returnchannel"
		};
	}

	@Override
	public String runRORFunction(String name, String[] params) {

		if((PREFIX_FUNCTION + "setfilter").equals(name) && params.length == 4) {
			int slot = IRORInteractive.parseInt(params[0], 1, 9) - 1;
			int itemId = IRORInteractive.parseInt(params[1], 0, Short.MAX_VALUE);
			int meta = IRORInteractive.parseInt(params[2], 0, Short.MAX_VALUE);
			int amount = IRORInteractive.parseInt(params[3], 1, 64);

			this.rorFilters[slot][0] = (short) itemId;
			this.rorFilters[slot][1] = (short) meta;
			this.rorFilters[slot][2] = (short) amount;
			this.markDirty();
			return null;
		}

		if((PREFIX_FUNCTION + "setcontinuous").equals(name) && params.length == 1) {
			if("on".equals(params[0])) this.continuousRequest = true;
			if("off".equals(params[0])) this.continuousRequest = false;
			this.markDirty();
			return null;
		}

		if((PREFIX_FUNCTION + "request").equals(name)) {
			this.doRequest(true);
			return null;
		}

		if((PREFIX_FUNCTION + "requestslot").equals(name) && params.length == 1) {
			int slot = IRORInteractive.parseInt(params[0], 1, 9) - 1;
			if(!this.requestSlot(slot, true)) this.slotDelay[slot] = SLOT_DELAY;
			return null;
		}

		if((PREFIX_FUNCTION + "checkavailability").equals(name) && params.length == 3) {
			int itemId = IRORInteractive.parseInt(params[0], 0, Short.MAX_VALUE);
			int meta = IRORInteractive.parseInt(params[1], 0, Short.MAX_VALUE);
			String ret = params[2];
			RTTYSystem.broadcast(world, ret, this.getAvailability(itemId, meta) + "");
			return null;
		}

		return null;
	}
}
