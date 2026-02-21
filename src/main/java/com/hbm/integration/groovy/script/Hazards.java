package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.registry.AbstractReloadableStorage;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.hazard.HazardData;
import com.hbm.hazard.HazardEntry;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.hazard.modifier.*;
import com.hbm.hazard.transformer.HazardTransformerPostCustom;
import com.hbm.hazard.transformer.HazardTransformerPostCustom.StackKey;
import com.hbm.hazard.type.IHazardType;
import com.hbm.hazard.type.HazardTypeDangerousDrop;
import com.hbm.hazard.type.HazardTypeUnstable;
import com.hbm.inventory.RecipesCommon;
import com.hbm.lib.ObjObjDoubleConsumer;
import com.hbm.util.ItemStackUtil;
import groovy.lang.Closure;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;

@SuppressWarnings("MethodMayBeStatic")
@RegistryDescription(linkGenerator = "hbm")
public final class Hazards extends VirtualizedRegistry<Hazards.HazardRecipe> {

    private final HazardTypeFacade types = new HazardTypeFacade();
    private final HazardModifierFacade modifiers = new HazardModifierFacade();

    public Hazards() {
        super(Collections.singletonList("hazards"));
    }

    @Override
    public void onReload() {
        for (HazardRecipe recipe : removeScripted()) {
            if (recipe instanceof MapEntry m) {
                HazardSystem.unregister(m.target.unwrap());
            } else if (recipe instanceof BlacklistEntry b) {
                HazardSystem.unblacklist(b.target.unwrap());
            } else if (recipe instanceof ItemPostTransformer ipt) {
                HazardTransformerPostCustom.removeItemPost(ipt.item, ipt.fn);
            } else if (recipe instanceof StackPostTransformer spt) {
                HazardTransformerPostCustom.removeStackPost(spt.key, spt.fn);
            } else if (recipe instanceof ItemMultiplier imc) {
                HazardTransformerPostCustom.clearItemMultiplier(imc.item);
            }
        }
        for (HazardRecipe recipe : restoreFromBackup()) {
            if (recipe instanceof MapEntry m) {
                HazardSystem.register(m.target.unwrap(), m.data);
            } else if (recipe instanceof BlacklistEntry b) {
                HazardSystem.blacklist(b.target.unwrap());
            } else if (recipe instanceof ItemPostTransformer ipt) {
                HazardTransformerPostCustom.addItemPost(ipt.item, ipt.fn);
            } else if (recipe instanceof StackPostTransformer spt) {
                HazardTransformerPostCustom.addStackPost(spt.key, spt.fn);
            } else if (recipe instanceof ItemMultiplier imc) {
                HazardTransformerPostCustom.setItemMultiplier(imc.item, imc.multiplier);
            }
        }
        refresh();
    }

    @Override
    protected AbstractReloadableStorage<HazardRecipe> createRecipeStorage() {
        return new AbstractReloadableStorage<>() {
            @Override
            protected boolean compareRecipe(HazardRecipe r1, HazardRecipe r2) {
                if (r1 == r2) return true;
                if (r1 == null || r2 == null) return false;
                if (r1.getClass() != r2.getClass()) return false;
                if (r1 instanceof MapEntry a && r2 instanceof MapEntry b) {
                    return Objects.equals(a.target(), b.target()) && a.data() == b.data();
                }
                if (r1 instanceof BlacklistEntry a && r2 instanceof BlacklistEntry b) {
                    return Objects.equals(a.target(), b.target());
                }
                if (r1 instanceof ItemPostTransformer a && r2 instanceof ItemPostTransformer b) {
                    return a.item() == b.item() && a.fn() == b.fn();
                }
                if (r1 instanceof StackPostTransformer a && r2 instanceof StackPostTransformer b) {
                    return Objects.equals(a.key(), b.key()) && a.fn() == b.fn();
                }
                if (r1 instanceof ItemMultiplier a && r2 instanceof ItemMultiplier b) {
                    return a.item() == b.item() && Double.compare(a.multiplier(), b.multiplier()) == 0;
                }
                return false;
            }
        };
    }

