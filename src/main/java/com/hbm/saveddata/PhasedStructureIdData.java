package com.hbm.saveddata;

import com.hbm.main.MainRegistry;
import com.hbm.world.phased.PhasedStructureRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A light-weight registry implementation for per-save ID/String mappings for phased structures stored in hbm_ce_phased.dat
 * Ensures consistent serialization when structures are added/removed by addon makers.
 * MUST NOT BE SYNC'd TO CLIENTS!!!
 */
public class PhasedStructureIdData {

    private static final String FILE_NAME = "hbm_ce_phased.dat";
    private static final int VERSION = 2;
    private static final String BACKUP_SUFFIX = ".bak";
    private static final String TEMP_SUFFIX = ".tmp";
    private static final int MAX_KEYS = 10_000_000;
    private static final int MAX_STRINGS = 1_000_000;

    private final Object2IntOpenHashMap<String> keyToId = new Object2IntOpenHashMap<>(64);
    private final Int2ObjectOpenHashMap<String> idToKey = new Int2ObjectOpenHashMap<>(64);
    private final Object2IntOpenHashMap<String> stringToId = new Object2IntOpenHashMap<>(128);
    private final Int2ObjectOpenHashMap<String> idToString = new Int2ObjectOpenHashMap<>(128);
    private int nextId = 0;
    private int nextStringId = 0;
    private int epoch = 0;

    private File saveFile;
    private boolean dirty = false;

    /**
     * Load or create ID mappings for the given server.
     * <p>
     * Load sequence:
     * - WorldServer is constructed and registered in DimensionManager (WorldServer ctor).
     * - WorldServer.initialize(...) runs before WorldEvent.Load.
     * - WorldEvent.Load is posted for each dimension.
     * - initialWorldChunkLoad() runs (spawn chunks load and NBT is read).
     * <p>
     * We must initialize before spawn chunk NBT deserializes, but after the overworld save handler exists.
     * DimensionManager.getWorld(0, false) returns the already-constructed overworld without forcing a load.
     */
    public PhasedStructureIdData(MinecraftServer server) {
        keyToId.defaultReturnValue(-1);
        stringToId.defaultReturnValue(-1);
        WorldServer world = DimensionManager.getWorld(0, false);
        if (world == null) {
            throw new IllegalStateException("PhasedStructureIdData accessed before overworld is loaded for " + server);
        }
        saveFile = new File(world.getSaveHandler().getWorldDirectory(), FILE_NAME);
        loadFromDisk();
        ensureAllRegistered();
        flush();
    }

    public boolean isDirty() {
        return dirty;
    }

