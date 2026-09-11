package com.hbm.tileentity.machine.pile;

import com.hbm.api.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockMeta;
import com.hbm.blocks.machine.pile.BlockPile;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.machine.pile.TileEntityPileCore.PileChannel;
import com.hbm.tileentity.network.ICachedPipeConnections;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Compat;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;

@AutoRegister
public class TileEntityPileVent extends TileEntityPileDeviceBase implements IFluidStandardReceiverMK2 {

	public FluidTankNTM compair;
	public boolean isActive = false;

	public float fan;
	public float lastFan;

	public TileEntityPileVent() {
		this.compair = new FluidTankNTM(Fluids.AIR, 4_000).withPressure(1);
	}

	@Override public FluidTankNTM[] getAllTanks() { return new FluidTankNTM[] {compair}; }
	@Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] {compair}; }

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		if(type != compair.getTankType()) return false;
		return dir == getOrientation();
	}

	@Override
	public void update() {

		if(!world.isRemote) {

			ForgeDirection dir = getOrientation();
			this.trySubscribe(compair.getTankType(), world, pos.getX() + dir.offsetX, pos.getY(), pos.getZ() + dir.offsetZ, dir);

			this.isActive = false;

			BlockPos inlet = pos.add(-dir.offsetX, 0, -dir.offsetZ);
			IBlockState inletState = world.getBlockState(inlet);

			if(inletState.getBlock() == ModBlocks.pile_block && inletState.getValue(BlockMeta.META) == BlockPile.META_AIR_IN) {
				TileEntity tile = Compat.getTileStandard(world, inlet.getX(), inlet.getY(), inlet.getZ());

				if(tile instanceof TileEntityPileBaseMK2) {
					TileEntityPileBaseMK2 pile = (TileEntityPileBaseMK2) tile;
					TileEntityPileCore core = pile.getCore();

					if(core != null) {
						PileChannel ventChan = core.getVentilationChannel(inlet.getX(), inlet.getY(), inlet.getZ());

						if(ventChan != null) {
							this.chanNum = core.getVentilationChannelNum(ventChan);
							int toFill = BobMathUtil.min(compair.getFill(), PileChannel.MAX_AIR - ventChan.air);
							ventChan.air += toFill;
							this.compair.setFill(this.compair.getFill() - toFill);
							this.isActive = toFill > 0;
						}
					}
				}
			}

			this.networkPackNT(35);

		} else {

			this.lastFan = fan;
			if(this.isActive) {
				this.fan += 45;
				if(world.rand.nextInt(20) == 0) world.spawnParticle(EnumParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0.05, 0);
			}

			if(this.fan >= 360) {
				this.lastFan -= 360;
				this.fan -= 360;
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(this.isActive);
		compair.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.isActive = buf.readBoolean();
		compair.deserialize(buf);
	}

	@Override
	public void deserializeInitial(ByteBuf buf) {
		super.deserializeInitial(buf);
		ForgeDirection dir = getOrientation();
		TileEntity te = world.getTileEntity(pos.add(dir.offsetX, 0, dir.offsetZ));
		if(te instanceof ICachedPipeConnections cached) cached.invalidateConnectionCache();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		compair.readFromNBT(nbt, "t");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		compair.writeToNBT(nbt, "t");
		return nbt;
	}
}
