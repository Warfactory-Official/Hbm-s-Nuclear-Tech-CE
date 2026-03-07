package com.hbm.handler;

import com.hbm.capability.NTMFluidCapabilityHandler;
import com.hbm.inventory.fluid.FluidType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.Nullable;

public class FluidTankV2Handler extends FluidHandlerItemStack {

    public FluidTankV2Handler(ItemStack container, int capacity) {
        super(container, capacity);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0) return 0;

        if (doFill) {
            FluidType type = NTMFluidCapabilityHandler.getFluidType(resource.getFluid());
            if (type != null) {
                container.setItemDamage(type.getID());
            }
        }

        return super.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        FluidStack drained = super.drain(maxDrain, doDrain);

        if (doDrain && drained != null && drained.amount > 0) {
            FluidStack remaining = super.getFluid();
            if (remaining == null || remaining.amount <= 0) {
                container.setItemDamage(0); // metadata = empty
                container.setTagCompound(null);
            }
        }

        return drained;
    }

    @Override
    protected void setFluid(FluidStack fluid) {
        super.setFluid(fluid);
        if (fluid != null) {
            NBTTagCompound tag = container.getOrCreateSubCompound("fluid");
            fluid.writeToNBT(tag);
        } else {
            container.removeSubCompound("fluid");
        }
    }

    @Override
    public @Nullable FluidStack getFluid() {
        NBTTagCompound tag = container.getSubCompound("fluid");
        if (tag != null) {
            return FluidStack.loadFluidStackFromNBT(tag);
        }
        return null;
    }
}
