package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.registry.AbstractReloadableStorage;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.hazard.HazardEntry;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.modifier.IHazardModifier;
import com.hbm.hazard.transformer.HazardTransformerForgeFluid;
import com.hbm.hazard.type.IHazardType;
import com.hbm.hazard.type.HazardTypeDangerousDrop;
import com.hbm.hazard.type.HazardTypeUnstable;
import com.hbm.lib.ObjObjDoubleConsumer;
import groovy.lang.Closure;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.ObjDoubleConsumer;

@SuppressWarnings("MethodMayBeStatic")
@RegistryDescription(linkGenerator = "hbm")
public final class FluidHazards extends VirtualizedRegistry<Tuple<String, HazardEntry>> {

    private final Hazards.HazardTypeFacade types = new Hazards.HazardTypeFacade();
    private final Hazards.HazardModifierFacade modifiers = new Hazards.HazardModifierFacade();

    public FluidHazards() {
        super(Collections.singletonList("fluidHazards"));
    }

    @Override
    public void onReload() {
        // Remove scripted additions first
        for (Tuple<String, HazardEntry> change : removeScripted()) {
            ObjectArrayList<HazardEntry> list = HazardTransformerForgeFluid.FLUID_HAZARDS.get(change.getFirst());
            if (list != null) {
                for (int i = list.size() - 1; i >= 0; --i) if (list.get(i) == change.getSecond()) list.remove(i);
                if (list.isEmpty()) HazardTransformerForgeFluid.FLUID_HAZARDS.remove(change.getFirst());
            }
        }
        // Then restore backups
        for (Tuple<String, HazardEntry> change : restoreFromBackup()) {
            HazardTransformerForgeFluid.FLUID_HAZARDS.computeIfAbsent(change.getFirst(), k -> new ObjectArrayList<>()).add(change.getSecond());
        }
    }

    @Override
    protected AbstractReloadableStorage<Tuple<String, HazardEntry>> createRecipeStorage() {
        return new AbstractReloadableStorage<>() {
            @Override
            protected boolean compareRecipe(Tuple<String, HazardEntry> a, Tuple<String, HazardEntry> b) {
                if (a == b) return true;
                if (a == null || b == null) return false;
                return Objects.equals(a.getFirst(), b.getFirst()) && a.getSecond() == b.getSecond();
            }
        };
    }

    @MethodDescription(type = MethodDescription.Type.QUERY, description = "Access built-in hazard types and factories (e.g., radiation(), unstable(timer), dangerousDrop(...)).")
    public Hazards.HazardTypeFacade getTypes() {
        return types;
    }

    @MethodDescription(type = MethodDescription.Type.QUERY, description = "Access helper hazard modifiers (fuel/RTG/RBMK/etc.) to attach to entries in the builder.")
    public Hazards.HazardModifierFacade getModifiers() {
        return modifiers;
    }

    @MethodDescription(type = MethodDescription.Type.QUERY, description = "Create a new FluidHazardBuilder. Use to compose one or more HazardEntry rows for a fluid.")
    public FluidHazardBuilder builder() {
        return new FluidHazardBuilder();
    }

    @Nullable
    private String normalizeFluidName(Object id) {
        if (id == null) return null;
        if (id instanceof String string) {
            String s = string.trim();
            if (s.isEmpty()) return null;
            int colon = s.indexOf(':');
            return (colon >= 0 && colon < s.length() - 1) ? s.substring(colon + 1).toLowerCase(Locale.ENGLISH) : s.toLowerCase(Locale.ENGLISH);
        }
        if (id instanceof ResourceLocation resourceLocation) {
            return resourceLocation.getPath().toLowerCase(Locale.ENGLISH);
        }
        if (id instanceof Fluid fluid) {
            return fluid.getName().toLowerCase(Locale.ENGLISH);
        }
        GroovyLog.get().warn("Unknown fluid identifier type '{}': {}", id.getClass().getName(), id);
        return null;
    }

    private void warnIfEmpty(@Nullable String fluidName, String action) {
        if (fluidName == null || fluidName.isEmpty()) {
            GroovyLog.get().warn("HBM fluid hazard {}: invalid or empty fluid id", action);
        }
    }

