package com.hbm.hazard.transformer;

import com.hbm.capability.NTMFluidCapabilityHandler;
import com.hbm.hazard.HazardEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.List;

public class HazardTransformerForgeFluid implements IHazardTransformer {
    // <Fluid's registry name, List of hazards>
    public static final Object2ObjectOpenHashMap<String, ObjectArrayList<HazardEntry>> FLUID_HAZARDS = new Object2ObjectOpenHashMap<>();
    public static boolean applyToNTMContainer = true;
    
    @Override
    public void transformPre(ItemStack stack, List<HazardEntry> entries) {
    }

    @Override
    public void transformPost(ItemStack stack, List<HazardEntry> entries) {
        if (FLUID_HAZARDS.isEmpty()) return;
        if (!applyToNTMContainer && NTMFluidCapabilityHandler.isNtmFluidContainer(stack.getItem())) return;
        if (!stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) return;
        final IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler == null) return;
        final IFluidTankProperties[] properties = handler.getTankProperties();
        if (properties == null) return;
        for (IFluidTankProperties property : properties) {
            FluidStack content = property.getContents();
            if (content == null) continue;
            final String fluidName = content.getFluid().getName();
            if (!FLUID_HAZARDS.containsKey(fluidName)) continue;
            double modifier = content.amount / 1000.0;
            ObjectArrayList<HazardEntry> hazardEntries = FLUID_HAZARDS.get(fluidName);
            for (HazardEntry entry : hazardEntries) entries.add(entry.clone(modifier));
        }
    }
}
