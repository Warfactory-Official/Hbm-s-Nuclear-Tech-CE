package com.hbm.inventory.recipes;

import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class DFCRecipes {

    public static LinkedHashMap<ComparableStack, Object[]> dfcRecipes = new LinkedHashMap<>();
    public static List<DFCRecipe> jeiDFCRecipes = null;

    public static void register() {
        DFCRecipes.setRecipe(10000000L, ModItems.billet_polonium, new ItemStack(ModItems.billet_yharonite));
    }

    public static void setRecipe(long requiredFlux, ItemStack in, ItemStack out) {
        dfcRecipes.put(new ComparableStack(in), new Object[]{requiredFlux, out});
    }

    public static void setRecipe(long requiredFlux, Item in, ItemStack out) {
        dfcRecipes.put(new ComparableStack(in), new Object[]{requiredFlux, out});
    }

    public static void setRecipe(long requiredFlux, Block in, ItemStack out) {
        dfcRecipes.put(new ComparableStack(in), new Object[]{requiredFlux, out});
    }

    public static void setRecipe(long requiredFlux, String in, ItemStack out) {
        dfcRecipes.put(new ComparableStack(OreDictionary.getOres(in).getFirst()), new Object[]{requiredFlux, out});
    }

    public static void removeRecipe(ItemStack in) {
        dfcRecipes.remove(new ComparableStack(in));
    }

    public static long getRequiredFlux(ItemStack stack) {

        if (stack == null || stack.isEmpty())
            return -1;

        ComparableStack comp = new ComparableStack(stack).makeSingular();
        if (dfcRecipes.containsKey(comp)) {
            return (long) dfcRecipes.get(comp)[0];
        }

        String[] dictKeys = comp.getDictKeys();

        for (String key : dictKeys) {
            if (dfcRecipes.containsKey(key)) {
                return (long) dfcRecipes.get(key)[1];
            }
        }
        return -1;
    }

    public static ItemStack getOutput(ItemStack stack) {
        if (stack == null) return ItemStack.EMPTY;

        ComparableStack comp = new ComparableStack(stack).makeSingular();
        if (dfcRecipes.containsKey(comp)) {
            return (ItemStack) dfcRecipes.get(comp)[1];
        }

        String[] dictKeys = comp.getDictKeys();

        for (String key : dictKeys) {
            if (dfcRecipes.containsKey(key)) {
                return (ItemStack) dfcRecipes.get(key)[1];
            }
        }
        return ItemStack.EMPTY;
    }

    public static List<DFCRecipe> getDFCRecipes() {
        if (jeiDFCRecipes == null) {
            jeiDFCRecipes = new ArrayList<>();
            for (Entry<ComparableStack, Object[]> e : dfcRecipes.entrySet()) {
                jeiDFCRecipes.add(new DFCRecipe(e.getKey().toStack(), (long) e.getValue()[0], (ItemStack) e.getValue()[1]));
            }
        }
        return jeiDFCRecipes;
    }

    public static class DFCRecipe implements IRecipeWrapper {

        private final ItemStack input;
        private final long requiredFlux;
        private final ItemStack output;

        public DFCRecipe(ItemStack input, long requiredFlux, ItemStack output) {
            this.input = input;
            this.requiredFlux = requiredFlux;
            this.output = output;
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            ingredients.setInput(VanillaTypes.ITEM, input);
            ingredients.setOutput(VanillaTypes.ITEM, output);
        }

        @Override
        public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
            FontRenderer fontRenderer = minecraft.fontRenderer;

            fontRenderer.drawString("Spark", 8, 8, 4210752);
            String number = Library.getShortNumber(requiredFlux);
            fontRenderer.drawString(number, 80 - fontRenderer.getStringWidth(number), 8, 0xa82a0e);
            GlStateManager.color(1, 1, 1, 1);
        }
    }
}