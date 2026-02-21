package com.hbm.items.block;

import com.hbm.interfaces.IBlockSpecialPlacementAABB;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

public class ItemBlockSpecialAABB<T extends Block & IBlockSpecialPlacementAABB> extends ItemBlock {

    public ItemBlockSpecialAABB(T block) {
        super(block);
        this.setHasSubtypes(true);
        this.canRepair = false;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (!block.isReplaceable(worldIn, pos)) {
            pos = pos.offset(facing);
        }

        ItemStack itemstack = player.getHeldItem(hand);
        // noinspection unchecked
        if (!itemstack.isEmpty() && player.canPlayerEdit(pos, facing, itemstack) && mayPlace(worldIn, (T) this.block, pos, false, facing, player, itemstack)) {
            int i = this.getMetadata(itemstack.getMetadata());
            IBlockState iblockstate1 = this.block.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, i, player, hand);

            if (placeBlockAt(itemstack, player, worldIn, pos, facing, hitX, hitY, hitZ, iblockstate1)) {
                iblockstate1 = worldIn.getBlockState(pos);
                SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, worldIn, pos, player);
                worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                itemstack.shrink(1);
            }

            return EnumActionResult.SUCCESS;
        } else {
            return EnumActionResult.FAIL;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
        Block block = worldIn.getBlockState(pos).getBlock();
        if (block == Blocks.SNOW_LAYER && block.isReplaceable(worldIn, pos)) {
            side = EnumFacing.UP;
        } else if (!block.isReplaceable(worldIn, pos)) {
            pos = pos.offset(side);
        }
        // noinspection unchecked
        return mayPlace(worldIn, (T) this.block, pos, false, side, player, stack);
    }

    public boolean mayPlace(World worldIn, T blockIn, BlockPos pos, boolean skipCollisionCheck, EnumFacing sidePlacedOn, @Nullable Entity placer, ItemStack stack) {
        IBlockState oldState = worldIn.getBlockState(pos);
        AxisAlignedBB axisalignedbb = skipCollisionCheck ? null : blockIn.getCollisionBoundingBoxForPlacement(worldIn, pos, stack);
        if (!((placer instanceof EntityPlayer) || !ForgeEventFactory.onBlockPlace(placer, new BlockSnapshot(worldIn, pos, blockIn.getDefaultState()), sidePlacedOn).isCanceled()))
            return false;
        if (axisalignedbb != Block.NULL_AABB && !worldIn.checkNoEntityCollision(axisalignedbb.offset(pos))) {
            return false;
        } else if (oldState.getMaterial() == Material.CIRCUITS && blockIn == Blocks.ANVIL) {
            return true;
        } else {
            return oldState.getBlock().isReplaceable(worldIn, pos) && blockIn.canPlaceBlockOnSide(worldIn, pos, sidePlacedOn);
        }
    }
}
