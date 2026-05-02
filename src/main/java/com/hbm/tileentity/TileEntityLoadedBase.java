package com.hbm.tileentity;

import com.hbm.api.tile.ILoadedTile;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.lib.Library;
import com.hbm.packet.toclient.BufPacket;
import com.hbm.sound.AudioWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TileEntityLoadedBase extends TileEntity implements ILoadedTile, IBufPacketReceiver {
    private static final ByteBuf UPDATE_TAG_SCRATCH = Unpooled.buffer(64);

    public boolean isLoaded = true;
    public boolean muffled = false;

    protected boolean hasDataChanged = true;
    private long lastPackedBufHash = 0L;

    /**
     * @return if the tileEntity is loaded. Note that even if it's loaded, it may be invalid!
     */
    @Override
    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        isLoaded = true;
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        isLoaded = false;
    }

    /**
     * The "chunks is modified, pls don't forget to save me" effect of markDirty, minus the block updates
     */
    public void markChanged() {
        world.markChunkDirty(pos, this);
    }

    public AudioWrapper createAudioLoop() {
        return null;
    } //Vidarin: Remember to override this if you use rebootAudio!!

    public AudioWrapper rebootAudio(AudioWrapper wrapper) {
        wrapper.stopSound();
        AudioWrapper audio = createAudioLoop();
        audio.startSound();
        return audio;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        muffled = nbt.getBoolean("muffled");
        hasDataChanged = true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("muffled", muffled);
        return super.writeToNBT(nbt);
    }

    public float getVolume(float baseVolume) {
        return muffled ? baseVolume * 0.1F : baseVolume;
    }

    public void setMuffled(boolean muffled) {
        this.muffled = muffled;
        dataChanged();
    }

    public void dataChanged() {
        hasDataChanged = true;
    }

    @Override
    public final NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        UPDATE_TAG_SCRATCH.clear();
        serializeInitial(UPDATE_TAG_SCRATCH);
        byte[] bytes = new byte[UPDATE_TAG_SCRATCH.readableBytes()];
        UPDATE_TAG_SCRATCH.readBytes(bytes);
        tag.setByteArray("hbmSync", bytes);
        return tag;
    }

    @Override
    public final void handleUpdateTag(@NotNull NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        if (tag.hasKey("hbmSync")) {
            ByteBuf buf = Unpooled.wrappedBuffer(tag.getByteArray("hbmSync"));
            deserializeInitial(buf);
        }
    }

    @Nullable
    @Override
    public final SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public final void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        handleUpdateTag(pkt.getNbtCompound());
    }

    /**
     * {@inheritDoc}
     * only call super.serialize() on noisy machines. It has no effect on others.<br>
     * The final ByteBuf is compared with previous packets sent in order to avoid unnecessary traffic.<br>
     * A side effect of this is that compilation effectively runs on server thread, instead of PacketThreading IO thread;
     * Override {@link #networkPackNT(int)} if this behavior is undesirable.
     */
    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(muffled);
    }

    /**
     * {@inheritDoc}
     * only call super.deserialize() on noisy machines. It has no effect on others.<br>
     * This happens on the <strong>Netty Client IO thread</strong>!
     * Direct List modification is guaranteed to produce a CME.<br>
     */
    @Override
    public void deserialize(ByteBuf buf) {
        muffled = buf.readBoolean();
    }

    /**
     * Payload emitted once per chunk-load sync via {@link #getUpdateTag()}. Defaults to the
     * per-tick {@link #serialize(ByteBuf)} payload so TEs that sync everything per-tick need no
     * extra work.
     */
    public void serializeInitial(ByteBuf buf) {
        serialize(buf);
    }

    /**
     * Symmetric counterpart to {@link #serializeInitial(ByteBuf)}. Invoked from
     * {@link #handleUpdateTag(NBTTagCompound)} on the main client thread during chunk data
     * resolution, after the standard NBT path has zeroed subclass fields, so it must not depend
     * on pre-existing field values.
     */
    public void deserializeInitial(ByteBuf buf) {
        deserialize(buf);
    }

    /**
     * Sends a sync packet that uses ByteBuf for efficient information-cramming
     */
    public void networkPackNT(int range) {
        if (world.isRemote) return;

        BufPacket packet = new BufPacket(pos.getX(), pos.getY(), pos.getZ(), this);
        ByteBuf preBuf = packet.getCompiledBuffer();

        long preHash = Library.fnv1a64(preBuf);
        if (preHash == lastPackedBufHash) {
            packet.releaseBuffer();
            return;
        }

        lastPackedBufHash = preHash;
        PacketThreading.createAllAroundThreadedPacket(packet,
                new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(),
                        range));
    }

    /**
     * Sends a sync packet, skipping compilation entirely when data has not changed.
     * <p>
     * TEs using this must call {@link #dataChanged()} whenever any synced field changes.
     * Failing to do so will cause clients to never receive the update.
     */
    public void networkPackMK2(int range) {
        if (world.isRemote) return;

        if (!hasDataChanged) return;

        BufPacket packet = new BufPacket(pos.getX(), pos.getY(), pos.getZ(), this);
        PacketThreading.createAllAroundThreadedPacket(packet,
                new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(),
                        range));
        hasDataChanged = false;
    }

}
