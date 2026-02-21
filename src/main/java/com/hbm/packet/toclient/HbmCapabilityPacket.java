package com.hbm.packet.toclient;

import com.hbm.capability.HbmCapability;
import com.hbm.capability.HbmCapability.IHBMData;
import com.hbm.main.MainRegistry;
import com.hbm.packet.threading.PrecompiledPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// mlbv: I have no fucking idea how the current system works and I dont want to waste time refactoring this whole clusterfuck
// So I decided to just make a new packet
// btw I dont know where the fuck were the HBMCapability got synced at
// TODO: figure it out and incorporate this into ExtPropPacket
public class HbmCapabilityPacket extends PrecompiledPacket {
    IHBMData pprps;
    ByteBuf buf;

    public HbmCapabilityPacket(){
    }

    public HbmCapabilityPacket(IHBMData pprps) {
        this.pprps = pprps;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.buf = buf.retainedSlice();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        pprps.serialize(buf);
    }

    public static class Handler implements IMessageHandler<HbmCapabilityPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(HbmCapabilityPacket m, MessageContext ctx) {
            if (m.buf == null) throw new NullPointerException();
            try {
                if (Minecraft.getMinecraft().world == null) return null;
                if (Minecraft.getMinecraft().player == null) return null;
                IHBMData pprps = HbmCapability.getData(Minecraft.getMinecraft().player);
                if (pprps != null) {
                    pprps.deserialize(m.buf);
                }
            } catch (Exception e) {
                MainRegistry.logger.error("Failed to sync HBM Capability", e);
            } finally {
                m.buf.release();
            }
            return null;
        }
    }
}
