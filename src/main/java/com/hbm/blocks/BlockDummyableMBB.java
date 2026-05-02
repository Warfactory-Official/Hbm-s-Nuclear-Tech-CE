package com.hbm.blocks;

import com.hbm.handler.MultiblockBBHandler;
import com.hbm.handler.MultiblockBBHandler.MultiblockBounds;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BlockDummyableMBB extends BlockDummyable {

	public BlockDummyableMBB(Material materialIn, String s) {
		super(materialIn, s);
	}
	
	@Override
	public boolean checkRequirement(World world, int xx, int yy, int zz, ForgeDirection dir, int o) {
		MultiblockBounds b = MultiblockBBHandler.REGISTRY.get(this);
		Map<BlockPos, List<AxisAlignedBB>> blocks = new HashMap<>();
		for(AxisAlignedBB unrotatedBox : b.boxes){
			AxisAlignedBB box = rotate(unrotatedBox, dir.toEnumFacing());
			int x1 = MathHelper.floor(box.minX);
			int x2 = MathHelper.ceil(box.maxX);
			int y1 = MathHelper.floor(box.minY);
			int y2 = MathHelper.ceil(box.maxY);
			int z1 = MathHelper.floor(box.minZ);
			int z2 = MathHelper.ceil(box.maxZ);

			for(int x = x1; x <= x2; x++) {
				for(int y = y1; y <= y2; y++) {
					for(int z = z1; z <= z2; z++) {
						BlockPos pos = new BlockPos(x, y, z);
                        List<AxisAlignedBB> blockBBs = blocks.computeIfAbsent(pos, k -> new ArrayList<>());
                        AxisAlignedBB blockBB = clampToPos(box, pos).offset(-pos.getX(), -pos.getY(), -pos.getZ());
						if(volume(blockBB) == 0){
							if(blockBBs.isEmpty())
								blocks.remove(pos);
                        } else if(FULL_BLOCK_AABB.equals(blockBB)){
							blockBBs.add(FULL_BLOCK_AABB);
						} else {
							blockBBs.add(blockBB);
						}
					}
				}
			}
		}
		int minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
		for(BlockPos pos : blocks.keySet()){
			BlockPos pos1 = pos.add(xx, yy, zz);
			if(pos1.getX() == xx && pos1.getY() == yy && pos1.getZ() == zz)
				continue;
			if(!world.getBlockState(pos1).getBlock().canPlaceBlockAt(world, pos1)) {
				return false;
			}
			minX = Math.min(minX, pos.getX());
			minY = Math.min(minY, pos.getY());
			minZ = Math.min(minZ, pos.getZ());
			maxX = Math.max(maxX, pos.getX());
			maxY = Math.max(maxY, pos.getY());
			maxZ = Math.max(maxZ, pos.getZ());
		}
		AxisAlignedBB aabb = new AxisAlignedBB(
				minX + xx, minY + yy, minZ + zz,
				maxX + xx + 1, maxY + yy + 1, maxZ + zz + 1);
        return Library.checkForPlayerEyePositions(world,aabb);
    }
	
	@Override
	protected void fillSpace(World world, int xxx, int yyy, int zzz, ForgeDirection dir, int o) {
		int xx = xxx + dir.offsetX * o;
		int yy = yyy + dir.offsetY * o;
		int zz = zzz + dir.offsetZ * o;
		BlockDummyable.safeRem = true;
		MultiblockBounds bbounds = MultiblockBBHandler.REGISTRY.get(this);
		Map<BlockPos, List<AxisAlignedBB>> blocks = new HashMap<>();
		for(AxisAlignedBB unrotatedBox : bbounds.boxes){
			AxisAlignedBB box = rotate(unrotatedBox, dir.toEnumFacing());
			int x1 = MathHelper.floor(box.minX);
			int x2 = MathHelper.ceil(box.maxX);
			int y1 = MathHelper.floor(box.minY);
			int y2 = MathHelper.ceil(box.maxY);
			int z1 = MathHelper.floor(box.minZ);
			int z2 = MathHelper.ceil(box.maxZ);

			for(int x = x1; x <= x2; x++) {
				for(int y = y1; y <= y2; y++) {
					for(int z = z1; z <= z2; z++) {
						BlockPos pos = new BlockPos(x, y, z);
						List<AxisAlignedBB> blockBBs = blocks.get(pos);
						if(blockBBs == null){
							blockBBs = new ArrayList<>();
							blocks.put(pos, blockBBs);
						}
						AxisAlignedBB blockBB = clampToPos(box, pos).offset(-pos.getX(), -pos.getY(), -pos.getZ());
						if(volume(blockBB) == 0){
							if(blockBBs.size() == 0)
								blocks.remove(pos);
							continue;
						} else if(FULL_BLOCK_AABB.equals(blockBB)){
							blockBBs.add(FULL_BLOCK_AABB);
						} else {
							blockBBs.add(blockBB);
						}
					}
				}
			}
		}
		for(BlockPos pos : blocks.keySet()){
			BlockPos pos1 = pos.add(xx, yy, zz);
			int meta = 0;
			int a = pos1.getX();
			int b = pos1.getY();
			int c = pos1.getZ();
			
			if(b < yy) {
				meta = ForgeDirection.DOWN.ordinal();
			} else if(b > yy) {
				meta = ForgeDirection.UP.ordinal();
			} else if(a < xx) {
				meta = ForgeDirection.WEST.ordinal();
			} else if(a > xx) {
				meta = ForgeDirection.EAST.ordinal();
			} else if(c < zz) {
				meta = ForgeDirection.NORTH.ordinal();
			} else if(c > zz) {
				meta = ForgeDirection.SOUTH.ordinal();
			} else {
				continue;
			}
			world.setBlockState(pos1, this.getStateFromMeta(meta), 3);
		}
		BlockDummyable.safeRem = false;
	}
	
	public AxisAlignedBB clampToPos(AxisAlignedBB box, BlockPos pos) {
		AxisAlignedBB b = new AxisAlignedBB(MathHelper.clamp(box.minX, pos.getX(), pos.getX() + 1),
				MathHelper.clamp(box.minY, pos.getY(), pos.getY() + 1),
				MathHelper.clamp(box.minZ, pos.getZ(), pos.getZ() + 1),
				MathHelper.clamp(box.maxX, pos.getX(), pos.getX() + 1),
				MathHelper.clamp(box.maxY, pos.getY(), pos.getY() + 1),
				MathHelper.clamp(box.maxZ, pos.getZ(), pos.getZ() + 1));
		return b;
	}
	
	public double volume(AxisAlignedBB box){
		return (box.maxX-box.minX)*(box.maxY-box.minY)*(box.maxZ-box.minZ);
	}
	
	public static AxisAlignedBB rotate(AxisAlignedBB box, EnumFacing dir) {
		box = box.offset(-0.5, 0, -0.5);
		if(box == null)
			return null;
		
		if(dir == EnumFacing.SOUTH)
			return new AxisAlignedBB(box.minZ, box.minY, -box.minX, box.maxZ, box.maxY, -box.maxX).offset(0.5, 0, 0.5);
		
		if(dir == EnumFacing.NORTH) {
			return new AxisAlignedBB(-box.minZ, box.minY, box.minX, -box.maxZ, box.maxY, box.maxX).offset(0.5, 0, 0.5);
		}
		
		if(dir == EnumFacing.EAST) {
			return new AxisAlignedBB(-box.minX, box.minY, -box.minZ, -box.maxX, box.maxY, -box.maxZ).offset(0.5, 0, 0.5);
		}
		
		if(dir == EnumFacing.WEST) {
			return box.offset(0.5, 0, 0.5);
		}
		
		return box.offset(0.5, 0, 0.5);
	}
}
