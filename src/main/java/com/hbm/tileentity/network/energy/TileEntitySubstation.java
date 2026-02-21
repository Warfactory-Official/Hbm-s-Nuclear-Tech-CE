package com.hbm.tileentity.network.energy;

import com.hbm.api.energymk2.Nodespace;
import com.hbm.blocks.BlockDummyable;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.util.Vec3NT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@AutoRegister
public class TileEntitySubstation extends TileEntityPylonBase {

	@Override
	public ConnectionType getConnectionType() {
		return ConnectionType.QUAD;
	}

	@Override
	public Vec3d[] getMountPos() {

		double topOff = 5.25D;
        Vec3NT vec = new Vec3NT(1, 0, 0);

        int meta = getBlockMetadata() - BlockDummyable.offset; // if you want to ask me "why" - judging by new torex' behaviour, it needs to be cached in a local int
        switch (meta) {
			case 4, 5 -> vec.rotateAroundYRad((float) Math.PI * 0.5F);
            default -> vec.rotateAroundYRad((float) Math.PI * 0.0F);
        }

		return new Vec3d[] {
				new Vec3d(0.5D + vec.x * 0.5D, topOff, 0.5D + vec.z * 0.5D),
				new Vec3d(0.5D + vec.x * 1.5D, topOff, 0.5D + vec.z * 1.5D),
				new Vec3d(0.5D - vec.x * 0.5D, topOff, 0.5D - vec.z * 0.5D),
				new Vec3d(0.5D - vec.x * 1.5D, topOff, 0.5D - vec.z * 1.5D),
		};
	}

	@Override
	public Vec3d getConnectionPoint() {
		return new Vec3d(pos.getX() + 0.5, pos.getY() + 5.25, pos.getZ() + 0.5);
	}

	@Override
	public double getMaxWireLength() {
		return 20;
	}

	@Override
	public Nodespace.PowerNode createNode() {
		TileEntity tile = this;
		Nodespace.PowerNode node = new Nodespace.PowerNode(new BlockPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ()),
				new BlockPos(tile.getPos().getX() + 1, tile.getPos().getY(), tile.getPos().getZ() + 1),
				new BlockPos(tile.getPos().getX() + 1, tile.getPos().getY(), tile.getPos().getZ() - 1),
				new BlockPos(tile.getPos().getX() - 1, tile.getPos().getY(), tile.getPos().getZ() + 1),
				new BlockPos(tile.getPos().getX() - 1, tile.getPos().getY(), tile.getPos().getZ() - 1)).setConnections(
				new DirPos(pos.getX(), pos.getY(), pos.getZ(), ForgeDirection.UNKNOWN),
				new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() - 1, Library.POS_X),
				new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() + 1, Library.POS_X),
				new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() - 1, Library.NEG_X),
				new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() + 1, Library.NEG_X),
				new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() + 2, Library.POS_Z),
				new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() + 2, Library.POS_Z),
				new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() - 2, Library.NEG_Z),
				new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() - 2, Library.NEG_Z)
		);
		for(int[] pos : this.connected) node.addConnection(new DirPos(pos[0], pos[1], pos[2], ForgeDirection.UNKNOWN));
		return node;
	}
}
