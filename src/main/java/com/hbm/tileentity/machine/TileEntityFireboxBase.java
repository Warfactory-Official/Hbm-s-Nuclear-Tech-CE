package com.hbm.tileentity.machine;

import com.hbm.api.tile.IHeatSource;
import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ItemEnums;
import com.hbm.lib.ForgeDirection;
import com.hbm.modules.ModuleBurnTime;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.ItemStackUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class TileEntityFireboxBase extends TileEntityMachinePolluting implements ITickable, IGUIProvider, IHeatSource {

	public int maxBurnTime;
	public int burnTime;
	public int burnHeat;
	public boolean wasOn = false;
	public int playersUsing = 0;
	
	public float doorAngle = 0;
	public float prevDoorAngle = 0;

	public int heatEnergy;
   
	public TileEntityFireboxBase() {
		super(2, 50, true, false);
	}
	 
	@Override
	public void update() {
		
		if(!world.isRemote) {
            for(int i = 2; i < 6; i++) {
                ForgeDirection dir = ForgeDirection.getOrientation(i);
                ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

                for(int j = -1; j <= 1; j++) {
                    this.sendSmoke(pos.getX() + dir.offsetX * 2 + rot.offsetX * j, pos.getY(), pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * j, dir);
                }
            }
			wasOn = false;
			
			if(burnTime <= 0) {
				
				for(int i = 0; i < 2; i++) {
					if(!inventory.getStackInSlot(i).isEmpty()) {

						int baseTime = getModule().getBurnTime(inventory.getStackInSlot(i));

						if(baseTime > 0) {
							int fuel = (int) (baseTime * getTimeMult());

							TileEntity below = world.getTileEntity(pos.down());

							if(below instanceof TileEntityAshpit) {
								TileEntityAshpit ashpit = (TileEntityAshpit) below;
								ItemEnums.EnumAshType type = this.getAshFromFuel(inventory.getStackInSlot(i));
								if(type == ItemEnums.EnumAshType.WOOD) ashpit.ashLevelWood += baseTime;
								if(type == ItemEnums.EnumAshType.COAL) ashpit.ashLevelCoal += baseTime;
								if(type == ItemEnums.EnumAshType.MISC) ashpit.ashLevelMisc += baseTime;
							}

							this.maxBurnTime = this.burnTime = fuel;
							this.burnHeat = getModule().getBurnHeat(getBaseHeat(), inventory.getStackInSlot(i));
							inventory.getStackInSlot(i).shrink(1);

							if(inventory.getStackInSlot(i).getCount() == 0) {
								inventory.setStackInSlot(i, inventory.getStackInSlot(i).getItem().getContainerItem(inventory.getStackInSlot(i)));
							}

							this.wasOn = true;
							break;
						}
					}
				} 
			} else {
				
				if(this.heatEnergy < getMaxHeat()) {
					burnTime--;
                    if(world.getTotalWorldTime() % 20 == 0) this.pollute(PollutionHandler.PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * 3);
                }
				this.wasOn = true;
				
				if(world.rand.nextInt(15) == 0 && !this.muffled) {
					world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 0.5F + world.rand.nextFloat() * 0.5F);
				}
			}
			
			if(wasOn) {
				this.heatEnergy = Math.min(this.heatEnergy + this.burnHeat, getMaxHeat());
			} else {
				this.heatEnergy = Math.max(this.heatEnergy - Math.max(this.heatEnergy / 1000, 1), 0);
				this.burnHeat = 0;
			}

			networkPackNT(50);
		} else {
			this.prevDoorAngle = this.doorAngle;
			float swingSpeed = (doorAngle / 10F) + 3;
			
			if(this.playersUsing > 0) {
				this.doorAngle += swingSpeed;
			} else {
				this.doorAngle -= swingSpeed;
			}
			
			this.doorAngle = MathHelper.clamp(this.doorAngle, 0F, 135F);
			
			if(wasOn && world.getTotalWorldTime() % 5 == 0) {
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
				double x = pos.getX() + 0.5 + dir.offsetX;
				double y = pos.getY() + 0.25;
				double z = pos.getZ() + 0.5 + dir.offsetZ;
				world.spawnParticle(EnumParticleTypes.FLAME, wasOn, x + world.rand.nextDouble() * 0.5 - 0.25, y + world.rand.nextDouble() * 0.25, z + world.rand.nextDouble() * 0.5 - 0.25, 0, 0, 0);
			}
		}
	}

	public static ItemEnums.EnumAshType getAshFromFuel(ItemStack stack) {

		List<String> names = ItemStackUtil.getOreDictNames(stack);

		for(String name : names) {
			if(name.contains("Coke"))		return ItemEnums.EnumAshType.COAL;
			if(name.contains("Coal"))		return ItemEnums.EnumAshType.COAL;
			if(name.contains("Lignite"))	return ItemEnums.EnumAshType.COAL;
			if(name.startsWith("log"))		return ItemEnums.EnumAshType.WOOD;
			if(name.contains("Wood"))		return ItemEnums.EnumAshType.WOOD;
			if(name.contains("Sapling"))	return ItemEnums.EnumAshType.WOOD;
		}

		return ItemEnums.EnumAshType.MISC;
	}

	public abstract ModuleBurnTime getModule();
	public abstract int getBaseHeat();
	public abstract double getTimeMult();
	public abstract int getMaxHeat();
	
	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		return new int[] { 0, 1 };
	}
	
	
	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return getModule().getBurnTime(itemStack) > 0;
	}

	@Override
	public void serialize(ByteBuf buf) {
        super.serialize(buf);
		buf.writeInt(this.maxBurnTime);
		buf.writeInt(this.burnTime);
		buf.writeInt(this.burnHeat);
		buf.writeInt(this.heatEnergy);
		buf.writeInt(this.playersUsing);
		buf.writeBoolean(this.wasOn);
	}

	@Override
	public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
		this.maxBurnTime = buf.readInt();
		this.burnTime = buf.readInt();
		this.burnHeat = buf.readInt();
		this.heatEnergy = buf.readInt();
		this.playersUsing = buf.readInt();
		this.wasOn = buf.readBoolean();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.maxBurnTime = nbt.getInteger("maxBurnTime");
		this.burnTime = nbt.getInteger("burnTime");
		this.burnHeat = nbt.getInteger("burnHeat");
		this.heatEnergy = nbt.getInteger("heatEnergy");
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		nbt.setInteger("maxBurnTime", maxBurnTime);
		nbt.setInteger("burnTime", burnTime);
		nbt.setInteger("burnHeat", burnHeat);
		nbt.setInteger("heatEnergy", heatEnergy);
		return super.writeToNBT(nbt);
	}

    @Override
	public int getHeatStored() {
		return heatEnergy;
	}

    @Override
	public void useUpHeat(int heat) {
		this.heatEnergy = Math.max(0, this.heatEnergy - heat);
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = new AxisAlignedBB(
					pos.getX() - 1,
					pos.getY(),
					pos.getZ() - 1,
					pos.getX() + 2,
					pos.getY() + 1,
					pos.getZ() + 2
					);
		}
		
		return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[0];
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return this.getSmokeTanks();
    }

    @Override
    public boolean canConnect(FluidType type, ForgeDirection dir) {
        return dir != ForgeDirection.UNKNOWN && dir != ForgeDirection.DOWN;
    }
}
