package com.hbm.inventory.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotCraftingOutput extends SlotItemHandler {

    private EntityPlayer player;
    private int craftBuffer;

    public SlotCraftingOutput(EntityPlayer player, IItemHandler inventory, int i, int j, int k) {
        super(inventory, i, j, k);
        this.player = player;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    // ugly but nothing to be done
    public static void checkAchievements(EntityPlayer player, ItemStack stack) {
        //AchievementHandler.fire(player, stack);
    }

    @Override
    public ItemStack decrStackSize(int amount) {
        if (this.getHasStack()) {
            this.craftBuffer += Math.min(amount, this.getStack().getCount());
        }
        return super.decrStackSize(amount);
    }

    @Override
    public ItemStack onTake(EntityPlayer player, ItemStack stack) {
        this.onCrafting(stack);
        super.onTake(player, stack);
        return stack;
    }

    @Override
    protected void onCrafting(ItemStack stack, int amount) {
        this.craftBuffer += amount;
        this.onCrafting(stack);
    }

    @Override
    protected void onCrafting(ItemStack stack) {
        stack.onCrafting(this.player.world, this.player, this.craftBuffer);
        checkAchievements(this.player, stack);
        this.craftBuffer = 0;
    }
}
