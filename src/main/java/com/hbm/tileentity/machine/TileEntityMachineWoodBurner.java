package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.container.ContainerMachineWoodBurner;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.gui.GUIMachineWoodBurner;
import com.hbm.items.ItemEnums.EnumAshType;
import com.hbm.items.ModItems;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.modules.ModuleBurnTime;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// THE BURNER OF WOOD

@AutoRegister
public class TileEntityMachineWoodBurner extends TileEntityMachineBase implements IFluidStandardReceiver, IControlReceiver, IEnergyProviderMK2, IGUIProvider, ITickable, IFluidCopiable {
	
	public long power;
	public static final long maxPower = 100_000;
	public int burnTime;
	public int maxBurnTime;
	public boolean liquidBurn = false;
	public boolean isOn = false;
	protected int powerGen = 0;
	
	public FluidTankNTM tank;
	
	public static ModuleBurnTime burnModule = new ModuleBurnTime().setLogTimeMod(4).setWoodTimeMod(2);

	public int ashLevelWood;
	public int ashLevelCoal;
	public int ashLevelMisc;

	public TileEntityMachineWoodBurner() {
		super(6, true, true);
		this.tank = new FluidTankNTM(Fluids.WOODOIL, 16_000);
	}

	@Override
	public String getDefaultName() {
		return "container.machineWoodBurner";
	}

	@Override
	public void update() {
		
		if(!world.isRemote) {
			
			powerGen = 0;
			
			this.tank.setType(2, inventory);
			this.tank.loadTank(3, 4, inventory);
			this.power = Library.chargeItemsFromTE(inventory, 5, power, maxPower);
			
			for(DirPos pos : getConPos()) {
                BlockPos blockPos = pos.getPos();
                if(power > 0) this.tryProvide(world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), pos.getDir());
                if(world.getTotalWorldTime() % 20 == 0) this.trySubscribe(tank.getTankType(), world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), pos.getDir());
            }
			
			if(!liquidBurn) {
				
				if(this.burnTime <= 0) {
					 {
						if (!inventory.getStackInSlot(0).isEmpty()) {
							int burn = this.burnModule.getBurnTime(inventory.getStackInSlot(0));
							if (burn > 0) {
								EnumAshType type = TileEntityFireboxBase.getAshFromFuel(inventory.getStackInSlot(0));
								if (type == EnumAshType.WOOD) ashLevelWood += burn;
								if (type == EnumAshType.COAL) ashLevelCoal += burn;
								if (type == EnumAshType.MISC) ashLevelMisc += burn;
								int threshold = 2000;
                                while(processAsh(ashLevelWood, EnumAshType.WOOD, threshold)) ashLevelWood -= threshold;
                                while(processAsh(ashLevelCoal, EnumAshType.COAL, threshold)) ashLevelCoal -= threshold;
                                while(processAsh(ashLevelMisc, EnumAshType.MISC, threshold)) ashLevelMisc -= threshold;

                                this.maxBurnTime = this.burnTime = burn;
								ItemStack container = inventory.getStackInSlot(0).getItem().getContainerItem(inventory.getStackInSlot(0));
								inventory.getStackInSlot(0).shrink(1);
								if (inventory.getStackInSlot(0).isEmpty() && !container.isEmpty())
									inventory.setStackInSlot(0, container.getItem().getContainerItem(container));
								this.markChanged();
							}
						}
					}
				} else if(this.power < this.maxPower && isOn){
					this.burnTime--;
					this.powerGen += 100;
					if(world.getTotalWorldTime() % 20 == 0) PollutionHandler.incrementPollution(world, pos, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND);
				}
				
			} else {
				
				if(this.power < this.maxPower && tank.getFill() > 0 && isOn) {
					FT_Flammable trait = tank.getTankType().getTrait(FT_Flammable.class);
					
					if(trait != null) {
						
						int toBurn = Math.min(tank.getFill(), 2);
						
						if(toBurn > 0) {
							this.powerGen += trait.getHeatEnergy() * toBurn / 2_000L;
							this.tank.setFill(this.tank.getFill() - toBurn);
							if(world.getTotalWorldTime() % 20 == 0) PollutionHandler.incrementPollution(world, pos, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * toBurn / 2F);
						}
					}
				}
			}
			
