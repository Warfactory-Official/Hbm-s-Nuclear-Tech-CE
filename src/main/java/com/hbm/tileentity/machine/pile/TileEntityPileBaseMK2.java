package com.hbm.tileentity.machine.pile;

import com.hbm.interfaces.AutoRegister;
import com.hbm.tileentity.TileEntityTickingBase;
import com.hbm.util.Compat;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@AutoRegister
public class TileEntityPileBaseMK2 extends TileEntityTickingBase {

	public TileEntityPileCore cachedCore;
	public int coreX;
	public int coreY = -999;
	public int coreZ;

	@Override
	public String getInventoryName() {
		return null;
	}


	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	public TileEntityPileBaseMK2 setCore(int x, int y, int z) {
		this.coreX = x;
		this.coreY = y;
		this.coreZ = z;
		return this;
	}

	@Override
	public void update() {

		if(!world.isRemote) {
			if(coreY >= 0) {

				TileEntityPileCore controller = getCore();

				if((controller == null || controller.isInvalid()) && world.isBlockLoaded(new BlockPos(coreX, coreY, coreZ))) {
					this.getBlockType().breakBlock(world, pos, world.getBlockState(pos));
				}
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		coreX = nbt.getInteger("cX");
		coreY = nbt.getInteger("cY");
		coreZ = nbt.getInteger("cZ");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("cX", coreX);
		nbt.setInteger("cY", coreY);
		nbt.setInteger("cZ", coreZ);
		return nbt;
	}

	@Override
	public void markDirty() {
		if(this.world != null) {
			this.world.markChunkDirty(this.pos, this);
		}
	}

	public TileEntityPileCore getCore() {
		if(cachedCore != null && !cachedCore.isInvalid()) return cachedCore;

		BlockPos corePos = new BlockPos(coreX, coreY, coreZ);

		if(world.isBlockLoaded(corePos)) {

			TileEntity tile = Compat.getTileStandard(world, coreX, coreY, coreZ);
			if(tile instanceof TileEntityPileCore) {
				TileEntityPileCore core = (TileEntityPileCore) tile;
				cachedCore = core;
				return core;
			}
		}
		return null;
	}
}
