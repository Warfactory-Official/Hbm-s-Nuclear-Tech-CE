package com.hbm.tileentity.deco;

import com.hbm.blocks.generic.DecoTapeRecorder;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister(name = "tileentity_taperecorder")
public class TileEntityDecoTapeRecorder extends TileEntity {

	private int rot = 0;

	public int getRotation() {

		if(world.getBlockState(pos).getBlock() instanceof DecoTapeRecorder) {

			if(world.isBlockPowered(pos)) {
				rot += 3;
				if(rot >= 360) rot -= 360;
				return rot;
			}

			rot = 0;
		}

		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
