package com.hbm.items.tool;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.util.Vec3NT;
import com.hbm.tileentity.machine.TileEntitySolarMirror;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemMirrorTool extends Item {

	public ItemMirrorTool(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos1, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		Block b = world.getBlockState(pos1).getBlock();

		if(b == ModBlocks.machine_solar_boiler) {

			int[] pos = ((BlockDummyable)b).findCore(world, pos1.getX(), pos1.getY(), pos1.getZ());

			if(pos != null && !world.isRemote) {

				if(!stack.hasTagCompound())
					stack.setTagCompound(new NBTTagCompound());

				stack.getTagCompound().setInteger("posX", pos[0]);
				stack.getTagCompound().setInteger("posY", pos[1] + 1);
				stack.getTagCompound().setInteger("posZ", pos[2]);

				player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".linked").setStyle(new Style().setColor(TextFormatting.YELLOW)));
			}

			return EnumActionResult.SUCCESS;
		}

		if(b == ModBlocks.solar_mirror && stack.hasTagCompound()) {

			if(!world.isRemote) {
				TileEntitySolarMirror mirror = (TileEntitySolarMirror)world.getTileEntity(pos1);
				int tx = stack.getTagCompound().getInteger("posX");
				int ty = stack.getTagCompound().getInteger("posY");
				int tz = stack.getTagCompound().getInteger("posZ");

				int x = pos1.getX();
				int y = pos1.getY();
				int z = pos1.getZ();

				boolean withinReach = Vec3NT.createVectorHelper(x - tx, y - ty, z - tz).length() <= 100;
				boolean withinAngle = (x - tx) * (x - tx) + (z - tz) * (z - tz) <= (y - ty) * (y - ty);

				if(!withinReach) player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".reach").setStyle(new Style().setColor(TextFormatting.RED)));
				else if(!withinAngle) player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".angle").setStyle(new Style().setColor(TextFormatting.RED)));
				else mirror.setTarget(tx, ty, tz);
			}

			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		for(String s : I18nUtil.resolveKeyArray(this.getTranslationKey() + ".desc"))
			tooltip.add(TextFormatting.YELLOW + s);
	}
}
