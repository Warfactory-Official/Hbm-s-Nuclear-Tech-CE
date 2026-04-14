package com.hbm.inventory.recipes;

import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.util.I18nUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class CrucibleRecipe extends GenericRecipe {

    public MaterialStack[] input;
    public MaterialStack[] output;
    public int frequency = 1;

    public CrucibleRecipe(String name) {
        super(name);
    }

    public CrucibleRecipe setup(int frequency, ItemStack icon) {
        this.frequency = frequency;
        this.setIcon(icon);
        return this;
    }

    public CrucibleRecipe inputs(MaterialStack... input) { this.input = input; return this; }
    public CrucibleRecipe outputs(MaterialStack... output) { this.output = output; return this; }

    public int getInputAmount() {
        int content = 0;
        for(MaterialStack stack : input) content += stack.amount;
        return content;
    }

    @Override
    public List<String> print() {
        List<String> list = new ArrayList<>();
        list.add(TextFormatting.YELLOW + this.getLocalizedName());

        input(list);
        output(list);

        return list;
    }

    @Override
    protected void input(List<String> list) {
        list.add(TextFormatting.BOLD + I18nUtil.resolveKey("gui.recipe.input") + ":");
        for(MaterialStack stack : input) {
            list.add(I18nUtil.resolveKey(stack.material.getTranslationKey()) + ": " + Mats.formatAmount(stack.amount, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)));
        }
    }

    @Override
    protected void output(List<String> list) {
        list.add(TextFormatting.BOLD + I18nUtil.resolveKey("gui.recipe.output") + ":");
        for(MaterialStack stack : output) {
            list.add(I18nUtil.resolveKey(stack.material.getTranslationKey()) + ": " + Mats.formatAmount(stack.amount, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)));
        }
    }
}
