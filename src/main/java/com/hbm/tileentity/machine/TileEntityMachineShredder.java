package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.Untested;
import com.hbm.inventory.container.ContainerMachineShredder;
import com.hbm.inventory.gui.GUIMachineShredder;
import com.hbm.inventory.recipes.ShredderRecipes;
import com.hbm.items.machine.ItemBlades;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityMachineShredder extends TileEntityMachineBase implements ITickable, IGUIProvider, IEnergyReceiverMK2 {

	public long power;
	public int progress;
	public int soundCycle = 0;
	public static final long maxPower = 10000;
	public static final int processingSpeed = 60;
	// Th3_Sl1ze: firstly it will try inserting blades, then it will try inserting inputs
	private static final int[] slots_other = new int[] {27, 28, 29, 0, 1, 2, 3, 4, 5, 6, 7, 8};
	private static final int[] slots_bottom = new int[] {9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};

	public TileEntityMachineShredder() {
		super(30, false, true);
	}

	@Override
	public String getDefaultName(){
		return "container.machineShredder";
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e){
		int i = e.ordinal();
		return i == 0 ? slots_bottom : slots_other;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemStack){
        if (slot != 27 && slot != 28 && itemStack.getItem() instanceof ItemBlades) return false;
        if (slot != 29 && Library.isDischargeableBattery(itemStack)) return false;
		return this.isItemValidForSlot(slot, itemStack);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack){
		if (i < 9) {
			return true;
		} else if (i == 29 && Library.isBattery(stack)) {
			return true;
		} else {
			return (i == 27 || i == 28) && stack.getItem() instanceof ItemBlades;
		}
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int amount){
		if(slot >= 9 && slot <= 26)
			return true;
		if(slot >= 27 && slot <= 29){
            return itemStack.getItemDamage() == itemStack.getMaxDamage() && itemStack.getMaxDamage() > 0;
		}
		return false;
	}

	public boolean isUseableByPlayer(EntityPlayer player) {
		if(world.getTileEntity(pos) != this)
		{
			return false;
		}else{
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <=64;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.power = compound.getLong("powerTime");
		if(compound.hasKey("inventory"))
			inventory.deserializeNBT(compound.getCompoundTag("inventory"));
		super.readFromNBT(compound);
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setLong("powerTime", power);
		compound.setTag("inventory", inventory.serializeNBT());
		return super.writeToNBT(compound);
	}

	public int getDiFurnaceProgressScaled(int i) {
		return (progress * i) / processingSpeed;
	}

	public boolean hasPower() {
		return power > 0;
	}

	public boolean isProcessing() {
		return this.progress > 0;
	}

	@Override
	public void update() {
		boolean flag1 = false;

		if(!world.isRemote)
		{
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
				this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
			if(hasPower() && canProcess())
			{
				progress++;

				power -= 5;

				if(this.progress == TileEntityMachineShredder.processingSpeed)
				{
					for(int i = 27; i <= 28; i++)
						if(inventory.getStackInSlot(i).getMaxDamage() > 0)
							inventory.getStackInSlot(i).setItemDamage(inventory.getStackInSlot(i).getItemDamage()+1);

					this.progress = 0;
					this.processItem();
					flag1 = true;
				}
				if(soundCycle == 0)
		        	this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_MINECART_RIDING, SoundCategory.BLOCKS, 1.0F, 0.75F);
				soundCycle++;

				if(soundCycle >= 50)
					soundCycle = 0;
			}else{
				progress = 0;
			}

			boolean trigger = true;

			if(hasPower() && canProcess() && this.progress == 0)
			{
				trigger = false;
			}

			if(trigger)
            {
                flag1 = true;
            }

			power = Library.chargeTEFromItems(inventory, 29, power, maxPower);
			networkPackNT(15);
		}

		if(flag1)
		{
			this.markDirty();
		}
	}

	public void processItem() {
		for(int inpSlot = 0; inpSlot < 9; inpSlot++)
		{
			if(!inventory.getStackInSlot(inpSlot).isEmpty() && hasSpace(inventory.getStackInSlot(inpSlot)))
			{
				ItemStack inp = inventory.getStackInSlot(inpSlot);
				ItemStack outp = ShredderRecipes.getShredderResult(inp);
				boolean flag = false;

				for (int outSlot = 9; outSlot < 27; outSlot++)
				{
					if (inventory.getStackInSlot(outSlot).getItem() == outp.getItem() &&
							inventory.getStackInSlot(outSlot).getItemDamage() == outp.getItemDamage() &&
									inventory.getStackInSlot(outSlot).getCount() + outp.getCount() <= outp.getMaxStackSize()) {

						inventory.getStackInSlot(outSlot).grow(outp.getCount());
						inventory.getStackInSlot(inpSlot).shrink(1);
						flag = true;
						break;
					}
				}

				if(!flag)
					for (int outSlot = 9; outSlot < 27; outSlot++)
					{
						if (inventory.getStackInSlot(outSlot).isEmpty()) {
							inventory.setStackInSlot(outSlot, outp.copy());
							inventory.getStackInSlot(inpSlot).shrink(1);
							break;
						}
					}

				if(inventory.getStackInSlot(inpSlot).isEmpty())
					inventory.setStackInSlot(inpSlot, ItemStack.EMPTY);
			}
		}
	}

	@Untested
	public boolean canProcess() {
		if(this.getGearLeft() > 0 && this.getGearLeft() < 3 && this.getGearRight() > 0 && this.getGearRight() < 3) {
			for(int i = 0; i < 9; i++)
			{
				if(!inventory.getStackInSlot(i).isEmpty() && inventory.getStackInSlot(i).getCount() > 0 && hasSpace(inventory.getStackInSlot(i)))
				{
					return true;
				}
			}
		}


		return false;
	}

	public boolean hasSpace(ItemStack stack) {

		ItemStack result = ShredderRecipes.getShredderResult(stack);

		if (result.isEmpty()) {
			return false;
		}

		for (int i = 9; i < 27; i++) {
			if (inventory.getStackInSlot(i).isEmpty()) {
				return true;
			}

			if (inventory.getStackInSlot(i).getItem().equals(result.getItem())
					&& inventory.getStackInSlot(i).getCount() + result.getCount() <= result.getMaxStackSize()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeLong(power);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		power = buf.readLong();
	}

	@Override
	public void setPower(long i) {
		this.power = i;
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	@Override
	public long getPower() {
		return this.power;
	}

	@Override
	public long getMaxPower() {
		return TileEntityMachineShredder.maxPower;
	}

	public int getGearLeft() {

		if(!inventory.getStackInSlot(27).isEmpty() && inventory.getStackInSlot(27).getItem() instanceof ItemBlades)
		{
			if(inventory.getStackInSlot(27).getMaxDamage() == 0)
				return 1;
			if(inventory.getStackInSlot(27).getItemDamage() < inventory.getStackInSlot(27).getMaxDamage()/2)
			{
				return 1;
			} else if(inventory.getStackInSlot(27).getItemDamage() != inventory.getStackInSlot(27).getMaxDamage()) {
				return 2;
			} else {
				return 3;
			}
		}

		return 0;
	}

	public int getGearRight() {

		if(!inventory.getStackInSlot(28).isEmpty() && inventory.getStackInSlot(28).getItem() instanceof ItemBlades)
		{
			if(inventory.getStackInSlot(28).getMaxDamage() == 0)
				return 1;
			if(inventory.getStackInSlot(28).getItemDamage() < inventory.getStackInSlot(28).getMaxDamage()/2)
			{
				return 1;
			} else if(inventory.getStackInSlot(28).getItemDamage() != inventory.getStackInSlot(28).getMaxDamage()) {
				return 2;
			} else {
				return 3;
			}
		}

		return 0;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
    	return new ContainerMachineShredder(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
    	return new GUIMachineShredder(player.inventory, this);
	}
}
