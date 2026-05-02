package com.hbm.tileentity.network.energy;

import com.hbm.api.energymk2.Nodespace;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@AutoRegister
public class TileEntityPylonMedium extends TileEntityPylonBase {

	@Override
	public ConnectionType getConnectionType() {
		return ConnectionType.TRIPLE;
	}

	@Override
	public Vec3d[] getMountPos() {
		
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		double height = 7.5D;
		
		return new Vec3d[] {
				new Vec3d(0.5, height, 0.5),
				new Vec3d(0.5 + dir.offsetX, height, 0.5 + dir.offsetZ),
				new Vec3d(0.5 + dir.offsetX * 2, height, 0.5 + dir.offsetZ * 2),
		};
	}

	@Override
	public double getMaxWireLength() {
		return 45;
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return this.hasTransformer() && ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getOpposite() == dir;
	}

	@Override
	public Nodespace.PowerNode createNode() {
		TileEntity tile = this;
		Nodespace.PowerNode node = new Nodespace.PowerNode(new BlockPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ())).setConnections(new DirPos(pos.getX(), pos.getY(), pos.getZ(), ForgeDirection.UNKNOWN));
		for(int[] pos : this.connected) node.addConnection(new DirPos(pos[0], pos[1], pos[2], ForgeDirection.UNKNOWN));
		if(this.hasTransformer()) {
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getOpposite();
			node.addConnection(new DirPos(pos.getX() + dir.offsetX, pos.getY(), pos.getZ() + dir.offsetZ, dir));
		}
		return node;
	}

	public boolean hasTransformer() {
		Block block = this.getBlockType();
		return block == ModBlocks.red_pylon_medium_wood_transformer || block == ModBlocks.red_pylon_medium_steel_transformer;
	}
}
