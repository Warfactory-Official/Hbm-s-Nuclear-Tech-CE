package com.hbm.packet.toclient;

import com.hbm.main.MainRegistry;
import com.hbm.packet.threading.PrecompiledPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TomBroadcastPacket extends PrecompiledPacket {

    public static float cachedFire;
    public static float cachedDust;
    public static boolean cachedImpact;
    public static long cachedTime;

    public TomBroadcastPacket() { }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(cachedFire);
        buf.writeFloat(cachedDust);
        buf.writeBoolean(cachedImpact);
        buf.writeLong(cachedTime);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.out = buf.retain();
    }

    private ByteBuf out;

    public static class Handler implements IMessageHandler<TomBroadcastPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(TomBroadcastPacket m, MessageContext ctx) {
            try {
                if(m.out == null) return null;
                float fire = m.out.readFloat();
                float dust = m.out.readFloat();
                boolean impact = m.out.readBoolean();
                long time = m.out.readLong();

                com.hbm.handler.ImpactWorldHandler.lastSyncWorld = Minecraft.getMinecraft().world;
                com.hbm.handler.ImpactWorldHandler.fire = fire;
                com.hbm.handler.ImpactWorldHandler.dust = dust;
                com.hbm.handler.ImpactWorldHandler.impact = impact;
                com.hbm.handler.ImpactWorldHandler.time = time;
            } catch(Exception x) {
                MainRegistry.logger.catching(x);
            } finally {
                if(m.out != null) m.out.release();
            }
            return null;
        }
    }
}
