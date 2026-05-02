package com.hbm.packet.toclient;

import com.hbm.main.MainRegistry;
import com.hbm.packet.threading.ThreadedPacket;
import com.hbm.particle.helper.HbmEffectNT;
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
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class AuxParticlePacketNT extends ThreadedPacket {

    private HbmEffectNT effect;
    private NBTTagCompound nbt;
    private double posX, posY, posZ;
    private boolean legacy = false;

    public AuxParticlePacketNT() {}

    @Deprecated
    public AuxParticlePacketNT(NBTTagCompound nbt, BlockPos pos) {
        this(nbt, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
    }

    @Deprecated
    public AuxParticlePacketNT(NBTTagCompound nbt, double x, double y, double z) {
        this.legacy = true;
        this.nbt = nbt;
        this.nbt.setDouble("posX", x);
        this.nbt.setDouble("posY", y);
        this.nbt.setDouble("posZ", z);
    }

    public AuxParticlePacketNT(HbmEffectNT effect, @Nullable NBTTagCompound nbt, BlockPos pos) {
        this.effect = effect;
        this.nbt = nbt;
        this.posX = pos.getX() + 0.5;
        this.posY = pos.getY() + 0.5;
        this.posZ = pos.getZ() + 0.5;
    }

    public AuxParticlePacketNT(HbmEffectNT effect, @Nullable NBTTagCompound nbt, double x, double y, double z) {
        this.effect = effect;
        this.nbt = nbt;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer pbuf = new PacketBuffer(buf);
        try {
            if (pbuf.readBoolean()) {
                this.nbt = pbuf.readCompoundTag();
            } else {
                this.effect = pbuf.readEnumValue(HbmEffectNT.class);
                this.nbt = pbuf.readCompoundTag();
                this.posX = pbuf.readDouble();
                this.posY = pbuf.readDouble();
                this.posZ = pbuf.readDouble();
            }
        } catch (IOException e) {
            MainRegistry.logger.error("Failed to read NBT in AuxParticlePacketNT", e);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pbuf = new PacketBuffer(buf);
        pbuf.writeBoolean(this.legacy);
        if (legacy) pbuf.writeCompoundTag(this.nbt);
        else {
            pbuf.writeEnumValue(this.effect);
            pbuf.writeCompoundTag(this.nbt);
            pbuf.writeDouble(this.posX);
            pbuf.writeDouble(this.posY);
            pbuf.writeDouble(this.posZ);
        }
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
                    if (m.legacy) MainRegistry.proxy.effectNT(m.nbt);
                    else MainRegistry.proxy.effectNT(m.effect, m.posX, m.posY, m.posZ, m.nbt);
                }
            });
            return null;
        }
    }
}