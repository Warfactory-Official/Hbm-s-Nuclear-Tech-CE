package com.hbm.inventory;

import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public final class TransferRule {
    private final int startInclusive;
    private final int endExclusive;
    private final Predicate<ItemStack> accepts;

    TransferRule(int startInclusive, int endExclusive, Predicate<ItemStack> accepts) {
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
        this.accepts = accepts;
    }

    public int startInclusive() {
        return startInclusive;
    }

    public int endExclusive() {
        return endExclusive;
    }

    public boolean accepts(ItemStack stack) {
        return accepts.test(stack);
    }
}
