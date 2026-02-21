package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMachinePress;
import com.hbm.inventory.gui.GUIMachinePress;
import com.hbm.inventory.recipes.PressRecipes;
import com.hbm.items.machine.ItemStamp;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoRegister
public class TileEntityMachinePress extends TileEntityMachineBase implements ITickable, IGUIProvider {

	public int speed = 0;
	public static final int maxSpeed = 400;
	public static final int progressAtMax = 25;
	public int burnTime = 0;

	public int progress;
	public final static int maxProgress = 200;
	private boolean isRetracting = false;
	private int delay;

	@Nullable
	@SideOnly(Side.CLIENT)
	public ItemStack syncStack;

	public TileEntityMachinePress() {
		super(4);
	}

	@Override
	public String getDefaultName() {
		return "container.press";
	}

	@Override
	public void update() {
		if (!world.isRemote) {
			boolean preheated = false;

			for (EnumFacing dir : EnumFacing.VALUES) {
				if (world.getBlockState(pos.offset(dir)).getBlock() == ModBlocks.press_preheater) {
					preheated = true;
					break;
				}
			}

			boolean canProcess = this.canProcess();
			if ((canProcess || this.isRetracting) && this.burnTime >= 200) {
				this.speed += preheated ? 4 : 1;
				if (this.speed > maxSpeed) {
					this.speed = maxSpeed;
				}
			} else {
				this.speed -= 1;
				if (this.speed < 0) {
					this.speed = 0;
				}
			}
			if (delay <= 0) {
				int stampSpeed = speed * progressAtMax / maxSpeed;

				if (this.isRetracting) {
					this.progress -= stampSpeed;

					if (this.progress <= 0) {
						this.progress = 0;
						this.isRetracting = false;
						this.delay = 5;
					}
				} else if (canProcess) {
					this.progress += stampSpeed;

					if (this.progress >= maxProgress) {
						this.progress = maxProgress;
						this.world.playSound(null, this.pos, HBMSoundHandler.pressOperate, SoundCategory.BLOCKS, 1.5F, 1.0F);
						ItemStack output = PressRecipes.getOutput(inventory.getStackInSlot(2), inventory.getStackInSlot(1));
						if (inventory.getStackInSlot(3).isEmpty()) {
							inventory.setStackInSlot(3, output.copy());
						} else {
							inventory.getStackInSlot(3).grow(output.getCount());
						}
						inventory.getStackInSlot(2).shrink(1);
						ItemStack stamp = inventory.getStackInSlot(1);
						if (stamp.isItemStackDamageable()) {
							stamp.setItemDamage(stamp.getItemDamage() + 1);
							if (stamp.getItemDamage() >= stamp.getMaxDamage()) {
								inventory.setStackInSlot(1, ItemStack.EMPTY);
							}
						}

						this.isRetracting = true;
						this.delay = 5;
						if (this.burnTime >= 200) {
							this.burnTime -= 200;
						}
						this.markDirty();
					}
				}
			} else {
				delay--;
			}
			if (!canProcess && !this.isRetracting && this.progress > 0) {
				this.isRetracting = true;
			}
			if (!inventory.getStackInSlot(0).isEmpty() && burnTime < 200 && TileEntityFurnace.getItemBurnTime(inventory.getStackInSlot(0)) > 0) {
				ItemStack fuel = inventory.getStackInSlot(0);
				burnTime += TileEntityFurnace.getItemBurnTime(fuel);

				ItemStack container = fuel.getItem().getContainerItem(fuel);
				fuel.shrink(1);

				if (fuel.isEmpty()) {
					inventory.setStackInSlot(0, container);
				}
				this.markDirty();
			}

			this.networkPackNT(50);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.speed);
		buf.writeInt(this.burnTime);
		buf.writeInt(this.progress);
		ByteBufUtils.writeItemStack(buf, inventory.getStackInSlot(2));
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.speed = buf.readInt();
		this.burnTime = buf.readInt();
		this.progress = buf.readInt();
		this.syncStack = ByteBufUtils.readItemStack(buf);
	}

	public boolean canProcess() {
		if (burnTime < 200) return false;
		ItemStack stampSlot = inventory.getStackInSlot(1);
		ItemStack inputSlot = inventory.getStackInSlot(2);
		if (stampSlot.isEmpty() || inputSlot.isEmpty()) return false;

		ItemStack output = PressRecipes.getOutput(inputSlot, stampSlot);

		if (output.isEmpty()) return false;

		ItemStack outputSlot = inventory.getStackInSlot(3);
		if (outputSlot.isEmpty()) return true;
		if (!outputSlot.isItemEqual(output)) return false;
		return outputSlot.getCount() + output.getCount() <= outputSlot.getMaxStackSize();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		progress = nbt.getInteger("progress");
		burnTime = nbt.getInteger("burnTime");
		speed = nbt.getInteger("speed");
		isRetracting = nbt.getBoolean("isRetracting");
		delay = nbt.getInteger("delay");
		if (nbt.hasKey("inventory"))
			inventory.deserializeNBT(nbt.getCompoundTag("inventory"));
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("progress", progress);
		nbt.setInteger("burnTime", burnTime);
		nbt.setInteger("speed", speed);
		nbt.setBoolean("isRetracting", isRetracting);
		nbt.setInteger("delay", delay);
		nbt.setTag("inventory", inventory.serializeNBT());
		return nbt;
	}

	@Override
	public @NotNull AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos, pos.add(1, 3, 1));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	public boolean isUsableByPlayer(EntityPlayer player) {
		if (this.world.getTileEntity(this.pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64;
		}
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		return e == EnumFacing.DOWN ? new int[]{3} : new int[]{0, 1, 2};
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
		return slot == 3;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return switch (i){
			case 0 -> TileEntityFurnace.getItemBurnTime(stack) > 0;
			case 1 -> stack.getItem() instanceof ItemStamp;
			case 2 -> !(stack.getItem() instanceof ItemStamp) && TileEntityFurnace.getItemBurnTime(stack) <= 0;
			default -> false;
		};
	}

	public int getProgressScaled(int i) {
		return (progress * i) / maxProgress;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachinePress(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachinePress(player.inventory, this);
	}
}