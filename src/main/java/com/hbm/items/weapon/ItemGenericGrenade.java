package com.hbm.items.weapon;

import com.hbm.entity.grenade.EntityGrenadeBouncyGeneric;
import com.hbm.entity.grenade.EntityGrenadeImpactGeneric;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemGenericGrenade extends ItemGrenade {

    public ItemGenericGrenade(int fuse, String s) {
        super(fuse, s);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);

        if (!playerIn.capabilities.isCreativeMode) {
            stack.shrink(1);
        }

        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        if (!worldIn.isRemote) {
            if (fuse == -1) {
                worldIn.spawnEntity(new EntityGrenadeImpactGeneric(worldIn, playerIn, handIn).setType(this));
            } else {
                worldIn.spawnEntity(new EntityGrenadeBouncyGeneric(worldIn, playerIn, handIn).setType(this));
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public void explode(Entity grenade, EntityLivingBase thrower, World world, double x, double y, double z) { }

    public int getMaxTimer() {
        return this.fuse * 20;
    }

    public double getBounceMod() {
        return 0.5D;
    }
}
