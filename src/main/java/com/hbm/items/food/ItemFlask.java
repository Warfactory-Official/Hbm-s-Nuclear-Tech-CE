package com.hbm.items.food;

import com.hbm.capability.HbmCapability;
import com.hbm.items.ItemEnumMulti;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemFlask extends ItemEnumMulti<ItemFlask.EnumInfusion> {

    public enum EnumInfusion {
        SHIELD;

        public static final EnumInfusion[] VALUES = values();
    }

    public ItemFlask(String s) {
        super(s, EnumInfusion.VALUES, true, true);
    }


    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        if (!(entityLiving instanceof EntityPlayer player)) return stack;
        if (!player.isCreative()) stack.shrink(1);
        if (worldIn.isRemote) return stack;
        if (stack.getItemDamage() == EnumInfusion.SHIELD.ordinal()) {
            float infusion = 5F;
            HbmCapability.IHBMData props = HbmCapability.getData(player);
            props.setMaxShield(Math.min(HbmCapability.IHBMData.shieldCap, props.getMaxShield() + infusion));
            props.setShield(Math.min(props.getShield() + infusion, props.getEffectiveMaxShield(player)));
        }
        return stack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.DRINK;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        if (itemstack.getItemDamage() == EnumInfusion.SHIELD.ordinal() && HbmCapability.getData(playerIn).getMaxShield() >= HbmCapability.IHBMData.shieldCap) {
            return new ActionResult<>(EnumActionResult.FAIL, itemstack);
        }
        playerIn.setActiveHand(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }
}