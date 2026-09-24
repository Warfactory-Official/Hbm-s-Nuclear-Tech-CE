package com.hbm.render.skinlayer;

import com.hbm.main.MainRegistry;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.UUID;

public final class MojangSkinLoader {

    private static final Result PENDING = new Result(null, null);
    private static final Map<UUID, Result> CACHE = new Object2ObjectOpenHashMap<>();

    private MojangSkinLoader() {
    }

    public static @Nullable Result get(UUID uuid) {
        Result r = CACHE.get(uuid);
        return r == PENDING ? null : r;
    }

    public static void preload(UUID uuid) {
        if (CACHE.putIfAbsent(uuid, PENDING) != null) return;
        Thread t = new Thread(() -> fetch(uuid), "BobbleSkinFetch-" + uuid);
        t.setDaemon(true);
        t.start();
    }

    private static void fetch(UUID uuid) {
        GameProfile profile;
        try {
            profile = Minecraft.getMinecraft().getSessionService()
                    .fillProfileProperties(new GameProfile(uuid, null), true);
        } catch (Throwable e) {
            MainRegistry.logger.warn("Failed to fill profile properties for UUID '{}'", uuid, e);
            return;
        }
        if (!profile.getProperties().containsKey("textures")) {
            MainRegistry.logger.warn("Mojang profile for UUID '{}' returned no textures property", uuid);
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> loadSkin(mc, profile));
    }

    private static void loadSkin(Minecraft mc, GameProfile profile) {
        SkinManager sm = mc.getSkinManager();
        sm.loadProfileTextures(profile, (type, location, profileTex) -> {
            if (type != Type.SKIN) return;
            try {
                String hash = profileTex.getHash();
                File dir = new File(sm.skinCacheDir, hash.length() > 2 ? hash.substring(0, 2) : "xx");
                File file = new File(dir, hash);
                BufferedImage raw = ImageIO.read(file);
                if (raw == null) {
                    MainRegistry.logger.warn("SkinManager cache file missing for '{}' at {}", profile.getId(), file);
                    return;
                }
                BufferedImage processed = new ImageBufferDownload().parseUserSkin(raw);
                mc.addScheduledTask(() -> CACHE.put(profile.getId(), new Result(processed, location)));
            } catch (Throwable e) {
                MainRegistry.logger.warn("Failed to read cached skin for '{}'", profile.getId(), e);
            }
        }, false);
    }

    public static final class Result {
        public final BufferedImage image;
        public final ResourceLocation texture;

        private Result(BufferedImage image, ResourceLocation texture) {
            this.image = image;
            this.texture = texture;
        }
    }
}
