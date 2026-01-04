package com.hbm.tileentity.machine;

import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerFurnaceIron;
import com.hbm.inventory.gui.GUIFurnaceIron;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.ForgeDirection;
import com.hbm.modules.ModuleBurnTime;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityFurnaceIron extends TileEntityMachineBase implements IGUIProvider, ITickable {
	
	public int maxBurnTime;
	public int burnTime;
	public boolean wasOn = false;

	public int progress;
	public int processingTime;
	public static final int baseTime = 200;
	
	public ModuleBurnTime burnModule;

	public TileEntityFurnaceIron() {
		super(5);
		
		burnModule = new ModuleBurnTime()
				.setLigniteTimeMod(1.25)
				.setCoalTimeMod(1.25)
				.setCokeTimeMod(1.5)
				.setSolidTimeMod(2)
				.setRocketTimeMod(2)
				.setBalefireTimeMod(2);
	}

	@Override
	public String getDefaultName() {
		return "container.furnaceIron";
	}

	@Override
	public void update() {
		
		if(!world.isRemote) {
			
			this.processingTime = baseTime - 15 * ItemMachineUpgrade.getSpeed(inventory.getStackInSlot(4));
			
			wasOn = false;
			
			if(burnTime <= 0) {
				
				for(int i = 1; i < 3; i++) {
					ItemStack input = inventory.getStackInSlot(i);
					if(!input.isEmpty()) {
						
						int fuel = burnModule.getBurnTime(input);
						
						if(fuel > 0) {
							this.maxBurnTime = this.burnTime = fuel;
							
							ItemStack copy = input.copy();
							input.shrink(1);

							if(input.isEmpty()) inventory.setStackInSlot(i, copy.getItem().getContainerItem(copy));
							break;
						}
					}
				} 
			}
			
			if(canSmelt()) {
				wasOn = true;
				this.progress++;
				this.burnTime--;
				
				if(this.progress % 15 == 0 && !this.muffled) {
					world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 0.5F + world.rand.nextFloat() * 0.5F);
				}
				
				if(this.progress >= this.processingTime) {
					ItemStack outputs = inventory.getStackInSlot(3);
					ItemStack result = FurnaceRecipes.instance().getSmeltingResult(inventory.getStackInSlot(0));
					ItemStack copy = outputs;
		
					if(outputs.isEmpty()) {
						 copy  = result.copy();
						 inventory.setStackInSlot(3, copy);
					} else {
						outputs.setCount(copy.getCount() + result.getCount());
					}
					
					inventory.getStackInSlot(0).shrink(1);
					
					this.progress = 0;
					this.markDirty();
				}
                if(world.getTotalWorldTime() % 20 == 0) PollutionHandler.incrementPollution(world, pos, PollutionHandler.PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND);
            } else {
				this.progress = 0;
			}

			networkPackNT(50);
		} else {
			
			if(this.progress > 0) {
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
				ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
				
				double offset = this.progress % 2 == 0 ? 1 : 0.5;
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + 0.5 - dir.offsetX * offset - rot.offsetX * 0.1875, pos.getY() + 2, pos.getZ() + 0.5 - dir.offsetZ * offset - rot.offsetZ * 0.1875, 0.0, 0.01, 0.0);
				
				if(this.progress % 5 == 0) {
					world.spawnParticle(EnumParticleTypes.FLAME, pos.getX() + 0.5 + dir.offsetX * 0.25 + rot.offsetX * world.rand.nextDouble(), pos.getY() + 0.25 +world.rand.nextDouble() * 0.25, pos.getZ() + 0.5 + dir.offsetZ * 0.25 + rot.offsetZ * world.rand.nextDouble(), 0.0, 0.0, 0.0);
				}
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeInt(this.maxBurnTime);
		buf.writeInt(this.burnTime);
		buf.writeInt(this.progress);
		buf.writeInt(this.processingTime);
		buf.writeBoolean(this.wasOn);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.maxBurnTime = buf.readInt();
		this.burnTime = buf.readInt();
		this.progress = buf.readInt();
		this.processingTime = buf.readInt();
		this.wasOn = buf.readBoolean();
	}
	
	public boolean canSmelt() {
		
		if(this.burnTime <= 0) return false;
		if(inventory.getStackInSlot(0).isEmpty()) return false;
		
		ItemStack result = FurnaceRecipes.instance().getSmeltingResult(inventory.getStackInSlot(0));
		
		if(result.isEmpty()) return false;
		if(inventory.getStackInSlot(3).isEmpty()) return true;
		
		if(!result.isItemEqual(inventory.getStackInSlot(3))) return false;
		if(inventory.getStackInSlot(3).getCount() < inventory.getSlotLimit(3) && inventory.getStackInSlot(3).getCount() < inventory.getStackInSlot(3).getMaxStackSize()) {
			return true;
		}else{
			return inventory.getStackInSlot(3).getCount() < result.getMaxStackSize();
		}
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		return new int[] { 0, 1, 2, 3 };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		
		if(i == 0)
			return !FurnaceRecipes.instance().getSmeltingResult(itemStack).isEmpty();
		
		if(i < 3)
			return burnModule.getBurnTime(itemStack) > 0;
			
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return i == 3;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.maxBurnTime = nbt.getInteger("maxBurnTime");
		this.burnTime = nbt.getInteger("burnTime");
		this.progress = nbt.getInteger("progress");
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("maxBurnTime", maxBurnTime);
		nbt.setInteger("burnTime", burnTime);
		nbt.setInteger("progress", progress);
		return nbt;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerFurnaceIron(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIFurnaceIron(player.inventory, this);
	}
	
	AxisAlignedBB bb = null;
	
	@Override
    public AxisAlignedBB getRenderBoundingBox() {
		
		if (bb == null) {
            bb = new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 3, pos.getZ() + 2);
        }

        return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
