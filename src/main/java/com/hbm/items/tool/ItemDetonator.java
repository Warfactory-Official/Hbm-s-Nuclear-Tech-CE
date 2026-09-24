package com.hbm.items.tool;

import com.hbm.config.GeneralConfig;
import com.hbm.interfaces.IBomb;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import java.util.List;

public class ItemDetonator extends Item {

	public ItemDetonator(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.controlTab);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		list.add(I18nUtil.resolveKey("desc.calldet1"));
		list.add(I18nUtil.resolveKey("desc.calldet2"));
		if(stack.getTagCompound() == null)
		{
			list.add("§e"+I18nUtil.resolveKey("chat.posnoset")+"§r");
		} else {
			list.add("§a" + I18nUtil.resolveKey("chat.possetxyz", stack.getTagCompound().getInteger("x"), stack.getTagCompound().getInteger("y"), stack.getTagCompound().getInteger("z"))+"§r");
		}
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if(stack.getTagCompound() == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}
		
		if(player.isSneaking())
		{
			stack.getTagCompound().setInteger("x", pos.getX());
			stack.getTagCompound().setInteger("y", pos.getY());
			stack.getTagCompound().setInteger("z", pos.getZ());
			
			if(world.isRemote)
			{
				player.sendMessage(new TextComponentTranslation("§2[" + I18nUtil.resolveKey("chat.posset") + "]"+"§r"));
			}
			
	        world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBoop, SoundCategory.AMBIENT, 1.0F, 1.0F);
        	
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
		ItemStack stack = player.getHeldItem(handIn);
		if(stack.getTagCompound() == null)
		{
			if(!world.isRemote)
				player.sendMessage(new TextComponentTranslation("§c"+I18nUtil.resolveKey("chat.posnoseterror")+"§r"));
		} else {
			 int x = stack.getTagCompound().getInteger("x");
			 int y = stack.getTagCompound().getInteger("y");
			 int z = stack.getTagCompound().getInteger("z");
			 BlockPos pos = new BlockPos(x, y, z);
			 if(world.getBlockState(pos).getBlock() instanceof IBomb)
			 {
				world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBleep, SoundCategory.AMBIENT, 1.0F, 1.0F);

				if(!world.isRemote)
				{
					((IBomb)world.getBlockState(pos).getBlock()).explode(world, pos, player);
		    		if(GeneralConfig.enableExtendedLogging)
		    			MainRegistry.logger.log(Level.INFO, "[DET] Tried to detonate block at " + x + " / " + y + " / " + z + " by " + player.getDisplayName() + "!");
		    		player.sendMessage(new TextComponentTranslation("§2["+I18nUtil.resolveKey("chat.detonated")+"]"+"§r"));
				}
			 } else {
				if(!world.isRemote)
				{
					player.sendMessage(new TextComponentTranslation("§c"+I18nUtil.resolveKey("chat.postoofarerror")+"§r"));
				}
			 }
		}
		return super.onItemRightClick(world, player, handIn);
	}
}
