package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IBomb;
import com.hbm.tileentity.machine.TileEntityHatch;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSeal extends Block implements IBomb {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyBool ACTIVATED = PropertyBool.create("activated");
	
	public BlockSeal(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking()) {
			int i = BlockSeal.getFrameSize(world, pos);
			
			if(i != 0)
				if(BlockSeal.isSealClosed(world, pos, i))
					BlockSeal.openSeal(world, pos, i);
				else
					BlockSeal.closeSeal(world, pos, i);
			
			return true;
		} else {
			return false;
		}
	}
	
	public static int getFrameSize(World world, BlockPos pos) {
		if(world.getBlockState(pos).getBlock() != ModBlocks.seal_controller)
			return 0;
		int max = 7;
		
		for(int size = 1; size < max; size ++) {
			
			boolean valid = true;
			int xOff = 0;
			int zOff = 0;
			if(world.getBlockState(pos).getValue(FACING) == EnumFacing.SOUTH)
				zOff -= size;
			if(world.getBlockState(pos).getValue(FACING) == EnumFacing.NORTH)
				zOff += size;
			if(world.getBlockState(pos).getValue(FACING) == EnumFacing.EAST)
				xOff -= size;
			if(world.getBlockState(pos).getValue(FACING) == EnumFacing.WEST)
				xOff += size;

			for(int X = pos.getX() - size; X <= pos.getX() + size; X ++) {
				if(world.getBlockState(new BlockPos(X + xOff, pos.getY(), pos.getZ()+ size + zOff)).getBlock() != ModBlocks.seal_frame && 
						world.getBlockState(new BlockPos(X + xOff, pos.getY(), pos.getZ() + size + zOff)).getBlock() != ModBlocks.seal_controller)
					valid = false;
			}
			for(int X = pos.getX() - size; X <= pos.getX() + size; X ++) {
				if(world.getBlockState(new BlockPos(X + xOff, pos.getY(), pos.getZ() - size + zOff)).getBlock() != ModBlocks.seal_frame && 
						world.getBlockState(new BlockPos(X + xOff, pos.getY(), pos.getZ() - size + zOff)).getBlock() != ModBlocks.seal_controller)
					valid = false;
			}
			for(int Z = pos.getZ() - size; Z <= pos.getZ() + size; Z ++) {
				if(world.getBlockState(new BlockPos(pos.getX() - size + xOff, pos.getY(), Z + zOff)).getBlock() != ModBlocks.seal_frame && 
						world.getBlockState(new BlockPos(pos.getX() - size + xOff, pos.getY(), Z + zOff)).getBlock() != ModBlocks.seal_controller)
					valid = false;
			}
			for(int Z = pos.getZ() - size; Z <= pos.getZ() + size; Z ++) {
				if(world.getBlockState(new BlockPos(pos.getX() + size + xOff, pos.getY(), Z + zOff)).getBlock() != ModBlocks.seal_frame && 
						world.getBlockState(new BlockPos(pos.getX() + size + xOff, pos.getY(), Z + zOff)).getBlock() != ModBlocks.seal_controller)
					valid = false;
			}
			/*for(int X = x - size + 1; X <= x + size - 1; X++) {
				for(int Z = z - size + 1; Z <= z + size - 1; Z++) {
					//if(world.getBlock(X + size + xOff, y, Z + zOff) != ModBlocks.block_steel && 
					//		world.getBlock(X + size + xOff, y, Z + zOff) != Blocks.air)
					//	valid = false;
					world.setBlock(X + xOff, y, Z + zOff, ModBlocks.block_steel);
					System.out.println(valid);
				}
			}*/
			
			if(valid)
				return size;
		}
		
		return 0;
	}
	
	public static void closeSeal(World world, BlockPos pos, int size) {

		int xOff = 0;
		int zOff = 0;
		if(world.getBlockState(pos).getValue(FACING) == EnumFacing.SOUTH)
			zOff -= size;
		if(world.getBlockState(pos).getValue(FACING) == EnumFacing.NORTH)
			zOff += size;
		if(world.getBlockState(pos).getValue(FACING) == EnumFacing.EAST)
			xOff -= size;
		if(world.getBlockState(pos).getValue(FACING) == EnumFacing.WEST)
			xOff += size;
		
		for(int X = pos.getX() - size + 1; X <= pos.getX() + size - 1; X++) {
			for(int Z = pos.getZ() - size + 1; Z <= pos.getZ() + size - 1; Z++) {
				if(world.getBlockState(new BlockPos(X + xOff, pos.getY(), Z + zOff)).getBlock() == Blocks.AIR && !world.isRemote) {
					world.setBlockState(new BlockPos(X + xOff, pos.getY(), Z + zOff), ModBlocks.seal_hatch.getDefaultState());
					TileEntity te = world.getTileEntity(new BlockPos(X + xOff, pos.getY(), Z + zOff));
					if(te != null && te instanceof TileEntityHatch)
						((TileEntityHatch)te).setControllerPos(pos);
						
				}
			}
		}
	}
	
	public static void openSeal(World world, BlockPos pos, int size) {
		
		int xOff = 0;
		int zOff = 0;
		if(world.getBlockState(pos).getValue(FACING) == EnumFacing.SOUTH)
			zOff -= size;
		if(world.getBlockState(pos).getValue(FACING) == EnumFacing.NORTH)
			zOff += size;
		if(world.getBlockState(pos).getValue(FACING) == EnumFacing.EAST)
			xOff -= size;
		if(world.getBlockState(pos).getValue(FACING) == EnumFacing.WEST)
			xOff += size;
		
		for(int X = pos.getX() - size + 1; X <= pos.getX() + size - 1; X++) {
			for(int Z = pos.getZ() - size + 1; Z <= pos.getZ() + size - 1; Z++) {
				if(world.getBlockState(new BlockPos(X + xOff, pos.getY(), Z + zOff)).getBlock() == ModBlocks.seal_hatch && !world.isRemote) {
					world.setBlockState(new BlockPos(X + xOff, pos.getY(), Z + zOff), Blocks.AIR.getDefaultState());
				}
			}
		}
	}
	
	public static boolean isSealClosed(World world, BlockPos pos, int size) {
		
		int xOff = 0;
		int zOff = 0;
		if(world.getBlockState(pos).getValue(FACING) == EnumFacing.SOUTH)
			zOff -= size;
		if(world.getBlockState(pos).getValue(FACING) == EnumFacing.NORTH)
			zOff += size;
		if(world.getBlockState(pos).getValue(FACING) == EnumFacing.EAST)
			xOff -= size;
		if(world.getBlockState(pos).getValue(FACING) == EnumFacing.WEST)
			xOff += size;
		
		for(int X = pos.getX() - size + 1; X <= pos.getX() + size - 1; X++) {
			for(int Z = pos.getZ() - size + 1; Z <= pos.getZ() + size - 1; Z++) {
				if(world.getBlockState(new BlockPos(X + xOff, pos.getY(), Z + zOff)).getBlock() == ModBlocks.seal_hatch) {
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public BombReturnCode explode(World world, BlockPos pos, Entity detonator) {

		if(!world.isRemote) {
			int i = BlockSeal.getFrameSize(world, pos);

			if (i != 0) {
				if (BlockSeal.isSealClosed(world, pos, i))
					BlockSeal.openSeal(world, pos, i);
				else
					BlockSeal.closeSeal(world, pos, i);
				return BombReturnCode.TRIGGERED;
			}

			return BombReturnCode.ERROR_INCOMPATIBLE;
		}

		return BombReturnCode.UNDEFINED;
	}
	
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (world.isBlockPowered(pos))
        {
        	if(!world.getBlockState(pos).getValue(ACTIVATED)) {
        		world.setBlockState(pos, world.getBlockState(pos).withProperty(ACTIVATED, true), 2);
        		
        		int i = BlockSeal.getFrameSize(world, pos);
        		
        		if(i != 0)
        			if(BlockSeal.isSealClosed(world, pos, i))
        				BlockSeal.openSeal(world, pos, i);
        			else
        				BlockSeal.closeSeal(world, pos, i);
        	}
        }
        else
        {
        	if(world.getBlockState(pos).getValue(ACTIVATED)) {
        		world.setBlockState(pos, world.getBlockState(pos).withProperty(ACTIVATED, false), 2);
        	}
        }
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{FACING, ACTIVATED});
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return (((EnumFacing)state.getValue(FACING)).getIndex() << 1) + (state.getValue(ACTIVATED) == true ? 1 : 0);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		boolean activated = (meta & 1) == 1 ? true : false;
		meta = meta >> 1;
		EnumFacing enumfacing = EnumFacing.byIndex(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing).withProperty(ACTIVATED, activated);
	}
	
	
	
	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
	}
	
	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
	{
	   return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
	}

}
