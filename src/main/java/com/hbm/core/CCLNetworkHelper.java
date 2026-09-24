package com.hbm.core;

import com.hbm.util.NetworkUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

/**
 * Runtime target of CodeChickenLibNetworkTransformer.
 *
 * CCL hands one FMLProxyPacket to the vanilla broadcast helpers, which write that same instance to every
 * connection. MixinNetworkDispatcher#write consumes exactly one payload reference per dispatch, so any
 * broadcast with two or more recipients underflows the count. The integrated server's local channel reads
 * its payload asynchronously and is usually the one left holding a freed buffer.
 *
 * Recipient selection mirrors PlayerList / PlayerChunkMapEntry exactly, one reference is retained per
 * recipient, and the reference the packet arrived with is dropped at the end.
 */
@SuppressWarnings("unused")
public class CCLNetworkHelper {

    private static ByteBuf managedPayload(Packet<?> packet) {
        if(!(packet instanceof FMLProxyPacket proxy)) return null;
        if(!NetworkUtil.shouldHandleProxyPacket(proxy.channel())) return null;
        return proxy.payload();
    }

    private static void send(EntityPlayerMP player, Packet<?> packet, ByteBuf payload) {
        if(payload != null) payload.retain();
        player.connection.sendPacket(packet);
    }

    public static void sendToClients(Packet<?> packet) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if(server == null) return;

        ByteBuf payload = managedPayload(packet);

        try {
            for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                send(player, packet, payload);
            }
        } finally {
            if(payload != null) payload.release();
        }
    }

    public static void sendToAllAround(Packet<?> packet, double x, double y, double z, double range, int dim) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if(server == null) return;

        ByteBuf payload = managedPayload(packet);

        try {
            for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                if(player.dimension != dim) continue;

                double dx = x - player.posX;
                double dy = y - player.posY;
                double dz = z - player.posZ;

                if(dx * dx + dy * dy + dz * dz < range * range) {
                    send(player, packet, payload);
                }
            }
        } finally {
            if(payload != null) payload.release();
        }
    }

    public static void sendToDimension(Packet<?> packet, int dim) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if(server == null) return;

        ByteBuf payload = managedPayload(packet);

        try {
            for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                if(player.dimension == dim) send(player, packet, payload);
            }
        } finally {
            if(payload != null) payload.release();
        }
    }

    public static void sendToChunk(Packet<?> packet, World world, int chunkX, int chunkZ) {
        if(!(world instanceof WorldServer server)) return;

        PlayerChunkMap map = server.getPlayerChunkMap();
        ByteBuf payload = managedPayload(packet);

        try {
            for(EntityPlayer player : server.playerEntities) {
                if(!(player instanceof EntityPlayerMP)) continue;
                if(!map.isPlayerWatchingChunk((EntityPlayerMP) player, chunkX, chunkZ)) continue;
                send((EntityPlayerMP) player, packet, payload);
            }
        } finally {
            if(payload != null) payload.release();
        }
    }

    public static void sendToOps(Packet<?> packet) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if(server == null) return;

        PlayerList players = server.getPlayerList();
        ByteBuf payload = managedPayload(packet);

        try {
            for(EntityPlayerMP player : players.getPlayers()) {
                if(!players.canSendCommands(player.getGameProfile())) continue;
                send(player, packet, payload);
            }
        } finally {
            if(payload != null) payload.release();
        }
    }
}