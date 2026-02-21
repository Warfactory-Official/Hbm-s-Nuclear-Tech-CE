package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class RadiolysisRecipeHandler implements IRecipeCategory<RadiolysisRecipeHandler.RadiolysisRecipe> {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_jei_radiolysis.png");

    private final IDrawable background;
    private final IDrawableAnimated progressBar;
    private final String title;

    public RadiolysisRecipeHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(GUI_TEXTURE, 5, 11, 166, 65);
        this.title = I18nUtil.resolveKey(ModBlocks.machine_radiolysis.getTranslationKey() + ".name");
        IDrawableStatic progressStatic = guiHelper.createDrawable(GUI_TEXTURE, 5, 87, 64, 28);
        this.progressBar = guiHelper.createAnimatedDrawable(progressStatic, 60, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @NotNull
    @Override
    public String getUid() {
        return JEIConfig.RADIOLYSIS;
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
    public void drawExtras(@NotNull Minecraft minecraft) {
        progressBar.draw(minecraft, 52, 19);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayout recipeLayout, @NotNull RadiolysisRecipe recipeWrapper, @NotNull IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
        guiItemStacks.init(0, true, 33, 24);
        guiItemStacks.init(1, false, 117, 15);
        guiItemStacks.init(2, false, 117, 33);
        guiItemStacks.set(ingredients);
    }

    public static class RadiolysisRecipe implements IRecipeWrapper {
        private final ItemStack input;
        private final ItemStack output1;
        private final ItemStack output2;

        public RadiolysisRecipe(ItemStack input, ItemStack output1, ItemStack output2) {
            this.input = input;
            this.output1 = output1;
            this.output2 = output2;
        }

        @Override
        public void getIngredients(@NotNull IIngredients ingredients) {
            ingredients.setInput(VanillaTypes.ITEM, this.input);
            ingredients.setOutputs(VanillaTypes.ITEM, Arrays.asList(this.output1, this.output2));
        }
    }
}