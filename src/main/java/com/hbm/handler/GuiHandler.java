package com.hbm.handler;

import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.EnumUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

		if(y >= 0) {
			BlockPos pos = new BlockPos(x, y, z);
			TileEntity entity = world.getTileEntity(pos);
			if(entity instanceof IGUIProvider) {
				return ((IGUIProvider) entity).provideContainer(ID, player, world, x, y, z);
			}

			Block b = world.getBlockState(pos).getBlock();
			
			if(b instanceof IGUIProvider) {
				return ((IGUIProvider) b).provideContainer(ID, player, world, x, y, z);
			}
		}
		
		ItemStack item = getItemGuiStack(player, x, y, z);
		
		if(!item.isEmpty() && item.getItem() instanceof IGUIProvider) {
			return ((IGUIProvider) item.getItem()).provideContainer(ID, player, world, x, y, z);
		}
		
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

		if(y >= 0) {
			BlockPos pos = new BlockPos(x, y, z);
			TileEntity entity = world.getTileEntity(pos);

			if(entity instanceof IGUIProvider) {
				return ((IGUIProvider) entity).provideGUI(ID, player, world, x, y, z);
			}
			
			Block b = world.getBlockState(pos).getBlock();
			
			if(b instanceof IGUIProvider) {
				return ((IGUIProvider) b).provideGUI(ID, player, world, x, y, z);
			}
		}
		
		ItemStack item = getItemGuiStack(player, x, y, z);
		
		if(!item.isEmpty() && item.getItem() instanceof IGUIProvider) {
			return ((IGUIProvider) item.getItem()).provideGUI(ID, player, world, x, y, z);
		}
		
		return null;
	}

	private static ItemStack getItemGuiStack(EntityPlayer player, int x, int y, int z) {
		if(y < 0 && x >= 0 && x < EnumUtil.HANDS.length) {
			ItemStack held = player.getHeldItem(EnumUtil.HANDS[x]);
			if(!held.isEmpty() && held.getItem() instanceof IGUIProvider) return held;
		}

		ItemStack main = player.getHeldItemMainhand();
		if(!main.isEmpty() && main.getItem() instanceof IGUIProvider) return main;

		ItemStack off = player.getHeldItemOffhand();
		if(!off.isEmpty() && off.getItem() instanceof IGUIProvider) return off;

		return main;
	}

}
