package com.hbm.tileentity.network.energy;

import com.hbm.blocks.BlockDummyable;
import com.hbm.interfaces.AutoRegister;
import com.hbm.util.Vec3NT;
import net.minecraft.util.math.Vec3d;

@AutoRegister
public class TileEntityPylonLarge extends TileEntityPylonBase {

	@Override
	public ConnectionType getConnectionType() {
		return ConnectionType.QUAD;
	}

	@Override
	public Vec3d[] getMountPos() {
		double topOff = 0.75D + 0.0625D;
		double sideOff = 3.375D;

		Vec3NT vec = new Vec3NT(sideOff, 0, 0);
        int meta = getBlockMetadata() - BlockDummyable.offset;
		switch (meta) {
			case 4 -> vec.rotateAroundYRad((float) Math.PI * 0.25F);
            case 3 -> vec.rotateAroundYRad((float) Math.PI * 0.5F);
            case 5 -> vec.rotateAroundYRad((float) Math.PI * 0.75F);
            default -> vec.rotateAroundYRad((float) Math.PI * 0.0F);
        }
		
		return new Vec3d[] {
				new Vec3d(0.5D + vec.x, 11.5D + topOff, 0.5D + vec.z),
				new Vec3d(0.5D + vec.x, 11.5D - topOff, 0.5D + vec.z),
				new Vec3d(0.5D - vec.x, 11.5D + topOff, 0.5D - vec.z),
				new Vec3d(0.5D - vec.x, 11.5D - topOff, 0.5D - vec.z),
		};
	}

	@Override
	public double getMaxWireLength() {
		return 100;
	}
}