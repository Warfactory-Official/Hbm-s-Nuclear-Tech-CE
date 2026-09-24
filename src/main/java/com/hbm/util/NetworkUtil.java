package com.hbm.util;

import com.hbm.Tags;
import com.hbm.config.GeneralConfig;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomPayload;
import org.spongepowered.asm.mixin.Unique;

public class NetworkUtil {

    public static boolean shouldHandleProxyPacket(String channel) {
        return !GeneralConfig.enableZeroCopyCompatibilityMode || Tags.MODID.equals(channel);
    }

    @Unique
    public static void releaseCustomPayloadData(Packet<?> pkt) {
        if (pkt instanceof SPacketCustomPayload sp) {
            PacketBuffer o = sp.data;
            if (o != null) {
                sp.data = null;
                o.release();
            }
        } else if (pkt instanceof CPacketCustomPayload cp) {
            PacketBuffer o = cp.data;
            if (o != null) {
                cp.data = null;
                o.release();
            }
        }
    }
}
