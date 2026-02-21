package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineRtgFurnace;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerRtgFurnace;
import com.hbm.inventory.gui.GUIRtgFurnace;
import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.RTGUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityRtgFurnace extends TileEntityMachineBase implements ITickable, IGUIProvider {

	public int dualCookTime;
	public int heat;
	public static final int processingSpeed = 3000;

	private static final int[] slots_top = new int[] {0};
	private static final int[] slots_bottom = new int[] {4};
	private static final int[] slots_side = new int[] {1, 2, 3};


	public TileEntityRtgFurnace() {
		super(5);
	}

	@Override
	public String getDefaultName() {
		return "container.rtgFurnace";
	}

	public boolean isUseableByPlayer(EntityPlayer player) {
		if(world.getTileEntity(pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
		}
	}

	@Override
	public boolean isLoaded() {
		return super.isLoaded() && RTGUtil.hasHeat(this.inventory);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		dualCookTime = compound.getShort("CookTime");
		if(compound.hasKey("inventory"))
			this.inventory.deserializeNBT(compound.getCompoundTag("inventory"));
		super.readFromNBT(compound);
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setShort("cookTime", (short) dualCookTime);
		compound.setTag("inventory", this.inventory.serializeNBT());
		return super.writeToNBT(compound);
	}
	
	public int getDiFurnaceProgressScaled(int i) {
		return (dualCookTime * i) / processingSpeed;
	}
	
	public boolean canProcess() {
		if(this.inventory.getStackInSlot(0).isEmpty())
		{
			return false;
		}
        ItemStack itemStack = FurnaceRecipes.instance().getSmeltingResult(this.inventory.getStackInSlot(0));
		if(itemStack == null || itemStack.isEmpty())
		{
			return false;
		}
		
		if(this.inventory.getStackInSlot(4).isEmpty())
		{
			return true;
		}
		
		if(!this.inventory.getStackInSlot(4).isItemEqual(itemStack)) {
			return false;
		}
		
		if(this.inventory.getStackInSlot(4).getCount() < this.inventory.getSlotLimit(4) && this.inventory.getStackInSlot(4).getCount() < this.inventory.getStackInSlot(4).getMaxStackSize()) {
			return true;
		}else{
			return this.inventory.getStackInSlot(4).getCount() < itemStack.getMaxStackSize();
		}
	}
	
	private void processItem() {
		if(canProcess()) {
	        ItemStack itemStack = FurnaceRecipes.instance().getSmeltingResult(this.inventory.getStackInSlot(0));
			
			if(this.inventory.getStackInSlot(4).isEmpty())
			{
				this.inventory.setStackInSlot(4, itemStack.copy());
			}else if(this.inventory.getStackInSlot(4).isItemEqual(itemStack)) {
				this.inventory.getStackInSlot(4).grow(itemStack.getCount());
			}
			
			for(int i = 0; i < 1; i++)
			{
				if(this.inventory.getStackInSlot(i).isEmpty())
				{
					this.inventory.setStackInSlot(i, new ItemStack(this.inventory.getStackInSlot(i).getItem()));
				}else{
					this.inventory.getStackInSlot(i).shrink(1);
				}
				if(this.inventory.getStackInSlot(i).isEmpty())
				{
					this.inventory.setStackInSlot(i, ItemStack.EMPTY);
				}
			}
		}
	}
	
	public boolean hasPower() {
		return isLoaded();
	}
	
	public boolean isProcessing() {
		return this.dualCookTime > 0;
	}

	@Override
	public void update() {
		boolean flag1 = false;
		
		if(!world.isRemote)
		{	
			heat = RTGUtil.updateRTGs(this.inventory, new int[] {1, 2, 3});
			if(hasPower() && canProcess())
			{
				dualCookTime+=heat;
				
				if(this.dualCookTime >= TileEntityRtgFurnace.processingSpeed)
				{
					this.dualCookTime = 0;
					this.processItem();
					flag1 = true;
				}
			}else{
				dualCookTime = 0;
			}
			boolean trigger = true;
			
			if(hasPower() && canProcess() && this.dualCookTime == 0)
			{
				trigger = false;
			}
			
			if(trigger)
            {
                flag1 = true;
                MachineRtgFurnace.updateBlockState(this.dualCookTime > 0, this.world, pos);
            }
		}
		
		if(flag1)
		{
			this.markDirty();
		}
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		int i = e.ordinal();
		return i == 0 ? slots_bottom : (i == 1 ? slots_top : slots_side);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
		if(slot < 4){
			if(!(itemStack.getItem() instanceof ItemRTGPellet)){
				return true;
			}
		}
		if(slot == 4){
			return true;
		}
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		if(0 < i && i < 4){
			if(stack.getItem() instanceof ItemRTGPellet)
				return true;
		}
		if(i == 0){
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canInsertItem(int slot, ItemStack itemStack) {
		return this.isItemValidForSlot(slot, itemStack);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerRtgFurnace(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIRtgFurnace(player.inventory, this);
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		if (Library.isSwappingBetweenVariants(oldState, newState, ModBlocks.machine_rtg_furnace_off, ModBlocks.machine_rtg_furnace_on)) return false;
		return super.shouldRefresh(world, pos, oldState, newState);
	}
}
