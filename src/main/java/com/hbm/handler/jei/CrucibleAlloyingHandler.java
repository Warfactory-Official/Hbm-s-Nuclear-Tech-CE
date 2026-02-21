package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.CrucibleRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemScraps;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrucibleAlloyingHandler implements IRecipeCategory<CrucibleAlloyingHandler.Wrapper> {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei_crucible.png");

    private final IDrawable background;

    public CrucibleAlloyingHandler(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(GUI_TEXTURE, 5, 11, 166, 65);
    }

    @Override
    public @NotNull String getUid() {
        return JEIConfig.CRUCIBLE_ALLOY;
    }

    @Override
    public @NotNull String getTitle() {
        return "Crucible Alloying";
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
        List<List<ItemStack>> outputLists = ingredients.getOutputs(VanillaTypes.ITEM);

        int dynamicInputs = wrapper.inputs.size();

        for (int i = 0; i < dynamicInputs; i++) {
            int x = 11 + (i % 3) * 18;
            int y = 5 + (i / 3) * 18;
            stacks.init(i, true, x, y);
        }

        int templateIndex = dynamicInputs;
        stacks.init(templateIndex, true, 74, 5);

        int crucibleIndex = dynamicInputs + 1;
        stacks.init(crucibleIndex, true, 74, 41);

        for (int i = 0; i < outputLists.size(); i++) {
            int x = 101 + (i % 3) * 18;
            int y = 5 + (i / 3) * 18;
            stacks.init(dynamicInputs + 2 + i, false, x, y);
        }

        stacks.set(ingredients);
    }

    public static class Wrapper implements IRecipeWrapper {
        final List<ItemStack> inputs;
        final ItemStack template;
        final ItemStack crucible;
        final List<ItemStack> outputs;

        public Wrapper(CrucibleRecipes.CrucibleRecipe recipe) {
            this.inputs = new ArrayList<>();
            this.outputs = new ArrayList<>();
            for (Mats.MaterialStack s : recipe.input) this.inputs.add(ItemScraps.create(s, true));
            for (Mats.MaterialStack s : recipe.output) this.outputs.add(ItemScraps.create(s, true));
            this.template = new ItemStack(ModItems.crucible_template, 1, recipe.getId());
            this.crucible = new ItemStack(ModBlocks.machine_crucible);
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            List<List<ItemStack>> ins = new ArrayList<>(inputs.size() + 2);
            for (ItemStack in : inputs) ins.add(Collections.singletonList(in.copy()));
            ins.add(Collections.singletonList(template.copy()));
            ins.add(Collections.singletonList(crucible.copy()));
            ingredients.setInputLists(VanillaTypes.ITEM, ins);

            List<ItemStack> outs = new ArrayList<>(outputs.size());
            for (ItemStack out : outputs) outs.add(out.copy());
            ingredients.setOutputs(VanillaTypes.ITEM, outs);
        }
    }

    public static List<Wrapper> getRecipes() {
        List<Wrapper> list = new ArrayList<>();
        for (CrucibleRecipes.CrucibleRecipe r : CrucibleRecipes.recipes) {
            list.add(new Wrapper(r));
        }
        return list;
    }
}
