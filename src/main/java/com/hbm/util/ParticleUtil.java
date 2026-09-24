package com.hbm.util;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.HbmEffectNT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class ParticleUtil {

	public static void spawnGasFlame(World world, double x, double y, double z, double mX, double mY, double mZ) {

		NBTTagCompound data = new NBTTagCompound();
		data.setDouble("mX", mX);
		data.setDouble("mY", mY);
		data.setDouble("mZ", mZ);
		
		if(world.isRemote) {
			MainRegistry.proxy.effectNT(HbmEffectNT.GasFlame, x, y, z, data);
		} else {
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.GasFlame, data, x, y, z), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 150));
		}
	}
	// what in the actual fuck
//	public static void spawnJesusFlame(World world, double x, double y, double z) {
//		Random rand = new Random();
//
//		if(rand.nextInt(12) == 0) {
//		NBTTagCompound data = new NBTTagCompound();
//		data.setString("type", "duodec");
//		if(world.isRemote) {
//			data.setDouble("posX", x);
//			data.setDouble("posY", y);
//			data.setDouble("posZ", z);
//			MainRegistry.proxy.effectNT(data);
//		} else {
//			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 150));
//		}
//		}
//	}
	
	public static void spawnDroneLine(World world, double x, double y, double z, double x0, double y0, double z0, int color) {

		NBTTagCompound data = new NBTTagCompound();
		data.setDouble("mX", x0);
		data.setDouble("mY", y0);
		data.setDouble("mZ", z0);
		data.setInteger("color", color);
		if(world.isRemote) {
			MainRegistry.proxy.effectNT(HbmEffectNT.DebugDrone, x, y, z, data);
		} else {
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.DebugDrone, data, x, y, z), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 150));
		}
	}
	
//	public static void spawnTuneFlame(World world, double x, double y, double z) {
//		Random rand = new Random();
//		if(rand.nextInt(12) == 0) {
//			NBTTagCompound data = new NBTTagCompound();
//			data.setString("type", "duoewe");
//			if(world.isRemote) {
//				data.setDouble("posX", x);
//				data.setDouble("posY", y);
//				data.setDouble("posZ", z);
//				MainRegistry.proxy.effectNT(data);
//			} else {
//				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 150));
//			}
//		}
//	}
//	public static void spawnDustFlame(World world, double x, double y, double z, double mX, double mY, double mZ) {
//
//		NBTTagCompound data = new NBTTagCompound();
//		data.setString("type", "duststorm");
//		data.setDouble("mX", mX);
//		data.setDouble("mY", mY);
//		data.setDouble("mZ", mZ);
//
//		if(world.isRemote) {
//			data.setDouble("posX", x);
//			data.setDouble("posY", y);
//			data.setDouble("posZ", z);
//			MainRegistry.proxy.effectNT(data);
//		} else {
//			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 150));
//		}
//	}
	public static void spawnNFlame(World world, double x, double y, double z, double mX, double mY, double mZ) {

		NBTTagCompound data = new NBTTagCompound();
		data.setFloat("lift", 0F);
		data.setFloat("base", 0.55F);
		data.setFloat("max", 0.56F);
		data.setFloat("strafe", 1F);
		data.setInteger("life", 560 + world.rand.nextInt(20));
		data.setInteger("color",0x404040);
		if(world.isRemote) {
			MainRegistry.proxy.effectNT(HbmEffectNT.Tower, x, y, z, data);
		} else {
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.Tower, data, x, y, z), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 150));
		}
	}
}
