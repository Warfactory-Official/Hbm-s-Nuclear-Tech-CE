package com.hbm.capability;

import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.MainRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Deal with fluid type conversions
 *
 * @author mlbv
 */
public class NTMFluidCapabilityHandler {

    public static final ResourceLocation HBM_FLUID_CAPABILITY = new ResourceLocation("hbm", "fluid_container_wrapper");

    private static final Set<Item> NTM_CONTAINERS = new ObjectOpenHashSet<>();
    private static final Set<Item> NTM_FULL_CONTAINERS = new ObjectOpenHashSet<>();
    private static final Set<Item> NTM_EMPTY_CONTAINERS = new ObjectOpenHashSet<>();
    private static final Object2ObjectOpenHashMap<String, FluidType> FF_TO_NTMF_MAP = new Object2ObjectOpenHashMap<>(256);
    private static final IFluidTankProperties[] NO_TANK_PROPS = new IFluidTankProperties[0];

    public static void initialize() {
        for (FluidType type : Fluids.getAll()) {
            if (type == null || type == Fluids.NONE || type.getName() == null) continue;

            String hbmFluidName = type.getName();
            Fluid forgeFluid = FluidRegistry.getFluid(type.getFFName());

            if (forgeFluid != null) {
                FF_TO_NTMF_MAP.put(forgeFluid.getName(), type);
            } else {
                MainRegistry.logger.warn("Could not find matching ForgeFluid for FluidType {}", hbmFluidName);
            }
        }

        for (FluidContainerRegistry.FluidContainer container : FluidContainerRegistry.allContainers) {
            Item full = container.fullContainer().getItem();
            NTM_CONTAINERS.add(full);
            NTM_FULL_CONTAINERS.add(full);
            if (container.emptyContainer() != null && !container.emptyContainer().isEmpty()) {
                Item empty = container.emptyContainer().getItem();
                NTM_CONTAINERS.add(empty);
                NTM_EMPTY_CONTAINERS.add(empty);
            }
        }

        MinecraftForge.EVENT_BUS.register(new NTMFluidCapabilityHandler());
        MainRegistry.logger.info("NTMFluidCapabilityHandler init: mapped {} Forge Fluids, tracking {} items.", FF_TO_NTMF_MAP.size(), NTM_CONTAINERS.size());
    }

    @Nullable
    public static FluidType getFluidType(@NotNull Fluid forgeFluid) {
        return FF_TO_NTMF_MAP.get(forgeFluid.getName());
    }

    public static boolean isNtmFluidContainer(@NotNull Item item) {
        return NTM_CONTAINERS.contains(item);
    }

    /**
     * @return true if the item ever appears as a full container (meta-insensitive).
     */
    public static boolean isFullNtmFluidContainer(@NotNull Item item) {
        return NTM_FULL_CONTAINERS.contains(item);
    }

    /**
     * @return true if the item ever appears as an empty container (meta-insensitive).
     */
    public static boolean isEmptyNtmFluidContainer(@NotNull Item item) {
        return NTM_EMPTY_CONTAINERS.contains(item);
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.isEmpty() || !NTM_CONTAINERS.contains(stack.getItem())) return;
        event.addCapability(HBM_FLUID_CAPABILITY, new Wrapper(stack));
    }

    private static final class Wrapper implements ICapabilityProvider, IFluidHandlerItem {
        private ItemStack container;

        private Wrapper(@NotNull ItemStack container) {
            this.container = container;
        }

        @Override
        public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;
        }

        @Nullable
        @Override
        public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY) {
                return CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.cast(this);
            }
            return null;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            // Fast path: if this is a known full container, capacity==content
            FluidContainerRegistry.FluidContainer full = FluidContainerRegistry.getFluidContainer(this.container);
            if (full != null) {
                Fluid f = full.type().getFF();
                if (f == null) return NO_TANK_PROPS;
                FluidStack contents = new FluidStack(f, full.content());
                return new IFluidTankProperties[]{new TankProperties(contents, full.content())};
            }

            // Otherwise, treat it as an empty candidate: capacity is the precomputed max for this empty stack
            int cap = FluidContainerRegistry.getMaxFillCapacity(this.container);
            if (cap <= 0) return NO_TANK_PROPS;
            return new IFluidTankProperties[]{new TankProperties(null, cap)};
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (resource == null || getContentsInternal() != null || resource.tag != null) return 0;
            FluidContainerRegistry.FluidContainer fillRecipe = FluidContainerRegistry.getFillRecipe(this.container, resource.getFluid());
            if (fillRecipe == null) return 0;
            int needed = fillRecipe.content();
            if (resource.amount < needed) return 0;
            if (doFill) this.container = fillRecipe.fullContainer().copy();
            return needed;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0 || resource.tag != null) return null;
            FluidStack contents = getContentsInternal();
            if (contents == null || !contents.isFluidEqual(resource) || resource.amount < contents.amount) return null;
            if (doDrain) {
                FluidContainerRegistry.FluidContainer fc = FluidContainerRegistry.getFluidContainer(this.container);
                if (fc == null) return null;
                if (fc.emptyContainer() != null) this.container = fc.emptyContainer().copy();
                else this.container = ItemStack.EMPTY;
            }
            return contents.copy();
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            FluidStack contents = getContentsInternal();
            if (contents == null || maxDrain < contents.amount) return null;
            return this.drain(contents, doDrain);
        }

        @NotNull
        @Override
        public ItemStack getContainer() {
            return this.container;
        }

        @Nullable
        private FluidStack getContentsInternal() {
            FluidContainerRegistry.FluidContainer fc = FluidContainerRegistry.getFluidContainer(this.container);
            if (fc == null) return null;
            Fluid forgeFluid = fc.type().getFF();
            return forgeFluid == null ? null : new FluidStack(forgeFluid, fc.content());
        }

        private final class TankProperties implements IFluidTankProperties {
            @Nullable
            final FluidStack contents;
            final int capacity;

            private TankProperties(@Nullable FluidStack contents, int capacity) {
                this.contents = contents;
                this.capacity = capacity;
            }

            @Nullable
            @Override
            public FluidStack getContents() {
                return contents == null ? null : contents.copy();
            }

            @Override
            public int getCapacity() {
                return capacity;
            }

            @Override
            public boolean canFill() {
                return contents == null;
            }

            @Override
            public boolean canDrain() {
                return contents != null;
            }

            @Override
            public boolean canFillFluidType(FluidStack fluidStack) {
                return canFill() && fluidStack.tag == null && FluidContainerRegistry.getFillRecipe(container, fluidStack.getFluid()) != null;
            }

            @Override
            public boolean canDrainFluidType(FluidStack fluidStack) {
                return canDrain() && fluidStack.tag == null && contents.isFluidEqual(fluidStack);
            }
        }
    }
}
