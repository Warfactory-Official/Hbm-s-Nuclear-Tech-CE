package com.hbm.items.tool;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.util.ContaminationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

import java.util.ArrayList;
import java.util.List;

@Optional.InterfaceList({@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")})
public class ItemGeigerCounter extends Item implements IBauble {
	
	public ItemGeigerCounter(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {

		if(!(entity instanceof EntityLivingBase) || world.isRemote)
			return;
		
		if(entity instanceof EntityPlayer) {
			
			if(ArmorFSB.hasFSBArmor((EntityPlayer)entity) && ((ArmorFSB)((EntityPlayer)entity).inventory.armorInventory.get(2).getItem()).geigerSound)
				return;
			
			playGeiger(world, (EntityPlayer)entity);
		}
	}

	public static void playGeiger(World world, EntityPlayer player){
        if (world.isRemote) return;
		double x = ContaminationUtil.getActualPlayerRads(player);
		
		if(world.getTotalWorldTime() % 5 == 0) {

			if(x > 1e-5) {
				List<Integer> list = new ArrayList<>();
                if(x < 1) list.add(0);
                if(x < 5) list.add(0);
                if(x < 10) list.add(1);
                if(x > 5 && x < 15) list.add(2);
                if(x > 10 && x < 20) list.add(3);
                if(x > 15 && x < 25) list.add(4);
                if(x > 20 && x < 30) list.add(5);
                if(x > 25) list.add(6);
                int r = list.get(world.rand.nextInt(list.size()));

                if(r > 0) world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.geigerSounds[r - 1], SoundCategory.PLAYERS, 1.0F, 1.0F);
            } else if(world.rand.nextInt(100) == 0) {
				world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.geigerSounds[(world.rand.nextInt(1))], SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
		}
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.getBlockState(pos).getBlock() == ModBlocks.block_red_copper) {
    		Library.consumeInventoryItem(player.inventory, ModItems.geiger_counter);
    		player.inventory.addItemStackToInventory(new ItemStack(ModItems.survey_scanner));
    		return EnumActionResult.SUCCESS;
    	}
    	
    	return EnumActionResult.PASS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
		if(!world.isRemote) {
	    	world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBoop, SoundCategory.PLAYERS, 1.0F, 1.0F);

	    	ContaminationUtil.printGeigerData(player);
		}
		
		return super.onItemRightClick(world, player, handIn);
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return !ItemStack.areItemsEqual(oldStack, newStack);
	}

	@Override
	public BaubleType getBaubleType(ItemStack itemstack){
		return BaubleType.TRINKET;
	}

	@Override
	public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
		onUpdate(itemstack, player.world, player, 0, true);
	}
}
