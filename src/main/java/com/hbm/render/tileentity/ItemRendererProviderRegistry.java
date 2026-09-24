package com.hbm.render.tileentity;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public final class ItemRendererProviderRegistry {
    private static final Set<IItemRendererProvider> ITEM_PROVIDERS = new ReferenceOpenHashSet<>(32);
    private static final Set<IItemRendererProvider> TILE_ENTITY_PROVIDERS = new ReferenceOpenHashSet<>(256);

    private ItemRendererProviderRegistry() {
    }

    public static void registerItemProvider(IItemRendererProvider provider) {
        ITEM_PROVIDERS.add(provider);
    }

    public static void registerTileEntityProvider(IItemRendererProvider provider) {
        TILE_ENTITY_PROVIDERS.add(provider);
    }

    public static Collection<IItemRendererProvider> getItemProviders() {
        return Collections.unmodifiableCollection(ITEM_PROVIDERS);
    }

    public static Collection<IItemRendererProvider> getTileEntityProviders() {
        return Collections.unmodifiableCollection(TILE_ENTITY_PROVIDERS);
    }
}
