package com.hbm.tileentity.machine;

import com.hbm.api.block.ICrucibleAcceptor;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockDynamicSlag.TileEntitySlag;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.lib.ForgeDirection;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.util.Compat;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

@AutoRegister
public class TileEntityFoundrySlagtap extends TileEntityFoundryOutlet implements ICrucibleAcceptor {

	@Override
	public boolean canAcceptPartialFlow(World world,BlockPos p,ForgeDirection side,MaterialStack stack) {
		if(filter != null && (filter != stack.material ^ invertFilter)) return false;
		if(isClosed()) return false;
		if(side != ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite()) return false;

		Vec3d start = new Vec3d(p.getX() + 0.5, p.getY() - 0.125, p.getZ() + 0.5);
		Vec3d end = new Vec3d(p.getX() + 0.5, p.getY() + 0.125 - 15, p.getZ() + 0.5);

		RayTraceResult mop = world.rayTraceBlocks(start, end, true, true, true);

		if(mop == null || mop.typeOfHit != mop.typeOfHit.BLOCK) {
			return false;
		}

		return true;
	}

	@Override
	public MaterialStack flow(World world, BlockPos p, ForgeDirection side, MaterialStack stack) {

		if(stack == null || stack.material == null || stack.amount <= 0) {
			return null;
		}

		Vec3d start = new Vec3d(p.getX() + 0.5, p.getY() - 0.125, p.getZ() + 0.5);
		Vec3d end = new Vec3d(p.getX() + 0.5, p.getY() + 0.125 - 15, p.getZ() + 0.5);

		RayTraceResult mop = world.rayTraceBlocks(start, end, true, true, true);

		if(mop == null || mop.typeOfHit != mop.typeOfHit.BLOCK) {
			return null;
		}

		Block hit = world.getBlockState(mop.getBlockPos()).getBlock();
		Block above = world.getBlockState(mop.getBlockPos().up()).getBlock();

		boolean didFlow = false;

		if(hit == ModBlocks.slag) {
			TileEntitySlag tile = (TileEntitySlag) Compat.getTileStandard(world, mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ());
			if(tile.mat == stack.material) {
				int transfer = Math.min(tile.maxAmount - tile.amount, stack.amount);
				tile.amount += transfer;
				stack.amount -= transfer;
				didFlow = didFlow || transfer > 0;
				//world.markBlockForUpdate(mop.blockX, mop.blockY, mop.blockZ);
				//world.scheduleBlockUpdate(mop.blockX, mop.blockY, mop.blockZ, ModBlocks.slag, 1);
				world.scheduleUpdate(mop.getBlockPos(),ModBlocks.slag,1);
			}
		} else if(hit.isReplaceable(world, mop.getBlockPos())) {
			world.setBlockState(mop.getBlockPos(), ModBlocks.slag.getDefaultState());
			TileEntitySlag tile = (TileEntitySlag) Compat.getTileStandard(world, mop.getBlockPos().getX(),mop.getBlockPos().getY()+1,mop.getBlockPos().getZ());
			tile.mat = stack.material;
			int transfer = Math.min(tile.maxAmount, stack.amount);
			tile.amount += transfer;
			stack.amount -= transfer;
			didFlow = didFlow || transfer > 0;
			//world.markBlockForUpdate(mop.blockX, mop.blockY, mop.blockZ);
			//world.scheduleBlockUpdate(mop.blockX, mop.blockY, mop.blockZ, ModBlocks.slag, 1);
			world.scheduleUpdate(mop.getBlockPos(), ModBlocks.slag, 1);
		}

		if(stack.amount > 0 && above.isReplaceable(world, mop.getBlockPos().up())) {
			world.setBlockState(mop.getBlockPos().up(), ModBlocks.slag.getDefaultState());
			TileEntitySlag tile = (TileEntitySlag) Compat.getTileStandard(world, mop.getBlockPos().getX(),mop.getBlockPos().getY()+1,mop.getBlockPos().getZ());
			tile.mat = stack.material;
			int transfer = Math.min(tile.maxAmount, stack.amount);
			tile.amount += transfer;
			stack.amount -= transfer;
			didFlow = didFlow || transfer > 0;
			//world.markBlockForUpdate(mop.blockX, mop.blockY+1, mop.blockZ);
			//world.scheduleBlockUpdate(mop.blockX, mop.blockY+1, mop.blockZ, ModBlocks.slag, 1);
			world.scheduleUpdate(mop.getBlockPos().up(), ModBlocks.slag, 1);
		}

		if(didFlow) {
			ForgeDirection dir = side.getOpposite();
			double hitY = mop.getBlockPos().getY();

			NBTTagCompound data = new NBTTagCompound();
			data.setInteger("color", stack.material.moltenColor);
			data.setByte("dir", (byte) dir.ordinal());
			data.setFloat("off", 0.375F);
			data.setFloat("base", 0F);
			data.setFloat("len", Math.max(1F, pos.getY() - (float) (Math.ceil(hitY))));
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.Foundry, data, p.getX() + 0.5D - dir.offsetX * 0.125, p.getY() + 0.125, p.getZ() + 0.5D - dir.offsetZ * 0.125), new TargetPoint(world.provider.getDimension(), p.getX() + 0.5, p.getY(), p.getZ() + 0.5, 50));
		}

		if(stack.amount <= 0) {
			stack = null;
		}

		return stack;
	}

	@Override public boolean canAcceptPartialPour(World world, BlockPos p, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) { return false; }
	@Override public MaterialStack pour(World world, BlockPos p, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) { return stack; }
}
