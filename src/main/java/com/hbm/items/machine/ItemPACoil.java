package com.hbm.items.machine;

import com.hbm.items.ItemEnumMulti;
import com.hbm.util.EnumUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;
import java.util.Locale;

public class ItemPACoil extends ItemEnumMulti<ItemPACoil.EnumCoilType> {

    public ItemPACoil(String s) {
        super(s, EnumCoilType.VALUES, true, true);
        this.setMaxStackSize(1);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
        EnumCoilType type = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());
        list.add(TextFormatting.BLUE + "Quadrupole operational range: " + TextFormatting.RESET + String.format(Locale.US, "%,d", type.quadMin) + " " +
                "- " + String.format(Locale.US, "%,d", type.quadMax));
        list.add(TextFormatting.BLUE + "Dipole operational range: " + TextFormatting.RESET + String.format(Locale.US, "%,d", type.diMin) + " - " + String.format(Locale.US, "%,d", type.diMax));
        list.add(TextFormatting.BLUE + "Dipole minimum side length: " + TextFormatting.RESET + type.diDistMin);
        list.add(TextFormatting.RED + "Minimums not met result in a power draw penalty!");
        list.add(TextFormatting.RED + "Maximums exceeded result in the particle crashing!");
        list.add(TextFormatting.RED + "Particles will crash in dipoles if both penalties take effect!");
    }

    public enum EnumCoilType {
        GOLD(0, 2_200, 0, 2_200, 15),
        NIOBIUM(1_500, 8_400, 1_500, 8_400, 21),
        BSCCO(7_500, 15_000, 7_500, 15_000, 27),
        CHLOROPHYTE(14_500, 75_000, 14_500, 75_000, 51);

        public static final EnumCoilType[] VALUES = values();

        public final int quadMin;
        public final int quadMax;
        public final int diMin;
        public final int diMax;
        public final int diDistMin;

        EnumCoilType(int quadMin, int quadMax, int diMin, int diMax, int diDistMin) {
            this.quadMin = quadMin;
            this.quadMax = quadMax;
            this.diMin = diMin;
            this.diMax = diMax;
            this.diDistMin = diDistMin;
        }
    }
}
