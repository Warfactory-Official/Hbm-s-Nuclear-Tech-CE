package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.items.machine.ItemFluidIcon;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class JEIGenericRecipeHandler implements IRecipeCategory<JEIGenericRecipeHandler.JeiGenericRecipe> {
    protected static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei.png");

    protected final IDrawable background;
    protected final String titleKey;
    protected final String uid;
    protected final GenericRecipes recipeSet;
    protected final ItemStack[] defaultMachines;
    protected final List<JeiGenericRecipe> recipes = new ArrayList<>();

    public JEIGenericRecipeHandler(IGuiHelper helper, String uid, String titleKey, GenericRecipes recipeSet, ItemStack... machines) {
        this.uid = uid;
        this.titleKey = titleKey + ".name";
        this.background = helper.createDrawable(GUI_TEXTURE, 5, 11, 166, 65);
        this.recipeSet = recipeSet;
        this.defaultMachines = machines;
        buildRecipes();
    }

    public JEIGenericRecipeHandler(IGuiHelper helper, String uid, String titleKey, GenericRecipes recipeSet, Block... machines) {
        this.uid = uid;
        this.titleKey = titleKey + ".name";
        this.background = helper.createDrawable(GUI_TEXTURE, 5, 11, 166, 65);
        this.recipeSet = recipeSet;
        this.defaultMachines = new ItemStack[machines.length];
        for (int i = 0; i < machines.length; i++) {
            this.defaultMachines[i] = new ItemStack(machines[i]);
        }
        buildRecipes();
    }

    protected void buildRecipes() {

        for (Object o : this.recipeSet.recipeOrderedList) {
            GenericRecipe recipe = (GenericRecipe) o;

            if (recipe.isPooled()) {
                String[] pools = recipe.getPools();
                boolean secret = false;
                for (String pool : pools) {
                    if (pool.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) {
                        secret = true;
                        break;
                    }
                }
                if (secret) continue;
            }

            if (recipe.inputItem != null) {
                boolean skip = false;
                for (RecipesCommon.AStack a : recipe.inputItem) {
                    for (ItemStack s : a.extractForJEI()) {
                        if (!s.isEmpty() && ModItems.excludeNEI.contains(s.getItem())) {
                            skip = true;
                            break;
                        }
                    }
                    if (skip) break;
                }
                if (skip) continue;
            }

            if (recipe.outputItem != null) {
                boolean skip = false;
                for (GenericRecipes.IOutput out : recipe.outputItem) {
                    for (ItemStack s : out.getAllPossibilities()) {
                        if (!s.isEmpty() && ModItems.excludeNEI.contains(s.getItem())) {
                            skip = true;
                            break;
                        }
                    }
                    if (skip) break;
                }
                if (skip) continue;
            }

            List<List<ItemStack>> inputs = new ArrayList<>();
            if (recipe.inputItem != null) {
                for (RecipesCommon.AStack a : recipe.inputItem) {
                    List<ItemStack> vars = a.extractForJEI();
                    if (!vars.isEmpty()) inputs.add(vars);
                }
            }
            if (recipe.inputFluid != null) {
                for (FluidStack f : recipe.inputFluid) {
                    ItemStack icon = ItemFluidIcon.make(f);
                    if (!icon.isEmpty()) {
                        inputs.add(Collections.singletonList(icon));
                    }
                }
            }

            List<List<ItemStack>> outputs = new ArrayList<>();
            if (recipe.outputItem != null) {
                for (GenericRecipes.IOutput out : recipe.outputItem) {
                    ItemStack[] vars = out.getAllPossibilities();
                    List<ItemStack> list = new ArrayList<>();
                    if (vars != null) {
                        for (ItemStack s : vars) {
                            if (s != null && !s.isEmpty()) list.add(s.copy());
                        }
                    }
                    if (!list.isEmpty()) outputs.add(list);
                }
            }
            if (recipe.outputFluid != null) {
                for (FluidStack f : recipe.outputFluid) {
                    ItemStack icon = ItemFluidIcon.make(f);
                    if (!icon.isEmpty()) {
                        outputs.add(Collections.singletonList(icon));
                    }
                }
            }

            List<ItemStack> templates = null;
            if (recipe.isPooled()) {
                String[] pools = recipe.getPools();
                templates = new ArrayList<>(pools.length);
                for (String pool : pools) {
                    templates.add(ItemBlueprints.make(pool));
                }
            }

            int inputOffset = getInputXOffset(recipe, inputs.size());
            int outputOffset = getOutputXOffset(recipe, outputs.size());
            int machineOffset = getMachineXOffset(recipe);

            ItemStack[] machines = getMachines(recipe);

            recipes.add(new JeiGenericRecipe(recipe, inputs, outputs, machines, templates, inputOffset, outputOffset, machineOffset));
        }
    }

    public List<JeiGenericRecipe> getRecipes() {
        return recipes;
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public String getTitle() {
        return I18n.format(titleKey);
    }

    @Override
    public String getModName() {
        return Tags.MODID;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, JeiGenericRecipe wrapper, IIngredients ingredients) {
        IGuiItemStackGroup stacks = recipeLayout.getItemStacks();

        List<List<ItemStack>> inputList = ingredients.getInputs(VanillaTypes.ITEM);
        List<List<ItemStack>> outputList = ingredients.getOutputs(VanillaTypes.ITEM);

        int[][] inPos = getInputSlotPositions(inputList.size());
        for (int i = 0; i < inputList.size(); i++) {
            stacks.init(i, true, inPos[i][0] + wrapper.inputOffset - 1, inPos[i][1] - 1);
        }

        int[][] outPos = getOutputSlotPositions(outputList.size());
        for (int i = 0; i < outputList.size(); i++) {
            stacks.init(inputList.size() + i, false, outPos[i][0] + wrapper.outputOffset - 1, outPos[i][1] - 1);
        }

        stacks.set(ingredients);

        int slotIndex = inputList.size() + outputList.size();
        int mx = 74 + wrapper.machineOffset;
        int my = (wrapper.templates == null) ? 29 : 36;
        stacks.init(slotIndex, false, mx, my);
        stacks.set(slotIndex, Arrays.asList(wrapper.machines));

        if (wrapper.templates != null && !wrapper.templates.isEmpty()) {
            int tIndex = slotIndex + 1;
            stacks.init(tIndex, false, 74 + wrapper.machineOffset, 9);
            stacks.set(tIndex, wrapper.templates);
        }
    }

    public int getInputXOffset(GenericRecipe recipe, int inputCount) { return 0; }
    public int getOutputXOffset(GenericRecipe recipe, int outputCount) { return 0; }
    public int getMachineXOffset(GenericRecipe recipe) { return 0; }
    public ItemStack[] getMachines(GenericRecipe recipe) { return this.defaultMachines; }

    public int[][] getInputSlotPositions(int count) {
        if (count == 1) return new int[][]{{48, 24}};
        if (count == 2) return new int[][]{{30, 24}, {48, 24}};
        if (count == 3) return new int[][]{{12, 24}, {30, 24}, {48, 24}};
        if (count == 4) return new int[][]{{30, 15}, {48, 15}, {30, 33}, {48, 33}};
        if (count == 5) return new int[][]{{12, 15}, {30, 15}, {48, 15}, {12, 33}, {30, 33}};
        if (count == 6) return new int[][]{{12, 15}, {30, 15}, {48, 15}, {12, 33}, {30, 33}, {48, 33}};

        int[][] slots = new int[count][2];
        int cols = (count + 2) / 3;

        for (int i = 0; i < count; i++) {
            slots[i][0] = 12 + (i % cols) * 18 - (cols == 4 ? 18 : 0);
            slots[i][1] = 6 + (i / cols) * 18;
        }
        return slots;
    }

    public int[][] getOutputSlotPositions(int count) {
        return switch (count) {
            case 1 -> new int[][]{{102, 24}};
            case 2 -> new int[][]{{102, 24}, {120, 24}};
            case 3 -> new int[][]{{102, 24}, {120, 24}, {138, 24}};
            case 4 -> new int[][]{{102, 15}, {120, 15}, {102, 33}, {120, 33}};
            case 5 -> new int[][]{{102, 15}, {120, 15}, {102, 33}, {120, 33}, {138, 24}};
            case 6 -> new int[][]{{102, 6}, {120, 6}, {102, 24}, {120, 24}, {102, 42}, {120, 42}};
            case 7 -> new int[][]{{102, 6}, {120, 6}, {102, 24}, {120, 24}, {102, 42}, {120, 42}, {138, 24}};
            case 8 -> new int[][]{{102, 6}, {120, 6}, {102, 24}, {120, 24}, {102, 42}, {120, 42}, {138, 24}, {138, 42}};
            default -> new int[count][2];
        };
    }

    public class JeiGenericRecipe implements IRecipeWrapper {
        protected final GenericRecipe recipe;
        protected final List<List<ItemStack>> inputs;
        protected final List<List<ItemStack>> outputs;
        protected final ItemStack[] machines;
        protected final List<ItemStack> templates; // nullable
        protected final int inputOffset;
        protected final int outputOffset;
        protected final int machineOffset;

        public JeiGenericRecipe(GenericRecipe recipe,
                                List<List<ItemStack>> inputs,
                                List<List<ItemStack>> outputs,
                                ItemStack[] machines,
                                List<ItemStack> templates,
                                int inputOffset,
                                int outputOffset,
                                int machineOffset) {
            this.recipe = recipe;
            this.inputs = inputs;
            this.outputs = outputs;
            this.machines = machines != null ? Arrays.stream(machines).map(ItemStack::copy).toArray(ItemStack[]::new) : new ItemStack[0];
            this.templates = templates;
            this.inputOffset = inputOffset;
            this.outputOffset = outputOffset;
            this.machineOffset = machineOffset;
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            ingredients.setInputLists(VanillaTypes.ITEM, inputs);
            ingredients.setOutputLists(VanillaTypes.ITEM, outputs);
        }

        public GenericRecipe getRecipe() {
            return recipe;
        }

        public boolean hasTemplate() {
            return templates != null && !templates.isEmpty();
        }

        public List<ItemStack> getTemplates() {
            return templates;
        }

        public ItemStack[] getMachines() {
            return machines;
        }

        @Override
        public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            minecraft.getTextureManager().bindTexture(GUI_TEXTURE);

            int[][] inCoords = getInputSlotPositions(inputs.size());
            for (int[] inCoord : inCoords) {
                int x = inCoord[0] + inputOffset;
                int y = inCoord[1];
                Gui.drawModalRectWithCustomSizedTexture(x - 1, y - 1, 5, 87, 18, 18, 256, 256);
            }

            int[][] outCoords = getOutputSlotPositions(outputs.size());
            for (int[] outCoord : outCoords) {
                int x = outCoord[0] + outputOffset;
                int y = outCoord[1];
                Gui.drawModalRectWithCustomSizedTexture(x - 1, y - 1, 5, 87, 18, 18, 256, 256);
            }

            int mx = 74 + machineOffset;
            if (hasTemplate()) {
                Gui.drawModalRectWithCustomSizedTexture(mx, 7, 77, 87, 18, 50, 256, 256);
            } else {
                Gui.drawModalRectWithCustomSizedTexture(mx, 14, 59, 87, 18, 36, 256, 256);
            }
        }
    }
}
