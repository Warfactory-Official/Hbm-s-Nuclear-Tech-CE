package com.hbm.packet.toclient;

import com.hbm.capability.HbmCapability;
import com.hbm.capability.HbmCapability.IHBMData;
import com.hbm.capability.HbmLivingCapability.IEntityHbmProps;
import com.hbm.capability.HbmLivingProps;
import com.hbm.capability.HbmLivingProps.ContaminationEffect;
import com.hbm.main.MainRegistry;
import com.hbm.packet.threading.PrecompiledPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HbmPlayerSyncPacket extends PrecompiledPacket {

    private static final ContaminationEffect[] EMPTY_CONTAMINATION = new ContaminationEffect[0];

    IEntityHbmProps livingProps;
    IHBMData hbmData;
    ContaminationEffect[] contaminationSnapshot;
    ByteBuf buf;

    public HbmPlayerSyncPacket() {
    }

    public HbmPlayerSyncPacket(IEntityHbmProps livingProps, IHBMData hbmData) {
        this.livingProps = livingProps;
        this.hbmData = hbmData;
        this.contaminationSnapshot = livingProps.getContaminationEffectList().toArray(EMPTY_CONTAMINATION);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.buf = buf.retainedSlice();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        livingProps.serialize(buf, contaminationSnapshot);
        hbmData.serialize(buf);
    }

    public static class Handler implements IMessageHandler<HbmPlayerSyncPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(HbmPlayerSyncPacket m, MessageContext ctx) {
            if (m.buf == null) throw new NullPointerException();
            ByteBuf buf = m.buf;
            Minecraft.getMinecraft().addScheduledTask(() -> {
                try {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    if (Minecraft.getMinecraft().world == null || player == null) return;
                    HbmLivingProps.getData(player).deserialize(buf);
                    HbmCapability.getData(player).deserialize(buf);
                } catch (Exception e) {
                    MainRegistry.logger.error("Failed to sync HBM player state", e);
                } finally {
                    buf.release();
                }
            });
            return null;
        }
    }
}
