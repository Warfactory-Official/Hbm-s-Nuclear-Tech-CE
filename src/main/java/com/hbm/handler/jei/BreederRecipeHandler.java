package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BreederRecipeHandler implements IRecipeCategory<JeiRecipes.BreederRecipe> {
    public static final ResourceLocation gui_rl =
            new ResourceLocation(Tags.MODID, "textures/gui/processing/gui_breeder.png");
    protected final IDrawable background;
    protected final IDrawableStatic progressStatic;
    protected final IDrawableAnimated progressAnimated;

    public BreederRecipeHandler(IGuiHelper help) {
        background = help.createDrawable(gui_rl, 34, 18, 108, 39);

        progressStatic = help.createDrawable(gui_rl, 176, 0, 70, 20);
        progressAnimated = help.createAnimatedDrawable(progressStatic, 100, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public @NotNull String getUid() {
        return JEIConfig.BREEDER;
    }

    @Override
    public @NotNull String getTitle() {
        return I18nUtil.resolveKey("tile.machine_breeder.name");
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
    public void setRecipe(
            IRecipeLayout recipeLayout, @NotNull JeiRecipes.BreederRecipe recipeWrapper, @NotNull IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        guiItemStacks.init(0, true, 0, 16);

        guiItemStacks.init(1, false, 90, 16);

        guiItemStacks.set(ingredients);
    }

    @Override
    public void drawExtras(@NotNull Minecraft minecraft) {
        progressAnimated.draw(minecraft, 19, 14);
    }
}
