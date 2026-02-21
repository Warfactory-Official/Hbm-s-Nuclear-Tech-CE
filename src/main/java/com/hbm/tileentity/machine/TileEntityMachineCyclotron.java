package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineCyclotron;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineCyclotron;
import com.hbm.inventory.recipes.CyclotronRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.I18nUtil;
import com.hbm.util.SoundUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.List;

@AutoRegister
public class TileEntityMachineCyclotron extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiver, IGUIProvider, IUpgradeInfoProvider, IFluidCopiable {

	public long power;
	public static final long maxPower = 100000000;
	public static int consumption = 1_000_000;

	private byte plugs;

	public int progress;
	public static final int duration = 690;

	public FluidTankNTM[] tanks;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityMachineCyclotron() {
		super(0, true, true);
		this.inventory = new ItemStackHandler(12){
			@Override
			protected void onContentsChanged(int slot) {
				super.onContentsChanged(slot);
				markDirty();
			}
			@Override
			public void setStackInSlot(int slot, ItemStack stack) {
				if(stack != ItemStack.EMPTY && slot >= 14 && slot <= 15 && stack.getItem() instanceof ItemMachineUpgrade)
					SoundUtil.playUpgradePlugSound(world, pos);
				super.setStackInSlot(slot, stack);
			}
		};
		this.tanks = new FluidTankNTM[3];
		this.tanks[0] = new FluidTankNTM(Fluids.WATER, 32000);
		this.tanks[1] = new FluidTankNTM(Fluids.SPENTSTEAM, 32000);
		this.tanks[2] = new FluidTankNTM(Fluids.AMAT, 8000);
	}
	
	@Override
	public String getDefaultName() {
		return "container.cyclotron";
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing side, BlockPos accessor) {
		int x = accessor.getX(), z = accessor.getZ();
		int xCoord = pos.getX(), zCoord = pos.getZ();
		for(int i = 2; i < 6; i++) {
			ForgeDirection dir = ForgeDirection.getOrientation(i);
			ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

			if(x == xCoord + dir.offsetX * 2 + rot.offsetX && z == zCoord + dir.offsetZ * 2 + rot.offsetZ) return new int[] {0, 3, 6, 7, 8};
			if(x == xCoord + dir.offsetX * 2 && z == zCoord + dir.offsetZ * 2) return new int[] {1, 4, 6, 7, 8};
			if(x == xCoord + dir.offsetX * 2 - rot.offsetX && z == zCoord + dir.offsetZ * 2 - rot.offsetZ) return new int[] {2, 5, 6, 7, 8};
		}

		return new int[] {6, 7, 8};
	}
	
	@Override
	public void update() {
		if(!world.isRemote) {

			this.updateConnections();

			this.power = Library.chargeTEFromItems(inventory, 9, power, maxPower);

			upgradeManager.checkSlots(inventory, 10, 11);

			if(canProcess()) {
				progress += getSpeed();
				power -= getConsumption();

				int convert = getCoolantConsumption();
				tanks[0].setFill(tanks[0].getFill() - convert);
				tanks[1].setFill(tanks[1].getFill() + convert);

				if(progress >= duration) {
					process();
					progress = 0;
					this.markDirty();
				}

			} else {
				progress = 0;
			}

			this.sendFluid();
			this.networkPackNT(25);
		}
	}

