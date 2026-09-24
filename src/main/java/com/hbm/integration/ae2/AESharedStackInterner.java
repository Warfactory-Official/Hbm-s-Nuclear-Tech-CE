package com.hbm.integration.ae2;

import com.hbm.main.MainRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public final class AESharedStackInterner {

    private static final Int2ObjectOpenHashMap<List<Entry>> BUCKETS = new Int2ObjectOpenHashMap<>();
    private static final ReferenceQueue<Object> QUEUE = new ReferenceQueue<>();
    private static Constructor<?> constructor;
    private static boolean failed = false;

    private AESharedStackInterner() {}

    public static synchronized Object intern(ItemStack stack) {
        if (failed) return null;
        purge();

        int hash = Objects.hash(stack.getItem(), stack.getItemDamage(), stack.getTagCompound());
        List<Entry> bucket = BUCKETS.get(hash);
        if (bucket != null) {
            for (Entry e : bucket) {
                Object shared = e.get();
                if (shared != null && matches(e.definition, stack)) return shared;
            }
        }

        ItemStack definition = stack.copy();
        definition.setCount(1);
        Object shared;
        try {
            if (constructor == null) {
                constructor = Class.forName("appeng.util.item.AESharedItemStack").getConstructor(ItemStack.class);
                constructor.setAccessible(true);
            }
            shared = constructor.newInstance(definition);
        } catch (ReflectiveOperationException | RuntimeException e) {
            failed = true;
            MainRegistry.logger.warn("AE2 shared stack registry patch failed, falling back to AE2's hash-only registry", e);
            return null;
        }

        if (bucket == null) {
            bucket = new ArrayList<>(1);
            BUCKETS.put(hash, bucket);
        }
        bucket.add(new Entry(shared, hash, definition));
        return shared;
    }

    private static boolean matches(ItemStack definition, ItemStack stack) {
        return definition.getItem() == stack.getItem()
                && definition.getItemDamage() == stack.getItemDamage()
                && ItemStack.areItemStackTagsEqual(definition, stack)
                && definition.areCapsCompatible(stack);
    }

    private static void purge() {
        Reference<?> ref;
        while ((ref = QUEUE.poll()) != null) {
            Entry e = (Entry) ref;
            List<Entry> bucket = BUCKETS.get(e.hash);
            if (bucket == null) continue;
            bucket.remove(e);
            if (bucket.isEmpty()) BUCKETS.remove(e.hash);
        }
    }

    private static final class Entry extends WeakReference<Object> {
        final int hash;
        final ItemStack definition;

        Entry(Object shared, int hash, ItemStack definition) {
            super(shared, QUEUE);
            this.hash = hash;
            this.definition = definition;
        }
    }
}
