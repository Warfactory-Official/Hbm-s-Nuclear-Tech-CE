package com.hbm.packet.toclient;

import com.hbm.items.machine.ItemCassette.SoundType;
import com.hbm.items.machine.ItemCassette.TrackType;
import com.hbm.sound.SoundLoopSiren;
import com.hbm.tileentity.machine.TileEntityMachineSiren;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TESirenPacket implements IMessage {

    int x;
    int y;
    int z;
    int id;
    boolean active;

    public TESirenPacket() {

    }

    public TESirenPacket(int x, int y, int z, int id, boolean active) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
        this.active = active;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        id = buf.readInt();
        active = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(id);
        buf.writeBoolean(active);
    }

    public static class Handler implements IMessageHandler<TESirenPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(TESirenPacket m, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                BlockPos pos = new BlockPos(m.x, m.y, m.z);
                SoundLoopSiren existingSound = null;
                for (SoundLoopSiren s : SoundLoopSiren.list) {
                    if (s.getTE() != null && !s.getTE().isInvalid() && s.getTE().getPos().equals(pos)) {
                        existingSound = s;
                        break;
                    }
                }

                if (!m.active) {
                    if (existingSound != null) {
                        existingSound.endSound();
                        SoundLoopSiren.list.remove(existingSound);
                    }
                    return;
                }

                TileEntity te = Minecraft.getMinecraft().world.getTileEntity(pos);
                if (!(te instanceof TileEntityMachineSiren)) {
                    if (existingSound != null) {
                        existingSound.endSound();
                        SoundLoopSiren.list.remove(existingSound);
                    }
                    return;
                }

                TrackType track = TrackType.byIndex(m.id);
                SoundEvent soundLocation = track.getSoundLocation();

                if (soundLocation == null || track == TrackType.NULL) {
                    if (existingSound != null) {
                        existingSound.endSound();
                        SoundLoopSiren.list.remove(existingSound);
                    }
                    return;
                }

                if (existingSound == null) {
                    SoundLoopSiren newSound = new SoundLoopSiren(soundLocation, te, track.getType());
                    newSound.setRepeat(track.getType() == SoundType.LOOP);
                    newSound.intendedVolume = track.getVolume();
                    Minecraft.getMinecraft().getSoundHandler().playSound(newSound);
                } else {
                    String newPath = soundLocation.getSoundName().toString();
                    if (!existingSound.getPath().equals(newPath)) {
                        existingSound.endSound();
                        SoundLoopSiren.list.remove(existingSound);

                        SoundLoopSiren newSound = new SoundLoopSiren(soundLocation, te, track.getType());
                        newSound.setRepeat(track.getType() == SoundType.LOOP);
                        newSound.intendedVolume = track.getVolume();
                        Minecraft.getMinecraft().getSoundHandler().playSound(newSound);
                    } else {
                        existingSound.intendedVolume = track.getVolume();
                    }
                }
            });
            return null;
        }
    }
}