package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineEPress;
import com.hbm.inventory.gui.GUIMachineEPress;
import com.hbm.inventory.recipes.PressRecipes;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.items.machine.ItemStamp;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.I18nUtil;
import com.hbm.util.SoundUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

@AutoRegister
public class TileEntityMachineEPress extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IGUIProvider, IUpgradeInfoProvider {

	public long power = 0;
	public final static long maxPower = 50000;

	public int progress;
	public final static int maxProgress = 200;
	@Nullable
	@SideOnly(Side.CLIENT)
	public ItemStack syncStack;
	private boolean isRetracting = false;
	private int delay;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityMachineEPress() {
		super(0, false, true);

        this.inventory = new ItemStackHandler(5) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                markDirty();
            }
            @Override
            public void setStackInSlot(int slot, @NotNull ItemStack stack) {
                if(Library.isMachineUpgrade(stack) && slot == 4)
                    SoundUtil.playUpgradePlugSound(world, pos);
                super.setStackInSlot(slot, stack);
            }
        };
	}

	@Override
	public String getDefaultName() {
		return "container.epress";
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(progress);
		ByteBufUtils.writeItemStack(buf, inventory.getStackInSlot(2));
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.progress = buf.readInt();
		this.syncStack = ByteBufUtils.readItemStack(buf);
	}

	@Override
	public void update() {
		if (!world.isRemote) {
			this.updateConnections();
			power = Library.chargeTEFromItems(inventory, 0, power, maxPower);

			boolean canProcess = this.canProcess();
			boolean hasPower = power >= 100;

			if ((canProcess || this.isRetracting || this.delay > 0) && hasPower) {
				power -= 100;

				if (delay <= 0) {
					ItemStack[] allSlots = new ItemStack[inventory.getSlots()];
					for(int i = 0; i < inventory.getSlots(); i++) {
						allSlots[i] = inventory.getStackInSlot(i);
					}
					upgradeManager.checkSlots(allSlots, 4, 4);
					int speed = 1 + upgradeManager.getLevel(UpgradeType.SPEED);
					double processSpeed = this.isRetracting ? 20 : 45;
					processSpeed *= (1.0D + (double) speed / 4.0D);

					if (this.isRetracting) {
						this.progress -= (int)Math.round(processSpeed);

						if (this.progress <= 0) {
							this.progress = 0;
							this.isRetracting = false;
							this.delay = 5 - speed + 1;
						}
					} else if (canProcess) {
						this.progress += (int)Math.round(processSpeed);

						if (this.progress >= maxProgress) {
							this.progress = maxProgress;
							this.world.playSound(null, this.pos, HBMSoundHandler.pressOperate, SoundCategory.BLOCKS, 1.5F, 1.0F);
							craftItem();
							this.isRetracting = true;
							this.delay = 5 - speed + 1;
							this.markDirty();
						}
					}
				} else {
					delay--;
				}
			} else if (this.progress > 0) {
				this.isRetracting = true;
			}
			this.networkPackNT(50);
		}
	}

	private boolean canProcess() {
		if (inventory.getStackInSlot(1).isEmpty() || inventory.getStackInSlot(2).isEmpty()) {
			return false;
		}

		ItemStack output = PressRecipes.getOutput(inventory.getStackInSlot(2), inventory.getStackInSlot(1));
		if (output.isEmpty()) {
			return false;
		}

		ItemStack outputSlot = inventory.getStackInSlot(3);
		if (outputSlot.isEmpty()) {
			return true;
		}
		if (!outputSlot.isItemEqual(output)) {
			return false;
		}
		return outputSlot.getCount() + output.getCount() <= outputSlot.getMaxStackSize();
	}

	private void craftItem() {
		ItemStack input = inventory.getStackInSlot(2);
		ItemStack stamp = inventory.getStackInSlot(1);
		ItemStack output = PressRecipes.getOutput(input, stamp);

		if (output.isEmpty()) return;
		inventory.insertItem(3, output.copy(), false);
		input.shrink(1);
		if (stamp.isItemStackDamageable()) {
			stamp.setItemDamage(stamp.getItemDamage() + 1);
			if (stamp.getItemDamage() >= stamp.getMaxDamage()) {
				inventory.setStackInSlot(1, ItemStack.EMPTY);
			}
		}
	}

	@Override
	public boolean isItemValidForSlot(int i, @NotNull ItemStack stack) {
		return switch (i) {
			case 0 -> Library.isBattery(stack);
			case 1 -> stack.getItem() instanceof ItemStamp;
			case 2 ->
					!(stack.getItem() instanceof ItemStamp) && !Library.isBattery(stack) && !(stack.getItem() instanceof ItemMachineUpgrade);
			case 4 -> stack.getItem() instanceof ItemMachineUpgrade;
			default -> false;
		};
	}

	public int[] getAccessibleSlotsFromSide(@NotNull EnumFacing side) {
		if (side == EnumFacing.DOWN) return new int[]{3};
		if (side == EnumFacing.UP) return new int[]{2};
		return new int[]{0, 1, 2};
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
		return slot == 3;
	}
	@NotNull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("progress", progress);
		nbt.setLong("power", power);
		nbt.setBoolean("isRetracting", isRetracting);
		nbt.setInteger("delay", delay);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		progress = nbt.getInteger("progress");
		power = nbt.getLong("power");
		isRetracting = nbt.getBoolean("isRetracting");
		delay = nbt.getInteger("delay");
	}

	@Override
	public void setPower(long i) {
		power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@NotNull
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos, pos.add(1, 3, 1));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_epress));
		if (type == UpgradeType.SPEED) {
			info.add(TextFormatting.GREEN + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_DELAY, "-" + (50 * level / 3) + "%"));
		}
	}

	private void updateConnections() {

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 3);
		return upgrades;
	}

	@Override
	public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Nullable
	@Override
	public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(
					new NTMEnergyCapabilityWrapper(this)
			);
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineEPress(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineEPress(player.inventory, this);
	}

	public long getPowerScaled(int i) {
		return (power * i) / maxPower;
	}

	public int getProgressScaled(int i) {
		return (progress * i) / maxProgress;
	}
}