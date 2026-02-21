package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.Watz;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityWatzStruct extends TileEntity implements ITickable {

	
	@Override
	public void update() {
		
		if(world.isRemote) return;
		if(world.getTotalWorldTime() % 20 != 0) return;

		/*
		 * skeptics may say that his is shit. i don't necessarily disagree, but it was both easy and quick to do
		 * and it remains readable and not terribly long, so who the fuck cares.
		 * Th3_Sl1ze: I don't give a fuck about my miserable life, so why should I care about this mess..
		 */
		if(!cbr(ModBlocks.watz_cooler, 0, 1, 0)) return;
		if(!cbr(ModBlocks.watz_cooler, 0, 2, 0)) return;
		
		for(int i = 0; i < 3; i++) {
			if(!cbr(ModBlocks.watz_element, 1, i, 0)) return;
			if(!cbr(ModBlocks.watz_element, 2, i, 0)) return;
			if(!cbr(ModBlocks.watz_element, 0, i, 1)) return;
			if(!cbr(ModBlocks.watz_element, 0, i, 2)) return;
			if(!cbr(ModBlocks.watz_element, -1, i, 0)) return;
			if(!cbr(ModBlocks.watz_element, -2, i, 0)) return;
			if(!cbr(ModBlocks.watz_element, 0, i, -1)) return;
			if(!cbr(ModBlocks.watz_element, 0, i, -2)) return;
			if(!cbr(ModBlocks.watz_element, 1, i, 1)) return;
			if(!cbr(ModBlocks.watz_element, 1, i, -1)) return;
			if(!cbr(ModBlocks.watz_element, -1, i, 1)) return;
			if(!cbr(ModBlocks.watz_element, -1, i, -1)) return;
			if(!cbr(ModBlocks.watz_cooler, 2, i, 1)) return;
			if(!cbr(ModBlocks.watz_cooler, 2, i, -1)) return;
			if(!cbr(ModBlocks.watz_cooler, 1, i, 2)) return;
			if(!cbr(ModBlocks.watz_cooler, -1, i, 2)) return;
			if(!cbr(ModBlocks.watz_cooler, -2, i, 1)) return;
			if(!cbr(ModBlocks.watz_cooler, -2, i, -1)) return;
			if(!cbr(ModBlocks.watz_cooler, 1, i, -2)) return;
			if(!cbr(ModBlocks.watz_cooler, -1, i, -2)) return;
			
			for(int j = -1; j < 2; j++) {
				if(!cbr(ModBlocks.watz_casing, 1, 3, i, j)) return;
				if(!cbr(ModBlocks.watz_casing, 1, j, i, 3)) return;
				if(!cbr(ModBlocks.watz_casing, 1, -3, i, j)) return;
				if(!cbr(ModBlocks.watz_casing, 1, j, i, -3)) return;
			}
			if(!cbr(ModBlocks.watz_casing, 1, 2, i, 2)) return;
			if(!cbr(ModBlocks.watz_casing, 1, 2, i, -2)) return;
			if(!cbr(ModBlocks.watz_casing, 1, -2, i, 2)) return;
			if(!cbr(ModBlocks.watz_casing, 1, -2, i, -2)) return;
		}
		
		Watz watz = (Watz)ModBlocks.watz;
		BlockDummyable.safeRem = true;
		world.setBlockState(pos, ModBlocks.watz.getStateFromMeta(12), 3);
		watz.fillSpace(world, pos.getX(), pos.getY(), pos.getZ(), ForgeDirection.NORTH, 0);
		BlockDummyable.safeRem = false;
	}

	/** [G]et [B]lock at [R]elative position */
	private Block gbr(int x, int y, int z) {
		return world.getBlockState(pos.add(x, y, z)).getBlock();
	}

	/** [G]et [M]eta at [R]elative position */
	private int gmr(int x, int y, int z) {
		return gbr(x, y, z).getMetaFromState(world.getBlockState(pos.add(x, y, z)));
	}
	
	/** [C]heck [B]lock at [R]elative position */
	private boolean cbr(Block b, int x, int y, int z) {
		return b == gbr(x, y, z);
	}
	private boolean cbr(Block b, int meta, int x, int y, int z) {
		return b == gbr(x, y, z) && meta == gmr(x, y, z);
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
					pos.getY() + 3,
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
}
