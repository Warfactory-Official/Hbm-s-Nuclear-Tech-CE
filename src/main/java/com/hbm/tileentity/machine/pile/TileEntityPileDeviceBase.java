package com.hbm.tileentity.machine.pile;

import com.hbm.blocks.generic.BlockMeta;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityTickingBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class TileEntityPileDeviceBase extends TileEntityTickingBase {

	public int chanNum;

	@Override
	public String getInventoryName() {
		return null;
	}

	public ForgeDirection getOrientation() {
		return ForgeDirection.getOrientation(this.getBlockMetadata() % 4 + 2);
	}


	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		if(oldState.getBlock() != newState.getBlock()) return true;
		return oldState.getValue(BlockMeta.META) / 4 != newState.getValue(BlockMeta.META) / 4;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.chanNum);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.chanNum = buf.readInt();
	}
}
