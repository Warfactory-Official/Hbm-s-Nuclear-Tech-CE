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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Level;

import java.util.List;

public class ItemMultiDetonator extends Item {

	public ItemMultiDetonator(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.controlTab);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		list.add(I18nUtil.resolveKey("desc.callmultdet1"));
		list.add(I18nUtil.resolveKey("desc.calldet2"));
		list.add(I18nUtil.resolveKey("desc.callmultdet2"));
		
		if(stack.getTagCompound() == null || getLocations(stack) == null)
		{
			list.add("§e"+I18nUtil.resolveKey("chat.posnoset")+"§r");
		} else {
			
			int[][] locs = getLocations(stack);
			
			for(int i = 0; i < locs[0].length; i++) {
				list.add("§a" + I18nUtil.resolveKey("chat.possetaxyz", (i+1), locs[0][i], locs[1][i], locs[2][i]));
			}
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
			addLocation(stack, pos.getX(), pos.getY(), pos.getZ());
			
			if(world.isRemote)
			{
				player.sendMessage(new TextComponentString("§a["+I18nUtil.resolveKey("chat.posadd")+"]§r"));
			}
			
	        world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBoop, SoundCategory.AMBIENT, 2.0F, 1.0F);
        	
			return EnumActionResult.SUCCESS;
		}
		
		return EnumActionResult.PASS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if(stack.getTagCompound() == null || getLocations(stack) == null)
		{
			if(!world.isRemote)
				player.sendMessage(new TextComponentString("§c"+I18nUtil.resolveKey("chat.posnoseterror")+"§r"));
			
		} else {
			
			if(!player.isSneaking()) {
				int[][] locs = getLocations(stack);
				
				int succ = 0;
				
				for (int i = 0; i < locs[0].length; i++) {
	
					int x = locs[0][i];
					int y = locs[1][i];
					int z = locs[2][i];
					BlockPos pos = new BlockPos(x, y, z);
					if (world.getBlockState(pos).getBlock() instanceof IBomb) {
						world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBleep, SoundCategory.AMBIENT, 1.0F, 1.0F);
						if (!world.isRemote) {
							((IBomb) world.getBlockState(pos).getBlock()).explode(world, pos, player);

				    		if(GeneralConfig.enableExtendedLogging)
				    			MainRegistry.logger.log(Level.INFO, "[DET] Tried to detonate block at " + x + " / " + y + " / " + z + " by " + player.getDisplayName() + "!");
						succ++;
						}
					}
				}
				
				if (!world.isRemote) {
					player.sendMessage(new TextComponentString("§2"+I18nUtil.resolveKey("chat.detonatedmulti", succ, locs[0].length)));
				}
			} else {

				stack.getTagCompound().setIntArray("xValues", new int[0]);
				stack.getTagCompound().setIntArray("yValues", new int[0]);
				stack.getTagCompound().setIntArray("zValues", new int[0]);
				
		        world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBoop, SoundCategory.AMBIENT, 2.0F, 1.0F);
				
				if(!world.isRemote)
				{
					player.sendMessage(new TextComponentString("§e"+I18nUtil.resolveKey("chat.removdedallpos")));
				}
			}
		}
		
		return super.onItemRightClick(world, player, hand);
	}
	
	private static void addLocation(ItemStack stack, int x, int y, int z){
		if(stack.getTagCompound() == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}

		int[] xs = stack.getTagCompound().getIntArray("xValues");
		int[] ys = stack.getTagCompound().getIntArray("yValues");
		int[] zs = stack.getTagCompound().getIntArray("zValues");
		
		stack.getTagCompound().setIntArray("xValues", ArrayUtils.add(xs, x));
		stack.getTagCompound().setIntArray("yValues", ArrayUtils.add(ys, y));
		stack.getTagCompound().setIntArray("zValues", ArrayUtils.add(zs, z));
	}
	
	public static int[][] getLocations(ItemStack stack){
		if(!stack.hasTagCompound())
			return null;
		int[] xs = stack.getTagCompound().getIntArray("xValues");
		int[] ys = stack.getTagCompound().getIntArray("yValues");
		int[] zs = stack.getTagCompound().getIntArray("zValues");

		if(xs == null || ys == null || zs == null || xs.length == 0 || ys.length == 0 || zs.length == 0) {
			return null;
		}
		
		return new int[][] { xs, ys, zs };
	}
}
