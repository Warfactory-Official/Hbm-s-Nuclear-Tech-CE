package com.hbm.packet.toclient;

import com.hbm.lib.HBMSoundHandler;
import com.hbm.sound.SoundLoopPlayer;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlayerSoundPacket implements IMessage {
    private byte id;
    private boolean restart;

    public enum SoundType {
        NULLRADAR;

        public static final SoundType[] VALUES = values();
    }

    public PlayerSoundPacket() {
    }

    public PlayerSoundPacket(SoundType type, boolean restart) {
        this.id = (byte)type.ordinal();
        this.restart = restart;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readByte();
        restart = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(id);
        buf.writeBoolean(restart);
    }

    public static class Handler implements IMessageHandler<PlayerSoundPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PlayerSoundPacket m, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                SoundType type = SoundType.VALUES[m.id];
                SoundLoopPlayer existingSound = SoundLoopPlayer.getPlayingSound(type);
                if (existingSound != null) {
                    if (m.restart) {
                        existingSound.endSound();
                        Minecraft.getMinecraft().addScheduledTask(() -> playSound(type));
                    } else {
                        existingSound.resetTimeout();
                    }
                    return;
                }
                playSound(type);
            });
            return null;
        }

        @SideOnly(Side.CLIENT)
        private void playSound(SoundType type) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (player == null) return;

            SoundEvent sound = switch (type) {
                case NULLRADAR -> HBMSoundHandler.alarmAirRaid;
            };
            if (sound != null) {
                SoundLoopPlayer newSound = new SoundLoopPlayer(sound, type, player);
                Minecraft.getMinecraft().getSoundHandler().playSound(newSound);
            }
        }
    }
}