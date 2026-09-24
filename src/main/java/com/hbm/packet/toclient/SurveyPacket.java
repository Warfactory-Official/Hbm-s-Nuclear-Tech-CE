package com.hbm.packet.toclient;

import com.hbm.packet.threading.ThreadedPacket;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SurveyPacket extends ThreadedPacket {
    // Raw dialColumnHeight gamerule value, i.e. total stacked block count including the core block.
    private int columnHeightRuleValue;

    public SurveyPacket() {
    }

    public SurveyPacket(int columnHeightRuleValue) {
        this.columnHeightRuleValue = columnHeightRuleValue;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        columnHeightRuleValue = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(columnHeightRuleValue);
    }

    public static class Handler implements IMessageHandler<SurveyPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(SurveyPacket m, MessageContext ctx) {
            String h = String.valueOf(m.columnHeightRuleValue);
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Minecraft mc = Minecraft.getMinecraft();
                WorldClient w = mc.world;
                if (w == null) return;
                RBMKDials.updateClientColumnHeightRuleValue(w, m.columnHeightRuleValue);
                w.getGameRules().setOrCreateGameRule(RBMKDials.RBMKKeys.KEY_COLUMN_HEIGHT.keyString, h);
                if (mc.player == null) return;

                int chunkRadius = mc.gameSettings.renderDistanceChunks;
                int chunkX = MathHelper.floor(mc.player.posX / 16.0D);
                int chunkZ = MathHelper.floor(mc.player.posZ / 16.0D);
                w.markBlockRangeForRenderUpdate(
                        (chunkX - chunkRadius) << 4,
                        0,
                        (chunkZ - chunkRadius) << 4,
                        ((chunkX + chunkRadius) << 4) + 15,
                        255,
                        ((chunkZ + chunkRadius) << 4) + 15
                );
            });
            return null;
        }
    }
}
