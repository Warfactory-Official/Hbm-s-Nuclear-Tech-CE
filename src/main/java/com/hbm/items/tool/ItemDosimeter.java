package com.hbm.items.tool;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.hbm.items.ModItems;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.util.ContaminationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Optional.InterfaceList({@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")})
public class ItemDosimeter extends Item implements IBauble {
	
	Random rand = new Random();

	public ItemDosimeter(String s){
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean bool) {
		
		if(!(entity instanceof EntityLivingBase) || world.isRemote)
			return;
		
		if(entity instanceof EntityPlayer) {
			
			if(ArmorFSB.hasFSBArmor((EntityPlayer)entity) && ((ArmorFSB)((EntityPlayer)entity).inventory.armorInventory.get(2).getItem()).geigerSound)
				return;
			
			double x = ContaminationUtil.getActualPlayerRads((EntityPlayer)entity);
			
			if(world.getTotalWorldTime() % 5 == 0) {
				
				if(x > 1e-5) {
					List<Integer> list = new ArrayList<>();

					if(x < 0.5)
						list.add(0);
					if(x < 1)
						list.add(1);
					if(x >= 0.5 && x < 2)
						list.add(2);
					if(x >= 1 && x >= 2)
						list.add(3);
				
					int r = list.get(rand.nextInt(list.size()));
					
					if(r > 0)
						world.playSound(null, entity.posX, entity.posY, entity.posZ, HBMSoundHandler.geigerSounds[r-1], SoundCategory.PLAYERS, 1.0F, 1.0F);
					
				} else if(rand.nextInt(100) == 0) {
					world.playSound(null, entity.posX, entity.posY, entity.posZ, HBMSoundHandler.geigerSounds[(rand.nextInt(1))], SoundCategory.PLAYERS, 1.0F, 1.0F);
				}
			}
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
		
		if(!world.isRemote) {
			world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBoop, SoundCategory.PLAYERS, 1.0F, 1.0F);
			ContaminationUtil.printDosimeterData(player);
		}
		
		return super.onItemRightClick(world, player, handIn);
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
