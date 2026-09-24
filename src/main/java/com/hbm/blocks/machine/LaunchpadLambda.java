package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityLaunchpadLambda;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class LaunchpadLambda extends BlockDummyable {

	public LaunchpadLambda(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		if(meta >= 12) return new TileEntityLaunchpadLambda();
		if(meta >= 6) return new TileEntityProxyCombo().inventory().power().fluid();
		return null;
	}

	@Override
	public int[][] getAllDimensions() {
		return new int[][] {
			new int[] {1, 0, 7, 7, 7, 7},
			new int[] {2, -2, 7, -6, 7, 7},
			new int[] {2, -2, -6, 7, 7, 7},
			new int[] {2, -2, 7, 7, 7, -6},
			new int[] {2, -2, 7, 7, -6, 7},
		};
	}

	@Override public int[] getDimensions() { return new int[] {1, 0, 7, 7, 7, 7}; }
	@Override public int getOffset() { return 7; }

	@Override
	public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
		return this.standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, 0);
	}

	@Override
	public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		if(!super.checkRequirement(world, x, y, z, dir, o)) return false;

		int ix = x + dir.offsetX * o;
		int iz = z + dir.offsetZ * o;

		if(!MultiblockHandlerXR.checkSpace(world, ix, y, iz, new int[] {2, -2, 7, -6, 7, 7}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, ix, y, iz, new int[] {2, -2, -6, 7, 7, 7}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, ix, y, iz, new int[] {2, -2, 7, 7, 7, -6}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, ix, y, iz, new int[] {2, -2, 7, 7, -6, 7}, x, y, z, dir)) return false;

		return true;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		x += dir.offsetX * o;
		z += dir.offsetZ * o;

		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {2, -2, 7, -6, 7, 7}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {2, -2, -6, 7, 7, 7}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {2, -2, 7, 7, 7, -6}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {2, -2, 7, 7, -6, 7}, this, dir);
	}
}
