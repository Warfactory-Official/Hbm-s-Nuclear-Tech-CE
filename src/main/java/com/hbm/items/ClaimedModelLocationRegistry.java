package com.hbm.items;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public final class ClaimedModelLocationRegistry {
    private static final Set<IModelLocationOwner> OWNERS = new ReferenceOpenHashSet<>(256);
    private static final Reference2ObjectOpenHashMap<Item, ITeisrBinding> TEISR_BINDINGS_BY_ITEM = new Reference2ObjectOpenHashMap<>(128);
    private static final Object2ObjectOpenHashMap<ModelResourceLocation, ITeisrBinding> TEISR_BINDINGS_BY_LOCATION = new Object2ObjectOpenHashMap<>(128);
    private static final Object2ObjectOpenHashMap<ModelResourceLocation, IModelLocationOwner> OWNERS_BY_LOCATION = new Object2ObjectOpenHashMap<>(256);
    private static final ObjectOpenHashSet<ModelResourceLocation> UNKNOWN_LOCATIONS = new ObjectOpenHashSet<>(256);

    private ClaimedModelLocationRegistry() {
    }

    public static void register(IClaimedModelLocation claimant) {
        OWNERS.add(claimant);
        clearLocationCache();
    }

    public static void registerTeisrBinding(ITeisrBinding binding) {
        Item item = binding.getItem();
        if (TEISR_BINDINGS_BY_ITEM.containsKey(item)) {
            throw new IllegalStateException("Duplicate TEISR binding for " + item.getRegistryName());
        }
        ModelResourceLocation modelLocation = binding.getModelLocation();
        ITeisrBinding collision = TEISR_BINDINGS_BY_LOCATION.get(modelLocation);
        if (collision != null) {
            throw new IllegalStateException("Duplicate TEISR model location " + modelLocation + " for " + item.getRegistryName() + " and " + collision.getItem().getRegistryName());
        }
        TEISR_BINDINGS_BY_LOCATION.put(modelLocation, binding);
        TEISR_BINDINGS_BY_ITEM.put(item, binding);
        OWNERS.add(binding);
        clearLocationCache();
    }

    public static void unregisterTeisrBinding(Item item) {
        ITeisrBinding binding = TEISR_BINDINGS_BY_ITEM.remove(item);
        if (binding != null) {
            TEISR_BINDINGS_BY_LOCATION.remove(binding.getModelLocation());
            OWNERS.remove(binding);
            clearLocationCache();
        }
    }

    public static boolean owns(ModelResourceLocation location) {
        return find(location) != null;
    }

    @Nullable
    public static IModelLocationOwner find(ModelResourceLocation location) {
        ITeisrBinding teisrBinding = TEISR_BINDINGS_BY_LOCATION.get(location);
        if (teisrBinding != null) {
            return teisrBinding;
        }
        IModelLocationOwner cachedOwner = OWNERS_BY_LOCATION.get(location);
        if (cachedOwner != null) {
            return cachedOwner;
        }
        if (UNKNOWN_LOCATIONS.contains(location)) {
            return null;
        }
        for (IModelLocationOwner owner : OWNERS) {
            if (owner.ownsModelLocation(location)) {
                OWNERS_BY_LOCATION.put(location, owner);
                return owner;
            }
        }
        UNKNOWN_LOCATIONS.add(location);
        return null;
    }

    @Nullable
    public static ITeisrBinding getTeisrBinding(Item item) {
        return TEISR_BINDINGS_BY_ITEM.get(item);
    }

    public static Collection<ITeisrBinding> getTeisrBindings() {
        return Collections.unmodifiableCollection(TEISR_BINDINGS_BY_ITEM.values());
    }

    @Nullable
    public static ModelResourceLocation getSyntheticTeisrModelLocation(Item item) {
        ITeisrBinding binding = TEISR_BINDINGS_BY_ITEM.get(item);
        if (binding == null || !binding.isSyntheticLocation()) {
            return null;
        }
        return binding.getModelLocation();
    }

    public static boolean hasSyntheticTeisrBinding(Item item) {
        return getSyntheticTeisrModelLocation(item) != null;
    }

    private static void clearLocationCache() {
        OWNERS_BY_LOCATION.clear();
        UNKNOWN_LOCATIONS.clear();
    }

    public interface ITeisrBinding extends IModelLocationOwner {
        Item getItem();

        ModelResourceLocation getModelLocation();

        boolean isSyntheticLocation();
    }
}