    @MethodDescription(type = MethodDescription.Type.QUERY, description = "Accessors for built-in and factory hazard types. Example: hazards.getTypes().radiation()")
    public HazardTypeFacade getTypes() {
        return types;
    }

    @MethodDescription(type = MethodDescription.Type.QUERY, description = "Accessors for helper hazard modifiers (fuel/RTG/RBMK/etc.).")
    public HazardModifierFacade getModifiers() {
        return modifiers;
    }

    @MethodDescription(type = MethodDescription.Type.QUERY, description = "Create a new mutable HazardData builder. Use with DSL or method chaining, then call build().")
    public HazardDataBuilder data() {
        return new HazardDataBuilder();
    }

    @MethodDescription(type = MethodDescription.Type.ADDITION, description = "Register hazard data for a target (Item/ItemStack/Block/ore dict/ResourceLocation/IIngredient/Collection). Replaces existing data, backs up old mapping for reload.")
    public HazardData register(Object target, HazardData data) {
        if (data == null) {
            GroovyLog.get().warn("HBM hazard register: data is null for target {}", target);
            return null;
        }
        int count = registerTargets(target, data);
        refreshIfNeeded(count, "register", target);
        return data;
    }

    @MethodDescription(type = MethodDescription.Type.ADDITION, description = "Register hazard data using a builder. Equivalent to register(target, builder.build()).")
    public HazardData register(Object target, HazardDataBuilder builder) {
        if (builder == null) {
            GroovyLog.get().warn("HBM hazard register: builder is null for target {}", target);
            return null;
        }
        return register(target, builder.build());
    }

    @MethodDescription(type = MethodDescription.Type.ADDITION, description = "Register hazard data using a Groovy closure DSL. The closure receives a HazardDataBuilder as its delegate.")
    public HazardData register(Object target, Closure<?> definition) {
        if (definition == null) {
            GroovyLog.get().warn("HBM hazard register: closure is null for target {}", target);
            return null;
        }
        HazardDataBuilder builder = data();
        Closure<?> callable = (Closure<?>) definition.clone();
        callable.setDelegate(builder);
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.call(builder);
        return register(target, builder.build());
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, description = "Remove hazard mappings for a target (Item/Stack/etc.). Restores previous mapping on reload if one existed.")
    public boolean unregister(Object target) {
        int count = forEachTarget(target, this::unregisterObject, this::unregisterStack);
        refreshIfNeeded(count, "unregister", target);
        return count > 0;
    }

    @MethodDescription(type = MethodDescription.Type.VALUE, description = "Add a target to the hazard blacklist. The target won't be evaluated for hazards until unblacklisted.")
    public void blacklist(Object target) {
        int count = forEachTarget(target, o -> {
            addScripted(new BlacklistEntry(HazardTarget.of(o)));
            HazardSystem.blacklist(o);
            return true;
        }, stack -> {
            addScripted(new BlacklistEntry(HazardTarget.ofStack(stack)));
            HazardSystem.blacklist(stack);
            return true;
        });
        refreshIfNeeded(count, "blacklist", target);
    }

    @MethodDescription(type = MethodDescription.Type.VALUE, description = "Remove a target from the hazard blacklist. Returns true if an entry was removed.")
    public boolean unblacklist(Object target) {
        int count = forEachTarget(target, this::unblacklistObject, this::unblacklistStack);
        refreshIfNeeded(count, "unblacklist", target);
        return count > 0;
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, description = "Clear internal hazard caches without changing mappings. Use after bulk content changes.")
    public void clearCaches() {
        HazardSystem.clearCaches();
    }

