package com.hbm.items.machine;

import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ItemTurretBiometry extends Item {

	public ItemTurretBiometry(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.weaponTab);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		String[] names = getNames(stack);
		if(names != null)
            Collections.addAll(tooltip, names);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
		ItemStack stack = player.getHeldItem(handIn);
		addName(stack, player.getDisplayName().getUnformattedText());

        if(world.isRemote)
        	player.sendMessage(new TextComponentTranslation("chat.addpldata"));

    	world.playSound(player.posX, player.posY, player.posZ, HBMSoundHandler.techBleep, SoundCategory.PLAYERS, 1.0F, 1.0F, true);
		
		player.swingArm(handIn);;
		
		return super.onItemRightClick(world, player, handIn);
	}
	
	public static String[] getNames(ItemStack stack) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
			return null;
		}
		
		String[] names = new String [stack.getTagCompound().getInteger("playercount")];
		
		for(int i = 0; i < names.length; i++) {
			names[i] = stack.getTagCompound().getString("player_" + i);
		}
		
		if(names.length == 0)
			return null;
		
		return names;
	}
	
	public static void addName(ItemStack stack, String s) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}
		
		String[] names = getNames(stack);
		int count = 0;
		
		if(names != null && Arrays.asList(names).contains(s))
			return;
		
		if(names != null)
			count = names.length;
		
		stack.getTagCompound().setInteger("playercount", count + 1);
		
		stack.getTagCompound().setString("player_" + count, s);
	}
	
	public static void clearNames(ItemStack stack) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}
		
		stack.getTagCompound().setInteger("playercount", 0);
	}
}
