package com.hbm.items.machine;

import com.hbm.items.ItemEnumMulti;
import com.hbm.util.EnumUtil;
import com.hbm.util.Function;
import com.hbm.util.Function.FunctionLogarithmic;
import com.hbm.util.Function.FunctionSqrt;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemPWRFuel extends ItemEnumMulti<ItemPWRFuel.EnumPWRFuel> {

    public ItemPWRFuel() {
        super("pwr_fuel", EnumPWRFuel.VALUES, true, true);
    }

    public enum EnumPWRFuel {
        MEU(		05.0D,	new FunctionLogarithmic(20 * 30).withDiv(2_500)),
        HEU233(		07.5D,	new FunctionSqrt(25)),
        HEU235(		07.5D,	new FunctionSqrt(22.5)),
        MEN(		07.5D,	new FunctionLogarithmic(22.5 * 30).withDiv(2_500)),
        HEN237(		07.5D,	new FunctionSqrt(27.5)),
        MOX(		07.5D,	new FunctionLogarithmic(20 * 30).withDiv(2_500)),
        MEP(		07.5D,	new FunctionLogarithmic(22.5 * 30).withDiv(2_500)),
        HEP239(		10.0D,	new FunctionSqrt(22.5)),
        HEP241(		10.0D,	new FunctionSqrt(25)),
        MEA(		07.5D,	new FunctionLogarithmic(25 * 30).withDiv(2_500)),
        HEA242(		10.0D,	new FunctionSqrt(25)),
        HES326(		12.5D,	new FunctionSqrt(27.5)),
        HES327(		12.5D,	new FunctionSqrt(30)),
        BFB_AM_MIX(	2.5D,	new FunctionSqrt(15), 250_000_000),
        BFB_PU241(	2.5D,	new FunctionSqrt(15), 250_000_000);

        public static final EnumPWRFuel[] VALUES = values();

        public final double yield;
        public final double heatEmission;
        public final Function function;

        EnumPWRFuel(double heatEmission, Function function, double yield) {
            this.heatEmission = heatEmission;
            this.function = function;
            this.yield = yield;
        }

        EnumPWRFuel(double heatEmission, Function function) {
            this(heatEmission, function, 1_000_000_000);
        }
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {

        EnumPWRFuel num = EnumUtil.grabEnumSafely(EnumPWRFuel.VALUES, stack.getItemDamage());

        String color = TextFormatting.GOLD + "";
        String reset = TextFormatting.RESET + "";

        tooltip.add(color + "Heat per flux: " + reset + num.heatEmission + " TU");
        tooltip.add(color + "Reaction function: " + reset + num.function.getLabelForFuel());
        tooltip.add(color + "Fuel type: " + reset + num.function.getDangerFromFuel());
    }
}