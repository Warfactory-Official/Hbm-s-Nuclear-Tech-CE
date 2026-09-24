package com.hbm.lib;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.Random;
import java.util.stream.IntStream;

public class InventoryHelper {
	
	public static final Random RANDOM = new Random();

	public static void dropInventoryItems(World world, BlockPos pos, ICapabilityProvider t) {
		if(t == null)
			return;
		if(!t.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
			return;
		IItemHandler inventory = t.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		for (int i = 0; i < inventory.getSlots(); ++i)
        {
            ItemStack itemstack = inventory.getStackInSlot(i);

            if (!itemstack.isEmpty())
            {
                spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), itemstack);
            }
        }
	}

    public static void dropInventoryItems(World world, BlockPos pos, IItemHandler inventory) {
        IntStream.range(0, inventory.getSlots()).mapToObj(inventory::getStackInSlot).filter(itemstack ->
                !itemstack.isEmpty()).forEach(itemstack -> spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), itemstack));
    }

    public static void dropInventoryItems(World world, BlockPos pos, IItemHandler inventory, int beginSlot, int endSlot) {
        if(inventory == null)
            return;
        for (int i = beginSlot; i <= endSlot && i < inventory.getSlots(); ++i)
        {
            ItemStack itemstack = inventory.getStackInSlot(i);

            if (!itemstack.isEmpty())
            {
                spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), itemstack);
            }
        }
    }

    public static void dropInventoryItems(World world, BlockPos pos, ICapabilityProvider t, int beginSlot, int endSlot) {
        if(t == null)
            return;
        if(!t.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
            return;
        IItemHandler inventory = t.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        for (int i = beginSlot; i <= endSlot; ++i)
        {
            ItemStack itemstack = inventory.getStackInSlot(i);

            if (!itemstack.isEmpty())
            {
                spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), itemstack);
            }
        }
    }

    /**
     * DO NOT ADD 0.5 to x, y, z if you are using this with a BlockPos!
     */
	public static void spawnItemStack(World worldIn, double x, double y, double z, ItemStack stack)
    {
        float xOffset = RANDOM.nextFloat() * 0.8F + 0.1F;
        float yOffset = RANDOM.nextFloat() * 0.8F + 0.1F;
        float zOffset = RANDOM.nextFloat() * 0.8F + 0.1F;

        while (!stack.isEmpty())
        {
            EntityItem entityitem = new EntityItem(worldIn, x + (double)xOffset, y + (double)yOffset, z + (double)zOffset, stack.splitStack(RANDOM.nextInt(21) + 10));
            entityitem.motionX = RANDOM.nextGaussian() * 0.05F;
            entityitem.motionY = RANDOM.nextGaussian() * 0.05F + 0.2F;
            entityitem.motionZ = RANDOM.nextGaussian() * 0.05F;
            worldIn.spawnEntity(entityitem);
        }
    }
}
