package com.hbm.handler;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Arrays;
import java.util.Random;

public class WeightedRandomChestContentFrom1710 extends WeightedRandom.Item {
    /**
     * The Item/Block ID to generate in the Chest.
     */
    public ItemStack theItemId;
    /**
     * The minimum chance of item generating.
     */
    public int theMinimumChanceToGenerateItem;
    /**
     * The maximum chance of item generating.
     */
    public int theMaximumChanceToGenerateItem;

    public WeightedRandomChestContentFrom1710(Item item, int meta, int minChance, int maxChance, int weight) {
        super(weight);
        this.theItemId = new ItemStack(item, 1, meta);
        this.theMinimumChanceToGenerateItem = minChance;
        this.theMaximumChanceToGenerateItem = maxChance;
    }

    public WeightedRandomChestContentFrom1710(ItemStack stack, int minChance, int maxChance, int weight) {
        super(weight);
        this.theItemId = stack;
        this.theMinimumChanceToGenerateItem = minChance;
        this.theMaximumChanceToGenerateItem = maxChance;
    }

    public static void generateChestContents(Random random, WeightedRandomChestContentFrom1710[] pool, ICapabilityProvider capabilityProvider, int roll) {
        if (capabilityProvider != null && capabilityProvider.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler inventory = capabilityProvider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (inventory instanceof IItemHandlerModifiable) {
                generateChestContents(random, pool, (IItemHandlerModifiable) inventory, roll);
            }
        }
    }

    /**
     * Generates the Chest contents.
     */
    public static void generateChestContents(Random random, WeightedRandomChestContentFrom1710[] pool, IItemHandlerModifiable modifiable, int roll) {
        for (int j = 0; j < roll; ++j) {
            WeightedRandomChestContentFrom1710 weightedrandomchestcontent = WeightedRandom.getRandomItem(random, Arrays.asList(pool));
            ItemStack[] stacks = weightedrandomchestcontent.generateChestContent(random, modifiable);

            for (ItemStack item : stacks) {
                modifiable.setStackInSlot(random.nextInt(modifiable.getSlots()), item);
            }
        }
    }

    /*public static void generateDispenserContents(Random p_150706_0_, WeightedRandomChestContentFrom1710[] p_150706_1_, TileEntityDispenser p_150706_2_, int p_150706_3_)
    {
        for (int j = 0; j < p_150706_3_; ++j)
        {
            WeightedRandomChestContentFrom1710 weightedrandomchestcontent = (WeightedRandomChestContentFrom1710)WeightedRandom.getRandomItem(p_150706_0_, Arrays.asList(p_150706_1_));
            ItemStack[] stacks = weightedrandomchestcontent.generateChestContent(p_150706_0_, p_150706_2_);
            for (ItemStack item : stacks)
            {
                p_150706_2_.setInventorySlotContents(p_150706_0_.nextInt(p_150706_2_.getSizeInventory()), item);
            }
        }
    }*/

    public static WeightedRandomChestContentFrom1710[] func_92080_a(WeightedRandomChestContentFrom1710[] p_92080_0_, WeightedRandomChestContentFrom1710... p_92080_1_) {
        WeightedRandomChestContentFrom1710[] aweightedrandomchestcontent1 = new WeightedRandomChestContentFrom1710[p_92080_0_.length + p_92080_1_.length];
        int i = 0;

        for (int j = 0; j < p_92080_0_.length; ++j) {
            aweightedrandomchestcontent1[i++] = p_92080_0_[j];
        }

        WeightedRandomChestContentFrom1710[] aweightedrandomchestcontent2 = p_92080_1_;
        int k = p_92080_1_.length;

        for (int l = 0; l < k; ++l) {
            WeightedRandomChestContentFrom1710 weightedrandomchestcontent1 = aweightedrandomchestcontent2[l];
            aweightedrandomchestcontent1[i++] = weightedrandomchestcontent1;
        }

        return aweightedrandomchestcontent1;
    }

    // -- Forge hooks

    /**
     * Generates an array of items based on the input min/max count.
     * If the stack can not hold the total amount, it will be split into
     * stacks of size 1.
     *
     * @param rand   A random number generator
     * @param source Source item stack
     * @param min    Minimum number of items
     * @param max    Maximum number of items
     * @return An array containing the generated item stacks
     */
    public static ItemStack[] generateStacks(Random rand, ItemStack source, int min, int max) {
        int count = min + (rand.nextInt(max - min + 1));

        ItemStack[] ret;
        if (source.getItem() == Items.AIR) {
            ret = new ItemStack[0];
        } else if (count > source.getMaxStackSize()) {
            ret = new ItemStack[count];
            for (int x = 0; x < count; x++) {
                ret[x] = source.copy();
                ret[x].setCount(1);
            }
        } else {
            ret = new ItemStack[1];
            ret[0] = source.copy();
            ret[0].setCount(count);
        }
        return ret;
    }

    /**
     * Allow a mod to submit a custom implementation that can delegate item stack generation beyond simple stack lookup
     *
     * @param random       The current random for generation
     * @param newInventory The inventory being generated (do not populate it, but you can refer to it)
     * @return An array of {@link ItemStack} to put into the chest
     */
    public ItemStack[] generateChestContent(Random random, IItemHandlerModifiable newInventory) {
        return generateStacks(random, theItemId, theMinimumChanceToGenerateItem, theMaximumChanceToGenerateItem);
    }
}
