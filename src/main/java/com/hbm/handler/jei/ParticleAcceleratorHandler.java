package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.ParticleAcceleratorRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ParticleAcceleratorHandler extends JEIUniversalHandler {

    public ParticleAcceleratorHandler(IGuiHelper helper) {
        super(helper, JEIConfig.PA, ModBlocks.pa_detector.getTranslationKey(), new ItemStack[]{new ItemStack(ModBlocks.pa_detector)},
                new HashMap<>(ParticleAcceleratorRecipes.getRecipes()));
    }

    @Override
    protected void buildRecipes(HashMap<Object, Object> recipeMap, ItemStack[] machines) {
        for (Map.Entry<Object, Object> entry : recipeMap.entrySet()) {
            List<List<ItemStack>> inputs = extractInputLists(entry.getKey());
            ItemStack[] outputs = extractOutput(entry.getValue());
            if (inputs.isEmpty() || inputs.size() < 2 || outputs.length == 0) continue;
            ItemStack input1 = inputs.get(0).get(0);
            ItemStack input2 = inputs.get(1).get(0);
            ParticleAcceleratorRecipes.ParticleAcceleratorRecipe paRecipe = ParticleAcceleratorRecipes.getOutput(input1, input2);
            final int momentum = (paRecipe != null) ? paRecipe.momentum : 0;
            JeiRecipes.JeiUniversalRecipe recipeWrapper = new JeiRecipes.JeiUniversalRecipe(inputs, outputs, machines) {
                @Override
                public void drawInfo(@NotNull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
                    super.drawInfo(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
                    if (momentum > 0) {
                        String momentumString = "Momentum: " + String.format(Locale.US, "%,d", momentum);
                        minecraft.fontRenderer.drawString(momentumString, 8, 52, 0x404040);
                    }
                }
            };
            this.recipes.add(recipeWrapper);
        }
    }
}