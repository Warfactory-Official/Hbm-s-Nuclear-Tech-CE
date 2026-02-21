package com.hbm.render.icon;

import com.hbm.Tags;
import com.hbm.main.MainRegistry;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility functions for discovering and registering texture resources located
 * inside a mod's JAR file. This class supports scanning a directory within
 * {@code assets/<modid>/textures/} and automatically registering all PNG
 * sprites found inside it.
 *
 * <p>Only works when the mod is running from a JAR. In a development environment
 * where resources are served from the filesystem, this scanner will simply return
 * an empty list.</p>
 *
 * @author MrNorwood
 */
public class RegistrationUtils {

    /**
     * Scans a folder inside a mod's texture directory and registers all PNG sprites
     * found within it to the provided {@link TextureMap}.
     *
     * <p>The path should be relative to {@code assets/<modid>/textures/}.
     * For example, passing {@code "blocks/forgefluid"} will scan:
     * <pre>
     * assets/&lt;modid&gt;/textures/blocks/forgefluid/
     * </pre>
     * </p>
     *
     * @param map  the texture map to register discovered sprites into
     * @param path the folder path relative to the mod's {@code textures/} directory
     */
    public static void registerInFolder(TextureMap map, String path) {
        var textureList = getResourcesFromPath(Tags.MODID, path);
        for (ResourceLocation resourceLocation : textureList) {
            map.registerSprite(resourceLocation);
        }
    }

    /**
     * Returns all texture {@link ResourceLocation}s found under a mod's texture
     * subdirectory within a JAR file.
     *
     * <p>This resolves the mod's code source, opens the JAR via {@link FileSystem},
     * walks {@code assets/<modid>/<dir>/}, and collects every {@code .png} file
     * located under the {@code textures/} hierarchy.</p>
     *
     * <p>The returned resource locations are normalized into the form:
     * <pre>
     * &lt;modid&gt;:&lt;relative_path_without_extension&gt;
     * </pre>
     * where the relative path begins immediately after {@code textures/}.</p>
     *
     * @param modid the mod ID used to construct {@link ResourceLocation}s
     * @param dir   the directory inside {@code assets/<modid>/} to scan
     * @return a list of discovered texture resource locations
     */
    private static List<ResourceLocation> getResourcesFromPath(String modid, String dir) {
        var result = new ArrayList<ResourceLocation>();

        URI codeSource;
        try {
            codeSource = MainRegistry.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to resolve mod JAR URI", e);
        }

        if (!"jar".equals(codeSource.getScheme()))
            return result;

        try (FileSystem fs = FileSystems.newFileSystem(codeSource, Map.of())) {
            var root = fs.getPath("assets", modid, dir);

            if (Files.exists(root)) {
                Files.walk(root).forEach(path -> {
                    if (path.toString().endsWith(".png")) {
                        String full = path.toString().replace('\\', '/');

                        int idx = full.indexOf("/textures/");
                        if (idx >= 0) {
                            String rel = full.substring(idx + "/textures/".length())
                                    .replaceFirst("\\.png$", "");

                            result.add(new ResourceLocation(modid, rel));
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
