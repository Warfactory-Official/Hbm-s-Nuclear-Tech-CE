package com.hbm.integration.groovy;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.compat.mods.GroovyPropertyContainer;
import com.cleanroommc.groovyscript.registry.NamedRegistry;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.google.gson.JsonElement;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.integration.groovy.script.*;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.packet.threading.ThreadedPacket;
import com.hbm.packet.toclient.SerializableRecipePacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class HbmGroovyPropertyContainer extends GroovyPropertyContainer {

    private static final ArrayList<VirtualizedRegistry<?>> properties = new ArrayList<>();

    public static final AnvilSmithing ANVILSMITHING = createProperty(new AnvilSmithing());
    public static final AnvilConstruction ANVILCONSTRUCTION = createProperty(new AnvilConstruction());
    public static final Assembler ASSEMBLER = createProperty(new Assembler());
    public static final Press PRESS = createProperty(new Press());
    public static final BlastFurnaceFuel BLASTFURNACEFUEL = createProperty(new BlastFurnaceFuel());
    public static final BlastFurnace BLASTFURNACE = createProperty(new BlastFurnace());
    public static final Shredder SHREDDER = createProperty(new Shredder());
    public static final Bobmazon BOBMAZON = createProperty(new Bobmazon());
    public static final BreedingReactor BREEDINGREACTOR = createProperty(new BreedingReactor());
    public static final Centrifuge CENTRIFUGE = createProperty(new Centrifuge());
    public static final DFC DFC = createProperty(new DFC());
    public static final SILEX SILEX = createProperty(new SILEX());
    public static final IrradiationChannel IRRADIATIONCHANNEL = createProperty(new IrradiationChannel());
    public static final WasteDrum WASTEDRUM = createProperty(new WasteDrum());

    private static RecipeOverrideManager activeRecipeOverrides;

    private final RecipeOverrideManager recipeOverrides;
    private final Hazards hazards;
    private final FluidHazards fluidHazards;


    public HbmGroovyPropertyContainer() {
        ensureRecipeHandlersRegistered();
        this.recipeOverrides = new RecipeOverrideManager(SerializableRecipe.recipeHandlers);
        activeRecipeOverrides = this.recipeOverrides;
        addProperty(this.recipeOverrides);
        for (RecipeFileBinding binding : this.recipeOverrides.createBindings()) {
            addProperty(binding);
        }
        for(VirtualizedRegistry<?> entry:properties){
            addProperty(entry);
        }
        this.hazards = new Hazards();
        addProperty(this.hazards);
        this.fluidHazards = new FluidHazards();
        addProperty(this.fluidHazards);
    }

    private static <T extends VirtualizedRegistry<?>> T createProperty(T entry){
        properties.add(entry);
        return entry;
    }

    public static void sendRecipeOverridesToPlayer(EntityPlayerMP player) {
        RecipeOverrideManager manager = activeRecipeOverrides;
        if (manager != null && player != null) {
            manager.sendToPlayer(player);
        }
    }

    private static void ensureRecipeHandlersRegistered() {
        if (SerializableRecipe.recipeHandlers.isEmpty()) {
            SerializableRecipe.registerAllHandlers();
        }
    }

    @SuppressWarnings({"MethodMayBeStatic", "ConstantValue"})
    static final class RecipeOverrideManager extends VirtualizedRegistry<OverrideData> {

        private final Map<SerializableRecipe, OverrideData> overrides = new LinkedHashMap<>();
        private final Map<SerializableRecipe, List<String>> aliasMap = new LinkedHashMap<>();
        private final Map<String, SerializableRecipe> aliasLookup = new LinkedHashMap<>();
        private boolean needsApply;

        RecipeOverrideManager(Collection<SerializableRecipe> handlers) {
            super(Collections.singletonList("recipeOverrides"));
            for (SerializableRecipe handler : handlers) {
                List<String> aliases = computeAliases(handler);
                if (aliases.isEmpty()) {
                    continue;
                }
                aliasMap.put(handler, aliases);
                for (String alias : aliases) {
                    aliasLookup.put(normalizeKey(alias), handler);
                }
            }
        }

        private static List<String> computeAliases(SerializableRecipe handler) {
            Set<String> aliases = new LinkedHashSet<>();
            String className = handler.getClass().getSimpleName();
            String classBase = stripSuffix(className, "Recipes");
            String fileName = handler.getFileName();
            String fileBase = stripSuffix(fileName, ".json");
            addAlias(aliases, className);
            addAlias(aliases, classBase);
            addAlias(aliases, classBase.toLowerCase(Locale.ENGLISH));
            addAlias(aliases, camelToSnake(classBase));
            addAlias(aliases, fileName);
            addAlias(aliases, fileBase);
            addAlias(aliases, camelToSnake(fileBase));
            addAlias(aliases, fileBase.toLowerCase(Locale.ENGLISH));
            List<String> list = new ArrayList<>(aliases);
            return list.isEmpty() ? Collections.singletonList(className.toLowerCase(Locale.ENGLISH)) : Collections.unmodifiableList(list);
        }

        private static void addAlias(Set<String> aliases, @Nullable String alias) {
            if (alias == null) return;
            String trimmed = alias.trim();
            if (!trimmed.isEmpty()) {
                aliases.add(trimmed);
            }
        }

        private static String camelToSnake(String value) {
            if (value == null || value.isEmpty()) return value;
            StringBuilder builder = new StringBuilder(value.length() + 4);
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (Character.isUpperCase(c)) {
                    if (builder.length() > 0) builder.append('_');
                    builder.append(Character.toLowerCase(c));
                } else if (c == ' ') {
                    builder.append('_');
                } else {
                    builder.append(Character.toLowerCase(c));
                }
            }
            return builder.toString();
        }

        private static String stripSuffix(String value, String suffix) {
            if (value != null && value.endsWith(suffix)) {
                return value.substring(0, value.length() - suffix.length());
            }
            return value;
        }

        private static String normalizeKey(String key) {
            StringBuilder builder = new StringBuilder(key.length());
            for (int i = 0; i < key.length(); i++) {
                char c = Character.toLowerCase(key.charAt(i));
                if (Character.isLetterOrDigit(c)) {
                    builder.append(c);
                }
            }
            return builder.toString();
        }

        List<RecipeFileBinding> createBindings() {
            List<RecipeFileBinding> result = new ArrayList<>();
            for (Map.Entry<SerializableRecipe, List<String>> entry : aliasMap.entrySet()) {
                result.add(new RecipeFileBinding(this, entry.getKey(), entry.getValue()));
            }
            return result;
        }

        @MethodDescription()
        public void override(@NotNull String target, @NotNull String json) {
            GroovyLog.Msg msg = GroovyLog.msg("Error overriding HBM recipe file").error();
            boolean empty = json == null || json.trim().isEmpty();
            msg.add(empty, "json data must not be empty");
            SerializableRecipe handler = resolve(target, msg);
            if (msg.postIfNotEmpty()) return;
            applyOverride(handler, json.getBytes(StandardCharsets.UTF_8));
        }

        @MethodDescription()
        public void override(@NotNull String target, @NotNull JsonElement json) {
            GroovyLog.Msg msg = GroovyLog.msg("Error overriding HBM recipe file").error();
            msg.add(json == null, "json data must not be null");
            SerializableRecipe handler = resolve(target, msg);
            if (msg.postIfNotEmpty()) return;
            applyOverride(handler, SerializableRecipe.gson.toJson(json).getBytes(StandardCharsets.UTF_8));
        }

        @MethodDescription()
        public void override(@NotNull String target, @NotNull File file) {
            GroovyLog.Msg msg = GroovyLog.msg("Error overriding HBM recipe file from disk").error();
            msg.add(file == null, "file must not be null");
            msg.add(file != null && !file.isFile(), () -> "file '" + file + "' does not exist or is not a regular file");
            SerializableRecipe handler = resolve(target, msg);
            if (msg.postIfNotEmpty()) return;
            try {
                applyOverride(handler, Files.readAllBytes(file.toPath()));
            } catch (IOException ex) {
                GroovyLog.get().error("Failed to read override file {}: {}", file, ex.getMessage());
                GroovyLog.get().exception(ex);
            }
        }

        @MethodDescription()
        public void clear(@NotNull String target) {
            GroovyLog.Msg msg = GroovyLog.msg("Error clearing HBM recipe override").error();
            SerializableRecipe handler = resolve(target, msg);
            if (msg.postIfNotEmpty()) return;
            if (!clearOverride(handler)) {
                GroovyLog.get().info("No override registered for {}", handler.getFileName());
            }
        }

        @MethodDescription()
        public void clearAll() {
            if (!overrides.isEmpty()) {
                overrides.clear();
                needsApply = true;
            }
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public boolean has(@NotNull String target) {
            SerializableRecipe handler = aliasLookup.get(normalizeKey(target));
            return handler != null && overrides.containsKey(handler);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public List<String> listTargets() {
            List<String> targets = new ArrayList<>();
            for (List<String> aliases : aliasMap.values()) {
                if (!aliases.isEmpty()) {
                    targets.add(aliases.get(0));
                }
            }
            return Collections.unmodifiableList(targets);
        }

        @Override
        public void onReload() {
            overrides.clear();
            removeScripted();
            needsApply = false;
            SerializableRecipe.clearReceivedRecipes();
        }

        @Override
        public void afterScriptLoad() {
            if (!needsApply) return;
            needsApply = false;
            if (!isServerSide()) return;
            SerializableRecipe.clearReceivedRecipes();
            if (overrides.isEmpty()) {
                PacketThreading.createSendToAllThreadedPacket(new SerializableRecipePacket(true));
                return;
            }
            for (OverrideData data : overrides.values()) {
                SerializableRecipe.receiveRecipes(data.fileName, data.data.clone());
            }
            SerializableRecipe.initialize();
            broadcastToClients(overrides.values());
        }

        private void broadcastToClients(Collection<OverrideData> entries) {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server == null) return;
            for (OverrideData data : entries) {
                PacketThreading.createSendToAllThreadedPacket(new SerializableRecipePacket(data.fileName, data.data));
            }
            PacketThreading.createSendToAllThreadedPacket(new SerializableRecipePacket(true));
        }

        void sendToPlayer(EntityPlayerMP player) {
            if (player == null || overrides.isEmpty()) return;
            for (OverrideData data : overrides.values()) {
                ThreadedPacket message = new SerializableRecipePacket(data.fileName, data.data);
                PacketThreading.createSendToThreadedPacket(message, player);
            }
            ThreadedPacket message = new SerializableRecipePacket(true);
            PacketThreading.createSendToThreadedPacket(message, player);
        }

        private boolean isServerSide() {
            return FMLCommonHandler.instance().getEffectiveSide().isServer();
        }

        void applyOverride(@Nullable SerializableRecipe handler, byte[] data) {
            if (handler == null || data == null || data.length == 0) return;
            OverrideData override = new OverrideData(handler, data.clone());
            overrides.put(handler, override);
            doAddScripted(override);
            needsApply = true;
        }

        boolean clearOverride(@Nullable SerializableRecipe handler) {
            if (handler == null) return false;
            OverrideData removed = overrides.remove(handler);
            if (removed != null) {
                needsApply = true;
                return true;
            }
            return false;
        }

        boolean hasOverride(@Nullable SerializableRecipe handler) {
            return handler != null && overrides.containsKey(handler);
        }

        private SerializableRecipe resolve(String target, GroovyLog.Msg msg) {
            if (target == null || target.trim().isEmpty()) {
                msg.add(true, "target must not be empty");
                return null;
            }
            SerializableRecipe handler = aliasLookup.get(normalizeKey(target));
            if (handler == null) {
                msg.add(true, () -> "Unknown recipe set '" + target + "'");
            }
            return handler;
        }
    }

    @SuppressWarnings("ConstantValue")
    static final class RecipeFileBinding extends NamedRegistry {

        private final RecipeOverrideManager manager;
        private final SerializableRecipe handler;

        RecipeFileBinding(RecipeOverrideManager manager, SerializableRecipe handler, Collection<String> aliases) {
            super(aliases);
            this.manager = manager;
            this.handler = handler;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public String fileName() {
            return handler.getFileName();
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public boolean hasOverride() {
            return manager.hasOverride(handler);
        }

        @MethodDescription()
        public void override(@NotNull String json) {
            GroovyLog.Msg msg = GroovyLog.msg("Error overriding HBM recipe file {0}", handler.getFileName()).error();
            boolean empty = json == null || json.trim().isEmpty();
            msg.add(empty, "json data must not be empty");
            if (msg.postIfNotEmpty()) return;
            manager.applyOverride(handler, json.getBytes(StandardCharsets.UTF_8));
        }

        @MethodDescription()
        public void override(@NotNull JsonElement json) {
            GroovyLog.Msg msg = GroovyLog.msg("Error overriding HBM recipe file {0}", handler.getFileName()).error();
            msg.add(json == null, "json data must not be null");
            if (msg.postIfNotEmpty()) return;
            manager.applyOverride(handler, SerializableRecipe.gson.toJson(json).getBytes(StandardCharsets.UTF_8));
        }

        @MethodDescription()
        public void override(@NotNull File file) {
            GroovyLog.Msg msg = GroovyLog.msg("Error overriding HBM recipe file {0}", handler.getFileName()).error();
            msg.add(file == null, "file must not be null");
            msg.add(file != null && !file.isFile(), () -> "file '" + file + "' does not exist or is not a regular file");
            if (msg.postIfNotEmpty()) return;
            try {
                manager.applyOverride(handler, Files.readAllBytes(file.toPath()));
            } catch (IOException ex) {
                GroovyLog.get().error("Failed to read override file {}: {}", file, ex.getMessage());
                GroovyLog.get().exception(ex);
            }
        }

        @MethodDescription()
        public void clear() {
            if (!manager.clearOverride(handler)) {
                GroovyLog.get().info("No override registered for {}", handler.getFileName());
            }
        }
    }

    static final class OverrideData {
        final SerializableRecipe handler;
        final String fileName;
        final byte[] data;

        OverrideData(SerializableRecipe handler, byte[] data) {
            this.handler = handler;
            this.fileName = handler.getFileName();
            this.data = data;
        }
    }
}