			this.power += this.powerGen;
			if(this.power > this.maxPower) this.power = this.maxPower;
			
			this.networkPackNT(25);
		} else {
			
			if(powerGen > 0) {
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
				ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + 0.5 - dir.offsetX + rot.offsetX, pos.getY() + 4, pos.getZ() + 0.5 - dir.offsetZ + rot.offsetZ, 0, 0.05, 0);
			}
		}
	}
	
	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(burnTime);
		buf.writeInt(powerGen);
		buf.writeInt(maxBurnTime);
		buf.writeBoolean(isOn);
		buf.writeBoolean(liquidBurn);
		
		tank.serialize(buf);
	}
	
	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		burnTime = buf.readInt();
		powerGen = buf.readInt();
		maxBurnTime = buf.readInt();
		isOn = buf.readBoolean();
		liquidBurn = buf.readBoolean();
		
		tank.deserialize(buf);
	}
	
	private DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
				new DirPos(this.pos.getX() - dir.offsetX * 2, this.pos.getY(), this.pos.getZ() - dir.offsetZ * 2, rot.getOpposite()),
				new DirPos(this.pos.getX() - dir.offsetX * 2 + rot.offsetX, this.pos.getY(), this.pos.getZ() - dir.offsetZ * 2 + rot.offsetZ, dir.getOpposite())
		};
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.power = nbt.getLong("power");
		this.burnTime = nbt.getInteger("burnTime");
		this.maxBurnTime = nbt.getInteger("maxBurnTime");
		this.isOn = nbt.getBoolean("isOn");
		this.liquidBurn = nbt.getBoolean("liquidBurn");
		tank.readFromNBT(nbt, "t");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setLong("power", power);
		nbt.setInteger("burnTime", burnTime);
		nbt.setInteger("maxBurnTime", maxBurnTime);
		nbt.setBoolean("isOn", isOn);
		nbt.setBoolean("liquidBurn", liquidBurn);
        tank.writeToNBT(nbt, "t");
		return super.writeToNBT(nbt);
	}
	
	protected boolean processAsh(int level, EnumAshType type, int threshold) {

		if(level >= threshold) {
			for(int i = 0; i < 5; i++) {
				if(inventory.getStackInSlot(i).isEmpty()) {
					inventory.setStackInSlot(i, OreDictManager.DictFrame.fromOne(ModItems.powder_ash, type));
					ashLevelWood -= threshold;
					return true;
				} else if(inventory.getStackInSlot(i).getCount() < inventory.getStackInSlot(i).getMaxStackSize() && inventory.getStackInSlot(i).getItem() == ModItems.powder_ash && inventory.getStackInSlot(i).getItemDamage() == type.ordinal()) {
					inventory.getStackInSlot(i).grow(1);
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.hasKey("toggle")) {
			this.isOn = !this.isOn;
			this.markDirty();
		}
		if(data.hasKey("switch")) {
			this.liquidBurn = !this.liquidBurn;
			this.markDirty();
		}
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return this.isUseableByPlayer(player);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineWoodBurner(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineWoodBurner(player.inventory, this);
	}

    @Override
	public int[] getAccessibleSlotsFromSide(EnumFacing facing) {
		return new int[] { 0, 1 };
	}
	
	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return i == 0 && burnModule.getBurnTime(itemStack) > 0;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int side) {
		return slot == 1;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
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
	public boolean canConnect(ForgeDirection dir) {
		ForgeDirection rot = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		return dir == rot.getOpposite();
	}
	
	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		ForgeDirection rot = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		return dir == rot.getOpposite();
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[] {tank};
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] {tank};

	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) bb = new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 6, pos.getZ() + 2);
		return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;

	}
/*
	@Override
	public void provideExtraInfo(NBTTagCompound data) {
		data.setBoolean(CompatEnergyControl.B_ACTIVE, isOn);
		if(this.liquidBurn) data.setDouble(CompatEnergyControl.D_CONSUMPTION_MB, 1D);
		data.setDouble(CompatEnergyControl.D_OUTPUT_HE, power);
	}
*/
	@Override
	public FluidTankNTM getTankToPaste() {
		return tank;
	}
}
