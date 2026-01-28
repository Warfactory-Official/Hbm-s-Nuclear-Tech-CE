package com.hbm.world.phased;

import com.hbm.lib.Library;
import com.hbm.saveddata.PhasedStructureIdData;
import com.hbm.world.*;
import com.hbm.world.dungeon.AncientTombStructure;
import com.hbm.world.dungeon.ArcticVault;
import com.hbm.world.dungeon.LibraryDungeon;
import com.hbm.world.feature.*;
import com.hbm.world.generator.JungleDungeonStructure;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Registry of all phased/dynamic structures using stable string keys and compact binary codecs.
 */
public final class PhasedStructureRegistry {

    private static final Object2ObjectOpenHashMap<String, Entry<? extends IPhasedStructure>> BY_KEY = new Object2ObjectOpenHashMap<>(64);
    private static final Reference2ObjectOpenHashMap<IPhasedStructure, String> BY_INSTANCE = new Reference2ObjectOpenHashMap<>(64);
    private static final Reference2ObjectOpenHashMap<Class<? extends IPhasedStructure>, String> BY_CLASS = new Reference2ObjectOpenHashMap<>(64);
    private static PhasedStructureIdData currentIdData;
    private static Entry<?>[] idToEntry;
    private static Reference2IntOpenHashMap<IPhasedStructure> instanceToId;
    private static Reference2IntOpenHashMap<Class<? extends IPhasedStructure>> classToId;
    private static int epoch;

    static {
        register("hbm:ancient_tomb", AncientTombStructure.INSTANCE);
        register("hbm:antenna", Antenna.INSTANCE);
        register("hbm:arctic_vault", ArcticVault.INSTANCE);
        register("hbm:barrel", Barrel.INSTANCE);
        register("hbm:bedrock_oil_deposit", BedrockOilDeposit.INSTANCE);
        register("hbm:bedrock_ore_overworld", BedrockOre.OVERWORLD);
        register("hbm:bedrock_ore_coltan", BedrockOre.COLTAN);
        register("hbm:bedrock_ore_nether_glowstone", BedrockOre.NETHER_GLOWSTONE);
        register("hbm:bedrock_ore_nether_quartz", BedrockOre.NETHER_QUARTZ);
        register("hbm:bedrock_ore_nether_powder_fire", BedrockOre.NETHER_POWDER_FIRE);
        register("hbm:bunker", Bunker.INSTANCE);
        register("hbm:desert_atom_001", DesertAtom001.INSTANCE);
        register("hbm:geyser", Geyser.INSTANCE);
        register("hbm:geyser_large", GeyserLarge.INSTANCE);
        register("hbm:glyphid_hive_infected", GlyphidHive.INFECTED);
        register("hbm:glyphid_hive_infected_noloot", GlyphidHive.INFECTED_NOLOOT);
        register("hbm:glyphid_hive_normal", GlyphidHive.NORMAL);
        register("hbm:glyphid_hive_normal_noloot", GlyphidHive.NORMAL_NOLOOT);
        register("hbm:jungle_dungeon", JungleDungeonStructure.INSTANCE);
        register("hbm:library_dungeon", LibraryDungeon.INSTANCE);
        register("hbm:flowers_foxglove", NTMFlowers.INSTANCE_FOXGLOVE);
        register("hbm:flowers_hemp", NTMFlowers.INSTANCE_HEMP);
        register("hbm:flowers_tobacco", NTMFlowers.INSTANCE_TOBACCO);
        register("hbm:flowers_nightshade", NTMFlowers.INSTANCE_NIGHTSHADE);
        register("hbm:radio_01", Radio01.INSTANCE);
        register("hbm:relay", Relay.INSTANCE);
        register("hbm:satellite", Satellite.INSTANCE);
        register("hbm:spaceship", Spaceship.INSTANCE);
        register("hbm:depth_deposit", DepthDeposit.class, DepthDeposit::readFromBuf, DepthDeposit::writeToBuf);
        register("hbm:oil_bubble", OilBubble.class, OilBubble::readFromBuf, OilBubble::writeToBuf);
        register("hbm:oil_sand_bubble", OilSandBubble.class, OilSandBubble::readFromBuf, OilSandBubble::writeToBuf);
        register("hbm:sellafield", Sellafield.class, Sellafield::readFromBuf, Sellafield::writeToBuf);
        register("hbm:minable_non_cascade", WorldGenMinableNonCascade.class, WorldGenMinableNonCascade::readFromBuf, WorldGenMinableNonCascade::writeToBuf);
    }

