package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.inventory.recipes.anvil.AnvilSmithingRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AnvilSmithingRecipeHandler implements IRecipeCategory<AnvilSmithingRecipeHandler.SmithingRecipe> {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei_smithing.png");

    private final IDrawable background;
    private final IDrawable slotDrawable;
    private final String title;

    public AnvilSmithingRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(GUI_TEXTURE, 5, 11, 166, 65);
        this.slotDrawable = guiHelper.getSlotDrawable();
        this.title = "Anvil";
    }

    @NotNull
    @Override
    public String getUid() {
        return JEIConfig.ANVIL_SMITH;
    }

    @NotNull
    @Override
    public String getTitle() {
        return this.title;
    }

    @NotNull
    @Override
    public String getModName() {
        return Tags.MODID;
    }

    @NotNull
    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayout recipeLayout, @NotNull SmithingRecipe recipeWrapper, @NotNull IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        guiItemStacks.init(0, true, 38, 23);
        guiItemStacks.setBackground(0, slotDrawable);
        guiItemStacks.init(1, true, 74, 23);
        guiItemStacks.setBackground(1, slotDrawable);

        guiItemStacks.init(2, false, 110, 23);
        guiItemStacks.setBackground(2, slotDrawable);

        guiItemStacks.set(ingredients);

        final int outputIndex = 2;
        guiItemStacks.addTooltipCallback((slotIndex, input, stack, tooltip) -> {
            if (slotIndex == outputIndex && !input) {
                tooltip.add(net.minecraft.util.text.TextFormatting.DARK_GRAY + "Tier " + recipeWrapper.getTier());
            }
        });
    }

    public static class SmithingRecipe implements IRecipeWrapper {
        private final List<ItemStack> leftOptions;
        private final List<ItemStack> rightOptions;
        private final ItemStack outputSimple;
        private final int tier;

        public SmithingRecipe(List<ItemStack> leftOptions, List<ItemStack> rightOptions, ItemStack outputSimple, int tier) {
            this.leftOptions = leftOptions;
            this.rightOptions = rightOptions;
            this.outputSimple = outputSimple;
            this.tier = tier;
        }

        @Override
        public void getIngredients(@NotNull IIngredients ingredients) {
            List<List<ItemStack>> inputs = new ArrayList<>(2);
            inputs.add(this.leftOptions);
            inputs.add(this.rightOptions);
            ingredients.setInputLists(VanillaTypes.ITEM, inputs);
            ingredients.setOutput(VanillaTypes.ITEM, this.outputSimple);
        }

        @Override
        public void drawInfo(@NotNull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
            minecraft.fontRenderer.drawString("Tier " + this.tier, 52, 43, 0x404040);
        }

        public int getTier() {
            return tier;
        }
    }

    public static List<SmithingRecipe> getRecipes() {
        List<SmithingRecipe> list = new ArrayList<>();
        for (AnvilSmithingRecipe recipe : AnvilRecipes.getSmithing()) {
            List<ItemStack> left = new ArrayList<>(recipe.getLeft());
            List<ItemStack> right = new ArrayList<>(recipe.getRight());
            ItemStack output = recipe.getSimpleOutput();
            int tier = recipe.tier;
            list.add(new SmithingRecipe(left, right, output, tier));
        }
        return list;
    }
}
