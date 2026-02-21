package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardSender;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.hazard.HazardSystem;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerStorageDrum;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIStorageDrum;
import com.hbm.inventory.recipes.StorageDrumRecipes;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.ContaminationUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityStorageDrum extends TileEntityMachineBase implements ITickable, IFluidStandardSender, IGUIProvider {

	public FluidTankNTM[] tanks;
	private static final int[] slots_arr = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23 };
	public int age = 0;

	private static final float decayRate = 0.9965402628F; //10s Halflife

	public TileEntityStorageDrum() {
		super(24, 1, true, false);
		tanks = new FluidTankNTM[2];
		tanks[0] = new FluidTankNTM(Fluids.WASTEFLUID, 16000);
		tanks[1] = new FluidTankNTM(Fluids.WASTEGAS, 16000);
	}

	@Override
	public String getDefaultName() {
		return "container.storageDrum";
	}

	@Override
	public void update() {
		
		if(!world.isRemote) {

            double rad = 0D;
			
			int liquid = 0;
			int gas = 0;
			
			for(int i = 0; i < 24; i++) {
				
				if(!inventory.getStackInSlot(i).isEmpty()) {

					ItemStack itemStack = inventory.getStackInSlot(i);

					if(world.getTotalWorldTime() % 20 == 0) {
                        rad += HazardSystem.getRawRadsFromStack(itemStack);
					}

					int[] wasteData = StorageDrumRecipes.getWaste(inventory.getStackInSlot(i));
					if(wasteData != null){
						if(world.rand.nextInt(wasteData[0]) == 0){
							ItemStack outputStack = StorageDrumRecipes.getOutput(inventory.getStackInSlot(i));
							if(outputStack != null){
								liquid += wasteData[1];
								gas += wasteData[2];
								inventory.setStackInSlot(i, outputStack.copy());
							}
						}
					} else {
						ContaminationUtil.neutronActivateItem(inventory.getStackInSlot(i), 0.0F, decayRate);
					}
				}
			}

			for(int i = 0; i < 2; i++) {
				
				int overflow = Math.max(this.tanks[i].getFluidAmount() + (i == 0 ? liquid : gas) - this.tanks[i].getCapacity(), 0);
				
				if(overflow > 0) {
                    ChunkRadiationManager.proxy.incrementRad(world, pos, overflow * 0.5F);
                }
			}
			
			this.tanks[0].fill(Fluids.WASTEFLUID, liquid, true);
			this.tanks[1].fill(Fluids.WASTEGAS, gas, true);
			
			age++;
			
			if(age >= 20)
				age -= 20;
			
			if(age == 9 || age == 19) {
				fillFluidInit(tanks[0]);
			}
			if(age == 8 || age == 18) {
				fillFluidInit(tanks[1]);
			}
			networkPackNT(10);
            if (rad > 0) {
                ContaminationUtil.radiate(world, pos.getZ(), pos.getY(), pos.getX(), 32, (float) rad);
            }
		}
	}

	@Override
	public void serialize(ByteBuf buf){
		super.serialize(buf);
		for(FluidTankNTM tank : this.tanks) {
			tank.serialize(buf);
		}
	}

	@Override
	public void deserialize(ByteBuf buf){
		super.deserialize(buf);
		for(FluidTankNTM tank : this.tanks) {
			tank.deserialize(buf);
		}
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return StorageDrumRecipes.getOutput(itemStack) != null || ContaminationUtil.isContaminated(itemStack);
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack) {
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
		return !ContaminationUtil.isContaminated(itemStack) && StorageDrumRecipes.getOutput(itemStack) == null;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing side) {
		return slots_arr;
	}

	private void fillFluidInit(FluidTankNTM tank) {
		sendFluid(tank, world, pos.add(-1, 0, 0), ForgeDirection.WEST);
		sendFluid(tank, world, pos.add(1, 0, 0), ForgeDirection.EAST);
		sendFluid(tank, world, pos.add(0, -1, 0), ForgeDirection.DOWN);
		sendFluid(tank, world, pos.add(0, 1, 0), ForgeDirection.UP);
		sendFluid(tank, world, pos.add(0, 0, -1), ForgeDirection.NORTH);
		sendFluid(tank, world, pos.add(0, 0, 1), ForgeDirection.SOUTH);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.tanks[0].readFromNBT(nbt, "liquid");
		this.tanks[1].readFromNBT(nbt,"gas");
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		this.tanks[0].writeToNBT(nbt, "liquid");
		this.tanks[1].writeToNBT(nbt,"gas");
		return nbt;
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return tanks;
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return tanks;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerStorageDrum(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIStorageDrum(player.inventory, this);
	}
}
