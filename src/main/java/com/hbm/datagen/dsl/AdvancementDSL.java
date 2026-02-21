package com.hbm.datagen.dsl;

import com.google.gson.*;
import net.minecraft.advancements.FrameType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

public final class AdvancementDSL {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private AdvancementDSL() {
    }

    private static String registryName(Item item) {
        ResourceLocation rl = GameRegistry.findRegistry(Item.class).getKey(item);
        return rl == null ? "minecraft:stone" : rl.toString();
    }

    private static String registryName(ItemStack stack) {
        ResourceLocation rl = GameRegistry.findRegistry(Item.class).getKey(stack.getItem());
        return rl == null ? "minecraft:stone" : rl.toString();
    }

    private static String sanitize(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
    }

    private static String uniqueCriterionName(Set<String> used, String base, String hint, int fallbackIndex) {
        String b = (base == null || base.isEmpty()) ? "crit" : base;
        String h = (hint == null || hint.isEmpty()) ? String.valueOf(fallbackIndex) : hint;
        String attempt = b + "_" + h;
        int i = 1;
        while (used.contains(attempt)) attempt = b + "_" + h + "_" + (i++);
        used.add(attempt);
        return attempt;
    }

    public static Batch batch(String namespace) {
        return new Batch(namespace);
    }

    public static Display display() {
        return new Display();
    }

    /**
     * create a fluent Builder for an advancement located at the given idPath
     */
    public static Builder builder(String idPath) {
        return new Builder(idPath);
    }

    /**
     * Build a completely custom criterion JSON with a writer lambda.
     */
    public static JsonObject customCriterion(String name, Consumer<JsonObject> writer) {
        JsonObject c = new JsonObject();
        writer.accept(c);
        return c;
    }

    /**
     * Quickly build a map for {@link Templates#anyOf}.
     */
    public static Map<String, JsonObject> criteria(Object... nameThenCriterionObjects) {
        if (nameThenCriterionObjects.length % 2 != 0)
            throw new IllegalArgumentException("criteria() expects name, JsonObject pairs");
        Map<String, JsonObject> map = new LinkedHashMap<>();
        for (int i = 0; i < nameThenCriterionObjects.length; i += 2) {
            String name = (String) nameThenCriterionObjects[i];
            JsonObject crit = (JsonObject) nameThenCriterionObjects[i + 1];
            map.put(name, crit);
        }
        return map;
    }

    public static JsonObject criterion(String trigger, @Nullable Consumer<JsonObject> conditionsWriter) {
        JsonObject obj = new JsonObject();
        obj.addProperty("trigger", trigger);
        if (conditionsWriter != null) {
            JsonObject cond = new JsonObject();
            conditionsWriter.accept(cond);
            obj.add("conditions", cond);
        }
        return obj;
    }

    static Path detectProjectRoot() {
        Path p = Paths.get("").toAbsolutePath();
        if (p.getFileName() != null && p.getFileName().toString().equalsIgnoreCase("run")) p = p.getParent();
        while (p != null) {
            if (Files.exists(p.resolve("build.gradle")) || Files.exists(p.resolve("build.gradle.kts")) || Files.exists(p.resolve(".git"))) {
                return p;
            }
            p = p.getParent();
        }
        return Paths.get("").toAbsolutePath();
    }

    public static final class Batch {
        private final String namespace;
        private final List<Entry> entries = new ArrayList<>();

        Batch(String namespace) {
            this.namespace = namespace;
        }

        public Batch add(Entry e) {
            entries.add(e);
            return this;
        }

        public Batch add(Entry... arr) {
            entries.addAll(Arrays.asList(arr));
            return this;
        }

        public void writeAll(Path resourcesRoot) {
            Path base = resourcesRoot.resolve("assets").resolve(namespace).resolve("advancements");
            for (Entry e : entries) {
                Path out = base.resolve(e.path + ".json");
                try {
                    Files.createDirectories(out.getParent());
                    try (OutputStreamWriter w = new OutputStreamWriter(Files.newOutputStream(out), StandardCharsets.UTF_8)) {
                        GSON.toJson(e.json, w);
                    }
                } catch (IOException io) {
                    throw new RuntimeException("Failed writing advancement JSON: " + out, io);
                }
            }
        }

        public Path writeAllToProject(Path projectRoot) {
            Path res = projectRoot.resolve("src/main/resources");
            writeAll(res);
            return res.resolve("assets").resolve(namespace).resolve("advancements");
        }

