package com.hbm.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;
import com.hbm.items.machine.ItemCassette;
import com.hbm.main.MainRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Locale;

/**
 * How to create a cassette element:
 * <pre>{@code
 *     {
 *         "name": "Name of track",
 *         "sound": "modid:sound.name", //Custom sounds can be added to any mod via resource packs (both as a file and in sounds.json), and hbm will try to register it
 *         "type": "LOOP", //Can also be PASS or SOUND
 *         "color": "00FF75", //Can also be a base-10 integer
 *         "volume": 100 //Optional; default is 50
 *     }
 * }</pre>
 */
public class CassetteJsonConfig {
    public static final String FILENAME = "hbm_siren_cassettes.json";

    public static void init() {
        if (!tryRead() && JsonConfig.isFileNonexistent(FILENAME)) {
            try {
                JsonWriter writer = JsonConfig.startWriting(FILENAME);
                writer.name("cassetteConfig").beginArray().endArray();
                JsonConfig.stopWriting(writer);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean tryRead() {
        try {
            JsonObject reader = JsonConfig.startReading(FILENAME);

            if (reader == null || !reader.has("cassetteConfig")) return false;
            JsonArray entries = reader.getAsJsonArray("cassetteConfig");
            for (JsonElement entry : entries) {
                if (entry == null || !entry.isJsonObject()) continue;
                JsonObject obj = entry.getAsJsonObject();

                if (!obj.has("name")) continue;
                String name = obj.get("name").getAsString();

                if (!obj.has("sound")) continue;
                ResourceLocation soundLocation = new ResourceLocation(obj.get("sound").getAsString());
                SoundEvent sound = SoundEvent.REGISTRY.getObject(soundLocation);
                if (sound == null) sound = tryRegisterSound(soundLocation);

                if (!obj.has("type")) continue;
                ItemCassette.SoundType type = switch (obj.get("type").getAsString().toLowerCase(Locale.ROOT)) {
                    case "loop" -> ItemCassette.SoundType.LOOP;
                    case "pass" -> ItemCassette.SoundType.PASS;
                    case "sound" -> ItemCassette.SoundType.SOUND;
                    default -> null;
                };
                if (type == null) continue;

                if (!obj.has("color")) continue;
                int color = 0;
                if (obj.get("color").isJsonPrimitive()) {
                    JsonPrimitive primitive = obj.get("color").getAsJsonPrimitive();
                    if (primitive.isNumber()) color = primitive.getAsInt();
                    else if (primitive.isString()) color = Integer.parseInt(primitive.getAsString(), 16);
                }

                int volume;
                if (!obj.has("volume")) volume = 50;
                else volume = obj.get("volume").getAsInt();

                ItemCassette.TrackType.register(name, sound, type, color, volume);
            }

            return true;
        } catch (Exception e) {
            MainRegistry.logger.error("Error while reading siren cassette config.");
            MainRegistry.logger.error(e.getStackTrace());
            return false;
        }
    }

    public static SoundEvent tryRegisterSound(ResourceLocation location) {
        try {
            SoundEvent e = new SoundEvent(location);
            e.setRegistryName(location.getPath());
            ForgeRegistries.SOUND_EVENTS.register(e);
            return e;
        } catch (Exception e) {
            throw new RuntimeException("Could not find nor register siren sound at location " + location, e);
        }
    }
}
