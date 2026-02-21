package com.hbm.hazard;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hbm.capability.HbmLivingProps;
import com.hbm.config.GeneralConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.hazard.modifier.IHazardModifier;
import com.hbm.hazard.transformer.IHazardTransformer;
import com.hbm.hazard.type.IHazardType;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.main.MainRegistry;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ItemStackUtil;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.hbm.util.ContaminationUtil.NTM_NEUTRON_NBT_KEY;

/**
 * This logic was heavily refactored to be threaded and event-driven. Do not aim for upstream parity.
 *
 * @author drillgon200, Alcater, mlbv
 */
public class HazardSystem {

    /**
     * Map for OreDict entries, always evaluated first. Avoid registering HazardData with 'doesOverride', as internal order is based on the item's
     * ore dict keys.
     */
    public static final HashMap<String, HazardData> oreMap = new HashMap<>();
    /**
     * Map for items, either with wildcard meta or stuff that's expected to have a variety of damage values, like tools.
     */
    public static final HashMap<Item, HazardData> itemMap = new HashMap<>();
    /**
     * Very specific stacks with item and meta matching. ComparableStack does not support NBT matching, to scale hazards with NBT please use
     * HazardModifiers.
     */
    public static final HashMap<ComparableStack, HazardData> stackMap = new HashMap<>();
    /**
     * For items that should, for whichever reason, be completely exempt from the hazard system.
     */
    public static final HashSet<ComparableStack> stackBlacklist = new HashSet<>();
    public static final HashSet<String> dictBlacklist = new HashSet<>();


    /**
     * For items from outside of that mod that require registration right at end of fml loading
     */
    public static final List<Tuple<ResourceLocation,HazardData>> locationRateRegisterList = new CopyOnWriteArrayList<>();
    /**
     * List of hazard transformers, called in order before and after unrolling all the HazardEntries.
     */
    public static final List<IHazardTransformer> trafos = new ArrayList<>();
    private static final int VOLATILITY_THRESHOLD = 16;
    private static final int VOLATILITY_WINDOW_SECONDS = 30;
    private static final int FINAL_HAZARD_CACHE_SIZE = 2048;
    private static final ConcurrentHashMap<ComparableStack, List<HazardData>> hazardDataChronologyCache = new ConcurrentHashMap<>();
    private static final Cache<NbtSensitiveCacheKey, List<HazardEntry>> finalHazardEntryCache =
            CacheBuilder.newBuilder().maximumSize(FINAL_HAZARD_CACHE_SIZE).build();
    private static final Cache<ComparableStack, AtomicInteger> volatilityTracker =
            CacheBuilder.newBuilder().expireAfterWrite(VOLATILITY_WINDOW_SECONDS, TimeUnit.SECONDS).build();
    private static final Set<ComparableStack> volatileItemsBlacklist = ConcurrentHashMap.newKeySet();
    private static final ConcurrentHashMap<UUID, PlayerHazardData> playerHazardDataMap = new ConcurrentHashMap<>();
    private static final Queue<InventoryDelta> inventoryDeltas = new ConcurrentLinkedQueue<>();
    private static final Set<UUID> playersToUpdate = ConcurrentHashMap.newKeySet();
    private static final double minRadRate = 0.000005D;
    private static volatile CompletableFuture<Void> scanFuture = CompletableFuture.completedFuture(null);
    private static long tickCounter = 0;

    /**
     * Schedules a full rescan for a player.
     *
     * @param player The player whose inventory has changed.
     */
    public static void schedulePlayerUpdate(EntityPlayer player) {
        playersToUpdate.add(player.getUniqueID());
    }

    /**
     * Records a delta for a single slot in the player's container.
     *
     * @apiNote hazard lookup count-insensitive; effects may be count-sensitive via modifiers; neutron handling delegated to ContaminationUtil
     */
    public static void onInventoryDelta(EntityPlayer player, int serverSlotIndex, ItemStack oldStack, ItemStack newStack) {
        inventoryDeltas.add(new InventoryDelta(player.getUniqueID(), serverSlotIndex, oldStack.copy(), newStack.copy()));
    }

