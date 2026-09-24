package com.hbm.capability;

import com.hbm.api.fluidmk2.IFluidProviderMK2;
import com.hbm.api.fluidmk2.IFluidReceiverMK2;
import com.hbm.api.fluidmk2.IFluidUserMK2;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.CapabilityContextProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static com.hbm.capability.NTMFluidCapabilityHandler.getFluidType;

public class NTMFluidHandlerWrapper implements IFluidHandler {
    private static final IFluidTankProperties[] NO_TANK_PROPS = new IFluidTankProperties[0];
    @Nullable
    private final IFluidReceiverMK2 receiver;
    @Nullable
    private final IFluidProviderMK2 provider;
    @NotNull
    private final IFluidUserMK2 user;
    @Nullable
    private final BlockPos accessor;

    /**
     * @param pos The position of the accessor. Null -> Internal access.
     */
    public NTMFluidHandlerWrapper(@NotNull TileEntity handler, @Nullable BlockPos pos) {
        if (handler instanceof IFluidProviderMK2 providerMK2) this.provider = providerMK2;
        else provider = null;
        if (handler instanceof IFluidReceiverMK2 receiverMK2) this.receiver = receiverMK2;
        else receiver = null;
        if (receiver == null && provider == null)
            throw new IllegalArgumentException("TileEntity " + handler.getClass().getName() + " must implement IFluidReceiverMK2 or IFluidProviderMK2");
        user = (IFluidUserMK2) handler;
        this.accessor = pos;
    }

    public NTMFluidHandlerWrapper(@NotNull TileEntity handler) {
        this(handler, null);
    }

    private static int clampToInt(long v) {
        if (v <= 0) return 0;
        return v > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) v;
    }

    /**
     * Whether this handler should ever claim a tank fillable via the vanilla capability. Defaults to
     * "this machine has a receiver at all" - subclasses that restrict fill() on some sides/modes (e.g.
     * TileEntityBarrel's per-face wrappers) should override this to match, otherwise external mods that
     * trust getTankProperties() over probing (unlike AE2's storage bus, which double-checks) will think
     * insertion is possible when fill() will actually always reject it.
     */
    protected boolean canFillExternally() {
        return receiver != null;
    }

    /** Same idea as {@link #canFillExternally()}, but for drain()/extraction. */
    protected boolean canDrainExternally() {
        return provider != null;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        if (accessor != null) {
            var prev = CapabilityContextProvider.pushPos(accessor);
            try {
                return buildTankProperties();
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return buildTankProperties();
    }

    private IFluidTankProperties[] buildTankProperties() {
        boolean canFill = canFillExternally();
        boolean canDrain = canDrainExternally();
        ArrayList<IFluidTankProperties> properties = new ArrayList<>();
        for (FluidTankNTM tank : user.getAllTanks()) {
            for (IFluidTankProperties base : tank.getTankProperties()) {
                properties.add(new FluidTankProperties(base.getContents(), base.getCapacity(), canFill, canDrain));
            }
        }
        return properties.toArray(NO_TANK_PROPS);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0 || receiver == null) return 0;
        if (accessor != null) {
            var prev = CapabilityContextProvider.pushPos(accessor);
            try {
                return fillInternal(resource, doFill);
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return fillInternal(resource, doFill);
    }

    /**
     * NTM tanks match on (type, pressure) as a pair - see IFluidStandardReceiverMK2#getDemand - but a
     * plain vanilla FluidStack has no pressure field to carry that information across the capability
     * boundary. So: ask the receiver which pressures it actually has tanks for at this fluid type
     * (getReceivingPressureRange), and try each one in turn, lowest first. Every existing implementer
     * either overrides this properly (IFluidStandardReceiverMK2, scanning its own getReceivingTanks())
     * or falls back to the {0,0} default - either way this replaces the old hardcoded "always pressure
     * 0", which silently discarded every fill attempt against a pressurized tank (compressed gas outputs,
     * the Hydrotreater's hydrogen input, the Vacuum Distiller's oil input, etc.) forever.
     */
    private int fillInternal(FluidStack resource, boolean doFill) {
        FluidType type = getFluidType(resource.getFluid());
        if (type == null) return 0;
        int[] range = receiver.getReceivingPressureRange(type);
        int remaining = resource.amount;
        int filled = 0;
        for (int p = range[0]; p <= range[1] && remaining > 0; p++) {
            long demand = receiver.getDemand(type, p);
            if (demand <= 0) continue;
            int offer = Math.min(remaining, clampToInt(demand));
            if (offer <= 0) continue;
            if (doFill) {
                int remainder = (int) receiver.transferFluid(type, p, offer);
                int accepted = offer - remainder;
                filled += accepted;
                remaining -= accepted;
            } else {
                filled += offer;
                remaining -= offer;
            }
        }
        return filled;
    }

    @Override
    public @Nullable FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource == null || resource.amount <= 0 || provider == null) return null;
        if (accessor != null) {
            var prev = CapabilityContextProvider.pushPos(accessor);
            try {
                return drainInternal(resource, doDrain);
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return drainInternal(resource, doDrain);
    }

    /** Pressure-aware counterpart to fillInternal, see its javadoc. */
    @Nullable
    private FluidStack drainInternal(FluidStack resource, boolean doDrain) {
        FluidType type = getFluidType(resource.getFluid());
        if (type == null) return null;
        int[] range = provider.getProvidingPressureRange(type);
        int remaining = resource.amount;
        int drained = 0;
        for (int p = range[0]; p <= range[1] && remaining > 0; p++) {
            long available = provider.getFluidAvailable(type, p);
            if (available <= 0) continue;
            int toDrain = Math.min(remaining, clampToInt(available));
            if (toDrain <= 0) continue;
            if (doDrain) provider.useUpFluid(type, p, toDrain);
            drained += toDrain;
            remaining -= toDrain;
        }
        if (drained <= 0) return null;
        FluidStack out = resource.copy();
        out.amount = drained;
        return out;
    }

    @Override
    public @Nullable FluidStack drain(int maxDrain, boolean doDrain) {
        if (maxDrain <= 0 || provider == null) return null;
        if (accessor != null) {
            var prev = CapabilityContextProvider.pushPos(accessor);
            try {
                return drainInternal(maxDrain, doDrain);
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return drainInternal(maxDrain, doDrain);
    }

    @Nullable
    private FluidStack drainInternal(int maxDrain, boolean doDrain) {
        for (FluidTankNTM tank : provider.getAllTanks()) {
            FluidType type = tank.getTankType();
            int pressure = tank.getPressure(); // this IS the actual tank, so no guessing needed here
            long available = provider.getFluidAvailable(type, pressure);
            if (available <= 0) continue;
            int toDrain = Math.min(maxDrain, clampToInt(available));
            if (toDrain <= 0) continue;
            FluidStack exemplar = tank.drain(toDrain, false);
            if (exemplar == null || exemplar.getFluid() == null) continue;
            exemplar.amount = toDrain;
            if (doDrain) provider.useUpFluid(type, pressure, toDrain);
            return exemplar;
        }
        return null;
    }
}