	private void updateConnections()  {
		for(DirPos pos : getConPos()) {
			this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			this.trySubscribe(tanks[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
		}
	}

	private void sendFluid() {
		for(int i = 1; i < 3; i++) {
			if(tanks[i].getFill() > 0) {
				for(DirPos pos : getConPos()) {
					this.sendFluid(tanks[i], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
				}
			}
		}
	}

	public DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(pos.getX() + 3, pos.getY(), pos.getZ() + 1, Library.POS_X),
				new DirPos(pos.getX() + 3, pos.getY(), pos.getZ() - 1, Library.POS_X),
				new DirPos(pos.getX() - 3, pos.getY(), pos.getZ() + 1, Library.NEG_X),
				new DirPos(pos.getX() - 3, pos.getY(), pos.getZ() - 1, Library.NEG_X),
				new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() + 3, Library.POS_Z),
				new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() + 3, Library.POS_Z),
				new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() - 3, Library.NEG_Z),
				new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() - 3, Library.NEG_Z)
		};
	}

	public boolean canProcess() {

		if(power < getConsumption())
			return false;

		int convert = getCoolantConsumption();

		if(tanks[0].getFill() < convert)
			return false;

		if(tanks[1].getFill() + convert > tanks[1].getMaxFill())
			return false;

		for(int i = 0; i < 3; i++) {

			Object[] res = CyclotronRecipes.getOutput(inventory.getStackInSlot(i + 3), inventory.getStackInSlot(i));

			if(res == null)
				continue;

			ItemStack out = (ItemStack)res[0];

			if(out == null)
				continue;

			if(inventory.getStackInSlot(i + 6).isEmpty())
				return true;

			if(inventory.getStackInSlot(i + 6).getItem() == out.getItem() && inventory.getStackInSlot(i + 6).getItemDamage() == out.getItemDamage() && inventory.getStackInSlot(i + 6).getCount() < out.getMaxStackSize())
				return true;
		}

		return false;
	}

	public void process() {

		for(int i = 0; i < 3; i++) {

			Object[] res = CyclotronRecipes.getOutput(inventory.getStackInSlot(i + 3), inventory.getStackInSlot(i));

			if(res == null)
				continue;

			ItemStack out = (ItemStack)res[0];

			if(out == null)
				continue;
			
			ItemStack item1 = inventory.getStackInSlot(i).copy();
			ItemStack item2 = inventory.getStackInSlot(i + 3).copy();
			ItemStack item3 = inventory.getStackInSlot(i + 6).copy();
			item1.shrink(1);
			item2.shrink(1);
			if(inventory.getStackInSlot(i + 6).isEmpty()) {

				this.inventory.setStackInSlot(i, item1);
				this.inventory.setStackInSlot(i + 3, item2);
				inventory.setStackInSlot(i + 6, out);

				this.tanks[2].setFill(this.tanks[2].getFill() + (Integer)res[1]);

				continue;
			}

			if(inventory.getStackInSlot(i + 6).getItem() == out.getItem() && inventory.getStackInSlot(i + 6).getItemDamage() == out.getItemDamage() && inventory.getStackInSlot(i + 6).getCount() < out.getMaxStackSize()) {

				this.inventory.setStackInSlot(i, item1);
				this.inventory.setStackInSlot(i + 3, item2);
				item3.grow(1);
				inventory.setStackInSlot(i + 6, item3);

				this.tanks[2].setFill(this.tanks[2].getFill() + (Integer)res[1]);
			}
		}

		if(this.tanks[2].getFill() > this.tanks[2].getMaxFill())
			this.tanks[2].setFill(this.tanks[2].getMaxFill());
	}

	public int getSpeed() {
		return upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED) + 1;
	}

	public int getConsumption() {
		int efficiency = upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.POWER);

		return consumption - 100_000 * efficiency;
	}

	public int getCoolantConsumption() {
		int efficiency = upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.EFFECT);
		//half a small tower's worth
		return 500 / (efficiency + 1) * getSpeed();
	}
	
	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}
	
	public int getProgressScaled(int i) {
		return (progress * i) / duration;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		for(int i = 0; i < 3; i++)
			tanks[i].readFromNBT(nbt, "t" + i);

		this.progress = nbt.getInteger("progress");
		this.power = nbt.getLong("power");
		this.plugs = nbt.getByte("plugs");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		for(int i = 0; i < 3; i++)
			tanks[i].writeToNBT(nbt, "t" + i);

		nbt.setInteger("progress", progress);
		nbt.setLong("power", power);
		nbt.setByte("plugs", plugs);

		return super.writeToNBT(nbt);
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(progress);
		buf.writeByte(plugs);

		for(int i = 0; i < 3; i++)
			tanks[i].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		progress = buf.readInt();
		plugs = buf.readByte();

		for(int i = 0; i < 3; i++)
			tanks[i].deserialize(buf);
	}
	
	public void setPlug(int index) {
		this.plugs |= (1 << index);
		this.markDirty();
	}

	public boolean getPlug(int index) {
		return (this.plugs & (1 << index)) > 0;
	}

	public static Item getItemForPlug(int i) {

		switch(i) {
		case 0: return ModItems.powder_balefire;
		case 1: return ModItems.book_of_;
		case 2: return ModItems.diamond_gavel;
		case 3: return ModItems.coin_maskman;
		}

		return null;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos.getX() - 2, pos.getY(), pos.getZ() - 2, pos.getX() + 3, pos.getY() + 4, pos.getZ() + 3);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	public void fillFluidInit(FluidTankNTM tank) {
		sendFluid(tank, world, pos.add( 3, 0,  1), ForgeDirection.WEST);
		sendFluid(tank, world, pos.add( 3, 0, -1), ForgeDirection.WEST);
		sendFluid(tank, world, pos.add(-3, 0,  1), ForgeDirection.EAST);
		sendFluid(tank, world, pos.add(-3, 0, -1), ForgeDirection.EAST);
		sendFluid(tank, world, pos.add( 1, 0,  3), ForgeDirection.NORTH);
		sendFluid(tank, world, pos.add(-1, 0,  3), ForgeDirection.NORTH);
		sendFluid(tank, world, pos.add( 1, 0, -3), ForgeDirection.SOUTH);
		sendFluid(tank, world, pos.add(-1, 0, -3), ForgeDirection.SOUTH);
	}

	@Override
	public void setPower(long i) {
		this.power = i;
	}

	@Override
	public long getPower() {
		return this.power;
	}

	@Override
	public long getMaxPower() {
		return this.maxPower;
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[] { tanks[1], tanks[2] };
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] { tanks[0] };
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return tanks;
	}

	@Override
	public boolean canProvideInfo(ItemMachineUpgrade.UpgradeType type, int level, boolean extendedInfo) {
		return type == ItemMachineUpgrade.UpgradeType.SPEED || type == ItemMachineUpgrade.UpgradeType.POWER || type == ItemMachineUpgrade.UpgradeType.EFFECT;
	}

	@Override
	public void provideInfo(ItemMachineUpgrade.UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_cyclotron));
		if(type == ItemMachineUpgrade.UpgradeType.SPEED) {
			info.add(TextFormatting.GREEN + I18nUtil.resolveKey(this.KEY_DELAY, "-" + (100 - 100 / (level + 1)) + "%"));
			info.add(TextFormatting.RED + I18nUtil.resolveKey(this.KEY_COOLANT_CONSUMPTION, "+" + (level * 100) + "%"));
		}
		if(type == ItemMachineUpgrade.UpgradeType.POWER) {
			info.add(TextFormatting.GREEN + I18nUtil.resolveKey(this.KEY_CONSUMPTION, "-" + (level * 10) + "%"));
		}
		if(type == ItemMachineUpgrade.UpgradeType.EFFECT) {
			info.add(TextFormatting.GREEN + I18nUtil.resolveKey(this.KEY_COOLANT_CONSUMPTION, "-" + (100 - 100 / (level + 1)) + "%"));
		}
	}

	@Override
	public HashMap<ItemMachineUpgrade.UpgradeType, Integer> getValidUpgrades() {
		HashMap<ItemMachineUpgrade.UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(ItemMachineUpgrade.UpgradeType.SPEED, 3);
		upgrades.put(ItemMachineUpgrade.UpgradeType.POWER, 3);
		upgrades.put(ItemMachineUpgrade.UpgradeType.EFFECT, 3);
		return upgrades;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineCyclotron(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineCyclotron(player.inventory, this);
	}
}
