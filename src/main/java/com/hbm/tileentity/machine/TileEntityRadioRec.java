package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.network.RTTYSystem;
import com.hbm.util.BufferUtil;
import com.hbm.util.NoteBuilder;
import com.hbm.util.NoteBuilder.Instrument;
import com.hbm.util.NoteBuilder.Note;
import com.hbm.util.NoteBuilder.Octave;
import com.hbm.util.Tuple;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityRadioRec extends TileEntityLoadedBase implements ITickable, IControlReceiver {

    public String channel = "";
    public boolean isOn = false;

    @Override
    public void update() {

        if (!world.isRemote) {

            if (this.isOn && !this.channel.isEmpty()) {
                RTTYSystem.RTTYChannel chan = RTTYSystem.listen(world, this.channel);

                if (chan != null && chan.timeStamp == world.getTotalWorldTime() - 1) {
                    Tuple.Triplet<Instrument, Note, Octave>[] notes = NoteBuilder.translate(chan.signal + "");

                    for (Tuple.Triplet<Instrument, Note, Octave> note : notes) {
                        Instrument i = note.getX();
                        Note n = note.getY();
                        Octave o = note.getZ();

                        int noteId = n.ordinal() + o.ordinal() * 12;
                        SoundEvent soundEvent = SoundEvents.BLOCK_NOTE_HARP;

                        if (i == Instrument.BASSDRUM) soundEvent = SoundEvents.BLOCK_NOTE_BASEDRUM;
                        if (i == Instrument.SNARE) soundEvent = SoundEvents.BLOCK_NOTE_SNARE;
                        if (i == Instrument.CLICKS) soundEvent = SoundEvents.BLOCK_NOTE_HAT;
                        if (i == Instrument.BASSGUITAR) soundEvent = SoundEvents.BLOCK_NOTE_BASS;

                        float f = (float) Math.pow(2.0D, (double) (noteId - 12) / 12.0D);
                        world.playSound(null, pos.add(0.0F, 0.5F, 0.0F), soundEvent, SoundCategory.BLOCKS, 3.0F, f);
                    }
                }
            }

            networkPackNT(15);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        BufferUtil.writeString(buf, this.channel);
        buf.writeBoolean(this.isOn);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.channel = BufferUtil.readString(buf);
        this.isOn = buf.readBoolean();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        channel = nbt.getString("channel");
        isOn = nbt.getBoolean("isOn");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setString("channel", channel);
        nbt.setBoolean("isOn", isOn);
        return nbt;
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return player.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 16D;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if (data.hasKey("channel")) this.channel = data.getString("channel");
        if (data.hasKey("isOn")) this.isOn = data.getBoolean("isOn");

        this.markDirty();
    }
}
