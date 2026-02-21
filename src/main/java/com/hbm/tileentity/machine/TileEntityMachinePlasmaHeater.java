package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineITER;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMachinePlasmaHeater;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachinePlasmaHeater;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class TileEntityMachinePlasmaHeater extends TileEntityMachineBase implements ITickable, IFluidStandardReceiver, IEnergyReceiverMK2, IGUIProvider {

	public long power;
	public static final long maxPower = 100000000;

	public FluidTankNTM[] tanks;
	public FluidTankNTM plasma;
	
	public TileEntityMachinePlasmaHeater() {
		super(5, true, true);
		tanks = new FluidTankNTM[2];
		tanks[0] = new FluidTankNTM(Fluids.DEUTERIUM, 16000);
		tanks[1] = new FluidTankNTM(Fluids.TRITIUM, 16000);
		plasma = new FluidTankNTM(Fluids.PLASMA_DT, 64000);
	}

	@Override
	public String getDefaultName() {
		return "container.plasmaHeater";
	}

	@Override
	public void update() {
		if(!world.isRemote) {
			if(this.world.getTotalWorldTime() % 20 == 0)
				this.updateConnections();

			/// START Managing all the internal stuff ///
			power = Library.chargeTEFromItems(inventory, 0, power, maxPower);
			tanks[0].setType(1, 2, inventory);
			tanks[1].setType(3, 4, inventory);
			updateType();

			int maxConv = 50;
			int powerReq = 10000;

			int convert = Math.min(tanks[0].getFill(), tanks[1].getFill());
			convert = Math.min(convert, (plasma.getMaxFill() - plasma.getFill()) * 2);
			convert = Math.min(convert, maxConv);
			convert = (int) Math.min(convert, power / powerReq);
			convert = Math.max(0, convert);

			if(convert > 0 && plasma.getTankType() != Fluids.NONE) {

				tanks[0].setFill(tanks[0].getFill() - convert);
				tanks[1].setFill(tanks[1].getFill() - convert);

				plasma.setFill(plasma.getFill() + convert * 2);
				power -= convert * powerReq;

				this.markDirty();
			}
			/// END Managing all the internal stuff ///

			/// START Loading plasma into the ITER ///

			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
			int dist = 11;

			if(world.getBlockState(new BlockPos(pos.getX() + dir.offsetX * dist, pos.getY() + 2, pos.getZ() + dir.offsetZ * dist)).getBlock() == ModBlocks.iter) {
				int[] pos1 = ((MachineITER)ModBlocks.iter).findCore(world, pos.getX() + dir.offsetX * dist, pos.getY() + 2, pos.getZ() + dir.offsetZ * dist);
				
				if(pos1 != null) {
					TileEntity te = world.getTileEntity(new BlockPos(pos1[0], pos1[1], pos1[2]));

					if(te instanceof TileEntityITER iter) {

                        if(iter.plasma.getFill() == 0 && this.plasma.getTankType() != Fluids.NONE) {
							iter.plasma.setTankType(this.plasma.getTankType());
						}

						if(iter.isOn) {

							if(iter.plasma.getTankType() == this.plasma.getTankType()) {

								int toLoad = Math.min(iter.plasma.getMaxFill() - iter.plasma.getFill(), this.plasma.getFill());
								toLoad = Math.min(toLoad, 40);
								this.plasma.setFill(this.plasma.getFill() - toLoad);
								iter.plasma.setFill(iter.plasma.getFill() + toLoad);
								this.markDirty();
								iter.markDirty();
							}
						}
					}
				}
			}

			/// END Loading plasma into the ITER ///

			/// START Notif packets ///
			this.networkPackNT(50);
			/// END Notif packets ///
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		for(FluidTankNTM tank : this.tanks)
			tank.serialize(buf);
		this.plasma.serialize(buf);
		buf.writeLong(power);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		for(FluidTankNTM tank : this.tanks)
			tank.deserialize(buf);
		this.plasma.deserialize(buf);
		this.power = buf.readLong();
	}

	private void updateConnections()  {

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection side = dir.getRotation(ForgeDirection.UP);

		for(int i = 1; i < 4; i++) {
			for(int j = -1; j < 2; j++) {
				this.trySubscribe(world, pos.getX() + side.offsetX * j + dir.offsetX * 2, pos.getY() + i, pos.getZ() + side.offsetZ * j + dir.offsetZ * 2, j < 0 ? ForgeDirection.DOWN : ForgeDirection.UP);
				this.trySubscribe(tanks[0].getTankType(), world, pos.getX() + side.offsetX * j + dir.offsetX * 2, pos.getY() + i, pos.getZ() + side.offsetZ * j + dir.offsetZ * 2, j < 0 ? ForgeDirection.DOWN : ForgeDirection.UP);
				this.trySubscribe(tanks[1].getTankType(), world, pos.getX() + side.offsetX * j + dir.offsetX * 2, pos.getY() + i, pos.getZ() + side.offsetZ * j + dir.offsetZ * 2, j < 0 ? ForgeDirection.DOWN : ForgeDirection.UP);
			}
		}
	}

	private void updateType() {

		List<FluidType> types = new ArrayList<>() {{ add(tanks[0].getTankType()); add(tanks[1].getTankType()); }};

		if(types.contains(Fluids.DEUTERIUM) && types.contains(Fluids.TRITIUM)) {
			plasma.setTankType(Fluids.PLASMA_DT);
			return;
		}
		if(types.contains(Fluids.DEUTERIUM) && types.contains(Fluids.HELIUM3)) {
			plasma.setTankType(Fluids.PLASMA_DH3);
			return;
		}
		if(types.contains(Fluids.DEUTERIUM) && types.contains(Fluids.HYDROGEN)) {
			plasma.setTankType(Fluids.PLASMA_HD);
			return;
		}
		if(types.contains(Fluids.HYDROGEN) && types.contains(Fluids.TRITIUM)) {
			plasma.setTankType(Fluids.PLASMA_HT);
			return;
		}
		if(types.contains(Fluids.HELIUM4) && types.contains(Fluids.OXYGEN)) {
			plasma.setTankType(Fluids.PLASMA_XM);
			return;
		}
		if(types.contains(Fluids.BALEFIRE) && types.contains(Fluids.AMAT)) {
			plasma.setTankType(Fluids.PLASMA_BF);
			return;
		}

		plasma.setTankType(Fluids.NONE);
	}
	
	public long getPowerScaled(int i) {
		return (power * i) / maxPower;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.power = nbt.getLong("power");
		tanks[0].readFromNBT(nbt, "fuel_1n");
		tanks[1].readFromNBT(nbt, "fuel_2n");
		plasma.readFromNBT(nbt, "plasman");
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		tanks[0].writeToNBT(nbt, "fuel_1n");
		tanks[1].writeToNBT(nbt, "fuel_2n");
		plasma.writeToNBT(nbt, "plasman");
		return nbt;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}
	
	@Override
	public void setPower(long i) {
		this.power = i;
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
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[] {tanks[0], tanks[1], plasma};
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return tanks;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachinePlasmaHeater(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachinePlasmaHeater(player.inventory, this);
	}
}