package com.hbm.items.machine;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.items.ItemEnumMulti;
import com.hbm.main.MainRegistry;
import com.hbm.util.BobMathUtil;
import com.hbm.util.EnumUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemBatterySC extends ItemEnumMulti<ItemBatterySC.EnumBatterySC> implements IBatteryItem {

    public ItemBatterySC(String s) {
        super(s, EnumBatterySC.VALUES, true, true);
        this.setMaxStackSize(1);
        this.setCreativeTab(MainRegistry.controlTab);
    }

    public enum EnumBatterySC {

        EMPTY(	    0),
        WASTE(	  150),
        RA226(	  200),
        TC99(	  500),
        CO60(	  750),
        PU238(	1_000),
        PO210(	1_250),
        AU198(	1_500),
        PB209(	2_000),
        AM241(	2_500);

        public static final EnumBatterySC[] VALUES = values();

        public final long power;

        EnumBatterySC(long power) {
            this.power = power;
        }
    }

    @Override public void chargeBattery(ItemStack stack, long i) { }
    @Override public void setCharge(ItemStack stack, long i) { }
    @Override public void dischargeBattery(ItemStack stack, long i) { }
    @Override public long getChargeRate(ItemStack stack) { return 0; }

    @Override public long getCharge(ItemStack stack) { return getMaxCharge(stack); }
    @Override public long getDischargeRate(ItemStack stack) { return getMaxCharge(stack); }

    @Override
    public long getMaxCharge(ItemStack stack) {
        EnumBatterySC pack = EnumUtil.grabEnumSafely(EnumBatterySC.VALUES, stack.getItemDamage());
        return pack.power;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn) {
        EnumBatterySC pack = EnumUtil.grabEnumSafely(EnumBatterySC.VALUES, stack.getItemDamage());
        if(pack.power > 0) list.add(TextFormatting.YELLOW + "Discharge rate: " + BobMathUtil.getShortNumber(pack.power) + "HE/t");
    }
}