    private PhasedStructureRegistry() {
    }

    /**
     * Public API for registration. This can be done at any time before worldgen, e.g. postInit.
     */
    @ApiStatus.AvailableSince("1.5.2.0")
    public static <T extends IPhasedStructure> void register(String key, T instance) {
        if (BY_KEY.containsKey(key)) throw new IllegalStateException("Duplicate phased structure key: " + key);
        if (BY_INSTANCE.containsKey(instance))
            throw new IllegalStateException("Duplicate phased structure instance: " + instance);
        Entry<T> entry = new Entry<>(key, Library.fnv1a64(key), instance, _ -> instance, (_, _) -> {}, false);
        BY_KEY.put(key, entry);
        BY_INSTANCE.put(instance, key);
    }

    /**
     * Public API for registration. This can be done at any time before worldgen, e.g. postInit.
     */
    @ApiStatus.AvailableSince("1.5.2.0")
    public static <T extends IPhasedStructure> void register(String key, Class<T> clazz, Function<ByteBuf, T> reader, BiConsumer<T, ByteBuf> writer) {
        if (BY_KEY.containsKey(key)) throw new IllegalStateException("Duplicate phased structure key: " + key);
        if (BY_CLASS.containsKey(clazz))
            throw new IllegalStateException("Duplicate phased structure class: " + clazz.getName());
        Entry<T> entry = new Entry<>(key, Library.fnv1a64(key), null, reader, writer, true);
        BY_KEY.put(key, entry);
        BY_CLASS.put(clazz, key);
    }

    public static String getKey(IPhasedStructure structure) {
        String key = BY_INSTANCE.get(structure);
        if (key == null) key = BY_CLASS.get(structure.getClass());
        if (key == null) throw new IllegalArgumentException("Unknown phased structure: " + structure);
        return key;
    }

    public static void onServerStarting(MinecraftServer server) {
        // at this time all worlds has been loaded so it's fine
        currentIdData = PhasedStructureIdData.loadForServer(server);
        buildDirectMappings();
        epoch = currentIdData.getEpoch();
    }

