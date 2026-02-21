package com.hbm.items.tool;

import com.hbm.handler.BossSpawnHandler;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;

//mlbv: useful for testing, please don't remove it
public class ItemMeteorRemote extends Item {

	public ItemMeteorRemote(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.canRepair = false;
		this.setMaxDamage(2);
		
		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return enchantment != Enchantments.UNBREAKING && enchantment != Enchantments.MENDING && super.canApplyAtEnchantingTable(stack, enchantment);
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		if (book.getItem() == Items.ENCHANTED_BOOK) {
			Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(book);
			if (enchantments.containsKey(Enchantments.MENDING) || enchantments.containsKey(Enchantments.UNBREAKING)) {
				return false;
			}
		}
		return super.isBookEnchantable(stack, book);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(I18nUtil.resolveKey("item.meteor_remote.desc"));
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		stack.damageItem(1, player);

		if(!world.isRemote) {
            BossSpawnHandler.spawnMeteorAtPlayer(player, false);
		} else {
			player.sendMessage(new TextComponentTranslation("chat.meteorremote.watchhead"));
		}

    	world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBleep, SoundCategory.PLAYERS, 1.0F, 1.0F);
		
		player.swingArm(hand);
		
		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}
}
