package com.hbm.core;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public final class ResourceAccessLogger {
    private static final Logger LOGGER = LogManager.getLogger("HBM ResourceLogger");
    private static final Set<String> USED = ConcurrentHashMap.newKeySet(65536);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(ResourceAccessLogger::flushToDisk, "HBM-ResourceLogger"));
    }

    private ResourceAccessLogger() {
    }

    public static void log(ResourceLocation location) {
        if (location == null) return;
        USED.add(location.toString());
    }

    private static void flushToDisk() {
        if (USED.isEmpty()) return;
        Path output = Launch.minecraftHome.toPath().resolve("resource-usage").resolve("used_resources.txt");
        List<String> lines = new ArrayList<>(USED);
        Collections.sort(lines);

        try {
            Files.createDirectories(output.getParent());
            Files.write(output, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            LOGGER.info("Wrote {} used resources to {}", lines.size(), output.toAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Failed to write used resources to {}", output.toAbsolutePath(), e);
        }
    }
}