    @MethodDescription(type = MethodDescription.Type.ADDITION, description = "Append a single HazardEntry to a fluid. fluidId may be String ('modid:fluid' or 'fluid'), ResourceLocation, or Fluid. Does not remove existing entries.")
    public void add(Object fluidId, HazardEntry entry) {
        String name = normalizeFluidName(fluidId);
        warnIfEmpty(name, "add");
        if (name == null || entry == null) return;
        addScripted(new Tuple<>(name, entry));
        HazardTransformerForgeFluid.FLUID_HAZARDS.computeIfAbsent(name, k -> new ObjectArrayList<>()).add(entry);
    }

    @MethodDescription(type = MethodDescription.Type.ADDITION, description = "Append all entries produced by the builder to a fluid. fluidId may be String/ResourceLocation/Fluid. Existing entries remain.")
    public void add(Object fluidId, FluidHazardBuilder builder) {
        String name = normalizeFluidName(fluidId);
        warnIfEmpty(name, "add");
        if (name == null || builder == null) return;
        for (HazardEntry entry : builder.entries()) {
            if (entry != null) {
                addScripted(new Tuple<>(name, entry));
                HazardTransformerForgeFluid.FLUID_HAZARDS.computeIfAbsent(name, k -> new ObjectArrayList<>()).add(entry);
            }
        }
    }

    @MethodDescription(type = MethodDescription.Type.ADDITION, description = "Groovy DSL variant of add(). The closure receives a FluidHazardBuilder as delegate. Existing entries remain.")
    public void add(Object fluidId, Closure<?> definition) {
        if (definition == null) {
            GroovyLog.get().warn("HBM fluid hazard add: closure is null for {}", fluidId);
            return;
        }
        FluidHazardBuilder b = builder();
        Closure<?> c = (Closure<?>) definition.clone();
        c.setDelegate(b);
        c.setResolveStrategy(Closure.DELEGATE_FIRST);
        c.call(b);
        add(fluidId, b);
    }

    @MethodDescription(type = MethodDescription.Type.VALUE, description = "Replace all existing hazards for a fluid with the builder's entries. Backs up previous entries for hot reload.")
    public void set(Object fluidId, FluidHazardBuilder builder) {
        clear(fluidId);
        add(fluidId, builder);
    }

    @MethodDescription(type = MethodDescription.Type.VALUE, description = "Groovy DSL variant of set(). Replaces all existing hazards for the fluid.")
    public void set(Object fluidId, Closure<?> definition) {
        clear(fluidId);
        add(fluidId, definition);
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, description = "Remove a specific HazardEntry instance from a fluid. Returns true if removed. Note: removal is identity-based on the entry object.")
    public boolean remove(Object fluidId, HazardEntry entry) {
        String name = normalizeFluidName(fluidId);
        warnIfEmpty(name, "remove");
        if (name == null || entry == null) return false;
        ObjectArrayList<HazardEntry> list = HazardTransformerForgeFluid.FLUID_HAZARDS.get(name);
        if (list == null) return false;
        boolean removed = list.remove(entry);
        if (removed && list.isEmpty()) {
            HazardTransformerForgeFluid.FLUID_HAZARDS.remove(name);
        }
        if (removed) {
            addBackup(new Tuple<>(name, entry));
        }
        return removed;
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, description = "Clear all hazard entries for a single fluid. Previous entries are backed up for reload; no-op if none present.")
    public void clear(Object fluidId) {
        String name = normalizeFluidName(fluidId);
        warnIfEmpty(name, "clear");
        if (name == null) return;
        ObjectArrayList<HazardEntry> prev = HazardTransformerForgeFluid.FLUID_HAZARDS.remove(name);
        if (prev == null || prev.isEmpty()) {
            GroovyLog.get().info("No fluid hazards registered for '{}'", name);
        } else {
            for (HazardEntry entry : prev) addBackup(new Tuple<>(name, entry));
        }
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, description = "Clear all fluids' hazard entries. Backs up everything for hot reload before clearing.")
    public void clearAll() {
        // Backup all entries before clearing for hot reload
        HazardTransformerForgeFluid.FLUID_HAZARDS.forEach((fluid, list) -> {
            for (HazardEntry entry : list) addBackup(new Tuple<>(fluid, entry));
        });
        HazardTransformerForgeFluid.FLUID_HAZARDS.clear();
    }

