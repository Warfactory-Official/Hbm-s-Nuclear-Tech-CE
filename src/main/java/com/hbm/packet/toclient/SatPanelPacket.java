package com.hbm.packet.toclient;

import com.hbm.items.tool.ItemSatInterface;
import com.hbm.main.MainRegistry;
import com.hbm.packet.threading.PrecompiledPacket;
import com.hbm.saveddata.satellites.Satellite;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SatPanelPacket extends PrecompiledPacket {
    private int type;
    private NBTTagCompound nbt;
    private ByteBuf payload;

    public SatPanelPacket() {
    }

    public SatPanelPacket(Satellite sat) {
        this.type = sat.getID();
        this.nbt = new NBTTagCompound();
        sat.writeToNBT(this.nbt);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(type);
        new PacketBuffer(buf).writeCompoundTag(nbt);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.payload = buf.retain();
    }

    public static class Handler implements IMessageHandler<SatPanelPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(SatPanelPacket m, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                try {
                    PacketBuffer buffer = new PacketBuffer(m.payload);
                    int type = buffer.readInt();
                    NBTTagCompound nbt = buffer.readCompoundTag();
                    ItemSatInterface.currentSat = Satellite.create(type);
                    if (nbt != null && ItemSatInterface.currentSat != null) {
                        ItemSatInterface.currentSat.readFromNBT(nbt);
                    }
                } catch (Exception x) {
                    MainRegistry.logger.catching(x);
                } finally {
                    m.payload.release();
                }
            });
            return null;
        }
    }
}