        public Path writeAllSmart() {
            Path projectRoot = AdvancementDSL.detectProjectRoot();
            Path out = writeAllToProject(projectRoot);
            System.out.println("[AdvancementDSL] Wrote advancements to: " + out.toAbsolutePath());
            return out;
        }
    }

    public static final class Entry {
        final String path;
        final JsonObject json;

        Entry(String path, JsonObject json) {
            this.path = path;
            this.json = json;
        }
    }

    public static final class Templates {
        @Contract("_, _, _, _ -> new")
        public static @NotNull Entry impossible(String idPath, @Nullable String parentRL, Display display, String criterionName) {
            JsonObject json = base(parentRL, display);
            JsonObject criteria = new JsonObject();
            criteria.add(criterionName, criterion(Triggers.IMPOSSIBLE, null));
            json.add("criteria", criteria);
            json.add("requirements", reqAll(criterionName)); // [["crit"]] => must complete this criterion
            return new Entry(idPath, json);
        }

        /**
         * Obtain one of the given items via inventory_changed.
         * Produces a single OR-group with all generated criteria: [[c1, c2, ...]]
         */
        @Contract("_, _, _, _, _ -> new")
        public static @NotNull Entry obtainAnyItem(String idPath, @Nullable String parentRL, Display display, String criterionName, Item... items) {
            if (items == null || items.length == 0) {
                throw new IllegalArgumentException("obtainAnyItem requires at least one Item");
            }
            JsonObject json = base(parentRL, display);

            JsonObject crit = new JsonObject();
            List<String> names = new ArrayList<>();
            Set<String> used = new HashSet<>();

            int idx = 0;
            for (Item it : items) {
                if (it == null) continue;
                JsonArray single = new JsonArray();
                single.add(itemPredicate(it));

                String hint = sanitize(registryName(it));
                String name = uniqueCriterionName(used, criterionName, hint, idx++);
                crit.add(name, criterion(Triggers.INVENTORY_CHANGED, c -> c.add("items", single)));
                names.add(name);
            }

            if (names.isEmpty()) throw new IllegalArgumentException("obtainAnyItem produced no valid items");

            json.add("criteria", crit);
            json.add("requirements", reqAny(names)); // [[ name1, name2, ... ]]
            return new Entry(idPath, json);
        }

        /**
         * Obtain one of the given ItemStacks (respects meta / NBT) via inventory_changed.
         * Produces a single OR-group with all generated criteria: [[c1, c2, ...]]
         */
        @Contract("_, _, _, _, _ -> new")
        public static @NotNull Entry obtainAnyItemStack(String idPath, @Nullable String parentRL, Display display, String criterionName, ItemStack... stacks) {
            if (stacks == null || stacks.length == 0) {
                throw new IllegalArgumentException("obtainAnyItemStack requires at least one ItemStack");
            }
            JsonObject json = base(parentRL, display);

            JsonObject crit = new JsonObject();
            List<String> names = new ArrayList<>();
            Set<String> used = new HashSet<>();

            int idx = 0;
            for (ItemStack s : stacks) {
                if (s == null || s.isEmpty()) continue;

                JsonArray single = new JsonArray();
                single.add(itemPredicate(s));

                String rn = registryName(s);
                String meta = s.getMetadata() != 0 ? "_" + s.getMetadata() : "";
                String hint = sanitize(rn + meta);

                String name = uniqueCriterionName(used, criterionName, hint, idx++);
                crit.add(name, criterion(Triggers.INVENTORY_CHANGED, cond -> cond.add("items", single)));
                names.add(name);
            }

            if (names.isEmpty()) throw new IllegalArgumentException("obtainAnyItemStack produced no valid stacks");

            json.add("criteria", crit);
            json.add("requirements", reqAny(names)); // [[ name1, name2, ... ]]
            return new Entry(idPath, json);
        }

        /**
         * Kill an entity of type RL (e.g. minecraft:creeper).
         */
        @Contract("_, _, _, _, _ -> new")
        public static @NotNull Entry killEntity(String idPath, @Nullable String parentRL, Display display, String criterionName, String entityRL) {
            JsonObject json = base(parentRL, display);
            JsonObject ent = new JsonObject();
            ent.addProperty("type", entityRL);
            JsonObject criteria = new JsonObject();
            criteria.add(criterionName, criterion(Triggers.PLAYER_KILLED_ENTITY, c -> c.add("entity", ent)));
            json.add("criteria", criteria);
            json.add("requirements", reqAll(criterionName));
            return new Entry(idPath, json);
        }

