package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.SoyuzLauncher;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntitySoyuzStruct extends TileEntity implements ITickable {

	int age;

	@Override
	public void update() {

		if(world.isRemote)
			return;

		age++;

		if(age < 20)
			return;

		age = 0;

		/// CHECK PAD ///
		for(int i = -6; i <= 6; i++)
			for(int j = 3; j <= 4; j++)
				for(int k = -6; k <= 6; k++)
					if(world.getBlockState(new BlockPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k)).getBlock() != ModBlocks.struct_launcher){
						return;
					}
		
		for(int i = -1; i <= 1; i++)
			for(int j = 3; j <= 4; j++)
				for(int k = -8; k <= -7; k++)
					if(world.getBlockState(new BlockPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k)).getBlock() != ModBlocks.struct_launcher)
						return;

		for(int i = -2; i <= 2; i++)
			for(int j = 3; j <= 4; j++)
				for(int k = 7; k <= 9; k++)
					if(world.getBlockState(new BlockPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k)).getBlock() != ModBlocks.struct_launcher)
						return;

		for(int i = -2; i <= 2; i++)
			for(int k = 5; k <= 9; k++)
				if(world.getBlockState(new BlockPos(pos.getX() + i, pos.getY() + 51, pos.getZ() + k)).getBlock() != ModBlocks.struct_launcher)
					return;

		for(int i = -1; i <= 1; i++)
			for(int k = -8; k <= -6; k++)
				if(world.getBlockState(new BlockPos(pos.getX() + i, pos.getY() + 38, pos.getZ() + k)).getBlock() != ModBlocks.struct_launcher)
					return;

		/// CHECK LEGS ///
		for(int i = 3; i <= 6; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = 3; k <= 6; k++)
					if(world.getBlockState(new BlockPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k)).getBlock() != ModBlocks.concrete &&
							world.getBlockState(new BlockPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k)).getBlock() != ModBlocks.concrete_smooth)
						return;

		for(int i = -6; i <= -3; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = 3; k <= 6; k++)
					if(world.getBlockState(new BlockPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k)).getBlock() != ModBlocks.concrete &&
					world.getBlockState(new BlockPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k)).getBlock() != ModBlocks.concrete_smooth)
				return;

		for(int i = -6; i <= -3; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = -6; k <= -3; k++)
					if(world.getBlockState(new BlockPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k)).getBlock() != ModBlocks.concrete &&
					world.getBlockState(new BlockPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k)).getBlock() != ModBlocks.concrete_smooth)
				return;

		for(int i = 3; i <= 6; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = -6; k <= -3; k++)
					if(world.getBlockState(new BlockPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k)).getBlock() != ModBlocks.concrete &&
					world.getBlockState(new BlockPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k)).getBlock() != ModBlocks.concrete_smooth)
				return;

		for(int i = -1; i <= 1; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = -8; k <= -6; k++)
					if(world.getBlockState(new BlockPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k)).getBlock() != ModBlocks.concrete &&
					world.getBlockState(new BlockPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k)).getBlock() != ModBlocks.concrete_smooth)
				return;

		for(int i = -2; i <= 2; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = 5; k <= 9; k++)
					if(world.getBlockState(new BlockPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k)).getBlock() != ModBlocks.concrete &&
					world.getBlockState(new BlockPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k)).getBlock() != ModBlocks.concrete_smooth)
				return;

		/// CHECK SCAFFOLDING ///
		for(int i = -1; i <= 1; i++)
			for(int j = 5; j <= 50; j++)
				for(int k = 6; k <= 8; k++)
					if(world.getBlockState(new BlockPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k)).getBlock() != ModBlocks.struct_scaffold)
						return;

		for(int j = 5; j <= 37; j++)
			if(world.getBlockState(new BlockPos(pos.getX(), pos.getY() + j, pos.getZ() - 7)).getBlock() != ModBlocks.struct_scaffold)
				return;
		/// CHECKS COMPLETE ///

		/// DELETE SCAFFOLDING ///

		for(int i = -2; i <= 2; i++)
			for(int k = 5; k <= 9; k++)
				world.setBlockState(new BlockPos(pos.getX() + i, pos.getY() + 51, pos.getZ() + k), Blocks.AIR.getDefaultState());

		for(int i = -1; i <= 1; i++)
			for(int k = -8; k <= -6; k++)
				world.setBlockState(new BlockPos(pos.getX() + i, pos.getY() + 38, pos.getZ() + k), Blocks.AIR.getDefaultState());

		for(int i = -2; i <= 2; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = 5; k <= 9; k++)
					world.setBlockState(new BlockPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k), Blocks.AIR.getDefaultState());

		for(int i = -1; i <= 1; i++)
			for(int j = 5; j <= 50; j++)
				for(int k = 6; k <= 8; k++)
					world.setBlockState(new BlockPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k), Blocks.AIR.getDefaultState());

		for(int j = 5; j <= 37; j++)
			world.setBlockState(new BlockPos(pos.getX(), pos.getY() + j, pos.getZ() - 7), Blocks.AIR.getDefaultState());

		/// GENERATE LAUNCHER ///

		ForgeDirection dir = ForgeDirection.EAST;

		world.setBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ()), Blocks.AIR.getDefaultState());
		world.setBlockState(new BlockPos(pos.getX(), pos.getY() + SoyuzLauncher.height, pos.getZ()), ModBlocks.soyuz_launcher.getDefaultState().withProperty(SoyuzLauncher.META, dir.ordinal() + SoyuzLauncher.offset), 3);
		((SoyuzLauncher)ModBlocks.soyuz_launcher).fillSpace(world, pos.getX(), pos.getY(), pos.getZ(), dir, 0);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}