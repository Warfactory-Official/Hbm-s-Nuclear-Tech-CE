package com.hbm.items.machine;

import com.hbm.items.ItemEnumMulti;
import com.hbm.util.EnumUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemArcElectrode extends ItemEnumMulti<ItemArcElectrode.EnumElectrodeType> {

    public ItemArcElectrode(String s) {
        super(s, EnumElectrodeType.VALUES, true, true);
        this.setFull3D();
        this.setMaxStackSize(1);
    }

    public static int getDurability(ItemStack stack) {
        if(!stack.hasTagCompound()) return 0;
        return stack.getTagCompound().getInteger("durability");
    }

    public static boolean damage(ItemStack stack) {
        if(!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        int durability = stack.getTagCompound().getInteger("durability");
        durability++;
        stack.getTagCompound().setInteger("durability", durability);
        return durability >= getMaxDurability(stack);
    }

    public static int getMaxDurability(ItemStack stack) {
        EnumElectrodeType num = EnumUtil.grabEnumSafely(EnumElectrodeType.VALUES, stack.getItemDamage());
        return num.durability;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getDurabilityForDisplay(stack) > 0D;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return (double) getDurability(stack) / (double) getMaxDurability(stack);
    }

    public static enum EnumElectrodeType {
        GRAPHITE(	10),
        LANTHANIUM(	100),
        DESH(		500),
        SATURNITE(	1500);

        public static final EnumElectrodeType[] VALUES = values();

        public int durability;

        private EnumElectrodeType(int dura) {
            this.durability = dura;
        }
    }
}
