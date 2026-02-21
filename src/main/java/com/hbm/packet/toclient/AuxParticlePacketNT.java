package com.hbm.packet.toclient;

import com.hbm.main.MainRegistry;
import com.hbm.packet.threading.ThreadedPacket;
import com.hbm.util.I18nUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

public class AuxParticlePacketNT extends ThreadedPacket {

    private NBTTagCompound nbt;

    public AuxParticlePacketNT() {
    }

    public AuxParticlePacketNT(NBTTagCompound nbt, BlockPos pos) {
        this.nbt = nbt;
        this.nbt.setDouble("posX", pos.getX() + 0.5);
        this.nbt.setDouble("posY", pos.getY() + 0.5);
        this.nbt.setDouble("posZ", pos.getZ() + 0.5);
    }

    public AuxParticlePacketNT(NBTTagCompound nbt, double x, double y, double z) {
        this.nbt = nbt;
        this.nbt.setDouble("posX", x);
        this.nbt.setDouble("posY", y);
        this.nbt.setDouble("posZ", z);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer pbuf = new PacketBuffer(buf);
        try {
            this.nbt = pbuf.readCompoundTag();
        } catch (IOException e) {
            MainRegistry.logger.error("Failed to read NBT in AuxParticlePacketNT", e);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pbuf = new PacketBuffer(buf);
        pbuf.writeCompoundTag(this.nbt);
    }

    public static class Handler implements IMessageHandler<AuxParticlePacketNT, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(AuxParticlePacketNT m, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                if (Minecraft.getMinecraft().world == null) return;

                if (m.nbt != null) {
                    if (m.nbt.hasKey("label", Constants.NBT.TAG_STRING)) {
                        m.nbt.setString("label", I18nUtil.resolveKey(m.nbt.getString("label")));
                    }
                    MainRegistry.proxy.effectNT(m.nbt);
                }
            });
            return null;
        }
    }
}