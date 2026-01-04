package com.hbm.items.special;

import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.util.ItemStackUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemKitNBT extends Item {
    public ItemKitNBT(String s) {
        this.setMaxStackSize(1);
        this.setRegistryName(s);
        this.setTranslationKey(s);

        ModItems.ALL_ITEMS.add(this);
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World worldIn, EntityPlayer playerIn, @NotNull EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        ItemStack[] stacks = ItemStackUtil.readStacksFromNBT(stack);

        if(stacks != null) {

            for(ItemStack item : stacks) {
                if(item != null && !item.isEmpty()) {
                    playerIn.inventory.addItemStackToInventory(item.copy());
                }
            }
        }

        ItemStack container = stack.getItem().getContainerItem(stack);

        stack.shrink(1);

        if(!container.isEmpty()) {

            if(stack.getCount() > 0) {
                playerIn.inventory.addItemStackToInventory(container.copy());
            } else {
                stack = container.copy();
            }
        }

        worldIn.playSound(playerIn, playerIn.getPosition(), HBMSoundHandler.itemUnpack, SoundCategory.PLAYERS, 1.0F, 1.0F);

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> list, @NotNull ITooltipFlag flagIn) {
        ItemStack[] stacks = ItemStackUtil.readStacksFromNBT(stack);

        if(stacks != null) {

            list.add("Contains:");

            for(ItemStack item : stacks) {
                list.add("-" + item.getDisplayName() + (item.getCount() > 1 ? (" x" + item.getCount()) : ""));
            }
        }
    }

    public static ItemStack create(ItemStack... contents) {
        ItemStack stack = new ItemStack(ModItems.legacy_toolbox);
        stack.setTagCompound(new NBTTagCompound());
        ItemStackUtil.addStacksToNBT(stack, contents);

        return stack;
    }
}
