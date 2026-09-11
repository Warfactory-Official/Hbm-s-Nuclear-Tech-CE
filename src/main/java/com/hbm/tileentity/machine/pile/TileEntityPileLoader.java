package com.hbm.tileentity.machine.pile;

import com.hbm.api.redstoneoverradio.IRORValueProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockMeta;
import com.hbm.blocks.machine.pile.BlockPile;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemPileRodMK2;
import com.hbm.items.machine.ItemPileRodMK2.EnumPileRod;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.machine.pile.TileEntityPileCore.PileChannel;
import com.hbm.util.Compat;
import com.hbm.util.EnumUtil;

import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityPileLoader extends TileEntityPileDeviceBase implements ISidedInventory, IRORValueProvider, CompatHandler.OCComponent {

	public double syncLevel;
	public double level;
	public double lastLevel;

	public int turnProgress;

	public static final double SPEED = 1D / 7D;

	public boolean loading = false;
	public int delay = 0;
	public ItemStack syncStack = ItemStack.EMPTY;
	public ItemStack stack = ItemStack.EMPTY;
	public boolean wasRedstone;

	public ItemStack channelStack = ItemStack.EMPTY;
	public double channelDepletion;
	public double channelTemp;

	@Override
	public void update() {

		if(!world.isRemote) {

			ForgeDirection dir = getOrientation();
			PileChannel fuelChan = null;
			this.channelStack = ItemStack.EMPTY;
			this.channelDepletion = 0D;
			this.channelTemp = 0D;

			BlockPos port = pos.add(-dir.offsetX, 0, -dir.offsetZ);
			IBlockState portState = world.getBlockState(port);

			if(portState.getBlock() == ModBlocks.pile_block && portState.getValue(BlockMeta.META) == BlockPile.META_FUEL_IN) {
				TileEntity tile = Compat.getTileStandard(world, port.getX(), port.getY(), port.getZ());

				if(tile instanceof TileEntityPileBaseMK2) {
					TileEntityPileBaseMK2 pile = (TileEntityPileBaseMK2) tile;
					TileEntityPileCore core = pile.getCore();

					if(core != null) {
						fuelChan = core.getFuelChannel(port.getX(), port.getY(), port.getZ());

						if(fuelChan != null) {
							this.chanNum = core.getFuelChannelNum(fuelChan);
							this.channelStack = fuelChan.rods[fuelChan.rods.length - 1];
							this.channelDepletion = ItemPileRodMK2.getDepletionPercent(channelStack);
							this.channelTemp = fuelChan.heat;
						}
					}
				}
			}

			boolean redstone = world.getRedstonePower(pos.add(dir.offsetX, 0, dir.offsetZ), dir.getOpposite().toEnumFacing()) > 0;
			if(redstone && !wasRedstone && this.delay <= 0 && this.level <= 0) this.loading = true;
			this.wasRedstone = redstone;

			if(this.delay > 0) {
				this.delay--;
			} else {

				if(loading) {

					if(this.level == 0) world.playSound(null, pos, HBMSoundHandler.boltOpen, SoundCategory.BLOCKS, this.getVolume(1F), 1F);

					this.level += SPEED;
					if(this.level >= 1D) {
						this.level = 1D;
						this.loading = false;
						this.delay = 5;
					}
				} else {

					if(this.level == 1) {
						world.playSound(null, pos, HBMSoundHandler.boltOpen, SoundCategory.BLOCKS, this.getVolume(1F), 0.75F);
						if(fuelChan != null) {
							fuelChan.loadItem(stack);
							this.setInventorySlotContents(0, ItemStack.EMPTY);
						}
					}

					if(this.level > 0D) {
						this.level -= SPEED;
						if(this.level < 0D) this.level = 0D;
					}
				}
			}

			this.networkPackNT(35);

		} else {

			this.lastLevel = this.level;

			if(this.turnProgress > 0) {
				this.level = this.level + ((this.syncLevel - this.level) / (double) this.turnProgress);
				--this.turnProgress;
			} else {
				this.level = this.syncLevel;
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeDouble(this.level);

		if(!this.stack.isEmpty()) {
			buf.writeInt(Item.getIdFromItem(this.stack.getItem()));
			buf.writeShort(this.stack.getItemDamage());
		} else {
			buf.writeInt(-1);
		}

		if(!this.channelStack.isEmpty()) {
			buf.writeInt(Item.getIdFromItem(this.channelStack.getItem()));
			buf.writeShort(this.channelStack.getItemDamage());
		} else {
			buf.writeInt(-1);
		}

		buf.writeDouble(this.channelDepletion);
		buf.writeDouble(this.channelTemp);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		double lastSync = this.syncLevel;
		this.syncLevel = buf.readDouble();

		int itemId = buf.readInt();
		if(itemId != -1) this.syncStack = new ItemStack(Item.getItemById(itemId), 1, buf.readShort());
		else this.syncStack = ItemStack.EMPTY;

		int chanId = buf.readInt();
		if(chanId != -1) this.channelStack = new ItemStack(Item.getItemById(chanId), 1, buf.readShort());
		else this.channelStack = ItemStack.EMPTY;

		this.channelDepletion = buf.readDouble();
		this.channelTemp = buf.readDouble();

		if(this.syncLevel != lastSync) this.turnProgress = 2;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.loading = nbt.getBoolean("loading");
		this.level = nbt.getDouble("level");
		this.delay = nbt.getInteger("delay");
		this.wasRedstone = nbt.getBoolean("redstone");

		if(nbt.hasKey("stack")) {
			this.stack = new ItemStack(nbt.getCompoundTag("stack"));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setBoolean("loading", loading);
		nbt.setDouble("level", level);
		nbt.setInteger("delay", delay);
		nbt.setBoolean("wasRedstone", wasRedstone);

		if(!this.stack.isEmpty()) {
			NBTTagCompound stackTag = new NBTTagCompound();
			this.stack.writeToNBT(stackTag);
			nbt.setTag("stack", stackTag);
		}
		return nbt;
	}

	public static boolean isItemLoadable(ItemStack stack) {
		return stack.getItem() == ModItems.pile_rod;
	}

	@NotNull
	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		if(amount == 1 && !this.stack.isEmpty()) {
			ItemStack ret = stack.copy();
			this.setInventorySlotContents(0, ItemStack.EMPTY);
			return ret;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setInventorySlotContents(int slot, @NotNull ItemStack stack) {
		this.stack = stack;
		this.markDirty();
	}

	@Override public int getSizeInventory() { return 1; }
	@Override public boolean isEmpty() { return this.stack.isEmpty(); }
	@NotNull @Override public ItemStack getStackInSlot(int slot) { return stack; }
	@NotNull @Override public ItemStack removeStackFromSlot(int slot) { return ItemStack.EMPTY; }
	@NotNull @Override public String getName() { return "NULL"; }
	@Override public boolean hasCustomName() { return false; }
	@Override public int getInventoryStackLimit() { return 1; }

	@Override public boolean isUsableByPlayer(@NotNull EntityPlayer player) { return false; }
	@Override public void openInventory(@NotNull EntityPlayer player) { }
	@Override public void closeInventory(@NotNull EntityPlayer player) { }

	@Override public int getField(int id) { return 0; }
	@Override public void setField(int id, int value) { }
	@Override public int getFieldCount() { return 0; }
	@Override public void clear() { this.stack = ItemStack.EMPTY; }

	@NotNull @Override public int[] getSlotsForFace(@NotNull EnumFacing side) { return new int[] {0}; }
	@Override public boolean isItemValidForSlot(int slot, @NotNull ItemStack stack) { return this.stack.isEmpty() && !this.loading && this.level <= 0D && isItemLoadable(stack); }
	@Override public boolean canInsertItem(int slot, @NotNull ItemStack stack, @NotNull EnumFacing side) { return isItemValidForSlot(slot, stack); }
	@Override public boolean canExtractItem(int slot, @NotNull ItemStack stack, @NotNull EnumFacing side) { return false; }

	@Override
	public String[] getFunctionInfo() {
		return new String[] {
				PREFIX_VALUE + "meta",
				PREFIX_VALUE + "depletion",
				PREFIX_VALUE + "deppercent",
				PREFIX_VALUE + "lifetime",
				PREFIX_VALUE + "temp",
		};
	}

	@Override
	public String provideRORValue(String name) {

		if(name.equals(PREFIX_VALUE + "meta")) {
			return this.channelStack.isEmpty() ? "-1" : this.channelStack.getItemDamage() + "";
		}
		if(name.equals(PREFIX_VALUE + "deppercent")) {
			return "" + (int) Math.round(this.channelDepletion);
		}
		if(name.equals(PREFIX_VALUE + "depletion")) {
			if(this.channelStack.isEmpty()) return "0";
			return "" + (int) Math.round(ItemPileRodMK2.getDepletionPercent(this.channelStack));
		}
		if(name.equals(PREFIX_VALUE + "lifetime")) {
			if(this.channelStack.isEmpty()) return "0";
			EnumPileRod rod = EnumUtil.grabEnumSafely(EnumPileRod.VALUES, this.channelStack.getItemDamage());
			return "" + (int) Math.round(rod.life);
		}
		if(name.equals(PREFIX_VALUE + "temp")) {
			return "" + (int) Math.round(this.channelTemp);
		}

		return null;
	}

	@Override
	@Optional.Method(modid = "opencomputers")
	public String getComponentName() {
		return "ntm_pile_loader";
	}

	@Callback(direct = true, doc = "function():number - Returns channel temperature")
	@Optional.Method(modid = "opencomputers")
	public Object[] getTemp(Context context, Arguments args) {
		return new Object[] {this.channelTemp};
	}

	@Callback(direct = true, doc = "function():number - Returns fuel depletion")
	@Optional.Method(modid = "opencomputers")
	public Object[] getDepletion(Context context, Arguments args) {
		if(this.channelStack.isEmpty()) return new Object[] {0};
		return new Object[] {this.channelDepletion};
	}

	@Callback(direct = true, doc = "function():number - Returns fuel lifetime")
	@Optional.Method(modid = "opencomputers")
	public Object[] getLifetime(Context context, Arguments args) {
		if(this.channelStack.isEmpty()) return new Object[] {0};
		EnumPileRod rod = EnumUtil.grabEnumSafely(EnumPileRod.VALUES, this.channelStack.getItemDamage());
		return new Object[] {rod.life};
	}

	@Callback(direct = true, doc = "function():number - Returns fuel type")
	@Optional.Method(modid = "opencomputers")
	public Object[] getType(Context context, Arguments args) {
		if(this.channelStack.isEmpty()) return new Object[] {-1};
		return new Object[] {this.channelStack.getItemDamage()};
	}

	@Callback(direct = true, doc = "function():number - Returns fuel type that will be loaded")
	@Optional.Method(modid = "opencomputers")
	public Object[] getLoadingType(Context context, Arguments args) {
		if(this.stack.isEmpty()) return new Object[] {-1};
		return new Object[] {this.stack.getItemDamage()};
	}

	@Callback(direct = true, doc = "function():boolean - Fuel loading state")
	@Optional.Method(modid = "opencomputers")
	public Object[] isLoading(Context context, Arguments args) {
		return new Object[] {loading};
	}

	@Callback(direct = true, limit = 4, doc = "function() - Load fuel")
	@Optional.Method(modid = "opencomputers")
	public Object[] load(Context context, Arguments args) {
		if(this.delay <= 0 && this.level <= 0) this.loading = true;
		return new Object[] {};
	}
}
