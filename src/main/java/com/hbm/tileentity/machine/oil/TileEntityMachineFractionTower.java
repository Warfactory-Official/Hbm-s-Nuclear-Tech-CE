package com.hbm.tileentity.machine.oil;

import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.recipes.FractionRecipes;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.packet.toclient.BufPacket;
import com.hbm.tileentity.IConnectionAnchors;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.Tuple;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@AutoRegister
public class TileEntityMachineFractionTower extends TileEntityLoadedBase implements IBufPacketReceiver, IFluidStandardTransceiver, ITickable, IConnectionAnchors {
	
	public FluidTankNTM[] tanks;
	
	public TileEntityMachineFractionTower() {
		super();

		tanks = new FluidTankNTM[3];
		tanks[0] = new FluidTankNTM(Fluids.HEAVYOIL, 4000).withOwner(this);
		tanks[1] = new FluidTankNTM(Fluids.BITUMEN, 4000).withOwner(this);
		tanks[2] = new FluidTankNTM(Fluids.SMEAR, 4000).withOwner(this);
	}
	
	@Override
	public void update() {

		if(!world.isRemote) {

			TileEntity stack = world.getTileEntity(pos.add(0, 3,0));

			if(stack instanceof TileEntityMachineFractionTower) {
				TileEntityMachineFractionTower frac = (TileEntityMachineFractionTower) stack;

				//make types equal
				for(int i = 0; i < 3; i++) {
					frac.tanks[i].setTankType(tanks[i].getTankType());
				}

				//calculate transfer
				int oil = Math.min(tanks[0].getFill(), frac.tanks[0].getMaxFill() - frac.tanks[0].getFill());
				int left = Math.min(frac.tanks[1].getFill(), tanks[1].getMaxFill() - tanks[1].getFill());
				int right = Math.min(frac.tanks[2].getFill(), tanks[2].getMaxFill() - tanks[2].getFill());

				//move oil up, pull fractions down
				tanks[0].setFill(tanks[0].getFill() - oil);
				tanks[1].setFill(tanks[1].getFill() + left);
				tanks[2].setFill(tanks[2].getFill() + right);
				frac.tanks[0].setFill(frac.tanks[0].getFill() + oil);
				frac.tanks[1].setFill(frac.tanks[1].getFill() - left);
				frac.tanks[2].setFill(frac.tanks[2].getFill() - right);
			}

			setupTanks();
			this.updateConnections();

			if(world.getTotalWorldTime() % 10 == 0)
				fractionate();

			this.sendFluid();

			PacketThreading.createAllAroundThreadedPacket(new BufPacket(pos.getX(), pos.getY(), pos.getZ(), this), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 50));
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		for(int i = 0; i < 3; i++)
			tanks[i].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		for(int i = 0; i < 3; i++)
			tanks[i].deserialize(buf);
	}

	private void sendFluid() {

		for(DirPos pos : getConPos()) {
			this.sendFluid(tanks[1], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			this.sendFluid(tanks[2], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
		}
	}

	private void updateConnections() {

		for(DirPos pos : getConPos()) {
			this.trySubscribe(tanks[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
		}
	}

	private void setupTanks() {

		Tuple.Pair<FluidStack, FluidStack> quart = FractionRecipes.getFractions(tanks[0].getTankType());

		if(quart != null) {
			tanks[1].setTankType(quart.getKey().type);
			tanks[2].setTankType(quart.getValue().type);
		} else {
			tanks[0].setTankType(Fluids.NONE);
			tanks[1].setTankType(Fluids.NONE);
			tanks[2].setTankType(Fluids.NONE);
		}
	}

	public DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(pos.getX() + 2, pos.getY(), pos.getZ(), Library.POS_X),
				new DirPos(pos.getX() - 2, pos.getY(), pos.getZ(), Library.NEG_X),
				new DirPos(pos.getX(), pos.getY(), pos.getZ() + 2, Library.POS_Z),
				new DirPos(pos.getX(), pos.getY(), pos.getZ() - 2, Library.NEG_Z)
		};
	}

	private void fractionate() {

		Tuple.Pair<FluidStack, FluidStack> quart = FractionRecipes.getFractions(tanks[0].getTankType());

		if(quart != null) {

			int left = quart.getKey().fill;
			int right = quart.getValue().fill;

			if(tanks[0].getFill() >= 100 && hasSpace(left, right)) {
				tanks[0].setFill(tanks[0].getFill() - 100);
				tanks[1].setFill(tanks[1].getFill() + left);
				tanks[2].setFill(tanks[2].getFill() + right);
			}
		}
	}

	private boolean hasSpace(int left, int right) {
		return tanks[1].getFill() + left <= tanks[1].getMaxFill() && tanks[2].getFill() + right <= tanks[2].getMaxFill();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		for(int i = 0; i < 3; i++)
			tanks[i].readFromNBT(nbt, "tank" + i);
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		for(int i = 0; i < 3; i++)
			tanks[i].writeToNBT(nbt, "tank" + i);
		return nbt;
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
					pos.getY() + 3,
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
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
					new NTMFluidHandlerWrapper(this)
			);
		}
		return super.getCapability(capability, facing);
	}
}