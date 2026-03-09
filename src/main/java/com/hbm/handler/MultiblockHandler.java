package com.hbm.handler;

import com.hbm.interfaces.IDummy;
import com.hbm.tileentity.machine.TileEntityDummy;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

public class MultiblockHandler {

	//Approved!
	//pos x, neg x, pos y, neg y, pos z, neg z
	public static final int[] uf6Dimension  = new int[] { 0, 0, 1, 0, 0, 0 };
	
	//Approved!
	public static boolean checkSpace(World world, BlockPos pos, int[] i) {
		boolean placable = true;
		MutableBlockPos replace = new BlockPos.MutableBlockPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		for(int a = x - i[1]; a <= x + i[0]; a++) {
			for(int b = y - i[3]; b <= y + i[2]; b++) {
				for(int c = z - i[5]; c <= z + i[4]; c++) {
					if(!(a == x && b == y && c == z)) {
						Block block = world.getBlockState(replace.setPos(a, b, c)).getBlock();
						if(block != Blocks.AIR && !block.isReplaceable(world, replace)) {
							placable = false;
						}
					}
				}
			}
		}
		
		return placable;
	}
	
	public static boolean fillUp(World world, BlockPos pos, int[] i, Block block) {
		boolean placable = true;
		MutableBlockPos replace = new BlockPos.MutableBlockPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		for(int a = x - i[1]; a <= x + i[0]; a++) {
			for(int b = y - i[3]; b <= y + i[2]; b++) {
				for(int c = z - i[5]; c <= z + i[4]; c++) {
					if(!(a == x && b == y && c == z)) {
						world.setBlockState(replace.setPos(a, b, c), block.getDefaultState());
						TileEntity te = world.getTileEntity(replace.setPos(a, b, c));
						if(te instanceof TileEntityDummy dummy) {
                            dummy.target = pos;
						}
					}
				}
			}
		}
		
		return placable;
	}
	
	public static boolean removeAll(World world, BlockPos pos, int[] i) {
		boolean placable = true;
		
		MutableBlockPos replace = new BlockPos.MutableBlockPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		
		for(int a = x - i[1]; a <= x + i[0]; a++) {
			for(int b = y - i[3]; b <= y + i[2]; b++) {
				for(int c = z - i[5]; c <= z + i[4]; c++) {
					if(!(a == x && b == y && c == z)) {
						if(world.getBlockState(replace.setPos(a, b, c)).getBlock() instanceof IDummy)
							if(!world.isRemote) {
								world.destroyBlock(replace.setPos(a, b, c), false);
							}
					}
				}
			}
		}
		
		return placable;
	}
}
