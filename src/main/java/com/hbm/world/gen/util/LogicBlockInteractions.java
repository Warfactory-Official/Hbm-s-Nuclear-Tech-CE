package com.hbm.world.gen.util;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.blocks.generic.LogicBlock;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.potion.HbmPotion;
import com.hbm.tileentity.machine.TileEntityLockableBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

/**Interactions are called when the player right-clicks the block**/
public class LogicBlockInteractions {

	/**Consumer consists of world instance, tile entity instance, three ints for coordinates, one int for block side, and player instance,
	 * in that order **/
	public static LinkedHashMap<String, Consumer<Object[]>> interactions;

	public static Consumer<Object[]> TEST = (array) -> {
		LogicBlock.TileEntityLogicBlock logic = (LogicBlock.TileEntityLogicBlock) array[1];
		EntityPlayer player = (EntityPlayer) array[5];

		if(logic.phase > 1) return;

		if(!player.getHeldItemMainhand().isEmpty())
			player.getHeldItemMainhand().shrink(1);

		logic.phase++;
	};

	public static Consumer<Object[]> RAD_CONTAINMENT_SYSTEM = (array) -> {
		LogicBlock.TileEntityLogicBlock logic = (LogicBlock.TileEntityLogicBlock) array[1];
		EntityPlayer player = (EntityPlayer) array[5];

		if(!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() == ModItems.key){
			player.getHeldItemMainhand().shrink(1);
			player.sendMessage(new TextComponentString(
					TextFormatting.LIGHT_PURPLE + "[RAD CONTAINMENT SYSTEM]" +
							TextFormatting.RESET + " Radiation treatment administered"));
			player.addPotionEffect(new PotionEffect(HbmPotion.radaway, 3 * 60 * 20, 4));
			player.addPotionEffect(new PotionEffect(HbmPotion.radx, 3 * 60 * 20, 4));
			logic.phase = 2;
			logic.timer = 0;
		}
	};



	public static Consumer<Object[]> POWER_LOCK = (array) -> {
		World world = (World) array[0];
		EntityPlayer player = (EntityPlayer) array[5];
		int x = (int) array[2];
		int y = (int) array[3];
		int z = (int) array[4];
		BlockPos pos = new BlockPos(x, y, z);

		IEnergyHandlerMK2 handler = null;
		for (EnumFacing dir : EnumFacing.VALUES) {
			TileEntity te = world.getTileEntity(pos.offset(dir));
			if (te instanceof IEnergyHandlerMK2) {
				handler = (IEnergyHandlerMK2) te;
				break;
			}
		}

		if (handler == null || !(handler.getPower() > 500_000)) {
			player.sendMessage(new TextComponentString(
					TextFormatting.LIGHT_PURPLE + "[POWER LOCK]" +
							TextFormatting.RESET + " Charge adjacent energy storage to at least 500KHE to release emergency lock"));
		} else {
			player.sendMessage(new TextComponentString(
					TextFormatting.LIGHT_PURPLE + "[POWER LOCK]" +
							TextFormatting.RESET + " Power Restorted! Safe Unlocked!"));

			TileEntityLockableBase safe = null;
			for (EnumFacing dir : EnumFacing.VALUES) {
				TileEntity te = world.getTileEntity(pos.offset(dir));
				if (te instanceof TileEntityLockableBase) {
					safe = (TileEntityLockableBase) te;
					break;
				}
			}
			if (safe != null) {
				safe.unlock();
				world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.lockOpen, SoundCategory.BLOCKS, 3.0F, 0.8F);
			}
		}
	};

	public static List<String> getInteractionNames(){
		return new ArrayList<>(interactions.keySet());
	}

	//register new interactions here
	static{
		initialize();
	}
	public static void initialize() {
		interactions = new LinkedHashMap<>();

		interactions.put("POWER_LOCK", POWER_LOCK);

		//example interactions
		interactions.put("TEST", TEST);
		interactions.put("RADAWAY_INJECTOR", RAD_CONTAINMENT_SYSTEM);
	}



}
