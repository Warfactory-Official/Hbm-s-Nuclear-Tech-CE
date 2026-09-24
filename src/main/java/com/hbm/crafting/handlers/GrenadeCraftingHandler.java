package com.hbm.crafting.handlers;

import com.hbm.items.ModItems;
import com.hbm.items.weapon.grenade.ItemGrenadeExtra.EnumGrenadeExtra;
import com.hbm.items.weapon.grenade.ItemGrenadeFilling.EnumGrenadeFilling;
import com.hbm.items.weapon.grenade.ItemGrenadeFuze.EnumGrenadeFuze;
import com.hbm.items.weapon.grenade.ItemGrenadeShell.EnumGrenadeShell;
import com.hbm.items.weapon.grenade.ItemGrenadeUniversal;
import com.hbm.util.EnumUtil;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class GrenadeCraftingHandler extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        if (hasForeignObject(inv)) return false; // can't be non-grenade items and can't be more than 4 items total
        EnumGrenadeShell shell = getFirst(inv, ModItems.grenade_shell, EnumGrenadeShell.VALUES); // only one shell, null otherwise
        EnumGrenadeFilling filling = getFirst(inv, ModItems.grenade_filling, EnumGrenadeFilling.VALUES); // only one filling, null otherwise
        if (shell == null || filling == null) return false;
        if (!filling.compatibleShells.contains(shell)) return false;
        EnumGrenadeFuze fuze = getFirst(inv, ModItems.grenade_fuze, EnumGrenadeFuze.VALUES); // only one fuze, null otherwise
        // this leaves the extra unaccounted for, but the restrictions we put in place will allow exactly one without dedicated check
        return fuze != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        EnumGrenadeShell shell = getFirst(inv, ModItems.grenade_shell, EnumGrenadeShell.VALUES);
        EnumGrenadeFilling filling = getFirst(inv, ModItems.grenade_filling, EnumGrenadeFilling.VALUES);
        EnumGrenadeFuze fuze = getFirst(inv, ModItems.grenade_fuze, EnumGrenadeFuze.VALUES);
        EnumGrenadeExtra extra = getFirst(inv, ModItems.grenade_extra, EnumGrenadeExtra.VALUES); // if this is null, then we don't care, MAKE works with a null extra too
        if (shell == null || filling == null || fuze == null) return ItemStack.EMPTY;
        return ItemGrenadeUniversal.make(shell, filling, fuze, extra);
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 4;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    // why write the same crap four times when you can just use your massive cock instead
    private static <T extends Enum<T>> T getFirst(InventoryCrafting inv, Item itemType, T[] values) { // god i love generics
        T first = null;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() == itemType) {
                if (first != null) return null;
                first = EnumUtil.grabEnumSafely(values, stack.getMetadata());
            }
        }
        return first;
    }

    // this should weed out non-grenade grids quickly as to not waste too much CPU time
    private static boolean hasForeignObject(InventoryCrafting inv) {
        int itemCount = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();
            if (item != ModItems.grenade_shell
                    && item != ModItems.grenade_filling
                    && item != ModItems.grenade_fuze
                    && item != ModItems.grenade_extra) return true;
            itemCount++;
            if (itemCount > 4) return true;
        }
        return false;
    }
}
