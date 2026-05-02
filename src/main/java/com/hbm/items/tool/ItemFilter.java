package com.hbm.items.tool;


import com.hbm.api.item.IGasMask;
import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.ArmorUtil;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemFilter extends Item {
	
	public ItemFilter(String s, int durability) {
		this.setMaxDamage(durability);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack helmet = player.inventory.armorInventory.get(3);
		ItemStack stack = player.getHeldItem(hand);
		if(helmet == null || helmet.isEmpty())
			return ActionResult.<ItemStack> newResult(EnumActionResult.PASS, player.getHeldItem(hand));

		if(!(helmet.getItem() instanceof IGasMask)) {

			if(ArmorModHandler.hasMods(helmet)) {
				ItemStack[] mods = ArmorModHandler.pryMods(helmet);

				if(mods[ArmorModHandler.helmet_only] != null) {
					ItemStack mask = mods[ArmorModHandler.helmet_only];

					ItemStack ret = installFilterOn(mask, stack, world, player);
					ArmorModHandler.applyMod(helmet, mask);
					return ActionResult.<ItemStack> newResult(EnumActionResult.SUCCESS, ret);
				}
			}
		}

		return ActionResult.<ItemStack> newResult(EnumActionResult.SUCCESS, installFilterOn(helmet, stack, world, player));
	}
	
	private ItemStack installFilterOn(ItemStack helmet, ItemStack filter, World world, EntityPlayer player) {
		
		if(!(helmet.getItem() instanceof IGasMask)) {
			return filter;
		}
		
		IGasMask mask = (IGasMask) helmet.getItem();
		if(!mask.isFilterApplicable(helmet, filter))
			return filter;

		ItemStack copy = filter.copy();
		copy.setCount(1);
		ItemStack current = ArmorUtil.getGasMaskFilter(helmet);

		filter.shrink(1);
		if(filter.isEmpty()) {
			filter = current;
		} else if(!current.isEmpty()) {
			if(!player.inventory.addItemStackToInventory(current)) {
				player.dropItem(current, true, false);
			}
		}
		
		ArmorUtil.installGasMaskFilter(helmet, copy);
		
		world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.gasmaskScrew, SoundCategory.PLAYERS, 1.0F, 1.0F);
				
		return filter;
	}
}
