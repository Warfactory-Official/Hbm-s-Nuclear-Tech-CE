package com.hbm.blocks.machine.rbmk;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.ContaminationUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class RBMKDebrisBurning extends RBMKDebris {

	public RBMKDebrisBurning(String s){
		super(s);
	}
	
	@Override
	public int tickRate(World world) {

		return 100 + world.rand.nextInt(20);
	}

	@Override
	public void updateTick(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull Random rand){
		if(!world.isRemote) {
			
			if(rand.nextInt(5) == 0) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "rbmkflame");
				data.setInteger("maxAge", 300);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, pos.getX() + 0.25 + rand.nextDouble() * 0.5, pos.getY() + 1.75, pos.getZ() + 0.25 + rand.nextDouble() * 0.5), new TargetPoint(world.provider.getDimension(), pos.getX() + 0.5, pos.getY() + 1.75, pos.getZ() + 0.5, 75));
				MainRegistry.proxy.effectNT(data);
				world.playSound(null, pos.getX() + 0.5F, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F);
			}

			ForgeDirection dir = ForgeDirection.getOrientation(world.rand.nextInt(6));
			
			ContaminationUtil.radiate(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 32, 0, 0, 50);
						
			//Foam helps stop the fire; Boron smothers it. 1.66% chance every 100-120 seconds for one side
			int chance = world.getBlockState(new BlockPos(pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ)).getBlock() == ModBlocks.sand_boron ? 10 : 100;
			
			if(rand.nextInt(chance) == 0) {
				world.setBlockState(pos, ModBlocks.pribris.getDefaultState());
			} else {
				world.scheduleUpdate(pos, this, this.tickRate(world));
			}
		}
	}

	@Override
	public void onBlockAdded(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state){
		super.onBlockAdded(world, pos, state);

		if(!world.isRemote) {
			if(world.rand.nextInt(3) == 0) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "rbmkflame");
				data.setInteger("maxAge", 300);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, pos.getX() + 0.25 + world.rand.nextDouble() * 0.5, pos.getY() + 1.75, pos.getZ() + 0.25 + world.rand.nextDouble() * 0.5), new TargetPoint(world.provider.getDimension(), pos.getX() + 0.5, pos.getY() + 1.75, pos.getZ() + 0.5, 75));
				MainRegistry.proxy.effectNT(data);
			}
		}

		world.scheduleUpdate(pos, this, this.tickRate(world));
	}
}