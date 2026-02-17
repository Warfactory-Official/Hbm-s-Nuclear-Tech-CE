package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.handler.jei.JeiRecipes.GasCentrifugeRecipe;
import com.hbm.items.ModItems;
import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GasCentrifugeRecipeHandler implements IRecipeCategory<GasCentrifugeRecipe> {

  public static final ResourceLocation gui_rl =
      new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_jei_gas_centrifuge.png");

  protected final IDrawable background;
  protected final IDrawableStatic powerStatic;
  protected final IDrawableAnimated powerAnimated;
  protected final IDrawableStatic progressStatic;
  protected final IDrawableAnimated progressAnimated;

  public GasCentrifugeRecipeHandler(IGuiHelper help) {
    background = help.createDrawable(gui_rl, 3, 6, 162, 54);

    powerStatic = help.createDrawable(gui_rl, 168, 37, 16, 34);
    powerAnimated = help.createAnimatedDrawable(powerStatic, 480, StartDirection.TOP, true);

    progressStatic = help.createDrawable(gui_rl, 168, 0, 44, 37);
    progressAnimated = help.createAnimatedDrawable(progressStatic, 150, StartDirection.LEFT, false);
  }

  @Override
  public @NotNull String getUid() {
    return JEIConfig.GAS_CENT;
  }

  @Override
  public @NotNull String getTitle() {
    return I18nUtil.resolveKey("tile.machine_gascent.name");
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
          IRecipeLayout recipeLayout, @NotNull GasCentrifugeRecipe recipeWrapper, @NotNull IIngredients ingredients) {
    IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

    guiItemStacks.init(0, true, 44, 18);

    guiItemStacks.init(1, false, 126, 9);
    guiItemStacks.init(2, false, 144, 9);
    guiItemStacks.init(3, false, 126, 27);
    guiItemStacks.init(4, false, 144, 27);

    guiItemStacks.init(5, true, 0, 36);
    guiItemStacks.init(6, true, 23, 19);

    guiItemStacks.set(ingredients);

    if(recipeWrapper.hasHighSpeed()) {
        guiItemStacks.set(6, ModItems.upgrade_gc_speed.getDefaultInstance());
    }
    
    guiItemStacks.set(5, JeiRecipes.getBatteries());
  }

  @Override
  public void drawExtras(@NotNull Minecraft minecraft) {
    powerAnimated.draw(minecraft, 1, 1);
    progressAnimated.draw(minecraft, 72, 12);
  }
}
