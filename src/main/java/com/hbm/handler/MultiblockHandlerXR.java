package com.hbm.handler;

import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

public class MultiblockHandlerXR {
	
	//when looking north
	//											U  D  N  S  W  E
	public static int[] uni = 		new int[] { 3, 0, 4, 4, 4, 4 };
	
	public static boolean checkSpace(World world, int x, int y, int z, int[] dim, int ox, int oy, int oz, ForgeDirection dir) {
		return checkSpace(world, x, y, z, dim, ox, oy, oz, dir.toEnumFacing());
	}
	
	public static boolean checkSpace(World world, int x, int y, int z, int[] dim, int ox, int oy, int oz, EnumFacing dir) {
		MutableBlockPos pos = new BlockPos.MutableBlockPos();
		if(dim == null || dim.length != 6)
			return false;
		
		int count = 0;
		
		int[] rot = rotate(dim, dir);

		for(int a = x - rot[4]; a <= x + rot[5]; a++) {
			for(int b = y - rot[1]; b <= y + rot[0]; b++) {
				for(int c = z - rot[2]; c <= z + rot[3]; c++) {
					
					//if the position matches the just placed block, the space counts as unoccupied
					if(a == ox && b == oy && c == oz)
						continue;
					
					if(!world.getBlockState(pos.setPos(a, b, c)).getBlock().isReplaceable(world, pos.setPos(a, b, c))) {
						return false;
					}
					
					count++;
					
					if(count > 2000) {
						System.out.println("checkspace: ded " + a + " " + b + " " + c + " " + x + " " + y + " " + z);
						return false;
					}
				}
			}
		}

		AxisAlignedBB aabb = new AxisAlignedBB(
				x - rot[4], y - rot[1], z - rot[2],
				x + rot[5] + 1, y + rot[0] + 1, z + rot[3] + 1);

        return Library.checkForPlayerEyePositions(world,aabb);
    }

	public static void fillSpace(World world, int x, int y, int z, int[] dim, Block block, ForgeDirection dir) {
		fillSpace(world, x, y, z, dim, block, dir.toEnumFacing());
	}
	
	@SuppressWarnings("deprecation")
	public static void fillSpace(World world, int x, int y, int z, int[] dim, Block block, EnumFacing dir) {
		MutableBlockPos pos = new BlockPos.MutableBlockPos();
		if(dim == null || dim.length != 6)
			return;
		
		int count = 0;
		
		int[] rot = rotate(dim, dir);
		
		BlockDummyable.safeRem = true;

		for(int a = x - rot[4]; a <= x + rot[5]; a++) {
			for(int b = y - rot[1]; b <= y + rot[0]; b++) {
				for(int c = z - rot[2]; c <= z + rot[3]; c++) {
					
					int meta = 0;
					
					if(b < y) {
						meta = ForgeDirection.DOWN.ordinal();
					} else if(b > y) {
						meta = ForgeDirection.UP.ordinal();
					} else if(a < x) {
						meta = ForgeDirection.WEST.ordinal();
					} else if(a > x) {
						meta = ForgeDirection.EAST.ordinal();
					} else if(c < z) {
						meta = ForgeDirection.NORTH.ordinal();
					} else if(c > z) {
						meta = ForgeDirection.SOUTH.ordinal();
					} else {
						continue;
					}
					
					world.setBlockState(pos.setPos(a, b, c), block.getStateFromMeta(meta), 3);
					
					count++;
					
					if(count > 2000) {
						System.out.println("fillspace: ded " + a + " " + b + " " + c + " " + x + " " + y + " " + z);
						
						BlockDummyable.safeRem = false;
						return;
					}
				}
			}
		}
		BlockDummyable.safeRem = false;
	}
	
	@Deprecated
	public static void emptySpace(World world, int x, int y, int z, int[] dim, Block block, EnumFacing dir) {
		MutableBlockPos pos = new BlockPos.MutableBlockPos();
		if(dim == null || dim.length != 6)
			return;

		int count = 0;
		
		System.out.println("emptyspace is deprecated and shouldn't even be executed");
		
		int[] rot = rotate(dim, dir);

		for(int a = x - rot[4]; a <= x + rot[5]; a++) {
			for(int b = y - rot[1]; b <= y + rot[0]; b++) {
				for(int c = z - rot[2]; c <= z + rot[3]; c++) {
					
					if(world.getBlockState(pos.setPos(a, b, c)).getBlock() == block)
						world.setBlockToAir(pos.setPos(a, b, c));
					
					count++;
					
					if(count > 2000) {
						System.out.println("emptyspace: ded " + a + " " + b + " " + c);
						return;
					}
				}
			}
		}
	}
	
	public static int[] rotate(int[] dim, EnumFacing dir) {
		
		if(dim == null)
			return null;
		
		if(dir == EnumFacing.SOUTH)
			return dim;
		
		if(dir == EnumFacing.NORTH) {
			//                 U       D       N       S       W       E
			return new int[] { dim[0], dim[1], dim[3], dim[2], dim[5], dim[4] };
		}
		
		if(dir == EnumFacing.EAST) {
			//                 U       D       N       S       W       E
			return new int[] { dim[0], dim[1], dim[5], dim[4], dim[2], dim[3] };
		}
		
		if(dir == EnumFacing.WEST) {
			//                 U       D       N       S       W       E
			return new int[] { dim[0], dim[1], dim[4], dim[5], dim[3], dim[2] };
		}
		
		return dim;
	}

}