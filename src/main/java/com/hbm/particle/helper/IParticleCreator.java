package com.hbm.particle.helper;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public interface IParticleCreator {

    @SideOnly(Side.CLIENT)
    void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data);

    static void sendPacket(World world, HbmEffectNT effect, double x, double y, double z, int range, @Nullable NBTTagCompound data) {
        PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(effect, data, x, y, z), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, range));
    }
}
