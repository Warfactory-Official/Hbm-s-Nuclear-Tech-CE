package com.hbm.mixin;

import com.hbm.interfaces.IMixinFMLProxyPacket;
import com.hbm.util.NetworkUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = NetworkDispatcher.class, remap = false)
public abstract class MixinNetworkDispatcher {
    @Final
    @Shadow
    private Side side;

    @Final
    @Shadow
    public NetworkManager manager;

    @Inject(method = "write", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void hbm$write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise, CallbackInfo ci) {
        if (networkDispatcherWrite(ctx, msg, promise)) {
            ci.cancel();
        }
    }

    @Unique
    private static SPacketCustomPayload createUncheckedSPacket(String channel, PacketBuffer buf) {
        SPacketCustomPayload pkt = new SPacketCustomPayload();
        // skip 1048576B size check in ctor
        pkt.channel = channel;
        pkt.data = buf;
        return pkt;
    }

    @Unique
    private static CPacketCustomPayload createUncheckedCPacket(String channel, PacketBuffer buf) {
        CPacketCustomPayload pkt = new CPacketCustomPayload();
        // skip 32767B size check in ctor
        pkt.channel = channel;
        pkt.data = buf;
        return pkt;
    }

    @Unique
    private boolean networkDispatcherWrite(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (!(msg instanceof FMLProxyPacket pkt) || !NetworkUtil.shouldHandleProxyPacket(pkt.channel())) return false;

        ByteBuf payload = pkt.payload();
        boolean local = manager.isLocalChannel();

        try {
            if (side == Side.CLIENT) { // Client -> Server
                PacketBuffer pb = new PacketBuffer(payload.retainedSlice());
                CPacketCustomPayload out;

                if (local) {
                    out = createUncheckedCPacket(pkt.channel(), pb);
                } else {
                    out = new CPacketCustomPayload(pkt.channel(), pb);
                }

                ctx.write(out, promise).addListener((ChannelFutureListener) future -> {
                    if (!local || !future.isSuccess()) {
                        NetworkUtil.releaseCustomPayloadData(out);
                    }
                });

            } else { // Server -> Client
                if (local) {
                    PacketBuffer pb = new PacketBuffer(payload.retainedSlice());
                    SPacketCustomPayload out = createUncheckedSPacket(pkt.channel(), pb);
                    ctx.write(out, promise).addListener((ChannelFutureListener) future -> {
                        if (!future.isSuccess()) {
                            NetworkUtil.releaseCustomPayloadData(out);
                        }
                    });
                    return true;
                }
                List<Packet<INetHandlerPlayClient>> parts = ((IMixinFMLProxyPacket) pkt).hbm$toS3FPackets();
                int last = parts.size() - 1;

                for (int i = 0; i <= last; i++) {
                    Packet<INetHandlerPlayClient> p = parts.get(i);
                    ChannelPromise pPromise = (i == last) ? promise : ctx.newPromise();
                    ctx.write(p, pPromise).addListener((ChannelFutureListener) _ -> NetworkUtil.releaseCustomPayloadData(p));
                }
            }
        } finally {
            // We retained slices for the packets, so we release the original reference from the FMLProxyPacket
            // This may throw IllegalReferenceCountException iff some other mod a) reused packets without properly
            // retaining them, or b) routed their packets through vanilla that reuses packet instances, or c) performed
            // incorrect reference counting. This is a bug on their side. Known mods:
            // - Ancient Warfare 2: problem b). Patched with AncientWarfareNetworkTransformer.
            payload.release();
        }
        return true;
    }
}
