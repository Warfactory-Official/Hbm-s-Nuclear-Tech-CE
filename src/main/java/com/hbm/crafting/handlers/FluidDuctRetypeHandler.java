package com.hbm.crafting.handlers;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidIDMulti;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;

public final class FluidDuctRetypeHandler extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    private static final Item fluid_duct_neo;

    static {
        fluid_duct_neo = Item.getItemFromBlock(ModBlocks.fluid_duct_neo);
        if (fluid_duct_neo == Items.AIR) throw new AssertionError("Neo Fluid Duct item not found");
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        boolean hasId = false;
        int ducts = 0;
        for (int s = 0; s < inv.getSizeInventory(); s++) {
            ItemStack st = inv.getStackInSlot(s);
            if (st.isEmpty()) continue;
            Item it = st.getItem();
            if (it == ModItems.fluid_identifier_multi) {
                if (hasId) return false;
                hasId = true;
            } else if (it == ModItems.fluid_duct || it == fluid_duct_neo) {
                ducts++;
            } else {
                return false;
            }
        }
        return hasId && ducts > 0;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack idStack = ItemStack.EMPTY;
        int ducts = 0;
        for (int s = 0; s < inv.getSizeInventory(); s++) {
            ItemStack st = inv.getStackInSlot(s);
            if (st.isEmpty()) continue;
            Item it = st.getItem();
            if (it == ModItems.fluid_identifier_multi) {
                idStack = st;
            } else if (it == ModItems.fluid_duct || it == fluid_duct_neo) {
                ducts++;
            }
        }
        int fluidMeta = ItemFluidIDMulti.getType(idStack, true).getID();
        return new ItemStack(ModItems.fluid_duct, ducts, fluidMeta);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> rem = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

        for (int s = 0; s < inv.getSizeInventory(); s++) {
            ItemStack st = inv.getStackInSlot(s);
            if (st.isEmpty()) continue;
            if (st.getItem() == ModItems.fluid_identifier_multi) {
                ItemStack back = st.copy();
                back.setCount(1);
                rem.set(s, back);
                break;
            }
        }

        return rem;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ing = NonNullList.create();
        ing.add(Ingredient.fromItems(ModItems.fluid_identifier_multi));
        ing.add(Ingredient.fromStacks(
                new ItemStack(ModItems.fluid_duct, 1, OreDictionary.WILDCARD_VALUE),
                new ItemStack(fluid_duct_neo, 1, OreDictionary.WILDCARD_VALUE)
        ));
        return ing;
    }
}
