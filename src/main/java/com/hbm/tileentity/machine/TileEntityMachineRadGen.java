package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.blocks.BlockDummyable;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.container.ContainerMachineRadGen;
import com.hbm.inventory.gui.GUIMachineRadGen;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemWasteLong;
import com.hbm.items.special.ItemWasteShort;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.Tuple;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

@AutoRegister
public class TileEntityMachineRadGen extends TileEntityMachineBase implements ITickable, IEnergyProviderMK2, IGUIProvider {

	public int[] progress = new int[12];
	public int[] maxProgress = new int[12];
	public int[] production = new int[12];
	public ItemStack[] processing = new ItemStack[12];
	protected int output;

	public long power;
	public static final long maxPower = 1000000;

	public boolean isOn = false;
	private static boolean converted = false;

	public TileEntityMachineRadGen() {
		super(24);
		converted = true;
	}

	@Override
	public String getDefaultName() {
		return "container.radGen";
	}

	@Override
	public void update() {
		if(!converted) {
			resizeInventory(24);
			converted = true;
		}

		if(!world.isRemote) {

			this.output = 0;

			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
			this.tryProvide(world, this.pos.getX() - dir.offsetX * 4, this.pos.getY(), this.pos.getZ() - dir.offsetZ * 4, dir.getOpposite());

			//check if reload necessary for any queues
			for(int i = 0; i < 12; i++) {

				if(processing[i] == null && !inventory.getStackInSlot(i).isEmpty() && getDurationFromItem(inventory.getStackInSlot(i)) > 0 &&
						(getOutputFromItem(inventory.getStackInSlot(i)) == null || inventory.getStackInSlot(i + 12).isEmpty() ||
								(getOutputFromItem(inventory.getStackInSlot(i)).getItem() == inventory.getStackInSlot(i + 12).getItem() && getOutputFromItem(inventory.getStackInSlot(i)).getItemDamage() == inventory.getStackInSlot(i + 12).getItemDamage() &&
										getOutputFromItem(inventory.getStackInSlot(i)).getCount() + inventory.getStackInSlot(i + 12).getCount() <= inventory.getStackInSlot(i + 12).getMaxStackSize()))) {

					progress[i] = 0;
					maxProgress[i] = this.getDurationFromItem(inventory.getStackInSlot(i));
					production[i] = this.getPowerFromItem(inventory.getStackInSlot(i));
					processing[i] = new ItemStack(inventory.getStackInSlot(i).getItem(), 1, inventory.getStackInSlot(i).getItemDamage());
					this.inventory.getStackInSlot(i).shrink(1);
					this.markDirty();
				}
			}

			this.isOn = false;

			for(int i = 0; i < 12; i++) {

				if(processing[i] != null) {

					this.isOn = true;
					this.power += production[i];
					this.output += production[i];
					progress[i]++;

					if(progress[i] >= maxProgress[i]) {
						progress[i] = 0;
						ItemStack out = getOutputFromItem(processing[i]);

						if(out != null) {

							if(inventory.getStackInSlot(i + 12).isEmpty()) {
								inventory.setStackInSlot(i + 12, out);
							} else {
								inventory.getStackInSlot(i + 12).grow(out.getCount());
							}
						}

						processing[i] = null;
						this.markDirty();
					}
				}
			}

			if(this.power > maxPower)
				this.power = maxPower;

			this.networkPackNT(50);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		BufferUtil.writeIntArray(buf, this.progress);
		BufferUtil.writeIntArray(buf, this.maxProgress);
		BufferUtil.writeIntArray(buf, this.production);
		buf.writeLong(this.power);
		buf.writeBoolean(this.isOn);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.progress = BufferUtil.readIntArray(buf);
		this.maxProgress = BufferUtil.readIntArray(buf);
		this.production = BufferUtil.readIntArray(buf);
		this.power = buf.readLong();
		this.isOn = buf.readBoolean();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.progress = nbt.getIntArray("progress");

		if(progress.length != 12) {
			progress = new int[12];
			return;
		}

		this.maxProgress = nbt.getIntArray("maxProgress");
		this.production = nbt.getIntArray("production");
		this.power = nbt.getLong("power");
		this.isOn = nbt.getBoolean("isOn");

		NBTTagList list = nbt.getTagList("progressing", 10);
		for(int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < processing.length) {
				processing[b0] = new ItemStack(nbt1);
			}
		}

		this.power = nbt.getLong("power");
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setIntArray("progress", this.progress);
		nbt.setIntArray("maxProgress", this.maxProgress);
		nbt.setIntArray("production", this.production);
		nbt.setLong("power", this.power);
		nbt.setBoolean("isOn", this.isOn);

		NBTTagList list = new NBTTagList();
		for(int i = 0; i < processing.length; i++) {
			if(processing[i] != null) {
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("slot", (byte) i);
				processing[i].writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
		}
		nbt.setTag("progressing", list);

		nbt.setLong("power", this.power);
		return super.writeToNBT(nbt);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {

		if(i >= 12 || getDurationFromItem(stack) <= 0)
			return false;

		if(inventory.getStackInSlot(i).isEmpty())
			return true;

		int size = inventory.getStackInSlot(i).getCount();

		for(int j = 0; j < 12; j++) {
			if(inventory.getStackInSlot(j).isEmpty())
				return false;

			if(inventory.getStackInSlot(j).getItem() == stack.getItem() && inventory.getStackInSlot(j).getItemDamage() == stack.getItemDamage() && inventory.getStackInSlot(j).getCount() < size)
				return false;
		}

		return true;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
				12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return i >= 12;
	}

	public static final HashMap<RecipesCommon.ComparableStack, Tuple.Triplet<Integer, Integer, ItemStack>> fuels = new HashMap<>();

	static {

		for(int i = 0; i < ItemWasteShort.WasteClass.VALUES.length; i++) {
			fuels.put(	new RecipesCommon.ComparableStack(ModItems.nuclear_waste_short, 1, i), new Tuple.Triplet<>(1500, 30 * 60 * 20, new ItemStack(ModItems.nuclear_waste_short_depleted, 1, i)));
			fuels.put(	new RecipesCommon.ComparableStack(ModItems.nuclear_waste_short_tiny, 1, i), new Tuple.Triplet<>(150, 3 * 60 * 20, new ItemStack(ModItems.nuclear_waste_short_depleted_tiny, 1, i)));
		}
		for(int i = 0; i < ItemWasteLong.WasteClass.VALUES.length; i++) {
			fuels.put(	new RecipesCommon.ComparableStack(ModItems.nuclear_waste_long, 1, i), new Tuple.Triplet<>(500, 2 * 60 * 60 * 20, new ItemStack(ModItems.nuclear_waste_long_depleted, 1, i)));
			fuels.put(	new RecipesCommon.ComparableStack(ModItems.nuclear_waste_long_tiny, 1, i), new Tuple.Triplet<>(50, 12 * 60 * 20, new ItemStack(ModItems.nuclear_waste_long_depleted_tiny, 1, i)));
		}

		fuels.put(		new RecipesCommon.ComparableStack(ModItems.scrap_nuclear), new Tuple.Triplet<>(50, 5 * 60 * 20, null));
		fuels.put(		new RecipesCommon.ComparableStack(ModItems.gem_rad), new Tuple.Triplet<>(25_000, 30 * 60 * 20, new ItemStack(Items.DIAMOND)));
	}

	private Tuple.Triplet<Integer, Integer, ItemStack> grabResult(ItemStack stack) {
		return fuels.get(new RecipesCommon.ComparableStack(stack).makeSingular());
	}

	private int getPowerFromItem(ItemStack stack) {
		Tuple.Triplet<Integer, Integer, ItemStack> result = grabResult(stack);
		if(result == null)
			return 0;
		return result.getX();
	}

	private int getDurationFromItem(ItemStack stack) {
		Tuple.Triplet<Integer, Integer, ItemStack> result = grabResult(stack);
		if(result == null)
			return 0;
		return result.getY();
	}

	private ItemStack getOutputFromItem(ItemStack stack) {
		Tuple.Triplet<Integer, Integer, ItemStack> result = grabResult(stack);
		if(result == null)
			return null;
		if(result.getZ() == null)
			return null;
		return result.getZ().copy();
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public void setPower(long i) {
		this.power = i;
	}

	@Override
	public @NotNull AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineRadGen(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineRadGen(player.inventory, this);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(
					new NTMEnergyCapabilityWrapper(this)
			);
		}
		return super.getCapability(capability, facing);
	}
}
