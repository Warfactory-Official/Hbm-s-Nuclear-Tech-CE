package com.hbm.integration.ae2.tileentity;

import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import com.hbm.tileentity.TileEntityProxyCombo;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.util.EnumFacing;

/**
 * AE2-only variant of {@link TileEntityProxyCombo}, swapped in by the AE2-integrated machines'
 * Block#createNewTileEntity for their non-core multiblock positions (metas 6-11), only when AE2
 * is loaded - see NTMCraftingMachineFactory for why this MUST go through reflection.
 *
 * This exists because the base TileEntityProxyCombo forwards getCapability/hasCapability (and
 * therefore plain item insertion) to the real core tile entity, but "te instanceof ICraftingMachine"
 * on the *proxy* object itself is always false regardless of what the core is - Java's instanceof
 * checks the object's own runtime type, it doesn't see through the delegation. Since a multiblock
 * machine exposes far more proxy-covered faces than actual-core faces, an ME Interface placed
 * against "the machine" (as opposed to the one specific core block) would never reach pushPattern
 * at all: it'd silently fall back to the plain-insert path forever, which only works once a recipe
 * is already selected manually - exactly the symptom this class fixes.
 */
public class TileEntityProxyComboAE2 extends TileEntityProxyCombo implements ICraftingMachine {

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table, EnumFacing ejectionDirection) {
        Object core = getCoreObject();
        return core instanceof ICraftingMachine && ((ICraftingMachine) core).pushPattern(patternDetails, table, ejectionDirection);
    }

    @Override
    public boolean acceptsPlans() {
        Object core = getCoreObject();
        return core instanceof ICraftingMachine && ((ICraftingMachine) core).acceptsPlans();
    }
}
