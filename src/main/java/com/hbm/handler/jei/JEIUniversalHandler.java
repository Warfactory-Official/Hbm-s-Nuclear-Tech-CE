package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.handler.jei.JeiRecipes.JeiUniversalRecipe;
import com.hbm.inventory.RecipesCommon;
import com.hbm.main.MainRegistry;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class JEIUniversalHandler implements IRecipeCategory<JeiRecipes.JeiUniversalRecipe> {
    protected static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei.png");

    protected final IDrawable background;
    protected final String titleKey;
    protected final String uid;
    protected final List<JeiRecipes.JeiUniversalRecipe> recipes;

    public JEIUniversalHandler(IGuiHelper helper, String uid, String titleKey, ItemStack[] machines, HashMap<Object, Object> recipeMap) {
        this.uid = uid;
        this.titleKey = titleKey + ".name";
        this.background = helper.createDrawable(GUI_TEXTURE, 5, 11, 166, 65);
        this.recipes = new ArrayList<>();
        buildRecipes(recipeMap, machines);
    }

    protected void buildRecipes(HashMap<Object, Object> recipeMap, ItemStack[] machines) {
        for (Map.Entry<Object, Object> entry : recipeMap.entrySet()) {
            List<List<ItemStack>> inputs = extractInputLists(entry.getKey());
            ItemStack[] outputs = extractOutput(entry.getValue());
            if (!inputs.isEmpty() && outputs.length > 0)
                recipes.add(new JeiUniversalRecipe(inputs, outputs, machines));
        }
    }

    protected List<List<ItemStack>> extractInputLists(Object o) {
        List<List<ItemStack>> allSlots = new ArrayList<>();
        if (o instanceof Object[]) {
            for (Object slotObject : (Object[]) o) {
                List<ItemStack> variants = toItemList(slotObject);
                if (!variants.isEmpty()) {
                    allSlots.add(variants);
                }
            }
            if (!allSlots.isEmpty()) {
                return allSlots;
            }
        } else {
            List<ItemStack> single = toItemList(o);
            if (!single.isEmpty()) {
                allSlots.add(single);
                return allSlots;
            }
        }

        MainRegistry.logger.warn("JEIUniversalHandler: extractInputLists failed for type " + o.getClass());
        return Collections.emptyList();
    }

    private List<ItemStack> toItemList(Object slotObject) {
        List<ItemStack> items = new ArrayList<>();
        if (slotObject instanceof ItemStack s) {
            if (!s.isEmpty()) items.add(s.copy());
            return items;
        }

        if (slotObject instanceof ItemStack[]) {
            for (ItemStack s : (ItemStack[]) slotObject) {
                if (s != null && !s.isEmpty()) items.add(s.copy());
            }
            return items;
        }

        if (slotObject instanceof RecipesCommon.AStack) {
            items.addAll(((RecipesCommon.AStack) slotObject).extractForJEI());
            return items;
        }

        if (slotObject instanceof RecipesCommon.AStack[]) {
            for (RecipesCommon.AStack a : (RecipesCommon.AStack[]) slotObject) {
                items.addAll(a.extractForJEI());
            }
            return items;
        }

        if (slotObject instanceof Collection) {
            for (Object obj : (Collection<?>) slotObject) {
                if (obj instanceof ItemStack s) {
                    if (!s.isEmpty()) items.add(s.copy());
                } else if (obj instanceof RecipesCommon.AStack) {
                    items.addAll(((RecipesCommon.AStack) obj).extractForJEI());
                }
            }
            return items;
        }

        return items;
    }

    public List<JeiUniversalRecipe> getRecipes() {
        return recipes;
    }

    protected ItemStack[] extractOutput(Object o) {
        return extract(o);
    }

    private ItemStack[] extract(Object o) {
        if (o instanceof ItemStack) {
            return new ItemStack[]{((ItemStack) o).copy()};
        }

        if (o instanceof ItemStack[]) {
            return Arrays.stream((ItemStack[]) o).map(ItemStack::copy).toArray(ItemStack[]::new);
        }

        if (o instanceof RecipesCommon.AStack) {
            return ((RecipesCommon.AStack) o).extractForJEI().toArray(new ItemStack[0]);
        }

        if (o instanceof RecipesCommon.AStack[]) {
            List<ItemStack> list = new ArrayList<>();
            for (RecipesCommon.AStack a : (RecipesCommon.AStack[]) o) {
                list.addAll(a.extractForJEI());
            }
            return list.toArray(new ItemStack[0]);
        }

        if (o instanceof Object[]) {
            List<ItemStack> list = new ArrayList<>();
            for (Object obj : (Object[]) o) {
                if (obj instanceof ItemStack) {
                    list.add(((ItemStack) obj).copy());
                } else if (obj instanceof RecipesCommon.AStack) {
                    list.addAll((((RecipesCommon.AStack) obj).extractForJEI()));
                }
            }
            return list.toArray(new ItemStack[0]);
        }

        MainRegistry.logger.warn("JEIUniversalHandler: extract failed for type " + o.getClass());
        return new ItemStack[0];
    }

    @Override
    public @NotNull String getUid() {
        return uid;
    }

    @Override
    public @NotNull String getTitle() {
        return I18n.format(titleKey);
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
    public void setRecipe(@NotNull IRecipeLayout recipeLayout, @NotNull JeiUniversalRecipe recipeWrapper, @NotNull IIngredients ingredients) {
        IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
        List<List<ItemStack>> inputList = ingredients.getInputs(VanillaTypes.ITEM);
        List<List<ItemStack>> outputList = ingredients.getOutputs(VanillaTypes.ITEM);

        for (int i = 0; i < inputList.size(); i++) {
            int[] pos = getInputCoords(inputList.size())[i];
            stacks.init(i, true, pos[0], pos[1]);
        }

        for (int i = 0; i < outputList.size(); i++) {
            int[] pos = getOutputCoords(outputList.size())[i];
            stacks.init(inputList.size() + i, false, pos[0], pos[1]);
        }

        stacks.set(ingredients);

        if (recipeWrapper.getMachines() != null && recipeWrapper.getMachines().length > 0) {
            int slotIndex = inputList.size() + outputList.size();
            int x = 74;
            int y = 31;

            stacks.init(slotIndex, false, x, y);
            stacks.set(slotIndex, Arrays.asList(recipeWrapper.getMachines()));
        }
    }
    public static int[][] getInputCoords(int count) {

        return switch (count) {
            case 1 -> new int[][]{
                    {48, 24}
            };
            case 2 -> new int[][]{
                    {48, 24},
                    {30, 24}
            };
            case 3 -> new int[][]{
                    {48, 24},
                    {30, 24},
                    {12, 24}
            };
            case 4 -> new int[][]{
                    {48, 24 - 9},
                    {30, 24 - 9},
                    {48, 24 + 9},
                    {30, 24 + 9}
            };
            case 5 -> new int[][]{
                    {48, 24 - 9},
                    {30, 24 - 9},
                    {12, 24},
                    {48, 24 + 9},
                    {30, 24 + 9},
            };
            case 6 -> new int[][]{
                    {48, 24 - 9},
                    {30, 24 - 9},
                    {12, 24 - 9},
                    {48, 24 + 9},
                    {30, 24 + 9},
                    {12, 24 + 9}
            };
            case 7 -> new int[][]{
                    {48, 24 - 18},
                    {30, 24 - 9},
                    {12, 24 - 9},
                    {48, 24},
                    {30, 24 + 9},
                    {12, 24 + 9},
                    {48, 24 + 18}
            };
            case 8 -> new int[][]{
                    {48, 24 - 18},
                    {30, 24 - 18},
                    {12, 24 - 9},
                    {48, 24},
                    {30, 24},
                    {12, 24 + 9},
                    {48, 24 + 18},
                    {30, 24 + 18}
            };
            case 9 -> new int[][]{
                    {48, 24 - 18},
                    {30, 24 - 18},
                    {12, 24 - 18},
                    {48, 24},
                    {30, 24},
                    {12, 24},
                    {48, 24 + 18},
                    {30, 24 + 18},
                    {12, 24 + 18}
            };
            default -> new int[count][2];
        };

    }

    public static int[][] getOutputCoords(int count) {

        return switch (count) {
            case 1 -> new int[][]{
                    {102, 24}
            };
            case 2 -> new int[][]{
                    {102, 24},
                    {120, 24}
            };
            case 3 -> new int[][]{
                    {102, 24},
                    {120, 24},
                    {138, 24}
            };
            case 4 -> new int[][]{
                    {102, 24 - 9},
                    {120, 24 - 9},
                    {102, 24 + 9},
                    {120, 24 + 9}
            };
            case 5 -> new int[][]{
                    {102, 24 - 9}, {120, 24 - 9},
                    {102, 24 + 9}, {120, 24 + 9},
                    {138, 24},
            };
            case 6 -> new int[][]{
                    {102, 6}, {120, 6},
                    {102, 24}, {120, 24},
                    {102, 42}, {120, 42},
            };
            case 7 -> new int[][]{
                    {102, 6}, {120, 6},
                    {102, 24}, {120, 24},
                    {102, 42}, {120, 42},
                    {138, 24},
            };
            case 8 -> new int[][]{
                    {102, 6}, {120, 6},
                    {102, 24}, {120, 24},
                    {102, 42}, {120, 42},
                    {138, 24}, {138, 42},
            };
            default -> new int[count][2];
        };

    }

    protected static HashMap<Object, Object> wrapRecipes1(HashMap<Object[], Object[]> map) { return new HashMap<>(map); }
    protected static HashMap<Object, Object> wrapRecipes2(HashMap<Object, Object[]> map) {
        return new HashMap<>(map);
    }
    protected static HashMap<Object, Object> wrapRecipes3(HashMap<Object[], Object> map) { return new HashMap<>(map); }
    protected static HashMap<Object, Object> wrapRecipes5(HashMap<Object, ItemStack> map) { return new HashMap<>(map); }
    protected static HashMap<Object, Object> wrapRecipes6(HashMap<ItemStack, ItemStack[]> map) { return new HashMap<>(map); }
    protected static HashMap<Object, Object> wrapRecipes7(HashMap<RecipesCommon.ComparableStack, ItemStack> map) { return new HashMap<>(map); }
}
