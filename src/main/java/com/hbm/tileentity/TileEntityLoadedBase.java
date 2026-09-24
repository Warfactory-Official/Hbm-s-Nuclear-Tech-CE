package com.hbm.tileentity;

import com.hbm.api.tile.ILoadedTile;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.packet.toclient.BufPacket;
import com.hbm.sound.AudioWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TileEntityLoadedBase extends TileEntity implements ILoadedTile, IBufPacketReceiver {
    public boolean isLoaded = true;
    public boolean muffled = false;
    public boolean tilted = false;
    public int tiltBlocksChecked = 0;
    public int tiltBlocksValid = 0;

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
        tilted = nbt.getBoolean("tilted");
        hasDataChanged = true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("muffled", muffled);
        nbt.setBoolean("tilted", tilted);
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
        ByteBuf scratch = Unpooled.buffer(64);
        serializeInitial(scratch);
        byte[] bytes = new byte[scratch.readableBytes()];
        scratch.readBytes(bytes);
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
        buf.writeBoolean(tilted);
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
        tilted = buf.readBoolean();
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

    public enum TiltType {
        UNAVOIDABLE, CONFIG
    }

    public void checkTilt(TiltType cfg, boolean extraHeavy) {
        boolean doesTilt = false;
        if (cfg == TiltType.UNAVOIDABLE) doesTilt = true;
        if (cfg == TiltType.CONFIG && GeneralConfig.enableMachineGravity) doesTilt = true;
        if (cfg == TiltType.CONFIG && GeneralConfig.enable528MachineGravity) doesTilt = true;

        if (!doesTilt) { this.tilted = false; return; }
        if (this.getFloorCount() <= 0) { this.tilted = false; return; }
        if ((world.getTotalWorldTime() + (pos.getY() + pos.getZ() * 27644437) * 27644437L + pos.getX()) % 20 != 0) return;

        if (this.tiltBlocksChecked >= this.getFloorCount()) {

            if (this.tiltBlocksValid >= this.tiltBlocksChecked * 0.95) {
                this.tilted = false;
            } else {
                if (!this.tilted) world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.metalImpact, SoundCategory.BLOCKS, 3F, 1F);
                this.tilted = true;
            }

            this.markChanged();
            this.tiltBlocksChecked = 0;
            this.tiltBlocksValid = 0;
        }

        BlockPos floorPos = getFloorPosFromIndex(this.tiltBlocksChecked);
        if (floorPos == null) return;

        IBlockState ground = world.getBlockState(floorPos);
        this.tiltBlocksChecked++;

        // for extra heavy machines, the ground needs to:
        // * be a fully solid block (side UP is checked for custom behavior)
        // * be opaque
        // * NOT be sand, cloth or ground material
        // * have an explosion resistance of stone or greater
        if (extraHeavy) {
            if (!ground.getMaterial().isSolid()) return;
            if (!ground.isNormalCube()) return;
            if (ground.getMaterial() == Material.SAND || ground.getMaterial() == Material.CLOTH || ground.getMaterial() == Material.GROUND) return;
            if (ground.getBlock().getExplosionResistance(null) < Blocks.STONE.getExplosionResistance(null)) return;
            this.tiltBlocksValid++;
        // for standard machines, the ground needs to:
        // * be solid at the top
        // * NOT be sand
        } else {
            if (!ground.isSideSolid(world, floorPos, EnumFacing.UP)) return;
            if (ground.getMaterial() == Material.SAND) return;
            Block block = ground.getBlock();
            if (block == ModBlocks.dirt_dead || block == ModBlocks.dirt_oily || block == ModBlocks.stone_cracked) return;
            this.tiltBlocksValid++;
        }
    }

    public int getFloorCount() { return 0; }
    public BlockPos getFloorPosFromIndex(int index) { return null; }

    public BlockPos standardFloor3x3(int index) {
        return new BlockPos(pos.getX() - 1 + (index / 2) * 2, pos.getY() - 1, pos.getZ() - 1 + (index % 2) * 2);
    }
    public BlockPos standardFloor5x5(int index) {
        return new BlockPos(pos.getX() - 2 + (index / 3) * 2, pos.getY() - 1, pos.getZ() - 2 + (index % 3) * 2);
    }
    public BlockPos standardFloor7x7(int index) {
        return new BlockPos(pos.getX() - 3 + (index / 4) * 2, pos.getY() - 1, pos.getZ() - 3 + (index % 4) * 2);
    }
}
