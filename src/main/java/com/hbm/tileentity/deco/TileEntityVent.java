package com.hbm.tileentity.deco;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.particle.EntityModFXShadow;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.HbmEffectNT;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.Random;

@AutoRegister
public class TileEntityVent extends TileEntity implements ITickable {

	Random rand = new Random();
	
	@Override
	public void update() {
		if(!world.isRemote && world.isBlockPowered(pos)) {
			Block b = world.getBlockState(pos).getBlock();

			if(b == ModBlocks.vent_chlorine) {
				emit(1.5D, HbmEffectNT.FX_Chlorine, EntityModFXShadow.Type.CHLORINE);
			}
			if(b == ModBlocks.vent_cloud) {
				emit(1.75D, HbmEffectNT.FX_Cloud, EntityModFXShadow.Type.CLOUD);
			}
			if(b == ModBlocks.vent_pink_cloud) {
				emit(2D, HbmEffectNT.FX_PinkCloud, EntityModFXShadow.Type.PINK_CLOUD);
			}
		}
	}

	private void emit(double spread, HbmEffectNT particleType, EntityModFXShadow.Type shadowType) {
		double x = rand.nextGaussian() * spread;
		double y = rand.nextGaussian() * spread;
		double z = rand.nextGaussian() * spread;

		int px = pos.getX() + (int) x;
		int py = pos.getY() + (int) y;
		int pz = pos.getZ() + (int) z;

		if (world.getBlockState(new BlockPos(px, py, pz)).isNormalCube()) return;

		double mx = x / 2.0D;
		double my = y / 2.0D;
		double mz = z / 2.0D;

		NBTTagCompound data = new NBTTagCompound();
		data.setDouble("moX", mx);
		data.setDouble("moY", my);
		data.setDouble("moZ", mz);
		PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(particleType, data, px, py, pz), new NetworkRegistry.TargetPoint(world.provider.getDimension(), px, py, pz, 128));

		EntityModFXShadow.spawn(world, shadowType, px, py, pz, mx, my, mz);
	}

}
