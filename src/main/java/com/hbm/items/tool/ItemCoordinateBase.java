package com.hbm.items.tool;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public abstract class ItemCoordinateBase extends Item {
    public static BlockPos getPosition(ItemStack stack) {

        if(stack.hasTagCompound()) {
            return new BlockPos(stack.getTagCompound().getInteger("posX"), stack.getTagCompound().getInteger("posY"), stack.getTagCompound().getInteger("posZ"));
        }

        return null;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if(this.canGrabCoordinateHere(worldIn, pos)) {
            if(!worldIn.isRemote) {
                BlockPos pos1 = this.getCoordinates(worldIn, pos);
                ItemStack stack = player.getHeldItem(hand);

                if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
                stack.getTagCompound().setInteger("posX", pos1.getX());
                if(includeY()) stack.getTagCompound().setInteger("posY", pos1.getY());
                stack.getTagCompound().setInteger("posZ", pos1.getZ());

                this.onTargetSet(worldIn, pos1, player);
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }

    /** Whether this position can be saved or if the position target is valid */
    public abstract boolean canGrabCoordinateHere(World world, BlockPos pos);

    /** Whether this linking item saves the Y coordinate */
    public boolean includeY() {
        return true;
    }

    /** Modified the saved coordinates, for example detecting the core for multiblocks */
    public BlockPos getCoordinates(World world, BlockPos pos) {
        return pos;
    }

    /** Extra on successful target set, eg. sounds */
    public void onTargetSet(World world, BlockPos pos, EntityPlayer player) { }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn) {

        if(stack.hasTagCompound()) {
            list.add("X: " + stack.getTagCompound().getInteger("posX"));
            if(includeY()) list.add("Y: " + stack.getTagCompound().getInteger("posY"));
            list.add("Z: " + stack.getTagCompound().getInteger("posZ"));
        } else {
            list.add(TextFormatting.RED + "No position set!");
        }
    }
}
