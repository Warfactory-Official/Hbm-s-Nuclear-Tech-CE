package com.hbm.packet.toserver;

import com.hbm.items.IItemControlReceiver;
import com.hbm.packet.threading.PrecompiledPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public class NBTItemControlPacket extends PrecompiledPacket {
    private NBTTagCompound nbt;
    private ByteBuf payload;

    public NBTItemControlPacket() { }

    public NBTItemControlPacket(NBTTagCompound nbt) {
        this.nbt = nbt;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        new PacketBuffer(buf).writeCompoundTag(nbt);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.payload = buf.retain();
    }

    public static class Handler implements IMessageHandler<NBTItemControlPacket, IMessage> {

        @Override
        public IMessage onMessage(NBTItemControlPacket m, MessageContext ctx) {
            ctx.getServerHandler().player.server.addScheduledTask(() -> {
                try {
                    EntityPlayer p = ctx.getServerHandler().player;
                    if (p == null) return;
                    PacketBuffer buffer = new PacketBuffer(m.payload);
                    NBTTagCompound nbt = buffer.readCompoundTag();

                    if (nbt != null) {
                        ItemStack held = p.getHeldItem(p.getActiveHand());

                        if (!held.isEmpty() && held.getItem() instanceof IItemControlReceiver) {
                            ((IItemControlReceiver) held.getItem()).receiveControl(held, nbt);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    m.payload.release();
                }
            });

            return null;
        }
    }
}
