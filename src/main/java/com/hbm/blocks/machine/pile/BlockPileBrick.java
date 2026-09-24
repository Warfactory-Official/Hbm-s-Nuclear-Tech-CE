package com.hbm.blocks.machine.pile;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockFlammable;
import com.hbm.blocks.generic.BlockMeta;
import com.hbm.blocks.machine.MachinePWRController;
import com.hbm.lib.ForgeDirection;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.tileentity.machine.pile.TileEntityPileBaseMK2;
import com.hbm.tileentity.machine.pile.TileEntityPileCore;
import com.hbm.tileentity.machine.pile.TileEntityPileCore.PileOrientation;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPileBrick extends BlockFlammable implements IToolable {

	public static final int MIN_V_SIZE = 5;
	public static final int MIN_H_SIZE = 5;
	public static final int MAX_V_SIZE = 15;
	public static final int MAX_H_SIZE = 15;

	public BlockPileBrick(String s) {
		super(Material.ROCK, s, 30, 5, BlockBakeFrame.cube("pile_brick_top", "pile_brick_top", "pile_brick", "pile_brick", "pile_brick_side", "pile_brick_side"));
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {

		if(tool == ToolType.HAND_DRILL) {
			if(side == EnumFacing.DOWN || side == EnumFacing.UP) return false;
			if(world.isRemote) return true;

			ForgeDirection dir = ForgeDirection.getOrientation(side).getOpposite();
			ForgeDirection dirLeft = dir.getRotation(ForgeDirection.DOWN);

			int negHeight = 0;
			int posHeight = 0;
			int left = 0;
			int right = 0;
			int depth = 0;

			for(int i = 1; i <= MAX_V_SIZE - 1; i++) {				if(world.getBlockState(new BlockPos(x, y + i, z)).getBlock() != this) break; posHeight = i; }
			for(int i = 1; i <= MAX_V_SIZE - posHeight - 1; i++) {	if(world.getBlockState(new BlockPos(x, y - i, z)).getBlock() != this) break; negHeight = i; }
			for(int i = 1; i <= MAX_H_SIZE - 1; i++) {				if(world.getBlockState(new BlockPos(x + dirLeft.offsetX * i, y, z + dirLeft.offsetZ * i)).getBlock() != this) break; left = i; }
			for(int i = 1; i <= MAX_H_SIZE - left - 1; i++) {		if(world.getBlockState(new BlockPos(x - dirLeft.offsetX * i, y, z - dirLeft.offsetZ * i)).getBlock() != this) break; right = i; }
			for(int i = 1; i <= MAX_H_SIZE; i++) {					if(world.getBlockState(new BlockPos(x + dir.offsetX * i, y, z + dir.offsetZ * i)).getBlock() != this) break; depth = i; }

			if(posHeight + negHeight + 1 < MIN_V_SIZE) {
				MachinePWRController.sendError(world, new BlockPos(x, y + posHeight, z), "Height too low (<" + MIN_V_SIZE + ")", player);
				MachinePWRController.sendError(world, new BlockPos(x, y - negHeight, z), "Height too low (<" + MIN_V_SIZE + ")", player);
				return true;
			}

			if(left + right + 1 < MIN_H_SIZE) {
				MachinePWRController.sendError(world, new BlockPos(x + dirLeft.offsetX * left, y, z + dirLeft.offsetZ * right), "Width too low (<" + MIN_H_SIZE + ")", player);
				MachinePWRController.sendError(world, new BlockPos(x - dirLeft.offsetX * right, y, z - dirLeft.offsetZ * right), "Width too low (<" + MIN_H_SIZE + ")", player);
				return true;
			}

			if(depth + 1 < MIN_H_SIZE) {
				MachinePWRController.sendError(world, new BlockPos(x + dir.offsetX * depth, y, z + dir.offsetZ * depth), "Depth too low (<" + MIN_H_SIZE + ")", player);
				return true;
			}

			if(posHeight == 0 || negHeight == 0 || left == 0 || right == 0) {
				MachinePWRController.sendError(world, new BlockPos(x, y, z), "Core cannot be on an edge", player);
				return true;
			}

			for(int h = -negHeight; h <= posHeight; h++) {
				for(int v = -left; v <= right; v++) {
					for(int d = 0; d <= depth; d++) {
						BlockPos iPos = new BlockPos(x - dirLeft.offsetX * v + dir.offsetX * d, y + h, z - dirLeft.offsetZ * v + dir.offsetZ * d);

						if(world.getBlockState(iPos).getBlock() != this) {
							MachinePWRController.sendError(world, iPos, "Graphite block missing", player);
							return true;
						}
					}
				}
			}

			for(int h = -negHeight; h <= posHeight; h++) {
				for(int v = -left; v <= right; v++) {
					for(int d = 0; d <= depth; d++) {
						BlockPos iPos = new BlockPos(x - dirLeft.offsetX * v + dir.offsetX * d, y + h, z - dirLeft.offsetZ * v + dir.offsetZ * d);

						if(iPos.getX() == x && iPos.getY() == y && iPos.getZ() == z) {
							world.setBlockState(iPos, ModBlocks.pile_block.getDefaultState().withProperty(BlockMeta.META, BlockPile.META_CORE), 3);
							TileEntityPileCore core = (TileEntityPileCore) world.getTileEntity(iPos);
							core.orientation = PileOrientation.getOrientation(dir);
							core.setupSize(posHeight, negHeight, left, right, depth + 1);
						} else {
							int edgeCount = 0;
							if(h == -negHeight || h == posHeight) edgeCount++;
							if(v == -left || v == right) edgeCount++;
							if(d == 0 || d == depth) edgeCount++;
							boolean isEdge = edgeCount > 1;
							world.setBlockState(iPos, ModBlocks.pile_block.getDefaultState().withProperty(BlockMeta.META, isEdge ? BlockPile.META_EDGE : BlockPile.META_DUMMY), 3);
							TileEntityPileBaseMK2 pile = (TileEntityPileBaseMK2) world.getTileEntity(iPos);
							pile.setCore(x, y, z);
						}
					}
				}
			}

			return true;
		}

		return false;
	}
}
