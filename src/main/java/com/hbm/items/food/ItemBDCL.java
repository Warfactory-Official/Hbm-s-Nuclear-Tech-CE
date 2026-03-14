package com.hbm.items.food;

import com.hbm.items.ItemBakedBase;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemBDCL extends ItemBakedBase {

    public ItemBDCL(String s){
        super(s);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 40;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.DRINK;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        playerIn.setActiveHand(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {

        if(entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityLiving;
            if(!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }
        return stack;
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase entity, int count) {

        if(!(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;

        if(count % 5 == 0 && count >= 10) {
            player.world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.gulp, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }

        if(count == 1) {
            this.onItemUseFinish(stack, player.world, player);
            player.stopActiveHand();
            player.resetActiveHand();
            player.playSound(HBMSoundHandler.groan, 1.0F, 1.0F);
            return;
        }

        if(count <= 24 && count % 4 == 0) {
            player.getActiveItemStack().shrink(1);
        }
    }
}