        /**
         * Enter a dimension by id (e.g. 0, -1, 1).
         */
        @Contract("_, _, _, _, _ -> new")
        public static @NotNull Entry enterDimension(String idPath, @Nullable String parentRL, Display display, String criterionName, int dim) {
            JsonObject json = base(parentRL, display);
            JsonObject criteria = new JsonObject();
            criteria.add(criterionName, criterion(Triggers.CHANGED_DIMENSION, c -> c.addProperty("dimension", dim)));
            json.add("criteria", criteria);
            json.add("requirements", reqAll(criterionName));
            return new Entry(idPath, json);
        }

        /**
         * Enter block trigger.
         */
        @Contract("_, _, _, _, _ -> new")
        public static @NotNull Entry enterBlock(String idPath, @Nullable String parentRL, Display display, String criterionName, String blockRL) {
            JsonObject json = base(parentRL, display);
            JsonObject criteria = new JsonObject();
            criteria.add(criterionName, criterion(Triggers.ENTER_BLOCK, c -> c.addProperty("block", blockRL)));
            json.add("criteria", criteria);
            json.add("requirements", reqAll(criterionName));
            return new Entry(idPath, json);
        }

        /**
         * Build a JSON with multiple named criteria and OR requirements (any one).
         */
        @Contract("_, _, _, _ -> new")
        public static @NotNull Entry anyOf(String idPath, @Nullable String parentRL, Display display, Map<String, JsonObject> namedCriteria) {
            JsonObject json = base(parentRL, display);
            JsonObject crit = new JsonObject();
            for (Map.Entry<String, JsonObject> e : namedCriteria.entrySet()) crit.add(e.getKey(), e.getValue());
            json.add("criteria", crit);
            json.add("requirements", reqAny(namedCriteria.keySet())); // [[ allNames... ]]
            return new Entry(idPath, json);
        }

        private static JsonObject base(@Nullable String parentRL, Display display) {
            JsonObject json = new JsonObject();
            if (parentRL != null) json.addProperty("parent", parentRL);
            json.add("display", display.toJson());
            return json;
        }

        /**
         * AND-of-OR helpers matching vanilla semantics.
         * <ul>
         *   <li>reqAll("a","b") -> [["a"],["b"]] (both required)</li>
         *   <li>reqAny(names) -> [["a","b",...]] (any one)</li>
         * </ul>
         */
        private static JsonArray reqAll(String... names) {
            JsonArray outer = new JsonArray(); // AND groups
            if (names.length == 0) {
                JsonArray inner = new JsonArray();
                inner.add("__missing");
                outer.add(inner);
                return outer;
            }
            for (String n : names) {
                JsonArray inner = new JsonArray(); // single-name OR group
                inner.add(n);
                outer.add(inner);
            }
            return outer;
        }

        private static JsonArray reqAny(Collection<String> names) {
            JsonArray outer = new JsonArray(); // AND groups
            JsonArray inner = new JsonArray(); // one OR group containing all names
            for (String n : names) inner.add(n);
            if (inner.size() == 0) inner.add("__missing");
            outer.add(inner);
            return outer;
        }

        private static JsonObject itemPredicate(Item item) {
            ResourceLocation rl = GameRegistry.findRegistry(Item.class).getKey(item);
            JsonObject obj = new JsonObject();
            obj.addProperty("item", rl == null ? "minecraft:stone" : rl.toString());
            return obj;
        }

        private static JsonObject itemPredicate(ItemStack stack) {
            ResourceLocation rl = GameRegistry.findRegistry(Item.class).getKey(stack.getItem());
            JsonObject obj = new JsonObject();
            obj.addProperty("item", rl == null ? "minecraft:stone" : rl.toString());
            int meta = stack.getMetadata();
            if (meta != 0) obj.addProperty("data", meta);
            if (stack.hasTagCompound() && stack.getTagCompound() != null && !stack.getTagCompound().isEmpty()) {
                obj.addProperty("nbt", stack.getTagCompound().toString());
            }
            return obj;
        }
    }

