package com.hbm.config;

import com.google.common.base.Optional;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockGlyphid;
import com.hbm.blocks.generic.BlockGlyphidSpawner;
import com.hbm.inventory.RecipesCommon;
import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockSand;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class FalloutConfigJSON {

    public static final List<FalloutEntry> entries = new ArrayList<>();
    public static final Gson gson = new Gson();
    public static final HashBiMap<String, Material> matNames = HashBiMap.create();

    static {
        matNames.put("grass", Material.GRASS);
        matNames.put("ground", Material.GROUND);
        matNames.put("wood", Material.WOOD);
        matNames.put("rock", Material.ROCK);
        matNames.put("iron", Material.IRON);
        matNames.put("anvil", Material.ANVIL);
        matNames.put("water", Material.WATER);
        matNames.put("lava", Material.LAVA);
        matNames.put("leaves", Material.LEAVES);
        matNames.put("plants", Material.PLANTS);
        matNames.put("vine", Material.VINE);
        matNames.put("sponge", Material.SPONGE);
        matNames.put("cloth", Material.CLOTH);
        matNames.put("fire", Material.FIRE);
        matNames.put("sand", Material.SAND);
        matNames.put("circuits", Material.CIRCUITS);
        matNames.put("carpet", Material.CARPET);
        matNames.put("redstoneLight", Material.REDSTONE_LIGHT);
        matNames.put("tnt", Material.TNT);
        matNames.put("coral", Material.CORAL);
        matNames.put("ice", Material.ICE);
        matNames.put("packedIce", Material.PACKED_ICE);
        matNames.put("snow", Material.SNOW);
        matNames.put("craftedSnow", Material.CRAFTED_SNOW);
        matNames.put("cactus", Material.CACTUS);
        matNames.put("clay", Material.CLAY);
        matNames.put("gourd", Material.GOURD);
        matNames.put("dragonEgg", Material.DRAGON_EGG);
        matNames.put("portal", Material.PORTAL);
        matNames.put("cake", Material.CAKE);
        matNames.put("web", Material.WEB);
        matNames.put("glass", Material.GLASS);
        matNames.put("piston", Material.PISTON);
    }

    public static void initialize() {
        File folder = MainRegistry.configHbmDir;

        File config = new File(folder.getAbsolutePath() + File.separatorChar + "hbmFallout.json");
        File template = new File(folder.getAbsolutePath() + File.separatorChar + "_hbmFallout.json");

        initDefault();

        if (!config.exists()) {
            writeDefault(template);
        } else {
            List<FalloutEntry> conf = readConfig(config);

            if (conf != null) {
                entries.clear();
                entries.addAll(conf);
            }
        }
    }

    @SuppressWarnings({"deprecation", "ObjectAllocationInLoop"})
    private static void initDefault() {
        double woodEffectRange = 65D;

        // petrify all wooden things possible
        entries.add(FalloutEntry.builder()
                .matchesBlock(Blocks.LOG)
                .preserve(BlockRotatedPillar.AXIS)
                .addPrimary(ModBlocks.waste_log.getDefaultState(), 1)
                .max(woodEffectRange)
                .solid(true)
                .build());

        entries.add(FalloutEntry.builder()
                .matchesBlock(Blocks.LOG2)
                .preserve(BlockRotatedPillar.AXIS)
                .addPrimary(ModBlocks.waste_log.getDefaultState(), 1)
                .max(woodEffectRange)
                .solid(true)
                .build());

        entries.add(FalloutEntry.builder()
                .matchesState(Blocks.RED_MUSHROOM_BLOCK.getStateFromMeta(10)) // exact state
                .addPrimary(ModBlocks.waste_log.getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.Y), 1)
                .max(woodEffectRange)
                .solid(true)
                .build());

        entries.add(FalloutEntry.builder()
                .matchesState(Blocks.BROWN_MUSHROOM_BLOCK.getStateFromMeta(10))
                .addPrimary(ModBlocks.waste_log.getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.Y), 1)
                .max(woodEffectRange)
                .solid(true)
                .build());

        entries.add(FalloutEntry.builder()
                .matchesBlock(Blocks.RED_MUSHROOM_BLOCK)
                .addPrimary(Blocks.AIR.getDefaultState(), 1)
                .max(woodEffectRange)
                .build());

        entries.add(FalloutEntry.builder()
                .matchesBlock(Blocks.BROWN_MUSHROOM_BLOCK)
                .addPrimary(Blocks.AIR.getDefaultState(), 1)
                .max(woodEffectRange)
                .build());

        entries.add(FalloutEntry.builder()
                .matchesBlock(Blocks.SNOW)
                .addPrimary(Blocks.AIR.getDefaultState(), 1)
                .max(woodEffectRange)
                .build());

        entries.add(FalloutEntry.builder()
                .matchesBlock(Blocks.PLANKS)
                .addPrimary(ModBlocks.waste_planks.getDefaultState(), 1)
                .max(woodEffectRange)
                .solid(true)
                .build());

        // if it can't be petrified, destroy it (wood/leaf/plant/vine)
        entries.add(FalloutEntry.builder()
                .matchingMaterial(Material.WOOD)
                .addPrimary(Blocks.AIR.getDefaultState(), 1)
                .max(woodEffectRange)
                .build());

        entries.add(FalloutEntry.builder()
                .matchingMaterial(Material.LEAVES)
                .addPrimary(Blocks.AIR.getDefaultState(), 1)
                .max(woodEffectRange)
                .build());

        entries.add(FalloutEntry.builder()
                .matchingMaterial(Material.PLANTS)
                .addPrimary(Blocks.AIR.getDefaultState(), 1)
                .max(woodEffectRange)
                .build());

        entries.add(FalloutEntry.builder()
                .matchingMaterial(Material.VINE)
                .addPrimary(Blocks.AIR.getDefaultState(), 1)
                .max(woodEffectRange)
                .build());

        entries.add(FalloutEntry.builder()
                .matchesBlock(ModBlocks.waste_leaves)
                .addPrimary(Blocks.AIR.getDefaultState(), 1)
                .max(woodEffectRange)
                .build());

        entries.add(FalloutEntry.builder()
                .matchesBlock(Blocks.LEAVES)
                .addPrimary(ModBlocks.waste_leaves.getDefaultState(), 1)
                .min(woodEffectRange - 5D)
                .build());

        entries.add(FalloutEntry.builder()
                .matchesBlock(Blocks.LEAVES2)
                .addPrimary(ModBlocks.waste_leaves.getDefaultState(), 1)
                .min(woodEffectRange - 5D)
                .build());

        entries.add(FalloutEntry.builder()
                .matchesBlock(Blocks.MOSSY_COBBLESTONE)
                .addPrimary(Blocks.COAL_ORE.getDefaultState(), 1)
                .solid(true)
                .build());

        entries.add(FalloutEntry.builder()
                .matchesBlock(ModBlocks.ore_nether_uranium)
                .addPrimary(ModBlocks.ore_nether_schrabidium.getDefaultState(), 1)
                .addPrimary(ModBlocks.ore_nether_uranium_scorched.getDefaultState(), 99)
                .solid(true)
                .build());

        entries.add(FalloutEntry.builder()
                .matchesBlock(ModBlocks.glyphid_base)
                .addPrimary(ModBlocks.glyphid_base.getDefaultState().withProperty(BlockGlyphid.TYPE, BlockGlyphid.Type.RAD), 1)
                .solid(true)
                .build());

        entries.add(FalloutEntry.builder()
                .matchesBlock(ModBlocks.glyphid_spawner)
                .addPrimary(ModBlocks.glyphid_spawner.getDefaultState().withProperty(BlockGlyphidSpawner.TYPE, BlockGlyphidSpawner.Type.RAD), 1)
                .solid(true)
                .build());

        for (int i = 1; i <= 10; i++) {
            int m = 10 - i;

            entries.add(FalloutEntry.builder()
                    .addPrimary(ModBlocks.ore_sellafield_diamond.getStateFromMeta(m), 3)
                    .addPrimary(ModBlocks.ore_sellafield_emerald.getStateFromMeta(m), 2)
                    .primaryChance(0.5)
                    .max(i * 5)
                    .opaque(true)
                    .solid(true)
                    .matchesBlock(Blocks.COAL_ORE)
                    .build());

            entries.add(FalloutEntry.builder()
                    .addPrimary(ModBlocks.ore_sellafield_diamond.getStateFromMeta(m), 1)
                    .primaryChance(0.2)
                    .max(i * 5)
                    .opaque(true)
                    .solid(true)
                    .matchesBlock(ModBlocks.ore_lignite)
                    .build());

            entries.add(FalloutEntry.builder()
                    .addPrimary(ModBlocks.ore_sellafield_emerald.getStateFromMeta(m), 1)
                    .max(i * 5)
                    .opaque(true)
                    .solid(true)
                    .matchesBlock(ModBlocks.ore_beryllium)
                    .build());

            if (m > 4) {
                entries.add(FalloutEntry.builder()
                        .addPrimary(ModBlocks.ore_sellafield_schrabidium.getStateFromMeta(m), 1)
                        .addPrimary(ModBlocks.ore_sellafield_uranium_scorched.getStateFromMeta(m), 9)
                        .max(i * 5)
                        .opaque(true)
                        .solid(true)
                        .matchesBlock(ModBlocks.ore_uranium)
                        .build());
            }
            if (m > 4) {
                entries.add(FalloutEntry.builder()
                        .addPrimary(ModBlocks.ore_sellafield_schrabidium.getStateFromMeta(m), 1)
                        .addPrimary(ModBlocks.ore_sellafield_uranium_scorched.getStateFromMeta(m), 9)
                        .max(i * 5)
                        .opaque(true)
                        .solid(true)
                        .matchesBlock(ModBlocks.ore_gneiss_uranium)
                        .build());
            }

            entries.add(FalloutEntry.builder()
                    .addPrimary(ModBlocks.ore_sellafield_radgem.getStateFromMeta(m), 1)
                    .max(i * 5)
                    .opaque(true)
                    .solid(true)
                    .matchesBlock(Blocks.DIAMOND_ORE)
                    .build());

            entries.add(FalloutEntry.builder()
                    .addPrimary(ModBlocks.sellafield_bedrock.getStateFromMeta(m), 1)
                    .max(i * 5)
                    .opaque(true)
                    .solid(true)
                    .matchesBlock(Blocks.BEDROCK)
                    .build());

            entries.add(FalloutEntry.builder()
                    .addPrimary(ModBlocks.sellafield_bedrock.getStateFromMeta(m), 1)
                    .max(i * 5)
                    .opaque(true)
                    .solid(true)
                    .matchesBlock(ModBlocks.ore_bedrock_block)
                    .build());

            entries.add(FalloutEntry.builder()
                    .addPrimary(ModBlocks.sellafield_bedrock.getStateFromMeta(m), 1)
                    .max(i * 5)
                    .opaque(true)
                    .solid(true)
                    .matchesBlock(ModBlocks.ore_bedrock_oil)
                    .build());

            entries.add(FalloutEntry.builder()
                    .addPrimary(ModBlocks.sellafield_bedrock.getStateFromMeta(m), 1)
                    .max(i * 5)
                    .opaque(true)
                    .solid(true)
                    .matchesBlock(ModBlocks.sellafield_bedrock)
                    .build());

            entries.add(FalloutEntry.builder()
                    .addPrimary(ModBlocks.sellafield_slaked.getStateFromMeta(m), 1)
                    .max(i * 5)
                    .opaque(true)
                    .solid(true)
                    .matchingMaterial(Material.IRON)
                    .build());

            entries.add(FalloutEntry.builder()
                    .addPrimary(ModBlocks.sellafield_slaked.getStateFromMeta(m), 1)
                    .max(i * 5)
                    .opaque(true)
                    .solid(true)
                    .matchingMaterial(Material.ROCK)
                    .build());

            entries.add(FalloutEntry.builder()
                    .addPrimary(ModBlocks.sellafield_slaked.getStateFromMeta(m), 1)
                    .max(i * 5)
                    .opaque(true)
                    .solid(true)
                    .matchingMaterial(Material.SAND)
                    .build());

            entries.add(FalloutEntry.builder()
                    .addPrimary(ModBlocks.sellafield_slaked.getStateFromMeta(m), 1)
                    .max(i * 5)
                    .opaque(true)
                    .solid(true)
                    .matchingMaterial(Material.GROUND)
                    .build());

            if (i <= 9) {
                entries.add(FalloutEntry.builder()
                        .addPrimary(ModBlocks.sellafield_slaked.getStateFromMeta(m), 1)
                        .max(i * 5)
                        .opaque(true)
                        .solid(true)
                        .matchingMaterial(Material.GRASS)
                        .build());
            }
        }

        entries.add(FalloutEntry.builder()
                .matchesBlock(Blocks.MYCELIUM)
                .addPrimary(ModBlocks.waste_mycelium.getDefaultState(), 1)
                .solid(true)
                .build());

        entries.add(FalloutEntry.builder()
                .matchesState(Blocks.SAND.getDefaultState())
                .addPrimary(ModBlocks.waste_trinitite.getDefaultState(), 1)
                .primaryChance(0.05)
                .solid(true)
                .build());

        entries.add(FalloutEntry.builder()
                .matchesState(Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND))
                .addPrimary(ModBlocks.waste_trinitite_red.getDefaultState(), 1)
                .primaryChance(0.05)
                .solid(true)
                .build());

        entries.add(FalloutEntry.builder()
                .matchesBlock(Blocks.CLAY)
                .addPrimary(Blocks.HARDENED_CLAY.getDefaultState(), 1)
                .solid(true)
                .build());
    }

    private static void writeDefault(File file) {
        try (JsonWriter writer = new JsonWriter(new FileWriter(file))) {
            writer.setIndent("  ");
            writer.beginObject();
            writer.name("entries").beginArray();

            for (FalloutEntry entry : entries) {
                writer.beginObject();
                entry.write(writer);
                writer.endObject();
            }

            writer.endArray();
            writer.endObject();
        } catch (IOException e) {
            MainRegistry.logger.catching(e);
        }
    }

    private static List<FalloutEntry> readConfig(File config) {
        try (FileReader fr = new FileReader(config)) {
            JsonObject json = gson.fromJson(fr, JsonObject.class);
            JsonArray arr = json.getAsJsonArray("entries");
            List<FalloutEntry> conf = new ArrayList<>(arr.size());

            for (JsonElement recipe : arr) {
                FalloutEntry e = FalloutEntry.readEntry(recipe);
                if (e != null) conf.add(e);
            }
            return conf;
        } catch (Exception ex) {
            MainRegistry.logger.catching(ex);
        }
        return null;
    }

    public static class FalloutEntry implements Cloneable {
        private IBlockState blockState = null;
        private Material matchesMaterial = null;
        private boolean matchState = true;
        private boolean matchesOpaque = false;
        private IProperty<?>[] preservedProperties = null;
        private List<Tuple<IBlockState, Integer>> primaryBlocks = Collections.emptyList();
        private List<Tuple<IBlockState, Integer>> secondaryBlocks = Collections.emptyList();

        private double primaryChance = 1.0D;
        private double minDist = 0.0D;
        private double maxDist = 100.0D;
        private double falloffStart = 0.9D;

        /**
         * Whether the depth value should be decremented when this block is converted
         */
        private boolean solid = false;

        public static Builder builder() {
            return new Builder();
        }

        private static String stateToString(IBlockState state) {
            StringBuilder builder = new StringBuilder();
            Block block = state.getBlock();

            builder.append(ForgeRegistries.BLOCKS.getKey(block));
            builder.append("[");

            ImmutableMap<IProperty<?>, Comparable<?>> properties = state.getProperties();
            Iterator<Map.Entry<IProperty<?>, Comparable<?>>> iterator =
                    properties.entrySet().stream()
                            .sorted(Comparator.comparing(e -> e.getKey().getName()))
                            .iterator();

            while (iterator.hasNext()) {
                Map.Entry<IProperty<?>, Comparable<?>> entry = iterator.next();
                builder.append(entry.getKey().getName())
                        .append("=")
                        .append(entry.getValue().toString());
                if (iterator.hasNext()) {
                    builder.append(", ");
                }
            }

            builder.append("]");
            return builder.toString();
        }

        private static FalloutEntry readEntry(JsonElement recipe) {
            if (!recipe.isJsonObject()) return null;
            JsonObject obj = recipe.getAsJsonObject();

            Builder b = builder();

            if (obj.has("matchesBlock")) {
                b.matchesState(parseBlockState(obj.get("matchesBlock").getAsString()));
            }
            if (obj.has("matchesState")) b.matchState = obj.get("matchesState").getAsBoolean();
            if (obj.has("mustBeOpaque")) b.opaque(obj.get("mustBeOpaque").getAsBoolean());
            if (obj.has("matchesMaterial")) {
                String key = obj.get("matchesMaterial").getAsString();
                Material m = matNames.get(key);
                if (m != null) b.matchingMaterial(m);
            }
            if (obj.has("restrictDepth")) b.solid(obj.get("restrictDepth").getAsBoolean());
            if (obj.has("preserveState") && b.blockState != null) {
                IProperty<?>[] props = readPreserveStateArray(b.blockState.getBlock(), obj.get("preserveState"));
                if (props != null && props.length > 0) b.preserve(props);
            }

            if (obj.has("primarySubstitution")) {
                List<Tuple<IBlockState, Integer>> p = readMetaArray(obj.get("primarySubstitution"));
                if (p != null) for (Tuple<IBlockState, Integer> t : p) b.addPrimary(t.getFirst(), t.getSecond());
            }
            if (obj.has("secondarySubstitutions")) {
                List<Tuple<IBlockState, Integer>> s = readMetaArray(obj.get("secondarySubstitutions"));
                if (s != null) for (Tuple<IBlockState, Integer> t : s) b.addSecondary(t.getFirst(), t.getSecond());
            }

            if (obj.has("chance")) b.primaryChance(obj.get("chance").getAsDouble());
            if (obj.has("minimumDistancePercent")) b.min(obj.get("minimumDistancePercent").getAsDouble());
            if (obj.has("maximumDistancePercent")) b.max(obj.get("maximumDistancePercent").getAsDouble());
            if (obj.has("falloffStartFactor")) b.falloff(obj.get("falloffStartFactor").getAsDouble());

            return b.build();
        }

        private static IProperty<?>[] readPreserveStateArray(Block block, JsonElement element) {
            if (!element.isJsonArray()) return null;

            JsonArray array = element.getAsJsonArray();
            List<IProperty<?>> props = new ArrayList<>(array.size());

            for (int i = 0; i < array.size(); i++) {
                String rawProperty = array.get(i).getAsString();
                IProperty<?> property = getPropertyByName(block, rawProperty);
                if (property != null && !props.contains(property)) {
                    props.add(property);
                }
            }
            return props.isEmpty() ? null : props.toArray(new IProperty<?>[0]);
        }

        private static void writePreserveStateArray(JsonWriter writer, IProperty<?>[] properties) throws IOException {
            writer.beginArray();
            for (IProperty<?> p : properties) {
                if (p == null) continue;
                writer.value(p.getName());
            }
            writer.endArray();
        }

        private static IProperty<?> getPropertyByName(Block block, String name) {
            for (IProperty<?> prop : block.getBlockState().getProperties()) {
                if (prop.getName().equals(name)) {
                    return prop;
                }
            }
            return null;
        }

        private static void writeStateArray(JsonWriter writer, List<Tuple<IBlockState, Integer>> array) throws IOException {
            writer.beginArray();
            writer.setIndent("");
            for (Tuple<IBlockState, Integer> state : array) {
                writer.beginArray();
                writer.value(stateToString(state.getFirst()));
                writer.value(state.getSecond());
                writer.endArray();
            }
            writer.endArray();
            writer.setIndent("  ");
        }

        private static List<Tuple<IBlockState, Integer>> readMetaArray(JsonElement jsonElement) {
            if (!jsonElement.isJsonArray()) return null;

            JsonArray array = jsonElement.getAsJsonArray();
            List<Tuple<IBlockState, Integer>> out = new ArrayList<>(array.size());

            for (int i = 0; i < array.size(); i++) {
                JsonElement metaBlock = array.get(i);
                if (!metaBlock.isJsonArray()) {
                    throw new IllegalStateException("Could not read meta block " + metaBlock);
                }
                JsonArray mBArray = metaBlock.getAsJsonArray();
                out.add(new Tuple<>(parseBlockState(mBArray.get(0).getAsString()), mBArray.get(1).getAsInt()));
            }
            return out;
        }

        private static IBlockState parseBlockState(String input) {
            String blockName;
            String propsPart = null;
            int idx = input.indexOf('[');
            if (idx != -1) {
                blockName = input.substring(0, idx);
                propsPart = input.substring(idx + 1, input.length() - 1);
            } else {
                blockName = input;
            }

            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
            if (block == null) {
                throw new IllegalArgumentException("Unknown block: " + blockName);
            }
            IBlockState state = block.getDefaultState();

            if (propsPart == null || propsPart.isEmpty()) {
                return state;
            }
            String[] props = propsPart.split(",");
            for (String prop : props) {
                String[] kv = prop.split("=");
                if (kv.length != 2) continue;

                String key = kv[0].trim();
                String val = kv[1].trim();

                IProperty<?> property = block.getBlockState().getProperty(key);
                if (property == null) {
                    throw new IllegalArgumentException("Unknown property " + key + " for block " + blockName);
                }

                state = setPropertyFromString(state, property, val);
            }
            return state;
        }

        private static <T extends Comparable<T>> IBlockState setPropertyFromString(IBlockState state, IProperty<T> property, String val) {
            Optional<T> parsed = property.parseValue(val);
            if (!parsed.isPresent()) {
                throw new IllegalArgumentException("Invalid value " + val + " for property " + property.getName());
            }
            return state.withProperty(property, parsed.get());
        }

        private static IBlockState copyProperty(IBlockState from, IBlockState to, String propertyName) {
            final IProperty<?> fromProp = getPropertyByName(from.getBlock(), propertyName);
            final IProperty<?> toProp = getPropertyByName(to.getBlock(), propertyName);
            if (fromProp == null || toProp == null) return to;
            return copyPropertyGeneric(from, to, fromProp, toProp);
        }

        private static <F extends Comparable<F>, T extends Comparable<T>> IBlockState copyPropertyGeneric(IBlockState from, IBlockState to,
                                                                                                          IProperty<F> fromProp,
                                                                                                          IProperty<T> toProp) {
            final F oldValue = from.getValue(fromProp);
            final T mapped = coerceValue(oldValue, fromProp, toProp);
            return mapped == null ? to : to.withProperty(toProp, mapped);
        }

        private static <F extends Comparable<F>, T extends Comparable<T>> T coerceValue(F oldValue, IProperty<F> fromProp, IProperty<T> toProp) {
            final String srcName = fromProp.getName(oldValue);
            final Optional<T> parsed = toProp.parseValue(srcName);
            if (parsed.isPresent()) return parsed.get();

            final Collection<T> allowed = toProp.getAllowedValues();
            if (toProp.getValueClass().isInstance(oldValue)) {
                final T sameTyped = (T) oldValue;
                if (allowed.contains(sameTyped)) return sameTyped;
            }

            for (T a : allowed) {
                String aName;
                try {
                    aName = toProp.getName(a);
                } catch (Throwable t) {
                    aName = String.valueOf(a);
                }
                if (aName.equals(srcName) || aName.equalsIgnoreCase(srcName) || String.valueOf(a).equals(srcName) ||
                        String.valueOf(a).equalsIgnoreCase(srcName)) {
                    return a;
                }
            }

            if (oldValue instanceof Enum<?> oldEnum) {
                for (T a : allowed) {
                    if (a instanceof Enum<?> tgt && tgt.ordinal() == oldEnum.ordinal()) {
                        return a;
                    }
                }
            }

            if (oldValue instanceof Integer oldInt) {
                for (T a : allowed) {
                    if (a instanceof Integer ai && ai.equals(oldInt)) return a;
                }
            } else if (oldValue instanceof Boolean oldBool) {
                for (T a : allowed) {
                    if (a instanceof Boolean ab && ab.equals(oldBool)) return a;
                }
            }
            return null;
        }

        private static RecipesCommon.MetaBlock chooseRandomOutcome(List<Tuple<IBlockState, Integer>> blocks, Random random) {
            if (blocks == null || blocks.isEmpty()) return null;

            int weight = 0;
            for (Tuple<IBlockState, Integer> choice : blocks) {
                weight += choice.getSecond();
            }

            int r = random.nextInt(weight);
            for (Tuple<IBlockState, Integer> choice : blocks) {
                r -= choice.getSecond();
                if (r <= 0) {
                    return RecipesCommon.metaOf(choice.getFirst());
                }
            }
            return RecipesCommon.metaOf(blocks.get(0).getFirst());
        }

        @Override
        public FalloutEntry clone() {
            FalloutEntry entry;
            try {
                entry = (FalloutEntry) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
            entry.preservedProperties = this.preservedProperties == null ? null : this.preservedProperties.clone();
            entry.primaryBlocks = this.primaryBlocks.isEmpty() ? Collections.emptyList() : new ArrayList<>(this.primaryBlocks);
            entry.secondaryBlocks = this.secondaryBlocks.isEmpty() ? Collections.emptyList() : new ArrayList<>(this.secondaryBlocks);
            return entry;
        }

        /**
         * Precise match by default
         */
        public FalloutEntry setBlockState(IBlockState block) {
            this.blockState = block;
            this.matchState = true;
            return this;
        }

        public FalloutEntry setBlock(Block block) {
            this.blockState = block.getDefaultState();
            this.matchState = false;
            return this;
        }

        public FalloutEntry setMatchingMaterial(Material mat) {
            this.matchesMaterial = mat;
            return this;
        }

        public FalloutEntry shouldBeOpaque(boolean opaque) {
            this.matchesOpaque = opaque;
            return this;
        }

        public FalloutEntry setPrimaryChance(double chance) {
            this.primaryChance = chance;
            return this;
        }

        public FalloutEntry setMinDistance(double min) {
            this.minDist = min;
            return this;
        }

        public FalloutEntry setMax(double max) {
            this.maxDist = max;
            return this;
        }

        public FalloutEntry shouldMatchState(boolean matchState) {
            this.matchState = matchState;
            return this;
        }

        public FalloutEntry setFallOffStart(double falloffStart) {
            this.falloffStart = falloffStart;
            return this;
        }

        public void setPreserveState(IProperty<?>... properties) {
            this.preservedProperties = properties;
        }

        public FalloutEntry withPreserveState(IProperty<?>... properties) {
            this.preservedProperties = properties;
            return this;
        }

        /**
         * Evaluates whether this {@link FalloutEntry} should convert the block at the given position.
         *
         * @param yGlobal    the target block's y coordinate
         * @param blockState the current block state at {@code pos} to test against this entry
         * @param dist       distance factor from the effect origin (same units as {@link #minDist}/{@link #maxDist}; typically a percentage)
         * @return the IBlockState if the block needs to be replaced, or null if no-op
         */
        @Nullable
        @Contract(mutates = "param4")
        public IBlockState eval(int yGlobal, IBlockState blockState, double dist, Random random) {
            if (dist > maxDist || dist < minDist) return null;

            Block originalBlock = blockState.getBlock();
            if (this.blockState != null) {
                boolean matches = matchState
                        ? blockState.equals(this.blockState)
                        : originalBlock == this.blockState.getBlock();
                if (!matches) return null;
            }
            if (matchesMaterial != null && blockState.getMaterial() != matchesMaterial) return null;
            if (matchesOpaque && !blockState.isOpaqueCube()) return null;

            if (dist > maxDist * falloffStart) {
                double t = (dist - maxDist * falloffStart) / (maxDist - maxDist * falloffStart);
                if (Math.abs(random.nextGaussian()) < t * t * 3.0) {
                    return null;
                }
            }

            RecipesCommon.MetaBlock conversion =
                    chooseRandomOutcome((primaryChance == 1D || random.nextDouble() < primaryChance) ? primaryBlocks : secondaryBlocks, random);

            if (conversion == null) return null;

            int originalMeta = originalBlock.getMetaFromState(blockState);

            if (conversion.block == ModBlocks.sellafield_slaked &&
                    originalBlock == ModBlocks.sellafield_slaked &&
                    conversion.meta <= originalMeta) {
                return null;
            }

            if (conversion.block == ModBlocks.sellafield_bedrock &&
                    originalBlock == ModBlocks.sellafield_bedrock &&
                    conversion.meta <= originalMeta) {
                return null;
            }

            if (originalBlock == ModBlocks.sellafield_bedrock &&
                    conversion.block != ModBlocks.sellafield_bedrock) {
                return null;
            }

            if (yGlobal == 0 && conversion.block != ModBlocks.sellafield_bedrock) {
                return null;
            }

            IBlockState newState = conversion.block.getStateFromMeta(conversion.meta);
            if (preservedProperties != null) {
                for (IProperty<?> property : preservedProperties) {
                    newState = copyProperty(blockState, newState, property.getName());
                }
            }
            return newState;
        }

        public boolean isSolid() {
            return this.solid;
        }

        public FalloutEntry setSolid(boolean solid) {
            this.solid = solid;
            return this;
        }

        public void write(JsonWriter writer) throws IOException {
            if (blockState != null) {
                writer.name("matchesBlock").value(stateToString(blockState));
                writer.name("matchesState").value(matchState);
            }
            if (matchesOpaque) writer.name("mustBeOpaque").value(true);

            if (matchesMaterial != null) {
                String matName = matNames.inverse().get(matchesMaterial);
                if (matName != null) {
                    writer.name("matchesMaterial").value(matName);
                }
            }
            if (solid) writer.name("restrictDepth").value(true);
            if (preservedProperties != null && preservedProperties.length > 0) {
                writer.name("preserveState");
                writePreserveStateArray(writer, preservedProperties);
            }

            if (!primaryBlocks.isEmpty()) {
                writer.name("primarySubstitution");
                writeStateArray(writer, primaryBlocks);
            }
            if (!secondaryBlocks.isEmpty()) {
                writer.name("secondarySubstitutions");
                writeStateArray(writer, secondaryBlocks);
            }

            if (primaryChance != 1D) writer.name("chance").value(primaryChance);
            if (minDist != 0.0D) writer.name("minimumDistancePercent").value(minDist);
            if (maxDist != 100.0D) writer.name("maximumDistancePercent").value(maxDist);
            if (falloffStart != 0.9D) writer.name("falloffStartFactor").value(falloffStart);
        }

        public static final class Builder {
            private final List<IProperty<?>> preservedProperties = new ArrayList<>();
            private final List<Tuple<IBlockState, Integer>> primaryBlocks = new ArrayList<>();
            private final List<Tuple<IBlockState, Integer>> secondaryBlocks = new ArrayList<>();
            private IBlockState blockState = null;
            private Material matchesMaterial = null;
            private boolean matchState = true;
            private boolean matchesOpaque = false;
            private double primaryChance = 1.0D;
            private double minDist = 0.0D;
            private double maxDist = 100.0D;
            private double falloffStart = 0.9D;
            private boolean isSolid = false;

            public Builder matchesState(IBlockState state) {
                this.blockState = state;
                this.matchState = true;
                return this;
            }

            public Builder matchesBlock(Block block) {
                this.blockState = block.getDefaultState();
                this.matchState = false;
                return this;
            }

            public Builder matchingMaterial(Material mat) {
                this.matchesMaterial = mat;
                return this;
            }

            public Builder opaque(boolean mustBeOpaque) {
                this.matchesOpaque = mustBeOpaque;
                return this;
            }

            public Builder preserve(IProperty<?>... properties) {
                Collections.addAll(this.preservedProperties, properties);
                return this;
            }

            public Builder addPrimary(IBlockState state, int weight) {
                this.primaryBlocks.add(new Tuple<>(state, weight));
                return this;
            }

            public Builder addSecondary(IBlockState state, int weight) {
                this.secondaryBlocks.add(new Tuple<>(state, weight));
                return this;
            }

            public Builder primaryChance(double chance) {
                this.primaryChance = chance;
                return this;
            }

            public Builder min(double min) {
                this.minDist = min;
                return this;
            }

            public Builder max(double max) {
                this.maxDist = max;
                return this;
            }

            public Builder falloff(double startFactor) {
                this.falloffStart = startFactor;
                return this;
            }

            public Builder solid(boolean solid) {
                this.isSolid = solid;
                return this;
            }

            public FalloutEntry build() {
                FalloutEntry e = new FalloutEntry();
                e.blockState = this.blockState;
                e.matchesMaterial = this.matchesMaterial;
                e.matchState = this.matchState;
                e.matchesOpaque = this.matchesOpaque;
                e.preservedProperties = this.preservedProperties.isEmpty() ? null : this.preservedProperties.toArray(new IProperty<?>[0]);
                e.primaryBlocks = this.primaryBlocks.isEmpty() ? Collections.emptyList() : new ArrayList<>(this.primaryBlocks);
                e.secondaryBlocks = this.secondaryBlocks.isEmpty() ? Collections.emptyList() : new ArrayList<>(this.secondaryBlocks);
                e.primaryChance = this.primaryChance;
                e.minDist = this.minDist;
                e.maxDist = this.maxDist;
                e.falloffStart = this.falloffStart;
                e.solid = this.isSolid;
                return e;
            }
        }
    }
}
