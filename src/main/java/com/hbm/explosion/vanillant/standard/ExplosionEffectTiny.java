package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IExplosionSFX;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class ExplosionEffectTiny implements IExplosionSFX {

    @Override
    public void doEffect(ExplosionVNT explosion, World world, double x, double y, double z, float size) {
        if(world.isRemote) return;

        world.playSound(null, x, y, z, HBMSoundHandler.explosion_tiny, SoundCategory.BLOCKS, 15.0F, 1.0F);

        NBTTagCompound data = new NBTTagCompound();
        data.setString("type", "vanillaExt");
        data.setString("mode", "largeexplode");
        data.setFloat("size", 1.5F);
        data.setByte("count", (byte)1);
        PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 100));
    }
}
