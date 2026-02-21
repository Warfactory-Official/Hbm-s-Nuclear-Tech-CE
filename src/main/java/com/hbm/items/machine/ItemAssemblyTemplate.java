package com.hbm.items.machine;

import com.hbm.Tags;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.NbtComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.gui.GUIAssemfac;
import com.hbm.inventory.gui.GUIMachineAssembler;
import com.hbm.inventory.recipes.AssemblerRecipes;
import com.hbm.inventory.recipes.AssemblerRecipes.AssemblerRecipe;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemAssemblyTemplate extends Item {

    public static final ModelResourceLocation location = new ModelResourceLocation(Tags.MODID + ":assembly_template", "inventory");

    // public static final ModelResourceLocation secret = new ModelResourceLocation(RefStrings.MODID + ":assembly_template_secret", "inventory");
    public ItemAssemblyTemplate(String s) {
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setCreativeTab(MainRegistry.templateTab);

        this.addPropertyOverride(new ResourceLocation(Tags.MODID, "secret"), (stack, worldIn, entityIn) -> {
            ComparableStack out = getRecipeOutput(stack);
            if (out != null) {
                AssemblerRecipe recipe = AssemblerRecipes.recipes.get(out);
                if (recipe != null && !recipe.folders.contains(ModItems.template_folder)) {
                    return 1.0F;
                }
            }
            return 0.0F;
        });

        ModItems.ALL_ITEMS.add(this);
    }

    public static ItemStack writeType(ItemStack stack, ComparableStack comp) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound nbt = stack.getTagCompound();
        nbt.setString("id", comp.item.getRegistryName().toString());
        nbt.setByte("count", (byte) comp.stacksize);
        nbt.setShort("meta", (short) comp.meta);
        return stack;
    }

    /**
     * Reads the recipe output's data from the template's NBT tag.
     */
    private static ComparableStack readType(ItemStack stack) {
        if (!stack.hasTagCompound()) return null;
        NBTTagCompound nbt = stack.getTagCompound();
        if (!nbt.hasKey("id")) return null;

        Item item = Item.getByNameOrId(nbt.getString("id"));
        byte count = nbt.getByte("count");
        short meta = nbt.getShort("meta");
        return new ComparableStack(item, count, meta);
    }

    /**
     * Gets the recipe output from an ItemStack, using NBT first and falling back to legacy metadata.
     */
    public static ComparableStack getRecipeOutput(ItemStack stack) {
        // NEW: Read from NBT
        ComparableStack out = readType(stack);
        // LEGACY: Fallback to metadata
        if (out == null) {
            int meta = stack.getMetadata();
            if (meta >= 0 && meta < AssemblerRecipes.recipeList.size()) {
                out = AssemblerRecipes.recipeList.get(meta);
            }
        }
        return out;
    }

    public static int getProcessTime(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemAssemblyTemplate)) return 100;
        ComparableStack out = getRecipeOutput(stack);
        if (out != null) {
            AssemblerRecipe recipe = AssemblerRecipes.recipes.get(out);
            if (recipe != null) return recipe.time;
        }
        return 100;
    }

    @SideOnly(Side.CLIENT)
    static CheckResult checkAndConsume(AStack ingredient, Map<ComparableStack, Integer> availableCounts) {
        int required = ingredient.count();
        int totalAvailable = 0;
        List<ComparableStack> matchingKeys = new ArrayList<>();
        for (Map.Entry<ComparableStack, Integer> entry : availableCounts.entrySet()) {
            if (entry.getValue() > 0 && ingredient.matchesRecipe(entry.getKey().toStack(), true)) {
                totalAvailable += entry.getValue();
                matchingKeys.add(entry.getKey());
            }
        }
        String color;
        if (totalAvailable >= required) {
            color = TextFormatting.GREEN.toString(); // Green: sufficient
        } else if (totalAvailable > 0) {
            color = TextFormatting.GOLD.toString(); // Orange: present but insufficient
        } else {
            color = TextFormatting.RED.toString(); // Red: not present
        }
        if (totalAvailable > 0) {
            int toConsume = Math.min(totalAvailable, required);
            for (ComparableStack key : matchingKeys) {
                if (toConsume <= 0) break;
                int countInStack = availableCounts.get(key);
                int consumedFromThisStack = Math.min(toConsume, countInStack);
                availableCounts.put(key, countInStack - consumedFromThisStack);
                toConsume -= consumedFromThisStack;
            }
        }
        return new CheckResult(color, totalAvailable);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        try {
            ComparableStack comp = getRecipeOutput(stack);
            String s = I18n.format(this.getTranslationKey() + ".name").trim();
            ItemStack out = (comp != null) ? comp.toStack() : ItemStack.EMPTY;

            if (out.isEmpty()) {
                return TextFormatting.RED + "Broken Template" + TextFormatting.RESET;
            }

            String s1 = out.getDisplayName().trim();
            return s + " " + s1;
        } catch (Exception ex) {
            return TextFormatting.RED + "Broken Template" + TextFormatting.RESET;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            for (ComparableStack comp : AssemblerRecipes.recipeList) {
                items.add(writeType(new ItemStack(ModItems.assembly_template), comp));
            }
        }
    }

    static void countItem(Map<ComparableStack, Integer> availableCounts, IItemHandler inventory, int slot) {
        ItemStack stackInSlot = inventory.getStackInSlot(slot);
        if (!stackInSlot.isEmpty()) {
            ItemStack keyStack = stackInSlot.copy();
            keyStack.setCount(1);
            ComparableStack key = stackInSlot.hasTagCompound() ? new NbtComparableStack(keyStack) : new ComparableStack(keyStack);
            availableCounts.merge(key, stackInSlot.getCount(), Integer::sum);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn) {
        Map<ComparableStack, Integer> availableCounts = null;
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;

        if (screen instanceof GUIMachineAssembler assemblerGui && assemblerGui.getSlotUnderMouse() != null) {
            IItemHandler assemblerInventory = assemblerGui.getInventory();
            if (assemblerInventory != null) {
                availableCounts = new HashMap<>();
                for (int slot = 6; slot < 18; slot++) {
                    countItem(availableCounts, assemblerInventory, slot);
                }
            }
        } else if (screen instanceof GUIAssemfac assemfacGui && assemfacGui.getSlotUnderMouse() != null) {
            int inventoryIndex = assemfacGui.getSlotUnderMouse().getSlotIndex();
            if (inventoryIndex >= 17 && (inventoryIndex - 17) % 14 == 0 && inventoryIndex < assemfacGui.getInventory().getSlots()) {
                int unitIndex = (inventoryIndex - 17) / 14;
                IItemHandler assemblerInventory = assemfacGui.getInventory();
                if (assemblerInventory != null) {
                    availableCounts = new HashMap<>();
                    int startSlot = 5 + unitIndex * 14;
                    int endSlot = 16 + unitIndex * 14;
                    for (int slot = startSlot; slot <= endSlot; slot++) {
                        countItem(availableCounts, assemblerInventory, slot);
                    }
                }
            }
        }

        ComparableStack out = getRecipeOutput(stack);
        if (out == null) {
            tooltip.add("I AM ERROR");
            return;
        }

        AssemblerRecipe recipe = AssemblerRecipes.recipes.get(out);
        if (recipe == null) {
            tooltip.add("I AM ERROR");
            return;
        }

        Object[] in = recipe.ingredients;
        if (in == null) {
            tooltip.add("I AM ERROR");
            return;
        }

        ItemStack output = out.toStack();

        tooltip.add(TextFormatting.BOLD + I18nUtil.resolveKey("info.template_out"));
        tooltip.add(TextFormatting.GREEN + " " + output.getCount() + "x " + output.getDisplayName());
        tooltip.add(TextFormatting.BOLD + I18nUtil.resolveKey("info.template_in_p"));

        Map<ComparableStack, Integer> workingCounts = availableCounts != null ? new HashMap<>(availableCounts) : null;
        for (Object o : in) {
            String prefix = TextFormatting.RED.toString();
            if (o instanceof AStack ingredient) {
                if (workingCounts != null) {
                    CheckResult result = checkAndConsume(ingredient, workingCounts);
                    prefix = result.color();
                }
                if (ingredient instanceof ComparableStack input) {
                    tooltip.add(" " + prefix + input.toStack().getCount() + "x " + input.toStack().getDisplayName());
                } else if (ingredient instanceof OreDictStack input) {
                    NonNullList<ItemStack> ores = OreDictionary.getOres(input.name);
                    if (!ores.isEmpty()) {
                        ItemStack inStack = ores.get((int) (Math.abs(System.currentTimeMillis() / 1000) % ores.size()));
                        tooltip.add(" " + prefix + input.count() + "x " + inStack.getDisplayName());
                    } else {
                        tooltip.add(prefix + "I AM ERROR - No OrdDict match found for " + input.name);
                    }
                }
            }
        }

        tooltip.add(TextFormatting.BOLD + I18nUtil.resolveKey("info.template_time"));
        tooltip.add(TextFormatting.BLUE + " " + Math.floor((float) (getProcessTime(stack)) / 20 * 100) / 100 + " " + I18nUtil.resolveKey("info" +
                ".template_seconds"));
    }

    record CheckResult(String color, int available) {
    }
}