    @MethodDescription(type = MethodDescription.Type.QUERY, description = "List all HazardEntry rows currently registered for a fluid as an immutable copy. Returns empty list if none.")
    public List<HazardEntry> list(Object fluidId) {
        String name = normalizeFluidName(fluidId);
        if (name == null) return Collections.emptyList();
        ObjectArrayList<HazardEntry> list = HazardTransformerForgeFluid.FLUID_HAZARDS.get(name);
        if (list == null) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    @MethodDescription(type = MethodDescription.Type.VALUE, description = "Toggle whether fluid hazards apply to NTM containers (filled items). Default true. Returns the new value.")
    public boolean setApplyToNTMContainer(boolean value) {
        return (HazardTransformerForgeFluid.applyToNTMContainer = value);
    }

    public static final class FluidHazardBuilder {
        private final List<HazardEntry> entries = new ArrayList<>();
        private boolean built;

        private void ensureMutable() {
            if (built) throw new IllegalStateException("FluidHazardBuilder already built");
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add a generic hazard entry for this fluid. Supply a HazardTypeBase, numeric level, and optional modifiers.")
        public FluidHazardBuilder entry(IHazardType type, double level, IHazardModifier... mods) {
            ensureMutable();
            if (type == null) {
                GroovyLog.get().warn("Cannot add fluid hazard entry for null type.");
                return this;
            }
            HazardEntry e = new HazardEntry(type, level);
            if (mods != null) {
                for (IHazardModifier m : mods) if (m != null) e.addMod(m);
            }
            entries.add(e);
            return this;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add radiation entry for the fluid.")
        public FluidHazardBuilder radiation(double level, IHazardModifier... mods) {
            return entry(HazardRegistry.RADIATION, level, mods);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add contaminating entry for the fluid.")
        public FluidHazardBuilder contaminating(double level) {
            return entry(HazardRegistry.CONTAMINATING, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add digamma entry for the fluid.")
        public FluidHazardBuilder digamma(double level) {
            return entry(HazardRegistry.DIGAMMA, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add hot entry for the fluid.")
        public FluidHazardBuilder hot(double level) {
            return entry(HazardRegistry.HOT, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add blinding entry for the fluid.")
        public FluidHazardBuilder blinding(double level) {
            return entry(HazardRegistry.BLINDING, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add asbestos entry for the fluid.")
        public FluidHazardBuilder asbestos(double level) {
            return entry(HazardRegistry.ASBESTOS, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add coal entry for the fluid.")
        public FluidHazardBuilder coal(double level) {
            return entry(HazardRegistry.COAL, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add hydroactive entry for the fluid (reacts with water).")
        public FluidHazardBuilder hydroactive(double level) {
            return entry(HazardRegistry.HYDROACTIVE, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add explosive entry for the fluid.")
        public FluidHazardBuilder explosive(double level) {
            return entry(HazardRegistry.EXPLOSIVE, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add toxic entry for the fluid.")
        public FluidHazardBuilder toxic(double level) {
            return entry(HazardRegistry.TOXIC, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add cold entry for the fluid (cryogenic).")
        public FluidHazardBuilder cold(double level) {
            return entry(HazardRegistry.COLD, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add an Unstable hazard with a decay timer in ticks (server ticks).")
        public FluidHazardBuilder unstable(double level, int timer) {
            return entry(new HazardTypeUnstable(timer), level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add an Unstable hazard with a decay timer and custom tooltip info provider.")
        public FluidHazardBuilder unstable(double level, int timer, IHazardType.HazardInfoConsumer info) {
            return entry(new HazardTypeUnstable(timer, info), level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add a custom Unstable hazard without tooltip.")
        public FluidHazardBuilder unstable(double level, ObjObjDoubleConsumer<EntityLivingBase, ItemStack> onUpdate, ObjDoubleConsumer<EntityItem> onDrop) {
            return entry(new HazardTypeUnstable(onUpdate, onDrop), level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add a custom Unstable hazard.")
        public FluidHazardBuilder unstable(double level, ObjObjDoubleConsumer<EntityLivingBase, ItemStack> onUpdate, ObjDoubleConsumer<EntityItem> onDrop, IHazardType.HazardInfoConsumer customInfo) {
            return entry(new HazardTypeUnstable(onUpdate, onDrop, customInfo), level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Add a hazard that only acts when the item containing this fluid is dropped.")
        public FluidHazardBuilder dangerousDrop(double level, ObjDoubleConsumer<EntityItem> onDrop) {
            return entry(new HazardTypeDangerousDrop(onDrop), level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY, description = "Return an immutable snapshot of the accumulated entries. Further mutation is disabled after this call.")
        public List<HazardEntry> entries() {
            built = true;
            return Collections.unmodifiableList(entries);
        }
    }
}