    private static ReadResult readFile(File file) {
        int loadedEpoch = -1;

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);
             GZIPInputStream gis = new GZIPInputStream(bis);
             DataInputStream in = new DataInputStream(gis)) {

            if (in.readInt() != VERSION) {
                return ReadResult.failed(-1);
            }

            loadedEpoch = in.readInt();

            int count = in.readInt();
            if (count < 0 || count > MAX_KEYS) {
                return ReadResult.failed(loadedEpoch);
            }

            Object2IntOpenHashMap<String> keyToId = new Object2IntOpenHashMap<>(Math.max(64, count));
            keyToId.defaultReturnValue(-1);
            Int2ObjectOpenHashMap<String> idToKey = new Int2ObjectOpenHashMap<>(Math.max(64, count));
            for (int i = 0; i < count; i++) {
                String key = in.readUTF();
                if (!key.isEmpty()) {
                    keyToId.put(key, i);
                    idToKey.put(i, key);
                }
            }

            int stringCount = in.readInt();
            if (stringCount < 0 || stringCount > MAX_STRINGS) {
                return ReadResult.failed(loadedEpoch);
            }

            Object2IntOpenHashMap<String> stringToId = new Object2IntOpenHashMap<>(Math.max(128, stringCount));
            stringToId.defaultReturnValue(-1);
            Int2ObjectOpenHashMap<String> idToString = new Int2ObjectOpenHashMap<>(Math.max(128, stringCount));
            for (int i = 0; i < stringCount; i++) {
                String s = in.readUTF();
                if (!s.isEmpty()) {
                    stringToId.put(s, i);
                    idToString.put(i, s);
                }
            }

            return new ReadResult(true, loadedEpoch, count, stringCount, keyToId, idToKey, stringToId, idToString);
        } catch (Exception e) {
            MainRegistry.logger.warn("[PhasedStructureIdData] Failed to deserialize file {}: {}", file, e.getMessage());
            return ReadResult.failed(loadedEpoch);
        }
    }

    private static void moveFile(File source, File target) throws Exception {
        try {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception ex) {
            MainRegistry.logger.warn("[PhasedStructureIdData] Failed to move file {} to {}: {}", source, target, ex.getMessage());
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void loadFromDisk() {
        if (saveFile == null) return;

        File dir = saveFile.getParentFile();
        File backupFile = new File(dir, FILE_NAME + BACKUP_SUFFIX);
        File tempFile = new File(dir, FILE_NAME + TEMP_SUFFIX);

        if (!saveFile.exists() && !backupFile.exists() && !tempFile.exists()) return;
        ReadResult temp = tempFile.exists() ? readFile(tempFile) : ReadResult.missing();
        if (temp.ok) {
            applyResult(temp);
            dirty = true;
            try {
                Files.deleteIfExists(tempFile.toPath());
            } catch (IOException _) {
            }
            MainRegistry.logger.warn("[PhasedStructureIdData] Recovered ID mappings from temp file, will rewrite canonical.");
            return;
        }

        ReadResult primary = saveFile.exists() ? readFile(saveFile) : ReadResult.missing();
        if (primary.ok) {
            applyResult(primary);
            return;
        }

        ReadResult backup = backupFile.exists() ? readFile(backupFile) : ReadResult.missing();
        if (backup.ok) {
            applyResult(backup);
            dirty = true; // restore primary on next save
            MainRegistry.logger.warn("[PhasedStructureIdData] Restored ID mappings from backup.");
            return;
        }

        int recoveredEpoch = Math.max(primary.epoch, backup.epoch);
        epoch = (recoveredEpoch >= 0 && recoveredEpoch < Integer.MAX_VALUE) ? (recoveredEpoch + 1) : Integer.MAX_VALUE;

        keyToId.clear();
        idToKey.clear();
        stringToId.clear();
        idToString.clear();
        nextId = 0;
        nextStringId = 0;
        dirty = true;

        MainRegistry.logger.warn("[PhasedStructureIdData] Failed to load registry, starting fresh with epoch {}.", epoch);
    }

    public void flush() {
        if (saveFile == null || !dirty) return;

        File dir = saveFile.getParentFile();
        try {
            writeFileAtomicGzip(new File(dir, FILE_NAME + TEMP_SUFFIX), saveFile, new File(dir, FILE_NAME + BACKUP_SUFFIX));
            dirty = false;
            MainRegistry.logger.debug("[PhasedStructureIdData] Saved {} ID mappings, {} string mappings (epoch={})", nextId, nextStringId, epoch);
        } catch (Exception e) {
            MainRegistry.logger.warn("[PhasedStructureIdData] Failed to save: {}", e.getMessage());
        }
    }

    private void ensureAllRegistered() {
        for (String key : PhasedStructureRegistry.getAllKeys()) {
            if (keyToId.getInt(key) >= 0) continue;

            int newId = nextId++;
            keyToId.put(key, newId);
            idToKey.put(newId, key);
            dirty = true;
        }
    }

    public int getEpoch() {
        return epoch;
    }

    private void applyResult(ReadResult result) {
        keyToId.clear();
        idToKey.clear();
        stringToId.clear();
        idToString.clear();

        keyToId.putAll(result.keyToId);
        idToKey.putAll(result.idToKey);
        stringToId.putAll(result.stringToId);
        idToString.putAll(result.idToString);

        nextId = result.nextId;
        nextStringId = result.nextStringId;
        epoch = result.epoch;

        MainRegistry.logger.debug("[PhasedStructureIdData] Loaded {} ID mappings, {} string mappings (epoch={})", nextId, nextStringId, epoch);
    }

    private void writeFileAtomicGzip(File tempFile, File target, File backup) throws Exception {
        if (tempFile.exists()) {
            Files.delete(tempFile.toPath());
        }

        try (FileOutputStream fos = new FileOutputStream(tempFile); BufferedOutputStream bos = new BufferedOutputStream(fos); GZIPOutputStream gz = new GZIPOutputStream(bos); DataOutputStream out = new DataOutputStream(gz)) {

            out.writeInt(VERSION);
            out.writeInt(epoch);

            out.writeInt(nextId);
            for (int i = 0; i < nextId; i++) {
                String key = idToKey.get(i);
                out.writeUTF(key != null ? key : "");
            }

            out.writeInt(nextStringId);
            for (int i = 0; i < nextStringId; i++) {
                String s = idToString.get(i);
                out.writeUTF(s != null ? s : "");
            }

            out.flush();
            gz.finish();
            bos.flush();
            fos.getChannel().force(true);
        } catch (Exception e) {
            MainRegistry.logger.warn("[PhasedStructureIdData] Failed to write file {}: {}", tempFile, e.getMessage());
            throw e;
        }

        if (target.exists()) {
            moveFile(target, backup);
        }
        moveFile(tempFile, target);
    }

    /**
     * Get ID for a key. Returns -1 if not mapped.
     * This ID, and the NBT holding it, MUST NOT BE SENT TO THE CLIENTS
     */
    public int getId(String key) {
        return keyToId.getInt(key);
    }

    /**
     * Get key for an ID. Returns null if not mapped.
     * MUST NOT BE CALLED ON LOGICAL CLIENT
     */
    public @Nullable String getKey(int id) {
        return idToKey.get(id);
    }

    /**
     * Get the max ID (exclusive). Used to size the id→Entry array.
     */
    public int getMaxId() {
        return nextId;
    }

    /**
     * Get ID for a string from the global string table. Registers if not present.
     * This ID, and the NBT holding it, MUST NOT BE SENT TO THE CLIENTS
     */
    public int getStringId(String s) {
        int id = stringToId.getInt(s);
        if (id >= 0) return id;
        id = nextStringId++;
        stringToId.put(s, id);
        idToString.put(id, s);
        dirty = true;
        return id;
    }

    /**
     * Get string for an ID from the global string table.
     * MUST NOT BE CALLED ON LOGICAL CLIENT
     */
    public @Nullable String getString(int id) {
        return idToString.get(id);
    }

    private record ReadResult(boolean ok, int epoch, int nextId, int nextStringId,
                              Object2IntOpenHashMap<String> keyToId, Int2ObjectOpenHashMap<String> idToKey,
                              Object2IntOpenHashMap<String> stringToId, Int2ObjectOpenHashMap<String> idToString) {
        static ReadResult failed() {
            return failed(-1);
        }

        static ReadResult failed(int epoch) {
            return new ReadResult(false, epoch, 0, 0, new Object2IntOpenHashMap<>(0), new Int2ObjectOpenHashMap<>(0), new Object2IntOpenHashMap<>(0),
                    new Int2ObjectOpenHashMap<>(0));
        }

        static ReadResult missing() {
            return failed();
        }
    }
}
