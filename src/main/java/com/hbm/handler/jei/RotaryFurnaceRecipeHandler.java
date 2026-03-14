package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.jei.JeiRecipes.JeiUniversalRecipe;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.RotaryFurnaceRecipes;
import com.hbm.inventory.recipes.RotaryFurnaceRecipes.RotaryFurnaceRecipe;
import com.hbm.items.machine.ItemScraps;
import mezz.jei.api.IGuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RotaryFurnaceRecipeHandler extends JEIUniversalHandler {

    public RotaryFurnaceRecipeHandler(IGuiHelper helper) {
        super(helper, JEIConfig.ROTARY_FURNACE, ModBlocks.machine_rotary_furnace.getTranslationKey(), new ItemStack[]{new ItemStack(ModBlocks.machine_rotary_furnace)}, RotaryFurnaceRecipes.getRecipes());
    }

    @Override
    protected void buildRecipes(HashMap<Object, Object> recipeMap, ItemStack[] machines) {
        for (Map.Entry<Object, Object> entry : recipeMap.entrySet()) {
            List<List<ItemStack>> inputs = extractInputLists(entry.getKey());
            ItemStack[] outputs = extractOutput(entry.getValue());
            if (!inputs.isEmpty() && outputs.length > 0) {
                recipes.add(new RotaryFurnaceRecipeWrapper(inputs, outputs, machines, entry.getKey(), outputs[0]));
            }
        }
    }

    public static class RotaryFurnaceRecipeWrapper extends JeiUniversalRecipe {

        private final Object originalInputInstance;
        private final ItemStack outputInstance;

        public RotaryFurnaceRecipeWrapper(List<List<ItemStack>> inputs, ItemStack[] outputs, ItemStack[] machines, Object originalInputInstance, ItemStack outputInstance) {
            super(inputs, outputs, machines);
            this.originalInputInstance = originalInputInstance;
            this.outputInstance = outputInstance;
        }

        @Override
        public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
            super.drawInfo(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);

            Object[] original = (Object[]) this.originalInputInstance;

            outer:
            for (RotaryFurnaceRecipe arc : RotaryFurnaceRecipes.recipes) {

                if (ItemStack.areItemStacksEqual(ItemScraps.create(arc.output, true), this.outputInstance) && arc.ingredients.length == original.length - (arc.fluid == null ? 0 : 1)) {

                    for (int i = 0; i < this.inputs.size() - (arc.fluid == null ? 0 : 1); i++) {
                        if (arc.ingredients[i] != original[i]) continue outer;
                    }

                    FontRenderer fontRenderer = minecraft.fontRenderer;
                    String duration = String.format(Locale.US, "%,d", arc.duration) + " ticks";
                    String consumption = I18n.format(Fluids.STEAM.getTranslationKey()) + ": " + String.format(Locale.US, "%,d", arc.steam) + " mB/t";
                    int side = 160;
                    fontRenderer.drawString(duration, side - fontRenderer.getStringWidth(duration), 43, 0x404040);
                    fontRenderer.drawString(consumption, side - fontRenderer.getStringWidth(consumption), 55, 0x404040);
                    return;
                }
            }
        }
    }
}
