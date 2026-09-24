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
import mezz.jei.api.gui.IGuiFluidStackGroup;
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
            List<net.minecraftforge.fluids.FluidStack> inputFluids = new ArrayList<>();
            List<Integer> inputFluidAnchors = new ArrayList<>();
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
                        int anchor = inputs.size();
                        inputs.add(Collections.singletonList(icon));
                        // NTM only ever displays fluid ingredients as this dummy icon item - fine for
                        // our own GUI, but external JEI-driven tooling (e.g. AE2's fluid autocrafting
                        // addons, which read a recipe's real VanillaTypes.FLUID ingredients to
                        // auto-encode a crafting pattern) has no reason to understand a modded dummy
                        // item and sees nothing to work with. Register the real forge FluidStack too,
                        // anchored to this same icon's slot, purely so that ingredient exists for
                        // anything reading the recipe layout - see setRecipe() below.
                        net.minecraftforge.fluids.Fluid ff = f.type == null ? null : f.type.getFF();
                        if (ff != null) {
                            inputFluids.add(new net.minecraftforge.fluids.FluidStack(ff, Math.max(f.fill, 1)));
                            inputFluidAnchors.add(anchor);
                        }
                    }
                }
            }

            List<List<ItemStack>> outputs = new ArrayList<>();
            List<net.minecraftforge.fluids.FluidStack> outputFluids = new ArrayList<>();
            List<Integer> outputFluidAnchors = new ArrayList<>();
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
                        int anchor = outputs.size();
                        outputs.add(Collections.singletonList(icon));
                        net.minecraftforge.fluids.Fluid ff = f.type == null ? null : f.type.getFF();
                        if (ff != null) {
                            outputFluids.add(new net.minecraftforge.fluids.FluidStack(ff, Math.max(f.fill, 1)));
                            outputFluidAnchors.add(anchor);
                        }
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

            recipes.add(new JeiGenericRecipe(recipe, inputs, outputs, machines, templates, inputOffset, outputOffset, machineOffset,
                    inputFluids, inputFluidAnchors, outputFluids, outputFluidAnchors));
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
        List<List<ItemStack>> inputList = ingredients.getInputs(VanillaTypes.ITEM);
        List<List<ItemStack>> outputList = ingredients.getOutputs(VanillaTypes.ITEM);

        int[][] inPos = getInputSlotPositions(inputList.size());
        for (int i = 0; i < inputList.size(); i++) {
            EmiCompat.initSlot(recipeLayout, i, true, inPos[i][0] + wrapper.inputOffset - 1, inPos[i][1] - 1, inputList.get(i));
        }

        boolean emi = EmiCompat.isEmiLayout(recipeLayout);
        List<GenericRecipes.IOutput> sources = new ArrayList<>();
        if (emi && wrapper.recipe.outputItem != null) {
            for (GenericRecipes.IOutput out : wrapper.recipe.outputItem) {
                ItemStack[] vars = out.getAllPossibilities();
                if (vars != null && Arrays.stream(vars).anyMatch(s -> s != null && !s.isEmpty())) sources.add(out);
            }
        }

        int[][] outPos = getOutputSlotPositions(outputList.size());
        for (int i = 0; i < outputList.size(); i++) {
            int x = outPos[i][0] + wrapper.outputOffset - 1;
            int y = outPos[i][1] - 1;
            GenericRecipes.IOutput source = i < sources.size() ? sources.get(i) : null;
            if (isChanced(source)) {
                EmiCompat.initDisplaySlot(recipeLayout, inputList.size() + i, false, x, y, outputList.get(i));
                addHiddenOutputs(recipeLayout, source);
            } else {
                EmiCompat.initSlot(recipeLayout, inputList.size() + i, false, x, y, outputList.get(i));
            }
        }

        int slotIndex = inputList.size() + outputList.size();
        EmiCompat.initDisplaySlot(recipeLayout, slotIndex, false, 74 + wrapper.machineOffset, wrapper.templates == null ? 29 : 36, Arrays.asList(wrapper.machines));
        if (wrapper.templates != null && !wrapper.templates.isEmpty()) {
            EmiCompat.initDisplaySlot(recipeLayout, slotIndex + 1, false, 74 + wrapper.machineOffset, 9, wrapper.templates);
        }

        if (emi) return;

        if (!wrapper.inputFluids.isEmpty() || !wrapper.outputFluids.isEmpty()) {
            IGuiFluidStackGroup fluids = recipeLayout.getFluidStacks();
            for (int i = 0; i < wrapper.inputFluids.size(); i++) {
                int anchor = wrapper.inputFluidAnchors.get(i);
                int x = inPos[anchor][0] + wrapper.inputOffset - 1;
                int y = inPos[anchor][1] - 1;
                // tiny and tucked under the icon item at the same slot - this isn't meant to be seen,
                // the fluid_icon item stays the actual on-screen representation. It exists purely so
                // recipe-transfer tooling (see the buildRecipes() comment above) has a real FluidStack
                // ingredient to read off this recipe layout.
                fluids.init(i, true, x, y, 2, 2, Math.max(wrapper.inputFluids.get(i).amount, 1000), false, null);
            }
            int outBase = wrapper.inputFluids.size();
            for (int i = 0; i < wrapper.outputFluids.size(); i++) {
                int anchor = wrapper.outputFluidAnchors.get(i);
                int x = outPos[anchor][0] + wrapper.outputOffset - 1;
                int y = outPos[anchor][1] - 1;
                fluids.init(outBase + i, false, x, y, 2, 2, Math.max(wrapper.outputFluids.get(i).amount, 1000), false, null);
            }
            fluids.set(ingredients);
        }
    }

    private static boolean isChanced(GenericRecipes.IOutput output) {
        if (output instanceof GenericRecipes.ChanceOutputMulti multi) return multi.pool.size() > 1 || multi.pool.stream().anyMatch(o -> o.chance < 1F);
        return output instanceof GenericRecipes.ChanceOutput single && single.chance < 1F;
    }

    private static void addHiddenOutputs(IRecipeLayout layout, GenericRecipes.IOutput output) {
        if (output instanceof GenericRecipes.ChanceOutputMulti multi) {
            int totalWeight = 0;
            for (GenericRecipes.ChanceOutput out : multi.pool) totalWeight += out.itemWeight;
            for (GenericRecipes.ChanceOutput out : multi.pool) {
                float share = totalWeight > 0 ? (float) out.itemWeight / totalWeight : 1F / multi.pool.size();
                EmiCompat.addHiddenOutput(layout, out.stack.copy(), out.stack.getCount(), share * Math.min(out.chance, 1F));
            }
        } else if (output instanceof GenericRecipes.ChanceOutput single) {
            EmiCompat.addHiddenOutput(layout, single.stack.copy(), single.stack.getCount(), single.chance);
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
        protected final List<net.minecraftforge.fluids.FluidStack> inputFluids;
        protected final List<Integer> inputFluidAnchors;
        protected final List<net.minecraftforge.fluids.FluidStack> outputFluids;
        protected final List<Integer> outputFluidAnchors;

        public JeiGenericRecipe(GenericRecipe recipe,
                                List<List<ItemStack>> inputs,
                                List<List<ItemStack>> outputs,
                                ItemStack[] machines,
                                List<ItemStack> templates,
                                int inputOffset,
                                int outputOffset,
                                int machineOffset,
                                List<net.minecraftforge.fluids.FluidStack> inputFluids,
                                List<Integer> inputFluidAnchors,
                                List<net.minecraftforge.fluids.FluidStack> outputFluids,
                                List<Integer> outputFluidAnchors) {
            this.recipe = recipe;
            this.inputs = inputs;
            this.outputs = outputs;
            this.machines = machines != null ? Arrays.stream(machines).map(ItemStack::copy).toArray(ItemStack[]::new) : new ItemStack[0];
            this.templates = templates;
            this.inputOffset = inputOffset;
            this.outputOffset = outputOffset;
            this.machineOffset = machineOffset;
            this.inputFluids = inputFluids;
            this.inputFluidAnchors = inputFluidAnchors;
            this.outputFluids = outputFluids;
            this.outputFluidAnchors = outputFluidAnchors;
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            ingredients.setInputLists(VanillaTypes.ITEM, inputs);
            ingredients.setOutputLists(VanillaTypes.ITEM, outputs);

            // See the buildRecipes() comment on inputFluids for why these exist alongside the icon items.
            if (!inputFluids.isEmpty()) {
                List<List<net.minecraftforge.fluids.FluidStack>> wrapped = new ArrayList<>(inputFluids.size());
                for (net.minecraftforge.fluids.FluidStack fs : inputFluids) wrapped.add(Collections.singletonList(fs));
                ingredients.setInputLists(VanillaTypes.FLUID, wrapped);
            }
            if (!outputFluids.isEmpty()) {
                List<List<net.minecraftforge.fluids.FluidStack>> wrapped = new ArrayList<>(outputFluids.size());
                for (net.minecraftforge.fluids.FluidStack fs : outputFluids) wrapped.add(Collections.singletonList(fs));
                ingredients.setOutputLists(VanillaTypes.FLUID, wrapped);
            }
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

            recipe.printNEIExtras();
        }
    }
}
