package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IExplosionSFX;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.HbmEffectNT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class ExplosionEffectAmat implements IExplosionSFX {
	@Override
	public void doEffect(ExplosionVNT explosion, World world, double x, double y, double z, float size) {
		NBTTagCompound data = new NBTTagCompound();
		data.setFloat("scale", size);
		PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.AmatExplosion, data, x, y, z), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 200));
	}
}
