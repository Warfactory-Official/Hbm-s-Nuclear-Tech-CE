package com.hbm.main;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.threading.ThreadedPacket;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.MessageToMessageCodec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleChannelHandlerWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.List;

import static net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec.INBOUNDPACKETTRACKER;

// Essentially the `SimpleNetworkWrapper` from FML but doesn't flush the packets immediately. Also now with a custom codec!
// sendTo*Direct is only intended for PacketThreading usage.
public class NetworkHandler {

    // Network codec for allowing packets to be "precompiled".
    @ChannelHandler.Sharable
    private static class PrecompilingNetworkCodec extends MessageToMessageCodec<FMLProxyPacket, Object> {

        private final TByteObjectHashMap<Class<? extends IMessage>> discriminators = new TByteObjectHashMap<>();
        private final TObjectByteHashMap<Class<? extends IMessage>> types = new TObjectByteHashMap<>();

        public void addDiscriminator(int discriminator, Class<? extends IMessage> type) {
            discriminators.put((byte) discriminator, type);
            types.put(type, (byte) discriminator);
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
            ctx.channel().attr(INBOUNDPACKETTRACKER).set(new ThreadLocal<>());
        }

        @Override
        protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) {
            ByteBuf headerBuf = null;
            ByteBuf payload = null;
            ByteBuf combined = null;
            try {
                headerBuf = ctx.alloc().ioBuffer(1);
                final byte discriminator;
                final Class<?> msgClass = msg.getClass();
                if (msg instanceof ThreadedPacket packet) {
                    if (!types.containsKey(msgClass)) {
                        throw new CodecException("Unregistered packet type " + msgClass.getName());
                    }
                    discriminator = types.get(msgClass);
                    headerBuf.writeByte(discriminator);
                    ByteBuf cb = packet.getCompiledBuffer();
                    payload = cb.retainedDuplicate();
                } else if (msg instanceof IMessage message) {
                    if (!types.containsKey(msgClass)) {
                        throw new CodecException("Unregistered packet type " + msgClass.getName());
                    }
                    discriminator = types.get(msgClass);
                    headerBuf.writeByte(discriminator);

                    payload = ctx.alloc().ioBuffer();
                    message.toBytes(payload);
                } else {
                    throw new CodecException("Unknown packet type " + msgClass.getName());
                }
                combined = ctx.alloc().compositeBuffer(2)
                                      .addComponent(true, headerBuf)
                                      .addComponent(true, payload);
                headerBuf = null;
                payload = null;

                FMLProxyPacket proxy = new FMLProxyPacket(new PacketBuffer(combined),
                        ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get());
                ThreadLocal<WeakReference<FMLProxyPacket>> tl = ctx.channel().attr(INBOUNDPACKETTRACKER).get();
                WeakReference<FMLProxyPacket> ref = tl == null ? null : tl.get();
                FMLProxyPacket old = ref == null ? null : ref.get();
                if (old != null) proxy.setDispatcher(old.getDispatcher());
                out.add(proxy);
                //noinspection UnusedAssignment
                combined = null;
            } catch (Throwable t) {
                if (combined != null && combined.refCnt() > 0) combined.release();
                if (payload != null && payload.refCnt() > 0) payload.release();
                if (headerBuf != null && headerBuf.refCnt() > 0) headerBuf.release();
                throw t;
            }
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, FMLProxyPacket msg, List<Object> out) throws Exception {
            ByteBuf inboundBuf = msg.payload();
            try {
                byte discriminator = inboundBuf.readByte();
                Class<?> originalMsgClass = discriminators.get(discriminator);

                if (originalMsgClass == null)
                    throw new CodecException("Undefined message for discriminator " + discriminator + " in channel " + msg.channel());

                Object newMsg = originalMsgClass.getDeclaredConstructor().newInstance();
                ctx.channel().attr(INBOUNDPACKETTRACKER).get().set(new WeakReference<>(msg));

                if (newMsg instanceof IMessage message)
                    // If 'message' is a BufPacket, it performs a retainedSlice() here.
                    // This increments the count to 2.
                    // The finally block below decrements to 1.
                    // The BufPacket handler eventually decrements to 0. Safe.
                    message.fromBytes(inboundBuf.slice());
                else
                    throw new CodecException("Unknown packet codec requested during decoding, expected IMessage/PrecompiledPacket, got " + msg.getClass().getName());

                out.add(newMsg);
            } finally {
                if (inboundBuf != null) {
                    inboundBuf.release();
                }
            }
        }
    }

    private static FMLEmbeddedChannel clientChannel;
    private static FMLEmbeddedChannel serverChannel;

    private static PrecompilingNetworkCodec packetCodec;

    public NetworkHandler(String name) {
        packetCodec = new PrecompilingNetworkCodec();
        EnumMap<Side, FMLEmbeddedChannel> channels = NetworkRegistry.INSTANCE.newChannel(name, packetCodec);
        clientChannel = channels.get(Side.CLIENT);
        serverChannel = channels.get(Side.SERVER);
    }

    private static <REQ extends IMessage, REPLY extends IMessage> IMessageHandler<? super REQ, ? extends REPLY> instantiate(Class<? extends IMessageHandler<? super REQ, ? extends REPLY>> handler) {
        try {
            return handler.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, int discriminator, Side side) {
        registerMessage(instantiate(messageHandler), requestMessageType, discriminator, side);
    }

    public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(IMessageHandler<? super REQ, ? extends REPLY> messageHandler, Class<REQ> requestMessageType, int discriminator, Side side) {
        packetCodec.addDiscriminator(discriminator, requestMessageType);

        FMLEmbeddedChannel channel = side.isClient() ? clientChannel : serverChannel;
        String type = channel.findChannelHandlerNameForType(PrecompilingNetworkCodec.class);

        SimpleChannelHandlerWrapper<REQ, REPLY> handler = new SimpleChannelHandlerWrapper<>(messageHandler, side, requestMessageType);
        channel.pipeline().addAfter(type, messageHandler.getClass().getName(), handler);
    }

    public Packet<?> getPacketFrom(IMessage message) {
        return serverChannel.generatePacketFrom(message);
    }

    // ClientTickEvent Phase END
    public static void flushClient() {
        PacketThreading.LOCK.lock();
        try {
            flushClientDirect();
        } finally {
            PacketThreading.LOCK.unlock();
        }
    }

    // ServerTickEvent Phase END
    public static void flushServer() {
        PacketThreading.LOCK.lock();
        try {
            flushServerDirect();
        } finally {
            PacketThreading.LOCK.unlock();
        }
    }

    public static void flushClientDirect() {
        clientChannel.flush();
    }

    public static void flushServerDirect() {
        serverChannel.flush();
    }

    public void sendToServer(IMessage message) {
        if (message instanceof ThreadedPacket packet) {
            MainRegistry.logger.warn("[NetworkHandler] Deprecated API usage: ThreadedPacket {} is sent through sendToServer. " +
                    "Delegating to PacketThreading.createSendToServerThreadedPacket.", packet.getClass().getName());
            PacketThreading.createSendToServerThreadedPacket(packet);
            return;
        }
        PacketThreading.LOCK.lock();
        try {
            sendToServerDirect(message);
        } finally {
            PacketThreading.LOCK.unlock();
        }
    }

    public void sendToServerDirect(IMessage message) {
        clientChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        clientChannel.write(message);
    }

    public void sendToDimension(IMessage message, int dimensionId) {
        if (message instanceof ThreadedPacket packet) {
            MainRegistry.logger.warn("[NetworkHandler] Deprecated API usage: ThreadedPacket {} is sent through sendToDimension. " +
                    "Delegating to PacketThreading.createSendToDimensionThreadedPacket.", packet.getClass().getName());
            PacketThreading.createSendToDimensionThreadedPacket(packet, dimensionId);
            return;
        }
        PacketThreading.LOCK.lock();
        try {
            sendToDimensionDirect(message, dimensionId);
        } finally {
            PacketThreading.LOCK.unlock();
        }
    }

    public void sendToDimensionDirect(IMessage message, int dimensionId) {
        serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION);
        serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimensionId);
        serverChannel.write(message);
    }

    public void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point) {
        if (message instanceof ThreadedPacket packet) {
            MainRegistry.logger.warn("[NetworkHandler] Deprecated API usage: ThreadedPacket {} is sent through sendToAllAround. " +
                    "Delegating to PacketThreading.createAllAroundThreadedPacket.", packet.getClass().getName());
            PacketThreading.createAllAroundThreadedPacket(packet, point);
            return;
        }
        PacketThreading.LOCK.lock();
        try {
            sendToAllAroundDirect(message, point);
        } finally {
            PacketThreading.LOCK.unlock();
        }
    }

    public void sendToAllAroundDirect(IMessage message, NetworkRegistry.TargetPoint point) {
        serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
        serverChannel.write(message);
    }

    public void sendToAllTracking(IMessage message, NetworkRegistry.TargetPoint point) {
        if (message instanceof ThreadedPacket packet) {
            MainRegistry.logger.warn("[NetworkHandler] Deprecated API usage: ThreadedPacket {} is sent through sendToAllTracking. " +
                    "Delegating to PacketThreading.createSendToAllTrackingThreadedPacket.", packet.getClass().getName());
            PacketThreading.createSendToAllTrackingThreadedPacket(packet, point);
            return;
        }
        PacketThreading.LOCK.lock();
        try {
            sendToAllTrackingDirect(message, point);
        } finally {
            PacketThreading.LOCK.unlock();
        }
    }

    public void sendToAllTrackingDirect(IMessage message, NetworkRegistry.TargetPoint point) {
        serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TRACKING_POINT);
        serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
        serverChannel.write(message);
    }

    public void sendToAllTracking(IMessage message, Entity entity) {
        if (message instanceof ThreadedPacket packet) {
            MainRegistry.logger.warn("[NetworkHandler] Deprecated API usage: ThreadedPacket {} is sent through sendToAllTracking. " +
                    "Delegating to PacketThreading.createSendToAllTrackingThreadedPacket.", packet.getClass().getName());
            PacketThreading.createSendToAllTrackingThreadedPacket(packet, entity);
            return;
        }
        PacketThreading.LOCK.lock();
        try {
            sendToAllTrackingDirect(message, entity);
        } finally {
            PacketThreading.LOCK.unlock();
        }
    }

    public void sendToAllTrackingDirect(IMessage message, Entity entity) {
        serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TRACKING_ENTITY);
        serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(entity);
        serverChannel.write(message);
    }

    public void sendTo(IMessage message, EntityPlayerMP player) {
        if (message instanceof ThreadedPacket packet) {
            MainRegistry.logger.warn("[NetworkHandler] Deprecated API usage: ThreadedPacket {} is sent through sendTo. " +
                    "Delegating to PacketThreading.createSendToThreadedPacket.", packet.getClass().getName());
            PacketThreading.createSendToThreadedPacket(packet, player);
            return;
        }
        PacketThreading.LOCK.lock();
        try {
            sendToDirect(message, player);
        } finally {
            PacketThreading.LOCK.unlock();
        }
    }

    public void sendToDirect(IMessage message, EntityPlayerMP player) {
        serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        serverChannel.write(message);
    }

    public void sendToAll(IMessage message) {
        if (message instanceof ThreadedPacket packet) {
            MainRegistry.logger.warn("[NetworkHandler] Deprecated API usage: ThreadedPacket {} is sent through sendToAll. " +
                    "Delegating to PacketThreading.createSendToAllThreadedPacket.", packet.getClass().getName());
            PacketThreading.createSendToAllThreadedPacket(packet);
            return;
        }
        PacketThreading.LOCK.lock();
        try {
            sendToAllDirect(message);
        } finally {
            PacketThreading.LOCK.unlock();
        }
    }

    public void sendToAllDirect(IMessage message) {
        serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
        serverChannel.write(message);
    }
}
