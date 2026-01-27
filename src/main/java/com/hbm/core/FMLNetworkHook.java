package com.hbm.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

import static com.hbm.lib.internal.UnsafeHolder.U;
import static com.hbm.lib.internal.UnsafeHolder.fieldOffset;

@SuppressWarnings("unused")
public final class FMLNetworkHook {
    private static final int PART_SIZE = 0x100000 - 0x50; // 1_048_496
    private static final int MAX_PARTS = 255;
    private static final long SIDE_OFF = fieldOffset(NetworkDispatcher.class, "side");
    private static final long SP_DATA_OFF = fieldOffset(SPacketCustomPayload.class, "data", "field_149171_b");
    private static final long SP_CHAN_OFF = fieldOffset(SPacketCustomPayload.class, "channel", "field_149172_a");
    private static final long CP_DATA_OFF = fieldOffset(CPacketCustomPayload.class, "data", "field_149561_c");
    private static final long CP_CHAN_OFF = fieldOffset(CPacketCustomPayload.class, "channel", "field_149562_a");

    private FMLNetworkHook() {
    }

    private static void releaseCustomPayloadData(Object pkt) {
        if (pkt instanceof SPacketCustomPayload sp) {
            Object o = U.getReference(sp, SP_DATA_OFF);
            if (o != null) {
                U.putReference(sp, SP_DATA_OFF, null);
                ((ByteBuf) o).release();
            }
        } else if (pkt instanceof CPacketCustomPayload cp) {
            Object o = U.getReference(cp, CP_DATA_OFF);
            if (o != null) {
                U.putReference(cp, CP_DATA_OFF, null);
                ((ByteBuf) o).release();
            }
        }
    }

    private static SPacketCustomPayload createUncheckedSPacket(String channel, PacketBuffer buf) {
        SPacketCustomPayload pkt = new SPacketCustomPayload();
        // skip 1048576B size check in ctor
        U.putReference(pkt, SP_CHAN_OFF, channel);
        U.putReference(pkt, SP_DATA_OFF, buf);
        return pkt;
    }

    private static CPacketCustomPayload createUncheckedCPacket(String channel, PacketBuffer buf) {
        CPacketCustomPayload pkt = new CPacketCustomPayload();
        // skip 32767B size check in ctor
        U.putReference(pkt, CP_CHAN_OFF, channel);
        U.putReference(pkt, CP_DATA_OFF, buf);
        return pkt;
    }

    @SuppressWarnings("unused")
    public static void networkDispatcherWrite(NetworkDispatcher self, ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (!(msg instanceof FMLProxyPacket pkt)) {
            ctx.write(msg, promise);
            return;
        }

        final ByteBuf payload = pkt.payload();
        final boolean local = self.manager.isLocalChannel();

        try {
            final Side side = (Side) U.getReference(self, SIDE_OFF);

            if (side == Side.CLIENT) { // Client -> Server
                PacketBuffer pb = new PacketBuffer(payload.retainedSlice());
                CPacketCustomPayload out;

                if (local) {
                    out = createUncheckedCPacket(pkt.channel(), pb);
                } else {
                    out = new CPacketCustomPayload(pkt.channel(), pb);
                }

                final ChannelFuture f = ctx.write(out, promise);
                f.addListener((ChannelFutureListener) future -> {
                    if (!local || !future.isSuccess()) {
                        releaseCustomPayloadData(out);
                    }
                });

            } else { // Server -> Client
                if (local) {
                    PacketBuffer pb = new PacketBuffer(payload.retainedSlice());
                    SPacketCustomPayload out = createUncheckedSPacket(pkt.channel(), pb);
                    final ChannelFuture f = ctx.write(out, promise);
                    f.addListener((ChannelFutureListener) future -> {
                        if (!future.isSuccess()) {
                            releaseCustomPayloadData(out);
                        }
                    });
                    return;
                }
                List<Packet<INetHandlerPlayClient>> parts = fmlProxyPacketToS3FPackets(pkt);
                int last = parts.size() - 1;

                for (int i = 0; i <= last; i++) {
                    Packet<INetHandlerPlayClient> p = parts.get(i);
                    ChannelPromise pPromise = (i == last) ? promise : ctx.newPromise();

                    final ChannelFuture f = ctx.write(p, pPromise);
                    f.addListener((ChannelFutureListener) _ -> {
                        releaseCustomPayloadData(p);
                    });
                }
            }
        } finally {
            // We retained slices for the packets, so we release the original reference from the FMLProxyPacket
            payload.release();
        }
    }

    public static List<Packet<INetHandlerPlayClient>> fmlProxyPacketToS3FPackets(FMLProxyPacket self) {
        final ByteBuf buf = self.payload();
        final int len = buf.readableBytes();
        final int ri = buf.readerIndex();

        final ArrayList<Packet<INetHandlerPlayClient>> ret = new ArrayList<>(Math.min(4, (len / (PART_SIZE - 1)) + 2));

        try {
            if (len < PART_SIZE) {
                PacketBuffer pb = new PacketBuffer(buf.retainedSlice(ri, len));
                ret.add(new SPacketCustomPayload(self.channel(), pb));
                return ret;
            }

            int parts = (int) Math.ceil(len / (double) (PART_SIZE - 1));
            if (parts > MAX_PARTS) throw new IllegalArgumentException("Payload too large (parts=" + parts + ", max=" + MAX_PARTS + ")");

            PacketBuffer preamble = new PacketBuffer(Unpooled.buffer());
            preamble.writeString(self.channel());
            preamble.writeByte(parts);
            preamble.writeInt(len);
            ret.add(new SPacketCustomPayload("FML|MP", preamble));

            int offset = 0;
            for (int x = 0; x < parts; x++) {
                int dataLen = Math.min(PART_SIZE - 1, len - offset);

                ByteBuf slice = buf.retainedSlice(ri + offset, dataLen);
                ByteBuf header = Unpooled.buffer(1, 1).writeByte(x & 0xFF);
                ByteBuf combined = Unpooled.wrappedBuffer(header, slice);
                PacketBuffer pb = new PacketBuffer(combined);

                try {
                    ret.add(new SPacketCustomPayload("FML|MP", pb));
                } catch (Throwable t) {
                    combined.release();
                    throw t;
                }
                offset += dataLen;
            }

            return ret;
        } catch (Throwable t) {
            for (Packet<INetHandlerPlayClient> p : ret) releaseCustomPayloadData(p);
            throw t;
        }
    }
}
