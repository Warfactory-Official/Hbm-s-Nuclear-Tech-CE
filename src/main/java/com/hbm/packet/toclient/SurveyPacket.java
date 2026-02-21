package com.hbm.packet.toclient;

import com.hbm.packet.threading.ThreadedPacket;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SurveyPacket extends ThreadedPacket {
    private int rbmkHeight;

    public SurveyPacket() {
    }

    public SurveyPacket(int height) {
        this.rbmkHeight = height;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        rbmkHeight = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(rbmkHeight);
    }

    public static class Handler implements IMessageHandler<SurveyPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(SurveyPacket m, MessageContext ctx) {
            String h = String.valueOf(m.rbmkHeight);
            Minecraft.getMinecraft().addScheduledTask(() -> {
                WorldClient w = Minecraft.getMinecraft().world;
                if (w == null) return;
                w.getGameRules().setOrCreateGameRule(RBMKDials.RBMKKeys.KEY_COLUMN_HEIGHT.keyString, h);
            });
            return null;
        }
    }
}
