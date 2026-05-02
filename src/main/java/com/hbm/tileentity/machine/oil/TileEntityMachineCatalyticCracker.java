package com.hbm.tileentity.machine.oil;

import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.recipes.CrackingRecipes;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IConnectionAnchors;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.Tuple;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@AutoRegister
public class TileEntityMachineCatalyticCracker extends TileEntityLoadedBase implements ITickable, IFluidStandardTransceiver, IConnectionAnchors {
	
	public FluidTankNTM[] tanks;
	
	public TileEntityMachineCatalyticCracker() {
		super();
		tanks = new FluidTankNTM[5];

		tanks[0] = new FluidTankNTM(Fluids.BITUMEN, 4000).withOwner(this);

		tanks[1] = new FluidTankNTM(Fluids.STEAM, 8000).withOwner(this);

		tanks[2] = new FluidTankNTM(Fluids.OIL, 4000).withOwner(this);

		tanks[3] = new FluidTankNTM(Fluids.PETROLEUM, 4000).withOwner(this);

		tanks[4] = new FluidTankNTM(Fluids.SPENTSTEAM, 4000).withOwner(this);
	}

	@Override
	public void update() {

		if(!world.isRemote) {

			this.world.profiler.startSection("catalyticCracker_setup_tanks");
			setupTanks();
			this.world.profiler.endStartSection("catalyticCracker_update_connections");
			updateConnections();

			this.world.profiler.endStartSection("catalyticCracker_do_recipe");
			if(world.getTotalWorldTime() % 5 == 0)
				crack();

			this.world.profiler.endStartSection("catalyticCracker_send_fluid");
			if(world.getTotalWorldTime() % 10 == 0) {

				for(DirPos pos : getConPos()) {
					for(int i = 2; i <= 4; i++) {
						if(tanks[i].getFill() > 0) this.sendFluid(tanks[i], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
					}
				}

				networkPackNT(50);
			}
			this.world.profiler.endSection();
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		for(int i = 0; i < 5; i++)
			tanks[i].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		for(int i = 0; i < 5; i++)
			tanks[i].deserialize(buf);
	}

	private void updateConnections() {

		for(DirPos pos : getConPos()) {
			this.trySubscribe(tanks[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			this.trySubscribe(tanks[1].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
		}
	}

	public DirPos[] getConPos() {

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
				new DirPos(pos.getX() + dir.offsetX * 4 + rot.offsetX * 1, pos.getY(), pos.getZ() + dir.offsetZ * 4 + rot.offsetZ * 1, dir),
				new DirPos(pos.getX() + dir.offsetX * 4 - rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 4 - rot.offsetZ * 2, dir),
				new DirPos(pos.getX() - dir.offsetX * 4 + rot.offsetX * 1, pos.getY(), pos.getZ() - dir.offsetZ * 4 + rot.offsetZ * 1, dir.getOpposite()),
				new DirPos(pos.getX() - dir.offsetX * 4 - rot.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 4 - rot.offsetZ * 2, dir.getOpposite()),
				new DirPos(pos.getX() + dir.offsetX * 2 + rot.offsetX * 3, pos.getY(), pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * 3, rot),
				new DirPos(pos.getX() + dir.offsetX * 2 - rot.offsetX * 4, pos.getY(), pos.getZ() + dir.offsetZ * 2 - rot.offsetZ * 4, rot),
				new DirPos(pos.getX() - dir.offsetX * 2 + rot.offsetX * 3, pos.getY(), pos.getZ() - dir.offsetZ * 2 + rot.offsetZ * 3, rot.getOpposite()),
				new DirPos(pos.getX() - dir.offsetX * 2 - rot.offsetX * 4, pos.getY(), pos.getZ() - dir.offsetZ * 2 - rot.offsetZ * 4, rot.getOpposite())
		};
	}

	private void setupTanks() {

		Tuple.Pair<FluidStack, FluidStack> quart = CrackingRecipes.getCracking(tanks[0].getTankType());

		if(quart != null) {
			tanks[1].setTankType(Fluids.STEAM);
			tanks[2].setTankType(quart.getKey().type);
			tanks[3].setTankType(quart.getValue().type);
			tanks[4].setTankType(Fluids.SPENTSTEAM);
		} else {
			tanks[0].setTankType(Fluids.NONE);
			tanks[1].setTankType(Fluids.NONE);
			tanks[2].setTankType(Fluids.NONE);
			tanks[3].setTankType(Fluids.NONE);
			tanks[4].setTankType(Fluids.NONE);
		}
	}

	private void crack() {

		Tuple.Pair<FluidStack, FluidStack> quart = CrackingRecipes.getCracking(tanks[0].getTankType());

		if(quart != null) {

			int left = quart.getKey().fill;
			int right = quart.getValue().fill;

			for(int i = 0; i < 2; i++) {
				if(tanks[0].getFill() >= 100 && tanks[1].getFill() >= 200 && hasSpace(left, right)) {
					tanks[0].setFill(tanks[0].getFill() - 100);
					tanks[1].setFill(tanks[1].getFill() - 200);
					tanks[2].setFill(tanks[2].getFill() + left);
					tanks[3].setFill(tanks[3].getFill() + right);
					tanks[4].setFill(tanks[4].getFill() + 2); //LPS has the density of WATER not STEAM (1%!)
				}
			}
		}
	}

	private boolean hasSpace(int left, int right) {
		return tanks[2].getFill() + left <= tanks[2].getMaxFill() && tanks[3].getFill() + right <= tanks[3].getMaxFill() && tanks[4].getFill() + 2 <= tanks[4].getMaxFill();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		for(int i = 0; i < tanks.length; i++)
			tanks[i].readFromNBT(nbt, "tank" + i);
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		for(int i = 0; i < tanks.length; i++)
			tanks[i].writeToNBT(nbt, "tank" + i);
		return nbt;
	}

	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = new AxisAlignedBB(
				pos.getX() - 3,
				pos.getY(),
				pos.getZ() - 3,
				pos.getX() + 4,
				pos.getY() + 16,
				pos.getZ() + 4
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
		return new FluidTankNTM[] {tanks[2], tanks[3], tanks[4]};
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] {tanks[0], tanks[1]};
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
