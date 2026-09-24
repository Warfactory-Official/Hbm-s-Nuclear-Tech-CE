package com.hbm.integration.ae2;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.modules.machine.ModuleMachineBase;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Bridges AE2's crafting CPU system (appeng.api.implementations.tiles.ICraftingMachine) to NTM's
 * {@link ModuleMachineBase}-family machines: Assembly Machine, Chemical Plant, Precision Assembly,
 * Rock Mill, Fusion Reactor, PUREX and Plasma Forge.
 *
 * These machines gate item insertion behind a single "currently selected recipe" field
 * ({@link ModuleMachineBase#recipe}), normally only ever written by a player clicking the GUI's
 * recipe-select arrows (see the various TileEntity#receiveControl implementations). A plain item
 * push (Import Bus, hopper, etc) can't set that field, so ModuleMachineBase#isItemValid rejects it
 * even when it's carrying exactly the right ingredients for some recipe. This helper does what the
 * GUI does: find the recipe the incoming pattern actually represents, select it, then hand-place the
 * ingredients directly into the machine's inventory - the same trust level the GUI/container code
 * already operates at, bypassing the capability-level insertItem validation that only guards against
 * the *currently selected* recipe.
 *
 * AE2's ICraftingPatternDetails format has no concept of a fluid ingredient at all - confirmed against
 * this AE2 fork's actual source: every class in the autocrafting subsystem (CraftingJob, CraftingCPUCluster,
 * CraftingLink, PatternHelper, the Pattern Encoding Terminal itself) is IAEItemStack-only, end to end -
 * this isn't a gap in NTM's integration, it's a real limitation of vanilla AE2 (the dedicated "AE2 Fluid
 * Crafting" addon fills this gap upstream, but isn't present here). So for Chemical Plant/Fusion Reactor/
 * PUREX/Plasma Forge recipes that also need fluid input, there are two independent ways the fluid can
 * arrive, and this bridge supports both at once rather than requiring one:
 *
 * 1. The standard AE2 way, matching how GT-style packs actually do this: a keep-stocked ME Fluid
 *    Interface sitting on the machine's fluid-connected face, kept topped off by the network completely
 *    independently of any crafting job. The CPU only ever orders the item side; pushPattern() below never
 *    even looks for the fluid, it just selects the recipe and places the items, trusting the tank to
 *    already be full (or fill soon) via the machine's own normal receive/subscribe logic.
 * 2. An NTM-specific convenience: represent the fluid as a real, storable FILLED FLUID CONTAINER item in
 *    the pattern itself (NTM's own {@link FluidContainerRegistry} already auto-registers a "Fluid Tank
 *    (Full)"/"Fluid Barrel (Full)" item for essentially every fluid in the game) - e.g. N "Fluid Tank
 *    (Full): Water" items where N * 1000mB exactly equals the recipe's water requirement. If present in
 *    the pattern, these get consumed and poured directly into the matching input tank at push time,
 *    guaranteeing the fluid arrives atomically with the items instead of depending on a separately-topped
 *    tank. The resulting empty containers are discarded rather than returned (ModuleMachineBase has no
 *    TileEntity/World reference to drop or re-insert them safely - a real limitation, not an oversight).
 *
 * 3. Direct support for the "AE2 Fluid Crafting" addon's own workaround for the same AE2 limitation:
 *    it represents a fluid ingredient as a single-item NBT "packet" (registry name
 *    ae2fc:fluid_packet) wrapping a real {@code net.minecraftforge.fluids.FluidStack}, so it can
 *    travel through a plain item-only Pattern Terminal/ME network. Normally you'd need the addon's
 *    own physical "Fluid Packet Decoder" block in the loop to turn one back into real fluid before a
 *    machine can use it. Requiring that extra block just to autocraft a Chemical Plant/PUREX/etc
 *    recipe would be a needless piece of infrastructure, so {@link #tryDecodeAe2fcPacket} decodes a
 *    packet directly instead. This has zero compile-time dependency on the addon - no imports of its
 *    classes, just a registry-name string check plus Forge's own public
 *    {@code FluidStack.loadFluidStackFromNBT} - so it's inert (never matches anything) if the addon
 *    isn't installed, same spirit as the AE2-presence gating this whole package already relies on.
 *
 * Options 2 and 3 are both opportunistic, never required: if a pattern doesn't carry a matching
 * container or packet for some fluid input, that's not treated as a mismatch - the recipe still
 * matches and pushes on the strength of its item ingredients alone, falling back to option 1's
 * behavior for that fluid.
 */
public class NTMCraftingMachineHelper {

    private static final String AE2FC_FLUID_PACKET_NAMESPACE = "ae2fc";
    private static final String AE2FC_FLUID_PACKET_PATH = "fluid_packet";

    private NTMCraftingMachineHelper() { }

    /** Refuses new plans while the machine is mid-craft or its input slots are already occupied. */
    public static boolean acceptsPlans(ModuleMachineBase module) {
        if (module.progress > 0D) return false;
        for (int slot : module.inputSlots) {
            if (!module.inventory.getStackInSlot(slot).isEmpty()) return false;
        }
        return true;
    }

    public static boolean pushPattern(ModuleMachineBase module, ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        if (!acceptsPlans(module)) return false;

        GenericRecipe matched = findMatchingRecipe(module, patternDetails);
        if (matched == null) return false;
        if (matched.inputItem == null || matched.inputItem.length == 0) return false;
        if (matched.inputItem.length > module.inputSlots.length) return false; // shouldn't happen, but don't risk voiding items
        if (matched.inputFluid != null && matched.inputFluid.length > module.inputTanks.length) return false;

        if (module.outputSlots != null) {
            for (int slot : module.outputSlots) {
                if (!module.inventory.getStackInSlot(slot).isEmpty()) return false;
            }
        }

        // pull the actual ingredient stacks the network supplied out of the crafting table
        List<ItemStack> pool = new ArrayList<>();
        for (int i = 0; i < table.getSizeInventory(); i++) {
            ItemStack stack = table.getStackInSlot(i);
            if (!stack.isEmpty()) pool.add(stack.copy());
        }

        ItemStack[] placement = new ItemStack[matched.inputItem.length];
        for (int i = 0; i < matched.inputItem.length; i++) {
            ItemStack found = extractFromPool(pool, matched.inputItem[i]);
            if (found == null) return false; // network didn't actually supply what this recipe needs
            placement[i] = found;
        }

        // fluid requirements are OPPORTUNISTICALLY represented in the same pool, either as filled
        // fluid-container items (option 2) or an ae2fc fluid packet (option 3) - see the class javadoc
        // for why neither is ever a hard requirement. Whichever is found gets poured in immediately;
        // finding neither just means that fluid is expected to already be arriving (or arrive soon) via
        // a separately-topped tank instead (option 1). fluidToAdd holds the amount to add per input
        // tank, 0 meaning "nothing opportunistic found for this one".
        int[] fluidToAdd = null;
        if (matched.inputFluid != null && matched.inputFluid.length > 0) {
            fluidToAdd = new int[matched.inputFluid.length];
            for (int i = 0; i < matched.inputFluid.length; i++) {
                com.hbm.inventory.fluid.FluidStack want = matched.inputFluid[i];

                ItemStack container = extractContainerFromPool(pool, want);
                if (container != null) {
                    fluidToAdd[i] = want.fill;
                    continue;
                }

                net.minecraftforge.fluids.FluidStack packetFluid = extractAe2fcPacketFromPool(pool, want.type);
                if (packetFluid != null) {
                    fluidToAdd[i] = packetFluid.amount;
                }
            }
        }

        module.setRecipe(matched.getInternalName(), false);
        module.setupTanks(matched);
        for (int i = 0; i < placement.length; i++) {
            module.inventory.setStackInSlot(module.inputSlots[i], placement[i]);
        }
        if (fluidToAdd != null) {
            for (int i = 0; i < fluidToAdd.length; i++) {
                if (fluidToAdd[i] <= 0) continue; // nothing opportunistic found - relying on a separately-topped tank
                com.hbm.inventory.fluid.FluidStack wantFluid = matched.inputFluid[i];
                FluidTankNTM tank = module.inputTanks[i];
                tank.conform(wantFluid); // no-op if already the right type/pressure, otherwise zeroes fill first
                tank.setFill(tank.getFill() + fluidToAdd[i]);
                // the emptied container/packet (e.g. "Fluid Tank (Empty)") is intentionally discarded
                // here - see the class javadoc for why it can't be handed back safely from this context.
            }
        }
        module.markDirty = true;
        return true;
    }

    /** Finds the GenericRecipe this pattern was encoded from, matching both inputs and primary output. */
    @SuppressWarnings("unchecked")
    private static GenericRecipe findMatchingRecipe(ModuleMachineBase module, ICraftingPatternDetails patternDetails) {
        IAEItemStack[] condensedInputs = patternDetails.getCondensedInputs();
        IAEItemStack primaryOutput = patternDetails.getPrimaryOutput();
        List<GenericRecipe> recipes = module.getRecipeSet().recipeOrderedList;

        recipeLoop:
        for (GenericRecipe recipe : recipes) {
            if (recipe.inputItem == null || recipe.inputItem.length == 0) continue;

            if (primaryOutput != null && recipe.outputItem != null && recipe.outputItem.length > 0) {
                ItemStack single = recipe.outputItem[0].getSingle();
                if (single != null && !single.isEmpty()
                        && (single.getItem() != primaryOutput.getItem() || single.getItemDamage() != primaryOutput.getItemDamage())) {
                    continue;
                }
            }

            List<IAEItemStack> scratch = new ArrayList<>(condensedInputs.length);
            for (IAEItemStack s : condensedInputs) if (s != null) scratch.add(s.copy());

            for (AStack want : recipe.inputItem) {
                boolean satisfied = false;
                for (IAEItemStack avail : scratch) {
                    if (avail.getStackSize() <= 0) continue;
                    if (avail.getStackSize() >= want.stacksize && want.matchesRecipe(avail.createItemStack(), true)) {
                        avail.decStackSize(want.stacksize);
                        satisfied = true;
                        break;
                    }
                }
                if (!satisfied) continue recipeLoop;
            }

            // Fluid requirements are opportunistic, not a match condition (see class javadoc) - actually
            // reserving/extracting a container happens later in pushPattern, against the real InventoryCrafting
            // pool, not here. There's nothing left to check against `recipe.inputFluid` at this point.

            return recipe;
        }
        return null;
    }

    private static ItemStack extractFromPool(List<ItemStack> pool, AStack want) {
        for (ItemStack candidate : pool) {
            if (candidate.isEmpty()) continue;
            if (want.matchesRecipe(candidate, false)) {
                return candidate.splitStack(want.stacksize);
            }
        }
        return null;
    }

    /**
     * Finds a filled fluid-container stack in the pool whose content exactly divides the requested
     * fluid amount, and splits off the exact count needed. See the class javadoc for why an exact
     * division is required rather than allowing overshoot.
     */
    private static ItemStack extractContainerFromPool(List<ItemStack> pool, com.hbm.inventory.fluid.FluidStack want) {
        for (ItemStack candidate : pool) {
            if (candidate.isEmpty()) continue;
            int contentPerItem = FluidContainerRegistry.getFluidContent(candidate, want.type);
            if (contentPerItem <= 0 || want.fill % contentPerItem != 0) continue;
            int neededCount = want.fill / contentPerItem;
            if (neededCount > 0 && candidate.getCount() >= neededCount) {
                return candidate.splitStack(neededCount);
            }
        }
        return null;
    }

    /**
     * Finds and consumes a single ae2fc:fluid_packet item in the pool whose decoded fluid matches
     * wantType, per {@link #tryDecodeAe2fcPacket}. Unlike NTM's own fluid containers, a packet isn't a
     * uniform-content stack that can be split by count - the entire wrapped amount lives in one item's
     * NBT - so exactly one matching packet is consumed whole, whatever amount it happens to carry
     * (mirroring what the addon's own Fluid Packet Decoder block would do to it).
     */
    @Nullable
    private static net.minecraftforge.fluids.FluidStack extractAe2fcPacketFromPool(List<ItemStack> pool, com.hbm.inventory.fluid.FluidType wantType) {
        for (ItemStack candidate : pool) {
            if (candidate.isEmpty()) continue;
            net.minecraftforge.fluids.FluidStack decoded = tryDecodeAe2fcPacket(candidate);
            if (decoded == null) continue;
            if (com.hbm.capability.NTMFluidCapabilityHandler.getFluidType(decoded.getFluid()) != wantType) continue;
            candidate.splitStack(candidate.getCount()); // consume the whole (always maxStackSize-1) packet
            return decoded;
        }
        return null;
    }

    /**
     * Decodes an ae2fc:fluid_packet item stack back into the real FluidStack it wraps, without any
     * compile-time dependency on the "AE2 Fluid Crafting" addon - see the class javadoc's option 3.
     * Matched by registry name alone; the NBT layout ({@code {"FluidStack": <forge FluidStack NBT>}})
     * was confirmed directly against that addon's own {@code ItemFluidPacket}/{@code FakeFluids}
     * classes. Returns null for anything that isn't a valid, non-empty packet - including when the
     * addon isn't installed at all, in which case no item will ever carry this registry name.
     */
    @Nullable
    private static net.minecraftforge.fluids.FluidStack tryDecodeAe2fcPacket(ItemStack candidate) {
        if (candidate.isEmpty() || !candidate.hasTagCompound()) return null;
        ResourceLocation name = candidate.getItem().getRegistryName();
        if (name == null || !AE2FC_FLUID_PACKET_NAMESPACE.equals(name.getNamespace()) || !AE2FC_FLUID_PACKET_PATH.equals(name.getPath())) {
            return null;
        }
        if (!candidate.getTagCompound().hasKey("FluidStack", 10)) return null; // 10 = NBT compound tag id
        net.minecraftforge.fluids.FluidStack decoded =
                net.minecraftforge.fluids.FluidStack.loadFluidStackFromNBT(candidate.getTagCompound().getCompoundTag("FluidStack"));
        return (decoded != null && decoded.amount > 0) ? decoded : null;
    }
}
