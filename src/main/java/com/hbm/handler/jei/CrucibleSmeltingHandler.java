package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.recipes.CrucibleRecipes;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CrucibleSmeltingHandler implements IRecipeCategory<CrucibleSmeltingHandler.Wrapper> {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei_crucible_smelting.png");

    private final IDrawable background;

    public CrucibleSmeltingHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(GUI_TEXTURE, 5, 11, 166, 65);
    }

    @Override
    public @NotNull String getUid() {
        return JEIConfig.CRUCIBLE_SMELT;
    }

    @Override
    public @NotNull String getTitle() {
        return "Crucible Smelting";
    }

    @Override
    public @NotNull String getModName() {
        return Tags.MODID;
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayout recipeLayout, @NotNull Wrapper wrapper, @NotNull IIngredients ingredients) {
        IGuiItemStackGroup stacks = recipeLayout.getItemStacks();

        // 0: main input (alternatives list)
        stacks.init(0, true, 47, 23);

        // 1: crucible
        stacks.init(1, true, 74, 41);

        // outputs start at index 2
        List<List<ItemStack>> outs = ingredients.getOutputs(VanillaTypes.ITEM);
        for (int i = 0; i < outs.size(); i++) {
            int x = 101 + (i % 3) * 18;
            int y = 5 + (i / 3) * 18;
            stacks.init(2 + i, false, x, y);
        }

        stacks.set(ingredients);
    }

    public static class Wrapper implements IRecipeWrapper {
        final RecipesCommon.AStack input;
        final ItemStack crucible;
        final List<ItemStack> outputs;

        public Wrapper(RecipesCommon.AStack input, List<ItemStack> outputs) {
            this.input = input;
            this.crucible = new ItemStack(ModBlocks.machine_crucible);
            this.outputs = new ArrayList<>(outputs.size());
            for (ItemStack s : outputs) this.outputs.add(s.copy());
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            List<List<ItemStack>> ins = new ArrayList<>(2);

            List<ItemStack> altInputs = new ArrayList<>();
            for (ItemStack s : input.extractForJEI()) altInputs.add(s.copy());
            ins.add(altInputs);

            ins.add(Collections.singletonList(crucible.copy()));

            ingredients.setInputLists(VanillaTypes.ITEM, ins);
            ingredients.setOutputs(VanillaTypes.ITEM, outputs);
        }

        @Override
        public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            minecraft.getTextureManager().bindTexture(GUI_TEXTURE);

            drawSlot(48, 24);   // input
            drawSlot(75, 42);   // crucible

            for (int i = 0; i < outputs.size(); i++) {
                int x = 102 + (i % 3) * 18;
                int y = 6 + (i / 3) * 18;
                drawSlot(x, y);
            }
        }

        private void drawSlot(int x, int y) {
            Gui.drawModalRectWithCustomSizedTexture(x - 1, y - 1, 5, 87, 18, 18, 256, 256);
        }
    }

    public static List<Wrapper> getRecipes() {
        List<Wrapper> list = new ArrayList<>();
        HashMap<RecipesCommon.AStack, List<ItemStack>> smelting = CrucibleRecipes.getSmeltingRecipes();
        for (Map.Entry<RecipesCommon.AStack, List<ItemStack>> e : smelting.entrySet()) {
            list.add(new Wrapper(e.getKey(), e.getValue()));
        }
        return list;
    }
}