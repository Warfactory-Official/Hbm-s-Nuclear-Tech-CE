package com.hbm.items;

import com.hbm.render.anim.BusAnimation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IAnimatedItem {

    @SideOnly(Side.CLIENT)
    BusAnimation getAnimation(NBTTagCompound data, ItemStack stack);
}
