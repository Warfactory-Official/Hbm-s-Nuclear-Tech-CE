package com.hbm.handler.jei;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidIcon;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FluidIconRecipeRegistryPlugin implements IRecipeRegistryPlugin {
    @Nullable
    private IRecipeRegistry recipeRegistry;

    public void setRecipeRegistry(@Nullable IRecipeRegistry recipeRegistry) {
        this.recipeRegistry = recipeRegistry;
    }

    @Override
    public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
        IFocus<FluidStack> fluidFocus = createFluidFocus(focus);
        if (fluidFocus == null) {
            return Collections.emptyList();
        }

        IRecipeRegistry registry = this.recipeRegistry;
        if (registry == null) {
            return Collections.emptyList();
        }

        List<IRecipeCategory> categories = registry.getRecipeCategories(fluidFocus);
        if (categories.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> uids = new ArrayList<>(categories.size());
        for (IRecipeCategory category : categories) {
            String uid = category.getUid();
            if (!uids.contains(uid)) {
                uids.add(uid);
            }
        }
        return uids;
    }

    @Override
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        IFocus<FluidStack> fluidFocus = createFluidFocus(focus);
        if (fluidFocus == null) {
            return Collections.emptyList();
        }

        IRecipeRegistry registry = this.recipeRegistry;
        if (registry == null) {
            return Collections.emptyList();
        }

        return registry.getRecipeWrappers(recipeCategory, fluidFocus);
    }

    @Override
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory) {
        return Collections.emptyList();
    }

    @Nullable
    private <V> IFocus<FluidStack> createFluidFocus(IFocus<V> focus) {
        IRecipeRegistry registry = this.recipeRegistry;
        if (registry == null) {
            return null;
        }

        V value = focus.getValue();
        if (!(value instanceof ItemStack stack) || stack.isEmpty() || stack.getItem() != ModItems.fluid_icon) {
            return null;
        }

        FluidType fluidType = ItemFluidIcon.getFluidType(stack);
        if (fluidType == null) {
            return null;
        }

        Fluid forgeFluid = fluidType.getFF();
        if (forgeFluid == null) {
            return null;
        }

        int amount = ItemFluidIcon.getQuantity(stack);
        if (amount <= 0) {
            amount = Fluid.BUCKET_VOLUME;
        }

        return registry.createFocus(focus.getMode(), new FluidStack(forgeFluid, amount));
    }
}
