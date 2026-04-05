package com.hbm.inventory.recipes;

import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class PlasmaForgeRecipe extends GenericRecipe {

    public long ignitionTemp;

    public PlasmaForgeRecipe(String name) {
        super(name);
    }

    public PlasmaForgeRecipe setInputEnergy(long ignitionTemp) {
        this.ignitionTemp = ignitionTemp;
        return this;
    }

    @Override
    public List<String> print() {
        List<String> list = new ArrayList<>();
        list.add(TextFormatting.YELLOW + this.getLocalizedName());

        autoSwitch(list);
        duration(list);
        power(list);
        list.add(TextFormatting.LIGHT_PURPLE + I18nUtil.resolveKey("gui.recipe.plasmaIn") + ": " + BobMathUtil.getShortNumber(ignitionTemp) + "TU/t");
        input(list);
        output(list);

        return list;
    }
}
