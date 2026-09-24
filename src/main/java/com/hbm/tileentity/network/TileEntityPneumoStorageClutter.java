package com.hbm.tileentity.network;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerPneumoStorageClutter;
import com.hbm.inventory.gui.GUIPneumoStorageClutter;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityPneumoStorageClutter extends TileEntityPneumaticStorageBase {

    public TileEntityPneumoStorageClutter() {
        super(6 * 9);
    }

    @Override
    public String getDefaultName() {
        return "container.pneumoStorageClutter";
    }

    @Override public boolean allowTypeSetting() { return true; }

    @Override
    public long getAmountAt(int index) {
        ItemStack stack = this.getSlotAt(index);
        return stack.isEmpty() ? 0 : stack.getCount();
    }

    @Override
    public long useUpItem(int index, long amount) {
        ItemStack stack = this.inventory.getStackInSlot(index);
        if(stack.isEmpty()) return amount;

        int toRemove = (int) Math.min(stack.getCount(), amount);
        stack.shrink(toRemove);
        if(stack.isEmpty()) this.inventory.setStackInSlot(index, ItemStack.EMPTY);
        this.markDirty();

        return amount - toRemove;
    }

    @Override
    public long addItem(int index, long amount) {
        ItemStack stack = this.inventory.getStackInSlot(index);
        if(stack.isEmpty()) return amount;

        int capacity = Math.min(stack.getMaxStackSize(), this.inventory.getSlotLimit(index));
        int toAdd = (int) Math.min(amount, capacity - stack.getCount());
        if(toAdd <= 0) return amount;

        stack.grow(toAdd);
        this.markDirty();

        return amount - toAdd;
    }

    @Override
    public long setupType(int index, ItemStack zeroStack, long amount) {
        int capacity = Math.min(zeroStack.getMaxStackSize(), this.inventory.getSlotLimit(index));
        int finalSize = (int) Math.min(amount, capacity);

        ItemStack placed = zeroStack.copy();
        placed.setCount(finalSize);
        this.inventory.setStackInSlot(index, placed);
        this.markDirty();

        return amount - finalSize;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerPneumoStorageClutter(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIPneumoStorageClutter(player.inventory, this);
    }
}
