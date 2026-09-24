package com.hbm.mixin.mod.ae2;

import appeng.api.storage.data.IAEFluidStack;
import com.hbm.main.MainRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Method;

/**
 * AE2's Fluid Import Bus, when it has a filter configured, drains the target with an *untargeted*
 * drain(int, boolean) call and only checks the result against the filter afterward - so on a machine
 * with several tanks (NTM's own multi-tank machines routinely have 4-5), it can only ever see whichever
 * tank the target's own iteration happens to return first, and just gives up for the tick if that one
 * isn't what the filter wants. It never tries any of the target's *other* tanks.
 *
 * This redirects that one untargeted drain() call: if a filter is configured, try a real targeted
 * drain(FluidStack, boolean) against each configured filter fluid in turn (this is exactly what a
 * multi-tank target needs, NTM or otherwise) and return the first one that actually offers something.
 * With no filter configured, falls straight back to the original untargeted call, unchanged. Everything
 * downstream of this call in doBusWork() (the filter re-check, the real targeted drain, injecting into
 * the ME network) is untouched - it already handles "got a matching FluidStack back" correctly on its
 * own, it just never got handed one for a non-first tank before now.
 *
 * getConfig() (declared on PartSharedFluidBus, returning appeng.fluids.util.IAEFluidTank) is called via
 * reflection rather than a normal typed @Shadow: only appeng.api.* is on NTM-CE's compile classpath (the
 * "api" artifact of ae2-uel), and both PartFluidImportBus and IAEFluidTank live outside that package, so
 * there's no compile-time type available to declare a normal shadow method with. IAEFluidStack (the
 * per-slot filter entry type) IS under appeng.api.storage.data, so that one's a normal import.
 *
 * @Pseudo because AE2 is an optional dependency - this mixin is only ever applied when
 * ModMixinConfigPlugin's "ae2" case (see ModPresence.AE2) says the target class actually exists.
 */
@Pseudo
@Mixin(targets = "appeng.fluids.parts.PartFluidImportBus", remap = false)
public abstract class MixinPartFluidImportBus {

    @Redirect(
            method = "doBusWork",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fluids/capability/IFluidHandler;drain(IZ)Lnet/minecraftforge/fluids/FluidStack;"),
            remap = false
    )
    private FluidStack hbm$targetedFilterDrain(IFluidHandler fh, int maxDrain, boolean doDrain) {
        try {
            Method getConfig = this.getClass().getMethod("getConfig");
            Object config = getConfig.invoke(this);
            Method getSlots = config.getClass().getMethod("getSlots");
            Method getFluidInSlot = config.getClass().getMethod("getFluidInSlot", int.class);
            int slots = (int) getSlots.invoke(config);

            boolean filterEnabled = false;
            for (int i = 0; i < slots; i++) {
                if (getFluidInSlot.invoke(config, i) != null) {
                    filterEnabled = true;
                    break;
                }
            }
            if (!filterEnabled) {
                return fh.drain(maxDrain, doDrain);
            }

            for (int i = 0; i < slots; i++) {
                Object filterStackObj = getFluidInSlot.invoke(config, i);
                if (filterStackObj == null) continue;
                IAEFluidStack filterStack = (IAEFluidStack) filterStackObj;
                FluidStack want = new FluidStack(filterStack.getFluidStack().getFluid(), maxDrain);
                FluidStack got = fh.drain(want, doDrain);
                if (got != null && got.amount > 0) return got;
            }
            return null;
        } catch (ReflectiveOperationException e) {
            // AE2 changed its internal API out from under us - fall back to stock (broken-for-multi-tank
            // but not broken-worse) behavior instead of hard-crashing the bus.
            MainRegistry.logger.warn("AE2 Fluid Import Bus targeted-filter-drain patch failed, falling back to vanilla behavior", e);
            return fh.drain(maxDrain, doDrain);
        }
    }
}
