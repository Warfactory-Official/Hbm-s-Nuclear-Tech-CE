package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityMultiblock extends TileEntity implements ITickable {

	@Override
	public void update() {
		if(!world.isRemote) {
			
			if(this.getBlockType() == ModBlocks.struct_launcher_core && isCompact()) {
				buildCompact();
			}
			
			if(this.getBlockType() == ModBlocks.struct_launcher_core_large) {
				
				EnumFacing meta = isTable();
				
				if(meta != null)
					buildTable(meta);
			}
		}
	}
	
	private boolean isCompact() {
		MutableBlockPos mPos = new BlockPos.MutableBlockPos();
		for(int i = -1; i <= 1; i++)
			for(int j = -1; j <= 1; j++)
				if(!(i == 0 && j == 0))
					if(world.getBlockState(mPos.setPos(pos.getX() + i, pos.getY(), pos.getZ() + j)).getBlock() != ModBlocks.struct_launcher)
						return false;
		
		return true;
	}
	
	private EnumFacing isTable() {
		MutableBlockPos mPos = new BlockPos.MutableBlockPos();
		for(int i = -4; i <= 4; i++)
			for(int j = -4; j <= 4; j++)
				if(!(i == 0 && j == 0))
					if(world.getBlockState(mPos.setPos(pos.getX() + i, pos.getY(), pos.getZ() + j)).getBlock() != ModBlocks.struct_launcher)
						return null;
		
		boolean flag = true;
		
		for(int k = 1; k < 12; k++) {
			if(world.getBlockState(mPos.setPos(pos.getX(), pos.getY() + k, pos.getZ()+3)).getBlock() != ModBlocks.struct_scaffold)
				flag = false;
		}
		
		if(flag)
			return EnumFacing.NORTH;
		flag = true;
		
		for(int k = 1; k < 12; k++) {
			if(world.getBlockState(mPos.setPos(pos.getX() - 3, pos.getY() + k, pos.getZ())).getBlock() != ModBlocks.struct_scaffold)
				flag = false;
		}
		
		if(flag)
			return EnumFacing.EAST;
		flag = true;
		
		for(int k = 1; k < 12; k++) {
			if(world.getBlockState(mPos.setPos(pos.getX(), pos.getY() + k, pos.getZ() - 3)).getBlock() != ModBlocks.struct_scaffold)
				flag = false;
		}
		
		if(flag)
			return EnumFacing.SOUTH;
		flag = true;
		
		for(int k = 1; k < 12; k++) {
			if(world.getBlockState(mPos.setPos(pos.getX()+3, pos.getY() + k, pos.getZ())).getBlock() != ModBlocks.struct_scaffold)
				flag = false;
		}
		
		if(flag)
			return EnumFacing.WEST;
		
		return null;
		
	}
	
	private void buildCompact() {
		
		world.setBlockState(pos, ModBlocks.compact_launcher.getDefaultState());

		placeDummy(pos.getX() + 1, pos.getY(), pos.getZ() + 1, pos, ModBlocks.dummy_port_compact_launcher);
		placeDummy(pos.getX() + 1, pos.getY(), pos.getZ(), pos, ModBlocks.dummy_plate_compact_launcher);
		placeDummy(pos.getX() + 1, pos.getY(), pos.getZ() - 1, pos, ModBlocks.dummy_port_compact_launcher);
		placeDummy(pos.getX(), pos.getY(), pos.getZ() - 1, pos, ModBlocks.dummy_plate_compact_launcher);
		placeDummy(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos, ModBlocks.dummy_port_compact_launcher);
		placeDummy(pos.getX() - 1, pos.getY(), pos.getZ(), pos, ModBlocks.dummy_plate_compact_launcher);
		placeDummy(pos.getX() - 1, pos.getY(), pos.getZ() + 1, pos, ModBlocks.dummy_port_compact_launcher);
		placeDummy(pos.getX(), pos.getY(), pos.getZ() + 1, pos, ModBlocks.dummy_plate_compact_launcher);
	}
	
	private void buildTable(EnumFacing meta) {
		
		world.setBlockState(pos, ModBlocks.launch_table.getDefaultState().withProperty(BlockHorizontal.FACING, meta), 2);
		
		switch(meta) {
		case NORTH:
			for(int i = 1; i < 12; i++)
				world.setBlockState(pos.add(0, i, 3), Blocks.AIR.getDefaultState());
			
			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(pos.getX() + i, pos.getY(), pos.getZ(), pos, ModBlocks.dummy_port_launch_table);
			
			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(pos.getX(), pos.getY(), pos.getZ() + i, pos, ModBlocks.dummy_plate_launch_table);
			
			break;
			
		case EAST:
			for(int i = 1; i < 12; i++)
				world.setBlockState(pos.add(-3, i, 0), Blocks.AIR.getDefaultState());
			
			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(pos.getX() + i, pos.getY(), pos.getZ(), pos, ModBlocks.dummy_port_launch_table);
			
			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(pos.getX(), pos.getY(), pos.getZ() + i, pos, ModBlocks.dummy_plate_launch_table);
			
			break;
			
		case SOUTH:
			for(int i = 1; i < 12; i++)
				world.setBlockState(pos.add(0, i, -3), Blocks.AIR.getDefaultState());
			
			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(pos.getX() + i, pos.getY(), pos.getZ(), pos, ModBlocks.dummy_plate_launch_table);
			
			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(pos.getX(), pos.getY(), pos.getZ() + i, pos, ModBlocks.dummy_port_launch_table);
			
			break;
			
		case WEST:
			for(int i = 1; i < 12; i++)
				world.setBlockState(pos.add(3, i, 0), Blocks.AIR.getDefaultState());
			
			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(pos.getX() + i, pos.getY(), pos.getZ(), pos, ModBlocks.dummy_plate_launch_table);
			
			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(pos.getX(), pos.getY(), pos.getZ() + i, pos, ModBlocks.dummy_port_launch_table);
			
			break;
		default:
			break;
		}

		for(int i = -4; i <= 4; i++)
			for(int j = -4; j <= 4; j++)
				if(i != 0 && j != 0)
					placeDummy(pos.getX() + i, pos.getY(), pos.getZ() + j, pos, ModBlocks.dummy_port_launch_table);
					
		
	}
	
	private void placeDummy(int x, int y, int z, BlockPos target, Block block) {
		BlockPos pos = new BlockPos(x, y, z);
		world.setBlockState(pos, block.getDefaultState());
		
		TileEntity te = world.getTileEntity(pos);
		
		if(te instanceof TileEntityDummy) {
			TileEntityDummy dummy = (TileEntityDummy)te;
			dummy.target = target;
		}
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}
}
