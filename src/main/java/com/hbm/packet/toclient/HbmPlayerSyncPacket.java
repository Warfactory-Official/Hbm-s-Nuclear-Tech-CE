package com.hbm.packet.toclient;

import com.hbm.capability.HbmLivingProps.ContaminationEffect;
import com.hbm.main.MainRegistry;
import com.hbm.packet.HbmSyncHandler;
import com.hbm.packet.threading.PrecompiledPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HbmPlayerSyncPacket extends PrecompiledPacket {

    private static final ContaminationEffect[] EMPTY_CONTAMINATION = new ContaminationEffect[0];

    private EntityPlayerMP player;
    private byte flags;
    private ContaminationEffect[] contaminationSnapshot;
    private ByteBuf buf;

    public HbmPlayerSyncPacket() {
        this.contaminationSnapshot = EMPTY_CONTAMINATION;
    }

    public HbmPlayerSyncPacket(EntityPlayerMP player, byte flags) {
        this.player = player;
        this.flags = flags;
        this.contaminationSnapshot = (flags & HbmSyncHandler.FLAG_CONTAMINATION) != 0
            ? com.hbm.capability.HbmLivingProps.getData(player).getContaminationEffectList().toArray(EMPTY_CONTAMINATION)
            : EMPTY_CONTAMINATION;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.buf = buf.retainedSlice();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        HbmSyncHandler.writePacket(buf, flags, contaminationSnapshot, player);
    }

    public static class Handler implements IMessageHandler<HbmPlayerSyncPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(HbmPlayerSyncPacket m, MessageContext ctx) {
            if (m.buf == null) throw new NullPointerException();
            ByteBuf buf = m.buf;
            Minecraft.getMinecraft().addScheduledTask(() -> {
                try {
                    HbmSyncHandler.readPacket(buf);
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