    public static final class Display {
        private Icon icon;
        private TitleDesc title;
        private TitleDesc desc;
        private FrameType frame = FrameType.TASK;
        private boolean showToast = true;
        private boolean announce = true;
        private boolean hidden = false;
        private String background;

        public Display icon(Item item) {
            this.icon = Icon.of(item);
            return this;
        }

        public Display icon(ItemStack stack) {
            this.icon = Icon.of(stack);
            return this;
        }

        public Display iconRL(String itemRL) {
            this.icon = Icon.of(itemRL);
            return this;
        }

        public Display icon(Item item, int meta, @Nullable NBTTagCompound nbt) {
            this.icon = Icon.of(item);
            this.icon.meta = meta;
            this.icon.withNbt(nbt);
            return this;
        }

        public Display icon(String itemRL, int meta, @Nullable NBTTagCompound nbt) {
            this.icon = Icon.of(itemRL);
            this.icon.meta = meta;
            this.icon.withNbt(nbt);
            return this;
        }

        public Display key(String key) {
            this.title = TitleDesc.key("achievement." + key);
            this.desc = TitleDesc.key("achievement." + key + ".desc");
            return this;
        }

        public Display titleKey(String key) {
            this.title = TitleDesc.key(key);
            return this;
        }

        public Display titleLiteral(String text) {
            this.title = TitleDesc.literal(text);
            return this;
        }

        public Display descKey(String key) {
            this.desc = TitleDesc.key(key);
            return this;
        }

        public Display descLiteral(String text) {
            return desc(TitleDesc.literal(text));
        }

        private Display desc(TitleDesc td) {
            this.desc = td;
            return this;
        }

        public Display frame(FrameType t) {
            this.frame = t;
            return this;
        }

        public Display toast(boolean b) {
            this.showToast = b;
            return this;
        }

        public Display announce(boolean b) {
            this.announce = b;
            return this;
        }

        public Display hidden(boolean b) {
            this.hidden = b;
            return this;
        }

        public Display background(String texRL) {
            this.background = texRL;
            return this;
        }

        JsonObject toJson() {
            if (icon == null) throw new IllegalStateException("Display.icon not set");
            if (title == null) throw new IllegalStateException("Display.title not set");
            if (desc == null) throw new IllegalStateException("Display.description not set");
            JsonObject obj = new JsonObject();
            obj.add("icon", icon.toJson());
            obj.add("title", title.toJson());
            obj.add("description", desc.toJson());
            obj.addProperty("frame", frame.name().toLowerCase(Locale.ROOT));
            obj.addProperty("show_toast", showToast);
            obj.addProperty("announce_to_chat", announce);
            obj.addProperty("hidden", hidden);
            if (background != null) obj.addProperty("background", background);
            return obj;
        }
    }

    public static final class Icon {
        private String itemRL;
        private int meta = 0; // include if != 0
        private @Nullable NBTTagCompound nbt;

        public static Icon of(ItemStack stack) {
            Item item = stack.getItem();
            Icon i = of(item);
            i.meta = stack.getMetadata();
            i.nbt = stack.hasTagCompound() ? stack.getTagCompound().copy() : null;
            return i;
        }

        public static Icon of(Item item) {
            ResourceLocation rl = GameRegistry.findRegistry(Item.class).getKey(item);
            Icon i = new Icon();
            i.itemRL = rl == null ? "minecraft:stone" : rl.toString();
            return i;
        }

        public static Icon of(String itemRL) {
            Icon i = new Icon();
            i.itemRL = itemRL;
            return i;
        }

        public Icon withNbt(@Nullable NBTTagCompound tag) {
            this.nbt = tag;
            return this;
        }

        JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("item", itemRL);
            if (meta != 0) obj.addProperty("data", meta);
            if (nbt != null && !nbt.isEmpty()) {
                obj.addProperty("nbt", nbt.toString());
            }
            return obj;
        }
    }

    public static final class TitleDesc {
        private final Kind kind;
        private final String value;

        private TitleDesc(Kind k, String v) {
            this.kind = k;
            this.value = v;
        }

        public static TitleDesc key(String key) {
            return new TitleDesc(Kind.KEY, key);
        }

        public static TitleDesc literal(String text) {
            return new TitleDesc(Kind.LITERAL, text);
        }

        JsonObject toJson() {
            JsonObject obj = new JsonObject();
            if (kind == Kind.KEY) obj.addProperty("translate", value);
            else obj.addProperty("text", value);
            return obj;
        }

        private enum Kind {
            KEY, LITERAL
        }
    }

    public static final class Builder {
        private final String idPath;
        private final LinkedHashMap<String, JsonObject> criteria = new LinkedHashMap<>();
        private final List<LinkedHashSet<String>> cnf = new ArrayList<>();

        private @Nullable String parentRL;
        private @Nullable Display display;
        private @Nullable Rewards rewards;
        private @Nullable List<List<String>> requirements;

        public Builder(String idPath) {
            if (idPath == null || idPath.isEmpty()) throw new IllegalArgumentException("idPath must not be empty");
            this.idPath = idPath;
        }

        private String uniqueName(String base, String hint) {
            String b = (base == null || base.isEmpty()) ? "crit" : base;
            String h = (hint == null || hint.isEmpty()) ? "x" : hint;
            String n = (b + "_" + h).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
            int i = 1;
            while (criteria.containsKey(n)) n = n + "_" + (i++);
            return n;
        }

        private void addAndGroup(String name) {
            if (requirements != null) return;
            LinkedHashSet<String> g = new LinkedHashSet<>();
            g.add(name);
            cnf.add(g);
        }

        private void addAnyGroup(Collection<String> names) {
            if (requirements != null) return;
            LinkedHashSet<String> g = new LinkedHashSet<>(names);
            if (!g.isEmpty()) cnf.add(g);
        }

        public Builder parent(@Nullable String parentRL) {
            this.parentRL = parentRL;
            return this;
        }

        /**
         * Set display block.
         */
        public Builder display(Display display) {
            this.display = display;
            return this;
        }

        /**
         * Set rewards.
         */
        public Builder rewards(Rewards rewards) {
            this.rewards = rewards;
            return this;
        }

        /**
         * Add an arbitrary prebuilt criterion JSON.
         * By default each added criterion becomes its own AND-group (requires all), unless a custom requirements
         * matrix was explicitly supplied.
         */
        public Builder criterion(String name, JsonObject criterion) {
            if (name == null || name.isEmpty()) throw new IllegalArgumentException("criterion name must not be empty");
            this.criteria.put(name, criterion);
            addAndGroup(name);
            return this;
        }

        /**
         * Add a criterion with a trigger and optional conditions writer.
         */
        public Builder criterion(String name, String trigger, @Nullable Consumer<JsonObject> conditionsWriter) {
            return criterion(name, AdvancementDSL.criterion(trigger, conditionsWriter));
        }

        /**
         * inventory_changed: TRUE if inventory has ANY of the given Items.
         * Emits one OR-group containing all generated criterion names.
         */
        public Builder inventoryHasAny(String baseName, Item... items) {
            List<String> added = new ArrayList<>();
            int valid = 0;
            for (Item it : items) if (it != null) ++valid;
            for (Item it : items) {
                if (it == null) continue;
                JsonArray single = new JsonArray();
                single.add(Templates.itemPredicate(it));
                final String name = (valid == 1)
                                    ? (baseName == null || baseName.isEmpty() ? "has_item" : baseName)
                                    : uniqueName(baseName, sanitize(AdvancementDSL.registryName(it)));
                this.criteria.put(name, AdvancementDSL.criterion(Triggers.INVENTORY_CHANGED, cond -> cond.add("items", single)));
                added.add(name);
            }
            if (valid == 1 && !added.isEmpty()) addAndGroup(added.get(0));
            else if (valid > 1) addAnyGroup(added);
            return this;
        }

        /**
         * inventory_changed: TRUE if inventory has ANY of the given ItemStacks (respects meta / NBT).
         * Emits one OR-group containing all generated criterion names.
         */
        public Builder inventoryHasAnyStacks(ItemStack... stacks) {
            String baseName = "has_items";
            List<String> added = new ArrayList<>();
            int valid = 0;
            for (ItemStack s : stacks) if (s != null && !s.isEmpty()) ++valid;
            for (ItemStack s : stacks) {
                if (s == null || s.isEmpty()) continue;
                JsonArray single = new JsonArray();
                single.add(AdvancementDSL.Templates.itemPredicate(s));
                String hint = sanitize(registryName(s) + (s.getMetadata() != 0 ? "_" + s.getMetadata() : ""));
                final String name = (valid == 1) ? baseName : uniqueName(baseName, hint);
                this.criteria.put(name, AdvancementDSL.criterion(Triggers.INVENTORY_CHANGED, cond -> cond.add("items", single)));
                added.add(name);
            }
            if (valid == 1 && !added.isEmpty()) addAndGroup(added.get(0));
            else if (valid > 1) addAnyGroup(added);
            return this;
        }

        /**
         * consume_item: TRUE if player consumes ANY of the given Items (FIX: uses "item", not "items").
         * Emits one OR-group containing all generated criterion names.
         */
        public Builder consumeAnyItem(String baseName, Item... items) {
            List<String> added = new ArrayList<>();
            int valid = 0;
            for (Item it : items) if (it != null) ++valid;
            for (Item it : items) {
                if (it == null) continue;
                JsonObject pred = new JsonObject();
                pred.addProperty("item", registryName(it));
                final String name = (valid == 1)
                                    ? (baseName == null || baseName.isEmpty() ? "consumed" : baseName)
                                    : uniqueName(baseName, sanitize(registryName(it)));
                this.criteria.put(name, AdvancementDSL.criterion(Triggers.CONSUME_ITEM, cond -> cond.add("item", pred)));
                added.add(name);
            }
            if (valid == 1 && !added.isEmpty()) addAndGroup(added.get(0));
            else if (valid > 1) addAnyGroup(added);
            return this;
        }

        public Builder playerKilledEntity(String name, String entityRL) {
            return criterion(name, Triggers.PLAYER_KILLED_ENTITY, cond -> {
                JsonObject ent = new JsonObject();
                ent.addProperty("type", entityRL);
                cond.add("entity", ent);
            });
        }

        /**
         * changed_dimension to specific dimension id.
         */
        public Builder changedDimension(String name, int dim) {
            return criterion(name, Triggers.CHANGED_DIMENSION, cond -> cond.addProperty("dimension", dim));
        }

        /**
         * enter_block with a block RL.
         */
        public Builder enterBlock(String name, String blockRL) {
            return criterion(name, Triggers.ENTER_BLOCK, cond -> cond.addProperty("block", blockRL));
        }

        public Builder placedBlock(String name, String blockRL) {
            return criterion(name, Triggers.PLACED_BLOCK, cond -> cond.addProperty("block", blockRL));
        }

        /**
         * recipe_unlocked for a specific recipe RL.
         */
        public Builder recipeUnlocked(String name, String recipeRL) {
            return criterion(name, Triggers.RECIPE_UNLOCKED, cond -> cond.addProperty("recipe", recipeRL));
        }

        /**
         * Make requirements be "all of" the provided criterion names (AND in outer list as singleton groups).
         * Produces [["a"],["b"],...]
         */
        public Builder requireAll(String... names) {
            List<List<String>> matrix = new ArrayList<>();
            for (String n : names) matrix.add(Collections.singletonList(n));
            this.requirements = matrix;
            return this;
        }

        /**
         * Make requirements be "any of" the provided criterion names (one OR group).
         * Produces [["a","b",...]]
         */
        public Builder requireAny(String... names) {
            List<List<String>> matrix = new ArrayList<>();
            matrix.add(Arrays.asList(names));
            this.requirements = matrix;
            return this;
        }

        /**
         * Set a custom requirements matrix. Each inner list is an OR group; the outer list is AND across groups.
         */
        public Builder requirementsMatrix(List<List<String>> matrix) {
            this.requirements = matrix;
            return this;
        }

        private JsonArray toJsonRequirements(List<List<String>> matrix) {
            JsonArray outer = new JsonArray();
            for (List<String> group : matrix) {
                JsonArray inner = new JsonArray();
                for (String n : group) inner.add(n);
                outer.add(inner);
            }
            if (outer.size() == 0) {
                JsonArray inner = new JsonArray();
                inner.add("__missing");
                outer.add(inner);
            }
            return outer;
        }

        private JsonArray cnfToRequirementsJson() {
            JsonArray outer = new JsonArray();
            for (Set<String> group : cnf) {
                if (group.isEmpty()) continue;
                JsonArray inner = new JsonArray();
                for (String n : group) inner.add(n);
                outer.add(inner);
            }
            if (outer.size() == 0) {
                JsonArray inner = new JsonArray();
                inner.add("__missing");
                outer.add(inner);
            }
            return outer;
        }

        /**
         * Finalize into an {@link Entry}.
         */
        public Entry build() {
            if (display == null) throw new IllegalStateException("display must be set");
            if (criteria.isEmpty()) throw new IllegalStateException("at least one criterion must be added");

            JsonObject json = new JsonObject();
            if (parentRL != null) json.addProperty("parent", parentRL);
            json.add("display", display.toJson());

            if (rewards != null) json.add("rewards", rewards.toJson());
            JsonObject crit = new JsonObject();
            for (Map.Entry<String, JsonObject> e : criteria.entrySet()) {
                crit.add(e.getKey(), e.getValue());
            }
            json.add("criteria", crit);

            if (requirements != null) {
                json.add("requirements", toJsonRequirements(requirements));
            } else {
                json.add("requirements", cnfToRequirementsJson());
            }

            return new Entry(idPath, json);
        }
    }

    public static final class Rewards {
        private final List<String> loot = new ArrayList<>();
        private final List<String> recipes = new ArrayList<>();
        private int experience;
        private @Nullable String function;

        public static Rewards create() {
            return new Rewards();
        }

        public Rewards experience(int xp) {
            this.experience = xp;
            return this;
        }

        /**
         * Add loot table RLs.
         */
        public Rewards loot(String... lootRLs) {
            Collections.addAll(this.loot, lootRLs);
            return this;
        }

        /**
         * Add recipe RLs.
         */
        public Rewards recipes(String... recipeRLs) {
            Collections.addAll(this.recipes, recipeRLs);
            return this;
        }

        /**
         * Set command function RL.
         */
        public Rewards function(@Nullable String functionRL) {
            this.function = functionRL;
            return this;
        }

        JsonObject toJson() {
            JsonObject obj = new JsonObject();
            if (experience != 0) obj.addProperty("experience", experience);

            if (!loot.isEmpty()) {
                JsonArray arr = new JsonArray();
                for (String s : loot) arr.add(s);
                obj.add("loot", arr);
            }
            if (!recipes.isEmpty()) {
                JsonArray arr = new JsonArray();
                for (String s : recipes) arr.add(s);
                obj.add("recipes", arr);
            }
            if (function != null && !function.isEmpty()) {
                obj.addProperty("function", function);
            }
            return obj;
        }
    }

    public static final class Triggers {
        public static final String BRED_ANIMALS = "minecraft:bred_animals";
        public static final String BREWED_POTION = "minecraft:brewed_potion";
        public static final String CHANGED_DIMENSION = "minecraft:changed_dimension";
        public static final String CONSTRUCT_BEACON = "minecraft:construct_beacon";
        public static final String CONSUME_ITEM = "minecraft:consume_item";
        public static final String CURED_ZOMBIE_VILLAGER = "minecraft:cured_zombie_villager";
        public static final String EFFECTS_CHANGED = "minecraft:effects_changed";
        public static final String ENCHANTED_ITEM = "minecraft:enchanted_item";
        public static final String ENTER_BLOCK = "minecraft:enter_block";
        public static final String ENTITY_HURT_PLAYER = "minecraft:entity_hurt_player";
        public static final String IMPOSSIBLE = "minecraft:impossible";
        public static final String INVENTORY_CHANGED = "minecraft:inventory_changed";
        public static final String ITEM_DURABILITY_CHANGED = "minecraft:item_durability_changed";
        public static final String PLAYER_KILLED_ENTITY = "minecraft:player_killed_entity";
        public static final String ENTITY_KILLED_PLAYER = "minecraft:entity_killed_player";
        public static final String LEVITATION = "minecraft:levitation";
        public static final String NETHER_TRAVEL = "minecraft:nether_travel";
        public static final String PLACED_BLOCK = "minecraft:placed_block";
        public static final String PLAYER_HURT_ENTITY = "minecraft:player_hurt_entity";
        public static final String LOCATION = "minecraft:location"; // PositionTrigger
        public static final String RECIPE_UNLOCKED = "minecraft:recipe_unlocked";
        public static final String SUMMONED_ENTITY = "minecraft:summoned_entity";
        public static final String TAME_ANIMAL = "minecraft:tame_animal";
        public static final String TICK = "minecraft:tick";
        public static final String USED_ENDER_EYE = "minecraft:used_ender_eye";
        public static final String USED_TOTEM = "minecraft:used_totem";
        public static final String VILLAGER_TRADE = "minecraft:villager_trade";
    }
}
