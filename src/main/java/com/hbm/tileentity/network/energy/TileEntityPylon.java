package com.hbm.tileentity.network.energy;

import com.hbm.api.energymk2.Nodespace;
import com.hbm.blocks.BlockDummyable;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@AutoRegister
public class TileEntityPylon extends TileEntityPylonBase {

	@Override
	public void update() {
		if(!world.isRemote && this.getBlockMetadata() == 0) {
			world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockDummyable.META, 12), 3);
			this.updateContainingBlockInfo();
		}
		super.update();
	}

	@Override
	public ConnectionType getConnectionType() {
		return ConnectionType.SINGLE;
	}

	@Override
	public Vec3d[] getMountPos() {
		return new Vec3d[]{new Vec3d(0.5D, 5.5D, 0.5D)};
	}

	@Override
	public double getMaxWireLength() {
		return 25D;
	}

	@Override
	public Nodespace.PowerNode createNode() {
		TileEntity tile = (TileEntity) this;
		Nodespace.PowerNode node = new Nodespace.PowerNode(new BlockPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ())).setConnections(
				new DirPos(pos.getX(), pos.getY(), pos.getZ(), ForgeDirection.UNKNOWN),
				new DirPos(pos.getX() + 1, pos.getY(), pos.getZ(), Library.POS_X),
				new DirPos(pos.getX() - 1, pos.getY(), pos.getZ(), Library.NEG_X),
				new DirPos(pos.getX(), pos.getY() + 1, pos.getZ(), Library.POS_Y),
				new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y),
				new DirPos(pos.getX(), pos.getY(), pos.getZ() + 1, Library.POS_Z),
				new DirPos(pos.getX(), pos.getY(), pos.getZ() - 1, Library.NEG_Z)
		);
		for(int[] pos : this.connected) node.addConnection(new DirPos(pos[0], pos[1], pos[2], ForgeDirection.UNKNOWN));
		return node;
	}
}
