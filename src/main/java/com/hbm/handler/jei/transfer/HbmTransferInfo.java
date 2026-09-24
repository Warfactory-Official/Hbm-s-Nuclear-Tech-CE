package com.hbm.handler.jei.transfer;

import com.hbm.items.machine.ItemFluidIcon;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntLists;
import mezz.jei.JustEnoughItems;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import mezz.jei.config.ServerInfo;
import mezz.jei.network.packets.PacketRecipeTransfer;
import mezz.jei.startup.StackHelper;
import mezz.jei.util.Translator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class HbmTransferInfo<C extends Container> implements IRecipeTransferHandler<C> {

    private static IRecipeTransferHandlerHelper handlerHelper;
    private static StackHelper stackHelper;

    private final Class<C> containerClass;
    private final int[] recipeSlots;
    private final int[] playerSlots;
    private final List<Integer> craftingSlotsList;
    private final List<Integer> inventorySlotsList;

    public HbmTransferInfo(Class<C> containerClass, int[] recipeSlots, int[] playerSlots) {
        this.containerClass = containerClass;
        Arrays.sort(recipeSlots);
        Arrays.sort(playerSlots);
        this.recipeSlots = recipeSlots;
        this.playerSlots = playerSlots;
        this.craftingSlotsList = IntLists.unmodifiable(new IntArrayList(recipeSlots));
        this.inventorySlotsList = IntLists.unmodifiable(new IntArrayList(playerSlots));
    }

    private static boolean isFluidIcon(IGuiIngredient<ItemStack> ing) {
        for (ItemStack alt : ing.getAllIngredients()) {
            if (alt != null && !alt.isEmpty() && !(alt.getItem() instanceof ItemFluidIcon)) return false;
        }
        return true;
    }

    /**
     * Capture JEI helpers once at plugin init time, before any recipes transfer.
     */
    public static void init(IJeiHelpers helpers) {
        handlerHelper = helpers.recipeTransferHandlerHelper();
        stackHelper = (StackHelper) helpers.getStackHelper();
    }

    /**
     * One-liner registration helper.
     */
    public static <C extends Container> void register(IRecipeTransferRegistry r, Class<C> cc, String uid,
                                                      int[] recipeSlots, int[] playerSlots) {
        r.addRecipeTransferHandler(new HbmTransferInfo<>(cc, recipeSlots, playerSlots), uid);
    }

    /**
     * Build a contiguous {@code [start, start+count)} index array.
     */
    public static int[] range(int start, int count) {
        int[] a = new int[count];
        for (int i = 0; i < count; i++) a[i] = start + i;
        return a;
    }

    @Override
    public Class<C> getContainerClass() {
        return containerClass;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(C container, IRecipeLayout recipeLayout, EntityPlayer player,
                                                         boolean maxTransfer, boolean doTransfer) {
        if (!ServerInfo.isJeiOnServer()) {
            return handlerHelper.createUserErrorWithTooltip(
                    Translator.translateToLocal("jei.tooltip.error.recipe.transfer.no.server"));
        }

        Map<Integer, ? extends IGuiIngredient<ItemStack>> rawIngredients = recipeLayout.getItemStacks()
                                                                                       .getGuiIngredients();
        int itemInputCount = 0;
        int fluidInputCount = 0;
        for (IGuiIngredient<ItemStack> ing : rawIngredients.values()) {
            if (!ing.isInput() || ing.getAllIngredients().isEmpty()) continue;
            if (isFluidIcon(ing)) fluidInputCount++;
            else itemInputCount++;
        }

        if (itemInputCount > recipeSlots.length) {
            return handlerHelper.createInternalError();
        }

        Map<Integer, ? extends IGuiIngredient<ItemStack>> matchIngredients;
        if (fluidInputCount == 0) {
            matchIngredients = rawIngredients;
        } else {
            Int2ObjectOpenHashMap<IGuiIngredient<ItemStack>> filtered = new Int2ObjectOpenHashMap<>(
                    rawIngredients.size() - fluidInputCount);
            for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> e : rawIngredients.entrySet()) {
                IGuiIngredient<ItemStack> ing = e.getValue();
                if (ing.isInput() && isFluidIcon(ing)) continue;
                filtered.put(e.getKey().intValue(), ing);
            }
            matchIngredients = filtered;
        }

        Int2ObjectOpenHashMap<ItemStack> availableItems = new Int2ObjectOpenHashMap<>(
                recipeSlots.length + playerSlots.length);
        int filledRecipeSlotCount = 0;
        for (int idx : recipeSlots) {
            Slot s = container.getSlot(idx);
            ItemStack stack = s.getStack();
            if (stack.isEmpty()) continue;
            if (!s.canTakeStack(player)) return handlerHelper.createInternalError();
            filledRecipeSlotCount++;
            availableItems.put(idx, stack.copy());
        }
        int emptyInventorySlotCount = 0;
        for (int idx : playerSlots) {
            ItemStack stack = container.getSlot(idx).getStack();
            if (stack.isEmpty()) emptyInventorySlotCount++;
            else availableItems.put(idx, stack.copy());
        }
        if (filledRecipeSlotCount - itemInputCount > emptyInventorySlotCount) {
            return handlerHelper.createUserErrorWithTooltip(
                    Translator.translateToLocal("jei.tooltip.error.recipe.transfer.inventory.full"));
        }

        StackHelper.MatchingItemsResult match = stackHelper.getMatchingItems(availableItems, matchIngredients);
        if (!match.missingItems.isEmpty()) {
            return handlerHelper.createUserErrorForSlots(
                    Translator.translateToLocal("jei.tooltip.error.recipe.transfer.missing"), match.missingItems);
        }

        if (doTransfer) {
            PacketRecipeTransfer packet = new PacketRecipeTransfer(match.matchingItems, craftingSlotsList,
                    inventorySlotsList, maxTransfer ? Integer.MAX_VALUE : 1, false, false);
            JustEnoughItems.getProxy().sendPacketToServer(packet);
        }
        return null;
    }
}
