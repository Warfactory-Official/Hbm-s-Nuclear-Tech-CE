package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.machine.TileEntityMachineLPW2;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class MachineLPW2 extends BlockDummyable {

	public MachineLPW2(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		if(meta >= 12) return new TileEntityMachineLPW2();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {6, 0, 3, 3, 9, 10};
	}

	@Override
	public int getOffset() {
		return 3;
	}
}
