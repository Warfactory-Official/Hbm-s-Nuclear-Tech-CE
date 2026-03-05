package com.hbm.saveddata;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.AnnihilatorRecipes;
import com.hbm.util.ItemStackUtil;
import com.hbm.interfaces.NotableComments;
import com.hbm.inventory.RecipesCommon.ComparableStack;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import org.jetbrains.annotations.NotNull;

@NotableComments
public class AnnihilatorSavedData extends WorldSavedData {

    public static final String KEY = "annihilator";

    public HashMap<String, AnnihilatorPool> pools = new HashMap<>();

    public AnnihilatorSavedData() {
        super(KEY);
        this.markDirty();
    }

    public AnnihilatorSavedData(String name) { super(name); }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList pools = nbt.getTagList("pools", 10);

        for(int i = 0; i < pools.tagCount(); i++) {
            NBTTagCompound poolCompound = pools.getCompoundTagAt(i);

            String poolName = poolCompound.getString("poolname");
            AnnihilatorPool pool = new AnnihilatorPool();
            pool.deserialize(poolCompound.getTagList("pool", 10));

            this.pools.put(poolName, pool);
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound nbt) {

        NBTTagList pools = new NBTTagList();

        for(Entry<String, AnnihilatorPool> entry : this.pools.entrySet()) {
            NBTTagCompound pool = new NBTTagCompound();
            NBTTagList poolList = new NBTTagList();

            entry.getValue().serialize(poolList);
            pool.setString("poolname", entry.getKey());
            pool.setTag("pool", poolList);
            pools.appendTag(pool);
        }

        nbt.setTag("pools", pools);

        return nbt;
    }

    public static AnnihilatorSavedData getData(World world) {
        AnnihilatorSavedData data = (AnnihilatorSavedData) world.getPerWorldStorage().getOrLoadData(AnnihilatorSavedData.class, KEY);
        if(data == null) {
            world.getPerWorldStorage().setData(KEY, new AnnihilatorSavedData());
            data = (AnnihilatorSavedData) world.getPerWorldStorage().getOrLoadData(AnnihilatorSavedData.class, KEY);
        }
        return data;
    }

    public AnnihilatorPool grabPool(String pool) {
        AnnihilatorPool poolInstance = pools.get(pool);
        if(poolInstance == null) {
            poolInstance = new AnnihilatorPool();
            pools.put(pool, poolInstance);
        }
        return poolInstance;
    }

    /** For fluids */
    public ItemStack pushToPool(String pool, FluidType type, long amount, boolean alwaysPayOut) {
        AnnihilatorPool poolInstance = grabPool(pool);
        ItemStack payout = poolInstance.increment(type, amount, alwaysPayOut);
        this.markDirty();
        return payout;
    }

    /** For items (type + meta as well as only type), also handles ore dict */
    public ItemStack pushToPool(String pool, ItemStack stack, boolean alwaysPayOut) {
        AnnihilatorPool poolInstance = grabPool(pool);

        ItemStack itemPayout = poolInstance.increment(stack.getItem(), stack.getCount(), alwaysPayOut);
        ItemStack compPayout = poolInstance.increment(new ComparableStack(stack).makeSingular(), stack.getCount(), alwaysPayOut);
        ItemStack dictPayout = null;

        List<String> oreDict = ItemStackUtil.getOreDictNames(stack);
        for(String name : oreDict) if(name != null && !name.isEmpty()) { // because some assholes pollute the ore dict with crap values
            ItemStack payout = poolInstance.increment(name, stack.getCount(), alwaysPayOut);
            if(payout != null) dictPayout = payout;
        }

        // originally a lookup for fluid containers was considered, but no one would use the annihilator like that, and it adds unnecessary overhead

        this.markDirty();

        return dictPayout != null ? dictPayout : compPayout != null ? compPayout : itemPayout;
    }

    public static class AnnihilatorPool {

        /**
         * Valid keys include:
         * <ul>
         * <li>Items, for wildcard</li>
         * <li>ComparableStacks, for type + meta</li>
         * <li>FluidTypes</li>
         * <li>Strings, for ore dict keys</li>
         * </ul>
         * ItemStacks should be analyzed for all matching types and added accordingly. Any additions need to be added for (de)serializing
         */
        public HashMap<Object, BigInteger> items = new HashMap<>();

        public ItemStack increment(Object type, long amount, boolean alwaysPayOut) {
            ItemStack payout;
            BigInteger counter = items.get(type);
            if(counter == null) {
                counter = BigInteger.valueOf(amount);
                payout = AnnihilatorRecipes.getHighestPayoutFromKey(type, BigInteger.ZERO, counter);
            } else {
                BigInteger prev = counter;
                counter = counter.add(BigInteger.valueOf(amount));
                payout = AnnihilatorRecipes.getHighestPayoutFromKey(type, alwaysPayOut ? null : prev, counter);
            }
            items.put(type, counter);
            return payout;
        }

        public void serialize(NBTTagList nbt) {
            for(Entry<Object, BigInteger> entry : items.entrySet()) {
                NBTTagCompound compound = new NBTTagCompound();
                serializeKey(compound, entry.getKey());
                compound.setByteArray("amount", entry.getValue().toByteArray());
                nbt.appendTag(compound);
            }
        }

        public void deserialize(NBTTagList nbt) {
            try {
                for(int i = 0; i < nbt.tagCount(); i++) {
                    NBTTagCompound compound = (NBTTagCompound) nbt.get(i);
                    Object key = deserializeKey(compound);
                    if(key != null) this.items.put(key, new BigInteger(compound.getByteArray("amount")));
                }
            } catch(Throwable _) { } // because world data can be dented to all fucking hell and back
        }

        /** Originally this uses NBTTagLists which broke down everything into byte arrays. It probably worked, but my stupid ass
         * defined some NBT crap in the upper levels wrong so nothing worked, and this got rewritten too. Well at least now it does. */
        public void serializeKey(NBTTagCompound nbt, Object key) {
            if(key instanceof Item item) { // 0
                nbt.setByte("key", (byte) 0);
                nbt.setString("item", Item.REGISTRY.getNameForObject(item).toString());
            }

            if(key instanceof ComparableStack comp) { // 1
                nbt.setByte("key", (byte) 1);
                nbt.setString("item", Item.REGISTRY.getNameForObject(comp.item).toString());
                nbt.setShort("meta", (short) comp.meta);
            }

            if(key instanceof FluidType type) { // 2
                nbt.setByte("key", (byte) 2);
                nbt.setString("fluid", type.getName());
            }

            if(key instanceof String) { // 3
                nbt.setByte("key", (byte) 3);
                nbt.setString("dict", (String) key);
            }
        }

        public Object deserializeKey(NBTTagCompound nbt) {
            try {
                byte key = nbt.getByte("key");

                if(key == 0) { // item
                    return Item.REGISTRY.getObject(new ResourceLocation(nbt.getString("item")));
                }
                if(key == 1) { // comparablestack
                    return new ComparableStack(Item.REGISTRY.getObject(new ResourceLocation(nbt.getString("item"))), 1, nbt.getShort("meta"));
                }
                if(key == 2) { // fluidtype
                    return Fluids.fromName(nbt.getString("fluid"));
                }
                if(key == 3) { // strong
                    return nbt.getString("dict");
                }
                // i feel filthy

            } catch(Throwable _) { }
            return null;
        }
    }
}
