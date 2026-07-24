package com.hbm.packet;

import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionData;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.potion.HbmPotion;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteSavedData;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility for permanently synchronizing values every tick with a player in the given context of a world.
 * Uses the Byte Buffer directly instead of NBT to cut back on unnecessary data.
 * @author hbm
 */
public class PermaSyncHandler {

    // Versioned packet format — sections are flag-gated so unchanged data is skipped per player
    // A reserved byte (0) after the section data acts as a protocol separator for afterReadPacket mixins
    private static final byte PERMA_SYNC_VERSION = 1;
    private static final byte FLAG_DEATH      = 1;
    private static final byte FLAG_POLLUTION  = 2;
    private static final byte FLAG_SATELLITES = 4;
    private static final byte FLAG_RIDING     = 8;

    // Client-side state (read from readPacket on client)
    public static IntOpenHashSet boykissers = new IntOpenHashSet();
    public static float[] pollution = new float[PollutionType.VALUES.length];

    // Thread-safe world-global cache (volatile for safe publication from world tick thread)
    private static volatile CacheSnapshot cache = CacheSnapshot.EMPTY;

    private record CacheSnapshot(IntArrayList deathPlayerIds, int deathHash, long satHash) {
        static final CacheSnapshot EMPTY = new CacheSnapshot(new IntArrayList(), 0, 0);
    }

    // Per-player sync state
    private static final ConcurrentHashMap<UUID, PlayerSyncState> playerStates = new ConcurrentHashMap<>();

    private static class PlayerSyncState {
        int lastDeathHash;
        long lastSatHash;
        int lastRidingId;
    }

    public static void tickCache(World world) {

        /// SHITTY MEMES ///
        IntArrayList ids = new IntArrayList();
        for(EntityPlayer p : world.playerEntities) {
            if(p.isPotionActive(HbmPotion.death)) {
                ids.add(p.getEntityId());
            }
        }
        int deathHash = ids.hashCode();

        /// SATELLITES ///
        Int2ObjectOpenHashMap<Satellite> sats = SatelliteSavedData.getData(world).sats;
        long satHash = 0;
        ObjectIterator<Int2ObjectMap.Entry<Satellite>> iter = sats.int2ObjectEntrySet().fastIterator();
        while(iter.hasNext()) {
            Int2ObjectMap.Entry<Satellite> entry = iter.next();
            satHash = 31 * satHash + entry.getIntKey();
            satHash = 31 * satHash + entry.getValue().getID();
        }

        cache = new CacheSnapshot(ids, deathHash, satHash);
    }

    public static boolean shouldSend(EntityPlayerMP player) {
        UUID uuid = player.getUniqueID();
        PlayerSyncState state = playerStates.get(uuid);
        if(state == null) return true;

        CacheSnapshot snap = cache;
        int offset = Math.abs(player.getEntityId());
        int tick = player.ticksExisted + offset;

        if(tick % 20 == 0 || state.lastDeathHash != snap.deathHash) return true;
        if(tick % 5 == 0) return true;
        if(tick % 100 == 0 || state.lastSatHash != snap.satHash) return true;
        int ridingId = player.getRidingEntity() != null ? player.getRidingEntity().getEntityId() : -1;
        if(tick % 20 == 0 || state.lastRidingId != ridingId) return true;

        return false;
    }