    @MethodDescription(type = MethodDescription.Type.VALUE, description = "Recompute cache and schedule updates for all online players. Called automatically by mutating methods.")
    public void refresh() {
        HazardSystem.clearCaches();
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null) {
            for (EntityPlayer player : server.getPlayerList().getPlayers()) {
                HazardSystem.schedulePlayerUpdate(player);
            }
        }
    }

    @MethodDescription(type = MethodDescription.Type.QUERY, description = "Convenience: read radiation hazard level for the given ItemStack. Returns 0 if empty.")
    public double radiationLevel(ItemStack stack) {
        return hazardLevel(stack, HazardRegistry.RADIATION);
    }

    @MethodDescription(type = MethodDescription.Type.QUERY, description = "Compute hazard level for the given ItemStack and HazardType. Returns 0 if stack/type invalid.")
    public double hazardLevel(ItemStack stack, IHazardType type) {
        if (stack == null || stack.isEmpty() || type == null) return 0D;
        return HazardSystem.getHazardLevelFromStack(stack, type);
    }

    @MethodDescription(type = MethodDescription.Type.ADDITION, description = "Add a post-transformer for a specific Item or ItemStack. If respectNbt=true, only applies when sanitized NBT matches. Runs after built-in and fluid hazards. The transformer receives (stack, entries) and must return the new list.")
    public void postTransform(Object target, boolean respectNbt, BiFunction<ItemStack, List<HazardEntry>, List<HazardEntry>> transformer) {
        if (transformer == null || target == null) {
            GroovyLog.get().warn("HBM hazards postTransform: invalid args: target={}, transformer={}", target, transformer);
            return;
        }
        if (target instanceof Item item) {
            addScripted(new ItemPostTransformer(item, transformer));
            HazardTransformerPostCustom.addItemPost(item, transformer);
            refresh();
            return;
        }
        if (target instanceof String || target instanceof ResourceLocation) {
            Item item = resolveItem(target);
            if (item != null) {
                addScripted(new ItemPostTransformer(item, transformer));
                HazardTransformerPostCustom.addItemPost(item, transformer);
                refresh();
                return;
            }
        }
        if (target instanceof ItemStack stack) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            StackKey key = StackKey.of(copy, respectNbt);
            addScripted(new StackPostTransformer(key, transformer));
            HazardTransformerPostCustom.addStackPost(key, transformer);
            refresh();
            return;
        }
        if (target instanceof IIngredient ingredient) {
            int added = 0;
            for (ItemStack s : ingredient.getMatchingStacks()) {
                if (s == null || s.isEmpty()) continue;
                ItemStack one = s.copy();
                one.setCount(1);
                StackKey key = StackKey.of(one, respectNbt);
                addScripted(new StackPostTransformer(key, transformer));
                HazardTransformerPostCustom.addStackPost(key, transformer);
                added++;
            }
            refreshIfNeeded(added, "postTransform", target);
            return;
        }
        GroovyLog.get().warn("HBM hazards postTransform: unsupported target type {}", target.getClass().getName());
    }

    @MethodDescription(type = MethodDescription.Type.ADDITION, description = "Multiply all computed hazard entries for an Item after fluid passes. Example: multiplyItemHazards('hbm:hot_ingot', 0.5D)")
    public void multiplyItemHazards(Object itemId, double hazardMultiplier) {
        Item item = resolveItem(itemId);
        if (item == null) {
            GroovyLog.get().warn("HBM hazards multiplyItemHazards: unknown item {}", itemId);
            return;
        }
        if (HazardTransformerPostCustom.hasItemMultiplier(item)) {
            double prev = HazardTransformerPostCustom.getItemMultiplier(item);
            addBackup(new ItemMultiplier(item, prev));
        }
        HazardTransformerPostCustom.setItemMultiplier(item, hazardMultiplier);
        addScripted(new ItemMultiplier(item, hazardMultiplier));
        refresh();
    }

    @Nullable
    private Item resolveItem(Object id) {
        if (id instanceof Item item) return item;
        if (id instanceof ResourceLocation rl) return ForgeRegistries.ITEMS.getValue(rl);
        if (id instanceof ItemStack st) return st.getItem();
        if (id instanceof String s) {
            String str = s.trim();
            if (str.isEmpty()) return null;
            ResourceLocation rl = new ResourceLocation(str);
            return ForgeRegistries.ITEMS.getValue(rl);
        }
        return null;
    }

    private int registerTargets(Object target, HazardData data) {
        return forEachTarget(target, o -> registerObject(o, data), stack -> registerStack(stack, data));
    }

    private int forEachTarget(Object target, Function<Object, Boolean> objectFunction, Function<ItemStack, Boolean> stackFunction) {
        if (target == null) {
            return 0;
        }
        if (target instanceof HazardDataBuilder) {
            GroovyLog.get().warn("Received HazardDataBuilder where a target was expected. Use hazards.register(target, builder) instead.");
            return 0;
        }
        if (target instanceof HazardData) {
            GroovyLog.get().warn("Expected hazard target but received HazardData. Did you mean to call hazards.register(target, data)?");
            return 0;
        }
        if (target instanceof IIngredient ingredient) {
            int count = 0;
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                if (stack != null && !stack.isEmpty()) {
                    ItemStack copy = stack.copy();
                    copy.setCount(1);
                    Boolean applied = stackFunction.apply(copy);
                    if (Boolean.TRUE.equals(applied)) count++;
                }
            }
            return count;
        }
        if (target instanceof ItemStack stack) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            Boolean applied = stackFunction.apply(copy);
            return Boolean.TRUE.equals(applied) ? 1 : 0;
        }
        if (target instanceof Collection<?>) {
            int count = 0;
            for (Object element : (Collection<?>) target) {
                count += forEachTarget(element, objectFunction, stackFunction);
            }
            return count;
        }
        if (target.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(target);
            int count = 0;
            for (int i = 0; i < length; i++) {
                Object element = java.lang.reflect.Array.get(target, i);
                count += forEachTarget(element, objectFunction, stackFunction);
            }
            return count;
        }
        if (target instanceof String value) {
            if (value.contains(":")) {
                try {
                    return forEachTarget(new ResourceLocation(value), objectFunction, stackFunction);
                } catch (IllegalArgumentException ex) {
                    GroovyLog.get().error("Invalid resource location '{}': {}", value, ex.getMessage());
                    return 0;
                }
            }
        }
        Boolean result = objectFunction.apply(target);
        return result != null && result ? 1 : 0;
    }

    private void refreshIfNeeded(int count, String action, Object target) {
        if (count > 0) {
            refresh();
        } else {
            GroovyLog.get().warn("HBM hazard {}: no targets matched for {}", action, target);
        }
    }

    private boolean registerObject(Object o, HazardData data) {
        backupExistingMapping(o);
        HazardSystem.register(o, data);
        addScripted(new MapEntry(HazardTarget.of(o), data));
        return true;
    }

    private boolean registerStack(ItemStack stack, HazardData data) {
        backupExistingMapping(stack);
        HazardSystem.register(stack, data);
        addScripted(new MapEntry(HazardTarget.ofStack(stack), data));
        return true;
    }

    private boolean unregisterObject(Object o) {
        boolean had = backupExistingMapping(o);
        boolean removed = HazardSystem.unregister(o);
        return had || removed;
    }

    private boolean unregisterStack(ItemStack stack) {
        boolean had = backupExistingMapping(stack);
        boolean removed = HazardSystem.unregister(stack);
        return had || removed;
    }

    private boolean unblacklistObject(Object o) {
        boolean wasBlacklisted = isCurrentlyBlacklisted(o);
        if (wasBlacklisted) addBackup(new BlacklistEntry(HazardTarget.of(o)));
        return HazardSystem.unblacklist(o);
    }

    private boolean unblacklistStack(ItemStack stack) {
        boolean wasBlacklisted = isCurrentlyBlacklisted(stack);
        if (wasBlacklisted) addBackup(new BlacklistEntry(HazardTarget.ofStack(stack.copy())));
        return HazardSystem.unblacklist(stack);
    }

    private boolean isCurrentlyBlacklisted(Object o) {
        if (o instanceof ItemStack s) {
            return HazardSystem.stackBlacklist.contains(ItemStackUtil.comparableStackFrom(s).makeSingular());
        } else if (o instanceof String str) {
            return HazardSystem.dictBlacklist.contains(str);
        } else if (o instanceof RecipesCommon.ComparableStack cs) {
            return HazardSystem.stackBlacklist.contains(cs.makeSingular());
        }
        return false;
    }

    private boolean backupExistingMapping(Object target) {
        List<MapEntry> backups = getExistingMappings(target);
        if (backups.isEmpty()) return false;
        Set<MapEntry> uniq = new ObjectOpenHashSet<>(backups);
        for (MapEntry entry : uniq) addBackup(entry);
        return true;
    }

    private List<MapEntry> getExistingMappings(Object target) {
        List<MapEntry> list = new ArrayList<>();
        if (target == null) return list;
        if (target instanceof String key) {
            HazardData d = HazardSystem.oreMap.get(key);
            if (d != null) list.add(new MapEntry(new HazardTarget.OreDict(key), d));
        } else if (target instanceof Item item) {
            HazardData d = HazardSystem.itemMap.get(item);
            if (d != null) list.add(new MapEntry(new HazardTarget.ItemKey(item), d));
        } else if (target instanceof ResourceLocation loc) {
            if (ForgeRegistries.ITEMS.containsKey(loc)) {
                Item item = ForgeRegistries.ITEMS.getValue(loc);
                if (item != null) {
                    HazardData d = HazardSystem.itemMap.get(item);
                    if (d != null) list.add(new MapEntry(new HazardTarget.ResourceKey(loc), d));
                }
            }
            for (Tuple<ResourceLocation, HazardData> t : HazardSystem.locationRateRegisterList) {
                if (loc.equals(t.getFirst())) list.add(new MapEntry(new HazardTarget.ResourceKey(loc), t.getSecond()));
            }
        } else if (target instanceof Block block) {
            Item item = Item.getItemFromBlock(block);
            HazardData d = HazardSystem.itemMap.get(item);
            if (d != null) list.add(new MapEntry(new HazardTarget.BlockKey(block), d));
        } else if (target instanceof ItemStack stack) {
            RecipesCommon.ComparableStack cs = ItemStackUtil.comparableStackFrom(stack);
            HazardData d = HazardSystem.stackMap.get(cs);
            if (d != null) list.add(new MapEntry(new HazardTarget.ComparableStackTarget(cs.makeSingular()), d));
        } else if (target instanceof RecipesCommon.ComparableStack cs) {
            HazardData d = HazardSystem.stackMap.get(cs);
            if (d != null) list.add(new MapEntry(new HazardTarget.ComparableStackTarget(cs.makeSingular()), d));
        }
        return list;
    }

    protected interface HazardRecipe {
    }

    public interface HazardTarget {
        static HazardTarget of(Object target) {
            if (target instanceof String s) return new OreDict(s);
            if (target instanceof Item i) return new ItemKey(i);
            if (target instanceof ResourceLocation rl) return new ResourceKey(rl);
            if (target instanceof Block b) return new BlockKey(b);
            if (target instanceof RecipesCommon.ComparableStack cs) return new ComparableStackTarget(cs);
            if (target instanceof ItemStack st) return new ItemStackTarget(st);
            return new ResourceKey(new ResourceLocation(String.valueOf(target)));
        }

        static HazardTarget ofStack(ItemStack st) {
            return new ItemStackTarget(st);
        }

        Object unwrap();

        record OreDict(String name) implements HazardTarget {
            @Override
            public Object unwrap() {
                return name;
            }

            @Override
            public boolean equals(Object o) {
                return o instanceof OreDict d && Objects.equals(name, d.name);
            }

            @Override
            public String toString() {
                return "OreDict(" + name + ")";
            }
        }

        record ItemKey(Item item) implements HazardTarget {

            @Override
            public Object unwrap() {
                return item;
            }

            @Override
            public boolean equals(Object o) {
                return o instanceof ItemKey k && item == k.item;
            }

            @Override
            public int hashCode() {
                return System.identityHashCode(item);
            }

            @Override
            public String toString() {
                return "Item(" + item + ")";
            }
        }

        record ResourceKey(ResourceLocation location) implements HazardTarget {

            @Override
            public Object unwrap() {
                return location;
            }

            @Override
            public boolean equals(Object o) {
                return o instanceof ResourceKey k && Objects.equals(location, k.location);
            }

            @Override
            public String toString() {
                return "Resource(" + location + ")";
            }
        }

        record BlockKey(Block block) implements HazardTarget {

            @Override
            public Object unwrap() {
                return block;
            }

            @Override
            public boolean equals(Object o) {
                return o instanceof BlockKey k && block == k.block;
            }

            @Override
            public int hashCode() {
                return System.identityHashCode(block);
            }

            @Override
            public String toString() {
                return "Block(" + block + ")";
            }
        }

        /**
         * Canonicalizes to a singular ComparableStack for stable equality.
         *
         * @param stack expected already singular
         */
        record ComparableStackTarget(RecipesCommon.ComparableStack stack) implements HazardTarget {
            public ComparableStackTarget(RecipesCommon.ComparableStack stack) {
                this.stack = stack == null ? null : stack.makeSingular();
            }

            @Override
            public Object unwrap() {
                return stack;
            }

            @Override
            public boolean equals(Object o) {
                return o instanceof ComparableStackTarget k && Objects.equals(stack, k.stack);
            }

            @Override
            public String toString() {
                return "CStack(" + stack + ")";
            }
        }

        /**
         * Holds a 1-count ItemStack for parity with blacklist/register calls that take stacks.
         *
         * @param stack stored as count=1 copy
         */
        record ItemStackTarget(ItemStack stack) implements HazardTarget {
            public ItemStackTarget(ItemStack stack) {
                ItemStack s = stack == null ? ItemStack.EMPTY : stack.copy();
                if (!s.isEmpty()) s.setCount(1);
                this.stack = s;
            }

            @Override
            public Object unwrap() {
                return stack;
            }

            @Override
            public int hashCode() {
                if (stack == null || stack.isEmpty()) return 0;
                int h = System.identityHashCode(stack.getItem());
                h = 31 * h + stack.getItemDamage();
                final NBTTagCompound tag = stack.getTagCompound();
                return 31 * h + (tag == null ? 0 : tag.hashCode());
            }

            @Override
            public boolean equals(Object o) {
                return o instanceof ItemStackTarget k && ItemStack.areItemStacksEqual(stack, k.stack);
            }

            @Override
            public String toString() {
                return "Stack(" + stack + ")";
            }
        }
    }

    @SuppressWarnings("MethodMayBeStatic")
    public static final class HazardTypeFacade {
        private final Map<String, IHazardType> lookup;

        HazardTypeFacade() {
            lookup = new LinkedHashMap<>();
            register("radiation", HazardRegistry.RADIATION, "rad", "rads");
            register("contaminating", HazardRegistry.CONTAMINATING, "contam");
            register("digamma", HazardRegistry.DIGAMMA, "dg");
            register("hot", HazardRegistry.HOT);
            register("blinding", HazardRegistry.BLINDING, "blind");
            register("asbestos", HazardRegistry.ASBESTOS);
            register("coal", HazardRegistry.COAL);
            register("hydroactive", HazardRegistry.HYDROACTIVE, "hydro");
            register("explosive", HazardRegistry.EXPLOSIVE, "boom");
            register("toxic", HazardRegistry.TOXIC);
            register("cold", HazardRegistry.COLD);
            // Note: Unstable and DangerousDrop are parameterized types; use dedicated creators below.
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Lookup a built-in hazard type by name or alias (e.g., 'radiation', 'rad', 'hot').")
        public IHazardType get(String name) {
            if (name == null) return null;
            IHazardType type = lookup.get(name.toLowerCase(Locale.ENGLISH));
            if (type == null) {
                GroovyLog.get().warn("Unknown HBM hazard type '{}'.", name);
            }
            return type;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Ionizing radiation. Level typically in µSv/h-scaled units used by the mod.")
        public IHazardType radiation() {
            return HazardRegistry.RADIATION;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Applies contamination on use/hold; may persist on entities/blocks depending on rules.")
        public IHazardType contaminating() {
            return HazardRegistry.CONTAMINATING;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "High-energy DIGAMMA effect. Rare, specialized radiation channel.")
        public IHazardType digamma() {
            return HazardRegistry.DIGAMMA;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Heat hazard (burning/hot). Often used for fresh-forged metals or engine parts.")
        public IHazardType hot() {
            return HazardRegistry.HOT;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Temporary blindness/flash effects.")
        public IHazardType blinding() {
            return HazardRegistry.BLINDING;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Asbestos fiber hazard—causes contamination on exposure.")
        public IHazardType asbestos() {
            return HazardRegistry.ASBESTOS;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Coal dust hazard.")
        public IHazardType coal() {
            return HazardRegistry.COAL;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Water-reactive hazard (e.g., alkali metals).")
        public IHazardType hydroactive() {
            return HazardRegistry.HYDROACTIVE;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Explosive hazard channel for volatile items.")
        public IHazardType explosive() {
            return HazardRegistry.EXPLOSIVE;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Poison/toxin exposure.")
        public IHazardType toxic() {
            return HazardRegistry.TOXIC;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Cryogenic/cold exposure.")
        public IHazardType cold() {
            return HazardRegistry.COLD;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Create an Unstable hazard type with a decay timer in ticks.")
        public IHazardType unstable(int timer) {
            return new HazardTypeUnstable(timer);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Create an Unstable hazard with custom update/drop handlers.")
        public IHazardType unstable(ObjObjDoubleConsumer<EntityLivingBase, ItemStack> onUpdate, ObjDoubleConsumer<EntityItem> onDrop) {
            return new HazardTypeUnstable(onUpdate, onDrop);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Create a hazard that only acts when the item is dropped.")
        public IHazardType dangerousDrop(ObjDoubleConsumer<EntityItem> onDrop) {
            return new HazardTypeDangerousDrop(onDrop);
        }

        private void register(String name, IHazardType type, String... aliases) {
            lookup.put(name, type);
            for (String alias : aliases) lookup.put(alias, type);
        }
    }

    @SuppressWarnings("MethodMayBeStatic")
    public static final class HazardModifierFacade {
        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Radiation curve for fission fuel (rods/fuel items). Parameter is the target equilibrium level.")
        public HazardModifierFuelRadiation fuelRadiation(double target) {
            return new HazardModifierFuelRadiation(target);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Radiation curve for RTGs. Parameter is the target output level.")
        public HazardModifierRTGRadiation rtgRadiation(double target) {
            return new HazardModifierRTGRadiation(target);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "RBMK-specific radiation profile. If linear=false, uses non-linear curve.")
        public HazardModifierRBMKRadiation rbmkRadiation(double target) {
            return new HazardModifierRBMKRadiation(target, false);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "RBMK-specific radiation profile with linear toggle.")
        public HazardModifierRBMKRadiation rbmkRadiation(double target, boolean linear) {
            return new HazardModifierRBMKRadiation(target, linear);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Marks items as RBMK hot (thermal).")
        public HazardModifierRBMKHot rbmkHot() {
            return new HazardModifierRBMKHot();
        }
    }

    public static final class HazardDataBuilder {
        private final HazardData data = new HazardData();
        private boolean built;

        private void ensureMutable() {
            if (built) throw new IllegalStateException("HazardDataBuilder already built");
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "If true, this HazardData overrides lower-priority mappings for the same target.")
        public HazardDataBuilder override(boolean override) {
            ensureMutable();
            data.setOverride(override);
            return this;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Set mutual exclusion group. Non-zero means entries in same mutex group won’t co-exist.")
        public HazardDataBuilder mutex(int mutex) {
            ensureMutable();
            data.setMutex(mutex);
            return this;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add a hazard entry (type + numeric level). Optional modifiers refine behavior.")
        public HazardDataBuilder entry(IHazardType type, double level, IHazardModifier... modifiers) {
            ensureMutable();
            if (type == null) {
                GroovyLog.get().warn("Cannot add hazard entry for null type.");
                return this;
            }
            HazardEntry entry = new HazardEntry(type, level);
            if (modifiers != null) {
                for (IHazardModifier modifier : modifiers) {
                    if (modifier != null) entry.addMod(modifier);
                }
            }
            data.addEntry(entry);
            return this;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add radiation entry.")
        public HazardDataBuilder radiation(double level, IHazardModifier... modifiers) {
            return entry(HazardRegistry.RADIATION, level, modifiers);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add contaminating entry.")
        public HazardDataBuilder contaminating(double level) {
            return entry(HazardRegistry.CONTAMINATING, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add digamma entry.")
        public HazardDataBuilder digamma(double level) {
            return entry(HazardRegistry.DIGAMMA, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add hot entry.")
        public HazardDataBuilder hot(double level) {
            return entry(HazardRegistry.HOT, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add blinding entry.")
        public HazardDataBuilder blinding(double level) {
            return entry(HazardRegistry.BLINDING, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add asbestos entry.")
        public HazardDataBuilder asbestos(double level) {
            return entry(HazardRegistry.ASBESTOS, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add coal entry.")
        public HazardDataBuilder coal(double level) {
            return entry(HazardRegistry.COAL, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add hydroactive entry.")
        public HazardDataBuilder hydroactive(double level) {
            return entry(HazardRegistry.HYDROACTIVE, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add explosive entry.")
        public HazardDataBuilder explosive(double level) {
            return entry(HazardRegistry.EXPLOSIVE, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add toxic entry.")
        public HazardDataBuilder toxic(double level) {
            return entry(HazardRegistry.TOXIC, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add cold entry.")
        public HazardDataBuilder cold(double level) {
            return entry(HazardRegistry.COLD, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add an Unstable hazard with a decay timer in ticks.")
        public HazardDataBuilder unstable(double level, int timer) {
            return entry(new HazardTypeUnstable(timer), level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add an Unstable hazard with a decay timer and custom tooltip info provider.")
        public HazardDataBuilder unstable(double level, int timer, IHazardType.HazardInfoConsumer info) {
            return entry(new HazardTypeUnstable(timer, info), level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add a custom Unstable hazard without tooltip.")
        public HazardDataBuilder unstable(double level, ObjObjDoubleConsumer<EntityLivingBase, ItemStack> onUpdate, ObjDoubleConsumer<EntityItem> onDrop) {
            return entry(new HazardTypeUnstable(onUpdate, onDrop), level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add a custom Unstable hazard.")
        public HazardDataBuilder unstable(double level, ObjObjDoubleConsumer<EntityLivingBase, ItemStack> onUpdate, ObjDoubleConsumer<EntityItem> onDrop, IHazardType.HazardInfoConsumer customInfo) {
            return entry(new HazardTypeUnstable(onUpdate, onDrop, customInfo), level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add a hazard that acts when the item is dropped.")
        public HazardDataBuilder dangerousDrop(double level, ObjDoubleConsumer<EntityItem> onDrop) {
            return entry(new HazardTypeDangerousDrop(onDrop), level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Finalize and return the immutable HazardData.")
        public HazardData build() {
            ensureMutable();
            built = true;
            return data;
        }
    }

    private record MapEntry(HazardTarget target, HazardData data) implements HazardRecipe {
    }

    private record BlacklistEntry(HazardTarget target) implements HazardRecipe {
    }

    private record ItemPostTransformer(Item item,
                                       BiFunction<ItemStack, List<HazardEntry>, List<HazardEntry>> fn) implements HazardRecipe {
    }

    private record StackPostTransformer(StackKey key,
                                        BiFunction<ItemStack, List<HazardEntry>, List<HazardEntry>> fn) implements HazardRecipe {
    }

    private record ItemMultiplier(Item item, double multiplier) implements HazardRecipe {
    }
}