    private static void buildDirectMappings() {
        int maxId = currentIdData.getMaxId();
        idToEntry = new Entry<?>[maxId];
        for (int id = 0; id < maxId; id++) {
            String key = currentIdData.getKey(id);
            if (key != null) {
                idToEntry[id] = BY_KEY.get(key);  // null if structure was removed
            }
        }
        instanceToId = new Reference2IntOpenHashMap<>(BY_INSTANCE.size());
        instanceToId.defaultReturnValue(-1);
        var iterator = BY_INSTANCE.reference2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            int id = currentIdData.getId(entry.getValue());
            if (id >= 0) {
                instanceToId.put(entry.getKey(), id);
            }
        }
        classToId = new Reference2IntOpenHashMap<>(BY_CLASS.size());
        classToId.defaultReturnValue(-1);
        var iter = BY_CLASS.reference2ObjectEntrySet().fastIterator();
        while (iter.hasNext()) {
            var entry = iter.next();
            int id = currentIdData.getId(entry.getValue());
            if (id >= 0) {
                classToId.put(entry.getKey(), id);
            }
        }
    }

    public static void onServerStopped() {
        currentIdData = null;
        idToEntry = null;
        instanceToId = null;
        classToId = null;
        epoch = 0;
    }

    public static int getEpoch() {
        return epoch;
    }

    public static Iterable<String> getAllKeys() {
        return BY_KEY.keySet();
    }

    public static @Range(from = -1, to = Integer.MAX_VALUE) int getId(IPhasedStructure structure) {
        if (instanceToId == null) return -1;
        int id = instanceToId.getInt(structure);
        if (id >= 0) return id;
        return classToId != null ? classToId.getInt(structure.getClass()) : -1;
    }

    public static @Nullable String getKeyById(int id) {
        if (currentIdData == null) return null;
        return currentIdData.getKey(id);
    }

    private static @Nullable Entry<?> getEntry(int id) {
        if (idToEntry == null || id < 0 || id >= idToEntry.length) return null;
        return idToEntry[id];
    }

    /**
     * Get ID for a string from the global string table.
     */
    public static int getStringId(String s) {
        if (currentIdData == null) return -1;
        return currentIdData.getStringId(s);
    }

    /**
     * Get string for an ID from the global string table.
     */
    public static @Nullable String getString(int id) {
        if (currentIdData == null) return null;
        return currentIdData.getString(id);
    }

    public static @Nullable IPhasedStructure deserializeById(int id, ByteBuf in, int dataLen) {
        Entry<?> entry = getEntry(id);
        if (entry == null) {
            if (dataLen > 0) in.skipBytes(dataLen);
            return null;
        }
        if (!entry.shouldSerialize()) {
            if (dataLen > 0) {
                in.skipBytes(dataLen);
            }
            IPhasedStructure instance = entry.instance();
            return instance != null ? instance : entry.read(in);
        }
        ByteBuf slice = dataLen == 0 ? Unpooled.EMPTY_BUFFER : in.readSlice(dataLen);
        return entry.read(slice);
    }

    public static long getKeyHash(IPhasedStructure structure) {
        String key = BY_INSTANCE.get(structure);
        if (key == null) key = BY_CLASS.get(structure.getClass());
        if (key == null) throw new IllegalArgumentException("Unknown phased structure: " + structure);
        Entry<? extends IPhasedStructure> entry = BY_KEY.get(key);
        if (entry == null) throw new IllegalStateException("Missing entry for key " + key);
        return entry.keyHash();
    }

    public static boolean shouldSerialize(IPhasedStructure structure) {
        String key = BY_INSTANCE.get(structure);
        if (key == null) key = BY_CLASS.get(structure.getClass());
        if (key == null) throw new IllegalArgumentException("Unknown phased structure: " + structure);
        Entry<? extends IPhasedStructure> entry = BY_KEY.get(key);
        if (entry == null) throw new IllegalStateException("Missing entry for key " + key);
        return entry.shouldSerialize();
    }

    public static boolean shouldSerialize(String key) {
        Entry<? extends IPhasedStructure> entry = BY_KEY.get(key);
        if (entry == null) return true;
        return entry.shouldSerialize();
    }

    public static @Nullable IPhasedStructure deserialize(String key, ByteBuf in) {
        Entry<? extends IPhasedStructure> entry = BY_KEY.get(key);
        if (entry == null) return null;
        return entry.read(in);
    }

    public static @Nullable IPhasedStructure deserializePacked(String key, ByteBuf in, int dataLen) {
        Entry<? extends IPhasedStructure> entry = BY_KEY.get(key);
        if (entry == null) return null;
        if (!entry.shouldSerialize()) {
            if (dataLen > 0) {
                in.skipBytes(dataLen);
            }
            IPhasedStructure instance = entry.instance();
            return instance != null ? instance : entry.read(in);
        }
        ByteBuf slice = dataLen == 0 ? Unpooled.EMPTY_BUFFER : in.readSlice(dataLen);
        return entry.read(slice);
    }

    public static <T extends IPhasedStructure> void serialize(T structure, ByteBuf out) {
        String key = getKey(structure);
        // noinspection unchecked
        Entry<T> entry = (Entry<T>) BY_KEY.get(key); // safe as long as registration is correct
        if (entry == null) throw new IllegalStateException("Missing entry for key " + key);
        entry.write(structure, out);
    }

    private record Entry<T extends IPhasedStructure>(String key, long keyHash, @Nullable T instance,
                                                     Function<ByteBuf, T> reader, BiConsumer<T, ByteBuf> writer,
                                                     boolean shouldSerialize) {
        T read(ByteBuf in) {
            return reader.apply(in);
        }

        void write(T structure, ByteBuf out) {
            writer.accept(structure, out);
        }
    }
}
