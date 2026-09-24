package com.hbm.mixin;

import com.hbm.interfaces.IMixinFMLProxyPacket;
import com.hbm.util.NetworkUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = FMLProxyPacket.class, remap = false)
public abstract class MixinFMLProxyPacket implements IMixinFMLProxyPacket {
    @Unique
    private static final int PART_SIZE = 0x100000 - 0x50; // 1_048_496
    @Unique
    private static final int MAX_PARTS = 255;

    @Final
    @Shadow
    String channel;

    @Final
    @Shadow
    private PacketBuffer payload;

    @Inject(method = "toS3FPackets", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void hbm$toS3FPackets(CallbackInfoReturnable<List<Packet<INetHandlerPlayClient>>> cir) {
        if (!NetworkUtil.shouldHandleProxyPacket(channel)) return;
        cir.setReturnValue(hbm$toS3FPackets());
    }

    @Unique
    @Override
    public List<Packet<INetHandlerPlayClient>> hbm$toS3FPackets() {
        ByteBuf buf = payload;
        int len = buf.readableBytes();
        int ri = buf.readerIndex();

        ArrayList<Packet<INetHandlerPlayClient>> ret = new ArrayList<>(Math.min(4, (len / (PART_SIZE - 1)) + 2));

        try {
            if (len < PART_SIZE) {
                PacketBuffer pb = new PacketBuffer(buf.retainedSlice(ri, len));
                ret.add(new SPacketCustomPayload(channel, pb));
                return ret;
            }

            int parts = (int) Math.ceil(len / (double) (PART_SIZE - 1));
            if (parts > MAX_PARTS)
                throw new IllegalArgumentException("Payload too large (parts=" + parts + ", max=" + MAX_PARTS + ")");

            PacketBuffer preamble = new PacketBuffer(Unpooled.buffer());
            preamble.writeString(channel);
            preamble.writeByte(parts);
            preamble.writeInt(len);
            ret.add(new SPacketCustomPayload("FML|MP", preamble));

            int offset = 0;
            for (int x = 0; x < parts; x++) {
                int dataLen = Math.min(PART_SIZE - 1, len - offset);
                ByteBuf combined = Unpooled.wrappedBuffer(
                        Unpooled.buffer(1, 1).writeByte(x & 0xFF),
                        buf.retainedSlice(ri + offset, dataLen)
                );
                try {
                    ret.add(new SPacketCustomPayload("FML|MP", new PacketBuffer(combined)));
                } catch (Throwable t) {
                    combined.release();
                    throw t;
                }
                offset += dataLen;
            }

            return ret;
        } catch (Throwable t) {
            for (Packet<INetHandlerPlayClient> p : ret) NetworkUtil.releaseCustomPayloadData(p);
            throw t;
        }
    }
}
