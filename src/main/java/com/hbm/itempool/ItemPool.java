package com.hbm.itempool;

import com.hbm.api.tile.ILootContainerModifiable;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.handler.WeightedRandomFrom1710;
import com.hbm.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.ILootContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static com.hbm.lib.HbmChestContents.weighted;

public class ItemPool {

    public static void initialize() {
        ItemPoolsLegacy.init();
        ItemPoolsComponent.init();
        ItemPoolsSingle.init();
        ItemPoolsRedRoom.init();
        ItemPoolsSatellite.init();
        ItemPoolsPile.init();
        ItemPoolsC130.init();
        ItemPoolsVendingMachine.init();
    }

    public static HashMap<String, ItemPool> pools = new HashMap<>();

    public String name;
    public WeightedRandomChestContentFrom1710[] pool = new WeightedRandomChestContentFrom1710[0];

    private List<WeightedRandomChestContentFrom1710> buildingList = new ArrayList<>();

    public ItemPool() { }

    public ItemPool(String name) {
        this.name = name;
        pools.put(name, this);
    }

    public ItemPool add(Item item, int meta, int min, int max, int weight) {	buildingList.add(new WeightedRandomChestContentFrom1710(item, meta, min, max, weight));							return this; }
    public ItemPool add(Block block, int meta, int min, int max, int weight) {	buildingList.add(new WeightedRandomChestContentFrom1710(Item.getItemFromBlock(block), meta, min, max, weight));	return this; }
    public ItemPool add(ItemStack item, int min, int max, int weight) {			buildingList.add(new WeightedRandomChestContentFrom1710(item, min, max, weight));								return this; }

    public ItemPool build() {

        this.pool = new WeightedRandomChestContentFrom1710[buildingList.size()];

        for(int i = 0; i < pool.length; i++) {
            this.pool[i] = this.buildingList.get(i);
        }

        this.buildingList.clear();

        return this;
    }

    /** Grabs the specified item pool out of the pool map, will return the backup pool if the given pool is not present */
    public static WeightedRandomChestContentFrom1710[] getPool(String name) {
        ItemPool pool = pools.get(name);
        if(pool == null) return backupPool;
        return pool.pool;
    }

    public static ItemStack getStack(String pool, Random rand) {
        return getStack(ItemPool.getPool(pool), rand);
    }

    public static ItemStack getStack(WeightedRandomChestContentFrom1710[] pool, Random rand) {
        WeightedRandomChestContentFrom1710 weighted = (WeightedRandomChestContentFrom1710) WeightedRandomFrom1710.getRandomItem(rand, pool);
        ItemStack stack = weighted.theItemId.copy();
        stack.setCount(weighted.theMinimumChanceToGenerateItem + rand.nextInt(weighted.theMaximumChanceToGenerateItem - weighted.theMinimumChanceToGenerateItem + 1));
        return stack;
    }

    /** Should a pool be lost due to misconfiguration or otherwise, this pool will be returned in its place */
    private static WeightedRandomChestContentFrom1710[] backupPool = new WeightedRandomChestContentFrom1710[] {
            weighted(Items.BREAD, 0, 1, 3, 10),
            weighted(Items.STICK, 0, 2, 5, 10),
            weighted(ModItems.scrap, 0, 1, 3, 10),
            weighted(ModItems.dust, 0, 2, 5, 5)
    };

    public static boolean writeLootTable(TileEntity tile, ResourceLocation table, long seed) {
        if (!(tile instanceof ILootContainer)) return false;
        if (tile instanceof ILootContainerModifiable modifiable) {
            modifiable.setLootTable(table, seed);
            return true;
        } else if (tile instanceof TileEntityLockableLoot loot){
            loot.setLootTable(table, seed);
            return true;
        }
        return false;
    }
}
