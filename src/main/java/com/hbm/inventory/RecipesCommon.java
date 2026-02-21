package com.hbm.inventory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipesCommon {
    private static final LoadingCache<Block, MetaBlock[]> META_POOLS =
            CacheBuilder.newBuilder().maximumSize(2048).concurrencyLevel(Runtime.getRuntime().availableProcessors()).build(new CacheLoader<>() {
                @Override
                public MetaBlock[] load(Block key) {
                    return new MetaBlock[16];
                }
            });

    @Contract("null -> null; !null -> !null")
    public static ItemStack[] copyStackArray(ItemStack[] array) {

        if (array == null) return null;

        ItemStack[] clone = new ItemStack[array.length];

        for (int i = 0; i < array.length; i++) {

            if (array[i] != null) clone[i] = array[i].copy();
        }

        return clone;
    }

    @Contract("null -> null; !null -> new")
    public static ItemStack[] objectToStackArray(Object[] array) {

        if (array == null) return null;

        ItemStack[] clone = new ItemStack[array.length];

        for (int i = 0; i < array.length; i++) {

            if (array[i] instanceof ItemStack) clone[i] = (ItemStack) array[i];
        }

        return clone;
    }


    /**
     * This is mutable!
     */
    public static abstract class AStack implements Comparable<AStack> {

        public int stacksize;

        @Contract(pure = true)
        public boolean isApplicable(ItemStack stack) {
            return isApplicable(new NbtComparableStack(stack));
        }

        @Contract(mutates = "this")
        public AStack singulize() {
            stacksize = 1;
            return this;
        }

        @Contract(pure = true)
        public int count() {
            return stacksize;
        }

        @Contract(mutates = "this")
        public void setCount(int c) {
            stacksize = c;
        }

        /*
         * Is it unprofessional to pool around in child classes from an abstract superclass? Do I look like I give a shit?
         */
        /**
         * Count sensitive for ComparableStacks.
         */
        public boolean isApplicable(ComparableStack comp) {

            if (this instanceof ComparableStack) {
                return this.equals(comp);
            }

            if (this instanceof OreDictStack) {

                List<ItemStack> ores = OreDictionary.getOres(((OreDictStack) this).name);

                for (ItemStack stack : ores) {
                    if (stack.getItem() == comp.item && stack.getItemDamage() == comp.meta) return true;
                }
            }

            return false;
        }

        /**
         * {@inheritDoc}
         * Whether the supplied itemstack is applicable for a recipe (e.g. anvils). Slightly different from {@code isApplicable}.
         *
         * @param stack      the ItemStack to check
         * @param ignoreSize whether size should be ignored entirely or if the ItemStack needs to be >at least< the same size as this' size
         * @return
         */
        public abstract boolean matchesRecipe(ItemStack stack, boolean ignoreSize);

        public abstract AStack copy();

        public abstract ItemStack getStack();

        public abstract List<ItemStack> getStackList();

        @Override
        @Contract(pure = true)
        public String toString() {
            return "AStack: size, " + stacksize;
        }

        /**
         * Generates either an ItemStack or an ArrayList of ItemStacks
         */
        public abstract List<ItemStack> extractForJEI();

        @Contract("_, -> !null")
        public ItemStack extractForCyclingDisplay(int cycle) {
            List<ItemStack> list = extractForJEI();

            cycle *= 50;

            return list.get((int) (System.currentTimeMillis() % (cycle * list.size()) / cycle));
        }
    }

    /**
     * This is mutable!
     */
    public static class ComparableStack extends AStack {

        public Item item;
        public int meta;

        public ComparableStack(ItemStack stack) {
            this.item = stack.getItem();
            this.stacksize = stack.getCount();
            this.meta = stack.getItemDamage();
        }

        public ComparableStack(Item item) {
            this.item = item;
            this.stacksize = 1;
            this.meta = 0;
        }

        public ComparableStack(Block item) {
            this.item = Item.getItemFromBlock(item);
            this.stacksize = 1;
            this.meta = 0;
        }

        public ComparableStack(Item item, int stacksize) {
            this(item);
            this.stacksize = stacksize;
        }

        public ComparableStack(Item item, int stacksize, int meta) {
            this(item, stacksize);
            this.meta = meta;
        }

        public ComparableStack(Item item, int stacksize, Enum<?> theEnum) {
            this(item, stacksize);
            this.meta = theEnum.ordinal();
        }

        public ComparableStack(Block item, int stacksize) {
            this.item = Item.getItemFromBlock(item);
            this.stacksize = stacksize;
            this.meta = 0;
        }

        public ComparableStack(Block item, int stacksize, int meta) {
            this.item = Item.getItemFromBlock(item);
            this.stacksize = stacksize;
            this.meta = meta;
        }

        public ComparableStack(Block item, int stacksize, Enum<?> theEnum) {
            this(item, stacksize, theEnum.ordinal());
        }

        @Contract(mutates = "this")
        public ComparableStack makeSingular() {
            stacksize = 1;
            return this;
        }

        @Contract("-> new")
        public ItemStack toStack() {
            return new ItemStack(item == null ? ModItems.nothing : item, stacksize, meta);
        }

        @Override
        @Contract("-> new")
        public ItemStack getStack() {
            return toStack();
        }

        @Override
        @Contract("-> new")
        public List<ItemStack> getStackList() {
            return Collections.singletonList(getStack());
        }

        @Contract("-> !null")
        public String[] getDictKeys() {

            int[] ids = toStack().isEmpty() ? null : OreDictionary.getOreIDs(toStack());
            if (ids == null || ids.length == 0) return new String[0];

            String[] entries = new String[ids.length];

            for (int i = 0; i < ids.length; i++) {

                entries[i] = OreDictionary.getOreName(ids[i]);
            }

            return entries;
        }

        //mlbv: the hashmap lookup + string hashing are really heavy, we should only mix the id + meta + stack integers if possible
        @Override
        @Contract(pure = true)
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            if (item == null) {
                MainRegistry.logger.error("ComparableStack has a null item! This is a serious issue!");
                Thread.dumpStack();
                item = Items.STICK;
            }

            ResourceLocation name = Item.REGISTRY.getNameForObject(item);

            if (name == null) {
                MainRegistry.logger.error("ComparableStack holds an item that does not seem to be registered. How does that even happen?");
                Thread.dumpStack();
                item = Items.STICK; //we know sticks have a name, so sure, why not
            }

            if (name != null)
                result = prime * result + Item.REGISTRY.getNameForObject(item).hashCode(); //using the int ID will cause fucky-wuckys if IDs are scrambled
            result = prime * result + meta;
            result = prime * result + stacksize;
            return result;
        }

        @Override
        @Contract(value = "null -> false", pure = true)
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (!(obj instanceof ComparableStack other)) return false;
            if (item == null) {
                if (other.item != null) return false;
            } else if (!item.equals(other.item)) return false;
            if (meta != OreDictionary.WILDCARD_VALUE && other.meta != OreDictionary.WILDCARD_VALUE && meta != other.meta) return false;
            return stacksize == other.stacksize;
        }

        @Override
        @Contract(pure = true)
        public int compareTo(@NotNull AStack stack) {

            if (stack instanceof ComparableStack comp) {

                int thisID = Item.getIdFromItem(item);
                int thatID = Item.getIdFromItem(comp.item);

                if (thisID > thatID) return 1;
                if (thatID > thisID) return -1;

                return Integer.compare(meta, comp.meta);
            }

            //if compared with an ODStack, the CStack will take priority
            if (stack instanceof OreDictStack) return 1;

            return 0;
        }

        @Override
        @Contract(value = "null, _ -> false", pure = true)
        public boolean matchesRecipe(ItemStack stack, boolean ignoreSize) {

            if (stack == null) return false;

            if (stack.getItem() != this.item) return false;

            if (this.meta != OreDictionary.WILDCARD_VALUE && stack.getItemDamage() != this.meta) return false;

            return ignoreSize || stack.getCount() >= this.stacksize;
        }

        @Override
        @Contract("-> new")
        public AStack copy() {
            return new ComparableStack(item, stacksize, meta);
        }

        @Override
        @Contract(pure = true)
        public String toString() {
            return "ComparableStack: { " + stacksize + " x " + item.getRegistryName() + "@" + meta + " }";
        }

        @Override
        @Contract("-> new")
        public List<ItemStack> extractForJEI() {
            return Collections.singletonList(this.toStack());
        }

        @Contract(pure = true)
        public boolean isEmpty() {
            return item == Items.AIR || stacksize <= 0 || meta < -32768 || meta > 65535;
        }
    }

    /**
     * This is mutable!
     */
    public static class NbtComparableStack extends ComparableStack {
        ItemStack stack;

        public NbtComparableStack(ItemStack stack) {
            super(stack);
            this.stack = stack.copy();
        }

        @Override
        @Contract("-> new")
        public ComparableStack makeSingular() {
            ItemStack st = stack.copy();
            st.setCount(1);
            return new NbtComparableStack(st);
        }

        @Override
        @Contract(mutates = "this")
        public AStack singulize() {
            stack.setCount(1);
            this.stacksize = 1;
            return this;
        }

        @Override
        @Contract("-> !null")
        public ItemStack toStack() {
            return stack.copy();
        }

        @Override
        @Contract("-> !null")
        public ItemStack getStack() {
            return toStack();
        }

        @Override
        @Contract(pure = true)
        public int hashCode() {
            if (!stack.hasTagCompound()) return super.hashCode();
            else return super.hashCode() * 31 + stack.getTagCompound().hashCode();
        }

        @Override
        @Contract("-> new")
        public AStack copy() {
            return new NbtComparableStack(stack);
        }

        @Override
        @Contract(value = "null -> false", pure = true)
        public boolean equals(Object obj) {
            if (!stack.hasTagCompound() || !(obj instanceof NbtComparableStack)) {
                return super.equals(obj);
            } else {
                return super.equals(obj) && Library.tagContainsOther(stack.getTagCompound(), ((NbtComparableStack) obj).stack.getTagCompound());
            }
        }

        @Override
        @Contract(value = "null, _ -> false", pure = true)
        public boolean matchesRecipe(ItemStack stack, boolean ignoreSize) {
            return super.matchesRecipe(stack, ignoreSize) && Library.tagContainsOther(this.stack.getTagCompound(), stack.getTagCompound());
        }

        @Override
        @Contract(pure = true)
        public String toString() {
            return "NbtComparableStack: " + stack.toString();
        }

    }

    /**
     * This is mutable!
     */
    public static class OreDictStack extends AStack {

        public String name;

        public OreDictStack(String name) {
            this.name = name;
            this.stacksize = 1;
        }

        public OreDictStack(String name, int stacksize) {
            this(name);
            this.stacksize = stacksize;
        }

        @Contract("-> !null")
        public List<ItemStack> toStacks() {
            return OreDictionary.getOres(name);
        }

        @Override
        @Contract("-> !null")
        public ItemStack getStack() {
            ItemStack stack = toStacks().get(0);
            return new ItemStack(stack.getItem(), stacksize, stack.getMetadata());
        }

        @Override
        @Contract("-> !null")
        public List<ItemStack> getStackList() {
            List<ItemStack> list = Library.copyItemStackList(toStacks());
            for (ItemStack stack : list) {
                stack.setCount(this.stacksize);
            }
            return list;
        }

        @Override
        @Contract(pure = true)
        public int hashCode() {
            return (name + this.stacksize).hashCode();
        }

        @Override
        @Contract(pure = true)
        public int compareTo(@NotNull AStack stack) {

            if (stack instanceof OreDictStack comp) {

                return name.compareTo(comp.name);
            }

            //if compared with a CStack, the ODStack will yield
            if (stack instanceof ComparableStack) return -1;

            return 0;
        }

        @Override
        @Contract(value = "null, _ -> false", pure = true)
        public boolean matchesRecipe(ItemStack stack, boolean ignoreSize) {

            if (stack == null || stack.isEmpty()) return false;

            if (!ignoreSize && stack.getCount() < this.stacksize) return false;

            int[] ids = OreDictionary.getOreIDs(stack);

            for (int id : ids) {
                if (this.name.equals(OreDictionary.getOreName(id))) return true;
            }

            return false;
        }

        @Override
        @Contract(value = "null -> false", pure = true)
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (!(obj instanceof OreDictStack other)) return false;
            if (name == null) {
                if (other.name != null) return false;
            } else if (!name.equals(other.name)) return false;
            return stacksize == other.stacksize;
        }

        @Override
        @Contract("-> new")
        public AStack copy() {
            return new OreDictStack(name, stacksize);
        }

        @Override
        @Contract(pure = true)
        public String toString() {
            return "OreDictStack: name, " + name + ", stacksize, " + stacksize;
        }

        @Override
        @Contract("-> !null")
        public List<ItemStack> extractForJEI() {

            List<ItemStack> fromDict = OreDictionary.getOres(name);
            List<ItemStack> ores = new ArrayList<>();

            for (ItemStack stack : fromDict) {

                ItemStack copy = stack.copy();
                copy.setCount(this.stacksize);

                if (stack.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
                    ores.add(copy);
                } else {
                    ores.addAll(MainRegistry.proxy.getSubItems(copy));
                }
            }

            return ores;
        }
    }

    public static MetaBlock metaOf(Block b, int meta) {
        final MetaBlock[] pool = META_POOLS.getUnchecked(b);
        final int m = meta & 15;
        MetaBlock mb = pool[m];
        if (mb == null) {
            mb = new MetaBlock(b, m);
            // mlbv: yes it races, but who cares?
            pool[m] = mb;
        }
        return mb;
    }

    public static MetaBlock metaOf(IBlockState state) {
        final Block b = state.getBlock();
        return metaOf(b, b.getMetaFromState(state));
    }

    public static void onServerStopping() {
        META_POOLS.invalidateAll();
    }

    @Immutable
    public static final class MetaBlock {

        public final Block block;
        public final int meta;

        /**
         * @deprecated Use {@link #metaOf(Block, int)} or {@link #metaOf(IBlockState)} instead.
         */
        @Deprecated
        public MetaBlock(Block block, int meta) {
            this.block = block;
            this.meta = meta;
        }

        public MetaBlock(Block block) {
            this(block, 0);
        }

        @Override
        @Contract(pure = true)
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Block.REGISTRY.getNameForObject(block).hashCode();
            result = prime * result + meta;
            return result;
        }

        @Override
        @Contract(value = "null -> false", pure = true)
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            MetaBlock other = (MetaBlock) obj;
            if (block == null) {
                if (other.block != null) return false;
            } else if (!block.equals(other.block)) return false;
            return meta == other.meta;
        }

        @Deprecated
        @Contract(pure = true)
        public int getID() {
            return hashCode();
        }
    }
}
