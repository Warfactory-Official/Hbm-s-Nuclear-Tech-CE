package com.hbm.handler.jei;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.main.MainRegistry;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IIngredientType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Small bridge between NTM's JEI recipe categories and <strong>EMI</strong> (retroEMI), which loads JEI categories
 * through its own JEI emulation layer ("JEMI"). JEMI builds EMI recipes from the slots a category sets up in
 * {@code IRecipeCategory#setRecipe}, so every slot becomes an ingredient: an input or an output.
 * That breaks a few things NTM relies on:
 * </p>
 *
 * <ul>
 *   <li>Machine / blueprint slots are registered as <strong>outputs</strong>, so EMI thinks every recipe produces
 *       its machine, and disables the recipe tree when there is more than one machine.</li>
 *   <li>Fluids are shown as {@link ItemFluidIcon} items, which the recipe tree cannot connect to real fluids.</li>
 *   <li>Chanced outputs are shown as one cycling slot with tooltip NBT, which EMI treats as a guaranteed,
 *       different item and again disables the tree.</li>
 * </ul>
 *
 * <p>
 * Every method here is safe to call unconditionally: with plain JEI (or any non-EMI layout) it behaves exactly like
 * the regular {@link IGuiItemStackGroup#init}/{@link IGuiItemStackGroup#set} pair or does nothing.
 * </p>
 *
 * <p><strong>Usage</strong>, inside {@code setRecipe}:</p>
 * <pre>{@code
 * EmiCompat.initSlot(layout, 0, true, 47, 23, inputStacks);                 // regular slot, fluid icons become real fluids in EMI
 * EmiCompat.initDisplaySlot(layout, 1, false, 74, 31, machines);            // visible & hoverable, never an ingredient in EMI
 * EmiCompat.initDisplaySlot(layout, 2, false, 102, 24, chancedStacks);      // visible cycling slot for a chanced output...
 * EmiCompat.addHiddenOutput(layout, cleanStack, 1, 0.5F);                   // ...plus the real outputs EMI should count
 * }</pre>
 *
 * <p><strong>Implementation note:</strong> the display slot and hidden output hooks reach into JEMI internals by
 * reflection. If JEMI changes, the failure is logged once and those two methods degrade to plain JEI behaviour.</p>
 */
public final class EmiCompat {

    /**
     * Whether EMI is installed at all. Prefer {@link #isEmiLayout} inside {@code setRecipe}.
     */
    public static final boolean LOADED = Loader.isModLoaded("emi");

    /**
     * Fluid slot indices are shifted by this amount so they never collide with the fluid slots JEI's own layout
     * may have registered for the same recipe.
     */
    private static final int FLUID_SLOT_OFFSET = 1000;

    private EmiCompat() {
    }

    /**
     * Check whether the given layout is EMI's recipe builder rather than a real JEI layout.
     *
     * <p>EMI still creates a real JEI layout for the same recipe while rendering, so this must be checked per
     * layout instead of relying on {@link #LOADED} alone.</p>
     *
     * @param layout the layout passed to {@code setRecipe}
     * @return {@code true} if EMI is building a recipe from this layout
     */
    public static boolean isEmiLayout(IRecipeLayout layout) {
        return LOADED && layout.getClass().getName().startsWith("dev.emi.");
    }

    /**
     * Convert a single {@link ItemFluidIcon} stack into its Forge fluid equivalent.
     *
     * @param stack a fluid icon stack
     * @return the Forge {@link FluidStack} with the icon's amount, or {@code null} if the stack is not a fluid icon
     *         or its NTM fluid has no Forge counterpart
     */
    @Nullable
    public static FluidStack toForgeFluid(ItemStack stack) {
        FluidType type = ItemFluidIcon.getFluidType(stack);
        if (type == null) return null;
        Fluid fluid = type.getFF();
        if (fluid == null) return null;
        return new FluidStack(fluid, Math.max(ItemFluidIcon.getQuantity(stack), 1));
    }

    /**
     * Convert a whole slot's worth of stacks via {@link #toForgeFluid}.
     *
     * @param stacks the stacks of one slot
     * @return the converted fluids, or {@code null} if the list is empty or <strong>any</strong> stack cannot be converted
     */
    @Nullable
    public static List<FluidStack> toForgeFluids(List<ItemStack> stacks) {
        if (stacks == null || stacks.isEmpty()) return null;
        List<FluidStack> fluids = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            FluidStack fluid = toForgeFluid(stack);
            if (fluid == null) return null;
            fluids.add(fluid);
        }
        return fluids;
    }

    /**
     * Drop-in replacement for {@code getItemStacks().init(...)} followed by {@code set(...)}.
     *
     * <p>
     * In EMI, a slot consisting only of convertible {@link ItemFluidIcon} stacks is registered as a Forge fluid
     * slot instead, so the recipe tree sees real fluids. It is still rendered as the NTM fluid icon.
     * Everything else is a regular item slot.
     * </p>
     *
     * @param layout the layout passed to {@code setRecipe}
     * @param index  the slot index, same as for {@link IGuiItemStackGroup#init}
     * @param input  whether the slot is an input
     * @param x      slot x, same coordinates as {@link IGuiItemStackGroup#init}
     * @param y      slot y
     * @param stacks the stacks cycled in this slot
     */
    public static void initSlot(IRecipeLayout layout, int index, boolean input, int x, int y, List<ItemStack> stacks) {
        if (isEmiLayout(layout)) {
            List<FluidStack> fluids = toForgeFluids(stacks);
            if (fluids != null) {
                IGuiFluidStackGroup group = layout.getFluidStacks();
                group.init(FLUID_SLOT_OFFSET + index, input, new FluidIconRenderer(stacks), x, y, 18, 18, 1, 1);
                group.set(FLUID_SLOT_OFFSET + index, fluids);
                return;
            }
        }
        IGuiItemStackGroup group = layout.getItemStacks();
        group.init(index, input, x, y);
        group.set(index, stacks);
    }

    /**
     * Initialize a slot that is shown and hoverable, but is <strong>not</strong> an ingredient in EMI.
     *
     * <p>
     * Use it for anything that is neither consumed nor produced: machines, blueprints, or the visible part of a
     * chanced output (pair it with {@link #addHiddenOutput} in that case). In JEI this is a regular slot with the
     * given role; in EMI the slot is always treated as an input for display purposes and then removed from the
     * recipe's ingredient list, so it doesn't affect recipe lookups or the recipe tree.
     * </p>
     *
     * @param layout the layout passed to {@code setRecipe}
     * @param index  the slot index
     * @param input  the slot role used by JEI
     * @param x      slot x
     * @param y      slot y
     * @param stacks the stacks cycled in this slot
     */
    public static void initDisplaySlot(IRecipeLayout layout, int index, boolean input, int x, int y, List<ItemStack> stacks) {
        boolean emi = isEmiLayout(layout);
        IGuiItemStackGroup group = layout.getItemStacks();
        group.init(index, input || emi, x, y);
        group.set(index, stacks);
        if (emi && Reflection.init()) {
            try {
                List<?> slots = (List<?>) Reflection.slots.get(layout);
                Object acceptor = Reflection.acceptor.get(slots.getLast());
                ((List<?>) Reflection.ingredients.get(layout)).remove(acceptor);
            } catch (ReflectiveOperationException e) {
                Reflection.fail(e);
            }
        }
    }

    /**
     * Register an output that EMI counts but never renders. Does nothing outside EMI.
     *
     * <p>
     * Meant for chanced outputs: show the stacks with {@link #initDisplaySlot}, then add one hidden output per
     * possible result. Pass a <strong>clean</strong> stack (no chance tooltip NBT), otherwise EMI won't match it
     * against the real item.
     * </p>
     *
     * @param layout the layout passed to {@code setRecipe}
     * @param stack  the clean output stack; its count is ignored in favour of {@code amount}
     * @param amount the amount produced when the output rolls, at least 1
     * @param chance the chance in {@code (0, 1]}; values {@code >= 1} mean guaranteed
     */
    public static void addHiddenOutput(IRecipeLayout layout, ItemStack stack, long amount, float chance) {
        if (!isEmiLayout(layout) || stack.isEmpty() || !Reflection.init()) return;
        try {
            Reflection.addInvisible.invoke(layout, Reflection.outputRole);
            List<?> ingredients = (List<?>) Reflection.ingredients.get(layout);
            Object acceptor = ingredients.getLast();
            Reflection.addIngredient.invoke(acceptor, VanillaTypes.ITEM, stack);
            for (Object emiStack : (List<?>) Reflection.stacks.get(acceptor)) {
                Reflection.setAmount.invoke(emiStack, Math.max(amount, 1L));
                if (chance < 1F) Reflection.setChance.invoke(emiStack, chance);
            }
        } catch (ReflectiveOperationException e) {
            Reflection.fail(e);
        }
    }

    /**
     * Variant of {@link #addHiddenOutput(IRecipeLayout, ItemStack, long, float)} taking the expected yield per craft.
     *
     * <p>The yield is split into {@code ceil(expected)} items at {@code expected / ceil(expected)} chance, which keeps
     * the average exact. For example, {@code 2.5} becomes 3 items at ~83%.</p>
     *
     * @param layout   the layout passed to {@code setRecipe}
     * @param stack    the clean output stack
     * @param expected the average amount produced per craft; non-positive values are ignored
     */
    public static void addHiddenOutput(IRecipeLayout layout, ItemStack stack, double expected) {
        if (expected <= 0) return;
        long amount = (long) Math.ceil(expected);
        addHiddenOutput(layout, stack, amount, (float) (expected / amount));
    }

    /**
     * Lazily resolved handles into JEMI's {@code JemiRecipeLayoutBuilder}. Resolved once; on failure every hook
     * becomes a no-op.
     */
    private static final class Reflection {

        private static boolean initialized;
        private static boolean failed;
        private static Field slots;
        private static Field ingredients;
        private static Field acceptor;
        private static Field stacks;
        private static Method addInvisible;
        private static Method addIngredient;
        private static Method setAmount;
        private static Method setChance;
        private static Object outputRole;

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static boolean init() {
            if (initialized) return !failed;
            initialized = true;
            try {
                Class<?> builder = Class.forName("dev.emi.emi.jemi.impl.JemiRecipeLayoutBuilder");
                Class<?> slotBuilder = Class.forName("dev.emi.emi.jemi.impl.JemiRecipeSlotBuilder");
                Class<?> acceptorClass = Class.forName("dev.emi.emi.jemi.impl.JemiIngredientAcceptor");
                Class<?> emiStack = Class.forName("dev.emi.emi.api.stack.EmiStack");
                Class roleClass = Class.forName("shim.mezz.jei.api.recipe.RecipeIngredientRole");
                slots = builder.getField("slots");
                ingredients = builder.getField("ingredients");
                acceptor = slotBuilder.getField("acceptor");
                stacks = acceptorClass.getField("stacks");
                addInvisible = builder.getMethod("addInvisibleIngredients", roleClass);
                addIngredient = acceptorClass.getMethod("addIngredient", IIngredientType.class, Object.class);
                setAmount = emiStack.getMethod("setAmount", long.class);
                setChance = emiStack.getMethod("setChance", float.class);
                outputRole = Enum.valueOf(roleClass, "OUTPUT");
            } catch (ReflectiveOperationException | RuntimeException e) {
                fail(e);
            }
            return !failed;
        }

        private static void fail(Exception e) {
            if (!failed) MainRegistry.logger.error("Failed to hook into EMI's JEI compatibility layer", e);
            failed = true;
        }
    }

    private static void drawStack(Minecraft minecraft, int x, int y, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();
        minecraft.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        GlStateManager.disableBlend();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Renders a Forge fluid slot as its matching {@link ItemFluidIcon}, so EMI recipe tabs keep NTM's look while the
     * underlying ingredient is a real fluid.
     */
    private static final class FluidIconRenderer implements IIngredientRenderer<FluidStack> {

        private final List<ItemStack> icons;

        private FluidIconRenderer(List<ItemStack> icons) {
            this.icons = icons;
        }

        @Nullable
        private ItemStack getIcon(@Nullable FluidStack fluid) {
            if (fluid == null || icons.isEmpty()) return null;
            for (ItemStack icon : icons) {
                FluidType type = ItemFluidIcon.getFluidType(icon);
                if (type != null && type.getFF() == fluid.getFluid()) return icon;
            }
            return icons.getFirst();
        }

        @Override
        public void render(@NotNull Minecraft minecraft, int x, int y, @Nullable FluidStack fluid) {
            ItemStack icon = getIcon(fluid);
            if (icon != null) drawStack(minecraft, x, y, icon);
        }

        @Override
        public @NotNull List<String> getTooltip(@NotNull Minecraft minecraft, FluidStack fluid, @NotNull ITooltipFlag flag) {
            List<String> tooltip = new ArrayList<>(2);
            tooltip.add(fluid.getLocalizedName());
            tooltip.add(fluid.amount + "mB");
            return tooltip;
        }
    }
}
