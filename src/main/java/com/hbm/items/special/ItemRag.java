package com.hbm.items.special;

import com.hbm.items.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ItemRag extends Item {

	public ItemRag(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		ModItems.ALL_ITEMS.add(this);

	}

	@Override
	public boolean onEntityItemUpdate(@NotNull EntityItem entityItem) {
		
		if(!entityItem.getItem().isEmpty() && !entityItem.world.isRemote) {
			
			if(entityItem.isInWater() || entityItem.world.getBlockState(new BlockPos((int)Math.floor(entityItem.posX), (int)Math.floor(entityItem.posY), (int)Math.floor(entityItem.posZ))).getMaterial() == Material.WATER) {
				ItemStack stack = entityItem.getItem();
				if(stack.getItem() == ModItems.rag)
					entityItem.setItem(new ItemStack(ModItems.rag_damp, stack.getCount()));
				else 
					entityItem.setItem(new ItemStack(ModItems.mask_damp, stack.getCount()));
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World world, EntityPlayer player, @NotNull EnumHand hand) {
		
		ItemStack stack = player.getHeldItem(hand);
		if(stack.getItem() == ModItems.rag)
			player.dropItem(new ItemStack(ModItems.rag_piss, 1, 0), false);
		else
			player.dropItem(new ItemStack(ModItems.mask_piss, 1, 0), false);
		stack.shrink(1);
		return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}
}
