package com.hbm.items.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ItemRBMKLid extends Item {

	public ItemRBMKLid(String s){
		this.setTranslationKey(s);
		this.setRegistryName(s);
		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public @NotNull EnumActionResult onItemUse(@NotNull EntityPlayer player, World world, @NotNull BlockPos bpos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ){
		Block b = world.getBlockState(bpos).getBlock();

		if(!world.isRemote && b instanceof RBMKBase rbmk) {
            int[] pos = rbmk.findCore(world, bpos.getX(), bpos.getY(), bpos.getZ());
			if(pos == null) return EnumActionResult.FAIL;

			BlockPos corePos = new BlockPos(pos[0], pos[1], pos[2]);
			TileEntity te = world.getTileEntity(corePos);
			if(!(te instanceof TileEntityRBMKBase tile)) return EnumActionResult.FAIL;

			IBlockState coreState = world.getBlockState(corePos);
			int currentMeta = coreState.getBlock().getMetaFromState(coreState);
			if (RBMKBase.metaToLid(currentMeta) != RBMKBase.LID_NONE) return EnumActionResult.FAIL;

			int metaOffset = RBMKBase.DIR_NORMAL_LID.ordinal();
			if(this == ModItems.rbmk_lid_glass) {
				metaOffset = RBMKBase.DIR_GLASS_LID.ordinal();
				world.playSound(null, bpos.getX() + 0.5, bpos.getY() + 0.5, bpos.getZ() + 0.5, SoundEvents.BLOCK_GLASS_PLACE, SoundCategory.BLOCKS, 1, 0.8F);
			} else {
				world.playSound(null, bpos.getX() + 0.5, bpos.getY() + 0.5, bpos.getZ() + 0.5, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 0.8F);
			}
			NBTTagCompound nbt = tile.writeToNBT(new NBTTagCompound());

			IBlockState newState = b.getStateFromMeta(metaOffset + RBMKBase.offset);
			world.setBlockState(new BlockPos(pos[0], pos[1], pos[2]), newState, 3);

			TileEntity newTe = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
			if (newTe != null) {
				newTe.readFromNBT(nbt);
				newTe.markDirty();
				world.notifyBlockUpdate(new BlockPos(pos[0], pos[1], pos[2]), newState, newState, 3);
			}

			player.getHeldItem(hand).shrink(1);

			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}
}
