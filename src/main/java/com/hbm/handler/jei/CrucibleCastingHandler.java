package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.CrucibleRecipes;
import com.hbm.items.machine.ItemMold;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrucibleCastingHandler implements IRecipeCategory<CrucibleCastingHandler.Wrapper> {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei_foundry.png");

    private final IDrawable background;

    public CrucibleCastingHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(GUI_TEXTURE, 5, 11, 166, 65);
    }

    @Override
    public @NotNull String getUid() {
        return JEIConfig.CRUCIBLE_CAST;
    }

    @Override
    public @NotNull String getTitle() {
        return "Crucible Casting";
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

        // Индексы слотов: 0=input, 1=mold, 2=basin, 3=output
        stacks.init(0, true, 47, 23); // input
        stacks.init(1, true, 74, 5);  // mold
        stacks.init(2, true, 74, 41); // basin
        stacks.init(3, false, 101, 23); // output

        stacks.set(ingredients);
    }

    public static class Wrapper implements IRecipeWrapper {
        private final ItemStack input;
        private final ItemStack mold;
        private final ItemStack basin;
        private final ItemStack output;

        public Wrapper(ItemStack input, ItemStack mold, ItemStack basin) {
            this.input = input.copy();
            this.mold = mold.copy();
            this.basin = basin.copy();

            ItemStack o = ItemMold.moldById.get(this.mold.getItemDamage())
                    .getOutput(Mats.matById.get(this.input.getItemDamage()));
            this.output = o.copy();
        }

        public Wrapper(ItemStack[] stacks) {
            this(stacks[0], stacks[1], stacks[2]);
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            List<List<ItemStack>> ins = new ArrayList<>(3);
            ins.add(Collections.singletonList(input));
            ins.add(Collections.singletonList(mold));
            ins.add(Collections.singletonList(basin));
            ingredients.setInputLists(VanillaTypes.ITEM, ins);
            ingredients.setOutput(VanillaTypes.ITEM, output);
        }

        @Override
        public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
            drawSlot(48, 24);  // input
            drawSlot(75, 6);   // mold
            drawSlot(75, 42);  // basin
            drawSlot(102, 24); // output
        }

        private void drawSlot(int x, int y) {
            Gui.drawModalRectWithCustomSizedTexture(x - 1, y - 1, 5, 87, 18, 18, 256, 256);
        }
    }

    public List<Wrapper> getRecipes() {
        List<Wrapper> list = new ArrayList<>();
        for (ItemStack[] r : CrucibleRecipes.getMoldRecipes()) {
            list.add(new Wrapper(r[0], r[1], r[2]));
        }
        return list;
    }
}
