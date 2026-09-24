package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityLaunchpadSoyuz;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class LaunchpadSoyuz extends BlockDummyable {

	public LaunchpadSoyuz(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		if(meta >= 12) return new TileEntityLaunchpadSoyuz();
		if(meta >= 6) return new TileEntityProxyCombo().power().fluid().inventory();
		return null;
	}

	@Override
	public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
		return this.standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, 0);
	}

	@Override public int getOffset() { return 2; }
	@Override public int[] getDimensions() { return new int[] {2, 0, 2, 2, 2, 2}; }

	@Override
	public int[][] getAllDimensions() {
		return new int[][] {
			new int[] {2, 0, 2, 2, 2, 2},
			new int[] {3, 0, 2, 1, 1, 2},

			new int[] {2, -2, 2, 2, -2, 10},
			new int[] {2, -2, 10, -2, 2, 10},
			new int[] {3, -3, 9, 1, 1, 9},
			new int[] {1, 0, 2, 2, -6, 10},
			new int[] {1, 0, 10, -6, 2, 2},
			new int[] {1, 0, 10, -6, -6, 10},

			new int[] {0, 0, 0, 0, -10, 58,   2, 0, 0},
			new int[] {1, -1, 0, 0, -56, 58,   2, 0, 0},
			new int[] {0, 0, 0, 0, -10, 58,   -10, 0, 0},
			new int[] {1, -1, 0, 0, -56, 58,   -10, 0, 0},
			
			new int[] {2, 0, 2, 1, 7, -3},
			new int[] {2, 0, 2, 1, 7, -3,   -6, 0, 0},
			new int[] {0, 0, 1, 1, 7, -3,   -4, 2, 0},
			new int[] {1, -1, 5, 5, 7, -3,   -4, 2, 0},
			new int[] {3, -2, 4, 4, 7, -3,   -4, 2, 0},
			new int[] {6, -4, 3, 3, 7, -3,   -4, 2, 0},
			new int[] {51, -7, 2, 2, 7, -3,   -4, 2, 0},

			new int[] {7, 0, -2, 3, 2, 2,   0, 0, 5},
		};
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
		
		x += dir.offsetX * o;
		z += dir.offsetZ * o;
		
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {3, 0, 2, 1, 1, 2}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y + 2, z, new int[] {0, 0, 2, 2, -2, 10}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y + 2, z, new int[] {0, 0, 10, -2, 2, 10}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {3, -3, 9, 1, 1, 9}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y + 2, z, new int[] {-1, 2, 2, 2, -6, 10}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y + 2, z, new int[] {-1, 2, 10, -6, 2, 2}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y + 2, z, new int[] {-1, 2, 10, -6, -6, 10}, this, dir);

		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * 2, y, z + dir.offsetZ * 2, new int[] {0, 0, 0, 0, -10, 58}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * 2, y, z + dir.offsetZ * 2, new int[] {1, -1, 0, 0, -56, 58}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - dir.offsetX * 10, y, z - dir.offsetZ * 10, new int[] {0, 0, 0, 0, -10, 58}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - dir.offsetX * 10, y, z - dir.offsetZ * 10, new int[] {1, -1, 0, 0, -56, 58}, this, dir);

		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {2, 0, 2, 1, 7, -3}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - dir.offsetX * 7, y, z - dir.offsetZ * 7, new int[] {2, 0, 2, 1, 7, -3}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - dir.offsetX * 4, y + 2, z - dir.offsetZ * 4, new int[] {0, 0, 1, 1, 7, -3}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - dir.offsetX * 4, y + 2, z - dir.offsetZ * 4, new int[] {1, -1, 5, 5, 7, -3}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - dir.offsetX * 4, y + 2, z - dir.offsetZ * 4, new int[] {3, -2, 4, 4, 7, -3}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - dir.offsetX * 4, y + 2, z - dir.offsetZ * 4, new int[] {6, -4, 3, 3, 7, -3}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - dir.offsetX * 4, y + 2, z - dir.offsetZ * 4, new int[] {51, -7, 2, 2, 7, -3}, this, dir);

		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		MultiblockHandlerXR.fillSpace(world, x + rot.offsetX * 5, y, z + rot.offsetZ * 5, new int[] {7, 0, -2, 3, 2, 2}, this, dir);
	}

	@Override
	public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		if(!super.checkRequirement(world, x, y, z, dir, o)) return false;
		
		int ix = x + dir.offsetX * o;
		int iz = z + dir.offsetZ * o;

		if(!MultiblockHandlerXR.checkSpace(world, ix, y, iz, new int[] {3, 0, 2, 1, 1, 2}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, ix, y + 2, iz, new int[] {0, 0, 2, 2, -2, 10}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, ix, y + 2, iz, new int[] {0, 0, 10, -2, 2, 10}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, ix, y, iz, new int[] {3, -3, 9, 1, 1, 9}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, ix, y + 2, iz, new int[] {-1, 2, 2, 2, -6, 10}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, ix, y + 2, iz, new int[] {-1, 2, 10, -6, 2, 2}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, ix, y + 2, iz, new int[] {-1, 2, 10, -6, -6, 10}, x, y, z, dir)) return false;

		if(!MultiblockHandlerXR.checkSpace(world, ix + dir.offsetX * 2, y, iz + dir.offsetZ * 2, new int[] {0, 0, 0, 0, -10, 58}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, ix + dir.offsetX * 2, y, iz + dir.offsetZ * 2, new int[] {1, -1, 0, 0, -56, 58}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, ix - dir.offsetX * 10, y, iz - dir.offsetZ * 10, new int[] {0, 0, 0, 0, -10, 58}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, ix - dir.offsetX * 10, y, iz - dir.offsetZ * 10, new int[] {1, -1, 0, 0, -56, 58}, x, y, z, dir)) return false;

		if(!MultiblockHandlerXR.checkSpace(world, ix, y, iz, new int[] {2, 0, 2, 1, 7, -3}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, ix - dir.offsetX * 7, y, iz - dir.offsetZ * 7, new int[] {2, 0, 2, 1, 7, -3}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, ix - dir.offsetX * 4, y + 2, iz - dir.offsetZ * 4, new int[] {0, 0, 1, 1, 7, -3}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, ix - dir.offsetX * 4, y + 2, iz - dir.offsetZ * 4, new int[] {1, -1, 5, 5, 7, -3}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, ix - dir.offsetX * 4, y + 2, iz - dir.offsetZ * 4, new int[] {3, -2, 4, 4, 7, -3}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, ix - dir.offsetX * 4, y + 2, iz - dir.offsetZ * 4, new int[] {6, -4, 3, 3, 7, -3}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, ix - dir.offsetX * 4, y + 2, iz - dir.offsetZ * 4, new int[] {51, -7, 2, 2, 7, -3}, x, y, z, dir)) return false;

		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		if(!MultiblockHandlerXR.checkSpace(world, ix + rot.offsetX * 5, y, iz + rot.offsetZ * 5, new int[] {7, 0, -2, 3, 2, 2}, x, y, z, dir)) return false;
		
		return true;
	}
}
