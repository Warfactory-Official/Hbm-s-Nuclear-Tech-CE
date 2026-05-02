package com.hbm.items.tool;

import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.TileEntityLockableBase;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ItemCounterfeitKeys extends Item {

	public ItemCounterfeitKeys(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(pos);

		if(te instanceof TileEntityLockableBase) {

			TileEntityLockableBase locked = (TileEntityLockableBase) te;

			if(locked.isLocked()) {
				ItemStack st = new ItemStack(ModItems.key_fake);
				ItemKeyPin.setPins(st, locked.getPins());

				player.inventory.setInventorySlotContents(player.inventory.currentItem, st.copy());

				if(!player.inventory.addItemStackToInventory(st.copy())) {
					player.dropItem(st.copy(), false);
				}

				player.inventoryContainer.detectAndSendChanges();

				player.swingArm(hand);

				return EnumActionResult.SUCCESS;
			}
		}

		return EnumActionResult.PASS;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add("Use on a locked container to create two counterfeit keys!");
	}
}