    /**
     * Main entry point, called from ServerTickEvent.
     */
    public static CompletableFuture<Void> onServerTickAsync(Executor backgroundExecutor) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) return CompletableFuture.completedFuture(null);
        tickCounter++;
        if (tickCounter % RadiationConfig.hazardRate == 0) {
            for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                if (player.isDead) continue;
                PlayerHazardData phd = playerHazardDataMap.computeIfAbsent(player.getUniqueID(), uuid -> new PlayerHazardData(player));
                if (phd.player != player) {
                    if (GeneralConfig.enableExtendedLogging)
                        MainRegistry.logger.debug("Player {} entity instance changed, re-initializing.", player.getName());
                    phd.updatePlayerReference(player);
                }
                phd.applyActiveHazards();
            }
        }
        CompletableFuture<Void> cur = scanFuture;
        if (!cur.isDone()) return cur;
        if (playersToUpdate.isEmpty() && inventoryDeltas.isEmpty()) return CompletableFuture.completedFuture(null);
        final List<EntityPlayer> playersForFullScan = new ArrayList<>();
        if (!playersToUpdate.isEmpty()) {
            for (UUID uuid : playersToUpdate) {
                EntityPlayer p = server.getPlayerList().getPlayerByUUID(uuid);
                if (p != null && !p.isDead) playersForFullScan.add(p);
            }
            playersToUpdate.clear();
        }
        final List<InventoryDelta> deltasForProcessing = new ArrayList<>();
        InventoryDelta delta;
        while ((delta = inventoryDeltas.poll()) != null) {
            deltasForProcessing.add(delta);
        }
        if (playersForFullScan.isEmpty() && deltasForProcessing.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        scanFuture = processHazardsAsync(playersForFullScan, deltasForProcessing, backgroundExecutor).thenAccept(HazardSystem::applyUpdateResult);
        return scanFuture;
    }

    private static void applyUpdateResult(HazardUpdateResult results) {
        results.fullScanResults.forEach((uuid, result) -> {
            PlayerHazardData phd = playerHazardDataMap.get(uuid);
            if (phd != null) phd.setScanResult(result);
        });
        results.deltaResults.forEach((uuid, result) -> {
            PlayerHazardData phd = playerHazardDataMap.get(uuid);
            if (phd != null) phd.applyDeltaResult(result);
        });
    }

    private static CompletableFuture<HazardUpdateResult> processHazardsAsync(List<EntityPlayer> playersForFullScan, List<InventoryDelta> deltas,
                                                                             Executor executor) {
        final HashMap<UUID, CompletableFuture<PlayerHazardData.HazardScanResult>> fullScanFutures = new HashMap<>();
        for (EntityPlayer p : playersForFullScan) {
            if (p == null || p.isDead) continue;
            UUID uuid = p.getUniqueID();
            fullScanFutures.put(uuid, CompletableFuture.supplyAsync(() -> PlayerHazardData.calculateHazardScanForPlayer(p), executor));
        }
        final CompletableFuture<Void> fullBarrier = CompletableFuture.allOf(fullScanFutures.values().toArray(new CompletableFuture[0]));
        return fullBarrier.thenCompose(_ -> {
            final HashMap<UUID, PlayerHazardData.HazardScanResult> fullScanResults = new HashMap<>(fullScanFutures.size() * 2);
            for (Map.Entry<UUID, CompletableFuture<PlayerHazardData.HazardScanResult>> e : fullScanFutures.entrySet()) {
                fullScanResults.put(e.getKey(), e.getValue().join());
            }
            final HashMap<UUID, ArrayList<InventoryDelta>> deltasByPlayer = new HashMap<>();
            for (InventoryDelta d : deltas) {
                if (fullScanResults.containsKey(d.playerUUID())) continue;
                deltasByPlayer.computeIfAbsent(d.playerUUID(), _ -> new ArrayList<>()).add(d);
            }
            final HashMap<UUID, CompletableFuture<PlayerDeltaResult>> deltaFutures = new HashMap<>();
            for (Map.Entry<UUID, ArrayList<InventoryDelta>> e : deltasByPlayer.entrySet()) {
                UUID uuid = e.getKey();
                ArrayList<InventoryDelta> list = e.getValue();
                deltaFutures.put(uuid, CompletableFuture.supplyAsync(() -> computeDeltaForPlayer(list), executor));
            }
            final CompletableFuture<Void> deltaBarrier = CompletableFuture.allOf(deltaFutures.values().toArray(new CompletableFuture[0]));
            return deltaBarrier.thenApply(_ -> {
                final HashMap<UUID, PlayerDeltaResult> deltaResults = new HashMap<>(deltaFutures.size() * 2);
                for (Map.Entry<UUID, CompletableFuture<PlayerDeltaResult>> e : deltaFutures.entrySet()) {
                    deltaResults.put(e.getKey(), e.getValue().join());
                }
                return new HazardUpdateResult(Collections.unmodifiableMap(fullScanResults), Collections.unmodifiableMap(deltaResults));
            });
        });
    }

    private static PlayerDeltaResult computeDeltaForPlayer(List<InventoryDelta> deltas) {
        float totalNeutronDelta = 0f;
        Map<Integer, Optional<Consumer<EntityPlayer>>> finalApplicators = new HashMap<>(Math.max(16, deltas.size() * 2));

        for (InventoryDelta delta : deltas) {
            DeltaUpdate update = calculateDeltaUpdate(delta);
            totalNeutronDelta += update.neutronRadsDelta();
            finalApplicators.put(delta.serverSlotIndex(), update.applicator());
        }
        return new PlayerDeltaResult(Collections.unmodifiableMap(finalApplicators), totalNeutronDelta);
    }

    /**
     * Calculates the change for a single slot. Runs on a background thread.
     *
     * @apiNote hazard presence comparison count-insensitive; applicator effects may be count-sensitive; neutron delta delegated to ContaminationUtil
     */
    private static DeltaUpdate calculateDeltaUpdate(InventoryDelta delta) {
        ItemStack oldStack = delta.oldStack();
        ItemStack newStack = delta.newStack();
        boolean isOldStackHazardous = isStackHazardous(oldStack);
        boolean isNewStackHazardous = isStackHazardous(newStack);

        float neutronDelta = 0;
        if (RadiationConfig.neutronActivation) {
            if (!isOldStackHazardous) {
                neutronDelta -= ContaminationUtil.getNeutronRads(oldStack);
            }
            if (!isNewStackHazardous) {
                neutronDelta += ContaminationUtil.getNeutronRads(newStack);
            }
        }

        if (!isNewStackHazardous) {
            return new DeltaUpdate(Optional.empty(), neutronDelta);
        }

        final int slotIndex = delta.serverSlotIndex();
        Consumer<EntityPlayer> applicator = p -> {
            if (p.inventoryContainer == null || slotIndex >= p.inventoryContainer.inventorySlots.size()) return;
            ItemStack liveStack = p.inventoryContainer.getSlot(slotIndex).getStack();
            applyHazards(liveStack, p);
        };
        return new DeltaUpdate(Optional.of(applicator), neutronDelta);
    }

    public static void onPlayerLogout(EntityPlayer player) {
        UUID uuid = player.getUniqueID();
        playersToUpdate.remove(uuid);
        playerHazardDataMap.remove(uuid);
        inventoryDeltas.removeIf(delta -> delta.playerUUID().equals(uuid));
    }

    /**
     * Call when doing hot reload.
     */
    public static void clearCaches() {
        MainRegistry.logger.info("Clearing HBM hazard calculation caches.");
        hazardDataChronologyCache.clear();
        finalHazardEntryCache.invalidateAll();
        volatilityTracker.invalidateAll();
        volatileItemsBlacklist.clear();
    }

    /**
     * @return {@code true} if there exists any applicable {@link HazardEntry} for the stack.
     *
     * @apiNote count insensitive
     */
    public static boolean isStackHazardous(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return !getHazardsFromStack(stack).isEmpty();
    }

    /**
     * Registers {@link HazardData} for a specific OreDictionary key.
     * <p>
     * <b>Priority:</b> OreDictionary mappings are evaluated before item and stack mappings. If multiple ore keys
     * match a stack, their entries are consulted in the same order as returned by
     * {@link net.minecraftforge.oredict.OreDictionary#getOreIDs(net.minecraft.item.ItemStack)}.
     * </p>
     * <p>
     * Avoid relying on {@code doesOverride} across different ore keys to fix ordering; prefer using mutex flags or
     * more specific registrations when keys collide.
     * </p>
     *
     * @param oreName non-null OreDictionary name (e.g. {@code "ingotUranium"}).
     * @param data    mapping to associate; its {@code entries}, {@code doesOverride}, and {@code mutex} bits control
     *                how it interacts with previously gathered data.
     * @apiNote lookup is <em>count-insensitive</em>; stack count is only considered later by modifiers/types at
     *          application time.
     */
    public static void register(final String oreName, final HazardData data) {
        oreMap.put(oreName, data);
    }

    /**
     * Registers {@link HazardData} for all stacks of a given {@link Item} (any damage value).
     * <p>
     * This mapping is evaluated after OreDictionary mappings and before exact-stack mappings.
     * </p>
     *
     * @param item target item (non-null).
     * @param data hazard data to associate.
     * @apiNote lookup is <em>count-insensitive</em>.
     */
    public static void register(final Item item, final HazardData data) {
        itemMap.put(item, data);
    }

    /**
     * Registers {@link HazardData} for an item addressed by a {@link ResourceLocation}.
     * <p>
     * If the item is already present in {@link IForgeRegistry} at call time, the
     * mapping is applied immediately. Otherwise it is queued in {@link #locationRateRegisterList} and applied near the
     * end of FML loading once the item appears.
     * </p>
     *
     * @param loc  item registry name (e.g. {@code modid:item_name}).
     * @param data hazard data to associate.
     * @apiNote lookup is <em>count-insensitive</em>.
     */
    public static void register(final ResourceLocation loc, final HazardData data) {
        retriveAndRegister(loc, data);
    }

    /**
     * Registers {@link HazardData} for the {@link Item} form of a {@link Block}.
     * <p>
     * Equivalent to calling {@link #register(Item, HazardData)} with {@link Item#getItemFromBlock(Block)}.
     * </p>
     *
     * @param block target block whose item form will be mapped.
     * @param data  hazard data to associate.
     * @apiNote lookup is <em>count-insensitive</em>.
     */
    public static void register(final Block block, final HazardData data) {
        itemMap.put(Item.getItemFromBlock(block), data);
    }

    /**
     * Registers {@link HazardData} for an exact item/meta pair.
     * <p>
     * The key is normalized via {@link ComparableStack#makeSingular()} so the
     * registration is <em>count-insensitive</em>. <b>NBТ is not considered</b> by the key; to vary hazard level by NBT
     * use {@link IHazardModifier} (or a transformer) rather than separate registrations.
     * </p>
     *
     * @param stack representative stack; only its item and meta are used to form the key.
     * @param data  hazard data to associate.
     */
    public static void register(final ItemStack stack, final HazardData data) {
        stackMap.put(ItemStackUtil.comparableStackFrom(stack), data);
    }

    /**
     * Registers {@link HazardData} for an exact {@link ComparableStack} key.
     * <p>
     * Callers are responsible for providing a <em>singular</em> key if count-insensitivity is desired, e.g.
     * {@code comp.makeSingular()}.
     * </p>
     *
     * @param comp normalized key (typically {@code makeSingular()}).
     * @param data hazard data to associate.
     */
    public static void register(final ComparableStack comp, final HazardData data) {
        stackMap.put(comp, data);
    }

    /**
     * Register hazard data for an object key (ore name, item, block, ItemStack, ComparableStack, or ResourceLocation).
     *
     * @apiNote count insensitive (ItemStack keys normalized via ComparableStack.makeSingular)
     */
    public static void register(final Object o, final HazardData data) {
        if (o instanceof String s) {
            register(s, data);
            return;
        }
        if (o instanceof Item i) {
            register(i, data);
            return;
        }
        if (o instanceof ResourceLocation rl) {
            register(rl, data);
            return;
        }
        if (o instanceof Block b) {
            register(b, data);
            return;
        }
        if (o instanceof ItemStack is) {
            register(is, data);
            return;
        }
        if (o instanceof ComparableStack cs) {
            register(cs, data);
            return;
        }
        throw new IllegalArgumentException("Unsupported key type for register: " + (o == null ? "null" : o.getClass().getName()));
    }

    /**
     * Removes the OreDictionary mapping for {@code oreName}, if present.
     *
     * @param oreName target key.
     * @return {@code true} if a mapping was removed.
     */
    public static boolean unregister(final String oreName) {
        return oreMap.remove(oreName) != null;
    }

    /**
     * Removes the item-level mapping for {@code item}, if present.
     *
     * @param item target item.
     * @return {@code true} if a mapping was removed.
     */
    public static boolean unregister(final Item item) {
        return itemMap.remove(item) != null;
    }

    /**
     * Removes mappings associated with {@code loc}.
     * <p>
     * This removes an already-resolved item mapping and/or a pending entry in
     * {@link #locationRateRegisterList} if it was queued earlier.
     * </p>
     *
     * @param loc item registry name.
     * @return {@code true} if anything was removed.
     */
    public static boolean unregister(final ResourceLocation loc) {
        return removeResourceLocation(loc);
    }

    /**
     * Removes the mapping for the item form of {@code block}, if present.
     *
     * @param block target block.
     * @return {@code true} if a mapping was removed.
     */
    public static boolean unregister(final Block block) {
        Item item = Item.getItemFromBlock(block);
        return item != Items.AIR && itemMap.remove(item) != null;
    }

    /**
     * Removes the exact-stack mapping corresponding to {@code stack}'s item/meta.
     * <p>
     * The lookup key is normalized and does not include NBT.
     * </p>
     *
     * @param stack representative stack; only item/meta are considered.
     * @return {@code true} if a mapping was removed.
     */
    public static boolean unregister(final ItemStack stack) {
        return stackMap.remove(ItemStackUtil.comparableStackFrom(stack)) != null;
    }

    /**
     * Removes the mapping for an exact {@link ComparableStack} key.
     *
     * @param comp key to remove.
     * @return {@code true} if a mapping was removed.
     */
    public static boolean unregister(final ComparableStack comp) {
        return stackMap.remove(comp) != null;
    }

    /**
     * Unregister hazard data for the given key or collection/array of keys.
     *
     * @apiNote count insensitive (mirrors registration semantics)
     */
    public static boolean unregister(final Object o) {
        if (o instanceof Collection<?> c) {
            boolean removed = false;
            for (Object element : c) {
                removed |= unregister(element);
            }
            return removed;
        }
        if (o == null) return false;

        if (o instanceof String s) return unregister(s);
        if (o instanceof Item i) return unregister(i);
        if (o instanceof ResourceLocation rl) return unregister(rl);
        if (o instanceof Block b) return unregister(b);
        if (o instanceof ItemStack is) return unregister(is);
        if (o instanceof ComparableStack cs) return unregister(cs);
        if (o.getClass().isArray()) {
            boolean removed = false;
            int length = Array.getLength(o);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(o, i);
                removed |= unregister(element);
            }
            return removed;
        }
        throw new IllegalArgumentException("Unsupported key type for unregister: " + o.getClass().getName());
    }

    /**
     * Attempts to retrive and append an item onto the map from resource location, helpful for groovy users
     * @param loc
     */
    private static void retriveAndRegister(ResourceLocation loc, HazardData data){
        IForgeRegistry<Item> registry = ForgeRegistries.ITEMS;
        if(registry.containsKey(loc))
            itemMap.put(registry.getValue(loc),data);
        else
            locationRateRegisterList.add(new Tuple<>(loc,data));

    }

    private static boolean removeResourceLocation(ResourceLocation loc) {
        boolean removed = false;
        IForgeRegistry<Item> registry = ForgeRegistries.ITEMS;
        if (registry.containsKey(loc)) {
            Item item = registry.getValue(loc);
            if (item != null) {
                removed |= itemMap.remove(item) != null;
            }
        }
        Iterator<Tuple<ResourceLocation, HazardData>> iterator = locationRateRegisterList.iterator();
        while (iterator.hasNext()) {
            Tuple<ResourceLocation, HazardData> tuple = iterator.next();
            if (loc.equals(tuple.getFirst())) {
                iterator.remove();
                removed = true;
            }
        }
        return removed;
    }

    /**
     * Blacklists an exact item/meta so that <em>no configured hazards</em> are returned for matching stacks.
     * <p>
     * The key is normalized via {@link ComparableStack#makeSingular()} and is <b>NBT-agnostic</b>.
     * </p>
     *
     * @param stack representative stack to blacklist (item/meta are used).
     * @apiNote Blacklisting suppresses configured {@link HazardEntry} evaluation only; neutron contamination (if
     *          enabled) is handled separately by {@link com.hbm.util.ContaminationUtil} and is not affected.
     */
    public static void blacklist(final ItemStack stack) {
        stackBlacklist.add(ItemStackUtil.comparableStackFrom(stack).makeSingular());
    }

    /**
     * Blacklists an OreDictionary key so that stacks with that key yield no configured hazards.
     *
     * @param oreName OreDictionary name to blacklist.
     */
    public static void blacklist(final String oreName) {
        dictBlacklist.add(oreName);
    }

    /**
     * Blacklists an exact {@link ComparableStack} key (usually {@code makeSingular()}).
     *
     * @param comp normalized key to blacklist.
     */
    public static void blacklist(final ComparableStack comp) {
        stackBlacklist.add(comp.makeSingular());
    }

    /**
     * Prevents the stack from returning any HazardData
     *
     * @apiNote count insensitive (ItemStacks normalized via ComparableStack.makeSingular)
     */
    public static void blacklist(final Object o) {
        if (o instanceof ItemStack is) {
            blacklist(is);
            return;
        }
        if (o instanceof String s) {
            blacklist(s);
            return;
        }
        if (o instanceof ComparableStack cs) {
            blacklist(cs);
            return;
        }
        throw new IllegalArgumentException("Unsupported key type for blacklist: " + (o == null ? "null" : o.getClass().getName()));
    }

    /**
     * Removes a previous blacklist entry for an exact item/meta pair.
     *
     * @param stack representative stack; only item/meta are considered.
     * @return {@code true} if an entry was removed.
     */
    public static boolean unblacklist(final ItemStack stack) {
        return stackBlacklist.remove(ItemStackUtil.comparableStackFrom(stack).makeSingular());
    }

    /**
     * Removes a previous blacklist entry for an OreDictionary key.
     *
     * @param oreName key to remove.
     * @return {@code true} if an entry was removed.
     */
    public static boolean unblacklist(final String oreName) {
        return dictBlacklist.remove(oreName);
    }

    /**
     * Removes a previous blacklist entry for an exact {@link ComparableStack} key.
     *
     * @param comp key to remove.
     * @return {@code true} if an entry was removed.
     */
    public static boolean unblacklist(final ComparableStack comp) {
        return stackBlacklist.remove(comp.makeSingular());
    }

    /**
     * Removes a previous blacklist entry. Collections/arrays expanded recursively.
     *
     * @apiNote count insensitive
     */
    public static boolean unblacklist(final Object o) {
        if (o instanceof Collection<?> c) {
            boolean removed = false;
            for (Object element : c) {
                removed |= unblacklist(element);
            }
            return removed;
        }
        if (o == null) return false;

        if (o instanceof ItemStack is) return unblacklist(is);
        if (o instanceof String s) return unblacklist(s);
        if (o instanceof ComparableStack cs) return unblacklist(cs);
        if (o.getClass().isArray()) {
            boolean removed = false;
            int length = Array.getLength(o);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(o, i);
                removed |= unblacklist(element);
            }
            return removed;
        }
        throw new IllegalArgumentException("Unsupported key type for unblacklist: " + o.getClass().getName());
    }

    /**
     * Checks whether the given stack is blacklisted by exact (item,meta) or by ore dictionary.
     *
     * @apiNote count insensitive
     */
    public static boolean isItemBlacklisted(final ItemStack stack) {
        if (stackBlacklist.contains(ItemStackUtil.comparableStackFrom(stack).makeSingular())) return true;
        final int[] ids = OreDictionary.getOreIDs(stack);
        for (final int id : ids) {
            if (dictBlacklist.contains(OreDictionary.getOreName(id))) return true;
        }
        return false;
    }

    /**
     * Will return a full list of applicable HazardEntries for this stack.
     * <br><br>ORDER:
     * <ol>
     * <li>ore dict (if multiple keys, in order of the ore dict keys for this stack)
     * <li>item
     * <li>item stack
     * </ol>
     * <p>
     * "Applicable" means that entries that are overridden or excluded via mutex are not in this list.
     * Entries that are marked as "overriding" will delete all fetched entries that came before it.
     * Entries that use mutex will prevent subsequent entries from being considered, shall they collide. The mutex system already assumes that
     * two keys are the same in priority, so the flipped order doesn't matter.
     *
     * @apiNote count insensitive (matching uses ComparableStack.makeSingular; NBT sensitivity handled via sanitized hash; neutron NBT ignored)<br>
     * the returned list is transformed by HazardTransformers but hasn't been modified by modifiers yet.
     */
    public static List<HazardEntry> getHazardsFromStack(final ItemStack stack) {
        if (stack.isEmpty() || isItemBlacklisted(stack)) {
            return Collections.emptyList();
        }

        final ComparableStack compStack = ItemStackUtil.comparableStackFrom(stack).makeSingular();

        if (volatileItemsBlacklist.contains(compStack)) {
            return computeHazards(stack, compStack);
        }

        int nbtHash = 0;
        if (stack.hasTagCompound()) {
            NBTTagCompound sanitizedNbt = stack.getTagCompound().copy();
            sanitizedNbt.removeTag(NTM_NEUTRON_NBT_KEY);
            if (!sanitizedNbt.isEmpty()) {
                nbtHash = sanitizedNbt.hashCode();
            }
        }

        final NbtSensitiveCacheKey nbtKey = new NbtSensitiveCacheKey(compStack, nbtHash);

        try {
            return finalHazardEntryCache.get(nbtKey, () -> {
                AtomicInteger missCount = volatilityTracker.get(compStack, AtomicInteger::new);
                if (missCount.incrementAndGet() > VOLATILITY_THRESHOLD) {
                    volatileItemsBlacklist.add(compStack);
                    volatilityTracker.invalidate(compStack);
                }
                return computeHazards(stack, compStack);
            });
        } catch (ExecutionException e) {
            throw new RuntimeException("Error calculating hazard entries for stack: " + stack, e.getCause());
        }
    }

    /**
     * Builds the final, NBT-aware list of hazard entries for a stack.
     *
     * @apiNote count insensitive (chronology keyed by ComparableStack without count; modifiers/types may read count at application time)
     */
    private static List<HazardEntry> computeHazards(ItemStack stack, ComparableStack compStack) {
        if (stack.isEmpty() || compStack.isEmpty()) {
            MainRegistry.logger.debug("HazardSystem.computeHazards got an empty stack or compStack(ItemStack: {}, ComparableStack: {}). " +
                    "This is not supposed to happen, please check for mod incompatibilities.", stack, compStack);
            return Collections.emptyList();
        }
        // Get NBT-agnostic base data
        List<HazardData> chronological = hazardDataChronologyCache.computeIfAbsent(compStack, cs -> {
            final List<HazardData> data = new ArrayList<>();
            final int[] ids = OreDictionary.getOreIDs(new ItemStack(cs.item, 1, cs.meta));
            for (final int id : ids) {
                final String name = OreDictionary.getOreName(id);
                final HazardData hazardData = oreMap.get(name);
                if (hazardData != null) data.add(hazardData);
            }
            final HazardData itemHazardData = itemMap.get(cs.item);
            if (itemHazardData != null) data.add(itemHazardData);
            final HazardData stackHazardData = stackMap.get(cs);
            if (stackHazardData != null) data.add(stackHazardData);
            return Collections.unmodifiableList(data);
        });

        if (chronological.isEmpty() && trafos.isEmpty()) {
            return Collections.emptyList();
        }

        // Apply NBT-sensitive transformers and build the final list
        final List<HazardEntry> entries = new ArrayList<>();
        for (final IHazardTransformer trafo : trafos) {
            trafo.transformPre(stack, entries);
        }

        int mutex = 0;
        for (final HazardData data : chronological) {
            if (data.doesOverride) entries.clear();
            if ((data.getMutex() & mutex) == 0) {
                entries.addAll(data.entries);
                mutex |= data.getMutex();
            }
        }

        for (final IHazardTransformer trafo : trafos) {
            trafo.transformPost(stack, entries);
        }

        return Collections.unmodifiableList(entries);
    }

    /**
     * Computes the effective level for a specific hazard type from the stack.
     *
     * @apiNote lookup count insensitive; result may be count-sensitive via modifiers
     */
    public static double getHazardLevelFromStack(ItemStack stack, IHazardType hazard) {
        return getHazardsFromStack(stack).stream().filter(entry -> entry.type == hazard).findFirst().map(entry -> IHazardModifier.evalAllModifiers(stack, null, entry.baseLevel, entry.mods)).orElse(0D);
    }

    public static double getRawRadsFromBlock(Block b) {
        return getHazardLevelFromStack(new ItemStack(Item.getItemFromBlock(b)), HazardRegistry.RADIATION);
    }

    /**
     * Radiation from configured entries (pre-contamination).
     *
     * @apiNote lookup count insensitive; value may be count-sensitive via modifiers
     */
    public static double getRawRadsFromStack(ItemStack stack) {
        return getHazardLevelFromStack(stack, HazardRegistry.RADIATION);
    }

    /**
     * Total radiation = configured radiation + neutron contamination.
     *
     * @apiNote configured part may be count-sensitive via modifiers; neutron part delegated to ContaminationUtil
     */
    public static double getTotalRadsFromStack(ItemStack stack) {
        return getHazardLevelFromStack(stack, HazardRegistry.RADIATION) + ContaminationUtil.getNeutronRads(stack);
    }

    public static void applyHazards(Block b, EntityLivingBase entity) {
        applyHazards(new ItemStack(Item.getItemFromBlock(b)), entity);
    }

    /**
     * Will grab and iterate through all assigned hazards of the given stack and apply their effects to the holder.
     *
     * @apiNote entry selection count insensitive; effect application may be count-sensitive via modifiers/types
     */
    public static void applyHazards(ItemStack stack, EntityLivingBase entity) {
        if (stack.isEmpty()) return;
        List<HazardEntry> hazards = getHazardsFromStack(stack);
        for (HazardEntry hazard : hazards) {
            hazard.applyHazard(stack, entity);
        }
    }

    /**
     * Updates hazards emitted by a dropped {@link EntityItem}.
     *
     * @apiNote entry selection count insensitive; evaluated level may be count-sensitive via modifiers
     */
    @SuppressWarnings("unused") // called by asm hook
    public static void updateDroppedItem(EntityItem entity) {
        if (entity.world.isRemote || entity.isDead) return;
        ItemStack stack = entity.getItem();
        if (stack.isEmpty()) return;
        for (HazardEntry entry : getHazardsFromStack(stack)) {
            entry.type.updateEntity(entity, IHazardModifier.evalAllModifiers(stack, null, entry.baseLevel, entry.mods));
        }
    }

    /**
     * Adds hazard tooltip info.
     *
     * @apiNote entry selection count insensitive; display content may be count-sensitive inside type/modifiers
     */
    public static void addHazardInfo(ItemStack stack, EntityPlayer player, List<String> list, ITooltipFlag flagIn) {
        for (HazardEntry hazard : getHazardsFromStack(stack)) {
            hazard.type.addHazardInformation(player, list, hazard.baseLevel, stack, hazard.mods);
        }
    }

    private static class PlayerHazardData {
        private final Map<Integer, Consumer<EntityPlayer>> activeApplicators = new ConcurrentHashMap<>();
        private EntityPlayer player;
        private float totalNeutronRads = 0f;

        PlayerHazardData(EntityPlayer player) {
            this.player = player;
            schedulePlayerUpdate(player);
        }

        /**
         * Performs a full scan of the player's inventory to build per-slot applicators, and aggregates neutron rads for non-hazardous stacks.
         *
         * @apiNote applicator presence count insensitive; neutron accumulation delegated to ContaminationUtil
         */
        static HazardScanResult calculateHazardScanForPlayer(EntityPlayer player) {
            Map<Integer, Consumer<EntityPlayer>> applicators = new HashMap<>();
            float totalNeutronRads = 0f;

            if (player.inventoryContainer == null) {
                return new HazardScanResult(Collections.emptyMap(), 0f);
            }

            for (int i = 0; i < player.inventoryContainer.inventorySlots.size(); i++) {
                Slot slot = player.inventoryContainer.getSlot(i);
                if (slot.inventory != player.inventory) continue;

                ItemStack stack = slot.getStack();
                if (stack.isEmpty()) continue;

                List<HazardEntry> hazards = getHazardsFromStack(stack);
                if (!hazards.isEmpty()) {
                    final int slotIndex = i;
                    applicators.put(slotIndex, p -> {
                        if (p.inventoryContainer == null || slotIndex >= p.inventoryContainer.inventorySlots.size()) return;
                        ItemStack liveStack = p.inventoryContainer.getSlot(slotIndex).getStack();
                        applyHazards(liveStack, p);
                    });
                }
                if (RadiationConfig.neutronActivation && hazards.isEmpty()) {
                    totalNeutronRads += ContaminationUtil.getNeutronRads(stack);
                }
            }
            return new HazardScanResult(Collections.unmodifiableMap(applicators), totalNeutronRads);
        }

        void updatePlayerReference(EntityPlayer player) {
            this.player = player;
            schedulePlayerUpdate(player);
        }

        void setScanResult(HazardScanResult result) {
            this.activeApplicators.clear();
            this.activeApplicators.putAll(result.applicatorMap);
            this.totalNeutronRads = Math.max(0f, result.totalNeutronRads);
        }

        void applyDeltaResult(PlayerDeltaResult result) {
            for (Map.Entry<Integer, Optional<Consumer<EntityPlayer>>> entry : result.finalApplicators.entrySet()) {
                Optional<Consumer<EntityPlayer>> applicatorOptional = entry.getValue();
                Integer slotIndex = entry.getKey();
                if (applicatorOptional.isPresent()) {
                    activeApplicators.put(slotIndex, applicatorOptional.get());
                } else {
                    activeApplicators.remove(slotIndex);
                }
            }
            this.totalNeutronRads += result.totalNeutronDelta;
            if (this.totalNeutronRads < 0) this.totalNeutronRads = 0;
        }

        void applyActiveHazards() {
            if (player.isDead) return;

            if (!activeApplicators.isEmpty()) {
                activeApplicators.values().forEach(applier -> applier.accept(this.player));
            }
            HbmLivingProps.setNeutron(player, 0);

            // 1:1 moved from RadiationSystemNT, but now scales with RadiationConfig.hazardRate
            if (RadiationConfig.neutronActivation) {
                if (totalNeutronRads > 0) {
                    ContaminationUtil.contaminate(player, ContaminationUtil.HazardType.NEUTRON, ContaminationUtil.ContaminationType.CREATIVE,
                            totalNeutronRads * 0.05F * RadiationConfig.hazardRate);
                }
                if (!player.isCreative() && !player.isSpectator()) {
                    double activationRate =
                            ContaminationUtil.getNoNeutronPlayerRads(player) * 0.00004D - (0.00004D * RadiationConfig.neutronActivationThreshold);
                    if (activationRate > minRadRate) {
                        float totalActivationAmount = (float) activationRate * RadiationConfig.hazardRate;
                        if (ContaminationUtil.neutronActivateInventory(player, totalActivationAmount, 1.0F)) {
                            schedulePlayerUpdate(this.player);
                        }
                    }
                }
            }

            if (this.player.inventoryContainer != null) {
                this.player.inventoryContainer.detectAndSendChanges();
            }
        }

        record HazardScanResult(Map<Integer, Consumer<EntityPlayer>> applicatorMap, float totalNeutronRads) {
        }
    }

    private record NbtSensitiveCacheKey(ComparableStack stack, int nbtHash) {
    }

    private record InventoryDelta(UUID playerUUID, int serverSlotIndex, ItemStack oldStack, ItemStack newStack) {
    }

    private record DeltaUpdate(Optional<Consumer<EntityPlayer>> applicator, float neutronRadsDelta) {
    }

    private record PlayerDeltaResult(Map<Integer, Optional<Consumer<EntityPlayer>>> finalApplicators, float totalNeutronDelta) {
    }

    private record HazardUpdateResult(Map<UUID, PlayerHazardData.HazardScanResult> fullScanResults, Map<UUID, PlayerDeltaResult> deltaResults) {
    }
}