    public static void writePacket(ByteBuf buf, World world, EntityPlayerMP player) {
        UUID uuid = player.getUniqueID();
        PlayerSyncState state = playerStates.get(uuid);
        boolean isNew = false;
        if(state == null) {
            state = new PlayerSyncState();
            playerStates.put(uuid, state);
            isNew = true;
        }

        int verIndex = buf.writerIndex();
        buf.writeByte(PERMA_SYNC_VERSION);
        int lenIndex = buf.writerIndex();
        buf.writeShort(0);
        int flagsIndex = buf.writerIndex();
        buf.writeByte(0);

        byte flags = 0;
        int startIndex = buf.writerIndex();

        int offset = Math.abs(player.getEntityId());
        int tick = player.ticksExisted + offset;

        /// SHITTY MEMES ///
        CacheSnapshot snap = cache;
        if(isNew || tick % 20 == 0 || state.lastDeathHash != snap.deathHash) {
            flags |= FLAG_DEATH;
            state.lastDeathHash = snap.deathHash;
            IntArrayList ids = snap.deathPlayerIds;
            buf.writeShort((short) ids.size());
            IntListIterator it = ids.iterator();
            while(it.hasNext()) {
                buf.writeInt(it.nextInt());
            }
        }

        /// POLLUTION ///
        if(isNew || tick % 5 == 0) {
            flags |= FLAG_POLLUTION;
            PollutionData pd = PollutionHandler.getPollutionData(world, player.getPosition());
            if(pd == null) pd = new PollutionData();
            for(int i = 0; i < PollutionType.VALUES.length; i++) {
                buf.writeFloat(pd.pollution[i]);
            }
        }

        /// SATELLITES ///
        if(isNew || tick % 100 == 0 || state.lastSatHash != snap.satHash) {
            flags |= FLAG_SATELLITES;
            state.lastSatHash = snap.satHash;
            Int2ObjectOpenHashMap<Satellite> sats = SatelliteSavedData.getData(world).sats;
            buf.writeInt(sats.size());
            ObjectIterator<Int2ObjectMap.Entry<Satellite>> sit = sats.int2ObjectEntrySet().fastIterator();
            while(sit.hasNext()) {
                Int2ObjectMap.Entry<Satellite> entry = sit.next();
                buf.writeInt(entry.getIntKey());
                buf.writeInt(entry.getValue().getID());
            }
        }

        /// RIDING DESYNC FIX ///
        int ridingId = player.getRidingEntity() != null ? player.getRidingEntity().getEntityId() : -1;
        if(isNew || tick % 20 == 0 || state.lastRidingId != ridingId) {
            flags |= FLAG_RIDING;
            state.lastRidingId = ridingId;
            buf.writeInt(ridingId);
        }

		int endIndex = buf.writerIndex();

		buf.setByte(verIndex, PERMA_SYNC_VERSION);
		buf.setShort(lenIndex, endIndex - startIndex);
		buf.setByte(flagsIndex, flags);

		// Reserved byte for forward compat — not included in sectionLength, so readPacket
		// leaves it for afterReadPacket / future-version mixin callbacks.
		buf.writeByte(0);
	}

    public static void readPacket(ByteBuf buf, World world, EntityPlayer player) {
        int version = buf.readByte();
        if(version != PERMA_SYNC_VERSION) return;

        int sectionLength = buf.readUnsignedShort();
        // +1 skips the reserved byte after section data — this lands on the protocol separator
        // so afterReadPacket mixins can read their own data from the reserved byte position
        int endIndex = buf.readerIndex() + sectionLength + 1;

        byte flags = buf.readByte();

        /// SHITTY MEMES ///
        if((flags & FLAG_DEATH) != 0) {
            boykissers.clear();
            int count = buf.readUnsignedShort();
            for(int i = 0; i < count; i++) {
                boykissers.add(buf.readInt());
            }
        }

        /// POLLUTION ///
        if((flags & FLAG_POLLUTION) != 0) {
            for(int i = 0; i < PollutionType.VALUES.length; i++) {
                pollution[i] = buf.readFloat();
            }
        }

        if(buf.readerIndex() >= endIndex) return;

        /// SATELLITES ///
        if((flags & FLAG_SATELLITES) != 0) {
            int satSize = buf.readInt();
            Int2ObjectOpenHashMap<Satellite> sats = new Int2ObjectOpenHashMap<>();
            for(int i = 0; i < satSize; i++) {
                sats.put(buf.readInt(), Satellite.create(buf.readInt()));
            }
            SatelliteSavedData.setClientSats(sats);
        }

        if(buf.readerIndex() >= endIndex) return;

        /// RIDING DESYNC FIX ///
        if((flags & FLAG_RIDING) != 0) {
            int ridingId = buf.readInt();
            if(ridingId >= 0 && player.getRidingEntity() == null) {
                Entity entity = world.getEntityByID(ridingId);
                player.startRiding(entity);
            }
        }

        buf.readerIndex(Math.min(endIndex, buf.writerIndex()));
    }
}
